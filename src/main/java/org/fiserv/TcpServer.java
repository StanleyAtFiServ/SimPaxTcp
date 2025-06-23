package org.fiserv;

import java.io.*;
import java.net.*;

public class TcpServer {

    private int port;
    private String file;
    private volatile boolean running = true;

    public TcpServer (String _file, String _tcpPort) {
        port = Integer.parseInt(_tcpPort);
        file = _file;
    }

    public void Process() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("EchoServer is running and listening on port " + port);

            // Main server loop
            while (running) {
                Socket clientSocket = null;
                try {
                    // Accept client connection
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                    // Handle client in a new thread
                    new Thread(new ClientHandler(clientSocket)).start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.err.println(e.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }

    // Inner class to handle client connections
    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            InputStream in = null;
            PrintWriter out = null;

            try {
                in = clientSocket.getInputStream();
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String fileContent = ReadFileContent();
                if (fileContent == null) {
                    fileContent = "Error reading file";
                }

                byte[] buffer = new byte[19200];
                int bytesRead;

                // Persistent connection handling
                while (running) {
                    try {
                        // Set read timeout (5 seconds)
                        clientSocket.setSoTimeout(60000);

                        bytesRead = in.read(buffer);

                        if (bytesRead == -1) {
                            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
                            break; // Client closed connection
                        }

                        String inputLine = new String(buffer, 0, bytesRead).trim();
                        System.out.println("Received message: " + inputLine);

                        System.out.println("Press Enter to send ACK and file content...");
                        new BufferedReader(new InputStreamReader(System.in)).readLine();

                        // Send ACK
                        AppMsg appMsgAcknowledge = new AppMsg(AppMsg.MSG_ACK, AppMsg.OP_ACK, null);
                        out.println(appMsgAcknowledge.packMessage());
                        System.out.println("Sent XML ACK");

                        // Send file content
                        out.println(fileContent);
                        System.out.println("Sent file content");

                    } catch (SocketTimeoutException e) {
                        // Timeout is normal - just check if we should keep running
                        continue;
                    } catch (IOException e) {
                        System.err.println("Client connection error: " + e.getMessage());
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                // Close resources
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
        }
    }

    public void stopServer() {
        running = false;
    }

    private String ReadFileContent() {
        String projectRoot = System.getProperty("user.dir");
        String filePath = projectRoot + File.separator + file;

        StringBuilder contentBuilder = new StringBuilder();
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = fileReader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            return contentBuilder.toString().trim();
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            System.err.println(e.getMessage());
            return null;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    System.err.println("Error closing file reader: " + e.getMessage());
                }
            }
        }
    }
}