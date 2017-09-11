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
package gui.propertyeditors;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

import java.lang.reflect.InvocationTargetException;

/**
 * This test class tests the main functionality of the property sheet,
 * property editors and property customizers customizable by IDE.
 *
 * @author  mmirilovic@netbeans.org
 */
public class PropertiesTest {

    /** Node with all customizable properties */
    private TNode tn;
    
    /** Create new instance of the TNode and show property sheet */
    public PropertiesTest() {
        // Create new TNode
        tn = new TNode();
        
        // Display Node
        //NodeOperation.getDefault().getNodeOperation().explore(tn);
        
        // Display Properties of a Node
        NodeOperation.getDefault().showProperties(tn);
       //NodeOperation no = (NodeOperation)org.openide.util.Lookup.getDefault().lookup(NodeOperation.class);
       //no.showProperties(tn);

        // Wait 3s for showing properties sheet
        try {
            Thread.currentThread().sleep(3000);
        }catch(Exception exc){
            System.err.println("Exception during sleep after showing properties sheet :" + exc.getMessage());
        }
    }
    
    /** Definition of the node with all customizable properties */
    public class TNode extends AbstractNode {
        
        /** Create new instance of the node */
        public TNode() {
            super(Children.LEAF);
            setName("TestNode"); // or, super.setName if needed
            setDisplayName("TestNode");
        }
        
        /**
         * Clone existing node 
         * @return cloned node
         */        
        public Node cloneNode() {
            return new TNode();
        }
        
        /**
         * Create a property sheet - that shows node with all customizable properties.
         * @return property sheet
         */        
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            // Make sure there is a "Properties" set:
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                props = Sheet.createPropertiesSet();
                sheet.put(props);
            }
            
            // props.put(new TestProperty("Boolean", java.lang.Boolean.class));            
            TestProperty booleanObjectProperty = new TestProperty("Boolean",     java.lang.Boolean.class);
            try {
                booleanObjectProperty.setValue(Boolean.TRUE);
                props.put(booleanObjectProperty);
            }
            catch (Exception exc) {
                System.err.println("Exception during set value and add Boolean property :" +
                                   exc.getMessage());
            }
            
            // props.put(new TestProperty("boolean", boolean.class));
            TestProperty booleanProperty = new TestProperty("boolean",     boolean.class);
            try {
                booleanProperty.setValue(Boolean.TRUE);
                props.put(booleanProperty);
            }
            catch (Exception exc) {
                System.err.println("Exception during set value and add boolean property :" +
                                   exc.getMessage());
            }
            
            props.put(new TestProperty("Byte",                  java.lang.Byte.class));            
            props.put(new TestProperty("byte",                  byte.class));            
            props.put(new TestProperty("Character",             java.lang.Character.class));
            
            // props.put(new TestProperty("char", char.class));
            TestProperty charProperty = new TestProperty("char",     char.class);
            try {
                charProperty.setValue("a");
                props.put(charProperty);
            }
            catch (Exception exc) {
                System.err.println("Exception during set value and add char property :" +
                                   exc.getMessage());
            }
            
            props.put(new TestProperty("Class",                 java.lang.Class.class));            
            props.put(new TestProperty("Color",                 java.awt.Color.class));            
            props.put(new TestProperty("Dimension",             java.awt.Dimension.class));            
            props.put(new TestProperty("Double",                java.lang.Double.class));            
            props.put(new TestProperty("double",                double.class));            
            props.put(new TestProperty("File",                  java.io.File.class));            
            props.put(new TestProperty("Float",                 java.lang.Float.class));            
            props.put(new TestProperty("float",                 float.class));            
            props.put(new TestProperty("Font",                  java.awt.Font.class));            
            props.put(new TestProperty("Html Browser",          org.openide.awt.HtmlBrowser.Factory.class));
            props.put(new TestProperty("Insets",                java.awt.Insets.class));            
            props.put(new TestProperty("Integer",               java.lang.Integer.class));            
            props.put(new TestProperty("int",                   int.class));            
            props.put(new TestProperty("Long",                  java.lang.Long.class));            
            props.put(new TestProperty("long",                  long.class));            
//            props.put(new TestProperty("NbClassPath",           org.openide.execution.NbClassPath.class));            
//            props.put(new TestProperty("NbProcessDescriptor",   org.openide.execution.NbProcessDescriptor.class));            
            props.put(new TestProperty("Object",                java.lang.Object.class));            
            props.put(new TestProperty("Point",                 java.awt.Point.class));            
            props.put(new TestProperty("property_Properties",   java.util.Properties.class));            
            props.put(new TestProperty("Rectangle",             java.awt.Rectangle.class));            
            props.put(new TestProperty("Short",                 java.lang.Short.class));            
            props.put(new TestProperty("short",                 short.class));            
            props.put(new TestProperty("String",                java.lang.String.class));            
            props.put(new TestProperty("String []",             java.lang.String[].class));            
            props.put(new TestProperty("URL",                   java.net.URL.class));            

            return sheet;
        }
        
        /**
         * Method firing changes
         * @param s
         * @param o1
         * @param o2
         */        
        public void fireMethod(String s, Object o1, Object o2) {
            firePropertyChange(s,o1,o2);
        }
    }
    
    /** Property definition */
    public class TestProperty extends PropertySupport {
        Object myValue;
        
        /**
         * Create new property 
         * @param name
         * @param classType
         */        
        public TestProperty(String name, Class classType) {
            super(name, classType, name, "", true, true);
        }
        
        /**
         * Get property value
         * @return property value
         */        
        public Object getValue() {
            return myValue;
        }
        
        /**
         * Set property value
         * @param value property value
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */        
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
    }
    
    
    /**
     * Main method for trying it within IDE.
     * @param args
     */    
    public static void main(String args[]) {
        new PropertiesTest();
    }
    
}
