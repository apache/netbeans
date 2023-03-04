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
package org.netbeans.modules.javascript2.jade.editor;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JadeStructureScannerTest extends JadeTestBase {
    
    public JadeStructureScannerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {
        super.assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }
    
    public void testComment01() throws Exception {
        checkFolds("testfiles/folding/comment01.jade");
    }
    
    public void testBlock01() throws Exception {
        checkFolds("testfiles/folding/block01.jade");
    }
    
    public void testIssue250570() throws Exception {
        checkFolds("testfiles/folding/issue250570.jade");
    }
    
    public void testIssue250490() throws Exception {
        checkFolds("testfiles/folding/issue250490.jade");
    }
    
    public void testIssue250522() throws Exception {
        checkFolds("testfiles/folding/issue250522.jade");
    }
}
