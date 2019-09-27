package test;

import java.lang.ref.WeakReference;
import javax.swing.text.Document;

public class BLE91246 {
    
    public void test() {
        Document doc = null;
        
        doc.putProperty(Document.class, new WeakReference<Document>(null) {});
    }
    
}
