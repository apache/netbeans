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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public class JsCodeCompletionWithTest extends JsCodeCompletionBase {

    public JsCodeCompletionWithTest(String testName) {
        super(testName);
    }

    public void testWith1() throws Exception {
        checkCompletion("testfiles/completion/with/with1.js", "    ^ // test", false);
    }

    public void testWith2() throws Exception {
        checkCompletion("testfiles/completion/with/with2.js", "    z.e.^", false);
    }

    public void testWith3() throws Exception {
        checkCompletion("testfiles/completion/with/with3.js", "    ( ^ )", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(1);
        cpRoots.add(ClasspathProviderImplAccessor.getJsStubs().get(0)); // Only use core stubs in unittests
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
