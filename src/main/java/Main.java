package src.main.java;

import java.sql.Connection;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            mysql.MySQLClient client = new mysql.MySQLClient("root", "1234567", "test");
            Connection connection = client.connect();

            tables.SomeTable someTable = new tables.SomeTable();

            var createTableStatement = new mysql.CreateTableStatement(connection, tables.SomeTable.class);

            createTableStatement.setTableName("some");
            createTableStatement.setIfNotExists(true);
            createTableStatement.setDbVersion(1);

            createTableStatement.toPreparedStatement().execute();

            createTableStatement = new mysql.CreateTableStatement(connection, tables.Person.class);

            createTableStatement.setTableName("person");
            createTableStatement.setIfNotExists(true);

            createTableStatement.toPreparedStatement().execute();

            var insertIntoTableStatement = new mysql.InsertIntoTableStatement(connection, someTable);

            insertIntoTableStatement.setDbVersion(2);
            insertIntoTableStatement.setFunctionValues("intV2:RAND()");

            insertIntoTableStatement.toPreparedStatement().execute();

            var reflectUpdateStatement = new mysql.UpdateTableStatement(connection, someTable,
                    new String[]{"stringNotCreate", "intNotInsert", "intV1", "stringV1"},
                    "id=? AND stringV0=?", new Object[]{null, 'f'});

            reflectUpdateStatement.toPreparedStatement().execute();

            List<String> queryList = new mysql.AlterTableStatement(connection, tables.SomeTable.class, 1, 3)
                    .toSQLStringList();
            for (String query : queryList) {
                System.out.println(query);
            }

            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
