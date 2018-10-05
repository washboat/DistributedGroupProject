import java.net.*;
import java.io.*;


/*
* TODO add logic so servers can exit the infinite loops without throwing an exception
* TODO read input from console so IP address aren't hardcoded
 * */
/*
 * So, the loosely defined and extremely inconsistent "protocol" I had in mind
 *   when writing this class goes like so:
 *   1) Processes connecting to the router must identify what their type is. Server or Client
 *
 *   2) After declaring their type, clients connecting to the router should send the IP of their destination before sending any data.
 *       Servers have no such restriction
 *
 *   3) After declaring their type, servers connecting to the router should indicate that they're ready.
 *       I know what you're thinking: "But shouldn't the fact that a server connected in the first place indicate that it's ready?"
 *       Yes, yes it should, but i coded myself into a corner and needed a workaround so this is how it works at the moment
 *
 *   4) Sending the router an IPv4 address after the initial handshakes are complete will tell the router to shift its routing to the
 *       new IP iff that new IP is in the routing table
 *
 *   5) There should probably be some end of service carriage that clients and servers can send to shut everything down,
 *       but i haven't gotten that far so clients, servers, and the router all have to be forcibly shut down at some point
 * */

public class Server extends Thread {
    protected static boolean serverContinue = true;

    protected Socket clientSocket = null;
    PrintWriter toRouter = null;
    BufferedReader fromRouter = null;

    String router = null;
    int routerPort = -1;

    String inputLine = null;
    String outputLine = null;
    String type = "server";

    public Server(String routerName, int port) throws IOException {
        router = routerName;
        routerPort = port;
        clientSocket = new Socket(router, routerPort);
        toRouter = new PrintWriter(clientSocket.getOutputStream(), true);
        fromRouter = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        toRouter.println("server");
        toRouter.println("ready");


        while((inputLine = fromRouter.readLine()) != null){
            System.out.println(inputLine);
            toRouter.println(inputLine.toUpperCase());
            if (inputLine.equals("")) {
                break;
            }
        }

    }

    public static void main(String[] args) {
        try {
            Server server = new Server("REPLACE ME", 6000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

