package api.syntaxerror.phpjavabridge;

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

public abstract class Bridge {

	protected int port,maxPacketLength;
	private boolean useAES;
	private byte[]password;
	protected Thread thread;
	protected Map<Integer,PacketHandler>handlers;
	
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
		
		if(this.maxPacketLength<6)
			throw new IllegalArgumentException("maxPacketLength must be greater than or equal to 6"); 
		
		this.maxPacketLength=maxPacketLength;
	}
	
	public int getPort() {
		return port;
	}

	public void start() {
		thread.start();
	}

	public void stop()throws InterruptedException {
		thread.join();
	}

	public void setDefaultPacketHandler(PacketHandler handler) {
		handlers.put(-1,handler);
	}

	public void setPacketHandler(PacketHandler handler,int...pids) {
		for(int pid:pids)
			if(pid<0||pid>255)
				throw new IndexOutOfBoundsException("Invalid Packet-ID: " +pid);
			else handlers.put(pid,handler);
	}
	
	protected final byte[]encrypt(byte[]plainText) {
		if(!useAES)
			return Base64.getEncoder().encode(plainText);
		
		byte[]iv=new byte[16],encrypted;
		new SecureRandom().nextBytes(iv);
		
		try {
			Cipher cipher=Cipher.getInstance("AES-256-CBC");
			cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(password,"AES"),new IvParameterSpec(iv));
			encrypted=cipher.doFinal(plainText);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		byte[]full=new byte[16+encrypted.length];
		System.arraycopy(iv,0,full,0,16);
		System.arraycopy(encrypted,0,full,16,encrypted.length);
		
		return Base64.getEncoder().encode(full);
	}
	protected final byte[]decrypt(byte[]cipherText) {
		cipherText=Base64.getDecoder().decode(cipherText);
		
		if(!useAES)
			return cipherText;
		
		byte[]iv=Arrays.copyOf(cipherText,16);
		cipherText=Arrays.copyOfRange(iv,16,cipherText.length);
		
		try {
			Cipher cipher=Cipher.getInstance("AES-256-CBC");
			cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(password,"AES"),new IvParameterSpec(iv));
			return cipher.doFinal(cipherText);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// STATIC METHODS
	
	public static Bridge newTCP(int port,boolean useAES,String password,int maxPacketLength) {
		return new Bridges.TCP(port,useAES,password,maxPacketLength);
	}
	public static Bridge newTCP(int port) {
		return newTCP(port,false,null,65535);
	}
	public static Bridge newTCP(int port,int maxPacketLength) {
		return newTCP(port,false,null,maxPacketLength);
	}
	public static Bridge newTCP(int port,boolean useAES,String password) {
		return newTCP(port,useAES,password,65535);
	}
	
	public static Bridge newUDP(int port,boolean useAES,String password,int maxPacketLength) {
		return new Bridges.UDP(port,useAES,password,maxPacketLength);
	}
	public static Bridge newUDP(int port) {
		return newUDP(port,false,null,65535);
	}
	public static Bridge newUDP(int port,int maxPacketLength) {
		return newUDP(port,false,null,maxPacketLength);
	}
	public static Bridge newUDP(int port,boolean useAES,String password) {
		return newUDP(port,useAES,password,65535);
	}
	
}
