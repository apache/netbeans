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

package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 *
 */
public class TemplateSpecializationTestCase extends CompletionBaseTestCase {
    
    public TemplateSpecializationTestCase(String testName) {
        super(testName, false);
    }
    
    public void testDirectIteratorVariableDeref() throws Exception {
        super.performTest("mapIterator.cc", 19, 10);
    }

    public void testTypedefIteratorVariableDeref() throws Exception {
        super.performTest("mapIterator.cc", 18, 11);
    }
    
    public void testInFunctionAfterUnderscore() throws Exception {
        // IZ#10495: A lot of identical items in code completion listbox
        super.performTest("file.cc", 5, 6);
    }  
    
    public void testIteratorVisibilityGlobal() throws Exception {
        super.performTest("mapIterator.cc", 14, 8);
    }
}
