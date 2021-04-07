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
	Scanner sc;
	UI ui;

	public InputHandler(UI ui) {
		sc = new Scanner(System.in);
		this.ui = ui;
	}

	public void run() {
		while (true) {
			System.out.print("> ");
			String input = sc.nextLine();
			ui.getInput(input);
		}
	}
}
