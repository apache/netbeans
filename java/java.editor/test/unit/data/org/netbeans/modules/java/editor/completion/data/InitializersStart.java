package test;

public class Test {

    public int field = hashCode() / 10;
    public static int staticField = 10;
    public Number num = ((Number)hashCode()).intValue();
    public boolean b = num instanceof Integer;
