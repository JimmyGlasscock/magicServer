package testServer;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class ConnectionHandler implements Runnable{
	
	Socket s;
	Main m;
	
	public ConnectionHandler(Socket client) {
		s = client;
		m = new Main();
	}
	@Override
	public void run() {
		DataInputStream dis;
		try {
			dis = new DataInputStream(s.getInputStream());
			while(true) {
				String  str =(String)dis.readUTF();  
				
				processInput(str, dis);	
				
				if(s.isClosed()) {
					restartConnection();
				}
			}
		}catch(EOFException e1) {
			restartConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}  
		
	}
	
	public void processInput(String str, DataInputStream dis) {
		if(str.equalsIgnoreCase("spamMouse")) {
			try {
				Robot rob = new Robot();
				Random rand = new Random();
				for(int i = 0; i < 5000; i++) {
					int X = rand.nextInt(1000);
					int Y = rand.nextInt(1000);
					rob.mouseMove(X, Y);
				}
			} catch (AWTException e) {
				e.printStackTrace();
			}
			
		}else {
			try {
				Process openCalc = Runtime.getRuntime().exec(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void restartConnection() {
		m.start();
	}

}
