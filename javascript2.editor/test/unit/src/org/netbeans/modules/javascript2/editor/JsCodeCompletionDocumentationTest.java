/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsCodeCompletionDocumentationTest extends JsCodeCompletionBase {

    public JsCodeCompletionDocumentationTest(String testName) {
        super(testName);
    }

    public void testCompletionDocumentation01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/documentation01.js", "eer^o(1, 2);", false, "eer");
    }

    public void testCompletionDocumentation02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/documentation01.js", "vari^able;", false, "vari");
    }

    public void testCompletionDocumentation03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/documentation01.js", "prom^enna;", false, "prom");
    }

    public void testIssue180805() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/documentation01.js", "tes^t(1, 2);", false, "tes");
    }

    public void testIssue223104() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue223104.js", "testOptionalPa^rameter(\"nevim\", \"nevim2\");", false, "testOptionalPa");
    }

    public void testIssue226631() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue226631.js", "/*HERE*/ maxipe^", false, "maxipe");
    }

    public void testIssue255966_1() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "personsArr^.push({});", false, "personsArr");
    }
    
    public void testIssue255966_2() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "arrS^tr.push(\"test\");", false, "arrS");
    }
    
    
    public void testIssue255966_3() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "arrO^bj[1]", false, "arrO");
    }
    
    public void testIssue255966_4() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "personO^bj[\"firstName\"];", false, "personO");
    }

   
    public void testIssue255966_5() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "personsN^ew.push({});", false, "personsN");
    }
    
    public void testIssue255966_6() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "printOb^js(persons);", false, "printOb");
    }
    public void testIssue255966_7() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "anotherFun^c(\"test\");", false, "anotherFun");
    }
    public void testIssue255966_8() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "getTmp^Object();", false, "getTmp");
    }
    public void testIssue255966_9() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "tryMe^ssage();", false, "tryMe");
    }
    public void testIssue255966_10() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "getSe^lect();", false, "getSe");
    }
    public void testIssue255966_11() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "returnOb^j();", false, "returnOb");
    }
    public void testIssue255966_12() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "testT^ype();", false, "testT");
    }
}    
