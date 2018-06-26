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

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author schmidtm
 */
public class CodeCompletionTest extends GroovyTestBase {

    String TEST_BASE = "testfiles/completion/";

    public CodeCompletionTest(String testName) {
        super(testName);
        Logger.getLogger(CompletionHandler.class.getName()).setLevel(Level.FINEST);
    }

    @Override
    protected Set<String> additionalSourceClassPath() {
        return Collections.singleton(TEST_BASE);
    }

    public void testMethodCompletion1() throws Exception {
        checkCompletion(TEST_BASE + "MethodCompletionTestCase.groovy", "new String().toS^", false);
    }

    public void testMethodCompletion2() throws Exception {
        checkCompletion(TEST_BASE + "MethodCompletionTestCase.groovy", "new String().find^", false);
    }

    public void testScriptLong1() throws Exception {
        checkCompletion(TEST_BASE + "ScriptLong1.groovy", "l.MA^", false);
    }

    public void testScriptLong2() throws Exception {
        checkCompletion(TEST_BASE + "ScriptLong2.groovy", "l.comp^", false);
    }

    public void testScriptString1() throws Exception {
        checkCompletion(TEST_BASE + "ScriptString1.groovy", "s.val^", false);
    }

    public void testScriptString2() throws Exception {
        checkCompletion(TEST_BASE + "ScriptString2.groovy", "s.spli^", false);
    }

    public void testScriptStringConst1() throws Exception {
        checkCompletion(TEST_BASE + "ScriptStringConst1.groovy", "\" ddd \".toS^", false);
    }

    public void testClassMethodFieldString1() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodFieldString1.groovy", "stringField.toL^", false);
    }

    public void testClassMethodFieldString2() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodFieldString2.groovy", "stringField.spli^", false);
    }

    public void testClassMethodFieldLong1() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodFieldLong1.groovy", "longField.MAX^", false);
    }

    public void testClassMethodFieldLong2() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodFieldLong2.groovy", "longField.comp^", false);
    }

    public void testClassMethodLocalLong1() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodLocalLong1.groovy", "localLong.MAX^", false);
    }

    public void testClassMethodLocalLong2() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodLocalLong2.groovy", "localLong.comp^", false);
    }

    public void testClassMethodLocalString1() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodLocalString1.groovy", "localString.toL^", false);
    }

    public void testClassMethodLocalString2() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodLocalString2.groovy", "localString.get^", false);
    }

    public void testKeywordImport1() throws Exception {
        checkCompletion(TEST_BASE + "KeywordImport1.groovy", "import java.lang.Ab^", false);
    }

    public void testKeywordAboveClass1() throws Exception {
        checkCompletion(TEST_BASE + "KeywordAboveClass1.groovy", "ab^", false);
    }

    public void testKeywordAboveClass2() throws Exception {
        checkCompletion(TEST_BASE + "KeywordAboveClass2.groovy", "ab^", false);
    }

//    // proper recognition of Constructor calls and the corresponding types.
//
//    public void testConstructorCall1() throws Exception {
//        checkCompletion(TEST_BASE + "ConstructorCall1.groovy", "println new URL(\"http://google.com\").getT^", false);
//    }
//
//    // Test CamelCase constructor-proposals
//
//    public void testCamelCaseConstructor1() throws Exception {
//        checkCompletion(TEST_BASE + "CamelCaseConstructor1.groovy", "SSC^", false);
//    }
//
//
//    // Package completion could not be tested at the moment, since this statement returns nothing for "java.n|":
////    pkgSet = pathInfo.getClassIndex().getPackageNames(packageRequest.fullString, true, EnumSet.allOf(ClassIndex.SearchScope.class));
//
////    public void testKeywordImport2() throws Exception {
////        checkCompletion(TEST_BASE + "KeywordImport2.groovy", "import java.n^", false);
////        assertTrue(false);
////    }
//
//
////    Testing all completion possibilities for java.lang.String is broken
//
////    public void testClassMethodLocalStringConst1() throws Exception {
////        checkCompletion(TEST_BASE + "ClassMethodLocalStringConst1.groovy", "\" ddd \".^", false);
////    }

}
