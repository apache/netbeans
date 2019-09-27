package markOccurrences;

import javax.swing.JTable;

public class Test5 {

    public void local(double number,JTable table) {
        int counter = 1;
        System.out.println(number);
        while(counter<number) {
            counter = counter + 1;
            System.out.println(counter);
        }
    }

}
