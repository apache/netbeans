/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package general;

/**
 *
 * @author jp159440
 */
public class BracesGeneration {

    public void generate(boolean a) {
        if (a) System.out.println("");       
        while (a) System.out.println("");        
        do System.out.println(""); while (a);
        for (; a;) System.out.println("");
    }

    public void eliminate(boolean a) {
        if (a) {
            System.out.println("");
        }
        while (a) {
            System.out.println("");
        }
        do {
            System.out.println("");
        } while (a);
        for (; a;) {
            System.out.println("");
        }
    }

    public void cannotEliminate(boolean a) {
        if (a) {
            System.out.println("");
            System.out.println("");
        }
        while (a) {
            System.out.println("");
            System.out.println("");
        }
        do {
            System.out.println("");
            System.out.println("");
        } while (a);
        for (; a;) {
            System.out.println("");
            System.out.println("");
        }
    }
}
