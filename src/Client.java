import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
public class Client extends TimeKeeper implements Runnable  {

    CopyOnWriteArrayList<String> line = null;
    String ServerRouterIP = null; //ServerRouter IP
    String ServerIP = null; // Server IP
    static String outputFileName = null;
    Socket socket = null;
    PrintWriter toRouter = null;
    BufferedReader fromRouter = null;
    int port;
    static int maxClients = -1;
    private static volatile int runCount = 0;
    private static volatile double avg = -1;

    public Client(String serverIP, String routerIP, int port, String outputFile, int numberOfClients)  throws IOException {
        outputFileName = outputFile;
        line = null;
        ServerRouterIP = routerIP; //ServerRouter IP
        ServerIP = serverIP; // Server IP
        socket = null;
        toRouter = null;
        fromRouter = null;
        this.port = port;
        maxClients= numberOfClients;

    }
    public static void main(String[] args) throws IOException {
        String serverIP = "192.168.1.76:0"; //SERVER ip NEED TO HAVE THE :0 AT THE END OF THE ADDRESS
        String routerIP = "192.168.1.76";
        String outputFile = "test.csv";
        int maxClients = 10;
        int port = 6000;

        int howManyClients = 1;    //how many clients we spawn
        table = new Object[maxClients][2];
            Runnable client = new Client(serverIP, routerIP, port, outputFile, maxClients);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(client, 0, 1, TimeUnit.SECONDS);
        while (runCount < maxClients){
            //System.out.println(runCount);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PrintWriter pw = new PrintWriter(new File(outputFileName));
        StringBuilder sb = new StringBuilder();

        sb.append(",");
        sb.append("client id");
        sb.append(",");
        sb.append("file transfer round trip (ms)");
        sb.append("\n");
        for (Object[] objects : table) {
            String id = Double.toString( (int)objects[0]);
            String duration = Double.toString( (double) objects[1]);
            sb.append(",");
            sb.append("Client: " + id);
            sb.append(",");
            sb.append(duration + " ms" );
            sb.append("\n");
            System.out.println(objects[1]);
        }
        average = average();
        BigDecimal bigDecimal = new BigDecimal(average);
        bigDecimal = bigDecimal.setScale(6, RoundingMode.HALF_UP);
        average = bigDecimal.doubleValue();

        double avgExcludingOutlier = ignoreOutliers();
        BigDecimal bigDecimalTheSequel = new BigDecimal(avgExcludingOutlier);
        bigDecimalTheSequel = bigDecimalTheSequel.setScale(6, RoundingMode.HALF_UP);
        avgExcludingOutlier = bigDecimalTheSequel.doubleValue();

        String strAvg =  Double.toString(average);
        sb.append("AVG");
        sb.append(",");
        sb.append("-");
        sb.append(",");
        sb.append(strAvg + " ms");
        sb.append("\n");
        sb.append("AVG w/o Client 0");
        comma(sb);
        sb.append("-");
        comma(sb);
        sb.append(Double.toString(avgExcludingOutlier) + " ms");
        newLine(sb);

        pw.write(sb.toString());
        pw.close();


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
        int myID = runCount;
        table[myID][0] = myID;
        try {
            Reader fileReader = new FileReader("C:\\Users\\Triston C Gregoire\\Desktop\\DistributedProject\\src\\file.txt"); //put file path to text file here
            BufferedReader fromFile = new BufferedReader(fileReader);
            line = readFile(fromFile);

            socket = new Socket(ServerRouterIP, port);
            toRouter = new PrintWriter(socket.getOutputStream(), true);
            fromRouter = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toRouter.println("client");
            toRouter.println(ServerIP);   //ServerIP
            startTime = System.nanoTime();
            for (String s : line) {
                toRouter.println(s);
            }


            String fromServer = "";
            while ((fromServer = fromRouter.readLine()) != null){
                System.out.println("Reading from input stream");
                System.out.println(fromServer);
                if(fromServer.equals("EOF")){
                    endTime = System.nanoTime();
                    break;
                }
            }
            double fromNanoToSec = 1000000000L;
            duration = (endTime -startTime) / TOMILI;
            table[myID][1] = duration;
            System.out.println( duration + "seconds");

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + ServerRouterIP);
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
        toRouter.close();
        try {
            fromRouter.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            runCount++;
        }


    }
}
