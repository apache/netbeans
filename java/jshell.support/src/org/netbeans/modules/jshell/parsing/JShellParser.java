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
package org.netbeans.modules.jshell.parsing;

import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.JShellToken;
import java.util.ArrayList;
import java.util.List;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author sdedic
 */
public class JShellParser {
    private final ShellAccessBridge   shellAccess;
    private TokenSequence<JShellToken>  sequence;
    private int startOffset;
    private int limit;
    
    public JShellParser(ShellAccessBridge access, TokenSequence<JShellToken> seq, int initialPos, int limit) {
        this.shellAccess = access;
        this.startOffset = initialPos;
        this.sequence = seq;
        this.limit = limit;
    }
    
    /**
     * Accumulates snippet text from the last incomplete snippet.
     */
    private StringBuilder snippetText;
    
    /**
     * The currently opened section
     */
    private ConsoleSection section;
    
    /**
     * Ranges / snippets for the current section
     */
    private List<Rng> ranges = new ArrayList<>();
    private List<Rng> snippets = new ArrayList<>();
    
    private int snippetPrefixLen;
    
    private List<ConsoleSection>    sectionList = new ArrayList<>();
    
    private int lineContentsStart;
    
    private int p(int pos) {
        return startOffset + pos;
    }
    
    private void finishSection() {
        finishSection(sequence.offset());
    }
    
    private void finishSection(int eoffset) {
        if (section == null) {
            return;
        }
        ModelAccessor.INSTANCE.extendSection(section, 
                p(lineContentsStart), p(eoffset),
                ranges,
                snippets);
        sectionList.add(section);
        
        section = null;
    }
    
    private void createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type type) {
        createSection(type, sequence.token().length(), sequence.offset());
    }
    
    private void createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type type, int l, int start) {
        if (section != null) {
            if (section != null && section.getType() == type) {
                int s = start;
                if (s + l >= limit) {
                    l = limit - s;
                }
                ModelAccessor.INSTANCE.extendSection(section, 
                        p(s), p(s + l), null, null);
                return;
            }
            finishSection();
        }        
        if (lineStart + l >= limit) {
            l = limit - lineStart;
        }
        section = new ConsoleSection(p(lineStart), type, l);
        ranges  = new ArrayList<>();
        snippets = new ArrayList<>();
    }
    
    private void addJavaSnippet(int start, int len) {
        int a = start;
        if (section != null && section.isIncomplete()) {
            // replace the last java snippet
            int i = snippets.size() - 1;
            Rng prev = snippets.get(i);
            Rng r = new Rng(prev.start, prev.start + len);
            snippets.set(i, r);
        } else {
            int b = a + len;
            snippets.add(new Rng(p(a), p(b)));
        }
    }
    
    private int lineStart;
    
    private void extendWithPart() {
        int ls = sequence.offset();
        int le = ls + sequence.token().length();
        extendWithPart(ls, le);
    }
    
    private void extendWithPart(int ls, int le) {
        if (le == limit + 1) {
            // hack: lexer produces token which is one past the document / snapshot contents
            le = limit;
        }
        if (!ranges.isEmpty()) {
            int l = ranges.size() -1;
            Rng r = ranges.get(l);
            int a = p(ls);
            // potentially join ranges
            if (r.end == a || r.start == a) {
                r = new Rng(r.start, p(le));
                ranges.set(l, r);
                ModelAccessor.INSTANCE.extendSection(section, 
                        r.start, r.end, null, null);
                return;
            }
        }
        Rng r = new Rng(p(ls), p(le));
        ranges.add(r);
        ModelAccessor.INSTANCE.extendSection(section, 
                r.start, r.end, null, null);
    }
    
    private void addSnippetText(int pos, CharSequence text) {
        if (snippetText == null || (section != null && !section.isIncomplete())) {
            snippetPrefixLen = 0;
            snippetText = new StringBuilder(text);
        } else {
            snippetPrefixLen = snippetText.length();
            snippetText.append(text);
        }
    }
    
    private void processJavaInput() {
        String input = snippetText.toString();

        int lpos = sequence.offset();
        CharSequence lineContents = sequence.token().text();
        if (!shellAccess.isInitialized()) {
            boolean empty = input.trim().isEmpty();
            int e = input.length();
            e = input.length();
            extendWithPart();
            ModelAccessor.INSTANCE.setSectionComplete(section, true);
            return;
        }
        
        int snipOffset = lpos;
        O:
        while (true) {
            SourceCodeAnalysis.CompletionInfo info = shellAccess.analyzeInput(input);
            int endPos;
            String rem = info.remaining();
            if (rem == null) {
                endPos = input.length() - 1;
            } else {
                endPos = input.length() - rem.length() - 1;
            }
            int e = endPos;
            boolean empty = info.remaining().trim().isEmpty();
            switch (info.completeness()) {
                case DEFINITELY_INCOMPLETE:
                case CONSIDERED_INCOMPLETE:
                    // parser thinks it is incomplete. If no prompt/marker is present, the JShell was not able
                    // to accept the input, so there must be continuation even without a prompt.
                    addJavaSnippet(snipOffset, input.length());
                    ModelAccessor.INSTANCE.setSectionComplete(section, false);
                    break O;

                case EMPTY:
                    // empty input should not be executed, so there has to be something else.
                    break O;

                case COMPLETE_WITH_SEMI:
                    // there is no further snippet, at least not on this line, otherwise this one would be complete
                case UNKNOWN:
                    // there should be either a message or a continuation which makes the input complete, the
                    // parser could not handle it
                    // fall through.
                case COMPLETE:
                    if (empty) {
                        e = input.length();
                    }
                    input = info.remaining();
                    e -= snippetPrefixLen;
                    addJavaSnippet(snipOffset, endPos + 1);
                    snipOffset += e;
                    ModelAccessor.INSTANCE.setSectionComplete(section, true);
                    if (empty) {
                        break O;
                    }
                    break;
                default:
                    throw new AssertionError(info.completeness().name());
            }
        }
    }

    public void execute() {
        boolean f = true;
        boolean wasPrompt = false;
        int end = 0;
        while (sequence.moveNext() && (limit <  1 || sequence.offset() < limit)) {
            if (f) {
                lineStart = sequence.offset();
                f = false;
            }
            Token<JShellToken> tukac = sequence.token();
            boolean resetJava = true;
            end = sequence.offset() + tukac.length();
            switch (tukac.id()) {
                case COMMAND:
                case ERR_COMMAND:
                case COMMAND_PARAM:
                case COMMAND_OPTION:
                case COMMAND_STRING:
                    // both go to an input section
                    createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.COMMAND);
                    extendWithPart();
                    break;
                    
                case PROMPT:
                    finishSection();
                    // may be followed by java or command. Finish any pending section
                    lineContentsStart = sequence.offset() + tukac.length();
                    if (lineContentsStart >= limit) {
                        lineContentsStart = limit;
                    }
                    if (!sequence.moveNext()) {
                        // prompt ends the line
                        createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.JAVA, 0, end);
                        extendWithPart(end, end);
                        break;
                    }
                    wasPrompt = true;
                    switch (sequence.token().id()) {
                        case COMMAND:
                        case COMMAND_PARAM:
                        case ERR_COMMAND:
                        case COMMAND_OPTION:
                        case COMMAND_STRING:
                            createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.COMMAND);
                            // these will create a section
                            sequence.movePrevious();
                            break;
                        case JAVA:
                        default:
                            // whatever after the prompt; create a java section,
                            // but move the sequence back before that
                            sequence.movePrevious();
                            createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.JAVA);
                    }
                    
                    break;
                    
                case CONTINUATION_PROMPT:
                    // only possible with java
                    lineContentsStart = sequence.offset();
                    // continue scanning
                    resetJava = false;
                    break;
                case JAVA:
                    if (!wasPrompt)  {
                        // special case: no real java section starts without a prompt;
                        // this one is a extra output after an executed java command.
                        createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.OUTPUT);
                        extendWithPart();
                        break;
                    }
                    createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.JAVA);
                    extendWithPart();
                    addSnippetText(sequence.offset(), tukac.text());
                    resetJava = false;
                    processJavaInput();
                    break;
                
                case MESSAGE_MARK:
                    lineContentsStart = sequence.offset();
                    createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.MESSAGE);
                    break;
                case MESSAGE_TEXT:
                case ERROR_MARKER:
                    createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.MESSAGE);
                    extendWithPart();
                    break;

                case OUTPUT:
                    createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.OUTPUT);
                    extendWithPart();
                    break;
                
                case WHITESPACE:
                    if (section == null) {
                        createSection(org.netbeans.modules.jshell.model.ConsoleSection.Type.OUTPUT);
                    }
                    extendWithPart();
                    resetJava = false;
                    break;
                    
                default:
                    throw new AssertionError(tukac.id().name());
            }
            
            // check if the token ends with a newline:
            CharSequence text = tukac.text();
            if (text.length() > 0 && text.charAt(text.length() - 1) == '\n') { // NOI18N
                lineStart = sequence.offset() + tukac.length();
                lineContentsStart = lineStart;
            }
            
            if (resetJava) {
                snippetText = null;
            }
        }
        finishSection(end);
    }
    
    public List<ConsoleSection> sections() {
        return sectionList;
    }

}
