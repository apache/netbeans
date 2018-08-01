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



/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

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
                ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[cpRoots.size()]))
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
