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
public class JsCodeCompletionArraysTest extends JsCodeCompletionBase {

    public JsCodeCompletionArraysTest(String testName) {
        super(testName);
    }

    public void testArrays01() throws Exception {
        checkCompletion("testfiles/completion/arrays/arrays1.js", "arr01.^pop();", false);
    }

    public void testArrays02() throws Exception {
        checkCompletion("testfiles/completion/arrays/arrays1.js", "arr02.l^ength;", false);
    }

    public void testArrays03() throws Exception {
        checkCompletion("testfiles/completion/arrays/arrays1.js", "arr03.l^ength;", false);
    }

    public void testArrays04() throws Exception {
        checkCompletion("testfiles/completion/arrays/arrays2.js", "      myArray.^", false);
    }

    public void testArrays05() throws Exception {
        checkCompletion("testfiles/completion/arrays/arrays3.js", "    myArray2.^", false);
    }

    public void testArrayLiteral01() throws Exception {
        checkCompletion("testfiles/completion/arrays/arrayliteral.js", "var prom1 = ar[1].to^UpperCase();", false);
    }

    public void testArrayLiteral02() throws Exception {
        checkCompletion("testfiles/completion/arrays/arrayliteral.js", "var prom2 = ar[3].get^Day();", false);
    }

    public void testIssue231267_01() throws Exception {
        checkCompletion("testfiles/completion/arrays/issue231267.js", "    var prom231267_2 = ar231267[3].get^Day();", false);
    }

    public void testIssue231267_02() throws Exception {
        checkCompletion("testfiles/completion/arrays/issue231267.js", "var prom231267_1 = ar231267[1].to^UpperCase();", false);
    }

    public void testIssue231449_01() throws Exception {
        checkCompletion("testfiles/completion/arrays/issue231449.js", "var prom231449_2 = ar231449[3].get^Day();", false);
    }

    public void testIssue231449_02() throws Exception {
        checkCompletion("testfiles/completion/arrays/issue231449.js", "var prom231449_1 = ar231449[1].to^UpperCase();", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(2);
        cpRoots.add(ClasspathProviderImplAccessor.getJsStubs().get(0)); // Only use core stubs in unittests
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/arrays")));
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
