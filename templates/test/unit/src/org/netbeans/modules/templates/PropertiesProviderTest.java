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
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.templates;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
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
        
        String res = readFile(n.getPrimaryFile());
        
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
    
    private static String readFile(FileObject fo) throws IOException {
        byte[] arr = new byte[(int)fo.getSize()];
        int len = fo.getInputStream().read(arr);
        assertEquals("Fully read", arr.length, len);
        return new String(arr);
    }

    private static String readChars(FileObject fo, Charset set) throws IOException {
        CharBuffer arr = CharBuffer.allocate((int)fo.getSize() * 2);
        BufferedReader r = new BufferedReader(new InputStreamReader(fo.getInputStream(), set));
        while (r.read(arr) != -1) {
            // again
        }
        r.close();
        
        arr.flip();
        return arr.toString();
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
