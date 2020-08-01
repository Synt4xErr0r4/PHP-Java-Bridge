package api.syntaxerror.phpjavabridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
 * JavaDocs (for /src/java): <a href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/javadoc/index.html>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/javadoc/index.html</a><br>
 * 
 * <hr>
 * 
 * This class contains the {@link Bridge}-implementations for {@link TCP} and {@link UDP}
 * 
 * @version 1.0
 * @author SyntaxError404, 2020
 */
public class BridgeImpl {
	
	private BridgeImpl() {}

	/**
	 * The <b>Transmission Control Protocol</b> (TCP) is a connection-based data transmission protocol<br>
	 * <br>
	 * Compared to {@link UDP}:
	 * <ul>
	 * 	<li>reliable data transmission</li>
	 * 	<li>slower than UDP</li>
	 * </ul>
	 * <br>
	 * see the <a href=https://en.wikipedia.org/wiki/Transmission_Control_Protocol>Wikipedia Article</a>
	 */
	public static class TCP extends Bridge {
		
		protected ServerSocket socket;

		/**
		 * Instantiates a new TCP-based PHP-Java-Bridge
		 * 
		 * @param port the port in range [0;65535]
		 * @param useAES whether or not AES-256-CBC is used (requres {@code password})
		 * @param password the password required for AES encryption
		 * @param maxPacketLength the max. packet length. default 65535
		 */
		public TCP(int port,boolean useAES,String password,int maxPacketLength) {
			super(port,useAES,password,maxPacketLength);
			
			try {
				socket=new ServerSocket(port,50,InetAddress.getLocalHost());
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}

			thread=new ServerThread(this);
		}
		
		static class ServerThread extends Thread {
			
			private TCP bridge;
			
			public ServerThread(TCP bridge) {
				this.bridge=bridge;
				setName("PHP-Java [TCP] @"+bridge.socket.getLocalSocketAddress());
			}
			
			public void run() {
				while(true)
					try {
						new ClientHandler(bridge,bridge.socket.accept()).start();
					} catch(Exception e) {
						e=new SocketFailureException(e);
					}
			}
			
		}
		
		static class ClientHandler extends Thread {
			
			private TCP bridge;
			private Socket client;
			
			public ClientHandler(TCP bridge,Socket client) {
				this.bridge=bridge;
				this.client=client;
				setName("ClientHandler PHP-Java [TCP] @"+bridge.socket.getLocalSocketAddress());
			}
			
			@Override
			public void run() {
				try {
					InputStream in=client.getInputStream();
					OutputStream out=client.getOutputStream();
					
					Packet incoming=new Packet(in.read()&0xFF);
					incoming.littleEndian=in.read()!=0;
					int length=	(in.read()>>>24)|
								(in.read()>>>16)|
								(in.read()>>>8)|
								in.read();
					
					byte[]buf=new byte[length];
					
					int bytesRead=in.read(buf);
					
					if(bytesRead!=length)
						throw new MalformedRequestException("Expected "+length+" bytes, got "+bytesRead+" instead");
					
					incoming.data=buf;
					incoming.validate();
					
				} catch(Exception e) {
					if(bridge.exceptionHandler!=null)
						bridge.exceptionHandler.uncaughtException(Thread.currentThread(),e);
					
					throw new RuntimeException(e);
				}
			}
			
		}
		
	}

	/**
	 * The <b>User Datagram Protocol</b> (UDP) is a connectionless data transmission protocol<br>
	 * <br>
	 * Compared to {@link TCP}:
	 * <ul>
	 * 	<li>faster than TCP</li>
	 * 	<li>unreliable</li>
	 * </ul>
	 * <br>
	 * see the <a href=https://en.wikipedia.org/wiki/Transmission_Control_Protocol>Wikipedia Article</a>
	 */
	public static class UDP extends Bridge {

		/**
		 * Instantiates a new UDP-based PHP-Java-Bridge
		 * 
		 * @param port the port in range [0;65535]
		 * @param useAES whether or not AES-256-CBC is used (requres {@code password})
		 * @param password the password required for AES encryption
		 * @param maxPacketLength the max. packet length. default 65535
		 */
		public UDP(int port,boolean useAES,String password,int maxPacketLength) {
			super(port,useAES,password,maxPacketLength);
		}
		
	}
	
}
