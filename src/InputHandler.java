package src;

import java.util.Scanner;

/**
 * InputHandler for UI
 * @author Kento Verlaan
 * 
 * This class is responsible for handling the input from the user
 *
 */
public class InputHandler extends Thread {
	private Scanner sc;
	private UI ui;
	private boolean exit = false;

	public InputHandler(UI ui) {
		sc = new Scanner(System.in);
		this.ui = ui;
	}
	
	public Scanner getScanner() {
		return sc;
	}
	
	public void end() {
		exit = true;
	}

	public void run() {
		while (!exit) {
			String input = sc.nextLine();
			ui.getInput(input);
		}
		sc.close();
	}
}
