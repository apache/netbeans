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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsStructureTest extends JsTestBase {
    
    public JsStructureTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File classManagerJs = new File(getDataDir(), "testfiles/completion/applyMethod/ClassManager.js");
        if(! classManagerJs.exists()) {
            URL source = new URL("https://cdnjs.cloudflare.com/ajax/libs/extjs/4.2.1/src/class/ClassManager.js");
            try(InputStream is = source.openStream();
                OutputStream os = new FileOutputStream(classManagerJs)) {
                byte[] buffer = new byte[1024 * 10];
                int read;
                while((read = is.read(buffer)) >= 0) {
                    os.write(buffer, 0, read);
                }
            }
        }
    }

    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {
        super.assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }
    
    public void testDefinedMethod() throws Exception {
        checkStructure("testfiles/completion/defineMethod/defineMethod.js");
    }
    
    public void testApplyMethod() throws Exception {
        checkStructure("testfiles/completion/applyMethod/ClassManager.js");
    }
    
    public void testIssue230177() throws Exception {
        checkStructure("testfiles/structure/issue230177.js");
    }
    
    public void testIssue231923() throws Exception {
        checkStructure("testfiles/structure/issue231923.js");
    }
}
