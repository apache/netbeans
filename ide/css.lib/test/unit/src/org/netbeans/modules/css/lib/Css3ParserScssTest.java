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

import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.lib.api.*;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class Css3ParserScssTest extends CssTestBase {

    public Css3ParserScssTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setScssSource();
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

    public void testDisabledScssSupport() {
        try {
            ExtCss3Parser.isScssSource_unit_tests = false;
            String source = "$color: #4D926F;\n"
                    + "\n"
                    + "#header {\n"
                    + "  color: $color;\n"
                    + "}\n"
                    + "h2 {\n"
                    + "  color: $color;\n"
                    + "}";

            CssParserResult result = TestUtil.parse(source);

            //there must be some css parsing errors as the less support is disabled
            assertTrue(result.getDiagnostics().size() > 0);
        } finally {
            ExtCss3Parser.isScssSource_unit_tests = true;
        }
    }

    public void testVariable() {
        assertParses("$color: #4D926F;\n"
                + "\n"
                + "#header {\n"
                + "  color: $color;\n"
                + "}\n"
                + "h2 {\n"
                + "  color: $color;\n"
                + "}");
    }

    public void testNamespacedVariable() {
        assertParses("@use \"vars\";\n"
                + "#test {     \n"
                + "    color: vars.$someColor;     \n"
                + "    font-size: 12px; \n"
                + "}");
    }

    public void testVariable2() {
        assertParses("#header {\n"
                + "  border: 2px $color solid;\n"
                + "}\n");
    }

    public void testNamespacedVariable2() {
        assertParses("@use \"vars\";\n"
                + "#header {\n"
                + "  border: 2px vars.$color solid;\n"
                + "}\n");
    }

    public void testVariableAsPropertyName() {
        assertParses(".class {\n"
                + "    $var: 2;\n"
                + "    three: $var;\n"
                + "    $var: 3;\n"
                + "  }");
    }

    public void testVariableAsPropertyName2() {
        assertParses(
                "@use \"vars\";\n"
                + ".class {\n"
                + "    vars.$var: 2;\n"
                + "    three: vars.$var;\n"
                + "    vars.$var: 3;\n"
                + "  }");
    }

    public void testFunction() {
        String source
                = "#header {\n"
                + "  color: ($base-color * 3);\n"
                + "  border-left: $the-border;\n"
                + "  border-right: ($the-border * 2);\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunction2() {
        String source
                = "#footer {\n"
                + "  border-color: desaturate($red, 10%);\n"
                + "  color: ($base-color + #003300);\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinDeclaration() {
        String source
                = "@mixin rounded-corners ($radius: 5px) {\n"
                + "  -webkit-border-radius: $radius;\n"
                + "  -moz-border-radius: $radius;\n"
                + "  -ms-border-radius: $radius;\n"
                + "  -o-border-radius: $radius;\n"
                + "  border-radius: $radius;\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinDeclaration2() {
        String source
                = "@mixin box-shadow ($x: 0, $y: 0, $blur: 1px, $color: #000) {\n"
                + "  box-shadow: $arguments;\n"
                + "  -moz-box-shadow: $arguments;\n"
                + "  -webkit-box-shadow: $arguments;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinDeclarationWithoutParams() {
        String source
                = "@mixin box-shadow {\n"
                + "  box-shadow: $arguments;\n"
                + "  -moz-box-shadow: $arguments;\n"
                + "  -webkit-box-shadow: $arguments;\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }
//
//    public void testMixinDeclarationAdvancedArguments() {
//        String source =
//                ".mixin1 (...) {}"
//                + ".mixin2 () {}"
//                + ".mixin3 (@a: 1) {}"
//                + ".mixin4 (@a: 1, ...) {}"
//                + ".mixin5 (@a, ...) {}";
//        ;
//
//    public void testGuardedMixins() {
//        String source =
//                ".mixin (@a) when (@a > 10), (@a = -10) {\n"
//                + "  background-color: black;\n"
//                + "}";
//        ;
//
//        CssParserResult result = TestUtil.parse(source);
//
////        NodeUtil.dumpTree(result.getParseTree());
//        assertResultOK(result);
//    }
//
//    public void testGuardedMixins2() {
//        String source =
//                ".truth (@a) when (@a) { }\n"
//                + ".truth (@a) when (@a = true) { }\n"
//                + ".mixin (@a) when (@media = mobile) { } \n";
//        ;
//
//        CssParserResult result = TestUtil.parse(source);
//
////        NodeUtil.dumpTree(result.getParseTree());
//        assertResultOK(result);
//    }
//
//    public void testGuardedMixinIsFunction() {
//        String source =
//                ".mixin (@a, @b: 0) when (isnumber(@b)) { }\n";
//        ;
//
//        CssParserResult result = TestUtil.parse(source);
//
//        NodeUtil.dumpTree(result.getParseTree());
//        assertResultOK(result);
//    }
//
//    public void testGuardedMixinNotOperator() {
//        String source =
//                ".mixin (@b) when not (@b > 0) { }\n";
//        ;
//
//        CssParserResult result = TestUtil.parse(source);
//
////        NodeUtil.dumpTree(result.getParseTree());
//        assertResultOK(result);
//    }
//

    public void testMixinCall() {
        String source
                = ".class {\n"
                + "  @include mixin($switch, #888);\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMixinCall2() {
        String source
                = "#menu a {\n"
                + "  color: #111;\n"
                + "  @include bordered;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunctions() {
        String source = ".class {\n"
                + "  width: percentage(0.5);\n"
                + "  color: saturate($base, 5%);\n"
                + "  background-color: spin(lighten($base, 25%), 8);\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testFunctions2() {
        String source = "#navbar {\n"
                + "  $navbar-width: 800px;\n"
                + "  $items: 5;\n"
                + "  $navbar-color: #ce4dd6;\n"
                + "\n"
                + "  width: $navbar-width;\n"
                + "  border-bottom: 2px solid $navbar-color;\n"
                + "\n"
                + "  li {\n"
                + "    float: left;\n"
                + "    width: $navbar-width/$items - 10px;\n"
                + "\n"
                + "    background-color:\n"
                + "      lighten($navbar-color, 20%);\n"
                + "    &:hover {\n"
                + "      background-color:\n"
                + "        lighten($navbar-color, 10%);\n"
                + "    }\n"
                + "  }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testRulesNesting() {
        String source = "#header {\n"
                + "  color: black;\n"
                + "  @mixin navigation {\n"
                + "    font-size: 12px;\n"
                + "  }\n"
                + "  font-size: 10px;\n"
                + "  @mixin navigation($a) {\n"
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
                + "        background:$lightRed;   \n"
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
        String source = "$darkBlue: $lightBlue - #555;";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testLessExpressionNotInParens() {
        String source = "div {"
                + "width: $pageWidth * .75;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallWithoutParams() {
        String source = "#shape1{ @include mymixin; }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testPropertyValueWithParenthesis() {
        String source = "div {\n"
                + "width: ($u * $unit) - (($margin * 2) + $gpadding + $gborder);\n "
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testPropertyValue() {
        String source = "div {\n"
                + "border-top: 1px solid $color1 - #222; "
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationInClassSelector() {
        String source
                = ".rounded-#{$vert}-#{$horz} {\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationInIdSelector() {
        String source
                = ".navb#{$navbar}ar {\n"
                + "  $navbar-width: 800px;"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationInPropertyName() {
        String source
                = ".rounded {\n"
                + "  border-#{$vert}-#{$horz}-radius: $radius;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionInSelectorWithWS() {
        String source
                = ".body.firefox #{$selector}:before {\n"
                + "    content: \"Hi, Firefox users!\";\n"
                + "  }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionInFunctionInTheExpression() {
        String source
                = ".body.firefox #{ie-hex-str($green)}:before {\n"
                + "    content: \"Hi, Firefox users!\";\n"
                + "  }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionInPropertyValue() {
        String source
                = "p {\n"
                + "  $font-size: 12px;\n"
                + "  $line-height: 30px;\n"
                + "  font: #{$font-size}/#{$line-height};\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //fails as the
    //
    //.body.firefox #{$selector}:before
    //
    //selector is parsed as property declaration - due to the colon presence - FIXME!!!
    public void testInterpolationExpressionComplex() {
        String source
                = "@mixin firefox-message($selector) {\n"
                + "  .body.firefox #{$selector}:before {\n"
                + "    content: \"Hi, Firefox users!\";\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinsWithArgumentsComplex() {
        String source
                = "/* style.scss */\n"
                + "\n"
                + "@mixin rounded($vert, $horz, $radius: 10px) {\n"
                + "  border-#{$vert}-#{$horz}-radius: $radius;\n"
                + "  -moz-border-radius-#{$vert}#{$horz}: $radius;\n"
                + "  -webkit-border-#{$vert}-#{$horz}-radius: $radius;\n"
                + "}\n"
                + "\n"
                + "#navbar li { @include rounded(top, left); }\n"
                + "#footer { @include rounded(top, left, 5px); }\n"
                + "#sidebar { @include rounded(top, left, 8px); }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
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

    public void testNestedProperties() {
        String source
                = ".funky {\n"
                + "  font: {\n"
                + "    family: fantasy;\n"
                + "    size: 30em;\n"
                + "    weight: bold;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testNestedPropertiesWithValue() {
        String source
                = ".funky {\n"
                + "  font: 2px/3px {\n"
                + "    family: fantasy;\n"
                + "    size: 30em;\n"
                + "    weight: bold;\n"
                + "  }\n"
                + "}";

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
                = "@include firefox-message(\".header\");\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testDefaultVariable() {
        String source
                = "$content: \"Second content?\" !default;\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMultipleImport() {
        String source
                = "@import \"rounded-corners\", \"text-shadow\";\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionInImport() {
        String source
                = "@import url(\"http://fonts.googleapis.com/css?family=#{$family}\");\n";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //the grammar defines the imports needs to be at the very beginning of the file,
    //though this is not true in case of the preprocessor code
    public void testSASSCodeMayPrecedeImport() {
        String source = "$var: my;\n"
                + "@import url(\"#{$var}\");\n";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testNestedMediaQueries() {
        String source = ".sidebar {\n"
                + "  width: 300px;\n"
                + "  @media screen and (orientation: landscape) {\n"
                + "  .class {\n"
                + "    width: 500px;\n"
                + "  }\n"
                + "}\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testNestedMediaQueryInMediaQuery() {
        String source = "@media screen {\n"
                + "  .sidebar {\n"
                + "    @media (orientation: landscape) {\n"
                + "   //   width: 500px;\n"
                + "    }\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //the media query can the property declarations directly.
    public void testPropertiesDirectlyInMediaQuery() {
        String source = "@media screen and (orientation: landscape) {\n"
                + "    width: 500px;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionInMediaQuery() {
        String source = "@media #{$media} {\n"
                + "  .sidebar {\n"
                + "    width: 500px;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testInterpolationExpressionWithParenMediaQuery() {
        String source = "$media: screen;\n"
                + "$feature: -webkit-min-device-pixel-ratio;\n"
                + "$value: 1.5;\n"
                + "\n"
                + "@media #{$media} and (#{$feature}: #{$value}) {\n"
                + "  .sidebar {\n"
                + "    width: 500px;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testExtend() {
        String source = ".seriousError {\n"
                + "  @extend .error;\n"
                + "  border-width: 3px;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testExtendComplex() {
        String source = ".hoverlink {\n"
                + "  @extend a:hover;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testExtendOnlySelectors() {
        String source = "#context a%extreme {\n"
                + "  color: blue;\n"
                + "  font-weight: bold;\n"
                + "  font-size: 2em;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testExtendOnlySelectorCall() {
        String source = ".notice {\n"
                + "  @extend %extreme;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testExtendMultipleSelectors() {
        assertParses("dummy {@extend .message, .info;};");
    }

    public void testDebug() {
        String source = "@debug 10em + 12em;\n"
                + ".class {\n"
                + "@debug \"hello\";\n"
                + "}\n"
                + "@mixin mymixin {\n"
                + "@debug 20;"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testWarn() {
        String source = "@warn 10em + 12em;\n"
                + ".class {\n"
                + "@warn \"hello\";\n"
                + "}\n"
                + "@mixin mymixin {\n"
                + "@warn 20;"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testIf() {
        String source = "p {\n"
                + "  @if 1 + 1 == 2 { border: 1px solid;  }\n"
                + "  @if 5 < 3      { border: 2px dotted; }\n"
                + "  @if null       { border: 3px double; }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testFor() {
        String source = "@for $i from 1 through 3 {\n"
                + "  .item-#{$i} { width: 2em * $i; }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testEach() {
        String source = "@each $animal in puma, sea-slug, egret, salamander {\n"
                + "  .#{$animal}-icon {\n"
                + "    background-image: url('/images/#{$animal}.png');\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testWhile() {
        String source = "$i: 6;\n"
                + "@while $i > 0 {\n"
                + "  .item-#{$i} { width: 2em * $i; }\n"
                + "  $i: $i - 2;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testDefineOwnFunction() {
        String source = "@function grid-width($n) {\n"
                + "  @return $n * $grid-width + ($n - 1) * $gutter-width;\n"
                + "}\n"
                + "\n"
                + "#sidebar { width: grid-width(5); }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallWithFunctionWithNoArgs() {
        String source = ".ease-out-expo-animation {\n"
                + "  @include transition-timing-function(ease-out-expo()); \n"
                + "  color: best-color();\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testVariableDeclarationWithCommaSeparatedValues() {
        String source = "$blueprint-font-family: Helvetica Neue, Arial, Helvetica, sans-serif;";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testAmpProblem() {
        String source
                = ".clazz {\n"
                + "    &.position#{$i} {\n"
                + "    left: ($i * -910px); \n"
                + "}\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    //commented out as the parser testing file contains a lot of unknown properties from the css point of view.
    //TODO: enable testing mode where the semantic errors are not added to the parsing diagnostics.
    //
//    public void testMergedScssTests() throws ParseException, BadLocationException, IOException {
//        CssParserResult result = TestUtil.parse(getTestFile("testfiles/scss/scss-tests-merged.scss"));
////        TestUtil.dumpResult(result);
//        assertResult(result, 0);
//    }
    public void testLocalVariableDeclaration() {
        String source
                = "p {\n"
                + "  $width: 1000px;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

        //the "$width: 1000px;" is supposed to be parsed as variable declaration, not property declaration!
        assertNull(NodeUtil.query(result.getParseTree(), "styleSheet/body/bodyItem/rule/declarations/declaration/propertyDeclaration"));
        assertNotNull(NodeUtil.query(result.getParseTree(), "styleSheet/body/bodyItem/rule/declarations/declaration/cp_variable_declaration"));

    }

    public void testMixinCallWithWSBeforeFirstArgument() {
        String source
                = "@mixin a {\n"
                + "  @include b( linear-gradient(\n"
                + "      lighten($bg-color, 5%),\n"
                + "      darken($bg-color, 5%)\n"
                + "    )\n"
                + "  );\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testUnexpectedANDInMedia() {
        String source
                = "@media screen and ($width-name : $target-width) {\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testIf_Else() {
        String source
                = "$type: monster;\n"
                + "p {\n"
                + "  @if $type == ocean {\n"
                + "    color: blue;\n"
                + "  } @else if $type == matador {\n"
                + "    color: red;\n"
                + "  } @else if $type == monster {\n"
                + "    color: green;\n"
                + "  } @else {\n"
                + "    color: black;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

        //only 'if' is allowed as the ident after @else keyword
        source
                = "p {\n"
                + "  @if $type == ocean {\n"
                + "    color: blue;\n"
                + "  } @else Yf $type == matador {\n"
                + "    color: red;\n"
                + "  }\n"
                + "}";

        result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertTrue(result.getDiagnostics().size() > 0);

    }

    public void testContentDirective() {
        String source
                = "@mixin apply-to-ie6-only {\n"
                + "  * html {\n"
                + "    @content;\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testContentDirectiveInMedia() {
        String source
                = "@mixin respond-to($media) {\n"
                + "  @if $media == handhelds {\n"
                + "    @media only screen and (max-width: $break-small) { @content; }\n"
                + "  } @else if $media == medium-screens {\n"
                + "    @media only screen and (min-width: $break-small + 1) and (max-width:\n"
                + "$break-large - 1) { @content; }\n"
                + "  }\n"
                + "  @else if $media == wide-screens {\n"
                + "    @media only screen and (min-width: $break-large) { @content; }\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallArgWithPropertyName() {
        String source
                = "@mixin border-radius($radius: 5px, $moz: true, $webkit: true, $ms: true) {\n"
                + "}\n"
                + "div{\n"
                + "    @include border-radius($webkit:false);\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testMixinCallArgWithValueSeparatedByWS() {
        String source
                = "#id {\n"
                + "    @include border-radius(5px, -moz -webkit);\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

    }

    public void testPropertyValueSyntacticPredicateBoundary() {
        //the scss_declaration_property_value_interpolation_expression synt. predicate
        //was terminated just by colon so it seen the interpolation expression
        //few lines below and caused bad parsing
        String source
                = "#test2 { \n"
                + "    background-color: cyan\n"
                + "}\n"
                + "#test#{$i} { }";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testMultiplicityOperatorInPropertyValueFunction() {
        String source
                = ".c {\n"
                + "    background-color: darken(orange, $i*5);\n"
                + "}\n"
                + "";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);
    }

    public void testIfControlExpression() {
        assertParses(" @if $arg != null and $arg2 != transparent { }");
        assertParses(" @if not $arg != null and $arg2 != transparent { }");
        assertParses(" @if not $arg != null or not $arg2 != transparent { }");
        assertParses(" @if true or not $arg2 != transparent { }");
    }

    public void testMixinWithFirstParamOnNewLine() {
        assertParses("@mixin color(\n"
                + "$bgcolor: red,"
                + "$fgcolor: blue) {\n"
                + "}");
    }

    public void testFunctionInIfStatementExpression() {
        assertParses("@function myfn($color) {\n"
                + "    @if lightness($color) > 50 {\n"
                + "        @return light;\n"
                + "    } @else {\n"
                + "        @return dark;\n"
                + "    }\n"
                + "}");
    }

    public void testImportInDeclarations() {
        assertParses(".clz { @import \"hello\"; }");
    }

    public void testCommaInSelectorInterpolationExpression() {
        assertParses(".#{$prefix}sg,.#{$prefix}ag .#{$prefix}xx { }");
    }

    public void testIfCondition() {
        assertParses(" @if ($mode == light) {}");
    }

    public void testSassFunctionWhereWithArgDefiningValue() {
        assertParses("@function color-by-background($bg-color, $contrast: $default-text-contrast) {\n"
                + "    @return color-offset($bg-color, $contrast, $tmpmode, $inverse: true);\n"
                + "}");
    }

    public void testWeirdControlBlockOperator() {
        assertParses("@if $right =< 0 {}");
        assertParses("@if $right <= 0 {}");
        assertParses("@if $right >= 0 {}");
        assertParses("@if $right => 0 {}");
    }

    public void testWSBetweenMixinCallArgAndComma() {
        assertParses(".clz {\n"
                + "     @include background-gradient(\n"
                + "         $background-color ,\n"
                + "         $background-direction\n"
                + ");\n"
                + "}");
    }

    public void testControlBlockExpression() {
        assertParses("@if $arg != null and ($arg2 == val1 or $arg2 == val2) {}");
    }

    public void testControlBlockExpression2() {
        assertParses("@if (not $arg or $arg2) and $arg3 != null {}");
    }

    public void testControlBlockExpression3() {
        assertParses("@if ($arg or $arg2) and arg3 != null {}");
    }

    public void testControlBlockExpression4() {
        assertParses("@if $arg != null and ($arg2 or arg3) {}");
    }

    public void testDashInSelectorInterpolationExpression() {
        assertParses(".#{$v1}#{$v2}-post {}");
    }

    public void testFunctionReturnBooleanExpression() {
        assertParses("@function even($number) {\n"
                + "    @return ceil($number / 2) == ($number / 2);\n"
                + "}");
    }

    public void testStarInSelectorInterpolationExpression() {
        assertParses(".#{$prefix}border-box * {}");
    }

    public void testGreaterSymbolInSelectorInterpolationExpression() {
        assertParses(".#{$prefix}rtl > .#{$prefix}box-item {}");
    }

    public void testLRPARENInPropertyValueInterpolationExpression() {
        assertParses(".clz { $rotation: rotate(#{$angle}deg); }");
        assertParses(".clz { background-image: slicer-corner-sprite(btn-#{$ui}-over, 'btn/btn-#{$ui}-over-corners'); }");
    }

    public void testCPExpressionInPropertyValue() {
        assertParses(".clz { background-position: 0 ($accordion-header-tool-size * -17); }");
    }

    public void testCPExpressionInPropertyValue2() {
        assertParses(".clz { padding: $toolbar-vertical-spacing ($toolbar-horizontal-spacing / 2) $toolbar-vertical-spacing ($toolbar-horizontal-spacing / 2); }");
    }

    public void testCPExpressionInPropertyValue3() {
        assertParses(".clz { $fieldset-collapse-tool-background-position-over: 0 (-$fieldset-collapse-tool-size) !default; }");
    }

    public void testFunctionInsideSASSInterpolationExpression() {
        assertParses(".clz { padding-left: #{left($fieldset-header-padding) - 2}; }");
    }

    public void testSimplePropertyValue() {
        assertParses(".clz { padding: 2cm 10px; }", false);
    }

    public void testPropertyValue2() {
        assertParses(".clz { padding-left: "
                + "top($form-error-under-padding) "
                + "right($form-error-under-padding) "
                + "bottom($form-error-under-padding) "
                + "(left($form-error-under-padding) + $form-error-icon-width + $form-error-under-icon-spacing); "
                + "}");
    }

    public void testPropertyValue3() {
        assertParses(".clz { background-position: 0 (0 - $form-checkbox-size); }");
    }

    public void testPropertyValue4() {
        assertParses(".clz { background-position: (-$html-editor-toolbar-icon-size) 0; }");
        assertParses(".clz { background-position: 0 (-$spinner-btn-height); }");
    }

    public void testPropertyValue5() {
        assertParses(".clz { background-position: 0 (-$spinner-btn-height); }");
    }

    public void testPropertyValueUnaryOperatorBeforeBrace() {
        assertParses(".clz { background-position: -($form-trigger-width * 3) (-$spinner-btn-height); }");
    }

    public void testCPVariableDeclaration() {
        assertParses("$panel-frame-header-padding:\n"
                + "    (top($panel-header-padding) - top($panel-frame-border-width))\n"
                + "    (right($panel-header-padding) - right($panel-frame-border-width))\n"
                + "    (bottom($panel-header-padding) - bottom($panel-frame-border-width))\n"
                + "    (left($panel-header-padding) - left($panel-frame-border-width))\n"
                + "    !default;");
    }

    public void testSASSInterpolationExpressionInCPVariableDeclaration() {
        assertParses("$fieldset-header-font: #{$fieldset-header-font-size}/#{$fieldset-header-line-height} $fieldset-header-font-weight $fieldset-header-font-family !default;");
        assertParses("$form-label-font: $form-label-font-weight #{$form-label-font-size}/#{$form-label-line-height} $form-label-font-family !default;");
        assertParses("$grid-editor-font: normal #{$grid-row-cell-font-size}/#{$grid-editor-line-height} $font-family !default;");
        assertParses("$grid-row-cell-font: normal #{$grid-row-cell-font-size}/#{$grid-row-cell-line-height} $font-family !default;");
    }

    public void testImportantKeywordInCPVariableDeclaration() {
        assertParses("$grid-row-editor-border: $grid-row-editor-border-width solid $grid-row-editor-border-color !important !default;");
    }

    public void testCommaSeparatedPropertyValues() {
        assertParses(".x { background-size: $majorsteps $majorsteps, $majorsteps $majorsteps, $minorsteps $minorsteps, $minorsteps $minorsteps; }");
    }

    public void testImportantSymbolJustAfterPropertyValue() throws ParseException, BadLocationException {
        assertParses(".x { z-index: 1000000!important; }");
        assertParses(".x { z-index: 1000000 !important; }");
    }

    public void testInclude() throws ParseException, BadLocationException {
        assertParses(".x { @include x-slicer($panel-header-ui + '-top'); }");
    }

    public void testMSPropertyValueAndInterpolationExpression() throws ParseException, BadLocationException {
        assertParses(".x { filter: progid:DXImageTransform.Microsoft.BasicImage(rotation=#{$ie-rotation}); }");
    }

    public void testLastItemInBlockDoesntNeedToBeTerminatedWithSemicolon() throws ParseException, BadLocationException {
        assertParses(".x { $image-search-path: '.' !default }"); //doesn't work
        assertParses(".x { $image-search-path: '.' !default; }"); //works
    }

    public void testUnaryOperatorWithIE() throws ParseException, BadLocationException {
        assertParses(".x { margin-top: -#{top($fieldset-border-width)}; }");
    }

    public void testFunctionArgumentsCanBeBooleanExpression() throws ParseException, BadLocationException {
        assertParses("$foo: if($direction == top or $direction == bottom, 0, 1);");
    }

    public void testFunction3() throws ParseException, BadLocationException {
        assertParses(".clz { @include linear-gradient(#3875d7 20%, #2a62bc 90%); }");
    }

    public void testFont_FaceInMixin() throws ParseException, BadLocationException {
        assertParses("@mixin font-face($name){\n"
                + "    @font-face {\n"
                + "        font-family: $name;\n"
                + "    }\n"
                + "}");
    }

    public void testCommaInCPExpression() throws ParseException, BadLocationException {
        assertParses(".highlighted {\n"
                + "    @include linear-gradient((#3875d7 20%, #2a62bc 90%));\n"
                + "    color: #fff;\n"
                + "}");
    }

    public void testSassInclude() throws ParseException, BadLocationException {
        assertParses(".clz { @include extjs-button-ui(\n"
                + "    $ui: 'default-small',\n"
                + "\n"
                + "    $border-radius: $button-small-border-radius,\n"
                + "    $border-width: $button-small-border-width); }");
    }

    public void testSassInclude2() throws ParseException, BadLocationException {
        assertParses(".clz { @include extjs-toolbar-ui(\n"
                + "    'default',\n"
                + "    $background-color: $toolbar-background-color,\n"
                + "    $background-gradient: $toolbar-background-gradient,\n"
                + "    $border-color: $toolbar-border-color\n"
                + "); "
                + "}");
    }

    public void testNestedRules2() throws ParseException, BadLocationException {
        assertParses("x { y {} z {} }");
        assertParses("x { y {} }");
    }

    public void testNestedIfs() throws ParseException, BadLocationException {
        assertParses("x { @if true {} }");
        assertParses("x { @if $a==10 {} }");
        assertParses("x { @if true {} @if false {} }");
    }

    public void testNestedRuleNotParsedAsDeclaration() {
        String cssCode = "x {\n"
                + "    a, a:hover {\n"
                + "    }\n"
                + "}";

        CssParserResult result = TestUtil.parse(cssCode);
        Node tree = result.getParseTree();
//        NodeUtil.dumpTree(tree);
        Node node = NodeUtil.query(tree, "styleSheet/body/bodyItem/rule/declarations/declaration/rule");
        assertNotNull(node);
        assertEquals(NodeType.rule, node.type());

        assertResultOK(result);

    }

    public void testMixinCallWithBlock() throws ParseException, BadLocationException {
        assertParses("@include respond_to ( handhelds ) { \n"
                + "    font-size: 1em;\n"
                + "}  ");

        assertParses("h1 {"
                + "     @include respond_to ( handhelds ) { \n"
                + "         font-size: 1em;\n"
                + "     }\n"
                + "}");

    }

    public void testParseCommentAtTheFileEnd() throws ParseException, BadLocationException {
        assertParses("div {}\n"
                + "/*comment*/");

        assertParses("//comment1\n"
                + "div {}\n"
                + "//comment2");

    }

    public void testMixinDeclarationWithVarargs() throws ParseException, BadLocationException {
        assertParses("@mixin box-shadow($shadows...) {}\n");
    }

    public void testMixinCallWithVarargs() throws ParseException, BadLocationException {
        assertParses("@include colors($values...);");
    }

    //https://netbeans.org/bugzilla/show_bug.cgi?id=227484#c21
    public void testMixinCallInMedia() throws ParseException, BadLocationException {
        assertParses("@media screen{\n"
                + "    @include test2;\n"
                + "}");
    }

    //https://netbeans.org/bugzilla/show_bug.cgi?id=227484#c19
    public void testAmpInSelector() throws ParseException, BadLocationException {
        assertParses("&.primary, input[type=\"submit\"] & { }\n");
        //assertParses("&.primary, input[type=\"submit\"]& { }\n"); //this is not possible in scss
        assertParses("&.primary, & { }\n");
    }

    public void testIncompleteSelectors() throws ParseException, BadLocationException {
        assertParses(".pills {\n"
                + "  @include clearfix;\n"
                + "  > li {\n"
                + "    float: left;\n"
                + "    > a {\n"
                + "      display: block;\n"
                + "    }\n"
                + "  }\n"
                + "}");
    }

    //https://netbeans.org/bugzilla/show_bug.cgi?id=233038#c2
    public void testCPExpressionInMediaQuery() throws ParseException, BadLocationException {
        assertParses("@media only tv and (resolution: 120 * 3 + \"dpi\"){\n"
                + "\n"
                + "}");
    }

    public void testMixinBodyPropertyRecovery() throws ParseException, BadLocationException {
        CssParserResult result = TestUtil.parse("@mixin mymixin() { color: }");
        Node node = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem/cp_mixin_declaration/cp_mixin_block/declarations/declaration/propertyDeclaration/property");
        assertNotNull(node);
    }

    public void testPercentageInExpression() throws ParseException, BadLocationException {
        CssParserResult result = TestUtil.parse("$size: 20;\n"
                + ".clz {\n"
                + "    font: %/20;\n"
                + "}");

//        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

        //original sample
        assertParses("@mixin base-type($weight, $line-height, $font-size, $font-family...) {\n"
                + "    @if $serif-boolean {\n"
                + "        font: $weight #{$font-size}%/#{$line-height} $font-family;\n"
                + "    }@else {\n"
                + "        font: $weight #{$font-size}%/#{$line-height} $font-family-sans;\n"
                + "    }\n"
                + "}");
    }

    public void testSelectorWithInterpolationExpressionInMediaBody() throws ParseException, BadLocationException {
        assertParses("@media only screen and (min-resolution: 1000) {\n"
                + "\n"
                + "      #{$selector}:before {\n"
                + "        content: $hiResIcon;\n"
                + "      }\n"
                + "    }");
    }

    public void testInterpolationExpressionWithBrackets() {
        assertParses(".#{$item}-important { background-color: green; }");

        assertParses(".important[href]  {  }\n");

        assertParses(".#{$item}important[href]  {  }\n");

        assertParses("@each $item in label, badge {\n"
                + "   .#{$item}-important         { background-color: green; }\n"
                + "   .#{$item}-important[href]   { background-color: blue; }\n"
                + " }");
    }

    public void testIssue237010() {
        assertParses("$textColorError: \"blue\";\n"
                + "$allowTagSelectors: true;\n"
                + "@if $allowTagSelectors != true {\n"
                + "  .oj-text-input {\n"
                + "    @include oj-normalize-text-input;\n"
                + "  }\n"
                + "  .oj-text-input[type='search'] {\n"
                + "    @include oj-normalize-search-input;\n"
                + "  }\n"
                + "}\n"
                + "$currSelectors: if($allowTagSelectors,\n"
                + "  // here:  \n"
                + " \".oj-date-input.oj-invalid, \n"
                + "  input[type='url'].oj-invalid\", \n"
                + "  // class selectors: \n"
                + "  \".oj-text-input.oj-invalid,\n"
                + "  .oj-textarea.oj-invalid\");\n"
                + "#{$currSelectors} {\n"
                + "    border: 2px solid $textColorError; \n"
                + "}");
    }

    public void testIssue236388() {
        assertParses("@media only screen and (min-width: 768px) {\n"
                + "    @import \"_grid.less\";\n"
                + "    @import \"_768up.less\";\n"
                + "}");
    }

    public void testIssue236706() {
        assertParses("@mixin web-font($fonts, $variants: (), $subsets: (), $text: '', $effects: (), $secure: false) {}");
    }

    //new SASS 3.3 stuff
    //http://thesassway.com/news/sass-3-3-released
    public void testSassMap() {
        assertParses("$colors: (\n"
                + "  header: #b06,\n"
                + "  footer: $another_var,\n"
                + ")");

        assertParses("$susy: (\n"
                + "    columns: 8,\n"
                + "    gutters: 1/10\n"
                + ");");

    }

    public void testSassMapInBlock() {
        assertParses("div { $colors: (\n"
                + "  header: #b06,\n"
                + "  footer: $another_var,\n"
                + ") }");

        assertParses(".clz {\n $susy: (\n"
                + "    columns: 8,\n"
                + "    gutters: 1/10\n"
                + "); }");
    }

    public void testSassMapComplex() {
        //bit more complex example
        assertParses("$var: red;\n"
                + "\n"
                + "$map: ( cool: 'pink');\n"
                + "\n"
                + "div {\n"
                + "\n"
                + "    $colors: (\n"
                + "        header: $var,\n"
                + "        text: #334,\n"
                + "        color: map-get($map, cool),\n"
                + "        );\n"
                + "\n"
                + "    .pink {\n"
                + "        color: map-get($colors, color);\n"
                + "    }\n"
                + "\n"
                + "}\n");
    }

    public void testParentSelectorSuffixes() {
        assertParses("// Ampersand in SassScript:\n"
                + ".button {\n"
                + "  &-primary { background: orange; }\n"
                + "  &-secondary { background: blue; }\n"
                + "}\n");
        // Test parent selector combined with interpolation
        assertParses("$i: 1;\n"
                + ".selector\n"
                + "{\n"
                + "    &--#{$i}\n"
                + "    {\n"
                + "    }\n"
                + "    &__#{$i}\n"
                + "    {\n"
                + "    }\n"
                + "}", true);
    }

    public void testAtRoot() {
        assertParses(".message {\n"
                + "  @at-root {\n"
                + "    .info { color: blue; }\n"
                + "    .error { color: red; }\n"
                + "  }\n"
                + "}\n");

        assertParses(".message {\n"
                + "  @at-root .info { color: blue; }\n"
                + "  @at-root .error { color: red; }\n"
                + "}\n");
    }

    public void testIfInFn() {
        assertParses("@function my-function($args...) {\n"
                + "  // Assign the first argument to $param-1\n"
                + "  $param-1: nth($args, 1);\n"
                + "\n"
                + "  // If a second argument was passed assign it to $param-2,\n"
                + "  // otherwise assign an empty list:\n"
                + "  $param-2: if(length($args) > 1, nth($args, 2), ());\n"
                + "}\n");
    }

    public void testBwLoop() {
        assertParses("@for $i from 5 through 1 {\n"
                + ".selector#{$i} { content: $i; }"
                + "}\n");
    }

    //can't parse an expression inside the interpolation expression
    public void _testInterpolationExpression_BUG1() {
        assertParses(".span:nth-child(#{6-$i}) { content: $i; }");
    }

    //can't parse an interpolation expression as fn argument
    public void _testInterpolationExpression_BUG2() {
        assertParses(".span:nth-child(#{$i}) { content: $i; }");
    }

    public void testForEachMultiAssignments() {
        assertParses("$animals: (puma, black, default),\n"
                + "          (sea-slug, blue, pointer),\n"
                + "          (egret, white, move);\n"
                + "\n"
                + "@each $animal, $color, $cursor in $animals {\n"
                + "  .#{$animal}-icon {\n"
                + "    background-image: url('/images/#{$animal}.png');\n"
                + "    border: 2px solid $color;\n"
                + "    cursor: $cursor;\n"
                + "  }\n"
                + "}\n");
    }

    public void testMixin() {
        String source = "@mixin simple($selectorPrefix){}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testScssVar() {
        String source = ".test {background-image: $var;}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testInterpolationInSelector() {
        String source = ".#{$selectorPrefix}-odd-cols-#{$i} > .oj-row > .oj-col:nth-child(even) {}";

        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

        source = ".table-odd-cols-#{$i} > .oj-row > .oj-col:nth-child(even) {}";
        result = TestUtil.parse(source);
        assertResultOK(result);

        source = ".table-odd-cols-#{$i}-test:nth-child(even) {}";
        result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testMapWithStrings() {
        String source = "$map: (\n"
                + "    '1':       'one',\n"
                + ");";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testMapDeclarationWithDefault() {
        String source = "$modules: () !default;";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testIfWithoutSpace() {
        String source = "@if($allowTagSelectors)\n"
                + "  {\n"
                + "    #{$tagSelectors} {\n"
                + "      color: red;\n"
                + "    }\n"
                + "  }";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testElseIfTogether() {
        String source = "@mixin icon($name, $character: null, $font-family: 'Pictos') {\n"
                + "    @if $character != null {\n"
                + "        content: \"#{$character}\";\n"
                + "    } @elseif $raw_character != null {\n"
                + "        content: \"#{$raw_character}\";\n"
                + "    }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testFunctionParametersOnNewLines() {
        String source = "@function matte-gradient (\n"
                + "    $bg-color: $base-color,\n"
                + "    $direction: top,\n"
                + "    $contrast: 1\n"
                + ") {\n"
                + "    @return linear-gradient(red);\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
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

    public void testQuoteEscapingInString() {
        String source = "@function icon-character-for-name($name) {\n"
                + "    // http://pictos.cc/font/\n"
                + "\n"
                + "    // Row 1\n"
                + "    @if ($name == \"anchor\") { @return \"a\"; }\n"
                + "    @else if ($name == \"quote\") { @return \"\\\"\"; }\n"
                + "    @else if ($name == \"volume_mute\") { @return \"<\"; }}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testEachWithMapPairs() {
        String source = "@each $header, $size in (h1: 2em, h2: 1.5em, h3: 1.2em) {\n"
                + "  #{$header} {\n"
                + "    font-size: $size;\n"
                + "  }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testParenSelectorAsPropertyValue() {
        String source = ".foo .bar, .baz { $selector: & }";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testMultiDimensionalMaps() {
        String source = "$type-scale: (\n"
                + "    tiny: (\n"
                + "        font-size: 10.24px,\n"
                + "        base-lines: 0.5\n"
                + "    ),\n"
                + "    small: (\n"
                + "        font-size: 12.8px,\n"
                + "        base-lines: 0.75\n"
                + "    )\n"
                + ");\n"
                + "";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testErrorWithSemi() {
        String source = "@mixin adjust-location($x, $y) {\n"
                + "  @if unitless($x) {\n"
                + "    @error \"$x may not be unitless, was #{$x}.\";\n"
                + "  }\n"
                + "  position: relative; left: $x; top: $y;\n"
                + "}\n"
                + "";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testAtRootDeclaration() {
        String source = "@media print {\n"
                + "  .page {\n"
                + "    width: 8in;\n"
                + "    @at-root (without: media) {\n"
                + "      color: red;\n"
                + "    }\n"
                + "  }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testKeyframesInSass() {
        String source = "@mixin keyframes($name)\n"
                + "{\n"
                + "    @-webkit-keyframes $name\n"
                + "    {\n"
                + "        @content;\n"
                + "    }\n"
                + "    @keyframes $name\n"
                + "    {\n"
                + "        @content;\n"
                + "    }\n"
                + "}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);

        source = "@mixin keyframes($name) {\n"
                + "  @-webkit-keyframes #{$name} {\n"
                + "    @content; \n"
                + "  }}";

        result = TestUtil.parse(source);
        assertResultOK(result);

        source = "@include keyframes(slide-down) {\n"
                + "  0% { opacity: 1; }\n"
                + "  90% { opacity: 0; }\n"
                + "}";

        result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testSassNestingGreater() {
        assertParses("div {\n"
                + "    margin: 0;\n"
                + "    > {\n"
                + "        label {\n"
                + "            float: left;\n"
                + "        }\n"
                + "        input {\n"
                + "            width: 70%; \n"
                + "        }\n"
                + "    }\n"
                + "}");
    }

    public void testPseudoClassBeforeAmpersand() {
        assertParses("  li {\n"
                + "        a:hover, &.selected {\n"
                + "            display: block;\n"
                + "        }\n"
                + "    }");

    }

    public void testDotInterpolationMinus() {
        assertParses("$fa-css-prefix : test;\n"
                + ".#{$fa-css-prefix}-2x { font-size: 2em; }");
    }

    public void testAmpColonInterpolation() {
        assertParses("a {\n"
                + "    &:#{$state}{\n"
                + "        color: black\n"
                + "    }\n"
                + "}");
    }

    public void testMathExpWithUnits() {
        assertParses("$fa-li-width: (30em / 14) !default;");
    }

    public void testExtendPlaceHolderWithInterpolation() {
        assertParses("%at-contactformelement-skin-default {\n"
                + "  margin-top: 6rem;\n"
                + "}\n"
                + "\n"
                + "@mixin contactformelement($selector, $skin: default) {\n"
                + "  #{$selector} {\n"
                + "    @extend %at-contactformelement-skin-#{$skin} !optional;\n"
                + "  }\n"
                + "}");
    }

    public void testSpacesInSassInterpolation() {
        assertParses(".test {\n"
                + "    &.#{ $active_class } > a {\n"
                + "        background: $active-bg;\n"
                + "    }\n"
                + "}");
    }

    public void testMathExpressionInPropertyValue() {
        assertParses("@mixin dropdown-container($content:list, $triangle:true, $max-width:$f-dropdown-max-width) {\n"
                + "    &:before {\n"
                + "      @include css-triangle($f-dropdown-triangle-size, $f-dropdown-triangle-color, #{$opposite-direction});\n"
                + "      position: absolute;\n"
                + "      top: $f-dropdown-triangle-side-offset;\n"
                + "      #{$default-float}: -($f-dropdown-triangle-size * 2);\n"
                + "      z-index: 89;\n"
                + "    }\n"
                + "}");
    }

    public void testPropertyDeclarationWithExpression() {
        assertParses("@media only screen and (min-width: $media-xs)\n"
                + "{\n"
                + "    width: calc(100% - #{$left-column-width});\n"
                + "}");
    }

    public void testURLinScss() {
        assertParses("@function oj-image-url($path){\n"
                + " @return url($imageDir + $path);\n"
                + "}");
        assertParses(".xxx {\n"
                + "    content: url($imagesDir + \"functional/func_back_24_ena.png\"); \n"
                + "}");
        assertParses("@function composeURL($a, $b)\n"
                + "{\n"
                + "    @return url($a + $b);\n"
                + "}");
        assertParses("@each $ord in 1, 2, 3, 4 {\n"
                + "	\n"
                + "	body._#{$ord} #page {\n"
                + "		background: $page-bg url(#{$scssDir}/images/bg#{$ord}-squares-blue.png);\n"
                + "	}\n"
                + "	\n"
                + "}");
        assertParses("li {\n"
                + "    background: transparent url(@{img_path}list-bullet.png) no-repeat 0 0;\n"
                + "				padding-left: 14px;\n"
                + "				margin-bottom: 5px;\n"
                + "}");

    }

    public void testPseudoWithWhitespaceOnEnd() {
        assertParses("#test\n"
                + "{\n"
                + "    &:not( .active )\n"
                + "    {\n"
                + "        background-color: #E1E1E1;\n"
                + "    }\n"
                + "}");
    }

    public void testFunctionQuoteInMap() {
        assertParses("$a: (quote(bgcolor): black)");
    }

    public void testSassMapWithNumbers() {
        assertParses("$twoToTheN: (0: 1, 1: 2, 2: 4, 3: 8, 4: 16, 5: 32);");
    }

    public void testStringEscapesInInterpolation() {
        assertParses("@function oj-prepend-slash($char)\n"
                + "{\n"
                + "  @return #{\"\\\"\\\\\"}#{$char + \"\\\"\"};\n"
                + "}");
    }

    public void testEachWithMultipleLists() {
        assertParses("@each $className, $iconFile\n"
                + "in\n"
                + "(my_class, my_file_name_base),\n"
                + "{}");
        assertParses("@each $animal, $color, $cursor in (puma, black, default),\n"
                + "                                  (sea-slug, blue, pointer),\n"
                + "                                  (egret, white, move) {\n"
                + "  .#{$animal}-icon {\n"
                + "    background-image: url('/images/#{$animal}.png');\n"
                + "    border: 2px solid $color;\n"
                + "    cursor: $cursor;\n"
                + "  }\n"
                + "}");
    }

    public void testMapAnyDatatypeasKey() {
        assertParses("$font-formats: 'woff' 'ttf'; // Define what webfont formats need importing\n"
                + "$font-path: '../fonts/'; // Set the a path to your fonts directory\n"
                + "\n"
                + "$fonts: (\n"
                + "  'heading': ( // give your font a semantic name for reference\n"
                + "    'name': 'maven', // optionally set a different font name\n"
                + "    'stack': ('helvetica', 'arial', sans-serif), // define the stack\n"
                + "    'normal': 'maven/maven_pro_regular-webfont', // point to any webfont files\n"
                + "    'bold': 'maven/maven_pro_bold-webfont',\n"
                + "  ),\n"
                + "\n"
                + "  'body': (\n"
                + "    'name': 'exo',\n"
                + "    'stack': ('helvetica', 'arial', sans-serif),\n"
                + "    'normal': 'exo/exo2-regular-webfont',\n"
                + "    'italic': 'exo/exo2-italic-webfont',\n"
                + "    'bold': 'exo/exo2-bold-webfont',\n"
                + "    'bold' 'italic': 'exo/exo2-bolditalic-webfont',\n"
                + "  ),\n"
                + "\n"
                + "  'alias': 'body', // create aliases when useful\n"
                + ");");
    }

    public void testMediaWithInterpolation() {
        assertParses("$information-phone: \"(max-width : 320px)\";\n"
                + "@media screen and (#{$information-phone}) {\n"
                + "  background: red;\n"
                + "}\n"
                + "@media screen and #{$information-phone} {\n"
                + "  background: red;\n"
                + "}");
    }

    public void testScssUseForward() {
        assertParses("@use 'test1';");
        assertParses("@use 'test2' as t;");
        assertParses("@use 'test2' with ($black: #222, $border-radius: 0.1rem);");
        assertParses("@use 'test2' as t with ($black: #222, $border-radius: 0.1rem);");
        assertParses("@forward 'test1';");
        assertParses("@forward 'test2' as t;");
        assertParses("@forward 'test2' with ($black: #222, $border-radius: 0.1rem);");
        assertParses("@forward 'test2' as t with ($black: #222, $border-radius: 0.1rem);");
        assertParses("@forward 'test2' hide dummy1;");
        assertParses("@forward 'test2' show dummy1, dummy2;");
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
}
