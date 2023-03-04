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
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.qa.form.OptionsForFormOperator;

/**
 * Componentes declaration test
 *
 * @author Jiri Vagner
 *
 * <b>Adam Senk</b> 26 APRIL 2011 WORKS
 */
public class GeneratedComponentsDestionationTest extends ExtJellyTestCase {

    /**
     * Constructor required by JUnit
     */
    public GeneratedComponentsDestionationTest(String testName) {
        super(testName);
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(GeneratedComponentsDestionationTest.class).addTest(
                "testGeneratedComponentsDestionationLocal",
                "testGeneratedComponentsDestionationClassField").clusters(".*").enableModules(".*").gui(true));
    }

    /**
     * Tests generation component declaration code with properties
     * LocalVariables=true Test for issue 95518
     */
    public void testGeneratedComponentsDestionationLocal() {
        testGeneratedComponentsDestionation(true);
    }

    /**
     * Tests generation component declaration code with properties
     * LocalVariables=false
     */
    public void testGeneratedComponentsDestionationClassField() {
        testGeneratedComponentsDestionation(false);
    }

    /**
     * Tests generation component declaration code with properties
     * LocalVariables=false
     *
     * @param local "Local Variables" settings
     */
    private void testGeneratedComponentsDestionation(Boolean local) {
        OptionsForFormOperator.invoke();
        //add timeout
        waitNoEvent(1000);
        log("Option dialog was opened");

        OptionsForFormOperator options = new OptionsForFormOperator();


        //add timeout
        waitNoEvent(1000);
        if (local) {
            options.selectJava();
            //add timeout
            waitNoEvent(1000);
            JTabbedPaneOperator jtpo = new JTabbedPaneOperator(options);
            jtpo.selectPage("GUI Builder");
            waitNoEvent(1000);
        }
        waitNoEvent(500);

        JRadioButtonOperator jrbo = new JRadioButtonOperator(options, "Local Variables in initComponents() Method");
        //int i = 0;
        if (!local) {
            // i = 1;
            jrbo = new JRadioButtonOperator(options, "Fields in the Form Class");
        }

        //new JRadioButtonOperator(options, i);
        jrbo.setSelected(true);

        waitNoEvent(1000);
        options.ok();
        waitAMoment();

        String name = createJFrameFile();
        waitAMoment();

        FormDesignerOperator designer = new FormDesignerOperator(name);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N

                runPopupOverNode("Add From Palette|Swing Controls|Label", node); // NOI18N
            }
        });

        waitAMoment();

        String code = "private javax.swing.JLabel jLabel1";  // NOI18N
        if (local) {
            missInCode(code, designer);
        } else {
            findInCode(code, designer);
        }

        waitAMoment();
        removeFile(name);
    }
}
