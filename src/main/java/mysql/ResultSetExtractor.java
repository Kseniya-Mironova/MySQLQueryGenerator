package mysql;

import exceptions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSetExtractor {
    public static List extractResultSet(ResultSet resultSet, Class className) throws MySQLException
    {
        return extractResultSet(resultSet, className, 0);
    }

    public static List extractResultSet(ResultSet resultSet, Class className, int dbVersion) throws MySQLException {
        return extractResultSet(resultSet, className, dbVersion, "#");
    }

    public static List extractResultSet(ResultSet resultSet, Class className, int dbVersion, String configVariant) throws MySQLException {
        try {
            List result = new ArrayList();
            Map<Field, Column> columnMap = new HashMap();

            for (Field field : className.getFields()) {
                int modifiers = field.getModifiers();
                if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers))) {
                    Column column = Column.fromField(field, configVariant);
                    if (column != null && dbVersion >= column.fromVersion) { columnMap.put(field, column);
                    }
                }
            }
            while (resultSet.next()) {
                Object row = className.newInstance();
                for (Map.Entry<Field, Column> entry : columnMap.entrySet()) {
                    Column column = entry.getValue();
                    Field field = entry.getKey();
                    Object value = null;
                    switch (column.columnType) {
                        case BOOL:
                            value = resultSet.getBoolean(column.name);
                            break;
                        case TINYINT:
                            value = resultSet.getByte(column.name);
                            break;
                        case SMALLINT:
                            value = resultSet.getShort(column.name);
                            break;
                        case INT:
                            value = resultSet.getInt(column.name);
                            break;
                        case BIGINT:
                            value = resultSet.getLong(column.name);
                            break;
                        case FLOAT:
                            value = resultSet.getFloat(column.name);
                            break;
                        case DOUBLE:
                            value = resultSet.getDouble(column.name);
                            break;
                        case CHAR:
                            String strValue = resultSet.getString(column.name);
                            value = strValue != null ? strValue.charAt(0) : null;
                            break;
                        case VARCHAR:
                        case TEXT:
                            value = resultSet.getString(column.name);
                    }

                    if (value != null) {
                        field.set(row, value);
                    } else {
                    }
                }
                result.add(row);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Ошибка выполнения запроса!");
        }
    }
}
