package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.BetterUI;

class BetterUITest {
	
	BetterUI ui;
	
	final static String MESSAGE_1 = "hello";
	final static String MESSAGE_2 = "I love this test";
	final static String MESSAGE_3 = "This text is slightly longer!";
	final static String MESSAGE_4 = "Why do you have to do this?!";
	final static String MESSAGE_5 = "more, more!";
	final static String MESSAGE_6 = "give me more ideas...";
	final static String MESSAGE_7 = "lucky seven";
	final static String MESSAGE_8 = "(OwO)";
	final static String MESSAGE_9 = "last message!";
	
	private final PrintStream standardOut = System.out;
	private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
	
	@BeforeEach
	void setup() {
		ui = new BetterUI();
		System.setOut(new PrintStream(outputStreamCaptor));
	}
	
	@AfterEach
	void clear() {
		System.setOut(standardOut);
	}

	@Test
	void messageTest() {
		String[] input = {MESSAGE_1, MESSAGE_2, MESSAGE_3, MESSAGE_4};
		int i = 0;
		for (String s : input) {
			ui.showMessage(i, s);
			assertTrue(outputStreamCaptor.toString()
				      .trim().contains(i + ": " + s));
			i++;
		}
	}
	
	@Test
	void missedMessageTest() {
		String[] input = {MESSAGE_1, MESSAGE_2, MESSAGE_3, MESSAGE_4, MESSAGE_5, MESSAGE_6};
		int i = 0;
		Thread t = new Thread(ui);
		t.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String s : input) {
			ui.showMessage(i, s);
			assertFalse(outputStreamCaptor.toString()
				      .trim().contains(i + ": " + s));
			i++;
		}
		ui.getInput("test");
		i = 0;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String s : input) {
			assertTrue(outputStreamCaptor.toString()
				      .trim().contains(i + ": " + s));
			i++;
		}
	}

}
