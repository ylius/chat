import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

	// The client socket
	private static Socket clientSocket = null;
	// The input stream
	private static BufferedReader inReader = null;
	// The output stream
	private static PrintStream outWriter = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	public static void main(String[] args) {

		// The default port.
		int portNumber = 2222;
		// The default host.
		String host = "localhost";

		if (args.length > 0) {
			portNumber = Integer.valueOf(args[0]).intValue();
		}
		
		// Open a socket on a given host and port. Open input and output streams.
		try {
			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			outWriter = new PrintStream(clientSocket.getOutputStream());
			inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
			return;
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host " + host);
			return;
		}
		
		System.out.println("Usage: java MultiThreadChatClient <portNumber>\n" + "Now using host = " + host
				+ ", portNumber = " + portNumber);

		// If everything has been initialized then we want to write some data to
		// the socket we have opened a connection to on the port portNumber.
		if (clientSocket != null && outWriter != null && inReader != null) {
			try {
				// Create a thread to read from the server.
				new Thread(new Client()).start();
				while (!closed) {
					outWriter.println(inputLine.readLine().trim());
				}

				// Close the output stream, close the input stream, close the socket.
				outWriter.close();
				inReader.close();
				clientSocket.close();
				return;
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * Create a thread to read from the server.
	 */
	public void run() {
		// Keep on reading from the socket till we receive "Bye" from the
		// server. Once we received that then we want to break.
		String responseLine;
		try {
			while ((responseLine = inReader.readLine()) != null) {
				System.out.println(responseLine);
				if (responseLine.indexOf("*** Bye") != -1)
					break;
			}
			closed = true;
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}
