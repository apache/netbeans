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

package org.netbeans.modules.web.indent.api.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.embedding.VirtualSource;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @since org.netbeans.modules.css.editor/1 1.3
 */
public abstract class AbstractIndenter<T1 extends TokenId> {

    private Language<T1> language;
    private Context context;
    private int indentationSize;

    private static final Logger LOG = Logger.getLogger(AbstractIndenter.class.getName());

    protected static final boolean DEBUG = LOG.isLoggable(Level.FINE);
    protected static final boolean DEBUG_PERFORMANCE = Logger.getLogger("AbstractIndenter.PERF").isLoggable(Level.FINE);
    private static long startTime1;
    private static long startTimeTotal;

    private static final int MAX_INDENT = 200;
    
    public static boolean inUnitTestRun = false;

    private IndenterFormattingContext formattingContext;

    public AbstractIndenter(Language<T1> language, Context context) {
        this.language = language;
        this.context = context;
        indentationSize = indentLevelSize(getDocument(), language.mimeType());
        formattingContext = new IndenterFormattingContext(getDocument());
    }
    
    //copied from org.netbeans.modules.editor.indent.api.IndentUtils as we need
    //to pass a second parameter to the CodeStylePreferences.get(doc) method.
    private static int indentLevelSize(Document doc, String mimeType) {
        Preferences prefs = CodeStylePreferences.get(doc, mimeType).getPreferences();
        int indentLevel = prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);
        
        if (indentLevel < 0) {
            boolean expandTabs = prefs.getBoolean(SimpleValueNames.EXPAND_TABS, true);
            if (expandTabs) {
                indentLevel = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, 4);
            } else {
                indentLevel = prefs.getInt(SimpleValueNames.TAB_SIZE, 8);
            }
        }

        assert indentLevel >= 0 : "Invalid indentLevelSize " + indentLevel + " for " + doc; //NOI18N
        return indentLevel;
    }


    public final IndenterFormattingContext createFormattingContext() {
        return formattingContext;
    }

    public final void beforeReindent(Collection<? extends IndenterFormattingContext> contexts) {
        assert contexts.size() > 0 : "your IndentTask must implement Lookup.Provider " + // NOI18N
                "and return an instance of IndenterFormattingContext in it"; // NOI18N
        IndenterFormattingContext first = null;
        IndenterFormattingContext last = null;
        for (IndenterFormattingContext ifc : contexts) {
            if (ifc.isInitialized()) {
                return;
            }
            if (first == null) {
                first = ifc;
                first.initFirstIndenter();
            } else {
                ifc.setDelegate(first);
            }
            last = ifc;
        }
        assert first != null;
        assert last != null;
        last.setLastIndenter();
    }

    protected final int getIndentationSize() {
        return indentationSize;
    }

    protected final Context getContext() {
        return context;
    }

    protected final BaseDocument getDocument() {
        return (BaseDocument)context.document();
    }

    protected final Language<T1> getLanguage() {
        return language;
    }

    /**
     * Iterate backwards from given start offset and return offset in document which
     * is good start to base formatting of document between given offsets. Returned
     * formatting start should ideally be a begining of language block which
     * encompasses everything between startOffset and endOffset.
     *
     * @return offset representing stable start for formatting
     */
    protected abstract int getFormatStableStart(JoinedTokenSequence<T1> ts, int startOffset, int endOffset,
            AbstractIndenter.OffsetRanges rangesToIgnore) throws BadLocationException;

    /**
     * Calculate and return list of indentation commands for the given line.
     *
     * @param context descriptor of line to be formatted
     * @param preliminaryNextLineIndent list of indentation commands to apply
     *  for next lines based on available data; correctness is limited as nothing
     *  is known about next line yet
     * @return list of indentation commands to apply when formatting this line
     */
    protected abstract List<IndentCommand> getLineIndent(IndenterContextData<T1> context, 
            List<IndentCommand> preliminaryNextLineIndent) throws BadLocationException;

    /**
     * Is this whitespace token?
     */
    protected abstract boolean isWhiteSpaceToken(Token<T1> token);

    protected abstract void reset();

//    private boolean isWithinLanguage(int startOffset, int endOffset) {
//        for (Context.Region r : context.indentRegions()) {
//            if ( (startOffset >= r.getStartOffset() && startOffset <= r.getEndOffset()) ||
//                    (endOffset >= r.getStartOffset() && endOffset <= r.getEndOffset()) ||
//                    (startOffset <= r.getStartOffset() && endOffset >= r.getEndOffset()) ) {
//                    return true;
//            }
//        }
//        return false;
//    }

    private boolean used = false;
    public final void reindent() {
        if (used && DEBUG) {
            System.err.println("WARNING: indentation task cannot be reused! is this ok?");
            //IllegalStateException x = new IllegalStateException("indentation task cannot be reused");
            //Exceptions.printStackTrace(x);
        }
        used = true;
        reset();
        beforeReindent(context.getLookup().lookupAll(IndenterFormattingContext.class));
        formattingContext.disableListener();
        try {
            if (!formattingContext.isFirstIndenter()) {
                // if there were document changes done by some other formatter
                // then update offsets of all lines we are keeping in memory:
                List<IndenterFormattingContext.Change> l = formattingContext.getAndClearChanges();
                if (l.size() > 0) {
                    updateLineOffsets(l);
                }
            } else {
                startTimeTotal = System.currentTimeMillis();
            }
            calculateIndentation();
            applyIndentation();
        } catch (BadLocationException ble) {
            if (inUnitTestRun) {
                throw new RuntimeException(ble);
            }
            Exceptions.printStackTrace(ble);
        } finally {
            if (formattingContext.isLastIndenter()) {
                formattingContext.removeListener();
                if (DEBUG_PERFORMANCE) {
                    System.err.println("[IndPer] Total time "+(System.currentTimeMillis()-startTimeTotal)+" ms");
                }
            } else {
                formattingContext.enableListener();
            }
        }
    }

    private void calculateIndentation() throws BadLocationException {
        final BaseDocument doc = getDocument();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

        if (DEBUG) {
            System.err.println(">> AbstractIndenter based indenter: "+this.getClass().toString());
        }

// TODO: this needs to be revisited. the problem is that different formatters
//       may have influence on each other and therefore has to be run even if
//       they are not covered by current indentation region. for example
//       closing a JSP tag and pressing Enter may need to be indented accoring to
//       previous HTML tag which may be dozens line above current line
//
//        boolean withinLanguage = isWithinLanguage(startOffset, endOffset);
//        boolean justAfterOurLanguage = isJustAfterOurLanguage();
//        if (DEBUG && !withinLanguage && justAfterOurLanguage) {
//            System.err.println("enabling formatter because it is justAfterOurLanguage case");
//        }
//
//        // abort if formatting is not within our language
//        if (!withinLanguage && !justAfterOurLanguage) {
//            if (DEBUG) {
//                System.err.println("Nothing to be done by "+this.getClass().toString());
//            }
//            return;
//        }
//        // quick check whether this is new line indent and if it is within our language
//        if (endOffset-startOffset < 4 && !justAfterOurLanguage) { // for line reindents etc.
//            boolean found = false;
//            for (int offset = startOffset; offset <= endOffset; offset++) {
//                Language l = LexUtilities.getLanguage(doc, offset);
//                if (l != null && l.equals(language)) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                if (DEBUG) {
//                    System.err.println("Nothing to be done by "+this.getClass().toString());
//                }
//                return;
//            }
//        }

        // debug these only when FINER logging is requested - it clutters output
        if (LOG.isLoggable(Level.FINER)) {
            System.err.println(">> TokenHierarchy of file to be indented:");
            System.err.println(org.netbeans.api.lexer.TokenHierarchy.get(doc));
        }
        
        // create chunks of our language from the document:
        VirtualSource virtualSource = createVirtualSource();
        List<JoinedTokenSequence.CodeBlock<T1>> blocks = LexUtilities.createCodeBlocks(doc, language, virtualSource);
        if (blocks == null) {
            // nothing to do:
            return;
        }
        if (DEBUG) {
            //System.err.println(">> Code blocks:\n"+blocks);
        }

        // create joined TokenSequence for our language
        JoinedTokenSequence<T1> joinedTS = JoinedTokenSequence.createFromCodeBlocks(blocks);

        // start on the beginning of line:
        int start = Utilities.getRowStart(doc, startOffset);
        // end after the last line:
        int end = Utilities.getRowEnd(doc, endOffset)+1;
        if (end > doc.getLength()) {
            end = doc.getLength();
        }

        int initialOffset = 0;
        OffsetRanges rangesToIgnore = new OffsetRanges();
        if (start > 0) {
            // find point of stable formatting start if start position not zero
            TokenSequence<T1> ts = LexUtilities.getTokenSequence(doc, start, language);
            if (ts == null) {
                int newStart = findPreviousOccuranceOfOurLanguage(joinedTS, start);
                if (newStart == -1) {
                    // nothing to do
                    return;
                }
                ts = LexUtilities.getTokenSequence(doc, newStart, language);
                // #204222 - below assert gets hit sometimes but I have no idea why.
                //     I'm commenting it out for now and hopefully it will result into
                //     some wrong formatting which users can report back and which would
                //     lead to answer why below assert is hit and in which concrete cases.
                //assert ts != null : "start="+start+" newStart="+newStart+" jts="+joinedTS;
                start = newStart;
            }
            startTime1 = System.currentTimeMillis();
            initialOffset = getFormatStableStart(joinedTS, start, end, rangesToIgnore);
            if (DEBUG_PERFORMANCE) {
                System.err.println("[IndPer] Locating FormatStableStart took "+(System.currentTimeMillis()-startTime1)+" ms");
                System.err.println("[IndPer] Current line index is: "+(Utilities.getLineOffset(doc, start)));
                System.err.println("[IndPer] FormatStableStart line starts at index: "+(Utilities.getLineOffset(doc, initialOffset)));
                System.err.println("[IndPer] Number of ranges to ignore: "+rangesToIgnore.ranges.size());
            }
            if (DEBUG && !rangesToIgnore.isEmpty()) {
                System.err.println("Ignored ranges: "+rangesToIgnore.dump());
            }
        }

        // list of lines with their indentation
        final List<Line> indentedLines = new ArrayList<Line>();

        startTime1 = System.currentTimeMillis();
        // get list of code blocks of our language in form of [line start number, line end number]
        List<LinePair> linePairs = calculateLinePairs(blocks, initialOffset, end);
        if (DEBUG_PERFORMANCE) {
            System.err.println("[IndPer] calculateLinePairs (total pairs="+linePairs.size()+") took "+(System.currentTimeMillis()-startTime1)+" ms");
        }
        if (DEBUG) {
            System.err.println("line pairs to process="+linePairs);
        }

        // process blocks of our language and record data for each line:
        startTime1 = System.currentTimeMillis();
        processLanguage(joinedTS, linePairs, initialOffset, end, indentedLines, rangesToIgnore);
        if (DEBUG_PERFORMANCE) {
            System.err.println("[IndPer] processLanguage ("+getContext().mimePath()+") took " +(System.currentTimeMillis()-startTime1)+" ms");
        }

        assert formattingContext.getIndentationData() != null;
        List<List<Line>> indentationData = formattingContext.getIndentationData();
        indentationData.add(indentedLines);
    }

    private int findPreviousOccuranceOfOurLanguage(JoinedTokenSequence<T1> ts, int start) throws BadLocationException {
        // this method finds previous non-empty line and
        // will try to find our language there:

        // find line start and move to previous line if possible:
        int lineStart = Utilities.getRowStart(getDocument(), start);
        if (lineStart > 0) {
            lineStart--;
        }
        // find first non-whitespace character going backwards:
        int offset = Utilities.getFirstNonWhiteRow(getDocument(), start, false);
        if (offset == -1) {
            offset = 0;
        }
        // find beginning of this line
        lineStart = Utilities.getRowStart(getDocument(), offset);

        // use line start as beginning for our language search:
        if (ts.move(lineStart, true)) {
            if (!ts.moveNext()) {
                ts.movePrevious();
            }
            offset = ts.offset();
            if (offset > start) {
                return -1;
            } else {
                return offset;
            }
        }
        return -1;
    }

    private void applyIndentation() throws BadLocationException {
        if (!formattingContext.isLastIndenter()) {
            // last formatter will apply changes
            return;
        }

        // recalcualte line numbers according to new offsets
        startTime1 = System.currentTimeMillis();
        recalculateLineIndexes();
        if (DEBUG_PERFORMANCE) {
            System.err.println("[IndPer] recalculateLineIndexes took "+(System.currentTimeMillis()-startTime1)+" ms");
        }

        // apply line data into concrete indentation:
        int lineStart = Utilities.getLineOffset(getDocument(), context.startOffset());
        int lineEnd = Utilities.getLineOffset(getDocument(), context.endOffset());
        assert formattingContext.getIndentationData() != null;
        List<List<Line>> indentationData = formattingContext.getIndentationData();

        startTime1 = System.currentTimeMillis();
        List<Line> indentedLines = mergeIndentedLines(indentationData);
        if (DEBUG_PERFORMANCE) {
            System.err.println("[IndPer] mergeIndentedLines took "+(System.currentTimeMillis()-startTime1)+" ms");
        }
        if (DEBUG) {
            System.err.println("Merged line data:");
            for (Line l : indentedLines) {
                debugIndentation(l.lineStartOffset, l.lineIndent, getDocument().getText(l.lineStartOffset, l.lineEndOffset-l.lineStartOffset+1).
                        replace("\n", "").replace("\r", "").trim(), l.indentThisLine);
            }
        }

        if (indentedLines.isEmpty()) {
            return;
        }
        applyIndents(indentedLines, lineStart, lineEnd);
    }

    private VirtualSource createVirtualSource() {
        String mimeType = (String)getDocument().getProperty("mimeType"); // NOI18N
        boolean isEmbedded = !getLanguage().mimeType().equals(mimeType);
        if (!isEmbedded) {
            return null;
        }
        for (VirtualSource.Factory factory : Lookup.getDefault().lookupAll(VirtualSource.Factory.class)) {
            VirtualSource vs = factory.createVirtualSource(getDocument(), language.mimeType());
            if (vs != null) {
                if (DEBUG) {
                    System.err.println("Virtual Source found:"+vs.toString());
                }
                return vs;
            }
        }
        return null;
    }

    private List<ForeignLanguageBlock> eliminateUnneededBlocks(List<ForeignLanguageBlock> blocks, List<Line> all) {
        List<ForeignLanguageBlock> newBlocks = new ArrayList<ForeignLanguageBlock>();

        // first eliminate blocks which are not foreign blocks and have their own formatter
        for (ForeignLanguageBlock b : blocks) {
            // if there is any line within foreign language block then it has its own formatter
            if (findLineByLineIndex(all, b.startLine+1) == null) {
                newBlocks.add(b);
            }
        }

        Comparator<ForeignLanguageBlock> c = new Comparator<ForeignLanguageBlock>() {
            @Override
            public int compare(ForeignLanguageBlock o1, ForeignLanguageBlock o2) {
                int res = o1.startLine - o2.startLine;
                if (res == 0) {
                    res = o1.endLine - o2.endLine;
                }
                return res;
            }
        };

        newBlocks.sort(c);
        List<ForeignLanguageBlock> result = new ArrayList<ForeignLanguageBlock>();
        for (ForeignLanguageBlock b : newBlocks) {
            addBlockAndMergeIfNeeded(result, b);
        }

        return result;
    }

    private void addBlockAndMergeIfNeeded(List<ForeignLanguageBlock> newBlocks, ForeignLanguageBlock toAdd) {
        if (newBlocks.isEmpty()) {
            newBlocks.add(toAdd);
            return;
        }
        ForeignLanguageBlock b = newBlocks.get(newBlocks.size()-1);
        // because blocks are sorted toAdd should never be bigger then already existing one:
        assert !(toAdd.startLine < b.startLine && toAdd.endLine > b.endLine) : "blocks: "+newBlocks+" toAdd:"+toAdd;

        if (toAdd.startLine >= b.startLine && toAdd.endLine <= b.endLine) {
            // already there
        } else if (toAdd.startLine >= b.startLine && toAdd.startLine <= b.endLine) {
            b.endLine = toAdd.endLine;
        } else {
            newBlocks.add(toAdd);
        }
    }

    private void extractForeignLanguageBlocks(List<ForeignLanguageBlock> blocks, List<Line> lines) throws BadLocationException {
        int start = -1;
        for (Line l : lines) {
            List<IndentCommand> cmds = new ArrayList<IndentCommand>();
            for (IndentCommand ic : l.lineIndent) {
                if (ic.getType() == IndentCommand.Type.BLOCK_START) {
                    // #195156 - not sure when this assert gets hit. disabling it
                    // and hopefully a resulting formatting if broken will get reported
                    // back
                    //assert start == -1 : ""+l;
                    start = l.index;
                } else if (ic.getType() == IndentCommand.Type.BLOCK_END) {
                    if (start == -1) {
                        // perhaps file is being editted and BLOCK_START is simply missing
                        // (you can simulate this by writing "<html> </style>"):
                        // do nothing
                    } else {
                        int end = l.index;
                        // ignore blocks which do not have 1 and more lines in them
                        if (end-start > 1) {
                            blocks.add(new ForeignLanguageBlock(start, end));
                        }
                        start = -1;
                    }
                } else {
                    cmds.add(ic);
                }
            }
            if (cmds.size() != l.lineIndent.size()) {
                l.lineIndent = cmds;
                if (cmds.isEmpty()) {
                    cmds.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, l.offset, getIndentationSize()));
                }
            }
        }
    }

    private void applyStoredBlocks(List<Line> all, List<ForeignLanguageBlock> blocks) {
        for (ForeignLanguageBlock b : blocks) {
            Line l = findLineByLineIndex(all, b.startLine);
            assert l != null : ""+b;
            l.foreignLanguageBlockStart = true;
            l = findLineByLineIndex(all, b.endLine);
            assert l != null : "fb="+b+" lines="+all;
            l.foreignLanguageBlockEnd = true;
        }
    }

    private List<Line> mergeIndentedLines(List<List<Line>> indentationData) throws BadLocationException {

        // iterate over individual List<Line> and translate CONTINUE 
        // to simple INDENT/RETURN commands
        for (List<Line> l : indentationData) {
            addLanguageEndLine(l);
        }

        // iterate over individual List<Line> and move indents after GAP
        // to special list of pairs [line number, commands]
        // + get rid of non-formattable lines; if non-formatable line contains
        // INDENT follow above procedure but set line number to removed line;
        List<ForeignLanguageBlock> blocks = new ArrayList<ForeignLanguageBlock>();
        List<LineCommandsPair> pairs = new ArrayList<LineCommandsPair>();
        for (List<Line> l : indentationData) {
            simplifyIndentationCommands(pairs, l);
            extractForeignLanguageBlocks(blocks, l);
            handleLanguageGaps(pairs, l);
            extractCommandsFromNonIndentableLines(pairs, l);
        }

        // merge all the lines
        List<Line> all = new ArrayList<Line>();
        for (List<Line> l : indentationData) {
            all = mergeProcessedIndentedLines(all, l);
        }

        if (all.isEmpty()) {
            return all;
        }
        // eliminate foreign language groups which have a formatter:
        blocks = eliminateUnneededBlocks(blocks, all);

        // apply collected blocks data on lines
        applyStoredBlocks(all, blocks);

        // apply stored commands per lines
        applyStoredCommads(all, pairs);

        return all;
    }

    private void handleLanguageGaps(List<LineCommandsPair> pairs, List<Line> lines) {
        List<Line> newLines = new ArrayList<Line>();
        Line prevLine = null;
        for (Line l : lines) {
            if (prevLine != null && prevLine.index+1 != l.index) {
                // there was a gap; move all INDENT commands from beginning of line:
                List<IndentCommand> removed = new ArrayList<IndentCommand>();
                List<IndentCommand> kept = new ArrayList<IndentCommand>();
                boolean keepRemoving = true;
                for (IndentCommand ic : l.lineIndent) {
                    if (keepRemoving && ic.getType() == IndentCommand.Type.INDENT) {
                        removed.add(ic);
                    } else {
                        kept.add(ic);
                        keepRemoving = false;
                    }
                }
                l.lineIndent = kept;
                if (l.lineIndent.isEmpty()) {
                    l.lineIndent.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, l.offset, getIndentationSize()));
                }
                if (removed.size() > 0) {
                    // should go to beginning of line:
                    pairs.add(new LineCommandsPair(prevLine.index+1, removed));
                }
            }
            newLines.add(l);
            prevLine = l;
        }
        lines.clear();
        lines.addAll(newLines);
    }

    private void extractCommandsFromNonIndentableLines(List<LineCommandsPair> pairs, List<Line> lines) {
        List<Line> newLines = new ArrayList<Line>();
        Line previousLine = null;
        for (Line l : lines) {
            if (!l.indentThisLine) {
                List<IndentCommand> accepted = new ArrayList<IndentCommand>();
                List<IndentCommand> nextLine = new ArrayList<IndentCommand>();
                for (IndentCommand ic : l.lineIndent) {
                    if (ic.getType() == IndentCommand.Type.INDENT) {
                        // should go to beginning of line:
                        accepted.add(ic);
                    } else if (ic.getType() == IndentCommand.Type.RETURN) {
                        if ((previousLine == null || previousLine.index+1 != l.index) && !l.emptyLine) {
                            // if this is first line of language block then put commands
                            // on next line rather than current, for example
                            //
                            // 05:      color:red;
                            // 06:   } </style>
                            //
                            // line 6 is end of CSS within HTML's <style> tag and
                            // if RETURN command corresponding to </style> was
                            // applied on line 06 it would be wrong - it needs to
                            // be applied to next line and line 6 should be indented
                            // fully by CSS formatter

                            // should go to beginning of line:
                            nextLine.add(ic);
                        } else {
                            // should go to beginning of line:
                            accepted.add(ic);
                        }
                    }
                }
                if (accepted.size() > 0) {
                    pairs.add(new LineCommandsPair(l.index, accepted));
                }
                if (nextLine.size() > 0) {
                    pairs.add(new LineCommandsPair(l.index+1, nextLine));
                }
                l.lineIndent = new ArrayList<IndentCommand>();
                l.lineIndent.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, l.offset, getIndentationSize()));
                newLines.add(l);
            } else {
                newLines.add(l);
            }
            previousLine = l;
        }
        lines.clear();
        lines.addAll(newLines);
    }

    private void addLanguageEndLine(List<Line> lines) throws BadLocationException {
        // check what last line of language suggests about next line:
        if (lines.isEmpty()) {
            return;
        }
        Line lastLine = lines.get(lines.size()-1);
        if (lastLine.preliminaryNextLineIndent.isEmpty()) {
            return;
        }
        int lineIndex = lastLine.index+1;
        int offset = Utilities.getRowStartFromLineOffset(getDocument(), lineIndex);
        if (offset == -1) {
            return;
        }
        Line l = generateBasicLine(lineIndex);
        l.indentThisLine = false;
        l.lineIndent = new ArrayList<IndentCommand>(lastLine.preliminaryNextLineIndent);
        lines.add(l);
    }

    /**
     * Merge lines and if there are two lines then accept one which is indentable.
     */
    private static List<Line> mergeProcessedIndentedLines(List<Line> originalLines, List<Line> newLines) {
        List<Line> merged = new ArrayList<Line>();
        Iterator<Line> it1 = originalLines.iterator();
        Iterator<Line> it2 = newLines.iterator();
        Line l1 = null;
        Line l2 = null;
        if (it1.hasNext()) {
            l1 = it1.next();
        }
        if (it2.hasNext()) {
            l2 = it2.next();
        }
        boolean move1;
        boolean move2;
        while (l1 != null && l2 != null) {

            if (l1.index < l2.index) {
                merged.add(l1);
                move1 = true;
                move2 = false;
            } else if (l1.index > l2.index) {
                merged.add(l2);
                move1 = false;
                move2 = true;
            } else {
                if (l1.indentThisLine) {
//                    assert !l2.indentThisLine :
//                        "two lines which claim to be indentable: l1="+l1+" l2="+l2;
                    merged.add(l1);
                } else {
                    assert !l1.indentThisLine;
                    merged.add(l2);
                }
                move1 = true;
                move2 = true;
            }
            if (move1) {
                if (it1.hasNext()) {
                    l1 = it1.next();
                } else {
                    l1 = null;
                }
            }
            if (move2) {
                if (it2.hasNext()) {
                    l2 = it2.next();
                } else {
                    l2 = null;
                }
            }
        }

        if (l1 != null) {
            merged.add(l1);
        } else if (l2 != null) {
            merged.add(l2);
        }
        while (it1.hasNext()) {
            merged.add(it1.next());
        }
        while (it2.hasNext()) {
            merged.add(it2.next());
        }

        return merged;
    }

    /**
     * Apply indent commands which we collected previously. These might be for
     * example commands from non-indentable lines, etc.
     */
    private void applyStoredCommads(List<Line> all, List<LineCommandsPair> pairs) throws BadLocationException {
        Comparator<LineCommandsPair> c = new Comparator<LineCommandsPair>() {
            @Override
            public int compare(LineCommandsPair o1, LineCommandsPair o2) {
                return o1.line - o2.line;
            }
        };
        //
        // sort pairs and then merge them; there can be multiple pairs
        // for one line and their order should be preserved, eg.
        //
        // 01: <table    id=smth
        // 02:           class=smth>
        // 03:    <p>
        //
        // line 3 will have RETURN and INDENT commands and they have to stay
        // in that order. If swapped the resulting indent would be
        //
        // 03:           <p>
        //
        // line was first indented and then returned back to previous position
        // which alligns with "class" attribute
        //
        List<LineCommandsPair> s1 = new ArrayList<LineCommandsPair>(pairs);
        s1.sort(c);
        List<LineCommandsPair> s2 = new ArrayList<LineCommandsPair>();
        LineCommandsPair previous = null;
        // merge commands on one line:
        for (LineCommandsPair pair : s1) {
            if (previous != null && previous.line == pair.line) {
                previous.commands.addAll(pair.commands);
            } else {
                s2.add(pair);
                previous = pair;
            }
        }

        Iterator<Line> it = all.iterator();
        assert all.size() > 0;
        Line l = null;
        Line lastLine = null;
        for (LineCommandsPair pair : s2) {
            while (it.hasNext() && (l == null || l.index < pair.line)) {
                l = it.next();
            }
            assert l != null;
            if (l.index >= pair.line) {
                List<IndentCommand> commands = new ArrayList<IndentCommand>(pair.commands);
                for (IndentCommand ic : l.lineIndent) {
                    if (ic.getType() != IndentCommand.Type.NO_CHANGE ||
                            (ic.getType() == IndentCommand.Type.NO_CHANGE && commands.isEmpty())) {
                        commands.add(ic);
                    }
                }
                l.lineIndent = commands;
            } else {
                assert !it.hasNext();
                // put all commands on the last line;
                // that should do the trick
                if (lastLine == null) {
                    int offset = Utilities.getRowStartFromLineOffset(getDocument(), pair.line);
                    if (offset == -1) {
                        // lines does not exist so ignore:
                        break;
                    }
                    lastLine = generateBasicLine(pair.line);
                    lastLine.lineIndent = new ArrayList<IndentCommand>(pair.commands);
                } else {
                    lastLine.lineIndent.addAll(pair.commands);
                }
            }
        }
        if (lastLine != null) {
            all.add(lastLine);
        }

    }

    /**
     * Replace CONTINUE with simple INDENT and RETURN commands.
     */
    private void simplifyIndentationCommands(List<LineCommandsPair> pairs, List<Line> lines) {
        boolean firstContinue = true;
        boolean inContinue = false;
        boolean fixedIndentContinue = false;
        Line lastLineWithContinue = null;
        for (Line l : lines) {
            List<IndentCommand> commands = new ArrayList<IndentCommand>();
            for (IndentCommand ic : l.lineIndent) {
                if (ic.getType() == IndentCommand.Type.CONTINUE) {
                    if (firstContinue) {
                        if (ic.getFixedIndentSize() != -1) {
                            IndentCommand ic2 = new IndentCommand(IndentCommand.Type.INDENT, ic.getLineOffset(), getIndentationSize());
                            ic2.setFixedIndentSize(ic.getFixedIndentSize());
                            ic2.setWasContinue();
                            commands.add(ic2);
                            fixedIndentContinue = true;
                        } else {
                            IndentCommand ic2 = new IndentCommand(IndentCommand.Type.INDENT, ic.getLineOffset(), getIndentationSize());
                            ic2.setWasContinue();
                            commands.add(ic2);
                            //commands.add(new IndentCommand(IndentCommand.Type.INDENT, ic.getLineOffset()));
                            fixedIndentContinue = false;
                        }
                        firstContinue = false;
                        inContinue = true;
                    }
                    lastLineWithContinue = l;
                } else {
                    // if we are in CONTINUE mode and there is PRESERVE_INDENTATION then
                    // do not abort CONTINUE; for example when there is comment within
                    // multiline statement then comment will PRESERVE_INDENTATION
                    if (inContinue && ic.getType() != IndentCommand.Type.PRESERVE_INDENTATION) {
                        List<IndentCommand> listToAddTo = commands;
                        assert lastLineWithContinue != null;
                        if (l.index - lastLineWithContinue.index > 1) {
                            List<IndentCommand> list = new ArrayList<IndentCommand>();
                            // should go to beginning of line:
                            pairs.add(new LineCommandsPair(lastLineWithContinue.index+1, list));
                            listToAddTo = list;
                        }
                        if (fixedIndentContinue) {
                            listToAddTo.add(new IndentCommand(IndentCommand.Type.RETURN, ic.getLineOffset(), getIndentationSize()));
                        } else {
                            listToAddTo.add(new IndentCommand(IndentCommand.Type.RETURN, ic.getLineOffset(), getIndentationSize()));
                            //listToAddTo.add(new IndentCommand(IndentCommand.Type.RETURN, ic.getLineOffset()));
                        }
                        inContinue = false;
                        firstContinue = true;
                    }
                    if (ic.getType() != IndentCommand.Type.NO_CHANGE || 
                            (ic.getType() == IndentCommand.Type.NO_CHANGE && commands.isEmpty())) {
                        commands.add(ic);
                    }
                }
            }
            if (commands.isEmpty()) {
                IndentCommand ic2 = new IndentCommand(IndentCommand.Type.NO_CHANGE, l.lineStartOffset, getIndentationSize());
                if (inContinue) {
                    ic2.setWasContinue();
                }
                commands.add(ic2);

            }
            l.lineIndent = commands;
        }
    }

    private void updateLineOffsets(List<IndenterFormattingContext.Change> l) {
        if (DEBUG) {
            System.err.println("update line offset with following deltas:"+l);
        }
        for (List<Line> lines : formattingContext.getIndentationData()) {
            for (Line line : lines) {
                for (IndenterFormattingContext.Change ch : l) {
                    if (ch.offset <= line.offset) {
                        line.updateOffset(ch.change);
                    }
                }
            }
        }
    }

    private void recalculateLineIndexes() throws BadLocationException {
        for (List<Line> lines : formattingContext.getIndentationData()) {
            List<Line> l = new ArrayList<Line>();
            Line previousLine = null;
            for (Line line : lines) {
                line.recalculateLineIndex(getDocument());

                // Java formatter is touching lines it should not, for example:
                //
                // 01: <html>
                // 02:        <%= response.SC_ACCEPTED
                // 03:                %>
                // 04: </html>
                //
                // will be changed by Java formatter to:
                //
                // 01: <html>
                // 02:        <%= response.SC_ACCEPTED %>
                // 03: </html>
                //
                // which screws up line 3 previously owned by JSP formatter
                //
                if (previousLine != null && previousLine.index == line.index) {
                    if (DEBUG) {
                        System.err.println("WARNING: some lines where deleted by " +
                                "other formatter. merging "+previousLine+" with "+line);
                    }
                    // TODO: review / test this:
                    // add all commands to previous line and drop it:
                    previousLine.lineIndent.addAll(line.lineIndent);
                } else {
                    l.add(line);
                }
                previousLine = line;
            }
            if (l.size() != lines.size()) {
                lines.clear();
                lines.addAll(l);
            }
        }
    }

//    private boolean isJustAfterOurLanguage() {
//        // get start of formatted area
//        int start = context.startOffset();
//        if (start > 0) {
//            start--;
//        }
//
//        while (start > 0) {
//            try {
//                String text = getDocument().getText(start, 1).trim();
//                if (text.length() > 0) {
//                    System.err.println("isJustAfterOurLanguage found: "+text+" at "+start);
//                    break;
//                }
//                start--;
//            } catch (BadLocationException ex) {
//                Exceptions.printStackTrace(ex);
//                return false;
//            }
//        }
//        if (start == 0) {
//            return false;
//        }
//        Language l = LexUtilities.getLanguage(getDocument(), start);
//        System.err.println("isJustAfterOurLanguage lang:"+l);
//        return (l != null && l.equals(language));
//    }

    private List<LinePair> calculateLinePairs(List<JoinedTokenSequence.CodeBlock<T1>> blocks, int startOffset, int endOffset) throws BadLocationException {
        List<LinePair> lps = new ArrayList<LinePair>();
        LinePair lastOne = null;
        int startLine = Utilities.getLineOffset(getDocument(), startOffset);
        int endLine = Utilities.getLineOffset(getDocument(), endOffset);
        for (JoinedTokenSequence.CodeBlock<T1> block : blocks) {
            for (JoinedTokenSequence.TokenSequenceWrapper<T1> tsw : block.tss) {
                if (tsw.isVirtual()) {
                    continue;
                }
                LinePair lp = new LinePair();
                lp.startingLine = Utilities.getLineOffset(getDocument(), LexUtilities.getTokenSequenceStartOffset(tsw.getTokenSequence()));
                lp.endingLine = Utilities.getLineOffset(getDocument(), LexUtilities.getTokenSequenceEndOffset(tsw.getTokenSequence()));
                if (lp.startingLine > endLine) {
                    break;
                }
                if (lp.startingLine < startLine) {
                    if (startLine <= lp.endingLine) {
                        lp.startingLine = startLine;
                    } else {
                        continue;
                    }
                }
                if (lp.endingLine > endLine) {
                    lp.endingLine = endLine;
                }
                if (lastOne != null && lastOne.endingLine == lp.startingLine) {
                    lastOne.endingLine = lp.endingLine;
                } else {
                    lps.add(lp);
                    lastOne = lp;
                }
            }
        }
        return lps;
    }

//    protected int getBlockIndent(int offset, int indentSize) throws BadLocationException {
//        BaseDocument doc = getDocument();
//        int start = Utilities.getRowStart(doc, offset);
//        start = Utilities.getFirstNonWhiteRow(doc, start-1, false);
//        TokenSequence<? extends TokenId> ts = TokenHierarchy.get((Document)doc).tokenSequence();
//        ts.move(start);
//        ts.movePrevious();
//        ts.moveNext();
//        if (ts.language().equals(language) && ts.embedded() == null) {
//            return 0;
//        } else {
//            return indentSize;
//        }
//    }

    /**
     * Iterates over given code blocks (decribed as pairs of start and end line)
     * and calls formatter on each line.
     *
     * Line can be skipped in two cases:
     * #1) line is blank and its first token is not of language of this formatter; or
     * #2) line does not start with language of our formatter and our language
     * is represented on the line only via whitespace tokens which can be ignored.
     *
     * If line does not start with language of the formatter then line.indentThisLine
     * is set to false.
     */
    private void processLanguage(JoinedTokenSequence<T1> joinedTS, List<LinePair> lines,
            int overallStartOffset, int overallEndOffset,
            List<Line> lineIndents,
            OffsetRanges rangesToIgnore) throws BadLocationException {

        BaseDocument doc = getDocument();

        joinedTS.moveStart();
        joinedTS.moveNext();

        // iterate over blocks of code to indent:
        for (LinePair lp : lines) {

            // check last line of this language block and it if is non-empty
            // but does not contain "our" language then use previous line as
            // last one; and do the same for first line:
            int realStartingLine = lp.startingLine;
            int realEndingLine = lp.endingLine;
            if (realEndingLine > realStartingLine) {
                // update realStartingLine:
                if (!doesLineStartWithOurLanguage(doc, realStartingLine, joinedTS)) {
                    realStartingLine++;
                }
            }
            if (realEndingLine > realStartingLine) {
                // update realEndingLine:
                if (!doesLineStartWithOurLanguage(doc, realEndingLine, joinedTS)) {
                    realEndingLine--;
                }
            }

            // iterate over each line:
            for (int line = lp.startingLine; line <= lp.endingLine; line++) {

                // find line starting offset
                int rowStartOffset = Utilities.getRowStartFromLineOffset(doc, line);
                if (rowStartOffset < overallStartOffset) {
                    rowStartOffset = overallStartOffset;
                }

                // find first non-white character
                int firstNonWhite = Utilities.getRowFirstNonWhite(doc, rowStartOffset);

                // find line ending offset
                int rowEndOffset = Utilities.getRowEnd(doc, rowStartOffset);
                int nextLineStartOffset = rowEndOffset+1;

                // check whether line can be skip completely:
                if (rangesToIgnore.contains(rowStartOffset, rowEndOffset)) {
                    continue;
                }

                boolean indentThisLine = true;
                boolean emptyLine = false;

                if (firstNonWhite != -1) {
                    // line contains some characters:
                    // move rowStartOffset to beginning of language
                    int newRowStartOffset = findLanguageOffset(joinedTS, rowStartOffset, rowEndOffset, true);
                    if (newRowStartOffset > overallEndOffset) {
                        continue;
                    }
                    // move rowEndOffset to end of language
                    rowEndOffset = findLanguageOffset(joinedTS, rowEndOffset, rowStartOffset, false);
                    rowStartOffset = newRowStartOffset;
                    // if this line does not contain any our "language" to format 
                    // then skip this line completely
                    if (rowStartOffset == -1 || rowEndOffset == -1) {
                        continue;
                    }

                    if (rowStartOffset > rowEndOffset) {
                        // trying to handle properly following CSS case:
                        //
                        // 03:       }/*CC */
                        // 04:   </style>
                        //
                        // when line 4 is being processed the CSS token text
                        // is "/*CC */\n        " which results in rowEndOffset
                        // pointing just after '\n' and rowStartOffset just to
                        // the end of token text. This happens because token is
                        // not whitespace. For now I will ignore these lines and see.
                        continue;
                    }

                    if (rowEndOffset > overallEndOffset) {
                        rowEndOffset = overallEndOffset;
                    }
                    
                    // set indentThisLine to false if line does not start with language
                    // but process tokens from the line
                    indentThisLine = firstNonWhite == rowStartOffset;
                    if (!indentThisLine) {
                        /*
                         * line of XHTML code containing just EL, eg. "#{bean.smth}",
                         * results in firstNonWhite==0 and rowStartOffset==2 ('#{' does not get any EL token).
                         * that's a reason for below hack:
                         */
                        if (rowStartOffset - firstNonWhite == 2) {
                            String st = doc.getText(firstNonWhite, 2);
                            if ("#{".equals(st) || "${".equals(st)) {
                                indentThisLine = true;
                            }
                        }
                    }
                } else {
                    // line is empty:
                    emptyLine = true;

                    Language l = LexUtilities.getLanguage(getDocument(), rowStartOffset);
                    if (l == null || !l.equals(language)) {
                        // line is empty and first token on line is not from our language
                        continue;
                    }
                }

                int newOffset[] = new int[2];

                if (rangesToIgnore.calculateUncoveredArea(rowStartOffset, rowEndOffset, newOffset)) {
                    rowStartOffset = newOffset[0];
                    rowEndOffset = newOffset[1];
                    if (rowStartOffset == -1 && rowEndOffset == -1) {
                        continue;
                    }
                }

                // firstNonWhite must be within our language:
                if (firstNonWhite < rowStartOffset) {
                    firstNonWhite = rowStartOffset;
                }

                // ask formatter for line indentation:
                IndenterContextData<T1> cd = new IndenterContextData<T1>(joinedTS, rowStartOffset, rowEndOffset, firstNonWhite, nextLineStartOffset, emptyLine, indentThisLine);
                cd.setLanguageBlockStart(line == realStartingLine);
                cd.setLanguageBlockEnd(line == realEndingLine);
                List<IndentCommand> preliminaryNextLineIndent = new ArrayList<IndentCommand>();
                List<IndentCommand> iis = getLineIndent(cd, preliminaryNextLineIndent);
                if (iis.isEmpty()) {
                    throw new IllegalStateException("getLineIndent must always return at least IndentInstance.Type.NO_CHANGE");
                }
                if (preliminaryNextLineIndent.isEmpty()) {
                    throw new IllegalStateException("preliminaryNextLineIndent from getLineIndent must always return at least IndentInstance.Type.NO_CHANGE");
                }

                // record line indentation:
                Line ln = generateBasicLine(line);
                ln.lineIndent = iis;
                ln.preliminaryNextLineIndent = preliminaryNextLineIndent;
                ln.lineStartOffset = rowStartOffset;
                ln.lineEndOffset = rowEndOffset;
                ln.indentThisLine = indentThisLine;
                ln.emptyLine = emptyLine;
                lineIndents.add(ln);

                // debug line:
                if (DEBUG) {
                    debugIndentation(cd.getLineStartOffset(), iis, getDocument().getText(rowStartOffset, rowEndOffset-rowStartOffset+1).
                            replace("\n", "").replace("\r", "").trim(), ln.indentThisLine);
                }
            }
        }
        if (DEBUG && lineIndents.size() > 0) {
            List<IndentCommand> l = lineIndents.get(lineIndents.size()-1).preliminaryNextLineIndent;
            if (l.size() > 0) {
                System.err.println("Preliminary indent commands for next line:"+l);
            }
        }
    }

    private boolean doesLineStartWithOurLanguage(BaseDocument doc, int lineIndex, JoinedTokenSequence<T1> joinedTS) throws BadLocationException {
        int rowStartOffset = Utilities.getRowStartFromLineOffset(doc, lineIndex);
        int rowEndOffset = Utilities.getRowEnd(doc, rowStartOffset);
        int firstNonWhite = Utilities.getRowFirstNonWhite(doc, rowStartOffset);
        if (firstNonWhite != -1) {
            // there is something on the line:
            int newRowStartOffset = findLanguageOffset(joinedTS, rowStartOffset, rowEndOffset, true);
            if (newRowStartOffset == -1) {
                // but it is not our langauge
                return false;
            }
        }
        return true;
    }

    private int findLanguageOffset(JoinedTokenSequence<T1> joinedTS, int rowStartOffset, int rowEndOffset, boolean forward) {
        if (!joinedTS.move(rowStartOffset, forward)) {
            return -1;
        }
        if (!joinedTS.moveNext()) {
            if (!forward) {
                if (!joinedTS.movePrevious()) {
                    return -1;
                }
            } else {
                return -1;
            }
        }

        while ((forward ? joinedTS.offset() <= rowEndOffset :
                        (joinedTS.offset()+joinedTS.token().length()) >= rowEndOffset)) {
            int tokenStart = joinedTS.offset();
            int tokenEnd = joinedTS.offset() + joinedTS.token().length();
            boolean ok = joinedTS.embedded() == null;
            if (!ok) {
                // there is some embedding; check for example for following case:
                //
                // 01: ${expression}
                //
                // this JSP code results in JSP token for "${expression}"
                // which contains embedded EL token for "expression". Following
                // code tries to treat "${" and "}" as JSP code and rest as EL code:
                TokenSequence<? extends TokenId> ts = joinedTS.embedded();
                if (!ts.isEmpty()) {
                    int start = LexUtilities.getTokenSequenceStartOffset(ts);
                    int end = LexUtilities.getTokenSequenceEndOffset(ts);
                    if (forward) {
                        if (start > tokenStart && tokenStart >= rowStartOffset) {
                            ok = true;
                            tokenEnd = tokenStart + (start - tokenStart);
                        } else if (end < tokenEnd && end >= rowStartOffset) {
                            ok = true;
                            tokenStart = end;
                        }
                    } else {
                        if (end < tokenEnd && tokenEnd <= rowStartOffset) {
                            ok = true;
                            tokenStart = end;
                        } else if (start > tokenStart && start <= rowStartOffset) {
                            ok = true;
                            tokenEnd = tokenStart + (start - tokenStart);
                        }
                    }
                }
            }
            if (ok && joinedTS.language() == language &&
                    !joinedTS.isCurrentTokenSequenceVirtual()) {
                boolean ws = isWhiteSpaceToken(joinedTS, rowStartOffset, rowEndOffset, forward);
                if (!ws) {
                    int offset;
                    if (rowStartOffset >= tokenStart && rowStartOffset <= tokenEnd) {
                        offset = rowStartOffset;
                    } else if (rowStartOffset < tokenStart) {
                        offset = tokenStart;
                    } else {
                        offset = tokenEnd;
                    }
                    offset = findNonWhiteSpaceCharacter(joinedTS, offset, forward);
                    return offset;
                }
            }
            if ((forward ? !joinedTS.moveNext() : !joinedTS.movePrevious())) {
                break;
            }
        }

        return -1;
    }

    /**
     * Check whether token text within given boundary is whitespace or not.
     */
    private boolean isWhiteSpaceToken(JoinedTokenSequence<T1> joinedTS, int rowStartOffset, int rowEndOffset, boolean forward) {
        int start = joinedTS.offset();
        int end = joinedTS.offset() + joinedTS.token().length();
        if (forward) {
            if (rowStartOffset > start) {
                start = Math.min(rowStartOffset, end);
            }
            if (rowEndOffset < end) {
                end = Math.max(rowEndOffset, start);
            }
        } else {
            if (rowEndOffset > start) {
                start = Math.min(rowEndOffset, end);
            }
            if (rowStartOffset < end) {
                end = Math.max(rowStartOffset, start);
            }
        }
        return CharSequenceUtilities.trim(joinedTS.token().text().subSequence(
                start-joinedTS.offset(), end-joinedTS.offset())).length() == 0;
    }

    private int findNonWhiteSpaceCharacter(JoinedTokenSequence<T1> joinedTS, int offset, boolean forward) {
        CharSequence tokenText = joinedTS.token().text();
        int tokenStart = joinedTS.offset();
        int index = offset - tokenStart;
        if (!forward && index == tokenText.length()) {
            index--;
        }
        while ((forward ? index < tokenText.length() : index > 0) &&
                tokenText.charAt(index) == ' ' || tokenText.charAt(index) == '\t') {
            if (forward) {
                index++;
            } else {
                index--;
            }
        }
        return tokenStart+index;

    }

    /**
     * This method is called when calculateLineIndent is processing RETURN command -
     * it iterates backward over list of all commands and tries to find corresponding
     * INDENT command which is being closed by the RETURN command.
     */
    private static int getCalulatedIndexOfPreviousIndent(List<IndentCommand> indentations, int shift) {
        int balance = 1;
        int i = indentations.size();
        if (i == 0) {
            return -1;
        }
        do {
            i--;
            if (indentations.get(i).getType() == IndentCommand.Type.RETURN) {
                balance++;
            }
            if (indentations.get(i).getType() == IndentCommand.Type.INDENT) {
                balance--;
            }
        } while (balance != 0 && i > 0);
        if (balance != 0 || indentations.get(i).getType() != IndentCommand.Type.INDENT) {
            if (DEBUG) {
                System.err.println("WARNING: cannot find INDENT command corresponding to RETURN " +
                        "command at index "+(indentations.size()-1)+". make sure RETURN and INDENT commands are always paired. " +
                        "this can be caused by wrong getFormatStableStart but also by user typing code which is not " +
                        "syntactically correct. commands:"+(indentations.size() < 30 ? indentations : "[too many commands]"));
            }
            if (i+shift < 0) {
                i = 0 - shift;
            }
        }
        if (i+shift < 0) {
            return -1;
        }
        return i+shift;
    }

    /**
     * Calculate indenation of this line for given base indentation and given
     * line indentation commands. This method can be called with update set to
     * false and with preliminary line commands given in currentLineIndents to
     * calculated indent of next line.
     */
    private int calculateLineIndent(int indentation, List<Line> lines, Line line,
            List<IndentCommand> currentLineIndents, List<IndentCommand> allPreviousCommands,
            boolean update, int lineStart) throws BadLocationException {

        // eliminate matching INDENT and RETURN commands
        // and clone commands if update is not desirable so that any changes are thrown away
        currentLineIndents = cleanUpAndPossiblyClone(currentLineIndents, !update);

        boolean beingFormatted = line != null ? line.index >= lineStart : false;
        int thisLineIndent = 0;
        
        //UnmodifiableButExtendableList assumptions:
        //1. allCommands does not escape from this context,
        //2. only get(index), size() and add(object) methods are called on it.
        List<IndentCommand> allCommands = new UnmodifiableButExtendableList<IndentCommand>(allPreviousCommands);
        
        int preservedLineIndentation = -1;

        // iterate over indent commands for the given line and calculate line's indentation
        for (IndentCommand ii : currentLineIndents) {

            switch (ii.getType()) {

                case NO_CHANGE:
                    break;

                case INDENT:
                    if (ii.getFixedIndentSize() != -1) {
                        thisLineIndent = ii.getFixedIndentSize();
                    } else {
                        thisLineIndent += ii.getIndentationSize() != -1 ?
                                ii.getIndentationSize() : indentationSize;
                    }
                    break;

                case RETURN:
                    // find index of INDENT command which is being closed by this
                    // RETURN command and move to previous indent command (shift is -1)
                    // which indent will be used as a base, eg:
                    //
                    // 01: if (a) {          NO_CHANGE
                    // 02:  something1();    INDENT
                    // 03:  something2();    NO_CHANGE
                    // 04: }                 RETURN
                    //
                    // when RETURN command is found on line 04 it finds opening INDENT
                    // command on line 02 and moves one command prior to that:
                    // command NO_CHANGE from line 01. That one is going to be
                    // used as base for indentation of line 04
                    int index = getCalulatedIndexOfPreviousIndent(allCommands, -1);
                    if (index != -1) {
                        // use indentation of found command and override any indent
                        // calculated so far
                        indentation = allCommands.get(index).getCalculatedIndentation();
                        thisLineIndent = 0;
                    }
                    break;

                case DO_NOT_INDENT_THIS_LINE:
                    if (update) {
                        line.indentThisLine = false;
                    }
                    break;

                case PRESERVE_INDENTATION:
                    if (update) {
                        line.preserveThisLineIndent = true;
                        // first formattable line (in indentation mode) should be
                        // formatted even though it's got PRESERVE_INDENTATION command;
                        // but only if fixed indent size for the command was set
                        if (line.index == lineStart && ii.getFixedIndentSize() != -1 && context.isIndent()) {
                            preservedLineIndentation = ii.getFixedIndentSize();
                        }
                    }
                    break;
            }
            ii.setCalculatedIndentation(indentation+thisLineIndent);
            allCommands.add(ii);
        }

        // indentation of this line is given by indentation of last command:
        int lineIndentation = currentLineIndents.get(currentLineIndents.size()-1).getCalculatedIndentation();

        // check whether value needs to be adjusted:
        int lineIndentAdjustment = 0;
        if (line != null && !line.preserveThisLineIndent && !beingFormatted && !line.emptyLine) {
            // calculate line adjustment for this line
            //
            // what is this line adjustment??
            //
            // it is difference between our calculated indentation and
            // line's current indentation, eg.:
            //
            //                     CURRENT   CALCULATED   ADJUSTMENT
            // 01:if (a) {         0         0            0
            // 02: if (b) {        1         4            -3
            // 03:  if (c)         2         8            -6
            // 04:      smth();              12
            //
            // if only line 4 is being indented than calculated indent 12
            // needs to be adjusted by -6 which is current-ident - calculated-indent
            // of previous (non-empty and non-continuation) line (2 - 8 = -6);
            // resulting indentation for line 4 will be 12 + (-6) = 6
            //
            lineIndentAdjustment = line.existingLineIndent - lineIndentation;
        }
        
        // update last command on line with lineIndentAdjustment. last command on
        // line represents overall line indentation and there should be adjusted
        IndentCommand ii = currentLineIndents.get(currentLineIndents.size()-1);
        if (lineIndentAdjustment != 0 && !beingFormatted) {
            ii.setCalculatedIndentation(ii.getCalculatedIndentation() + lineIndentAdjustment);
        }

        // if we are formatting this line then re-read this line's indent
        if (beingFormatted) {
            lineIndentation = currentLineIndents.get(currentLineIndents.size()-1).getCalculatedIndentation();
        }

        if (update) {
            line.indentation = lineIndentation;
            line.indentationAdjustment = lineIndentAdjustment;
        }

        if (preservedLineIndentation != -1) {
            // in this case override line's indentation:
            line.indentation = preservedLineIndentation;
            assert line.indentationAdjustment == 0;
        }

        return currentLineIndents.get(currentLineIndents.size()-1).getCalculatedIndentation();
    }

    private List<IndentCommand> cleanUpAndPossiblyClone(List<IndentCommand> commands, boolean clone) {
        // cleanup was removed so for now just cloning:
        if (!clone) {
            return commands;
        }
        List<IndentCommand> newItems = new ArrayList<IndentCommand>();
        for (int i=0; i<commands.size(); i++) {
            IndentCommand item = commands.get(i);
            newItems.add(clone ? item.cloneMe() : item);
        }
        return newItems;
    }

//    /**
//     * Returns true if line has CONTINUE command.
//     */
//    private static boolean isContinueLine(Line l) {
//        return l.lineIndent.get(l.lineIndent.size()-1).wasContinue();
//    }

    private Line findLineByLineIndex(List<Line> lines, int index) {
        for (Line l : lines) {
            if (l == null) {
                continue;
            }
            if (l.index == index) {
                return l;
            } else if (l.index > index) {
                break;
            }
        }
        return null;
    }

//    private Line findPreviousLine(List<Line> lines, int index,
//            boolean canBeEmpty, boolean canBeContinue, boolean canBePreserved) {
//        for (int i = lines.size()-1; i>=0; i--) {
//            Line l = lines.get(i);
//            if (l.index < index) {
//                if ((canBeEmpty || !l.emptyLine) && (canBeContinue || !isContinueLine(l)) &&
//                        (canBePreserved || !l.preserveThisLineIndent)) {
//                    return l;
//                }
//            }
//        }
//        return null;
//    }

    private void applyIndents(final List<Line> indentedLines,
            final int lineStart, final int lineEnd) throws BadLocationException {
        final BadLocationException ex[] = new BadLocationException[1];
        getDocument().runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    applyIndents0(indentedLines, lineStart, lineEnd);
                } catch (BadLocationException ble) {
                    ex[0] = ble;
                }
            }
        });
        if (ex[0] != null) {
            throw ex[0];
        }
    }

    private void applyIndents0(List<Line> indentedLines,
            int lineStart, int lineEnd) throws BadLocationException {

        if (DEBUG) {
            System.err.println(">> reindentation done by all AbstractIndenter subclasses:");
        }

        // indentation should indent empty lines; format should not
        boolean indentEmptyLines = context.isIndent();

        int indentation = 0;
        List<IndentCommand> commands = new ArrayList<IndentCommand>();

        int nextLineIndent = -1;
        Line previousLine = null;
        Map<Integer, Integer> suggestedIndentsForOtherLines = new HashMap<Integer, Integer>();

        // iterate over lines indentation commands and calculate real indentation
        startTime1 = System.currentTimeMillis();
        for (Line line : indentedLines) {

            if (previousLine != null && previousLine.index+1 != line.index) {
                // there was a gap: store what the identation of such lines should
                // be for other formatters to follow. for example Ruby formatter
                // expects it.
                for (int i = previousLine.index+1; i < line.index; i++) {
                    suggestedIndentsForOtherLines.put(i, nextLineIndent);
                }
            }

            // calculate indentation:
            indentation = calculateLineIndent(indentation, indentedLines, line,
                    line.lineIndent, commands, true, lineStart);

            // force zero indent if line is empty and empty lines should not be indented
            if (line.emptyLine && !indentEmptyLines) {
                line.indentation = 0;
            }

            commands.addAll(line.lineIndent);

            if (!line.indentThisLine) {
                // if we are not indenting this line store indent for other formatter:
                suggestedIndentsForOtherLines.put(line.index, indentation);
            }
            nextLineIndent = calculateLineIndent(indentation, indentedLines, null,
                    line.preliminaryNextLineIndent, commands, false, lineStart);
            previousLine = line;
        }
        if (DEBUG_PERFORMANCE) {
            System.err.println("[IndPer] calculateLineIndent took "+(System.currentTimeMillis()-startTime1)+" ms");
        }

        for (int i = previousLine.index+1; i <= lineEnd; i++) {
            // store indent for all other lines to the end of document:
            suggestedIndentsForOtherLines.put(i, nextLineIndent);
        }

        // set line indent for preserved lines:
        startTime1 = System.currentTimeMillis();
        updateIndentationForPreservedLines(indentedLines, context.isIndent() ? lineStart : -1);
        if (DEBUG_PERFORMANCE) {
            System.err.println("[IndPer] updateIndentationForPreservedLines took "+(System.currentTimeMillis()-startTime1)+" ms");
        }

        // generate line indents for lines within a block:
        startTime1 = System.currentTimeMillis();
        indentedLines = generateBlockIndentsForForeignLanguage(indentedLines, suggestedIndentsForOtherLines);
        if (DEBUG_PERFORMANCE) {
            System.err.println("[IndPer] generateBlockIndentsForForeignLanguage took "+(System.currentTimeMillis()-startTime1)+" ms");
        }

       // DEBUG info:
        if (DEBUG) {
            System.err.println(">> line data:");
            for (Line line : indentedLines) {
                System.err.println(" "+line.dump());
            }
            System.err.println(">> line indentations:");
            for (Line line : indentedLines) {
                if (line.indentThisLine) {
                    debugLineIndentation(line, line.index >= lineStart && line.index <= lineEnd);
                }
            }
        }

        startTime1 = System.currentTimeMillis();
        storeIndentsForOtherFormatters(suggestedIndentsForOtherLines);
        if (DEBUG_PERFORMANCE) {
            System.err.println("[IndPer] storeIndentsForOtherFormatters took "+(System.currentTimeMillis()-startTime1)+" ms");
        }

        // physically modify document's indentation
        startTime1 = System.currentTimeMillis();
        modifyDocument(indentedLines, lineStart, lineEnd);
        if (DEBUG_PERFORMANCE) {
            System.err.println("[IndPer] modifyDocument took "+(System.currentTimeMillis()-startTime1)+" ms");
        }
    }

    private void storeIndentsForOtherFormatters(Map<Integer, Integer> suggestedIndentsForOtherLines) {
        getDocument().putProperty("AbstractIndenter.lineIndents", suggestedIndentsForOtherLines);
        if (DEBUG && !suggestedIndentsForOtherLines.isEmpty()) {
            Set<Integer> keys = new TreeSet<Integer>(suggestedIndentsForOtherLines.keySet());
            System.err.print("AbstractIndenter.lineIndents:");
            for (int l : keys) {
                System.err.print(""+(l+1)+":"+suggestedIndentsForOtherLines.get(l)+" ");
            }
            System.err.println("");
        }
    }

    private List<Line> generateBlockIndentsForForeignLanguage(List<Line> indentedLines,
            Map<Integer, Integer> suggestedIndentsForOtherLines) throws BadLocationException {
        // go through compound blocks and generate lines for them:
        List<Line> indents = new ArrayList<Line>();
        List<Line> linesInBlock = new ArrayList<Line>();
        int lastStart = -1;
        Line lastStartLine = null;
        for (Line line : indentedLines) {
            if (line.foreignLanguageBlockStart) {
                lastStart = line.index;
                lastStartLine = line;
            }
            if (line.foreignLanguageBlockEnd) {
                if (lastStart == -1) {
                    assert false : "found line.compoundEnd without start: "+indentedLines;
                }
                if (lastStart != line.index) {
                    int end = line.index;
                    if (!line.indentThisLine) {
                        // if end line is not indentable than treat it as part of
                        // BLOCK to shift (eg. JSP code "javaCall(); %>")
                        end++;
                    }
                    for (int i = lastStart+1; i < end; i++) {
                        Line line2;
                        line2 = findLineByLineIndex(linesInBlock, i);
                        if (line2 == null) {
                            line2 = generateBasicLine(i);//new Line();
                            line2.indentThisLine = true;
                            line2.preserveThisLineIndent = true;
                            line2.indentation = calculatePreservedLineIndentation(lastStartLine, line2.offset);
                        } else {
                            if (!line2.indentThisLine) {
                                line2.preserveThisLineIndent = true;
                                line2.indentThisLine = true;
                                line2.indentation = calculatePreservedLineIndentation(lastStartLine, line2.offset);
                            }
                        }
                        if (!line2.emptyLine) {
                            indents.add(line2);
                        }
                        suggestedIndentsForOtherLines.remove(i);
                    }
                    linesInBlock.clear();
                }
                lastStart = -1;
            }
            if (lastStart != -1 && line.index > lastStart) {
                linesInBlock.add(line);
            } else {
                indents.add(line);
            }
        }
        return indents;
    }

    private Line generateBasicLine(int index) throws BadLocationException {
        Line line = new Line();
        line.index = index;
        line.offset = Utilities.getRowStartFromLineOffset(getDocument(), index);
        line.existingLineIndent = IndentUtils.lineIndent(getDocument(), line.offset);
        int nonWS = Utilities.getRowFirstNonWhite(getDocument(), line.offset);
        line.emptyLine = nonWS == -1;
        // if (first-non-whitespace-offset - line-start-offset) is different from 
        // existingLineIndent then line starts with tab characters which will need
        // to be replaced; if line is empty set tabIndentation to true just to make sure
        // possible tabs get replaced:
        line.tabIndentation = nonWS == -1 || line.existingLineIndent != (nonWS - line.offset);
        line.lineStartOffset = line.offset;
        line.lineEndOffset = Utilities.getRowEnd(getDocument(), line.offset);
        line.lineIndent = new ArrayList<IndentCommand>();
        line.lineIndent.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, line.offset, getIndentationSize()));
        line.preliminaryNextLineIndent = new ArrayList<IndentCommand>();
        line.preliminaryNextLineIndent.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, line.offset, getIndentationSize()));

        return line;
    }

    private void updateIndentationForPreservedLines(List<Line> indentedLines, int lineToBeIndented) throws BadLocationException {
        // iterate through lines and ignore all lines with line.indentThisLine == false
        // search for line.preserveThisLineIndent and apply indent calculated using
        // last non-preserveThisLineIndent's line indent
        Line lineBeforePreserveIndent = null;
        for (Line line : indentedLines) {
            if (!line.indentThisLine) {
                continue;
            }
            if (line.preserveThisLineIndent) {
                if (lineBeforePreserveIndent != null) {
                    // do not alter indentation of 'lineToBeIndented'
                    if (lineToBeIndented == -1 || line.index != lineToBeIndented) {
                        line.indentation = calculatePreservedLineIndentation(lineBeforePreserveIndent, line.offset);
                    }
                } else {
                    // #162031 and JspIndenterTest.testFormattingIssue162031()
                    lineBeforePreserveIndent = line;
                }
            } else {
                lineBeforePreserveIndent = line;
            }
        }
    }

    private int calculatePreservedLineIndentation(Line lineBeforePreserveIndent, int lineOffset) throws BadLocationException {
        int originalFirstLineIndent = IndentUtils.lineIndent(getDocument(), lineBeforePreserveIndent.offset);
        int originalCurrentLineIndent = IndentUtils.lineIndent(getDocument(), lineOffset);
        return lineBeforePreserveIndent.indentation + (originalCurrentLineIndent-originalFirstLineIndent);
    }


    private void modifyDocument(List<Line> indentedLines, int lineStart, int lineEnd) throws BadLocationException {
        // iterate through lines backwards and ignore all lines with line.indentThisLine == false
        // modify line's indent using calculated indentation
        for (int i=indentedLines.size()-1; i>=0; i--) {
            Line line = indentedLines.get(i);
            if (!line.indentThisLine || line.index < lineStart || line.index > lineEnd) {
                continue;
            }
            int newIndent = line.indentation;
            if (newIndent < 0) {
                newIndent = 0;
            }
            assert line.existingLineIndent != -1 : "line is missing existingLineIndent "+line;
            if (line.existingLineIndent != newIndent || line.tabIndentation) {
                    context.modifyIndent(line.offset, Math.min(newIndent, MAX_INDENT));
            }
        }
    }
        
    private void debugIndentation(int lineOffset, List<IndentCommand> iis, String text, boolean indentable) throws BadLocationException {
        int index = Utilities.getLineOffset(getDocument(), lineOffset);
        char ch = ' ';
        if (indentable) {
            ch = '*';
        }
        System.err.println(String.format("%1c[%4d]", ch, index+1)+text);
        for (IndentCommand ii : iis) {
            System.err.println("      "+ii);
        }
    }

    private void debugLineIndentation(Line ln, boolean indentable) throws BadLocationException {
        String line = "";
        if (ln.lineStartOffset != ln.lineEndOffset) {
            line = getDocument().getText(ln.lineStartOffset, ln.lineEndOffset-ln.lineStartOffset+1).replace("\n", "").replace("\r", "").trim();
        }
        StringBuilder sb = new StringBuilder();
        char ch = ' ';
        if (indentable) {
            ch = '*';
        } else if (ln.preserveThisLineIndent) {
            ch = 'P';
        }
        sb.append(String.format("%1c[%4d]", ch, ln.index+1));
        for (int i=0; i<ln.indentation; i++) {
            sb.append('.');
        }
        sb.append(line);
        if (sb.length() > 75) {
            sb.setLength(75);
        }

        System.err.println(sb.toString());
    }

    static final class Line {
        private List<IndentCommand> lineIndent;
        private List<IndentCommand> preliminaryNextLineIndent;
        private int offset;
        private int lineStartOffset;
        private int lineEndOffset;
        private int index;
        private boolean indentThisLine = true;
        private boolean preserveThisLineIndent;
        private int indentation;
        private boolean emptyLine;
        private boolean foreignLanguageBlockStart;
        private boolean foreignLanguageBlockEnd;
        private int existingLineIndent = -1;
        private boolean tabIndentation;
        private int indentationAdjustment = 0;


        private void updateOffset(int diff) {
            offset += diff;
            lineStartOffset += diff;
            lineEndOffset += diff;
            for (IndentCommand ic : lineIndent) {
                ic.updateOffset(diff);
            }
            for (IndentCommand ic : preliminaryNextLineIndent) {
                ic.updateOffset(diff);
            }
        }

        private void recalculateLineIndex(BaseDocument doc) throws BadLocationException {
            index = Utilities.getLineOffset(doc, offset);
            int rowStart = Utilities.getRowStart(doc, offset);

            // Java formatter is fiddling with lines it should not. This is a check
            // that if line start is different then issue a warning. See also
            // AbstractIndenter.recalculateLineIndexes() for more examples.
            if (rowStart != this.offset) {
               if (DEBUG) {
                   System.err.println("WARNING: disabling line indentability because its start has changed: "+this);
               }
               this.indentThisLine = false;
            }

        }

        public String dump() {
            return String.format("[%4d]", index+1)+
                    " offset="+offset+
                    " ("+lineStartOffset+
                    "-"+lineEndOffset+
                    ") indent=" +indentation+
                    (indentationAdjustment != 0 ? "("+indentationAdjustment+")" : "") +
                    ((existingLineIndent != -1) ? " existingIndent="+existingLineIndent : "") +
                    (foreignLanguageBlockStart? " foreignLangBlockStart" : "") +
                    (foreignLanguageBlockEnd? " foreignLangBlockEnd" : "") +
                    (preserveThisLineIndent? " preserve" : "") +
                    (emptyLine? " empty" : "") +
                    (!indentThisLine? " noIndent" : "");
        }

        @Override
        public String toString() {
            return "Line["+
                    "index="+index+
                    ",lineOffset="+offset+
                    ",startOffset="+lineStartOffset+
                    ",endOffset="+lineEndOffset+
                    ",indentation=" +indentation+
                    (indentationAdjustment != 0 ? "("+indentationAdjustment+")" : "") +
                    ((existingLineIndent != -1) ? ",existingIndent="+existingLineIndent : "") +
                    (preserveThisLineIndent? ",preserveThisLineIndent" : "") +
                    (emptyLine? ",empty" : "") +
                    (!indentThisLine? ",doNotIndentThisLine" : "") +
                    ",lineIndent=" +lineIndent+
                    "]";
        }

    }

    private static final class OffsetRange {
        private int start;
        private int end;

        public OffsetRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getEnd() {
            return end;
        }

        public int getStart() {
            return start;
        }

        @Override
        public String toString() {
            return "OffsetRange["+start+"-"+end+"]";
        }

    }

    /**
     * Descriptor of range within document defined by (inclusive) document offsets.
     */
    public static final class OffsetRanges {
        List<OffsetRange> ranges;

        public OffsetRanges() {
            this.ranges = new ArrayList<OffsetRange>();
        }

        /**
         * Add new range. Bordering ranges are automatically merged.
         */
        public void add(int start, int end) {
            for (OffsetRange range : ranges) {
                if (range.end == start) {
                    range.end = end;
                    return;
                } else if (range.start == end) {
                    range.start = start;
                    return;
                }
            }
            ranges.add(new OffsetRange(start, end));
        }

        /**
         * Is area given by start and end offset within this range?
         */
        private boolean contains(int start, int end) {
            for (OffsetRange or : ranges) {
                if (start >= or.getStart() && end <= or.getEnd()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Adjusts given start and end offset to describe area not covered
         * by a range. Exceptions are: #1) if given start-end region lies within
         * a range then -1 values will be returned; and #2) if there is a range
         * which lies within start-end region it will be ignored.
         * @param result array of two integers which will be populated by this
         *  method; first one will be new start and second one will be new end
         * @return true if something was calculated
         */
        private boolean calculateUncoveredArea(int start, int end, int[] result) {
            boolean changed = false;
            for (OffsetRange or : ranges) {
                if (start < or.getStart() && end > or.getEnd()) {
                    // ignore-area lies within the line -> just process whole line
                    continue;
                }
                if (start >= or.getStart() && end <= or.getEnd()) {
                    // skip this line
                    result[0] = -1;
                    result[1] = -1;
                    return true;
                }
                if (start >= or.getStart() && start <= or.getEnd()) {
                    assert or.getEnd()+1 <= end : ""+start+"-"+end+" range="+or;
                    start = or.getEnd()+1;
                    changed = true;
                }
                if (end >= or.getStart() && end <= or.getEnd()) {
                    assert start <= or.getStart()-1 : ""+start+"-"+end+" range="+or;
                    end = or.getStart()-1;
                    changed = true;
                }
            }
            if (changed) {
                result[0] = start;
                result[1] = end;
            }
            return changed;
        }

        public boolean isEmpty() {
            return ranges.isEmpty();
        }

        public String dump() {
            StringBuilder sb = new StringBuilder();
            for (OffsetRange or : ranges) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append("[").append(or.start).append("-").append(or.end).append("]");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "OffsetRanges["+dump()+"]";
        }

    }

    private final class LinePair {
        private int startingLine;
        private int endingLine;

        public int getEndingLine() {
            return endingLine;
        }

        public int getStartingLine() {
            return startingLine;
        }

        @Override
        public String toString() {
            return "LP[" +startingLine+":"+endingLine+"]";
        }
    }

    private static class LineCommandsPair {
        private int line;
        private List<IndentCommand> commands;

        public LineCommandsPair(int line, List<IndentCommand> commands) {
            this.line = line;
            this.commands = commands;
        }

        @Override
        public String toString() {
            return "LineCommandsPair["+line+":"+commands+"]";
        }

    }

    private static class ForeignLanguageBlock {
        private int startLine;
        private int endLine;

        public ForeignLanguageBlock(int startLine, int endLine) {
            this.startLine = startLine;
            this.endLine = endLine;
        }

        public int getEndLine() {
            return endLine;
        }

        public int getStartLine() {
            return startLine;
        }

        @Override
        public String toString() {
            return "ForeignLangBlock["+startLine+"-"+endLine+"]";
        }


    }
    
    /**
     * !!! the implementation only implements following methods: size, get(...), add(...) !!!
     * @param <T> 
     */
    private static class UnmodifiableButExtendableList<T> implements List<T> {

        private static final String NOT_SUPPORTED_MGS = "UnmodifiableButExtendableList doesn't implement the method, if you've modified the using code, please update the UnmodifiableButExtendableList class accordingly!"; //NOI18N
        
        private List<T> original;
        private List<T> ext;

        public UnmodifiableButExtendableList(List<T> original) {
            this.original = original;
            this.ext = new ArrayList<T>();
        }
        
        @Override
        public int size() {
            return original.size() + ext.size();
        }

        @Override
        public boolean isEmpty() {
            return original.isEmpty() && ext.isEmpty();
        }

        @Override
        public boolean add(T e) {
            return ext.add(e);
        }
        
        @Override
        public T get(int i) {
            int os = original.size();
            if(i < os) {
                return original.get(i);
            } else {
                return ext.get(i - os);
            }
        }
        
        //>>> follows unsupported operations >>>
        
        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public Iterator<T> iterator() {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public <T> T[] toArray(T[] ts) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public boolean containsAll(Collection<?> clctn) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public boolean addAll(Collection<? extends T> clctn) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public boolean addAll(int i, Collection<? extends T> clctn) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public boolean removeAll(Collection<?> clctn) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public boolean retainAll(Collection<?> clctn) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public T set(int i, T e) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public void add(int i, T e) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public T remove(int i) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public int indexOf(Object o) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public ListIterator<T> listIterator() {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public ListIterator<T> listIterator(int i) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }

        @Override
        public List<T> subList(int i, int i1) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_MGS);
        }
        
    }

}
