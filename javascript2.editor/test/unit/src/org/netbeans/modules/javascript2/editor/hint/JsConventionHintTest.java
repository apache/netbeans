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
package org.netbeans.modules.javascript2.editor.hint;

import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.javascript2.editor.hints.ArrayTrailingComma;
import org.netbeans.modules.javascript2.editor.hints.AssignmentInCondition;
import org.netbeans.modules.javascript2.editor.hints.BetterConditionHint;
import org.netbeans.modules.javascript2.editor.hints.DuplicatePropertyName;
import org.netbeans.modules.javascript2.editor.hints.JsConventionRule;
import org.netbeans.modules.javascript2.editor.hints.MissingSemicolonHint;
import org.netbeans.modules.javascript2.editor.hints.ObjectTrailingComma;

/**
 *
 * @author Petr Pisl
 */
public class JsConventionHintTest extends HintTestBase {

    public JsConventionHintTest(String testName) {
        super(testName);
    }
    
    private Rule createRule() {
        return new JsConventionRule();
    }
    
    private Rule createSemicolonHint() {
        return new MissingSemicolonHint();
    }
    
    private Rule createBetterConditionHint() {
        return new BetterConditionHint();
    }
    
    private Rule createDuplicatePropertyHint() {
        return new DuplicatePropertyName();
    }
    
    public void testClassDeclaration01() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/classDeclaration.js", null);
    }
    
    public void testDefaultParameters01() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/defaultParameters.js", null);
    }
    
    public void testSemicolonAssignment() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/coloring/assignments01.js", null);
    }
    
    public void testSemicolon01() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/variableDeclaration.js", null);
    }
    
    public void testSemicolon02() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/missingSemicolon01.js", null);
    }
    
    public void testSemicolon03() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/varInForNode.js", null);
    }
    
    public void testSemicolon04() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/docComment1.js", null);
    }
    
    public void testSemicolon05() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/docComment2.js", null);
    }

    public void testSemicolonIssue218042() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue218042.js", null);
    }

    public void testSemicolonIssue219193() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue219193.js", null);
    }

    public void testBetterConditionIssue218042() throws Exception {
        checkHints(this, createBetterConditionHint(), "testfiles/hints/issue218042.js", null);
    }

    public void testSemicolonIssue218108() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue218108.js", null);
    }

    public void testSemicolonIssue218446() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue218446.js", null);
    }

    public void testSemicolonIssue217079() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue217079.js", null);
    }
    
    public void testSemicolonIssue226996() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue226996.js", null);
    }
    
    public void testSemicolonIssue228217() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue228217.js", null);
    }
    
    public void testSemicolonIssue262468() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue262468.js", null);
    }

    public void testObjectTrailingComma01() throws Exception {
        checkHints(this, new ObjectTrailingComma(), "testfiles/hints/objectTrailingComma.js", null);
    }

    public void testArrayTrailingComma01() throws Exception {
        checkHints(this, new ArrayTrailingComma(), "testfiles/hints/arrayTrailingComma.js", null);
    }
    
    public void testAccidentalAssignment01() throws Exception {
        checkHints(this, new AssignmentInCondition(), "testfiles/hints/accidentalAssignment.js", null);
    }
    
    public void testBetterCondition01() throws Exception {
        checkHints(this, createBetterConditionHint(), "testfiles/hints/betterCondition.js", null);
    }
    
    public void testDuplicateName01() throws Exception {
        checkHints(this, createDuplicatePropertyHint(), "testfiles/hints/duplicateName.js", null);
    }
    
    public void testDuplicateName02() throws Exception {
        checkHints(this, createDuplicatePropertyHint(), "testfiles/hints/duplicateName02.js", null);
    }
    
    public void testDuplicateName03() throws Exception {
        checkHints(this, createDuplicatePropertyHint(), "testfiles/hints/duplicateName03.js", null);
    }

    public void testIssue218590() throws Exception {
        checkHints(this, createDuplicatePropertyHint(), "testfiles/hints/issue218590.js", null);
    }

    public void testIssue221454() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue221454.js", null);
    }

    public void testIssue221497() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue221497.js", null);
    }
    
    public void testIssue244944() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue244944.js", null);
    }
    
    public void testIssue251642() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue251642.js", null);
    }
    
    public void testIssue252023() throws Exception {
        checkHints(this, new AssignmentInCondition(), "testfiles/hints/issue252023.js", null);
    }
    
    public void testIssue258874() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue258874.js", null);
    }
    
    public void testIssue258901_01() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue258901_01.js", null);
    }
    
    public void testIssue258901_02() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue258901_02.js", null);
    }
    
    public void testIssue258901_03() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue258901_03.js", null);
    }
    
    public void testIssue258901_04() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue258901_04.js", null);
    }
    
    public void testIssue258901_05() throws Exception {
        checkHints(this, createSemicolonHint(), "testfiles/hints/issue258901_05.js", null);
    }   
       
    public void testIssue269659_01() throws Exception {
        checkHints(this, createDuplicatePropertyHint(), "testfiles/hints/issue269659_01.js", null);
    }
	
    public void testIssue269659_02() throws Exception {
        checkHints(this, createDuplicatePropertyHint(), "testfiles/hints/issue269659_02.js", null);
    }
        
    public void testIssue269659_03() throws Exception {
        checkHints(this, createDuplicatePropertyHint(), "testfiles/hints/issue269659_03.js", null);
    } 
    
    public void testIssue269659_04() throws Exception {
        checkHints(this, createDuplicatePropertyHint(), "testfiles/hints/issue269659_04.js", null);
    } 
}
