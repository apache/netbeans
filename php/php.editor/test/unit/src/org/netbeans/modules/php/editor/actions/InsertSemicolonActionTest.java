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
package org.netbeans.modules.php.editor.actions;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class InsertSemicolonActionTest extends PHPActionTestBase {

    public InsertSemicolonActionTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testInsert() throws Exception {
        testInFile("testfiles/actions/insertSemicolon/testInsert.php", InsertSemicolonAction.CompleteLine.ACTION_NAME);
    }

    public void testInsertNewLine() throws Exception {
        testInFile("testfiles/actions/insertSemicolon/testInsertNewLine.php", InsertSemicolonAction.CompleteLineNewLine.ACTION_NAME);
    }

    @Override
    protected String goldenFileExtension() {
        return ".insertSemicolon";
    }

}
