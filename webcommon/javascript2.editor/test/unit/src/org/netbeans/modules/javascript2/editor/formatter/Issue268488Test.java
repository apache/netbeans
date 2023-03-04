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
package org.netbeans.modules.javascript2.editor.formatter;

import org.netbeans.modules.javascript2.editor.JsonTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author arusinha
 */
public class Issue268488Test extends JsonTestBase {

    private String preferredMimeType;

    public Issue268488Test(String testName) {
        super(testName);
    }

    @Override
    protected String getPreferredMimeType() {
        return preferredMimeType;
    }

    public void testIssue268488_1() throws Exception {
        preferredMimeType = JsTokenId.PACKAGE_JSON_MIME_TYPE;
        reformatFileContents("testfiles/formatter/issue268488/package.json", new IndentPrefs(4, 4));
    }

    public void testIssue268488_2() throws Exception {
        preferredMimeType = JsTokenId.BOWER_JSON_MIME_TYPE;
        reformatFileContents("testfiles/formatter/issue268488/bower.json", new IndentPrefs(4, 4));
    }

}
