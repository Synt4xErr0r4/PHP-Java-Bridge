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

require_once 'Bridge.php';

/**
 * either 'true' or 'false'
 * 
 * in theory a boolean is just a single bit: 
 * 
 * Space required (in bytes): 1
 */
define('DATA_BOOL',0);
/**
 * a signed (negative or positive) 8 bit = 1 byte integer (also known as int8)
 * 
 * range: -128 to 127
 * 
 * Space required (in bytes): 1
 */
define('DATA_BYTE',1);
/**
 * an unsigned (always positive) 8 bit = 1 byte integer (also known as uint8)
 * 
 * range: 0 to 255
 * 
 * Space required (in bytes): 1
 */
define('DATA_UNSIGNED_BYTE',2);
/**
 * a signed (negative or positive) 16 bit = 2 byte integer (also known as int16)
 * 
 * range: -32,768 to 32,767
 * 
 * Space required (in bytes): 2
 */
define('DATA_SHORT',3);
/**
 * an unsigned (always positive) 16 bit = 2 byte integer (also known as uint16)
 * 
 * range: 0 to 65,535
 * 
 * Space required (in bytes): 2
 */
define('DATA_UNSIGNED_SHORT',4);
/**
 * a signed (negative or positive) 32 bit = 4 byte integer (also known as int32)
 * 
 * range: -2,147,483,648 to 2,147,483,647
 * 
 * Space required (in bytes): 4
 */
define('DATA_INT',5);
/**
 * an unsigned (always positive) 32 bit = 4 byte integer (also known as uint32)
 * 
 * range: 0 to 4,294,967,295
 * 
 * Space required (in bytes): 4
 */
define('DATA_UNSIGNED_INT',6);
/**
 * a signed (negative or positive) 64 bit = 4 byte integer (also known as int64)
 * 
 * range: -2^63 = -9,223,372,036,854,775,808 to 2^63-1 ≈ 9,223,372,036,854,775,807
 * 
 * Note: unsigned longs are not supported
 */
define('DATA_LONG',7);
/**
 * a 32 bit = 4 byte single precision floating point number
 * 
 * range: (2 - 2^(-23)) * (-2)^127 ≈ -3.403E+38 ≈ 1.401E-45 to (2 - 2^(-23)) * 2^127 ≈ 3.403E+38
 * 
 * Space required (in bytes): 4
 * 
 * Note: due to floats being not 100% precise, calculations like 8 - 6.4 are
 *       actually not 1.6 as you might except, but 1.5999...
 *       You can even try this in PHP:
 * 
 *       > $a = 8 - 6.4; 
 *       > $b = 1.6;
 * 
 *       If you try this now, you will get 'bool(false)' as an output:
 * 
 *       > var_dump($a == $b);
 * 
 *       In order for this comparison to acutally work, you need to round the floats:
 * 
 *       > var_dump( round($a, 2), round($b, 2)); 
 * 
 *       This now prints 'bool(true)'
 * 
 * Note: this might not work on machines whose internal float-size is not 32 bit
 */
define('DATA_FLOAT',8);
/**
 * a 64 bit = 8 byte double precision floating point number
 * 
 * range: (2 - 2^(-52)) * (-2)^1023 ≈ -1.798E+308 to (2 - 2^(-52)) * 2^1023 ≈ 1.798E+308
 * 
 * Space required (in bytes): 8
 * 
 * Note: see DATA_FLOAT for information about precision
 * Note: this might not work on machines whose internal double-size is not 64 bit
 */
define('DATA_DOUBLE',9);
/**
 * Supports characters in range 0x0 - 0xFFFF (Unicode Plane 0/ Basic Multilingual Plane)
 * 
 * each character uses 1 to 3 bytes of space
 * additionally, 2 bytes are used for the length of the string
 * 
 * Space required (in bytes): <varying>
 */
define('DATA_STRING_UTF8',10);
/**
 * Supports characters in range 0x0 - 0x7F (Standard US-ASCII without NUL)
 * 
 * each character uses 7 bits of space
 * 
 * This implementation uses a space-optimized method of storing ASCII-charcters:
 * instead of each character using 1 full byte, each uses only 7 bits
 * using this method, you can store 8 characters in 7 bytes or (for instance) 1024 characters in 896 bytes.
 * 
 * the length of the string is stored as a short (int16) at the beginning of the string.
 * 
 * Space required (in bytes): 2+⌈0.875*n⌉ or 2+ceil(0.875*n)
 */
define('DATA_STRING_ASCII',11);
/**
 * Supports characters in range 0x1 - 0x7F (Standard US-ASCII without NUL)
 * 
 * each character uses 1 byte
 * unlike DATA_STRING_ASCII, the string length is not stored as a short (int16), but the end of the string is marked with a NUL-byte (like C-strings/ null-terminated strings)
 * 
 * Space required (in bytes): 1+n
 */
define('DATA_STRING_C',12);
/**
 * Stores an arbitrary amount of bytes
 * 
 * Space required (in bytes): 4+n
 */
define('DATA_BYTE_ARRAY',13);

/**
 * checks if the system is LE (Little Endian) or BE (Big Endian).
 * 
 * on a Little Endian system, 00000000 00000001 is interpreted as 1
 * but on big endian, it is the binary representation for 256
 * 
 * usually, Big Endian is used
 */
function isLittleEndian():bool {
    return unpack('S',"\x00\x01")[1]==1;
}

/**
 * A packet is a set of data sent to and received from the server.
 * 
 * Valid data types are: (defined as constants)
 *  - DATA_BYTE
 *  - DATA_UNSIGNED_BYTE
 *  - DATA_SHORT
 *  - DATA_UNSIGNED_SHORT
 *  - DATA_INT
 *  - DATA_UNSIGNED_INT
 *  - DATA_LONG
 *  - DATA_FLOAT
 *  - DATA_DOUBLE
 *  - DATA_STRING_ASCII 
 *  - DATA_STRING_C
 *  - DATA_STRING_UTF8
 *  - DATA_BYTE_ARRAY
 * 
 * To write data to the packet, use Packet::write(int,mixed), where the first argument is the data type and the second one is the actual data
 * To read data from the packet, use Packet::read(int), where the first argument is the data type
 * 
 * Structure of a Packet:
 * 
 * <int8 Packet-ID> <int8 Endianness> <int32 Packet-length> [<int8 data-type> <mixed data> [...]]
 * 
 * The max. Packet-length can be set when instantiating the Bridge
 * 
 * @see phpjava\Bridge::__construct(int,string,int,bool,?string,int)
 */
class Packet {

    /**
     * an array containing format characters used for pack(string,mixed...) and unpack(string,string)
     * 
     * layout:
     * 
     * ID => [
     *     string: name,
     *     string: format character,
     *     bool: whether or not strrev(string) is required (for unpack(string,string)) if system is Little Endian,
     *     int: byte size,
     *     callable: function to check if type is correct,
     *     callable: ensures correct type
     * ]
     * 
     * <initialized in __construct(int)>
     */
    private$dataTypes;

    private$data,$pid;

    /**
     * Instantiates a new Packet
     * 
     * @param pid the Packet-ID. must be in range [0;255]
     */
    public function __construct(int $pid) {
        $this->dataTypes=[
            DATA_BOOL=>          ['bool',  'c',false,1,fn($x)=>is_numeric($x)||is_bool($x),                                                                              fn($x)=>intval($x)!=0?1:0],
            DATA_BYTE=>          ['int8',  'c',false,1,fn($x)=>is_numeric($x)&&-128<=$x&&$x<=127,                                                                        'intval'],
            DATA_UNSIGNED_BYTE=> ['uint8', 'C',false,1,fn($x)=>is_numeric($x)&&0<=$x&&$x<=255,                                                                           'intval'],
            DATA_SHORT=>         ['int16', 's',true, 2,fn($x)=>is_numeric($x)&&-32768<=$x&&$x<=32767,                                                                    'intval'],
            DATA_UNSIGNED_SHORT=>['uint16','n',false,2,fn($x)=>is_numeric($x)&&0<=$x&&$x<=65535,                                                                         'intval'],
            DATA_INT=>           ['int32', 'l',true, 4,fn($x)=>is_numeric($x)&&-2147483648<=$x&&$x<=2147483647,                                                          'intval'],
            DATA_UNSIGNED_INT=>  ['uint32','N',false,4,fn($x)=>is_numeric($x)&&-0<=$x&&$x<=4294967295,                                                                   'intval'],
            DATA_LONG=>          ['int64', 'q',true, 8,fn($x)=>is_numeric($x)&&PHP_INT_MIN<=$x&&$x<=PHP_INT_MAX,                                                         'intval'], // system dependent
            DATA_FLOAT=>         ['float', 'G',false,4,fn($x)=>is_numeric($x)&&-340282346638528859811704183484516925440<=$x&&$x<=340282346638528859811704183484516925440,'floatval'],
            DATA_DOUBLE=>        ['double','E',false,8,fn($x)=>is_numeric($x)&&-PHP_FLOAT_MAX<=$x&&$x<=PHP_FLOAT_MAX,                                                    'floatval'] // system dependent
        ];

        if($pid<0||$pid>=255)
            warn("PacketIDs cannot be lower than 0 or greater than 255: $pid");

        $this->pid=$pid&0xFF; // make sure it's byte (int8)
    }

    /**
     * @return int the Packet-ID
     */
    public function getPacketID():int {
        return$this->pid;
    }

    /**
     * Writes data to the packet.
     * 
     * @param data_type The type of data. Can be one of the following:
     *                  - DATA_BYTE
     *                  - DATA_UNSIGNED_BYTE
     *                  - DATA_SHORT
     *                  - DATA_UNSIGNED_SHORT
     *                  - DATA_INT
     *                  - DATA_UNSIGNED_INT
     *                  - DATA_LONG
     *                  - DATA_FLOAT
     *                  - DATA_DOUBLE
     *                  - DATA_STRING_ASCII
     *                  - DATA_STRING_C
     *                  - DATA_STRING_UTF8
     *                  - DATA_BYTE_ARRAY
     * @param writeType internal use only
     */
    public function write(int $data_type,$data,bool $writeType=true) {
        if(isset($this->dataTypes[$data_type])) {
            $args=$this->dataTypes[$data_type];

            if(!$args[4]($data))
                throw InvalidTypeException::notTypeOf($data,$args[0]);

            if($writeType)
                $this->data.=pack('C',$data_type);
            $this->data.=pack($args[1],$args[5]($data));

            return;
        }

        switch($data_type) {
        case DATA_STRING_UTF8:
            
            if(!is_string($data))
                throw InvalidTypeException::notTypeOf($data,'string');

            if($writeType)
                $this->data.=pack('C',DATA_STRING_UTF8);
            $this->write(DATA_UNSIGNED_SHORT,strlen($data),false);

            for($i=0;$i<strlen($data);++$i)
                $this->write(DATA_UNSIGNED_BYTE,ord($data[$i]),false);

            break;
        case DATA_STRING_ASCII:
            
            if(!is_string($data))
                throw InvalidTypeException::notTypeOf($data,'string');
            
            if($writeType)
                $this->data.=pack('C',DATA_STRING_ASCII);

            $data=Packet::ensureASCII($data,null,fn(int $ord,string $chr)=>"$chr (U+".sprintf("%'04X",$ord).") is not a valid ASCII character");

            $this->write(DATA_UNSIGNED_SHORT,strlen($data),false);

            $previous=0;

            for($i=0;$i<strlen($data);++$i) {
                $ord=ord($data[$i]);

                $mod=$i%8;
                $temp=($ord<<($mod==8?0:1+$mod))&0xFFFF;

                if($mod!=0)
                    $this->write(DATA_UNSIGNED_BYTE,$previous|($temp>>8),false);

                $previous=$temp&0xFF;
            }

            if(strlen($data)%8!=0)
                $this->write(DATA_UNSIGNED_BYTE,$previous,false);

            break;
        case DATA_STRING_C:
            
            if(!is_string($data))
                throw InvalidTypeException::notTypeOf($data,'string');

            if($writeType)
                $this->data.=pack('C',DATA_STRING_C);

            $data=Packet::ensureASCII($data,null,fn(int $ord,string $chr)=>"$chr (U+".sprintf("%'04X",$ord).") is not a valid ASCII character");

            for($i=0;$i<strlen($data);++$i)
                $this->write(DATA_BYTE,ord($data[$i]),false);

            $this->write(DATA_BYTE,0,false);
            
            break;
        case DATA_BYTE_ARRAY:
            
            if(!is_array($data))
                throw InvalidTypeException::notTypeOf($data,'array');

            if($writeType)
                $this->data.=pack('C',DATA_BYTE_ARRAY);
            
            $byteOnly=[];

            foreach($data as$value)
                if(!is_int($value))
                    warn('DATA_BYTE_ARRAY only accepts integer values, got '.gettype($value).' instead (skipping value)');
                else {
                    if($value<-128||$value>255) // allow signed and unsigned bytes
                        notice("byte $value out of range: [-128;255] (converting to byte)");
                    elseif($value>127)
                        notice('DATA_BYTE_ARRAY handles bytes as signed bytes (converting to signed byte)');

                    array_push($byteOnly,$value&0xFF); // convert non-bytes to bytes and add to array
                }

            $this->write(DATA_INT,sizeof($byteOnly),false);
            foreach($byteOnly as$value)$this->write(DATA_UNSIGNED_BYTE,$value,false); // write as unsigned byte, but read as signed byte

            break;
        default:
            throw new InvalidTypeException("Unrecognized type: $data_type");
        }
    }

    /**
     * Read raw data from the packet and removes it. internal use only
     * 
     * @param fmt the type of data. See https://www.php.net/manual/en/function.pack
     * @param len the length of the data
     * @param strrevLE whether or not the data should be reversed if the system is Little Endian
     * 
     * @return mixed the read data (either int or float)
     */
    private function read0(string $fmt,int $len,bool $strrevLE=false) {
        if(strlen($fmt)!=1)
            throw new Exception('can only unpack one type at a time, got '.strlen($fmt).' instead.');

        $unpack=substr($this->data,0,$len);
        
        if(strlen($this->data)-$len<0)
            throw new IndexOutOfBoundsException("Too few data left: need $len, ".strlen($this->data)." left");

        $data=unpack($fmt,isLittleEndian()&&$strrevLE?strrev($unpack):$unpack)[1];

        $this->data=substr($this->data,$len);

        return$data;
    }

    /**
     * Reads data from the packet
     * 
     * @param data_type the datatype. Can be one of the following:
     *                  - DATA_BYTE
     *                  - DATA_UNSIGNED_BYTE
     *                  - DATA_SHORT
     *                  - DATA_UNSIGNED_SHORT
     *                  - DATA_INT
     *                  - DATA_UNSIGNED_INT
     *                  - DATA_LONG
     *                  - DATA_FLOAT
     *                  - DATA_DOUBLE
     *                  - DATA_STRING_ASCII
     *                  - DATA_STRING_C
     *                  - DATA_STRING_UTF8
     *                  - DATA_BYTE_ARRAY
     * @param readType internal use only
     * 
     * @return mixed the read data 
     */
    public function read(int $data_type,bool $readType=true) {
        if($readType) {
            $type=$this->read0('C',1);

            if($type!==$data_type)
                throw new InvalidTypeException('Types don\'t match: '.(is_null($type)?'NULL':$type)." != $data_type");
        }

        if(isset($this->dataTypes[$data_type])) {
            $args=$this->dataTypes[$data_type];

            return$this->read0($args[1],$args[3],$args[2]);
        }


        switch($data_type) {
        case DATA_STRING_UTF8:
            $len=$this->read(DATA_UNSIGNED_SHORT,false);

            $str='';

            for($i=0;$i<$len;++$i)
                $str.=chr($this->read(DATA_UNSIGNED_BYTE,false));

            return$str;
        case DATA_STRING_ASCII:
            $previous=0;
            $str='';

            $len=$this->read0('n',2,false);

            for($i=0;strlen($str)<$len;++$i) {

                if($i%7==0&&$i!=0) {
                    $str.=chr($previous&0x7F);
                    $previous=0;

                    if(strlen($str)>=$len)
                        break;
                }

                $current=$this->read0('C',1);

                $modPrev=$i%7;
                $mod=$modPrev+1;

                $oldPart=($previous&(pow(2,$modPrev)-1))<<(8-$mod);
                $newPart=($current>>$mod)&(pow(2,8-$mod)-1);

                $full=$oldPart|$newPart;
                $str.=chr($full);

                $previous=$current;
            }

            return$str;
        case DATA_STRING_C:
            
            $str='';

            while(true) {
                $ord=$this->read(DATA_UNSIGNED_BYTE,false);

                if($ord==0)
                    break;

                $str.=chr($ord);
            }

            return$str;
        case DATA_BYTE_ARRAY:
            $len=$this->read(DATA_INT,false);

            $bytes=[];

            for($i=0;$i<$len;++$i)
                array_push($bytes,$this->read(DATA_BYTE,false));

            return$bytes;
        default:
            throw new InvalidTypeException("Unrecognized type: $data_type");
        }
    }

    /**
     * Ensures a string consists only of US-ASCII (without NUL) characters
     * 
     * @param string the string to be checked
     * @param replace the replacement string if a character is non-ASCII. If null, the string is not replaced, but omitted
     * @param warning either a string or a callable. Used to display a warning message. The callable takes two arguments: 
     *                 - the Unicode codepoint of the illegal char (as an int) and 
     *                 - the char itself (as a string).
     *                If null, no warning is shown
     * 
     * @return string the ASCII string
     */
    public static function ensureASCII(string $string,?string $replace=null,$warning=null):string {
        $ascii='';

        for($i=0;$i<strlen($string);++$i) {
            $chr=$string[$i];
            $ord=ord($chr);

            if($ord<=0x7F&&$ord>=0x0)
                $ascii.=$chr;
            else {
                if($ord>0xDF)
                    $codepoint=(($ord&0x0F)<<12)|((ord($string[++$i])&0x3F)<<6)|(ord($string[++$i])&0x3F);
                else$codepoint=(($ord&0x1F)<<6)|(ord($string[++$i])&0x3F);

                if(!is_null($warning)) {
                    if(is_string($warning))
                        warn($warning);
                    elseif(is_callable($warning))
                        warn($warning($codepoint,mb_chr($codepoint)));
                }

                if(!is_null($replace))
                    $ascii.=$replace;
            }
        }

        return$ascii;
    }

    /**
     * @return int the size of the packet's data
     */
    public function size() {
        return strlen($this->data);
    }

    /**
     * internal use only
     */
    public function raw() {
        $data=$this->data;
        $this->data='';
        return$data;
    }

    /**
     * internal use only
     */
    public function setAndValidate(string $new) {
        $this->data=clone$new;

        while(strlen($this->data)>0) {
            $type=unpack('C',$this->data);
            $this->read($type);
        }

        $this->data=$new;
    }

}