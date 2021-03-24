package test;

import java.util.Collections;

public class App {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.err.println("args." + i + "=" + args[i]);
        }
        String[] props = Collections.list(System.getProperties().propertyNames()).stream().
            filter(n -> n.toString().startsWith("test.")).
            map(Object::toString).sorted().toArray(i -> new String[i]);

        for (int i = 0; i < props.length; i++) {
            System.err.println("prop." + i + "=" + props[i] + "=" + System.getProperty(props[i]));
        }
    }
}
