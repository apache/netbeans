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

package org.netbeans.modules.javascript2.editor;


public class JsonDeletedTextInterceptorTest extends JsonTestBase {
    
    public JsonDeletedTextInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    protected org.netbeans.modules.csl.api.Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }
    
    public void testBackspace1() throws Exception {
        deleteChar("x^", "^");
    }

    public void testBackspace2() throws Exception {
        deleteChar("x^y", "^y");
    }

    public void testBackspace3() throws Exception {
        deleteChar("xy^z", "x^z");
    }

    public void testBackspace4() throws Exception {
        deleteChar("xy^z", "x^z");
    }

    public void testBackspace5() throws Exception {
        deleteChar("\"^\"", "^");
    }

    public void testBackspace6() throws Exception {
        deleteChar("'^'", "^'");
    }

    public void testBackspace7() throws Exception {
        deleteChar("(^)", "^)");
    }

    public void testBackspace8() throws Exception {
        deleteChar("[^]", "^");
    }

    public void testBackspace9() throws Exception {
        deleteChar("{^}", "^");
    }

    public void testBackspace10() throws Exception {
        deleteChar("/^/", "^/");
    }

    public void testNoDeleteContComment1() throws Exception {
        deleteChar("// ^", "//^");
        deleteChar("\n// ^", "\n//^");
    }

    public void testNoDeleteContComment2() throws Exception {
        deleteChar("// ^  ", "//^  ");
        deleteChar("\n// ^  ", "\n//^  ");
    }

    public void testNoDeleteContComment3() throws Exception {
        deleteChar("//  ^", "// ^");
        deleteChar("//^", "/^");
    }

}
