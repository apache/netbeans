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

package org.netbeans.lib.html.lexer;

import junit.framework.TestCase;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.lib.lexer.test.FixedTextDescriptor;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.RandomCharDescriptor;
import org.netbeans.lib.lexer.test.RandomModifyDescriptor;
import org.netbeans.lib.lexer.test.RandomTextProvider;
import org.netbeans.lib.lexer.test.TestRandomModify;

/**
 * Jsp Lexer random test
 *
 * @author Miloslav.Metelka@Sun.COM
 * @author Marek.Fukala@Sun.COM
 */
public class JspLexerRandomTest extends TestCase {
    
    public JspLexerRandomTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testRandom() throws Exception {
        test(0);
    }
    
    private void test(long seed) throws Exception {
        TestRandomModify randomModify = new TestRandomModify(seed);
        randomModify.setLanguage(HTMLTokenId.language());
        
        //randomModify.setDebugOperation(true);
        //randomModify.setDebugDocumentText(true);
        //randomModify.setDebugHierarchy(true);

        // Explicit inserts/removals checks
        randomModify.insertText(0, "<a>");
        randomModify.insertText(2, " "); //mfukala - IMO it will be broken even here
        randomModify.removeText(2, 1);
        
        randomModify.clearDocument();

        //mfukala - temp. test
        randomModify.insertText(0, "<body>");
        randomModify.insertText(5, " "); // error: '>' is text here
        randomModify.insertText(6, "bgcolor='red'");  //error: attr name and value are lexed incorrectly
        randomModify.clearDocument();
        
        // Begin really randomized testing
        FixedTextDescriptor[] fixedTexts = new FixedTextDescriptor[] {
            FixedTextDescriptor.create("/>", 0.2),
        };
        
        RandomCharDescriptor[] regularChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.letter(0.2),
            RandomCharDescriptor.space(0.2),
            RandomCharDescriptor.lf(0.05),
            RandomCharDescriptor.chars(new char[] { '<', '>', '=' }, 0.3),
        };

        RandomTextProvider regularTextProvider = new RandomTextProvider(regularChars, fixedTexts);
        
        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(200, regularTextProvider,
                        0.4, 0.2, 0.2,
                        0.1, 0.1,
                        0.0, 0.0), // snapshots create/destroy
                new RandomModifyDescriptor(200, regularTextProvider,
                        0.2, 0.2, 0.1,
                        0.4, 0.3,
                        0.0, 0.0), // snapshots create/destroy
            }
        );
    }
    
}
