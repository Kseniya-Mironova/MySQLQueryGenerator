package tables;

import annotations.*;
import annotations.TableName;

@TableName("some")
public class SomeTable
{
    @PrimaryKey
    @AutoIncrement
    public int id;
    @DoNotCreateNotInsert
    public int intNotCreate;
    @DoNotCreateNotInsert
    public String stringNotCreate;
    @DoNotInsert
    public int intNotInsert;
    @DoNotInsert
    public String stringNotInsert;
    @DoNotCreateNotInsert
    public int intNotCreateNorInsert;
    @DoNotCreateNotInsert
    public String stringNotCreateNorInsert;
    @Default("0")
    public int intV0;
    @Default("")
    public String stringV0;
    @FromVersion(1)
    public int intV1;
    @FromVersion(1)
    public String stringV1 = "v1";
    @FromVersion(1) //исправила, было 2
    @Default("2")
    public int intV2;
    @Default("two")
    @FromVersion(2)
    public String stringV2;
    @FromVersion(3)
    @Default("3")
    public int intV3;
    @Default("three")
    @FromVersion(3)
    public String stringV3;
}
