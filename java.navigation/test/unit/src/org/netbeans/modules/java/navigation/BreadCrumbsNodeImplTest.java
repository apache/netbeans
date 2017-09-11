/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
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
        performBreadcrumbsSelectionTest("package test; public class Test { t(int i) { if (i == 1) ; else { int i|i = 0; } }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else>>>>ii>>>>");
    }
    
    public void testIfElse2() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t(int i) { if (i == 1) ; else { | } }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else>>>>");
    }
    
    public void testIfElse3() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t(int i) { if (i == 1) ; |else { } }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else>>>>");
    }
    
    public void testIfElse4() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t(int i) { if (i == 1) ; | else { } }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font>>>>>");
    }
    
    public void testIfElse5() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t(int i) { if (i == 1) ; e|lse if (i == 2) ; else if (i == 3) ; }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else>>>>");
    }
    
    public void testIfCascade1() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t(int i) { if (i == 1) ; else if (i == 2) ; else if (i =|= 3) ; }", "test.Test>>>>t>>>>if <font color=#707070>(i == 1)</font> else if <font color=#707070>(i == 2)</font> else if <font color=#707070>(i == 3)</font>>>>>");
    }
    
    public void testInfiniteForLoop() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t() { for (;;) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(; ; )</font>>>>>");
    }
    
    public void testFor231278a() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t() { for (int i = 0, j = 1; i< 10 && j < 10; i++, j++) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(int i = 0, j = 1; i &lt; 10 &amp;&amp; j &lt; 10; i++, j++)</font>>>>>");
    }
    
    public void testFor231278b() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t() { for (int i = 0, j; i< 10; i++) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(int i = 0, j; i &lt; 10; i++)</font>>>>>");
    }
    
    public void testFor231278c() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t() { int i,j; for (i = 0, j = 1; i < 10; i++) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(i = 0, j = 1; i &lt; 10; i++)</font>>>>>");
    }
    
    public void XtestArray231278() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t() { for (Object object : new String[]{\"\"}) { | } }", "test.Test>>>>t>>>>for <font color=#707070>(Object object : new String[]{&quot;&quot;})</font>>>>>");
    }
    
    public void test226618() throws Exception {
        performBreadcrumbsSelectionTest("package test; public class Test { t(String str) { if (str.equals(\"čcč|\")) { | } }", "test.Test>>>>t>>>>if <font color=#707070>(str.equals(&quot;čcč&quot;))</font>>>>>");
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
            return Charset.forName("UTF-8");
        }
    }
}
