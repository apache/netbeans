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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class SuspiciousNamesCombinationTest extends NbTestCase {
    
    public SuspiciousNamesCombinationTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testMethodInvocation1() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {int height = 0; a(height);} public void a(int width){}}", Collections.singletonList("0:68-0:74:verifier:Suspicious names combination"));
    }

    public void testMethodInvocation2() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {int y = 0; a(y);} public void a(int x){}}", Collections.singletonList("0:63-0:64:verifier:Suspicious names combination"));
    }
    
    public void testMethodInvocation3() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {a(getY());} public void a(int x){} public int getY() {return 0;}}", Collections.singletonList("0:52-0:58:verifier:Suspicious names combination"));
    }
    
    public void testMethodInvocation4() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {a(this.getY());} public void a(int x){} public int getY() {return 0;}}", Collections.singletonList("0:52-0:63:verifier:Suspicious names combination"));
    }
    
    public void testMethodInvocation5() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {a(this.y);} public void a(int x){} public int y = 0;}", Collections.singletonList("0:52-0:58:verifier:Suspicious names combination"));
    }
    
    public void testAssignment() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {int x = 0; int y = 0; x = y;}}", Collections.singletonList("0:72-0:73:verifier:Suspicious names combination"));
    }
    
    public void testVariableDeclaration1() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {int x = 0; int y = x;}}", Collections.singletonList("0:65-0:66:verifier:Suspicious names combination"));
    }
    
    public void testVariableDeclaration2() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {int x; int y;}}", Collections.<String>emptyList());
    }
    
    public void testCorrect() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {int x = getX();} public int getX() {return 0;}}", Collections.<String>emptyList());
    }
    
    public void testNameBroker() {
        assertEquals(new HashSet(Arrays.asList("x")), SuspiciousNamesCombination.breakName("x"));
        assertEquals(new HashSet(Arrays.asList("x", "get")), SuspiciousNamesCombination.breakName("getX"));
        assertEquals(new HashSet(Arrays.asList("getx")), SuspiciousNamesCombination.breakName("getx"));
        assertEquals(new HashSet(Arrays.asList("get", "x")), SuspiciousNamesCombination.breakName("get-x"));
        assertEquals(new HashSet(Arrays.asList("get", "x")), SuspiciousNamesCombination.breakName("get--x"));
    }
    
    protected void prepareTest(String code) throws Exception {
        FileObject workFO = makeScratchDir(this);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");
        
        writeIntoFile(data, code);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        ec.openDocument();
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private CompilationInfo info;
    
    protected void performTestAnalysisTest(String code, List<String> golden) throws Exception {
        prepareTest(code);
        
        final SuspiciousNamesCombination snc = new SuspiciousNamesCombination();
        final List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        
        class ScannerImpl extends ErrorAwareTreePathScanner {
            @Override
            public Object scan(Tree tree, Object p) {
                if (tree != null && snc.getTreeKinds().contains(tree.getKind())) {
                    List<ErrorDescription> localErrors = snc.run(info, new TreePath(getCurrentPath(), tree));
                    
                    if (localErrors != null) {
                        errors.addAll(localErrors);
                    }
                }
                return super.scan(tree, p);
            }
        };
        
        new ScannerImpl().scan(info.getCompilationUnit(), null);
        
        List<String> errorDisplaNames = new ArrayList<String>();
        
        for (ErrorDescription ed : errors) {
            errorDisplaNames.add(ed.toString());
        }
        
        assertEquals(golden, errorDisplaNames);
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
}
