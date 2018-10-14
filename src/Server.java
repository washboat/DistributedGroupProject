import java.net.*;
import java.io.*;


/*
 * TODO add logic so servers can exit the infinite loops without throwing an exception
 * TODO read input from console so IP address aren't hardcoded
 * */


/*
 * So, the "protocol" I had in mind
 *   when writing this class goes like so:
 *   1) Processes connecting to the router must identify what their type is. Server or Client
 *
 *   2) After declaring their type, clients connecting to the router should send the IP of their destination before sending any data.
 *       Servers have no such restriction
 *
 *   3) Sending the router an IPv4 address after the initial handshakes are complete will tell the router to shift its routing to the
 *       new IP iff that new IP is in the routing table
 *
 *   4) There should probably be some end of service carriage that clients and servers can send to shut everything down while testing. Currently "eof" is that carriage
 * */

/**
 * Converts any message it receives from router into uppercase and sends the message back to the router
 * @author Triston C Gregoire
 */
public class Server extends Thread {
    public Server(String routerName, int port) throws IOException {
        Socket clientSocket = new Socket(routerName, port);
        PrintWriter toRouter = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader fromRouter = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String type = "server";
        toRouter.println(type);

        String inputLine = null;
        while((inputLine = fromRouter.readLine()) != null){
            if(inputLine.contains(":")) {
                String ip[] = inputLine.split(":");
                if (checkIPv4( ip[0] )) {
                    System.out.printf("Talking to : %s%n", inputLine);
                    toRouter.println(inputLine);
                }
            }else {
                if (inputLine.isEmpty())
                    System.out.println("Received: <EMPTY LINE>");
                else {
                    System.out.printf("Received: %s%n", inputLine);
                    toRouter.println(inputLine.toUpperCase());
                }//end else
            }// end else
        }//end while
    }// end constructor

    private static boolean checkIPv4(final String ip) {
        boolean isIPv4;
        try {
            final InetAddress inet = InetAddress.getByName(ip);
            isIPv4 = inet.getHostAddress().equals(ip)
                    && inet instanceof Inet4Address;
        }
        catch (final UnknownHostException e) {
            isIPv4 = false;
        }
        return isIPv4;
    }

    public static void main(String[] args) {
        try {
            Server server = new Server("192.168.1.76", 6000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

