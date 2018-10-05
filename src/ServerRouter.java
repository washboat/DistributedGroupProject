import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/*
* TODO add logic for router to exit infinite loop
* TODO implement function to check routing table for broken sockets and remove those entries
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

public class ServerRouter {
    private ServerSocket serverSocket = null;
    private int localport = 6000;
    PrintWriter output = null;
    BufferedReader input = null;
    //CopyOnWriteArrayList<String> routingTable[][] = null;
    private Object[][] routingTable;    //[IP address] [Socket]
    public ServerRouter() {
        try {
            routingTable = new Object[5][2]; // [number of max clients][number of variables specific to each client i.e socket]
            serverSocket = new ServerSocket(localport);

            int clientCounter = 0;  //Used by each thread to insert itself at the end of the routing table
            System.out.println("Server Started");
            System.out.printf("Listening on Port: %d%n", localport);
            do {    //condition is always true

                Socket clientSocket = serverSocket.accept();
                ServerRouterThread t = new ServerRouterThread(routingTable, clientSocket, clientCounter);

                t.start();
                clientCounter++;
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ServerRouter serverRouter = new ServerRouter();
    }
}
