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
package org.netbeans.modules.languages.toml;

/**
 *
 * @author lkishalmi
 */
public class TomlKeystrokeTest extends TomlTestBase {

    public TomlKeystrokeTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    public void testInsertBracket() throws Exception {
        insertChar("^", '[', "[^]");
    }

    public void testDeleteBracket() throws Exception {
        deleteChar("[^]", "^");
    }

    public void testInsertCurly() throws Exception {
        insertChar("^", '{', "{^}");
    }

    public void testDeleteCurly() throws Exception {
        deleteChar("{{^}}", "{^}");
    }

    public void testInsertSpaceCurly() throws Exception {
        insertChar("{{^}}", ' ', "{{ ^ }}");
    }

    public void testInsertSpaceBracket() throws Exception {
        insertChar("[^]", ' ', "[ ^ ]");
    }

    public void testInsertSingleQuote1() throws Exception {
        insertChar("foo= ^", '\'', "foo= '^'");
    }

    public void testInsertSingleQuote2() throws Exception {
        insertChar("foo= 'a^", '\'', "foo= 'a'^");
    }

    public void testInsertSingleQuote3() throws Exception {
        insertChar("foo= bar^", '\'', "foo= 'bar'^", "bar");
    }

    public void testInsertSingleQuote4() throws Exception {
        insertChar("foo= ^bar", '\'', "foo= 'bar'^", "bar");
    }

    public void testStepSingleQuote() throws Exception {
        insertChar("foo= '^'", '\'', "foo= ''^");
    }

    public void testDeleteSingle2() throws Exception {
        deleteChar("foo= ''^", "foo= '^");
    }

    public void testDeleteLastBrace() throws Exception {
        deleteChar("[plugins]\n"
                + "jooq-codegen = { id = \"org.jooq.jooq-codegen-gradle\", version.ref=\"jooq\" }^",
                "[plugins]\n"
                + "jooq-codegen = { id = \"org.jooq.jooq-codegen-gradle\", version.ref=\"jooq\" ^"
        );
    }

}
