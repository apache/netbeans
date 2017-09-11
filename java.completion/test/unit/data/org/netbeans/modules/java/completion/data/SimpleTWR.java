package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Test {

    public void op(FileWriter fw) {
        try (fw; BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("Hello");
        } catch (IOException e) {
        }
    }
}
