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
    static public double ignoreOutliers(){
        Object[][] temp = table;
        temp[0][1] = 0.0;
        double sum = 01;
        double avg = -1;
        double size = temp.length;
        for (Object[] objects : temp) {
            sum += (double) objects[1];
        }
        avg = sum / (size - 1);
        return avg;
    }

    static public void comma(StringBuilder sb){
        sb.append(",");
    }

    static public void newLine(StringBuilder sb){
        sb.append("\n");
    }
}
