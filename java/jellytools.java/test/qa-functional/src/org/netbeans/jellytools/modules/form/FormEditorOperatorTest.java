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
package org.netbeans.jellytools.modules.form;

import java.awt.Component;
import java.io.IOException;
import javax.swing.JButton;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.actions.AttachWindowAction;
import org.netbeans.jellytools.actions.PaletteViewAction;
import org.netbeans.jellytools.nodes.FormNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test FormDesignerOperator, ComponentPaletteOperator and
 * ComponentInspectorOperator.
 */
public class FormEditorOperatorTest extends JellyTestCase {

    private static final String SAMPLE_FRAME = "JFrameSample.java";
    public static final String[] tests = new String[]{
        "testOpen",
        "testSourceButton",
        "testEditor",
        "testDesignButton",
        "testDesign",
        "testProperties",
        "testPreviewForm",
        "testClose"
    };

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(FormEditorOperatorTest.class, tests);
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public FormEditorOperatorTest(String testName) {
        super(testName);
    }

    /** Print out test name. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Opens sample JFrame. */
    public void testOpen() throws Exception {
        FormNode node = new FormNode(new SourcePackagesNode("SampleProject"),
                "sample1|" + SAMPLE_FRAME); // NOI18N
        node.open();
    }

    /** Test source toggle button. */
    public void testSourceButton() {
        new FormDesignerOperator(SAMPLE_FRAME).source();
    }

    /** Test editor method. */
    public void testEditor() {
        new FormDesignerOperator(SAMPLE_FRAME).editor();
    }

    /** Test Design toggle button. */
    public void testDesignButton() {
        new FormDesignerOperator(SAMPLE_FRAME).design();
    }

    /** Test design actions. */
    public void testDesign() {
        FormDesignerOperator designer = new FormDesignerOperator(SAMPLE_FRAME);
        new PaletteViewAction().perform();
        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        ComponentInspectorOperator.invokeNavigator();
        // attach Palette to better position because components are not visible
        // when screen resolution is too low
        palette.attachTo(OutputOperator.invoke(), AttachWindowAction.RIGHT);
        //add something there
        palette.expandSwingControls();
        palette.selectComponent("Label"); // NOI18N
        designer.clickOnComponent(designer.fakePane().getSource());
        palette.selectComponent("Button"); // NOI18N
        designer.clickOnComponent(designer.fakePane().getSource());
        palette.selectComponent("Text Field"); // NOI18N
        designer.clickOnComponent(designer.fakePane().getSource());
        // add second button next to the first one
        Component button1 = designer.findComponent(JButton.class);
        palette.selectComponent("Button"); // NOI18N
        designer.clickOnComponent(button1);
    }

    /** Test setting properties of components. */
    public void testProperties() {
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.selectComponent("JFrame|jButton2"); // NOI18N
        PropertySheetOperator pso = inspector.properties();
        new Property(pso, "text").setValue("Add"); // NOI18N
        inspector.selectComponent("JFrame|jLabel1"); // NOI18N
        new Property(pso, "text").setValue("Text to be added:"); // NOI18N
        inspector.selectComponent("JFrame|jTextField1"); // NOI18N
        new Property(pso, "text").setValue("             "); // NOI18N
        inspector.selectComponent("JFrame|jButton1"); // NOI18N
        new Property(pso, "text").setValue("Close"); // NOI18N
    }

    /** Test preview form mode of form designer. */
    public void testPreviewForm() {
        FormDesignerOperator designer = new FormDesignerOperator(SAMPLE_FRAME);
        JFrameOperator myFrame = designer.previewForm(SAMPLE_FRAME.substring(0, SAMPLE_FRAME.indexOf('.')));
        myFrame.resize(400, 400);
        myFrame.requestClose();
    }

    /** Closes java source together with form editor. */
    public void testClose() {
        new FormDesignerOperator(SAMPLE_FRAME).closeDiscard();
    }
}
