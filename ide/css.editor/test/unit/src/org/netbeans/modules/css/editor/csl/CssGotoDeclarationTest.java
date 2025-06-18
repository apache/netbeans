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
package org.netbeans.modules.css.editor.csl;

import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.css.editor.ProjectTestBase;

public class CssGotoDeclarationTest extends ProjectTestBase {

    public CssGotoDeclarationTest(String testName) {
        super(testName, "testProject");
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        try {
            return (DefaultLanguageConfig) Class.forName("org.netbeans.modules.html.editor.gsf.HtmlLanguage").getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void testClass_01() throws Exception {
        checkDeclaration(getSourcesFolderName() + "/test.html", "<p class=\"classSelec^tor1\">test class</p>", "test1.css", 819);
    }

    public void testClass_02() throws Exception {
        checkDeclaration(getSourcesFolderName() + "/test.html", "<p class=\"classSelec^tor3\">test class</p>", "test2.css", 819);
    }

    public void testId_01() throws Exception {
        checkDeclaration(getSourcesFolderName() + "/test.html", "<p id=\"yetAnoth^erId\">test id</p>", "test2.css", 929);
    }
}
