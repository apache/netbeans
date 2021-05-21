package org.netbeans.test.java.hints.BroadCatchBlockTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SingleUmbrella {
    public void u() throws Exception {
        try {
            FileInputStream istm = new FileInputStream("foobar");
        } catch (FileNotFoundException parent) {
             
        }
    }
}
