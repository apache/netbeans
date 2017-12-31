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
package org.netbeans.modules.java.hints.generator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.junit.Assert;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.Task;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.MimeTypes;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class TestBase extends NbTestCase {

    public TestBase(String testName) {
        super(testName);
    }

    // private method for deleting a file/directory (and all its subdirectories/files)
    private static void deleteFile(File file) throws IOException {
        if (file.isDirectory() && file.equals(file.getCanonicalFile())) {
            // file is a directory - delete sub files first
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }

        }
        // file is a File :-)
        boolean result = file.delete();
        if (result == false ) {
            // a problem has appeared
            throw new IOException("Cannot delete file, file = "+file.getPath());
        }
    }

    // private method for deleting every subfiles/subdirectories of a file object
    protected static void deleteSubFiles(File file) throws IOException {
        File files[] = file.getCanonicalFile().listFiles();
        if (files != null) {
            for (File f : files) {
                deleteFile(f);
            }
        } else {
            // probably do nothing - file is not a directory
        }
    }

    protected static FileObject copyStringToFile (FileObject f, String content) throws Exception {
        OutputStream os = f.getOutputStream();
        os.write(content.getBytes("UTF-8"));
        os.close ();

        return f;
    }

    protected static String copyFileToString (FileObject f) throws Exception {
        return new String(f.asBytes(), "UTF-8");
    }

    protected FileObject setUpTest() throws Exception {
        List<URL> layers = new LinkedList<URL>();

        for (String layer : new String[] {"META-INF/generated-layer.xml"}) {
            boolean found = false;

            for (Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(layer); en.hasMoreElements(); ) {
                found = true;
                layers.add(en.nextElement());
            }

            Assert.assertTrue(layer, found);
        }

        Lookup metaInfLookup = Lookups.metaInfServices(PatternGeneratorTest.class.getClassLoader());

        ((TestLookup) Lookup.getDefault()).setLookupsImpl(metaInfLookup);

        XMLFileSystem xmlFS = new XMLFileSystem();
        xmlFS.setXmlUrls(layers.toArray(new URL[0]));

        FileSystem system = new MultiFileSystem(new FileSystem[] {FileUtil.createMemoryFileSystem(), xmlFS});

        Repository repository = new Repository(system);

        assertEquals(Lookup.getDefault().getClass().getCanonicalName(), TestLookup.class, Lookup.getDefault().getClass());

        File workDir = getWorkDir();
        deleteSubFiles(workDir);
        FileUtil.refreshFor(workDir);

        FileObject wd = FileUtil.toFileObject(workDir);

        assertNotNull(wd);

        FileObject sourceRoot = FileUtil.createFolder(wd, "src");
        FileObject buildRoot = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");

        CacheFolder.setCacheFolder(cache);

        ClassPath sourcePath = ClassPathSupport.createClassPath(sourceRoot);

        ((TestLookup) Lookup.getDefault()).setLookupsImpl(
            Lookups.fixed(repository,
                          new TestProxyClassPathProvider(buildRoot, sourcePath, ClassPathSupport.createClassPath(new URL[0])),
                          new TestSourceForBinaryQuery(sourceRoot, buildRoot),
                          new TestSourceLevelQueryImplementation(),
                          JavaDataLoader.findObject(JavaDataLoader.class, true),
                          new JavaCustomIndexer.Factory()),
            metaInfLookup,
            Lookups.singleton(PatternGeneratorTest.class.getClassLoader())
        );

        Set<String> amt = MimeTypes.getAllMimeTypes();
        if (amt == null) {
            amt = new HashSet<String>();
        } else {
            amt = new HashSet<String>(amt);
        }
        amt.add("text/x-java");
        MimeTypes.setAllMimeTypes(amt);
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();

        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {ClassPathSupport.createClassPath(sourceRoot)});

        SourceUtilsTestUtil2.disableConfinementTest();
        Main.initializeURLFactory();

        return sourceRoot;
    }

    private static class TempPreferences extends AbstractPreferences {

        /*private*/Properties properties;

        private TempPreferences() {
            super(null, "");
        }

        private  TempPreferences(TempPreferences parent, String name)  {
            super(parent, name);
            newNode = true;
        }

        protected final String getSpi(String key) {
            return properties().getProperty(key);
        }

        protected final String[] childrenNamesSpi() throws BackingStoreException {
            return new String[0];
        }

        protected final String[] keysSpi() throws BackingStoreException {
            return properties().keySet().toArray(new String[0]);
        }

        protected final void putSpi(String key, String value) {
            properties().put(key,value);
        }

        protected final void removeSpi(String key) {
            properties().remove(key);
        }

        protected final void removeNodeSpi() throws BackingStoreException {}
        protected  void flushSpi() throws BackingStoreException {}
        protected void syncSpi() throws BackingStoreException {
            properties().clear();
        }

        @Override
        public void put(String key, String value) {
            try {
                super.put(key, value);
            } catch (IllegalArgumentException iae) {
                if (iae.getMessage().contains("too long")) {
                    // Not for us!
                    putSpi(key, value);
                } else {
                    throw iae;
                }
            }
        }

        Properties properties()  {
            if (properties == null) {
                properties = new Properties();
            }
            return properties;
        }

        protected AbstractPreferences childSpi(String name) {
            return new TempPreferences(this, name);
        }
    }

    private static class TestSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

        private final FileObject sourceRoot;
        private final FileObject buildRoot;

        public TestSourceForBinaryQuery(FileObject sourceRoot, FileObject buildRoot) {
            this.sourceRoot = sourceRoot;
            this.buildRoot = buildRoot;
        }

        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            FileObject f = URLMapper.findFileObject(binaryRoot);

            if (buildRoot.equals(f)) {
                return new SourceForBinaryQuery.Result() {
                    public FileObject[] getRoots() {
                        return new FileObject[] {
                            sourceRoot,
                        };
                    }

                    public void addChangeListener(ChangeListener l) {
                    }

                    public void removeChangeListener(ChangeListener l) {
                    }
                };
            }

            return null;
        }

    }

    private static Logger log = Logger.getLogger(TestBase.class.getName());

    private class TestProxyClassPathProvider implements ClassPathProvider {

        private final FileObject buildRoot;
        private final ClassPath sourcePath;
        private final ClassPath compileClassPath;

        public TestProxyClassPathProvider(FileObject buildRoot, ClassPath sourcePath, ClassPath compileClassPath) {
            this.buildRoot = buildRoot;
            this.sourcePath = sourcePath;
            this.compileClassPath = compileClassPath;
        }

        public ClassPath findClassPath(FileObject file, String type) {
            try {
            if (ClassPath.BOOT == type) {
                return TestUtil.getBootClassPath();
            }

            if (ClassPath.SOURCE == type) {
                return sourcePath;
            }

            if (ClassPath.COMPILE == type) {
                return compileClassPath;
            }

            if (ClassPath.EXECUTE == type) {
                return ClassPathSupport.createClassPath(new FileObject[] {
                    buildRoot
                });
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private class TestSourceLevelQueryImplementation implements SourceLevelQueryImplementation {

        private final String sourceLevel = "1.5";

        public String getSourceLevel(FileObject javaFile) {
            return sourceLevel;
        }

    }
    
    static {
        System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
        Assert.assertEquals(TestLookup.class, Lookup.getDefault().getClass());
    }

    private static class DeadlockTask implements Task<CompilationController> {

        private final Phase phase;
        private CompilationInfo info;

        public DeadlockTask(Phase phase) {
            assert phase != null;
            this.phase = phase;
        }

        public void run( CompilationController info ) {
            try {
                info.toPhase(this.phase);
                this.info = info;
            } catch (IOException ioe) {
                if (log.isLoggable(Level.SEVERE))
                    log.log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }

    }
    @ServiceProvider(service = Lookup.class)
    public static final class TestLookup extends ProxyLookup {

        public void setLookupsImpl(Lookup... lookups) {
            setLookups(lookups);
        }

    }

    @ServiceProvider(service=MimeDataProvider.class)
    public static final class JavacParserProvider implements MimeDataProvider {

        private Lookup javaLookup = Lookups.fixed(new JavacParserFactory(), new JavaCustomIndexer.Factory());

        public Lookup getLookup(MimePath mimePath) {
            if (mimePath.getPath().endsWith(JavacParser.MIME_TYPE)) {
                return javaLookup;
            }

            return Lookup.EMPTY;
        }

    }

    @ServiceProvider(service=MIMEResolver.class)
    public static final class JavaMimeResolver extends MIMEResolver {

        public JavaMimeResolver() {
            super(JavacParser.MIME_TYPE);
        }

        @Override
        public String findMIMEType(FileObject fo) {
            if ("java".equals(fo.getExt())) {
                return JavacParser.MIME_TYPE;
            }

            return null;
        }

    }
}
