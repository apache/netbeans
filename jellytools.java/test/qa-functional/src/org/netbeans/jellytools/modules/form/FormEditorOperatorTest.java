/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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
    public final static String[] tests = new String[]{
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
