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
package org.netbeans.modules.css.prep.editor.scss;

import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.css.editor.csl.CssCompletion;
import org.netbeans.modules.css.editor.module.main.CssModuleTestBase;
import org.netbeans.modules.css.prep.editor.model.CPModel;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class ScssCompletionTest extends CssModuleTestBase {

    public ScssCompletionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CPModel.topLevelSnapshotMimetype = getTopLevelSnapshotMimetype();
        CssCompletion.testFileObjectMimetype = "text/scss";
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        CPModel.topLevelSnapshotMimetype = null;
        CssCompletion.testFileObjectMimetype = null;
    }

    @Override
    protected String getTopLevelSnapshotMimetype() {
        return "text/scss";
    }

    @Override
    protected String getCompletionItemText(CompletionProposal cp) {
        return cp.getInsertPrefix();
    }

    public void testVarCompletionInSimplePropertyValue() throws ParseException {
        checkCC("$var: 1; h1 { color: $| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1; h1 { color: $v| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1; h1 { color: $va| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1; h1 { color: $var| }", arr("$var"), Match.EXACT);

        checkCC("$var: 1; h1 { font: red $| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1; h1 { font: red $v| }", arr("$var"), Match.EXACT);

        checkCC("$var1: 1; $var2: 2; h1 { font: red $v| }", arr("$var1", "$var2"), Match.EXACT);
        checkCC("$var1: 1; $var2: 2; h1 { font: red $var2| }", arr("$var2"), Match.EXACT);
    }

    public void testVarCompletionInMixinBody() throws ParseException {
        checkCC("$var: 1;  @mixin my { $| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1;  @mixin my { $v| }", arr("$var"), Match.EXACT);

        checkCC("$var: 1;  @mixin my { color: $| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1;  @mixin my { color: $va| }", arr("$var"), Match.EXACT);

        //this fails as the $foo is not parsed - see CPModelTest.testVariablesInMixinWithError_fails
//        checkCC("$var: 1;  @mixin my { $foo: $v| }", arr("$var", "$foo"), Match.CONTAINS);
        checkCC("$var: 1;  @mixin my { $foo: $v| }", arr("$var"), Match.CONTAINS);

    }

    public void testVarCompletionInRuleBody() throws ParseException {
        checkCC("$var: 1;  .clz { $| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1;  .clz { $v| }", arr("$var"), Match.EXACT);

        //this fails as the $foo is not parsed - see CPModelTest.testVariablesInMixinWithError_fails
//        checkCC("$var: 1;  .clz { $foo: $v| }", arr("$var", "$foo"), Match.CONTAINS);
        checkCC("$var: 1;  .clz { $foo: $v| }", arr("$var"), Match.CONTAINS);

    }

    public void testKeywordsCompletion() throws ParseException {
        checkCC("@| ", arr("@mixin"), Match.CONTAINS);
        checkCC("@mix| ", arr("@mixin"), Match.EXACT);
    }

    public void testContentKeywordsCompletion() throws ParseException {
        checkCC("@| ", arr("@content"), Match.CONTAINS);
        checkCC("@cont| ", arr("@content"), Match.EXACT);
    }

    public void testMixinsCompletion() throws ParseException {
        checkCC("@mixin mymixin() {}\n @include | ", arr("mymixin"), Match.CONTAINS);
        checkCC("@mixin mymixin() {}\n @include mymi| ", arr("mymixin"), Match.EXACT);
    }

    public void testMixinsCompletion2() throws ParseException {
        checkCC("div{\n"
                + "    @include |;\n"
                + "}\n"
                + "\n"
                + "\n"
                + "@mixin mix1($dist) {\n"
                + "  float: left;\n"
                + "  margin-left: $dist;\n"
                + "}\n"
                + "@mixin mix2($dist) {\n"
                + "  float: left;\n"
                + "  margin-left: $dist;\n"
                + "}", arr("mix1", "mix2"), Match.EXACT);

        checkCC("div{\n"
                + "    @include mi|;\n"
                + "}\n"
                + "\n"
                + "\n"
                + "@mixin mix1($dist) {\n"
                + "  float: left;\n"
                + "  margin-left: $dist;\n"
                + "}\n"
                + "@mixin mix2($dist) {\n"
                + "  float: left;\n"
                + "  margin-left: $dist;\n"
                + "}", arr("mix1", "mix2"), Match.EXACT);

    }

    public void testDeclarationsInMixin() throws ParseException {
        //we are checking insert prefixes!
        checkCC("@mixin mymixin() { | } ", arr("color: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { co| } ", arr("color: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { colo| } ", arr("color: "), Match.EXACT);
        checkCC("@mixin mymixin() { | \n color: blue} ", arr("color: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { co| \n color: blue} ", arr("color: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { | ;\n color: blue} ", arr("color: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { col| ;\n color: blue} ", arr("color: "), Match.CONTAINS);
    }

    public void testDeclarationsInMixinBeforeNestedRule() throws ParseException {
        //we are checking insert prefixes!
        checkCC("@mixin mymixin() { | div { } } ", arr("color: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { co| div { } } ", arr("color: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { colo| div { } }  ", arr("color: "), Match.EXACT);
    }

    public void testDeclarationsInMixinAfterNestedRule() throws ParseException {
        //we are checking insert prefixes!
        checkCC("@mixin mymixin() { div { } | } ", arr("color: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { div { } co| } ", arr("color: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { div { } colo| }  ", arr("color: "), Match.EXACT);

        checkCC("@mixin mymixin() { div { color: red; } | }  ", arr("blue"), Match.DOES_NOT_CONTAIN);
    }

    public void testSelectorsInMixin() throws ParseException {
        //we are checking insert prefixes!
        checkCC("@mixin mymixin() { | } ", arr("div"), Match.CONTAINS);
        checkCC("@mixin mymixin() { tabl| } ", arr("table-layout: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { table| } ", arr("table-layout: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { | \n color: blue} ", arr("table-layout: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { ta| \n color: blue} ", arr("table-layout: "), Match.CONTAINS);
    }

    public void testSelectorsInMixinWithNestedRule() throws ParseException {
        //we are checking insert prefixes!
        checkCC("@mixin mymixin() { | div { } } ", arr("div"), Match.CONTAINS);
        checkCC("@mixin mymixin() { tabl| div { } } ", arr("table-layout: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { table| div { } } ", arr("table-layout: "), Match.CONTAINS);

        checkCC("@mixin mymixin() { div {} | } ", arr("div"), Match.CONTAINS);
        checkCC("@mixin mymixin() { div {} tabl|  } ", arr("table-layout: "), Match.CONTAINS);
        checkCC("@mixin mymixin() { div {} table|  } ", arr("table-layout: "), Match.CONTAINS);
    }

    public void testPropertyValueInRuleNestedInMixinBody() throws ParseException {
        //we are checking insert prefixes!
        checkCC("@mixin test2 {\n"
                + "    abbr {\n"
                + "        color: | \n"
                + "    }"
                + "}", arr("red"), Match.CONTAINS);

    }

    public void testPropertyValueCompletion() throws ParseException {
        checkCC(".clz { color: | }", arr("red"), Match.CONTAINS);
        checkCC(".clz { color: f| }", arr("fuchsia"), Match.CONTAINS);
        checkCC(".clz { color: fuch| }", arr("fuchsia", "$color_chooser"), Match.EXACT);
    }

    //Bug 233597 - Completion for color based properties offers colors twice
    public void testPropertyValueCompletionDoesntOfferValuesMoreTimes() throws ParseException {
        checkCC("div{\n"
                + "       color: red | \n"
                + "   }",
                arr("red"), Match.DOES_NOT_CONTAIN);
    }

    //Bug 234184 - Sass: missing completion for mixins inside media query
    public void testMixinsCompletionInMQ() throws ParseException {
        checkCC("@mixin test2($para, $para2) { \n"
                + "}\n"
                + "@mixin test($para, $para2) { \n"
                + "}\n"
                + "@for $i from 1 through 3 { \n"
                + "   @media tv {  \n"
                + "        @include | \n"
                + " }\n"
                + "}", arr("test2", "test"), Match.EXACT);
        checkCC("@mixin one($para, $para2) { \n"
                + "}\n"
                + "@mixin two($para, $para2) { \n"
                + "}\n"
                + "@for $i from 1 through 3 { \n"
                + "   @media tv {  \n"
                + "        @include o| \n"
                + " }\n"
                + "}", arr("one"), Match.EXACT);
    }

    public void testSassKeywordsInMedia() throws ParseException {
        checkCC("@media tv {  \n"
                + "    @| \n"
                + "}", arr("@if"), Match.CONTAINS);

        checkCC("@media tv {  \n"
                + "    @ea| \n"
                + "}", arr("@each"), Match.EXACT);
    }

    public void testPseudoForParentSelectorPrefix() throws ParseException {
        //test pseudo class w/ prefix
        assertCompletion("#main {\n"
                + "    &:hov|\n"
                + "\n"
                + "}", Match.EXACT, "hover");

        //pseudo element w/ prefix
        assertCompletion("#main {\n"
                + "    &::first-li|\n"
                + "\n"
                + "}", Match.EXACT, "first-line");
    }

    public void testPseudoForParentSelectorNoPrefix() throws ParseException {
        assertCompletion("#main {\n"
                + "    &:|\n"
                + "\n"
                + "}", Match.CONTAINS, "hover");

        //pseudo element w/o prefix
        assertCompletion("#main {\n"
                + "    &::|\n"
                + "\n"
                + "}", Match.CONTAINS, "first-line");

    }

    public void testPseudoForParentSelectorNoGarbage() throws ParseException {
        //test if it doesn't contain a garbage - like properties
        assertCompletion("#main {\n"
                + "    &:|\n"
                + "\n"
                + "}", Match.DOES_NOT_CONTAIN, "color: ");

        assertCompletion("#main {\n"
                + "    &:colo|\n"
                + "\n"
                + "}", Match.EMPTY);

        //test if it doesn't contain a garbage - like properties
        assertCompletion("#main {\n"
                + "    &::|\n"
                + "\n"
                + "}", Match.DOES_NOT_CONTAIN, "color");

        //test if it doesn't contain a garbage - like properties
        assertCompletion("#main {\n"
                + "    &::colo|\n"
                + "\n"
                + "}", Match.EMPTY, "color");

    }

    //https://netbeans.org/bugzilla/show_bug.cgi?id=236137
    /*
     Tests how the code completion in rule body is affected by presence of CSS comments.
     */
    public void testIssue236137() throws ParseException {
        //test html elements offered before the comment
        assertCompletion("@for $i from 1 through 3 { \n"
                + "    @media tv {  \n"
                + "         |\n"
                + "        /*cc;51; ;div,span;0*/\n"
                + "        \n"
                + "    }\n"
                + "}", Match.CONTAINS, "div");

        //test html elements offered after the comment
        assertCompletion("@for $i from 1 through 3 { \n"
                + "    @media tv {  \n"
                + "        /*cc;51; ;div,span;0*/\n"
                + "         |\n"
                + "        \n"
                + "    }\n"
                + "}", Match.CONTAINS, "div");

    }

    public void testIssue236137_part2() throws ParseException {
        //test html elements offered after the comment
        assertCompletion("div {  \n"
                + "     \n"
                + "     /*cc;51; ;div,span;0*/\n"
                + "     | \n"
                + "     \n"
                + "}", Match.CONTAINS, "div");

        //test html elements offered before the comment
        assertCompletion("div {  \n"
                + "     \n"
                + "     | \n"
                + "     \n"
                + "     /*cc;51; ;div,span;0*/\n"
                + "     \n"
                + "}", Match.CONTAINS, "div");
        
        assertCompletion("div {  \n"
                + "     \n"
                + "     | \n"
                + "     \n"
                + "}", Match.CONTAINS, "div");        

    }
}
