/*
* Stephen Kim
* CSC 376 - Heart
* Chat Lab - ChatServer.java
* 
* Video Link - https://youtu.be/a3Y0tXZGMGM 
*/

import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatServer {

	//Create socket and thread
	private ServerSocket serverSocket;
	private ExecutorService thread;
	//Create hashmap to keep track of clients
	private HashMap<Socket, PrintWriter> socketMap;

	public static void main(String[] args) {
		//Get port number specified by the user
		int portNum = Integer.parseInt(args[0]);
		//Create new server with the port number
		ChatServer server = new ChatServer(portNum);
		//Let server handle clients
		server.clientControl();
		//Close server when threads are done
		server.close();
	}
	
	//ChatServer constructor
	private ChatServer(int portNum) {
		try {
			//Try to get current instance of server socket
			this.serverSocket = new ServerSocket(portNum);
			//Create new instance of hashmap
			this.socketMap = new HashMap<>();
			//Start a new thread with fixed pool of 10 threads
			this.thread = Executors.newFixedThreadPool(10);
		//Handle any exception	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	//Let server handle when clients are trying to connect
	private void clientControl() {
		//Listen for clients
		while (true) {
			try {
				//Accept the client
				Socket client = serverSocket.accept();
				//Get the output stream from the client
				PrintWriter outputData = new PrintWriter(client.getOutputStream(), true);
				//Put the new client and output data in the socket map
				socketMap.put(client, outputData);
				//The thread that was started calls on messageControl and passes client socket
				//Send and receive messages between clients
				thread.submit(() -> messageControl(client));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	//Allow message to be sent to all clients
	private void messageControl(Socket s) {
		try {
			//Variable for client messages and client name
			String clientMessages;
			String clientName = null;
			//Read message from the input stream
			BufferedReader inputData = new BufferedReader(new InputStreamReader(s.getInputStream()));
			//Read input while it is not null
			while ((clientMessages = inputData.readLine()) != null) {
				//If client name isn't given, first user input is client name
				if (clientName == null) {
					clientName = clientMessages;
				} else {
					//Send message to all clients besides itself
					for (Socket socket : socketMap.keySet()) {
						if (!socket.equals(s)) {
							socketMap.get(socket).println(clientName + ": " + clientMessages);
						}
					}
				}
			}
			//Remove the socket from the hashmap once the connection is terminated
			socketMap.remove(socketMap);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	
	
	//Stop all threads
	private void close() {
		thread.shutdown();
	}

}
