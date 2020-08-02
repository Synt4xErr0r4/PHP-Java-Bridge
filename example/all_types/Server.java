package test;

import java.util.Arrays;

import api.syntaxerror.phpjavabridge.Bridge;
import api.syntaxerror.phpjavabridge.Packet;

public class UDPTest {
	
	public static void main(String[] args) {
		Bridge bridge = Bridge.newUDP(8877, true, "I like pizza!");
		bridge.setExceptionHandler((t, e) -> {
			System.err.println("Thread: " + t.getName());
			e.printStackTrace();
		});
		bridge.setPacketHandler((client, incoming) -> {
			System.out.println(incoming.readByte());
			System.out.println(incoming.readUnsignedByte());
			System.out.println(incoming.readShort());
			System.out.println(incoming.readUnsignedShort());
			System.out.println(incoming.readInt());
			System.out.println(incoming.readUnsignedInt());
			System.out.println(incoming.readLong());
			System.out.println(incoming.readFloat());
			System.out.println(incoming.readDouble());
			System.out.println(incoming.readStringUTF8());
			System.out.println(incoming.readStringASCII());
			System.out.println(incoming.readStringC());
			System.out.println(Arrays.toString(incoming.readByteArray()));
			
			Packet outgoing = new Packet(0xF7);
			
			outgoing.writeByte(Byte.MIN_VALUE);
			outgoing.writeUnsignedByte(0xFF);
			outgoing.writeShort(Short.MIN_VALUE);
			outgoing.writeUnsignedShort(0xFFFF);
			outgoing.writeInt(Integer.MIN_VALUE);
			outgoing.writeUnsignedInt(0xFFFFFFFFL);
			outgoing.writeLong(Long.MIN_VALUE);
			outgoing.writeFloat(Float.MAX_VALUE);
			outgoing.writeDouble(Double.MAX_VALUE);
			outgoing.writeStringUTF8("Hello from Java! \u2022\u2022\u2022");
			outgoing.writeStringASCII("This is an ASCII string");
			outgoing.writeStringC("I like c strings");
			outgoing.writeByteArray(new byte[] {Byte.MAX_VALUE, Byte.MIN_VALUE, 0, 0xF, -0xF});
			
			return outgoing;
		}, 0x7F);
		
		bridge.start();
	}
	
}
