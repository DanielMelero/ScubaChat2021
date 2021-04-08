package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.InputHandler;
import src.SimpleUI;

class UITest {
	
	SimpleUI ui;
	InputHandler ih;
	
	@BeforeEach
	void setUp() {
		ui = new SimpleUI();
		ih = new InputHandler(ui);
	}

	@Test
	void inputTest() {
		for (int i = 0; i < 10; i++) {
			assertEquals("test input nr." + i ,ui.getInput("test input nr." + i));
		}
	}
	
	@Test
	void visualTest() {
		ih.start();
		for (int i = 0; i < 10; i++) {
			ui.showMessage(0, "test message");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
