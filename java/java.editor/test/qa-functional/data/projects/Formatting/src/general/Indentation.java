/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package general;

/**
 *
 * @author jp159440
 */
public class Indentation {

    {
    }

    static {
    }

    public void method(int param) {
        synchronized (this) {
            if (param == 2) {
            } else if (param == 3) {
            }
        }

        switch (param) {
            case 1:
                System.out.println("one");
                break;
            default:
                System.out.println("other");

        }

        while (param != 0) {
            param--;
            for (int i = 0; i < param; i++) {
                System.out.println(i);
            }
        }
    }

    class Inner {

        int a;

        public void main(String[] args) {
        }
    }
    int filed;
}
