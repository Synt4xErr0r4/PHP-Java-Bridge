package test;

import api.syntaxerror.phpjavabridge.*;

public class Server implements PacketHandler {

    public static void main(String[] args) {
        
        Bridge bridge = Bridge.newUDP(8998, true, "my super secret password");

        bridge.setPacketHandler(new Server(), 37);

        bridge.start();

    }

    @Override
    public Packet handle(Packet incoming) {
        System.out.println(incoming.readStringUTF8());

        Packet outgoing = new Packet(38);

        outgoing.writeStringUTF8("Answer from Java!");

        return outgoing;
    }
    
}