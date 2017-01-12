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

// Klasa przechowuj�ca pul� w�tk�w
class PooledConnectionHandler implements Runnable { 

	protected Socket connection;

	// Sta�a pula w�tk�w Klient�w
	protected static List<Socket> pool = new LinkedList<Socket>(); 

	// Definicja strumieni
	public void handleConnection() {
		try {
			PrintWriter out = new PrintWriter(connection.getOutputStream(),
					true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			System.out.println("Wewn�trz metody handleConnection()");
			String line;
			while ((line = in.readLine()) != null) {
				out.println(line);
			}
			out.close();// Najpierw zawsze zamyka si� strumie� wyj�ciowy
			in.close();
			connection.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// wykonanie zadania jakim jest dodanie Klienta do puli Klient�w (w�tk�w)
	public static void processRequest(Socket requestToHandle) {
		// zapewnienie �e nikt nie mo�e modyfikowa�
		// metody podczas dodawania w�tk�w Klient�w
		synchronized (pool) {

			// dodanie w�tk�w Klient�w do puli
			pool.add(pool.size(), requestToHandle);
			System.out.println("pool.add(requestSocket)"); 

			// obudzenie wszystkich w�tk�w Klient�w - teraz dost�pne wszystkie 
			// gotowe do realizacji zada�
			pool.notifyAll(); 

		}
	}

	public void run() {
		while (true) {
			synchronized (pool) {

				// Je�li pusta pula watk�w Kient�w to wstrzymaj zadanie i
				// zaczekaj a� jaki� zostanie dodany
			
				while (pool.isEmpty()) {
					try {
						System.out.println("pool.wait()");
						pool.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
				// zdejmi z puli W�tk�w tego klienta
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

	// limit w�tk�w
	public void setUpHandler() {
		for (int i = 0; i < maxConnections; i++) {
			PooledConnectionHandler currentHandler = new PooledConnectionHandler();
			Thread t = new Thread(currentHandler, "W�tek " + i);
			System.out.println("W�tek " +i);
			t.start();
		}

	}

	// akceptacja po��czenia Server/Client
	public void acceptConnection() {
		/*** Server nas�uchuje na porcie 3000 ***/
		try {
			server = new ServerSocket(3000);
			while (true) {

				// Akceptuj port do nas�uchiwania
				client = server.accept();

				// zadanie dodania kolejnego Klienta do puli w�tk�w
				handleConnection(client);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Przeprowad� zadanie dodania do puli w�tk�w Klient�w
	private void handleConnection(Socket client2) {
		PooledConnectionHandler.processRequest(client2);
	}

	public static void main(String args[]) {

		ServerPoolExample poolServer = new ServerPoolExample(3000, 3);
		poolServer.setUpHandler();
		poolServer.acceptConnection();

	}
}
