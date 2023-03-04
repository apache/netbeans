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
package org.netbeans.modules.web.core.syntax;

import org.netbeans.test.web.core.syntax.TestBase2;

/**
 *
 * @author Petr Hejl
 */
public class JspTypedTextInterceptorTest extends TestBase2 {

    public JspTypedTextInterceptorTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testELHash1() throws Exception {
        insertChar("#^", '{', "#{^}");
    }

    public void testELHash2() throws Exception {
        insertChar("#{^}", '}', "#{}^");
    }

    public void testELHash3() throws Exception {
        insertChar("#{}^", '}', "#{}}^");
    }

    public void testELDollar1() throws Exception {
        insertChar("$^", '{', "${^}");
    }

    public void testELDollar2() throws Exception {
        insertChar("${^}", '}', "${}^");
    }

    public void testELDollar3() throws Exception {
        insertChar("${}^", '}', "${}}^");
    }

    public void test184156() throws Exception {
        insertChar("<p class=\"item^\"", '\"', "<p class=\"item\"^");
    }

    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }
}
