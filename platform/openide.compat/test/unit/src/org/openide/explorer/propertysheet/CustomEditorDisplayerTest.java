/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.openide.nodes.*;
import java.beans.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.*;
import org.openide.ErrorManager;

/* A comprehensive test of CustomEditorDisplayer */
public class CustomEditorDisplayerTest extends NbTestCase {
    
    static {
        ComboTest.registerPropertyEditors();
    }
    
    public CustomEditorDisplayerTest(String name) {
        super(name);
    }
    
//    public static void main(String args[]) {
//        LookAndFeel lf = UIManager.getLookAndFeel();
/*        try {
            UIManager.setLookAndFeel(new com.jgoodies.plaf.plastic.Plastic3DLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        TestRunner.run(suite ());
        
        
        boolean go=false;
        try {
            UIManager.setLookAndFeel(new PseudoWindowsLookAndFeel());
            go = true;
        } catch (NoClassDefFoundError e) {
            System.err.println("Couldn't run tests on windows look and feel");
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Couldn't run tests on windows look and feel");
        }            
        if (go) {
            TestRunner.run(suite ());
        }
        go=false;
        try {
            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.gtk.GTKLookAndFeel());
            go = true;
        } catch (NoClassDefFoundError e) {
            System.err.println("Couldn't run tests on GTK look and feel");
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Couldn't run tests on GTK look and feel");
            e.printStackTrace();
        }
        if (go) {
            TestRunner.run(suite ());
        }
        try {
            UIManager.setLookAndFeel(lf);
        } catch (Exception e) {
            //highly unlikely 
        }
        try {
//        new CustomEditorDisplayerTest("goo").setUp();
        } catch (Exception e){}
 
    }
         */

    static int idx = -1;
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
   
    CustomEditorDisplayer basicRen;
    CustomEditorDisplayer fileRen;
    
    private TNode tn;
    private BasicProperty basicProp;
    private FileProperty fileProp;
    private BasicEditor te;
    
    private boolean setup=false;
    private JFrame jf=null;
    private JPanel jp=null;
    private int SLEEP_LENGTH=10;
    
    protected void tearDown() {
        if (jf != null) {
            jf.hide();
            jf.dispose();
        }
    }
   
    protected void setUp() throws Exception {
//            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
//            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.gtk.GTKLookAndFeel());

        try {
            if (setup) return;
            basicProp= new BasicProperty("basicProp", true);
            fileProp= new FileProperty("FileProp", true);
            
            // Create new BasicEditor
            te = new BasicEditor();
            // Create new TNode
            tn = new TNode();

            System.err.println("Crating frame");
            jf = new JFrame();
            jf.getContentPane().setLayout(new BorderLayout());
            jp = new JPanel();
            jp.setLayout(new FlowLayout());
            jf.getContentPane().add(jp, BorderLayout.CENTER);
            jf.setLocation (20,20);
            jf.setSize (600, 200);

            synchronized (jp.getTreeLock()) {
                System.err.println("BasicProp = " + basicProp);

                basicRen = new CustomEditorDisplayer(basicProp);
                fileRen = new CustomEditorDisplayer(fileProp);

                
                jp.add(basicRen.getComponent());
                
                jp.add(fileRen.getComponent());
            }

            System.err.println("Waiting for window");
            new WaitWindow(jf);  //block until window open
            System.err.println("Window shown");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            setup = true;
        }
    }
    
    public void testDummy() {
        
    }

    public void disabled_testEntryInCustomEditor() throws Exception {
        //Just types into the value field and presses
        basicRen.setUpdatePolicy(PropertyDisplayer.UPDATE_ON_CONFIRMATION);
        
        BasicCustomEditor custom = (BasicCustomEditor)basicProp.getPropertyEditor().getCustomEditor();
        
        clickOn(custom.valueField);
        typeKey(custom.valueField, KeyEvent.VK_W);
        typeKey(custom.valueField, KeyEvent.VK_O);
        typeKey(custom.valueField, KeyEvent.VK_O);
        typeKey(custom.valueField, KeyEvent.VK_G);
        typeKey(custom.valueField, KeyEvent.VK_L);
        typeKey(custom.valueField, KeyEvent.VK_E);

        Object pre = basicProp.getValue();

        pressKey(custom.valueField, KeyEvent.VK_ENTER);

        Object post = basicProp.getValue();
        
        assertTrue ("After entering text in editor, value should be the text.  Expected WOOGLE got " + post, "WOOGLE".equals(post));

        assertNotSame("After entering data in the custom editor and pressing enter with policy UPDATE_ON_CONFIRMATION, property value should be changed", pre, post);
        
        basicRen.setUpdatePolicy(PropertyDisplayer.UPDATE_ON_EXPLICIT_REQUEST);
        
        clickOn(custom.valueField);
        typeKey(custom.valueField, KeyEvent.VK_N);
        typeKey(custom.valueField, KeyEvent.VK_I);
        typeKey(custom.valueField, KeyEvent.VK_F);
        typeKey(custom.valueField, KeyEvent.VK_T);
        typeKey(custom.valueField, KeyEvent.VK_Y);

        pre = basicProp.getValue();

        pressKey(custom.valueField, KeyEvent.VK_ENTER);

        post = basicProp.getValue();
        
        assertTrue ("After entering text in editor with policy UPDATE_ON_EXPLICIT_REQUEST, the property value should not be the entered text.  Expected NIFTY got " + post, "WOOGLE".equals(post));

        assertSame("After entering data in the custom editor and pressing enter with policy UPDATE_ON_EXPLICIT_REQUEST, property value should NOT be changed", pre, post);
        
        
    }
    
    public void disabled_testFailureModes() throws Exception {
        basicRen.setUpdatePolicy(PropertyDisplayer.UPDATE_ON_CONFIRMATION);
        BasicCustomEditor custom = (BasicCustomEditor)basicProp.getPropertyEditor().getCustomEditor();
        
        custom.setInvalidValueButton.doClick();
        sleep();
        sleep();
        requestFocus(custom.valueField);
        
        
        IllegalArgumentException iae=null;
        try {
            pressKey(custom.valueField, KeyEvent.VK_ENTER);
        } catch (IllegalArgumentException e) {
            iae = e;
        }
        assertNotNull("Entering a bad value should throw an exception", iae);
        iae = null;
        
        custom.setDontAllowValidateButton.doClick();
        sleep();
        sleep();
        requestFocus(custom.valueField);
        try {
            pressKey(custom.valueField, KeyEvent.VK_ENTER);
        } catch (IllegalArgumentException e) {
            iae = e;
        }
        assertNotNull("If a state change on the PropertyEnv causes a PropertyVetoException, an illegal argument exception should be thrown with the message from the PVE", iae);
        iae = null;
        
        BasicEditor editor = (BasicEditor) basicRen.getPropertyEditor();

        
        PropertyEnv env = basicRen.getPropertyEnv();
        assertEquals("After a failure to validate, the editor's property env's state should be STATE_INVALID", PropertyEnv.STATE_INVALID, env.getState());
        
        assertSame("After a failure to validate, the PropertyEnv the editor is talking to should be the one owned by the CustomEditorDisplayer", env, editor.env);
    }
    
    
    public void disabled_testValidationMethods() throws Exception {
        basicRen.setUpdatePolicy(PropertyDisplayer.UPDATE_ON_EXPLICIT_REQUEST);
        BasicCustomEditor custom = (BasicCustomEditor)basicProp.getPropertyEditor().getCustomEditor();
        
        clickOn(custom.valueField);
        typeKey(custom.valueField, KeyEvent.VK_F);
        typeKey(custom.valueField, KeyEvent.VK_U);
        typeKey(custom.valueField, KeyEvent.VK_N);
        typeKey(custom.valueField, KeyEvent.VK_K);
        typeKey(custom.valueField, KeyEvent.VK_Y);
        pressKey(custom.valueField, KeyEvent.VK_ENTER);

        assertTrue("After entering text with update policy UPDATE_ON_EXPLICIT_REQUEST, isValueModified should return true", basicRen.isValueModified());
        String legality = basicRen.isModifiedValueLegal();
        assertTrue("After entering a legal value with update policy UPDATE_ON_EXPLICIT_REQUEST, isModifiedValueLegal should return null but returned " + legality, legality == null);
        
        Exception e = null;
        try {
            basicRen.commit();
        } catch (Exception e1) {
            e = e1;
        }
        assertNull("Committing a legal value should not throw an exception", e);
        assertEquals("Calling commit() with update policy UPDATE_ON_EXPLICIT_REQUEST should store the edited value in the property", 
            "FUNKY", basicProp.getValue());
        
        assertTrue("After committing a legal value, isValueModified should return false", !basicRen.isValueModified());
        assertNull("After committing a legal value, isModifiedValueLegal should return null", basicRen.isModifiedValueLegal());
        
        custom.setDontAllowValidateButton.doClick();
        sleep();
        sleep();
        
        try {
            pressKey(custom.valueField, KeyEvent.VK_ENTER);
        } catch (Exception e2) {
            
        }
        
        assertNotNull("With an unvalidatable value, isModifiedValueLegal should return a localized message", basicRen.isModifiedValueLegal());
        assertTrue("With an unvalidatable value, isValueModified should return true", basicRen.isValueModified());
        
        custom.valueField.setText("foo goo");
        custom.setDontAllowValidateButton.doClick();
        sleep();
        sleep();
        
        try {
            basicRen.commit();
        } catch (Exception e3) {
            e = e3;
        }
        
        assertNotNull("Committing an unvalidatable value should throw an exception", e);
        
    }
    
    private class FL implements FocusListener {
        private FocusEvent gainedEvent=null;
        private FocusEvent lostEvent=null;
        private int gainedCount=0;
        private int lostCount=0;
        public void assertGained() {
            assertNotNull ("No focus gained received after clicking on an editable renderer", gainedEvent);
            assertTrue("Received wrong number of focus gained events for a single click on a renderer " +  gainedCount, gainedCount == 1);
        }
        
        public void assertLost() {
            assertNotNull ("No focus lost event received after clicking away from a focused, editable renderer", lostEvent);
            assertTrue("Received wrong number of focus lost events for a single click away from a focused renderer" + lostCount, lostCount == 1);
        }
        
        public void focusGained(java.awt.event.FocusEvent e) {
            gainedEvent = e;
            gainedCount++;
        }
        
        public void focusLost(java.awt.event.FocusEvent e) {
            lostEvent = e;
            lostCount++;
        }
    }
    
    private class CL implements ChangeListener {
        
        private ChangeEvent e;
        public void assertEvent(String msg) {
            sleep(); //give the event time to happen
            assertNotNull (msg, e);
            e = null;
        }
        
        public void assertNoEvent(String msg) {
            sleep();
            assertNull (e);
            e = null;
        }
        
        public void stateChanged(ChangeEvent e) {
            this.e = e;
        }
        
    }
    
    private static class TestGCVal extends Object {
        public String toString() {
            return "TestGCVal";
        }
    }

    private static class WaitWindow extends WindowAdapter {
        boolean shown=false;
        public WaitWindow (JFrame f) {
            f.addWindowListener(this);
            f.show();
            if (!shown) {
                synchronized(this) {
                    try {
                        //System.err.println("Waiting for window");
                            wait(5000);
                    } catch (Exception e) {}
                }
            }
        }

        @Override
        public void windowOpened(WindowEvent e) {
            shown = true;
            synchronized(this) {
                //System.err.println("window opened");
                notifyAll();
                ((JFrame) e.getSource()).removeWindowListener(this);
            }
        }
    }
    
    private void sleep() {
         //useful when running interactively
        
        try {
            Thread.currentThread().sleep(SLEEP_LENGTH);
        } catch (InterruptedException ie) {
            //go away
        }
         
        
         
        //runs faster -uncomment for production use
        
        try {
            //jf.getTreeLock().wait();
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    System.currentTimeMillis();
                }
            });
            //jf.getTreeLock().wait();
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    System.currentTimeMillis();
                }
            });            
        } catch (Exception e) {
        }
        
        
    }
    
    private void requestFocus(final JComponent jc) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                jc.requestFocus();
            }
        });
        sleep();
    }
    
    private void changeProperty (final RendererPropertyDisplayer ren, final Node.Property newProp) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ren.setProperty(newProp);
            }
        });
    }
    
    private void clickOn (final JComponent ren, final int fromRight, final int fromTop) throws Exception {
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                Point toClick = new Point(ren.getWidth() - fromRight, fromTop);
                Component target=ren.getComponentAt(toClick);
                toClick = SwingUtilities.convertPoint(ren, toClick, target);
                System.err.println("Target component is " + target.getClass().getName() + " - " + target + " clicking at " + toClick);
                
                MouseEvent me = new MouseEvent (target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
                target.dispatchEvent(me);
                me = new MouseEvent (target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
                target.dispatchEvent(me);
                me = new MouseEvent (target, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
            }
        });
        sleep();
    }
    
    private void clickOn (final JComponent ren) throws Exception {
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                Point toClick = new Point(5,5);
                Component target=ren.getComponentAt(toClick);
                MouseEvent me = new MouseEvent (target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
                target.dispatchEvent(me);
            }
        });
        sleep();
    }
    
    private void setEnabled(final CustomEditorDisplayer ren,final boolean val) throws Exception {
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                ren.setEnabled(val);
            }
        });
        sleep();
    }
    
    private Exception throwMe = null;
    private String flushResult = null;
    private String flushValue(final CustomEditorDisplayer ren) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    //flushResult = ren.flushValue();
                } catch (Exception e) {
                    throwMe = e;
                    flushResult = null;
                }
            }
        });
        if (throwMe != null) {
            try {
                throw throwMe;
            } finally {
                throwMe = null;
            }
        }
        return flushResult;
    }
    

    private void releaseKey (final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                KeyEvent ke = new KeyEvent (target, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, key, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
    private Exception throwMe2 = null;
    private void pressKey (final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                KeyEvent ke = new KeyEvent (target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, (char) key);
                try {
                    target.dispatchEvent(ke);
                } catch (Exception e) {
                    throwMe2 = e;
                }
            }
        });
        sleep();
        if (throwMe2 != null) {
            Exception e1 = throwMe2;
            throwMe2 = null;
            throw e1;
        }
    }
    
    private void shiftPressKey (final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                KeyEvent ke = new KeyEvent (target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.SHIFT_MASK, key, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
        
    private void typeKey (final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                KeyEvent ke = new KeyEvent (target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
    //Node definition
    public class TNode extends AbstractNode {
        //create Node
        public TNode() {
            super (Children.LEAF);
            setName("TNode"); // or, super.setName if needed
            setDisplayName("TNode");
            createSheet();
        }
        //clone existing Node
        public Node cloneNode() {
            return new TNode();
        }
        
        public void addProp (Node.Property p) {
            props.put(p);
            this.firePropertyChange(PROP_PROPERTY_SETS, null, null);
            this.firePropertySetsChange(null, null);
        }
        
        Sheet sheet=null;
        Sheet.Set props=null;
        // Create a property sheet:
        protected Sheet createSheet() {
            sheet = super.createSheet();
            // Make sure there is a "Properties" set:
            props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                props = Sheet.createPropertiesSet();
                sheet.put(props);
            }
            props.put(basicProp);
            props.put(fileProp);
            
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            firePropertyChange(s,o1,o2);
        }
    }
    
    // Property definition
    public class BasicProperty extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public BasicProperty(String name, boolean isWriteable) {
            super(name, Object.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            System.err.println("BASICPROP setValue to " + value + " (was " + myValue+")");
            Object oldVal = myValue;
            myValue = value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        // get the property editor
        public PropertyEditor getPropertyEditor() {
            return te;
        }
    }

    // Property definition
    public class FileProperty extends PropertySupport {
        private Object myValue = new File("aFile");
        // Create new Property
        public FileProperty(String name, boolean isWriteable) {
            super(name, File.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
    }
    
    
    // Editor definition
    public class BasicEditor extends PropertyEditorSupport implements ExPropertyEditor, PropertyChangeListener, VetoableChangeListener {
        PropertyEnv env;
        
        // Create new BasicEditor
        public BasicEditor() {
        }
        
        /*
         * This method is called by the IDE to pass
         * the environment to the property editor.
         */
        public void attachEnv(PropertyEnv env) {
            if (env != null) {
                env.removeVetoableChangeListener(this);
            }
            this.env = env;
            
            env.setState(env.STATE_VALID);
            env.addVetoableChangeListener(this);
            System.err.println("  ATTACHENV");
            
        }
        
        // Set that this Editor doesn't support custom Editor
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        // Set the Property value threw the Editor
        @Override
        public void setValue(Object newValue) {
            System.err.println(" BasicEditor.setValue: " + newValue);
            super.setValue(newValue);
        }

        @Override
        public String getAsText() {
            return getValue() == null ? "null" : getValue().toString();
        }

        private Component custom;
        @Override
        public Component getCustomEditor() {
            if (custom == null) {
                custom = new BasicCustomEditor(this);
            }
            return custom;
        }
        
        public void vetoNext() {
            env.setState(env.STATE_NEEDS_VALIDATION);
            vetoNextChange = true;
            System.err.println(" veto next");
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            
        }
        
        boolean vetoNextChange=false;
        public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
            System.err.println("GOT A VETOABLE CHANGE IN BASIC EDITOR");
            PropertyEnv env = (PropertyEnv) e.getSource();
            if ((vetoNextChange || "Dont allow validate".equals(getAsText())) && PropertyEnv.STATE_NEEDS_VALIDATION.equals(env.getState())) {
                System.err.println(" VETOING");
                PropertyVetoException pve = new PropertyVetoException("NoNoNoNoNo", e);
                ErrorManager.getDefault().annotate(pve, ErrorManager.USER, null, "You can't do that!", null, null);
                vetoNextChange=false;
                throw pve;
            }
        }

        @Override
        public void setAsText(String s) {
            System.err.println(" BasicEditor.setAsText: " + s);
            if ("invalidValue".equals(s)) {
                IllegalArgumentException iae = new IllegalArgumentException();
                ErrorManager.getDefault().annotate(iae, ErrorManager.USER, "invalid value", "No way", null, null);
                throw iae;
            }
            setValue(s);
        }
        
    }
    
    
    public class BasicCustomEditor extends JPanel implements ActionListener {
        JTextField valueField=new JTextField();
        JButton setInvalidValueButton = new JButton("Invalid value");
        JButton setDontAllowValidateButton = new JButton("Dont allow validate");
        BasicEditor editor;
        public BasicCustomEditor(BasicEditor editor) {
            this.editor = editor;
            init();
        }
        
        private void init() {
            setLayout(new FlowLayout());
            valueField.addActionListener(this);
            setInvalidValueButton.addActionListener(this);
            setDontAllowValidateButton.addActionListener(this);
            valueField.setColumns(30);
            setBackground(Color.ORANGE);
            add (valueField);
            add (setInvalidValueButton);
            add (setDontAllowValidateButton);
        }
        boolean processing;
        public void actionPerformed(ActionEvent e) {
            processing = true;
            try {
                if (e.getSource() == setDontAllowValidateButton) {
                    editor.vetoNext();
                    valueField.setText("dont allow validate");
                }
                if (e.getSource() == setInvalidValueButton) {
                    valueField.setText("invalidValue");
                }
                if (e.getSource() == valueField) {
                    editor.setAsText(valueField.getText());
                }
                editor.env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            } finally {
                processing = false;
            }
        }
        
    }
}
