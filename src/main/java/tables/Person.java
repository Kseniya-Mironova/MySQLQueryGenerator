package tables;

import annotations.*;
import annotations.PrimaryKey;
import annotations.TableName;


@TableName("person") // Произвольное имя таблицы
public class Person {
    @AutoIncrement // Добавить модификатор AUTO_INCREMENT
    @PrimaryKey // Создать на основе этого поля PRIMARY KEY
    public int id;
    @NotNull // Добавить модификатор NOT NULL
    public long createTime;
    @NotNull
    public String firstName;
    @NotNull
    public String lastName;
    @Default("21") // Значение по умолчанию
    public Integer age;
    @Default("")
    @MaxLength(1024) // Длина VARCHAR
    public String address;
    @ColumnName("letter") // Произвольное имя поля
    public Character someLetter;
}
