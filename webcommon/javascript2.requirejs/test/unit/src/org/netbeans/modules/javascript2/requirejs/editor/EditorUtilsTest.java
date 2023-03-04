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
package org.netbeans.modules.javascript2.requirejs.editor;

import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Petr Pisl
 */
public class EditorUtilsTest extends JsTestBase {

    public EditorUtilsTest(String testName) {
        super(testName);
    }

    public void testCompletionContext01() throws Exception {
        checkCompletionContext("TestProject2/public_html/js/main.js");
    }

    private void checkCompletionContext(final String filePath) throws Exception {
        Source testSource = getTestSource(getTestFile(filePath));
        Snapshot snapshot = testSource.createSnapshot();
        CharSequence text = snapshot.getText();
        StringBuilder sb = new StringBuilder();
        for (int offset = 0; offset < text.length(); offset++) {
            EditorUtils.CodeCompletionContext context = EditorUtils.findContext(snapshot, offset);
            sb.append(offset);
            sb.append(" '");
            switch (text.charAt(offset)) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(text.charAt(offset));
            }
            sb.append("' : ");
            sb.append(context);
            sb.append("\n");
        }
        assertDescriptionMatches(filePath, sb.toString(), false, ".context");
    }

}
