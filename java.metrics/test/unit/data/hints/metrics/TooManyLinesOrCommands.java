/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Collection;

/**
 *
 * @author sdedic
 */
public class TooManyLinesOrCommands {
    private Collection col;

    public int tooManyLines() {
        @Deprecated 
        int a = 
                0; // 1  
        OUTER: for (
                int i = 0; 
                i < 10; 
                i++) { // 2
            int 
                j = 
                0; // 3
            try { 
                do { // 4
                    if 
                        (j % 2 == 0) { // 5
                        a = 
                                a + 
                                Math.random() 
                                > 0.5 ? 
                                1 : 
                                2; // 6
                    }
                    if 
                        (i 
                        % 2 
                        == 0) { // 7
                        a 
                        *= 
                        2; // 8
                        throw 
                            new RuntimeException(); // 9
                    }
                    if 
                        (j 
                            == 
                            i / 2) { // 10
                        break; // 11
                    } else 
                    if (j 
                            == 
                            i / 3) { // 12
                        continue OUTER; // 13
                    }
                    j++; // 14
                } while 
                    (j < i); 
            } catch (IllegalArgumentException ex) { // 15
                a--; // 16
            } catch (NullPointerException ex) { // 17
                a -= 
                    this.tooManyStatements(); // 18
            }
            for (Object o : col) { // 19
                int x 
                        = 0; // 20
                while 
                    (x < 10) { // 21
                    x++; // 22
                    switch (x) { // 23
                        case 1: 
                            continue; // 24
                    }
                }
            }
        }
        assert a >= 0; // -
        if 
            (a > 0) { // 25
            a += 
                3; // 26
            tooManyStatements(); // 27
            while 
                (a > 1) { // 28
                return 1; // 29
            }
        }
        return 
                a; // 30
    }

    public int tooManyStatements() {
        @Deprecated 
        int a = 0; // 1  
        OUTER: for (int i = 0; i < 10; i++) { // 2
            int j = 0; // 3
            try { 
                do { // 4
                    if (j % 2 == 0) { // 5
                        a = a + Math.random() > 0.5 ? 1 : 2; // 6
                    }
                    if (i % 2 == 0) { // 7
                        a *= 2; // 8
                        throw new RuntimeException(); // 9
                    }
                    if (j == i / 2) { // 10
                        break; // 11
                    } else if (j == i / 3) { // 12
                        continue OUTER; // 13
                    }
                    j++; // 14
                } while (j < i); 
            } catch (IllegalArgumentException ex) { // 15
                a--; // 16
            } catch (NullPointerException ex) { // 17
                a -= this.tooManyStatements(); // 18
            }
            for (Object o : col) { // 19
                int x = 0; // 20
                while (x < 10) { // 21
                    x++; // 22
                    switch (x) { // 23
                        case 1: 
                            continue; // 24
                    }
                }
            }
        }
        assert a >= 0; // -
        if (a > 0) { // 25
            a += 3; // 26
            tooManyStatements(); // 27
            while (a > 1) { // 28
                return 1; // 29
            }
            a++; // 30
        }
        return a; // 31
    }
}
