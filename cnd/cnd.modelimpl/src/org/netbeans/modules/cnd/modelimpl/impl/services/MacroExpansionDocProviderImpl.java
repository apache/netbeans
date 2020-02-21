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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTFileInfoQuerySupport;
import org.netbeans.modules.cnd.modelimpl.parser.clank.ClankFileInfoQuerySupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider;
import org.openide.text.NbDocument;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 * Service that provides macro expansions implementation.
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider.class)
public class MacroExpansionDocProviderImpl implements CsmMacroExpansionDocProvider {

    public final static String MACRO_EXPANSION_OFFSET_TRANSFORMER = "macro-expansion-offset-transformer"; // NOI18N
    public final static String MACRO_EXPANSION_MACRO_TABLE = "macro-expansion-macro-table"; // NOI18N

    public final static String MACRO_EXPANSION_STOP_ON_OFFSET_PARSE_FILE_WALKER_CACHE = "macro-expansion-stop-on-offset-parse-file-walker-cache"; // NOI18N

    @Override
    public synchronized int expand(final Document inDoc, final int startOffset, final int endOffset, final Document outDoc, final AtomicBoolean canceled) {
        if (inDoc == null || outDoc == null) {
            return 0;
        }
        final CsmFile file = CsmUtilities.getCsmFile(inDoc, false, false);
        if (file == null) {
            return 0;
        }


        final MyTokenSequence fileTS = getFileTokenSequence(file, startOffset, endOffset);
        if (fileTS == null) {
            return 0;
        }

        final StringBuilder expandedData = new StringBuilder();
        final TransformationTable tt = new TransformationTable(DocumentUtilities.getDocumentVersion(inDoc), CsmFileInfoQuery.getDefault().getFileVersion(file));

        Runnable r = new Runnable() {

            @Override
            public void run() {
                // Init token sequences
                TokenSequence<TokenId> docTS = CndLexerUtilities.getCppTokenSequence(inDoc, inDoc.getLength(), false, true);
                if (docTS == null) {
                    return;
                }
                docTS.move(startOffset);

                // process tokens
                tt.setInStart(startOffset);
                tt.setOutStart(0);

                boolean inMacroParams = false;
                boolean inDeadCode = true;

                while (docTS.moveNext()) {
                    Token<TokenId> docToken = docTS.token();

                    int docTokenStartOffset = docTS.offset();
                    int docTokenEndOffset = docTokenStartOffset + docToken.length();

                    if (isWhitespace(docToken)) {
                        continue;
                    }
                    if (canceled.get()) {
                        break;
                    }
                    APTToken fileToken = findToken(fileTS, docTokenStartOffset);
                    if (fileToken == null) {
                        // expanded stream ended
                        if (!(inMacroParams || inDeadCode)) {
                            copyInterval(inDoc, ((endOffset > docTokenStartOffset) ? docTokenStartOffset : endOffset) - tt.currentIn.start, tt, expandedData);
                        }
                        tt.appendInterval(endOffset - tt.currentIn.start, 0);
                        break;
                    }
                    if (docTokenEndOffset <= fileToken.getOffset() || !APTUtils.isMacroExpandedToken(fileToken)) {
                        if (isOnInclude(docTS)) {
                            if (!(inMacroParams || inDeadCode)) {
                                copyInterval(inDoc, docTokenStartOffset - tt.currentIn.start, tt, expandedData);
                            } else {
                                tt.appendInterval(docTokenStartOffset - tt.currentIn.start, 0);
                            }
                            expandIcludeToken(docTS, inDoc, file, tt, expandedData);
                        } else if (docTokenEndOffset <= fileToken.getOffset()) {
                            if (inMacroParams || inDeadCode) {
                                // skip token in dead code
                                tt.appendInterval(docTokenEndOffset - tt.currentIn.start, 0);
                                continue;
                            } else {
                                // copy tokens befor dead token and skip this token
                                copyInterval(inDoc, docTokenStartOffset - tt.currentIn.start, tt, expandedData);
                                tt.appendInterval(docTokenEndOffset - tt.currentIn.start, 0);
                                inDeadCode = true;
                                continue;
                            }
                        }
                        inMacroParams = false;
                        inDeadCode = false;
                        continue;
                    }
                    // process macro
                    copyInterval(inDoc, docTokenStartOffset - tt.currentIn.start, tt, expandedData);
                    Formatter formatter = null;
                    if (outDoc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null) {
                        formatter = new Formatter((BaseDocument) outDoc);
                    }
                    expandMacroToken(docTS, fileTS, tt, expandedData, formatter);
                    inMacroParams = true;
                }
                // copy the tail of the code
                copyInterval(inDoc, endOffset - tt.currentIn.start, tt, expandedData);

                tt.cleanUp();
            }
        };

        inDoc.render(r);

        // apply transformation to result document
        outDoc.putProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER, tt);
        try {
            outDoc.insertString(0, expandedData.toString(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        initGuardedBlocks(outDoc, tt);

        return calcExpansionNumber(tt);
    }

    @Override
    public int getOffsetInExpandedText(Document expandedDoc, int originalOffset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt.getOutOffset(originalOffset);
        }
        return originalOffset;
    }

    @Override
    public int getOffsetInOriginalText(Document expandedDoc, int expandedOffset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt.getInOffset(expandedOffset);
        }
        return expandedOffset;
    }

    @Override
    public int getNextMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt.getNextMacroExpansionStartOffset(expandedOffset);
        }
        return expandedOffset;
    }

    @Override
    public int getPrevMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt.getPrevMacroExpansionStartOffset(expandedOffset);
        }
        return expandedOffset;
    }

    private void fillParamsToExpansionMap(APTToken fileToken, TransformationTable tt, int expandedOffsetShift, Map<Interval, List<Interval>> paramsToExpansion) {
        APTToken to = APTUtils.getExpandedToken(fileToken);
        if (to != null) {
            Interval paramInterval = createInterval(to.getOffset(), to.getEndOffset());
            Interval paramExpansionInterval = createInterval(tt.currentOut.start + expandedOffsetShift, tt.currentOut.start + expandedOffsetShift + fileToken.getTextID().length());
            List<Interval> paramExpansions = paramsToExpansion.get(paramInterval);
            if (paramExpansions != null) {
                paramExpansions.add(paramExpansionInterval);
            } else {
                paramExpansions = new ArrayList<>(1);
                paramExpansions.add(paramExpansionInterval);
                paramsToExpansion.put(paramInterval, paramExpansions);
            }
        }
    }

    private APTToken findToken(MyTokenSequence fileTS, int offset) {
        while (fileTS.token() != null && !APTUtils.isEOF(fileTS.token()) && fileTS.token().getOffset() < offset) {
            fileTS.moveNext();
        }
        if (fileTS.token() == null || APTUtils.isEOF(fileTS.token())) {
            return null;
        }
        return fileTS.token();
    }

    private TransformationTable getCachedMacroTable(Document doc) {
        Object o = doc.getProperty(MACRO_EXPANSION_MACRO_TABLE);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt;
        }
        return null;
    }

    private TransformationTable getTransformationTable(Document doc) {
        Object o = doc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt;
        }
        return null;
    }

    public String[] getMacroExpansion(Document doc, int offset) {
        // returns empty expansion
        return new String[]{"", ""}; // NOI18N
    }

    @Override
    public String expand(Document doc, int startOffset, int endOffset) {
        // this is only for tool tips
        if(doc == null) {
            return null;
        }
        final CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
        Formatter formatter = null;
        if (doc instanceof BaseDocument) {
            formatter = new Formatter((BaseDocument) doc);
        }
        TransformationTable tt = new TransformationTable(DocumentUtilities.getDocumentVersion(doc), CsmFileInfoQuery.getDefault().getFileVersion(csmFile));
        expand(doc, csmFile, tt, formatter);
        tt.cleanUp();
        return expandInterval(doc, tt, startOffset, endOffset);
    }

    @Override
    public String expand(Document doc, CsmFile file, int startOffset, int endOffset, boolean updateIfNeeded) {
        TransformationTable tt = getMacroTable(doc, file, updateIfNeeded);
        return tt == null ? null : expandInterval(doc, tt, startOffset, endOffset);
    }

    @Override
    public int[] getMacroExpansionSpan(Document doc, int offset, boolean wait) {
        int[] span = new int[]{offset, offset};
        TransformationTable tt;
        if (wait) {
            CsmFile file = CsmUtilities.getCsmFile(doc, false, false);
            tt = getMacroTable(doc, file, true);
        } else {
            synchronized (doc) {
                tt = getCachedMacroTable(doc);
            }
        }
        if (tt != null) {
            int startIndex = tt.findInIntervalIndex(offset);
            if (0 <= startIndex && startIndex < tt.intervals.size()) {
                if (tt.intervals.get(startIndex).getInIntervalEnd() == offset && startIndex < tt.intervals.size() - 1) {
                    // use next
                    startIndex++;
                }
                boolean foundMacroExpansion = false;
                int macroIndex = tt.intervals.size();
                // back to start of macro expansion
                for (int i = startIndex; i >= 0; i--) {
                    IntervalCorrespondence ic = tt.intervals.get(i);
                    if (ic.isMacro()) {
                        span[0] = ic.getInIntervalStart();
                        span[1] = ic.getInIntervalEnd();
                        foundMacroExpansion = true;
                        macroIndex = i;
                        break;
                    } else if (ic.getOutIntervalLength() != 0) {
                        // we are out of macro expansion
                        return span;
                    }
                }
                if (foundMacroExpansion) {
                    // forward to the end of macro expansion
                    for (int i = macroIndex + 1; i < tt.intervals.size(); i++) {
                        IntervalCorrespondence ic = tt.intervals.get(i);
                        if (ic.getOutIntervalLength() == 0) {
                            // we are in macro expansion
                            span[1] = ic.getInIntervalEnd();
                        } else {
                            return span;
                        }
                    }
                }
            }
        }
        return span;
    }

    @Override
    public int[][] getUsages(Document expandedDoc, int offset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;

            int startIndex = tt.findInIntervalIndex(offset);
            if (0 <= startIndex && startIndex < tt.intervals.size()) {
                if (tt.intervals.get(startIndex).getInIntervalEnd() == offset && startIndex < tt.intervals.size() - 1) {
                    // use next
                    startIndex++;
                }
                // back to start of macro expansion
                for (int i = startIndex; i >= 0; i--) {
                    IntervalCorrespondence ic = tt.intervals.get(i);
                    if (ic.isMacro()) {
                        List<Interval> anIntervals = ic.getIntervals();
                        for(int k = 0; k < anIntervals.size(); k++) {
                            Interval in = anIntervals.get(k);
                            if (in.contains(offset)) {
                                List<Interval> params = ic.getParamIntervals(k);
                                int usages[][] = new int[params.size()][2];
                                for (int j = 0; j < usages.length; j++) {
                                    usages[j][0] = params.get(j).getStart();
                                    usages[j][1] = params.get(j).getEnd();
                                }
                                return usages;
                            }
                        }
                        break;
                    } else if (ic.getOutIntervalLength() != 0) {
                        // we are out of macro expansion
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /** returns expanded code or NULL on error */
    @Override
    public String expand(Document doc, int offset, String code) {
        if (doc == null) {
            return code;
        }
        CsmFile file = CsmUtilities.getCsmFile(doc, false, false);
        if (!(file instanceof FileImpl)) {
            return code;
        }
        FileImpl fileImpl = (FileImpl) file;
        PreprocHandler handler = ((FileImpl) file).getPreprocHandler(offset);
        if (handler == null) {
            return code;
        }
        CsmProject project = file.getProject();
        if(!(project instanceof ProjectBase)) {
            return code;
        }
        ProjectBase base = (ProjectBase) project;
        if (APTTraceFlags.USE_CLANK) {
          return ClankFileInfoQuerySupport.expand(fileImpl, code, handler, base, offset);
        } else {
          return APTFileInfoQuerySupport.expand(fileImpl, code, handler, base, offset);
        }
    }

    private String expandInterval(Document doc, TransformationTable tt, int startOffset, int endOffset) {
        if (tt.intervals.isEmpty()) {
            return null;
        }
        int size = tt.intervals.size();
        int startIndex = tt.findInIntervalIndex(startOffset);
        if (startIndex < 0) {
            return ""; // NOI18N
        }
        StringBuilder sb = new StringBuilder(""); // NOI18N
        for(int i = startIndex; i < size; i++) {
            IntervalCorrespondence ic = tt.intervals.get(i);
            if (ic.getInIntervalStart() >= endOffset) {
                break;
            }
            if (ic.getInIntervalEnd() <= startOffset) {
                continue;
            }
            int startShift = startOffset - ic.getInIntervalStart();
            if (startShift < 0) {
                startShift = 0;
            }
            if (startShift >= ic.getOutIntervalLength()) {
                continue;
            }
            int endShift = startShift + (endOffset - startOffset);
            if(endOffset >= ic.getInIntervalEnd()) {
                endShift = ic.getOutIntervalLength();
            }
            if (endShift > ic.getOutIntervalLength()) {
                endShift = ic.getOutIntervalLength();
            }
            if (endShift - startShift != 0) {
                if (ic.isMacro()) {
                    if(startShift == 0 && endShift == ic.getOutIntervalLength()) {
                        sb.append(ic.getMacroExpansion());
                    } else {
                        sb.append(ic.getMacroExpansion().toString().substring(startShift, endShift));
                    }
                } else if (ic.getOutIntervalLength() != 0) {
                    sb.append(getDocumentText(doc, ic.getInIntervalStart() + startShift, endShift - startShift));
                }
            } 
        }
        return sb.toString();
    }

    private void expand(final Document doc, final CsmFile file, final TransformationTable tt, final Formatter formatter) {
        if (doc == null) {
            return;
        }
        if (file == null) {
            return;
        }
        // Init file token sequence
        // why only one token stream is analyzed and not all preprocessor branches?
        final MyTokenSequence fileTS = getFileTokenSequence(file, 0, doc.getLength());
        if (fileTS == null) {
            return;
        }

        Runnable r = new Runnable() {

            @Override
            public void run() {
                // Init document token sequence
                TokenSequence<TokenId> docTS = CndLexerUtilities.getCppTokenSequence(doc, doc.getLength(), false, true);
                if (docTS == null) {
                    return;
                }
                docTS.moveStart();

                int startOffset = 0;
                int endOffset = doc.getLength();

                // process tokens
                tt.setInStart(startOffset);
                tt.setOutStart(0);

                boolean inMacroParams = false;
                boolean inDeadCode = true;

                while (docTS.moveNext()) {
                    Token<TokenId> docToken = docTS.token();

                    int docTokenStartOffset = docTS.offset();
                    int docTokenEndOffset = docTokenStartOffset + docToken.length();

                    if (isWhitespace(docToken)) {
                        continue;
                    }

                    APTToken fileToken = findToken(fileTS, docTokenStartOffset);
                    if (fileToken == null) {
                        // expanded stream ended
                        if (!(inMacroParams || inDeadCode)) {
                            copyInterval(doc, ((endOffset > docTokenStartOffset) ? docTokenStartOffset : endOffset) - tt.currentIn.start, tt, null);
                        }
                        tt.appendInterval(endOffset - tt.currentIn.start, 0);
                        break;
                    }
                    if (docTokenEndOffset <= fileToken.getOffset() || !APTUtils.isMacroExpandedToken(fileToken)) {
                        if (isOnInclude(docTS)) {
                            if (!(inMacroParams || inDeadCode)) {
                                copyInterval(doc, docTokenStartOffset - tt.currentIn.start, tt, null);
                            } else {
                                tt.appendInterval(docTokenStartOffset - tt.currentIn.start, 0);
                            }
                            expandIcludeToken(docTS, doc, file, tt, null);
                        } else if (docTokenEndOffset <= fileToken.getOffset()) {
                            if (inMacroParams || inDeadCode) {
                                // skip token in dead code
                                tt.appendInterval(docTokenEndOffset - tt.currentIn.start, 0);
                                continue;
                            } else {
                                // copy tokens befor dead token and skip this token
                                copyInterval(doc, docTokenStartOffset - tt.currentIn.start, tt, null);
                                tt.appendInterval(docTokenEndOffset - tt.currentIn.start, 0);
                                inDeadCode = true;
                                continue;
                            }
                        }
                        inMacroParams = false;
                        inDeadCode = false;
                        continue;
                    }
                    // process macro
                    copyInterval(doc, docTokenStartOffset - tt.currentIn.start, tt, null);
                    expandMacroToken(docTS, fileTS, tt, null, formatter);
                    inMacroParams = true;
                }
                // copy the tail of the code
                copyInterval(doc, endOffset - tt.currentIn.start, tt, null);
            }
        };
        doc.render(r);

//        System.out.println("MACRO_EXPANSION_MACRO_TABLE");
//        System.out.println(tt);
    }

    private CharSequence expandMacroToken(MyTokenSequence fileTS, int docTokenStartOffset, int docTokenEndOffset, TransformationTable tt, Formatter formatter) {
        APTToken fileToken = fileTS.token();
        StringBuilder expandedToken = new StringBuilder();
        int expandedOffsetShift = 0;

        Map<Interval, List<Interval>> paramsToExpansion = new HashMap<>();
                
        boolean skipIndent = true;
        if (fileToken.getOffset() < docTokenEndOffset) {
            Pair<? extends CharSequence, ? extends CharSequence> format = null;
            if (formatter != null) {
                formatter.init(fileToken);
                format = formatter.process(fileToken, fileTS);
            }
            // empty comment - expansion of empty macro
            if (!APTUtils.isCommentToken(fileToken)) {
                expandedToken.append(fileToken.getTextID());
                if (APTUtils.isMacroParamExpandedToken(fileToken)) {
                    fillParamsToExpansionMap(fileToken, tt, expandedOffsetShift, paramsToExpansion);
                }
                expandedOffsetShift += fileToken.getTextID().length();
                skipIndent = false;
            }
            APTToken prevFileToken = fileToken;
            fileToken = skipMetaTokens(fileTS, prevFileToken);
            while (fileToken != null && !APTUtils.isEOF(fileToken) && fileToken.getOffset() < docTokenEndOffset) {
                if (formatter != null) {
                    format = formatter.process(fileToken, fileTS);
                }
                if (!APTUtils.isCommentToken(fileToken)) {
                    if (format != null) {
                        CharSequence prefix = format.first();
                        if (prefix != null) {
                            if (prefix instanceof UndoIndent) {
                                for(int i = 0; i < prefix.length(); i++) {
                                    char c = prefix.charAt(i);
                                    if (expandedToken.length() > 0) {
                                        char done = expandedToken.charAt(expandedToken.length()-1);
                                        if (c == done) {
                                            expandedToken.setLength(expandedToken.length()-1);
                                            expandedOffsetShift--;
                                        } else {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            } else {
                                expandedToken.append(prefix);
                                expandedOffsetShift += prefix.length();
                            }
                            skipIndent = true;
                        }
                    }
                    if (!skipIndent) {
                        if (!APTUtils.areAdjacent(prevFileToken, fileToken)) {
                            expandedToken.append(' '); // NOI18N
                            expandedOffsetShift++;
                        }
                    }
                    skipIndent = false;
                    expandedToken.append(fileToken.getTextID());
                    if (APTUtils.isMacroParamExpandedToken(fileToken)) {
                        fillParamsToExpansionMap(fileToken, tt, expandedOffsetShift, paramsToExpansion);
                    }
                    expandedOffsetShift += fileToken.getTextID().length();
                    if (format != null) {
                        CharSequence suffix = format.second();
                        if (suffix != null) {
                            expandedToken.append(suffix);
                            expandedOffsetShift += suffix.length();
                            skipIndent = true;
                        }
                    }
                }
                prevFileToken = fileToken;
                fileTS.moveNext();
                fileToken = fileTS.token();
            }
        }
        if (formatter != null) {
            // remove last indent
            for(int i = expandedToken.length()-1; i >= 0; i--) {
                char c = expandedToken.charAt(i);
                if (c == ' ') { // NOI18N
                    expandedToken.setLength(expandedToken.length()-1);
                    expandedOffsetShift--;
                } else if (c == '\n') { // NOI18N
                    expandedToken.setLength(expandedToken.length()-1);
                    expandedOffsetShift--;
                    break;
                } else {
                    break;
                }
            }
        }

        tt.appendInterval(docTokenEndOffset - docTokenStartOffset, expandedToken.length(), true, expandedToken, paramsToExpansion);
        return expandedToken;
    }

    private APTToken skipMetaTokens(MyTokenSequence fileTS, APTToken prevFileToken) {
        if (APTUtils.isMacroExpandedToken(prevFileToken) && APTUtils.isCommentToken(prevFileToken)) {
            // Conversion from Clank tokens to APT tokens requires to add fake annotation tokens
            // to capture additional info about macro expansion. If there is a macro call with nested
            // macro calls, skip all meta tokens after the first one.
            APTToken fileToken;
            do {
                fileTS.moveNext();
                fileToken = fileTS.token();
            } while (APTUtils.isMacroExpandedToken(fileToken)
                && APTUtils.isCommentToken(fileToken)
                && prevFileToken.getOffset() < fileToken.getOffset()
                && prevFileToken.getEndOffset() > fileToken.getEndOffset());
            return fileToken;
        } else {
            fileTS.moveNext();
            return fileTS.token();
        }
    }

    private void expandMacroToken(TokenSequence docTS, MyTokenSequence fileTS, TransformationTable tt, StringBuilder expandedData, Formatter formatter) {
        expandMacroToken(docTS.token(), docTS.offset(), fileTS, tt, expandedData, formatter);
    }

    private void expandMacroToken(Token docToken, int docTokenStartOffset, MyTokenSequence fileTS, TransformationTable tt, StringBuilder expandedData, Formatter formatter) {
        CharSequence expandedToken = expandMacroToken(fileTS, docTokenStartOffset, docTokenStartOffset + docToken.length(), tt, formatter);
        addString(expandedToken, expandedData);
    }

    private void expandIcludeToken(TokenSequence<TokenId> docTS, Document inDoc, CsmFile file, TransformationTable tt, StringBuilder expandedData) {
        int incStartOffset = docTS.offset();
        String includeName = getIncludeName(file, incStartOffset);
        if (includeName == null) {
            return;
        }
        int incNameStartOffset = incStartOffset;
        int incNameEndOffset = incStartOffset;
        Token<TokenId> docToken = docTS.token();
        TokenId id = docToken.id();
        if(id instanceof CppTokenId) {
            switch ((CppTokenId)id) {
                case PREPROCESSOR_DIRECTIVE:
                    TokenSequence<?> embTS = docTS.embedded();
                    if (embTS != null) {
                        embTS.moveStart();
                        if (!embTS.moveNext()) {
                            return;
                        }
                        Token embToken = embTS.token();
                        if (embToken == null || !(embToken.id() instanceof CppTokenId) || 
                                (embToken.id() != CppTokenId.PREPROCESSOR_START && embToken.id() != CppTokenId.PREPROCESSOR_START_ALT)) {
                            return;
                        }
                        if (!embTS.moveNext()) {
                            return;
                        }
                        skipWhitespacesAndComments(embTS);
                        embToken = embTS.token();
                        if (embToken != null && (embToken.id() instanceof CppTokenId)) {
                            switch ((CppTokenId) embToken.id()) {
                                case PREPROCESSOR_INCLUDE:
                                    if (!embTS.moveNext()) {
                                        return;
                                    }
                                    skipWhitespacesAndComments(embTS);
                                    incNameStartOffset = embTS.offset();
                                    embToken = embTS.token();
                                    while (embToken != null && (embToken.id() instanceof CppTokenId) && (embToken.id() != CppTokenId.NEW_LINE)) {
                                        if (!embTS.moveNext()) {
                                            return;
                                        }
                                        incNameEndOffset = embTS.offset();
                                        skipWhitespacesAndComments(embTS);
                                        embToken = embTS.token();
                                    }
                                    break;
                                default:
                                    return;
                            }
                        }
                    }
                    break;
                default:
                    return;
            }
        }
        copyInterval(inDoc, incNameStartOffset - incStartOffset, tt, expandedData);
        int expandedLength = addString(includeName, expandedData);
        tt.appendInterval(incNameEndOffset - incNameStartOffset, expandedLength);
    }

    private String getIncludeName(CsmFile file, int offset) {
        for (CsmInclude inc : file.getIncludes()) {
            if (inc.getStartOffset() == offset) {
                if(inc.isSystem()) {
                    StringBuilder sb = new StringBuilder("<"); // NOI18N
                    sb.append(inc.getIncludeName().toString());
                    sb.append(">"); // NOI18N
                    return sb.toString();
                } else {
                    StringBuilder sb = new StringBuilder("\""); // NOI18N
                    sb.append(inc.getIncludeName().toString());
                    sb.append("\""); // NOI18N
                    return sb.toString();
                }
            }
        }
        return null;
    }

    private void skipWhitespacesAndComments(TokenSequence ts) {
        if (ts != null) {
            Token token = ts.token();
            while (token != null && (token.id() instanceof CppTokenId)) {
                switch ((CppTokenId) token.id()) {
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case ESCAPED_LINE:
                        ts.moveNext();
                        token = ts.token();
                        continue;
                    default:
                        return;
                }
            }
        }
    }

    private void initGuardedBlocks(Document doc, TransformationTable tt) {
        if (doc instanceof StyledDocument) {
            for (IntervalCorrespondence ic : tt.intervals) {
                if (ic.isMacro()) {
                    NbDocument.markGuarded((StyledDocument) doc, ic.getOutIntervalStart(), ic.getOutIntervalLength());
                }
            }
        }
    }

    private int calcExpansionNumber(TransformationTable tt) {
        int expansionsNumber = 0;
        for (IntervalCorrespondence ic : tt.intervals) {
            if (ic.isMacro()) {
                expansionsNumber++;
            }
        }
        return expansionsNumber;
    }

    private MyTokenSequence getFileTokenSequence(CsmFile file, int startOffset, int endOffset) {
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;
            // why only one token stream is analyzed and not all preprocessor branches?
            TokenStream ts = fileImpl.getTokenStream(startOffset, endOffset, 0, false);
            if (ts != null) {
                return new MyTokenSequence(ts, fileImpl);
            }
        }
        return null;
    }

    private void copyInterval(Document inDoc, int length, TransformationTable tt, StringBuilder expandedString) {
        if (length != 0) {
            addString(getDocumentText(inDoc, tt.currentIn.start, length), expandedString);
            tt.appendInterval(length, length);
        }
    }

    static private String getDocumentText(Document doc, int startOffset, int length) {
        try {
            int docLength = doc.getLength();
            startOffset = startOffset > 0 ? startOffset : 0;
            startOffset = startOffset < docLength ? startOffset : docLength;
            length = length > 0 ? length : 0;
            length = startOffset + length <= docLength ? length : docLength - startOffset;
            if (length > 0) {
                return doc.getText(startOffset, length);
            }
        } catch (BadLocationException ex) {
            //
        }
        return ""; // NOI18N
    }

    private boolean isWhitespace(Token<TokenId> docToken) {
        TokenId id = docToken.id();
        if(id instanceof CppTokenId) {
            switch ((CppTokenId)id) {
                case NEW_LINE:
                case WHITESPACE:
                case ESCAPED_WHITESPACE:
                case ESCAPED_LINE:
                    return true;
            }
        }
        return false;
    }

    private boolean isOnInclude(TokenSequence<TokenId> docTS) {
        Token<TokenId> docToken = docTS.token();
        TokenId id = docToken.id();
        if(id instanceof CppTokenId) {
            switch ((CppTokenId)id) {
                case PREPROCESSOR_DIRECTIVE:
                    TokenSequence<?> embTS = docTS.embedded();
                    if (embTS != null) {
                        embTS.moveStart();
                        if (embTS.moveNext()) {
                            Token embToken = embTS.token();
                            if (embToken == null || !(embToken.id() instanceof CppTokenId) ||
                                    (embToken.id() != CppTokenId.PREPROCESSOR_START && embToken.id() != CppTokenId.PREPROCESSOR_START_ALT)) {
                                return false;
                            }
                            if (embTS.moveNext()) {
                                skipWhitespacesAndComments(embTS);
                                embToken = embTS.token();
                                if (embToken != null && (embToken.id() instanceof CppTokenId)) {
                                    switch ((CppTokenId) embToken.id()) {
                                        case PREPROCESSOR_INCLUDE:
                                        case PREPROCESSOR_INCLUDE_NEXT:
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            }
                        }
                    }
                    break;
                default:
                    return false;
            }
        }
        return false;
    }

    private int addString(CharSequence s, StringBuilder expandedString) {
        if(expandedString != null) {
            expandedString.append(s);
        }
        return s.length();
    }

    /* package local */ String dumpTables(Document doc) {
        StringBuilder sb = new StringBuilder();
        TransformationTable tt = getCachedMacroTable(doc);
        if(tt != null) {
            sb.append("MacroTable: "); // NOI18N
            sb.append(tt.toString());
        }
        tt = getTransformationTable(doc);
        if(tt != null) {
            sb.append("TransformationTable: "); // NOI18N
            sb.append(tt.toString());
        }
        return sb.toString();
    }
    
    private static final class MyTokenSequence {

        private final TokenStream ts;
        private final FileImpl file;
        private APTToken currentToken = null;
        private APTToken LAToken;

        public MyTokenSequence(TokenStream ts, FileImpl file) {
            this.ts = ts;
            this.file = file;
            try {
                currentToken = (APTToken) ts.nextToken();
                if (currentToken != null) {
                    LAToken = (APTToken) ts.nextToken();
                }
            } catch (TokenStreamException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public APTToken token() {
            return currentToken;
        }

        public void moveNext() {
            try {
                currentToken = LAToken;
                if (currentToken != null) {
                    LAToken = (APTToken) ts.nextToken();
                }
            } catch (TokenStreamException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public APTToken LA() {
            return LAToken;
        }
    }

    private interface Interval {
        boolean contains(int offset);
        int getEnd();
        int getStart();
        int length();
    }
    
    private static Interval createInterval(int start, int end) {
        return new IntervalImpl(start, end);
    }
    
    private static class IntervalImpl implements Interval {

        private final int start;
        private final int end;

        public IntervalImpl(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int length() {
            return getEnd() - getStart();
        }

        @Override
        public int getStart() {
            return start;
        }

        @Override
        public int getEnd() {
            return end;
        }

        @Override
        public boolean contains(int offset) {
            return (getStart() <= offset && getEnd() >= offset);
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof Interval) {
                Interval i = (Interval) o;
                return getStart() == i.getStart() && getEnd() == i.getEnd();
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + this.getStart();
            hash = 79 * hash + this.getEnd();
            return hash;
        }

        @Override
        public String toString() {
            return "["+getStart()+"-"+getEnd()+"]"; // NOI18N
        }
    }

    private static class CurrentInterval {

        private int start;
        private int end;

        public CurrentInterval(int start) {
            this.start = start;
            this.end = start;
        }
        
        public void init(int start) {
            this.start = start;
            this.end = start;
        }

        public void setLength(int length) {
            this.end = start + length;
        }

        @Override
        public String toString() {
            return "["+start+"-"+end+"]"; // NOI18N
        }
    }

    private abstract static class IntervalCorrespondence {
        public abstract int getInIntervalStart();
        public abstract int getInIntervalEnd();
        public abstract int getInIntervalLength();
        public abstract boolean inIntervalContains(int offset);

        public abstract int getOutIntervalStart();
        public abstract int getOutIntervalEnd();
        public abstract int getOutIntervalLength();
        public abstract boolean outIntervalContains(int offset);

        /**
         * @return the macro
         */
        public boolean isMacro() {
            return false;
        }

        /**
         * @return the macroExpansion
         */
        public CharSequence getMacroExpansion() {
            return null;
        }

        public List<Interval> getIntervals(){
            return null;
        }

        public List<Interval> getParamIntervals(int i){
            return null;
        }

        @Override
        public String toString() {
            return "IN:"+"["+getInIntervalStart()+"-"+getInIntervalEnd()+"]"+" OUT:"+"["+getOutIntervalStart()+"-"+getOutIntervalEnd()+"]"; // NOI18N
        }
    }
    
    private static class IntervalCorrespondenceSimple extends IntervalCorrespondence {

        private final int inStart;
        private final int inEnd;
        private final int outStart;
        private final int outEnd;

        public IntervalCorrespondenceSimple(int inStart, int inEnd, int outStart, int outEnd) {
            this.inStart = inStart;
            this.inEnd = inEnd;
            this.outStart = outStart;
            this.outEnd = outEnd;
        }

        @Override
        public int getInIntervalStart() {
            return inStart;
        }

        @Override
        public int getInIntervalEnd() {
            return inEnd;
        }

        @Override
        public int getInIntervalLength() {
            return inEnd - inStart;
        }

        @Override
        public boolean inIntervalContains(int offset) {
            return (getInIntervalStart() <= offset && getInIntervalEnd() >= offset);
        }
        
        @Override
        public int getOutIntervalStart() {
            return outStart;
        }
        
        @Override
        public int getOutIntervalEnd() {
            return outEnd;
        }

        @Override
        public int getOutIntervalLength() {
            return outEnd - outStart;
        }
        
        @Override
        public boolean outIntervalContains(int offset) {
            return (getOutIntervalStart() <= offset && getOutIntervalEnd() >= offset);
        }
    }

    private static class IntervalCorrespondenceSimpleCompact extends IntervalCorrespondence {

        private final int offset;
        private final short length;
        private final short shift;

        public IntervalCorrespondenceSimpleCompact(int offset, short length, short shift) {
            this.offset = offset;
            this.length = length;
            this.shift = shift;
        }

        @Override
        public int getInIntervalStart() {
            return offset;
        }

        @Override
        public int getInIntervalEnd() {
            return offset+length;
        }

        @Override
        public int getInIntervalLength() {
            return length;
        }

        @Override
        public boolean inIntervalContains(int offset) {
            return (getInIntervalStart() <= offset && getInIntervalEnd() >= offset);
        }
        
        @Override
        public int getOutIntervalStart() {
            return offset+shift;
        }
        
        @Override
        public int getOutIntervalEnd() {
            return offset+shift+length;
        }

        @Override
        public int getOutIntervalLength() {
            return length;
        }
        
        @Override
        public boolean outIntervalContains(int offset) {
            return (getOutIntervalStart() <= offset && getOutIntervalEnd() >= offset);
        }
    }

    private static class IntervalCorrespondenceMacro extends IntervalCorrespondenceSimple {
        private final CharSequence macroExpansion;
        private final List<Interval> keys;
        private final List<List<Interval>> values;

        private IntervalCorrespondenceMacro(int inStart, int inEnd, int outStart, int outEnd, CharSequence macroExpansion, Map<Interval, List<Interval>> paramsToExpansion){
            super(inStart, inEnd, outStart, outEnd);
            this.macroExpansion = macroExpansion;
            if (paramsToExpansion.isEmpty()) {
                keys = Collections.<Interval>emptyList();
                values = Collections.<List<Interval>>emptyList();
            } else {
                keys = new ArrayList<>(paramsToExpansion.size());
                values = new ArrayList<>(paramsToExpansion.size());
                for (Map.Entry<Interval, List<Interval>> in : paramsToExpansion.entrySet()) {
                    keys.add(in.getKey());
                    final List<Interval> value = in.getValue();
                    if (value instanceof ArrayList) {
                        ((ArrayList)value).trimToSize();
                    }
                    values.add(value);
                }
            }
        }

        @Override
        public CharSequence getMacroExpansion() {
            return macroExpansion;
        }

        @Override
        public boolean isMacro() {
            return true;
        }

        @Override
        public List<Interval> getIntervals() {
            return keys;
        }

        @Override
        public List<Interval> getParamIntervals(int i) {
            return values.get(i);
        }
    }

    private static IntervalCorrespondence createIntervalCorrespondence(int offset) {
        return new IntervalCorrespondenceSimpleCompact(offset, (short)0, (short)0);
    }

    private static IntervalCorrespondence createIntervalCorrespondence(int inStart, int inEnd, int outStart, int outEnd, boolean macro, CharSequence macroExpansion, Map<Interval, List<Interval>> paramsToExpansion)  {
        if (macro) {
            if (paramsToExpansion != null && paramsToExpansion.isEmpty()) {
                paramsToExpansion = Collections.<Interval, List<Interval>>emptyMap();
            }
            return new IntervalCorrespondenceMacro(inStart, inEnd, outStart, outEnd, macroExpansion, paramsToExpansion);
        } else {
            if (inEnd - inStart == outEnd - outStart) {
                final int length = inEnd - inStart;
                if (Short.MIN_VALUE < length && length < Short.MAX_VALUE) {
                    final int shift = outEnd - inEnd;
                    if (Short.MIN_VALUE < shift && shift < Short.MAX_VALUE) {
                        return new IntervalCorrespondenceSimpleCompact(inStart, (short)length, (short)shift);
                    }
                }
            }
            return new IntervalCorrespondenceSimple(inStart, inEnd, outStart, outEnd);
        }
    }

    private static class TransformationTable {

        private ArrayList<IntervalCorrespondence> intervals = new ArrayList<>();
        private Map<CharSequence, CharSequence> cache = new HashMap<>();
        private final CurrentInterval currentIn = new CurrentInterval(0);
        private final CurrentInterval currentOut= new CurrentInterval(0);
        private final long documentVersion;
        private final long fileVersion;

        public TransformationTable(long documentVersion, long fileVersion) {
            this.documentVersion = documentVersion;
            this.fileVersion = fileVersion;
        }

        public void cleanUp() {
            cache = null;
        }

        public boolean isInited() {
            return cache == null;
        }

        public void setInStart(int start) {
            currentIn.init(start);
        }

        public void setOutStart(int start) {
            currentOut.init(start);
        }

        public void appendInterval(int inLength, int outLength) {
            appendInterval(inLength, outLength, false, null, null);
        }

        public void appendInterval(int inLength, int outLength, boolean macro, CharSequence macroExpansion, Map<Interval, List<Interval>> paramsToExpansion) {
            assert(cache != null);
            CharSequence cs = CharSequences.create(macroExpansion);
            CharSequence cachedCS = cache.get(cs);
            if(cachedCS != null) {
                cs = cachedCS;
            } else {
                cache.put(cs, cs);
            }
            currentIn.setLength(inLength);
            currentOut.setLength(outLength);
            intervals.add(createIntervalCorrespondence(currentIn.start,currentIn.end, currentOut.start, currentOut.end, macro, cs, paramsToExpansion));
            setInStart(currentIn.end);
            setOutStart(currentOut.end);
        }

        public int getOutOffset(int inOffset) {
            if (intervals.isEmpty()) {
                return inOffset;
            }
            if (intervals.get(0).getInIntervalStart() > inOffset) {
                int shift = intervals.get(0).getInIntervalStart() - inOffset;
                return intervals.get(0).getOutIntervalStart() - shift;
            }

            IntervalCorrespondence lastMacro = null;
            for (IntervalCorrespondence ic : intervals) {
                if (ic.getOutIntervalLength() != 0) {
                    lastMacro = null;
                }
                if (ic.isMacro()) {
                    lastMacro = ic;
                }
                if (ic.inIntervalContains(inOffset)) {
                    if(ic.getOutIntervalLength() == 0 && lastMacro != null) {
                        List<Interval> anIntervals = lastMacro.getIntervals();
                        for(int k = 0; k < anIntervals.size(); k++) {
                            Interval i = anIntervals.get(k);
                            if (i.contains(inOffset)) {
                                int shift = inOffset - i.getStart();
                                List<Interval> params = lastMacro.getParamIntervals(k);
                                Interval j = params.get(0);
                                if (shift >= j.length()) {
                                    return j.getEnd();
                                } else {
                                    return j.getStart() + shift;
                                }
                            }
                        }
                    }
                    int shift = inOffset - ic.getInIntervalStart();
                    if (shift >= ic.getInIntervalLength() || shift >= ic.getOutIntervalLength()) {
                        return ic.getOutIntervalEnd();
                    } else {
                        if(ic.isMacro()) {
                            return ic.getOutIntervalStart();
                        } else {
                            return ic.getOutIntervalStart() + shift;
                        }
                    }
                }
            }
            int shift = inOffset - intervals.get(intervals.size() - 1).getInIntervalEnd();
            return intervals.get(intervals.size() - 1).getOutIntervalEnd() + shift;
        }

        public int getInOffset(int outOffset) {
            if (intervals.isEmpty()) {
                return outOffset;
            }
            if (intervals.get(0).getOutIntervalStart() > outOffset) {
                int shift = intervals.get(0).getOutIntervalStart() - outOffset;
                return intervals.get(0).getInIntervalStart() - shift;
            }
            for (IntervalCorrespondence ic : intervals) {
                if (ic.outIntervalContains(outOffset)) {
                    if(ic.isMacro()) {
                        List<Interval> anIntervals = ic.getIntervals();
                        for(int k = 0; k < anIntervals.size(); k++) {
                            Interval i = anIntervals.get(k);
                            for(Interval j : ic.getParamIntervals(k)) {
                                if(j.contains(outOffset)) {
                                    int shift = outOffset - j.getStart();
                                    if (shift >= i.length() || shift >= j.length()) {
                                        return i.getEnd();
                                    } else {
                                        return i.getStart() + shift;
                                    }
                                }
                            }
                        }
                    }
                    int shift = outOffset - ic.getOutIntervalStart();
                    if (shift >= ic.getOutIntervalLength() || shift >= ic.getInIntervalLength()) {
                        return ic.getInIntervalEnd();
                    } else {
                        if(ic.isMacro()) {
                            return ic.getInIntervalStart();
                        } else {
                            return ic.getInIntervalStart() + shift;
                        }
                    }
                }
            }
            int shift = outOffset - intervals.get(intervals.size() - 1).getOutIntervalEnd();
            return intervals.get(intervals.size() - 1).getInIntervalEnd() + shift;
        }

        public int getNextMacroExpansionStartOffset(int outOffset) {
            if (intervals.isEmpty()) {
                return outOffset;
            }
            for (IntervalCorrespondence ic : intervals) {
                if (ic.getOutIntervalStart() <= outOffset) {
                    continue;
                }
                if (ic.isMacro()) {
                    return ic.getOutIntervalStart();
                }
            }
            return outOffset;
        }

        public int getPrevMacroExpansionStartOffset(int outOffset) {
            if (intervals.isEmpty()) {
                return outOffset;
            }
            int result = outOffset;
            for (IntervalCorrespondence ic : intervals) {
                if (ic.getOutIntervalEnd() >= outOffset) {
                    return result;
                }
                if (ic.isMacro()) {
                    result = ic.getOutIntervalStart();
                }
            }
            return outOffset;
        }

        public int findInIntervalIndex(int offset) {
            return Collections.binarySearch(intervals, createIntervalCorrespondence(offset),
                    new Comparator<IntervalCorrespondence>() {

                @Override
                        public int compare(IntervalCorrespondence o1, IntervalCorrespondence o2) {
                            if (o1.getInIntervalEnd() < o2.getInIntervalStart()) {
                                return -1;
                            }
                            if (o1.getInIntervalStart() > o2.getInIntervalEnd()) {
                                return 1;
                            }
                            return 0;
                        }
                    });
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(""); // NOI18N
            for (IntervalCorrespondence ic : intervals) {
                sb.append("[").append(ic.getInIntervalStart()).append(",").append(ic.getInIntervalEnd()).append("] => [").append(ic.getOutIntervalStart()).append(",").append(ic.getOutIntervalEnd()).append("]\n"); // NOI18N
            }
            return sb.toString();
        }
    }

    private TransformationTable getMacroTable(Document doc, CsmFile file, boolean updateIfNeeded) {
        if (file == null || doc == null) {
            return null;
        }
        Formatter formatter = null;
        if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null) {
            formatter = new Formatter((BaseDocument) doc);
        }
        TransformationTable tt;
        synchronized (doc) {
            tt = getCachedMacroTable(doc);
            if (tt == null) {
                if (updateIfNeeded) {
                    tt = new TransformationTable(DocumentUtilities.getDocumentVersion(doc), CsmFileInfoQuery.getDefault().getFileVersion(file));
                } else {
                    tt = new TransformationTable(-1, -1);
                }
                doc.putProperty(MACRO_EXPANSION_MACRO_TABLE, tt);
            }
        }
        if (!updateIfNeeded && tt.isInited()) {
            return tt;
        }
        synchronized (tt) {
            synchronized (doc) {
                tt = getCachedMacroTable(doc);
                if (updateIfNeeded) {
                    if (tt.documentVersion != DocumentUtilities.getDocumentVersion(doc) || tt.fileVersion != CsmFileInfoQuery.getDefault().getFileVersion(file)) {
                        tt = new TransformationTable(DocumentUtilities.getDocumentVersion(doc), CsmFileInfoQuery.getDefault().getFileVersion(file));
                    }
                }
            }
            if (updateIfNeeded && !tt.isInited()) {
                expand(doc, file, tt, formatter);
                tt.cleanUp();
                synchronized (doc) {
                    doc.putProperty(MACRO_EXPANSION_MACRO_TABLE, tt);
                }
            }
        }
        return tt;
    }
    
    private static final class UndoIndent implements CharSequence {
        private final int length;
        private UndoIndent(int length) {
            this.length = length;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public char charAt(int index) {
            return ' ';
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            throw new UnsupportedOperationException();
        }
    }
        
    private static final class Formatter {
        private static final boolean MACRO_BASED_INDENTER = false;
        private final int tab;
        private final Engine engine;
        private int shift;
        private int line;
        private int indent;

        Formatter(BaseDocument document) {
            // It is not valid mrthod.
            // Right method requres editor dependency (NbEditorDocument.getShiftWidth() or IndentUtils.indentLevelSize(document))
            tab = document.getShiftWidth();
            if (MACRO_BASED_INDENTER) {
                engine = new MacroBasedFormatter();
            } else {
                engine = new TokenBasedFormatter();
            }
        }

        private void init(APTToken fileToken) {
            shift = 0;
            indent = 0;
        }

        private Pair<? extends CharSequence, ? extends CharSequence> process(APTToken fileToken, MyTokenSequence fileTS) {
            return engine.process(fileToken, fileTS);
        }

        private interface Engine {
            Pair<? extends CharSequence, ? extends CharSequence> process(APTToken fileToken, MyTokenSequence fileTS);
        }

        private final class MacroBasedFormatter implements Engine {

            public Pair<? extends CharSequence, ? extends CharSequence> process(APTToken fileToken, MyTokenSequence fileTS) {
                // Keep formatting in macros if it is possible
                int aLine = 0;
                int column = 0;
                if (APTUtils.isMacroExpandedToken(fileToken) && !APTUtils.isMacroParamExpandedToken(fileToken)) {
                    aLine = APTUtils.getExpandedToken(fileToken).getLine();
                    column = APTUtils.getExpandedToken(fileToken).getColumn();
                }
                StringBuilder prefix = null;
                if (aLine > 0) {
                    if (line > 0 && aLine != line) {
                        prefix = new StringBuilder();
                        prefix.append('\n'); // NOI18N
                        if (column > 1) {
                            for (int i = 1; i < column; i++) {
                                prefix.append(' '); // NOI18N
                            }
                        }
                    }
                    line = aLine;
                }
                if (prefix == null) {
                    return null;
                }
                return Pair.of(prefix, null);
            }
        }

        private final class TokenBasedFormatter implements Engine {
            public Pair<? extends CharSequence, ? extends CharSequence> process(APTToken fileToken, MyTokenSequence fileTS) {
                // Do simplified reformatting
                shift = fileTS.file.getLineColumn(fileToken.getOffset())[1];
                switch (fileToken.getType()) {
                    case APTTokenTypes.LCURLY: // {
                    {
                        indent++;
                        StringBuilder suffix = new StringBuilder();
                        suffix.append('\n'); // NOI18N
                        for (int i = 1; i < shift + indent * tab; i++) {
                            suffix.append(' '); // NOI18N
                        }
                        return Pair.of(null, suffix);
                    }
                    case APTTokenTypes.RCURLY: // }
                    {
                        APTToken next = fileTS.LA();
                        UndoIndent undo = null;
                        if (indent > 0) {
                            //need to remove prev spaces if indent have been added before
                            undo = new UndoIndent(tab);
                            indent--;
                        }
                        if (next != null && next.getType() == APTTokenTypes.SEMICOLON) {
                            if (undo == null) {
                                return null;
                            }
                            // deferred new line;
                            return Pair.of(undo, null);
                        }
                        StringBuilder suffix = new StringBuilder();
                        suffix.append('\n'); // NOI18N
                        for (int i = 1; i < shift + indent * tab; i++) {
                            suffix.append(' '); // NOI18N
                        }
                        return Pair.of(undo, suffix);
                    }
                    case APTTokenTypes.SEMICOLON: // ;
                    {
                        StringBuilder suffix = new StringBuilder();
                        suffix.append('\n'); // NOI18N
                        for (int i = 1; i < shift + indent * tab; i++) {
                            suffix.append(' '); // NOI18N
                        }
                        return Pair.of(null, suffix);
                    }
                    case APTTokenTypes.LITERAL_for: // for
                        // TODO: Nice to detect "for" scope and do not insert new line on semicolumn
                        break;
                    default:
                        break;
                }
                return null;
            }
        }
    }
    
}
