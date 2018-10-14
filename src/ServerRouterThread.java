import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.String;

public class ServerRouterThread extends Thread {

    private Object routingTable[][] = null;

    private PrintWriter toClient = null;
    private BufferedReader fromClient = null;

    private PrintWriter toDestination = null;
    private BufferedReader fromDestination = null;

    private String inputLine = null;
    private String outputLine = null;
    String clientIP[] = null;
    private String destinationIP = null;
    private int index = -1;
    private String clientType = null;
    private final String CLIENT = "client";
    private final String SERVER = "server";

    /**
     * Services each new connection to the ServerRouter
     *
     * @param table Object [String][Socket]
     * @param clientSocket socket to the client to be inserted into routing table
     * @param index  the location to insert the client into the routing table
     * @throws IOException
     */
    public ServerRouterThread(Object table[][], @NotNull Socket clientSocket, int index) throws IOException {
        routingTable = table;
        toClient = new PrintWriter(clientSocket.getOutputStream(), true);
        fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.index = index;


        //Place client's IP and socket in routing table
        routingTable[this.index][0] = clientSocket.getInetAddress().getHostAddress() + ":" + Integer.toString(index); //append ':index' to client's IP to differentiate between clients on the same machine
        routingTable[this.index][1] = clientSocket;
    }

    /**
     * Start of thread's life
     */
    @Override
    public void run() {
        try {
            String clientIdentifier;
            while((clientIdentifier = fromClient.readLine()) != null) {
                if (clientIdentifier.equals(CLIENT) || clientIdentifier.equals(SERVER)) {
                    clientType = clientIdentifier;
                    break;
                }
                else {
                    throw new UnknownClientException(clientIdentifier);
                }
            }
            /*
            Clients and servers are serviced a little differently so we check their type
            */
            switch(clientType){
                case CLIENT:
                    serviceClient();
                    break;
                case SERVER:
                    serviceServer();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (UnknownClientException e){
            e.printStackTrace();
            System.err.printf("Unknown type of client \"%s\" %n", e.getType());
        }
    }

    /**
     * Searches routing table for a record matching destinationIP. initializes input and output streams if lookup is successful
     *
     * @throws IOException
     *
     */
    private void tableLookup() throws IOException {
        for (Object[] objects : routingTable) {
            String ip = (String) objects[0];
            if (destinationIP.equals(ip)){
                System.out.printf("Destination found %s in table%n", destinationIP);
                //Destination streams
                Socket destinationSocket = (Socket) objects[1];
                toDestination = new PrintWriter(destinationSocket.getOutputStream(), true);
                fromDestination = new BufferedReader(new InputStreamReader(destinationSocket.getInputStream()));
                break;
            }
        }
    }


    /**
     * Reads from the fromClient input stream and sends data to the destination. if an ipv4 address is found in the stream, the routing table is searched and the matching socket becomes the new destination
     *
     * @throws IOException
     *
     */
    private void deliver() throws IOException {
        while((inputLine = fromClient.readLine()) != null) {
            System.out.printf("SENDING LINE: %s TO DESTINATION: %s%n", inputLine, destinationIP);
            outputLine = inputLine;
            String ip = null;
            if(outputLine.contains(":")) {// check for ip address if line contains ':'
                ip = (outputLine.split(":"))[0];
                if (checkIPv4(ip)){
                    destinationIP = outputLine;
                    tableLookup();
                }
            }
            if(outputLine != null ) toDestination.println(outputLine);
        }
    }

    /**
     * Handles communication with a client process
     *
     *  @throws IOException
     */
    public void serviceClient() throws IOException {
        System.out.printf("SERVICING CLIENT: %s%n", routingTable[index][0]);
        destinationIP = fromClient.readLine();
        tableLookup();
        //send client IP to the server
        toDestination.println(routingTable[this.index][0]);
        deliver();
    }


    /**
     * Handles communication with a server process
     *
     *  @throws IOException
     */
    public void serviceServer() throws IOException {
        System.out.println("Server connected");
        destinationIP = fromClient.readLine();
        System.out.println("Destination IP address" +  destinationIP);
        tableLookup();
        deliver();
    }

    /**
     * @param ip String to be checked for IPv4 compliance
     * @return returns true if ip is a string representation of a valid IPv4, returns false otherwise
     */
    private static boolean checkIPv4(final String ip) {
        boolean isIPv4;
        try {
            final InetAddress inet = InetAddress.getByName(ip);
            isIPv4 = inet.getHostAddress().equals(ip)
                    && inet instanceof Inet4Address;
        } catch (final UnknownHostException e) {
            isIPv4 = false;
        }
        return isIPv4;
    }
}
