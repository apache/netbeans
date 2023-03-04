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

import java.io.IOException;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import java.util.*;
import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 * Menu and popup menu tests from NetBeans 5.5.1 Form Test Specification
 * from  Visual Development Test Specification
 * @see <a href="http://qa.netbeans.org/modules/form/promo-f/testspecs/visualDevelopment.html">Test specification</a>
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class MenuAndPopupMenuTest extends ExtJellyTestCase {

    /** Constructor required by JUnit */
    public MenuAndPopupMenuTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws IOException {
        openDataProjects(_testProjectName);
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(MenuAndPopupMenuTest.class).addTest(
                "testMenuCreation",
                "testPopupMenuCreation").clusters(".*").enableModules(".*").gui(true));

    }

    public void testMenuCreation() {
        p("testMenuCreation");

        String frameName = createJFrameFile()+".java";
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode("SampleProject");
        prn.select();
        Node formnode = new Node(prn, "Source Packages|data|" + frameName);
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node node = new Node(inspector.treeComponents(), "JFrame");
                runNoBlockPopupOverNode("Add From Palette|Swing Menus|Menu Bar", node);

            }
        });

        //Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N

        Action openAction = new OpenAction();
        openAction.perform(formnode);
        designer = new FormDesignerOperator(frameName);
        //new Action(null, "Add From Palette|Swing Menus|Menu Bar").perform(node); // NOI18N
        findInCode("jMenuBar1 = new javax.swing.JMenuBar();", designer); // NOI18N



        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                String menuPalettePath = "Add From Palette|";

                ArrayList<String> items = new ArrayList<String>();
                items.add(menuPalettePath + "Menu Item"); // NOI18N
                items.add(menuPalettePath + "Menu Item / CheckBox"); // NOI18N
                items.add(menuPalettePath + "Menu Item / RadioButton"); // NOI18N
                items.add(menuPalettePath + "Separator"); // NOI18N
                items.add(menuPalettePath + "Menu"); // NOI18N

                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, false);

                Node node = new Node(inspector.treeComponents(), "[JFrame]|jMenuBar1 [JMenuBar]|jMenu1 [JMenu]"); // NOI18N
                runPopupOverNode(items, node, comparator);

                node = new Node(inspector.treeComponents(), "[JFrame]|jMenuBar1 [JMenuBar]|jMenu1 [JMenu]|jMenu3 [JMenu]"); // NOI18N
                runPopupOverNode(items, node, comparator);

            }
        });

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("jMenu3.add(jMenu4);"); // NOI18N
        lines.add("jMenu1.add(jMenu3);"); // NOI18N
        lines.add("jMenuBar1.add(jMenu1);"); // NOI18N
        lines.add("setJMenuBar(jMenuBar1);"); // NOI18N

        openAction = new OpenAction();
        openAction.perform(formnode);
        designer = new FormDesignerOperator(frameName);

        findInCode(lines, designer);
        closeOpenedProjects();
        //removeFile(frameName);
    }

    public void testPopupMenuCreation() throws IOException {
        openDataProjects(_testProjectName);
        p("testPopupMenuCreation");


        String frameName = createJFrameFile()+".java";
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode("SampleProject");
        prn.select();
        Node formnode = new Node(prn, "Source Packages|data|" + frameName);
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                String menuPalettePath = "Add From Palette|Swing Menus|";
                Node node = new Node(inspector.treeComponents(), "Other Components"); // NOI18N

                runPopupOverNode(menuPalettePath + "Popup Menu", node);

            }
        });

        Action openAction = new OpenAction();
        openAction.perform(formnode);
        designer = new FormDesignerOperator(frameName);
        findInCode("jPopupMenu1 = new javax.swing.JPopupMenu();", designer); // NOI18N

        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                String popupMenuPalettePath = "Add From Palette|";
                ArrayList<String> items = new ArrayList<String>();
                items.add(popupMenuPalettePath + "Menu Item"); // NOI18N
                items.add(popupMenuPalettePath + "Menu Item / CheckBox"); // NOI18N
                items.add(popupMenuPalettePath + "Menu Item / RadioButton"); // NOI18N
                items.add(popupMenuPalettePath + "Separator"); // NOI18N
                items.add(popupMenuPalettePath + "Menu"); // NOI18N

                Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, false);
                Node node = new Node(inspector.treeComponents(), "Other Components|jPopupMenu1 [JPopupMenu]"); // NOI18N

                runPopupOverNode(items, node, comparator);

            }
        });

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("jPopupMenu1.add(jMenuItem1);"); // NOI18N
        lines.add("jPopupMenu1.add(jCheckBoxMenuItem1)"); // NOI18N
        lines.add("jPopupMenu1.add(jRadioButtonMenuItem1);");
        lines.add("jPopupMenu1.add(jSeparator1);"); // NOI18N
        lines.add("jPopupMenu1.add(jMenu1);"); // NOI18N

        openAction = new OpenAction();
        openAction.perform(formnode);
        designer = new FormDesignerOperator(frameName);

        findInCode(lines, designer);

        //removeFile(frameName);

    }
}
