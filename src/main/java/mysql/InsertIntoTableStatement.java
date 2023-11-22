package mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import annotations.DoNotCreateNotInsert;
import annotations.DoNotInsert;
import org.apache.commons.lang3.StringUtils;

public class InsertIntoTableStatement {

    public static final int INSERT_DEFAULT = 0;
    public static final int INSERT_REPLACE = 1;
    public static final int INSERT_IGNORE = 2;
    private Connection connection;
    private Object object;
    private Class className;
    private int initIndex = 0;

    private int insertType;
    private String _tableName;
    private int dbVersion;

    private Map<Column, Object> valueMap = new HashMap();
    private Map<Column, Integer> indexMap = new HashMap();
    private Map<String, String> functionValueMap = new HashMap();

    public InsertIntoTableStatement(Connection connection, Object object) {
        this.connection = connection;
        this.object = object;
        this.className = object.getClass();
        this.insertType = INSERT_DEFAULT;
        this._tableName = null;
        this.dbVersion = 0;
    }

    public void setInsertType(int insertType) {
        this.insertType = insertType;
    }

    public void setTableName(String _tableName) {
        this._tableName = _tableName;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    public void setFunctionValues(String functionValues) {
        String[] arr0 = functionValues.split(";");
        for (String str0 : arr0) {
            String[] arr1 = str0.split(":");
            functionValueMap.put(arr1[0], arr1[1]);
        }
    }

    private String generateInsertQuery() throws IllegalAccessException, exceptions.NotNullColumnHasNullValueException {
        String tableName = (_tableName == null) || (_tableName.isEmpty()) ? mysql.Table.getTableName(className) : _tableName;

        List<String> setValueStringList = new ArrayList();
        Field[] fields = className.getFields();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers))
                    && (!field.isAnnotationPresent(DoNotInsert.class))
                    && (!field.isAnnotationPresent(DoNotCreateNotInsert.class))) {

                Column column = Column.fromField(field);
                if ((column != null) && (!(column.isAutoIncrement && insertType==INSERT_DEFAULT))  && (dbVersion >= column.fromVersion)) {
                    if (functionValueMap.containsKey(column.name)) {
                        String setValueString = "`" + column.name + "`" + "=" + functionValueMap.get(column.name);

                        setValueStringList.add(setValueString);
                    } else {
                        Object value;

                        value = field.get(object);

                        if ((value != null) || (!column.hasDefaultValue)) {
                            if ((column.isNotNull) && (value == null)) {
                                throw new exceptions.NotNullColumnHasNullValueException("Поле не может содержать пустые значения!");
                            }

                            String setValueString = Const.STRING_BEFORE_NAME + column.name + Const.STRING_AFTER_NAME
                                    + Const.STRING_ASSIGN + (value != null ? Const.STRING_VAR : Const.STRING_NULL);

                            setValueStringList.add(setValueString);

                            if (value != null) {
                                if ((value.getClass() == Character.TYPE) || (value.getClass() == Character.class)) {
                                    valueMap.put(column, String.valueOf(value));
                                } else {
                                    valueMap.put(column, value);
                                }
                                indexMap.put(column, Integer.valueOf(++initIndex));
                            }
                        }
                    }
                }
            }
        }
        boolean insertReplaceSet = (insertType == INSERT_REPLACE);

        boolean insertIgnoreSet = (insertType == INSERT_IGNORE);

        String header = (insertReplaceSet ? Const.STRING_REPLACE : Const.STRING_INSERT) +
                (insertIgnoreSet ? Const.STRING_IGNORE : "") + Const.STRING_INTO +
                Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + Const.STRING_SET;

        String result = header + StringUtils.join(setValueStringList, Const.STRING_GLUE) + Const.STRING_QUERY_ENDING;

        return result;
    }

    public PreparedStatement toPreparedStatement() throws exceptions.MySQLException {
        try {
            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement(generateInsertQuery());

            for (Column column : valueMap.keySet()) {
                Object object = valueMap.get(column);

                preparedStatement.setObject(indexMap.get(column).intValue(), object);
            }
            System.out.println(preparedStatement.toString());
            return preparedStatement;
        } catch (Exception e) {
            e.printStackTrace();
            throw new exceptions.MySQLException("Ошибка выполнения запроса!");
        }
    }
}
