package api.syntaxerror.phpjavabridge;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

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
				try(InputStream in=client.getInputStream();
					OutputStream out=client.getOutputStream();
					Packet incoming=new Packet(in.read()&0xFF)) {
					
					incoming.littleEndian=in.read()!=0;
					int length=	(in.read()>>>24)|
								(in.read()>>>16)|
								(in.read()>>>8)|
								in.read();
					
					byte[]buf=new byte[length];
					
					int bytesRead=in.read(buf);
					
					if(bytesRead+6>bridge.maxPacketLength)
						throw new MalformedRequestException("Incoming Packet too large: "+(bytesRead+6)+" (max. "+bridge.maxPacketLength+")");
						
					if(bytesRead!=length)
						throw new MalformedRequestException("Expected "+length+" bytes, got "+bytesRead+" instead");
					
					buf=bridge.decrypt(buf);
					
					incoming.validate(buf);
					incoming.data=buf;
					incoming.size=buf.length;
					
					PacketHandler handler=bridge.handlers.getOrDefault(incoming.getPacketID(),bridge.handlers.getOrDefault(-1,null));
					
					if(handler==null)
						throw new UnsupportedOperationException("Cannot process Packet: No handler for ID #"+incoming.getPacketID()+" found");
					
					Packet outgoing=handler.handle(client.getRemoteSocketAddress(),incoming);
					
					byte[]data=bridge.encrypt(Arrays.copyOf(outgoing.data,outgoing.size()));
					
					out.write(outgoing.getPacketID()&0xFF);
					out.write(0);
					
					if(data.length+6>bridge.maxPacketLength)
						throw new MalformedRequestException("Outgoing Packet too large: "+(data.length+6)+" (max. "+bridge.maxPacketLength+")");
					
					int len=data.length;
					
					out.write((len>>24)&0xFF);
					out.write((len>>16)&0xFF);
					out.write((len>>8)&0xFF);
					out.write(len&0xFF);
					
					out.write(data);
					out.flush();
				} catch(Exception e) {
					if(bridge.exceptionHandler!=null)
						bridge.exceptionHandler.uncaughtException(Thread.currentThread(),e);
					
					try {
						client.close();
					} catch(Exception e2) {}
					
					throw new RuntimeException(e);
				} finally {
					System.gc();
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

		protected DatagramSocket socket;
		
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
			
			try {
				socket=new DatagramSocket(port,InetAddress.getLocalHost());
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}

			thread=new ServerThread(this);
		}
		
		static class ServerThread extends Thread {
			
			private UDP bridge;
			
			public ServerThread(UDP bridge) {
				this.bridge=bridge;
				setName("PHP-Java [UDP] @"+bridge.socket.getLocalSocketAddress());
			}
			
			public void run() {
				while(true)
					try {
						System.out.println("Listening @ "+bridge.socket.getLocalSocketAddress());
						DatagramPacket client=new DatagramPacket(new byte[bridge.maxPacketLength],bridge.maxPacketLength);
						bridge.socket.receive(client);
						
						System.out.println("Client: "+client.getSocketAddress());
						
						new ClientHandler(bridge,client).start();
					} catch(Exception e) {
						e=new SocketFailureException(e);
					}
			}
			
		}
		
		static class ClientHandler extends Thread {
			
			private UDP bridge;
			private DatagramPacket client;
			
			public ClientHandler(UDP bridge,DatagramPacket client) {
				this.bridge=bridge;
				this.client=client;
				setName("ClientHandler PHP-Java [UDP] @"+bridge.socket.getLocalSocketAddress());
			}
			
			@Override
			public void run() {
				try(InputStream in=new ByteArrayInputStream(client.getData());
					Packet incoming=new Packet(in.read()&0xFF)) {
					
					incoming.littleEndian=in.read()!=0;
					int length=	(in.read()>>>24)|
								(in.read()>>>16)|
								(in.read()>>>8)|
								in.read();
					
					byte[]buf=new byte[length];
					
					int bytesRead=in.read(buf);
					
					if(bytesRead+6>bridge.maxPacketLength)
						throw new MalformedRequestException("Incoming Packet too large: "+(bytesRead+6)+" (max. "+bridge.maxPacketLength+")");
						
					if(bytesRead!=length)
						throw new MalformedRequestException("Expected "+length+" bytes, got "+bytesRead+" instead");
					
					buf=bridge.decrypt(buf);
					
					incoming.validate(buf);
					incoming.data=buf;
					incoming.size=buf.length;
					
					PacketHandler handler=bridge.handlers.getOrDefault(incoming.getPacketID(),bridge.handlers.getOrDefault(-1,null));
					
					if(handler==null)
						throw new UnsupportedOperationException("Cannot process Packet: No handler for ID #"+incoming.getPacketID()+" found");
					
					Packet outgoing=handler.handle(client.getSocketAddress(),incoming);
					
					byte[]data=bridge.encrypt(Arrays.copyOf(outgoing.data,outgoing.size()));
					
					int len=data.length;
					
					if(data.length+6>bridge.maxPacketLength)
						throw new MalformedRequestException("Outgoing Packet too large: "+(data.length+6)+" (max. "+bridge.maxPacketLength+")");
					
					byte[]finalData=new byte[len+6];
					
					finalData[0]=(byte)(outgoing.getPacketID()&0xFF);
					
					finalData[2]=(byte)((len>>24)&0xFF);
					finalData[3]=(byte)((len>>16)&0xFF);
					finalData[4]=(byte)((len>>8)&0xFF);
					finalData[5]=(byte)(len&0xFF);
					
					System.arraycopy(data,0,finalData,6,len);
					data=null;
					
					DatagramPacket packet=new DatagramPacket(finalData,finalData.length,client.getSocketAddress());
					bridge.socket.send(packet);
				} catch(Exception e) {
					if(bridge.exceptionHandler!=null)
						bridge.exceptionHandler.uncaughtException(Thread.currentThread(),e);
					
					throw new RuntimeException(e);
				} finally {
					System.gc();
				}
			}
			
		}
		
	}
	
}
