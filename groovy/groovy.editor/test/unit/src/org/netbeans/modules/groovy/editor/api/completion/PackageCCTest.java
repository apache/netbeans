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
 * I've created this class to test cases from issue 209453.
 * Package proposals should be shown only if they are prefixed by typed prefix.
 *
 * @author Martin Janicek
 */
public class PackageCCTest extends GroovyCCTestBase {

    public PackageCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "package";
    }
    

    public void testAfterExtendsTypeCompletion1() throws Exception {
        checkCompletion(BASE + "AfterExtendsTypeCompletion1.groovy", "class NoPrefix extends ^ {", false);
    }

    public void testAfterExtendsTypeCompletion3() throws Exception {
        checkCompletion(BASE + "AfterExtendsTypeCompletion3.groovy", "class PrefixIsEqualWithKeyword extends in^ {", false);
    }

    public void testAfterExtendsTypeCompletion4() throws Exception {
        checkCompletion(BASE + "AfterExtendsTypeCompletion4.groovy", "class PrefixMatchPackageCaseInsensitive extends Ja^ {", false);
    }

    public void testFieldTypeCompletion1() throws Exception {
        checkCompletion(BASE + "FieldTypeCompletion1.groovy", "    in^", false);
    }

    public void testFieldTypeCompletion2() throws Exception {
        checkCompletion(BASE + "FieldTypeCompletion2.groovy", "    Ja^", false);
    }
    
    public void testLocalVariableTypeCompletion1() throws Exception {
        checkCompletion(BASE + "LocalVariableTypeCompletion1.groovy", "    in^", false);
    }

    public void testLocalVariableTypeCompletion2() throws Exception {
        checkCompletion(BASE + "LocalVariableTypeCompletion2.groovy", "    Ja^", false);
    }
}
