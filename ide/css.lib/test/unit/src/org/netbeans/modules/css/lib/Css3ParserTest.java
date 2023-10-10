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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.css.lib.api.*;
import org.netbeans.modules.css.lib.nbparser.ProgressingTokenStream;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class Css3ParserTest extends CssTestBase {

    public Css3ParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CssParserResult.IN_UNIT_TESTS = true;
    }

    public void testAllANTLRRulesHaveNodeTypes() {
        for (String rule : Css3Parser.ruleNames) {
            if (!rule.startsWith("synpred") && !rule.toLowerCase().endsWith("predicate")) {
                assertNotNull(NodeType.valueOf(rule));
            }
        }
    }

    //checks if there's an existing rule in Css3Parser for each declared NodeType
    public void testNoUnusedNodeTypes() {
        Set<String> ruleNames = new HashSet<>(Arrays.asList(Css3Parser.ruleNames));
        Set<String> specialRuleNames = new HashSet<>(Arrays.asList(
                new String[]{"root", "error", "recovery", "token"}));
        for (NodeType nodeType : NodeType.values()) {
            String nodeTypeName = nodeType.name();
            assertTrue(String.format("Unused NodeType.%s", nodeTypeName),
                    ruleNames.contains(nodeTypeName) || specialRuleNames.contains(nodeTypeName));
        }
    }

    public void testErrorRecoveryInRule() throws ParseException, BadLocationException {
        //resync the parser to the last right curly bracket
        String code = "myns|h1  color: red; } h2 { color: blue; }";

        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);

        assertNotNull(NodeUtil.query(res.getParseTree(),
                "styleSheet/body/bodyItem|1/rule/declarations/declaration/propertyDeclaration/property/color"));
    }

    public void testErrorRecoveryInsideDeclaration() throws ParseException, BadLocationException {
        //recovery inside declaration rule, resyncing to next semicolon or right curly brace
        String code = "a {\n"
                + " s  red; \n"
                + " background: red; \n"
                + "}";

        CssParserResult res = TestUtil.parse(code);

        //one error:
        //DefaultError[Unexpected token IDENT found, Unexpected token IDENT found, ERROR] (file:null, from:8, to:11)
        assertResult(res, 1);
//        TestUtil.dumpResult(res);

        //the background: red; declaration is properly parsed even if the previous declaration is broken
        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + "rule/declarations/declaration|1/propertyDeclaration/property/background"));

    }

    public void testErrorRecoveryGargabeBeforeDeclaration() throws ParseException, BadLocationException {
        //recovery before entering declaration rule, the Parser.syncToIdent() is used to skip until ident is found

        String code = "a {\n"
                + " % color: red; \n"
                + " background: red; \n"
                + "}";

        CssParserResult res = TestUtil.parse(code);
        TestUtil.dumpResult(res);

        assertResult(res, 1);

        //the garbage char % is skipped by Parser.syncToIdent()
        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + "rule/declarations/declaration|0/propertyDeclaration/property/color"));
        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + "rule/declarations/declaration|1/propertyDeclaration/property/background"));

    }

    public void testSimpleValidCode() throws ParseException, BadLocationException {
        String code = "a {"
                + "color : black;"
                + "}";

        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);
        assertResult(res, 0);

        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + "rule/declarations/declaration/propertyDeclaration/property/color"));

    }

    public void testValidCode() throws ParseException, BadLocationException {
        String code = "a {\n"
                + "color : black; \n"
                + "background: red; \n"
                + "}\n\n"
                + ".class { }\n"
                + "#id { }";

        CssParserResult res = TestUtil.parse(code);
        assertResult(res, 0);

        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + "rule/declarations/declaration|0/propertyDeclaration/property/color"));
        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + "rule/declarations/declaration|1/propertyDeclaration/property/background"));

    }

    public void testParseTreeOffsets() throws ParseException, BadLocationException {
        String code = "/* comment */ body { color: red; }";
        //             01234567890123456789
        //             0         1

        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);
        assertResult(res, 0);

        Node aNode = NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + "rule/selectorsGroup/selector/simpleSelectorSequence/typeSelector/elementName/body");

        assertNotNull(aNode);
        assertTrue(aNode instanceof TokenNode);

        assertEquals("body", aNode.name());
        assertEquals(NodeType.token, aNode.type());

        assertEquals("body".length(), aNode.name().length());
        assertEquals(14, aNode.from());
        assertEquals(18, aNode.to());
    }

    public void testNamespacesInSelector() throws ParseException, BadLocationException {
        CssParserResult res = assertResultOK(TestUtil.parse("myns|h1 { color: red; }"));
//        NodeUtil.dumpTree(res.getParseTree());

        String typeSelectorPath = "rule/selectorsGroup/selector/simpleSelectorSequence/typeSelector/";

        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + typeSelectorPath + "namespacePrefix/namespacePrefixName/myns"));
        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + typeSelectorPath + "elementName/h1"));

        res = assertResultOK(TestUtil.parse("*|h1 { color: red; }"));

        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + typeSelectorPath + "namespacePrefix/*"));
        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + typeSelectorPath + "elementName/h1"));

        res = assertResultOK(TestUtil.parse("*|* { color: red; }"));

        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + typeSelectorPath + "namespacePrefix/*"));
        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + typeSelectorPath + "elementName/*"));
    }

    public void testNamespacesInAttributes() throws ParseException, BadLocationException {
        CssParserResult res = assertResultOK(TestUtil.parse("h1[myns|attr=val] { color: red; }"));
//        NodeUtil.dumpTree(res.getParseTree());

        String simpleSelectorPath = "rule/selectorsGroup/selector/simpleSelectorSequence/";

        assertNotNull(NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + simpleSelectorPath + "typeSelector/elementName/h1"));

        Node attribNode = NodeUtil.query(res.getParseTree(),
                TestUtil.bodysetPath + simpleSelectorPath + "elementSubsequent/slAttribute");

        assertNotNull(attribNode);
        assertNotNull(NodeUtil.query(attribNode,
                "namespacePrefix/namespacePrefixName/myns"));

        assertNotNull(NodeUtil.query(attribNode,
                "slAttributeName/attr"));

        assertNotNull(NodeUtil.query(attribNode,
                "slAttributeValue/val"));

    }

    public void testNodeImages() throws ParseException, BadLocationException {
        String selectors = "#id .class body";
        String code = selectors + " { color: red}";
        CssParserResult res = TestUtil.parse(code);
//        dumpResult(res);

        String selectorsGroupPath = "rule/selectorsGroup";

        //test rule node image
        Node selectorsGroup = NodeUtil.query(res.getParseTree(), TestUtil.bodysetPath + selectorsGroupPath);
        assertNotNull(selectorsGroup);

        assertEquals(selectors, selectorsGroup.image().toString());

        //test root node image
        assertEquals(code, res.getParseTree().image().toString());

        //test token node image
        Node id = NodeUtil.query(selectorsGroup, "selector/simpleSelectorSequence/elementSubsequent/cssId/#id");
        assertNotNull(id);
        assertTrue(id instanceof TokenNode);
        assertEquals("#id", id.image().toString());

    }

    public void testCommon() throws ParseException, BadLocationException {
        String code = "#id .class body { color: red}     body {}";
        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);
        assertResult(res, 0);
    }

    public void testMedia() throws ParseException, BadLocationException {
        String code = "@media screen { h1 { color: red; } }";
        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);
        assertResult(res, 0);
    }

    public void testRootNodeSpan() throws ParseException, BadLocationException {
        String code = "   h1 { }    ";
        //             012345678901234
        CssParserResult res = TestUtil.parse(code);
//        TestUtil.dumpResult(res);

        Node root = res.getParseTree();
        assertEquals(0, root.from());
        assertEquals(code.length(), root.to());
    }

    public void testImport() throws ParseException, BadLocationException {
        String code = "@import \"file.css\";";
        CssParserResult res = TestUtil.parse(code);

//        TestUtil.dumpResult(res);
        Node imports = NodeUtil.query(res.getParseTree(), "styleSheet/imports");
        assertNotNull(imports);

        //url form
        code = "@import url(\"file.css\");";
        res = TestUtil.parse(code);

//        TestUtil.dumpResult(res);
        imports = NodeUtil.query(res.getParseTree(), "styleSheet/imports");
        assertNotNull(imports);

    }

    public void testErrorCase1() throws BadLocationException, ParseException, FileNotFoundException {
        String code = "h1 { color:  }";
        CssParserResult res = TestUtil.parse(code);

        //Test whether all the nodes are properly intialized - just dump the tree.
        //There used to be a bug that error token caused some rule
        //nodes not having first token set properly by the NbParseTreeBuilder
        NodeUtil.dumpTree(res.getParseTree(), new PrintWriter(new StringWriter()));

//        NodeUtil.dumpTree(res.getParseTree());
    }

    public void testErrorCase2() throws BadLocationException, ParseException, FileNotFoundException {
        String code = "a { color: red; } ";

        CssParserResult res = TestUtil.parse(code);

//        NodeUtil.dumpTree(res.getParseTree());
        assertResult(res, 0);

    }

    public void testErrorCase_emptyDeclarations() throws BadLocationException, ParseException, FileNotFoundException {
        String code = "h1 {}";

        CssParserResult res = TestUtil.parse(code);

        //syncToIdent bug - it cannot sync to ident since there isn't one - but the case is valid
        //=> reconsider putting syncToIdent back to the declarations rule, but then I need
        //to resolve why it is not called even in proper cases!!!
//        NodeUtil.dumpTree(res.getParseTree());
        AtomicBoolean recoveryNodeFound = new AtomicBoolean(false);
        NodeVisitor<AtomicBoolean> visitor = new NodeVisitor<AtomicBoolean>(recoveryNodeFound) {

            @Override
            public boolean visit(Node node) {
                if (node.type() == NodeType.recovery) {
                    getResult().set(true);
                    return true;
                }
                return false;
            }
        };

        visitor.visitChildren(res.getParseTree());

        assertResult(res, 0);

        assertFalse(recoveryNodeFound.get());

        //this doesn't work actually, the resyncing to ident doesn't work naturally
    }

    //issue #160780
    public void testFatalParserError() throws ParseException, BadLocationException {
        //fatal parse error on such input
        String content = "@charset";

        CssParserResult result = TestUtil.parse(content);
        assertNotNull(result.getParseTree());
        assertEquals(1, result.getDiagnostics().size());
    }

    public void testCharsetParsing() throws ParseException, BadLocationException {
        String content = "@charset \"iso-8859-1\";\n h1 { color: red; }";

        CssParserResult result = TestUtil.parse(content);
        assertResult(result, 0);
    }

    public void testErrorCase4() throws ParseException, BadLocationException {
        String content = "h1 { color: ;}";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpResult(result);

        assertResult(result, 1);
    }

    public void testIdParsing() throws ParseException, BadLocationException {
        String content = "h1 #myid { color: red }";

        CssParserResult result = TestUtil.parse(content);
        assertResult(result, 0);
//        TestUtil.dumpResult(result);

        Node id = NodeUtil.query(result.getParseTree(), TestUtil.bodysetPath + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/cssId");
        assertNotNull(id);

        assertEquals(NodeType.cssId, id.type());

    }

    public void testErrorRecoveryBetweenrules() throws ParseException, BadLocationException {
        String content = "h1 { color: red} ; h2 { color: blue }";
        //                                 ^ -- semicolon not allowed here

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpResult(result);

        //commented out since it currently fails
        //assertResult(result, 0);
    }

    public void testErrorCase5() throws ParseException, BadLocationException {
        String content = "a { }   m { }";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpResult(result);

        assertResult(result, 0);
    }

    public void testMSFunction() throws ParseException, BadLocationException {
        //Microsoft css extension allows following code:
        String content = "a {"
                + "filter: progid:DXImageTransform.Microsoft.gradient("
                + "startColorstr='#bdb1a0', endColorstr='#958271',GradientType=0 ); /* IE6-9 */"
                + "   color: red;"
                + "}";

        CssParserResult result = TestUtil.parse(content);
        TestUtil.dumpResult(result);

        assertNotNull(NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath + "rule/declarations/declaration|0/propertyDeclaration/property/filter"));

        Node function = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath + "rule/declarations/declaration|0/propertyDeclaration/propertyValue/expression/term/function");
        assertNotNull(function);

        Node functionName = NodeUtil.query(function, "functionName");
        assertNotNull(functionName);
        assertEquals("progid:DXImageTransform.Microsoft.gradient", functionName.image().toString());

        Node attr = NodeUtil.query(function, "fnAttributes/fnAttribute|0");
        assertNotNull(attr);

        Node attrName = NodeUtil.query(attr, "fnAttributeName");
        assertNotNull(attrName);
        assertEquals("startColorstr", attrName.image().toString());

        Node attrVal = NodeUtil.query(attr, "fnAttributeValue");
        assertNotNull(attrVal);
        assertEquals("'#bdb1a0'", attrVal.image().toString());

        assertResult(result, 0);

    }

    public void testNamespaceDeclaration() throws ParseException, BadLocationException {
        String content = "@namespace prefix \"url\";";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpResult(result);

        Node ns = NodeUtil.query(result.getParseTree(),
                "styleSheet/namespaces/namespace");
        assertNotNull(ns);

        Node prefix = NodeUtil.query(ns, "namespacePrefixName");
        assertNotNull(prefix);
        assertEquals("prefix", prefix.image().toString());

        Node res = NodeUtil.query(ns, "resourceIdentifier");
        assertNotNull(res);
        assertEquals("\"url\"", res.image().toString());

        assertResult(result, 0);
    }

    public void testErrorCase7() throws ParseException, BadLocationException {
        String content = "h1[ $@# ]{ }";
        //                012345678

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        Node error = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/slAttribute/slAttributeName/error");
        assertNotNull(error);

        assertEquals(4, error.from());
        assertEquals(5, error.to());
        assertEquals("$", error.image().toString());

    }

    public void testErrorCase8() throws ParseException, BadLocationException {
        String content = "h1[f { }";
        //                01234567

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        Node error = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/slAttribute/error");
        assertNotNull(error);
        assertEquals(5, error.from());
        assertEquals(6, error.to());
        assertEquals("{", error.image().toString());

        content = "h1[foo=] { }";
        //         012345678
        result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        error = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/slAttribute/slAttributeValue/error");
        assertNotNull(error);
        assertEquals(7, error.from());
        assertEquals(8, error.to());
        assertEquals("]", error.image().toString());

    }

    public void testErrorCase9() throws ParseException, BadLocationException {
        String content = "h1[foo|attr=val,]";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        Node error = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/slAttribute/error");
        assertNotNull(error);
        assertEquals(15, error.from());
        assertEquals(17, error.to());

        //premature end of file
        Node error2 = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "rule/error");
        assertNotNull(error2);
        assertEquals(content.length(), error2.from());
        assertEquals(content.length(), error2.to());

        assertResult(result, 2);

    }

    public void testPseudoClasses() throws ParseException, BadLocationException {
        String content = "div:enabled { }";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);
        Node pseudo = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/pseudo");
        assertNotNull(pseudo);
        assertEquals(":enabled", pseudo.image().toString());

        assertResultOK(result);

        content = "div:nth-child(even) { }";

        result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);
        pseudo = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/pseudo");
        assertNotNull(pseudo);
        assertEquals(":nth-child(even)", pseudo.image().toString());
        assertResultOK(result);

    }

    public void testPseudoElements() throws ParseException, BadLocationException {
        String content = "div::before { }";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);
        Node pseudo = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/pseudo");
        assertNotNull(pseudo);
        assertEquals("::before", pseudo.image().toString());

        assertResultOK(result);

    }

    public void testErrorCase10() throws ParseException, BadLocationException {
        String content = "p { color: hsl(10, }";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);
        Node error = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "rule/declarations/declaration/propertyDeclaration/propertyValue/expression/term/function/error");
        assertNotNull(error);

    }

    public void testParseURL() throws ParseException, BadLocationException {
        String content = "p { background-image: url(flower.png); }";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);
        assertResultOK(result);

    }

    public void testMediaQueries() throws ParseException, BadLocationException {
        String content = "@media screen and (device-aspect-ratio: 2560/1440) { p { } }";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        Node media_query = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "at_rule/media/mediaQueryList/mediaQuery");
        assertNotNull(media_query);

        Node media_type = NodeUtil.query(media_query, "mediaType");
        assertNotNull(media_type);
        assertEquals("screen", media_type.image().toString());

        Node media_expression = NodeUtil.query(media_query, "mediaExpression");
        assertNotNull(media_expression);

        Node media_feature = NodeUtil.query(media_expression, "mediaFeature");
        assertNotNull(media_feature);

        assertResultOK(result);

    }

    public void testMediaQueries2() throws ParseException, BadLocationException {
        assertResultOK(TestUtil.parse("@media tv and (scan: progressive) { p {} }"));
        assertResultOK(TestUtil.parse("@media print and (min-resolution: 118dpcm) { p {} }"));
        assertResultOK(TestUtil.parse("@media all and (min-monochrome: 1) { p {} }"));
        assertResultOK(TestUtil.parse("@media screen and (min-width: 400px) and (max-width: 700px) { p {} }"));
        assertResultOK(TestUtil.parse("@media handheld and (min-width: 20em), screen and (min-width: 20em) { p {} }"));
        assertResultOK(TestUtil.parse("@media (orientation: portrait) { p {} }"));
    }

    public void testPagedMedia() throws ParseException, BadLocationException {
        String content = "@page test:first {"
                + "color: green;"
                + "@top-left {"
                + "content: \"foo\";"
                + "color: blue;"
                + "};"
                + "@top-right {"
                + "content: \"bar\";"
                + "} "
                + "}";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        assertResultOK(result);

        //test page node
        Node page = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "at_rule/page");
        assertNotNull(page);

        //pseudo page
        Node pseudoPage = NodeUtil.query(page,
                "pseudoPage");
        assertNotNull(pseudoPage);
        assertEquals(":first", pseudoPage.image().toString());

        //declaration
        Node declaration = NodeUtil.query(page,
                "propertyDeclaration");
        assertNotNull(declaration);
        assertEquals("color: green", declaration.image().toString());

        //margin
        Node margin = NodeUtil.query(page,
                "margin");
        assertNotNull(margin);

        //margin symbol
        Node margin_sym = NodeUtil.query(margin, "margin_sym");
        assertNotNull(margin_sym);
        assertEquals("@top-left", margin_sym.image().toString());

        //declaration in the margin body
        Node declarationInMargin = NodeUtil.query(margin, "declarations/declaration/propertyDeclaration");
        assertNotNull(declarationInMargin);

        //next margin
        Node margin2 = NodeUtil.query(page,
                "margin|1");
        assertNotNull(margin2);

        //next margin symbol
        Node margin_sym2 = NodeUtil.query(margin2, "margin_sym");
        assertNotNull(margin_sym2);
        assertEquals("@top-right", margin_sym2.image().toString());

        assertNotNull(NodeUtil.query(margin2, "declarations/declaration/propertyDeclaration"));

    }

    public void testPagedMediaWithoutPseudoClass() throws ParseException, BadLocationException {
        String content = "@page test {"
                + "color: green;"
                + "}";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        assertResultOK(result);

        //test page node
        Node page = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "at_rule/page");
        assertNotNull(page);

        //declaration
        Node declaration = NodeUtil.query(page,
                "propertyDeclaration");
        assertNotNull(declaration);
        assertEquals("color: green", declaration.image().toString());

    }
    
    public void testSimpleSupports() throws ParseException, BadLocationException {
        String content = "@supports ( display: block ) {\n" 
                + "#content { background: white; color: black; }\n" 
                + "}";
        
        CssParserResult result = TestUtil.parse(content);
        //TestUtil.dumpTokens(result);
        //TestUtil.dumpResult(result);

        assertResultOK(result);
        
        //test supportsAtRule node
        Node supportsAtRule = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "at_rule/supportsAtRule");
        assertNotNull(supportsAtRule);
        
        //supportsCondition
        Node supportsCondition = NodeUtil.query(supportsAtRule,
                "supportsCondition");
        assertNotNull(supportsCondition);
        
        //supportsInParens
        Node supportsInParens = NodeUtil.query(supportsCondition,
                "supportsInParens");
        assertNotNull(supportsInParens);
        assertEquals("( display: block )", supportsInParens.image().toString());
        
        //supportsFeature
        Node supportsFeature = NodeUtil.query(supportsInParens,
                "supportsFeature");
        assertNotNull(supportsFeature);
        
        //supportsDecl
        Node supportsDecl = NodeUtil.query(supportsFeature,
                "supportsDecl");
        assertNotNull(supportsDecl);
        
        //declaration
        Node declaration = NodeUtil.query(supportsDecl,
                "declaration");
        assertNotNull(declaration);
        
        //propertyDeclaration
        Node propertyDeclaration = NodeUtil.query(declaration,
                "propertyDeclaration");
        assertNotNull(propertyDeclaration);
        assertEquals("display: block", propertyDeclaration.image().toString());
        
        //mediaBody
        Node mediaBody = NodeUtil.query(supportsAtRule,
                "mediaBody");
        assertNotNull(mediaBody);
        
        //mediaBodyItem
        Node mediaBodyItem = NodeUtil.query(mediaBody,
                "mediaBodyItem");
        assertNotNull(mediaBodyItem);
        
        //rule
        Node rule = NodeUtil.query(mediaBodyItem,
                "rule");
        assertNotNull(rule);
        assertEquals("#content { background: white; color: black; }", 
                rule.image().toString());
        
    }
    
    public void testSupportsNegation() throws ParseException, BadLocationException {
        String content = "@supports not ( display: inline ) {\n" 
                + "body { width: 100% }\n" 
                + "}";
        
        CssParserResult result = TestUtil.parse(content);
        // TestUtil.dumpTokens(result);
        //TestUtil.dumpResult(result);
        
        assertResultOK(result);
        
        //test supportsCondition node
        Node supportsCondition = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "at_rule/supportsAtRule/supportsCondition");
        assertNotNull(supportsCondition);
        assertEquals("not ( display: inline )", 
                supportsCondition.image().toString());
        
    }
    
    public void testSupportsConjunction() throws ParseException, BadLocationException {
        String content = "@supports (display: table-cell) and \n" 
                + "(display: list-item) and \n" 
                + "(display:run-in) {\n" 
                + ".myclass {\n" 
                + "display: table-cell;\n" 
                + "}\n" 
                + "}";
        
        CssParserResult result = TestUtil.parse(content);
        // TestUtil.dumpTokens(result);
        //TestUtil.dumpResult(result);
        
        assertResultOK(result);
        
        //test supportsCondition node
        Node supportsCondition = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "at_rule/supportsAtRule/supportsCondition");
        assertNotNull(supportsCondition);
        assertEquals("(display: table-cell) and \n"
                + "(display: list-item) and \n"
                + "(display:run-in)", 
                supportsCondition.image().toString());
        
    }
    
    public void testSupportsDisjunction() throws ParseException, BadLocationException {
        String content = "@supports ( box-shadow: 2px lime ) or\n"
                + "( -o-transition: all ) or\n"
                + "( -moz-box-shadow: 2px lime ) {\n" 
                + ".outline {\n" 
                + "box-shadow: 2px lime;\n" 
                + "  }\n" 
                + "}";
        
        CssParserResult result = TestUtil.parse(content);
        // TestUtil.dumpTokens(result);
        TestUtil.dumpResult(result);
        
        assertResultOK(result);
        
        //test supportsCondition node
        Node supportsCondition = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "at_rule/supportsAtRule/supportsCondition");
        assertNotNull(supportsCondition);
        assertEquals("( box-shadow: 2px lime ) or\n"
                + "( -o-transition: all ) or\n"
                + "( -moz-box-shadow: 2px lime )", 
                supportsCondition.image().toString());
        
    }
    
    public void testSupportsMixedConjunctionDisjunction() throws ParseException, BadLocationException {
        String invalidCss = "@supports (color: lightseagreen) or (color: lawngreen) and (color: hotpink) {}";
        String validCss1 = "@supports ((color: lightseagreen) or (color: lawngreen)) and (color: hotpink) {}";
        String validCss2 = "@supports (color: lightseagreen) or ((color: lawngreen) and (color: hotpink)) {}";
        
        CssParserResult result = TestUtil.parse(invalidCss);
        assertTrue(result.getParserDiagnostics().size() > 0);
        
        assertParses(validCss1, true);
        assertParses(validCss2);
        
    }
    
    public void testSupportsWhitespace() throws ParseException, BadLocationException {
        String invalidCss1 = "@supports not(color: lightseagreen)";
        String invalidCss2 = "@supports (color: lightseagreen)or (color: lawngreen) {}";
        String invalidCss3 = "@supports (color: lightseagreen) or(color: lawngreen) {}";
        String invalidCss4 = "@supports (color: lawngreen)and (color: hotpink) {}";
        String invalidCss5 = "@supports (color: lawngreen) and(color: hotpink) {}";
        
        CssParserResult result1= TestUtil.parse(invalidCss1);
        CssParserResult result2= TestUtil.parse(invalidCss2);
        CssParserResult result3= TestUtil.parse(invalidCss3);
        CssParserResult result4= TestUtil.parse(invalidCss4);
        CssParserResult result5= TestUtil.parse(invalidCss5);
        
        assertTrue(result1.getParserDiagnostics().size() > 0);
        assertTrue(result2.getParserDiagnostics().size() > 0);
        assertTrue(result3.getParserDiagnostics().size() > 0);
        assertTrue(result4.getParserDiagnostics().size() > 0);
        assertTrue(result5.getParserDiagnostics().size() > 0);
        
    }

    public void testSupportsFunctions() throws Exception {
        assertParses("@supports selector(h2 > p) {}");
        assertParses("@supports font-tech(color-COLRv1) {}");
        assertParses("@supports font-format(opentype) {}");
        assertParses("@supports font-format(opentype) and selector(h2 > p) {}");
        assertParses("@supports font-format(opentype) and (color: hotpink) {}");
    }

    public void testCounterStyle() throws ParseException, BadLocationException {
        String content = "@counter-style cool { glyph: '|'; }";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        Node counterStyle = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "at_rule/counterStyle");
        assertNotNull(counterStyle);

        Node ident = NodeUtil.getChildTokenNode(counterStyle, CssTokenId.IDENT);
        assertNotNull(ident);
        assertEquals("cool", ident.image().toString());

        Node declaration = NodeUtil.query(counterStyle, "declarations/declaration/propertyDeclaration");
        assertNotNull(declaration);
        assertEquals("glyph: '|'", declaration.image().toString());

        assertResultOK(result);

    }

    public void testSubstringMatchingAttributeSelectors() throws BadLocationException, ParseException {
        assertResultOK(TestUtil.parse("p[class$=\"st\"]{ } "));
        assertResultOK(TestUtil.parse("p[class*=\"st\"]{ } "));
        assertResultOK(TestUtil.parse("p[class^=\"st\"]{ } "));
    }

    public void testFontFace() throws ParseException, BadLocationException {
        String content = "@font-face { font-family: Gentium; src: url(http://example.com/fonts/Gentium.ttf); }";

        CssParserResult result = TestUtil.parse(content);
//        TestUtil.dumpTokens(result);
//        TestUtil.dumpResult(result);

        Node counterStyle = NodeUtil.query(result.getParseTree(),
                TestUtil.bodysetPath
                + "at_rule/fontFace");
        assertNotNull(counterStyle);

        Node declaration = NodeUtil.query(counterStyle, "declarations/declaration|0/propertyDeclaration");
        assertNotNull(declaration);
        assertEquals("font-family: Gentium", declaration.image().toString());

        assertResultOK(result);

    }

    public void testNetbeans_Css() throws ParseException, BadLocationException, IOException {
        CssParserResult result = TestUtil.parse(getTestFile("testfiles/netbeans.css"));
//        TestUtil.dumpResult(result);
        assertResult(result, 0);
    }

    public void testPropertyValueAsFunction() throws BadLocationException, ParseException {
        String code = "div { animation: cubic-bezier(1,2,3,4); } ";
        assertResultOK(TestUtil.parse(code));
    }

    public void testIssue203573() throws ParseException, BadLocationException, IOException {
        String code = "h1 { color:red; - }";
        CssParserResult result = TestUtil.parse(code);

//        NodeUtil.dumpTree(result.getParseTree());
        assertNoTokenNodeLost(result);
    }

    public void testParserRecovery_Issue203579() throws BadLocationException, ParseException {
        String code = "p {} div {}";
        CssParserResult result = TestUtil.parse(code);

        NodeUtil.dumpTree(result.getParseTree());

        Node node = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem|0/"
                + "rule/selectorsGroup/selector/simpleSelectorSequence/typeSelector/elementName");
        assertNotNull(node);
        assertFalse(NodeUtil.containsError(node));

    }

//    public void testParserRecovery_Issue203579_class_fails() throws BadLocationException, ParseException {
//        String code = ".{} ";
//        CssParserResult result = TestUtil.parse(code);
//
////        NodeUtil.dumpTree(result.getParseTree());
//
//        Node node = NodeUtil.query(result.getParseTree(),
//                "styleSheet/body/bodyItem/"
//                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/cssClass");
//        assertNotNull(node);
//        assertTrue(NodeUtil.containsError(node));
//
//    }
//
//    public void testParserRecovery_Issue203579_id_fails() throws BadLocationException, ParseException {
//        String code = "#{} ";
//        CssParserResult result = TestUtil.parse(code);
//
////        NodeUtil.dumpTree(result.getParseTree());
//
//        //fails due to the scss_interpolation_expression rule being applied to the #{} input as it conforms
//        //the semantic predicate looking for "#{"
//        //Can possibly be fixed by extending the predicate by #{$ as AFAIK the SASS variable is always
//        //present in the interpolation expression
//
//        Node node = NodeUtil.query(result.getParseTree(),
//                "styleSheet/body/bodyItem/"
//                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/cssClass");
//        assertNotNull(node);
//        assertTrue(NodeUtil.containsError(node));
//
//    }
    public void testMSExpression() throws BadLocationException, ParseException {
        String code
                = "div {"
                + "zoom:expression(runtimeStyle.zoom = 1, insertAdjacentHTML('beforeEnd', '<u class=\"after\"></u>'));"
                + "}";

        CssParserResult result = TestUtil.parse(code);
//        TestUtil.dumpResult(result);

        assertResultOK(result);

    }

    public void testMozDocumentAtRule() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse(
                "@-moz-document url(http://www.w3.org/),  "
                + "url-prefix(http://www.w3.org/Style/),  "
                + "domain(mozilla.org),  "
                + "regexp(\"^https:.*\") { div { color: red; } }");

        assertResultOK(result);
//        NodeUtil.dumpTree(result.getParseTree());
        assertNotNull(NodeUtil.query(result.getParseTree(), "styleSheet/body/bodyItem/at_rule/vendorAtRule/moz_document"));
    }

    //Bug 204128 - CC stops work after # in a color attribute
    public void testErrorRecoveryAfterHash() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse(
                "#test {\n"
                + "color: #\n"
                + "\n"
                + "   }\n"
                + "div { color: red; }\n");

//        TestUtil.dumpResult(result);
//        Node node = NodeUtil.query(result.getParseTree(),
//                "styleSheet/body/bodyItem/"
//                + "rule/declarations/declaration/propertyValue/error");
        Node node = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem/"
                + "rule/declarations/declaration/propertyDeclaration/propertyValue/expression/term/error");
        assertNotNull(node);
        assertEquals(15, node.from());
        assertEquals(16, node.to());
    }

    public void testParsingOfAsterixOnly() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse("*     ");
        //                                       0123456
//        TestUtil.dumpResult(result);

        Node node = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem/"
                + "rule/error");
        assertNotNull(node);
        assertEquals(6, node.from());
        assertEquals(6, node.to());

    }

    public void testErrorInSelector() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse("h1[|");
//        TestUtil.dumpResult(result);

        assertResult(result, 1); //premature EOF

        Node node = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem/"
                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/slAttribute/error");
        assertNotNull(node);
        assertEquals(4, node.from());
        assertEquals(4, node.to());

    }

    public void testNoEmptyRuleNodesInTheParseTree() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse("*  ");
        AtomicBoolean foundEmptyRuleNode = new AtomicBoolean(false);
        NodeVisitor<AtomicBoolean> visitor = new NodeVisitor<AtomicBoolean>(foundEmptyRuleNode) {

            @Override
            public boolean visit(Node node) {
                if (node instanceof RuleNode) {
                    if (node.children().isEmpty()) {
                        getResult().set(true);
                        return true;
                    }
                }
                return false;
            }
        };
        visitor.visitChildren(result.getParseTree());
        assertFalse(foundEmptyRuleNode.get());
    }

    public void testDuplicatedErrors() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse(
                "head{\n"
                + "    background-image: uri(;"
                + "}");

        TestUtil.dumpResult(result);

        assertResult(result, 1);

    }

    public void testParseWSAfterImportantSym() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse(".green { "
                + "    background-color : lime ! important "
                + "}");
        assertResultOK(result);
    }

    public void testParse_nth_child() throws BadLocationException, ParseException {
        assertResultOK(TestUtil.parse("table.t1 tr:nth-child(-n+4) { background-color : red }"));
        assertResultOK(TestUtil.parse("table.t2 td:nth-child(3n+1) { background-color : red }"));
    }

    public void testParseNotInPseudo() throws BadLocationException, ParseException {
        assertResultOK(TestUtil.parse("a:not(a) {  }"));
    }

    public void testParseAsterixInPseudo() throws BadLocationException, ParseException {
        assertResultOK(TestUtil.parse("a:not(*) { }"));
    }

    public void testParseClassInPseudo() throws BadLocationException, ParseException {
        assertResultOK(TestUtil.parse("a:not(.t2) {  }"));
    }

    public void testParseAttribInPseudo() throws BadLocationException, ParseException {
        assertResultOK(TestUtil.parse("div.stub *|*:not([test]) { }"));
    }

    public void testParsePseudoClassInPseudo() throws BadLocationException, ParseException {
        assertResultOK(TestUtil.parse("p:not(:target) { }"));
    }

    public void testParsePipeWithoutPrefixInSelector() throws BadLocationException, ParseException {
        assertResultOK(TestUtil.parse("|testA {background-color : lime }"));
    }

    public void testParseNsPrefixedElementInPseudo() throws BadLocationException, ParseException {
        assertResultOK(TestUtil.parse("div.test *:not(a|p) {  }"));
    }

    //Bug 207080 - Insufficient CSS parser error recovery
    public void testIssue_207080() {
        String code = "#wrapper {\n"
                + "   height: 100%;\n"
                + "   #z-index: 200; \n"
                + "   color: red;\n"
                + "}\n"
                + "\n"
                + "#header {\n"
                + "}\n";
        CssParserResult result = TestUtil.parse(code);

//        TestUtil.dumpResult(result);
        //the unexpected colon error is in fact a bug as normally the sass/less constructs as nested rules
        //should not be allowed in plain css. But so far I haven't found any way how to combine semantic and syntactic predicates
        //(see the (rule)=>rule { declarationType = DeclarationType.BLOCK; } rule in the css3.g
        //it's ought to be {isCPSource()?} (rule)=>rule { declarationType = DeclarationType.BLOCK; } which doesn't seem to work
        assertResult(result, 1);

        //check if the color: red; is properly parsed, e.g. whether the error recover works
        //in the preceding erroneous declaration
        Node node = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem/"
                + "rule/declarations/declaration|2/propertyDeclaration");
        assertNotNull(node);

        //check if the #header rule is properly parsed
        node = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem|1/"
                + "rule/selectorsGroup/selector/simpleSelectorSequence/elementSubsequent/cssId");
        assertNotNull(node);

    }

    public void testGenericAtRule() {
        String code = "@-cool-rule spin { h2 { color: red; } }";
        CssParserResult result = TestUtil.parse(code);

        assertResultOK(result);

//        TestUtil.dumpResult(result);
        Node node = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem/at_rule/vendorAtRule/generic_at_rule");

        assertNotNull(node);

    }

    //Bug 211103 - Freezes on starting IDE at "Scanning project" for too long
    public void testIssue211103() throws IOException, ParseException, BadLocationException {
        FileObject file = getTestFile("testfiles/itabbar.css.testfile");
        //check if we can even parse the file w/o an infinite loop in the recovery
        TestUtil.parse(file);
    }

//    public void testRecoveryInBodySet() {
//        String code = "div { } ;@ a { } h1 { }";
//        CssParserResult result = TestUtil.parse(code);
//
//        assertResultOK(result);
//
//        TestUtil.dumpResult(result);
//
//        Node node = NodeUtil.query(result.getParseTree(),
//                "styleSheet/bodylist/bodyset/generic_at_rule");
//
//        assertNotNull(node);
//
//
//    }
    public void testWebkitKeyFrames() {
        String code = "@-webkit-keyframes spin { 40% {  left: 150px;  } from { left: 2px } }";
        //             012345678901234567890123456789012345678901234567890123456789
        //             0         1         2         3         4         5
        CssParserResult result = TestUtil.parse(code);

        assertResultOK(result);

//        TestUtil.dumpResult(result);
        Node wkf = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem/at_rule/vendorAtRule/webkitKeyframes");

        assertNotNull(wkf);

        Node atRuleName = NodeUtil.query(wkf, "atRuleId");
        assertNotNull(atRuleName);
        assertEquals("spin", atRuleName.image().toString());

        //block1
        Node block = NodeUtil.query(wkf, "webkitKeyframesBlock|0");
        Node selectors = NodeUtil.query(block, "webkitKeyframeSelectors");
        assertNotNull(selectors);
        assertEquals("40%", selectors.image().toString());

        Node declarations = NodeUtil.query(wkf, "webkitKeyframesBlock/declarations");
        assertNotNull(declarations);
        assertNotNull(NodeUtil.query(declarations, "declaration/propertyDeclaration/property"));
        assertNotNull(NodeUtil.query(declarations, "declaration/propertyDeclaration/propertyValue"));

        //block2
        block = NodeUtil.query(wkf, "webkitKeyframesBlock|1");
        selectors = NodeUtil.query(block, "webkitKeyframeSelectors");
        assertNotNull(selectors);
        assertEquals("from", selectors.image().toString());

        declarations = NodeUtil.query(wkf, "webkitKeyframesBlock/declarations");
        assertNotNull(declarations);
        assertNotNull(NodeUtil.query(declarations, "declaration/propertyDeclaration/property"));
        assertNotNull(NodeUtil.query(declarations, "declaration/propertyDeclaration/propertyValue"));

    }

    //http://en.wikipedia.org/wiki/CSS_filter#Star_hack
    //Bug 215168 - Netbeans doesn't know about CSS star hack
    public void testIEPropertyStarHack() throws ParseException, BadLocationException {
        //case #1 error appears before declarations grammar rule
        String source = ".aclass { *color: red; }";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertEquals(0, result.getDiagnostics().size());

        //case #2 - error happens in the declarations grammar rule
        source = ".aclass { padding: 2px; *color: red; }";
        result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertEquals(0, result.getDiagnostics().size());
    }

    public void testFromTextPropertyValueBug() {
        String source = ".x { background-image: -webkit-gradient(linear, 0 0, 0 100%, from(#149bdf), to(#0480be)) }";
        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertEquals(0, result.getDiagnostics().size());
    }

    //Bug 219587 - parsing error on .box:nth-child(4n - 2)
    public void testWSInExpression() throws ParseException, BadLocationException {
        String source = ".box:nth-child(4n - 2) { } ";
        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertEquals(0, result.getDiagnostics().size());

    }

    public void testDataURI() throws ParseException, BadLocationException {
        String source = "div.menu {"
                + "    background-image: url('data:image/png;base64,iVBORw0KGgoAA"
                + "AANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0l"
                + "EQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6"
                + "P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC');"
                + "}";

        CssParserResult result = TestUtil.parse(source);
//        NodeUtil.dumpTree(result.getParseTree());

        assertEquals(0, result.getDiagnostics().size());

        Node term = NodeUtil.query(result.getParseTree(),
                "styleSheet/body/bodyItem/rule/declarations/declaration/propertyDeclaration/propertyValue/expression/term");
        assertNotNull(term);

        Node uri = NodeUtil.getChildTokenNode(term, CssTokenId.URI);
        assertNotNull(uri);

    }

    //Bug 223809 - Incorrect CSS syntax error highlighting when @-ms-viewport rule is added
    public void testVendorAtRuleInMedia() throws ParseException, BadLocationException {
        String source = "@media screen and (max-width: 400px) {"
                + "  @-ms-viewport { width: 320px; }"
                + "  @-o-viewport {}"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertEquals(0, result.getDiagnostics().size());

    }

    //Just a partial hotfix for nested MQ
    //complete grammar is defined in: http://www.w3.org/TR/css3-conditional/#processing
    public void testNestedMediaQuery() throws ParseException, BadLocationException {
        String source = "@media print { \n"
                + "  #navigation { display: none }\n"
                + "  @media (max-width: 12cm) { \n"
                + "    .note { float: none }\n"
                + "  }\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

//        NodeUtil.dumpTree(result.getParseTree());
        assertEquals(0, result.getDiagnostics().size());

    }

    public void testCommaSeparatedPropertyValues() throws ParseException, BadLocationException {
        assertParses(".x { font-family: \"Myriad Pro\",\"Myriad Web\",\"Tahoma\",\"Helvetica\",\"Arial\",sans-serif; }");
    }

    public void testImportantSymbolJustAfterPropertyValue() throws ParseException, BadLocationException {
        assertParses(".x { z-index: 1000000!important; }");
    }

    //http://netbeans.org/bugzilla/show_bug.cgi?id=227880
    public void testFunctionArgumentWithMultipleTermsSeparatedByWS() throws ParseException, BadLocationException {
        assertParses(".test { background: -moz-linear-gradient(center top);\n }");
        assertParses(".test { background: -moz-linear-gradient(center, top);\n }");
        assertParses(".test { background: -moz-linear-gradient(center top, #f3f3f3, #dddddd);\n }");
//
//        assertParses(".test { background-image: linear-gradient(top left);\n }");
//
//        assertParses(".test { background-image: linear-gradient(top left, rgba(79,99,31,1) );\n }");
//
//        assertParses(".test { background-image: linear-gradient(top left, rgba(79,99,31,1) 0%);\n }");
//
//        assertParses(".test { background-image: linear-gradient(top left, rgba(79,99,31,1) 0%,rgba(47,67,33,1)\n"
//                + "44%,rgba(20,45,19,1) 100%);\n }");
    }

    public void testFunctionArgumentWithMultipleTermsSeparatedByWS2() throws ParseException, BadLocationException {
        assertParses(".test { background: -moz-linear-gradient(top,  #b02000 0%, #dc4a00 100%);\n"
                + "background: -webkit-gradient(linear, left top, left bottom,\n"
                + "color-stop(0%,#b02000), color-stop(100%,#dc4a00));\n"
                + "background: -webkit-linear-gradient(top,  #b02000 0%,#dc4a00 100%);\n"
                + "background: -o-linear-gradient(top,  #b02000 0%,#dc4a00 100%);\n"
                + "background: -ms-linear-gradient(top,  #b02000 0%,#dc4a00 100%);\n"
                //                + "background: linear-gradient(to bottom,  #b02000 0%,#dc4a00 100%); "
                + "}");
    }

    public void testJustOneDeclarationNotTerminatedBySemi() throws ParseException, BadLocationException {
        assertParses(".rcol {\n"
                + "width:249px\n"
                + "}\n"
                + ".clz {\n"
                + "color:red;\n"
                + "}");
    }

    public void testDeclarationsWithJustOneProperty() throws ParseException, BadLocationException {
        assertParses("a { color: red }");
    }

    public void testDeclarations() throws ParseException, BadLocationException {
        assertParses("a { color: red; font-weight: bold }");
    }

    public void testPropertyValueSeparatedByCommas() throws ParseException, BadLocationException {
        assertParses("div { font-family: fantasy,monospace; }");
    }

    public void testPageContext() throws ParseException, BadLocationException {
        assertParses("@page:left { margin-left: 2cm }");
    }

    public void testParseJustSemiInDeclarations() throws ParseException, BadLocationException {
        assertParses(".clz { ; }");
        assertParses(".clz { ; ; }");
        assertParses(".clz { ;;; ; }");
        assertParses(".clz { ;;; ; color: red }");
        assertParses(".clz { ;;; ; color: red; ; }");
        assertParses(".clz { color: red; ;; }");
    }

    public void testMaskFn() throws ParseException, BadLocationException {
        assertParses(".clz { filter: mask(); }");
    }

    public void testWSBeforeCommaInSelectorsList() throws ParseException, BadLocationException {
        assertParses(".tablenav .tablenav-pages a.disabled:hover ,\n"
                + ".tablenav .tablenav-pages a.disabled:active {\n"
                + "	cursor: default;\n"
                + "}");
    }

    public void testWSAfterPropertyName() throws ParseException, BadLocationException {
        assertParses(".clz { color: red }");
        assertParses(".clz { color : red }");
        assertParses(".clz { color   : red }");
    }

    public void testKeyframesWithNLAfterName() throws ParseException, BadLocationException {
        assertParses("@keyframes glow\n"
                + "{}");
    }

    public void testTemplatingMarksInDeclarations() throws ParseException, BadLocationException {
        assertParses(".clz { @@@ }");
        assertParses(".clz { @@@ @@@; @@@ @@@ @@@; @@@}");
        assertParses(".clz { @@@; @@@; @@@; }");
        assertParses(".clz { @@@; color: red; @@@ }");
        assertParses(".clz { color: red; @@@ @@@ }");
    }

    public void testIssue240881_01() throws ParseException, BadLocationException {
        assertParses(".css {}");
    }

    public void testIssue240881_02() throws ParseException, BadLocationException {
        assertParses(".less {}");
    }

    public void testParseVariable() {
        assertParses("h1 {--demoVar: 1em; margin: var(--demoVar);}");
        assertParses("h1 {--demoVar: 1em; margin: var(--demoVar, 3ex 2em);}");
        assertParses(":root {--primary-font: 'Arial', 'Helvetica', sans-serif;}");
        assertParses("h1 {--grid-gutter: ( var(--margins) * var(--spacing-unit));}");
        assertParses("h1 {--grid-gutter:;}");
    }

    public void testMathExpressionInFunction() {
        assertParses("div {\n"
                + "    padding: calc(1 * 1);\n"
                + "}");
    }

    public void testCalcVarCombination() {
        assertParses("div {width: calc(var(--widthC) + 2px);}");
        assertParses("div {width: calc(var(--grid-margin) - var(--cell-margin));}");
        assertParses("div {width: var(--demoVal, calc(var(--grid-margin) - var(--cell-margin)));}");
        assertParses(read(getTestFile("testfiles/google-chrome-css-custom-properties.css")));
    }
    
    public void testLessScssKeywordInCss() {
        assertParses(".and {}");
        assertParses(".or {}");
        assertParses(".not {}");
        assertParses(".only {}");
        assertParses(".when {}");
        assertParses(".less {}");
        assertParses(".css {}");
        assertParses(".reference {}");
        assertParses(".inline {}");
        assertParses(".once {}");
        assertParses(".multiple {}");
    }

    public void testIssue248270() {
        assertParses("video {\n"
                + "  display: inline-block;\n"
                + "  *display: inline;\n"
                + "  *zoom: 1;\n"
                + "}\n"
                + "\n"
                + "select[multiple],\n"
                + "select[size] {\n"
                + "  height: auto;\n"
                + "}");

        assertParses(".test {\n"
                + "  display: inline;\n"
                + "}");
    }
//    public void testTemplatingMarksInBody() throws ParseException, BadLocationException {
//        assertParses(" @@@ ");
//        assertParses(" @@@ .clz {  } @@@ ");
//        assertParses(" @@@ @@@ @@@ ");
//        assertParses(" @@@ @@@ .clz {  } ");
//    }
    //https://netbeans.org/bugzilla/show_bug.cgi?id=230042#c1
//    public void testIEExpressionHack_fails() throws ParseException, BadLocationException {
//        assertParses("div {\n"
//                + "     margin-top: expression(0 - parseInt(this.offsetHeight / 2) + (TBWindowMargin = document.documentElement && document.documentElement.scrollTop || document.body.scrollTop) + 'px');\n"
//                + "}");
//    }

    public void testMediaBodyMinimized() {
        String source = "@media (min-width: 940px){.demo-appheader-appname-block{margin-top:1px}.demo-appheader-classic .demo-appheader-logo{margin-top:37px}.demo-appheader-classic .demo-appheader-toolbar{height:34px}.demo-appheader-classic .demo-appheader-nav{height:50px}html:not([dir=\"rtl\"]){}}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }

    public void testCombinatorWithoutSpaces() {
        String source = "a.oj>.oj {}";
        CssParserResult result = TestUtil.parse(source);
        assertResultOK(result);
    }
    
    public void testLineCommentThrowsErrorInCSS() {
        String source = ".div {\n"
                + "    color: red\n"
                + "    // hello\n"
                + "    font-family: Ariel\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);
        assertEquals(1, result.getDiagnostics().size());
    }
    
    public void testNthChildInMedia(){
        assertParses("@media all and (min-width : 980px) {\n"
                + "    /* OK */\n"
                + "    div {\n"
                + "        color:#000;\n"
                + "    }\n"
                + "\n"
                + "    /* OK */\n"
                + "    div#withId {\n"
                + "        color:#000;\n"
                + "    }\n"
                + "\n"
                + "    /* Error - Unexpected dot found, Unexpected brace found */\n"
                + "    div:nth-child(1) {\n"
                + "        color:#000;\n"
                + "    }\n"
                + "\n"
                + "    /* Error - Unexpected dot found, Unexpected brace found */\n"
                + "    div.withClassAndNthChild:nth-child(1) {\n"
                + "        color:#000;\n"
                + "    }\n"
                + "\n"
                + "    /* Errror - Unexpected hash found, Unexpected brace found */\n"
                + "    div#withIdAndNthChild:nth-child(1) {\n"
                + "        color:#000;\n"
                + "    }\n"
                + "}");
    }
    
    public void testClassNameStartingWithNumber() {
        assertParses(".5hallo {\n"
                + "	color: #ff3366;\n"
                + "}");
    }

    public void testParseSelectorListCSS4() {
        assertParses("h1:not(.dummy) {}");
        assertParses("h1:not( .h2:visible ) {}");
        assertParses("h1:not( .h2, .h3:visible ) {}");
        assertParses("h1:not(.h2,:visible,h2) {}");
        assertParses("h1:not(.h2,:visible, h1 > h2) {}");
        assertParses("html|*:not(:link):not(:visited) {}");
        assertParses("html|*:not(:link, :visited) {}");
        assertParses("*|*:is(:hover, :focus) {}");
        assertParses("*|*:is(*:hover, *:focus) {}");
        assertParses("a:where(:valid, :unsupported) {}");
        assertParses("a:where(:not(:hover)) {text-decoration: none;}");
        assertParses("body:has(#aCheck:checked) {}");
        assertParses(":is(h1, h2, h3):has(+ :is(h2, h3, h4)) {}");
        assertParses(":is(h1, h2, h3):has(+ h2, + h3, + h4) {}");
    }

    public void testParsePage() {
        assertParses("@page {  @top-left { content: attr(test) }; color: red; @bottom-center { content: attr(test2) } }");
        assertParses("@page {  @top-left { content: attr(test) } }");
        assertParses("@page {  @top-left { content: attr(test); }; }");
        assertParses("@page {  ; @top-left { content: attr(test) } }");
        assertParses("@page {  @top-left { content: attr(test) } ; text-align: right }");
        assertParses("@page {  @top-left { content: attr(test) } text-align: right }");
        assertParses("@page {  @top-left { content: attr(test) } text-align: right; background-color: green }");
        assertParses("@page {  @top-center { content: attr(test) } @bottom-center { content: attr(test2) } background-color: red; }");
        assertParses("@page {  background-color: red; @top-center { content: attr(test) } @bottom-center { content: attr(test2) } }");
        assertParses("@page{@top-left{content: attr(test)}}");
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

    public void testParseContainer() throws Exception {
        assertParses("@container test style(--responsive: true) {}");
        assertParses("@container style(--responsive: true) {}");
        assertParses("@container my-layout (inline-size > 45em) {}");
        assertParses("@container my-layout (45em < inline-size) {}");
        assertParses("@container (--cards: small) {}");
        assertParses("@container (--cards: small) { h1 {background: green; border: 1px solid green; } }");
        assertParses("@container my-component-library (inline-size > 30em) {}");
        assertParses("@container card (inline-size > 30em) { @container style(--responsive: true) { } }");
        assertParses("@container (width < 30em) { }");
        assertParses("@container (20em < width < 30em) { }");
        assertParses("@container name (max-height: 780px) {}");
    }
}
