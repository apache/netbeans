package codeGenerating;

public class SortOrder {

    private static int statField;

    static {
        //initializer
        statField = 2;
    }

    public static int getField() {
        return statField;
    }
    private int field;

    {
        //instance initializer
        field = 1;
    }

    public SortOrder() {
    }

    public static int getStatField() {
        return statField;
    }

    public static class InnerStaticClass {
    }

    private class InserClass {
    }
}
