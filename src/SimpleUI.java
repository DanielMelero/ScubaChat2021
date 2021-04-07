package src;

import java.util.Scanner;

/**
 * a simple CLI for testing
 * 
 * @author Kento Verlaan
 * 
 * this CLI is very basic and should be improved on a later stage
 *
 */

public class SimpleUI {
	
	// this class handles the input from the user as a separate thread
	class InputHandler extends Thread{
		Scanner sc;
		ApplicationLayer layer;
		
		public InputHandler(ApplicationLayer layer) {
			sc = new Scanner(System.in);
			this.layer = layer;
		}
		
		public void run() {
			while(true) {
				System.out.print("> ");
				String input = sc.nextLine();
				layer.sendMessage(input);
			}
		}
	}
	
	public void showMessage(String msg) {
		System.out.println(msg);
	}
	
	public static void main(String[] args) {
		
	}

}
