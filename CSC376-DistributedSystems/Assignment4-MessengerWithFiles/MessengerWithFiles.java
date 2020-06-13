/* 
 * Stephen Kim
 * CSC 376 - Heart
 * Messenger With Files Lab
 * 
 * A direct messenger program. In addition to transmitting and receiving
 * messages, the program will also transfer files using a binary transfer 
 * method, meaning that all of the bytes of a files with be
 * transmitted exactly as is.
 */
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;


public class MessengerWithFiles {

	//Listen class
	class Listen implements Runnable {
		BufferedReader reader;
		DataOutputStream output;
		boolean isServer;
		int listenPort;

		//Constructor
		public Listen(BufferedReader reader, DataOutputStream output, boolean isServer, int listenPort) {
			this.reader = reader;
			this.output = output;
			this.isServer = isServer;
			this.listenPort = listenPort;

		}

		@Override
		public void run() {
			try {
				String localInput = "";

				while (localInput != null) {
					//Create menu for user
					System.out.println("Enter an opiton ('m', 'f', 'x'):\n" + "(M)essage (send)\n"
							+ "(F)ile (request)\n" + "e(X)it ");
					localInput = reader.readLine();
					//If input is m, user can input message
					if (localInput.equals("m")) {
						System.out.println("Enter your message:");
						localInput = reader.readLine();
						output.writeUTF(localInput);
					//If input is f, user can send file
					} else if (localInput.equals("f")) {
						System.out.println("Which file do you want?");
						String fileName = reader.readLine();
						//Create new messenger
						MessengerWithFiles messenger = new MessengerWithFiles();
						//Transfer file on listener port
						FileTransfer receive = messenger.new FileTransfer(fileName, isServer, listenPort);
						Thread receiveThread = new Thread(receive);
						receiveThread.setDaemon(true);
						receiveThread.start();
					//If user input is x, exit 
					} else if (localInput.equals("x")) {
						break;
					} else {
						System.out.println("Invalid");
					}
				}
				output.writeUTF("");
			
			} catch (IOException e) {
				//Exit
				System.exit(1);
			}

		}

	}

	//Transfer files class
	class FileTransfer implements Runnable {
		String fileName;
		boolean isServer;
		int listenPort;

		//Constructor
		public FileTransfer(String fileName, boolean isServer, int listenPort) {
			this.fileName = fileName;
			this.isServer = isServer;
			this.listenPort = listenPort;

		}

		@Override
		public void run() {
			try {
					Socket clientSocket = new Socket("localhost", lPort);
					DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream input = new DataInputStream(clientSocket.getInputStream());
					output.writeUTF(fileName);
					FileOutputStream fileOut = new FileOutputStream(fileName);
					int number_read;
					byte[] buffer = new byte[1500];
					while ((number_read = input.read(buffer)) != -1) {
						fileOut.write(buffer, 0, number_read);
					}
					fileOut.close();
					clientSocket.close();

			} catch (Exception e) {
				System.exit(1);
			}

		}

	}

	class ConnectC implements Runnable {
		int lPort;

		public ConnectC(int lPort) {
			this.lPort = lPort;

		}

		@Override
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(lPort);
				while (true) {
					Socket clientSocket = serverSocket.accept();
					DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream input = new DataInputStream(clientSocket.getInputStream());
					String fileName = input.readUTF();
					File file = new File(fileName);
					FileInputStream file_input = new FileInputStream(file);
					byte[] file_buffer = new byte[1500];
					int number_read;
					while ((number_read = file_input.read(file_buffer)) != -1) {
						output.write(file_buffer, 0, number_read);
					}
					file_input.close();
					clientSocket.close();
				}

			} catch (Exception e) {
				System.exit(1);
			}

		}

	}
	
	class ConnectS implements Runnable {
		ServerSocket serverSocket;

		public ConnectS(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;

		}

		@Override
		public void run() {
			try {
				while (true) {
					Socket clientSocket = serverSocket.accept();
					DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream input = new DataInputStream(clientSocket.getInputStream());
					String fileName = input.readUTF();
					File file = new File(fileName);
					FileInputStream file_input = new FileInputStream(file);
					byte[] file_buffer = new byte[1500];
					int number_read;
					while ((number_read = file_input.read(file_buffer)) != -1) {
						output.write(file_buffer, 0, number_read);
					}
					file_input.close();
					clientSocket.close();
				}

			} catch (Exception e) {
				System.exit(1);
			}

		}

	}
	
	//run client
	public static void client(int lPort, int sPort) throws InterruptedException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			Socket clientSocket = new Socket("localhost", sPort);
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			output.writeUTF(Integer.toString(lPort));
			MessengerWithFiles messenger = new MessengerWithFiles();
			ConnectC connect = messenger.new ConnectC(lPort);
			Listen listen = messenger.new Listen(reader, output, false, sPort);
			Thread listenThread = new Thread(listen);
			Thread connectThread = new Thread(connect);
			listenThread.setDaemon(true);
			listenThread.start();
			connectThread.setDaemon(true);
			connectThread.start();
			String message = "";

			while ((message = input.readUTF()) != null) {
				if (message.length() == 0) {
					break;
				} else {
					System.out.println(message);
				}

			}
			output.writeUTF("");
			clientSocket.close();
			System.exit(0);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	
	//run server
	public static void server(int port) throws InterruptedException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			ServerSocket serverSocket = new ServerSocket(port);
			Socket clientSocket = serverSocket.accept();
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			String lPortString = input.readUTF();
			int lPort = Integer.decode(lPortString);
			System.out.println(lPort);
			MessengerWithFiles messenger = new MessengerWithFiles();
			ConnectS connect = messenger.new ConnectS(serverSocket);
			Thread connectThread = new Thread(connect);
			Listen listen = messenger.new Listen(reader, output, true, lPort);
			Thread listenThread = new Thread(listen);
			listenThread.setDaemon(true);
			listenThread.start();
			connectThread.setDaemon(true);
			connectThread.start();
			String message = "";

			while ((message = input.readUTF()) != null) {
				if (message.length() == 0) {
					break;
				} else {
					System.out.println(message);
				}

			}
			output.writeUTF("");
			serverSocket.close();
			clientSocket.shutdownOutput();
			clientSocket.close();
			System.exit(0);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	public static void main(String[] args) throws NumberFormatException, InterruptedException {
		if (args.length == 2) {

			server(Integer.decode(args[1]));

		} else {

			client(Integer.decode(args[1]), Integer.decode(args[3]));
		}

	}

}
