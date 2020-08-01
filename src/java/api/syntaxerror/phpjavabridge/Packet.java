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
 * @version 1.0
 * @author SyntaxError404, 2020
 */
public class Packet {
	
	private byte[]data;
	private int pid,pointer,mode;
	private boolean littleEndian;
	
	/**
	 * Instantiates a new Packet
	 * 
	 * @param pid The Packet-ID. must be in range [0;255]
	 * @param mode either 'w' (write) or 'r' (read). If 'w' is set, you cannot read from the Packet and vice versa.
	 */
	public Packet(int pid,char mode) {
		if(pid<0||pid>255)
			throw new IllegalArgumentException("PacketIDs cannot be lower than 0 or greater than 255: "+pid);
		
		this.pid=pid;
		this.mode=mode;
		
		littleEndian=false;
		data=new byte[128];
		pointer=-1;
	}
	
	/**
	 * @return the ID of this Packet
	 */
	public int getPacketID() {
		return pid;
	}
	
	/**
	 * @return a single (unsigned) byte from the packet
	 */
	private int read() {
		if(mode!='r')
			throw new UnsupportedOperationException("Cannot read from Packet: Packet is write-only ");
		
		if(pointer<0)
			throw new IndexOutOfBoundsException("End of data reached");
		
		return data[--pointer+1]&0xFF; // &0xFF to make it an 'unsigned byte'
	}
	/**
	 * Reads n bytes, where n is the length of {@code bytes}
	 * 
	 * @param bytes the array to be filled
	 * @param rotateIfLE whether or not the array should be rotated if the system is Little Endian
	 */
	private void read(byte[]bytes,boolean rotateIfLE) {
		for(int i=0;i<bytes.length;++i)
			bytes[i]=(byte)read();
		
		if(rotateIfLE&&littleEndian)
			for(int i=0;i<Math.floor(bytes.length/2D);++i) {
				int j=bytes.length-i-1;
				byte b=bytes[i];
				bytes[i]=bytes[j];
				bytes[j]=b;
			}
	}
	
	/**
	 * Checks for a single byte (used for data-type IDs)
	 * 
	 * @param i the flag to be checked for 
	 */
	private void checkFlag(int i) {
		int j=read();
		if(j!=i)
			throw new IllegalArgumentException("Type mismatch: "+i+" and "+j);
	}
	
	/**
	 * Writes a single byte to the packet
	 * 
	 * @param i the byte to be written
	 */
	private void write(int i) {
		if(mode!='r')
			throw new UnsupportedOperationException("Cannot write to Packet: Packet is read-only ");
		
		if(++pointer>data.length) {
			byte[]copy=new byte[data.length+128];
			System.arraycopy(data,0,copy,0,data.length);
			data=copy;
		}
		
		data[pointer]=(byte)(i&0xFF);
	}
	
	/**
	 * in theory a boolean is just a single bit: <br>
	 * <br>
	 * Space required (in bytes): 1
	 * 
	 * @return either 'true' or 'false'
	 */
	public boolean readBoolean() {
		checkFlag(0);
		return read()!=0;
	}
	/**
	 * range: -128 to 127<br>
	 * <br>
	 * Space required (in bytes): 1
	 * 
	 * @return a signed (negative or positive) 8 bit = 1 byte integer (also known as int8)
	 */
	public byte readByte() {
		checkFlag(1);
		return(byte)read();
	}
	/**
	 * range: 0 to 255<br>
	 * <br>
	 * Space required (in bytes): 1
	 * 
	 * @return an unsigned (always positive) 8 bit = 1 byte integer (also known as uint8)
	 */
	public int readUnsignedByte() {
		checkFlag(2);
		return read();
	}
	/**
	 * range: -32,768 to 32,767<br>
	 * <br>
	 * Space required (in bytes): 2
	 * 
	 * @return a signed (negative or positive) 16 bit = 2 byte integer (also known as int16)
	 */
	public short readShort() {
		checkFlag(3);
		byte[]bytes=new byte[2];
		read(bytes,true);
		return(short)((bytes[0]<<8)|bytes[1]);
	}
	/**
	 * range: 0 to 65,535<br>
	 * <br>
	 * Space required (in bytes): 2
	 * 
	 * @return an unsigned (always positive) 16 bit = 2 byte integer (also known as uint16)
	 */
	public int readUnsignedShort() {
		checkFlag(4);
		return(read()<<8)|read();
	}
	/**
	 * range: -2,147,483,648 to 2,147,483,647<br>
	 * <br>
	 * Space required (in bytes): 4
	 * 
	 * @return a signed (negative or positive) 32 bit = 4 byte integer (also known as int32)
	 */
	public int readInt() {
		checkFlag(5);
		byte[]bytes=new byte[4];
		read(bytes,true);
		return(bytes[0]<<24)|(bytes[1]<<16)|(bytes[2]<<8)|bytes[3];
	}
	/**
	 * range: 0 to 4,294,967,295<br>
	 * <br>
	 * Space required (in bytes): 4
	 * 
	 * @return an unsigned (always positive) 32 bit = 4 byte integer (also known as uint32)
	 */
	public long readUnsignedInt() {
		checkFlag(6);
		return((long)read()<<24)|(read()<<16)|(read()<<8)|read();
	}
	/**
	 * range: -2^63 = -9,223,372,036,854,775,808 to 2^63-1 = 9,223,372,036,854,775,807
	 * 
	 * @apiNote unsigned longs are not supported
	 * 
	 * @return a signed (negative or positive) 64 bit = 4 byte integer (also known as int64)
	 */
	public long readLong() {
		checkFlag(7);
		return readLong(true);
	}
	
	/**
	 * internal use only<br>
	 * <br>
	 * default longs might be stored as Little Endian, but doubles aren't
	 */
	private long readLong(boolean rotateIfLE) {
		byte[]bytes=new byte[8];
		read(bytes,true);
		return	((long)bytes[0]<<56)|
				((long)bytes[1]<<48)|
				((long)bytes[2]<<40)|
				((long)bytes[3]<<32)|
				(bytes[4]<<24)|
				(bytes[5]<<16)|
				(bytes[6]<<8)|
				bytes[7];
	}
	
	/**
	 * range: (2 - 2^(-23)) * (-2)^127 ≈ -3.403E+38 ≈ 1.401E-45 to (2 - 2^(-23)) * 2^127 ≈ 3.403E+38<br>
	 * <br>
	 * Space required (in bytes): 4<br>
	 * <br>
	 * Note: due to floats being not 100% precise, calculations like <code>8 - 6.4</code> are<br>
	 *       actually not <code>1.6</code> as you might except, but <code>1.5999...</code><br>
	 *       You can even try this in PHP:<br>
	 * <br>
	 *       {@code $a = 8 - 6.4;}<br> 
	 *       {@code $b = 1.6;}<br>
	 * <br>
	 *       If you try this now, you will get 'bool(false)' as an output:<br>
	 * <br>
	 *       {@code var_dump($a == $b);}<br>
	 * <br>
	 *       In order for this comparison to acutally work, you need to round the floats:<br>
	 * <br>
	 *       {@code var_dump( round($a, 2), round($b, 2));}<br> 
	 * <br>
	 *       This now prints 'bool(true)'<br>
	 * 
	 * @implNote this might not work on machines whose internal float-size is not 32 bit
	 * 
	 * @return a 32 bit = 4 byte single precision floating point number
	 */
	public float readFloat() {
		checkFlag(8);
		return Float.intBitsToFloat((int)readUnsignedInt());
	}
	/**
	 * range: (2 - 2^(-52)) * (-2)^1023 ≈ -1.798E+308 to (2 - 2^(-52)) * 2^1023 ≈ 1.798E+308<br>
	 * <br>
	 * Space required (in bytes): 8
	 * 
	 * @implNote see {@link #readFloat()} for information about precision
	 * @implNote this might not work on machines whose internal double-size is not 64 bit
	 * 
	 * @return a 64 bit = 8 byte double precision floating point number
	 */
	public double readDouble() {
		checkFlag(9);
		return Double.longBitsToDouble(readLong(false));
	}
	/**
	 * Supports characters in range 0x0 - 0xFFFF (Unicode Plane 0/ Basic Multilingual Plane)<br>
	 * <br>
	 * each character uses 1 to 3 bytes of space<br>
	 * additionally, 2 bytes are used for the length of the string<br>
	 * <br>
	 * Space required (in bytes): 2+n (min) to 2+3*n (max)
	 * 
	 * @return a UTF-8 encoded string
	 */
	public String readStringUTF8() {
		checkFlag(10);
		
		int len=(read()<<8)|read();
		
		String str="";
		
		for(int i=0;i<len;++i) {
			int chr=read();
			
			if(chr<=0x7F)
				str+=(char)chr;
			else if(chr>0xDF) {
				i+=2;
				str+=(char)((chr&0xF)<<12)|((chr&0x3F)<<6)|(read()&0x3F);
			} else {
				++i;
				str+=(char)((chr&0x1F)<<6)|(read()&0x3F);
			}
		}
		
		return str;
	}
	/**
	 * Supports characters in range 0x0 - 0x7F (Standard US-ASCII without NUL)<br>
	 * <br>
	 * each character uses 7 bits of space<br>
	 * <br>
	 * This implementation uses a space-optimized method of storing ASCII-charcters:<br>
	 * instead of each character using 1 full byte, each uses only 7 bits<br>
	 * using this method, you can store 8 characters in 7 bytes or (for instance) 1024 characters in 896 bytes.<br>
	 * <br>
	 * the length of the string is stored as a short (int16) at the beginning of the string.<br>
	 * <br>
	 * Space required (in bytes): 2+⌈0.875*n⌉ or 2+ceil(0.875*n)
	 * 
	 * @return an ASCII string
	 */
	public String readStringASCII() {
		checkFlag(11);

		int len=(read()<<8)|read();
		
		String str="";
		int previous=0;
		
		for(int i=0;str.length()<len;++i) {
			if(i%7==0&&i!=0) {
				str+=(char)(previous&0x7F);
				previous=0;
				
				if(str.length()>=len)
					break;
			}
			
			int current=read();
			
			int modPrev=i%7,
				mod=modPrev+1;
			
			int oldPart=(previous&((int)Math.pow(2,modPrev)-1))<<(8-mod),
				newPart=(current>>mod)&((int)Math.pow(2,8-mod)-1);
			
			str+=(char)(oldPart|newPart);
			
			previous=current;
		}
		
		return str;
	}
	/**
	 * Supports characters in range 0x1 - 0x7F (Standard US-ASCII without NUL)<br>
	 * <br>
	 * each character uses 1 byte<br>
	 * unlike DATA_STRING_ASCII, the string length is not stored as a short (int16), but the end of the string is marked with a NUL-byte (like C-strings/ null-terminated strings)<br>
	 * <br>
	 * Space required (in bytes): 1+n
	 * 
	 * @return an ASCII string
	 */
	public String readStringC() {
		checkFlag(12);
		
		String str="";
		
		for(int i=read();i!=0;i=read())
			str+=(char)i;
		
		return str;
	}
	/**
	 * Stores an arbitrary amount of bytes
	 * 
	 * Space required (in bytes): 4+n
	 * 
	 * @return a byte array
	 */
	public byte[]readByteArray() {
		byte[]bytes=new byte[4];
		read(bytes,true);
		int len=(bytes[0]<<24)|(bytes[1]<<16)|(bytes[2]<<8)|bytes[3];
		
		byte[]buf=new byte[len];
		
		read(buf,false);
		
		return buf;
	}
	
	/**
	 * @param b the boolean to be written
	 * 
	 * @see #readBoolean()
	 */
	public void writeBoolean(boolean b) {
		write(0);
		write(b?1:0);
	}
	/**
	 * @param b the byte to be written
	 * 
	 * @see #readByte()
	 */
	public void writeByte(byte b) {
		write(0);
		write(b);
	}
	/**
	 * @param b the unsigned byte to be written
	 * 
	 * @see #readUnsignedByte()
	 */
	public void writeUnsignedByte(int b) {
		write(2);
		write(b&0xFF);
	}
	/**
	 * @param s the short to be written
	 * 
	 * @see #readShort()
	 */
	public void writeShort(short s) {
		write(3);
		write(s>>>8);
		write(s);
	}
	/**
	 * @param s the unsigned short to be written
	 * 
	 * @see #readUnsignedShort()
	 */
	public void writeUnsignedShort(int s) {
		write(4);
		write(s>>>8);
		write(s);
	}
	/**
	 * @param i the int to be written
	 * 
	 * @see #readInt()
	 */
	public void writeInt(int i) {
		write(5);
		write(i>>24);
		write(i>>16);
		write(i>>8);
		write(i);
	}
	/**
	 * @param i the unsigned int to be written
	 * 
	 * @see #readUnsignedInt()
	 */
	public void writeUnsignedInt(long i) {
		write(6);
		write((int)(i>>24));
		write((int)(i>>16));
		write((int)(i>>8));
		write((int)i);
	}
	/**
	 * @param l the long to be written
	 * 
	 * @see #readLong()
	 */
	public void writeLong(long l) {
		write(7);
		write((int)(l>>56));
		write((int)(l>>48));
		write((int)(l>>40));
		write((int)(l>>32));
		write((int)(l>>24));
		write((int)(l>>16));
		write((int)(l>>8));
		write((int)l);
	}
	/**
	 * @param f the float to be written
	 * 
	 * @see #readFloat()
	 */
	public void writeFloat(float f) {
		writeInt(Float.floatToIntBits(f));
		data[pointer-4]=8;
	}
	/**
	 * @param d the double to be written
	 * 
	 * @see #readDouble()
	 */
	public void writeDouble(double d) {
		writeLong(Double.doubleToLongBits(d));
		data[pointer-8]=9;
	}

	/**
	 * @param b the UTF-8 encoded string to be written
	 * 
	 * @see #readStringUTF8()
	 */
	public void writeStringUTF8(String s) {
		write(10);
		
		int utflen=0;
		
		for(char c:s.toCharArray()) {
			if(c>0&&c<=0x7F)
				++utflen;
			else if(c>0x7FF)
				utflen+=3;
			else utflen+=2;
		}
		
		write(utflen>>8);
		write(utflen);
		
		for(char c:s.toCharArray()) {
			if(c>0&&c<=0x7F)
				write(c);
			else if(c>0x7FF) {
				write((c>>>12)&0x0F);
				write((c>>>6)&0x3F);
				write(c&0x3F);
			} else {
				write((c>>>6)&0x1F);
				write(c&0x3F);
			}
		}
	}
	/**
	 * @param b the ASCII string to be written
	 * 
	 * @see #readStringASCII()
	 */
	public void writeStringASCII(String s) {
		write(11);
		
		int len=s.length()&0xFFFF;
		
		write(len>>8);
		write(len);
		
		int previous=0;
		
		for(int i=0;i<len;++i) {
			int chr=s.charAt(i);
			
			if(chr==0||chr>0x7F)
				throw new IllegalArgumentException("only US-ASCII (without NUL) strings are allowed");
			
			int mod=i%8,
				temp=(chr<<(mod==8?0:1+mod))&0xFFFF;
			
			if(mod!=0)
				write(previous|(temp>>8));
			
			previous=temp&0xFF;
		}
		
		if(len%8!=0)
			write(previous);
	}
	/**
	 * @param b the ASCII string to be written
	 * 
	 * @see #readStringC()
	 */
	public void writeStringC(String s) {
		write(12);
		
		for(char c:s.toCharArray()) {
			if(c==0||c>0x7F)
				throw new IllegalArgumentException("only US-ASCII (without NUL) strings are allowed");
			write(c);
		}
		write(0);
	}

	/**
	 * @param b the byte array to be written
	 * 
	 * @see #readByteArray()
	 */
	public void writeByteArray(byte[]b) {
		write(13);
		
		int len=b.length;
		
		write(len>>24);
		write(len>>16);
		write(len>>8);
		write(len);
		
		for(byte x:b)
			write(x);
	}
	
	/**
	 * @return whether or not short, int and long are stored in Little Endian format
	 */
	public boolean isLittleEndian() {
		return littleEndian;
	}
	
	/**
	 * internal use only
	 */
	public byte[]getData() {
		return data;
	}
	
	/**
	 * @return the Packet's size
	 */
	public int size() {
		return pointer;
	}
	
}
