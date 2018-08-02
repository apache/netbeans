package org.netbeans.test.java.hints;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

public class Test {
    public Test() {
        this(new FileInputStream("")); //the hint should not be proposed here
    }
    
    public Test(int i) {
        this(new aa(new FileInputStream(""))); //the hint should not be proposed here
    }

    public Test(InputStream i) {
        new FileInputStream("");
    }
    
    Test(Runnable r) { this(bb.thx());}

    Test(aa a) {
       this(new Runnable() {
            public void run() {
                new FileInputStream(""); //the hint should be proposed here.
            }
        });
    }
    
    Test(double d) {
	java.io.BufferedInputStream bi = null;
        bi.read(new byte[bi.read()]);
    }
    
    URL url = new URL("");
}

class aa {
    public aa(FileInputStream is) {
    }
}

class bb {
    static double thx() throws java.io.IOException {
    }
}
    
