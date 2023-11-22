package mysql;

public enum ColumnType {
    BOOL,
    TINYINT,
    SMALLINT,
    INT,
    BIGINT,
    FLOAT,
    DOUBLE,
    VARCHAR,
    CHAR,
    TEXT;

    public String toString() {
        switch (this) {
            case BOOL:
                return mysql.Const.STRING_BOOL;
            case TINYINT:
                return mysql.Const.STRING_TINYINT;
            case SMALLINT:
                return mysql.Const.STRING_SMALLINT;
            case INT:
                return mysql.Const.STRING_INT;
            case BIGINT:
                return mysql.Const.STRING_BIGINT;
            case FLOAT:
                return mysql.Const.STRING_FLOAT;
            case DOUBLE:
                return mysql.Const.STRING_DOUBLE;
            case CHAR:
            case VARCHAR:
                return mysql.Const.STRING_VARCHAR;
            case TEXT:
                return mysql.Const.STRING_TEXT;
        }

        return "OTHER";
    }
}
