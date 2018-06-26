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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.netbeans.modules.javascript2.requirejs.RequireJsPreferences;
import org.netbeans.modules.javascript2.requirejs.TestProjectSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Pisl
 */
public class RequireJSCodeCompletionTest extends JsCodeCompletionBase {

    public RequireJSCodeCompletionTest(String testName) throws IOException {
        super(testName);
    }

    static private boolean isSetup = false;

    @Override
    protected void setUp() throws Exception {
//        super.setUp(); //To change body of generated methods, choose Tools | Templates.
        if (!isSetup) {
            // only for the first run index all sources
            super.setUp();
            isSetup = true;
        }
        FileObject folder = getTestFile("TestProject1");
        Project tp = new TestProjectSupport.TestProject(folder, null);

        Map<String, String> mappings = new HashMap();
        mappings.put("utils", "js/folder1/api/utils.js");
        mappings.put("api", "js/folder1/api");
        mappings.put("lib/api", "js/folder1/api");
        RequireJsPreferences.storeMappings(tp, mappings);

        List lookupAll = new ArrayList();
        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
        lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(tp));
        MockLookup.setInstances(lookupAll.toArray());

    }

    public void testObjectLiteral01() throws Exception {
        checkCompletion("TestProject1/js/folder1/usingObjectLiteral.js", "   ob.ol1^", false);
    }

    public void testObjectLiteral02() throws Exception {
        checkCompletion("TestProject1/js/folder1/usingObjectLiteral2.js", "   ob.ol2^", false);
    }

    public void testObjectLiteral03() throws Exception {
        checkCompletion("TestProject1/js/folder1/usingObjectLiteral2.js", "   ob.ol^2", false);
    }

    public void testProjectMappings01() throws Exception {
        checkCompletion("TestProject1/js/main.js", "utils.util^Method1();", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>();

        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/TestProject1")));
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
}
