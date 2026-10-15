/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.templates;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.util.SharedClassObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Marek Fukala
 * @author Jaroslav Tulach
 */
public class ScriptingCreateFromTemplateTest extends NbTestCase {
    
    public ScriptingCreateFromTemplateTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(SharedClassObject.findObject(SimpleLoader.class, true));
    }

    public void testCreateFromTemplateEncodingProperty() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        os.write("print(encoding)".getBytes());
        os.close();
        assertEquals("content/unknown", fo.getMIMEType());
        fo.setAttribute ("template", Boolean.TRUE);
        assertEquals("content/unknown", fo.getMIMEType());
        fo.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");
        
        DataObject obj = DataObject.find(fo);
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.emptyMap();
        DataObject inst = obj.createFromTemplate(folder, "complex", parameters);
        FileObject instFO = inst.getPrimaryFile();
        
        Charset targetEnc = FileEncodingQuery.getEncoding(instFO);
        assertNotNull("Template encoding is null", targetEnc);
        String instText = stripNewLines(instFO.asText());
        assertEquals("Encoding in template doesn't match", targetEnc.name(), instText);
    }

    static String stripNewLines(String str) {
        return str.replace("\n", "").replace("\r", "");
    }

    public void testFreeFileExtension() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject template = FileUtil.createData(root, "simple.pl");
        try (OutputStream os = template.getOutputStream()) {
            os.write("print('#!/usr/bin/perl'); print('# '+license); print('# '+name+' in '+nameAndExt);".getBytes());
        }
        template.setAttribute("template", true);
        template.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("license", "GPL");
        parameters.put(CreateFromTemplateHandler.FREE_FILE_EXTENSION, true);
        String newLine = System.getProperty("line.separator");
        FileObject inst = DataObject.find(template).createFromTemplate(DataFolder.findFolder(root), "nue", parameters).getPrimaryFile();
        assertEquals("#!/usr/bin/perl" + newLine + "# GPL" + newLine + "# nue in nue.pl" + newLine, inst.asText());
        assertEquals("nue.pl", inst.getPath());
        /* XXX perhaps irrelevant since typical wizards disable Finish in this condition
            inst = DataObject.find(template).createFromTemplate(DataFolder.findFolder(root), "nue", parameters).getPrimaryFile();
            assertEquals("#!/usr/bin/perl\n# GPL\n# nue_1 in nue_1.pl\n", inst.asText());
            assertEquals("nue_1.pl", inst.getPath());
         */
        inst = DataObject.find(template).createFromTemplate(DataFolder.findFolder(root), "nue.cgi", parameters).getPrimaryFile();
        assertEquals("#!/usr/bin/perl" + newLine + "# GPL" + newLine + "# nue in nue.cgi" + newLine, inst.asText());
        assertEquals("nue.cgi", inst.getPath());
        /* XXX
            inst = DataObject.find(template).createFromTemplate(DataFolder.findFolder(root), "nue.cgi", parameters).getPrimaryFile();
            assertEquals("#!/usr/bin/perl\n# GPL\n# nue_1 in nue_1.cgi\n", inst.asText());
            assertEquals("nue_1.cgi", inst.getPath());
         */
        inst = DataObject.find(template).createFromTemplate(DataFolder.findFolder(root), "explicit.pl", parameters).getPrimaryFile();
        assertEquals("#!/usr/bin/perl" + newLine + "# GPL" + newLine + "# explicit in explicit.pl" + newLine, inst.asText());
        assertEquals("explicit.pl", inst.getPath());
        /* XXX
            inst = DataObject.find(template).createFromTemplate(DataFolder.findFolder(root), "explicit.pl", parameters).getPrimaryFile();
            assertEquals("#!/usr/bin/perl\n# GPL\n# explicit_1 in explicit_1.pl\n", inst.asText());
            assertEquals("explicit_1.pl", inst.getPath());
         */
    }
    
    //fix for this test was rolled back because of issue #120865
    public void XtestCreateFromTemplateDocumentCreated() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        os.write("test".getBytes());
        os.close();
        fo.setAttribute ("template", Boolean.TRUE);
        fo.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");

        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.parse("content/unknown"), new TestEditorKit());
        
        DataObject obj = DataObject.find(fo);
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        assertFalse(TestEditorKit.createDefaultDocumentCalled);
        DataObject inst = obj.createFromTemplate(folder, "test");
        assertTrue(TestEditorKit.createDefaultDocumentCalled);
        
        String exp = "test";
        assertEquals(exp, inst.getPrimaryFile().asText());
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class.getName());
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("prima")) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FE(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    
    private static final class FE extends FileEntry {
        public FE(MultiDataObject mo, FileObject fo) {
            super(mo, fo);
        }

        @Override
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            fail("I do not want to be called");
            return null;
        }
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
        
        public String getName() {
            return getPrimaryFile().getNameExt();
        }
    }
    
    private static final class TestEditorKit extends DefaultEditorKit {
        
        static boolean createDefaultDocumentCalled;

        @Override
        public Document createDefaultDocument() {
            createDefaultDocumentCalled = true;
            return super.createDefaultDocument();
        }
        
    }

}
