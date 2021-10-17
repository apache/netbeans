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

package org.netbeans.modules.java.hints.infrastructure;

import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.spiimpl.TestUtilities;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.java.completion.TreeShims;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorHintsProviderTest extends NbTestCase {
    
    public ErrorHintsProviderTest(String testName) {
        super(testName);
    }

    private FileObject testSource;
    private JavaSource js;
    private CompilationInfo info;
    
    private static File cache;
    private static FileObject cacheFO;

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        clearWorkDir();
        
        if (cache == null) {
            cache = new File(getWorkDir(), "cache");
            cacheFO = FileUtil.createFolder(cache);
            IndexUtil.setCacheFolder(cache);
        }

        RepositoryUpdater.getDefault().start(true);
        ClassPath empty = ClassPathSupport.createClassPath(new FileObject[0]);
        JavaSource.create(ClasspathInfo.create(empty, empty, empty)).runWhenScanFinished(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {}
        }, true).get();
    }
    
    private void prepareTest(String capitalizedName) throws Exception {
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
//        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = FileUtil.createFolder(sourceRoot, "javahints");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        String testPackagePath = "javahints/";
        File   testPackageFile = new File(getDataDir(), testPackagePath);
        
        String[] names = testPackageFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".java"))
                    return true;
                
                return false;
            }
        });
        
        String[] files = new String[names.length];
        
        for (int cntr = 0; cntr < files.length; cntr++) {
            files[cntr] = testPackagePath + names[cntr];
        }
        
        TestUtil.copyFiles(getDataDir(), FileUtil.toFile(sourceRoot), files);
        
        packageRoot.refresh();

        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        testSource = packageRoot.getFileObject(capitalizedName + ".java");
        
        assertNotNull(testSource);
        
        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private void performTest(String name, boolean specialMacTreatment) throws Exception {
        prepareTest(name);
        
        DataObject testData = DataObject.find(testSource);
        EditorCookie ec = testData.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        for (ErrorDescription ed : new ErrorHintsProvider().computeErrors(info, doc, Utilities.JAVA_MIME_TYPE))
            ref(ed.toString().replaceAll("\\p{Space}*:\\p{Space}*", ":"));

        if (!org.openide.util.Utilities.isMac() && specialMacTreatment) {
            compareReferenceFiles(this.getName()+".ref",this.getName()+"-nonmac.pass",this.getName()+".diff");
        } else {
            compareReferenceFiles();
        }
    }
    
    public void testShortErrors1() throws Exception {
        performTest("TestShortErrors1", false);
    }
    
    public void testShortErrors2() throws Exception {
        performTest("TestShortErrors2", false);
    }
    
    public void testShortErrors3() throws Exception {
        performTest("TestShortErrors3", false);
    }

    public void testShortErrors4() throws Exception {
        performTest("TestShortErrors4", false);
    }
    
    public void testShortErrors5() throws Exception {
        performTest("TestShortErrors5", true);
    }
    
    public void testShortErrors6() throws Exception {
        performTest("TestShortErrors6", false);
    }
    
    public void testShortErrors7() throws Exception {
        performTest("TestShortErrors7", false);
    }
    
    public void testShortErrors8() throws Exception {
        performTest("TestShortErrors8", false);
    }
    
    public void testShortErrors9() throws Exception {
        performTest("TestShortErrors9", false);
    }

    public void testShortErrors10() throws Exception {
        performTest("TestShortErrors10", false);
    }
    
    public void testTestShortErrorsMethodInvocation1() throws Exception {
        performTest("TestShortErrorsMethodInvocation1", true);
    }
    
    public void DISABLED194510testTestShortErrorsMethodInvocation2() throws Exception {
        performTest("TestShortErrorsMethodInvocation2", true);
    }
    
    public void testTestShortErrorsNewClass() throws Exception {
        performTest("TestShortErrorsNewClass", true);
    }
    
    public void XtestTestShortErrorsNewClass2() throws Exception {
        performTest("TestShortErrorsNewClass2", true);
    }

    //TODO: fix
//    public void testTestShortErrorsPrivateAccess() throws Exception {
//        performTest("TestShortErrorsPrivateAccess");
//    }
    
    public void testTestShortErrorsSVUIDWarning() throws Exception {
        TestCompilerSettings.commandLine = "-Xlint:serial";

        try {
            performTest("TestShortErrorsSVUIDWarning", true);
        } finally {
            TestCompilerSettings.commandLine = null;
        }
    }

    public void testTestSpaceAfterDot218655() throws Exception {
        performTest("TestSpaceAfterDot", false);
    }
    
    public void testTestUnicodeError() throws Exception {
        //only run this test with javac 17 and higher, there were adjustments to the
        //diagnostics in previous versions:
        try {
            SourceVersion.valueOf("RELEASE_17");
        } catch (IllegalArgumentException ex) {
            return ;
        }
        performTest("TestUnicodeError", false);
    }
    
    public void testOverrideAnnotation() throws Exception {
        performTest("TestOverrideAnnotation", false);
    }
    
    public void testTestClassNameNotMatchingFileName() throws Exception {
        performInlinedTest("test/Test.java",
                           "package javahints;\n" +
                           "public class |A| {\n" +
                           "    public A() {}\n" +
                           "}\n");
    }

    private void performInlinedTest(String name, String code) throws Exception {
        int[] expectedSpan = new int[2];
        code = TestUtilities.detectOffsets(code, expectedSpan);
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        testSource = FileUtil.createData(sourceRoot, name);
        
        assertNotNull(testSource);
        
        org.netbeans.api.java.source.TestUtilities.copyStringToFile(testSource, code);
        
        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
        
        DataObject testData = DataObject.find(testSource);
        EditorCookie ec = testData.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        List<Integer> actual = new ArrayList<>();
        
        for (ErrorDescription ed : new ErrorHintsProvider().computeErrors(info, doc, Utilities.JAVA_MIME_TYPE)) {
            actual.add(ed.getRange().getBegin().getOffset());
            actual.add(ed.getRange().getEnd().getOffset());
        }
        
        List<Integer> golden = new ArrayList<>();
        
        for (int e : expectedSpan) {
            golden.add(e);
        }
        
        assertEquals(golden, actual);
    }

}
