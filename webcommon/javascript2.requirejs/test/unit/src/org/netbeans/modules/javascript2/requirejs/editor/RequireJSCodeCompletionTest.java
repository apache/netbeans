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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.junit.RandomlyFails;
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

    private static boolean isSetup = false;

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

        Map<String, String> mappings = new HashMap<>();
        mappings.put("utils", "js/folder1/api/utils.js");
        mappings.put("api", "js/folder1/api");
        mappings.put("lib/api", "js/folder1/api");
        RequireJsPreferences.storeMappings(tp, mappings);

        List lookupAll = new ArrayList();
        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
        lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(tp));
        MockLookup.setInstances(lookupAll.toArray());

    }

    @RandomlyFails
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
}
