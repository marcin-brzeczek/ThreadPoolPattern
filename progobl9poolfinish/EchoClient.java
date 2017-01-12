package progobl9poolfinish;

import java.io.*;
import java.net.*;
 
public class EchoClient {
    public static void main(String[] args) throws IOException {
 
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
 
        try {
            echoSocket = new Socket("localhost", 3000);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
            echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Nie znany localhost");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Nie mo¿na uzyskaæ po³¹czenia");
            System.exit(1);
        }
 
        BufferedReader stdIn = new BufferedReader(
        new InputStreamReader(System.in));
        String userInput;
 
        System.out.println("Type a message: ");
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            System.out.println("echo: " + in.readLine());
        }
 
        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
    }
}


