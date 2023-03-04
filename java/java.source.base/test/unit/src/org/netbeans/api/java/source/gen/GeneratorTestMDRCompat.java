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
import javax.swing.JEditorPane;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.ClassIndexTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.save.Reindenter;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;

/**
 *
 * @author Pavel Flaska
 */
public abstract class GeneratorTestMDRCompat extends ClassIndexTestCase {

    File testFile = null;
    
    public GeneratorTestMDRCompat(String aName) {
        super(aName);
    }
    
    @Override
    protected void setUp() throws Exception {
        ClassPathProvider cpp = new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                if (type == ClassPath.SOURCE)
                    return ClassPathSupport.createClassPath(getSourcePath());
                    if (type == ClassPath.COMPILE)
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    if (type == ClassPath.BOOT)
                        return BootClassPathUtil.getBootClassPath();
                    if (type == JavaClassPathConstants.MODULE_BOOT_PATH)
                        return BootClassPathUtil.getModuleBootPath();
                    return null;
            }
        };
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        SourceLevelQueryImplementation slq = new SourceLevelQueryImplementation() {
            @Override public String getSourceLevel(FileObject javaFile) {
                return GeneratorTestMDRCompat.this.getSourceLevel();
            }
        };
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/source/resources/layer.xml"}, new Object[] {loader, cpp, slq});
        MockMimeLookup.setInstances(MimePath.get("text/x-java"), new Reindenter.Factory());
        
        TestUtil.setupEditorMockServices();
        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
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
    
//    static ClassPath createClassPath(String classpath) {
//        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
//        List/*<PathResourceImplementation>*/ list = new ArrayList();
//        while (tokenizer.hasMoreTokens()) {
//            String item = tokenizer.nextToken();
//            File f = FileUtil.normalizeFile(new File(item));
//            URL url = getRootURL(f);
//            if (url!=null) {
//                list.add(ClassPathSupport.createResource(url));
//            }
//        }
//        return ClassPathSupport.createClassPath(list);
//    }
    
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
        return getDataDir().getAbsolutePath();
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

    abstract String getGoldenPckg();

    abstract String getSourcePckg();
    
    String getSourceLevel() {
        return null;
    }

    FileObject[] getSourcePath() {
        return new FileObject[] {FileUtil.toFileObject(getDataDir())};
    }
}
