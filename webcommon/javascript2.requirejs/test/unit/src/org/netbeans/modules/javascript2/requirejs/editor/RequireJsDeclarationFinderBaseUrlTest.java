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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
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
public class RequireJsDeclarationFinderBaseUrlTest extends JsCodeCompletionBase {
    
    public RequireJsDeclarationFinderBaseUrlTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        FileObject folder = getTestFile("TestProject2");
        Project testProject = new TestProjectSupport.TestProject(folder, null);
        List lookupAll = new ArrayList();
        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
        lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(testProject));
        MockLookup.setInstances(lookupAll.toArray());
        
    }
    
    public void testNavigation01() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "requirejs(['module^Lib1',", "moduleLib1.js", 0);
    }
    
    public void testNavigation02() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "'js/lib/moduleLib2.^js',", "moduleLib2.js", 0);
    }
    
    public void testNavigation03() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "'app/module^App1',", "moduleApp1.js", 0);
    }
    
    public void testNavigation04() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "'pikn^ic'", "picnic.js", 0);
    }
    
    public void testNavigation05() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "'proto/locali^zation',", "localization.js", 0);
    }
    
    public void testNavigationFromParameter01() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "function(l^ib1, lib2, test, loc, pik)", "moduleLib1.js", 0);
    }
    
    public void testNavigationFromParameter02() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "function(lib1, l^ib2, test, loc, pik)", "moduleLib2.js", 0);
    }
    
    public void testNavigationFromParameter03() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "function(lib1, lib2, te^st, loc, pik)", "moduleApp1.js", 0);
    }
    
    public void testNavigationFromParameter04() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "function(lib1, lib2, test, l^oc, pik)", "localization.js", 0);
    }
    
    public void testNavigationFromParameter05() throws Exception {
        checkDeclaration("TestProject2/public_html/js/main.js", "function(lib1, lib2, test, loc, p^ik)", "picnic.js", 0);
    }
    
    public void testNavigationWithPlugin01() throws Exception {
        checkDeclaration("TestProject2/public_html/js/app/issue245184.js", "'te^xt!app/issue245156'", "issue245156.js", 0);
    }
    
    public void testNavigationWithPlugin02() throws Exception {
        checkDeclaration("TestProject2/public_html/js/app/issue245184.js", "'order!lib/proto/locali^zation'", "localization.js", 0);
    }

    public void testNavigationWithPlugin03() throws Exception {
        checkDeclaration("TestProject2/public_html/js/app/issue245184.js", "'text!pik^nic'", "picnic.js", 0);
    }
    
    public void testIssue249282_01() throws Exception {
        checkDeclaration("TestProject2/public_html/js/app/issue249282.js", "'ojs/^oj'", "oj.js", 0);
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>();
        
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/TestProject2/public_html")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[0]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return false;
    }

    @Override
    protected boolean cleanCacheDir() {
        return false;
    }
}
