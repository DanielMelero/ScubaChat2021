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

public class SimpleUI implements UI{
	
	ApplicationLayer layer;
	
	public SimpleUI(ApplicationLayer layer) {
		this.layer = layer;
	}
	
	public SimpleUI() {
		this(new ApplicationLayer());
	}

	public void getInput(String input) {
		layer.sendMessage(input);
	}
	
	public void showMessage(String msg) {
		System.out.println(msg);
	}
	
	public static void main(String[] args) {
		SimpleUI ui = new SimpleUI();
		InputHandler ih = new InputHandler(ui);
		ih.start();
	}

}
