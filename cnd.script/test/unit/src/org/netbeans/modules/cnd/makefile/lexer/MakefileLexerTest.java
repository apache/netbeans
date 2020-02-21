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

package org.netbeans.modules.cnd.makefile.lexer;

import org.netbeans.modules.cnd.api.script.MakefileTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import static org.netbeans.lib.lexer.test.LexerTestUtilities.assertNextTokenEquals;

/**
 */
public class MakefileLexerTest extends NbTestCase {

    public MakefileLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testSimple() {
        TokenSequence<?> ts = lex("# Environment\n" +
                      "MKDIR=mkdir\n" +
                      "BUILDDIR=build/${CONF}\n" +
                      "OS := $(shell uname | grep -i Darwin)\n\n" +
                      "build:\n" +
                      "\t$(COMPILE.cc) source.cpp -o source.o\n\n" +
                      ".PHONY: build\n" +
                      "include foo.mk\n");

        assertNextTokenEquals(ts, MakefileTokenId.COMMENT, "# Environment");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "MKDIR");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "mkdir");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "BUILDDIR");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "build/");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "${CONF}");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "OS");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.COLON_EQUALS, ":=");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(shell uname | grep -i Darwin)");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "build");
        assertNextTokenEquals(ts, MakefileTokenId.COLON, ":");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(COMPILE.cc)");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, " source.cpp -o source.o");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.SPECIAL_TARGET, ".PHONY");
        assertNextTokenEquals(ts, MakefileTokenId.COLON, ":");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "build");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.INCLUDE, "include");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "foo.mk");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testBare() {
        TokenSequence<?> ts = lex("a\\ b := a\\:b a:b a\\ b\na\\ b:");

        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a\\ b");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.COLON_EQUALS, ":=");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a\\:b");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a:b");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a\\ b");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a\\ b");
        assertNextTokenEquals(ts, MakefileTokenId.COLON, ":");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testTabs() {
        TokenSequence<?> ts = lex("\tfoo\n\t\tbar\n \tbaz");

        assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, "foo");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, "bar");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " \t");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "baz");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testNewline() {
        TokenSequence<?> ts = lex("var = foo\\\n\\\r\n\tbar\n");

        assertNextTokenEquals(ts, MakefileTokenId.BARE, "var");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "foo");
        assertNextTokenEquals(ts, MakefileTokenId.ESCAPED_NEW_LINE, "\\\n");
        assertNextTokenEquals(ts, MakefileTokenId.ESCAPED_NEW_LINE, "\\\r\n");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "bar");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testShell() {
        TokenSequence<?> ts = lex("\t@+-echo foo\\\nbar\\\n\tbaz\nfoo: ; -bar\n");

        assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, "@+-echo foo\\\nbar\\\n\tbaz");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "foo");
        assertNextTokenEquals(ts, MakefileTokenId.COLON, ":");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.SEMICOLON, ";");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, "-bar");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testMacro() {
        TokenSequence<?> ts = lex("ab=$(a\nb)\ncd=$(c\\\nd");

        assertNextTokenEquals(ts, MakefileTokenId.BARE, "ab");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(a");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "b)");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "cd");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(c\\\nd");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testComment() {
        TokenSequence<?> ts = lex("A=B #\\\nA=B");

        assertNextTokenEquals(ts, MakefileTokenId.BARE, "A");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "B");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.COMMENT, "#\\\nA=B");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testEscapes() {
        TokenSequence<?> ts = lex("a\\:=b\nc\\=d\ne\\+=f\n\\:all\\::\\#\n\techo $(a\\) $(c\\) $(e\\)");

        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a\\");
        assertNextTokenEquals(ts, MakefileTokenId.COLON_EQUALS, ":=");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "b");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "c\\");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "d");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "e\\");
        assertNextTokenEquals(ts, MakefileTokenId.PLUS_EQUALS, "+=");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "f");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "\\:all\\:");
        assertNextTokenEquals(ts, MakefileTokenId.COLON, ":");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "\\#");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, "echo ");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(a\\)");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, " ");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(c\\)");
        assertNextTokenEquals(ts, MakefileTokenId.SHELL, " ");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(e\\)");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testDefine() {
        TokenSequence<?> ts = lex("define FOO\nBAR=$(BAZ)\na: b\n\techo endef\n#dummy comment\nendef\nBAR=BAZ\n");

        assertNextTokenEquals(ts, MakefileTokenId.DEFINE, "define");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "FOO");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "BAR=");
        assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(BAZ)");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a:");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "b");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, "\t");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "echo");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "endef");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.COMMENT, "#dummy comment");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.ENDEF, "endef");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "BAR");
        assertNextTokenEquals(ts, MakefileTokenId.EQUALS, "=");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "BAZ");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    public void testIndent() {
        TokenSequence<?> ts = lex("include a\n  include b\n    include c\n");

        assertNextTokenEquals(ts, MakefileTokenId.INCLUDE, "include");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "a");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, "  ");
        assertNextTokenEquals(ts, MakefileTokenId.INCLUDE, "include");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "b");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, "    ");
        assertNextTokenEquals(ts, MakefileTokenId.INCLUDE, "include");
        assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        assertNextTokenEquals(ts, MakefileTokenId.BARE, "c");
        assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");

        assertFalse("Unexpected tokens remaining", ts.moveNext());
    }

    private static TokenSequence<MakefileTokenId> lex(String text) {
        TokenHierarchy<?> hi = TokenHierarchy.create(text, MakefileTokenId.language());
        return hi.tokenSequence(MakefileTokenId.language());
    }
}
