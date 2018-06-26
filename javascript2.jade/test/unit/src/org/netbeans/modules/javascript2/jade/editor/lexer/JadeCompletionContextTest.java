/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.jade.editor.lexer;

import java.util.Collections;
import static junit.framework.Assert.assertTrue;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.jade.editor.JadeCompletionContext;
import org.netbeans.modules.javascript2.jade.editor.JadeTestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Petr Pisl
 */
public class JadeCompletionContextTest extends JadeTestBase {

    public JadeCompletionContextTest(String testName) {
        super(testName);
    }
    
    public void testContext01() throws Exception {
        checkCompletionContext("testfiles/lexer/tag01.jade");
    }
    
    public void testAttribute03() throws Exception {
        checkCompletionContext("testfiles/lexer/attribute03.jade");
    }
    
    public void testTag01() throws Exception {
        checkCompletionContext("testfiles/ccContext/tag01.jade");
    }
    
    public void testTag02() throws Exception {
        checkCompletionContext("testfiles/ccContext/tag02.jade");
    }
    
    public void testTag03() throws Exception {
        checkCompletionContext("testfiles/ccContext/tag03.jade");
    }
    
    public void testIssue250743() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250743.jade");
    }
    
    public void testIssue250742() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250742.jade");
    }
    
    public void testIssue250741() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250741.jade");
    }
    
    public void testIssue250739() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250739.jade");
    }
    
    public void testIssue250738() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250738.jade");
    }
    
    public void testIssue250734() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250734.jade");
    }
    
    public void testIssue250732() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250732.jade");
    }
    
    public void testIssue250731() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250731.jade");
    }
    
    public void testIssue250736() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250736.jade");
    }
    
    public void testIssue250735() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250735.jade");
    }
    
    public void testIssue251132() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251132.jade");
    }
    
    public void testIssue251152() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251152.jade");
    }
    
    public void testIssue251160() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251160.jade");
    }
    
    public void testIssue251278() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251278.jade");
    }
    
    public void testIssue251281() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251281.jade");
    }
    
    public void testIssue251157() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251157.jade");
    }
    
    public void testIssue251153() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251153.jade");
    }
    
    public void testIssue254618() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue254618.jade");
    }
    
    public void testIssue254617() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue254617.jade");
    }
    
    private void checkCompletionContext(final String filePath) throws Exception {
        Source testSource = getTestSource(getTestFile(filePath));
        final Snapshot snapshot = testSource.createSnapshot();
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult info = (ParserResult) r;

                CharSequence text = snapshot.getText();
                StringBuilder sb = new StringBuilder();
        
                JadeCompletionContext contextPrevious = null;
                for (int offset = 0; offset < text.length(); offset++) {
                    JadeCompletionContext context = JadeCompletionContext.findCompletionContext(info , offset);
                    if (!context.equals(contextPrevious)) {
                        sb.append('[').append(context).append(']');
                        contextPrevious = context;
                    }
                    sb.append(text.charAt(offset));
                }
                assertDescriptionMatches(filePath, sb.toString(), false, ".context");
            }
        });
        
    }
}
