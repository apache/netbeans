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
package org.netbeans.test.beans;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.beans.operators.BeanInfoOperator;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 *
 * @author jprox
 */
public class BeanInfoEditorTest extends BeansTestCase {

    public BeanInfoEditorTest(String testName) {
        super(testName);
    }

    private EditorOperator editor;

    public void switchToDesign(EditorOperator editor) {
        AbstractButtonOperator toolbarButton = editor.getToolbarButton("Designer");
        toolbarButton.pushNoBlock();
        new EventTool().waitNoEvent(1000);
    }

    public void switchToSource(EditorOperator editor) {
        AbstractButtonOperator toolbarButton = editor.getToolbarButton("Source");
        toolbarButton.pushNoBlock();
        new EventTool().waitNoEvent(1000);
    }

    public void testTree() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");
            String tree = getTree(bio.getTreeOperator(), BeanInfoOperator.defaultConverter);
            String expected = ""
                    + "BeanInfo\n"
                    + "__Bean\n"
                    + "____beans.Source\n"
                    + "__Properties\n"
                    + "____x\n"
                    + "____y\n"
                    + "__Event Sources\n"
                    + "____propertyChangeListener\n"
                    + "____vetoableChangeListener\n"
                    + "__Methods\n"
                    + "____method\n";
            assertEquals(tree, expected);
        } finally {
            if (editor != null) {
                editor.closeDiscard();
            }
        }
    }
            
    public void testBeanInfoNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("BeanInfo", new BeanInfoOperator.PropertyValue(1, "/beans/Beans.java"),
                    new BeanInfoOperator.PropertyValue(2, "/beans/Beans.java"),
                    new BeanInfoOperator.PropertyValue(3, "/beans/Beans.java"),
                    new BeanInfoOperator.PropertyValue(4, "/beans/Beans.java"),
                    new BeanInfoOperator.PropertyValue(5, "2"),
                    new BeanInfoOperator.PropertyValue(6, "3"),
                    new BeanInfoOperator.PropertyValue(7, "TRUE"));                              
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testGetFromIntrospection() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("Bean", new BeanInfoOperator.PropertyValue(1, "TRUE"));
            bio.setPropertyValues("Properties", new BeanInfoOperator.PropertyValue(1, "TRUE"));
            bio.setPropertyValues("Event Sources", new BeanInfoOperator.PropertyValue(1, "TRUE"));
            bio.setPropertyValues("Methods", new BeanInfoOperator.PropertyValue(1, "TRUE"));            
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testBeanNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("Bean", new BeanInfoOperator.PropertyValue(3, "FALSE"));            
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testBeansSourceNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("beans.Source", new BeanInfoOperator.PropertyValue(2, "TRUE"),
                    new BeanInfoOperator.PropertyValue(3, "TRUE"),
            new BeanInfoOperator.PropertyValue(4, "TRUE"),
            new BeanInfoOperator.PropertyValue(5, "\"display\""),
            new BeanInfoOperator.PropertyValue(6, "\"description\""),
            new BeanInfoOperator.PropertyValue(8, "String.class"));            
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testPropertiesNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("Properties", new BeanInfoOperator.PropertyValue(3, "FALSE"));
            
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testPropertyNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("x", new BeanInfoOperator.PropertyValue(7, "FALSE"));
            bio.setPropertyValues("y", new BeanInfoOperator.PropertyValue(2, "TRUE"),
                    new BeanInfoOperator.PropertyValue(3, "TRUE"),
                    new BeanInfoOperator.PropertyValue(4, "TRUE"),
                    new BeanInfoOperator.PropertyValue(5, "\"display\""),
                    new BeanInfoOperator.PropertyValue(6, "\"description\""),
                    new BeanInfoOperator.PropertyValue(9, "TRUE"),
                    new BeanInfoOperator.PropertyValue(10, "TRUE"),
                    new BeanInfoOperator.PropertyValue(12, "String.class"),
                    new BeanInfoOperator.PropertyValue(13, "TRUE"),
                    new BeanInfoOperator.PropertyValue(14, "TRUE"));            
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testEventSourcesNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("Event Sources", new BeanInfoOperator.PropertyValue(3, "FALSE"));
            
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testChangeListenerNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("propertyChangeListener", new BeanInfoOperator.PropertyValue(7, "FALSE"));
            bio.setPropertyValues("vetoableChangeListener", new BeanInfoOperator.PropertyValue(2, "TRUE"),
                    new BeanInfoOperator.PropertyValue(3, "TRUE"),
                    new BeanInfoOperator.PropertyValue(4, "TRUE"),
                    new BeanInfoOperator.PropertyValue(5, "\"display\""),
                    new BeanInfoOperator.PropertyValue(6, "\"description\""),
                    new BeanInfoOperator.PropertyValue(10, "FALSE"));            
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testMethodsNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("Methods", new BeanInfoOperator.PropertyValue(3, "FALSE"));
            
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testMethodNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");                        
            bio.setPropertyValues("method", new BeanInfoOperator.PropertyValue(2, "TRUE"),
                    new BeanInfoOperator.PropertyValue(3, "TRUE"),
                    new BeanInfoOperator.PropertyValue(4, "TRUE"),
                    new BeanInfoOperator.PropertyValue(5, "\"display\""),
                    new BeanInfoOperator.PropertyValue(6, "\"description\""));                    
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testMethodExcludeNode() {
        try {
            editor = openEditor("beans", "SourceBeanInfo");       
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("SourceBeanInfo");            
            bio.setPropertyValues("method", new BeanInfoOperator.PropertyValue(7, "FALSE"));            
            switchToSource(editor);
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            if(editor!=null) editor.closeDiscard();
        }                        
    }
    
    public void testCreateBeanInfo() {
        org.netbeans.jellytools.nodes.Node node = new org.netbeans.jellytools.nodes.Node(
                new ProjectsTabOperator().getProjectRootNode("Beans"),"Source Packages|Beans|Generate.java");
        node.performPopupAction("BeanInfo Editor...");
        JDialogOperator jDialogOperator = new JDialogOperator("Question");
        JButtonOperator okButon = new JButtonOperator(jDialogOperator,"Yes");
        okButon.push();
        try {
            editor = openEditor("beans", "GenerateBeanInfo");
            switchToDesign(editor);
            BeanInfoOperator bio = new BeanInfoOperator("GenerateBeanInfo");
            String tree = getTree(bio.getTreeOperator(), BeanInfoOperator.defaultConverter);
           String expected = ""
                    + "BeanInfo\n"
                    + "__Bean\n"
                    + "____beans.Source\n"
                    + "__Properties\n"
                    + "____x\n"
                    + "__Event Sources\n"
                    + "__Methods\n";
            assertEquals(tree, expected);
        } finally {
            if (editor != null) {
                editor.closeDiscard();
            }
        }        
    }
    
    public void testAddBeanInfo() {
        try {
            editor = openEditor("beans", "Add");        
            editor.insert("public void method() {};",5,1);
            org.netbeans.jellytools.nodes.Node node = new org.netbeans.jellytools.nodes.Node(
                new ProjectsTabOperator().getProjectRootNode("Beans"),"Source Packages|Beans|Add.java");
            node.performPopupAction("BeanInfo Editor...");
            BeanInfoOperator bio = new BeanInfoOperator("GenerateBeanInfo");
            String tree = getTree(bio.getTreeOperator(), BeanInfoOperator.defaultConverter);
            String expected = ""
                    + "BeanInfo\n"
                    + "__Bean\n"
                    + "____beans.Source\n"
                    + "__Properties\n"
                    + "__Event Sources\n"                    
                    + "__Methods\n"
                    + "____method\n";                    
            assertEquals(tree, expected);
            bio.selectNode("method");
            Property p = new Property(bio.getPropertySheetOperator(), 7);
            assertEquals("FALSE", p.getValue());
        } finally {
            if (editor != null) {
                editor.closeDiscard();
            }
        }
    }
    
    public void printAllComponents(Operator comp) {
        System.out.println("**************************");
        printComp((Component) comp.getSource(), "");
        System.out.println("**************************");
    }

    public void printComp(Component c, String s) {
        System.out.println(s + c.getClass().getName());
        if (c instanceof Container) {
            for (Component com : ((Container) c).getComponents()) {
                printComp((Container) com, s + "__");
            }
        }
    }

    private void browseTree(Object root, TreeModel model, String string) {
        Object node = Visualizer.findNode(root);
        if (node instanceof Node) {
            Node n = (Node) node;
            System.out.println(n.getDisplayName());
        }
        System.out.println(string + node.getClass().getName());
        int childCount = model.getChildCount(root);
        for (int i = 0; i < childCount; i++) {
            browseTree(model.getChild(root, i), model, string + "  ");
        }
    }

    
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(BeanInfoEditorTest.class)
                .enableModules(".*")
                .clusters(".*"));
    }


}
