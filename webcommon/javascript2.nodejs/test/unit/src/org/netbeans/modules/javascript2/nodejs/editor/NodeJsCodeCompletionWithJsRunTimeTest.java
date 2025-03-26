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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.modules.javascript2.nodejs.TestProjectSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Pisl
 */
public class NodeJsCodeCompletionWithJsRunTimeTest extends JsCodeCompletionBase {

    public NodeJsCodeCompletionWithJsRunTimeTest(String testName) throws IOException {
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
        FileObject folder = getTestFile("TestNavigation");
            Project testProject = new TestProjectSupport.TestProject(folder, null);
            List lookupAll = new ArrayList();
            lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
            lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(testProject));
            MockLookup.setInstances(lookupAll.toArray());

    }
    
    public void testIssue249630() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue249630.js", "rfnc.rinn.rda.getM^onth();", false);
    }
    
    public void testIssue248499_01() throws Exception {
        checkCompletion("TestNavigation/public_html/js/cc01/issue248499.js", "modul.instRef.dateOfSpell().getD^", false);
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(2);
        cpRoots.add(ClasspathProviderImplAccessor.getJsStubs().get(0)); // Only use core stubs in unittests
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/TestNavigation/public_html/")));
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
