import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
* TODO implement function to check routing table for broken sockets and remove those entries
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
 * Listens for connections indefinitely and spawns a new thread to handle each new connection
 *
 * @author Triston C Gregoire
 */
public class ServerRouter {
    public ServerRouter() {
        try {
            //[IP address] [Socket]
            Object[][] routingTable = new Object[16000][2];
            int localport = 6000;
            ServerSocket serverSocket = new ServerSocket(localport);

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
