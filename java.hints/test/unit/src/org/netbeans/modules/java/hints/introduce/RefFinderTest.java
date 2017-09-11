/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * 
 * This tests checks that all references to 'this' are eventually found
 * 
 * @author sdedic
 */
public class RefFinderTest extends NbTestCase {

    public RefFinderTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil
                .prepareTest(new String[0], new Object[0]);
        super
                .setUp();
    }

    /**
     * Method invocation in a form of m()
     * @throws Exception 
     */
    public void testSimpleMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }
    
    /**
     * Invocation of superclass' method in a form m()
     * @throws Exception 
     */
    public void testSimpleInheritedMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }
    
    /**
     * Invocation of superclass' method in a form super.method()
     * @throws Exception 
     */
    public void testSuperMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }
    
    /**
     * Invocation of this class' method in a form this.method()
     * @throws Exception 
     */
    public void testThisMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }
    
    /**
     * Invocation in a form of ThisClass.this.method()
     * @throws Exception 
     */
    public void testQualifiedThisMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }
    
    /**
     * Invocation in a form of ThisClass.super.method()
     * @throws Exception 
     */
    public void testQualifiedSuperMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }
    
    /**
     * Invocation in a form of ThisClass.this.method()
     * @throws Exception 
     */
    public void testQualifiedThisInheritedMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }
    
    public void testEnclosingMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }

    public void testEnclosingThisMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }

    public void testEnclosingSuperMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }

    public void testEnclosingInheritedMethod() throws Exception {
        prepareStandardTest();
        assertTestTypeRequired();
    }


    /**
     * Invocation in a form of OuterClass.this.method()
     * @throws Exception 
     */
    public void testOuterThisMethod() throws Exception {
        prepareStandardTest("Test.I", null);
        processReferences();
        assertTestTypeRequired();
    }
    
    /**
     * Invocation in a form of OuterClass.super.method()
     * @throws Exception 
     */
    public void testOuterSuperMethod() throws Exception {
        prepareStandardTest("Test.I", null);
        processReferences();
        assertTestTypeRequired();
    }
    
    /**
     * Invocation in a form of OuterClass.this.method(), which was
     * inherited from a superclass
     * @throws Exception 
     */
    public void testOuterThisInheritedMethod() throws Exception {
        prepareStandardTest("Test.I", null);
        processReferences();
        assertTestTypeRequired();
    }
    
    public void testOuterInheritedMethod() throws Exception {
        prepareStandardTest("Test.I", null);
        processReferences();
        assertTestTypeRequired();
    }
    
    public void testOuterMethod() throws Exception {
        prepareStandardTest("Test.I", null);
        processReferences();
        assertTestTypeRequired();
    }
    
    public void testLocalClassReference() throws Exception {
        prepareStandardTest();
        assertTrue(finder.containsLocalReferences());
    }
    
    
    private void assertTestTypeRequired() {
        TypeElement el = info.getElements().getTypeElement(getMyPackageName() + ".Test");
        assertEquals(Collections.singleton(el), finder.getRequiredInstances());
    }
    
    private String getMyPackageName() {
        String pn = getClass().getName();
        pn = pn.replaceAll("\\.modules\\.", ".test.");
        return pn;
    }
    
    private ExecutableElement testMethod;
    private MethodTree testMethodTree;
    private Tree expressionTree;
    
    private InstanceRefFinder finder;
    
    private void processReferences() {
        finder = new InstanceRefFinder(info, info.getTrees().getPath(info.getCompilationUnit(), expressionTree));
        finder.process();
    }
    
    private void prepareStandardTest() throws Exception {
        prepareStandardTest("Test", null);
        processReferences();
    }
    
    private void prepareStandardTest(String className, String methodName) throws Exception {
        if (methodName == null) {
             methodName = getName();
             methodName = Character.toLowerCase(methodName.charAt(4)) + 
             methodName.substring(5);
        }
        prepareFileTest(false, "Test.java", "Base.java");
        String pn = getMyPackageName();
        TypeElement tel = info.getElements().getTypeElement(pn + "." + className);
        for (ExecutableElement e : ElementFilter.methodsIn(tel.getEnclosedElements())) {
            if (e.getSimpleName().contentEquals(methodName)) {
                testMethod = e;
                
                MethodTree  mt = (MethodTree)info.getTrees().getTree(testMethod);
                testMethodTree = mt;
                List<? extends StatementTree> stmts = mt.getBody().getStatements();
                Tree t = stmts.get(0);
                if (t.getKind() == Tree.Kind.RETURN) {
                    t = ((ReturnTree)t).getExpression();
                } else if (stmts.size() > 1) {
                    t = mt.getBody();
                }
                this.expressionTree = t;
                return;
            }
        }
        fail("Testcase method source not found");
    }
    
    
    private void prepareFileTest(boolean allowErrors, String fileName, String... additionalFiles) throws Exception {
        clearWorkDir();

        FileObject workFO = FileUtil
                .toFileObject(getWorkDir());

        assertNotNull(workFO);
        
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        String cn = getClass().getName().replaceAll("\\.", "/").replace("/modules/", "/test/");
        
        FileObject srcPackage = dataRoot.getFileObject(cn);
        

        FileObject sourceRoot = workFO
                .createFolder("src");
        FileObject buildRoot = workFO
                .createFolder("build");
        FileObject cache = workFO
                .createFolder("cache");
        FileObject dstPackage = FileUtil.createFolder(sourceRoot, cn);
        
        TestUtil.copyFiles(FileUtil.toFile(srcPackage), 
                FileUtil.toFile(dstPackage), fileName);
        
        TestUtil.copyFiles(FileUtil.toFile(srcPackage), 
                FileUtil.toFile(dstPackage), additionalFiles);
        
        
        dstPackage
                .refresh();
        FileObject data = dstPackage.getFileObject(fileName);

        SourceUtilsTestUtil
                .prepareTest(sourceRoot, buildRoot, cache);

        DataObject od = DataObject
                .find(data);
        EditorCookie ec = od
                .getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec
                .openDocument();

        doc
                .putProperty(Language.class, JavaTokenId
                .language());
        doc
                .putProperty("mimeType", "text/x-java");
        
        List<FileObject> fos = new ArrayList<FileObject>();
        fos.add(data);
        if (additionalFiles != null) {
            for (String s : additionalFiles) {
                fos.add(dstPackage.getFileObject(s));
            }
        }

        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.getURL(), null);
        JavaSource js = JavaSource
                .forFileObject(data);

        assertNotNull(js);

        
        info = SourceUtilsTestUtil
                .getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);

        if (!allowErrors) {
            assertTrue(info
                    .getDiagnostics()
                    .toString(), info
                    .getDiagnostics()
                    .isEmpty());
        }
    }

    private Document doc;
    private CompilationInfo info;
}
