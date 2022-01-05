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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import org.netbeans.modules.javascript2.model.ModelTestBase;

/**
 *
 * @author Petr Pisl
 */
public class ExtModelTest extends ModelTestBase {
    
    public ExtModelTest(String testName) {
        super(testName);
    }

    @Override
    @SuppressWarnings("NestedAssignment")
    protected void setUp() throws Exception {
        super.setUp();
        File classManagerJs = new File(getDataDir(), "testfiles/completion/applyMethod/ClassManager.js");
        if(! classManagerJs.exists()) {
            URL source = new URL("https://cdnjs.cloudflare.com/ajax/libs/extjs/4.2.1/src/class/ClassManager.js");
            URLConnection connection = source.openConnection();
            connection.addRequestProperty("User-Agent", "NetBeans Unittesting");
            try(InputStream is = connection.getInputStream();
                OutputStream os = new FileOutputStream(classManagerJs)) {
                byte[] buffer = new byte[1024 * 10];
                int read;
                while((read = is.read(buffer)) >= 0) {
                    os.write(buffer, 0, read);
                }
            }
        }
    }

    public void testExtDefineMethod() throws Exception {
        checkModel("testfiles/completion/defineMethod/defineMethod.js");
    }
    
    public void testExtApplyMethod() throws Exception {
        checkModel("testfiles/completion/applyMethod/ClassManager.js");
    }
    
    public void testIssue231923() throws Exception {
        checkModel("testfiles/structure/issue231923.js");
    }
}
