/**
 * @author Triston C Gregoire
 *
 * */
public class UnknownClientException extends Exception {
    private String type;
    public UnknownClientException(String clientType){
        type = clientType;
    }
    public String getType(){
        return type;
    }
}
