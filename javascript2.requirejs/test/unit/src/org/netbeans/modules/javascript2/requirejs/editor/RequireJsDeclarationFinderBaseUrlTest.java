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
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[cpRoots.size()]))
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
