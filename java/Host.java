import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

/*
 * A chat server that delivers public and private messages.
 */
public class Host {

	// The server socket.
	private static ServerSocket serverSocket = null;
	// The client socket.
	private static Socket clientSocket = null;

	private static final int MAX_NUM_CLIENTS = 6;
	private static final int MAX_NUM_ROOMS = 5;
	private static final List<ServerThread[]> rooms = new ArrayList<>();

	public static void main(String args[]) {

		// The default port number.
		int portNumber = 2222;
		if (args.length > 0) {
			portNumber = Integer.valueOf(args[0]).intValue();
		}

		/*
		 * Open a server socket on the portNumber (default 2222). Note that we
		 * can not choose a port less than 1023 if we are not privileged users
		 * (root).
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
			return;
		}
		System.out.println(
				"Usage: java MultiThreadChatServerSync <portNumber>\n" + "Now using port number = " + portNumber);

		BufferedReader inputLine = new BufferedReader(new InputStreamReader(System.in));
		int numOfRooms = 1;
		while (true) {
			System.out.println("Enter the number of chat rooms you want to create (1 to " + MAX_NUM_ROOMS + "):");
			try {
				numOfRooms = Integer.valueOf(inputLine.readLine().trim());
				if (numOfRooms >= 1 && numOfRooms <= MAX_NUM_ROOMS) {
					break;
				} else {
					System.out.println("Invalid number of chat rooms.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Not a number.");
			} catch (IOException e) {
				System.out.println(e);
				return;
			}
		}
		for (int i = 0; i < numOfRooms; i++) {
			rooms.add(new ServerThread[MAX_NUM_CLIENTS]);
		}
		System.out.println(numOfRooms + " chat rooms created.");

		/*
		 * Create a client socket for each connection and pass it to a new
		 * server thread.
		 */
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				BufferedReader inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintStream outWriter = new PrintStream(clientSocket.getOutputStream());
				int roomNo = 1;
				while (true) {
					outWriter.println("Enter a chat room number, 1 to " + rooms.size());
					try {
						roomNo = Integer.valueOf(inReader.readLine().trim());
						if (roomNo >= 1 && roomNo <= rooms.size()) {
							break;
						} else {
							outWriter.println("Invalid chat room number.");
						}
					} catch (NumberFormatException e) {
						outWriter.println("Not a number.");
					} catch (IOException e) {
						System.out.println(e);
					}
				}
				enterRoom(rooms.get(roomNo - 1), outWriter);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

	private static void enterRoom(ServerThread[] threads, PrintStream outWriter) throws IOException {
		int i = 0;
		for (; i < threads.length; i++) {
			if (threads[i] == null) {
				(threads[i] = new ServerThread(clientSocket, threads)).start();
				break;
			}
		}
		if (i == threads.length) {
			outWriter.println("Server too busy. Try later.");
			outWriter.close();
			clientSocket.close();
		}
	}
}

class ServerThread extends Thread {

	private BufferedReader inReader = null;
	private PrintStream outWriter = null;
	private Socket clientSocket = null;
	private final ServerThread[] threads;

	public ServerThread(Socket clientSocket, ServerThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
	}

	public void run() {

		try {
			// Create input and output streams for this client.
			inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outWriter = new PrintStream(clientSocket.getOutputStream());
			String name;
			while (true) {
				outWriter.println("Enter your name.");
				name = inReader.readLine().trim();
				if (name.indexOf('@') == -1) {
					break;
				} else {
					outWriter.println("The name should not contain '@' character.");
				}
			}

			// Welcome the new the client.
			outWriter.println("Welcome " + name + " to our chat room.\nTo leave enter /quit in a new line.");
			synchronized (this) {
				for (int i = 0; i < threads.length; i++) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].outWriter.println("*** A new user " + name + " entered the chat room !!! ***");
					}
				}
			}

			// Start the conversation.
			while (true) {
				String line = inReader.readLine();
				if (line.startsWith("/quit")) {
					break;
				}

				synchronized (this) {
					for (int i = 0; i < threads.length; i++) {
						if (threads[i] != null) {
							threads[i].outWriter.println("<" + name + "> " + line);
						}
					}
				}
			}

			synchronized (this) {
				for (int i = 0; i < threads.length; i++) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].outWriter.println("*** The user " + name + " is leaving the chat room !!! ***");
					}
				}
			}
			outWriter.println("*** Bye " + name + " ***");

			// Clean up. Set the current thread variable to null so that a new
			// client could be accepted by the server.
			synchronized (this) {
				for (int i = 0; i < threads.length; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}

			// Close the input stream, close the output stream, close the
			// socket.
			inReader.close();
			outWriter.close();
			clientSocket.close();
		} catch (IOException e) {
		}
	}
}