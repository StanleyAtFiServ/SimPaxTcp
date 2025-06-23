package org.fiserv;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.err.println("Usage: java POSAppTCPClient <xml-file-path>");
            System.err.println("Example: java POSAppTCPClient payment.xml");
            return;
        }

        String fileName = args[0];
        File xmlFile = new File(fileName);
        String listenPort = args[1];

        if (!xmlFile.exists()) {
            System.err.println("Error: File not found at path: " + xmlFile.getAbsolutePath());
            return;
        }

        System.out.print("TCP Server");
        TcpServer tcpClient = new TcpServer(fileName, listenPort);
        tcpClient.Process();


    }
}