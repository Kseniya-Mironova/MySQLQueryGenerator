package mysql;

import annotations.*;
import annotations.ColumnName;
import annotations.Default;
import annotations.FromVersion;
import annotations.MaxLength;
import annotations.NotNull;
import annotations.PrimaryKey;
import annotations.ResultSetConfig;

import java.lang.reflect.Field;

public class Column {

    public String name;
    public mysql.ColumnType columnType;
    public boolean isAutoIncrement;
    public boolean isPrimaryKey;
    public boolean isNotNull;
    public boolean hasDefaultValue;
    public String defaultValue;

    public int maxLength = 256;
    public int fromVersion = 0;

    private Column() {}

    public static Column fromField(Field field) {

        return fromField(field, "#");
    }

    public static Column fromField(Field field, String configVariant) {
        Class fieldType = field.getType();
        mysql.ColumnType columnType;
        if ((fieldType == Boolean.TYPE) || (fieldType == Boolean.class)) {
            columnType = mysql.ColumnType.BOOL;
        } else if ((fieldType == Byte.TYPE) || (fieldType == Byte.class)) {
            columnType = mysql.ColumnType.TINYINT;
        } else if ((fieldType == Short.TYPE) || (fieldType == Short.class)) {
            columnType = mysql.ColumnType.SMALLINT;
        } else if ((fieldType == Integer.TYPE) || (fieldType == Integer.class)) {
            columnType = mysql.ColumnType.INT;
        } else if ((fieldType == Long.TYPE) || (fieldType == Long.class)) {
            columnType = mysql.ColumnType.BIGINT;
        } else if ((fieldType == Float.TYPE) || (fieldType == Float.class)) {
            columnType = mysql.ColumnType.FLOAT;
        } else if ((fieldType == Double.TYPE) || (fieldType == Double.class)) {
            columnType = mysql.ColumnType.DOUBLE;
        } else if ((fieldType == Character.TYPE) || (fieldType == Character.class)) {
            columnType = mysql.ColumnType.CHAR;
        } else if (fieldType == String.class) {
            columnType = mysql.ColumnType.VARCHAR;
        } else {
            return null;
        }
        Column column = new Column();
        column.columnType = columnType;
        column.name = field.getName();
        column.isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);
        column.isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
        column.isNotNull = field.isAnnotationPresent(NotNull.class);
        column.hasDefaultValue = field.isAnnotationPresent(Default.class);
        if (column.hasDefaultValue) {
            Default defaultAnnotation = field.getAnnotation(Default.class);
            column.defaultValue = defaultAnnotation.value();
        }
        column.maxLength = mysql.Table.getDefaultMaxLength(field.getDeclaringClass());
        if (field.isAnnotationPresent(MaxLength.class)) {
            MaxLength maxLength = field.getAnnotation(MaxLength.class);
            column.maxLength = maxLength.value();
        }
        column.fromVersion = 0;
        if (field.isAnnotationPresent(FromVersion.class)) {
            FromVersion fromVersion = field.getAnnotation(FromVersion.class);
            column.fromVersion = fromVersion.value();
        }
        if ((columnType == mysql.ColumnType.VARCHAR) && (column.maxLength == Integer.MAX_VALUE)) {
            column.columnType = mysql.ColumnType.TEXT;
        }
        if (field.isAnnotationPresent(ColumnName.class)) {
            ColumnName columnName = field.getAnnotation(ColumnName.class);
            String name = columnName.value();
            if (!name.trim().isEmpty()) {
                column.name = name;
            }
        }
        if (field.isAnnotationPresent(ResultSetConfig.class)) {
            if (configVariant == null) return null;
            ResultSetConfig resultSetConfig = field.getAnnotation(ResultSetConfig.class);
            String configStr = resultSetConfig.value();
            String[] configArr = configStr.split(";");
            boolean variantFound = false;
            for (String config : configArr) {
                String[] mapping = config.split(":");
                String key = mapping[0].trim();
                String value = mapping[1].trim();
                if (configVariant.equals(key)) {
                    column.name = value;
                    variantFound = true;
                    break;
                }
            }
            if (!variantFound) return null;
            if (column.name.equals("#")) {
                return null;
            }
        }
        return column;
    }

    public Object getDefaultValue() {
        if (!hasDefaultValue) return null;
        switch (columnType) {
            case BOOL:
                return Boolean.parseBoolean(defaultValue);
            case TINYINT:
                return Byte.parseByte(defaultValue);
            case SMALLINT:
                return Short.parseShort(defaultValue);
            case INT:
                return Integer.parseInt(defaultValue);
            case BIGINT:
                return Long.parseLong(defaultValue);
            case FLOAT:
                return Float.parseFloat(defaultValue);
            case DOUBLE:
                return Double.parseDouble(defaultValue);
            case CHAR:
            case VARCHAR:
            case TEXT:
                return defaultValue;
        }
        return null;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("`");
        result.append(name);
        result.append("`");
        result.append(columnType.toString());
        if (columnType == mysql.ColumnType.CHAR) {
            result.append("(1)");
        } else if (columnType == mysql.ColumnType.VARCHAR) {
            result.append("(");
            result.append(maxLength);
            result.append(")");
        }
        if (isNotNull) { result.append(mysql.Const.STRING_NOT_NULL);
        }

        if (hasDefaultValue) {
            result.append(mysql.Const.STRING_DEFAULT);
        }
        if (isAutoIncrement) { result.append(mysql.Const.STRING_AUTO_INCREMENT);
        }
        return result.toString();
    }
}
