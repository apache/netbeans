/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
