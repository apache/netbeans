
package markOccurrences;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Test6 {


    public String  method(String name) throws FileNotFoundException, MalformedURLException {
        FileReader fr = new FileReader("a.b");        
        BufferedReader br = new BufferedReader(fr);
        if(name == null) return null;
        String s = null;
        try {
          s = br.readLine();
        } catch(IOException ioe) {
            
        }
        URL u = new URL(s);
        return s;
    }
}
