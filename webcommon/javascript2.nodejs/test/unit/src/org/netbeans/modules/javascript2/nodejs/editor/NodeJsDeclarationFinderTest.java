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

package org.netbeans.modules.javascript2.nodejs.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;
import org.netbeans.modules.javascript2.nodejs.TestProjectSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class NodeJsDeclarationFinderTest extends JsTestBase {
    private static boolean projectCreated = false;
    
    public NodeJsDeclarationFinderTest(String testName) {
        super(testName);
    }
    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp(); 
//        if (!projectCreated) {
            projectCreated = true;
            FileObject folder = getTestFile("TestNavigation");
            Project testProject = new TestProjectSupport.TestProject(folder, null);
            List lookupAll = new ArrayList();
            lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
            lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(testProject));
            MockLookup.setInstances(lookupAll.toArray());
//        }
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(1);

        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/TestNavigation/public_html/")));
//        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/NodeJsRuntime/")));
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
    
    @Test
    public void testNavigation01() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/foo.js", "var circle = require('./cir^cle.js');", "circle.js", 0);
    }
    
    @Test
    public void testNavigation02() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/foo.js", "var triangle = require('trian^gle.js');", "triangle.js", 0);
    }
    
    @Test
    public void testNavigation03() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/lib/testLib.js", "var bb = require ('./some_^lib');", "some-library.js", 0);
    }
    
    @Test
    public void testIssue247565_01() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o^1_247565.obj.conf.a;", "issue247565.js", 4);
    }
    
    @Test
    public void testIssue247565_02() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o1_247565.o^bj.conf.a;", "literal247565.js", 198);
    }

    @Test
    public void testIssue247565_03() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o1_247565.obj.co^nf.a;", "literal247565.js", 114);
    }
    
    @Test
    public void testIssue247565_04() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o1_247565.obj.conf.a^;", "literal247565.js", 131);
    }
    
    @Test
    public void testIssue247565_05() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o1_247565.pok^us.getDay();", "literal247565.js", 175);
    }
    
    @Test
    public void testIssue247565_06() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o1_247565.obj.do^b.getMilliseconds();", "literal247565.js", 49);
    }
    
    @Test
    public void testIssue247565_07() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o1_247565.obj.he^llo();", "literal247565.js", 76);
    }
    
    @Test
    public void testIssue247565_08() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o1_247565.obj.ni^ck;", "literal247565.js", 29);
    }
    
    @Test
    public void testIssue247565_09() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o2_247565.ob^j.conf.a;", "literalRef247565.js", 1037);
    }
    
    @Test
    public void testIssue247565_10() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o2_247565.obj.co^nf.a;", "literalRef247565.js", 925);
    }
    
    @Test
    public void testIssue247565_11() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o2_247565.obj.conf.a^;", "literalRef247565.js", 942);
    }
    
    @Test
    public void testIssue247565_12() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o2_247565.po^kus.getSeconds();", "literalRef247565.js", 1014);
    }
    
    @Test
    public void testIssue247565_13() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o2_247565.obj.d^ob.getFullYear();", "literalRef247565.js", 860);
    }
    
    @Test
    public void testIssue247565_14() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o2_247565.obj.he^llo();", "literalRef247565.js", 887);
    }
    
    @Test
    public void testIssue247565_15() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247565/issue247565.js", "o2_247565.obj.ni^ck;", "literalRef247565.js", 840);
    }
    
    @Test
    public void testIssue249854_01() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/app/maingt.js", "p.b^ar();//gt;5;func.js;8;10", "func.js", 105);
    }
    
    @Test
    public void testIssue249854_02() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/app/maingt.js", "console.log(p.getAtt^empt().aa);//gt;19;func.js;38;22", "func.js", 601);
    }
    
    @Test
    public void testIssue247727_01() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247727/issue247727.js", "o7.ma^rs.jejda;", "test247727.js", 148);
    }
    
    @Test
    public void testIssue247727_02() throws Exception {
        checkDeclaration("TestNavigation/public_html/js/issue247727/issue247727.js", "o7.mars.jej^da;", "test247727.js", 75);
    }
}
