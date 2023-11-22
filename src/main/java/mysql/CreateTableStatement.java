package mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import annotations.DoNotCreateNotInsert;
import org.apache.commons.lang3.StringUtils;

public class CreateTableStatement {

    private String _tableName;
    private Connection connection;
    private Class className;
    private int dbVersion;
    private boolean ifNotExists;

    private List<Object> defaultValueList = new ArrayList();

    public CreateTableStatement(Connection connection, Class className) {
        this.connection = connection;
        this.className = className;
        this._tableName = mysql.Table.getTableName(className);
        this.dbVersion = 0;
        this.ifNotExists = true;
    }

    public void setTableName(String _tableName) {
        this._tableName = _tableName;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    private String generateCreateTableQuery() throws exceptions.MoreThanOnePrimaryKeyException {
        String tableName = (_tableName == null) || (_tableName.isEmpty()) ? mysql.Table.getTableName(className) : _tableName;

        List<Column> columnList = new ArrayList();
        Field[] fields = className.getFields();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers)) && (!field.isAnnotationPresent(DoNotCreateNotInsert.class))) {
                Column column = Column.fromField(field);
                if ((column != null) && (dbVersion >= column.fromVersion)) {
                    columnList.add(column);
                    if (column.hasDefaultValue) {
                        defaultValueList.add(column.getDefaultValue());
                    }
                }
            }
        }

        String header = Const.STRING_CREATE_TABLE + (ifNotExists ? Const.STRING_IF_NOT_EXISTS : "") +
                Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + " (\n";

        int primaryKeyCount = 0;
        String primaryKeyName = "";

        List<String> columnStringList = new ArrayList();

        for (Column column : columnList) {
            if ((column.isPrimaryKey) || (column.isAutoIncrement)) {
                primaryKeyCount++;
                primaryKeyName = column.name;
            }
            columnStringList.add(column.toString());
        }

        if (primaryKeyCount > 1) throw new exceptions.MoreThanOnePrimaryKeyException("Не может быть более одного первичного ключа!");
        String footer = "";
        if (primaryKeyCount == 1) {
            footer = Const.STRING_PRIMARY_KEY + "`" + primaryKeyName + "`" + "))";
        } else {
            footer = ")";
        }

        return header + StringUtils.join(columnStringList, ",\n") + footer + Const.STRING_QUERY_ENDING;
    }

    public PreparedStatement toPreparedStatement() throws exceptions.MySQLException {

        try {
            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement(generateCreateTableQuery());

            int index = 0;
            for (Object defValue : defaultValueList) {
                preparedStatement.setObject(++index, defValue);
            }
            System.out.println(preparedStatement.toString());
            return preparedStatement;
        } catch (Exception e) {
            e.printStackTrace();
            throw new exceptions.MySQLException("Ошибка выполнения запроса!");
        }
    }
}
