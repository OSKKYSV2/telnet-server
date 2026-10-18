package utb.fai;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class App {

    public static void main(String[] args) {
        int port = 12345;
        int max_threads = 10;

        // Input parameter processing: args[0] = port, args[1] = max number of clients
        try {
            if (args.length > 0) {
                port = Integer.parseInt(args[0].trim());
            }
            if (args.length > 1) {
                max_threads = Integer.parseInt(args[1].trim());
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid parameter: port and max threads must be integers.");
            System.err.println("Usage: java -jar app.jar <port> <max_threads>");
            System.exit(1);
        }
        if (port < 1 || port > 65535 || max_threads < 1) {
            System.err.println("Invalid parameter: port must be 1-65535 and max threads must be at least 1.");
            System.err.println("Usage: java -jar app.jar <port> <max_threads>");
            System.exit(1);
        }

        // Implementation of the main server loop
        // At most max_threads clients are served at once. Further clients are still
        // accepted (their connection stays open and they can send), but wait in the
        // queue without getting any echo until a served client disconnects.
        ThreadPoolExecutor executor = new ThreadPoolExecutor(max_threads, max_threads,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Echo server listening on port " + port + " (max clients: " + max_threads + ")");

            while (true) {
                Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        break;
                    }
                    System.err.println("Accept failed: " + e.getMessage());
                    continue;
                }

                try {
                    executor.execute(new ClientThread(clientSocket));
                } catch (RejectedExecutionException e) {
                    try {
                        clientSocket.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot start server on port " + port + ": " + e.getMessage());
            System.exit(1);
        } finally {
            executor.shutdown();
        }
    }
}
