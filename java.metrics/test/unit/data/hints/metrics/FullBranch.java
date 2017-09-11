/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author sdedic
 */
public class FullBranch<A extends java.util.Iterator> {
    private Collection col;
    private Collection<String> stringCol;
    private int d[][] = new int[5][];
    
    @Deprecated
    public void n() {
        if (col instanceof List) {
            d[0][0] = (Integer)((List)col).get(1) + d[0][1];
        } else {
            Collection<?> o = col;
            d[2][0] = col.size();
        }
        d[1][0] = other.m(2);
    }
    
    public int m(int input) {
        @Deprecated
        int a = 0;
        OUTER: for (int i = 0; i < 10; i++) {
            int j = 0;
            try {
                do {
                    if (j % 2 == 0) {
                        a = a + Math.random() > 0.5 ? 1 : 2;
                    }
                    if (i % 2 == 0) {
                        a *= 2;
                        throw new RuntimeException();
                    }
                    if (j == i / 2) {
                        break;
                    } else if (j == i / 3) {
                        continue OUTER;
                    }
                    j++;
                } while (j < i);
            } catch (IllegalArgumentException ex) {
                a--; 
            } catch (NullPointerException ex) {
                a -= this.m(a);
            }
            for (Object o : col) {
                int x = 0;
                while (x < 10) {
                    x++;
                    switch (x) {
                        case 1:
                    }
                }
            }
        }
        assert a >= 0;
        return a;
    }
    
    private FullBranch other;
}
