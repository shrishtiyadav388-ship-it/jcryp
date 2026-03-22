package jcrypt;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    static int MAX_THREADS = 10;
    static AtomicInteger clientCounter = new AtomicInteger(0);

    public static void main(String[] args) {

        System.out.println("   J-Crypt Server");
        System.out.println();
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

        try {
            ServerSocket serverSocket = new ServerSocket(Constants.PORT);
            System.out.println("Server started on port " + Constants.PORT);
            System.out.println("Waiting for clients to connect...");
            System.out.println("Press Ctrl+C to stop the server");
            System.out.println();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientNum = clientCounter.incrementAndGet();

                System.out.println("New client connected! Client number: " + clientNum
                        + " IP: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler handler = new ClientHandler(clientSocket, clientNum);
                threadPool.execute(handler);
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}