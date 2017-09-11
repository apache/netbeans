/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.MissingResourceException;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

// This test class tests the main functionality of the property sheet
public class RestoreDefaultValueTest extends ExtTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(RestoreDefaultValueTest.class);
    }

    private PropertySheet ps = null;
    public RestoreDefaultValueTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    static {
        ExtTestCase.installCorePropertyEditors();
    }
    
    private static boolean setup = false;
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        // Create new TNode
        tn = new TNode();
        
        
        //Replacing NodeOp w/ JFrame to eliminate depending on full IDE init
        //and long delay while waiting for property sheet thus requested to
        //initialize
        final JFrame jf = new JFrame();
        ps = new PropertySheet();
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(ps, BorderLayout.CENTER);
        jf.setLocation(30,30);
        jf.setSize(500,500);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ps.setNodes(new Node[] {tn});
                //ps.setCurrentNode(tn);
                jf.show();
                jf.toFront();
            }
        });
        
        new ExtTestCase.WaitWindow(jf);
        
        try {
            ensurePainted(ps);
        } catch (Exception e) {
            fail("FAILED - Exception thrown "+e.getClass().toString());
        }
    }
    
    private void ensurePainted(final PropertySheet ps) throws Exception {
        //issues 39205 & 39206 - ensure the property sheet really repaints
        //before we get the value, or the value in the editor will not
        //have changed
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Graphics g = ps.getGraphics();
                ps.paintImmediately(0,0,ps.getWidth(), ps.getHeight());
            }
        });
    }
    
    
    public void testRestoreDefaultValueWorks() throws Exception {
        System.err.println("testRestoreDefaultValueWorks");
        if (!super.canSafelyRunFocusTests()) {
            return;
        }
        
        sleep();
        sleep();
        sleep();
        
        assertNotNull("Property not created", tp);
        
        final Action invoke = ps.table.getActionMap().get("invokeCustomEditor");
        assertNotNull("Incompatible change - no action from table for the key" +
                "\"invokeCustomEditor\".", invoke);
        
        pressCell(ps.table, 1, 1);
        
        Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        
        
        sleep();
        sleep();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                invoke.actionPerformed(null);
            }
        });
        
        int ct = 0;
        while (!(w instanceof JDialog)) {
            if (ct++ > 10000) {
                fail("No dialog ever shown");
            }
            sleep();
            w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        }
        sleep();
        System.out.println("  Dlg now showing");
        final AbstractButton jb = findResetToDefaultButton((JDialog)w);
        
        if (jb == null) {
            fail("Could not find Reset To Default Button in dialog");
        }
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                jb.doClick();
            }
        });
        
        
        
        Thread.currentThread().sleep(10000);
        
    }
    
    private AbstractButton findResetToDefaultButton(JDialog jd) {
        String txt = null;
        try {
            txt = NbBundle.getMessage(PropertyDialogManager.class, "CTL_Default");
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            fail("Bundle key CTL_DEFAULT missing from org.openide.explorer.propertysheet.Bundle.properties");
        }
        return findButton(jd.getContentPane(), txt);
    }
    
    private AbstractButton findButton(Container c, String s) {
        if (c instanceof AbstractButton && s.equals(((AbstractButton) c).getText())) {
            return ((AbstractButton) c);
        } else {
            Component[] cs = c.getComponents();
            for (int i=0; i < cs.length; i++) {
                if (cs[i] instanceof Container) {
                    AbstractButton result = findButton((Container) cs[i], s);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
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
        
        public void destroy() {
            fireNodeDestroyed();
        }
        
        // Create a property sheet:
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            // Make sure there is a "Properties" set:
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                props = Sheet.createPropertiesSet();
                sheet.put(props);
            }
            tp = new TProperty("property", true);
            props.put(tp);
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            firePropertyChange(s,o1,o2);
        }
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private String myValue = "Value";
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
            myValue = (String) value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        private boolean defValue = false;
        public boolean isDefaultValue() {
            return defValue;
        }
        
        private boolean rdv = false;
        public void restoreDefaultValue() {
            defValue = true;
            rdv = true;
            System.err.println("RestoreDefaultValue");
            try {
                setValue("default");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        
        public void assertRestoreDefaultValueCalled() {
            assertTrue("Restore default value not called", rdv);
            rdv = false;
        }
        
        public void assertRestoreDefaultValueNotCalled() {
            assertFalse("Restore default value not called", rdv);
        }
    }
    
    private TNode tn;
    private TProperty tp;
}
