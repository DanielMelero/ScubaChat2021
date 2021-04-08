package src;

/**
 * Application Layer mechanism
 * 
 * @author Kento Verlaan
 * 
 * methods used to send and receive messages from and to others
 *
 */

public class ApplicationLayer {
	
	private TransportLayer tl;
	private UI ui;
	
	public ApplicationLayer(TransportLayer tl, UI ui) {
		this.tl = tl;
		this.ui = ui;
	}
	
	public ApplicationLayer(UI ui) {
		this.ui = ui;
	}
	
	public ApplicationLayer(TransportLayer tl) {
		this.tl = tl;
		ui = new SimpleUI(this);
	}
	
	/**
	 * method to set the transport layer for the application layer
	 * @param tl the transport layer
	 */
	public void setTransportLayer(TransportLayer tl) {
		this.tl = tl;
	}
	
	/**
	 * method to receive a message from others, transport layer can call this method
	 * @return the message as a string
	 */
	public void receiveMessage(int sourceAdress, String msg) {
		ui.showMessage(sourceAdress, msg);
	}
	
	/**
	 * method to send the message, invokes a function in the lower layer
	 * @param msg the message that should be sent as a String
	 */
	public void sendMessage(String msg) {
		try {
			tl.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
