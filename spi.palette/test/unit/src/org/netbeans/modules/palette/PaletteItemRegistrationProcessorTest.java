/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.palette;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.AnnotationProcessorTestUtils;

/**
 *
 * @author Didier
 */
public class PaletteItemRegistrationProcessorTest extends NbTestCase {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    public PaletteItemRegistrationProcessorTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    private static final String VALID_CLASS_ANNOTATION = "@PaletteItemRegistration(paletteid = \"JavaPalette\", category = \"dummycat\", itemid = \"myitem\","
            + "icon16 = \"org/netbeans/modules/palette/resources/unknown16.gif\", "
            + "icon32 = \"org/netbeans/modules/palette/resources/unknown32.gif\", tooltip = \"tooltip\", name = \"name\")";
    private static final String INVALIDRESOURCE_CLASS_ANNOTATION = "@PaletteItemRegistration(paletteid = \"JavaPalette\", category = \"dummycat\", itemid = \"myitem\","
            + "icon16 = \"org/netbeans/modules/palette/resources/unknown16wrong.gif\", "
            + "icon32 = \"org/netbeans/modules/palette/resources/unknown32wrong.gif\", tooltip = \"tooltip\", name = \"name\")";
    private static final String VALID_PACKAGE_ANNOTATION = "@PaletteItemRegistration(paletteid = \"JavaPalette\", category = \"dummycat\", itemid = \"myitem\","
            + "icon16 = \"org/netbeans/modules/palette/resources/unknown16.gif\", "
            + "icon32 = \"org/netbeans/modules/palette/resources/unknown32.gif\", tooltip = \"tooltip\", name = \"name\" ,body =\"mydummybody\")";    
    private static final String INVALIDRESOURCE_PACKAGE_ANNOTATION = "@PaletteItemRegistration(paletteid = \"JavaPalette\", category = \"dummycat\", itemid = \"myitem\","
            + "icon16 = \"org/netbeans/modules/palette/resources/unknown16wrong.gif\", "
            + "icon32 = \"org/netbeans/modules/palette/resources/unknown32wrong.gif\", tooltip = \"tooltip\", name = \"name\" ,body =\"mydummybody\")";   
    private static final String INVALID_CLASS_ANNOTATION_1 = "@PaletteItemRegistration(paletteid = \"JavaPalette\", category = \"dummycat\", itemid = \"myitem\","
            + "icon32 = \"org/netbeans/modules/palette/resources/unknown32.gif\", tooltip = \"tooltip\", name = \"name\")";
    private static final String INVALID_CLASS_ANNOTATION_2 = "@PaletteItemRegistration(paletteid = \"JavaPalette\", category = \"dummycat\", itemid = \"myitem\","
            + "icon16 = \"org/netbeans/modules/palette/resources/unknown16.gif\", tooltip = \"tooltip\", name = \"name\")";

    public void testNoCompliantClassAnnotated() throws IOException {
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A",
                "import org.netbeans.spi.palette.PaletteItemRegistration;\n"
                + VALID_CLASS_ANNOTATION
                + "public class A {\n"
                + "    A() {}"
                + "}\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testActiveEditorDropAnnotated() throws IOException {
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                "import org.netbeans.spi.palette.PaletteItemRegistration;\n"
                + "import org.openide.text.ActiveEditorDrop;\n;import javax.swing.text.JTextComponent;"
                + VALID_CLASS_ANNOTATION
                + "public class B implements ActiveEditorDrop {\n"
                + "     public boolean handleTransfer(JTextComponent targetComponent) {return true; }"
                + "}\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation has to be successfull:\n" + os, r);
    }

    public void testActiveEditorDropAnnotatedWithInvalidResources() throws IOException {
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                "import org.netbeans.spi.palette.PaletteItemRegistration;\n"
                + "import org.openide.text.ActiveEditorDrop;\n;import javax.swing.text.JTextComponent;"
                + INVALIDRESOURCE_CLASS_ANNOTATION
                + "public class B implements ActiveEditorDrop {\n"
                + "     public boolean handleTransfer(JTextComponent targetComponent) {return true; }"
                + "}\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }
    
    public void testActiveEditorDropAnnotatedWithMissingIcon16() throws IOException {
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                "import org.netbeans.spi.palette.PaletteItemRegistration;\n"
                + "import org.openide.text.ActiveEditorDrop;\n;import javax.swing.text.JTextComponent;"
                + INVALID_CLASS_ANNOTATION_1
                + "public class B implements ActiveEditorDrop {\n"
                + "     public boolean handleTransfer(JTextComponent targetComponent) {return true; }"
                + "}\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testActiveEditorDropAnnotatedWithMissingIcon32() throws IOException {
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                "import org.netbeans.spi.palette.PaletteItemRegistration;\n"
                + "import org.openide.text.ActiveEditorDrop;\n;import javax.swing.text.JTextComponent;"
                + INVALID_CLASS_ANNOTATION_2
                + "public class B implements ActiveEditorDrop {\n"
                + "     public boolean handleTransfer(JTextComponent targetComponent) {return true; }"
                + "}\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testInvalidAnnotatedPackage() throws IOException {
        //missing body
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "testb.package-info",
                VALID_CLASS_ANNOTATION
                + "\npackage testa\n;"
                + "import org.netbeans.spi.palette.PaletteItemRegistration;\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testAnnotatedPackage() throws IOException {
        //missing body
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "testa.package-info",
                VALID_PACKAGE_ANNOTATION
                + "\npackage testa\n;"
                + "import org.netbeans.spi.palette.PaletteItemRegistration;\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation has to be successfull:\n" + os, r);
    }
    public void testAnnotatedPackageWithInvalidResource() throws IOException {
        //missing body
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "testa.package-info",
                INVALIDRESOURCE_PACKAGE_ANNOTATION
                + "\npackage testa\n;"
                + "import org.netbeans.spi.palette.PaletteItemRegistration;\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }
}