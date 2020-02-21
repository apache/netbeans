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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import static org.clang.basic.BasicClangGlobals.*;
import org.clang.basic.SourceLocation;
import org.clang.basic.SourceManager;
import org.clang.basic.tok;
import org.clang.lex.Lexer;
import org.clang.lex.Preprocessor;
import org.clang.lex.llvm.SmallVectorToken;
import org.clang.lex.Token;
import org.clang.tools.services.support.FileInfoCallback;
import static org.clank.java.std.*;
import org.llvm.adt.SmallString;
import org.netbeans.modules.cnd.apt.impl.support.APTCommentToken;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteConstTextToken;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteIdToken;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteLiteralToken;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.apt.impl.support.APTConstTextToken;

/**
 *
 */
/*package*/
class ClankToAPTToken implements APTToken {

    static APTToken[] convertToAPT(Preprocessor PP, SmallVectorToken toks, boolean needLineColumns) {
        int nrTokens = toks.size();
        Token[] tokens = toks.$array();
        APTToken[] out = new APTToken[nrTokens];
        SmallString spell = new SmallString(1024);

        // cache for last function-like macro expansion range
        long/*<SourceLocation, SourceLocation>*/ lastExpansionRange = -1L;
        int lastExpandedStartOffset = 0;
        APTCommentToken lastEndOffsetToken = null;
        int macroTokenIndex = -1;
        ///////

        for (int i = 0; i < nrTokens; i++) {
            assert PP != null;
            SourceManager SM = PP.getSourceManager();
            assert SM != null;
            Token token = tokens[i];
            FileInfoCallback.MacroExpansionInfo info = null;
            if (token.isAnnotation() && (token.getAnnotationValue() instanceof FileInfoCallback.MacroExpansionInfo)) {
                info = (FileInfoCallback.MacroExpansionInfo)token.getAnnotationValue();
            }
            int rawLocation = token.$getLocation();
            int offset;
            // is the token real macro expansion?
            boolean isFromRealMacro = (info == null) && SourceLocation.isMacroID(rawLocation);
            APTCommentToken endOffsetToken = null;
            APTToken converted;
            boolean needToWrapAsMacro = isFromRealMacro || (info != null);
            if (info != null) {
                // handle annotated macro expansion range
                // convert it into comment token and use as end offset token
                // duing macro expansion
                converted = ClankToAPTToken.convertAnnotation(PP, token, info, needLineColumns);
                lastExpandedStartOffset = info.getStartOffset();
                lastExpansionRange = info.$getExpansionRange();
                lastEndOffsetToken = (APTCommentToken)converted;
                endOffsetToken = lastEndOffsetToken;
                needToWrapAsMacro = true;
                macroTokenIndex = -1; // clear macro index - this is the next expansion
                assert APTUtils.isCommentToken(converted) : "annotated token must be comment";
            } else if (isFromRealMacro) {
                // reuse start/end if was already calculated for this range
                long/*<SourceLocation, SourceLocation>*/ curExpansionRange = SM.getExpansionRange(rawLocation);
                if (lastExpansionRange != curExpansionRange) {
                    long/*<FileID, uint>*/ decomposedRangeStart = SM.getDecomposedLoc($first_SourceLocation(curExpansionRange));
                    long/*<FileID, uint>*/ decomposedRangeEnd = SM.getDecomposedLoc($second_SourceLocation(curExpansionRange));
                    lastExpandedStartOffset = $second_offset(decomposedRangeStart);
                    // end offset is start of the last token in expRange, so add TokSize
                    int TokSize = Lexer.MeasureTokenLength($second_SourceLocation(curExpansionRange), SM, PP.getLangOpts());
                    int expandedEndOffset = $second_offset(decomposedRangeEnd);
                    expandedEndOffset += TokSize;

                    int tokenEndLine = FAKE_LINE;
                    int tokenEndColumn = FAKE_COLUMN;
                    if (needLineColumns) {
                        tokenEndLine = SM.getLineNumber($first_FileID(decomposedRangeEnd), $second_offset(decomposedRangeEnd), null);
                        tokenEndColumn = SM.getColumnNumber($first_FileID(decomposedRangeEnd), $second_offset(decomposedRangeEnd), null);
                    }
                    lastEndOffsetToken = new APTCommentToken();
                    lastEndOffsetToken.setType(APTTokenTypes.COMMENT);
                    lastEndOffsetToken.setOffset(expandedEndOffset);
                    lastEndOffsetToken.setTextLength(0);
                    lastEndOffsetToken.setColumn(tokenEndColumn);
                    lastEndOffsetToken.setLine(tokenEndLine);
                    // remember range marker
                    lastExpansionRange = curExpansionRange;
                    macroTokenIndex = -1; // clear macro index - this is the next expansion
                }
                endOffsetToken = lastEndOffsetToken;
                offset = lastExpandedStartOffset;
                converted = ClankToAPTToken.convert(PP, token, offset, spell, needLineColumns);
                needToWrapAsMacro = true;
            } else {
                long/*<FileID, uint>*/ decomposedLoc = SM.getDecomposedLoc(rawLocation);
                offset = $second_offset(decomposedLoc);
                converted = ClankToAPTToken.convert(PP, token, offset, spell, needLineColumns);
                macroTokenIndex = -1; // clear macro index - we are not in expansion
            }
            if (needToWrapAsMacro) {
                assert endOffsetToken != null;
                if (info == null) {
                    converted = new ClankMacroExpandedToken(converted, endOffsetToken, ++macroTokenIndex);
                } else {
                    // annotated tokens should not have macro indexes
                    converted = new ClankMacroExpandedToken(converted, endOffsetToken, -1); 
                }
                assert info == null || APTUtils.isCommentToken(converted) : "annotated token must be macro expanded comment";
            }
            out[i] = converted;
        }
        return out;
    }

    private static APTToken convertAnnotation(Preprocessor PP, Token token, FileInfoCallback.MacroExpansionInfo info, boolean needLineColumns) {
        int tokenLine = FAKE_LINE;
        int tokenColumn = FAKE_COLUMN;
        if (needLineColumns) {
            SourceManager SM = PP.getSourceManager();
            long/*<FileID, uint>*/ LocInfo = SM.getDecomposedExpansionLoc(token.$getLocation());
            tokenLine = SM.getLineNumber($first_FileID(LocInfo), $second_offset(LocInfo), null);
            tokenColumn = SM.getColumnNumber($first_FileID(LocInfo), $second_offset(LocInfo), null);
        }
        APTCommentToken out = new APTCommentToken();
        out.setType(APTTokenTypes.COMMENT);
        out.setOffset(info.getStartOffset());
        out.setTextLength(info.getEndOffset() - info.getStartOffset());
        out.setColumn(tokenColumn);
        out.setLine(tokenLine);
        return out;
    }

    static APTToken convert(Preprocessor PP, Token token, /*uint*/ int offset, SmallString spell, boolean needLineColumns) {
        if (token.is(tok.TokenKind.eof)) {
            return APTUtils.EOF_TOKEN;
        } else {
            int tokenLine = FAKE_LINE;
            int tokenColumn = FAKE_COLUMN;
            if (needLineColumns) {
                SourceManager SM = PP.getSourceManager();
                long/*<FileID, uint>*/ LocInfo = SM.getDecomposedExpansionLoc(token.$getLocation());
                tokenLine = SM.getLineNumber($first_FileID(LocInfo), $second_offset(LocInfo), null);
                tokenColumn = SM.getColumnNumber($first_FileID(LocInfo), $second_offset(LocInfo), null);
            }
            int aptTokenType = ClankToAPTUtils.convertClankToAPTTokenKind(token.getKind());
            if (APTLiteConstTextToken.isApplicable(aptTokenType, offset, tokenColumn, tokenLine)) {
                APTToken out = new APTLiteConstTextToken(aptTokenType, offset, tokenColumn, tokenLine);
                return out;
            } else if (aptTokenType == APTTokenTypes.COMMENT) {
                APTCommentToken out = new APTCommentToken();
                out.setType(APTTokenTypes.COMMENT);
                out.setOffset(offset);
                out.setTextLength(token.getLength());
                out.setColumn(tokenColumn);
                out.setLine(tokenLine);
                return out;
            } else {
                CharSequence textID = ClankToAPTUtils.getTokenText(token, PP, spell);
                int literalType = aptTokenType;
                if (aptTokenType > APTTokenTypes.FIRST_LITERAL_TOKEN && aptTokenType < APTTokenTypes.LAST_LITERAL_TOKEN) {
                    // convert all keywords into IDENT, then it should be converted
                    // to keyword by language filter
                    aptTokenType = APTTokenTypes.IDENT;
                } else if (aptTokenType == APTTokenTypes.DECIMALINT) {
                    // TODO: adjust numeric token kind by text
                    // parser wants '0' to be OCTALINT for i.e. pure virtual methods
                    if (textID.length() > 0 && textID.charAt(0) == '0') {
                        aptTokenType = APTTokenTypes.OCTALINT;
                    }
                }
                if (APTLiteLiteralToken.isApplicable(APTTokenTypes.IDENT, offset, tokenColumn, tokenLine, literalType)) {
                    CharSequence LiteText = APTConstTextToken.getConstTextID(literalType);
                    // check if spelling in clang the same as our token, then reuse
                    // APTLiteLiteralToken otherwise create fallback to APTLiteIdToken with known textID
                    if (CharSequences.comparator().compare(textID, LiteText) == 0) {
                        return new APTLiteLiteralToken(offset, tokenColumn, tokenLine, literalType);
                    }
                }
                if (APTLiteIdToken.isApplicable(aptTokenType, offset, tokenColumn, tokenLine)) {
                    return new APTLiteIdToken(offset, tokenColumn, tokenLine, textID);
                } else {
                    if (needLineColumns) {
                        return new ClankToAPTTokenWithLineAndColumn(token, aptTokenType, offset, tokenColumn, tokenLine, textID);
                    }
                    return new ClankToAPTToken(token, aptTokenType, offset, textID);
                }
            }
        }
    }

    private static final int FAKE_LINE = 333;
    private static final int FAKE_COLUMN = 111;

    private final int endOffset;
    private final int aptTokenType;
    private final int offset;
    private final CharSequence textID;

    private ClankToAPTToken(Token token, int tokenType, int offset, CharSequence text) {
        this.offset = offset;
        assert offset >= 0 : "negative " + offset + " for " + token;
        this.endOffset = this.offset + token.getLength();
        this.aptTokenType = tokenType;
        assert !(APTLiteConstTextToken.isLiteConstTextType(aptTokenType));
        assert !(APTLiteLiteralToken.isApplicable(APTTokenTypes.IDENT, offset, FAKE_COLUMN, FAKE_LINE, aptTokenType));
        assert !(APTLiteIdToken.isApplicable(aptTokenType, offset, FAKE_COLUMN, FAKE_LINE));
        assert (text != null);
        assert CharSequences.isCompact(text);
        assert (token.isNot(tok.TokenKind.comment));
        textID = TextCache.getManager().getString(text);
        assert textID.length() <= token.getLength() : textID + "\n vs. \n" + token;
    }

    @Override
    public int getType() {
        return aptTokenType;
    }

    @Override
    public String getText() {
        return getTextID().toString();
    }

    @Override
    public CharSequence getTextID() {
        return textID;
    }

    @Override
    public String toString() {
        return "ClankToAPTToken{offset=" + offset + "; aptType=" + APTUtils.getAPTTokenName(aptTokenType) + ":" + textID + '}'; // NOI18N
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public void setEndOffset(int o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getEndColumn() {
        return 222;
    }

    @Override
    public void setEndColumn(int c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getEndLine() {
        return 444;
    }

    @Override
    public void setEndLine(int l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTextID(CharSequence id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getProperty(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumn() {
        return FAKE_COLUMN;
    }

    @Override
    public void setColumn(int c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLine() {
        return FAKE_LINE;
    }

    @Override
    public void setLine(int l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public void setFilename(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setText(String t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setType(int t) {
        throw new UnsupportedOperationException();
    }

    private static final class ClankToAPTTokenWithLineAndColumn extends ClankToAPTToken {

        private final int line;
        private final int column;
        private final int endColumn;

        public ClankToAPTTokenWithLineAndColumn(Token token, int tokenType, int offset, int column, int line, CharSequence text) {
            super(token, tokenType, offset, text);
            this.line = line;
            this.column = column;
            this.endColumn = column + token.getLength();
        }

        @Override
        public int getLine() {
            return this.line;
        }

        @Override
        public int getEndLine() {
            return this.line;
        }

        @Override
        public int getColumn() {
            return column;
        }

        @Override
        public int getEndColumn() {
            return this.endColumn;
        }
    }
}
