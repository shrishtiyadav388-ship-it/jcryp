package jcrypt;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final int MAX_THREADS = 10;
    private static final AtomicInteger clientCounter = new AtomicInteger(0);

    public static void main(String[] args) {

        System.out.println("J-CRYPT SERVER ");
        System.out.println("JCRYPT- File Security System ");
        System.out.println();

        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(Constants.PORT)) {
            Utils.log("Server", "Started on port " + Constants.PORT);
            Utils.log("Server", "Thread pool size: " + MAX_THREADS);
            Utils.log("Server", "Waiting for clients... (Ctrl+C to stop)");
            System.out.println();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Utils.log("Server", "Shutting down...");
                threadPool.shutdown();
            }));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientNum = clientCounter.incrementAndGet();
                Utils.log("Server", "New connection #" + clientNum
                        + " from " + clientSocket.getInetAddress().getHostAddress());
                threadPool.execute(new ClientHandler(clientSocket, clientNum));
            }

        } catch (IOException e) {
            Utils.log("Server", "Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
