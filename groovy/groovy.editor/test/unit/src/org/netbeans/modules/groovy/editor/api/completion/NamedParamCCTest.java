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

package org.netbeans.modules.groovy.editor.api.completion;

/**
 * Tests related to code completion of named parameters.
 * <p>
 * See issue 234297.
 * </p>
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class NamedParamCCTest extends GroovyCCTestBase {

    public NamedParamCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "namedparams";
    }

    public void testEmptyConstructor() throws Exception {
        checkCompletion(BASE + "EmptyConstructor.groovy", "Bar bar = new Bar(^)", false);
    }

    public void testFewParamsConstructor_AfterComma() throws Exception {
        checkCompletion(BASE + "FewParamsConstructor.groovy", "Bar baar = new Bar(aaa: 0, bbb: 1, ^)", false);
    }

    public void testFewParamsConstructor_AfterLeftParenthesis() throws Exception {
        checkCompletion(BASE + "FewParamsConstructor.groovy", "Bar baar = new Bar(^aaa: 0, bbb: 1, )", false);
    }

    public void testFewParamsConstructor_InsideNamedParameter1() throws Exception {
        checkCompletion(BASE + "FewParamsConstructor.groovy", "Bar baar = new Bar(a^aa: 0, bbb: 1, )", false);
    }

    public void testFewParamsConstructor_InsideNamedParameter2() throws Exception {
        checkCompletion(BASE + "FewParamsConstructor.groovy", "Bar baar = new Bar(aaa:^ 0, bbb: 1, )", false);
    }

    public void testFewParamsConstructor_InsideNamedParameter3() throws Exception {
        checkCompletion(BASE + "FewParamsConstructor.groovy", "Bar baar = new Bar(aaa: 0^, bbb: 1, )", false);
    }
}
