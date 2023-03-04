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
 * Created for the purpose of the specific Groovy operators - see issue #151034.
 * 
 * Link: http://docs.codehaus.org/display/GROOVY/Operators#Operators-ElvisOperator
 *
 * @author Petr Hejl
 * @author Martin Janicek
 */
public class OperatorsCCTest extends GroovyCCTestBase {

    public OperatorsCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "operators";
    }

    // Safe Navigation operator should access the same items as dot
    public void testSafeNavigation1_1() throws Exception {
        checkCompletion(BASE + "SafeNavigation1.groovy", "        x?.b^", true);
    }

    public void testSafeNavigation1_2() throws Exception {
        checkCompletion(BASE + "SafeNavigation1.groovy", "        this?.a^", true);
    }

    public void testSafeNavigation1_3() throws Exception {
        checkCompletion(BASE + "SafeNavigation1.groovy", "        r?.t^", true);
    }

    public void testSafeNavigation2_1() throws Exception {
        checkCompletion(BASE + "SafeNavigation2.groovy", "        \"\"?.a^", true);
    }

    
    // Method Closure operator is used for accessing methods only on the given object
    // Thus we are expecting that methods from either String in case of "x" or Integer
    // in case of "r" will be shown in code completion results
    public void testMethodClosure1_1() throws Exception {
        checkCompletion(BASE + "MethodClosure1.groovy", "        x.&b^", true);
    }

    public void testMethodClosure1_2() throws Exception {
        checkCompletion(BASE + "MethodClosure1.groovy", "        this.&a^", true);
    }

    public void testMethodClosure1_3() throws Exception {
        checkCompletion(BASE + "MethodClosure1.groovy", "        r.&t^", true);
    }

    public void testMethodClosure2_1() throws Exception {
        checkCompletion(BASE + "MethodClosure2.groovy", "        \"\".&a^", true);
    }

    public void testElvisOperator1_1() throws Exception {
        checkCompletion(BASE + "ElvisOperator1.groovy", "    def something = x?:e^", true);
    }

    public void testElvisOperator2_1() throws Exception {
        checkCompletion(BASE + "ElvisOperator2.groovy", "    def something = x ?:e^", true);
    }

    public void testElvisOperator3_1() throws Exception {
        checkCompletion(BASE + "ElvisOperator3.groovy", "    def something = x ?: e^", true);
    }

    
    // Spread operator is accessing fields/methods on each item in the given collection
    // Thus we are expecting that fields/methods from String will be shown in code completion
    public void testSpreadOperator1_stringArray_all() throws Exception {
        checkCompletion(BASE + "SpreadOperator1.groovy", "        ['cat', 'elephant']*.^", true);
    }

    public void testSpreadOperator2_stringArray_sPrefix() throws Exception {
        checkCompletion(BASE + "SpreadOperator2.groovy", "        ['cat', 'elephant']*.s^", true);
    }

    public void testSpreadOperator3_intArray_all() throws Exception {
        checkCompletion(BASE + "SpreadOperator3.groovy", "        [1,2]*.^", true);
    }

    public void testSpreadOperator4_intArray_sPrefix() throws Exception {
        checkCompletion(BASE + "SpreadOperator4.groovy", "        [1,2]*.s^", true);
    }
    
    
    // Java Field operator is accessing fields only on the given object. Thus we are expecting
    // that only properties/field of the Foo object will be shown in code completion
    public void testJavaFieldOperator1_all() throws Exception {
        checkCompletion(BASE + "JavaFieldOperator1.groovy", "        new Foo().@^", true);
    }
    
    public void testJavaFieldOperator2_withPrefix() throws Exception {
        checkCompletion(BASE + "JavaFieldOperator2.groovy", "        new Foo().@t^", true);
    }
    
    public void testJavaFieldOperator3_withSuffix() throws Exception {
        checkCompletion(BASE + "JavaFieldOperator3.groovy", "        new Foo().@^tes", true);
    }
    
    public void testJavaFieldOperator4_withinIdentifier() throws Exception {
        checkCompletion(BASE + "JavaFieldOperator4.groovy", "        new Foo().@te^s", true);
    }
    
    
    // Spread Java Field operator is accessing fields on each item in the given collection
    // Thus we are expecting that properties/fields from String will be shown in code completion
    public void testSpreadJavaFieldOperator1_all() throws Exception {
        checkCompletion(BASE + "SpreadJavaFieldOperator1.groovy", "        ['abc', 'def']*.@^", true);
    }

    public void testSpreadJavaFieldOperator2_withPrefix() throws Exception {
        checkCompletion(BASE + "SpreadJavaFieldOperator2.groovy", "        ['abc', 'def']*.@b^", true);
    }

    public void testSpreadJavaFieldOperator3_withSufix() throws Exception {
        checkCompletion(BASE + "SpreadJavaFieldOperator3.groovy", "        ['abc', 'def']*.@^byt", true);
    }

    public void testSpreadJavaFieldOperator4_withinIdentifier() throws Exception {
        checkCompletion(BASE + "SpreadJavaFieldOperator4.groovy", "        ['abc', 'def']*.@by^t", true);
    }
}
