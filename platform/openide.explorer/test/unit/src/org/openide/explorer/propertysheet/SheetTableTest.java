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

import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

// This test class tests the main functionality of the property sheet
public class SheetTableTest extends NbTestCase {
    
    static {
        String[] syspesp = PropertyEditorManager.getEditorSearchPath();
        String[] nbpesp = new String[] {
            "org.netbeans.beaninfo.editors", // NOI18N
            "org.openide.explorer.propertysheet.editors", // NOI18N
        };
        String[] allpesp = new String[syspesp.length + nbpesp.length];
        System.arraycopy(nbpesp, 0, allpesp, 0, nbpesp.length);
        System.arraycopy(syspesp, 0, allpesp, nbpesp.length, syspesp.length);
        PropertyEditorManager.setEditorSearchPath(allpesp);
    }
    
    
    public SheetTableTest(String name) {
        super(name);
    }
    
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        if (setup) return;
        PropUtils.forceRadioButtons =false;
        final JFrame jf = new JFrame();
        // Create new TestProperty
        tp = new TProperty("What a", true);
        tp1 = new TProperty2("marvelous", true);
        tp2 = new TProperty2("use of", true);
        tp3 = new TProperty2("technology!", true);
        tp4 = new TProperty3("AAAA", true);
        tp5 = new TProperty4("vvvv", true);
        // Create new TEditor
        te = new TEditor();
        // Create new TNode
        tn = new TNode();
        
        try {
            PropUtils.forceRadioButtons=false;
            final PropertySheet ps = new PropertySheet();
            //ensure no stored value in preferences:
            ps.setCurrentNode(tn);
            sleep();
            
            setSortingMode(ps, PropertySheet.UNSORTED);
            
            jf.getContentPane().add(ps);
            jf.setLocation(20,20);
            jf.setSize(300, 400);
            
            
            new WaitWindow(jf);
            
            final SheetTable tb = ps.table;
            
            SheetCellEditor editor = tb.getEditor();
            
            tb.requestFocus();
            sleep();
            
            assertTrue("Table should have focus", KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == tb);
            
            clickOn(tb, 1, 0);
            
            fd_after1_0click = tb._getSelection();
            ie_after1_0click = editor.getInplaceEditor();
            
            System.err.println("Selection after 1_0 click is " + fd_after1_0click + " - inplaceEditor=" + ie_after1_0click);
            
            clickOn(tb, 1, 1);
            
            fd_after1_1click = tb._getSelection();
            ie_after1_1click = editor.getInplaceEditor();
            
            System.err.println("Selection after 1_1 click is " + fd_after1_1click + " - inplaceEditor=" + ie_after1_1click);
            
            clickOn(tb, 1, 0);
            
            fd_after1_0click2 = tb._getSelection();
            ie_after1_0click2 = editor.getInplaceEditor();
            
            System.err.println("Selection after 1_0 click2 is " + fd_after1_0click2 + " - inplaceEditor=" + ie_after1_0click2);
            
            sleep();
            //test key events
            pressKey(tb, KeyEvent.VK_SPACE);
            sleep();
            ie_afterSpaceKeystroke = editor.getInplaceEditor();
            
            System.err.println("IE AFTER PRESSING SPACE" + ie_afterSpaceKeystroke);
            
            try {Thread.currentThread().sleep(300);}catch(Exception e){}
            Component comp = ie_afterSpaceKeystroke==null ? null : ie_afterSpaceKeystroke.getComponent();
            sleep();
            editorFocusedAfterSpacebarInitiation = (comp != null) && comp == KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
            if (comp == null) {
                fail("Failed to get editor");
            }
            
            typeKey(comp, KeyEvent.VK_H);
            typeKey(comp, KeyEvent.VK_I);
            typeKey(comp, KeyEvent.VK_SPACE);
            typeKey(comp, KeyEvent.VK_M);
            typeKey(comp, KeyEvent.VK_O);
            typeKey(comp, KeyEvent.VK_M);
            
            pressKey(comp, KeyEvent.VK_ENTER);
            
            valAfterTyping = tp.getValue();
            
            sleep();
            
            pressKey(tb, KeyEvent.VK_SPACE);
            typeKey(comp, KeyEvent.VK_B);
            typeKey(comp, KeyEvent.VK_Y);
            typeKey(comp, KeyEvent.VK_E);
            typeKey(comp, KeyEvent.VK_SPACE);
            typeKey(comp, KeyEvent.VK_M);
            typeKey(comp, KeyEvent.VK_O);
            typeKey(comp, KeyEvent.VK_M);
            sleep();
            
            pressKey(comp, KeyEvent.VK_ESCAPE);
            
            valAfterEscape = tp.getValue();
            
            //Now test auto popup of combo boxes
            clickOn(tb, 4, 1);
            sleep();
            popupConsistent &= (editor.getInplaceEditor() instanceof JComboBox) &&
                    ((JComboBox) editor.getInplaceEditor()).isPopupVisible();
            
            clickOn(tb, 3, 1);
            sleep();
            popupConsistent &= (editor.getInplaceEditor() instanceof JComboBox) &&
                    ((JComboBox) editor.getInplaceEditor()).isPopupVisible();
            
            clickOn(tb, 2, 1);
            sleep();
            popupConsistent &= (editor.getInplaceEditor() instanceof JComboBox) &&
                    ((JComboBox) editor.getInplaceEditor()).isPopupVisible();
            
            comp = editor.getInplaceEditor().getComponent();
            pressKey(comp, KeyEvent.VK_ESCAPE);
            escClosedPopup = !((JComboBox) comp).isPopupVisible();
            inplaceReferenceCleared = editor.getInplaceEditor() == null;
            
            clickOn(tb, 0,0);
            closedSetSuccessfullyWithMouse = tb.getRowCount() == 1;
            
            pressKey(tb, KeyEvent.VK_SPACE);
            reopenedSetSuccessfullyWithMouse = tb.getRowCount() > 1;
            
            pressKey(tb, KeyEvent.VK_SPACE);
            closedSetSuccessfullyWithKeyboard = tb.getRowCount() == 1;
            
            clickOn(tb, 0,0);
            reopenedSetSuccessfullyWithMouse = tb.getRowCount() > 1;
            
            try {Thread.currentThread().sleep(300);}catch(Exception e){}
            if (GraphicsTestCase.canSafelyRunPixelTests()) {
                grayMarginEdgePaintedWhenSortByCategory = checkPixel(ps, PropUtils.getSetRendererColor(),  6, 200);
            } else {
                grayMarginEdgePaintedWhenSortByCategory = true;
                System.err.println("CANNOT RUN PAINTING TESTS IN A <16 BIT OR HEADLESS ENVIRONMENT");
            }
            
            setSortingMode(ps, PropertySheet.SORTED_BY_NAMES);
            
            try {Thread.currentThread().sleep(300);}catch(Exception e){}
            
            grayMarginEdgeNotPaintedWhenSortByName = checkPixel(ps, ps.getBackground(),  6, 200);
            
            clickOn(tb, 0,0);
            boolPropFirst = tb._getSelection() == tp4;
            
            clickOn(tb, 0, 1);
            boolNowTrue = Boolean.TRUE.equals(tp4.getValue());
            
            pressKey(tb, KeyEvent.VK_SPACE);
            boolNowFalse = Boolean.FALSE.equals(tp4.getValue());
            
            int i = tb.getRowCount();
            tn.addProp(new TProperty2("ZZZZ", true));
            
            sleep();
            
            addedPropertiesFound = tb.getRowCount() == i+1;
            
            PropUtils.noAltBg = Boolean.FALSE;
            PropUtils.altBg = new Color(255,255, 200);
            jf.repaint();
            
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("FAILED - Exception thrown "+e.getClass().toString());
        } finally {
            setup = true;
            //            jf.hide();
            //            jf.dispose();
        }
    }
    
    
    private static Exception throwMe=null;
    private synchronized void setSortingMode(final PropertySheet ps, final int mode) throws Exception {
        throwMe = null;
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    ps.setSortingMode(mode);
                } catch (Exception e) {
                    throwMe = e;
                }
            }
        });
        if (throwMe != null) {
            Exception ex = throwMe;
            throwMe = null;
            throw (throwMe);
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
    
    private static final int SLEEP_LENGTH=1000;
    private void sleep() {
        //useful when running interactively
        
        /*
        try {
            Thread.currentThread().sleep(SLEEP_LENGTH);
        } catch (InterruptedException ie) {
            //go away
        }
         */
        
        
        
        
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
    
    
    public void testSpaceBarToggleBoolean() {
        assertTrue("After sending spacebar with boolean property selected, boolean value unchanged", boolNowFalse);
    }
    
    public void testSingleClickToggleBoolean() {
        assertTrue("Property named AAA should be first property in alphabetic sort but isn't", boolPropFirst);
    }
    
    public void testFindAddedProperties() {
        assertTrue("Adding a new property to the node did not change the number of table rows displayed", addedPropertiesFound);
    }
    
    private static boolean addedPropertiesFound=false;
    private static boolean boolNowTrue=false;
    private static boolean boolNowFalse=false;
    private static boolean boolPropFirst=false;
    private static boolean grayMarginEdgePaintedWhenSortByCategory=false;
    private static boolean grayMarginEdgeNotPaintedWhenSortByName=false;
    private static Color checkColor=null;
    private synchronized boolean checkPixel(final Component c, final Color toMatch, final int x, final int y) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                final BufferedImage bi = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
                ((JComponent) c).paintAll(bi.getGraphics());
                int[] cArr = new int[3];
                bi.getData().getPixel(6, 200, cArr);
                checkColor = new Color(cArr[0], cArr[1], cArr[2]);
                //uncomment the code below for diagnosing painting problems
                /*
                JFrame jf = new JFrame() {
                    public void paint (Graphics g) {
                        new ImageIcon (bi).paintIcon(this, g, 0, 0);
                        g.setColor (Color.RED);
                        g.drawLine(x, y, x, y);
                    }
                };
                jf.setLocation (400,400);
                jf.setSize (500,500);
                jf.show();
                 */
            }
        });
        boolean result = toMatch.equals(checkColor);
        return result;
    }
    
    public void testGrayMarginEdgeNotPaintedWhenSortByName() {
        assertTrue("Gray margin edge painted when in sort by names mode, but shouldn't be", grayMarginEdgeNotPaintedWhenSortByName);
    }
    
    public void testGrayMarginEdgePaintedWhenSortByCategory() {
        assertTrue("Gray margin edge not painted in sort by category mode", grayMarginEdgePaintedWhenSortByCategory);
    }
    
    public void testCloseSetWithMouse() {
        assertTrue("Click on row 0 in upper left did not close property set", closedSetSuccessfullyWithMouse);
    }
    
    public void testOpenSetWithKeyboard() {
        assertTrue("Spacebar on closed set did not open it", reopenedSetSuccessfullyWithMouse);
    }
    
    public void testCloseSetWithKeyboard() {
        assertTrue("Spacebar on open set did not close it", closedSetSuccessfullyWithKeyboard);
    }
    
    public void testOpenWithMouse() {
        assertTrue("Mouse on closed set did not open it", reopenedSetSuccessfullyWithMouse);
    }
    
    public void testPopupConsistent() throws Exception {
        assertTrue("Popup not always shown when mouse repeatedly clicked on different rows with popups", popupConsistent);
    }
    
    public void testInplaceReferenceCleared() {
        assertTrue("After cancelling an edit with escape, SheetCellEditor should no longer hold a reference to the former editor", inplaceReferenceCleared);
    }
    
    private static boolean setup=false;
    
    static boolean popupConsistent=true;
    static boolean closedSetSuccessfullyWithMouse=false;
    static boolean reopenedSetSuccessfullyWithMouse=false;
    static boolean reopenedSetSuccessfullyWithKeyboard=false;
    static boolean closedSetSuccessfullyWithKeyboard=false;
    static FeatureDescriptor fd_after1_0click=null;
    static InplaceEditor ie_after1_0click=null;
    static FeatureDescriptor fd_after1_1click=null;
    static InplaceEditor ie_after1_1click=null;
    static FeatureDescriptor fd_after1_0click2=null;
    static InplaceEditor ie_after1_0click2=null;
    static InplaceEditor ie_afterSpaceKeystroke=null;
    static Object valAfterTyping=null;
    static Object valAfterEscape=null;
    static boolean editorFocusedAfterSpacebarInitiation=false;
    static boolean escClosedPopup=false;
    static boolean inplaceReferenceCleared=false;
    
    public void testEscClosesPopup() {
        assertTrue("After sending escape key to open combo inplace editor, popup is still open", escClosedPopup);
    }
    
    public void testEditorFocusedAfterSpacebarInitiation() throws Exception {
        assertTrue("After instantiating inplace editor with spacebar, editor was not focus owner", editorFocusedAfterSpacebarInitiation);
    }
    
    public void testEditModeAfterSpacePressed() {
        assertTrue("Should be in edit mode after pressing space bar",  ie_afterSpaceKeystroke instanceof StringInplaceEditor);
    }
    
    public void testValAfterTyping() {
        assertTrue("Value after faking keyboard events to edit should be \"HI MOM\" but is \"" + valAfterTyping +"\"", "HI MOM".equals(valAfterTyping));
    }
    
    public void testValAfterEscape() {
        assertTrue("Value after faking keyboard events to edit, then cancelling with Escape should be \"HI MOM\" but is \"" + valAfterEscape +"\"", "HI MOM".equals(valAfterEscape));
    }
    
    public void testNoEditAfterNameClick() throws Exception {
        assertTrue("Clicking name should not put property sheet into edit mode", ie_after1_0click==null);
    }
    
    public void testSelectionAfter1_0Click() throws Exception {
        assertTrue("Selection should be first element after click on row 1, column 0, but is " + fd_after1_0click, fd_after1_0click==tp);
    }
    
    public void testEditorAfter1_1Click() throws Exception {
        assertTrue("Should be editing using string editor after click on row 1, column 1, but editor is " + ie_after1_1click, ie_after1_1click instanceof StringInplaceEditor);
    }
    
    public void testSelectionAfter1_1Click() throws Exception {
        assertTrue("Selection should be first element after click on row 1, column 1, but is " + fd_after1_1click, fd_after1_1click==tp);
    }
    
    public void testEditCancelledAfterNameClick() throws Exception {
        assertTrue("Clicking name should cancel editing, but editor.getInplaceEditor() returns " + ie_after1_0click2, ie_after1_0click2==null);
    }
    
    private void clickOn(final SheetTable tb, final int row, final int col) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Rectangle r = tb.getCellRect(row, col, false);
                Point toClick = r.getLocation();
                toClick.x += 15;
                toClick.y +=3;
                MouseEvent me = new MouseEvent(tb, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x, toClick.y, 2, false);
                tb.dispatchEvent(me);
            }
        });
        sleep();
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
                KeyEvent ke = new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, (char) key);
                target.dispatchEvent(ke);
            }
        });
        sleep();
    }
    
    private void typeKey(final Component target, final int key) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
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
            props.put(tp);
            props.put(tp1);
            props.put(tp2);
            props.put(tp3);
            props.put(tp4);
            //            props.put(tp5);
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            firePropertyChange(s,o1,o2);
        }
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public TProperty(String name, boolean isWriteable) {
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
    public class TEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        // Create new TEditor
        public TEditor() {
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
    public class TProperty2 extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public TProperty2(String name, boolean isWriteable) {
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
            return new TagsEditor();
        }
    }
    
    // Property definition
    public class TProperty3 extends PropertySupport {
        private Boolean myValue = Boolean.FALSE;
        // Create new Property
        public TProperty3(String name, boolean isWriteable) {
            super(name, Boolean.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = (Boolean) value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
    }
    
    // Property definition
    public class TProperty4 extends PropertySupport {
        private Boolean myValue = Boolean.FALSE;
        // Create new Property
        public TProperty4(String name, boolean isWriteable) {
            super(name, String.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = (Boolean) value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        public PropertyEditor getPropertyEditor() {
            return new BadEditor();
        }
    }
    
    public class BadEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public BadEditor() {
        }

        @Override
        public String[] getTags() {
            //return new String[] {"a","b","c","d","Value"};
            return null;
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            env.setState(env.STATE_INVALID);
        }

        @Override
        public boolean supportsCustomEditor() {
            return false;
        }

        @Override
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }

        @Override
        public Object getValue() {
            return Boolean.FALSE;
        }
    }
    
    
    private static TNode tn;
    private static TProperty tp;
    private static TProperty2 tp1;
    private static TProperty2 tp2;
    private static TProperty2 tp3;
    private static TProperty3 tp4;
    private static TProperty4 tp5;
    private static TEditor te;
    private static String initEditorValue;
    private static String initPropertyValue;
    private static String postChangePropertyValue;
    private static String postChangeEditorValue;

    public boolean canRun() {
        if( !ExtTestCase.canSafelyRunFocusTests() )
            return false;
        
        return super.canRun();
    }
}
