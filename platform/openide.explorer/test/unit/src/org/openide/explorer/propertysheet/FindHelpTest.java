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
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.JFrame;
import org.netbeans.junit.RandomlyFails;
import org.openide.explorer.propertysheet.ExtTestCase.WaitWindow;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;

/** Test finding help IDs in the property sheet.
 * @author Jesse Glick
 * @see "#14701"
 */
@RandomlyFails // NB-Core-Build #3747
public class FindHelpTest extends ExtTestCase {
    
    public FindHelpTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    private PropertySheet sheet = null;
    private JFrame frame = null;
    protected void setUp() throws Exception {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new BorderLayout());
        sheet = new PropertySheet();
        jf.getContentPane().add(sheet);
        
        jf.setBounds(20, 20, 200, 400);
        frame = jf;
        new WaitWindow(jf);
    }
    
    public void testFindHelpOnProperty() throws Exception {
        Node n = new WithPropertyHelpNode();
        setCurrentNode(n, sheet);
        
        sleep();
        
        PropertySheet.HelpAction act = sheet.helpAction;
        
        assertTrue("No help context found", act.getContext() != null);
        
        assertTrue("Help action should be enabled", act.isEnabled());
        
    }
    
    public void testFindPropertiesHelpOnNode() throws Exception {
        Node n = new WithPropertiesHelpNode();
        setCurrentNode(n, sheet);
        
        sleep();
        
        PropertySheet.HelpAction act = sheet.helpAction;
        
        assertTrue("No help context found", act.getContext() != null);
        
        assertTrue("Help action should be enabled", act.isEnabled());
    }
    
    public void testNoHelpProvided() throws Exception {
        Node n = new HelplessNode();
        setCurrentNode(n, sheet);
        
        sleep();
        
        PropertySheet.HelpAction act = sheet.helpAction;
        
        assertFalse("A help context was found on a node with no properties help", act.getContext() != null);
        
        assertFalse("Help action should be disabled", act.isEnabled());
        
    }
    
    // XXX test use of ExPropertyEditor.PROPERTY_HELP_ID
    
    private static Collection findChildren(Component p, Class c) {
        Collection x = new LinkedList();
        findChildren(p, c, x);
        return x;
    }
    
    private static void findChildren(Component p, Class c, Collection x) {
        if (c.isInstance(p)) {
            x.add(p);
        } else if (p instanceof Container) {
            Component[] k = ((Container)p).getComponents();
            for (int i = 0; i < k.length; i++) {
                findChildren(k[i], c, x);
            }
        }
    }
    
    /**
     * A node which provides no help - the help action should always be disabled
     */
    private static final class HelplessNode extends AbstractNode {
        public HelplessNode() {
            super(Children.LEAF);
        }
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.put(new WithHelpProperty("prop1", "row-help-1"));
            ss.put(new WithHelpProperty("prop2", "row-help-2"));
            ss.put(new WithHelpProperty("prop3", null));
            s.put(ss);
            ss = Sheet.createExpertSet();
            ss.put(new WithHelpProperty("prop4", "row-help-4"));
            ss.put(new WithHelpProperty("prop5", null));
            s.put(ss);
            return s;
        }
    }
    
    /**
     * A node whose properties provide their own help IDs
     */
    private static final class WithPropertyHelpNode extends AbstractNode {
        public WithPropertyHelpNode() {
            super(Children.LEAF);
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx("node-help");
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.setValue("helpID", "properties-help");
            ss.put(new WithHelpProperty("prop1", "row-help-1"));
            ss.put(new WithHelpProperty("prop2", "row-help-2"));
            ss.put(new WithHelpProperty("prop3", null));
            s.put(ss);
            ss = Sheet.createExpertSet();
            ss.put(new WithHelpProperty("prop4", "row-help-4"));
            ss.put(new WithHelpProperty("prop5", null));
            s.put(ss);
            return s;
        }
    }
    
    /**
     * A node which uses the per-node key for property sheet specific help -
     * the help action should be enabled for all its properties
     */
    private static final class WithPropertiesHelpNode extends AbstractNode {
        public WithPropertiesHelpNode() {
            super(Children.LEAF);
            setValue("propertiesHelpID", "propertiesHelp");
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx("node-help");
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.put(new WithoutHelpProperty("prop1"));
            ss.put(new WithoutHelpProperty("prop2"));
            ss.put(new WithoutHelpProperty("prop3"));
            s.put(ss);
            ss = Sheet.createExpertSet();
            ss.put(new WithoutHelpProperty("prop4"));
            ss.put(new WithoutHelpProperty("prop5"));
            s.put(ss);
            return s;
        }
    }
    
    private static final class WithTabsSetHelpNode extends AbstractNode {
        public WithTabsSetHelpNode() {
            super(Children.LEAF);
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx("node-help");
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.put(new WithoutHelpProperty("prop1"));
            ss.put(new WithoutHelpProperty("prop2"));
            ss.put(new WithoutHelpProperty("prop3"));
            ss.setValue("tabName", "Tab 1");
            ss.setValue("helpID", "set-help-id");
            s.put(ss);
            ss = Sheet.createExpertSet();
            ss.put(new WithoutHelpProperty("prop4"));
            ss.put(new WithoutHelpProperty("prop5"));
            ss.setValue("tabName", "Tab 2");
            s.put(ss);
            return s;
        }
    }
    
    
    private static final class WithHelpProperty extends PropertySupport.ReadOnly {
        public WithHelpProperty(String name, String helpID) {
            super(name, String.class, name, name);
            if (helpID != null) {
                setValue("helpID", helpID);
            }
        }
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return "value-" + getName();
        }
    }
    
    private static final class WithoutHelpProperty extends PropertySupport.ReadOnly {
        public WithoutHelpProperty(String name) {
            super(name, String.class, name, name);
        }
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return "value-" + getName();
        }
    }
    
    
}
