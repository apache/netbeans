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
package org.netbeans.modules.php.editor.verification;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

public class PSR12FilesHintTest extends PSR12HintTestBase {

    private static volatile boolean isCRLF = false;

    public PSR12FilesHintTest(String testName) {
        super(testName);
    }

    public void testHasNewline() throws Exception {
        checkHints();
    }

    public void testHasNewlineCRLF() throws Exception {
        checkHints(true);
    }

    public void testNoNewlineAtEOF() throws Exception {
        checkHints();
    }

    public void testEndWithWhitespace() throws Exception {
        checkHints();
    }

    private void checkHints() throws Exception {
        checkHints(new PSR12FilesHint(), getName() + ".php");
    }

    private void checkHints(boolean isCRLF) throws Exception {
        if (isCRLF) {
            PSR12FilesHintTest.isCRLF = true;
            checkHints();
            PSR12FilesHintTest.isCRLF = false;
        } else {
            checkHints();
        }
    }

    @Override
    protected Source getTestSource(FileObject f) {
        BaseDocument doc = (BaseDocument) GsfUtilities.getADocument(f, true);
        if (isCRLF) {
            doc.putProperty(BaseDocument.READ_LINE_SEPARATOR_PROP, BaseDocument.LS_CRLF);
        }
        return Source.create(doc);
    }

}
