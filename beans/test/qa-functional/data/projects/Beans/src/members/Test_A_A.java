package members;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;

public class Test_A_A {

    private String name;

    public static class C1 {

        interface I1 {

            public void m1();
        }

        @interface Annotation {

            String value();

            String[] array() default "";
        }

        public void m2() {
            JButton button = new JButton();
            button.addActionListener(new ActionListenerImpl());
        }

        private class ActionListenerImpl implements ActionListener {

            public ActionListenerImpl() {
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("click");
            }
        }
    }

    public Test_A_A(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList getAl() {
        return al;
    }

    private ArrayList al;

    public void setAl(ArrayList al) {
        this.al = al;
    }
}
