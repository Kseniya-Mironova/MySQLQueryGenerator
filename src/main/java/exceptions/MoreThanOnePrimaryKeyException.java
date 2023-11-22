package exceptions;

public class MoreThanOnePrimaryKeyException extends Exception {

    String name;

    public MoreThanOnePrimaryKeyException(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
