package api.syntaxerror.phpjavabridge;

import java.net.SocketAddress;

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
 * @version 1.0
 * @author SyntaxError404, 2020
 */
public interface PacketHandler {
	
	/**
	 * Handles an incoming {@link Packet} and responds with a new one.
	 * 
	 * @param sender contains the sender's {@link java.net.InetAddress} 
	 * @param incoming the incoming {@link Packet} received by the server 
	 * @return the answer to the {@code incoming} {@link Packet}
	 */
	Packet handle(SocketAddress sender,Packet incoming);
	
}
