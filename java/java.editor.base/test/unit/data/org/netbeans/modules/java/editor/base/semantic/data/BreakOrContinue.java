package org.netbeans.modules.java.editor.semantic.data;

public class BreakOrContinue {
    
    public static void main(String[] args) {
        A: while (1 == 1) {
            B: for (int c = 0; c < 100; c++) {
                switch (c) {
                    case 0: break;
                    case 1: break B;
                    case 2: break A;
                    case 3: continue;
                    case 4: continue A;
                    case 5: continue B;
                }
                if (1 == 1) {
                    break;
                } else {
                    if (1 == 1) {
                        break B;
                    } else {
                        if (1 == 1)
                            break A;
                    }
                }
                if (1 == 1) {
                    continue;
                } else {
                    if (1 == 1) {
                        continue B;
                    } else {
                        continue A;
                    }
                }
            }
            if (1 == 1) {
                break;
            } else {
                if (1 == 1)
                    break A;
            }
            if (1 == 1) {
                continue;
            } else {
                continue A;
            }
        }
        
        A: while (1 == 1)
            B: for (int c = 0; c < 100; c++)
                switch (c) {
                    case 0: break;
                    case 1: break B;
                    case 2: break A;
                    case 3: continue;
                    case 4: continue A;
                    case 5: continue B;
                }

        AAA: do {
            if (1 == 1) {
                break AAA;
            } else {
                if (1 == 1) {
                    break;
                }
            }
            if (1 == 1) {
                continue AAA;
            } else {
                if (1 == 1) {
                    continue;
                }
            }
        } while (1 == 1);
        
        AAA: do {
            if (1 == 1) {
                break XXX;
            } else {
                if (1 == 1) {
                    continue XXX;
                }
            }
        } while (1 == 1);
    }
    
}
