package test;

import java.io.File;
import java.io.FileWriter;

public class Test {

    public void op(File f) {
        try (final FileWriter fw = new FileWriter(f)) {
            fw.write("Hello");
        } catch (java.io.IOException e) {
        }
    }
}
