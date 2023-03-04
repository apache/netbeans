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
package org.netbeans.qa.form.visualDevelopment;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.JPanel;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.ComponentPaletteOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import junit.framework.Test;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.BorderCustomEditorOperator;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 * Tests from NetBeans 5.5.1 Form Test Specification
 * from  Visual Development Test Specification
 * @see <a href="http://qa.netbeans.org/modules/form/promo-f/testspecs/visualDevelopment.html">Test specification</a>
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class FormGenerateCodeTest extends ExtJellyTestCase {

    String frameName;
    String layoutCmd;
    Map.Entry<String, String> entryGlob;

    /** Constructor required by JUnit */
    public FormGenerateCodeTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws IOException {
        openDataProjects(_testProjectName);
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(FormGenerateCodeTest.class).addTest(
                "testAWTAndSwingComponentsTogether",
                "testAddComponentsIntoContainersSwing",
                "testAddComponentsIntoContainersAwt",
                "testInPlaceEditing",
                "testSimpleComponentInsertingIntoForm",
                "testLayouts",
                "testBorderSettings").clusters(".*").enableModules(".*").gui(true));

    }

    public void testSimpleComponentInsertingIntoForm() {
        p("testSimpleComponentInsertingIntoForm - start"); // NOI18N
        String frameName = createJFrameFile()+".java";
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();

        palette.expandSwingContainers();
        palette.selectComponent("Panel"); // NOI18N
        designer.clickOnComponent(designer.fakePane().getSource());

        inspector.selectComponent("[JFrame]|jPanel1 [JPanel]"); // NOI18N
        new Property(inspector.properties(), "background").setValue("[0,255,0]"); // NOI18N

        Component firstPanel = designer.findComponent(JPanel.class);
        palette.expandSwingControls();
        palette.selectComponent("Label"); // NOI18N
        designer.clickOnComponent(firstPanel);

        findInCode("jPanel1.setBackground(new java.awt.Color(0, 255, 0));", designer); // NOI18N
        findInCode("jLabel1 = new javax.swing.JLabel();", designer); // NOI18N

        //removeFile(frameName);
    }

    public void testLayouts() {
        p("testLayouts - start"); // NOI18N
         // NOI18N
        String name = createJFrameFile()+".java";

        FormDesignerOperator designer = new FormDesignerOperator(name);
        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();


        HashMap<String, String> task = new HashMap<String, String>();
        task.put("Set Layout|Absolute Layout", "setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());"); // NOI18N
        task.put("Set Layout|Box Layout", "setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));"); // NOI18N
        task.put("Set Layout|Card Layout", "setLayout(new java.awt.CardLayout());"); // NOI18N
        task.put("Set Layout|Flow Layout", "setLayout(new java.awt.FlowLayout());"); // NOI18N
        task.put("Set Layout|Grid Bag Layout", "setLayout(new java.awt.GridBagLayout());"); // NOI18N
        task.put("Set Layout|Grid Layout", "setLayout(new java.awt.GridLayout());"); // NOI18N
        task.put("Set Layout|Null Layout", "setLayout(null);"); // NOI18N
        task.put("Set Layout|Free Design", ".GroupLayout(getContentPane());"); // NOI18N
        task.put("Set Layout|Border Layout", "setLayout("); //setLayout("); // NOI18N

        for (Map.Entry<String, String> entry : task.entrySet()) {
            inspector = new ComponentInspectorOperator();
            entryGlob = entry;
            inspector.freezeNavigatorAndRun(new Runnable() {

                @Override
                public void run() {
                    String nodeName = "JFrame";
                    ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                    layoutCmd = entryGlob.getKey();
                    Node frameNode = new Node(inspector.treeComponents(), nodeName);
                    runPopupOverNode(layoutCmd, frameNode);
                }
            });

            designer = new FormDesignerOperator(name);
            if (!layoutCmd.equals("Set Layout|Border Layout")) // NOI18N
            {
                findInCode(entry.getValue(), designer);
            } else {
                missInCode(entry.getValue(), designer);
            }
        }
        //removeFile(name);
    }

    public void testAWTAndSwingComponentsTogether() {
        p("testAWTAndSwingComponentsTogether - start"); // NOI18N

        String name = createJFrameFile()+".java";


        FormDesignerOperator designer = new FormDesignerOperator(name);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                String nodeName = "JFrame"; // NOI18N
                ArrayList<String> cmds = new ArrayList<String>();
                cmds.add("Add From Palette|Swing Controls|Label"); // NOI18N
                cmds.add("Add From Palette|AWT|Label"); // NOI18N
                Node node = new Node(inspector.treeComponents(), nodeName);

                runPopupOverNode(cmds, node);
            }
        });


        findInCode("jLabel1 = new javax.swing.JLabel();", designer); // NOI18N
        findInCode("label1 = new java.awt.Label()", designer); // NOI18N

       // removeFile(name);
    }

    public void testAddComponentsIntoContainersSwing() {
        p("testAddComponentsIntoContainersSwing - start"); // NOI18N
        frameName = createJFrameFile()+".java";
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();

        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node frameNode = new Node(inspector.treeComponents(), "JFrame"); // NOI18N

                ArrayList<String> containers = new ArrayList<String>();
                containers.add("Add From Palette|Swing Containers|Panel"); // NOI18N
                containers.add("Add From Palette|Swing Containers|Tabbed Pane"); // NOI18N
                containers.add("Add From Palette|Swing Containers|Scroll Pane"); // NOI18N
                containers.add("Add From Palette|Swing Containers|Split Pane"); // NOI18N
                containers.add("Add From Palette|Swing Containers|Tool Bar"); // NOI18N
                containers.add("Add From Palette|Swing Containers|Internal Frame"); // NOI18N
                containers.add("Add From Palette|Swing Containers|Desktop Pane"); // NOI18N
                runPopupOverNode(containers, frameNode);
            }
        });


        ArrayList<String> lines = new ArrayList<String>();
        lines.add("jPanel1 = new javax.swing.JPanel();"); // NOI18N
        lines.add("jTabbedPane1 = new javax.swing.JTabbedPane();"); // NOI18N
        lines.add("jScrollPane1 = new javax.swing.JScrollPane();"); // NOI18N
        lines.add("jSplitPane1 = new javax.swing.JSplitPane();"); // NOI18N
        lines.add("jToolBar1 = new javax.swing.JToolBar();"); // NOI18N
        lines.add("jInternalFrame1 = new javax.swing.JInternalFrame();"); // NOI18N
        lines.add("jDesktopPane1 = new javax.swing.JDesktopPane();"); // NOI18N
        designer = new FormDesignerOperator(frameName);
        findInCode(lines, designer);

        inspector = new ComponentInspectorOperator();

        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node node = new Node(inspector.treeComponents(), "[JFrame]|jPanel1 [JPanel]"); // NOI18N
                runPopupOverNode("Add From Palette|Swing Controls|Text Area", node); // NOI18N
            }
        });
        findInCode("jScrollPane2.setViewportView(jTextArea1);", designer); // NOI18N

        inspector = new ComponentInspectorOperator();

        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node node = new Node(inspector.treeComponents(), "[JFrame]|jTabbedPane1 [JTabbedPane]"); // NOI18N
                runPopupOverNode("Add From Palette|Swing Containers|Panel", node); // NOI18N
            }
        });
        designer = new FormDesignerOperator(frameName);
        findInCode("jTabbedPane1.addTab(\"tab1\", jPanel2);", designer); // NOI18N

        inspector = new ComponentInspectorOperator();

        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node node = new Node(inspector.treeComponents(), "[JFrame]|jScrollPane1 [JScrollPane]"); // NOI18N
                runPopupOverNode("Add From Palette|Swing Controls|Table", node); // NOI18N
            }
        });
        designer = new FormDesignerOperator(frameName);
        findInCode("jScrollPane1.setViewportView(jTable1);", designer); // NOI18N

        inspector = new ComponentInspectorOperator();

        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node node = new Node(inspector.treeComponents(), "[JFrame]|jSplitPane1 [JSplitPane]"); // NOI18N
                runPopupOverNode("Add From Palette|Swing Controls|Button", node); // NOI18N
                runPopupOverNode("Add From Palette|Swing Containers|Panel", node); // NOI18N
            }
        });

        designer = new FormDesignerOperator(frameName);
        findInCode("jSplitPane1.setRightComponent(jPanel3);", designer); // NOI18N
        findInCode("jSplitPane1.setLeftComponent(jButton1);", designer); // NOI18N

        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node node = new Node(inspector.treeComponents(), "[JFrame]|jToolBar1 [JToolBar]"); // NOI18N
                runPopupOverNode("Add From Palette|Swing Controls|Toggle Button", node); // NOI18N
            }
        });

        designer = new FormDesignerOperator(frameName);
        findInCode("jToolBar1.add(jToggleButton1);", designer); // NOI18N

        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node node = new Node(inspector.treeComponents(), "[JFrame]|jInternalFrame1 [JInternalFrame]"); // NOI18N
                runPopupOverNode("Add From Palette|Swing Controls|Tree", node); // NOI18N
            }
        });

        designer = new FormDesignerOperator(frameName);
        findInCode("jScrollPane3.setViewportView(jTree1);", designer); // NOI18N

        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node node = new Node(inspector.treeComponents(), "[JFrame]|jDesktopPane1 [JDesktopPane]"); // NOI18N
                runPopupOverNode("Add From Palette|Swing Containers|Internal Frame", node); // NOI18N
            }
        });

        designer = new FormDesignerOperator(frameName);
        findInCode("jDesktopPane1.add(jInternalFrame2, javax.swing.JLayeredPane.DEFAULT_LAYER);", designer); // NOI18N

        //removeFile(frameName);
    }

    public void testAddComponentsIntoContainersAwt() {
        p("testAddComponentsIntoContainersAwt - start"); // NOI18N
        frameName = createFrameFile();
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node frameNode = new Node(inspector.treeComponents(), "Frame"); // NOI18N


                ArrayList<String> containers = new ArrayList<String>();
                containers.add("Add From Palette|AWT|Panel"); // NOI18N
                containers.add("Add From Palette|AWT|Scroll Pane"); // NOI18N
                runPopupOverNode(containers, frameNode);
            }
        });

        designer = new FormDesignerOperator(frameName);

        findInCode("scrollPane1 = new java.awt.ScrollPane();", designer); // NOI18N
        findInCode("panel1 = new java.awt.Panel();", designer); // NOI18N

        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node node = new Node(inspector.treeComponents(), "[Frame]|scrollPane1 [ScrollPane]"); // NOI18N
                runPopupOverNode("Add From Palette|AWT|Text Area", node); // NOI18N
            }
        });

        designer = new FormDesignerOperator(frameName);
        findInCode("scrollPane1.add(textArea1);", designer); // NOI18N

        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                Node node = new Node(inspector.treeComponents(), "[Frame]|panel1 [Panel]"); // NOI18N
                runPopupOverNode("Add From Palette|AWT|Canvas", node); // NOI18N
            }
        });

        designer = new FormDesignerOperator(frameName);
        findInCode("panel1.add(canvas1);", designer); // NOI18N

        //removeFile(frameName);
    }

    public void testInPlaceEditing() {
        p("testInPlaceEditing - start"); // NOI18N
        String testText = "xyz"; // NOI18N
        String dialogName = createJDialogFile();

        HashMap<String, Class> components = new HashMap<String, Class>();
        // TODO: strange, findComponent is not able to find JToggleButton.class
        //components.put("Toggle Button", JToggleButton.class);
        components.put("Label", JLabel.class); // NOI18N
        components.put(
                "Button", JButton.class); // NOI18N
        components.put(
                "Check Box", JCheckBox.class); // NOI18N
        components.put(
                "Radio Button", JRadioButton.class); // NOI18N

        FormDesignerOperator designer = new FormDesignerOperator(dialogName);
        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();

        palette.expandSwingControls();

        for (Map.Entry<String, Class> entry : components.entrySet()) {
            palette.selectComponent(entry.getKey());
            designer.clickOnComponent(designer.fakePane().getSource());

            Class componentClass = entry.getValue();
            java.awt.Component component = designer.findComponent(componentClass);

            designer.clickOnComponent(component);
            waitNoEvent(300);
            designer.handleLayer().pushKey(KeyEvent.VK_SPACE);
            waitNoEvent(300);
            new JTextFieldOperator(designer).typeText(testText);
            waitNoEvent(300);
            designer.handleLayer().pushKey(KeyEvent.VK_ENTER);
            waitNoEvent(300);
            designer.handleLayer().pushKey(KeyEvent.VK_ESCAPE);
        }
        String baseName = "[JDialog]"; // NOI18N
        Node dialogNode = new Node(inspector.treeComponents(), baseName);
        String[] names = dialogNode.getChildren();

        for (String name : names) {
            inspector.selectComponent(baseName + "|" + name);
            Property prop = new Property(inspector.properties(), "text"); // NOI18N
            assertEquals("Text property of component " + name + " was not set correctly.", prop.getValue(), testText); // NOI18N
        }

        //removeFile(dialogName);
    }

    public void testBorderSettings() {
        p("testBorderSettings - start"); // NOI18N
        String dialogName = createJFrameFile()+".java";

        HashMap<String, Class> components = new HashMap<String, Class>();
        components.put("Button", JButton.class); // NOI18N
        components.put("Label", JLabel.class); // NOI18N
        components.put("Radio Button", JRadioButton.class); // NOI18N
        components.put("Toggle Button", JToggleButton.class); // NOI18N
        components.put("Text Area", JTextArea.class); // NOI18N

        FormDesignerOperator designer = new FormDesignerOperator(dialogName);
        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();

        palette.expandSwingControls();

        for (Map.Entry<String, Class> entry : components.entrySet()) {
            String componentName = entry.getKey();
            palette.selectComponent(componentName);
            designer.clickOnComponent(designer.fakePane().getSource());
        }

        String baseName = "[JFrame]"; // NOI18N
        Node dialogNode = new Node(inspector.treeComponents(), baseName);
        String[] names = dialogNode.getChildren();

        int counter = 0;
        for (String name : names) {
            String path = baseName + "|" + name;
            inspector.selectComponent(path);

            Property prop = new Property(inspector.properties(), "border"); // NOI18N
            prop.openEditor();

            BorderCustomEditorOperator editor = new BorderCustomEditorOperator(name);
            JListOperator lstOp = editor.lstAvailableBorders();
            lstOp.clickOnItem(++counter, 1);
            editor.ok();
        }

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("jToggleButton1.setBorder(javax.swing.BorderFactory.");
        lines.add("jLabel1.setBorder(javax.swing.BorderFactory.");
        lines.add("jButton1.setBorder(javax.swing.BorderFactory.");
        findInCode(lines, designer);

        //removeFile(dialogName);
    }
}
