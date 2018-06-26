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
 *
 * @author schmidtm
 */
public class MethodCCTest extends GroovyCCTestBase {

    public MethodCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "method"; //NOI18N
    }

    public void testCompletionInsideFor1_1() throws Exception {
        checkCompletion(BASE + "CompletionInsideFor1.groovy", "for(new Date().get^", false);
    }

    public void testCompletionInsideFor1_2() throws Exception {
        checkCompletion(BASE + "CompletionInsideFor1.groovy", "for (String other in [1:\"Alice\", 2:\"Bob\"].^) {", false);
    }

    public void testMethods1_1() throws Exception {
        checkCompletion(BASE + "Methods1.groovy", "        new URL(\"http://google.com\").getPr^", false);
    }

    public void testMethods1_2() throws Exception {
        checkCompletion(BASE + "Methods1.groovy", "        new URL(\"http://google.com\").getP^r", false);
    }

    public void testMethods1_3() throws Exception {
        checkCompletion(BASE + "Methods1.groovy", "        new URL(\"http://google.com\").get^Pr", false);
    }

    public void testMethods1_4() throws Exception {
        checkCompletion(BASE + "Methods1.groovy", "        new URL(\"http://google.com\").^getPr", false);
    }

    public void testMethods2_1() throws Exception {
        checkCompletion(BASE + "Methods2.groovy", "        new Byte().^", false);
    }

    public void testMethods2_2() throws Exception {
        checkCompletion(BASE + "Methods2.groovy", "        new GroovyClass3().in^", false);
    }

    public void testCompletionInMethodCall1_1() throws Exception {
        checkCompletion(BASE + "CompletionInMethodCall1.groovy", "        new File(\"something\").ea^", false);
    }

    public void testCompletionInMethodCall2_1() throws Exception {
        checkCompletion(BASE + "CompletionInMethodCall2.groovy", "        new File(\"something\").c^", false);
    }

    public void testCompletionInMethodCall3_1() throws Exception {
        checkCompletion(BASE + "CompletionInMethodCall3.groovy", "if (new File(\"/\").is^) {", false);
    }

    public void testCompletionInArgument1_1() throws Exception {
        checkCompletion(BASE + "CompletionInArgument1.groovy", "println new URL(\"http://google.com\").getT^", false);
    }

    public void testCompletionForLiteral1_1() throws Exception {
        checkCompletion(BASE + "CompletionForLiteral1.groovy", "1.d^", false);
    }

    public void testCompletionForLiteral1_2() throws Exception {
        checkCompletion(BASE + "CompletionForLiteral1.groovy", "1.0.d^", false);
    }

    public void testCompletionForLiteral1_3() throws Exception {
        checkCompletion(BASE + "CompletionForLiteral1.groovy", "\"\".c^", false);
    }

    public void testCompletionInsideConstructor1_1() throws Exception {
        checkCompletion(BASE + "CompletionInsideConstructor1.groovy", "new File(\"/\").equals(new Date().a^", false);
    }

    public void testCompletionInsideConstructor1_2() throws Exception {
        checkCompletion(BASE + "CompletionInsideConstructor1.groovy", "new File(new Date().get^", false);
    }

    public void testCompletionInsideConstructor1_3() throws Exception {
        checkCompletion(BASE + "CompletionInsideConstructor1.groovy", "if (new File(new Date().get^", false);
    }

    public void testCompletionGeneratedAccessors1_1() throws Exception {
        checkCompletion(BASE + "CompletionGeneratedAccessors1.groovy", "        new Test().get^", false);
    }

    public void testCompletionGeneratedAccessors1_2() throws Exception {
        checkCompletion(BASE + "CompletionGeneratedAccessors1.groovy", "        new Test().set^", false);
    }
    
    public void testCompletionGeneratedAccessors1_3() throws Exception {
        checkCompletion(BASE + "CompletionGeneratedAccessors1.groovy", "        new Test().is^", false);
    }

    public void testCompletionGroovyClass1_1() throws Exception {
        checkCompletion(BASE + "CompletionGroovyClass1.groovy", "        new Test1().^", false);
    }

    public void testCompletionGroovyThis1_1() throws Exception {
        checkCompletion(BASE + "CompletionGroovyThis1.groovy", "        this.get^", false);
    }

    public void testCompletionGroovySuper1_1() throws Exception {
        checkCompletion(BASE + "CompletionGroovySuper1.groovy", "        super.^", false);
    }

    public void testCompletionNoDot1_1() throws Exception {
        checkCompletion(BASE + "CompletionNoDot1.groovy", "        no^", false);
    }

    public void testCompletionNoDot1_2() throws Exception {
        checkCompletion(BASE + "CompletionNoDot1.groovy", "        x^", false);
    }

    public void testCompletionNoDot1_3() throws Exception {
        checkCompletion(BASE + "CompletionNoDot1.groovy", "        n^", false);
    }

    public void testCompletionNoPrefixString1() throws Exception {
        checkCompletion(BASE + "CompletionNoPrefixString1.groovy", "println \"Hello $name!\".^", false);
    }

    public void testCompletionNoPrefixString2() throws Exception {
        checkCompletion(BASE + "CompletionNoPrefixString2.groovy", "def name='Petr'.^", false);
    }
}

