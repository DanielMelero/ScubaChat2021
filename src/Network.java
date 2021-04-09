package src;

import java.util.TimerTask;

import client.Message;

public class Network {
	
	
	private TransportLayer transportlayer;
	private MyProtocol protocol;
	
	
	public Network() {
		
		//this.transportlayer = new TransportLayer();
		this.protocol = new MyProtocol("netsys.ewi.utwente.nl", 8954, 4100);
	}
	
	public void sendToTransportLayer() {
		
		
		
		try {
			//transportlayer.createPackets(protocol.input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
			
	}
	
	public void sendToTransportLayerReceive() {
		
		
		
		try {
	//		transportlayer.createPackets(protocol.input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
			
	}
	
	
	public void onlineCheck() {
		
		
		
	}
	
	public class timertask extends TimerTask {

        private Message msg;

		public timertask(Message msg) {
            this.msg = msg;
        }

		public void run() {
            System.out.println("");
        }
    }
    
}



