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

import java.awt.Dialog;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
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
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.modules.ModuleInfo;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class PropertiesProviderTest extends NbTestCase {
    static {
        System.setProperty("netbeans.full.hack", "true");
        // Ensure polyglot can be loaded cleanly and executed. Normally set by
        // org.netbeans.libs.graalsdk.impl.Installer, when loaded as a netbeans
        // module
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        System.setProperty("truffle.UseFallbackRuntime", "true");
    }

    public PropertiesProviderTest(String testName) {
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

        Lookup.getDefault().lookup(ModuleInfo.class);

        MockServices.setServices(DD.class, Pool.class, FEQI.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWeDefineTemplatesPropertiesUserProperties() throws Exception {
        FileObject props = FileUtil.getConfigFile(
            "Templates/Properties/User.properties"
        );
        if (props.getSize() < 100) {
            fail("There should be some content: " + props.getSize());
        }
    }

    public void testBasePropertiesAlwaysPresent() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        {
            OutputStream os = fo.getOutputStream();
            String txt =
                "print('<html><h1>');print(name);print('</h1>');" +
                "print('<h2>');print(date);print('</h2>');" +
                "print('<h3>');print(time);print('</h3>');" +
                "print('<h4>');print(user);print('</h4>');" +
                "print('</html>');";
            os.write(txt.getBytes());
            os.close();
        }
        fo.setAttribute("javax.script.ScriptEngine", "js");


        FileObject props = FileUtil.createData(FileUtil.getConfigRoot(),
            "Templates/Properties/my.properties"
        );

        {
            for (FileObject f : props.getChildren()) {
                f.delete();
            }

            OutputStream os = props.getOutputStream();
            String txt = "user=Yarda";
            os.write(txt.getBytes());
            os.close();
        }


        DataObject obj = DataObject.find(fo);

        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));

        Map<String,String> parameters = Collections.singletonMap("title", "Nazdar");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);

        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex.txt", n.getName());

        String res = n.getPrimaryFile().asText();

        if (res.indexOf("date") >= 0) {
            fail(res);
        }
        if (res.indexOf("time") >= 0) {
            fail(res);
        }
        if (res.indexOf("user") >= 0) {
            fail(res);
        }
        if (res.indexOf("name") >= 0) {
            fail(res);
        }

        if (res.indexOf("Yarda") == -1) {
            fail("There should be Yarda:\n" + res);
        }
    }

    public void testFolderTemplateWithProperties() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject tmplt = root.createFolder("tmplt");
        FileObject fo = FileUtil.createData(tmplt, "simpleObject.txt");
        {
            OutputStream os = fo.getOutputStream();
            String txt =
                "print('<html><h1>');print(firstName);print('</h1>');" +
                "print('<h2>');print(lastName);print('</h2>');" +
                "print('<h3>');print(title);print('</h3>');" +
                "print('</html>');";
            os.write(txt.getBytes());
            os.close();
        }
        fo.setAttribute("javax.script.ScriptEngine", "js");


        FileObject props = FileUtil.createData(FileUtil.getConfigRoot(),
            "Templates/Properties/my.properties"
        );

        {
            for (FileObject f : props.getChildren()) {
                f.delete();
            }
            props.delete();
        }

        DataObject obj = DataObject.find(tmplt);

        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));

        Map<String,String> parameters = new HashMap<>();
        parameters.put("firstName", "Yarda");
        parameters.put("lastName", "Tulach");
        parameters.put("title", "Anarchitect");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);

        assertEquals("Created in right place", folder, n.getFolder());
        assertTrue("Folder created", n instanceof DataFolder);
        assertEquals("Created with right name", "complex", n.getName());

        DataFolder nf = (DataFolder) n;
        DataObject[] arr = nf.getChildren();
        assertEquals("One object created", 1, arr.length);
        assertEquals("simpleObject.txt", arr[0].getName());

        String res = arr[0].getPrimaryFile().asText();

        if (res.indexOf("firstName") >= 0) {
            fail(res);
        }
        if (res.indexOf("lastName") >= 0) {
            fail(res);
        }
        if (res.indexOf("title") >= 0) {
            fail(res);
        }

        if (res.indexOf("Yarda") == -1) {
            fail("There should be Yarda:\n" + res);
        }
        if (res.indexOf("Tulach") == -1) {
            fail("There should be Tulach:\n" + res);
        }
        if (res.indexOf("Anarchitect") == -1) {
            fail("There should be Anarchitect:\n" + res);
        }
    }

    public static final class JustCreateFolderHandler extends CreateFromTemplateHandler {
        @Override
        protected boolean accept(CreateDescriptor desc) {
            return Boolean.TRUE.equals(desc.getTemplate().getAttribute("justCreate"));
        }

        @Override
        protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
            FileObject folder = desc.getTarget().createFolder(desc.getName());
            for (Map.Entry<String, Object> entry : desc.getParameters().entrySet()) {
                folder.setAttribute(entry.getKey(), entry.getValue());
            }
            return Collections.nCopies(1, folder);
        }

    }

    public void testFolderTemplateHandledByHandler() throws Exception {
        MockServices.setServices(JustCreateFolderHandler.class);

        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject tmplt = root.createFolder("tmplt");
        tmplt.setAttribute("justCreate", true);
        FileObject fo = FileUtil.createData(tmplt, "simpleObject.txt");
        {
            OutputStream os = fo.getOutputStream();
            String txt =
                "print('<html><h1>');print(firstName);print('</h1>');" +
                "print('<h2>');print(lastName);print('</h2>');" +
                "print('<h3>');print(title);print('</h3>');" +
                "print('</html>');";
            os.write(txt.getBytes());
            os.close();
        }
        fo.setAttribute("javax.script.ScriptEngine", "js");


        FileObject props = FileUtil.createData(FileUtil.getConfigRoot(),
            "Templates/Properties/my.properties"
        );

        {
            for (FileObject f : props.getChildren()) {
                f.delete();
            }
            props.delete();
        }

        DataObject obj = DataObject.find(tmplt);

        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));

        Map<String,String> parameters = new HashMap<>();
        parameters.put("firstName", "Yarda");
        parameters.put("lastName", "Tulach");
        parameters.put("title", "Anarchitect");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);

        assertEquals("Created in right place", folder, n.getFolder());
        assertTrue("Folder created", n instanceof DataFolder);
        assertEquals("Created with right name", "complex", n.getName());

        DataFolder nf = (DataFolder) n;
        DataObject[] arr = nf.getChildren();
        assertEquals("No object created", 0, arr.length);
        assertEquals("Yarda", nf.getPrimaryFile().getAttribute("firstName"));
        assertEquals("Tulach", nf.getPrimaryFile().getAttribute("lastName"));
        assertEquals("Anarchitect", nf.getPrimaryFile().getAttribute("title"));
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
