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

package org.netbeans.modules.javafx2.editor;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.gen.WhitespaceIgnoringDiff;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.completion.CompletionItemComparator;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.usages.BinaryAnalyser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.javafx2.editor.parser.FxmlParserFactory;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.indexing.IndexingUtils;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater.IndexingState;
import org.netbeans.modules.xml.text.structure.XMLDocumentModelProvider;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * Copied from Java Editor Tests and adapted by David Strupl
 */
public class FXMLCompletionTestBase extends NbTestCase {
    
    static {
        FXMLCompletionTestBase.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals(Lkp.class, Lookup.getDefault().getClass());

        SourceUtilsTestUtil2.disableArtificalParameterNames();
    }

    static final int FINISH_OUTTIME = 5 * 60 * 1000;
    
    protected ClasspathInfo cpInfo;
    
    protected ClassPathProvider cpProvider;
    
    public static class Lkp extends ProxyLookup {
        
        private static Lkp DEFAULT;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public Lkp() {
            assertNull(DEFAULT);
            DEFAULT = this;
        }
        
        public static void initLookups(Object[] objs) throws Exception {
            ClassLoader l = Lkp.class.getClassLoader();
            DEFAULT.setLookups(new Lookup [] {
                Lookups.fixed(objs),
                Lookups.metaInfServices(l),
                Lookups.singleton(l)
            });
        }
    }
    
    public FXMLCompletionTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void tearDown() throws Exception {
        Field f = org.netbeans.modules.parsing.impl.Utilities.class.getDeclaredField("status");
        f.setAccessible(true);
        f.set(null, null);
        super.tearDown();
    }
    
    @Override
    protected void setUp() throws Exception {
        XMLFileSystem system = new XMLFileSystem();
        String[] initUrls = new String[] {
            "/org/netbeans/modules/javafx2/editor/resources/layer.xml",
            "/org/netbeans/modules/javafx2/editor/test/layer.xml",
            "/META-INF/generated-layer.xml",
            "/org/netbeans/modules/defaults/mf-layer.xml",
            "/org/netbeans/modules/xml/text/resources/mf-layer.xml",
        };
        Collection<URL> allUrls = new ArrayList<URL>();
        for (String u : initUrls) {
            if (u.charAt(0) == '/') {
                u = u.substring(1);
            }
            for (Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(u); en.hasMoreElements(); ) {
                allUrls.add(en.nextElement());
            }
        }
        system.setXmlUrls(allUrls.toArray(new URL[0]));
        Repository repository = new Repository(new MultiFileSystem(new FileSystem[] {FileUtil.createMemoryFileSystem(), system}));
        final ClassPath bootPath = createClassPath(System.getProperty("sun.boot.class.path"));
        final ClassPath fxPath = ClassPathSupport.createClassPath(getFxrtJarURL());
        ClassPathProvider cpp = new ClassPathProvider() {
            @Override
            public ClassPath findClassPath(FileObject file, String type) {
                try {
                    if (ClassPath.SOURCE.equals(type)) {
                        return ClassPathSupport.createClassPath(new FileObject[]{FileUtil.toFileObject(getWorkDir())});
                                }
                    if (ClassPath.COMPILE.equals(type)) {
                        return fxPath;
                    }
                    if (ClassPath.BOOT.equals(type)) {
                        return bootPath;
                    }
                } catch (IOException ex) {}
                return null;
            }
        };
        this.cpProvider = cpp;
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        MimeDataProvider mdp = new MimeDataProvider() {
            @Override
            public Lookup getLookup(MimePath mimePath) {
                if (mimePath.toString().contains("/x-fxml")) {
                    return Lookups.fixed(
                            new XMLKit(), 
                            new FxmlParserFactory(), 
                            new XMLDocumentModelProvider(), 
                            XMLTokenId.language()
                            );
                } else {
                    return Lookups.fixed(
                            new XMLKit(), 
                            new JavacParserFactory(), 
                            new XMLDocumentModelProvider(), 
                            XMLTokenId.language()
                            );
                }
            }
        };
        Lkp.initLookups(new Object[] {repository, loader, cpp /*, mdp */});
        //Collection<? extends ModuleInfo> mods = Lookup.getDefault().lookupAll(ModuleInfo.class);
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
//        JEditorPane.registerEditorKitForContentType("text/xml", "org.netbeans.modules.edit");
        final ClassPath sourcePath = ClassPathSupport.createClassPath(new FileObject[] {FileUtil.toFileObject(getDataDir())});
        final ClassIndexManager mgr  = ClassIndexManager.getDefault();
        for (ClassPath.Entry entry : sourcePath.entries()) {
            TransactionContext tx = TransactionContext.beginStandardTransaction(entry.getURL(), true, ()->true, false);
            try {
                mgr.createUsagesQuery(entry.getURL(), true);
            } finally {
                tx.commit();
            }
        }
        cpInfo = ClasspathInfo.create(bootPath, fxPath, sourcePath);
        assertNotNull(cpInfo);
        final JavaSource js = JavaSource.create(cpInfo);
        assertNotNull(js);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                List<ClassPath.Entry> entries = new ArrayList<ClassPath.Entry>(bootPath.entries());
                entries.addAll(fxPath.entries());
                for (ClassPath.Entry entry : entries) {
                    final URL url = entry.getURL();
                    TransactionContext.beginStandardTransaction(entry.getURL(), false, ()->true, false);
                    try {
                        final ClassIndexImpl cii = mgr.createUsagesQuery(url, false);
                        BinaryAnalyser ba = cii.getBinaryAnalyser();
                        ba.analyse(url);
                    } finally {
                        TransactionContext.get().commit();
                    }
                }
            }
        }, true);
        IndexingUtils.setIndexingStatus(new IndexingUtils.IndexingStatus() {
            @Override
            public Set<? extends IndexingState> getIndexingState() {
                return EnumSet.<IndexingState>noneOf(IndexingState.class);
            }
        });
    }

    private URL getFxrtJarURL() throws IOException {
        File finalDestination = new File(getDataDir(), "jfxrt.jar");
        if (!finalDestination.exists()) {
            String jfxrtUrl = System.getProperty("jfxrt.url");
            if (jfxrtUrl == null) {
                fail("jfxrt.url system property not set. It is set it in project.properties as test-unit-sys-prop.jfxrt.url=...");
            }
            URL location = new URL(jfxrtUrl);
            copyToWorkDir(location, finalDestination);
        }
        return new URL("jar:file:/"+ finalDestination.getCanonicalPath().replace("\\", "/") + "!/");
    }
        
    protected void performTest(String source, int caretPos, String textToInsert, String goldenFileName) throws Exception {
        performTest(source, caretPos, textToInsert, goldenFileName, null, null);
    }
    
    private String getClassDir() {
        String clName = getClass().getName();
        return clName.replaceAll("\\.", "/");
    }
    
    protected void performTest(String source, int caretPos, String textToInsert, String goldenFileName, String toPerformItemRE, String goldenFileName2) throws Exception {
        performTest(source, caretPos, textToInsert, CompletionProvider.COMPLETION_QUERY_TYPE, goldenFileName, toPerformItemRE, goldenFileName2);
    }
    
    protected void performTest(String source, int caretPos, String textToInsert, int queryType, String goldenFileName, String toPerformItemRE, String goldenFileName2) throws Exception {
        performTest(source, caretPos, 0, -1, textToInsert, queryType, goldenFileName, toPerformItemRE, goldenFileName2);
    }
    
    protected void performTest(String source, int caretPos, int charsDelete, int caret2Pos, String textToInsert, int queryType, String goldenFileName, String toPerformItemRE, String goldenFileName2) throws Exception {
        File testSource = new File(getWorkDir(), "test/test.fxml");
        testSource.getParentFile().mkdirs();
        copyToWorkDir(new File(getDataDir(), "org/netbeans/modules/javafx2/editor/completion/data/" + source + ".fxml"), testSource);
        FileObject testSourceFO = FileUtil.toFileObject(testSource);
        assertNotNull(testSourceFO);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        assertNotNull(testSourceDO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        assertNotNull(ec);
        final Document doc = ec.openDocument();
        assertNotNull(doc);
        doc.putProperty(Language.class, XMLTokenId.language());
        doc.putProperty("mimeType", "text/x-fxml+xml");
        int textToInsertLength = textToInsert != null ? textToInsert.length() : 0;
        if (charsDelete > 0) {
            doc.remove(caretPos, charsDelete);
        }
        if (caret2Pos != -1) {
            caretPos = caret2Pos;
        }
        if (textToInsertLength > 0)
            doc.insertString(caretPos, textToInsert, null);
        Source s = Source.create(doc);
        List<? extends CompletionItem> items = performQuery(s, queryType, caretPos + textToInsertLength, caretPos + textToInsertLength, doc);
        items.sort(CompletionItemComparator.BY_PRIORITY);
        
        File output = new File(getWorkDir(), getName() + ".out");
        Writer out = new FileWriter(output);
        List<String> sorted = new ArrayList<String>(items.size());
        for (CompletionItem item : items) {
            String itemString = item.toString();
            if (!(org.openide.util.Utilities.isMac() && itemString.equals("apple"))) { //ignoring 'apple' package
                sorted.add(itemString);
            }
        }
        Collections.sort(sorted);
        for (String itemString : sorted) {
            out.write(itemString);
            out.write("\n");
        }
        out.close();
        
        String version = System.getProperty("java.specification.version");
        version = "1.5".equals(version) ? "" : version + "/";
        
        File goldenFile = new File(getDataDir(), "/goldenfiles/" + getClassDir() + "/" + version + goldenFileName);
        File diffFile = new File(getWorkDir(), getName() + ".diff");        
        assertFile(output, goldenFile, diffFile);
        
        if (toPerformItemRE != null) {
            assertNotNull(goldenFileName2);            

            Pattern p = Pattern.compile(toPerformItemRE);
            CompletionItem item = null;            
            for (CompletionItem i : items) {
                if (p.matcher(i.toString()).find()) {
                    item = i;
                    break;
                }
            }            
            assertNotNull(item);
            
            JEditorPane editor = new JEditorPane();
            editor.setDocument(doc);
            editor.setCaretPosition(caretPos + textToInsertLength);
            item.defaultAction(editor);
            
            File output2 = new File(getWorkDir(), getName() + ".out2");
            Writer out2 = new FileWriter(output2);            
            out2.write(doc.getText(0, doc.getLength()));
            out2.close();
            
            File goldenFile2 = new File(getDataDir(), "/goldenfiles/" + getClassDir() + "/" + goldenFileName2);
            File diffFile2 = new File(getWorkDir(), getName() + ".diff2");
            
            assertFile(output2, goldenFile2, diffFile2, new WhitespaceIgnoringDiff());
        }
        
        LifecycleManager.getDefault().saveAll();
    }

    private void copyToWorkDir(File resource, File toFile) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(resource));
        OutputStream outs = new BufferedOutputStream(new FileOutputStream(toFile));
        int read;
        while ((read = is.read()) != (-1)) {
            outs.write(read);
        }
        outs.close();
        is.close();
    }
    
    private void copyToWorkDir(URL resource, File toFile) throws IOException {
        InputStream is = new BufferedInputStream(resource.openStream());
        OutputStream outs = new BufferedOutputStream(new FileOutputStream(toFile));
        int read;
        while ((read = is.read()) != (-1)) {
            outs.write(read);
        }
        outs.close();
        is.close();
    }
    
    private static ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List/*<PathResourceImplementation>*/ list = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            String item = tokenizer.nextToken();
            File f = FileUtil.normalizeFile(new File(item));
            URL url = getRootURL(f);
            if (url!=null) {
                list.add(ClassPathSupport.createResource(url));
            }
        }
        return ClassPathSupport.createClassPath(list);
    }
    
    // XXX this method could probably be removed... use standard FileUtil stuff
    private static URL getRootURL  (File f) {
        URL url = null;
        try {
            if (isArchiveFile(f)) {
                url = FileUtil.getArchiveRoot(f.toURI().toURL());
            } else {
                url = f.toURI().toURL();
                String surl = url.toExternalForm();
                if (!surl.endsWith("/")) {
                    url = new URL(surl+"/");
                }
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        return url;
    }
    
    private static boolean isArchiveFile(File f) {
        // the f might not exist and so you cannot use e.g. f.isFile() here
        String fileName = f.getName().toLowerCase();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");    //NOI18N
    }
    
    protected List<? extends CompletionItem> performQuery(Source source, int queryType, int offset, int substitutionOffset, Document doc) throws Exception {
        return query(source, queryType, offset, substitutionOffset, doc);
    }
    
    // ONLY FOR TESTS!
    static List<? extends CompletionItem> query(Source source, int queryType, int offset, int substitutionOffset, Document doc) throws Exception {
        assert source != null;
        return FXMLCompletion2.testQuery(source, doc, queryType, offset);
    }

}
