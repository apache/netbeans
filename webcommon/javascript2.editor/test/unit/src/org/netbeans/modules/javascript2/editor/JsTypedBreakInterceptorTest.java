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

package org.netbeans.modules.javascript2.editor;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

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
// XXX note this also tests indenter
public class JsTypedBreakInterceptorTest extends JsTestBase {

    public JsTypedBreakInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class).clear();
        JsTypedBreakInterceptor.completeDocumentation = false;
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    // FIXME this is wrong because it is computed form diff of previous
    public void testInsertBrace4() throws Exception {
        insertBreak("function test(){\n    if(true &&\n        true){^\n    }\n}",
                "function test(){\n    if(true &&\n        true){\n    ^\n    }\n}");
    }

    public void testInsertBrace1() throws Exception {
        insertBreak("foobar({^});", "foobar({\n    ^\n});");
    }

    public void testInsertBrace2() throws Exception {
        insertBreak("foobar([^]);", "foobar([\n    ^\n]);");
    }

    public void testInsertBrace3() throws Exception {
        insertBreak("x = {^}", "x = {\n    ^\n}");
    }

    public void testInsertBlockComment() throws Exception {
        insertBreak("/**^", "/**\n * ^\n */");
    }

    public void testInsertBlockComment2() throws Exception {
        insertBreak("    /**^", "    /**\n     * ^\n     */");
    }

    public void testInsertBlockComment3() throws Exception {
        insertBreak("/*^\n", "/*\n * ^\n */\n");
    }

    public void testInsertBlockComment4() throws Exception {
        insertBreak("/*^\nfunction foo() {}", "/*\n * ^\n */\nfunction foo() {}");
    }

    public void testInsertBlockComment5() throws Exception {
        insertBreak("^/*\n*/\n", "\n^/*\n*/\n");
    }

    public void testSplitStrings1() throws Exception {
        insertBreak("  x = 'te^st'", "  x = 'te\\n\\\n^st'");
    }

    public void testSplitStrings1b() throws Exception {
        insertBreak("  x = '^test'", "  x = '\\\n^test'");
    }

    public void testSplitStrings2() throws Exception {
        insertBreak("  x = 'test^'", "  x = 'test\\n\\\n^'");
    }

    public void testSplitStrings3() throws Exception {
        insertBreak("  x = \"te^st\"", "  x = \"te\\n\\\n^st\"");
    }

    public void testNoSplitTemplates1() throws Exception {
        insertBreak("  x = `te^st`", "  x = `te\n^st`");
    }

    public void testInsertNewLine1() throws Exception {
        insertBreak("x^", "x\n^");
    }

    public void testInsertNewLine2() throws Exception {
        insertBreak("function foo() {^", "function foo() {\n    ^\n}");
    }

    public void testInsertNewLine3() throws Exception {
        insertBreak("function foo() {^\n}", "function foo() {\n    ^\n}");
    }

    public void testInsertNewLine4() throws Exception {
        insertBreak("function foo() {\n    if(bar())^\n\n}", "function foo() {\n    if(bar())\n        ^\n\n}");
    }

    public void testInsertIf1() throws Exception {
        insertBreak("    if (true) {^", "    if (true) {\n        ^\n    }");
    }

    public void testContComment() throws Exception {
        if (JsTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("// ^", "// \n// ^");
        } else {
            insertBreak("// ^", "// \n^");
        }
    }

    public void testContComment2() throws Exception {
        // No auto-# on new lines
        if (JsTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("   //  ^", "   //  \n   //  ^");
        } else {
            insertBreak("   //  ^", "   //  \n   ^");
        }
    }

    public void testContComment3() throws Exception {
        // No auto-# on new lines
        if (JsTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("   //\t^", "   //\t\n   //\t^");
        } else {
            insertBreak("   //\t^", "   //\t\n   ^");
        }
    }

    public void testContComment4() throws Exception {
        insertBreak("// foo\n^", "// foo\n\n^");
    }

    public void testContComment5() throws Exception {
        // No auto-# on new lines
        if (JsTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("      // ^", "      // \n      // ^");
        } else {
            insertBreak("      // ^", "      // \n      ^");
        }
    }

    public void testContComment6() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }

    public void testContComment7() throws Exception {
        insertBreak("   // foo^\n   // bar", "   // foo\n   // ^\n   // bar");
    }

    public void testContComment8() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }

    public void testContComment9() throws Exception {
        insertBreak("^// foobar", "\n^// foobar");
    }

    public void testContComment10() throws Exception {
        insertBreak("//foo\n^// foobar", "//foo\n// ^\n// foobar");
    }

    public void testContComment11() throws Exception {
        // This behavior is debatable -- to be consistent with testContComment10 I
        // should arguably continue comments here as well
        insertBreak("code //foo\n^// foobar", "code //foo\n\n^// foobar");
    }

    public void testContComment12() throws Exception {
        insertBreak("  code\n^// foobar", "  code\n\n  ^// foobar");
    }

    public void testContComment14() throws Exception {
        insertBreak("function foo() {\n    code\n^// foobar\n}\n", "function foo() {\n    code\n\n    ^// foobar\n}\n");
    }

    public void testContComment15() throws Exception {
        insertBreak("\n\n^// foobar", "\n\n\n^// foobar");
    }

    public void testContComment16() throws Exception {
        insertBreak("\n  \n^// foobar", "\n  \n\n^// foobar");
    }

    public void testContComment17() throws Exception {
        insertBreak("function foo() {\n  // cmnt1\n^  // cmnt2\n}\n", "function foo() {\n  // cmnt1\n  // ^\n  // cmnt2\n}\n");
    }

    public void testContComment18() throws Exception {
        insertBreak("x = /*^\n*/", "x = /*\n * ^\n*/");
    }

    public void testContComment19() throws Exception {
        insertBreak("x = /**^\n*/", "x = /**\n * ^\n*/");
    }

    public void testContComment20() throws Exception {
        insertBreak("/**^", "/**\n * ^\n */");
    }

    public void testContComment21() throws Exception {
        insertBreak("/*^\nvar a = 5;", "/*\n * ^\n */\nvar a = 5;");
    }

    public void testContComment22() throws Exception {
        insertBreak("/*^\nvar a = 5;/**\n*/", "/*\n * ^\n */\nvar a = 5;/**\n*/");
    }

    public void testNoContComment() throws Exception {
        // No auto-// on new lines
        insertBreak("foo // ^", "foo // \n^");
    }

    public void testNoContcomment2() throws Exception {
        insertBreak("x = /*\n*/^", "x = /*\n*/\n^");
    }

    public void testContinuation1() throws Exception {
        insertBreak("for (i = 0; i < 10; i++) {\n"
                + "    a = 5 +^\n"
                + "}",
                "for (i = 0; i < 10; i++) {\n"
                + "    a = 5 +\n"
                + "            ^\n"
                + "}");
    }

    public void testContinuation2() throws Exception {
        insertBreak("alert(^);\n", "alert(\n        ^);\n");
    }

    public void testCommentUnbalancedBraces() throws Exception {
        insertBreak("var MyObj = {\n"
                + "    version: 10,\n"
                + "    factory: function () {\n"
                + "        return this;\n"
                + "    },\n"
                + "\n"
                + "    /*^\n"
                + "    create: function () {\n"
                + "        return new MyObj();\n"
                + "    }"
                + "}",
                "var MyObj = {\n"
                + "    version: 10,\n"
                + "    factory: function () {\n"
                + "        return this;\n"
                + "    },\n"
                + "\n"
                + "    /*\n"
                + "     * ^\n"
                + "     */\n"
                + "    create: function () {\n"
                + "        return new MyObj();\n"
                + "    }"
                + "}");
    }

    public void testBreakInsideObject() throws Exception {
        insertBreak("function Synergy() { \n"
                + "/**^\n"
                + "this.endpoints = {};\n"
                + "this.session = {};\n"
                + "this.labels = {};\n"
                + "}\n"
                + "/** */\n"
                + "window.SYNERGY = new Synergy();",
                "function Synergy() { \n"
                + "/**\n"
                + " * ^\n"
                + " */\n"
                + "this.endpoints = {};\n"
                + "this.session = {};\n"
                + "this.labels = {};\n"
                + "}\n"
                + "/** */\n"
                + "window.SYNERGY = new Synergy();");
    }

    public void testDisabledBrackets() throws Exception {
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class)
                .putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
         insertBreak("function test() {^", "function test() {\n    ^");
    }

    // FIXME are those actually indenter tests ?
    public void testIssue118656() throws Exception {
        insertBreak("if (true) ^thing()", "if (true) \n    ^thing()");
    }

    public void testIssue219683() throws Exception {
        insertBreak("$(table).find(\"tbody tr\").each(function(){^)",
                "$(table).find(\"tbody tr\").each(function(){\n    ^\n})");
    }

    public void testIssue221676() throws Exception {
        insertBreak("var obj = (new function() {\n"
                + "   this.myFunc = function () {\n"
                + "      if (this.condition) {\n"
                + "         if (this.condition2) {\n"
                + "            this.doSth();\n"
                + "         }^\n"
                + "   };\n"
                + "}())\n",
                "var obj = (new function() {\n"
                + "   this.myFunc = function () {\n"
                + "      if (this.condition) {\n"
                + "         if (this.condition2) {\n"
                + "            this.doSth();\n"
                + "         }\n"
                + "         ^\n"
                + "   };\n"
                + "}())\n");
    }

    public void testIssue222239() throws Exception {
        insertBreak("var empowered = {\n"
            + "    showFooter: function() {\n"
            + "        $(\"footer #footer-permanent-content\").removeClass('fixed');\n"
            + "        $(\"footer #footer-variable-content\").removeClass('padding-top');\n"
            + "    },\n"
            + "    showTestMenu: function() {^,\n"
            + "    showPhoneMenu: function() {\n"
            + "        $(\"footer #footer-permanent-content nav ul\").toggleClass('hidden-menu');\n"
            + "    },\n",
            "var empowered = {\n"
            + "    showFooter: function() {\n"
            + "        $(\"footer #footer-permanent-content\").removeClass('fixed');\n"
            + "        $(\"footer #footer-variable-content\").removeClass('padding-top');\n"
            + "    },\n"
            + "    showTestMenu: function() {\n"
            + "        ^\n"
            + "    },\n"
            + "    showPhoneMenu: function() {\n"
            + "        $(\"footer #footer-permanent-content nav ul\").toggleClass('hidden-menu');\n"
            + "    },\n");
    }

    public void testIssue222475() throws Exception {
        insertBreak("(function () { ^ window.$prom = x || window}{);",
                "(function () { \n    ^window.$prom = x || window}{);");
    }

    public void testIssue223285() throws Exception {
        insertBreak("/*^\naaa\n*/\nfunction test(foo) {}",
                "/*\n * ^\naaa\n*/\nfunction test(foo) {}");
    }

    public void testIssue231018() throws Exception {
        insertBreak("angular.module('quizesApp.services', ['ngResource'])\n"
            + "        .factory('QuizService', function($resource) {^",
            "angular.module('quizesApp.services', ['ngResource'])\n"
            + "        .factory('QuizService', function($resource) {\n"
            + "            ^\n"
            + "}");
    }

    public void testIssue225016_1() throws Exception {
        insertBreak("$(document).ready(function() {\n"
                + "    var a = {\n"
                + "        test1: function() {\n"
                + "            if (a == 1^)\n"
                + "        }\n"
                + "    };\n"
                + "});",
                "$(document).ready(function() {\n"
                + "    var a = {\n"
                + "        test1: function() {\n"
                + "            if (a == 1\n"
                + "                    ^)\n"
                + "        }\n"
                + "    };\n"
                + "});");
    }

    public void testIssue225016_2() throws Exception {
        insertBreak("$(document).ready(function() {\n"
                + "    var a = {\n"
                + "        test1: function() {\n"
                + "            if (a == 1\n"
                + "                    && b == 3) {^\n"
                + "        }\n"
                + "    };\n"
                + "});",
                "$(document).ready(function() {\n"
                + "    var a = {\n"
                + "        test1: function() {\n"
                + "            if (a == 1\n"
                + "                    && b == 3) {\n"
                + "                ^\n"
                + "            }\n"
                + "        }\n"
                + "    };\n"
                + "});");
    }

    public void testIssue225016_3() throws Exception {
        insertBreak("var a = {\n"
                + "    test1: function() {\n"
                + "\n"
                + "    },^\n"
                + "};",
                "var a = {\n"
                + "    test1: function() {\n"
                + "\n"
                + "    },\n"
                + "    ^\n"
                + "};");
    }

    public void testIssue225016_4() throws Exception {
        insertBreak("$(document).ready(function() {\n"
                + "    var a = {\n"
                + "        test1: function() {\n"
                + "            if (a == 1\n"
                + "                    && b == 3) {^ a = 1;\n"
                + "        }\n"
                + "    };\n"
                + "});",
                "$(document).ready(function() {\n"
                + "    var a = {\n"
                + "        test1: function() {\n"
                + "            if (a == 1\n"
                + "                    && b == 3) {\n"
                + "                ^a = 1;\n"
                + "            }\n"
                + "        }\n"
                + "    };\n"
                + "});");
    }

    public void testIssue234177() throws Exception {
        insertBreak("Game = function(name, priority)\n"
                + "{\n"
                + "	var self = this;\n"
                + "	this.name;\n"
                + "	this.priority;\n"
                + "};\n"
                + "\n"
                + "MyGameListViewModel = function(games)\n"
                + "{\n"
                + "	var self = this;\n"
                + "	self.gamesToPlay = ko.observableArray(games);\n"
                + "	self.gamesCount = ko.computed(function()\n"
                + "	{\n"
                + "		return self.gamesToPlay().length + \" games found.\";\n"
                + "	});\n"
                + "};\n"
                + "\n"
                + "ko.applyBindings(new MyGameListViewModel([{name: \"Skyrim\", priority: 1}, {name: \"Max Payne 3\", priority: 2}]));^",
                "Game = function(name, priority)\n"
                + "{\n"
                + "	var self = this;\n"
                + "	this.name;\n"
                + "	this.priority;\n"
                + "};\n"
                + "\n"
                + "MyGameListViewModel = function(games)\n"
                + "{\n"
                + "	var self = this;\n"
                + "	self.gamesToPlay = ko.observableArray(games);\n"
                + "	self.gamesCount = ko.computed(function()\n"
                + "	{\n"
                + "		return self.gamesToPlay().length + \" games found.\";\n"
                + "	});\n"
                + "};\n"
                + "\n"
                + "ko.applyBindings(new MyGameListViewModel([{name: \"Skyrim\", priority: 1}, {name: \"Max Payne 3\", priority: 2}]));\n"
                + "^");
    }
    
    public void testIssue236799_01() throws Exception {
        insertBreak("var a = {\n"
                + "template: '<button class=\"btn\"><i class=\"icon-location-arrow\"></i> Demo</button>^'\n"
                + "};",
                "var a = {\n"
                + "template: '<button class=\"btn\"><i class=\"icon-location-arrow\"></i> Demo</button>\\n\\\n^'\n"
                + "};"
        );
    }
    
    public void testIssue236799_02() throws Exception {
        insertBreak("var a = {\n"
                + "template: '<button class=\"btn\"><i class=\"icon-location-arrow\"></i> Demo</button>\\n\\^\n"
                + "'\n"
                + "};",
                "var a = {\n"
                + "template: '<button class=\"btn\"><i class=\"icon-location-arrow\"></i> Demo</button>\\n\\\n"
                + "\\n\\^\n'\n"
                + "};"
        );
    } 
   
    public void testIssue215318() throws Exception {
        insertBreak(
                "    /**\n"
                + "     *^",
                "    /**\n"
                + "     *\n"
                + "     * ^\n"
                + "     */");
    }

    public void testIssue215318_1() throws Exception {
        insertBreak(
                "    /** *^",
                "    /** *\n"
                + "     * ^\n"
                + "     */");
    } 
}
