package api.syntaxerror.phpjavabridge;

import java.io.IOException;

/**
 * PHP-Java-Bridge provides a TCP- or UDP-based connection between PHP (Client)
 * and Java (Server)<br>
 * <br>
 * licensed under the Apache License 2.0:<br>
 * <br>
 * Permissions:
 * <ul>
 * <li>Commercial use</li>
 * <li>Modification</li>
 * <li>Distribution</li>
 * <li>Patent use</li>
 * <li>Private use</li>
 * </ul>
 * 
 * Limitiations:
 * <ul>
 * <li>Trademark use</li>
 * <li>Liability</li>
 * <li>Warranty</li>
 * </ul>
 * 
 * Conditions:
 * <ul>
 * <li>License and copyright notice</li>
 * <li>State changes</li>
 * </ul>
 * 
 * License: <a href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/LICENSE>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/LICENSE</a><br>
 * GitHub Repository: <a href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/</a><br>
 * Wiki: <a href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/wiki>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/wiki</a><br>
 *
 * <hr>
 * 
 * <code>SocketFailureException</code> is thrown when a {@link Packet} couldn't be parsed
 *
 * @author SyntaxError404, 2020
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MalformedRequestException extends IOException {
	
	public MalformedRequestException() {
		super();
	}
	public MalformedRequestException(String message) {
		super(message);
	}
	public MalformedRequestException(Throwable cause) {
		super(cause);
	}
	public MalformedRequestException(String message,Throwable cause) {
		super(message,cause);
	}
	
}
