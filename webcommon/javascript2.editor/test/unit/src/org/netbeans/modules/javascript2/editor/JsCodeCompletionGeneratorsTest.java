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

import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;

import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class JsCodeCompletionGeneratorsTest extends JsCodeCompletionBase {

    public JsCodeCompletionGeneratorsTest(String testName) {
        super(testName);
    }

    public void testGenerators01_01() throws Exception {
        checkCompletion("testfiles/ecmascript6/generators/generator01.js", "var g = gen^01();", false);
    }

    public void testGenerators01_02() throws Exception {
        checkCompletion("testfiles/ecmascript6/generators/generator01.js", "var d = g.next().v^", false);
    }

    public void testGenerators02_01() throws Exception {
        checkCompletion("testfiles/ecmascript6/generators/generator02.js", "console.log(Utils.values().next().d^one);", false);
    }

    public void testGenerators03_01() throws Exception {
        checkCompletion("testfiles/ecmascript6/generators/generator03.js", "var t = n.^used();", false);
    }

    public void testGenerators03_02() throws Exception {
        checkCompletion("testfiles/ecmascript6/generators/generator03.js", "console.log(t.^next());", false);
    }

    public void testGenerators04_01() throws Exception {
        checkCompletion("testfiles/ecmascript6/generators/generator04.js", "console.log(keyboard.keys().n^ext());", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(2);
        cpRoots.add(ClasspathProviderImplAccessor.getJsStubs().get(0)); // Only use core stubs in unittests
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "testfiles/ecmascript6/generators")));
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
