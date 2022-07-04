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
package org.netbeans.modules.javascript2.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class JsCodeCompletionIssue223602Test extends JsCodeCompletionBase {

    public JsCodeCompletionIssue223602Test(String testName) {
        super(testName);
    }

    public void testIssue223602_01() throws Exception {
        checkCompletion("testfiles/structure/issue223602/issue223602.js", "p223602_1.^event;", false);
    }

    public void testIssue223602_02() throws Exception {
        checkCompletion("testfiles/structure/issue223602/issue223602.js", "p223602_2.b^ig();", false);
    }

    public void testIssue223602_03() throws Exception {
        checkCompletion("testfiles/structure/issue223602/issue223602.js", "p223602_3.b^ig();", false);
    }

    public void testIssue223679_01() throws Exception {
        checkCompletion("testfiles/structure/issue223602/issue223602.js", "p223602^_3.big();", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(2);
        cpRoots.add(ClasspathProviderImplAccessor.getJsStubs().get(0)); // Only use core stubs in unittests
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/structure/issue223602")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[0]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }
}
