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
package org.netbeans.qa.form.binding;

import junit.framework.Test;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.BindDialogOperator;

/**
 * Beans Binding advanced tests
 *
 * @author Jiri Vagner
 *
 * <b>Adam Senk</b>
 *
 */
public class AdvancedBeansBindingTest extends ExtJellyTestCase {

    private String ACTION_PATH = "Bind|text";  // NOI18N
    private String BIND_EXPRESSION = "${text}";  // NOI18N
    private String FILENAME = "ConvertorAndValidatorTest.java"; // NOI18N
    private String VALIDATOR_NAME = "loginLengthValidator";  // NOI18N
    private String CONVERTOR_NAME = "bool2FaceConverter";  // NOI18N    
    private String jLabelNameGlobal = "";
    private String selectedConvertor = "";

    /**
     * Constructor required by JUnit
     */
    public AdvancedBeansBindingTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        //TODO "testUpdateMode"
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(AdvancedBeansBindingTest.class).addTest(
                "testCompileComponents",
                "testUpdateMode",
                "testConversion",
                "testValidation",
                "testAlternateValues").gui(true).enableModules(".*").clusters(".*"));

    }

    /**
     * Form component classes compilation
     */
    public void testCompileComponents() {
        Node beanNode = openFile(CONVERTOR_NAME);
        CompileJavaAction action = new CompileJavaAction();
        action.perform(beanNode);

        beanNode = openFile(VALIDATOR_NAME);
        action = new CompileJavaAction();
        action.perform(beanNode);
    }

    /**
     * Tests different update modes
     */
    public void testUpdateMode() {
        // open frame
        openFile(FILENAME);

        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                selectUpdateModeForJLabel(inspector, "jLabel2", BindDialogOperator.READ_ONCE_UPDATE_MODE); // NOI18N

                selectUpdateModeForJLabel(inspector, "jLabel4", BindDialogOperator.READ_ONLY_UPDATE_MODE); // NOI18N

                selectUpdateModeForJLabel(inspector, "jLabel6", BindDialogOperator.READ_ONLY_UPDATE_MODE); // NOI18N

                selectUpdateModeForJLabel(inspector, "jLabel6", BindDialogOperator.READ_WRITE_UPDATE_MODE); // NOI18N


                // find generated code
                //findInCode("setUpdateStrategy(javax.beans.binding.Binding.UpdateStrategy.READ_ONCE)",designer); // NOI18N
                //findInCode("setUpdateStrategy(javax.beans.binding.Binding.UpdateStrategy.READ_FROM_SOURCE)", designer); // NOI18N

                // test values in bind dialog
                assertTrue(getSelectedUpdateModeForJLabel(inspector, "jLabel2").contains(BindDialogOperator.READ_ONCE_UPDATE_MODE)); // NOI18N


                assertTrue(getSelectedUpdateModeForJLabel(inspector, "jLabel4").contains(BindDialogOperator.READ_ONLY_UPDATE_MODE)); // NOI18N


                assertTrue(getSelectedUpdateModeForJLabel(inspector, "jLabel6").contains(BindDialogOperator.READ_WRITE_UPDATE_MODE));
            }
        });
        //FormDesignerOperator designer = new FormDesignerOperator(FILENAME);
        //designer.source();
        //designer.design();
        // select update modes for jlabels
        // NOI18N
    }

    /**
     * Tests alternate values
     */
    public void testAlternateValues() {

        String nullLabelPath = "[JFrame]|jLabel7 [JLabel]"; // NOI18N
        String incompleteLabelPath = "[JFrame]|jLabel8 [JLabel]"; // NOI18N
        String nullMsg = "null foo msg"; // NOI18N
        String incompleteMsg = "incomplete foo msg";   // NOI18N      

        // open frame
        openFile(FILENAME);
        FormDesignerOperator designer = new FormDesignerOperator(FILENAME);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                String nullLabelPath = "[JFrame]|jLabel7 [JLabel]";
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node actNode = new Node(inspector.treeComponents(), nullLabelPath);
                actNode.select();
                actNode = new Node(inspector.treeComponents(), nullLabelPath);
                actNode.performPopupActionNoBlock(ACTION_PATH);
            }
        });
        // invoke bind dialog



        JDialogOperator bindOp = new JDialogOperator("Bind");  // NOI18N
        JTabbedPaneOperator tabOp = new JTabbedPaneOperator(bindOp);
        tabOp.selectPage("Advanced");

        // null checkbox
        JCheckBoxOperator checkBoxOp = new JCheckBoxOperator(tabOp, 0);
        checkBoxOp.changeSelection(true);

        // incomplet path checkbox
        checkBoxOp = new JCheckBoxOperator(tabOp, 1);
        checkBoxOp.changeSelection(true);

        // incomplete value settings
        new JButtonOperator(tabOp, 6).pushNoBlock();
        NbDialogOperator valueOp = new NbDialogOperator("Incomplete Path Value");  // NOI18N
        new JTextAreaOperator(valueOp, 0).setText(incompleteMsg);
        new JButtonOperator(valueOp, "OK").push();  // NOI18N

        // null value settings
        new JButtonOperator(tabOp, 7).pushNoBlock();
        valueOp = new NbDialogOperator("Null Value");  // NOI18N
        new JTextAreaOperator(valueOp, 0).setText(nullMsg);
        new JButtonOperator(valueOp, "OK").push();  // NOI18N

        // closing bind dialog
        new JButtonOperator(bindOp, "OK").push();  // NOI18N

        // test generated code
        findInCode("binding.setSourceNullValue(\"" + nullMsg + "\");", designer); // NOI18N
        findInCode("binding.setSourceUnreadableValue(\"" + incompleteMsg + "\");", designer); // NOI18N
        openFile(FILENAME);
        designer.source();
        designer.design();
        openFile(FILENAME);
        // invoke bind dialog again and check values

        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                String nullLabelPath = "[JFrame]|jLabel7 [JLabel]";
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node actNode = new Node(inspector.treeComponents(), nullLabelPath);
                //actNode.select();
                actNode.performPopupActionNoBlock(ACTION_PATH);
            }
        });


        bindOp = new JDialogOperator("Bind");  // NOI18N
        tabOp = new JTabbedPaneOperator(bindOp);
        tabOp.selectPage("Advanced");  // NOI18N

        // get incomplete path value
        new JButtonOperator(tabOp, 6).pushNoBlock();
        valueOp = new NbDialogOperator("Incomplete Path Value");  // NOI18N
        String incomleteValue = new JTextAreaOperator(valueOp, 0).getText();
        new JButtonOperator(valueOp, "OK").push();  // NOI18N

        // get null value
        new JButtonOperator(tabOp, 7).pushNoBlock();
        valueOp = new NbDialogOperator("Null Value");  // NOI18N
        String nullValue = new JTextAreaOperator(valueOp, 0).getText();
        new JButtonOperator(valueOp, "OK").push();  // NOI18N

        // closing bind dialog
        new JButtonOperator(bindOp, "OK").push();  // NOI18N

        // compare values
        assertEquals(incomleteValue, incompleteMsg);
        assertEquals(nullValue, nullMsg);
    }

    /**
     * Tests validation
     */
    public void testValidation() throws InterruptedException {

        // open frame
        ProjectRootNode prn;
        ProjectsTabOperator pto;


        openFile(FILENAME);
        FormDesignerOperator designer = new FormDesignerOperator(FILENAME);
        designer.source();
        designer.design();
        openFile(FILENAME);

        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node actNode = new Node(inspector.treeComponents(), "[JFrame]"); // NOI18N
                actNode.expand();
                inspector = new ComponentInspectorOperator();
                actNode = new Node(inspector.treeComponents(), "[JFrame]|jLabel12 [JLabel]");
                actNode.performPopupActionNoBlock(ACTION_PATH);
            }
        });

        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        bindOp.selectValidator(VALIDATOR_NAME);
        bindOp.ok();

        //new JButtonOperator(tabOp, 4).pushNoBlock();
        //NbDialogOperator valOp = new NbDialogOperator("Validator");  // NOI18N
        //JComboBoxOperator jcbOp = new JComboBoxOperator(valOp,0);
        //jcbOp.selectItem(2);
        //JTextFieldOperator jtfOp= new JTextFieldOperator(valOp, 0);
        //jtfOp.setText(VALIDATOR_NAME);
        //new JButtonOperator(valOp, "OK").push();  // NOI18N
        //new JButtonOperator(bindOp, "OK").push();  // NOI18N
        openFile(FILENAME);
        designer = new FormDesignerOperator(FILENAME);
        findInCode("binding.setValidator(" + VALIDATOR_NAME + ");", designer);  // NOI18N
        designer.source();
        designer.design();
        openFile(FILENAME);
        designer = new FormDesignerOperator(FILENAME);
        designer.source();
        designer.design();
        openFile(FILENAME);

        inspector = new ComponentInspectorOperator();
        // open bind dialog again and check selected
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node actNode = new Node(inspector.treeComponents(), "[JFrame]|jLabel12 [JLabel]"); // NOI18N
                // actNode.select();
                actNode.performPopupActionNoBlock(ACTION_PATH);
                //runNoBlockPopupOverNode(ACTION_PATH, actNode);
            }
        });

        BindDialogOperator bindOp1 = new BindDialogOperator();
        bindOp1.selectAdvancedTab();
        String selected = bindOp1.getValidator();
        bindOp1.ok();

        // test name
        assertEquals(selected, VALIDATOR_NAME);
    }

    /**
     * Tests conversion
     */
    public void testConversion() {
        // open frame
        openFile(FILENAME);
        FormDesignerOperator designer = new FormDesignerOperator(FILENAME);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();


        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                String jLabelPath = "[JFrame]|jLabel9 [JLabel]";  // NOI18N
                // test value before using convertor
                assertEquals(ExtJellyTestCase.getTextValueOfLabel(inspector, jLabelPath), Boolean.FALSE.toString());


                openFile(FILENAME);
                FormDesignerOperator designer = new FormDesignerOperator(FILENAME);
                designer.source();
                designer.design();
                openFile(FILENAME);
                inspector = new ComponentInspectorOperator();
                inspector.freezeNavigatorAndRun(new Runnable() {

                    @Override
                    public void run() {
                        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                        String jLabelPath = "[JFrame]|jLabel9 [JLabel]";
                        Node actNode = new Node(inspector.treeComponents(), jLabelPath);
                        actNode.select();
                        actNode.performPopupActionNoBlock(ACTION_PATH);

                    }
                });

                BindDialogOperator bindOp = new BindDialogOperator();
                bindOp.selectAdvancedTab();
                bindOp.selectConverter(CONVERTOR_NAME);
                bindOp.ok();

                // find code in source file
                findInCode("binding.setConverter(" + CONVERTOR_NAME + ");", designer);  // NOI18N

                designer.source();
                designer.design();
                openFile(FILENAME);
                inspector = new ComponentInspectorOperator();
                inspector.freezeNavigatorAndRun(new Runnable() {

                    @Override
                    public void run() {
                        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                        String jLabelPath = "[JFrame]|jLabel9 [JLabel]";
                        // test value after using convertor
                        Node actNode = new Node(inspector.treeComponents(), jLabelPath);
                        actNode.select();
                        actNode.performPopupActionNoBlock("Properties");
                        //runNoBlockPopupOverNode("Properties", actNode);
                        NbDialogOperator dialogOp = new NbDialogOperator("[JLabel]");  // NOI18N
                        Property prop = new Property(new PropertySheetOperator(dialogOp), "text");  // NOI18N
                        String result = prop.getValue();

                        // close property dialog
                        new JButtonOperator(dialogOp, "Close").push();
                        assertEquals(result, ":(");  // NOI18N
                    }
                });

                designer.source();
                designer.design();
                openFile(FILENAME);
                inspector = new ComponentInspectorOperator();
                inspector.freezeNavigatorAndRun(new Runnable() {

                    @Override
                    public void run() {
                        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                        String jLabelPath = "[JFrame]|jLabel9 [JLabel]";
                        // open bind dialog again and check selected convertor


                        Node actNode = new Node(inspector.treeComponents(), jLabelPath);
                        actNode.select();
                        actNode.performPopupActionNoBlock(ACTION_PATH);

                    }
                });
                bindOp = new BindDialogOperator();
                bindOp.selectAdvancedTab();
                selectedConvertor = bindOp.getSelectedConverter();
                bindOp.ok();
                assertEquals(selectedConvertor, CONVERTOR_NAME);
                designer.source();
                designer.design();
                openFile(FILENAME);
                inspector = new ComponentInspectorOperator();

                inspector.freezeNavigatorAndRun(new Runnable() {

                    @Override
                    public void run() {
                        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                        String jLabelPath = "[JFrame]|jLabel9 [JLabel]";

                        // test convertor name

                        Node actNode = new Node(inspector.treeComponents(), jLabelPath);
                        actNode.select();
                        actNode.performPopupActionNoBlock(ACTION_PATH);
                    }
                });
                bindOp = new BindDialogOperator();
                bindOp.selectAdvancedTab();

                new JButtonOperator(bindOp.tbdPane(), 2).pushNoBlock();
                JDialogOperator dialog = new JDialogOperator("Converter");  // NOI18N
                new JComboBoxOperator(dialog, 0).selectItem(2);

                JEditorPaneOperator textOp = new JEditorPaneOperator(dialog, 0);
                textOp.clearText();
                textOp.typeText("new Bool2FaceConverter()");  // NOI18N

                new JButtonOperator(dialog, "OK").push();  // NOI18N

                bindOp = new BindDialogOperator();
                bindOp.ok();

                // find custom code in form code
                findInCode("binding.setConverter(new Bool2FaceConverter());", designer);  // NOI18N

                // open bind dialog again and check custom code value
                designer.source();
                designer.design();
                openFile(FILENAME);
                inspector = new ComponentInspectorOperator();

                inspector.freezeNavigatorAndRun(new Runnable() {

                    @Override
                    public void run() {
                        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                        String jLabelPath = "[JFrame]|jLabel9 [JLabel]";

                        Node actNode = new Node(inspector.treeComponents(), jLabelPath);
                        actNode.select();
                        actNode.performPopupActionNoBlock(ACTION_PATH);
                    }
                });
                bindOp = new BindDialogOperator();
                bindOp.selectAdvancedTab();

                new JButtonOperator(bindOp.tbdPane(), 2).pushNoBlock();
                dialog = new JDialogOperator("Converter");  // NOI18N
                new JComboBoxOperator(dialog, 0).selectItem(2);

                textOp = new JEditorPaneOperator(dialog, 0);
                String result = textOp.getText();
                new JButtonOperator(dialog, "OK").push();  // NOI18N
                bindOp.ok();

                assertEquals("new Bool2FaceConverter()", result);  // NOI18N

                designer.source();
            }
        });
    }

    /**
     * Select update mode for jlabel
     */
    private void selectUpdateModeForJLabel(ComponentInspectorOperator inspector, String jLabelName, String mode) {
        // invoke bind dialog

        jLabelNameGlobal = jLabelName;

        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node actNode = new Node(inspector.treeComponents(), "[JFrame]|" + jLabelNameGlobal + " [JLabel]"); // NOI18N
                actNode.select();
                actNode.performPopupActionNoBlock(ACTION_PATH);

            }
        });


        // select update mode
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        bindOp.selectUpdateMode(mode);
        bindOp.ok();
    }

    /*
     * Get selected update mode text caption for jlabel
     */
    private String getSelectedUpdateModeForJLabel(ComponentInspectorOperator inspector, String jLabelName) {
        // invoke bind dialog

        jLabelNameGlobal = jLabelName;

        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node actNode = new Node(inspector.treeComponents(), "[JFrame]|" + jLabelNameGlobal + " [JLabel]"); // NOI18N
                actNode.select();
                actNode.performPopupActionNoBlock(ACTION_PATH);
            }
        });

        // get selected update mode
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        String result = bindOp.getSelectedUpdateMode();
        bindOp.cancel();
        return result;
    }
}