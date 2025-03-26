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
package org.netbeans.modules.groovy.editor.api.completion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lsp.Completion;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CompletionProposal;
import static org.netbeans.modules.csl.api.test.CslTestBase.getCaretOffset;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyLanguage;
import org.netbeans.modules.groovy.editor.completion.provider.GroovyCompletionCollector;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.spi.lsp.CompletionCollector;

/**
 * Base class for all groovy CC tests, providing various helper methods.
 * 
 * @author Martin Janicek
 */
public abstract class GroovyCCTestBase extends GroovyTestBase {

    protected final String BASE;

    
    protected GroovyCCTestBase(String testName) {
        super(testName);
        Logger.getLogger(CompletionHandler.class.getName()).setLevel(Level.FINEST);

        BASE = getExpandedSourcePath() + "/"; //NOI18N
    }

    /**
     * This method should return concrete test type which will be used for ClassPath initialization. 
     * For example if method CC tests are located under completion/method this method should return
     * "method" for MethodCCTest.java test case
     *
     * @return concrete test type
     */
    protected abstract String getTestType();

    @Override
    protected Set<String> additionalSourceClassPath() {
        HashSet<String> sourceClassPath = new HashSet<String>();
        sourceClassPath.add(getExpandedSourcePath());

        return sourceClassPath;
    }

    private String getExpandedSourcePath() {
        return getBasicSourcePath() + "/" + firstLetterToLowerCase(getClassName()); //NOI18N
    }

    private String firstLetterToLowerCase(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    protected String getBasicSourcePath() {
        return "testfiles/completion/" + getTestType(); //NOI18N
    }

    protected String getTestPath() {
        return getExpandedSourcePath() + "/" + getClassName() + ".groovy"; //NOI18N
    }

    /*
     * This method returns simple test class name. For example when test method uses SomeTestMethod.groovy as a
     * class for code completion test, this test method is typically named testSomeNameMethod_1 (and if there
     * is more tests for the same SomeTestMethod.groovy class then the number behind '_' is typically incremented).
     * In this case simple test class name is SomeTestMethod.
     */
    private String getClassName() {
        String name = getName();
        String nameWithoutPrefix = name.substring(4); // Removing 'test' prefix

        int indexOf = nameWithoutPrefix.indexOf("_");
        if (indexOf != -1) {
            nameWithoutPrefix = nameWithoutPrefix.substring(0, indexOf); // Removing _someNumber sufix
        }
        return nameWithoutPrefix;
    }
    
    @Override
    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, boolean includeJavaVersion, String ext, boolean checkFileExistence, boolean skipMarkers) throws Exception {
        super.assertDescriptionMatches(relFilePath, removeSpuriousCompletionItemsFromDescription(description), includeTestName,
                includeJavaVersion, ext, checkFileExistence, skipMarkers);
    }    
    
    private String removeSpuriousCompletionItemsFromDescription(String description) {
        return description.replaceAll("PACKAGE\\s+apple\\s+null\n", "")
                .replaceAll("PACKAGE\\s+oracle\\s+null\n", "")
                .replaceAll("PACKAGE\\s+netscape\\s+null\n", "")
                .replaceAll("PACKAGE\\s+nbjavac\\s+null\n", "");
    }
    
    protected boolean checkLspCompletion = true;
    
    public void checkCompletion(final String file, final String caretLine, final boolean includeModifiers) throws Exception {
        super.checkCompletion(file, caretLine, includeModifiers);
        if (checkLspCompletion) {
            checkLSPCompletion(file, caretLine, includeModifiers);
        }
    }

    protected void checkLSPCompletion(String file, final String caretLine, boolean includeModifiers) throws Exception {
        Completion.Context ctx = new Completion.Context(Completion.TriggerKind.Invoked, null);
        GroovyCompletionCollector cc = (GroovyCompletionCollector)MimeLookup.getLookup(GroovyLanguage.GROOVY_MIME_TYPE).lookup(CompletionCollector.class);
        assertNotNull(cc);
        
        Document testDoc = GsfUtilities.getDocument(getTestFile(file), true);
        String content = testDoc.getText(0, testDoc.getLength());
        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(content, caretLine);
        } else {
            caretOffset = -1;
        }
        List<Completion> items = new ArrayList<>();

        final boolean deprecatedHolder[] = new boolean[1];

        GroovyCompletionCollector.CompletionTask ct = cc.collectCompletions2(testDoc, caretOffset, ctx, items::add);
        String desc = describeCompletion(content, caretOffset, true, true, CodeCompletionHandler.QueryType.COMPLETION, items, 
                ct.getOriginalProposals(), includeModifiers, deprecatedHolder);
        assertDescriptionApproxMatches(file, desc, true, ".completion");
    }

    protected void assertDescriptionApproxMatches(String relFilePath, 
            String description, boolean includeTestName, String ext) throws Exception {
        super.assertDescriptionMatches(relFilePath, 
                removeSpuriousCompletionItemsFromDescription(description), includeTestName, true, ext, true, true);
    }    
    
    private String getSourceLine(String s, int offset) {
        int begin = offset;
        if (begin > 0) {
            begin = s.lastIndexOf('\n', offset-1);
            if (begin == -1) {
                begin = 0;
            } else if (begin < s.length()) {
                begin++;
            }
        }
        if (s.length() == 0) {
            return s;
        }
//        s.charAt(offset);
        int end = s.indexOf('\n', begin);
        if (end == -1) {
            end = s.length();
        }

        if (offset < end) {
            return (s.substring(begin, offset)+"|"+s.substring(offset,end)).trim();
        } else {
            return (s.substring(begin, end) + "|").trim();
        }
    }
    
    private String getLhs(Completion c) {
        String text = c.getLabel();
        int idx = text.indexOf(" : ");
        if (idx > 0) {
            return text.substring(0, idx);
        } else {
            return text;
        }
    }

    private String getRhs(Completion c) {
        String text = c.getLabel();
        int idx = text.indexOf(" : ");
        if (idx > 0) {
            return text.substring(idx + 3);
        } else {
            return null;
        }
    }

    private String describeCompletion(String text, int caretOffset, boolean prefixSearch, boolean caseSensitive, CodeCompletionHandler.QueryType type, 
            List<Completion> proposals,
            List<CompletionProposal> cslProposals,
            boolean includeModifiers, boolean[] deprecatedHolder) {
        StringBuilder sb = new StringBuilder();
        sb.append("Code completion result for source line:\n");
        String sourceLine = getSourceLine(text, caretOffset);
        if (sourceLine.length() == 1) {
            sourceLine = getSourceWindow(text, caretOffset);
        }
        sb.append(sourceLine);
        sb.append("\n(QueryType=" + type + ", prefixSearch=" + prefixSearch + ", caseSensitive=" + caseSensitive + ")");
        sb.append("\n");

        // Sort to make test more stable
        Map<Completion, CompletionProposal> m = new HashMap<>();
        Map<Completion, Integer> indexes = new HashMap<>();
        for (int idx = 0; idx < proposals.size(); idx++) {
            Completion c = proposals.get(idx);
            indexes.put(c, idx);
            m.put(c, cslProposals.get(idx));
        }
        proposals.sort(new Comparator<Completion>() {

            public int compare(Completion p1, Completion p2) {
                int idx1 = indexes.get(p1);
                int idx2 = indexes.get(p2);
                
                CompletionProposal cp1 = cslProposals.get(idx1);
                CompletionProposal cp2 = cslProposals.get(idx2);
                
                // Smart items first
                if (cp1.isSmart() != cp2.isSmart()) {
                    return cp1.isSmart() ? -1 : 1;
                }

                if (cp1.getKind() != cp2.getKind()) {
                    return cp1.getKind().compareTo(cp2.getKind());
                }

                String p1L = getLhs(p1);
                String p2L = getLhs(p2);

                if (!p1L.equals(p2L)) {
                    return p1L.compareTo(p2L);
                }

                String p1Rhs = getRhs(p1);
                String p2Rhs = getRhs(p2);
                if (p1Rhs == null) {
                    p1Rhs = "";
                }
                if (p2Rhs == null) {
                    p2Rhs = "";
                }
                if (!p1Rhs.equals(p2Rhs)) {
                    return p1Rhs.compareTo(p2Rhs);
                }
                return 0;
            }
        });
        
        boolean isSmart = true;
        int idx = -1;
        for (Completion proposal : proposals) {
            idx++;
            CompletionProposal cslProposal = m.get(proposal);
            
            if (isSmart && !cslProposal.isSmart()) {
                sb.append("------------------------------------\n");
                isSmart = false;
            }

            deprecatedHolder[0] = proposal.getTags() != null && proposal.getTags().contains(Completion.Tag.Deprecated);
            boolean strike = includeModifiers && deprecatedHolder[0];

            String n;
            
            switch (proposal.getKind()) {
                case Folder: n = "PACKAGE"; break;
                case Property: n = "PARAMETER"; break;
                case Function: n = "METHOD"; break;
                default:
                   n = proposal.getKind().toString().toUpperCase(); 
                   break;
            }
            int MAX_KIND = 10;
            if (n.length() > MAX_KIND) {
                sb.append(n.substring(0, MAX_KIND));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_KIND; i++) {
                    sb.append(" ");
                }
            }

//            if (proposal.getModifiers().size() > 0) {
//                List<String> modifiers = new ArrayList<String>();
//                for (Modifier mod : proposal.getModifiers()) {
//                    modifiers.add(mod.name());
//                }
//                Collections.sort(modifiers);
//                sb.append(modifiers);
//            }

            sb.append(" ");

            n = getLhs(proposal);
            int MAX_LHS = 30;
            if (strike) {
                MAX_LHS -= 6; // Account for the --- --- strikethroughs
                sb.append("---");
            }
            if (n.length() > MAX_LHS) {
                sb.append(n.substring(0, MAX_LHS));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_LHS; i++) {
                    sb.append(" ");
                }
            }

            if (strike) {
                sb.append("---");
            }

            sb.append("  ");

            if (cslProposal.getModifiers().isEmpty()) {
                n = "";
            } else {
                n = cslProposal.getModifiers().toString();
            }
            int MAX_MOD = 9;
            if (n.length() > MAX_MOD) {
                sb.append(n.substring(0, MAX_MOD));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_MOD; i++) {
                    sb.append(" ");
                }
            }

            sb.append("  ");

            sb.append(getRhs(proposal));
            sb.append("\n");

            isSmart = cslProposal.isSmart();
        }

        return sb.toString();
    }
}
