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
package org.netbeans.modules.java.editor.imports;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.test.support.MemoryValidator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.editor.imports.ComputeImports.Pair;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class ComputeImportsTest extends NbTestCase {
    
    private static final Set<String> IGNORE_CLASSES = new HashSet<String>(Arrays.asList(new String[] {
        "com.sun.xml.bind.v2.schemagen.xmlschema.List",
        "com.sun.xml.txw2.Document",
        "com.sun.xml.internal.txw2.Document",
        "com.sun.xml.internal.bind.v2.schemagen.xmlschema.List",
        "com.sun.xml.internal.ws.wsdl.writer.document.Documented",
        "com.sun.xml.internal.bind.v2.model.core.Element",
        "com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl.Element",
        "com.sun.xml.internal.bind.v2.schemagen.xmlschema.Element",
        "sun.text.normalizer.RangeValueIterator.Element",
        "javax.xml.bind.Element",
        "javax.lang.model.element.Element",
        "com.sun.org.apache.xalan.internal.xsltc.runtime.AttributeList",
        "com.sun.xml.internal.ws.api.server.Adapter.Toolkit",
        "sunw.io.Serializable",
        "sun.rmi.transport.Target",
        "com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Element",
        "com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections"
    }));
    
    private FileObject testSource;
    private JavaSource js;
    private CompilationInfo info;
    
    private static File cache;
    private static FileObject cacheFO;
    
    public ComputeImportsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        
        clearWorkDir();
        
        if (cache == null) {
            cache = new File(getWorkDir(), "cache");
            cacheFO = FileUtil.createFolder(cache);

            IndexUtil.setCacheFolder(cache);

            TestUtilities.analyzeBinaries(SourceUtilsTestUtil.getBootClassPath());
        }
    }
    
    public static Test suite() {
        return MemoryValidator.wrap(new TestSuite(ComputeImportsTest.class));
    }
    
    public void testSimple() throws Exception {
        doTest("TestSimple");
    }
    
    public void testFilterDeclaration() throws Exception {
        doTest("TestFilterDeclaration");
    }
    
    public void testFilterTypedInitializator() throws Exception {
        doTest("TestFilterTypedInitializator");
    }
    
    public void testFilterWithMethods() throws Exception {
        doTest("TestFilterWithMethods");
    }
    
    public void testGetCookie() throws Exception {
        doTest("TestGetCookie");
    }
    
    public void testNew() throws Exception {
        doTest("TestNew");
    }
    
    public void testException() throws Exception {
        doTest("TestException");
    }
    
    public void testEmptyCatch() throws Exception {
        doTest("TestEmptyCatch");
    }
    
    public void testUnfinishedMethod() throws Exception {
        doTest("TestUnfinishedMethod");
    }
    
    public void testUnsupportedOperation1() throws Exception {
        doTest("TestUnsupportedOperation1");
    }
    
    public void testPackageDoesNotExist() throws Exception {
        doTest("TestPackageDoesNotExist");
    }

    public void testUnfinishedMethod2() throws Exception {
        doTest("TestUnfinishedMethod2");
    }
    
    public void testAnnotation() throws Exception {
        doTest("TestAnnotation");
    }
    
    public void testAnnotation2() throws Exception {
        doTest("TestAnnotation2");
    }
    
    public void test90743() throws Exception {
        doTest("Test90743");
    }
    
    public void test97420() throws Exception {
        doTest("Test97420");
    }
    
    public void test102613() throws Exception {
        doTest("Test102613");
    }
    
    public void testFilterByKind() throws Exception {
        doTest("TestFilterByKind");
    }

    public void test202604() throws Exception {
        doTest("Test202604");
    }

    public void testBrokenLambdaParameter() throws Exception {
        doTest("TestBrokenLambdaParameter");
    }
    
    public void testStaticImports1() throws Exception {
        doTest("StaticImports1");
    }
    
    public void testStaticImportsArrays1() throws Exception {
        doTest("StaticImportsArrays1");
    }
    
    public void testStaticImportsArrays2() throws Exception {
        doTest("StaticImportsArrays2");
    }
    
    public void test232647() throws Exception {
        doTest("Test232647");
    }
    
    public void testStaticImports233117() throws Exception {
        doTest("StaticImports233117", "1.8");
    }
    
    private void prepareTest(String capitalizedName, String sourceLevel) throws Exception {
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
//        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = FileUtil.createFolder(sourceRoot, "org/netbeans/modules/java/editor/imports/data");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        String testPackagePath = "org/netbeans/modules/java/editor/imports/data/";
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
        
        SourceUtilsTestUtil.setSourceLevel(testSource, sourceLevel);
        
        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private void dump(PrintStream out, Map<String, List<Element>> set, Set<String> masks) {
        List<String> keys = new LinkedList<String>(set.keySet());
        
        Collections.sort(keys);
        
        for (String key : keys) {
            List<String> fqns = new ArrayList<String>();
            
            for (Element t : set.get(key)) {
                String fqn;
                
                if (t.getKind().isClass() || t.getKind().isInterface()) {
                    fqn = ((TypeElement) t).getQualifiedName().toString();
                } else {
                    StringBuilder fqnSB = new StringBuilder();
                    
                    fqnSB.append(((TypeElement) t.getEnclosingElement()).getQualifiedName());
                    fqnSB.append('.');
                    fqnSB.append(t.getSimpleName());
                    
                    if (t.getKind() == ElementKind.METHOD) {
                        fqnSB.append('(');
                        boolean first = true;
                        for (VariableElement var : ((ExecutableElement) t).getParameters()) {
                            if (!first) {
                                fqnSB.append(", ");
                            }
                            fqnSB.append(info.getTypes().erasure(var.asType()).toString());
                            first = false;
                        }
                        fqnSB.append(')');
                    }
                    
                    fqn = fqnSB.toString();
                }
                
                if (!masks.contains(fqn))
                    fqns.add(fqn);
            }
            
            Collections.sort(fqns);
            
            out.println(key + ":" + fqns.toString());
        }
    }
    
    private void doTest(String name) throws Exception {
        doTest(name, "1.5");
    }
    
    private void doTest(String name, String sourceLevel) throws Exception {
        prepareTest(name, sourceLevel);
        
        DataObject testDO = DataObject.find(testSource);
        EditorCookie ec = testDO.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        Document doc = ec.openDocument();
        
        Pair<Map<String, List<Element>>, Map<String, List<Element>>> candidates = new ComputeImports(info).computeCandidates();
        
        dump(getLog(getName() + "-unfiltered.ref"), candidates.b, IGNORE_CLASSES);
        dump(getLog(getName() + "-filtered.ref"), candidates.a, IGNORE_CLASSES);

        String version = System.getProperty("java.specification.version") + "/";
        
        compareReferenceFiles(getName() + "-unfiltered.ref", version + getName() + "-unfiltered.pass", getName() + "-unfiltered.diff");
        compareReferenceFiles(getName() + "-filtered.ref", version + getName() + "-filtered.pass", getName() + "-filtered.diff");
    }
}
