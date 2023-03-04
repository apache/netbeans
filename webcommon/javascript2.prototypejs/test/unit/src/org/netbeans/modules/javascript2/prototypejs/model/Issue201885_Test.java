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

package org.netbeans.modules.javascript2.prototypejs.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;

/**
 *
 * @author Petr Pisl
 */
public class Issue201885_Test extends JsCodeCompletionBase {

    private static final String filePath = "testfiles/basic/issue201885.js";
    
    public Issue201885_Test(String testName) {
        super(testName);
    }

    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {
        super.assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }

    public void testStructure() throws Exception {
        checkStructure(filePath);
    }

    public void testSemantic() throws Exception {
        checkSemantic(filePath);
    }
    
    public void testCC_01() throws Exception {
        checkCompletion(filePath, "var person = new Pe^rson();", false);
    }
    
    public void testCC_02() throws Exception {
        checkCompletion(filePath, "person.^", false);
    }
    
    public void testOccurreces_01() throws Exception {
        checkOccurrences(filePath, "var Per^son = Class.create({", true);
    }
    
    public void testOccurreces_02() throws Exception {
        checkOccurrences(filePath, "initialize: function(nam^e){", true);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(3);
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/basic")));
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
