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

import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.javascript2.editor.JsonTestBase;

/**
 *
 * @author Petr Hejl
 */
public class JsonFormatterTest extends JsonTestBase {

    public JsonFormatterTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        reformatFileContents("testfiles/simple.json",new IndentPrefs(4, 4));
    }

    public void testBasic1() throws Exception {
        reformatFileContents("testfiles/formatter/basic1.json",new IndentPrefs(4, 4));
    }

    public void testBasic2() throws Exception {
        reformatFileContents("testfiles/formatter/basic2.json",new IndentPrefs(4, 4));
    }

    public void testBasic3() throws Exception {
        reformatFileContents("testfiles/formatter/basic3.json",new IndentPrefs(4, 4));
    }

}
