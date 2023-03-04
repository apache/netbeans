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
package org.netbeans.qa.form.options;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.qa.form.OptionsForFormOperator;

/**
 * Automatic internationalization test
 *
 * @author Jiri Vagner
 *
 * <b>Adam Senk</b> 26 APRIL 2011 WORKS
 */
public class AutomaticInternationalizationTest extends ExtJellyTestCase {

    String name = "";
    public String PACKAGE_NAME = "data";
    public String DATA_PROJECT_NAME = "SampleProject";
    private Runnable r = new Runnable() {

        @Override
        public void run() {
            ComponentInspectorOperator inspector = new ComponentInspectorOperator();
            Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N

            //new Action(null, "Add From Palette|Swing Controls|Button").performPopup(node);

            runPopupOverNode("Add From Palette|Swing Controls|Button", node); // NOI18N

            //String baseName = "[JFrame]"; // NOI18N
            //Node dialogNode = new Node(inspector.treeComponents(), baseName);
            //String[] names = dialogNode.getChildren();

            inspector.selectComponent("[JFrame]|jButton1");
        }
    };

    /**
     * Constructor required by JUnit
     */
    public AutomaticInternationalizationTest(String testName) {
        super(testName);
    }

    /**
     * Steps which should be done before starting of test
     */
    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(AutomaticInternationalizationTest.class).addTest(
                "testAutomaticInternationalizationEnabled",
                "testAutomaticInternationalizationDisabled").clusters(".*").enableModules(".*").gui(true));
    }

    /**
     * Tests component code with properties Automatic Internationalization =
     * true
     */
    public void testAutomaticInternationalizationEnabled() {
        //testAutomaticInternationalization(true);
        OptionsForFormOperator.invoke();
        //add timeout
        waitNoEvent(1000);
        log("Option dialog was opened");

        OptionsForFormOperator options = new OptionsForFormOperator();


        //add timeout
        waitNoEvent(1000);

        options.selectJava();
        //add timeout
        waitNoEvent(1000);
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(options);
        jtpo.selectPage("GUI Builder");
        waitNoEvent(1000);
        JComboBoxOperator jcbo = new JComboBoxOperator(options, 3);

        jcbo.selectItem("On");
        options.ok();
        //add timeout
        waitNoEvent(2000);
        log("AutomaticResource Management was set");

        name = createJFrameFile();
        FormDesignerOperator designer = new FormDesignerOperator(name);
        designer.source();
        designer.design();


        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(r);

        inspector = new ComponentInspectorOperator();
        Property prop = new Property(inspector.properties(), "text"); // NOI18N
        prop.setValue("Lancia Lybra");
        log("text component of button was set");


        findInCode("jButton1.setText(bundle.getString(\"MyJFrame", designer);

        designer.design();
        //removeFile(name);

    }

    /**
     * Tests component code with properties Automatic Internationalization =
     * false
     */
    public void testAutomaticInternationalizationDisabled() {
        //testAutomaticInternationalization(false);
        OptionsForFormOperator.invoke();
        //add timeout
        waitNoEvent(1000);
        log("Option dialog was opened");

        OptionsForFormOperator options = new OptionsForFormOperator();

        //add timeout
        waitNoEvent(1000);

        options.selectJava();
        waitNoEvent(1000);
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(options);
        jtpo.selectPage("GUI Builder");
        waitNoEvent(1000);
        
        //add timeout
        waitNoEvent(2000);
        
        JComboBoxOperator jcbo = new JComboBoxOperator(options, 3);

        jcbo.selectItem("Off");

        //Property property = new Property(options.getPropertySheet("Miscellaneous|GUI Builder"), "Automatic Internationalization"); // NOI18N
        //property.setValue(String.valueOf( enabled ? "On" : "Off"));
        options.ok();
        //add timeout
        waitNoEvent(2000);
        log("AutomaticResource Management was set");

        name = createJFrameFile();

        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();

        Node formnode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + name);
        formnode.select();
        log("Form node selected.");

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);



        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(r);


        inspector = new ComponentInspectorOperator();
        Property prop = new Property(inspector.properties(), "text"); // NOI18N
        prop.setValue("Lancia Lybra");
        log("text component of button was set");
        FormDesignerOperator designer = new FormDesignerOperator(name);
        designer.source();
        designer.design();

        findInCode("jButton1.setText(\"Lancia Lybra\");", designer);

        //designer.design();
        //removeFile(name);

    }

    /**
     * Tests component code with different value of properties Automatic
     * Internationalization
     *
     * @param local "Automatic Internationalization" settings
     */
    private void testAutomaticInternationalization(Boolean enabled) {

        OptionsOperator.invoke();
        //add timeout
        waitNoEvent(1000);
        log("Option dialog was opened");

        OptionsOperator options = new OptionsOperator();


        //add timeout
        waitNoEvent(1000);

        options.selectJava();
        //add timeout
        waitNoEvent(2000);
        options.pushKey(KeyEvent.VK_TAB);
        waitNoEvent(500);
        options.pushKey(KeyEvent.VK_TAB);
        waitNoEvent(500);
        options.pushKey(KeyEvent.VK_TAB);
        waitNoEvent(500);
        options.pushKey(KeyEvent.VK_TAB);
        waitNoEvent(500);
        options.pushKey(KeyEvent.VK_TAB);
        waitNoEvent(500);
        options.pushKey(KeyEvent.VK_LEFT);
        waitNoEvent(500);
        options.pushKey(KeyEvent.VK_LEFT);
        waitNoEvent(500);
        options.pushKey(KeyEvent.VK_LEFT);
        waitNoEvent(500);
        options.pushKey(KeyEvent.VK_SPACE);
        waitNoEvent(500);
        JComboBoxOperator jcbo = new JComboBoxOperator(options, 4);
        if (enabled) {
            jcbo.selectItem("On");

        } else {
            jcbo.selectItem("Off");

        }
        //Property property = new Property(options.getPropertySheet("Miscellaneous|GUI Builder"), "Automatic Internationalization"); // NOI18N
        //property.setValue(String.valueOf( enabled ? "On" : "Off"));
        options.ok();
        //add timeout
        waitNoEvent(2000);
        log("AutomaticResource Management was set");

        name = createJFrameFile();
        FormDesignerOperator designer = new FormDesignerOperator(name);
        designer.source();
        designer.design();


        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N

                //new Action(null, "Add From Palette|Swing Controls|Button").performPopup(node);

                runPopupOverNode("Add From Palette|Swing Controls|Button", node); // NOI18N

                //String baseName = "[JFrame]"; // NOI18N
                //Node dialogNode = new Node(inspector.treeComponents(), baseName);
                //String[] names = dialogNode.getChildren();

                inspector.selectComponent("[JFrame]|jButton1");
            }
        });




        inspector = new ComponentInspectorOperator();
        Property prop = new Property(inspector.properties(), "text"); // NOI18N
        prop.setValue("Lancia Lybra");
        log("text component of button was set");

        if (enabled) {
            findInCode("jButton1.setText(bundle.getString(\"MyJFrame", designer);
        } else {
            findInCode("jButton1.setText(\"Lancia Lybra\");", designer);
        }
        designer.design();
        //removeFile(name);
    }
}
