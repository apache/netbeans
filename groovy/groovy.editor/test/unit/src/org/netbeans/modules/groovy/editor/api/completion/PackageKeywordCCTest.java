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

import org.junit.Before;

/**
 *
 * @author Petr Pisl
 */
public class PackageKeywordCCTest extends GroovyCCTestBase {

    public PackageKeywordCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() { 
       return "package";
    }
 
    public void testPackageKeyword01() throws Exception {
        checkCompletion(BASE + "PackageKeyword01.groovy", "pac^", true);
    }
    
    public void testPackageKeyword02() throws Exception {
        checkCompletion(BASE + "PackageKeyword02.groovy", "pac^", true);
    }
    
    public void testPackageKeyword03() throws Exception {
        checkCompletion(BASE + "PackageKeyword03.groovy", "pac^", true);
    }
    
    public void testPackagesCC01() throws Exception {
        checkCompletion(BASE + "PackagesCC01.groovy", "package     /* a comment */              or^", true);
    }
    
}
