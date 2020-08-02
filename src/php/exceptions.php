<?php
/**
 * PHP-Java-Bridge provides a TCP- or UDP-based connection between PHP (Client) and Java (Server)
 * 
 * licensed under the Apache License 2.0:
 * 
 * Permissions:
 *  - Commercial use
 *  - Modification
 *  - Distribution
 *  - Patent use
 *  - Private use
 * 
 * Limitiations:
 *  - Trademark use
 *  - Liability
 *  - Warranty
 * 
 * Conditions:
 *  - License and copyright notice
 *  - State changes
 * 
 * @link License: https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/LICENSE
 * @link GitHub Repository: https://github.com/Synt4xErr0r4/PHP-Java-Bridge/
 * @link Wiki: https://github.com/Synt4xErr0r4/PHP-Java-Bridge/wiki
 * 
 * @version 1.0
 * @author SyntaxError404, 2020
 */
namespace phpjava;

use Exception;
use Throwable;

class ConnectionAlreadyEstablishedException extends Exception {

    public function __construct(?string $message=null,int $code=0,?Throwable $previous=null) {
        parent::__construct($message,$code,$previous);
    }

}

class ConnectionNotEstablishedYetException extends Exception {

    public function __construct(?string $message=null,int $code=0,?Throwable $previous=null) {
        parent::__construct($message,$code,$previous);
    }

}

class ConnectionFailedException extends Exception {

    public function __construct(?string $message=null,int $code=0,?Throwable $previous=null) {
        parent::__construct($message,$code,$previous);
    }

}

class InvalidTypeException extends Exception {

    public function __construct(?string $message=null,int $code=0,?Throwable $previous=null) {
        parent::__construct($message,$code,$previous);
    }

    public static function notTypeOf($invalid,string $expected):InvalidTypeException {
        return new InvalidTypeException(print_r($invalid,true).' ('.gettype($invalid).") is not typeof $expected");
    }

}

class IndexOutOfBoundsException extends Exception {

    public function __construct(?string $message=null,int $code=0,?Throwable $previous=null) {
        parent::__construct($message,$code,$previous);
    }

}