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

package org.netbeans.api.templates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class CreateFromTemplateHandlerTest extends NbTestCase {
    
    public CreateFromTemplateHandlerTest(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    protected void setUp() throws Exception {
        Hand.acceptObject = new ArrayList<FileObject>();
        Hand.fileObject = new ArrayList<FileObject>();
        Hand.origObject = new ArrayList<FileObject>();
        Hand.name = null;
        Hand.parameters = null;
        
        MockServices.setServices(Hand.class, Attr.class, Pool.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateFromTemplate() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        
        DataObject obj = DataObject.find(fo);
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.singletonMap("type", "empty");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex.txt", n.getName());
        
        assertEquals("The right source", fo, Hand.origObject.get(0));
        assertEquals("The right source in query", fo, Hand.acceptObject.get(0));
        assertEquals("The right destiny folder", folder.getPrimaryFile(), Hand.fileObject.get(0));
        assertEquals("The right name", "complex", Hand.name);
        if (Hand.parameters.size() < 2) {
            fail("As least two: " + Hand.parameters + " but was " + Hand.parameters.size());
        }
        assertEquals("empty", Hand.parameters.get("type"));
        assertEquals("complex", Hand.parameters.get("name"));
        try {
            Hand.parameters.put("kuk", "buk");
        } catch (UnsupportedOperationException ex) {
            // ok
            return;
        }
        fail("Modifications shall be unsupported");
    }
    
    /**
     * Check that a handler / annotation can be bound to a static factory method
     * @throws Exception 
     */
    public void testExplicitCreateHandlerFactory() throws Exception {
        FileBuilder fb = new FileBuilder(FileUtil.getConfigFile("Templates/Test/test1"), FileUtil.getConfigRoot());
        executedHandlerId = 0;
        assertEquals(FileUtil.getConfigRoot(), fb.build().get(0));
        assertEquals(2, executedHandlerId);
    }

    /**
     * Check that a handler / annotation can be bound to a class
     * @throws Exception 
     */
    public void testExplicitCreateHandlerClass() throws Exception {
        FileBuilder fb = new FileBuilder(FileUtil.getConfigFile("Templates/Test/test2"), FileUtil.getConfigRoot());
        executedHandlerId = 0;
        assertEquals(FileUtil.getConfigRoot(), fb.build().get(0));
        assertEquals(1, executedHandlerId);
    }
    
    /**
     * Checks that the handler class can be specified by an attribute. Static etc checks are suppressed.
     * @throws Exception 
     */
    @TemplateRegistration(category = "Test", folder = "Test", id = "test3", iconBase = "org/netbeans/modules/templates/default.gif",
            createHandlerClass = CFTH3.class)
    public void testExplicitCreateHandlerAttribute() throws Exception {
        FileBuilder fb = new FileBuilder(FileUtil.getConfigFile("Templates/Test/test3"), FileUtil.getConfigRoot());
        executedHandlerId = 0;
        assertEquals(FileUtil.getConfigRoot(), fb.build().get(0));
        assertEquals(3, executedHandlerId);
    }
    
    static int executedHandlerId;
    
    @TemplateRegistration(category = "Test", folder = "Test", id = "test1", iconBase = "org/netbeans/modules/templates/default.gif")
    public static org.netbeans.api.templates.CreateFromTemplateHandler handlerFactory() {
        return new CFTH(2);
    }
    
    @TemplateRegistration(category = "Test", folder = "Test", id = "test2", iconBase = "org/netbeans/modules/templates/default.gif")
    public static class CFTH extends org.netbeans.api.templates.CreateFromTemplateHandler {
        private final int id;
        
        public CFTH() {
            this(1);
        }
        
        CFTH(int mark) {
            this.id = mark;
        }

        @Override
        protected boolean accept(CreateDescriptor desc) {
            return true;
        }

        @Override
        protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
            executedHandlerId = id;
            return Collections.singletonList(FileUtil.getConfigRoot());
        }
    }
    
    public static class CFTH3 extends CFTH {

        public CFTH3() {
            super(3);
        }
    }
    
    public static final class Hand extends org.openide.loaders.CreateFromTemplateHandler {
        public static List<FileObject>  fileObject, origObject, acceptObject;
        public static String name;
        public static Map<String, Object> parameters;
    
        public boolean accept(FileObject fo) {
            acceptObject.add(fo);
            // we cannot be so eager, accept just file from the testCreateFromTemplate.
            return fo.getNameExt().equals("simpleObject.txt");
        }

        public FileObject createFromTemplate(
            FileObject orig, FileObject f, String n,
            Map<String, Object> p
        ) throws IOException {
            origObject.add(orig);
            fileObject.add(f);
            name = n;
            parameters = p;

            return FileUtil.copyFile(orig, f, name);
        }
    }
    
    public static final class Attr implements CreateFromTemplateAttributes {
        @Override
        public Map<String, ?> attributesFor(CreateDescriptor desc) {
            return Collections.singletonMap("name", desc.getProposedName());
        }
    }
    
    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders() {
            return Enumerations.<DataLoader>array(new DataLoader[] { 
                SimpleLoader.getLoader(SimpleLoader.class),
                TwoPartLoader.getLoader(TwoPartLoader.class),
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
        
        public String getName() {
            return getPrimaryFile().getNameExt();
        }
    }
    
    

    public static final class TwoPartLoader extends MultiFileLoader {
        public TwoPartLoader() {
            super(TwoPartObject.class.getName ());
        }
        protected String displayName() {
            return "TwoPart";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("java") || fo.hasExt("form")) {
                return org.openide.filesystems.FileUtil.findBrother(fo, "java");
            } else {
                return null;
            }
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new TwoPartObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FE(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FE(obj, secondaryFile);
        }
    }
    public static final class TwoPartObject extends MultiDataObject {
        public TwoPartObject(TwoPartLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
        }
    }
    
    public void testSubstituteNone() throws Exception {
        assertEquals("Nothing", CreateFromTemplateHandler.mapParameters("Nothing", Collections.emptyMap()));
    }
    
    public void testSubstituteSimple() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("one", "1");
        assertEquals("Test 1", CreateFromTemplateHandler.mapParameters("Test ${one}", m));
    }
    
    public void testSubstituteMore() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("one", "1");
        m.put("two", "2");
        assertEquals("Test 1 and 2 plus half", CreateFromTemplateHandler.mapParameters("Test ${one} and ${two} plus half", m));
    }
    
    public void testSubstituteRecursively() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("one", "one and ${two}");
        m.put("two", "2");
        assertEquals("Test one and 2 and 2 plus one and 2 or 2", CreateFromTemplateHandler.mapParameters("Test ${one} and ${two} plus ${half:${one} or ${half:${two}}}", m));
    }
    
    public void testSubstituteDefault() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("one", "1");
        m.put("two", "2");
        assertEquals("Test 1 and 2 plus 1/2", CreateFromTemplateHandler.mapParameters("Test ${one} and ${two} plus ${half:1/2}", m));
    }
    
    public void testSubstituteEscaped() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("one", "1");
        m.put("two", "2");
        assertEquals("Escaped \\${one} but not 2 plus escaped \\${two}", CreateFromTemplateHandler.mapParameters("Escaped \\${one} but not ${two} plus ${half:escaped \\${two}}", m));
    }
    
    public static class ReplaceHandler extends CreateFromTemplateHandler {
        @Override
        protected boolean accept(CreateDescriptor desc) {
            return desc.getTemplate().getAttribute("replaceHandler") != null;
        }

        @Override
        protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
            //Document doc = createDocument(template.getMIMEType());
            FileObject template = desc.getTemplate();
            String n = desc.getName();
            if (n == null) {
                n = desc.getProposedName();
            }
            if (!desc.hasFreeExtension() || n.indexOf('.') == -1) {
                n = n + "." + desc.getTemplate().getExt(); // NOI18N
            }
            FileObject output = FileUtil.createData(desc.getTarget(), n);
            FileLock lock = output.lock();
            Charset targetEnc = FileEncodingQuery.getEncoding(output);
            Charset sourceEnc = FileEncodingQuery.getEncoding(template);
            try (Writer w = new OutputStreamWriter(output.getOutputStream(lock), targetEnc);
                 Reader is = new InputStreamReader(template.getInputStream(), sourceEnc);
                 BufferedReader br = new BufferedReader(is);
                 PrintWriter pw = new PrintWriter(w)) {
                String l;
                while ((l = br.readLine()) != null) {
                    pw.println(mapParameters(l, desc.getParameters()));
                }
            }
            return Collections.singletonList(output);
        }
    }
    
    public void testSimpleFolderTemplate() throws Exception {
        MockLookup.setLayersAndInstances(new ReplaceHandler());
        org.netbeans.ProxyURLStreamHandlerFactory.register();
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject t = FileUtil.getConfigFile("Templates/Test/Simple");

        FileBuilder b = new FileBuilder(t, root)
                .defaultMode(FileBuilder.Mode.COPY)
                .name("BooBoo")
                .param("unchanged", "replaced");
        List<FileObject> files = b.build();
        assertFalse(files.isEmpty());

        // the 'foo.txt' was marked for open, so it should be returned
        Optional<FileObject> file = files.stream().filter(f -> f.isData()).findFirst();
        assertTrue(file.isPresent());

        FileObject foo = file.get();
        assertEquals("foo.txt", foo.getNameExt());

        // check the other files:
        FileObject rawReadme = foo.getParent().getFileObject("README.txt");
        assertNotNull(rawReadme);
        // README.txt is not interpolated - no scriptengine was specified.
        assertTrue(rawReadme.asText().contains("${unchanged}"));  // not interpolated

        FileObject substReadme = foo.getParent().getFileObject("README-freemarker.txt");
        assertNotNull(substReadme);
        // README-freemarker.txt should be processed, the parameter should be replaced.
        assertFalse(substReadme.asText().contains("${unchanged}"));  // not interpolated
        assertTrue(substReadme.asText().contains(" replaced."));  // not interpolated
    }
    
    /**
     * Checks parameter interpolation in filenames and paths.
     * @throws Exception 
     */
    public void testInterpolatedPaths() throws Exception {
        org.netbeans.ProxyURLStreamHandlerFactory.register();
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject t = FileUtil.getConfigFile("Templates/Test/InterpolatedNames");
        
        FileBuilder b = new FileBuilder(t, root)
                .defaultMode(FileBuilder.Mode.COPY)
                .name("BooBoo")
                .param("unchanged", "replaced")
                .param("packagePath", "org/netbeans/modules/templates")
                .param("package", "org.netbeans.modules.templates");
        List<FileObject> files = b.build();
        assertFalse(files.isEmpty());
        
        // the 'foo.txt' was marked for open, so it should be returned
        Optional<FileObject> file = files.stream().filter(f -> f.isData()).findFirst();
        assertTrue(file.isPresent());
        
        FileObject appSource = file.get();
        assertEquals("App.java", appSource.getNameExt());
        assertTrue(appSource.getPath().contains("org/netbeans/modules/templates/"));
    }


    @TemplateRegistration(category = "Test", folder = "Test", id = "test4", iconBase = "org/netbeans/modules/templates/default.gif")
    public static org.netbeans.api.templates.CreateFromTemplateHandler handlerFactory4() {
        return new CFTH(4) {
            @Override
            protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
                Lookup lkp = desc.getLookup();
                assertNotNull(lkp);
                assertNotNull(lkp.lookup(CreateFromTemplateHandlerTest.class));
                return super.createFromTemplate(desc);
            }
        };
    }
    
    /**
     * Checks that the lookup provided by the FileBuilder caller is available in the 
     * CreateFromTemplateHandler.
     * @throws Exception 
     */
    public void testLookupPassedToHandler() throws Exception {
        FileBuilder fb = new FileBuilder(FileUtil.getConfigFile("Templates/Test/test4"), FileUtil.getConfigRoot());
        fb.useLookup(Lookups.fixed(this));
        executedHandlerId = 0;
        fb.build();
    }
    
}
