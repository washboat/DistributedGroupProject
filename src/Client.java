import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
/*
* TODO move code out of main and into constructor or other function
* TODO read input from console so IP address aren't hardcoded
* TODO use relative file path
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
 *
 *       GUCK
 *
 *       GDG R
 * */
public class Client extends Thread {

    CopyOnWriteArrayList<String> line = null;
    String ServerRouterIP = null; //ServerRouter IP
    String ServerIP = null; // Server IP
    Socket socket = null;
    PrintWriter toRouter = null;
    BufferedReader fromRouter = null;
    int port;

    public Client(String serverIP, String routerIP, int port)  throws IOException {
        line = null;
        ServerRouterIP = routerIP; //ServerRouter IP
        ServerIP = serverIP; // Server IP
        socket = null;
        toRouter = null;
        fromRouter = null;
        this.port = port;


    }
    public static void main(String[] args) throws IOException {
        String serverIP = "192.168.1.76:0"; //SERVER ip NEED TO HAVE THE :0 AT THE END OF THE ADDRESS
        String routerIP = "192.168.1.76";
        int port = 6000;

        int howManyClients = 15;    //how many clients we spawn
        for (int i = 1; i <= howManyClients; i++){
            Client client = new Client(serverIP, routerIP, port);
            client.start();
        }

}

    /**
     * @param fileReader
     * @return
     * @throws IOException
     */
    public static CopyOnWriteArrayList<String> readFile(BufferedReader fileReader) throws IOException {
        String newLine;
        CopyOnWriteArrayList<String> newList = new CopyOnWriteArrayList<String>();
        while(( newLine = fileReader.readLine()) != null){
            newList.add(newLine);
        }
        return newList;
    }

    @Override
    public void run() {
        try {
            Reader fileReader = new FileReader("C:\\Users\\Triston C Gregoire\\Desktop\\DistributedProject\\src\\file.txt"); //put file path to text file here
            BufferedReader fromFile = new BufferedReader(fileReader);
            line = readFile(fromFile);

            socket = new Socket(ServerRouterIP, port);
            toRouter = new PrintWriter(socket.getOutputStream(), true);
            fromRouter = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toRouter.println("client");
            toRouter.println(ServerIP);   //ServerIP
            Long StartTime = System.nanoTime();
            Long EndTime = 0L;
            for (String s : line) {
                toRouter.println(s);
            }


            Thread.sleep(3000);
            String fromServer = "";
            while ((fromServer = fromRouter.readLine()) != null){
                System.out.println("Reading from input stream");
                System.out.println(fromServer);
                if(fromServer.equals("END")){
                    EndTime = System.nanoTime();
                    //break;
                }
            }
            System.out.println((EndTime - StartTime) + "nanoseconds");

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + ServerRouterIP);
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        toRouter.close();
        try {
            fromRouter.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
