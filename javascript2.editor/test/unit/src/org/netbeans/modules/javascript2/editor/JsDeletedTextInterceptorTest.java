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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.javascript2.editor;

import java.util.prefs.Preferences;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.editor.options.OptionsUtils;

/**
 * @todo Try typing in whole source files and other than tracking missing end and } closure
 *   statements the buffer should be identical - both in terms of quotes to the rhs not having
 *   accumulated as well as indentation being correct.
 * @todo
 *   // automatic reindentation of "end", "else" etc.
 *
 * 
 * 
 * @author Tor Norbye
 */
public class JsDeletedTextInterceptorTest extends JsTestBase {
    
    public JsDeletedTextInterceptorTest(String testName) {
        super(testName);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class).clear();
    }
    
    @Override
    protected org.netbeans.modules.csl.api.Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }
    
    @Override
    public void deleteWord(String original, String expected) throws Exception {
        // Try deleting the word not just using the testcase but also surrounded by strings
        // to make sure there's no problem with lexer token directions
        super.deleteWord(original, expected);
        super.deleteWord(original+"foo", expected+"foo");
        super.deleteWord("foo"+original, "foo"+expected);
        super.deleteWord(original+"::", expected+"::");
        super.deleteWord(original+"::", expected+"::");
    }
    
    public void testBackspace1() throws Exception {
        deleteChar("x^", "^");
    }

    public void testBackspace2() throws Exception {
        deleteChar("x^y", "^y");
    }

    public void testBackspace3() throws Exception {
        deleteChar("xy^z", "x^z");
    }

    public void testBackspace4() throws Exception {
        deleteChar("xy^z", "x^z");
    }

    public void testBackspace5() throws Exception {
        deleteChar("x=\"^\"", "x=^");
    }

    public void testBackspace6() throws Exception {
        deleteChar("x='^'", "x=^");
    }

    public void testBackspace7() throws Exception {
        deleteChar("x=(^)", "x=^");
    }

    public void testBackspace7b() throws Exception {
        deleteChar("x=[^]", "x=^");
    }

    public void testBackspace8() throws Exception {
        // See bug 111534
        deleteChar("x={^}", "x=^");
    }

    public void testBackspace9() throws Exception {
        deleteChar("x=/^/", "x=^");
    }
    
    public void testBackspace10() throws Exception {
        deleteChar("x=`^`", "x=^");
    }

    public void testDeleteContComment() throws Exception {
        deleteChar("// ^", "//^");
        deleteChar("\n// ^", "\n//^");
    }

    public void testDeleteContComment2() throws Exception {
        deleteChar("// ^var x = 5", "^var x = 5");
        deleteChar("\n// ^alert()", "\n^alert()");
    }

    public void testNoDeleteContComment() throws Exception {
        deleteChar("//  ^", "// ^");
        deleteChar("//^", "/^");
        deleteChar("// ^  ", "//^  ");
        deleteChar("\n// ^  ", "\n//^  ");
        deleteChar("puts ('// ^')", "puts ('//^')");
    }

    public void testDeleteWord1() throws Exception {
        deleteWord("FooBarBaz^", "FooBar^");
    }

    public void testDeleteWord2() throws Exception {
        deleteWord("Set^Foo", "^Foo");
    }


    public void testDeleteWord3() throws Exception {
        deleteWord("foo bar^", "foo ^");
    }

    public void testDeleteWord4() throws Exception {
        String before = "  snark^\n";
        String after = "  ^\n";
        deleteWord(before, after);
    }

    public void testDeleteWord5() throws Exception {
        deleteWord("foo bar     ^", "foo bar^");
    }

    public void testDeleteWord6() throws Exception {
        deleteWord("foo bar ^", "foo bar^");
    }

    public void testBackwardsDeletion() throws Exception {
        String s = "alert('hello')  \n  nextline";
        final Exception[] ex = new Exception[1];
        for (int i = s.length(); i >= 1; i--) {
            String shortened = s.substring(0, i);
            final BaseDocument doc = getDocument(shortened);
            final int index = i;
            ex[0] = null;
            doc.render(new Runnable() {

                @Override
                public void run() {
                    JTextArea ta = new JTextArea(doc);
                    Caret caret = ta.getCaret();
                    int dot = index;
                    caret.setDot(dot);
                    int begin = JsCamelCaseInterceptor.getWordOffset(doc, dot, true);
                    if (begin == -1) {
                        try {
                            begin = Utilities.getPreviousWord(ta, dot);
                        } catch (BadLocationException ex1) {
                            ex[0] = ex1;
                        }
                    }

                    assert begin != -1 && begin < index;
                }
            });
            if (ex[0] != null) {
                throw ex[0];
            }
        }
    }

    public void testDisabledSmartQuotes1() throws Exception {
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class)
                .putBoolean(OptionsUtils.AUTO_COMPLETION_SMART_QUOTES, false);
        deleteChar("x = \"^\"", "x = ^\"");
    }

    public void testDisabledSmartQuotes2() throws Exception {
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class)
                .putBoolean(OptionsUtils.AUTO_COMPLETION_SMART_QUOTES, false);
        deleteChar("x = `^`", "x = ^`");
    }

    public void testDisabledBrackets1() throws Exception {
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class)
                .putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
        deleteChar("x = (^)", "x = ^)");
    }

    public void testDisabledBrackets2() throws Exception {
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class)
                .putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
        deleteChar("x = [^]", "x = ^]");
    }

    public void testDisabledBrackets3() throws Exception {
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class)
                .putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
        deleteChar("x = {^}", "x = ^}");
    }

    public void testNoDeleteSlashInString() throws Exception {
        deleteChar("'http://localhost/aa/src/^/todolists.php'", "'http://localhost/aa/src^/todolists.php'");
    }

}
