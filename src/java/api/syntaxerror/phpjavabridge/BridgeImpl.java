package api.syntaxerror.phpjavabridge;

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
 * <hr>
 * 
 * This class contains the implementations for {@link TCP} and {@link UDP}
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
			thread=new ServerThread(this);
		}
		
		static class ServerThread extends Thread {
			
			private Bridge bridge;
			
			public ServerThread(Bridge bridge) {
				this.bridge=bridge;
			}
			
			public void run() {
				while(true)
					try {
						
					} catch(Exception e) {
						e=new SocketFailureException(e);
					}
			}
			
		}
		
		static class ClientHandler extends Thread {
			
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
