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

package org.netbeans.modules.cnd.apt.utils;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.structure.APTDefineNode;
import org.netbeans.modules.cnd.apt.impl.structure.APTNodeBuilder;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteConstTextToken;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.impl.support.APTCommentToken;
import org.netbeans.modules.cnd.apt.impl.support.APTConstTextToken;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteIdToken;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteLiteralToken;
import org.netbeans.modules.cnd.apt.impl.support.APTMacroParamExpansion;
import org.netbeans.modules.cnd.apt.impl.support.APTTestToken;
import org.netbeans.modules.cnd.apt.impl.support.MacroExpandedToken;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankDriverImpl;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankMacroExpandedToken;
import org.netbeans.modules.cnd.apt.impl.support.generated.APTExprParser;
import org.netbeans.modules.cnd.apt.support.lang.APTBaseLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 * APT utilities
 */
public class APTUtils {
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.apt"); // NOI18N
    
    public static final String SCOPE = "::"; // NOI18N
            
    public static final int NOT_AN_EXPANDED_TOKEN = -1;

    static {
        // command line param has priority for logging
        String level = System.getProperty("org.netbeans.modules.cnd.apt.level"); // NOI18N
        // do not change it
        if (level == null) {
            // command line param has priority for logging
            if (APTTraceFlags.TRACE_APT | APTTraceFlags.TRACE_APT_LEXER) {
                LOG.setLevel(Level.ALL);
            } else {
                LOG.setLevel(Level.SEVERE);
            }
        } else {
            try {
                LOG.setLevel(Level.parse(level));
            } catch (IllegalArgumentException e) {
                // skip
            }
        }
    }

    public static CharSequence getFileOnceMacroName(APTFile apt) {
        // use Unix like separators to be the same on Win/Unix
        String path = apt.getPath().toString().replace("\\", "/");//NOI18N
        if (CndUtils.isUnitTestMode()) {
            String TEST_DATA_DIR = "/unit/data/";//NOI18N
            int idx = path.indexOf(TEST_DATA_DIR);
            assert idx > 0 : "no " + TEST_DATA_DIR + " prefix in " + path;
            path = path.substring(idx + TEST_DATA_DIR.length());
        }
        return CharSequences.create(CharSequenceUtils.concatenate("\"", path, "\"")); //NOI18N
    }

    public static String getAPTTokenName(int type) {
        if (type == APTTokenTypes.IDENT) {
            return "ID"; // NOI18N
        } else if (type == EOF_TOKEN2.getType()) {
            return "EOF3"; // NOI18N
        }
        return APTExprParser._tokenNames[type];
    }

    /**
     * dumps APT related statistics (for test diagnostics)
     */
    public static void dumpStatistics() {
        APTDriver.dumpStatistics();
    }

    /** Creates a new instance of APTUtils */
    private APTUtils() {
    }

    public static int hash(int h) {
        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }

    public static int hash(List<?> list) {
        if (list == null) {
            return 0;
        }
        int hashCode = 1;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Object obj = list.get(i);
            hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
        }
        return hash(hashCode);
    }

    public static boolean equalArrayLists(List<?> l1, List<?> l2) {
        if (l1 != l2) {
            if (l1 == null || l2 == null) {
                return false;
            } else {
                int n1 = l1.size();
                int n2 = l2.size();
                if (n1 != n2) {
                    return false;
                }
                for (int i = 0; i < n1; i++) {
                    if (!l1.get(i).equals(l2.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            return true;
        }
    }

    public static void setTokenText(APTToken _token, char buf[], int start, int count) {
        if (_token instanceof APTBaseToken || _token instanceof APTLiteIdToken) {
            _token.setTextID(CharSequences.create(buf, start, count));
        } else if (_token instanceof APTCommentToken) {
            // no need to set text in comment token, but set text len
            ((APTCommentToken)_token).setTextLength(count);
        } else if (_token instanceof APTConstTextToken) {
            // no need to set text in const token
        } else if (_token instanceof APTLiteConstTextToken || _token instanceof APTLiteLiteralToken) {
            // no need to set text in const token
        } else {
            System.err.printf("unexpected token %s while assigning text %s", _token, new String(buf, start, count));
            _token.setText(new String(buf, start, count));
        }
    }

    private static final String DEFINE_PREFIX = "#define "; // NOI18N

    public static APTDefine createAPTDefineOnce(CharSequence filePath) {
        APTDefineNode defNode = null;
        String text = DEFINE_PREFIX + filePath;
        TokenStream stream = APTTokenStreamBuilder.buildTokenStream(text, APTFile.Kind.C_CPP);
        try {
            APTToken next = (APTToken) stream.nextToken();
            // use define node to initialize #define directive from stream
            APTToken fileName = (APTToken) stream.nextToken();
            defNode = new APTDefineNode(next, fileName);
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, "error on lexing macros {0}\n\t{1}", new Object[]{filePath, ex.getMessage()});
        }
        return defNode;
    }

    public static APTDefine createAPTDefine(String macroText) {
        APTNodeBuilder nodeBuilder = null;
        macroText = DEFINE_PREFIX + macroText;
        TokenStream stream = APTTokenStreamBuilder.buildTokenStream(macroText, APTFile.Kind.C_CPP);
        try {
            APTToken next = (APTToken) stream.nextToken();
            // use define node to initialize #define directive from stream
            nodeBuilder = new APTDefineNode.Builder(next);
            boolean look4Equal = true;
            do {
                next = (APTToken) stream.nextToken();
                if (look4Equal && (next.getType() == APTTokenTypes.ASSIGNEQUAL)) {
                    // skip the first equal token, it's delimeter
                    look4Equal = false;
                    next = (APTToken) stream.nextToken();
                }
            } while (nodeBuilder.accept(null, next));
            // special check for macros without values, we must set it to be 1
            if (((APTDefineNode)nodeBuilder.getNode()).getBody().isEmpty() && look4Equal) {
                nodeBuilder.accept(null, APTUtils.DEF_MACRO_BODY);
            }
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, "error on lexing macros {0}\n\t{1}", new Object[]{macroText, ex.getMessage()});
        }
        return (APTDefineNode)nodeBuilder.getNode();
    }

    public static APTToken createAPTToken(int type, int startOffset, int endOffset, 
            int startColumn, int startLine, int endColumn, int endLine, int literalType) {
        // TODO: optimize factory
        if (APTLiteConstTextToken.isApplicable(type, startOffset, startColumn, startLine)){
            return new APTLiteConstTextToken(type, startOffset, startColumn, startLine);
        } else if (APTLiteLiteralToken.isApplicable(type, startOffset, startColumn, startLine, literalType)){
            return new APTLiteLiteralToken(startOffset, startColumn, startLine, literalType);
        } else if (APTLiteIdToken.isApplicable(type, startOffset, startColumn, startLine)){
            return new APTLiteIdToken(startOffset, startColumn, startLine);
        }
        APTToken out = createAPTToken(type);
        out.setType(type);
        out.setColumn(startColumn);
        out.setLine(startLine);
        out.setOffset(startOffset);
        out.setEndOffset(endOffset);
        out.setEndColumn(endColumn);
        out.setEndLine(endLine);
        return out;
    }

    public static APTToken createIDENT(CharSequence text) {
        assert CharSequences.isCompact(text);
        APTToken out = createAPTToken(APTTokenTypes.IDENT);
        out.setType(APTTokenTypes.IDENT);
        out.setTextID(text);
        return out;
    }
    
    public static APTToken createAPTToken(int type) {
        // Preprocessor tokens can be made constText, but we can get '#define' and '# define'
        // which have different text. so for now they are treated as usual tokens
        if (isPreprocessorToken(type)) {
            return APTTraceFlags.USE_APT_TEST_TOKEN ? (APTToken)new APTTestToken() : new APTBaseToken();
        }
        switch (type) {
            // IDs
            case APTTokenTypes.IDENT:
            case APTTokenTypes.ID_DEFINED:
                // Strings and chars
            case APTTokenTypes.STRING_LITERAL:
            case APTTokenTypes.CHAR_LITERAL:
                // Numbers
            case APTTokenTypes.DECIMALINT:
            case APTTokenTypes.HEXADECIMALINT:
            case APTTokenTypes.BINARYINT:
            case APTTokenTypes.FLOATONE:
            case APTTokenTypes.FLOATTWO:
            case APTTokenTypes.OCTALINT:
            case APTTokenTypes.NUMBER:
                // Include strings
            case APTTokenTypes.INCLUDE_STRING:
            case APTTokenTypes.SYS_INCLUDE_STRING:
                return APTTraceFlags.USE_APT_TEST_TOKEN ? (APTToken)new APTTestToken() : new APTBaseToken();
                
                // Comments
            case APTTokenTypes.CPP_COMMENT:
            case APTTokenTypes.COMMENT:
            case APTTokenTypes.FORTRAN_COMMENT:
                return new APTCommentToken();
                
            default: /*assert(APTConstTextToken.constText[type] != null) : "Do not know text for constText token of type " + type;  // NOI18N*/
                return new APTConstTextToken();
        }
    }

    public static APTToken getLastToken(TokenStream ts) {
        APTToken last = null;
        try {
            for (APTToken token = (APTToken) ts.nextToken(); !APTUtils.isEOF(token);) {
                assert (token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                last = token;
                token = (APTToken) ts.nextToken();
            }
        } catch (TokenStreamException ex) {
            // ignore
        }
        return last;
    }

    public static CharSequence debugString(TokenStream ts) {
        // use simple stringize
        return stringize(ts, false);
    }
    
    public static String toString(TokenStream ts) {
        StringBuilder retValue = new StringBuilder();
        try {
            for (Token token = ts.nextToken();!isEOF(token);) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                retValue.append(token.toString());
                
                token=ts.nextToken();
                
                if (!isEOF(token)) {
                    retValue.append(" "); // NOI18N
                }
            }
        } catch (TokenStreamException ex) {
            LOG.log(Level.SEVERE, "error on converting token stream to text\n{0}", new Object[] { ex }); // NOI18N
        }
        return retValue.toString();
    }
    
    public static CharSequence stringize(TokenStream ts, boolean inIncludeDirective) {
        StringBuilder retValue = new StringBuilder();
        try {
            for (APTToken token = (APTToken)ts.nextToken();!isEOF(token);) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                retValue.append(token.getTextID());
                APTToken next =(APTToken)ts.nextToken();
                if (!isEOF(next) && !inIncludeDirective) { // disable for IZ#124635
                    // if tokens were without spaces => no space
                    // if were with spaces => insert only one space
                    retValue.append(next.getOffset() == token.getEndOffset() ? "" : ' ');// NOI18N
                }
                token = next;
            }
        } catch (TokenStreamException ex) {
            LOG.log(Level.SEVERE, "error on stringizing token stream\n{0}", new Object[] { ex }); // NOI18N
        }
        return retValue;
    }
    
    public static String macros2String(Collection<? extends CharSequence> macros) {
        StringBuilder retValue = new StringBuilder();
        retValue.append("MACROS (sorted ").append(macros.size()).append("):\n"); // NOI18N
        List<CharSequence> macrosSorted = new ArrayList<CharSequence>(macros);
        Collections.sort(macrosSorted, CharSequences.comparator());
        for (CharSequence macro : macrosSorted) {
            assert(macro != null);
            retValue.append(macro);
            retValue.append("'\n"); // NOI18N
        }
        return retValue.toString();
    }

    public static String macros2String(Map<CharSequence/*getTokenTextKey(token)*/, APTMacro> macros) {
        StringBuilder retValue = new StringBuilder();
        retValue.append("MACROS (sorted ").append(macros.size()).append("):\n"); // NOI18N
        List<CharSequence> macrosSorted = new ArrayList<CharSequence>(macros.keySet());
        Collections.sort(macrosSorted, CharSequences.comparator());
        for (CharSequence key : macrosSorted) {
            APTMacro macro = macros.get(key);
            assert(macro != null);
            retValue.append(macro);
            retValue.append("'\n"); // NOI18N
        }
        return retValue.toString();
    }
    
    public static CharSequence includes2String(List<IncludeDirEntry> includePaths) {
        StringBuilder retValue = new StringBuilder();
        for (Iterator<IncludeDirEntry> it = includePaths.iterator(); it.hasNext();) {
            IncludeDirEntry path = it.next();
            retValue.append(CndFileSystemProvider.toUrl(path.getFileSystem(), path.getAsSharedCharSequence()));
            if (it.hasNext()) {
                retValue.append('\n'); // NOI18N
            }
        }
        return retValue;
    }
    
    public static boolean isPreprocessorToken(Token token) {
        assert (token != null);
        return isPreprocessorToken(token.getType());
    }
    
    public static boolean isPreprocessorToken(int/*APTTokenTypes*/ ttype) {
        switch (ttype) {
            case APTTokenTypes.PREPROC_DIRECTIVE:
            case APTTokenTypes.INCLUDE:
            case APTTokenTypes.INCLUDE_NEXT:
            case APTTokenTypes.DEFINE:
            case APTTokenTypes.UNDEF:
            case APTTokenTypes.IFDEF:
            case APTTokenTypes.IFNDEF:
            case APTTokenTypes.IF:
            case APTTokenTypes.ELIF:
            case APTTokenTypes.ELSE:
            case APTTokenTypes.ENDIF:
            case APTTokenTypes.PRAGMA:
            case APTTokenTypes.LINE:
            case APTTokenTypes.ERROR:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isID(Token token) {
        return token != null && token.getType() == APTTokenTypes.IDENT;
    }

    public static boolean isFortranKeyword(int tokenType) {
        APTLanguageFilter filter = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.FORTRAN);
        if (filter instanceof APTBaseLanguageFilter) {
            return ((APTBaseLanguageFilter) filter).isKeyword(tokenType);
        }
        return false;
    }

    public static boolean isInt(Token token) {
        if (token != null) {
            switch (token.getType()) {
                case APTTokenTypes.DECIMALINT:
                case APTTokenTypes.HEXADECIMALINT:
                case APTTokenTypes.OCTALINT:
                case APTTokenTypes.BINARYINT:
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isEOF(Token token) {
        assert (token != null);
        return token == null || isEOF(token.getType());
    }
    
    public static boolean isEOF(int ttype) {
        return ttype == APTTokenTypes.EOF || ttype == EOF3;
    }
    
    public static boolean isVaArgsToken(APTToken token) {
        return token != null && token.getTextID().equals(VA_ARGS_TOKEN.getTextID());
    }
    
    public static boolean isStartConditionNode(int/*APT.Type*/ ntype) {
        switch (ntype) {
            case APT.Type.IFDEF:
            case APT.Type.IFNDEF:
            case APT.Type.IF:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isStartOrSwitchConditionNode(int/*APT.Type*/ ntype) {
        switch (ntype) {
            case APT.Type.IFDEF:
            case APT.Type.IFNDEF:
            case APT.Type.IF:
            case APT.Type.ELIF:
            case APT.Type.ELSE:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isEndCondition(Token token) {
        return isEndCondition(token.getType());
    }
    
    public static boolean isEndCondition(int/*APTTokenTypes*/ ttype) {
        switch (ttype) {
            case APTTokenTypes.ELIF:
            case APTTokenTypes.ELSE:
            case APTTokenTypes.ENDIF:
                return true;
            default:
                return false;
        }
    }

    public static boolean isEndConditionNode(int/*APT.Type*/ ntype) {
        switch (ntype) {
            case APT.Type.ELIF:
            case APT.Type.ELSE:
            case APT.Type.ENDIF:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isCommentToken(Token token) {
        assert (token != null);
        return isCommentToken(token.getType());
    }
    
    public static boolean isCommentToken(int ttype) {
        switch (ttype) {
            case APTTokenTypes.COMMENT:
            case APTTokenTypes.CPP_COMMENT:
            case APTTokenTypes.FORTRAN_COMMENT:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isOpenBracket(Token token) {
        assert (token != null);
        return isOpenBracket(token.getType());
    }
    
    public static boolean isOpenBracket(int ttype) {
        switch (ttype) {
            case APTTokenTypes.LCURLY:
            case APTTokenTypes.LPAREN:
            case APTTokenTypes.LSQUARE:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isCloseBracket(Token token) {
        assert (token != null);
        return isCloseBracket(token.getType());
    }
    
    public static boolean isCloseBracket(int ttype) {
        switch (ttype) {
            case APTTokenTypes.RCURLY:
            case APTTokenTypes.RPAREN:
            case APTTokenTypes.RSQUARE:
                return true;
            default:
                return false;
        }
    }
    
    public static int getMatchBracket(int ttype) {
        switch (ttype) {
            case APTTokenTypes.RCURLY:
                return APTTokenTypes.LCURLY;
            case APTTokenTypes.RPAREN:
                return APTTokenTypes.LPAREN;
            case APTTokenTypes.RSQUARE:
                return APTTokenTypes.LSQUARE;
            case APTTokenTypes.LCURLY:
                return APTTokenTypes.RCURLY;
            case APTTokenTypes.LPAREN:
                return APTTokenTypes.RPAREN;
            case APTTokenTypes.LSQUARE:
                return APTTokenTypes.RSQUARE;
            default:
                return APTUtils.EOF_TOKEN.EOF_TYPE;
        }
    }    
    
    public static boolean isEndDirectiveToken(int ttype) {
        switch(ttype) {
            case APTTokenTypes.END_PREPROC_DIRECTIVE:
            case APTTokenTypes.EOF:
                return true;
        }
        return false;
    }

    public static boolean isMacroExpandedToken(Token token) {
        if(token instanceof MacroExpandedToken) {
            return true;
        } else if (token instanceof APTBaseLanguageFilter.FilterToken) {
            return isMacroExpandedToken(((APTBaseLanguageFilter.FilterToken)token).getOriginalToken());
        } else if (token instanceof ClankMacroExpandedToken) {
            return true;
        }
        return false;
    }

    public static boolean isMacroParamExpandedToken(Token token) {
        if (token instanceof APTMacroParamExpansion) {
            return true;
        } else if (token instanceof MacroExpandedToken) {
            return isMacroParamExpandedToken(((MacroExpandedToken) token).getTo());
        } else if (token instanceof APTBaseLanguageFilter.FilterToken) {
            return isMacroParamExpandedToken(((APTBaseLanguageFilter.FilterToken) token).getOriginalToken());
        } else if (token instanceof ClankMacroExpandedToken) {
            return isMacroParamExpandedToken(((ClankMacroExpandedToken) token).getTo());
        }
        return false;
    }

    public static APTToken getExpandedToken(APTToken token) {
        if (token instanceof APTMacroParamExpansion) {
            return getExpandedToken(((APTMacroParamExpansion) token).getOriginal());
        } else if (token instanceof MacroExpandedToken) {
            return getExpandedToken(((MacroExpandedToken) token).getTo());
        } else if (token instanceof APTBaseLanguageFilter.FilterToken) {
            return getExpandedToken(((APTBaseLanguageFilter.FilterToken) token).getOriginalToken());
        } else if (token instanceof ClankMacroExpandedToken) {
            return getExpandedToken(((ClankMacroExpandedToken) token).getTo());
        }
        return token;
    }
    
    public static int getExpandedTokenMarker(APTToken token) {
        if (token instanceof APTBaseLanguageFilter.FilterToken) {
            return getExpandedTokenMarker(((APTBaseLanguageFilter.FilterToken) token).getOriginalToken());
        } else if (token instanceof ClankMacroExpandedToken) {
            return ((ClankMacroExpandedToken) token).getMacroIndex();
        } else if (isMacroExpandedToken(token)) {
            APTToken expanded = getExpandedToken(token);
            return expanded.getOffset();
        }
        return NOT_AN_EXPANDED_TOKEN;
    }

    public static boolean areAdjacent(APTToken left, APTToken right) {
        while ((left instanceof MacroExpandedToken || left instanceof ClankMacroExpandedToken)
            && (right instanceof MacroExpandedToken || right instanceof ClankMacroExpandedToken)) {
            left = (left instanceof MacroExpandedToken) ? ((MacroExpandedToken) left).getTo() : ((ClankMacroExpandedToken) left).getTo();
            right = (right instanceof MacroExpandedToken) ? ((MacroExpandedToken) right).getTo() : ((ClankMacroExpandedToken) right).getTo();
        }
//        if (left instanceof APTToken && right instanceof APTToken) {
        return (left).getEndOffset() == (right).getOffset();
//        } else {
//            return left.getLine() == right.getLine()
//                    && left.getColumn() + left.getText().length() == right.getColumn();
//        }
    }

    public static List<APTToken> toList(TokenStream ts) {
        if (ts instanceof ClankDriverImpl.ArrayBasedAPTTokenStream) {
            return ((ClankDriverImpl.ArrayBasedAPTTokenStream) ts).toList();
        } else {
            LinkedList<APTToken> tokens = new LinkedList<APTToken>();
            try {
                APTToken token = (APTToken) ts.nextToken();
                while (!isEOF(token)) {
                    assert (token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                    tokens.add(token);
                    token = (APTToken) ts.nextToken();
                }
            } catch (TokenStreamException ex) {
                LOG.log(Level.INFO, "error on converting token stream to list", ex.getMessage()); // NOI18N
            }
            return tokens;
        }
    }
    
    public static Object getTextKey(String text) {
        assert (text != null);
        assert (text.length() > 0);
        // now use text as is, but it will be faster to use textID
        return text;
    }
    
    public static APTToken createAPTToken(APTToken token, int ttype) {
        APTToken newToken;
        if (APTTraceFlags.USE_APT_TEST_TOKEN) {
            newToken = new APTTestToken(token, ttype);
        } else {
            newToken = new APTBaseToken(token, ttype);
        }
        return newToken;
    }
    
    public static APTToken createAPTToken(APTToken token) {
        return createAPTToken(token, token.getType());
    }
    
    public static APTToken createAPTToken() {
        APTToken newToken;
        if (APTTraceFlags.USE_APT_TEST_TOKEN) {
            newToken = new APTTestToken();
        } else {
            newToken = new APTBaseToken();
        }
        return newToken;
    }
    
    public static final APTToken VA_ARGS_TOKEN; // support ELLIPSIS for IZ#83949 in macros
    public static final APTToken EMPTY_ID_TOKEN; // support ELLIPSIS for IZ#83949 in macros
    public static final APTToken COMMA_TOKEN; // support ELLIPSIS for IZ#83949 in macros
    public static final APTToken DEF_MACRO_BODY; //support "1" as content of #defined tokens without body IZ#122091
    static {
        VA_ARGS_TOKEN = createAPTToken();
        VA_ARGS_TOKEN.setType(APTTokenTypes.IDENT);
        VA_ARGS_TOKEN.setText("__VA_ARGS__"); // NOI18N
        
        EMPTY_ID_TOKEN = createAPTToken();
        EMPTY_ID_TOKEN.setType(APTTokenTypes.IDENT);
        EMPTY_ID_TOKEN.setText(""); // NOI18N        

        COMMA_TOKEN = createAPTToken(APTTokenTypes.COMMA);
        COMMA_TOKEN.setType(APTTokenTypes.COMMA);
        COMMA_TOKEN.setText(","); // NOI18N             
        
        DEF_MACRO_BODY = createAPTToken();
        DEF_MACRO_BODY.setType(APTTokenTypes.NUMBER);
        DEF_MACRO_BODY.setText("1"); // NOI18N
    }
    
    public static final APTToken EOF_TOKEN = new APTEOFToken();    
    public static final APTToken EOF_TOKEN2 = new APTEOFTokenAntlr3();
    private static final int EOF3 = -1; // EOF in Antrl3 is -1
    
    public static final TokenStream EMPTY_STREAM = new TokenStream() {
        @Override
        public Token nextToken() throws TokenStreamException {
            return EOF_TOKEN;
        }
    };
    
    private static final class APTEOFToken extends APTTokenAbstact {
        public APTEOFToken() {
        }
        
        @Override
        public int getOffset() {
            throw new UnsupportedOperationException("getOffset must not be used"); // NOI18N
        }
        
        @Override
        public void setOffset(int o) {
            throw new UnsupportedOperationException("setOffset must not be used"); // NOI18N
        }
        
        @Override
        public int getEndOffset() {
            throw new UnsupportedOperationException("getEndOffset must not be used"); // NOI18N
        }
        
        @Override
        public void setEndOffset(int o) {
            throw new UnsupportedOperationException("setEndOffset must not be used"); // NOI18N
        }
        
        @Override
        public CharSequence getTextID() {
            throw new UnsupportedOperationException("getTextID must not be used"); // NOI18N
        }
        
        @Override
        public void setTextID(CharSequence id) {
            throw new UnsupportedOperationException("setTextID must not be used"); // NOI18N
        }
        
        @Override
        public int getEndColumn() {
            throw new UnsupportedOperationException("getEndColumn must not be used"); // NOI18N
        }
        
        @Override
        public void setEndColumn(int c) {
            throw new UnsupportedOperationException("setEndColumn must not be used"); // NOI18N
        }
        
        @Override
        public int getEndLine() {
            throw new UnsupportedOperationException("getEndLine must not be used"); // NOI18N
        }
        
        @Override
        public void setEndLine(int l) {
            throw new UnsupportedOperationException("setEndLine must not be used"); // NOI18N
        }
        
        @Override
        public int getType() {
            return APTTokenTypes.EOF;
        }

        @Override
        public String getText() {
            return "<EOF>"; // NOI18N
        }

        @Override
        public int getColumn() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getLine() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public String toString() {
            return "<EOF>"; // NOI18N
        }        
    }


    private static final class APTEOFTokenAntlr3 extends APTTokenAbstact {
        public APTEOFTokenAntlr3() {
        }

        @Override
        public int getOffset() {
            throw new UnsupportedOperationException("getOffset must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public void setOffset(int o) {
            throw new UnsupportedOperationException("setOffset must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public int getEndOffset() {
            throw new UnsupportedOperationException("getEndOffset must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public void setEndOffset(int o) {
            throw new UnsupportedOperationException("setEndOffset must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public CharSequence getTextID() {
            throw new UnsupportedOperationException("getTextID must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public void setTextID(CharSequence id) {
            throw new UnsupportedOperationException("setTextID must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public int getEndColumn() {
            throw new UnsupportedOperationException("getEndColumn must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public void setEndColumn(int c) {
            throw new UnsupportedOperationException("setEndColumn must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public int getEndLine() {
            throw new UnsupportedOperationException("getEndLine must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public void setEndLine(int l) {
            throw new UnsupportedOperationException("setEndLine must not be used; use APTUtils.isEOF in client"); // NOI18N
        }

        @Override
        public int getType() {
            return EOF3;
        }

        @Override
        public String getText() {
            return "<EOF3>"; // NOI18N
        }

        @Override
        public int getColumn() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getLine() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public String toString() {
            return "<EOF3>"; // NOI18N
        }
    }

}
