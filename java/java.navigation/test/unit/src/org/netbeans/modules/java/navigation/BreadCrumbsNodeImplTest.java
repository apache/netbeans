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
package org.netbeans.modules.java.navigation;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class BreadCrumbsNodeImplTest extends NbTestCase {

    public BreadCrumbsNodeImplTest(String name) {
        super(name);
    }

    public void testLookup() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { | }", "test.Test>>>>");
    }
    
    public void testIfElse1() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t(int i) { if (i == 1) ; else { int i|i = 0; } }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else>>>>ii>>>>");
    }
    
    public void testIfElse2() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t(int i) { if (i == 1) ; else { | } }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else>>>>");
    }
    
    public void testIfElse3() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t(int i) { if (i == 1) ; |else { } }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else>>>>");
    }
    
    public void testIfElse4() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t(int i) { if (i == 1) ; | else { } }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font>>>>>");
    }
    
    public void testIfElse5() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t(int i) { if (i == 1) ; e|lse if (i == 2) ; else if (i == 3) ; }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else>>>>");
    }
    
    public void testIfCascade1() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t(int i) { if (i == 1) ; else if (i == 2) ; else if (i =|= 3) ; }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else if <font color=#707070>(i == 2)</font> else if <font color=#707070>(i == 3)</font>>>>>");
    }
    
    public void testInfiniteForLoop() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t() { for (;;) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(; ; )</font>>>>>");
    }
    
    public void testFor231278a() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t() { for (int i = 0, j = 1; i< 10 && j < 10; i++, j++) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(int i = 0, j = 1; i &lt; 10 &amp;&amp; j &lt; 10; i++, j++)</font>>>>>");
    }
    
    public void testFor231278b() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t() { for (int i = 0, j; i< 10; i++) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(int i = 0, j; i &lt; 10; i++)</font>>>>>");
    }
    
    public void testFor231278c() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t() { int i,j; for (i = 0, j = 1; i < 10; i++) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(i = 0, j = 1; i &lt; 10; i++)</font>>>>>");
    }
    
    public void XtestArray231278() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t() { for (Object object : new String[]{\"\"}) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(Object object : new String[]{&quot;&quot;})</font>>>>>");
    }
    
    public void test226618() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { void t(String str) { if (str.equals(\"čcč|\")) { | } }", "test.Test>>>>t>>>>if <font color=#707070>(str.equals(&quot;čcč&quot;))</font>>>>>");
    }
    
    private void performBreadcrumbsSelectionTest(String code, String golden) throws Exception {
        int caret = code.indexOf('|');
        
        prepareTest("test/Test.java", code.replace("|", ""));

        BreadcrumbsElement[] rootAndSelection = BreadCrumbsScanningTask.rootAndSelection(info, caret, new AtomicBoolean());
        List<BreadcrumbsElement> toPrint = new ArrayList<BreadcrumbsElement>();
        BreadcrumbsElement current = rootAndSelection[1];
        
        while (current != null) {
            toPrint.add(current);
            current = current.getParent();
        }
        
        toPrint.remove(toPrint.size() - 1); //do not print the root node
        
        Collections.reverse(toPrint);
        
        StringBuilder output = new StringBuilder();
        
        for (BreadcrumbsElement n : toPrint) {
            output.append(n.getHtmlDisplayName());
            output.append(">>>>");
        }

        assertEquals(golden, output.toString());
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }
    
    private CompilationInfo info;

    private void prepareTest(String filename, String code) throws Exception {
        clearWorkDir();
        
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = sourceRoot.createFolder("test");

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        FileObject testSource = FileUtil.createData(packageRoot, filename);

        assertNotNull(testSource);

        TestUtilities.copyStringToFile(FileUtil.toFile(testSource), code);

        JavaSource js = JavaSource.forFileObject(testSource);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, JavaSource.Phase.RESOLVED);

        assertNotNull(info);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    @ServiceProvider(service=FileEncodingQueryImplementation.class)
    public static final class FEQImpl extends FileEncodingQueryImplementation {
        @Override public Charset getEncoding(FileObject file) {
            return StandardCharsets.UTF_8;
        }
    }
}
