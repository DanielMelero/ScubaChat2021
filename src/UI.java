package src;

/**
 * UI interface for multiple version of the UI
 * 
 * @author Kento Verlaan
 * 
 * this interface is here to make the creation of new UIs easier
 *
 */
public interface UI {
	
	// method for getting input from other classes
	public String getInput(String input);
	
	// method for showing incoming messages
	public void showMessage(int sourceAddress, String msg);

}
