package api.syntaxerror.phpjavabridge;

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
 * License: <a
 * href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/LICENSE>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/LICENSE</a><br>
 * GitHub Repository: <a
 * href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/</a><br>
 * Wiki: <a
 * href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/wiki>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/wiki</a><br>
 * JavaDocs (for /src/java): <a
 * href=https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/javadoc/>https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/javadoc/</a><br>
 *
 * <hr>
 * 
 * <code>SocketFailureException</code> is thrown when an error occured while trying to communicate with a connected (PHP) client
 *
 * @author SyntaxError404, 2020
 * @version 1.0
 */
@SuppressWarnings("serial")
public class SocketFailureException extends RuntimeException {
	
	/**
	 * Instantiates a new SocketFailureException.
	 *
	 * @param cause the cause
	 */
	public SocketFailureException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Instantiates a new SocketFailureException.
	 *
	 * @param message the message
	 */
	public SocketFailureException(String message) {
		super(message);
	}
	
	/**
	 * Instantiates a new SocketFailureException.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public SocketFailureException(String message,Throwable cause) {
		super(message,cause);
	}
	
	/**
	 * Instantiates a new SocketFailureException.
	 */
	public SocketFailureException() {
		super();
	}
	
}
