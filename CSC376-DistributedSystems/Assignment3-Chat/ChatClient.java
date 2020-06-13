/*
* Stephen Kim
* CSC 376 - Heart
* Chat Lab - ChatClient.java
* 
* Video Link - https://youtu.be/a3Y0tXZGMGM 
*/

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {

	//Create socket and readers and writers
	private Socket clientSocket;
	private Scanner inputScanner;
	private PrintWriter outputStream;
	private Scanner lineInput;
	private String clientMessage;
	
	public static void main(String args[]) throws Exception {
		//Get the port number
		int portNum = Integer.valueOf(args[0]);
		//Create new client with port number
		ChatClient client = new ChatClient(portNum);
		//Start the client
		client.start();
	}

	private ChatClient(int portNum) {
		try {
			//Create and get input data as well as pass the messages along
			//Create client socker with local host and port number
			this.clientSocket = new Socket("localhost", portNum);
			//Create scanner to read user input
			this.inputScanner = new Scanner(new InputStreamReader(System.in));
			//Create print writer to send message
			this.outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
			//Create scanner to get input stream
			this.lineInput = new Scanner(new InputStreamReader(clientSocket.getInputStream()));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	//Start threads
	private void start() {
		//Create fixed thread pool of 10
		ExecutorService thread = Executors.newFixedThreadPool(10);
		//Start receive message thread
		thread.submit(this::receiveMessage);
		//Start send message thread
		thread.submit(this::sendMessage);
		//Shut down thread
		thread.shutdown();
	}

	//Send a message
	private void sendMessage() {
		//While input scanner has input
		while (inputScanner.hasNextLine()) {
			//Message in user input
			clientMessage = inputScanner.nextLine();
			//End if no message
			if (clientMessage.length() == 0) {
				break;
			}
			//Send client message to server
			outputStream.println(clientMessage);
		}
		//Call close
		close();
	}

	//Receive a message
	private void receiveMessage() {
		//If there is a line coming in
		while (lineInput.hasNextLine()) {
			//Message is incoming input
			clientMessage = lineInput.nextLine();
			//If message is empty break
			if (clientMessage.length() == 0) {
				break;
			}
			//Print out message
			System.out.println(clientMessage);
		}
		//Thread shut down
		close();
	}

	//Close function
	private void close() {
		try {
			//Shut down client socket
			clientSocket.shutdownOutput();
			clientSocket.close();
			//System exit
			System.exit(0);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
