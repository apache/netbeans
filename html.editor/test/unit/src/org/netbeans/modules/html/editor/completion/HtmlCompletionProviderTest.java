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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor.completion;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class HtmlCompletionProviderTest extends TestBase {

    public HtmlCompletionProviderTest(String testName) {
        super(testName);
    }

    public void testCheckOpenCompletion() throws BadLocationException {
        Document doc = createDocument();

        doc.insertString(0, "<", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 1, "<"));

        doc.insertString(1, "div", null);
        assertFalse(HtmlCompletionProvider.checkOpenCompletion(doc, 4, "div"));

        doc.insertString(4, " ", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 5, " "));

        doc.insertString(5, "/>", null);
        assertFalse(HtmlCompletionProvider.checkOpenCompletion(doc, 7, "/>"));

        doc.insertString(7, "</", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 9, "</"));

        doc.insertString(9, "div> &", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 15, "div> &"));

        //test end tag autocomplete
        doc.remove(0, doc.getLength());
        doc.insertString(0, "<div>", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 5, ">"));

    }

    //Bug 203048 - code completion autopopup doesn't always work on space
    public void test_issue203048() throws BadLocationException {
        Document doc = createDocument();

        doc.insertString(0, "<div >", null);
        //                   012345
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 5, " "));
        
        doc = createDocument();
        doc.insertString(0, "<div    >", null);
        //                   012345
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 6, " "));

    }

    //Bug 235048 - second tab activates suggestion in html editor 
    public void testDoNotOpenCompletionOnTabOrEnter() throws BadLocationException {
        Document doc = createDocument();

        doc.insertString(0, "<div >", null);
        //                   012345
        assertFalse(HtmlCompletionProvider.checkOpenCompletion(doc, 5, "    ")); //tab size 
        
    }
    
}
