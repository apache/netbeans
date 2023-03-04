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
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.explorer.propertysheet.ExtTestCase.WaitWindow;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/** Tests property marking functionality and the ability of a Property to
 * provide a "postSetAction" action hint, which will be run if the user
 * successfully changes the property value.
 */
public class PropertyMarkingTest extends GraphicsTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(PropertyMarkingTest.class);
    }

    private static boolean setup=false;
    
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
    
    public PropertyMarkingTest(String name) {
        super(name);
    }
    
    static SheetTable tb=null;
    static JFrame jf=null;
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        if (setup) return;
        PropUtils.forceRadioButtons=false;
        
        try {
            
            tp = new TProperty("oh", true);
            tp1 = new TProperty2("the", true);
            tp2 = new TProperty2("pretty", true);
            tp3 = new TProperty2("pictures",true);
            tp4 = new TProperty3("I can create",true);
            postSetAction = new PostSetAction();
            
            tn = new TNode();
            
            final PropertySheet ps = new PropertySheet();
            
            //ensure no stored value in preferences:
            ps.setCurrentNode(tn);
            sleep();
            ps.setSortingMode(PropertySheet.UNSORTED);
            
            jf = new JFrame();
            jf.getContentPane().add(ps);
            jf.setLocation(20,20);
            jf.setSize(300, 400);
            new WaitWindow(jf);
            tb = ps.table;
            
            ps.setSortingMode(ps.SORTED_BY_NAMES);
            jf.repaint();
            
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("FAILED - Exception thrown "+e.getClass().toString());
        } finally {
            setup = true;
        }
    }
    
    public void testPostSetAction() throws Exception {
        if (!canSafelyRunFocusTests()) {
            System.err.println("Cannot run post set " +
                    "action test - testing machine's focus behavior is not sane");
            return;
        }
        pressCell(tb, 1, 1);
        
        sleep();
        requestFocus(tb);
        System.err.println("WILL TYPE MIAOU");
        JComponent focusOwner = (JComponent)KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        assertTrue("Focus should be on a JTextField, not " + focusOwner, focusOwner instanceof JTextField);
        
        typeString("MIAOU", focusOwner);
        
        sleep();
        sleep();
        System.err.println("MIAOU TYPED");
        
        postSetAction.assertNotPerformed();
        System.err.println("ACTION NOT PERFORMED AS IT SHOULD BE");
        
        pressKey(tb, KeyEvent.VK_ENTER);
        System.err.println("SENDING ENTER TO " + tb);
        Thread.currentThread().sleep(1000);
        
        sleep();
        
        System.err.println("TYPING ENTER TO " + tb);
        typeKey(tb, KeyEvent.VK_ENTER);
        sleep();
        sleep();
        
        System.err.println("POST SET ACTION PERFORMED: " + postSetAction.performed);
        postSetAction.assertPerformed();
        click(tb, 1, 0);
    }
    
    /*
    public void testNameIconDrawn() throws Exception {
        if (!canSafelyRunPixelTests()) {
            //This test will fail on 8 bit displays
            return;
        }
        Rectangle r = tb.getCellRect (1, 0, false);
        assertColorOnComponent(tb, Color.BLUE, r.x+7, r.y+7);
    }
     
     
    public void testValueIconDrawn() throws Exception {
        if (!canSafelyRunPixelTests()) {
            //This test will fail on 8 bit displays
            return;
        }
        Rectangle r = tb.getCellRect (1, 1, false);
        assertColorOnComponent(tb, Color.GREEN, r.x+7, r.y+7);
    }
     
     
    public void testErrorIconDrawn() throws Exception {
        if (!canSafelyRunPixelTests()) {
            //This test will fail on 8 bit displays
            return;
        }
        Image i = loadImage("org/openide/resources/propertysheet/invalid.gif");
        Rectangle r = tb.getCellRect (3, 1, false);
        assertPixelFromImage(i, tb, 9 - PropUtils.getTextMargin(), 6, r.x+7, r.y+7);
//        assertPixelFromImage(i, tb, 12 - PropUtils.getTextMargin(), 11, r.x+12, r.y+12);
//        assertPixelFromImage(i, tb, 5 - PropUtils.getTextMargin(), 4, r.x+6, r.y+5);
    }
     */
    
    
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
            super(name, String.class, name, "", true, isWriteable);
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
            return new TEditor();
        }
        
        public Object getValue(String key) {
            if ("nameIcon".equals(key)) {
                return new NameIcon();
            } else if ("valueIcon".equals(key)) {
                return new ValueIcon();
            } else if ("postSetAction".equals(key)) {
                return postSetAction;
            }
            return super.getValue(key);
        }
    }
    
    // Editor definition
    public class TEditor extends PropertyEditorSupport {
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
        
        public TagsEditor() {
        }

        @Override
        public String[] getTags() {
            return new String[] {"a","b","c","d","Value"};
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
        
        public Object getValue(String key) {
            if ("nameIcon".equals(key)) {
                return new NameIcon();
            } else if ("valueIcon".equals(key)) {
                return new ValueIcon();
            } else if ("postSetAction".equals(key)) {
                return postSetAction;
            }
            return super.getValue(key);
        }
        
        public PropertyEditor getPropertyEditor() {
            return new WrapperEx(super.getPropertyEditor());
        }
        
        public class WrapperEx implements ExPropertyEditor {
            private PropertyEditor orig;
            public WrapperEx(PropertyEditor orig) {
                this.orig = orig;
            }
            
            
            public void attachEnv(PropertyEnv env) {
                env.setState(myValue == Boolean.FALSE ? env.STATE_INVALID : env.STATE_VALID);
            }
            
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                orig.addPropertyChangeListener(listener);
            }
            
            public String getAsText() {
                return orig.getAsText();
            }
            
            public Component getCustomEditor() {
                return orig.getCustomEditor();
            }
            
            public String getJavaInitializationString() {
                return orig.getJavaInitializationString();
            }
            
            public String[] getTags() {
                return orig.getTags();
            }
            
            public Object getValue() {
                return orig.getValue();
            }
            
            public boolean isPaintable() {
                return orig.isPaintable();
            }
            
            public void paintValue(Graphics gfx, Rectangle box) {
                orig.paintValue(gfx, box);
            }
            
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                orig.removePropertyChangeListener(listener);
            }
            
            public void setAsText(String text) throws IllegalArgumentException {
                orig.setAsText(text);
            }
            
            public void setValue(Object value) {
                orig.setValue(value);
            }
            
            public boolean supportsCustomEditor() {
                return true;
            }
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
            return true;
        }

        @Override
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
    }
    
    private class NameIcon implements Icon {
        
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
                g.setColor(Color.GREEN);
                g.drawRect(x, y, getIconWidth(), getIconHeight());
                g.fillRect(x+3, y+3, getIconWidth()-5, getIconHeight()-5);
            } finally {
                g.setColor(col);
            }
        }
    }
    
    private class PostSetAction extends AbstractAction {
        boolean performed = false;
        
        public void assertPerformed() {
            assertTrue("Action was not performed", performed);
            performed = false;
        }
        
        public void assertNotPerformed() {
            assertTrue("Action should not be performed before an appropriate event is triggered", !performed);
        }
        
        public void actionPerformed(ActionEvent e) {
            performed = true;
        }
        
    }
    
    
    private static TNode tn;
    private static TProperty tp;
    private static TProperty2 tp1;
    private static TProperty2 tp2;
    private static TProperty2 tp3;
    private static TProperty3 tp4;
    private static TEditor te;
    private static PostSetAction postSetAction;
    private static String initEditorValue;
    private static String initPropertyValue;
    private static String postChangePropertyValue;
    private static String postChangeEditorValue;
    
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
