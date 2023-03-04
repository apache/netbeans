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
package org.netbeans.jellytools.properties;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.netbeans.jemmy.EventTool;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/** Node with all customizable properties
 *
 * @author Marian Mirilovic
 */
public class TestNode extends AbstractNode {

    public static final String NODE_NAME = "TestNode";

    /** Create new instance of the node */
    public TestNode() {
        super(Children.LEAF);
        setName(NODE_NAME); // or, super.setName if needed
        setDisplayName(NODE_NAME);
    }

    public void showProperties() {
        // Display Properties of a Node
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                NodeOperation.getDefault().showProperties(TestNode.this);
            }
        });
        // Wait 2s for showing properties sheet
        new EventTool().waitNoEvent(2000);
    }

    /************************************************************************/
    /**
     * Clone existing node
     * @return cloned node
     */
    @Override
    public Node cloneNode() {
        return new TestNode();
    }

    /**
     * Create a property sheet - that shows node with all customizable properties.
     * @return property sheet
     */
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        // Make sure there is a "Properties" set:
        Sheet.Set props = sheet.get(Sheet.PROPERTIES);
        if (props == null) {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }

        // props.put(new TestProperty("Boolean", java.lang.Boolean.class));
        TestProperty booleanObjectProperty = new TestProperty("Boolean", java.lang.Boolean.class);
        try {
            booleanObjectProperty.setValue((Object) Boolean.TRUE);
            props.put(booleanObjectProperty);
        } catch (Exception exc) {
            System.err.println("Exception during set value and add Boolean property :" + exc.getMessage());
        }

        // props.put(new TestProperty("boolean", boolean.class));
        TestProperty booleanProperty = new TestProperty("boolean", boolean.class);
        try {
            booleanProperty.setValue((Object) Boolean.TRUE);
            props.put(booleanProperty);
        } catch (Exception exc) {
            System.err.println("Exception during set value and add boolean property :" + exc.getMessage());
        }

        props.put(new TestProperty("Byte", java.lang.Byte.class));
        props.put(new TestProperty("byte", byte.class));
        //props.put(new TestProperty("Character", java.lang.Character.class));
        TestProperty characterProperty = new TestProperty("Character", java.lang.Character.class);
        try {
            characterProperty.setValue("a");
            props.put(characterProperty);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        // props.put(new TestProperty("char", char.class));
        TestProperty charProperty =
                new TestProperty("char", char.class);
        try {
            charProperty.setValue((Object) "a");
            props.put(charProperty);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        props.put(new TestProperty("Class", java.lang.Class.class));
        TestProperty colorProperty =
                new TestProperty("Color", java.awt.Color.class);
        try {
            // need to set not null value
            colorProperty.setValue(Color.BLACK);
            props.put(colorProperty);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        props.put(new TestProperty("Dimension", java.awt.Dimension.class));
        props.put(new TestProperty("Double", java.lang.Double.class));
        props.put(new TestProperty("double", double.class));
        props.put(new TestProperty("File", java.io.File.class));
        props.put(new TestProperty("Filesystem", org.openide.filesystems.FileSystem.class));
        props.put(new TestProperty("Float", java.lang.Float.class));
        props.put(new TestProperty("float", float.class));
        props.put(new TestProperty("Font", java.awt.Font.class));
        props.put(new TestProperty("Html Browser", org.openide.awt.HtmlBrowser.Factory.class));
        props.put(new TestProperty("Indent Engine", org.openide.text.IndentEngine.class));
        props.put(new TestProperty("Insets", java.awt.Insets.class));
        props.put(new TestProperty("Integer", java.lang.Integer.class));
        props.put(new TestProperty("Integer", java.lang.Integer.class));
        props.put(new TestProperty("int", int.class));
        props.put(new TestProperty("Long", java.lang.Long.class));
        props.put(new TestProperty("long", long.class));
        props.put(new TestProperty("NbClassPath", org.openide.execution.NbClassPath.class));
        props.put(new TestProperty("NbProcessDescriptor", org.openide.execution.NbProcessDescriptor.class));
        props.put(new TestProperty("Object", java.lang.Object.class));
        props.put(new TestProperty("Point", java.awt.Point.class));
        props.put(new TestProperty("property_Properties", java.util.Properties.class));
        props.put(new TestProperty("Rectangle", java.awt.Rectangle.class));
        props.put(new TestProperty("Service Type", org.openide.ServiceType.class));
        props.put(new TestProperty("Short", java.lang.Short.class));
        props.put(new TestProperty("short", short.class));
        props.put(new TestProperty("String", java.lang.String.class));
        props.put(new TestProperty("String []", java.lang.String[].class));
        props.put(new TestProperty("Table Model", javax.swing.table.TableModel.class));
        props.put(new TestProperty("URL", java.net.URL.class));

        return sheet;
    }

    /**
     * Method firing changes
     * @param s
     * @param o1
     * @param o2
     */
    public void fireMethod(String s, Object o1, Object o2) {
        firePropertyChange(s, o1, o2);
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
        @Override
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
        @Override
        public void setValue(Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = value;
            TestNode.this.fireMethod(getName(), oldVal, myValue);
        }
    }
}
