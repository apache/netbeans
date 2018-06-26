/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.nodejs.editor;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Petr Pisl
 */
public class NodeJsContextTest extends JsTestBase {

    public NodeJsContextTest(String testName) {
        super(testName);
    }
    
    public void testContext01() throws Exception {
        checkCompletionContext("testfiles/context/context01.js");
    }
    
    public void testContext02() throws Exception {
        checkCompletionContext("testfiles/context/context02.js");
    }
    
    public void testSimpleServer() throws Exception {
        checkCompletionContext("testfiles/context/simpleServer.js");
    }
    
    public void testOnEvents() throws Exception {
        checkCompletionContext("testfiles/context/eventer.js");
    }
    
    public void testIssue248135() throws Exception {
        checkCompletionContext("testfiles/context/issue248135.js");
    }
    
    public void testIssue248135_01() throws Exception {
        checkCompletionContext("testfiles/context/issue248135A.js");
    }
    
    private void checkCompletionContext(final String filePath) throws Exception {
        Source testSource = getTestSource(getTestFile(filePath));
        Snapshot snapshot = testSource.createSnapshot();
        CharSequence text = snapshot.getText();
        StringBuilder sb = new StringBuilder();
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, 0);
        NodeJsContext contextPrevious = null;
        for (int offset = 0; offset < text.length(); offset++) {
            NodeJsContext context = NodeJsContext.findContext(ts , offset);
            if (!context.equals(contextPrevious)) {
                sb.append('[').append(context).append(']');
                contextPrevious = context;
            }
            sb.append(text.charAt(offset));
        }
        assertDescriptionMatches(filePath, sb.toString(), false, ".context");
    }
}
