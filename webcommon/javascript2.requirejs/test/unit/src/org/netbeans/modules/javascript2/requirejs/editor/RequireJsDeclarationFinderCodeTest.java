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

package org.netbeans.modules.javascript2.requirejs.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.netbeans.modules.javascript2.requirejs.TestProjectSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Pisl
 */
public class RequireJsDeclarationFinderCodeTest extends JsTestBase {

    public RequireJsDeclarationFinderCodeTest(String testName) {
        super(testName);
    }
    
    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp(); 
        FileObject folder = getTestFile("SimpleRequire");
        Project testProject = new TestProjectSupport.TestProject(folder, null);
        List lookupAll = new ArrayList();
        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
        lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(testProject));
        MockLookup.setInstances(lookupAll.toArray());
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>();
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/SimpleRequire/public_html/")));
        return Collections.singletonMap(
                JS_SOURCE_ID,
                ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[0]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }

    @Override
    protected boolean cleanCacheDir() {
        return false;
    }
    
    // TODO the test are randomly failing. Needs to be corrected. Time issue. 
    
    
    @Test
    public void testIssue245034_01() throws Exception {    
//        checkDeclaration("SimpleRequire/public_html/js/bbb/def.js", "newFunc.an^atomy;", "newFunction.js", 962); 
    }
    
//    @Test
//    public void testIssue245034_02() throws Exception {
//        Source testSource = getTestSource(getTestFile("SimpleRequire/public_html/js/bbb/def.js"));
//        ParserManager.parseWhenScanFinished(Collections.singleton(testSource), new UserTask() {
//
//            @Override
//            public void run(ResultIterator resultIterator) throws Exception {
//                checkDeclaration("SimpleRequire/public_html/js/bbb/def.js", "newFunc.anatomy.ey^es();", "newFunction.js", 962);
//            }
//        });
//        
//    }
//    
//    public void testIssue245034_03() throws Exception {
//        checkDeclaration("SimpleRequire/public_html/js/bbb/def.js", "newFunc.anatomy.he^ads.leftOnes;", "newFunction.js", 962);
//    }
//    
//    public void testIssue245034_04() throws Exception {
//        checkDeclaration("SimpleRequire/public_html/js/bbb/def.js", "newFunc.anatomy.heads.left^Ones;", "newFunction.js", 962);
//    }
//    
//    public void testIssue245034_05() throws Exception {
//        checkDeclaration("SimpleRequire/public_html/js/bbb/def.js", "newFunc.getSt^uff().c().x;", "newFunction.js", 962);
//    }
//    
//    public void testIssue245034_06() throws Exception {
//        checkDeclaration("SimpleRequire/public_html/js/bbb/def.js", "newFunc.getWan^nabeDate();", "newFunction.js", 962);
//    }
//    
//    public void testIssue245034_07() throws Exception {
//        checkDeclaration("SimpleRequire/public_html/js/bbb/def.js", "newFunc.bi^rth;", "newFunction.js", 962);
//    }
//    
    
}
