import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/*
* TODO read input from console so IP address aren't hardcoded
* TODO use relative file path
 * */

/**
 *  Creates multiple threads to connect to some remote or local server. Each thread times itself and inserts its time into a data structure which is written to an external .csv file after all threads execute
 *
 * @author Triston C Gregoire
 *  Requires TimeKeeper.java
 */

public class Client extends TimeKeeper implements Runnable  {

    private CopyOnWriteArrayList<String> line;
    private String ServerRouterIP;
    private String ServerIP;
    private static String outputFileName = null;
    private Socket socket;
    private PrintWriter toRouter;
    private BufferedReader fromRouter;
    private int port;
    private static volatile int runCount = 0;

    public Client(String serverIP, String routerIP, int port, String outputFile, int numberOfClients)  throws IOException {
        outputFileName = outputFile;
        line = null;
        ServerRouterIP = routerIP;
        ServerIP = serverIP;
        socket = null;
        toRouter = null;
        fromRouter = null;
        this.port = port;

    }
    public static void main(String[] args) throws IOException {
        String serverIP = "192.168.1.76:0"; //SERVER ip NEEDS TO HAVE THE :0 AT THE END OF THE ADDRESS
        String routerIP = "192.168.1.76";
        String outputFile = "test.csv";
        int maxClients = 10;
        int port = 6000;

        table = new Object[maxClients][2];
            Runnable client = new Client(serverIP, routerIP, port, outputFile, maxClients);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(client, 0, 1, TimeUnit.SECONDS);
        while (runCount < maxClients){
            //wait for client threads to finish
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
        }

        PrintWriter pw = new PrintWriter(new File(outputFileName));
        StringBuilder sb = new StringBuilder();

        /*
        * Build string to output to .csv file
        * */
        sb.append(",");
        sb.append("client id");
        sb.append(",");
        sb.append("file transfer round trip (ms)");
        sb.append("\n");
        for (Object[] objects : table) {
            String id = Double.toString( (int)objects[0]);
            String duration = Double.toString( (double) objects[1]);
            sb.append(",");
            sb.append("Client: ").append(id);
            sb.append(",");
            sb.append(duration).append(" ms");
            sb.append("\n");
            System.out.println(objects[1] + " ms");
        }
        average = average();
        BigDecimal bigDecimal = new BigDecimal(average);
        bigDecimal = bigDecimal.setScale(6, RoundingMode.HALF_UP);
        average = bigDecimal.doubleValue();

        String strAvg =  Double.toString(average);
        sb.append("AVG");
        sb.append(",");
        sb.append("-");
        sb.append(",");
        sb.append(strAvg).append(" ms");
        sb.append("\n");
        newLine(sb);
        pw.write(sb.toString());
        pw.close();
    }

    /**
     * @param fileReader BufferedReader to read from
     * @return returns thread safe array list of contents read from fileReader
     * @throws IOException throws exception if fileReader is null or if file is in use by another process
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
            duration = (endTime - startTime) / TOMILI;
            table[myID][1] = duration;
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + ServerRouterIP);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
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
