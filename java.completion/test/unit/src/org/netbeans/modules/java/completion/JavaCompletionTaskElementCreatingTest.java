/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.completion;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class JavaCompletionTaskElementCreatingTest extends CompletionTestBase {

    public JavaCompletionTaskElementCreatingTest(String testName) {
        super(testName);
    }

    public void testUnimplementedMethod() throws Exception {
        performTest("UnimplementedMethod", 85, "", "UnimplementedMethod.pass");
    }
    
    public void testOverrideAbstractList() throws Exception {
        performTest("OverrideAbstractList", 118, "", "OverrideAbstractList.pass");
    }
    
    /**
     * Checks that cc: offers just one size() for override, but offers size() for both implement AND override.
     */
    public void testOverrideAbstractListAbstract() throws Exception {
        performTest("OverrideAbstractListAbstract", 126, "", "OverrideAbstractListAbstract.pass");
    }
    
    /** CC should not offer overriding private method from superclass */
    public void testOverridePrivateMethod() throws Exception {
        performTest("OverridePrivateMethod", 89, "cl", "OverridePrivateMethod.pass");
    }
    
    /** CC should not offer overriding package private method from superclass in a different package */
    public void testOverridePackagePrivateMethod() throws Exception {
        performTest("OverridePackagePrivateMethod", 108, "add", "OverridePackagePrivateMethod.pass");
    }

    public void testOverrideAbstractListWithPrefix() throws Exception {
        performTest("OverrideAbstractList", 118, "to", "OverrideAbstractListWithPrefix.pass");
    }
    
    public void testOverrideFinalize() throws Exception {
        performTest("OverrideAbstractList", 118, "fin", "OverrideFinalize.pass");
    }
    
    public void testOverrideAbstractList2a() throws Exception {
        performTest("OverrideAbstractList2", 139, "ad", "OverrideAbstractList2a.pass");
    }
    
    public void testOverrideAbstractList2b() throws Exception {
        performTest("OverrideAbstractList2", 139, "ge", "OverrideAbstractList2b.pass");
    }
    
    public void testOverrideAbstractList3a() throws Exception {
        performTest("OverrideAbstractList3", 126, "ad", "OverrideAbstractList3a.pass");
    }
    
    public void testOverrideAbstractList3b() throws Exception {
        performTest("OverrideAbstractList3", 126, "ge", "OverrideAbstractList3b.pass");
    }
    
    public void testOverrideTypedException1() throws Exception {
        performTest("OverrideTypedException", 209, "tes", "OverrideTypedException.pass");
    }
    
    public void testOverrideTypedException2() throws Exception {
        performTest("OverrideTypedException", 305, "tes", "OverrideTypedException.pass");
    }
    
    public void testOverrideInInnerClass() throws Exception {
        performTest("OverrideInInnerClass", 185, "pai", "OverrideInInnerClass.pass");
    }
    
    public void testOverrideInInnerClassUnresolvable() throws Exception {
        performTest("OverrideInInnerClassUnresolvable", 157, "pai", "empty.pass");
    }
    
    public void testCreateConstructorTest() throws Exception {
        performTest("CreateConstructorTest", 249, "", "CreateConstructorTest.pass");
    }

    public void testCreateConstructorTestInnerClass() throws Exception {
        performTest("CreateConstructorTest", 434, "", "CreateConstructorTestInnerClass.pass");
    }

    public void testCreateConstructorWithConstructors() throws Exception {
        performTest("CreateConstructorWithConstructors", 400, "", "CreateConstructorWithConstructors.pass");
    }

    public void testCreateConstructorWithConstructorsInnerClass() throws Exception {
        performTest("CreateConstructorWithConstructors", 667, "", "CreateConstructorWithConstructorsInnerClass.pass");
    }

    public void testCreateConstructorWithDefaultConstructor() throws Exception {
        performTest("CreateConstructorWithDefaultConstructor", 352, "", "CreateConstructorWithDefaultConstructor.pass");
    }

    public void testCreateConstructorWithDefaultConstructorInnerClass() throws Exception {
        performTest("CreateConstructorWithDefaultConstructor", 560, "", "CreateConstructorWithDefaultConstructorInnerClass.pass");
    }

    public void testCreateConstructorNonDefaultConstructor() throws Exception {
        performTest("CreateConstructorNonDefaultConstructor", 379, "", "CreateConstructorNonDefaultConstructor.pass");
    }

    public void testCreateConstructorNonDefaultConstructorInnerClass() throws Exception {
        performTest("CreateConstructorNonDefaultConstructor", 624, "", "CreateConstructorNonDefaultConstructorInnerClass.pass");
    }
}
