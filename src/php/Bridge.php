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
 * @link JavaDocs (for /src/java): https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/javadoc/
 * 
 * @version 1.0
 * @author SyntaxError404, 2020
 */
namespace phpjava;

use Exception;

require_once 'Packet.php';
require_once 'exceptions.php';

define('BRIDGE_TCP',0);
define('BRIDGE_UDP',1);

/**
 * send a warning message if the 'E_WARNING' bitmask for 'error_reporting(?int)' is set
 */
function warn(string $msg) {
    if((E_WARNING&error_reporting())!=0)
        echo"<b>Warning:</b> $msg<br/>";
}
/**
 * send a notice message if the 'E_NOTICE' bitmask for 'error_reporting(?int)' is set
 */
function notice(string $msg) {
    if((E_NOTICE&error_reporting())!=0)
        echo"<b>Notice:</b> $msg<br/>";
}

/**
 * The connection between PHP and Java
 * 
 * Data can be transmitted via the Packet class
 * 
 * @see phpjava\Packet
 */
class Bridge {

    private$sock,$hostname,$port,$useAES,$passwd,$maxPacketSize,$method;

    /**
     * Instantiates a new PHP-Java-Bridge
     * 
     * @param method the method to be used: either TCP (BRIGE_TCP) or UDP (BRIDGE_UDP)
     * @param hostname the address (IPv4 or domain) to connect to the server
     * @param port the port of the server. default 8998
     * @param useAES whether or not the data should be AES-256-CBC encrypted. default false
     * @param passwd only required if useAES is true. Hashed with SHA3-256. default null
     * @param maxPacketSize the maximum size of a Packet. default 65535
     */
    public function __construct(int $method,string $hostname,int $port=8998,bool $useAES=false,?string $passwd=null,int $maxPacketSize=65535) {
        $this->hostname=$hostname;

        if($port<0||$port>65535)
            throw new Exception("Port out of range: $port: [0;65,535]");

        if(is_null($passwd)&&$useAES)
            throw new Exception("AES required a password, got null instead");

        $this->port=$port;
        $this->useAES=$useAES;
        $this->passwd=is_null($passwd)?null:hash('sha3-256',$passwd);

        if($maxPacketSize>0x7fffffff)
        throw new Exception("max. Packet size out of range: $maxPacketSize [6;2,147,483,647]");

        $this->maxPacketSize=$maxPacketSize;

        if($method!=BRIDGE_TCP&&$method!=BRIDGE_UDP)
            throw new Exception("Unrecognized method: $method");
    }

    /**
     * TCP: establishes connection to the server
     * UDP: initialized socket (doesn't throw Exception if the server is not reachable)
     */
    public function connect() {
        if(!is_null($this->sock))
            throw new ConnectionAlreadyEstablishedException("Already connected");

        if($this->method==BRIDGE_TCP)
            $this->sock=fsockopen($this->hostname,$this->port,$errno,$errstr,30);
        else$this->sock=socket_create(AF_INET,SOCK_DGRAM,0);

        if(!$this->sock)
            throw new ConnectionFailedException("Couldn't connect to server: $errstr [#$errno]");
    }
    public function disconnect() {
        if(is_null($this->sock))
            throw new ConnectionNotEstablishedYetException("Not connected yet");

        if($this->method==BRIDGE_TCP)
            fclose($this->sock);
        else socket_close($this->sock);
    }

    /**
     * Sends and receives a packet.
     * 
     * @param packet the Packet to be sent to the server
     * 
     * @return Packet the Packet received from the server
     */
    public function sendPacket(Packet $packet):Packet {
        
        if(is_null($this->sock))
            throw new ConnectionNotEstablishedYetException("Not connected yet");

        if($packet->size()+6>$this->maxPacketSize)
            throw new Exception("Packets exceeds max. allowed size: ".$packet->size());

        $message=pack('C',$packet->getPacketID()).pack('C',isLittleEndian()?1:0).pack('N',$packet->size()&0x7fffffff).$this->encrypt($packet->raw());

        if($this->method==BRIDGE_TCP) {

            if(fwrite($this->sock,$message,strlen($message))===false) {
                $err=error_get_last()or['type'=>0,'message'=>'null'];
                throw new Exception("Couldn't send packet: ".$err['message']." [#".$err['type']."]");
            }

            $buffer=fgets($this->sock,6);

            if(!$buffer) {
                $err=error_get_last()or['type'=>0,'message'=>'null'];
                throw new Exception("Couldn't read packet: ".$err['message']." [#".$err['type']."]");
            }

            $pid=unpack('C',$buffer);
            $size=unpack('N',substr($buffer,2,4));

            $buffer=fgets($this->sock,$size);
            
            if(!$buffer) {
                $err=error_get_last()or['type'=>0,'message'=>'null'];
                throw new Exception("Couldn't read packet: ".$err['message']." [#".$err['type']."]");
            }

            $response=new Packet($pid);
            $response->setAndValidate($this->decrypt($buffer));

        } else {
            
            if(!socket_sendto($this->sock,$message,strlen($message),$this->hostname,$this->port)) {
                $errno=socket_last_error();
                $errstr=socket_strerror($errno);
                throw new Exception("Couldn't send packet: $errstr [#$errno]");
            }

            if(socket_recvfrom($this->sock,$buffer,6,MSG_WAITALL,$this->hostname,$this->port)===false) {
                $errno=socket_last_error();
                $errstr=socket_strerror($errno);
                throw new Exception("Couldn't send packet: $errstr [#$errno]");
            }

            $pid=unpack('C',$buffer);
            $size=unpack('N',substr($buffer,2,4));

            $response=new Packet($pid);
            $response->setAndValidate($this->decrypt(substr($buffer,6,$size)));

        }

        return$response;
    }

    /**
     * @param plainText the text to be encrypted
     * 
     * @return string the AES-256-CBC encrypted string. The password is hashed with SHA3-256
     */
    private function encrypt(string $plainText):string {
        if(!$this->useAES)
            return base64_encode($plainText);

        $iv=openssl_random_pseudo_bytes(16);

        if(!$iv) {
            $err=error_get_last()or['type'=>0,'message'=>'null'];
            throw new Exception("Couldn't generate IV: ".$err['message']." [#".$err['type']."]");
        }

        return base64_encode($iv.openssl_encrypt($plainText,'AES-256-CBC',$this->passwd,OPENSSL_RAW_DATA,$iv));
    }
    /**
     * @param cipherText the encrypted text
     * 
     * @return string the decrypted plain text
     */
    private function decrypt(string $cipherText):string {
        $cipherText=base64_decode($cipherText);

        if(!$this->useAES)
            return$cipherText;
        
        $iv=substr($cipherText,0,16);
        $cipherText=substr($cipherText,16);

        return openssl_decrypt($cipherText,'AES-256-CBC',$this->passwd,OPENSSL_RAW_DATA,$iv);
    }

}

?>