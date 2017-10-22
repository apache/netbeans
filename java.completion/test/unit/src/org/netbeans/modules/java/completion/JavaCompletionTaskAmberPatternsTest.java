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

import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author Dusan Balek
 */
//TODO: smart types
public class JavaCompletionTaskAmberPatternsTest extends CompletionTestBase {

    public JavaCompletionTaskAmberPatternsTest(String testName) {
        super(testName);
    }

    public void testBoundPatternVariable1() throws Exception {
        performTest("Patterns", 143, "+ st", "testBoundPatternVariable1.pass", "1.9");
    }

    public void testBoundPatternVariable2() throws Exception {
        performTest("Patterns", 143, "+ str.len", "testBoundPatternVariable2.pass", "1.9");
    }

    public void testMatches1() throws Exception {
        //TODO: fix the remaining instances of addKeyword(env, INSTANCEOF_KEYWORD, SPACE, false);
        performTest("Patterns", 169, " ", "testMatches1.pass", "1.9");
    }

    public void testTypeAfterMatches() throws Exception {
        performTest("Patterns", 169, " __matches ", "javaLangTypes.pass", "1.9");
    }

    public void testVariablePatternName() throws Exception {
        performTest("Patterns", 169, " __matches String ", "testVariablePattern1.pass", "1.9");
        performTest("Patterns", 169, " __matches int ", "testVariablePattern2.pass", "1.9");
        performTest("Patterns", 169, " __matches java.util.List<String> ", "testVariablePattern3.pass", "1.9");
        performTest("Patterns", 169, " __matches String[] ", "testVariablePattern4.pass", "1.9");
        performTest("Patterns", 169, " __matches int[] ", "testVariablePattern5.pass", "1.9");
    }

    public void testVariablePatternInSwitch() throws Exception {
        performTest("Patterns", 213, " ", "javaLangTypes.pass", "1.9");
        performTest("Patterns", 213, "String ", "testVariablePatternInSwitch2.pass", "1.9");
    }

    @Override
    protected void runTest() throws Throwable {
        try {
            Class.forName("com.sun.source.tree.MatchesTree");
        } catch (ClassNotFoundException ex) {
            //skip
            return ;
        }
        super.runTest();
    }
    

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
