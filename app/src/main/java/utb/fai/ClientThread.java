package utb.fai;

import java.io.*;
import java.net.*;

public class ClientThread extends Thread {

    private Socket clientSocket;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        // Echo everything received from the telnet client straight back to it
        try (Socket socket = clientSocket;
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream()) {
            socket.setTcpNoDelay(true);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                out.flush();
            }
        } catch (IOException e) {
            // Client disconnected or the connection was reset - nothing more to do
        }
    }
}
