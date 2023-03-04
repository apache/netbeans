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

import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;

import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class JsWithTest extends JsWithBase {

    public JsWithTest(String testName) {
        super(testName);
    }

    public void testGoTo_01() throws Exception {
        checkDeclaration("testfiles/with/test01.js", "console.log(getFirs^tName());", "man.js", 141);
    }

    public void testWith_05() throws Exception {
        checkOccurrences("testfiles/with/test02.js", "pavel.address.cit^y = \"Praha\";", true);
    }

    public void testWith_06() throws Exception {
        checkOccurrences("testfiles/with/test02.js", "pavel.addr^ess.city = \"Praha\";", true);
    }

    public void testWith_07() throws Exception {
        checkOccurrences("testfiles/with/test02.js", "pav^el.address.city = \"Praha\";", true);
    }

    public void testSemantic_01() throws Exception {
        checkSemantic("testfiles/with/test02.js");
    }

    public void testIssue234375_01() throws Exception {
        checkCompletion("testfiles/with/issue234375.js", "g^ // cc doesn't offer Date functions", true);
    }

    public void testIssue234375_02() throws Exception {
        checkCompletion("testfiles/with/issue234375.js", "r^ // this works", true);
    }

    public void testIssue234375_03() throws Exception {
        checkCompletion("testfiles/with/issue234375.js", "p^ // cc doesn't offer e.g. pull() or push()", true);
    }

    public void testIssue234637_01() throws Exception {
        checkCompletion("testfiles/with/issue234637Test.js", "({id: x^});", true);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(2);
        cpRoots.add(ClasspathProviderImplAccessor.getJsStubs().get(0)); // Only use core stubs in unittests
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/with")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[0]))
        );
    }




}
