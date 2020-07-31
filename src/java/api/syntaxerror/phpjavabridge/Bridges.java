package api.syntaxerror.phpjavabridge;

class Bridges {
	
	static class TCP extends Bridge {

		TCP(int port,boolean useAES,String password,int maxPacketLength) {
			super(port,useAES,password,maxPacketLength);
		}
		
	}
	
	static class UDP extends Bridge {

		UDP(int port,boolean useAES,String password,int maxPacketLength) {
			super(port,useAES,password,maxPacketLength);
		}
		
	}
	
}
