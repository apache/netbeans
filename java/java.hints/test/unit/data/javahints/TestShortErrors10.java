package javahints;

public class TestShortErrors10 {

    public String formatThis(String argument) {
        return String.format(FORMAT_STRING, new Object());
    }
}
