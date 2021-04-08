package src;

import java.util.HashMap;
import java.util.Map;

public class BetterUI implements UI {
	
	private static final char MENU = 'm';
	
	
	private InputHandler ih;
	private Map<Integer, String> log;
	private Map<Integer, String> users;
	private int current = 0;
	
	private boolean inputMode;
	private String lastInput;
	
	public BetterUI() {
		log = new HashMap<>();
		users = new HashMap<>();
		inputMode = false;
		InputHandler ih = new InputHandler(this);
		ih.start();
	}

	@Override
	public String getInput(String input) {
		lastInput = input;
		notifyAll();
		return null;
	}

	/**
	 * this method stores the received message in a map to show it later
	 */
	@Override
	public void showMessage(int sourceAddress, String msg) {
		String origin = users.get(sourceAddress);
		log.put(current, origin + ": " + msg);
		current++;
		
	}
	
	/**
	 * method to show received messages on console
	 */
	private void displayMessage() {
		
	}
	
	public void addUser(int sourceAddress, String name) {
		users.put(sourceAddress, name);
	}
	
	/**
	 * method to clear the console
	 */
	private void clearConsole() {
		// ANSI escape code
		System.out.print("\033[H\033[2J");  
		System.out.flush();
	}
	
	/**
	 * enter a input mode, where commands can be put in
	 */
	public void inputMode() {
		clearConsole();
		inputMode = true;
		
		// print instructions
		System.out.println("Input mode: \n enter 'm' for available commands");
		// wait for input
		try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// read input
		
		// if menu option is detected
		if (lastInput.length() == 1) {
			switch(lastInput.charAt(0)) {
			
			case MENU:
				break;
			}
		} else {
			// else send text
		}
	}

}
