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
package org.netbeans.modules.db.sql.editor;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.db.sql.lexer.SQLLanguageConfig;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

public class SQLTypedTextInterceptorTest extends CslTestBase {

    private final Preferences prefs;

    public SQLTypedTextInterceptorTest() {
        super("SQLTypedTextInterceptorTest");
        prefs = MimeLookup.getLookup(SQLLanguageConfig.mimeType).lookup(Preferences.class);
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new SQLLanguageConfig();
    }

    @Override
    protected String getPreferredMimeType() {
        return SQLTokenId.language().mimeType();
    }

    public void testBasicDisabled() throws Exception {
        prefs.putBoolean(OptionsUtils.PAIR_CHARACTERS_COMPLETION, false);
        insertChar(
                "select * from a where b = ^",
                '\'',
                "select * from a where b = '^",
                null, false);
        insertChar(
                "select * from a where b = ^",
                '"',
                "select * from a where b = \"^",
                null, false);
        insertChar(
                "select * from a where b = ^",
                '(',
                "select * from a where b = (^",
                null, false);
        insertChar(
                "select * from a where b = ^",
                '[',
                "select * from a where b = [^",
                null, false);
        insertChar(
                "select * from a where b = 'DEMO^'",
                '\'',
                "select * from a where b = 'DEMO'^'",
                null, false);
        insertChar(
                "select * from a where b = \"DEMO^\"",
                '"',
                "select * from a where b = \"DEMO\"^\"",
                null, false);
        insertChar(
                "select * from a where b = (DEMO^)",
                ')',
                "select * from a where b = (DEMO)^)",
                null, false);
        insertChar(
                "select * from a where b = [DEMO^]",
                ']',
                "select * from a where b = [DEMO]^]",
                null, false);
    }

    public void testBasicEnabled() throws Exception {
        prefs.putBoolean(OptionsUtils.PAIR_CHARACTERS_COMPLETION, true);
        insertChar(
                "select * from a where b = ^",
                '\'',
                "select * from a where b = '^'",
                null, false);
        insertChar(
                "select * from a where b = ^",
                '"',
                "select * from a where b = \"^\"",
                null, false);
        insertChar(
                "select * from a where b = ^",
                '(',
                "select * from a where b = (^)",
                null, false);
        insertChar(
                "select * from a where b = ^",
                '[',
                "select * from a where b = [^]",
                null, false);
        insertChar(
                "select * from a where b = 'DEMO^'",
                '\'',
                "select * from a where b = 'DEMO'^",
                null, false);
        insertChar(
                "select * from a where b = \"DEMO^\"",
                '"',
                "select * from a where b = \"DEMO\"^",
                null, false);
        insertChar(
                "select * from a where b = (DEMO^)",
                ')',
                "select * from a where b = (DEMO)^",
                null, false);
        insertChar(
                "select * from a where b = [DEMO^]",
                ']',
                "select * from a where b = [DEMO]^",
                null, false);
    }

    public void testEmptyCompletion() throws Exception {
        prefs.putBoolean(OptionsUtils.PAIR_CHARACTERS_COMPLETION, true);
        insertChar(
                "select * from a where b = '^'",
                '\'',
                "select * from a where b = ''^",
                null, false);
        insertChar(
                "select * from a where b = \"^\"",
                '"',
                "select * from a where b = \"\"^",
                null, false);
        insertChar(
                "select * from a where b = (^)",
                ')',
                "select * from a where b = ()^",
                null, false);
        insertChar(
                "select * from a where b = [^]",
                ']',
                "select * from a where b = []^",
                null, false);
    }

    public void testMultiStatementHandling() throws Exception {
        prefs.putBoolean(OptionsUtils.PAIR_CHARACTERS_COMPLETION, true);
        insertChar(
                "create table dummy(id integer, titel varchar(255)); select * from a where b = ^",
                '\'',
                "create table dummy(id integer, titel varchar(255)); select * from a where b = '^'",
                null, false);
        insertChar(
                "create table dummy(id integer, titel varchar(255)); select * from a where b = ^",
                '"',
                "create table dummy(id integer, titel varchar(255)); select * from a where b = \"^\"",
                null, false);
        insertChar(
                "create table dummy(id integer, titel varchar(255)); select * from a where b = ^",
                '(',
                "create table dummy(id integer, titel varchar(255)); select * from a where b = (^)",
                null, false);
        insertChar(
                "create table dummy(id integer, titel varchar(255)); select * from a where b = ^",
                '[',
                "create table dummy(id integer, titel varchar(255)); select * from a where b = [^]",
                null, false);
        insertChar(
                "create table dummy(id integer, titel varchar(255)); select * from a where b = 'DEMO^'",
                '\'',
                "create table dummy(id integer, titel varchar(255)); select * from a where b = 'DEMO'^",
                null, false);
        insertChar(
                "create table dummy(id integer, titel varchar(255)); select * from a where b = \"DEMO^\"",
                '"',
                "create table dummy(id integer, titel varchar(255)); select * from a where b = \"DEMO\"^",
                null, false);
        insertChar(
                "create table dummy(id integer, titel varchar(255)); select * from a where b = (DEMO^)",
                ')',
                "create table dummy(id integer, titel varchar(255)); select * from a where b = (DEMO)^",
                null, false);
        insertChar(
                "create table dummy(id integer, titel varchar(255)); select * from a where b = [DEMO^]",
                ']',
                "create table dummy(id integer, titel varchar(255)); select * from a where b = [DEMO]^",
                null, false);
    }

    public void testAlwaysSwallowClosingBraces() throws Exception {
        prefs.putBoolean(OptionsUtils.PAIR_CHARACTERS_COMPLETION, true);
        insertChar(
                "select * from a where (b = a^)",
                ')',
                "select * from a where (b = a)^",
                null, false);
        insertChar(
                "select * from a where (b = a)^)",
                ')',
                "select * from a where (b = a))^",
                null, false);
    }

    public void testNoCompletionInQuotes() throws Exception {
        prefs.putBoolean(OptionsUtils.PAIR_CHARACTERS_COMPLETION, true);
        // In quotes: No further completion
        insertChar(
                "select * from a where \"  ^  \"",
                '"',
                "select * from a where \"  \"^  \"",
                null, false);
        insertChar(
                "select * from a where \"  ^  \"",
                '(',
                "select * from a where \"  (^  \"",
                null, false);
        // Braces still allow completion
        insertChar(
                "select * from a where (  ^  )",
                '(',
                "select * from a where (  (^)  )",
                null, false);
    }
    
    public void testCompletionPrerequisite() throws Exception {
        prefs.putBoolean(OptionsUtils.PAIR_CHARACTERS_COMPLETION, true);
        // Only invoke completion for quotes with a leading whitespace or a dot
        insertChar(
                "select * from a where ^",
                '"',
                "select * from a where \"^\"",
                null, false);
        insertChar(
                "select * from a where^",
                '"',
                "select * from a where\"^",
                null, false);
        insertChar(
                "select * from a where a.^",
                '[',
                "select * from a where a.[^]",
                null, false);
        // braces only work after white-space
        insertChar(
                "select * from a where ^",
                '(',
                "select * from a where (^)",
                null, false);
        insertChar(
                "select * from a where^",
                '(',
                "select * from a where(^",
                null, false);
    }
}
