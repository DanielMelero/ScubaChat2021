package src;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class BetterUI implements UI, Runnable {
	
	// these are used for the menu
	private static final char MENU = 'm';
	private static final char COUNT = 'c';
	private static final char EXIT = 'e';
	private static final char ORIGINS = 'o';
	private static final char SET = 's';
	
	// instances used in this class
	private InputHandler ih;
	private ApplicationLayer al;
	
	// variables used to store the conversation
	private Map<Integer, String> log;
	private Map<Integer, String> users;
	private Set<Integer> origins;
	private int current = 0;
	
	// Dynamic variables used to change the behavior of the code depending on condition
	private boolean inputMode = false;
	private String lastInput;
	private boolean gotInput;
	private int nextMessage;
	private boolean exit = false;
	
	public BetterUI(ApplicationLayer al) {
		log = new HashMap<>();
		users = new HashMap<>();
		inputMode = false;
		origins = new HashSet<>();
		this.al = al;
	}
	public BetterUI() {
		log = new HashMap<>();
		users = new HashMap<>();
		inputMode = false;
		origins = new HashSet<>();
		this.al = new ApplicationLayer(this);
	}
	
	/**
	 * method that starts the input handler as a separate thread
	 */
	private void startInputHandler() {
		ih = new InputHandler(this);
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
		
		// put the address in the origins set for later use
		if (!origins.contains(sourceAddress)) {
			origins.add(sourceAddress);
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
		if (!users.containsKey(sourceAddress)) {
			System.out.println("address not known but users name was added");
		} else {
			System.out.println("user name added");
		}
		users.put(sourceAddress, name);
		
	}
	
	/**
	 * method to show the nodes that where in the conversation
	 */
	private synchronized void showOrigins() {
		for (int i : origins) {
			if (users.containsKey(i)) {
				System.out.println(i + ": " + users.get(i));
			} else {
				System.out.println(i);
			}
		}
		
	}
	
	private void getNodes() {
		// get the available nodes at the time
		int[] nodes = al.getTransportLayer().getNetworkLayer().getRouting().getNeededAcknowledgements();
		// print it out
		System.out.println("The available nodes are currently:");
		for (int i : nodes) {
			System.out.print(i + ", ");
		}
		System.out.println();
	}
	
	/**
	 * method to show menu
	 */
	private void showMenu() {
		
		System.out.println("Available commands");
		System.out.println("'m': show menu");
		System.out.println("'c': show count of all messages");
		System.out.println("'o': show all addresses of nodes present in the conversation");
		System.out.println("'e': exit the programm");
		System.out.println("'s': set names for specific addresses");
		System.out.println("usage: s [address] [name of node]");
		
	}
	
	/**
	 * enter a input mode, where commands can be put in
	 */
	public void inputMode() {
		// set inputMode boolean to true and clear console if possible
		inputMode = true;
		//clearConsole();

		// store last shown message -> used in later check for missed messages
		nextMessage = current;
		
		// print instructions
		System.out.println("Input mode: \nEnter 'm' for available commands");
		System.out.println("input message and press Enter or press Enter to exit");
		
		a: // break point for exit function
		while (true) {

			// wait for input

			synchronized (this) {
				gotInput = false;
				if (!gotInput) {
					// using a separate scanner as the inputhandler is will be in a dead lock if
					// wait() is used
					System.out.print("> ");
					String input = new Scanner(System.in).nextLine();
					getInput(input);
				}
			}

			// read input

			// break out of input mode when the return key is pressed
			if (lastInput.length() == 0) {
				break;
			}

			// if menu option is detected
			// TODO: add more options and make exit work
			
			// if SET option is used
			if (lastInput.charAt(0) == SET && lastInput.length() > 1) {
				String[] arguments = lastInput.split(" ");
				if (arguments.length == 3) {
					// check for enough arguments
					if (isNumeric(arguments[1])) {
						// add user name
						addUser(Integer.parseInt(arguments[1]), arguments[2]);
						continue;
					} else {
						// give error message
						System.out.println("unknown command try again");
						continue;
					}
				}
			}
			
			if (lastInput.length() == 1) {
				switch (lastInput.charAt(0)) {

				case MENU:
					showMenu();
					break;
				case COUNT:
					System.out.println("the message count is: " + log.size());
					break;
				case EXIT:
					exit = true;
					System.out.println("exiting program...\nall messages that came will be displayed");
					break a;
				case ORIGINS:
					//showOrigins();
					getNodes();
					break;
				default:
					System.out.println("unknown command try again");
					break;
				}
			} else {
				// else send text to application layer
				boolean error = false;
				try {
					al.sendMessage(lastInput);
				} catch (Exception e) {
					System.out.println("Error:" + e.getMessage());
					error = true;
				}
				if (error) {
					continue;
				}

				System.out.println("message sent");
				break;
			}
		}
		
		// display all messages that came during the input mode
		displayMissedMessage();
		inputMode = false;
		
		// exit program
		if (exit) {
			ih.end();
		}
	}
	
	/**
	 * method that check if the input is numeric
	 * @param str string that is being checked
	 * @return true if it is, false if it isn't
	 */
	private boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		try {
			int i = Integer.parseInt(str);
		} catch (Exception e) {
			return false;
		}
		return true;
	}


	@Override
	public void run() {
		System.out.println("Welcome to ScubChat!\nPress \"Enter\" to change to input mode.");
		showMenu();
		startInputHandler();
	}
	
	
	public static void main(String[] args) {
		BetterUI ui = new BetterUI();
		Thread t = new Thread(ui);
		t.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("Error:" + e.getMessage());
		}
		for (int i = 0; i < 100; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Error:" + e.getMessage());
			}
			ui.showMessage(i, "test message");
		}
	}

}
