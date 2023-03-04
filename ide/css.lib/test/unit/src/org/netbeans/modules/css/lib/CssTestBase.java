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
package org.netbeans.modules.css.lib;

import java.io.PrintWriter;
import java.util.*;
import junit.framework.AssertionFailedError;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.netbeans.modules.css.lib.api.properties.GrammarElement;
import org.netbeans.modules.css.lib.api.properties.GrammarResolver;
import org.netbeans.modules.css.lib.api.properties.GroupGrammarElement;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.lib.api.properties.Token;
import org.netbeans.modules.css.lib.api.properties.ValueGrammarElement;
import org.netbeans.modules.css.lib.properties.GrammarParser;

/**
 *
 * @author marekfukala
 */
public class CssTestBase extends CslTestBase {

    protected static boolean PRINT_GRAMMAR_RESOLVE_TIMES = false;
    protected static boolean PRINT_INFO_IN_ASSERT_RESOLVE = false;

    public CssTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        CssParserResult.IN_UNIT_TESTS = true;
    }
    
    protected void setPlainSource() {
        TestUtil.setPlainSource();
    }
    
    protected void setScssSource() {
        TestUtil.setScssSource();
    }
    
    protected void setLessSource() {
        TestUtil.setLessSource();
    }
    
    protected Collection<GrammarResolver.Feature> getEnabledGrammarResolverFeatures() {
        return Collections.emptyList();
    }

    protected CssParserResult assertParses(String cssCode, boolean debug) {
        CssParserResult result = TestUtil.parse(cssCode);
        if(debug) {
            TestUtil.dumpResult(result);
        }
        assertResultOK(result);
        return result;
        
    }
    protected CssParserResult assertParses(String cssCode) {
        return assertParses(cssCode, false);
    }
    
    protected CssParserResult assertResultOK(CssParserResult result) {
        return assertResult(result, 0);
    }

    protected CssParserResult assertResult(CssParserResult result, int problems) {
        assertNotNull(result);
        assertNotNull(result.getParseTree());

        if (problems != result.getDiagnostics().size()) {
            TestUtil.dumpResult(result);
        }

        int foundProblemsCount = result.getDiagnostics().size();
        if(problems != foundProblemsCount) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected error(s):\n");
            for(Error e : result.getDiagnostics()) {
                sb.append(e.toString());
            }
            assertEquals(sb.toString(), problems, foundProblemsCount);            
        }
        

        if (foundProblemsCount == 0) {
            //Check whether the parse tree covers the whole file only if it is not broken. 
            //This doesn't mean an errorneous file should not produce parse tree
            //fully covering the source. Just there're some cases where it doesn't work now.
            //TODO: enable the parse tree tokens consistency check for all parse result, not just for the errorneous ones.
            assertNoTokenNodeLost(result);
        }

        return result;
    }

    /**
     * Checks whether the parser result covers every character in the source
     * code. In another words ensure there are no lexer tokens which doesn't
     * have a corresponding parse tree token node.
     * 
     * Now as we have css preprocessors and line comments it is bit more complicated.
     * The line comment tokens are ignored by the css parser and hence the parse tree
     * contains "holes". But this is legal now but *just for the line comment tokens!*
     */
    protected void assertNoTokenNodeLost(CssParserResult result) {
        final StringBuilder sourceCopy = new StringBuilder(result.getSnapshot().getText());
        
        //mark the code parts covered by the line comments as "seen"
        TokenHierarchy<?> tokenHierarchy = result.getSnapshot().getTokenHierarchy();
        TokenSequence<CssTokenId> ts = tokenHierarchy.tokenSequence(CssTokenId.language());
        ts.moveStart();
        while(ts.moveNext()) {
            if(ts.token().id() == CssTokenId.LINE_COMMENT) {
                for(int i = ts.offset(); i < ts.offset() + ts.token().length(); i++) {
                    sourceCopy.setCharAt(i, Character.MAX_VALUE);
                }
            }
        }

        NodeVisitor.visitChildren(result.getParseTree(), Collections.<NodeVisitor<Node>>singleton(new NodeVisitor<Node>() {

            @Override
            public boolean visit(Node node) {
                if (node.type() == NodeType.token) {
                    for (int i = node.from(); i < node.to(); i++) {
                        sourceCopy.setCharAt(i, Character.MAX_VALUE);
                    }
                }

                return false;
            }
        }));

        for (int i = 0; i < sourceCopy.length(); i++) {
            if (sourceCopy.charAt(i) != Character.MAX_VALUE) {
                assertTrue(String.format("No token node found for char '%s' at offset %s of the parser source.", sourceCopy.charAt(i), i), false);
            }
        }
    }

    protected ResolvedProperty assertResolve(PropertyDefinition propertyModel, String inputText) {
        return assertResolve(propertyModel, inputText, true);
    }
    
    protected ResolvedProperty assertResolve(PropertyDefinition propertyModel, String inputText, boolean expectedSuccess) {
        return assertResolve(propertyModel.getGrammarElement(null), inputText, expectedSuccess);
    }
    
    protected ResolvedProperty assertResolve(String grammar, String inputText) {
        return assertResolve(grammar, inputText, true);
    }

    protected ResolvedProperty assertNotResolve(String grammar, String inputText) {
        return assertResolve(grammar, inputText, false);
    }

    protected ResolvedProperty assertResolve(String grammar, String inputText, boolean expectedSuccess) {
        long a = System.currentTimeMillis();
        GroupGrammarElement tree = GrammarParser.parse(grammar);
        long b = System.currentTimeMillis();
        return assertResolve(tree, inputText, expectedSuccess);
    }

    protected ResolvedProperty assertResolve(GroupGrammarElement tree, String inputText) {
        return assertResolve(tree, inputText, true);
    }

    protected ResolvedProperty assertResolve(GroupGrammarElement tree, String inputText, boolean expectedSuccess) {

        long a = System.currentTimeMillis();
        
        ResolvedProperty pv = new ResolvedProperty(createGrammarResolver(tree), inputText);
        long c = System.currentTimeMillis();

        if (PRINT_INFO_IN_ASSERT_RESOLVE) {
            System.out.println("Tokens:");
            System.out.println(dumpList(pv.getTokens()));
            System.out.println("Grammar:");
            System.out.println(dumpGETree(tree));
        }
        if (PRINT_GRAMMAR_RESOLVE_TIMES) {
            System.out.println(String.format("Input '%s' resolved in %s ms.", inputText, c - a));
        }
//        if(pv.isResolved()) {
//            List<Token> unresolvedTokens = pv.getUnresolvedTokens();
//            assertTrue(unresolvedTokens.isEmpty());
//        }
        
        if (pv.isResolved() != expectedSuccess) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected parsing result");
            
            if(!pv.isResolved()) {
                sb.append(", tokens left:");
                List<Token> unresolvedTokens = pv.getUnresolvedTokens();
                for(Token t : unresolvedTokens) {
                    sb.append(t);
                    sb.append(',');
                }
            } else {
                sb.append('.');
            }
            assertTrue(sb.toString(), false);
        }

        return pv;
    }
    
    private GrammarResolver createGrammarResolver(GroupGrammarElement tree) {
        GrammarResolver grammarResolver = new GrammarResolver(tree);
        for(GrammarResolver.Feature feature : getEnabledGrammarResolverFeatures()) {
            grammarResolver.enableFeature(feature);
        }
        return grammarResolver;
    }

    protected void assertParseFails(String grammar, String inputText) {
        assertResolve(grammar, inputText, false);
    }

    protected void assertAlternatives(ResolvedProperty propertyValue, String... expected) {
        Set<ValueGrammarElement> alternatives = propertyValue.getAlternatives();
        Collection<String> alts = convert(alternatives);
        Collection<String> expc = new ArrayList<String>(Arrays.asList(expected));
        if (alts.size() > expc.size()) {
            alts.removeAll(expc);
            throw new AssertionFailedError(String.format("Found %s unexpected alternative(s): %s", alts.size(), toString(alts)));
        } else if (alts.size() < expc.size()) {
            expc.removeAll(alts);
            throw new AssertionFailedError(String.format("There're %s expected alternative(s) missing : %s", expc.size(), toString(expc)));
        } else {
            Collection<String> alts2 = new ArrayList<String>(alts);
            Collection<String> expc2 = new ArrayList<String>(expc);

            alts2.removeAll(expc);
            expc2.removeAll(alts);

            assertTrue(String.format("Missing expected: %s; Unexpected: %s", toString(expc2), toString(alts2)), alts2.isEmpty() && expc2.isEmpty());

        }
    }

    protected void assertAlternatives(String grammar, String input, String... expected) {
        GroupGrammarElement tree = GrammarParser.parse(grammar);
        GrammarResolver grammarResolver = createGrammarResolver(tree);
        ResolvedProperty pv = new ResolvedProperty(grammarResolver, input);
        assertAlternatives(pv, expected);
    }

    private Collection<String> convert(Set<ValueGrammarElement> toto) {
        Collection<String> x = new HashSet<String>();
        for (ValueGrammarElement e : toto) {
            x.add(e.getValue().toString());
        }
        return x;
    }

    private String toString(Collection<String> c) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> i = c.iterator(); i.hasNext();) {
            sb.append('"');
            sb.append(i.next());
            sb.append('"');
            if (i.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    protected String dumpList(Collection<?> col) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<?> itr = col.iterator(); itr.hasNext();) {
            sb.append('"');
            sb.append(itr.next());
            sb.append('"');
            if (itr.hasNext()) {
                sb.append(',');
                sb.append(' ');
            }
        }
        return sb.toString();
    }
    
     protected void dumpTree(org.netbeans.modules.css.lib.api.properties.Node node) {
        PrintWriter pw = new PrintWriter(System.out);
        dump(node, 0, pw);
        pw.flush();
    }

    private void dump(org.netbeans.modules.css.lib.api.properties.Node tree, int level, PrintWriter pw) {
        for (int i = 0; i < level; i++) {
            pw.print("    ");
        }
        pw.print(tree.toString());
        pw.println();
        for (org.netbeans.modules.css.lib.api.properties.Node c : tree.children()) {
            dump(c, level + 1, pw);
        }
    }

    protected static final String dumpGETree(GrammarElement ge) {
        StringBuilder result = new StringBuilder();
        dumpTree(result, ge, new ArrayList<>());
        return result.toString();
    }

    private static final void dumpTree(StringBuilder sb, GrammarElement ge, List<GrammarElement> parentList) {
        int level = parentList.size();
        if (ge instanceof GroupGrammarElement) {
            List<GrammarElement> newParentList = new ArrayList<>(parentList.size() + 1);
            newParentList.addAll(parentList);
            newParentList.add(ge);
            String heading = ge.toString();
            heading = heading.substring(0, heading.length() - 1);
            indentString(sb, level);
            sb.append(heading);
            if (ge.getName() != null) {
                sb.append("(").append(ge.getName()).append(") "); //NOI18N
            }

            sb.append('\n');
            if (!parentList.contains(ge)) {
                for (GrammarElement e : ((GroupGrammarElement) ge).elements()) {
                    dumpTree(sb, e, newParentList);
                }
                indentString(sb, level);
                sb.append("]\n");
            }
        } else {
            indentString(sb, level);
            sb.append(ge.toString());
            sb.append("\n");
        }
    }

    private static void indentString(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
    }
}
