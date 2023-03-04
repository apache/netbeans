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

package org.netbeans.modules.templates;

import java.awt.Dialog;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Enumerations;

/**
 *
 * @author Jaroslav Tulach
 */
public class SCFTHandlerTest extends NbTestCase {
    static {
        // confuse the system a bit, if your system runs with UTF-8 default locale...
        //System.setProperty("file.encoding", "cp1252");
    }
    
    public SCFTHandlerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(DD.class, Pool.class, FEQI.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateFromTemplateUsingFreemarker() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        String txt = "print('<html><h1>', title, '</h1></html>');";
        os.write(txt.getBytes());
        os.close();
        fo.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");
        
        
        DataObject obj = DataObject.find(fo);
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.singletonMap("title", "Nazdar");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex.txt", n.getName());
        
        String exp = "<html><h1> Nazdar </h1></html>\n";
        assertEquals(exp, readFile(n.getPrimaryFile()));
        
    }

    public void testCreateWithNameAndExt() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        String txt = "print('<html><h1>', nameAndExt, '</h1></html>')";
        os.write(txt.getBytes());
        os.close();
        fo.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");
        
        
        DataObject obj = DataObject.find(fo);
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.emptyMap();
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex.txt", n.getName());
        
        String exp = "<html><h1> complex.txt </h1></html>\n";
        assertEquals(exp, readFile(n.getPrimaryFile()));
        
    }

    public void testCreateWithNameAndExtForForm() throws Exception {
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        FileObject root = lfs.getRoot();
        FileObject fo = FileUtil.createData(root, "j.java");
        OutputStream os = fo.getOutputStream();
        String txt = "print('<html><h1>', nameAndExt, '</h1></html>')";
        os.write(txt.getBytes());
        os.close();
        fo.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");
        
        FileObject fo2 = FileUtil.createData(root, "j.form");
        OutputStream os2 = fo2.getOutputStream();
        String txt2 = "print('<html><h2>', nameAndExt, '</h2></html>')";
        os2.write(txt2.getBytes());
        os2.close();
        fo2.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");
        
        DataObject obj = DataObject.find(fo);
        assertEquals("Both files", 2, obj.files().size());
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.emptyMap();
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        
        assertEquals("Two files", 2, n.files().size());
        
        
        FileObject newForm = FileUtil.findBrother(n.getPrimaryFile(), "form");
        
        assertEquals("Primary file is java", "java", n.getPrimaryFile().getExt());
        
        assertNotNull("Form copied", newForm);
        DataObject frm = DataObject.find(newForm);
        assertSame("Form belongs to java", n, frm);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex", n.getName());
        
        String exp = "<html><h1> complex.java </h1></html>\n";
        assertEquals("Primary file" + n.getPrimaryFile(), exp, readFile(n.getPrimaryFile()));
        
        String exp2 = "<html><h2> complex.form </h2></html>\n";
        assertEquals(exp2, readFile(newForm));
    }
    
    public void testBasePropertiesAlwaysPresent() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        String txt = "print('<html><h1>'); print(name); print('</h1>');" +
            "print('<h2>'); print(date); print('</h2>');" +
            "print('<h3>'); print(time); print('</h3>');" +
            "print('<h4>'); print(user); print('</h4>');" +
            "print('<h4>'); print(dateTime.getTime()); print('</h4>');" +
            "print('</html>');";
        os.write(txt.getBytes());
        os.close();
        fo.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");
        
        
        DataObject obj = DataObject.find(fo);
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.singletonMap("title", "Nazdar");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex.txt", n.getName());
        
        String res = readFile(n.getPrimaryFile());
        
        if (res.indexOf("date") >= 0) fail(res);
        if (res.indexOf("time") >= 0) fail(res);
        if (res.indexOf("user") >= 0) fail(res);
        if (res.indexOf("name") >= 0) fail(res);
        if (res.indexOf("dateTime") >= 0) fail(res);
    }
    
    private static String readFile(FileObject fo) throws IOException {
        return readChars(fo, Charset.defaultCharset());
    }

    private static String readChars(FileObject fo, Charset set) throws IOException {
        try (InputStream is = fo.getInputStream()) {
            StringBuilder sb = new StringBuilder();
            int read = 0;
            char[] buffer = new char[1024];
            InputStreamReader r = new InputStreamReader(is, set);
            while ((read = r.read(buffer)) > 0) {
                sb.append(buffer, 0, read);
            }
            return sb.toString().replace(System.getProperty("line.separator"), "\n");
        }
    }
    
     public void testUTF8() throws Exception {
         FileObject root = FileUtil.getConfigRoot();
         FileObject xmldir = FileUtil.createFolder(root, "xml");
         FileObject xml = FileUtil.createData(xmldir, "class.txt");
         OutputStream os = xml.getOutputStream();
         FileUtil.copy(getClass().getResourceAsStream("utf8.xml"), os);
         xml.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");
         os.close();
         
         DataObject obj = DataObject.find(xml);
         
         
         FileObject target = FileUtil.createFolder(FileUtil.createMemoryFileSystem().getRoot(), "dir");
         DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(target, "target"));
         
         
         
         Charset set = Charset.forName("iso-8859-2");
         FEQI.fs = target.getFileSystem();
         FEQI.result = set;
         
         
         Map<String,String> parameters = Collections.singletonMap("title", "Nazdar");
         DataObject n = obj.createFromTemplate(folder, "complex", parameters);
         
         assertEquals("Created in right place", folder, n.getFolder());
         assertEquals("Created with right name", "complex.txt", n.getName());
         
         
         String read = readChars(n.getPrimaryFile(), set).replaceAll("print\\('", "").replaceAll("'\\);", "");
         String exp = readChars(xml, Charset.forName("utf-8")).replaceAll("print\\('", "").replaceAll("'\\);", "");
         assertEquals(exp, read);
         
     }

    public void testTemplateWizardCopiesItsPropertiesToMapForOverridenEntryOnMoreEntries() throws Exception {
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        
        FileObject root = fs.getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.java");
        FileObject fo2 = FileUtil.createData(root, "simpleObject.form");
        fo.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");
        fo2.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "js");

        Charset set = Charset.forName("iso-8859-2");
        OutputStream os = fo2.getOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(os, set);
        String txt = "print('skvělej tým, co nikdy neusíná - ěščřžýáíéúů')";
        w.write(txt);
        w.close();
        
        
        DataObject obj = DataObject.find(fo);
        assertEquals(TwoPartObject.class, obj.getClass());
        TwoPartObject tpo = (TwoPartObject)obj;
        tpo.encoding = set;
        
        FileObject root2 = FileUtil.createMemoryFileSystem().getRoot();
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root2, "target"));
        
        Map<String,String> parameters = Collections.singletonMap("type", "empty");
        
        FEQI.fs = root2.getFileSystem();
        FEQI.result = StandardCharsets.UTF_8;
        
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        Integer cnt = TwoPartLoader.queried.get(n.getPrimaryFile());
        assertEquals("No query", null, cnt);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex", n.getName());
        Iterator<FileObject> it = n.files().iterator();
        it.next();
        FileObject snd = it.next();
        
        long length = snd.getSize();
        if (length <= 0) {
            fail("Too small file: " + length + " for " + snd);
        }
        
        String normRead = readChars(snd, StandardCharsets.UTF_8);

        txt = txt.replaceAll("print\\('", "").replaceAll("'\\)", "") + "\n";
        
        assertEquals(txt, normRead);
    }
     
    public static final class DD extends DialogDisplayer {
        public Object notify(NotifyDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Dialog createDialog(final DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*
            return new JDialog() {
                @Deprecated
                public void show() {
                    for (Object object : descriptor.getOptions()) {
                        if (object instanceof JButton) {
                            JButton b = (JButton)object;
                            if (b.getText().equals("Finish")) {
                                descriptor.setValue(WizardDescriptor.FINISH_OPTION);
                                b.doClick();
                                return;
                            }
                        }
                    }
                    fail("Cannot find Finish button: " + Arrays.asList(descriptor.getOptions()));
                }
            };
             */
        }
    }

    public static final class FEQI extends FileEncodingQueryImplementation {
        public static FileSystem fs;
        public static Charset result;
    
        public Charset getEncoding(FileObject f) {
            try {
                if (f.getFileSystem() == fs) {
                    return result;
                }
                return null;
            } catch (FileStateInvalidException ex) {
                return null;
            }
        }
    }
    
    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders() {
            return Enumerations.<DataLoader>array(new DataLoader[] { 
                TwoPartLoader.getLoader(TwoPartLoader.class),
                SimpleLoader.getLoader(SimpleLoader.class),
            });
        }
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
        
        @Override
        public String getName() {
            return getPrimaryFile().getNameExt();
        }
    }

    static final Logger LOG = Logger.getLogger("tst.TwoPartLoader");
    public static final class TwoPartLoader extends MultiFileLoader {
        static Map<FileObject,Integer> queried = new HashMap<FileObject,Integer>();
        
        public TwoPartLoader() {
            super(TwoPartObject.class.getName ());
        }
        protected String displayName() {
            return "TwoPart";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            Integer i = queried.get(fo);
            queried.put(fo, i == null ? 1 : i + 1);
            FileObject ret;
            
            if (fo.hasExt("java") || fo.hasExt("form")) {
                ret = org.openide.filesystems.FileUtil.findBrother(fo, "java");
            } else {
                ret = null;
            }
            
            LOG.fine("findPrimaryFile for " + fo + " yeilded " + ret);
            return ret;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            LOG.info("New data object for " + primaryFile);
            return new TwoPartObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            LOG.fine("new primary entry " + primaryFile);
            return new FE(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            LOG.fine("new snd entry: " + secondaryFile);
            return new FE(obj, secondaryFile);
        }
    }
    public static final class TwoPartObject extends MultiDataObject {
        public TwoPartObject(TwoPartLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
            getCookieSet().assign(FileEncodingQueryImplementation.class, eq);
        }
        private Charset encoding;
        private FileEncodingQueryImplementation eq = new FileEncodingQueryImplementation() {

            public Charset getEncoding(FileObject file) {
                return encoding;
            }
            
        };
    }
    
}
