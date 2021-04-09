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
		
	}
	
	public void setApplicationLayer(ApplicationLayer layer) {
		this.layer = layer;
	}

	public String getInput(String input) {
		//layer.sendMessage(input);
		return input;
	}
	
	public void showMessage(int sourceAddress, String msg) {
		System.out.println(msg);
	}
	
	public static void main(String[] args) {
		SimpleUI ui = new SimpleUI();
		InputHandler ih = new InputHandler(ui);
		ih.start();
	}

}
