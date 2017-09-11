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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.hints.infrastructure;

import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
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

//    public static Test suite() {
//        TestSuite suite = new TestSuite(JavaHintsProviderTest.class);
//        
//        return suite;
//    }
    
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
