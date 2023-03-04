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
package org.netbeans.qa.form.beans;

import java.awt.Component;
import junit.framework.Test;
import org.netbeans.qa.form.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.jellytools.modules.form.ComponentPaletteOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests adding and removing beans into/from palette
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class AddAndRemoveBeansTest extends ExtJellyTestCase {

    public static String VISUAL_BEAN_NAME = "TestVisualBean"; // NOI18N
    public static String NONVISUAL_BEAN_NAME = "TestNonVisualBean"; // NOI18N
    public static String TESTED_BEAN_TEXT = "Lancia Lybra"; // NOI18N
    public static String TESTED_BEAN_POWER = "140"; // NOI18N

    /**
     * Constructor required by JUnit
     */
    public AddAndRemoveBeansTest(String testName) {
        super(testName);
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(AddAndRemoveBeansTest.class).addTest(
                "testAddingBeans", 
                "testRemovingBeans"
                ).gui(true).clusters(".*").enableModules(".*"));

    }

    /**
     *  Tests "Add Bean" dialog
     */
    public void testAddingBeans() {
        addBean(VISUAL_BEAN_NAME + ".java"); // NOI18N
        addBean(NONVISUAL_BEAN_NAME + ".java"); // NOI18N
    }

    /**
     * Tests removing bean using Palette Manager
     */
    public void testRemovingBeans() {
        Action ac = new Action("Tools|Palette|Swing/AWT Components", null);
        ac.setComparator(new DefaultStringComparator(true, true));
        ac.perform();

        PaletteManagerOperator manOp = new PaletteManagerOperator();
        JTreeOperator treeOp = manOp.treePaletteContentsTree();

        treeOp.clickOnPath(treeOp.findPath("Beans|" + VISUAL_BEAN_NAME, "|")); // NOI18N
        manOp.remove();
        new NbDialogOperator("Confirm").yes(); // NOI18N

        treeOp.clickOnPath(treeOp.findPath("Beans|" + NONVISUAL_BEAN_NAME, "|")); // NOI18N
        manOp.remove();
        new NbDialogOperator("Confirm").yes(); // NOI18N

        manOp.close();
    }

    /**
     * Tests removing beans using popup menu from palette
     */
    public void testRemovingBeansFromPalette() {
        openFile("clear_Frame.java");

        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        palette.expandBeans();
        palette.collapseSwingContainers();
        palette.collapseSwingMenus();
        palette.collapseSwingWindows();
        palette.collapseAWT();
        palette.collapseSwingControls();

        JListOperator list = palette.lstComponents();
        list.clickOnItem(NONVISUAL_BEAN_NAME, new Operator.DefaultStringComparator(true, false));

        // TODO: I'm not able to invoke popup menu :(
        int i = list.findItemIndex(NONVISUAL_BEAN_NAME, new Operator.DefaultStringComparator(true, false));
        p(i);

        Component[] comps = list.getComponents();
        p(comps.length);
        for (Component comp : comps) {
            p(comp.toString());
        }
    }
}