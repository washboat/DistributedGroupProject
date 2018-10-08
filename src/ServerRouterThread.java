import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.String;
import java.util.concurrent.CopyOnWriteArrayList;



public class ServerRouterThread extends Thread {

    private Object routingTable[][] = null;

    //Client streams
    private PrintWriter toClient = null;
    private BufferedReader fromClient = null;

    //Destination streams
    private Socket destinationSocket = null;
    private PrintWriter toDestination = null;
    private BufferedReader fromDestination = null;

    private String inputLine = null;
    private String outputLine = null;
    String clientIP = null;
    private String destinationIP = null;
    private int index = -1;
    private String clientType = null;
    private final String CLIENT = "client";
    private final String SERVER = "server";

    /**
     * @param table - Object [String][Socket]
     * @param clientSocket - socket to the client to be inserted into routing table
     * @param index - the location to insert the client into the routing table
     * @throws IOException - if we can't I/O streams usually do to being passed a null socket
     */
    public ServerRouterThread(Object table[][], Socket clientSocket, int index) throws IOException {
        routingTable = table;
        toClient = new PrintWriter(clientSocket.getOutputStream(), true);
        fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.index = index;

        //Place client's IP and socket in routing table
        routingTable[this.index][0] = clientSocket.getInetAddress().getHostAddress() + ":" + Integer.toString(index);
        routingTable[this.index][1] = clientSocket;
    }

    /**
     *
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
            }
            /*
            TODO add client type to routing table
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
    }

    /**
     * TODO handle case where destinationIP doesn't match any ip in the routing table
     * @throws IOException
     *
     * searches routing table for a record matching destinationIP
     */
    private void tableLookup() throws IOException {
        for (Object[] objects : routingTable) {
            String ip = (String) objects[0];
            String split[] = ip.split(":");
            if(destinationIP.equals(split[0])){
                System.out.printf("Destination found %s in table%n", destinationIP);
                destinationSocket = (Socket)objects[1];
                toDestination = new PrintWriter(destinationSocket.getOutputStream(), true);
                fromDestination = new BufferedReader(new InputStreamReader(destinationSocket.getInputStream()));
                break;
            }
        }
    }


    /**
     * @throws IOException
     *
     * reads from the fromClient input stream and sends data to the destination
     * if an ipv4 address is found in the stream, the routing table is searched and the matching socket is the new destination
     */
    private void deliver() throws IOException {
        while((inputLine = fromClient.readLine()) != null) {
            System.out.printf("Attempting to send line: %s to destination: %s%n", inputLine, destinationIP);
            outputLine = inputLine;
            if (checkIPv4(outputLine)){
                destinationIP = outputLine;
                //
                tableLookup();
            }
            if(outputLine != null && !outputLine.isEmpty()) {

                toDestination.println(outputLine);
                //System.out.println(outputLine + "Successfully sent");

            }
        }
    }

    public void serviceClient() throws IOException {
        System.out.println("Servicing client" + routingTable[index][0]);
        destinationIP = fromClient.readLine();
        System.out.println("Destination IP address" +  destinationIP);


        tableLookup();
        toDestination.println(routingTable[this.index][0]);
        deliver();
    }

    //WE ARE NOT UPDATING DESTINATION ip TO THE CURRENT IP SO THE MESSAGES ALWAYS GET SENT TO FIRST CLIENT
    // "fixed" issue by doing another table lookup in the deliver() method. find better solution later.
    public void serviceServer() throws IOException {
        System.out.println("Serving a Server");
        String ready;
        while ((ready = fromClient.readLine()) != null){
            if (ready.equals("ready")) {
                break;
            }
        }
        destinationIP = fromClient.readLine();
        System.out.println("Destination IP address" +  destinationIP);

        tableLookup();
        deliver();

    }
    public static final boolean checkIPv4(final String ip) {
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
