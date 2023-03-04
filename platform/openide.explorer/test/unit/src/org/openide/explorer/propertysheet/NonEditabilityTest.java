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
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import java.lang.reflect.InvocationTargetException;
import java.beans.PropertyEditor;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.explorer.propertysheet.ExtTestCase.WaitWindow;
import org.openide.nodes.Node;

/** Test finding help IDs in the property sheet.
 * @author Jesse Glick
 * @see "#14701"
 */
public class NonEditabilityTest extends ExtTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NonEditabilityTest.class);
    }

    public NonEditabilityTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }

    private PropertySheet sheet = null;
    private JFrame frame = null;
    protected void setUp() throws Exception {
        //Ensure we don't have a bogus stored value
        PropUtils.putSortOrder(PropertySheet.UNSORTED);

        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new BorderLayout());
        sheet = new PropertySheet();
        jf.getContentPane().add (sheet);

        jf.setBounds (20, 20, 200, 400);
        frame = jf;
        new WaitWindow(jf);
    }

    public void testClickInvokesCustomEditor() throws Exception {
        if( !ExtTestCase.canSafelyRunFocusTests() )
            return;
        
        Node n = new ANode();
        setCurrentNode (n, sheet);

        sleep();

        requestFocus (sheet.table);
        sleep();

        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        if (owner == sheet.table) { //sanity check to avoid random failures on some window managers

            System.out.println ("About to click cell");

            Rectangle r = sheet.table.getCellRect(1, 1, false);
            final MouseEvent me = new MouseEvent (sheet.table, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, r.x + 3,
                r.y + 3, 2, false);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    sheet.table.dispatchEvent(me);
                }
            });

            sleep();
            sleep();

            System.out.println ("Now checking focus");

            owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            assertTrue ("Focus owner should be custom editor, not " + owner, owner instanceof JTextArea);

            JComponent jc = (JComponent) owner;
            assertTrue ("Custom editor should have been invoked, but focus owner's top level ancestor is not a dialog", jc.getTopLevelAncestor() instanceof Dialog);

            Dialog d = (Dialog) jc.getTopLevelAncestor();

            d.setVisible(false);
        }

        requestFocus (sheet.table);
        sleep();

        owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        if (owner == sheet.table) { //sanity check to avoid random failures on some window managers
            pressKey(sheet.table, KeyEvent.VK_SPACE);
            sleep();

            owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
            assertTrue ("After pressing a key, focus owner should still be the table, not " + owner, sheet.table == owner);
        }

    }
    
    private static final class ANode extends AbstractNode {
        public ANode() {
            super(Children.LEAF);
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx("node-help");
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.put (new AProperty());
            ss.put (new AProperty());
            s.put(ss);
            return s;
        }
    }

    private static final String name = "foo";
    private static final class AProperty extends PropertySupport.ReadOnly {
        public AProperty() {
            super(name, String.class, name, name);
        }
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return "value-" + getName();
        }

        public PropertyEditor getPropertyEditor() {
            return new APropertyEditor();
        }
    }

    private static final class APropertyEditor implements PropertyEditor {
        public void setValue(Object value) {

        }

        public Object getValue() {
            return null;
        }

        public boolean isPaintable() {
            return true;
        }

        public void paintValue(Graphics gfx, Rectangle box) {
            gfx.setColor (Color.ORANGE);
            gfx.fillRect (box.x, box.y, box.width, box.height);
        }

        public String getJavaInitializationString() {
            return null;
        }

        public String getAsText() {
            return null;
        }

        public void setAsText(String text) throws IllegalArgumentException {

        }

        public String[] getTags() {
            return null;
        }

        public Component getCustomEditor() {
            return new JTextArea();
        }

        public boolean supportsCustomEditor() {
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {

        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {

        }
    }
}
