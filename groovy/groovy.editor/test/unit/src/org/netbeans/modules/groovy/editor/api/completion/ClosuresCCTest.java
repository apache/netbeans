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
 * Closure related code completion tests
 *
 * @author Martin Janicek
 */
public class ClosuresCCTest extends GroovyCCTestBase {

    
    public ClosuresCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "closures";
    }

    public void testInsideClosure1_1() throws Exception {
        checkCompletion(BASE + "InsideClosure1.groovy", "(1..3).any {println ^}", false);
    }

    public void testInsideClosure1_2() throws Exception {
        checkCompletion(BASE + "InsideClosure1.groovy", "[3,4,5].each {println i^}", false);
    }

    public void testInsideClosure1_3() throws Exception {
        checkCompletion(BASE + "InsideClosure1.groovy", "(1..3).any {aa,ab -> println a^}", false);
    }

    public void testInsideClosure1_4() throws Exception {
        checkCompletion(BASE + "InsideClosure1.groovy", "[3,4,5].each {xu1,xu2,xu3 -> println xu^}", false);
    }

    public void testInsideClosure1_5() throws Exception {
        checkCompletion(BASE + "InsideClosure1.groovy", "def t1 = {println i^}", false);
    }

    public void testInsideClosure1_6() throws Exception {
        checkCompletion(BASE + "InsideClosure1.groovy", "def t2 = {test1,test2,test3 -> println test^}", false);
    }

    public void testInsideClosure1_7() throws Exception {
        checkCompletion(BASE + "InsideClosure1.groovy", "\"TestString\".eachLine {String line -> println i^}", false);
    }

    public void testInsideClosure1_8() throws Exception {
        checkCompletion(BASE + "InsideClosure1.groovy", "\"TestString\".eachLine {String line -> println lin^}", false);
    }
}
