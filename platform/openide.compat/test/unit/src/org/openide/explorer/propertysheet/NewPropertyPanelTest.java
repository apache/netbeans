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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import javax.swing.text.JTextComponent;
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

/* A comprehensive test of PropertyPanel */
public class NewPropertyPanelTest extends NbTestCase {
    
    static {
        ComboTest.registerPropertyEditors();
    }
    
    public NewPropertyPanelTest(String name) {
        super(name);
    }
    
//    public static void main(String args[]) {
//        LookAndFeel lf = UIManager.getLookAndFeel();
/*        try {
            UIManager.setLookAndFeel(new com.jgoodies.plaf.plastic.Plastic3DLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
 */
        
        
//        TestRunner.run(suite ());
        
        
        /*
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
         */
//        try {
//        new NewPropertyPanelTest("goo").setUp();
//        } catch (Exception e){}
 
//    }

    static int idx = -1;
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
   
    private JButton focusButton;
    PropertyPanel customPanel;
    PropertyPanel filePanel;
    PropertyPanel tagsPanel;
    PropertyPanel stringPanel;
    
    private TNode tn;
    private BasicProperty basicProp;
    private FileProperty fileProp;
    private BasicEditor te;
    private TagsProperty tagsProp;
    private StringProperty stringProp;
    
    private boolean setup=false;
    private JFrame jf=null;
    private JPanel jp=null;
    private int SLEEP_LENGTH=10; //Make this longer to see what the test is doing
    
    protected void tearDown() {
        if (jf != null) {
//            jf.hide();
//            jf.dispose();
        }
    }
   
    protected void setUp() throws Exception {
        PropUtils.forceRadioButtons=false;
        
        
        try {
            if (setup) return;
            basicProp= new BasicProperty("basicProp", true);
            fileProp= new FileProperty("FileProp", true);
            tagsProp = new TagsProperty("TagsProp", true);
            stringProp = new StringProperty("StringProp", true);
            
            focusButton = new JButton("Somewhere over the rainbow");
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
            jf.setSize (600, 600);

            synchronized (jp.getTreeLock()) {
                System.err.println("BasicProp = " + basicProp);

                customPanel = new PropertyPanel(basicProp);
                filePanel = new PropertyPanel(fileProp);
                tagsPanel = new PropertyPanel(tagsProp);
                stringPanel = new PropertyPanel(stringProp);
                
                tagsPanel.setBackground(Color.GREEN);
                
                filePanel.setBackground(Color.YELLOW);
                
                stringPanel.setBackground(Color.PINK);

                customPanel.setPreferences(PropertyPanel.PREF_CUSTOM_EDITOR);
                
                filePanel.setPreferences(PropertyPanel.PREF_CUSTOM_EDITOR);
                
                jp.add(customPanel);
                
                jp.add(filePanel);
                
                jp.add (focusButton);
                
                jp.add (stringPanel);
                
                jp.add (tagsPanel);
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
    
    public void disabled_testReadOnlyMode() throws Exception {
        requestFocus (focusButton);
        changeProperty (stringPanel, stringProp);
        setPreferences(stringPanel, PropertyPanel.PREF_READ_ONLY | PropertyPanel.PREF_INPUT_STATE);
        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        requestFocus(stringPanel);
        Component owner2 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        assertSame("Requesting focus on a read only inline editor panel should not change the focus owner, but it was " + owner2, owner, owner2);

        requestFocus(stringPanel);
        setPreferences(filePanel, PropertyPanel.PREF_READ_ONLY | PropertyPanel.PREF_CUSTOM_EDITOR);
        jf.repaint();
        Component owner3 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        assertSame("Requesting focus on a read only custom editor panel should not change the focus owner, not " + owner3, owner, owner3);
    }
    
    
    public void disabled_testEnabled() throws Exception {
        requestFocus (focusButton);
        sleep();
        changeProperty (filePanel, fileProp);
        sleep();
        setEnabled (filePanel, false);
        sleep();
        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        requestFocus(filePanel);
        Component owner2 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        assertSame("Requesting focus on a disabled inline editor panel should not change the focus owner, but it was " + owner2, owner, owner2);
        
        setPreferences(filePanel, PropertyPanel.PREF_CUSTOM_EDITOR);
        
        requestFocus(filePanel);
        Component owner3 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        assertSame("Requesting focus on a disabled custom editor panel should not change the focus owner, not " + owner3, owner, owner3);

        setPreferences(filePanel, PropertyPanel.PREF_INPUT_STATE);
    
        Component owner4 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        assertSame("Changing preferences on a property panel should not enable it to be focused if it is disabled" + owner4, owner, owner4);
        
        setEnabled(filePanel, true);
        
        requestFocus(filePanel);
        
        Component owner5 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        assertNotSame("Setting enabled to true should restore focusability", owner5, owner);
        assertTrue("Setting enabled to true and requesting focus should set focus to a child of the panel", filePanel.isAncestorOf(owner5));
        
        requestFocus(focusButton);
        owner5 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        //XXX better to do this for JFileChooser, but it has bugs - it *is* still focusable
        
        changeProperty(filePanel, stringProp);
        setEnabled(filePanel, false);
        requestFocus(focusButton);
        owner5 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        setPreferences(filePanel, PropertyPanel.PREF_CUSTOM_EDITOR);
        requestFocus(focusButton);
        jp.repaint();
        sleep();
        
        requestFocus(filePanel);
        Component owner6 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        assertTrue("Setting focus on a disabled custom editor component should neither set focus to it nor its children, but focus is on " + owner6,
            owner6 != filePanel && !filePanel.isAncestorOf(owner6));
    }
     

    public void disabled_testChangingPreferences() throws Exception {
        Dimension d = filePanel.getPreferredSize();
        requestFocus(filePanel);
        sleep();
        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        assertTrue ("After requesting focus on an enabled property panel containing a file custom editor, focus should be on some child of the property panel, but it is " + owner,
            filePanel.isAncestorOf(owner));
        
        sleep();
        
        setPreferences(filePanel, PropertyPanel.PREF_INPUT_STATE);
        jf.validate();
        jf.repaint();
        Dimension d2 = filePanel.getPreferredSize();
        assertTrue ("Panel returned same preferred size in inline and custom editor mode.  Probably the inner component wasn't changed when the preferences were changed.",
            !d.equals(d2));
        sleep();
        
        Component newOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        assertTrue ("After changing preferences from custom editor to non-custom editor in a displayed property panel, focus owner should still be in panel, but it is " + newOwner,
            filePanel.isAncestorOf(newOwner));
    }
    
    
    public void disabled_testPropertyPanelSendsFocusLost() throws Exception {
        //This test is in referenced to a filed issue that the old property
        //panel did not send focus lost events properly
        
        requestFocus(focusButton);
        FL fliFile = new FL();
        FL fliBasic = new FL();
        filePanel.addFocusListener(fliFile);
        customPanel.addFocusListener(fliBasic);
        
        requestFocus(filePanel);
        fliFile.assertGained("Setting focus to a custom editor did not generate a focus gained event");
        
        requestFocus(customPanel);
        fliFile.assertLost("Setting focus from one custom editor to another did not generate a focus lost event from the first one");
        
        fliBasic.assertGained("Setting focus to a custom editor from another did not generate a focus gained event from the second one");

        requestFocus(focusButton);
        fliBasic.assertLost("Setting focus to a non property panel component did not generate a focus lost event");
        
        setPreferences (filePanel, PropertyPanel.PREF_INPUT_STATE);
        
        requestFocus(filePanel);
        fliFile.assertGained("Setting focus to a property panel displaying an inline editor from a non-property panel component did not generate a focus gained event");
        
        requestFocus(focusButton);
        fliFile.assertLost("Setting focus away from a property panel displaying an inline editor to a non-property panel component did not generate a focus lost event");
    }
    
    public void disabled_testTableUIBehavior () throws Exception {
        requestFocus (tagsPanel);
        
        System.err.println("TAGS PANEL BOUNDS: " + tagsPanel.getBounds());
        
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        assertTrue ("After requesting focus on a property with tags, a combo box should have focus, not " + focusOwner,
            focusOwner instanceof JComboBox);
        
        assertTrue("After requesting focus on a property with tags, with TableUI false, the combo's popup should not be open",
            !((JComboBox) focusOwner).isPopupVisible());
        
        requestFocus(focusButton);
        
        tagsPanel.setPreferences(PropertyPanel.PREF_TABLEUI);
        
        requestFocus(tagsPanel);
        focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        
        assertTrue ("After requesting focus on a property with tags, a combo box should have focus, not " + focusOwner,
            focusOwner instanceof JComboBox);
        
        assertTrue("After requesting focus on a property with tags, with TableUI true, the combo's popup should be open",
            ((JComboBox) focusOwner).isPopupVisible());
        
        sleep();
        requestFocus(focusButton);

        /*
        //Commenting this out for now - too heavily depends to on the look and
        //feel's behavior
        assertTrue("After shifting focus away from a tableUI combo box, its popup should no longer be open",
            !((JComboBox) focusOwner).isPopupVisible());
         */
        
    }
    
    
    public void disabled_testClientProperties() throws Exception {
        tagsPanel.putClientProperty("radioButtonMax", new Integer(100));
        sleep();
        sleep();
        requestFocus(tagsPanel);
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

        assertTrue ("After setting focus to a panel for a property editor with tags below the radio button threshold, focus should be on a radio button, but was " + focusOwner,
            focusOwner instanceof JRadioButton);
        
        requestFocus (focusButton);
        
        tagsPanel.putClientProperty("radioButtonMax", null);
        sleep();
        sleep();
        requestFocus(tagsPanel);
        Component focusOwner2 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        
        assertNotSame("Focus owner should not be same as previous if the value of radioButtonMax has changed significantly",
            focusOwner, focusOwner2);
        
        assertTrue("After resetting radioButtonMax client property and requesting focus, a radio button should not be what gets focus on requestFocus",
            (!(focusOwner2 instanceof JRadioButton)));
        
        assertTrue("After resetting radioButtonMax client property and requesting focus, a combo box should be what gets focus on requestFocus",
            focusOwner2 instanceof JComboBox);

        requestFocus (focusButton);
        
        tagsPanel.putClientProperty("radioButtonMax", new Integer(100));
        sleep();
        tagsPanel.putClientProperty("useLabels", Boolean.TRUE);
        
        sleep();
        
    }
       
    public void disabled_testReplaceProperty() throws Exception {
        Node.Property p = stringPanel.getProperty();
        requestFocus(stringPanel);
        typeKey(stringPanel, KeyEvent.VK_W);
        typeKey(stringPanel, KeyEvent.VK_H);
        typeKey(stringPanel, KeyEvent.VK_O);
        typeKey(stringPanel, KeyEvent.VK_A);

        changeProperty(stringPanel, tagsProp);
        
        Node.Property p2 = stringPanel.getProperty();
        
        assertNotSame("After replacing the property, it should not be the former one", 
            p2, p);

        setPreferences(stringPanel, PropertyPanel.PREF_CUSTOM_EDITOR);
        
        changeProperty(stringPanel, fileProp);
        
        PCL pcl = new PCL();
        stringPanel.addPropertyChangeListener(pcl);
        
        //Set the value in a way that will trigger an env property change
        //on a property the panel should no longer be listening to
        tagsProp.getPropertyEditor().setAsText("c"); //will be an invalid value
        
        pcl.assertNoEvent("Change in a property editor not related to the current property in a propertypanel whose property was changed nonetheless triggered a state changed event from the panel");
        
        filePanel.setPreferences(PropertyPanel.PREF_CUSTOM_EDITOR);
        requestFocus(filePanel);
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

        changeProperty (filePanel, stringProp);
        requestFocus(filePanel);
        
        Component focusOwner2 = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        
        assertNotSame("After changing the property in a custom editor panel to one with different property type, the component focus gets set to on a call to requestFocus should not be the same type as before",
            focusOwner.getClass(), focusOwner2.getClass());
    }

    public void disabled_testWriteValues() throws Exception {
        stringPanel.setChangeImmediate(false);
        assertTrue ("Panel contains the wrong property: " + stringPanel.getProperty(), stringPanel.getProperty() == stringProp);
        
        requestFocus(stringPanel);
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        assertTrue ("Requesting focus on a property panel for a string property should set focus to a JTextField not " + focusOwner,
            focusOwner instanceof JTextComponent);
        
        typeKey(stringPanel, KeyEvent.VK_T);
        typeKey(stringPanel, KeyEvent.VK_H);
        typeKey(stringPanel, KeyEvent.VK_E);
        typeKey(stringPanel, KeyEvent.VK_R);
        typeKey(stringPanel, KeyEvent.VK_E);
        typeKey(stringPanel, KeyEvent.VK_S);
        typeKey(stringPanel, KeyEvent.VK_SPACE);
        typeKey(stringPanel, KeyEvent.VK_A);
        
        Object o = stringPanel.getProperty().getValue();
        assertTrue ("After typing into  a property panel string editor, property value should not be what was typed",
            !o.equals("THERES A"));
        
        //pressKey(focusOwner, KeyEvent.VK_ENTER);
        stringPanel.updateValue();
        Object o2 = stringPanel.getProperty().getValue();

        assertNotSame(o,o2);
        assertTrue ("After typing into a property panel string editor and pressing enter, property value should be what was typed, but it was <" + o + ">",
            o2.equals("THERES A"));
    }
    
    
    public void disabled_testMarkingUpdates() throws Exception {
        requestFocus(tagsPanel);
        sleep();
        
        InplaceEditor outer = ((EditablePropertyDisplayer) tagsPanel.inner).getInplaceEditor();
        assertTrue ("Should be a button panel for component supporting a custom editor", outer instanceof ButtonPanel);
        assertTrue ("Button panel should contain a combo box", ((ButtonPanel) outer).getInplaceEditor() instanceof ComboInplaceEditor);
        
        sleep();
        sleep();
        pressKey(tagsPanel, KeyEvent.VK_DOWN);
        pressKey(tagsPanel, KeyEvent.VK_UP);
        pressKey(tagsPanel, KeyEvent.VK_UP);

        InplaceEditor newOuter = ((EditablePropertyDisplayer) tagsPanel.inner).getInplaceEditor();
//        assertTrue ("After setting an illegal value, the outer component should be an IconPanel to show the illegal value mark, not " + newOuter,
//            newOuter instanceof IconPanel);
        
        
        pressKey(tagsPanel, KeyEvent.VK_UP);

        sleep();
        sleep();
        newOuter = ((EditablePropertyDisplayer) tagsPanel.inner).getInplaceEditor();
        sleep();
        assertTrue ("After setting a legal value, the outer component should be an IconPanel to show the illegal value mark, but it is " + newOuter,
            newOuter instanceof ButtonPanel);
    }
    
    public void disabled_testSuppressEditorButton () throws Exception {
        requestFocus (tagsPanel);
        InplaceEditor outer = ((EditablePropertyDisplayer) tagsPanel.inner).getInplaceEditor();
        assertTrue ("Should be a button panel for component supporting a custom editor", outer instanceof ButtonPanel);
        
        tagsPanel.putClientProperty("suppressCustomEditor", Boolean.TRUE);
        sleep();
        sleep();
        sleep();
        
        outer = ((EditablePropertyDisplayer) tagsPanel.inner).getInplaceEditor();
        
        assertTrue ("Should be no button panel when suppressing custom editor", !(outer instanceof ButtonPanel));
    }
    
    
    private class PCL implements PropertyChangeListener {
        private PropertyChangeEvent event;
        
        public void assertNoEvent (String msg) {
            assertNull(msg, event);
        }
        
        public void assertEvent (String msg) {
            PropertyChangeEvent pce = event;
            event = null;
            assertNotNull(msg, event);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            
        }
        
    }
    
    
    private void setPreferences (final PropertyPanel pp, final int preferences) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                throwMe = null;
                try {
                    pp.setPreferences(preferences);
                } catch (Exception e) {
                    throwMe = e;
                }
            }
        });
        if (throwMe != null) {
            Exception e = throwMe;
            throwMe = null;
            throw e;
        } else {
            sleep();
        }
    }
    
    
    private class FL implements FocusListener {
        private FocusEvent gainedEvent=null;
        private FocusEvent lostEvent=null;
        private int gainedCount=0;
        private int lostCount=0;
        public void assertGained(String msg) {
            int currGainedCount = gainedCount;
            gainedCount = 0;
            FocusEvent gained = gainedEvent;
            gainedEvent = null;
            assertNotNull (msg, gained);
            assertTrue("Received wrong number of focus gained events for a single click on a renderer " +  currGainedCount, currGainedCount == 1);
        }
        
        public void assertLost(String msg) {
            int currLostCount = lostCount;
            lostCount = 0;
            FocusEvent lost = lostEvent;
            lostEvent = null;
            assertNotNull (msg, lost);
            assertTrue("Received wrong number of focus lost events for a single click away from a focused renderer" + currLostCount, currLostCount == 1);
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
            f.toFront();
            f.requestFocus();
            if (!shown) {
                synchronized(this) {
                    try {
                        //System.err.println("Waiting for window");
                            wait(6000);
                    } catch (Exception e) {}
                }
            }
            int ct = 0;
            while (!f.isShowing()) {
                ct++;
                try {
                    Thread.currentThread().sleep(400);
                } catch (Exception e) {
                    
                }
                if (ct > 100) {
                    break;
                }
            }
            ct=0;
            Container c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            while (c != f) {
                try {
                    Thread.currentThread().sleep(400);
                } catch (Exception e) {
                    
                }
                c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                ct++;
                if (ct > 100) {
                    break;
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
    
    private void changeProperty (final PropertyPanel ren, final Node.Property newProp) throws Exception {
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
    
    private void setEnabled(final PropertyPanel ren,final boolean val) throws Exception {
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                ren.setEnabled(val);
            }
        });
        sleep();
    }
    
    private Exception throwMe = null;
    private String flushResult = null;
    private String flushValue(final PropertyPanel ren) throws Exception {
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

    // Property definition
    public class TagsProperty extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public TagsProperty(String name, boolean isWriteable) {
            super(name, Object.class, name, "", true, isWriteable);
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
        // get the property editor
        PropertyEditor editor = null;
        public PropertyEditor getPropertyEditor() {
            if (editor == null) {
                editor = new TagsEditor();
            }
            return editor;
        }
    }
    
    // Property definition
    public class StringProperty extends PropertySupport {
        private Object myValue = "way up high";
        // Create new Property
        public StringProperty(String name, boolean isWriteable) {
            super(name, String.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            System.err.println("SETVALUE ON STRINGPROPERTY: " + value);
            Object oldVal = myValue;
            myValue = value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
    }    
    
    
    public class TagsEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public TagsEditor() {
        }

        @Override
        public String[] getTags() {
            return new String[] {"a","b","c","d","Value"};
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            if ("c".equals(getValue())) {
                env.setState(env.STATE_INVALID);
            } else {
                env.setState(env.STATE_VALID);
            }
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            return new JColorChooser();
        }

        @Override
        public void setValue(Object newValue) {
            super.setValue(newValue);
            if (env != null) {
                if ("c".equals(newValue)) {
                    env.setState(env.STATE_INVALID);
                } else {
                    env.setState(env.STATE_NEEDS_VALIDATION);
                }
            }
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
