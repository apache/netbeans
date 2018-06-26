/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import junit.framework.AssertionFailedError;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.php.smarty.editor.TplDataLoader;
import org.netbeans.modules.php.smarty.editor.gsf.TplLanguage;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplCompletionQueryTest extends CslTestBase {

    public TplCompletionQueryTest(String name) {
        super(name);
    }

    // 
    public void testTagAttributeValues() throws Exception {
        assertItems("{|", arr("append", "if", "section"), Match.CONTAINS);
    }

    public void testIssue22376() throws Exception {
        assertItems("{|\n", arr("append", "if", "section"), Match.CONTAINS);
    }

    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type) throws Exception {
        assertItems(documentText, expectedItemsNames, type, -1);
    }

    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws Exception {
        assertItems(getDocument(documentText), expectedItemsNames, type, expectedAnchor);
    }

    protected void assertItems(Document doc, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws Exception {
        String content = doc.getText(0, doc.getLength());

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";

        //remove the pipe
        doc.remove(pipeOffset, 1);

        TplCompletionQuery query = new TplCompletionQuery(doc);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);

        TplCompletionQuery.CompletionResult completionResult = query.query();

        if (type != Match.EMPTY) {
            assertNotNull("null completion query result", completionResult);
        }

        if (expectedItemsNames.length == 0 && completionResult == null) {
            //result may be null if we do not expect any result, nothing to test then
            return;
        }

        ArrayList<TplCompletionItem> items = completionResult.getFunctions();
        items.addAll(completionResult.getVariableModifiers());
        assertNotNull(items);

        try {
            assertCompletionItemNames(expectedItemsNames, items, type);
        } catch (AssertionFailedError e) {
            for (CompletionItem item : items) {
                System.out.println(((TplCompletionItem) item).getItemText());
            }
            throw e;
        }
    }

    private void assertCompletionItemNames(String[] expected, Collection<? extends CompletionItem> ccresult, Match type) {
        Collection<String> real = new ArrayList<String>();
        for (CompletionItem ccp : ccresult) {
            //check only html items
            if (ccp instanceof TplCompletionItem) {
                TplCompletionItem tplci = (TplCompletionItem) ccp;
                real.add(tplci.getItemText());
            }
        }
        Collection<String> exp = new ArrayList<String>(Arrays.asList(expected));

        if (type == Match.EXACT) {
            assertEquals(exp, real);
        } else if (type == Match.CONTAINS) {
            exp.removeAll(real);
            assertEquals(exp, Collections.EMPTY_LIST);
        } else if (type == Match.EMPTY) {
            assertEquals(0, real.size());
        } else if (type == Match.NOT_EMPTY) {
            assertTrue(real.size() > 0);
        } else if (type == Match.DOES_NOT_CONTAIN) {
            int originalRealSize = real.size();
            real.removeAll(exp);
            assertEquals(originalRealSize, real.size());
        }
    }

    public static enum Match {
        EXACT, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY;
    }

    protected String[] arr(String... args) {
        return args;
    }

    @Override
    protected String getPreferredMimeType() {
        return TplDataLoader.MIME_TYPE;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new TplLanguage();
    }
}
