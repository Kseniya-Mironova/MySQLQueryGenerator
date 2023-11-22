package mysql;

import annotations.*;
import annotations.TableName;

public class Table {

    private Class className;
    private String tableName;

    public Table(Class className, String tableName) {
        this.className = className;
        this.tableName = ((tableName == null) || (tableName.isEmpty()) ? getTableName(className) : tableName);
    }

    public static String getTableName(Class className) {
        String tableName = "";
        if (className.isAnnotationPresent(TableName.class)) {
            TableName tableNameAnnotation = (TableName)className.getAnnotation(TableName.class);
            tableName = tableNameAnnotation.value();
        }

        if (tableName.isEmpty()) {
            tableName = className.getSimpleName() + "_table";
        }
        return tableName;
    }

    public static int getDefaultMaxLength(Class className) {
        if (className.isAnnotationPresent(DefaultMaxLength.class)) {
            DefaultMaxLength defaultMaxLength = (DefaultMaxLength)className.getAnnotation(DefaultMaxLength.class);
            return defaultMaxLength.value();
        }
        return 256;
    }

}
