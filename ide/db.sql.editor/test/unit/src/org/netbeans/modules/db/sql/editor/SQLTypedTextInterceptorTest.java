/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
