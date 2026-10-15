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
package org.netbeans.modules.javascript2.extjs.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javascript2.editor.*;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;

/**
 *
 * @author Petr Pisl
 */
public class ExtDefineCodeCompletionTest extends JsCodeCompletionBase {
    
    public ExtDefineCodeCompletionTest(String testName) {
        super(testName);
    }
    
    public void testDefineMethodInterceptor_01() throws Exception {
        checkCompletion("testfiles/completion/defineMethod/test01.js", "NetB^", false);
    }
    
    public void testDefineMethodInterceptor_02() throws Exception {
        checkCompletion("testfiles/completion/defineMethod/test02.js", "NetBeans.s^", false);
    }
    
    public void testDefineMethodInterceptor_03() throws Exception {
        checkCompletion("testfiles/completion/defineMethod/test03.js", "NetBeans.stuff.e^", false);
    }
    
    public void testDefineMethodInterceptor_04() throws Exception {
        checkCompletion("testfiles/completion/defineMethod/test04.js", "NetBeans.stuff.engineer.d^", false);
    }
    
    public void testDefineMethodInterceptor_05() throws Exception {
        checkCompletion("testfiles/completion/defineMethod/test05.js", "NetBeans.stuff.engineer.developer.a^", false);
    }
    
    public void testDefineMethodInterceptor_06() throws Exception {
        checkCompletion("testfiles/completion/defineMethod/test06.js", "NetBeans.stuff.engineer.developer.address.z^", false);
    }
    
    public void testIssue231923() throws Exception {
        checkCompletion("testfiles/completion/defineMethod/issue231923.js", "var obj = My.app.Pan^;", false);
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(2);
        cpRoots.add(ClasspathProviderImplAccessor.getJsStubs().get(0)); // Only use core stubs in unittests
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/defineMethod")));
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
