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

package org.netbeans.test.beans.operators;

import java.awt.Component;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.beans.BeansTestCase;
import org.netbeans.test.beans.BeansTestCase.NodeConverter;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 *
 * @author jprox
 */
public class BeanInfoOperator extends TopComponentOperator {
    
    private JTreeOperator tree;
    private PropertySheetOperator propertySheetOperator;   

    public BeanInfoOperator(String name) {
        this(name,0);
    }
    
    public BeanInfoOperator(String name, int index) {
        super(waitTopComponent(null, name, index, new BeanInfoDesignerSubchooser()));
    }
    
    public JTreeOperator getTreeOperator() {
        if(tree == null) {
            tree = new JTreeOperator(this);
        }
        return tree;
    }
    
    public PropertySheetOperator getPropertySheetOperator() {
        if(propertySheetOperator== null) {
            propertySheetOperator = new PropertySheetOperator(this);
        }
        return propertySheetOperator;
    }
    
    public void selectNode(String nodeName) {
	JTreeOperator tree = getTreeOperator();
	tree.selectPath(BeansTestCase.getTreePath(tree, nodeName, defaultConverter));
    }
    
    public void setPropertyValues(String nodeName, PropertyValue... propertyValues) {        
        this.selectNode(nodeName);
        PropertySheetOperator pso = this.getPropertySheetOperator();
        for (PropertyValue propertyValue : propertyValues) {
            new Property(pso, propertyValue.index).setValue(propertyValue.value);
        }                
        pso.updateUI();
    }
    
    public TreePath findPath(String node) {
        return BeansTestCase.getTreePath(getTreeOperator(), node,defaultConverter);
    }
    
    private static final class BeanInfoDesignerSubchooser implements ComponentChooser {

        @Override
        public boolean checkComponent(Component comp) {            
            return comp.getClass().getName().contains("BIEditorSupport$BeanInfoElement");
        }

        @Override
        public String getDescription() {
            return " org.netbeans.modules.beans.beaninfo.BIEditorSupport$BeanInfoElement";
        }
    }
    
    public static NodeConverter defaultConverter = new BeansTestCase.NodeConverter() {
        @Override
        public String getDisplayName(TreeNode node) {
            Node n = Visualizer.findNode(node);
            return n.getDisplayName();
        }
    };
    
    public static class PropertyValue {
        private int index;
        private String value;

        public PropertyValue(int index, String value) {
            this.index = index;
            this.value = value;
        }                
    }
    
}
