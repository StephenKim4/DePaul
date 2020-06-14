/*
 * Stephen Kim
 * CSC 376 - Heart
 * Final: Chat with File Transfers
 * 
 * ChatClient.java - The user will interact with the client through
 * standard input. The port number on which the client will listen for 
 * file requests is also provided on the command line, along with the
 * port number of the chat server. The port number that the client will
 * listen on is designated with an -l option, while -p specifies the
 * port number of the chat server.
 */


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;



public class ChatClient {

	public static void main(String[] CC) {
		
		//Convert CC to a list
		List<String> args = Arrays.asList(CC);
		
		//Create boolean variables to check for -l and -p
		boolean hasL = args.contains("-l");
		boolean hasP = args.contains("-p");
		
		//Create variables for listen port and server port
		int listenerPort = 0;
		int serverPort = 0;
		String address = "localhost";

		//If input does not contain -l or -p then exit
		if (!hasL || !hasP) { // Must contain -l
			System.out.println("Incorrect Parameters");
			System.exit(0);
		}
		
		//Listening port number should be right after -l
		listenerPort = Integer.valueOf(args.get(args.indexOf("-l") + 1)); 
		//Server port number should be after -p
		serverPort = Integer.valueOf(args.get(args.indexOf("-p") + 1));
		//Run startClient with listener port and server port established
		startClient(listenerPort, serverPort);
	}
	
	private static String fileName;
	
	//Menu class implementing runnable interface
	public static class Menu implements Runnable {
		private DataOutputStream output;
		private int listenerPort;

		//Menu constructor
		public Menu(DataOutputStream output, int port) {
			this.output = output;
			this.listenerPort = port;
		}
		
		//Override run method
		@Override
		public void run() {
			try {
				Scanner in = new Scanner(System.in);
				String selection = "";
				boolean exit = false;
				//While select is not null and exit is not false, print the menu for user
				while (selection != null && !exit) {
					System.out.println("Enter an option ('m', 'f', 'x'):\n" + "  (M)essage (send)\n"
							+ "  (F)ile (request)\n" + " e(X)it"); 
					//Store user's selection		
					selection = in.nextLine();
					
					//Switch statement for menu options 
					switch (selection) {
					//If user selects m, send a message
					case "m":
						System.out.println("Enter your message:");
						String message = in.nextLine();
						//Write message to current output stream
						output.writeUTF(message);
						break;
					//If user selects f, send file	
					case "f":
						System.out.println("Who owns the file?");
						//Enter name of user who owns the file
						String user = in.nextLine();
						System.out.println("Which file do you want?");
						//Enter name of file and send file
						String fileName = in.nextLine();
						ChatClient.fileName = fileName;
						output.writeUTF("$" + user);
						break;
					//If user selects x, exit	
					case "x":
						exit = true;
						break;
					default:
						break;
					}
				}
				System.exit(0);
			} catch (Exception e) {
				System.exit(1);
			}
		}

	}
	
	//Listen class implements Runnable interface
	public static class Listen implements Runnable {
		int listenerPort;
		
		//Listen constructor
		public Listen(int port) {
			this.listenerPort = port;
		}
		
		//Override run method
		@Override
		public void run() {
			try {
				//Create serversocket
				ServerSocket serverSocket = new ServerSocket(listenerPort);
				//Continue to listen until closed
				while(true) {
					//Accept incoming request to socket
					Socket clientSocket = serverSocket.accept();
					//Create output and input streams
					DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream input = new DataInputStream(clientSocket.getInputStream());
					//Decode input to string
					String fileName = input.readUTF();
					//Create new file from filename
					File file = new File(fileName);
					//Create file input stream for file
					FileInputStream fileInput = new FileInputStream(file);
					//Create buffer
					byte[] buffer = new byte[1500];
					int number_read;
					
					while ((number_read = fileInput.read(buffer)) != -1) {
						output.write(buffer, 0, number_read);
					}
					//Close
					fileInput.close();
					clientSocket.close();
				}
			} catch (Exception e) {
				System.exit(3);
			}
		}
	}
	
	//File transfer class implements runnable interface
	public static class FileTransfer implements Runnable {
		private String fName;
		private int lPort;

		//FileTransfer constructor takes file name and listener port as arguments
		public FileTransfer(String fName, int lPort) {
			this.fName = fName;
			this.lPort = lPort;
		}

		@Override
		public void run() {
			try {
				//Create client socket
				Socket clientSocket = new Socket("localhost", lPort);
				//Create input and output streams
				DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
				DataInputStream input = new DataInputStream(clientSocket.getInputStream());
				//Output file name
				output.writeUTF(fName);
				//Create stream for file and transfer
				FileOutputStream fOut = new FileOutputStream(fName);
				int number_read;
				byte[] buffer = new byte[1500];
				while ((number_read = input.read(buffer)) != -1)
					fOut.write(buffer, 0, number_read);
				//Close
				fOut.close();
				clientSocket.close();
			} catch (Exception e) {
				System.exit(2);
			}

		}
	}
	
	//Start client function - takes listener port and server port as arguments
	private static void startClient(int listenerPort, int serverPort) {
		try {
			Socket clientSocket = new Socket("localhost", serverPort);
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());

			//First message is always the name
			Scanner in = new Scanner(System.in);
			System.out.println("What is your name?");
			String name = in.nextLine();
			//Confirmation message
			System.out.println("Sending name to server...");
			output.writeUTF(name);
			//Sort ports
			output.writeUTF(Integer.toString(listenerPort));
			//Create listen object with listener port
			Listen listen = new Listen(listenerPort);
			//Create new menu 
			Menu menu = new Menu(output, serverPort);
			//Create and start threads
			Thread listenThread = new Thread(listen);
			Thread selectThread = new Thread(menu);		
			listenThread.start();
			selectThread.start();
			
			String msg = "";
			//Continue if the message is not null
			while((msg = input.readUTF()) != null) {
				//If message is blank break
				if(msg.length() == 0)
					break;
				//If message starts with $ transfer file
				if(msg.startsWith("$")) {
					int port = Integer.valueOf(msg.substring(1));
					FileTransfer transfer = new FileTransfer(fileName, port);
					Thread transferThread = new Thread(transfer);
					transferThread.start();
				}
				else
					System.out.println(msg);
			}
			//Close all
			clientSocket.shutdownOutput();
			clientSocket.close();
			System.exit(0);
		} catch(Exception e) {
			System.exit(1);
		}
	}

}
