import java.net.*;
import java.io.*;


/*
 * TODO add logic so servers can exit the infinite loops without throwing an exception
 * TODO read input from console so IP address aren't hardcoded
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

