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
 * 
 * @author schmidtm, Martin Janicek
 */
public class ConstructorsCCTest extends GroovyCCTestBase {

    public ConstructorsCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "constructors";
    }
    

    public void testConstructors1() throws Exception {
        checkCompletion(BASE + "Constructors1.groovy", "StringBuffer sb = new StringBuffer^", false);
    }

    public void testConstructors2() throws Exception {
        checkCompletion(BASE + "Constructors2.groovy", "StringBuffer sb = new stringbuffer^", false);
    }

    public void testConstructors3() throws Exception {
        checkCompletion(BASE + "Constructors3.groovy", "FileOutputStream fos = new fileoutputstr^", false);
    }

    public void testConstructors4() throws Exception {
        checkCompletion(BASE + "Constructors4.groovy", "    Foo f = new F^", false);
    }

    public void testConstructors5() throws Exception {
        checkCompletion(BASE + "Constructors5.groovy", "    Foo f = new F^", false);
    }

    public void testConstructors6() throws Exception {
        checkCompletion(BASE + "Constructors6.groovy", "    Foo f = new Foo^", false);
    }

    public void testConstructors7() throws Exception {
        checkCompletion(BASE + "Constructors7.groovy", "        String s = new String^", false);
    }

    public void testConstructors8() throws Exception {
        checkCompletion(BASE + "Constructors8.groovy", "        String s = new String^(\"abc\");", false);
    }

    public void testSamePackage() throws Exception {
        checkCompletion(BASE + "SamePackage.groovy", "    Bar bar = new Bar^", false);
    }

    public void testSamePackageMoreConstructors() throws Exception {
        checkCompletion(BASE + "SamePackageMoreConstructors.groovy", "    Bar bar = new Bar^", false);
    }

    public void testImportedType() throws Exception {
        checkCompletion(BASE + "ImportedType.groovy", "    Bar bar = new Bar^", false);
    }

    public void testImportedTypeMoreConstructors() throws Exception {
        checkCompletion(BASE + "ImportedTypeMoreConstructors.groovy", "    Bar bar = new Bar^", false);
    }
}
