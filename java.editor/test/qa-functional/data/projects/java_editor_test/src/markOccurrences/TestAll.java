
package markOccurrences;

import java.io.IOException;
import java.nio.CharBuffer;

public class TestAll extends Exception implements Readable{
    
    @Override
    public String getMessage() {
        return super.getMessage();
    }
    
    public static final int KONST = 3;
    
    public TestAll() {
        String s;
    }
    
    public int read(CharBuffer cb) throws IOException {
        if(cb==null) throw  new IOException("error");
        int i = 0;
        int count = 0;
        LOOP: while(cb.charAt(i)==' ') {
            i++;
            count++;
            if(count>KONST) break LOOP;            
        }
        return 0;
        
    }
    
    
}
