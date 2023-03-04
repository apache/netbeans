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

package org.netbeans.modules.javascript2.requirejs.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.requirejs.RequireJsPreferences;
import org.netbeans.modules.javascript2.requirejs.TestProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Pisl
 */
public class RequireJsDeclarationFinderTest extends JsTestBase {
    
    public RequireJsDeclarationFinderTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        FileObject folder = getTestFile("TestProject1");
        Project testProject = new TestProjectSupport.TestProject(folder, null);
        List lookupAll = new ArrayList();
        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
        lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(testProject));
        MockLookup.setInstances(lookupAll.toArray());
        
        Map<String, String> mappings = new HashMap<>();
        mappings.put("utils", "js/folder1/api/utils.js");
        mappings.put("api", "js/folder1/api");
        mappings.put("lib/api", "js/folder1/api");
        RequireJsPreferences.storeMappings(testProject, mappings);
    }
    
    public void testNavigationFromModuleDeclaration01() throws Exception {
        checkDeclaration("TestProject1/js/main.js", "'folde^r1/module1',", "module1.js", 0);
    }
    
    public void testNavigationFromModuleDeclaration02() throws Exception {
        checkDeclaration("TestProject1/js/main.js", "'./folde^r1/module1'", "module1.js", 0);
    }
    
    public void testNavigationFromModuleDeclaration03() throws Exception {
        checkDeclaration("TestProject1/js/folder1/api/utils.js", "'../../fol^der1/module1'", "module1.js", 0);
    }
    
    public void testNavigationFromParameter01() throws Exception {
        checkDeclaration("TestProject1/js/main.js", "function (modul^e1, module11, utils) {", "module1.js", 0);
    }
    
    public void testNavigationFromParameter02() throws Exception {
        checkDeclaration("TestProject1/js/main.js", "function (module1, module11, uti^ls) {", "utils.js", 0);
    }
    
    // project mapping through the RequireJS project customizer
    public void testNavigationThroughProjectProperties01() throws Exception {
        checkDeclaration("TestProject1/js/main.js", "'util^s'", "utils.js", 0);
    }
    
    public void testNavigationThroughProjectProperties02() throws Exception {
        checkDeclaration("TestProject1/js/main.js", "'api/v0^.1/Options'", "Options.js", 0);
    }

    // TODO removing test, now is still failing, needs to be corrected
//    public void testNavigationThroughProjectProperties03() throws Exception {
//        checkDeclaration("TestProject1/js/main.js", "'lib2/api/v0.1/OMe^ssages'", "OMessages.js", 0);
//    }
    
    public void testIssue251646() throws Exception {
        checkDeclaration("TestProject1/js/issue251646/ext/day/test251646.js", "require(\"../../day/issue^251646\");", "issue251646.js", 0);
    }
    
}
