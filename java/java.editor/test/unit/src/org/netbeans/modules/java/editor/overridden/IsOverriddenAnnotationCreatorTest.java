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
package org.netbeans.modules.java.editor.overridden;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.text.StyledDocument;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**XXX: tests for multi source root is overridden annotations are missing!
 *
 * @author Jan Lahoda
 */
public class IsOverriddenAnnotationCreatorTest extends NbTestCase {
    
    private FileObject testSource;
    private JavaSource js;
    private CompilationInfo info;
    
    private static File cache;
    private static FileObject cacheFO;
    
    public IsOverriddenAnnotationCreatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);

        clearWorkDir();
        
        if (cache == null) {
            cache = new File(getWorkDir(), "cache");
            cacheFO = FileUtil.createFolder(cache);

            cache.deleteOnExit();
        }
    }
    
    private void prepareTest(String capitalizedName) throws Exception {
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
//        FileObject cacheFO = workFO.createFolder("cache");
        FileObject packageRoot = FileUtil.createFolder(sourceRoot, "org/netbeans/modules/editor/java");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        String testPackagePath = "org/netbeans/modules/editor/java/";
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

        ComputeOverriders.dependenciesOverride = Collections.singletonMap(sourceRoot.getURL(), Collections.<URL>emptyList());
        ComputeOverriders.reverseSourceRootsInOrderOverride = Arrays.asList(sourceRoot.getURL());
        SourceUtilsTestUtil2.disableArtificalParameterNames();
        SourceUtilsTestUtil2.disableConfinementTest();
    }
    
    //does not work as recursive lookup does not work:
    public void testExtendsList() throws Exception {
        doTest("TestExtendsList");
    }

    public void testOverrides() throws Exception {
        doTest("TestOverrides");
    }

    //does not work as recursive lookup does not work:
    public void testInterface() throws Exception {
        doTest("TestInterface");
    }

    public void testInterfaceImplOverride() throws Exception {
        doTest("TestInterfaceImplOverride");
    }
    
    //the "is overridden" part is currently disabled:
    public void testInterfaceImpl() throws Exception {
        doTest("TestInterfaceImpl");
    }
    
    public void testInterface2() throws Exception {
        doTest("TestInterface2");
    }

    public void testHierarchy1() throws Exception {
        doTest("TestHierarchy1");
    }

    public void testHierarchy2() throws Exception {
        doTest("TestHierarchy2");
    }

    public void testBrokenSource() throws Exception {
        doTest("TestBrokenSource");
    }
    
    public void test179540() throws Exception {
        doTest("T179540");
    }
    
    public void testTestOverriddenClassWithAnnotation() throws Exception {
        doTest("TestOverriddenClassWithAnnotation", true);
    }

    private void doTest(String name) throws Exception {
        doTest(name, false);
    }
    
    private void doTest(String name, boolean includePosition) throws Exception {
        prepareTest(name);

        DataObject testDO = DataObject.find(testSource);
        EditorCookie ec = testDO.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        StyledDocument doc = ec.openDocument();
        
        List<IsOverriddenAnnotation> annotations = new ComputeAnnotations().computeAnnotations(info, doc);
        List<String> result = new ArrayList<String>();

        for (IsOverriddenAnnotation annotation : annotations) {
            result.add(annotation.debugDump(includePosition));
        }

        Collections.sort(result);

        for (String r : result) {
            ref(r);
        }

        compareReferenceFiles();
    }
    
}
