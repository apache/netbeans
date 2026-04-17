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

package org.netbeans.modules.java.completion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.JEditorPane;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.usages.BinaryAnalyser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 */
public class CompletionTestBaseBase extends NbTestCase {

    static {
        JavaCompletionTaskBasicTest.class.getClassLoader().setDefaultAssertionStatus(true);
        SourceUtilsTestUtil2.disableArtificalParameterNames();
        System.setProperty("org.netbeans.modules.java.source.parsing.JavacParser.no_parameter_names", "true");
        // bump tresholds to avoid context dumps from "excessive indexing" warnings
        System.setProperty("org.netbeans.modules.parsing.impl.indexing.LogContext$EventType.PATH.treshold", "100");
        System.setProperty("org.netbeans.modules.parsing.impl.indexing.LogContext$EventType.MANAGER.treshold", "100");
    }

    static final int FINISH_OUTTIME = 5 * 60 * 1000;

    public static class Lkp extends ProxyLookup {

        private static Lkp DEFAULT;

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

    protected final AtomicReference<String> sourceLevel = new AtomicReference<>();
    private   final String goldenFilePath;

    public CompletionTestBaseBase(String testName, String goldenFilePath) {
        super(testName);
        this.goldenFilePath = goldenFilePath;
    }


    @Override
    protected void setUp() throws Exception {
        ClassPathProvider cpp = new ClassPathProvider() {
            volatile ClassPath bootCache;
            volatile ClassPath moduleBootCache;
            @Override
            public ClassPath findClassPath(FileObject file, String type) {
                try {
                    if (type.equals(ClassPath.SOURCE)) {
                        return ClassPathSupport.createClassPath(new FileObject[]{FileUtil.toFileObject(getWorkDir())});
                    }
                    if (type.equals(ClassPath.COMPILE)) {
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    }
                    if (type.equals(ClassPath.BOOT)) {
                        ClassPath cp = bootCache;
                        if (cp == null) {
                            bootCache = cp = BootClassPathUtil.getBootClassPath();
                        }
                        return cp;
                    }
                    if (type.equals(JavaClassPathConstants.MODULE_BOOT_PATH)) {
                        ClassPath cp = moduleBootCache;
                        if (cp == null) {
                            moduleBootCache = cp = BootClassPathUtil.getModuleBootPath();
                        }
                        return cp;
                    }
                } catch (IOException ex) {}
                return null;
            }
        };
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        SourceLevelQueryImplementation slq = new SourceLevelQueryImplementation() {
            @Override public String getSourceLevel(FileObject javaFile) {
                return sourceLevel.get();
            }
        };
        SourceUtilsTestUtil.prepareTest(new String[] {
            "META-INF/generated-layer.xml",
            "org/netbeans/modules/java/editor/resources/layer.xml",
            "org/netbeans/modules/defaults/mf-layer.xml"
        }, new Object[] {loader, cpp, slq});
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
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
        final ClassPath bootPath = cpp.findClassPath(FileUtil.toFileObject(getWorkDir()), ClassPath.BOOT);
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, ClassPathSupport.createClassPath(new URL[0]), sourcePath);
        assertNotNull(cpInfo);
        final JavaSource js = JavaSource.create(cpInfo);
        assertNotNull(js);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                for (ClassPath.Entry entry : bootPath.entries()) {
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
        Main.initializeURLFactory();
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("completion-case-sensitive", true);
    }

    private URL[] prepareLayers(String... paths) throws IOException {
        List<URL> layers = new LinkedList<>();

        for (int cntr = 0; cntr < paths.length; cntr++) {
            boolean found = false;

            for (Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(paths[cntr]); en.hasMoreElements(); ) {
                found = true;
                layers.add(en.nextElement());
            }

            assertTrue(paths[cntr], found);
        }

        return layers.toArray(new URL[0]);
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public File getGoldenFile(String goldenFileName) {
        File goldenFile = null;
        String version = System.getProperty("java.specification.version");
        for (String variant : computeVersionVariantsFor(version)) {
            goldenFile = new File(getDataDir(), "/goldenfiles/" + goldenFilePath + "/" + variant + "/" + goldenFileName);
            if (goldenFile.exists())
                break;
        }
        assertNotNull(goldenFile);
        return goldenFile;
    }

    private List<String> computeVersionVariantsFor(String version) {
        int dot = version.indexOf('.');
        version = version.substring(dot + 1);
        int versionNum = Integer.parseInt(version);
        List<String> versions = new ArrayList<>();

        for (int v = versionNum; v >= 8; v--) {
            versions.add(v != 8 ? "" + v : "1." + v);
        }

        return versions;
    }

    protected void copyToWorkDir(File resource, File toFile) throws IOException {
        InputStream is = new FileInputStream(resource);
        OutputStream outs = new FileOutputStream(toFile);
        int read;
        while ((read = is.read()) != (-1)) {
            outs.write(read);
        }
        outs.close();
        is.close();
    }

    protected static ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List list = new ArrayList();
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

}
