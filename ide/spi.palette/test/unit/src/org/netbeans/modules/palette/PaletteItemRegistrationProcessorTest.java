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
package org.netbeans.modules.palette;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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