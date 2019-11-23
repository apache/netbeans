/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.test.java.hints.TooStrongCastTest;

import java.awt.Component;
import java.awt.event.MouseWheelEvent;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JViewport;

public class CastMethods {
    public void callMethodInBaseClass() {
        Component view = new JLabel();
        ((JViewport)view).getBounds(); 
    }
    
    public void callMethodInBaseClassAndUseResult() {
        JPanel panel = null;
        Object event = null;
        
        int x = panel.getToolTipLocation((MouseWheelEvent)event).x;
    }
    
    public void useTypeInParameter() {
        Component c = null;
        JRootPane rootPane = new JRootPane();
        rootPane.add((JButton)c);
    }
    
    public void useOverridenMethod() {
        JRootPane rootPane = new JRootPane();
        Object o = null;
        rootPane.add(((SuperDerived)o).c());
    }
    
    public void useOverridingMethod() {
        Object o = null;
        DefaultCellEditor ed = new DefaultCellEditor(((SuperDerived)o).c());
    }
    
    interface Base {
        public Object c();
    }
    
    interface Derived extends Base {
        public JComponent c();
    }
    
    interface SuperDerived extends Derived {
        public JTextField c();
    }
}
