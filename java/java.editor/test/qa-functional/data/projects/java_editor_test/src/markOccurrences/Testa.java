
package markOccurrences;

public class Testa {
    
    public void loops(int c1) {
        OUTER:
            while(c1>0) {
                if(c1==2) continue OUTER;
                INNER:
                    for (int i = 0; i < 10; i++) {
                        if(i==c1) break OUTER;
                        if(i==0) continue INNER;
                        
                    }
                
            }
    }
    
    
}
