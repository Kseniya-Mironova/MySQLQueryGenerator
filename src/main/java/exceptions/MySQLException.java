package exceptions;

public class MySQLException extends Exception {

        String name;

        public MySQLException(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
