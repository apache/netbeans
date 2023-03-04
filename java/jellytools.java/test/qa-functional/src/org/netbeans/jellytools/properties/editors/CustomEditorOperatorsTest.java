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
package org.netbeans.jellytools.properties.editors;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.actions.AttachWindowAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.ComponentPaletteOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.FormNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.TestNode;

/** Tests of all custom editors which reside in package org.netbeans.jellytools.properties.editors.
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class CustomEditorOperatorsTest extends org.netbeans.jellytools.JellyTestCase {

    /** Node with all customizable properties */
    private static TestNode testNode;
    public static final String[] tests = {
        "testStringCustomEditorOperator",
        "testStringArrayCustomEditorOperator",
        "testPointCustomEditorOperator",
        "testDimensionCustomEditorOperator",
        "testRectangleCustomEditorOperator",
        "testColorCustomEditorOperator",
        "testFontCustomEditorOperator",
        "testFileCustomEditorOperator",
        "testClasspathCustomEditorOperator",
        "testProcessDescriptorCustomEditorOperator",
        "testClose",
        "testIconCustomEditorOperator"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CustomEditorOperatorsTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(CustomEditorOperatorsTest.class, tests);
    }

    /** Method called before each testcase. */
    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");  // NOI18N
        if (testNode == null) {
            testNode = new TestNode();
            testNode.showProperties();
        }
    }

    /** Test of org.netbeans.jellytools.properties.editors.StringCustomEditorOperator. */
    public void testStringCustomEditorOperator() {
        StringCustomEditorOperator editor = null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "String");
        p.openEditor();
        editor = new StringCustomEditorOperator("String");
        editor.setStringValue("tested text");
        assertEquals("tested text", editor.getStringValue());
        editor.ok();
        assertEquals("tested text", p.getValue());
    }

    /** Test of org.netbeans.jellytools.properties.editors.StringArrayCustomEditorOperator. */
    public void testStringArrayCustomEditorOperator() {
        StringArrayCustomEditorOperator editor = null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "String []");
        p.openEditor();
        editor = new StringArrayCustomEditorOperator("String []");
        editor.setItemText("tested text 1");
        assertEquals("tested text 1", editor.getItemText());
        editor.add();
        editor.setItemText("tested text 2");
        editor.add();
        editor.lstItemList().selectItem("tested text 1");
        assertEquals(0, editor.lstItemList().getSelectedIndex());
        editor.down();
        assertEquals(1, editor.lstItemList().getSelectedIndex());
        editor.up();
        assertEquals(0, editor.lstItemList().getSelectedIndex());
        editor.down("tested text 1");
        assertEquals("down(String) failed.", 1, editor.lstItemList().getSelectedIndex());
        editor.up("tested text 1");
        assertEquals("up(String) failed.", 0, editor.lstItemList().getSelectedIndex());
        editor.setItemText("tested text 3");
        editor.edit();
        editor.remove("tested text 3");
        assertEquals(1, editor.lstItemList().getModel().getSize());
        editor.edit("tested text 2", "tested text 4");
        assertEquals("edit(String, String) failed.", "tested text 4", editor.getItemText());
        String s[] = new String[]{"aa", "bb", "cc"};
        editor.setStringArrayValue(s);
        String s2[] = editor.getStringArrayValue();
        assertEquals(s[0], s2[0]);
        assertEquals(s[1], s2[1]);
        assertEquals(s[2], s2[2]);
        editor.ok();
        assertEquals("aa, bb, cc", p.getValue());
    }

    /** Test of org.netbeans.jellytools.properties.editors.PointCustomEditorOperator. */
    public void testPointCustomEditorOperator() {
        PointCustomEditorOperator editor = null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Point");
        p.openEditor();
        editor = new PointCustomEditorOperator("Point");
        editor.setPointValue("10", "20");
        assertEquals("10", editor.getXValue());
        assertEquals("20", editor.getYValue());
        editor.setXValue("30");
        assertEquals("30", editor.getXValue());
        editor.setYValue("40");
        assertEquals("40", editor.getYValue());
        editor.ok();
        assertEquals("[30, 40]", p.getValue());
    }

    /** Test of org.netbeans.jellytools.properties.editors.RectangleCustomEditorOperator. */
    public void testRectangleCustomEditorOperator() {
        RectangleCustomEditorOperator editor = null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Rectangle");
        p.openEditor();
        editor = new RectangleCustomEditorOperator("Rectangle");
        editor.setRectangleValue("10", "20", "30", "40");
        assertEquals("10", editor.getXValue());
        assertEquals("20", editor.getYValue());
        assertEquals("30", editor.getWidthValue());
        assertEquals("40", editor.getHeightValue());
        editor.setXValue("50");
        assertEquals("50", editor.getXValue());
        editor.setYValue("60");
        assertEquals("60", editor.getYValue());
        editor.setWidthValue("70");
        assertEquals("70", editor.getWidthValue());
        editor.setHeightValue("80");
        assertEquals("80", editor.getHeightValue());
        editor.ok();
        assertEquals("[50, 60, 70, 80]", p.getValue());
    }

    /** Test of org.netbeans.jellytools.properties.editors.DimensionCustomEditorOperator. */
    public void testDimensionCustomEditorOperator() {
        DimensionCustomEditorOperator editor = null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Dimension");
        p.openEditor();
        editor = new DimensionCustomEditorOperator("Dimension");
        editor.setDimensionValue("10", "20");
        assertEquals("10", editor.getWidthValue());
        assertEquals("20", editor.getHeightValue());
        editor.setWidthValue("30");
        assertEquals("30", editor.getWidthValue());
        editor.setHeightValue("40");
        assertEquals("40", editor.getHeightValue());
        editor.ok();
        assertEquals("[30, 40]", p.getValue());
    }

    /** Test of org.netbeans.jellytools.properties.editors.ColorCustomEditorOperator. */
    public void testColorCustomEditorOperator() {
        ColorCustomEditorOperator editor = null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Color");
        p.openEditor();
        editor = new ColorCustomEditorOperator("Color");
        editor.setRGBValue(10, 20, 30);
        assertEquals(new java.awt.Color(10, 20, 30), editor.getColorValue());
        java.awt.Color c = new java.awt.Color(40, 50, 60);
        editor.setColorValue(c);
        assertEquals(c, editor.getColorValue());
        editor.ok();
    }

    /** Test of org.netbeans.jellytools.properties.editors.FontCustomEditorOperator. */
    public void testFontCustomEditorOperator() {
        FontCustomEditorOperator editor = null;
        PropertySheetOperator pso = new PropertySheetOperator(TestNode.NODE_NAME);
        Property p = new Property(pso, "Font");
        p.openEditor();
        editor = new FontCustomEditorOperator("Font");
        editor.setFontName("Serif");
        assertTrue(editor.getFontName().indexOf("Serif") >= 0);
        editor.setFontStyle(FontCustomEditorOperator.STYLE_BOLDITALIC);
        assertEquals(FontCustomEditorOperator.STYLE_BOLDITALIC, editor.getFontStyle());
        editor.setFontSize("14");
        assertEquals("14", editor.getFontSize());
        editor.ok();
        // need to change selection because it gets editable otherwise
        pso.tblSheet().selectCell(0, 0);
    }

    /** Test of org.netbeans.jellytools.properties.editors.FileCustomEditorOperator. */
    public void testFileCustomEditorOperator() throws Exception {
        FileCustomEditorOperator editor = null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "File");
        p.openEditor();
        editor = new FileCustomEditorOperator("File");
        editor.setFileValue(getWorkDir().getAbsolutePath());
        assertEquals(getWorkDir(), editor.getFileValue());
        editor.ok();
        assertEquals(getWorkDir().getAbsolutePath(), p.getValue());
    }

    /** Test of org.netbeans.jellytools.properties.editors.ClasspathCustomEditorOperator. */
    public void testClasspathCustomEditorOperator() throws Exception {
        ClasspathCustomEditorOperator editor = null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "NbClassPath");
        p.openEditor();
        editor = new ClasspathCustomEditorOperator("NbClassPath");
        editor.addJARZIP().close();
        editor.addDirectory(getWorkDir());
        assertEquals(1, editor.getClasspathValue().length);
        assertEquals(getWorkDir().getAbsolutePath().toLowerCase(), editor.getClasspathValue()[0].toLowerCase());
        File parent = getWorkDir().getParentFile();
        editor.addDirectory(parent);
        assertEquals(2, editor.getClasspathValue().length);
        assertEquals(parent.getAbsolutePath().toLowerCase(), editor.getClasspathValue()[1].toLowerCase());
        editor.lstClasspath().selectItem(0);
        editor.moveDown();
        assertEquals(getWorkDir().getAbsolutePath().toLowerCase(), editor.getClasspathValue()[1].toLowerCase());
        editor.lstClasspath().selectItem(1);
        editor.moveUp();
        assertEquals(getWorkDir().getAbsolutePath().toLowerCase(), editor.getClasspathValue()[0].toLowerCase());
        editor.remove(parent.getAbsolutePath());
        assertEquals(1, editor.getClasspathValue().length);
        String s[] = new String[]{getWorkDir().getAbsolutePath(), parent.getAbsolutePath()};
        editor.setClasspathValue(s);
        String s2[] = editor.getClasspathValue();
        assertEquals(s[0].toLowerCase(), s2[0].toLowerCase());
        assertEquals(s[1].toLowerCase(), s2[1].toLowerCase());
        editor.ok();
        assertTrue(p.getValue().toLowerCase().indexOf(s[0].toLowerCase() + File.pathSeparator + s[1].toLowerCase()) >= 0);
    }

    /** Test of org.netbeans.jellytools.properties.editors.ProcessDescriptorCustomEditorOperator. */
    public void testProcessDescriptorCustomEditorOperator() {
        ProcessDescriptorCustomEditorOperator editor = null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "NbProcessDescriptor");
        p.openEditor();
        editor = new ProcessDescriptorCustomEditorOperator("NbProcessDescriptor");
        editor.selectProcessExecutable().close();
        editor.setProcess("test process");
        assertEquals("test process", editor.getProcess());
        editor.setArguments("test arguments");
        assertEquals("test arguments", editor.getArguments());
        assertEquals("", editor.getArgumentKey());
        editor.ok();
        assertEquals("test process test arguments", p.getValue());
    }

    /** Test of org.netbeans.jellytools.properties.editors.IconCustomEditorOperator. */
    public void testIconCustomEditorOperator() throws Exception {
        openDataProjects("SampleProject");
        Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
        FormNode node = new FormNode(sample1, "JFrameSample");
        node.open();
        // wait for form opened
        FormDesignerOperator designer = new FormDesignerOperator("JFrameSample");
        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        palette.attachTo(OutputOperator.invoke(), AttachWindowAction.RIGHT);
        //add something there
        palette.expandSwingControls();
        palette.selectComponent("Button"); // NOI18N
        designer.clickOnComponent(designer.fakePane().getSource());
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.selectComponent("JFrame|JButton"); // NOI18N
        PropertySheetOperator pso = inspector.properties();
        Property p = new Property(pso, "icon");
        p.openEditor();
        IconCustomEditorOperator editor = new IconCustomEditorOperator("Icon");
        editor.externalImage();
        assertTrue("External Image radio button not pushed.", editor.rbExternalImage().isSelected());
        editor.noImage();
        assertTrue("No Image radio button not pushed.", editor.rbNoImage().isSelected());
        editor.imageWithinProject();
        assertTrue("Image Within Project radio button not pushed.", editor.rbImageWithinProject().isSelected());
        editor.browseClasspath().close();
        editor.browseLocalDisk().close();
        editor.importToProject().close();
    }

    /** Close property sheet. */
    public void testClose() {
        new PropertySheetOperator(TestNode.NODE_NAME).close();
    }
}
