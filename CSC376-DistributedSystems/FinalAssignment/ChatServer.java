/*
 * Stephen Kim
 * CSC 376 - Heart
 * Final: Chat with File Transfers
 * 
 * ChatServer.java - The server will listen for connections from clients. 
 * Immediately after receiving a message, the server must forward the 
 * message and the name of the user who sent the message to all 
 * connected clients, except for the client that sent the message. Do
 * not send the message back to the originating client!
 */


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ChatServer {
	
	//Create a client which implements runnable interface
	static class ClientObject implements Runnable {
		
		private String name;
		private Socket clientSocket;
		private DataOutputStream output;
		private DataInputStream input;
		private int lPort;
		
		//Client constructor
		public ClientObject(String name, Socket socket, DataOutputStream output, DataInputStream input, int lPort) {
			this.name = name;
			this.clientSocket = socket;
			this.output = output;
			this.input = input;
			this.lPort = lPort;
		}
		
		@Override
		public void run() {
			//Continue to accept messages
			while (true) { 
				try {
					//Read the message
					String message = input.readUTF();
					if(message.startsWith("$")) {
						//Get users name and message
						String transfer = message.substring(1);
						for(ClientObject client : ChatServer.clientsAL) {
							if(client.getName().equals(transfer)) {
								output.writeUTF("$" + client.getLPort());
							}
						}
					} else {
						//Forward message to all clients except sender
						for(ClientObject client : ChatServer.clientsAL) {
							if(!client.getName().equals(name)) {
								client.getOutput().writeUTF(name + ": " + message);
							}
						}
					}
					
				} catch(Exception e) {
					try {
						clientSocket.close();
					} catch (Exception e2) {
					}
					
					ChatServer.clientsAL.remove(this);
				}
			}
		}
		
		//Get user name
		public String getName() {
			return name;
		}

		//Get client socket
		public Socket getSocket() {
			return clientSocket;
		}

		//Get message
		public DataOutputStream getOutput() {
			return output;
		}

		//Get listener port
		public int getLPort() {
			return lPort;
		}
		
	}
	
	private static ServerSocket serverSocket;
	private static ArrayList<ClientObject> clientsAL;
	
	public static void main(String[] args) {
		int port = 0;
		
		//No port number, so exit
		if (args.length != 1) 
			System.exit(0);
		
		
		try { 
			//Get port number
			port = Integer.valueOf(args[0]);
			//Invalid port number, exit
		} catch (IndexOutOfBoundsException | NumberFormatException e) { 
			System.exit(0);
		}
		
		//Start server with port number
		startServer(port);
		
	}

	//Function to start server
	private static void startServer(int port) {
		try {
			//Create server socket
			serverSocket = new ServerSocket(port);
			//Create list of clients
			clientsAL = new ArrayList<ClientObject>();
			Socket clientSocket;
			while(true) {
				//Create client socket and input, output streams
				clientSocket = serverSocket.accept();
				DataInputStream input = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
				
				String name = input.readUTF();
				int lPort = Integer.valueOf(input.readUTF());
				//Create new client
				ClientObject serverReceive = new ClientObject(name, clientSocket, output, input, lPort);
				//Create client thread
				Thread clientThread = new Thread(serverReceive);
				//Add client to client list
				clientsAL.add(serverReceive);
				//Start thread
				clientThread.start();
			}
		} catch (IOException e) {
		}
	}
	


}
