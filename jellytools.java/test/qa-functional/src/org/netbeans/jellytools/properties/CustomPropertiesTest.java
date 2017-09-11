/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools.properties;

import java.io.File;
import java.io.IOException;
import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JDialogOperator;

/** Tests of all custom properties which extend org.netbeans.jellytools.properties.Property
 * and reside in package org.netbeans.jellytools.properties.
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class CustomPropertiesTest extends org.netbeans.jellytools.JellyTestCase {

    /** Node with all customizable properties */
    private static TestNode testNode;
    static final String[] tests = {
        "testStringProperty",
        "testStringArrayProperty",
        "testPointProperty",
        "testDimensionProperty",
        "testRectangleProperty",
        "testColorProperty",
        "testFontProperty",
        "testFileProperty",
        "testClasspathProperty",
        "testProcessDescriptorProperty",
        "testClose"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CustomPropertiesTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        return createModuleTest(CustomPropertiesTest.class, tests);
    }

    /** Method called before each testcase. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");  // NOI18N
        if (testNode == null) {
            testNode = new TestNode();
            testNode.showProperties();
        }
    }

    /** Test org.netbeans.jellytools.properties.StringProperty. */
    public void testStringProperty() {
        StringProperty p = new StringProperty(new PropertySheetOperator(TestNode.NODE_NAME), "String");  // NOI18N
        p.setStringValue("test value");  // NOI18N
        assertEquals("test value", p.getStringValue());  // NOI18N
    }

    /** Test org.netbeans.jellytools.properties.StringArrayProperty. */
    public void testStringArrayProperty() {
        StringArrayProperty p = new StringArrayProperty(new PropertySheetOperator(TestNode.NODE_NAME), "String []");  // NOI18N
        String s[] = new String[]{"aa", "bb"};  // NOI18N
        p.setStringArrayValue(s);
        String s2[] = p.getStringArrayValue();
        assertEquals(s[0], s2[0]);
        assertEquals(s[1], s2[1]);
    }

    /** Test org.netbeans.jellytools.properties.PointProperty. */
    public void testPointProperty() {
        PointProperty p = new PointProperty(new PropertySheetOperator(TestNode.NODE_NAME), "Point");  // NOI18N
        p.setPointValue("10", "20");  // NOI18N
        String s[] = p.getPointValue();
        assertEquals(s[0], "10");  // NOI18N
        assertEquals(s[1], "20");  // NOI18N
    }

    /** Test org.netbeans.jellytools.properties.RectangleProperty. */
    public void testRectangleProperty() {
        RectangleProperty p = new RectangleProperty(new PropertySheetOperator(TestNode.NODE_NAME), "Rectangle");  // NOI18N
        p.setRectangleValue("10", "20", "30", "40");  // NOI18N
        String s[] = p.getRectangleValue();
        assertEquals(s[0], "10");  // NOI18N
        assertEquals(s[1], "20");  // NOI18N
        assertEquals(s[2], "30");  // NOI18N
        assertEquals(s[3], "40");  // NOI18N
    }

    /** Test org.netbeans.jellytools.properties.DimensionProperty. */
    public void testDimensionProperty() {
        DimensionProperty p = new DimensionProperty(new PropertySheetOperator(TestNode.NODE_NAME), "Dimension");  // NOI18N
        p.setDimensionValue("10", "20");  // NOI18N
        String s[] = p.getDimensionValue();
        assertEquals(s[0], "10");  // NOI18N
        assertEquals(s[1], "20");  // NOI18N
    }

    /** Test org.netbeans.jellytools.properties.ColorProperty. */
    public void testColorProperty() {
        ColorProperty p = new ColorProperty(new PropertySheetOperator(TestNode.NODE_NAME), "Color");  // NOI18N
        p.setRGBValue(10, 20, 30);
        java.awt.Color c = new java.awt.Color(10, 20, 30);
        assertEquals(c, p.getColorValue());
        c = new java.awt.Color(40, 50, 60);
        p.setColorValue(c);
        assertEquals(c, p.getColorValue());
    }

    /** Test org.netbeans.jellytools.properties.FontProperty. */
    public void testFontProperty() {
        PropertySheetOperator pso = new PropertySheetOperator(TestNode.NODE_NAME);
        FontProperty p = new FontProperty(pso, "Font");  // NOI18N
        try {
            p.setFontValue("Serif", FontProperty.STYLE_BOLDITALIC, "14");  // NOI18N
        } catch (TimeoutExpiredException e) {
            // sometimes it fails on Solaris
            log("jemmy.log", "ERROR: " + e.getMessage());
            JDialog fontDialog = JDialogOperator.findJDialog(p.getName(), false, false);
            if (fontDialog != null) {
                log("jemmy.log", "   Closing Font dialog.");
                new NbDialogOperator(fontDialog).close();
            }
            log("jemmy.log", "   Trying to set font once more");
            p.setFontValue("Serif", FontProperty.STYLE_BOLDITALIC, "14");  // NOI18N
        }
        // need to change selection because it gets editable otherwise
        pso.tblSheet().selectCell(0, 0);
        String s[] = p.getFontValue();
        // need to change selection because it gets editable otherwise
        pso.tblSheet().selectCell(0, 0);
        assertTrue(s[0].indexOf("Serif") >= 0);  // NOI18N
        assertEquals(FontProperty.STYLE_BOLDITALIC, s[1]);
        assertEquals("14", s[2]);  // NOI18N
    }

    /** Test org.netbeans.jellytools.properties.FileProperty. */
    public void testFileProperty() throws Exception {
        FileProperty p = new FileProperty(new PropertySheetOperator(TestNode.NODE_NAME), "File");  // NOI18N
        p.setFileValue(getWorkDir());
        assertEquals(getWorkDir(), p.getFileValue());
        log("init file");
        p.setFileValue(new File(getWorkDir(), getName() + ".log").getAbsolutePath());
        assertEquals(new File(getWorkDir(), getName() + ".log"), p.getFileValue());
    }

    /** Test org.netbeans.jellytools.properties.ClasspathProperty. */
    public void testClasspathProperty() throws Exception {
        ClasspathProperty p = new ClasspathProperty(new PropertySheetOperator(TestNode.NODE_NAME), "NbClassPath");  // NOI18N
        String s[] = new String[]{getWorkDir().getAbsolutePath(), getWorkDir().getParentFile().getAbsolutePath()};
        p.setClasspathValue(s);
        String s2[] = p.getClasspathValue();
        assertEquals(s[0].toLowerCase(), s2[0].toLowerCase());
        assertEquals(s[1].toLowerCase(), s2[1].toLowerCase());
    }

    /** Test org.netbeans.jellytools.properties.ProcessDescriptorProperty. */
    public void testProcessDescriptorProperty() {
        ProcessDescriptorProperty p = new ProcessDescriptorProperty(new PropertySheetOperator(TestNode.NODE_NAME), "NbProcessDescriptor");  // NOI18N
        p.setProcessDescriptorValue("test process", "test arguments");  // NOI18N
        String s[] = p.getProcessDescriptorValue();
        assertEquals("test process", s[0]);  // NOI18N
        assertEquals("test arguments", s[1]);  // NOI18N
    }

    /** Close property sheet. */
    public void testClose() {
        new PropertySheetOperator(TestNode.NODE_NAME).close();
    }
}
