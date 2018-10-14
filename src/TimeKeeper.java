import org.jetbrains.annotations.NotNull;

/**
* Holds all variables and functions needed to time threads
* @author Triston C Gregoire
* */
public class TimeKeeper {
    double startTime = -1;
    double endTime = -1;
    double duration = -1;
    static double average = -1;
    static Object[][] table = null;
    double FROMNANO = 1000000000;
    double TOMILI = 1000000;

    static public double average(){
        double sum = 01;
        double avg = -1;
        double size = table.length;
        for (Object[] objects : table) {
            sum += (double) objects[1];
        }
        avg = sum / size;
        return avg;
    }

    static void comma(@NotNull StringBuilder sb){
        sb.append(",");
    }

    static void newLine(@NotNull StringBuilder sb){
        sb.append("\n");
    }
}
