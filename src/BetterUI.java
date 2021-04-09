package src;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BetterUI implements UI, Runnable {
	
	private static final char MENU = 'm';
	private static final char COUNT = 'c';
	private static final char EXIT = 'e';
	
	
	private InputHandler ih;
	private ApplicationLayer al;
	
	private Map<Integer, String> log;
	private Map<Integer, String> users;
	private int current = 0;
	
	private boolean inputMode = false;
	private String lastInput;
	private boolean gotInput;
	private int nextMessage;
	private boolean exit = false;
	
	public BetterUI(ApplicationLayer al) {
		log = new HashMap<>();
		users = new HashMap<>();
		inputMode = false;
		this.al = al;
	}
	public BetterUI() {
		log = new HashMap<>();
		users = new HashMap<>();
		inputMode = false;
		this.al = new ApplicationLayer(this);
	}
	
	private void startInputHandler() {
		InputHandler ih = new InputHandler(this);
		ih.start();
	}

	@Override
	public String getInput(String input) {
		if (!inputMode) {
			inputMode();
			return null;
		} else {
			synchronized (this) {
				lastInput = input;
				gotInput = true;
				notifyAll();
			}
			return null;
		}
	}

	/**
	 * this method stores the received message in a map to show it later
	 */
	@Override
	public synchronized void showMessage(int sourceAddress, String msg) {
		String origin;
		// check if this address has a user name set, if not address is used
		if (users.containsKey(sourceAddress)) {
			origin = users.get(sourceAddress);
		} else {
			origin = Integer.toString(sourceAddress);
		}
		// put the message in a hashmap to later recall it
		log.put(current, origin + ": " + msg);
		
		// display message if not in input mode
		if (!inputMode) {
			displayMessage(current);
		}
		
		// increment message count
		current++;
		
	}
	
	/**
	 * method to show received messages on console
	 */
	private void displayMessage(int index) {
		System.out.println(log.get(index));
	}
	
	/**
	 * method that check if there were missed messages during the input mode and displays them
	 */
	private void displayMissedMessage() {
		if (current > nextMessage) {
			for (int i = nextMessage; i < current; i++) {
				displayMessage(i);
			}
		}
	}
	
	/**
	 * method to make add user names associated with source address
	 * @param sourceAddress the address of the user
	 * @param name the name of the user
	 */
	public void addUser(int sourceAddress, String name) {
		users.put(sourceAddress, name);
	}
	
	/**
	 * method to clear the console
	 */
	private void clearConsole() {
		try {
			final String os = System.getProperty("os.name");
			if (os.contains("Windows")) {
				Runtime.getRuntime().exec("cls");
			} else {
				Runtime.getRuntime().exec("clear");
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	// TODO: make a menu
	
	/**
	 * method to show menu
	 */
	private void showMenu() {
		
		System.out.println("WIP");
		
	}
	
	/**
	 * enter a input mode, where commands can be put in
	 */
	public void inputMode() {
		// set inputMode boolean to true and clear console if possible
		inputMode = true;
		clearConsole();

		// store last shown message -> used in later check for missed messages
		nextMessage = current;
		
		// print instructions
		System.out.println("Input mode: \nEnter 'm' for available commands");
		while (true) {

			// wait for input

			synchronized (this) {
				gotInput = false;
				if (!gotInput) {
					// using a separate scanner as the inputhandler is will be in a dead lock if
					// wait() is used
					String input = new Scanner(System.in).nextLine();
					getInput(input);
				}
			}

			// read input

			// break out of input mode when the return ley is pressed
			if (lastInput.length() == 0) {
				break;
			}

			// if menu option is detected
			// TODO: add more options and make exit work
			
			if (lastInput.length() == 1) {
				switch (lastInput.charAt(0)) {

				case MENU:
					showMenu();
					break;
				case COUNT:
					System.out.println("the message count is: " + log.size());
					break;
				case EXIT: // not working
					exit = true;
					break;
				default:
					System.out.println("try again");
					break;
				}
			} else {
				// else send text to application layer
				boolean error = false;
				while (true) {
					
					try {
						al.sendMessage(lastInput);
						break;
					} catch (Exception e) {
						System.out.println(e.getMessage());
						error = true;
						break;
					}
				}
				if(error) {
					continue;
				}
				
				System.out.println("message sent");
				break;
			}
		}
		
		// display all messages that came during the input mode
		displayMissedMessage();
		inputMode = false;
	}


	@Override
	public void run() {
		startInputHandler();
	}
	
	
	public static void main(String[] args) {
		BetterUI ui = new BetterUI();
		Thread t = new Thread(ui);
		t.start();
		for (int i = 0; i < 100; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ui.showMessage(i, "test message");
		}
	}

}
