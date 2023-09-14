/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.lib;

import org.netbeans.modules.css.lib.api.*;

/**
 *
 * @author marekfukala
 */
public class Css3ParserLessTest extends CssTestBase {

    public Css3ParserLessTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setLessSource();
    }

    @Override
    protected void tearDown() throws Exception {
        setPlainSource();
    }

    public void testAllANTLRRulesHaveNodeTypes() {
        for (String rule : Css3Parser.ruleNames) {
            if (!rule.startsWith("synpred") && !rule.toLowerCase().endsWith("predicate")) {
                assertNotNull(NodeType.valueOf(rule));
            }
        }
    }

    public void testDisabledLessSupport() {
        try {
            ExtCss3Parser.isLessSource_unit_tests = false;
            String source = "@color: #4D926F;\n"
                    + "\n"
                    + "#header {\n"
                    + "  color: @color;\n"
                    + "}\n"
                    + "h2 {\n"
                    + "  color: @color;\n"
                    + "}";

            CssParserResult result = TestUtil.parse(source);
            NodeUtil.dumpTree(result.getParseTree());

            //there must be some css parsing errors as the less support is disabled
            assertTrue(result.getDiagnostics().size() > 0);
        } finally {
            ExtCss3Parser.isLessSource_unit_tests = true;
        }
    }

    public void testVariable() {
        String source = "@color: #4D926F;\n"
                + "\n"
                + "#header {\n"
                + "  color: @color;\n"
                + "}\n"
                + "h2 {\n"
                + "  color: @color;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testVariable2() {
        String source = "#header {\n"
                + "  border: 2px @color solid;\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testVariableAsPropertyName() {
        String source = ".class {\n"
                + "    @var: 2;\n"
                + "    three: @var;\n"
                + "    @var: 3;\n"
                + "  }";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunction() {
        String source
                = "#header {\n"
                + "  color: (@base-color * 3);\n"
                + "  border-left: @the-border;\n"
                + "  border-right: (@the-border * 2);\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunction2() {
        String source
                = "#footer {\n"
                + "  border-color: desaturate(@red, 10%);\n"
                + "  color: (@base-color + #003300);\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinDeclaration() {
        String source
                = ".rounded-corners (@radius: 5px) {\n"
                + "  -webkit-border-radius: @radius;\n"
                + "  -moz-border-radius: @radius;\n"
                + "  -ms-border-radius: @radius;\n"
                + "  -o-border-radius: @radius;\n"
                + "  border-radius: @radius;\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinDeclaration2() {
        String source
                = ".box-shadow (@x: 0, @y: 0, @blur: 1px, @color: #000) {\n"
                + "  box-shadow: @arguments;\n"
                + "  -moz-box-shadow: @arguments;\n"
                + "  -webkit-box-shadow: @arguments;\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinDeclarationAdvancedArguments() {
        String source
                = ".mixin1 (...) {}"
                + ".mixin2 () {}"
                + ".mixin3 (@a: 1) {}"
                + ".mixin4 (@a: 1, ...) {}"
                + ".mixin5 (@a, ...) {}";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testGuardedMixins() {
        String source
                = ".mixin (@a) when (@a > 10), (@a = -10) {\n"
                + "  background-color: black;\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testGuardedMixins2() {
        String source
                = ".truth (@a) when (@a) { }\n"
                + ".truth (@a) when (@a = true) { }\n"
                + ".mixin (@a) when (@media = mobile) { } \n";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testGuardedMixinIsFunction() {
        String source
                = ".mixin (@a, @b: 0) when (isnumber(@b)) { }\n";
        ;

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testGuardedMixinNotOperator() {
        String source
                = ".mixin (@b) when not (@b > 0) { }\n";
        ;

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinNesting() {
        String source
                = ".class {\n"
                + "  .mixin(@switch, #888);\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinNesting2() {
        String source
                = ".class {\n"
                + "  .mixin(@switch, #888);\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinNesting3() {
        String source
                = "#menu a {\n"
                + "  color: #111;\n"
                + "  .bordered;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunctions() {
        String source = ".class {\n"
                + "  width: percentage(0.5);\n"
                + "  color: saturate(@base, 5%);\n"
                + "  background-color: spin(lighten(@base, 25%), 8);\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testRulesNesting() {
        String source = "#header {\n"
                + "  color: black;\n"
                + "  .navigation {\n"
                + "    font-size: 12px;\n"
                + "  }\n"
                + "  font-size: 10px;\n"
                + "  .navigation (@a) {\n"
                + "    font-size: 12px;\n"
                + "  }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testAmpCombinatorInNestedRules() {
        String source = "#header        { color: black;\n"
                + "  .navigation  { font-size: 12px; }\n"
                + "  .logo        { width: 300px;\n"
                + "    &:hover    { text-decoration: none; }\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testAmpCombinatorInNestedRules2() {
        String source = ".shape{\n"
                + "    &:hover{ \n"
                + "        background:@lightRed;   \n"
                + "    }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testNestedRules() {
        String source = "#header{\n"
                + "    /* #header styles */\n"
                + "    h1{\n"
                + "        /* #header h1 styles */\n"
                + "    }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testOperationsInVariableDeclaration() {
        String source = "@darkBlue: @lightBlue - #555;";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testLessExpressionNotInParens() {
        String source = "div {"
                + "width: @pageWidth * .75;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallWithoutParams() {
        String source = "#shape1{ .Round; }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallOldWeirSyntax() {
        String source = "#skyscraper {  \n"
                + "    h2 {  \n"
                + "        .header(@color3; #A1915F);  \n"
                + "    }  \n"
                + "}  ";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testPropertyValueWithParenthesis() {
        String source = "div {\n"
                + "width: (@u * @unit) - ((@margin * 2) + @gpadding + @gborder);\n "
                + "}";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testPropertyValue() {
        String source = "div {\n"
                + "border-top: 1px solid @color1 - #222; "
                + "}";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //like normal css import, but the ref. file doesn't need to have an extension,
    //there are also some rules regarding the naming convention, but these
    //are covered by semantic analysis, not parsing
    public void testImport() {
        String source
                = "@import \"rounded\";\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testLineComment() {
        String source
                = ".funky {\n"
                + " //line comment\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallInStylesheet() {
        String source
                = ".firefox-message(\".header\");\n";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //https://netbeans.org/bugzilla/show_bug.cgi?id=231698
    public void testMixinDeclarationPredicate() {
        String source
                = ".mxc(\"param\");\n"
                + ".next {\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

        //verify that .mxc(\"param\") was parsed as mixin call
        Node mixinCall = NodeUtil.query(result.getParseTree(), "styleSheet/body/bodyItem/cp_mixin_call");
        assertNotNull(mixinCall);

    }

    public void testNLInSelectors() {
        assertParses(".a,\n"
                + ".b {\n"
                + "    width: 1050px;\n"
                + "}");
    }

    public void testMixinCallFollowedByRule() {
        CssParserResult result = assertParses("foo{\n"
                + "    @a: 12px;\n"
                + "    @b: normal;\n"
                + "    .test(@a @b);\n"
                + "}\n"
                + "\n"
                + "div {\n"
                + "    \n"
                + "}");

        assertNotNull(NodeUtil.query(result.getParseTree(), "styleSheet/body/bodyItem/rule/declarations/declaration|2/cp_mixin_call"));

        assertParses("foo{\n"
                + "    @a: 12px;\n"
                + "    @b: normal;\n"
                + "    .test(@a @b);\n"
                + "    div {\n"
                + "    }\n"
                + "}\n");
    }

    public void testSASSKeywordAsLessVariable() {
        assertParses(".transform (@function) {\n"
                + "	-webkit-transform: @function;\n"
                + "}\n"
                + "\n"
                + "div{\n"
                + "  .transform(rotate(90));\n"
                + "}");

        assertParses(".mx(@each) {}");
    }

    public void testExpressionWithLessJavascript() {
        assertParses("@var: `\"hello\".toUpperCase() + '!'`;");
    }

    public void testEscapedContent() {
        assertParses(".class {\n"
                + "  filter: ~\"ms:alwaysHasItsOwnSyntax.For.Stuff()\";\n"
                + "}");

    }

    public void testSelectorInterpolation() {
        assertParses(".@{name} {\n"
                + "    color: black;\n"
                + "}");
        assertParses("#@{name} {\n"
                + "    color: black;\n"
                + "}");
        assertParses("@{name} {\n"
                + "    color: black;\n"
                + "}");
    }

    public void testVariableAsMediaQuery() {
        assertParses("@singleQuery: \"(max-width: 500px)\";\n"
                + "@media screen, @singleQuery {\n"
                + "  set {\n"
                + "    padding: 3 3 3 3;\n"
                + "  }\n"
                + "}");
    }

    //https://netbeans.org/bugzilla/show_bug.cgi?id=227510#c4 / case 1
    public void testFunctionInMixinCall() {
        assertParses("#anid {\n"
                + " .background-image(linear-gradient(top, #fffffd 0%, #e8e6e6, 100%));\n"
                + " .border-radius(5px 5px 0 0);\n"
                + " border: 0;"
                + "}");
    }

    //https://netbeans.org/bugzilla/show_bug.cgi?id=227510#c4 / case 1
    public void testMixinCallWithWSSeparatedValues() {
        assertParses("#anid {\n"
                + " .box-shadow(0 0 5px #333);\n"
                + " .border-radius(7px 7px 4px 4px);\n"
                + " padding: 0;\n"
                + "}");
    }

    //https://netbeans.org/bugzilla/show_bug.cgi?id=227510#c4 / case 2
    public void testCommaInSelectorsGroup() {
        assertParses(".dbx-clone, .dbx-clone .dbx-handle-cursor {\n"
                + "    cursor: move !important; \n"
                + "}");
    }

    public void testImportantSymbolInMixinCall() {
        assertParses(".important {\n"
                + "  .mixin(2) !important; \n"
                + "} ");
    }

    public void testGuardedMixinsWithFunctionInLessCondition() {
        assertParses(".mixin (@a) when (lightness(@a) >= 50%) {\n"
                + "  background-color: black;\n"
                + "}");
    }

    public void testNestedSelectorConcatenation() {
        assertParses(".child, .sibling {\n"
                + "    .parent & {\n"
                + "        color: black;\n"
                + "    }\n"
                + "    & + & {\n"
                + "        color: red;\n"
                + "    }\n"
                + "}");
    }

    public void testMixinDeclarationPredicate2() {
        assertParses("@attr:1;\n"
                + "@name:1;\n"
                + "\n"
                + "pre {    \n"
                + "    .test2(red);\n"
                + "}\n"
                + "\n"
                + ".test2(@j){\n"
                + "    color: @j;\n"
                + "    font-size: @attr;\n"
                + "}");
    }

    public void testRuleParsedAsMixin() {
        assertParses(".bar,\n"
                + "    .baz {\n"
                + "        width: 100%;\n"
                + "    }\n");

        assertParses("#foo {\n"
                + "    .bar, .baz {\n"
                + "        width: 100%;\n"
                + "    }\n"
                + "}");

        assertParses("#foo {\n"
                + "    .bar,\n"
                + "    .baz {\n"
                + "        width: 100%;\n"
                + "    }\n"
                + "}");
    }

    //https://netbeans.org/bugzilla/show_bug.cgi?id=234712
    //so far closed as INVALID -- seems to be an erroneous sample
//    public void testDotInMixinName() {
//        assertParses("a.bar {\n"
//                + "    color: red;\n"
//                + "}\n"
//                + "\n"
//                + ".foo {\n"
//                + "    a.bar;\n"
//                + "}");
//    }

    public void testEscapedString() {
        assertParses("@str: \"hello\";");

        assertParses("@var: ~\"xyz\";");

        assertParses("@var: ~`\"@{str}\".toUpperCase() + '!'`;");
    }

//    //https://netbeans.org/bugzilla/show_bug.cgi?id=227510#c10 / case#18
//    public void testMixinCallAsSelector() {
//        assertParses(".x { #gradient > .vertical(#f5f5f5, #eeeeee); }");
//
//        assertParses(".subnav-fixed {\n"
//                + "#gradient > .vertical(#f5f5f5, #eeeeee);\n"
//                + "}");
//    }

    public void testIssue236388() {
        assertParses("@media only screen and (min-width: 768px) {\n" +
                "    @import \"_grid.less\";\n" +
                "    @import \"_768up.less\";\n" +
                "}");
    }

    public void testIssue237976_01() {
        assertParses(".img-responsive(@display: block; @disp: block) {\n" +
                "  height: auto;\n" +
                "}");
    }

    public void testIssue237976_02() {
        assertParses(".img-responsive2(@display: block;) {\n" +
                "  height: auto;\n" +
                "}");
    }

    public void testIssue237975_01() {
        assertParses("@import (less) \"theme\";");
    }

    public void testIssue237975_02() {
        assertParses("@import (css) \"theme\";");
    }

    public void testIssue240757() {
        assertParses(".mixin (@a) when (isnumber(@a)) and (@a > 0) { }");
    }

    public void testIssue240701() {
        assertParses(".linear-gradient-multi(...) {}");
    }
    
    public void testIssue248194() {
        assertParses(
        "@import (reference) \"foo.less\";\n" +
        "@import (inline) \"not-less-compatible.css\";\n" +
        "@import (once) \"foo.less\";\n" +
        "@import (multiple) \"foo.less\";\n" +
        "@import (less) \"foo.css\";\n" +
        "@import (css) \"foo.less\";"
        );
    }
    
    public void testErrorKeywordInImport() {
        String source = " @import(wrong) \"test\";";
        CssParserResult result = TestUtil.parse(source);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        Node error = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "importItem/less_import_types/error");
        assertNotNull(error);

        assertEquals(9, error.from());
        assertEquals(14, error.to());
        assertEquals("wrong", error.image().toString());
    }
    
    public void testAdditionalCommaInTheEndOfSelector() {
        String source = ".ui-convex,\n"
                + ".ui-convex-hover,\n"
                + "{\n"
                + "    width: 10em;\n"
                + "    height: 2em;\n"
                + "    \n"
                + "    margin: 1em auto;\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testParentInheritingWithNumbers() {
        String source = ".author {\n"
                + "  &-name {  }\n"
                + "  &-picture { }\n"
                + "}\n"
                + "\n"
                + ".author {\n"
                + "  &-1 { }\n"
                + "  &-2 {  }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testDetachedRuleset() {
        String source = "// declare detached ruleset\n"
                + "@detached-ruleset: { background: red; };\n"
                + "\n"
                + "// use detached ruleset\n"
                + ".top {\n"
                + "    @detached-ruleset(); \n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testDetachedRulesetestWithMixin() {
        String source = ".mymixin(@ruleset) {\n"
                + "    @ruleset(); // Hint: Unexpected LPAREN found\n"
                + "}\n"
                + ".xx {\n"
                + "    .mymixin({.red()}); // Hint: Unexpected IDENT found\n"
                + "} ";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testAndWithWhen() {
        String source = ".mixin-filled(@delta:0%) {\n"
                + "	& when (@delta > 0%) {\n"
                + "		background:lighten(@mixin-boxcolor, @delta);\n"
                + "	}\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testMultipleAnds() {
        String source = ".heading {\n"
                + "    &&--type-small {\n"
                + "        font-size: 15px;\n"
                + "    }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testTwoClassesWithoutSpace() {
        String source = "body {\n"
                + "    .foo.bar {\n"
                + "        // any css statement\n"
                + "    }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testMixinCallsWithHash() {
        String source = ".a, #b {\n"
                + "  color: red;\n"
                + "}\n"
                + ".mixin-class {\n"
                + "  .a();\n"
                + "}\n"
                + ".mixin-id {\n"
                + "  #b();\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testMixinDefWithHash() {
        String source = "#mymixin(@ruleset) { \n"
                + "    @ruleset(); \n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testMixinCallInMedia() {
        String source = ".desktop-and-old-ie(@rules) {\n"
                + "  @media sceren and (min-width: 1200) { @rules(); }\n"
                + "  html.lt-ie9 &                       { @rules(); }\n"
                + "}";
        assertParses(source);
    }
    
    public void testExtendKeyword() {
        
        assertParses(".sidenav:extend(.nav, #foo, .bar) {}");
        assertParses(".big-division,\n"
                + ".big-bag:extend(.bag),\n"
                + ".big-bucket:extend(.bucket) {\n"
                + "  // body\n"
                + "}");
        assertParses(".b {\n"
                + "	&:extend(.a);\n"
                + "}");
        assertParses(".aa{\n"
                + "font-size: 12px;\n"
                + "&:extend(.clearfix);\n"
                + "}");
        
        String source = ".sidenav:extend(.nav) {}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testInterpolationInPseudo() {
        assertParses(".@{var}bar{}");
        assertParses("li:nth-child(@{somevar}+1) {}");
        assertParses("li:nth-child(@{somevar}) {}");
        assertParses("li:nth-child(~\"@{somevar}\") {}");
        assertParses(".foo {\n"
                + "\n"
                + "	&:not(.@{var}bar):active {\n"
                + "		color: red;\n"
                + "	}\n"
                + "\n"
                + "}");
    }
    
    public void testPatternMatchingInMixin() {
        assertParses(".mixin(@s; @color) { }\n"
                + "\n"
                + ".class {\n"
                + "  .mixin(@switch; #888);\n"
                + "}");
        assertParses(".mixin(dark; @color) {\n"
                + "  color: darken(@color, 10%);\n"
                + "}");
        
        assertParses(".mixin(@a; @b) {\n"
                + "  color: fade(@a; @b);\n"
                + "}");
    }
    
    public void testMathExpWithUnits() {
        assertParses("@fa-li-width: (30em / 14);");
    }
    
    public void testInterpolationWithLength() {
        assertParses(".@{fa-css-prefix}-500px:before {padding: 10;}");
    }
    
    public void testIdentBeforeLessAnd() {
        assertParses(".badge {\n"
                + "  display: inline-block;\n"
                + "  &:after {\n"
                + "    display: none;\n"
                + "  }\n"
                + "\n"
                + "  a& {\n"
                + "    &:hover,\n"
                + "    &:focus {\n"
                + "      text-decoration: none;\n"
                + "    }\n"
                + "  }\n"
                + "}\n"
                );
    }
    
    public void testIdentInterpolation() {
        assertParses(".prefix(@property,@value) {\n"
                + " -moz-border-radius: @radius;\n"
                + "	-moz-@{property}: @value;\n"
                + "	-webkit-@{property}: @value;\n"
                + "	-ms-@{property}: @value;\n"
                + "  @{property}: @value;\n"
                + "}");
    }
    
    public void testDefineVariableByVariableName() {
        assertParses("@fnord:  \"I am fnord.\";\n"
                + "@var:    \"fnord\";\n"
                + ".test {\n"
                + "    content: @@var;\n"
                + "}");
    }
    
    public void testMixinArgsWithSemicolonAtTheEnd() {
        assertParses(".classA {\n"
                + "     .transitions(300ms border linear, transform 300ms linear;);\n"
                + "}");
    }
    
    public void testUnderscoreInNameWithInterpolation() {
        assertParses(".classname {\n"
                + "   &__@{classname_in_variable} { \n"
                + "       height:0;\n"
                + "   }\n"
                + "}"
        );
    }
    
    public void testMixinCallWithGreater() {
        assertParses("#gradient {\n"
                + "    .vertical(@start-color: #555; @end-color: #333; @start-percent: 0%; @end-percent: 100%) {\n"
                + "        background-image: -webkit-linear-gradient(top, @start-color @start-percent, @end-color @end-percent);  // Safari 5.1-6, Chrome 10+\n"
                + "   }\n"
                + "}\n"
                + "  \n"
                + ".bg-gd{\n"
                + "  #gradient > .vertical(rgba(40,50,60,0), rgba(40,50,60,0.075), 0, 100%);\n"
                + "}");
    }

    public void testScssUseForward() {
        assertParses("@use: blue;\n");
        assertParses("@forward: darken(@demo, 10%);");
        assertParses("a { color: @use }");
        assertParses("a { color: @forward }");
        assertParses(".@{my-selector} { font-weight: bold }", true);
    }

    public void testParseLayer() {
        assertParses("@layer layer1;");
        assertParses("@layer layer1, layer2;");
        assertParses("@layer layer1 {}");
        assertParses("@layer layer1 {h1 {font-weight: bold}}");
        assertParses("@layer layer1 {h1 {font-weight: bold}} @layer layer2 {}");
        assertParses("@layer layer1.sublayer1 {h1 {font-weight: bold}}");
        assertParses("@layer layer1 { @layer sublayer1 {}}");
        assertParses("@layer layer1 { @layer sublayer1 {h1 {font-weight: bold}}}");
        assertParses("@layer layer1 { @layer sublayer1, sublayer2; @layer sublayer1 {} @layer sublayer2{}}");
        assertParses("@layer {}");
        assertParses("@layer {h1 {font-weight: bold}}");
        assertParses("@layer {h1 {font-weight: bold}} @layer layer2 {}");
        assertParses("@import \"test.css\" layer;");
        assertParses("@import \"test.css\" layer(test);");
        assertParses("@import \"test.css\" layer(test.test2);");
        assertParses("@layer default;\n"
                + "@import url(theme.css) layer(theme);\n"
                + "@layer components;\n"
                + "@layer default {}");
    }

    public void testIssueGH6447() {
        assertParses("@import (optional) \"user.less\";");
    }
}
