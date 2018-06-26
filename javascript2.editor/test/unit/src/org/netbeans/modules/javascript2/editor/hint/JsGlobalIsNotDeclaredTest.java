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

import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.javascript2.editor.hints.GlobalIsNotDefined;

/**
 *
 * @author Petr Pisl
 */
public class JsGlobalIsNotDeclaredTest extends HintTestBase {

    public JsGlobalIsNotDeclaredTest(String testName) {
        super(testName);
    }
    
    private static class GlobalIsNotDefinedHint extends GlobalIsNotDefined {
        @Override
        public HintSeverity getDefaultSeverity() {
            return HintSeverity.WARNING;
        }
        
    }
    
    private Rule createRule() {
        GlobalIsNotDefined gind = new GlobalIsNotDefinedHint();
        return gind;
    }
    
    public void testSimple01() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/globalIsNotDeclared.js", null);
    }
    
    public void testIssue224040() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue224040.js", null);
    }
    
    public void testIssue224041() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue224041.js", null);
    }
    
    public void testIssue224035() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue224035.js", null);
    }
    
    public void testIssue225048() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue225048.js", null);
    }
    
    public void testIssue225048_01() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue225048_01.js", null);
    }
    
    public void testIssue250372() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue250372.js", null);
    }
    
    public void testIssue248696_01() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue248696_01.js", null);
    }
    
    public void testIssue248696_02() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue248696_02.js", null);
    }
    
    public void testIssue252022() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue252022.js", null);
    }

    public void testIssue249487() throws Exception {
        checkHints(this, createRule(), "testfiles/markoccurences/issue249487.js", null);
    }
    
    public void testIssue255494() throws Exception {
        checkHints(this, createRule(), "testfiles/coloring/issue255494.js", null);
    }
    
    public void testIssue268384() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue268384.js", null);
    }
    
}
