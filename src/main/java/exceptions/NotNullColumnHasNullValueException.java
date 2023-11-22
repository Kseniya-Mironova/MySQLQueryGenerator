package exceptions;

public class NotNullColumnHasNullValueException extends Exception {

    String name;

    public NotNullColumnHasNullValueException(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
