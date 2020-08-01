package api.syntaxerror.phpjavabridge;

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * PHP-Java-Bridge provides a TCP- or UDP-based connection between PHP (Client) and Java (Server)<br>
 * <br>
 * licensed under the Apache License 2.0:<br>
 * <br>
 * Permissions:
 * <ul>
 *  <li>Commercial use</li>
 *  <li>Modification</li>
 *  <li>Distribution</li>
 *  <li>Patent use</li>
 *  <li>Private use</li>
 * </ul>
 * 
 * Limitiations:
 * <ul>
 *  <li>Trademark use</li>
 *  <li>Liability</li>
 *  <li>Warranty</li>
 * </ul>
 * 
 * Conditions:
 * <ul>
 *  <li>License and copyright notice</li>
 *  <li>State changes</li>
 * </ul>
 * 
 * License: <a href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/LICENSE>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/LICENSE</a><br>
 * GitHub Repository: <a href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/</a><br>
 * Wiki: <a href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/wiki>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/wiki</a><br>
 * JavaDocs (for /src/java): <a href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/javadoc/>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/javadoc/</a><br>
 * 
 * @version 1.0
 * @author SyntaxError404, 2020
 */
public abstract class Bridge {

	protected int port,maxPacketLength;
	private boolean useAES;
	private byte[]password;
	protected Thread thread;
	protected Map<Integer,PacketHandler>handlers;
	protected UncaughtExceptionHandler exceptionHandler;
	
	/**@see BridgeImpl.TCP#TCP(int, boolean, String, int)
	 * @see BridgeImpl.UDP#UDP(int, boolean, String, int)
	 */
	Bridge(int port,boolean useAES,String password,int maxPacketLength) {
		handlers=new HashMap<>();
		
		this.port=port;
		
		if(useAES) {
			if(password==null)
				throw new NullPointerException("AES requires a password, got null instead");
			
			this.useAES=true;
			
			try {
				MessageDigest md=MessageDigest.getInstance("SHA3-256");
				this.password=md.digest(password.getBytes(StandardCharsets.UTF_8));
			} catch(NoSuchAlgorithmException e) {
				throw new RuntimeException("Couldn't find SHA3-256 algorithm"); 
			}
			
		}
		
		if(maxPacketLength<6)
			throw new IllegalArgumentException("maxPacketLength must be greater than or equal to 6"); 
		
		this.maxPacketLength=maxPacketLength;
		
		thread=new Thread(()->{});
		exceptionHandler=(t,e)->{};
	}
	
	public int getPort() {
		return port;
	}

	/**
	 * Starts the Server-Thread.
	 */
	public void start() {
		thread.start();
	}

	/**
	 * Stops the Server Thread.<br>
	 * You cannot call {@link #start()} afterwards.
	 * 
	 * @throws InterruptedException thrown by {@link Thread#join()}
	 */
	public void stop()throws InterruptedException {
		thread.join();
	}

	/**
	 * If a {@link Packet} couldn't be handled (because there was no {@link PacketHandler} set via {@link #setPacketHandler(PacketHandler, int...)}), this handler is called.<br>
	 * <br>
	 * If there is no default {@link PacketHandler} either, a {@link UnsupportedOperationException} is thrown.
	 * 
	 * @param handler the new default {@link PacketHandler}
	 */
	public void setDefaultPacketHandler(PacketHandler handler) {
		handlers.put(-1,handler);
	}

	/**
	 * Defines a {@link PacketHandler} for specific Packet-IDs.<br>
	 * <br>
	 * If there is no {@link PacketHandler} defined for a Packet-ID and no default {@link Packet} is set via {@link #setDefaultPacketHandler(PacketHandler)}, a
	 * {@link UnsupportedOperationException} is thrown if such a {@link Packet} is received
	 * 
	 * @param handler the {@link PacketHandler}
	 * @param pids the Packet-IDs where this {@link PacketHandler} should be used
	 */
	public void setPacketHandler(PacketHandler handler,int...pids) {
		for(int pid:pids)
			if(pid<0||pid>255)
				throw new IndexOutOfBoundsException("Invalid Packet-ID: " +pid);
			else handlers.put(pid,handler);
	}
	
	/**
	 * A {@link UncaughtExceptionHandler} prevents the {@link Bridge} from stopping if an Exception occured. 
	 * 
	 * @param exceptionHandler the {@link UncaughtExceptionHandler}
	 */
	public void setExceptionHandler(UncaughtExceptionHandler exceptionHandler) {
		this.exceptionHandler=exceptionHandler;
	}
	
	/**
	 * internal use only
	 * 
	 * AES-256-CBC encryption with SHA3-256 hashed password
	 */
	protected final byte[]encrypt(byte[]plainText) {
		if(!useAES)
			return Base64.getEncoder().encode(plainText);
		
		byte[]iv=new byte[16],encrypted;
		new SecureRandom().nextBytes(iv);
		
		try {
			Cipher cipher=Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(password,"AES"),new IvParameterSpec(iv));
			encrypted=cipher.doFinal(plainText);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		byte[]full=new byte[16+encrypted.length];
		System.arraycopy(iv,0,full,0,16);
		System.arraycopy(encrypted,0,full,16,encrypted.length);
		
		return Base64.getEncoder().encode(encrypted);
	}
	/**
	 * internal use only
	 * 
	 * AES-256-CBC decryption with SHA3-256 hashed password
	 */
	protected final byte[]decrypt(byte[]cipherText) {
		cipherText=Base64.getDecoder().decode(cipherText);
		
		if(!useAES)
			return cipherText;
		
		byte[]iv=Arrays.copyOf(cipherText,16);
		cipherText=Arrays.copyOfRange(cipherText,16,cipherText.length);
		
		try {
			Cipher cipher=Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(password,"AES"),new IvParameterSpec(iv));
			return cipher.doFinal(cipherText);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// STATIC METHODS

	/**
	 * Creates a new {@link BridgeImpl.TCP TCP-Bridge}
	 * 
	 * @see {@link #newTCP(int, boolean, String, int) Bridge#newTCP}(8998, false, null, 65535) 
	 * 
	 * @return TCP-based {@link Bridge}
	 */
	public static Bridge newTCP() {
		return newTCP(8998,false,null,65535);
	}
	/**
	 * Creates a new {@link BridgeImpl.TCP TCP-Bridge}
	 * 
	 * @param port the port to be used
	 * 
	 * @see Bridge#Bridge(int, boolean, String, int)
	 * @see BridgeImpl.TCP#TCP(int, boolean, String, int)
	 * 
	 * @return TCP-based {@link Bridge} 
	 */
	public static Bridge newTCP(int port) {
		return newTCP(port,false,null,65535);
	}
	/**
	 * Creates a new {@link BridgeImpl.TCP TCP-Bridge}
	 * 
	 * @param port the port to be used
	 * @param maxPacketLength the max. length of a single {@link Packet}
	 * 
	 * @see Bridge#Bridge(int, boolean, String, int)
	 * @see BridgeImpl.TCP#TCP(int, boolean, String, int)
	 * 
	 * @return TCP-based {@link Bridge}
	 */
	public static Bridge newTCP(int port,int maxPacketLength) {
		return newTCP(port,false,null,maxPacketLength);
	}
	/**
	 * Creates a new {@link BridgeImpl.TCP TCP-Bridge}
	 * 
	 * @param port the port to be used
	 * @param useAES whether or not AES-256-CBC should be used. requires {@code password}
	 * @param password the password to be used. Required by AES
	 * 
	 * @see Bridge#Bridge(int, boolean, String, int)
	 * @see BridgeImpl.TCP#TCP(int, boolean, String, int)
	 * 
	 * @return TCP-based {@link Bridge}
	 */
	public static Bridge newTCP(int port,boolean useAES,String password) {
		return newTCP(port,useAES,password,65535);
	}
	/**
	 * Creates a new {@link BridgeImpl.TCP TCP-Bridge}
	 * 
	 * @param port the port to be used
	 * @param useAES whether or not AES-256-CBC should be used. requires {@code password}
	 * @param password the password to be used. Required by AES
	 * @param maxPacketLength the max. length of a single {@link Packet}
	 * 
	 * @see Bridge#Bridge(int, boolean, String, int)
	 * @see BridgeImpl.TCP#TCP(int, boolean, String, int)
	 * 
	 * @return TCP-based {@link Bridge}
	 */
	public static Bridge newTCP(int port,boolean useAES,String password,int maxPacketLength) {
		return new BridgeImpl.TCP(port,useAES,password,maxPacketLength);
	}

	/**
	 * Creates a new {@link BridgeImpl.UDP UDP-Bridge}
	 * 
	 * @see {@link #newUDP(int, boolean, String, int) Bridge#newUDP}(8998, false, null, 65535) 
	 * 
	 * @return UDP-based {@link Bridge}
	 */
	public static Bridge newUDP() {
		return newUDP(8998,false,null,65535);
	}
	/**
	 * Creates a new {@link BridgeImpl.UDP UDP-Bridge}
	 * 
	 * @param port the port to be used
	 * 
	 * @see Bridge#Bridge(int, boolean, String, int)
	 * @see BridgeImpl.UDP#UDP(int, boolean, String, int)
	 * 
	 * @return UDP-based {@link Bridge} 
	 */
	public static Bridge newUDP(int port) {
		return newUDP(port,false,null,65535);
	}
	/**
	 * Creates a new {@link BridgeImpl.UDP UDP-Bridge}
	 * 
	 * @param port the port to be used
	 * @param maxPacketLength the max. length of a single {@link Packet}
	 * 
	 * @see Bridge#Bridge(int, boolean, String, int)
	 * @see BridgeImpl.UDP#UDP(int, boolean, String, int)
	 * 
	 * @return UDP-based {@link Bridge}
	 */
	public static Bridge newUDP(int port,int maxPacketLength) {
		return newUDP(port,false,null,maxPacketLength);
	}
	/**
	 * Creates a new {@link BridgeImpl.UDP UDP-Bridge}
	 * 
	 * @param port the port to be used
	 * @param useAES whether or not AES-256-CBC should be used. requires {@code password}
	 * @param password the password to be used. Required by AES
	 * 
	 * @see Bridge#Bridge(int, boolean, String, int)
	 * @see BridgeImpl.UDP#UDP(int, boolean, String, int)
	 * 
	 * @return UDP-based {@link Bridge}
	 */
	public static Bridge newUDP(int port,boolean useAES,String password) {
		return newUDP(port,useAES,password,65535);
	}
	/**
	 * Creates a new {@link BridgeImpl.UDP UDP-Bridge}
	 * 
	 * @param port the port to be used
	 * @param useAES whether or not AES-256-CBC should be used. requires {@code password}
	 * @param password the password to be used. Required by AES
	 * @param maxPacketLength the max. length of a single {@link Packet}
	 * 
	 * @see Bridge#Bridge(int, boolean, String, int)
	 * @see BridgeImpl.UDP#UDP(int, boolean, String, int)
	 * 
	 * @return UDP-based {@link Bridge}
	 */
	public static Bridge newUDP(int port,boolean useAES,String password,int maxPacketLength) {
		return new BridgeImpl.UDP(port,useAES,password,maxPacketLength);
	}
	
}
