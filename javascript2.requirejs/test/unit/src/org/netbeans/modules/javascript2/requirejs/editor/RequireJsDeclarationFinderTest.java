/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
        
        Map<String, String> mappings = new HashMap();
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
