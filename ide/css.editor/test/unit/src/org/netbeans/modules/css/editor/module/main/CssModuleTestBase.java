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
package org.netbeans.modules.css.editor.module.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import junit.framework.AssertionFailedError;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.properties.GrammarElement;
import org.netbeans.modules.css.lib.api.properties.GroupGrammarElement;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.lib.api.properties.Token;
import org.netbeans.modules.css.lib.api.properties.ValueGrammarElement;
import org.netbeans.modules.css.lib.properties.GrammarParser;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssModuleTestBase extends CslTestBase {

    protected static boolean PRINT_GRAMMAR_RESOLVE_TIMES = false;
    protected static boolean PRINT_INFO_IN_ASSERT_RESOLVE = false;

    protected void assertCssCode(String code) throws ParseException {
        Source source = Source.create(getDocument(code));
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult();
                assertNotNull(result);
                assertTrue(result instanceof CssParserResult);
                CssParserResult cssresult = (CssParserResult) result;
                Collection<? extends Error> errors = cssresult.getDiagnostics();
                if(errors.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for(Iterator<? extends Error> itr = errors.iterator(); itr.hasNext(); ) {
                        Error e = itr.next();
                        sb.append(e.getSeverity());
                        sb.append(" at ");
                        sb.append(e.getStartPosition());
                        sb.append(':');
                        sb.append(e.getDescription());
                        if(itr.hasNext()) {
                            sb.append(", ");
                        }
                    }
                    assertEquals(String.format("Unexpected errors found: %s.", sb), 0, errors.size());
                }
                
                
            }
        });
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
        ResolvedProperty pv = new ResolvedProperty(tree, inputText);
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
        if (pv.isResolved() != expectedSuccess) {
            StringBuilder msg = new StringBuilder();
            msg.append("Unexpected parsing result - ");
            if(expectedSuccess) {
                msg.append(" success expected but was failure.");
                msg.append(" Uresolved token(s): ");
                List<Token> unresolved = pv.getUnresolvedTokens();
                for(int i = unresolved.size() - 1; i >= 0; i--) {
                    String token = unresolved.get(i).image().toString();
                    msg.append(token);
                    if(i > 0) {
                        msg.append(",");
                    }
                }
                
            } else {
                msg.append(" failure expected but was success.");
            }
            assertTrue(msg.toString(), false);
            
        }

        return pv;
    }

    protected void assertParseFails(String grammar, String inputText) {
        assertResolve(grammar, inputText, false);
    }

    protected void assertAlternatives(ResolvedProperty propertyValue, String... expected) {
        Set<ValueGrammarElement> alternatives = propertyValue.getAlternatives();
        Collection<String> alts = convert(alternatives);
        Collection<String> expc = new ArrayList<>(Arrays.asList(expected));
        if (alts.size() > expc.size()) {
            alts.removeAll(expc);
            throw new AssertionFailedError(String.format("Found %s unexpected alternative(s): %s", alts.size(), toString(alts)));
        } else if (alts.size() < expc.size()) {
            expc.removeAll(alts);
            throw new AssertionFailedError(String.format("There're %s expected alternative(s) missing : %s", expc.size(), toString(expc)));
        } else {
            Collection<String> alts2 = new ArrayList<>(alts);
            Collection<String> expc2 = new ArrayList<>(expc);

            alts2.removeAll(expc);
            expc2.removeAll(alts);

            assertTrue(String.format("Missing expected: %s; Unexpected: %s", toString(expc2), toString(alts2)), alts2.isEmpty() && expc2.isEmpty());

        }
    }

    
    protected void assertAlternatives(GroupGrammarElement grammar, String input, String... expected) {
        ResolvedProperty pv = new ResolvedProperty(grammar, input);
        assertAlternatives(pv, expected);
    }

    private Collection<String> convert(Set<ValueGrammarElement> toto) {
        Collection<String> x = new HashSet<>();
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

    public static enum Match {

        EXACT, CONTAINS, EMPTY, NOT_EMPTY, DOES_NOT_CONTAIN, CONTAINS_ONCE;
    }

    public CssModuleTestBase(String name) {
        super(name);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new CssLanguage();
    }
    
    protected String getTopLevelSnapshotMimetype() {
        return getPreferredMimeType();
    }

    @Override
    protected String getPreferredMimeType() {
        return CssLanguage.CSS_MIME_TYPE;
    }

    protected CssEditorModule getCssModuleByClass(Class clazz) {
        for (CssEditorModule module : CssModuleSupport.getModules()) {
            if (module.getClass().equals(clazz)) {
                return module;
            }
        }
        return null;
    }

    /**
     *
     * @param declaration - in the form: "property: value" as in css rule
     */
    protected void assertPropertyDeclaration(String declaration) {
        //cut off the semicolon if present
        int semiIndex = declaration.indexOf(';');
        if (semiIndex >= 0) {
            declaration = declaration.substring(0, semiIndex);
        }

        int commaIndex = declaration.indexOf(':');
        assertTrue(commaIndex >= 0);

        String propertyName = declaration.substring(0, commaIndex);
        String propertyValue = declaration.substring(commaIndex + 1);

        assertPropertyValues(propertyName, propertyValue);
    }

    protected void assertPropertyValues(String propertyName, String... values) {

        PropertyDefinition model = Properties.getPropertyDefinition( propertyName);
        assertNotNull(String.format("Cannot find property %s", propertyName), model);

        for (String val : values) {
            assertNotNull(assertResolve(model.getGrammarElement(null), val));
        }

    }

    public void checkCC(String documentText, final String[] expectedItemsNames) throws ParseException {
        checkCC(documentText, expectedItemsNames, Match.EXACT);
    }

    public void checkCC(String documentText, final String[] expectedItemsNames, final Match type) throws ParseException {
        checkCC(documentText, expectedItemsNames, type, '|');
    }

    public void checkCC(String documentText, final String[] expectedItemsNames, final Match type, char caretChar) throws ParseException {
        StringBuilder content = new StringBuilder(documentText);

        final int pipeOffset = content.indexOf(Character.toString(caretChar));
        assertTrue(String.format("Missing pipe char - you forgot to define the caret position in the test code: '%s'", documentText), pipeOffset >= 0);

        //remove the pipe
        content.deleteCharAt(pipeOffset);
        Document doc = getDocument(content.toString());
        Source source = Source.create(doc);
        
        CssParserResult cssresult = TestUtil.parse(source, getTopLevelSnapshotMimetype());
        CodeCompletionHandler cc = getPreferredLanguage().getCompletionHandler();
        String prefix = cc.getPrefix(cssresult, pipeOffset, false);
        CodeCompletionResult ccresult = cc.complete(createContext(pipeOffset, cssresult, prefix));

        try {
            assertCompletionItemNames(expectedItemsNames, ccresult, type);
        } catch (junit.framework.AssertionFailedError afe) {
            System.out.println("AssertionFailedError debug information:");
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println("Caret offset: " + pipeOffset);
            System.out.println("Parse tree:");
            NodeUtil.dumpTree(cssresult.getParseTree());
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            throw afe;
        }

    }

    protected Document getDocumentForFileObject(FileObject file) {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
            if (ec == null) {
                return null;
            }
            return ec.openDocument();
        } catch (IOException e) {
            throw new AssertionError("Cann't load document", e);
        }

    }
    
    protected void setDocumentContent(Document document, String text) {
        try {
            document.remove(0, document.getLength());
            document.insertString(0, text, null);
        } catch (BadLocationException ex) {
            throw new AssertionError(null, ex);
        }
    }
    
    protected void assertCompletion(String documentContent, final Match type, String... expectedItemsNames) {
        Document doc = getDocument(documentContent);
        assertCompletion(doc, type, expectedItemsNames);
    }
    
    protected void assertCompletion(Document document, final Match type, String... expectedItemsNames) {
        try {
            String text = document.getText(0, document.getLength());
            int pipeOffset = text.indexOf(Character.toString('|'));
            assertTrue(String.format("Missing pipe char - you forgot to define the caret position in the test code: '%s'", text), pipeOffset >= 0);
            
            //remove the pipe
            document.remove(pipeOffset, 1);
            Source source = Source.create(document);
            
            CssParserResult cssresult = TestUtil.parse(source, getTopLevelSnapshotMimetype());
            CodeCompletionHandler cc = getPreferredLanguage().getCompletionHandler();
            String prefix = cc.getPrefix(cssresult, pipeOffset, false);
            CodeCompletionResult ccresult = cc.complete(createContext(pipeOffset, cssresult, prefix));
            
            try {
                assertCompletionItemNames(expectedItemsNames, ccresult, type);
            } catch (junit.framework.AssertionFailedError afe) {
                System.out.println("AssertionFailedError debug information:");
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                System.out.println("Caret offset: " + pipeOffset);
                System.out.println("Parse tree:");
                NodeUtil.dumpTree(cssresult.getParseTree());
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                
                throw afe;
            }
            
        } catch (ParseException | BadLocationException ex) {
            throw new AssertionError(null, ex);
        }
    }

    
    public void assertComplete(String documentText, String expectedDocumentText, final String itemToComplete) throws ParseException, BadLocationException {
        StringBuilder content = new StringBuilder(documentText);
        StringBuilder expectedContent = new StringBuilder(expectedDocumentText);

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0;
        final int expPipeOffset = expectedContent.indexOf("|");
        assert expPipeOffset >= 0;

        //remove the pipe
        content.deleteCharAt(pipeOffset);
        expectedContent.deleteCharAt(expPipeOffset);

        final BaseDocument doc = getDocument(content.toString());
        Source source = Source.create(doc);
        final AtomicReference<CompletionProposal> found = new AtomicReference<>();
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult();
                assertNotNull(result);
                assertTrue(result instanceof CssParserResult);

                CssParserResult cssresult = (CssParserResult) result;

                CodeCompletionHandler cc = getPreferredLanguage().getCompletionHandler();
                String prefix = cc.getPrefix(cssresult, pipeOffset, false);
                CodeCompletionResult ccresult = cc.complete(createContext(pipeOffset, cssresult, prefix));

                assertCompletionItemNames(new String[]{itemToComplete}, ccresult, Match.CONTAINS);

                for (CompletionProposal ccp : ccresult.getItems()) {
                    if (itemToComplete.equals(ccp.getName())) {
                        //complete the item
                        found.set(ccp);
                        break;
                    }
                }

            }
        });

        CompletionProposal proposal = found.get();
        assertNotNull(proposal);

        final String text = proposal.getInsertPrefix();
        final int offset = proposal.getAnchorOffset();
        final int len = pipeOffset - offset;


        //since there's no access to the GsfCompletionItem.defaultAction() I've copied important code below:
        doc.runAtomic(new Runnable() {

            @Override
            public void run() {
                try {
                    int semiPos = -2;
                    String textToReplace = doc.getText(offset, len);
                    if (text.equals(textToReplace)) {
                        if (semiPos > -1) {
                            doc.insertString(semiPos, ";", null); //NOI18N
                        }
                        return;
                    }
                    int common = 0;
                    while (text.regionMatches(0, textToReplace, 0, ++common)) {
                        //no-op
                    }
                    common--;
                    Position position = doc.createPosition(offset + common);
                    Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                    doc.remove(offset + common, len - common);
                    doc.insertString(position.getOffset(), text.substring(common), null);
                    if (semiPosition != null) {
                        doc.insertString(semiPosition.getOffset(), ";", null);
                    }
                } catch (BadLocationException e) {
                    // Can't update
                }
            }
        });


        assertEquals(expectedContent.toString(), doc.getText(0, doc.getLength()));

    }
    
    protected void dumpTree(Node node) {
        PrintWriter pw = new PrintWriter(System.out);
        dump(node, 0, pw);
        pw.flush();
    }

    private void dump(Node tree, int level, PrintWriter pw) {
        for (int i = 0; i < level; i++) {
            pw.print("    ");
        }
        pw.print(tree.toString());
        pw.println();
        for (Node c : tree.children()) {
            dump(c, level + 1, pw);
        }
    }

    //--- utility methods ---
    protected String[] arr(String... args) {
        return args;
    }
    
    protected String getCompletionItemText(CompletionProposal cp) {
        return cp.getName();
    }

    private void assertCompletionItemNames(String[] expected, CodeCompletionResult ccresult, Match type) {
        Collection<String> real = new ArrayList<>();
        for (CompletionProposal ccp : ccresult.getItems()) {
            real.add(getCompletionItemText(ccp));
        }
        Collection<String> exp = new ArrayList<>(Arrays.asList(expected));

        if (type == Match.EXACT) {
            assertEquals(exp, real);
        } else if (type == Match.CONTAINS) {
            exp.removeAll(real);
            assertTrue("Expected " + arrayToString(exp.toArray(new String[0])) + ", but was " + arrayToString(real.toArray(new String[0])), exp.isEmpty());
        } else if (type == Match.EMPTY) {
            assertEquals("The unexpected element(s) '" + arrayToString(real.toArray(new String[]{})) + "' are present in the completion items list", 0, real.size());
        } else if (type == Match.NOT_EMPTY) {
            assertTrue(real.size() > 0);
        } else if (type == Match.DOES_NOT_CONTAIN) {
            int originalRealSize = real.size();
            real.removeAll(exp);
            assertEquals("The unexpected element(s) '" + arrayToString(expected) + "' are present in the completion items list", originalRealSize, real.size());
        } else if (type == Match.CONTAINS_ONCE) {
            for(String e : expected) {
                assertTrue(String.format("Expected item '%s' not found!", e), real.contains(e));
                real.remove(e);
                assertTrue(String.format("Expected item '%s' is contained multiple times!", e), !real.contains(e));
            }
        }

    }

    private String arrayToString(String[] elements) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            buf.append(elements[i]);
            if (i < elements.length - 1) {
                buf.append(',');
                buf.append(' ');
            }
        }
        return buf.toString();
    }

    private static TestCodeCompletionContext createContext(int offset, ParserResult result, String prefix) {
        return new TestCodeCompletionContext(offset, result, prefix, QueryType.COMPLETION, false);
    }

    private static class TestCodeCompletionContext extends CodeCompletionContext {

        private int caretOffset;
        private ParserResult result;
        private String prefix;
        private QueryType type;
        private boolean isCaseSensitive;

        public TestCodeCompletionContext(int caretOffset, ParserResult result, String prefix, QueryType type, boolean isCaseSensitive) {
            this.caretOffset = caretOffset;
            this.result = result;
            this.prefix = prefix;
            this.type = type;
            this.isCaseSensitive = isCaseSensitive;
        }

        @Override
        public int getCaretOffset() {
            return caretOffset;
        }

        @Override
        public ParserResult getParserResult() {
            return result;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public boolean isPrefixMatch() {
            return true;
        }

        @Override
        public QueryType getQueryType() {
            return type;
        }

        @Override
        public boolean isCaseSensitive() {
            return isCaseSensitive;
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
            for (GrammarElement e : ((GroupGrammarElement) ge).elements()) {
                dumpTree(sb, e, newParentList);
                sb.append('\n');
            }
            indentString(sb, level);
            sb.append(']');
        } else {
            sb.append(level);
            sb.append(ge.toString());
            sb.append("\n");
        }
    }

    private static void indentString(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append('\t');
        }
    }
}
