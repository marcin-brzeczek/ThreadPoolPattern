package progobl9poolfinish;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/*** Wzorzec THREAD POOL ***/

// Klasa przechowuj¹ca pulê w¹tków
class PooledConnectionHandler implements Runnable { 

	protected Socket connection;

	// Sta³a pula w¹tków Klientów
	protected static List<Socket> pool = new LinkedList<Socket>(); 

	// Definicja strumieni
	public void handleConnection() {
		try {
			PrintWriter out = new PrintWriter(connection.getOutputStream(),
					true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			System.out.println("Wewn¹trz metody handleConnection()");
			String line;
			while ((line = in.readLine()) != null) {
				out.println(line);
			}
			out.close();// Najpierw zawsze zamyka siê strumieñ wyjœciowy
			in.close();
			connection.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// wykonanie zadania jakim jest dodanie Klienta do puli Klientów (w¹tków)
	public static void processRequest(Socket requestToHandle) {
		// zapewnienie ¿e nikt nie mo¿e modyfikowaæ
		// metody podczas dodawania w¹tków Klientów
		synchronized (pool) {

			// dodanie w¹tków Klientów do puli
			pool.add(pool.size(), requestToHandle);
			System.out.println("pool.add(requestSocket)"); 

			// obudzenie wszystkich w¹tków Klientów - teraz dostêpne wszystkie 
			// gotowe do realizacji zadañ
			pool.notifyAll(); 

		}
	}

	public void run() {
		while (true) {
			synchronized (pool) {

				// Jeœli pusta pula watków Kientów to wstrzymaj zadanie i
				// zaczekaj a¿ jakiœ zostanie dodany
			
				while (pool.isEmpty()) {
					try {
						System.out.println("pool.wait()");
						pool.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
				// zdejmi z puli W¹tków tego klienta
				connection = pool.remove(0); 
				System.out.println("Connenction remove(0)"+ connection);
			}
			// Ustaw strumienie 
			System.out.println("handleConnection");
			handleConnection();

		}
		// TODO Auto-generated method stub
	}

}

public class ServerPoolExample {

	ServerSocket server = null;
	Socket client = null;
	protected int listenPort;
	protected int maxConnections;

	public ServerPoolExample(int listenPort, int maxConnections) {
		this.listenPort = listenPort;
		this.maxConnections = maxConnections;
	}

	// limit w¹tków
	public void setUpHandler() {
		for (int i = 0; i < maxConnections; i++) {
			PooledConnectionHandler currentHandler = new PooledConnectionHandler();
			Thread t = new Thread(currentHandler, "W¹tek " + i);
			System.out.println("W¹tek " +i);
			t.start();
		}

	}

	// akceptacja po³¹czenia Server/Client
	public void acceptConnection() {
		/*** Server nas³uchuje na porcie 3000 ***/
		try {
			server = new ServerSocket(3000);
			while (true) {

				// Akceptuj port do nas³uchiwania
				client = server.accept();

				// zadanie dodania kolejnego Klienta do puli w¹tków
				handleConnection(client);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// PrzeprowadŸ zadanie dodania do puli w¹tków Klientów
	private void handleConnection(Socket client2) {
		PooledConnectionHandler.processRequest(client2);
	}

	public static void main(String args[]) {

		ServerPoolExample poolServer = new ServerPoolExample(3000, 3);
		poolServer.setUpHandler();
		poolServer.acceptConnection();

	}
}
