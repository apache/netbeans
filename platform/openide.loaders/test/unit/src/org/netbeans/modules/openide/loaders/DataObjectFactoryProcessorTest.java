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
package org.netbeans.modules.openide.loaders;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.loaders.data.DoFPDataObject;
import org.netbeans.modules.openide.loaders.data.DoFPDataObjectMultiple;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.test.AnnotationProcessorTestUtils;

/**
 *
 * @author Eric Barboni <skygo@netbeans.org>
 */
public class DataObjectFactoryProcessorTest extends NbTestCase {
// XXX inner class for DataObject fail
//

    static {
        System.setProperty("java.awt.headless", "true");
    }

    public DataObjectFactoryProcessorTest(String n) {
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

// Several test for javac
    public void testConstructorWrongType() throws IOException {
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A",
                "import org.openide.loaders.DataObject;\n"
                + "@DataObject.Registration(mimeType=\"text/testa\")"
                + "public class A {\n"
                + "    A() {}"
                + "}\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testWrongAmountofParameterInConstructorTypeDataObject() throws IOException {
        {
            clearWorkDir();
            assertTrue("Headless run", GraphicsEnvironment.isHeadless());
            AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                    DUMMYCLASSIMPORTPART
                    + "@DataObject.Registration(mimeType = \"text/testb\")"
                    + DUMMYCLASSDEFPART
                    + "public B(int a,int b, int c){}"
                    + DUMMYCLASSOVERRIDEPART);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
            assertFalse("Compilation has to fail:\n" + os, r);
        }
        {
            clearWorkDir();
            assertTrue("Headless run", GraphicsEnvironment.isHeadless());
            AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                    DUMMYCLASSIMPORTPART
                    + "@DataObject.Registration(mimeType = \"text/testb\")"
                    + DUMMYCLASSDEFPART
                    + "public B(){}"
                    + DUMMYCLASSOVERRIDEPART);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
            assertFalse("Compilation has to fail:\n" + os, r);
        }
    }

    public void testConstructorTypeDataObject() throws IOException {
        {
            clearWorkDir();
            assertTrue("Headless run", GraphicsEnvironment.isHeadless());
            AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                    DUMMYCLASSIMPORTPART
                    + "@DataObject.Registration(mimeType = \"text/testb\")"
                    + DUMMYCLASSDEFPART
                    + "public B(FileObject fo,DataLoader dol)throws DataObjectExistsException {        super(fo,dol);    }"
                    + DUMMYCLASSOVERRIDEPART);



            ByteArrayOutputStream os = new ByteArrayOutputStream();
            boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
            assertFalse("Compilation has to fail:\n" + os, r);
        }
        {
            clearWorkDir();
            assertTrue("Headless run", GraphicsEnvironment.isHeadless());
            AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                    DUMMYCLASSIMPORTPART
                    + "@DataObject.Registration(mimeType = \"text/testb\")"
                    + DUMMYCLASSDEFPART
                    + "public B(FileObject fo,MultiFileLoader dol)throws DataObjectExistsException {        super(fo,dol);    }"
                    + DUMMYCLASSOVERRIDEPART);



            ByteArrayOutputStream os = new ByteArrayOutputStream();
            boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
            assertTrue("Compilation has to succed:\n" + os, r);
        }
    }

    public void testConstructorTypeDataObjectFactory() throws IOException {
        {
            clearWorkDir();
            assertTrue("Headless run", GraphicsEnvironment.isHeadless());
            AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                    DUMMYFACTORYCLASSIMPORTPART
                    + "@DataObject.Registration(mimeType = \"text/testb\")"
                    + DUMMYFACTORYCLASSDEFPART
                    + "public B() { super( \"test\"); }"
                    + DUMMYFACTORYCLASSOVERRIDEPART);



            ByteArrayOutputStream os = new ByteArrayOutputStream();
            boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
            assertTrue("Compilation has to succed:\n" + os, r);
        }
        {
            clearWorkDir();
            assertTrue("Headless run", GraphicsEnvironment.isHeadless());
            AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.B",
                    DUMMYFACTORYCLASSIMPORTPART
                    + "@DataObject.Registration(mimeType = \"text/testb\")"
                    + DUMMYFACTORYCLASSDEFPART
                    + "protected B() { super( \"test\"); }"
                    + DUMMYFACTORYCLASSOVERRIDEPART);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
            assertFalse("Compilation has to fail:\n" + os, r);
        }
    }

    // use external DoFP* class and check if registration is good
    public void testSeveralMimeType() throws IOException {
        clearWorkDir();
        {
            FileObject fo = FileUtil.getConfigFile(
                    "Loaders/text/test1/Factories/" + DoFPDataObject.class.getName().replace(".", "-") + ".instance");
            assertNotNull("File found", fo);

            assertEquals("Position Ok", 3565, fo.getAttribute("position"));
            assertEquals("Label Ok", "labeltest", fo.getAttribute("displayName"));
            assertEquals("MimeOk", "text/test1", fo.getAttribute("mimeType"));
            Object icon = fo.getAttribute("iconBase");
            assertEquals("Icon found", "org/openide/loaders/unknown.gif", icon);
            assertEquals("DataObjectClass found", DoFPDataObject.class.getName(), fo.getAttribute("dataObjectClass"));

        }        
    }

// use external DoFP* class and their registration to test if dataobject return is good
    public void testDataLoad() throws Exception {
        // be sure mime are correct
        {
            FileObject fo = createXmlFile("sdfsdf", ".tt1");
            assertEquals("text/test1", fo.getMIMEType());
            DataObject find = DataObject.find(fo);
            assertEquals("DataLoader type", DoFPDataObject.class, find.getClass());
        }
        {
            FileObject fo = createXmlFile("sdfsdf", ".tt3");
            assertEquals("text/test3", fo.getMIMEType());
            // XXX DoFPCustomLoader not loaded cannot assert for loader
        }
        {
            FileObject fo = createXmlFile("sdfsdf", ".ttm2");
            assertEquals("text/testm2", fo.getMIMEType());
            DataObject find = DataObject.find(fo);
            assertEquals("DataLoader type", DoFPDataObjectMultiple.class, find.getClass());
        }
    }

   

    private FileObject createXmlFile(String content, String ext) throws Exception {
        FileObject file = FileUtil.createMemoryFileSystem().getRoot().createData("file" + ext);
        FileLock lock = file.lock();
        try {
            OutputStream out = file.getOutputStream(lock);
            try {
                out.write(content.getBytes());
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return file;
    }
    //---------------------- class text part DataObject
    String DUMMYCLASSIMPORTPART = "import java.io.IOException;"
            + "import org.openide.filesystems.FileObject;"
            + "import org.openide.loaders.DataFolder;"
            + "import org.openide.loaders.DataObject;"
            + "import org.openide.loaders.MultiFileLoader;"
            + "import org.openide.loaders.DataObjectExistsException;"
            + "import org.openide.loaders.DataLoader;"
            + "import org.openide.util.HelpCtx;";
    String DUMMYCLASSDEFPART = "public class B extends DataObject {";
    String OVERRIDE = "@Override\n";
    String DUMMYRUNTIMEEXCEPTION = " throw new RuntimeException(\"Not implemented yet.\");";
    String DUMMYCLASSOVERRIDEPART =
            ""
            + OVERRIDE + "public boolean isDeleteAllowed() {" + DUMMYRUNTIMEEXCEPTION + "}"
            + OVERRIDE + "public boolean isCopyAllowed() {" + DUMMYRUNTIMEEXCEPTION + "}"
            + OVERRIDE + "public boolean isMoveAllowed() {" + DUMMYRUNTIMEEXCEPTION + "}"
            + OVERRIDE + "public boolean isRenameAllowed() {" + DUMMYRUNTIMEEXCEPTION + "}"
            + OVERRIDE + "public HelpCtx getHelpCtx() {" + DUMMYRUNTIMEEXCEPTION + "}"
            + OVERRIDE + "protected DataObject handleCopy(DataFolder f) throws IOException {" + DUMMYRUNTIMEEXCEPTION + "}"
            + OVERRIDE + "protected void handleDelete() throws IOException {" + DUMMYRUNTIMEEXCEPTION + "}"
            + OVERRIDE + "protected FileObject handleRename(String name) throws IOException {" + DUMMYRUNTIMEEXCEPTION + "}"
            + OVERRIDE + "protected FileObject handleMove(DataFolder df) throws IOException {" + DUMMYRUNTIMEEXCEPTION + "}"
            + OVERRIDE + "protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {" + DUMMYRUNTIMEEXCEPTION + "}"
            + "}\n";
    // ---------------------- class text part DataObject.Factory
    String DUMMYFACTORYCLASSIMPORTPART = "   import java.io.IOException;"
            + "import org.openide.filesystems.FileObject;"
            + "import org.openide.loaders.DataObject;"
            + "import org.openide.loaders.MultiDataObject;"
            + "import org.openide.loaders.DataObjectExistsException;"
            + "import org.openide.loaders.UniFileLoader;";
//@DataObject.Registration(mimeType = "text/test3", position = 300)
    String DUMMYFACTORYCLASSDEFPART = "public class B extends UniFileLoader {";

    /*
     * public DoFPCustomLoader() {
     * super("org.netbeans.modules.openide.loaders.DoFPDataObjectCustomLoader");
     * }
     */
    String DUMMYFACTORYCLASSOVERRIDEPART = OVERRIDE
            + "  protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {"
            + DUMMYRUNTIMEEXCEPTION
            + "   }"
            + "}";
}
