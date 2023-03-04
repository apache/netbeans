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

package org.netbeans.modules.j2ee.jpa.refactoring;

import junit.framework.TestCase;

/**
 *
 * @author Erno Mononen
 */
public class RefactoringUtilTest extends TestCase {
    
    public RefactoringUtilTest(String testName) {
        super(testName);
    }

    public void testGetPropertyName() {
        String getter = "getSomething";
        String setter = "setSomething";
        String booleanGetter = "isSomething";
        String nonJavaBeanAccessor = "something"; 
        String nonJavaBeanGetter = "getme"; 
        
        assertEquals("something", RefactoringUtil.getPropertyName(getter));
        assertEquals("something", RefactoringUtil.getPropertyName(setter));
        assertEquals("something", RefactoringUtil.getPropertyName(booleanGetter));
        assertEquals("something", RefactoringUtil.getPropertyName(nonJavaBeanAccessor));
        assertEquals("getme", RefactoringUtil.getPropertyName(nonJavaBeanGetter));
    }

}
