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
package org.netbeans.api.java.source.gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import javax.swing.JEditorPane;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.core.startup.Main;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.ClassIndexTestCase;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.save.Reindenter;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.netbeans.modules.java.source.TestUtil;

/**
 *
 * @author Pavel Flaska
 */
public abstract class GeneratorTestBase extends ClassIndexTestCase {

    private FileObject dataDir;
    
    File testFile = null;
    
    private TransactionContext testTx;
    
    public GeneratorTestBase(String aName) {
        super(aName);
    }
    
    private void deepCopy(FileObject source, FileObject targetDirectory) throws IOException {
        for (FileObject child : source.getChildren()) {
            if (child.isFolder()) {
                FileObject target = targetDirectory.createFolder(child.getNameExt());
                
                deepCopy(child, target);
            } else {
                FileUtil.copyFile(child, targetDirectory, child.getName());
            }
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        // ensure JavaKit is present, so that NbEditorDocument is eventually created.
        // it handles PositionRefs differently than PlainDocument/PlainEditorKit.
        MockMimeLookup.setInstances(MimePath.get("text/x-java"), 
                new Reindenter.Factory(), new JavaKit());
        dataDir = SourceUtilsTestUtil.makeScratchDir(this);
        FileObject dataTargetPackage = FileUtil.createFolder(dataDir, getSourcePckg());
        assertNotNull(dataTargetPackage);
        FileObject dataSourceFolder = FileUtil.toFileObject(getDataDir()).getFileObject(getSourcePckg());
        assertNotNull(dataSourceFolder);
        deepCopy(dataSourceFolder, dataTargetPackage);
        ClassPathProvider cpp = new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                if (type == ClassPath.SOURCE)
                    return ClassPathSupport.createClassPath(new FileObject[] {dataDir});
                    if (type == ClassPath.COMPILE)
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    if (type == ClassPath.BOOT)
                        return BootClassPathUtil.getBootClassPath();
                    return null;
            }
        };
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        
        SourceUtilsTestUtil.prepareTest(
                new String[] {
                    "org/netbeans/modules/java/project/ui/layer.xml", 
                    "org/netbeans/modules/project/ui/resources/layer.xml",
                    "META-INF/generated-layer.xml"
                },
                new Object[] {loader, cpp}
        );
        
        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
        ensureRootValid(dataDir.getURL());
        TestUtil.setupEditorMockServices();
        Main.initializeURLFactory();
    }
    
    public <R, P> void process(final Transformer<R, P> transformer) throws IOException {
        assertNotNull(testFile);
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        JavaSource js = JavaSource.forFileObject(testSourceFO);
        js.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws IOException {
                wc.toPhase(Phase.RESOLVED);
                SourceUtilsTestUtil2.run(wc, transformer);
            }
        }).commit();
        printFile();
    }
    
    // XXX this method could probably be removed... use standard FileUtil stuff
    private static URL getRootURL  (File f) {
        URL url = null;
        try {
            if (isArchiveFile(f)) {
                url = FileUtil.getArchiveRoot(Utilities.toURI(f).toURL());
            } else {
                url = Utilities.toURI(f).toURL();
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
    
    String getGoldenDir() {
        return getDataDir() + "/goldenfiles";
    }
    
    String getSourceDir() {
        return FileUtil.toFile(dataDir).getAbsolutePath();
    }
    
    public static File getFile(String aDataDir, String aFileName) throws FileStateInvalidException {
        String result = new File(aDataDir).getAbsolutePath() + '/' + aFileName;
        return new File(result);
    }
    
    static JavaSource getJavaSource(File aFile) throws IOException {
        FileObject testSourceFO = FileUtil.toFileObject(aFile);
        assertNotNull(testSourceFO);
        return JavaSource.forFileObject(testSourceFO);
    }

    File getTestFile() {
        return testFile;
    }
    
    void assertFiles(final String aGoldenFile) throws IOException, FileStateInvalidException {
        assertFile("File is not correctly generated.",
            getTestFile(),
            getFile(getGoldenDir(), getGoldenPckg() + aGoldenFile),
            getWorkDir()
        );
    }
    
    void printFile() throws FileNotFoundException, IOException {
        PrintStream log = getLog();
        BufferedReader in = new BufferedReader(new FileReader(getTestFile()));
        String str;
        while ((str = in.readLine()) != null) {
            log.println(str);
        }
        in.close();
    }

    protected void fileModificationTest(String code, Consumer<WorkingCopy> modification, String expected) throws Exception {
        testFile = new File(getWorkDir(), "Test.java");

        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                modification.accept(workingCopy);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(expected, res);
    }

    abstract String getGoldenPckg();

    abstract String getSourcePckg();

}
