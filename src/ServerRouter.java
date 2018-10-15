import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
* TODO implement function to check routing table for broken sockets and remove those entries
* TODO read input from console so IP address aren't hardcoded
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
