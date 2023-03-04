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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.ConsoleSection.Type;
import org.netbeans.modules.jshell.model.Rng;

/**
 * Parses a JShell file, creates a list of sections.
 * A JShell file can contain the following parts:
 * <ul>
 * <li>java content. This content can span either a single or multiple line, continuation
 * lines may or may not contain shell prompts
 * <li>output. These lines are unprefixed, they are output from the target VM
 * <li>message. Messages are prefixed by msg character, they are emitted by the JShell
 * as a response to a JShell command or java input
 * <li>command. Commands are slash-commands sent to the jshell
 * </ul>
 * 
 * @author sdedic
 */
public class JShellParser2 {
    private static final String CONTENTS_GROUP_NAME = "contents"; // NOI18N
    
    public JShellParser2(JShell state, CharSequence contents, int initialPos) {
        this.state = state;
        this.contents = contents;
        this.len = contents.length();
        this.startOffset = initialPos;
    }

    /**
     * Contents to analyze
     */
    private final CharSequence contents;
    
    /**
     * Length where the analysis should stop
     */
    private final int len;
    
    /**
     * The JShell instance for parsing
     */
    private final JShell state;

    private Pattern promptPattern = Pattern.compile("(^->|^>>) {0,2}(?<contents>.*)", Pattern.DOTALL); // NOI18N
    private Pattern commandPattern = Pattern.compile("(^->|^>>) {0,2}/(?<contents>.*)", Pattern.DOTALL); // NOI18N
    private Pattern messagePattern = Pattern.compile("^\\| (?<contents>.*)", Pattern.DOTALL); // NOI18N
    
    /**
     * The current position
     */
    private int pos;
    
    /**
     * Starting offset of the passed content, will be added to ALL positions
     * when creating sections or ranges.
     */
    private int startOffset;
    
    /**
     * Position of the current line's start, from the content start
     */
    private int lineStart;

    /**
     * The entire current line text, including markup
     */
    private String currentLine;

    /**
     * The contents of the current line, after markup is recognized. Note that
     * lineStart != start of lineContents!
     */
    private String lineContents;
    
    /**
     * Offset of the line contents, within the SINGLE line
     */
    private int lineContentsOffset;
    
    private void consumed() {
        currentLine = null;
        lineContents = null;
    }

    /**
     * True, if the current line terminates with newline. false means the current
     * line is the last one and is not terminated.
     */
    private boolean foundNewline = false;

    /**
     * Optionally adds 1 if the line ends with a newline.
     */
    private int addNewline(int a) {
        return /*foundNewline ? a + 1 :*/ a;
    }

    private String readLine() {
        if (currentLine != null) {
            return currentLine;
        }
        lineStart = pos;

        foundNewline = false;
        // recognize the following sequences: \r, \n, \r\n
        outer:
        while (pos < len) {
            char c = contents.charAt(pos++);
            switch (c) {
                case '\r':
                    foundNewline = true;
                    if (pos < len && contents.charAt(pos) == '\n') {
                        pos++;
                    }
                    break outer;
                case '\n':
                    foundNewline = true;
                    if (pos < len && contents.charAt(pos) == '\r') {
                        pos++;
                    }
                    break outer;
            }
        }
        currentLine = pos > lineStart
                ? contents.subSequence(lineStart, pos).toString()
                : null; // indicate end
        return currentLine;
    }

    State s = State.INITIAL;

    enum State {
        /**
         * Initial state 
         */
        INITIAL, 
        
        /**
         * Initial state, output is expected
         */
        OUTPUT, 
        OUTPUT_NEXT,
        
        /**
         * input provoked by a prompt
         */
        INPUT, 
        /**
         * possible input, if no marker is present
         */
        MAY_INPUT,
        MUST_INPUT,
        
        /**
         * command provoked by a prompt
         */
        COMMAND,
        COMMAND_LINE,
        
        /**
         * Continuation after a command; may produce output
         */
        COMMAND_OUTPUT,
        
        /**
         * Message provoked by a message char
         */
        MESSAGE,
        MESSAGE_NEXT,
    }

    /**
     * Accumulates snippet text from the last incomplete snippet.
     */
    private StringBuilder snippetText;
    
    /**
     * If s snippet is collected from multiple lines, this records
     * the length of the prefix from previous lines.
     */
    private int snippetPrefixLen;

    /**
     * The currently opened section
     */
    private ConsoleSection section;
    
    /**
     * Ranges / snippets for the current section
     */
    private List<Rng> ranges;
    private List<Rng> snippets;
    
    private List<ConsoleSection> sectionList = new ArrayList<>();
    
    private int p(int pos) {
        return startOffset + pos;
    }
    
    private void finishSection() {
        if (section == null) {
            return;
        }

        ModelAccessor.INSTANCE.extendSection(section, 
                section.getStart() + lineContentsOffset, p(lineStart), 
                ranges, 
                snippets);
        /*
        if (ranges.isEmpty()) {
            section.extendWithPart(section.getStart() + lineContentsOffset, p(lineStart));
        } else {
            section.extendToWithRanges(ranges);
        }
        if (snippets.size() > 1) {
            section.setSnippetRanges(snippets);
        }
                */
        sectionList.add(section);
    }
    
    private void createSection(Type type) {
        if (section != null) {
            if (section != null && section.getType() == type) {
                return;
            }
            finishSection();
        }        
        section = new ConsoleSection(p(lineStart), type);
        ranges  = new ArrayList<>();
        snippets = new ArrayList<>();
    }
    
    private void onMessage() {
        createSection(Type.MESSAGE);
        extendWithPart(lineContentsOffset, len());
        consumed();
        s = State.MESSAGE_NEXT;
    }
    
    private void onCommand() {
        createSection(Type.COMMAND);
        extendWithPart(lineContentsOffset, len());
        consumed();
        s = State.COMMAND;
    }
    
    private void onCommandLine() {
        
    }
    
    private void onMessageNext() {
        if (checkCommand() || checkInputJava()) {
            return;
        }
        if (checkMessage()) {
            onMessage();
        } else {
            s = State.OUTPUT;
        }
    }
    
    private void onCommandOutput() {
        if (checkCommand() || checkInputJava()) {
            return;
        }
        createSection(Type.OUTPUT);
        // PENDING: the section should contain info on the markers
        extendSection(section, p(endLine()));
        //section.extendTo(p(endLine()));
        consumed();
    }
    
    private void addJavaSnippet(int start, int len) {
        int a = p(lineStart + start);
        if (section != null && section.isIncomplete()) {
            // replace the last java snippet
            int i = snippets.size() - 1;
            Rng prev = snippets.get(i);
            Rng r = new Rng(prev.start, prev.start + len);
            snippets.set(i, r);
        } else {
            int b = a + len;
            snippets.add(new Rng(a, a + len));
        }
    }
    
    private void extendWithPart(int ls, int le) {
        if (!ranges.isEmpty()) {
            int l = ranges.size() -1;
            Rng r = ranges.get(l);
            int a = p(lineStart + ls);
            // potentially join ranges
            if (r.end == a) {
                ranges.set(l, new Rng(r.start, p(this.lineStart + le)));
                return;
            }
        }
        ranges.add(new Rng(p(this.lineStart + ls), p(this.lineStart + le)));
    }
    
    private void extendSection(ConsoleSection s, int end) {
        ModelAccessor.INSTANCE.extendSection(section, 
                section.getStart(), 
                end, null, null);
    }
    
    private void onOutput() {
        if (checkCommand() || checkInputJava() || checkMessage()) {
            return;
        }
        // it is an output, does not start with a message -- so it is a plain
        // command output
        createSection(Type.OUTPUT);
        // PENDING: the section should contain info on the markers
        extendSection(section, p(endLine()));
        consumed();
        s = State.OUTPUT_NEXT;
    }
    
    private void onOutputNext() {
        if (checkInputJava()) {
            return;
        }
        extendSection(section, p(endLine()));
        consumed();
        // expect further output, do not change state
    }
    
    private int len() {
        return currentLine.length();
    }
    
    private int endLine() {
        return lineStart + currentLine.length();
    }

    private void addSnippetText(int pos, String text) {
        if (snippetText == null || (section != null && !section.isIncomplete())) {
            snippetPrefixLen = 0;
            snippetText = new StringBuilder(text);
        } else {
            snippetPrefixLen = snippetText.length();
            snippetText.append(text);
        }
    }

    private boolean checkInputJava() {
        return doCheckPattern(State.INPUT, promptPattern);
    }

    private boolean checkMessage() {
        return doCheckPattern(State.MESSAGE, messagePattern);
    }
    
    private boolean doCheckPattern(State nextState, Pattern pat) {
        Matcher m = pat.matcher(currentLine);
        boolean prompt = m.find();

        if (prompt) {
            s = nextState;
            lineContentsOffset = m.start(CONTENTS_GROUP_NAME);
            lineContents = m.group(CONTENTS_GROUP_NAME);
            return true;
        } else {
            return false;
        }
    }        

    private boolean checkCommand() {
        return doCheckPattern(State.COMMAND, commandPattern);
    }

    private void onInitial() {
        if (checkCommand() || checkInputJava() || checkMessage()) {
            return;
        }
        // wtf ?
        // it is an output, does not start with a message -- so it is a plain
        // command output
        createSection(Type.OUTPUT);
        // PENDING: the section should contain info on the markers
        extendSection(section, p(endLine()));
        consumed();
        s = State.COMMAND_OUTPUT;
    }

    private void onInput() {
        snippetText = null;
        createSection(Type.JAVA);
        addSnippetText(lineContentsOffset, lineContents);
        processJavaInput();
        consumed();
    }
    
    private void onContinueInput() {
        createSection(Type.JAVA);
        lineContents = this.currentLine;
        lineContentsOffset = 0; // starts at the beginning of the line
        addSnippetText(lineContentsOffset, lineContents);
        processJavaInput();
        consumed();
    }
    
    private void onMayInput() {
        if (checkCommand() || checkInputJava() || checkMessage()) {
            return;
        }
        onContinueInput();
    }
    
    private void onMustInput() {
        if (checkInputJava()) {
            // okay, prompt is present
            s = State.INPUT;
            return;
        } else {
            // since there must be an input, get the whole line as java input ...
            onContinueInput();
        }
    }
    
    private void processJavaInput() {
        String input = snippetText.toString();

        int lpos = lineContentsOffset;
        if (state == null) {
            boolean empty = input.trim().isEmpty();
            int e = input.length();
            e = addNewline(input.length()); // plus newline
            extendWithPart(lpos, lpos += e);
            ModelAccessor.INSTANCE.setSectionComplete(section, true);
            s = State.MAY_INPUT;
            return;
        }
        
        extendWithPart(lpos, lpos + lineContents.length());
        
        int snipOffset = lpos;
        O:
        while (true) {
            SourceCodeAnalysis.CompletionInfo info = state.sourceCodeAnalysis().analyzeCompletion(input);
            int endPos;
            String rem = info.remaining();
            if (rem == null) {
                endPos = input.length() - 1;
            } else {
                endPos = input.length() - rem.length();
            }
            int e = endPos;
            boolean empty = info.remaining().trim().isEmpty();
            switch (info.completeness()) {
                case DEFINITELY_INCOMPLETE:
                case CONSIDERED_INCOMPLETE:
                    // parser thinks it is incomplete. If no prompt/marker is present, the JShell was not able
                    // to accept the input, so there must be continuation even without a prompt.
                    s = State.MUST_INPUT;
                    addJavaSnippet(snipOffset, input.length());
                    ModelAccessor.INSTANCE.setSectionComplete(section, false);
                    break O;

                case EMPTY:
                    // empty input should not be executed, so there has to be something else.
                    s = State.MAY_INPUT;
                    break O;

                case COMPLETE_WITH_SEMI:
                    // there is no further snippet, at least not on this line, otherwise this one would be complete
                case UNKNOWN:
                    // there should be either a message or a continuation which makes the input complete, the
                    // parser could not handle it
                    // fall through.
                case COMPLETE:
                    if (empty) {
                        e = addNewline(input.length()); // plus newline
                    }
                    input = info.remaining();
                    e -= snippetPrefixLen;
                    addJavaSnippet(snipOffset, endPos);
                    snipOffset += e;
                    ModelAccessor.INSTANCE.setSectionComplete(section, true);
                    s = State.MAY_INPUT;
                    if (empty) {
                        break O;
                    }
                    break;
                default:
                    throw new AssertionError(info.completeness().name());
            }
        }
    }

    public List<ConsoleSection> sections() {
        if (!parsed) {
            execute();
        }
        return sectionList;
    }
    
    private boolean parsed;
    
    public void setInitialState(State s) {
        this.s = s;
    }
    
    public void setAfterState(ConsoleSection s) {
        State init;
        
        switch (s.getType()) {
            case MESSAGE:
                init = State.MESSAGE_NEXT;
                break;
            case OUTPUT:
                init = State.OUTPUT_NEXT;
                break;
            default:
                throw new AssertionError(s.getType().name());
        }
        setInitialState(init);
        createSection(s.getType());
    }
    
    private void appendNewline() {
        if (!ranges.isEmpty()) {
            extendWithPart(0, len());
        }
        consumed();
    }

    public void execute() {
        String l;
        
        while ((l = readLine()) != null) {
            if (currentLine.trim().isEmpty() && section != null) {
                // special handling: assign the divisor line to the last opened section, if there's any
                appendNewline();
                continue;
            }
            switch (s) {
                case INITIAL:
                    onInitial();
                    break;
                case OUTPUT:
                    onOutput();
                    break;
                case OUTPUT_NEXT:
                    onOutputNext();
                    break;
                case INPUT:
                    onInput();
                    break;
                case MAY_INPUT:
                    onMayInput();
                    break;
                case MUST_INPUT:
                    onMustInput();
                    break;
                case COMMAND:
                    onCommand();
                    break;
                case COMMAND_LINE:
                    onCommandLine();
                    break;
                case COMMAND_OUTPUT:
                    onCommandOutput();
                    break;
                case MESSAGE:
                    onMessage();
                    break;
                case MESSAGE_NEXT:
                    onMessageNext();
                    break;
                default:
                    throw new AssertionError(s.name());
            }
        }
        finishSection();
        parsed = true;
    }
}

