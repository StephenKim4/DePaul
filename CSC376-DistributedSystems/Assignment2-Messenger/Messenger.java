/*
 * Stephen Kim
 * CSC 376 - Heart
 * Messenger Lab
 * A direct messenger program 
 * Video Link: https://youtu.be/zOxDJt4fbHE
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;


/*
 * Messenger class 
 */
class Messenger {
	
	//Main method
	public static void main(String[] args) throws IOException {
		
		//Port number
		int portNum;
		//Create server and client objects
		Server s = null;
		Client c = null;
		
		//If there are more than 2 inputs, print correct usage
		if ( args.length > 2 )	{
			System.out.println( "Usage:" );
			System.out.println( "Server -l <port number> \n Client <port number>");
			return;
		}
		else{
		try {
			//If first argument is -l then create, run, and stop server
			if (args[0].equals("-l")) {
				s = new Server(Integer.parseInt(args[1]));
				//Thread s = new Thread(thread1);
				//s.start();
				s.run();
				s.quit();
			}
			else {
				//If first argument is not -l, assign port to client
				//Create, run, and stop new client thread
				c = new Client(Integer.parseInt(args[0]));
				//Thread c = new Thread(thread2);
				//c.start();
				c.run();
				c.quit();
			}
		} finally {
			//Server and client are null
			if(s == null || c == null){
				return;
			}
		}
	}
}
}

/*
 * Server class that implements Runnable interface
 */
class Server implements Runnable {
	
	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter output;
	ExecutorService thread;
	BufferedReader reader;
	BufferedReader input;
	String message;

	//Server constructor
	public Server(int portNum) throws IOException {
		//ServerSocket using port number
		serverSocket = new ServerSocket(portNum);
		//Accept incoming request to socket
		clientSocket = serverSocket.accept();
		//Output object for messages with boolean for autoflushing
		output = new PrintWriter(clientSocket.getOutputStream(), true);
		//ExecutorService object provides methods for shutdown and
		//automatically provides pool of threads
		//Create thread pool that reuses fixed number of threads
		//operating off a shared unbounded queue
		thread = Executors.newFixedThreadPool(2);
		//Input message from standard input
    	reader = new BufferedReader(new InputStreamReader(System.in));
    	//Input object for messages
		input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	//Close method to reduce redundant code
	void close() {
		try {
			//Close server socket
			serverSocket.close();
			//Send remaining data followed by termination sequence
			clientSocket.shutdownOutput();
			//Close client socket
			clientSocket.close();
		} catch (Exception e) {}
	}
	
	//Read method for incoming messages
	public void read(){
		//Submit method returns a future to track asynchronous tasks
		//
		this.thread.submit(() -> {
			try {
				//While the client socket is connected to server
				while (this.clientSocket.isConnected()) {
					//If there is an incoming message greater than length 0
					//print message
					if((message = input.readLine()) != null && message.length() > 0) {
						System.out.println(message);
					}
					else {
						//Close input object
						input.close();
						//Call close method
						close();
					}
				}

			} catch (IOException e) {
			}
		});
	}
	
	//Write method to send messages
	public void write(){
		//Submit is overloaded to take a Runnable object
		this.thread.submit(() -> {
			try {
				//While client socket is connected
				while (this.clientSocket.isConnected()) {
					//If there is an message in standard input greater than 
					//length 0 send message
					if((message = reader.readLine()) != null && message.length() > 0) {
						output.println(message);
					}
					else {
						//Close standard input and call close method
						reader.close();
						close();
					}
				}
			} catch (IOException e) {
			}
		});
	}
	
	//Run method calls read and write for server object
	public void run() {
		read();
		write();
		
	}
	
	//Quit method to shut down 
	public void quit() {
		//Allow previously submitted tasks to execute before terminating
		thread.shutdown();
	}
}

/*
 * Client class that implements Runnable interface
 */
class Client implements Runnable {
	
	Socket clientSocket;
	PrintWriter output;
	ExecutorService thread;
	BufferedReader reader;
	BufferedReader input;
	String message;

	//Client constructor
	public Client(int portNum) throws IOException {
		//Use port number and local host to create client socket
		clientSocket = new Socket("localhost", portNum);
		//Create printwriter from clientsocket outputstream
		output = new PrintWriter(clientSocket.getOutputStream(), true);
		//Create thread pool with 2 threads
		thread = Executors.newFixedThreadPool(2);
		//Input messages from standard input
		reader = new BufferedReader(new InputStreamReader(System.in));
		//Create input object to read messages from client socket
		input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	//Close method to reduce redundancy
	void close() {
		try {
			//Send remaining data followed by termination sequence
			clientSocket.shutdownOutput();
			//Close client socket
      		clientSocket.close();
		} 
		catch (Exception e) {}
	}
	
	//Read method from incoming messages
	public void read(){
		this.thread.submit(() -> {
			try {
				//While client socket is connected
				while (clientSocket.isConnected()) {
					//If there is an incoming message, print to screen
					if((message = input.readLine()) != null && message.length() > 0) {
					System.out.println(message);
					}
					else {
						//Close input object and call close method
						input.close();
						close();
					}
				}

			} catch (IOException e) {
			}
		});
	}
	
	//Write method for sending messages
	public void write(){
		this.thread.submit(() -> {
			try {
				//While client socket is connected
				while (clientSocket.isConnected()) {
					//Read user input and send to server
					if((message = reader.readLine()) != null && message.length() > 0) {
					output.println(message);
					}
					else {
						//Close reader object and call close method
						reader.close();
						close();
					}
				}
			} catch (IOException e) {
			}
		});
	}
	
	//Run method that invokes read and write
	public void run() {
		read();
		write();
	}
	
	//Shutdown method that allows previously submitted tasks to finish
	public void quit() {
		thread.shutdown();
	}
}
