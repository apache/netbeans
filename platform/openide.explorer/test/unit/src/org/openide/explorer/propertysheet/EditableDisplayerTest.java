/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/* A comprehensive test of EditablePropertyDisplayer */
public class EditableDisplayerTest extends NbTestCase {
    
    static {
        ComboTest.registerPropertyEditors();
    }
    
    public EditableDisplayerTest(String name) {
        super(name);
    }
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    
    EditablePropertyDisplayer basicRen;
    EditablePropertyDisplayer tagsRen1;
    EditablePropertyDisplayer tagsRen2;
    EditablePropertyDisplayer tagsRen3;
    EditablePropertyDisplayer boolRen;
    EditablePropertyDisplayer custRen;
    EditablePropertyDisplayer custRen2;
    EditablePropertyDisplayer exRen;
    EditablePropertyDisplayer numRen;
    EditablePropertyDisplayer edRen;
    EditablePropertyDisplayer stringRen;
    
    private TNode tn;
    private BasicProperty basicProp;
    private TagsProperty tags1;
    private TagsProperty tags2;
    private TagsProperty tags3;
    private BooleanProperty booleanProp;
    private CustomProperty customProp;
    private CustomProperty customProp2;
    private BasicEditor te;
    private StringProperty stringProp;
    private JFrame jf=null;
    private JPanel jp=null;
    private int SLEEP_LENGTH=120;
    
    private static boolean setup=false;
    
    protected void tearDown() {
        /*        jf.hide();
        jf.dispose();
        Frame[] frms = Frame.getFrames();
        for (int i=0; i < frms.length; i++) {
            frms[i].hide();
            frms[i].dispose();
        }
         */
    }
    
    static final boolean canRun = ExtTestCase.canSafelyRunFocusTests() && GraphicsTestCase.canSafelyRunPixelTests();
    protected void setUp() throws Exception {
        
        //            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
        //            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.gtk.GTKLookAndFeel());
        PropUtils.forceRadioButtons=false;
        try {
            //            if (setup) return;
            basicProp= new BasicProperty("basicProp", true);
            System.err.println("Created basicProp at " + System.currentTimeMillis() + " - " + basicProp);
            
            tags1 = new TagsProperty("tags1", true, new String[] {"What","is","the","meaning","of","life"});
            tags2 = new TagsProperty("tags2", true, new String[] {"Austrolopithecines","automatically","engender","every","one"});
            tags3 = new TagsProperty("tags3", true, new String[] {"Behold","the","power","of","cheese"});
            booleanProp = new BooleanProperty("booleanProp", true);
            customProp = new CustomProperty("CustomProp", true);
            customProp2 = new CustomProperty("CustomProp2", true);
            ExceptionProperty exProp = new ExceptionProperty("Exception prop", true);
            NumProperty numProp = new NumProperty("Int prop", true);
            EditableNumProperty edProp = new EditableNumProperty("Editable", true);
            stringProp = new StringProperty("stringProp",true);
            
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
            jf.setLocation(20,20);
            jf.setSize(600, 200);
            
            synchronized (jp.getTreeLock()) {
                System.err.println("BasicProp = " + basicProp);
                
                basicRen = new EditablePropertyDisplayer(basicProp);
                tagsRen1 = new EditablePropertyDisplayer(tags1);
                tagsRen2 = new EditablePropertyDisplayer(tags2);
                tagsRen3 = new EditablePropertyDisplayer(tags3);
                boolRen = new EditablePropertyDisplayer(booleanProp);
                custRen = new EditablePropertyDisplayer(customProp);
                custRen2 = new EditablePropertyDisplayer(customProp2);
                exRen = new EditablePropertyDisplayer(exProp);
                numRen = new EditablePropertyDisplayer(numProp);
                edRen = new EditablePropertyDisplayer(edProp);
                stringRen = new EditablePropertyDisplayer(stringProp);
                
                tagsRen2.setRadioButtonMax(10);
                
                jp.add(basicRen);
                jp.add(tagsRen1);
                jp.add(tagsRen2);
                jp.add(tagsRen3);
                jp.add(boolRen);
                jp.add(custRen);
                jp.add(custRen2);
                jp.add(numRen);
                jp.add(edRen);
                jp.add(stringRen);
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
    
    public void testRadioButtonThreshold() throws Exception {
        if (!canRun) return;
        
        System.err.println("running");
        clickOn(basicRen);
        tagsRen2.setRadioButtonMax(2);
        
        clickOn(tagsRen2);
        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        assertTrue("after setting radio max below threshold, click on the renderer should focus a combo box, not " + c, c instanceof JComboBox);
        
        clickOn(basicRen);
        tagsRen2.setRadioButtonMax(10);
        sleep();
        
        clickOn(tagsRen2, 80, 25);
        tagsRen2.requestFocus();
        sleep();
        
        c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        assertTrue("after setting radio button max > threshold, focus owner should be a radio button, not " + c, c instanceof JRadioButton);
    }
    
    public void testBooleanEditor() throws Exception {
        if (!canRun) return;
        
        boolRen.setUpdatePolicy(boolRen.UPDATE_ON_CONFIRMATION);
        requestFocus(boolRen);
        sleep();
        sleep();
        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        assertTrue("After requesting focus on a boolean property, focus owner should be a checkbox, not " + c, c instanceof JCheckBox);
        
        Boolean b = (Boolean) booleanProp.getValue();
        pressKey(boolRen, KeyEvent.VK_SPACE);
        releaseKey(boolRen, KeyEvent.VK_SPACE);
        Boolean b2 = (Boolean) booleanProp.getValue();
        assertNotSame("Clicking on a checkbox with policy UPDATE_ON_CONFIRMATION should change the property value",b, b2);
        
        boolRen.setUpdatePolicy(boolRen.UPDATE_ON_EXPLICIT_REQUEST);
        Boolean b3 = (Boolean) booleanProp.getValue();
        pressKey(boolRen, KeyEvent.VK_SPACE);
        releaseKey(boolRen, KeyEvent.VK_SPACE);
        Boolean b4 = (Boolean) booleanProp.getValue();
        assertEquals("Clicking on a checkbox with policy UPDATE_ON_EXPLICIT_REQUEST should not change the underlying property", b3, b4);
        
        Boolean b5 = (Boolean) boolRen.getEnteredValue();
        assertNotSame("Clicking on a checkbox wiith policy UDPATE_ON_EXPLICIT_REQUEST should mean that the value returned by the editor and the value returned by the property are different until commit() is called",
                b4, b5);
        
        boolean rslt = boolRen.commit();
        assertTrue("Should have been able to update boolean property", rslt);
        
        Boolean b6 = (Boolean) booleanProp.getValue();
        assertEquals("After commit, bool editor value should eqaul bool property value", b6, boolRen.getEnteredValue());
        
        pressKey(boolRen, KeyEvent.VK_SPACE);
        releaseKey(boolRen, KeyEvent.VK_SPACE);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                jf.getContentPane().remove(boolRen);
            }
        });
        sleep();
        
        boolean val = boolRen.commit();
        assertTrue("Should still be able to commit after removal from a parent", val);
        
        assertNotSame("Commit should update value even if called when no parent is present", b6, booleanProp.getValue());
    }
    
    public void testEditableCombo() throws Exception{
        if (!canRun) return;
        
        edRen.setUpdatePolicy(edRen.UPDATE_ON_CONFIRMATION);
        clickOn(edRen);
        requestFocus(edRen);
        sleep();
        sleep();
        JComponent c = (JComponent)KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        
        Object o = edRen.getProperty().getValue();
        String nuVal = "FOO";
        
        typeKey(c, KeyEvent.VK_F);
        typeKey(c, KeyEvent.VK_O);
        typeKey(c, KeyEvent.VK_O);
        assertEquals("After typing into editable combo, value should match the typed value", nuVal, edRen.getEnteredValue());
        
        assertNotSame("After typing into editable combo with policy UPDATE_ON_CONFIRMATION, the property should not have the value typed",
                nuVal, edRen.getProperty().getValue());
        
        pressKey(c, KeyEvent.VK_ENTER);
        
        assertEquals("After pressing enter on an editable combo, value should be updated", nuVal, edRen.getProperty().getValue());
    }
    
    public void testEnv() throws Exception {
        if (!canRun) return;
        
        try {
            custRen.setUpdatePolicy(custRen.UPDATE_ON_EXPLICIT_REQUEST);
            
            requestFocus(custRen);
            JComponent c = (JComponent)KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            typeKey(c, KeyEvent.VK_W);
            typeKey(c, KeyEvent.VK_O);
            typeKey(c, KeyEvent.VK_W);
            pressKey(c, KeyEvent.VK_ENTER);
            
            EditorCustom ec = (EditorCustom) custRen.getPropertyEditor();
            PropertyEnv env = ec.env;
            assertSame(" The PropertyEnv the editor is attached to should be the same as the one the component posesses",
                    custRen.getPropertyEnv(), env);
            
            assertTrue(" After pressing enter with a new value with policy UPDATE_ON_EXPLICIT_REQUEST, the property value should not be the typed value",
                    !custRen.getProperty().getValue().equals("WOW"));
            
            assertTrue(" After pressing enter with a new value with policy UPDATE_ON_EXPLICIT_REQUEST, isValueModified() should return true",
                    custRen.isValueModified());
            
            env = ec.env;
            assertSame(" Calling isValueModified attached a different PropertyEnv to the editor and didn't call attachEnv again with the one it listens on for changes",
                    custRen.getPropertyEnv(), env);
            
            
            String legality = custRen.isModifiedValueLegal();
            assertNull("After pressing enter with a new value with policy UPDATE_ON_EXPLICIT_REQUEST with a valid value, isModifiedValueLegal should return null, not " + legality,
                    legality);
            
            env = ec.env;
            
            env = ec.env;
            assertSame(" Calling isModifiedValueLegal attached a different PropertyEnv to the editor and didn't call attachEnv again with the one it listens on for changes",
                    custRen.getPropertyEnv(), env);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        
        requestFocus(basicRen);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                custRen.setEnteredValue("");
            }
        });
        sleep();
        sleep();
        sleep();
        
        requestFocus(custRen);
        
        typeKey(custRen, KeyEvent.VK_V);
        typeKey(custRen, KeyEvent.VK_A);
        typeKey(custRen, KeyEvent.VK_L);
        typeKey(custRen, KeyEvent.VK_U);
        typeKey(custRen, KeyEvent.VK_E);
        
        pressKey(custRen, KeyEvent.VK_ENTER);
        
        assertEquals("After entering a value, getEnteredValue should return it", "VALUE", custRen.getEnteredValue());
        
        
        String legality = custRen.isModifiedValueLegal();
        assertNotNull("After entering a value that will put the env in STATE_INVALID, a localized message should be returned by isModifiedValueLegal", legality);
        
    }
    
    
    public void testPropertyMarking() throws Exception{
        if (!canRun) return;
        
        if (!checkGraphicsEnvironment()) {
            System.err.println("  Cannot run this test in a < 16 bit graphics environment");
        }
        custRen.setUpdatePolicy(custRen.UPDATE_ON_CONFIRMATION);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    throwMe = null;
                    custRen.getProperty().setValue("Value");
                    custRen.refresh();
                } catch (Exception e) {
                    throwMe = e;
                }
            }
        });
        
        if (throwMe != null) {
            Exception exc = throwMe;
            throwMe = null;
            throw exc;
        }
        
        
        requestFocus(custRen);
        
        typeKey(custRen, KeyEvent.VK_S);
        typeKey(custRen, KeyEvent.VK_N);
        typeKey(custRen, KeyEvent.VK_O);
        typeKey(custRen, KeyEvent.VK_R);
        typeKey(custRen, KeyEvent.VK_K);
        typeKey(custRen, KeyEvent.VK_E);
        typeKey(custRen, KeyEvent.VK_L);
        
        //The property marking image
        Image i = ImageUtilities.loadImage("org/openide/resources/propertysheet/invalid.gif");
        ImageIcon icon = new ImageIcon(i);
        int yOffset = (custRen.getHeight() / 2) - (icon.getIconHeight()/2);
        
        //        assertPixelFromImage(i, custRen, 7, 7, 7, yOffset + 7);
        assertImageMatch("Error icon should be painted for invalid value", i, custRen, 0, yOffset);
        
        requestFocus(custRen);
        
        //        SLEEP_LENGTH=1000;
        sleep();
        typeKey(custRen, KeyEvent.VK_M);
        typeKey(custRen, KeyEvent.VK_R);
        typeKey(custRen, KeyEvent.VK_F);
        typeKey(custRen, KeyEvent.VK_ENTER);
        pressKey(custRen, KeyEvent.VK_ENTER);
        pressKey(custRen, KeyEvent.VK_ENTER);
        custRen.commit();
        sleep();
        sleep();
        
        Icon icon2 = new ValueIcon();
        int yOffset2 = (custRen.getHeight() / 2) - (icon2.getIconHeight()/2);
        
        assertPixel("Supplied value icon should be drawn on panel, not the error marking icon, after committing a valid value.",
                custRen, Color.BLUE, icon2.getIconWidth() / 2, (icon2.getIconHeight() / 2) + yOffset2);
        
        requestFocus(custRen);
        
        typeKey(custRen, KeyEvent.VK_V);
        typeKey(custRen, KeyEvent.VK_A);
        typeKey(custRen, KeyEvent.VK_L);
        typeKey(custRen, KeyEvent.VK_U);
        typeKey(custRen, KeyEvent.VK_E);
        custRen.setEnteredValue("VALUE");
        pressKey(custRen, KeyEvent.VK_ENTER);
        custRen.commit();
        sleep();
        sleep();
        sleep();
        custRen.paintImmediately(0,0,custRen.getWidth(),custRen.getHeight());
        assertImageMatch("After reentering an invalid value, the icon should change back to the error icon", i, custRen, 0, yOffset);
    }
    
    
    
    public void testControlSpaceInvokesCustomEditor() throws Exception {
        if (!canRun) return;
        
        requestFocus(custRen2);
        final Component focusOwner =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        assertTrue("Requesting focus on an enabled renderer should set focus to it or its child", custRen2 == focusOwner || custRen2.isAncestorOf(focusOwner));
        
        System.err.println("CONTROL PRESS KEY");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    throwMe = null;
                    ctrlPressKey(focusOwner, KeyEvent.VK_SPACE);
                } catch (Exception e) {
                    throwMe = e;
                }
            }
        });
        if (throwMe != null) {
            Exception e1 = throwMe;
            throwMe = null;
            throw throwMe;
        }
        
        sleep();
        sleep();
        sleep();
        sleep();
        sleep();
        sleep();
        
        Component newOwner =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        
        assertNotSame("Pressing ctrl-space should move focus to a dialog", focusOwner, newOwner);
        
        pressKey(newOwner, KeyEvent.VK_ESCAPE);
        sleep();
        
        Component owner =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        
        if (owner == null) {
            sleep();
            sleep();
            owner =
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        }
        
        assertNotSame("Pressing escape on custom editor dialog should dismiss it", owner, newOwner);
        assertSame("Focus should return to the editor if lost from a custom editor dialog", focusOwner, owner);
        
        if (newOwner != null) {
            Container c = ((JComponent) newOwner).getTopLevelAncestor();
            if (c != null) {
                c.hide();
            }
        }
    }
    
    public void testRenderersPaintIndentically() throws Exception {
        if (!canRun) return;
        if (!checkGraphicsEnvironment()) {
            System.err.println("Cannot run test in < 16 bit graphics environment");
        }
        Component[] c = jp.getComponents();
        Hashtable map = new Hashtable();
        synchronized (jp.getTreeLock()) {
            for (int i=0; i < c.length; i++) {
                System.err.println("  Checking " + c[i]);
                if (c[i] instanceof EditablePropertyDisplayer) {
                    System.err.println(" CREATE A RENDERER AND ADD IT");
                    EditablePropertyDisplayer curr = (EditablePropertyDisplayer) c[i];
                    try {
                        curr.commit();
                    } catch (Exception e) {
                        curr.reset();
                    }
                    
                    RendererPropertyDisplayer rpd = new RendererPropertyDisplayer(curr.getProperty());
                    rpd.setRadioButtonMax(curr.getRadioButtonMax());
                    rpd.setProperty(curr.getProperty());
                    map.put(curr, rpd);
                    jp.add(rpd);
                }
            }
        }
        
        jp.repaint();
        
        
        Iterator i = map.keySet().iterator();
        while (i.hasNext()) {
            EditablePropertyDisplayer editable = (EditablePropertyDisplayer) i.next();
            RendererPropertyDisplayer renderer = (RendererPropertyDisplayer) map.get(editable);
            assertPaintIdentically("Painting was not a pixel-for-pixel match between " + editable + " and " + renderer, editable, renderer);
            //            assertEquals("Preferred size of a renderer and an editor should match.  They do not for " + editable + " and " + renderer, getPreferredSize(editable), getPreferredSize(renderer));
        }
        
    }
    
    
    public void testCustomEditor() throws Exception {
        if (!canRun) return;
        
        requestFocus(stringRen);
        Runnable run = new Runnable() {
            public void run() {
                try {
                    doCtrlPressKey(stringRen, KeyEvent.VK_SPACE);
                } catch (Exception e) {}
            }
        };
        new Thread(run).start();
        
        Thread.currentThread().sleep(1000);
        
        sleep();
        Component owner =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        
        assertNotNull("After invoking custom editor, focus owner should not be null", owner);
        
        assertTrue("Control press should invoke custom editor", ((JComponent) owner).getTopLevelAncestor() !=
                stringRen.getTopLevelAncestor());
        
        assertTrue("String custom editor should be a JTextComponent", owner instanceof JTextComponent);
        
        final JTextComponent jtc = (JTextComponent) owner;
        
        jtc.setText("Wuggle buggle");
        
        sleep();
        sleep();
        
        doCtrlPressKey(owner, KeyEvent.VK_TAB);
        
        sleep();
        sleep();
        
        Component okbutton =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        
        assertTrue("Button after editor in string renderer should be ok button", okbutton instanceof JButton);
        
        ((JButton) okbutton).doClick();
        sleep();
        sleep();
        sleep();
        
        assertEquals("After clicking ok on custom editor, property should be updated", "Wuggle buggle", stringRen.getProperty().getValue());
        sleep();
        assertEquals("After clicking ok button, inline editor should have the custom editor value", stringRen.getProperty().getValue(), stringRen.getEnteredValue());
        
    }
    
    public void testCustomEditorTitle() throws Exception {
        if (!canRun) return;
        
        requestFocus(custRen);
        Runnable run = new Runnable() {
            public void run() {
                try {
                    doCtrlPressKey(stringRen, KeyEvent.VK_SPACE);
                } catch (Exception e) {}
            }
        };
        new Thread(run).start();
        
        Thread.currentThread().sleep(1000);
        
        sleep();
        Component owner =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        
        assertTrue("Control press should invoke custom editor", ((JComponent) owner).getTopLevelAncestor() !=
                stringRen.getTopLevelAncestor());
        
        Container c = ((JComponent) owner).getTopLevelAncestor();
        System.err.println("CLASS: " + c.getClass());
        
        if (c instanceof JDialog) {
            assertEquals("Custom editor supplying a title via client properties should be shown in a dialog with that title",
                    ((JDialog) c).getTitle(), "Don't panic");
        }
    }
    
    private Dimension dim=null;
    /** Fetches a preferred size on the event thread.  This will actually cause
     * intermittent failures otherwise, becuase the layout can be asekd for
     * preferred size while components are still being added */
    private Dimension getPreferredSize(final JComponent jc) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                dim = null;
                throwMe = null;
                try {
                    dim = jc.getPreferredSize();
                } catch (Exception e) {
                    throwMe = e;
                }
            }
        });
        Dimension result = dim;
        dim = null;
        if (throwMe != null) {
            Exception exc = throwMe;
            throwMe = null;
            throw throwMe;
        }
        return result;
    }
    
    private void assertPaintIdentically(String msg, JComponent a, JComponent b) throws Exception {
        
        if (true) return; //Don't enable these tests by default
        
        //do this so focus rectangle won't produce a false non-match
        KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        
        final Dimension d = getPreferredSize(a);
        
        if (d.width < 0 || d.height < 0) {
            StringBuffer sb = new StringBuffer();
            Container con = a;
            while (con != null && con.getComponentCount() != 0) {
                sb.append(con.getClass().getName() + " - preferred size: " + con.getPreferredSize() + "\n");
                if (con.getComponent(0) instanceof Container) {
                    con = (Container) con.getComponent(0);
                } else {
                    con = null;
                }
            }
            fail("Got a negative preferred size: " + d + " from tree " + sb.toString());
        }
        
        final BufferedImage bia = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        final BufferedImage bib = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        System.err.println("Created an image of size " + d);
        
        Graphics2D ga = (Graphics2D) bia.getGraphics();
        
        a.setBounds(0, 0, d.width, d.height);
        if (a.getLayout() != null) {
            a.getLayout().layoutContainer(a);
        }
        
        sleep();
        sleep();
        try {
            a.paint(ga);
        } catch (Exception e) {
            SwingUtilities.paintComponent(ga, a, jp, 0, 0, d.width, d.height);
        }
        
        Graphics2D gb = (Graphics2D) bib.getGraphics();
        b.setBounds(0,0,d.width,d.height);
        if (b.getLayout() != null) {
            b.getLayout().layoutContainer(a);
        }
        sleep();
        sleep();
        try {
            b.paint(gb);
        } catch (Exception e) {
            SwingUtilities.paintComponent(gb, b, jp, 0, 0, d.width, d.height);
        }
        
        final BufferedImage diff = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        
        boolean match = true;
        
        for (int x=0; x < d.width; x++) {
            for (int y=0; y < d.height; y++) {
                int pixa = bia.getRGB(x, y);
                int pixb = bib.getRGB(x, y);
                boolean matches = pixa == pixb;
                if (!matches) {
                    System.err.println("Non match: " + x + "," + y);
                    diff.setRGB(x, y, 0);
                } else {
                    diff.setRGB(x, y, 1239847103);
                }
                match &= matches;
            }
        }
        
        final String classa = a.getClass().getName();
        final String classb = b.getClass().getName();
        
        
        if (!match) {
            JFrame jf = new JFrame("assertPaintIdentically diff") {
                @Override
                public void paint(Graphics g) {
                    new ImageIcon(diff).paintIcon(this, g, 25, 25);
                    new ImageIcon(bia).paintIcon(this, g, 25, d.height+25);
                    new ImageIcon(bib).paintIcon(this, g, 25, d.height+d.height+25);
                    g.setColor(Color.BLUE);
                    g.drawString(classa, d.width + 10, 57);
                    g.drawString(classb, d.width + 10, 82);
                }
            };
            jf.setLocation(500, 20);
            jf.setSize(d.width + 20, (d.height*3)+20);
            new WaitWindow(jf);
            //            fail (msg);
        }
    }
    
    static boolean checkGraphicsEnvironment() {
        if (GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadless()) {
            System.err.println("Cannot run test in a headless environment");
        }
        DisplayMode dm =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        int i = dm.getBitDepth();
        if (i == dm.BIT_DEPTH_MULTI || i >= 16) {
            return true;
        }
        return false;
    }
    
    /** Samples a trivial number of pixels from an image and compares them with
     *pixels from a component to determine if the image is painted on the
     * component at the exact position specified */
    private void assertImageMatch(String msg, Image i, JComponent comp, int xpos, int ypos) throws Exception {
        ImageIcon ic = new ImageIcon(i);
        int width = ic.getIconWidth();
        int height = ic.getIconHeight();
        
        for (int x=2; x < 5; x++) {
            for (int y=2; y < 5; y++) {
                int posX = width / x;
                int posY = height / y;
                System.err.println("  Check " + posX + "," + posY);
                assertPixelFromImage(msg, i, comp, posX, posY, xpos + posX, ypos + posY);
            }
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
    
    private class FL implements FocusListener {
        private FocusEvent gainedEvent=null;
        private FocusEvent lostEvent=null;
        private int gainedCount=0;
        private int lostCount=0;
        public void assertGained() {
            assertNotNull("No focus gained received after clicking on an editable renderer", gainedEvent);
            assertTrue("Received wrong number of focus gained events for a single click on a renderer " +  gainedCount, gainedCount == 1);
        }
        
        public void assertLost() {
            assertNotNull("No focus lost event received after clicking away from a focused, editable renderer", lostEvent);
            assertTrue("Received wrong number of focus lost events for a single click away from a focused renderer" + lostCount, lostCount == 1);
        }
        
        public void focusGained(FocusEvent e) {
            gainedEvent = e;
            gainedCount++;
        }
        
        public void focusLost(FocusEvent e) {
            lostEvent = e;
            lostCount++;
        }
    }
    
    private class CL implements ChangeListener {
        
        private ChangeEvent e;
        public void assertEvent(String msg) {
            sleep(); //give the event time to happen
            assertNotNull(msg, e);
            e = null;
        }
        
        public void assertNoEvent(String msg) {
            sleep();
            assertNull(e);
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
        public WaitWindow(JFrame f) {
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
    
    private static Color checkColor=null;
    private static int count=0;
    /** Asserts that a pixel at a given position in an image matches a
     * pixel in a given position in a component */
    private synchronized void assertPixelFromImage(String msg, final Image i, final Component c, final int imageX, final int imageY, final int compX, final int compY) throws Exception {
        final BufferedImage bi = i instanceof BufferedImage ? (BufferedImage) i : toBufferedImage(i);
        throwMe = null;
        sleep();
        
        int rgb = bi.getRGB(imageX, imageY);
        Color color = new Color(rgb);
        
        
        //uncomment the code below for diagnosing painting problems
        //and seeing which pixel you'return really checking
        JFrame jf = new JFrame("assertPixelFromImage " + count + " (look for the yellow line)") {
            @Override
            public void paint(Graphics g) {
                new ImageIcon(bi).paintIcon(this, g, 25, 25);
                g.setColor(Color.YELLOW);
                g.drawLine(imageX+20, imageY+25, imageX+25, imageY+25);
            }
        };
        jf.setLocation(500,500);
        jf.setSize(100,100);
        jf.show();
        
        try {
            assertPixel(msg, c, color, compX, compY);
        } catch (Exception e) {
            throwMe = e;
        }
        if (throwMe != null) {
            throw throwMe;
        }
    }
    
    private Exception throwMe2=null;
    /** Asert that a pixel at a specified position on a component is the
     *  specified color  */
    private synchronized void assertPixel(final String msg, final Component c, final Color toMatch, final int x, final int y) throws Exception {
        sleep();
        throwMe2 = null;
        if (true) {
            doAssertPixel(msg, c, toMatch, x, y);
            return;
        }
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    doAssertPixel(msg, c, toMatch, x, y);
                } catch (Exception e) {
                    throwMe2 = e;
                }
            }
        });
        if (throwMe2 != null) {
            throw throwMe2;
        }
    }
    
    /** Implementation of assertPixel sans invokeAndWait  */
    private synchronized void doAssertPixel(final String msg, final Component c, final Color toMatch, final int x, final int y) throws Exception {
        final BufferedImage bi = new BufferedImage(700, 700, BufferedImage.TYPE_INT_RGB);
        
        sleep();
        ((JComponent) c).paintAll(bi.getGraphics());
        sleep();
        int[] cArr = new int[3];
        bi.getData().getPixel(x, y, cArr);
        checkColor = new Color(cArr[0], cArr[1], cArr[2]);
        
        
        //uncomment the code below for diagnosing painting problems
        //and seeing which pixel you'return really checking
        JFrame jf = new JFrame("Assert pixel test " + count + " (look for the yellow line)") {
            @Override
            public void paint(Graphics g) {
                new ImageIcon(bi).paintIcon(this, g, 25, 25);
                g.setColor(Color.YELLOW);
                g.drawLine(x+20, y+25, x+25, y+25);
            }
        };
        jf.setLocation(400,400);
        jf.setSize(500,500);
        jf.show();
        count++;
        
        assertEquals("Pixel test " + (count-1) + " " + msg + " - Color at " + x + "," + y + " does not match", toMatch, checkColor);
    }
    
    
    
    private void changeProperty(final PropertyDisplayer_Mutable ren, final Node.Property newProp) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ren.setProperty(newProp);
            }
        });
    }
    
    private void clickOn(final EditablePropertyDisplayer ren, final int fromRight, final int fromTop) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Point toClick = new Point(ren.getWidth() - fromRight, fromTop);
                Component target=ren.getComponentAt(toClick);
                if (target == null) target = ren;
                toClick = SwingUtilities.convertPoint(ren, toClick, target);
                System.err.println("Target component is " + target.getClass().getName() + " - " + target + " clicking at " + toClick);
                
                MouseEvent me = new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
                target.dispatchEvent(me);
                me = new MouseEvent(target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
                target.dispatchEvent(me);
                me = new MouseEvent(target, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
            }
        });
        sleep();
    }
    
    private void clickOn(final EditablePropertyDisplayer ren) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Point toClick = new Point(5,5);
                Component target=ren.getComponentAt(toClick);
                System.err.println("Clicking on " + target);
                MouseEvent me = new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
                target.dispatchEvent(me);
            }
        });
        sleep();
    }
    
    private void setEnabled(final EditablePropertyDisplayer ren,final boolean val) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ren.setEnabled(val);
            }
        });
        sleep();
    }
    
    private Exception throwMe = null;
    private String flushResult = null;
    private String flushValue(final EditablePropertyDisplayer ren) throws Exception {
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
    
    
    private void releaseKey(final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                KeyEvent ke = new KeyEvent(target, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, key, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
    private void pressKey(final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                System.err.println("   pressKey: " + KeyStroke.getKeyStroke(key, 0).getKeyChar());
                KeyEvent ke = new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
    private void shiftPressKey(final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                KeyEvent ke = new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.SHIFT_MASK, key, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
    private void ctrlPressKey(final Component target, final int key) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            KeyEvent k = new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_MASK, key, (char) key);
            target.dispatchEvent(k);
        } else {
            
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    KeyEvent ke = new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_MASK, key, (char) key);
                    target.dispatchEvent(ke);
                }
            });
            sleep();
        }
    }
    
    private void doCtrlPressKey(final Component target, final int key) throws Exception {
        
        KeyEvent k = new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_MASK, key, (char) key);
        target.dispatchEvent(k);
    }
    
    
    private void typeKey(final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                System.err.println("   typeKey: " + KeyStroke.getKeyStroke(key, 0).getKeyChar());
                KeyEvent ke = new KeyEvent(target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
    //Node definition
    public class TNode extends AbstractNode {
        //create Node
        public TNode() {
            super(Children.LEAF);
            setName("TNode"); // or, super.setName if needed
            setDisplayName("TNode");
            createSheet();
        }
        //clone existing Node
        public Node cloneNode() {
            return new TNode();
        }
        
        public void addProp(Node.Property p) {
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
            props.put(tags1);
            props.put(tags2);
            props.put(tags3);
            props.put(booleanProp);
            props.put(customProp);
            
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
            Object oldVal = myValue;
            myValue = value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        // get the property editor
        public PropertyEditor getPropertyEditor() {
            return te;
        }
    }
    
    // Editor definition
    public class BasicEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        // Create new BasicEditor
        public BasicEditor() {
        }
        
        /*
         * This method is called by the IDE to pass
         * the environment to the property editor.
         */
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        // Set that this Editor doesn't support custom Editor
        @Override
        public boolean supportsCustomEditor() {
            return false;
        }
        
        // Set the Property value threw the Editor
        @Override
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }

        @Override
        public String getAsText() {
            return getValue() == null ? "null" : getValue().toString();
        }
    }
    
    
    public class TagsEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        String[] tags;
        public TagsEditor(String[] tags) {
            this.tags = tags;
        }

        @Override
        public String[] getTags() {
            return tags;
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }

        @Override
        public boolean supportsCustomEditor() {
            return false;
        }

        @Override
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
        
        
    }
    
    // Property definition
    public class TagsProperty extends PropertySupport {
        private Object myValue = "Value";
        private String[] tags;
        // Create new Property
        public TagsProperty(String name, boolean isWriteable, String[] tags) {
            super(name, Object.class, name, "", true, isWriteable);
            this.tags = tags;
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
        public PropertyEditor getPropertyEditor() {
            return new TagsEditor(tags);
        }
        
        public String getShortDescription() {
            return "I have tags!";
        }
    }
    
    // Property definition
    public class BooleanProperty extends PropertySupport {
        private Boolean myValue = Boolean.FALSE;
        // Create new Property
        public BooleanProperty(String name, boolean isWriteable) {
            super(name, Boolean.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            System.err.println("GetValue of boolean property returning " + myValue);
            if (myValue == null) {
                throw new IllegalStateException();
            }
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = (Boolean) value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        public Object getValue(String key) {
            if ("valueIcon".equals(key)) {
                return new ValueIcon();
            } else {
                return super.getValue(key);
            }
        }
    }
    
    public class CustomProperty extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public CustomProperty(String name, boolean isWriteable) {
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
        
        private PropertyEditor editor=null;
        public PropertyEditor getPropertyEditor() {
            if (editor == null) {
                editor = new EditorCustom();
            }
            return editor;
        }
        
        public Object getValue(String key) {
            if ("valueIcon".equals(key)) {
                return new ValueIcon();
            } else {
                return super.getValue(key);
            }
        }
    }
    
    public class ExceptionProperty extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public ExceptionProperty(String name, boolean isWriteable) {
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
        public PropertyEditor getPropertyEditor() {
            return exed;
        }
    }
    
    private ExEditor exed = new ExEditor();
    public static class ExEditor extends PropertyEditorSupport {
        private Object myVal="Value";
        public ExEditor() {}
        public void setAsText(String val) {
            //System.err.println("SetAsText");
            if (val.equals("Value") || val.equals("VALUE")) {
                myVal = val;
            } else {
                IllegalArgumentException iae = new IllegalArgumentException("No!");
                Exceptions.attachLocalizedMessage(iae, "Localized message");
                throw iae;
            }
        }

        @Override
        public void setValue(Object newValue) {
            myVal = newValue;
            firePropertyChange();
        }

        @Override
        public Object getValue() {
            return "Value";
        }
    }
    
    
    // Editor definition
    public class EditorCustom extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        // Create new BasicEditor
        public EditorCustom() {
        }
        
        /*
         * This method is called by the IDE to pass
         * the environment to the property editor.
         */
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            if ("Value".equals(getValue()) || "VALUE".equals(getValue())) {
                env.setState(env.STATE_INVALID);
            } else {
                env.setState(env.STATE_VALID);
            }
        }
        
        // Set that this Editor doesn't support custom Editor
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        // Set the Property value threw the Editor
        @Override
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }

        @Override
        public String getAsText() {
            return getValue() == null ? "null" : getValue().toString();
        }

        @Override
        public Component getCustomEditor() {
            JLabel result = new JLabel("Everything is exactly as it should be.  Relax.");
            result.putClientProperty("title","Don't panic");
            return result;
        }

        @Override
        public void setAsText(String s) {
            super.setValue(s);
            if (!"Value".equals(s) && !"VALUE".equals(s)) {
                env.setState(env.STATE_VALID);
            } else {
                env.setState(env.STATE_INVALID);
            }
        }
    }
    
    public class NumProperty extends PropertySupport {
        private Integer myValue = new Integer(4);
        // Create new Property
        public NumProperty(String name, boolean isWriteable) {
            super(name, Integer.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            if (!(value instanceof Integer)) {
                throw new IllegalArgumentException("Not an integer - " + value);
            }
            Object oldVal = myValue;
            myValue = (Integer) value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        // get the property editor
        public PropertyEditor getPropertyEditor() {
            return new NumberedTagsEditor();
        }
    }
    
    public class EditableNumProperty extends TagsProperty {
        public EditableNumProperty(String name, boolean isWriteable) {
            super(name, isWriteable, new String[]{"boo"});
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new EditableDisplayerTest.EditableTagsEditor();
        }
    }
    
    
    // Combo must display text, not numbers
    public class NumberedTagsEditor extends PropertyEditorSupport {
        private int val=3;
        // Create new BasicEditor
        public NumberedTagsEditor() {
        }

        @Override
        public String[] getTags() {
            return new String[] {"zero","one","two","three","four","five","six","seven"};
        }
        
        
        // Set the Property value threw the Editor
        @Override
        public void setValue(Object newValue) {
            val = ((Integer) newValue).intValue();
            firePropertyChange();
        }

        @Override
        public String getAsText() {
            return getTags()[((Integer) getValue()).intValue()];
        }

        @Override
        public void setAsText(String txt) {
            String[] t = getTags();
            for (int i=0; i < t.length; i++) {
                if (txt.trim().equals(t[i])) {
                    setValue(new Integer(i));
                    return;
                }
            }
            IllegalArgumentException iae = new IllegalArgumentException(txt);
            Exceptions.attachMessage(iae, txt + " is not a valid value");
        }

        @Override
        public Object getValue() {
            return new Integer(val);
        }

        @Override
        public Component getCustomEditor() {
            return new JPanel();
        }
    }
    
    public class EditableTagsEditor extends TagsEditor implements ExPropertyEditor {
        private Object val="woof";
        public EditableTagsEditor() {
            super(new String[] {"miaou","woof","moo","quack"});
        }
        public void attachEnv(PropertyEnv env) {
            env.getFeatureDescriptor().setValue("canEditAsText", Boolean.TRUE);
        }
        @Override
        public void setAsText(String s) {
            setValue(s);
        }
        @Override
        public void setValue(Object val) {
            this.val = val;
        }
        @Override
        public Object getValue() {
            return val;
        }
        @Override
        public String getAsText() {
            return val.toString();
        }
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        @Override
        public Component getCustomEditor() {
            return new JLabel("You called?");
        }
    }
    
    private class ValueIcon implements Icon {
        
        public int getIconHeight() {
            return 12;
        }
        
        public int getIconWidth() {
            return 12;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color col = g.getColor();
            try {
                g.setColor(Color.BLUE);
                g.drawRect(x, y, getIconWidth(), getIconHeight());
                g.fillRect(x+3, y+3, getIconWidth()-5, getIconHeight()-5);
            } finally {
                g.setColor(col);
            }
        }
    }
    
    
    // Property definition
    public class StringProperty extends PropertySupport {
        private String myValue = "my oh my";
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
            myValue = value.toString();
            tn.fireMethod(getName(), oldVal, myValue);
        }
    }
    
    //Shamelessly stolen from util.IconManager
    private static final BufferedImage toBufferedImage(Image img) {
        // load the image
        new ImageIcon(img);
        BufferedImage rep = createBufferedImage(img.getWidth(null), img.getHeight(null));
        Graphics g = rep.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        img.flush();
        return rep;
    }
    
    /** Creates BufferedImage 16x16 and Transparency.BITMASK */
    private static final BufferedImage createBufferedImage(int width, int height) {
        ColorModel model = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().getColorModel(Transparency.BITMASK);
        BufferedImage buffImage = new BufferedImage(model,
                model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null);
        return buffImage;
    }
    
    
    
}
