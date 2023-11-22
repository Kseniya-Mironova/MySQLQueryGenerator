package mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AlterTableStatement {

    private String _tableName;
    private Connection connection;
    private Class className;
    private int oldVersion;
    private int newVersion;

    private Map<String, Object> defaultValueMap = new HashMap();

    public AlterTableStatement(Connection connection, Class className, int oldVersion, int newVersion)
    {
        this.connection = connection;
        this.className = className;
        this._tableName = mysql.Table.getTableName(className);
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    public void setTableName(String _tableName) {
        this._tableName = _tableName;
    }

    private List<String> generateAlterTableQueryList() {
        List<String> queryList = new ArrayList();

        List<mysql.Column> newColumns = new ArrayList();

        Field[] fields = className.getFields();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers)) && (!field.isAnnotationPresent(annotations.DoNotCreateNotInsert.class)))
            {

                mysql.Column column = mysql.Column.fromField(field);
                if ((column != null) && (column.fromVersion > oldVersion) && (column.fromVersion <= newVersion)) {
                    newColumns.add(column);
                }
            }
        }

        for (mysql.Column column : newColumns) {
            String addQuery = mysql.Const.STRING_ALTER_TABLE +
                    mysql.Const.STRING_BEFORE_NAME + _tableName + mysql.Const.STRING_AFTER_NAME + mysql.Const.STRING_ADD_COLUMN +
                    column.toString() + mysql.Const.STRING_QUERY_ENDING;

            queryList.add(addQuery);
            if (column.hasDefaultValue) {
                defaultValueMap.put(addQuery, column.getDefaultValue());
            }
        }

        return queryList;
    }

    public List<PreparedStatement> toPreparedStatementList() throws exceptions.MySQLException {
        try {
            List<PreparedStatement> result = new ArrayList();
            for (String query : generateAlterTableQueryList()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                if (defaultValueMap.containsKey(query)) {
                    preparedStatement.setObject(1, defaultValueMap.get(query));
                    preparedStatement.execute();
                }
                result.add(preparedStatement);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new exceptions.MySQLException("Ошибка выполнения запроса!");
        }
    }

    public List<String> toSQLStringList() throws exceptions.MySQLException {
        List<String> result = new ArrayList();
        for (PreparedStatement preparedStatement : toPreparedStatementList()) {
            result.add(preparedStatement.toString());
        }
        return result;
    }
}
