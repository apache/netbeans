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
package org.netbeans.cnd.api.lexer;

import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.spi.lexer.CndLexerLanguageEmbeddingProvider;
import org.netbeans.modules.cnd.utils.CndLanguageStandards.CndLanguageStandard;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class CndLexerUtilities {
    
    public enum FortranFormat {
        FREE,
        FIXED,
        UNDEFINED
    }

    public static final String LEXER_FILTER = "lexer-filter"; // NOI18N
    public static final String FORTRAN_FREE_FORMAT = "fortran-free-format"; // NOI18N
    public static final String FORTRAN_MAXIMUM_TEXT_WIDTH = "fortran-maximum-text-width"; // NOI18N
    public static final String FLAVOR = "language-flavor"; // NOI18N

    private CndLexerUtilities() {
    }

    /**
     * returns C/C++/Preprocessor tokens sequence for component
     * @param component component
     * @param offset offset
     * @param lexPP if <code>true</code> and offset is in preprocessor directive then return tokens sequence of this
     * directive. If <code>false</code> and offset is in preprocessor directive do not dive into embedding
     * @param backwardBias @see TokenHierarchy.embeddedTokenSequences
     * If <code>true</code> the backward lying token will
     *   be used in case that the <code>offset</code> specifies position between
     *   two tokens. If <code>false</code> the forward lying token will be used.     * 
     * @return token sequence positioned on token with offset (no need to call moveNext()/movePrevious() before token())
     */
    public static TokenSequence<TokenId> getCppTokenSequence(final JTextComponent component, final int offset,
            boolean lexPP, boolean backwardBias) {
        Document doc = component.getDocument();
        return getCppTokenSequence(doc, offset, lexPP, backwardBias);
    }

    public static Language<CppTokenId> getLanguage(String mime) {
        if (MIMENames.C_MIME_TYPE.equals(mime)) {
            return CppTokenId.languageC();
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
            return CppTokenId.languageCpp();
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mime)) {
            return CppTokenId.languageHeader();
        }
        return null;
    }

    public static Language<CppTokenId> getLanguage(final Document doc) {
        // try from property
        Language<?> lang = (Language<?>) doc.getProperty(Language.class);
        if (!isCppLanguage(lang, true)) {
            lang = getLanguage((String) doc.getProperty("mimeType")); // NOI18N
        }
        @SuppressWarnings("unchecked")
        Language<CppTokenId> out = (Language<CppTokenId>) lang;
        return out;
    }
    
    /**
     * returns C/C++/Preprocessor tokens sequence for document
     * @param doc document
     * @param offset offset
     * @param lexPP if <code>true</code> and offset is in preprocessor directive then return tokens sequence of this
     * directive. If <code>false</code> and offset is in preprocessor directive do not dive into embedding
     * @param backwardBias @see TokenHierarchy.embeddedTokenSequences
     * If <code>true</code> the backward lying token will
     *   be used in case that the <code>offset</code> specifies position between
     *   two tokens. If <code>false</code> the forward lying token will be used.     * 
     * @return token sequence positioned on token with offset (no need to call moveNext()/movePrevious() before token())
     */
    public static TokenSequence<TokenId> getCppTokenSequence(final Document doc, final int offset,
            boolean lexPP, boolean backwardBias) {
        if (doc == null) {
            return null;
        }
        TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(offset, backwardBias);
        // Go from inner to outer TSes
        for (int i = tsList.size() - 1; i >= 0; i--) {
            TokenSequence<?> ts = tsList.get(i);
            final Language<?> lang = ts.languagePath().innerLanguage();
            if (isCppLanguage(lang, lexPP)) {
                @SuppressWarnings("unchecked")
                TokenSequence<TokenId> cppInnerTS = (TokenSequence<TokenId>) ts;
                return cppInnerTS;
            }
        }
        return null;
    }

    public static TokenSequence<TokenId> getCppTokenSequenceWithoutEmbeddings(final Document doc, final int offset) {
        if (doc == null) {
            return null;
        }
        TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(offset, false);
        if(!tsList.isEmpty()) {
            TokenSequence<?> ts = tsList.get(0);
            final Language<?> lang = ts.languagePath().innerLanguage();
            if (isCppLanguage(lang, false)) {
                @SuppressWarnings("unchecked")
                TokenSequence<TokenId> cppTS = (TokenSequence<TokenId>) ts;
                return cppTS;
            }
        }
        return null;
    }
    
    private final static class CndLexerEmbeddingProviders {
        private final static Collection<? extends CndLexerLanguageEmbeddingProvider> providers = Lookups.forPath(CndLexerLanguageEmbeddingProvider.REGISTRATION_PATH).lookupAll(CndLexerLanguageEmbeddingProvider.class);
    }

    public static boolean isCppLanguage(Language<?> lang, boolean allowPrepoc) {
        if (!CndLexerEmbeddingProviders.providers.isEmpty()) {
            for (org.netbeans.cnd.spi.lexer.CndLexerLanguageEmbeddingProvider provider : CndLexerEmbeddingProviders.providers) {
                if (provider.isKnownLanguage(lang)) {
                    return true;
                }
            }
        }

        return lang == CppTokenId.languageC() || lang == CppTokenId.languageCpp()
                || lang == CppTokenId.languageHeader()
                || (allowPrepoc && lang == CppTokenId.languagePreproc());
    }

    public static TokenSequence<FortranTokenId> getFortranTokenSequence(final Document doc, final int offset) {
        TokenHierarchy<?> th = doc != null ? TokenHierarchy.get(doc) : null;
        TokenSequence<FortranTokenId> ts = th != null ? getFortranTokenSequence(th, offset) : null;
        return ts;
    }

    private static TokenSequence<FortranTokenId> getFortranTokenSequence(final TokenHierarchy<?> hierarchy, final int offset) {
        if (hierarchy != null) {
            TokenSequence<?> ts = hierarchy.tokenSequence();
            while (ts != null && (offset == 0 || ts.moveNext())) {
                ts.move(offset);
                if (ts.language() == FortranTokenId.languageFortran()) {
                    @SuppressWarnings("unchecked")
                    TokenSequence<FortranTokenId> innerTS = (TokenSequence<FortranTokenId>) ts;
                    return innerTS;
                }
                if (!ts.moveNext() && !ts.movePrevious()) {
                    return null;
                }
                ts = ts.embedded();
            }
        }
        return null;
    }

    public static FortranFormat detectFortranFormat(Document doc) {
        CharSequence sequence;
        try {
            sequence = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            return FortranFormat.FIXED;
        }
        return detectFortranFormat(sequence);
    }

    public static FortranFormat detectFortranFormat(CharSequence text) {
        int column = 0;
        boolean ignoreRestLine = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                column = 0;
                ignoreRestLine = false;
                continue;
            }
            if (ignoreRestLine) {
                continue;
            }
            column++;
            switch (column) {
                case 1:
                    if (c == 'C' || c == 'c' || c == '*') {
                        //like to fixed format
                        ignoreRestLine = true;
                        break;
                    } else if (c >= '0' && c <= '9') {
                        //like to fixed format
                        break;
                    } else if (c == ' ') {
                        // undefined format
                        break;
                    } else if (c == '\t') {
                        // undefined format
                        column = 6;
                        break;
                    } else {
                        return FortranFormat.FREE;
                    }
                case 2:
                case 3:
                case 4:
                case 5:
                    if (c >= '0' && c <= '9') {
                        //like to fixed format
                        break;
                    } else if (c == ' ') {
                        // undefined format
                        break;
                    } else if (c == '\t') {
                        // undefined format
                        column = 6;
                        break;
                    } else {
                        return FortranFormat.FREE;
                    }
                default:
                    break;
            }
        }
        return FortranFormat.FIXED;
    }    
    
    public static boolean isCppIdentifier(CharSequence id) {
        if (id == null) {
            return false;
        }

        if (id.length() == 0) {
            return false;
        }

        if (!(isCppIdentifierStart(id.charAt(0)))) {
            return false;
        }

        for (int i = 1; i < id.length(); i++) {
            if (!(isCppIdentifierPart(id.charAt(i)))) {
                return false;
            }
        }
        return getDefatultFilter(true).check(id) == null && getDefatultFilter(false).check(id) == null;
    }

    public static boolean isCppIdentifierStart(int ch) {
        //MS VC also supports $ as start or part of id
//        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z') || (ch == '_') || (ch == '$');
        return Character.isJavaIdentifierStart(ch);
    }

    public static boolean isCppIdentifierPart(int ch) {
        return ('0' <= ch && ch <= '9') || isCppIdentifierStart(ch);
    }

//    public static boolean isCppIdentifierStart(int codePoint) {
//        return Character.isJavaIdentifierStart(codePoint);
//    }
//
//    public static boolean isCppIdentifierPart(int codePoint) {
//        return Character.isJavaIdentifierPart(codePoint);
//    }
//
    public static boolean isFortranIdentifierPart(int codePoint) {
        return Character.isJavaIdentifierPart(codePoint);
    }

    public static CharSequence removeEscapedLF(CharSequence text, boolean escapedLF) {
        if (!escapedLF) {
            return text;
        } else {
            StringBuilder buffer = new StringBuilder();
            int lengthM1 = text.length() - 1;
            for (int i = 0; i <= lengthM1; i++) {
                char c = text.charAt(i);
                boolean append = true;
                if (c == '\\') { // check escaped LF
                    if ((i < lengthM1) && (text.charAt(i + 1) == '\r')) {
                        i++;
                        append = false;
                    }
                    if ((i < lengthM1) && (text.charAt(i + 1) == '\n')) {
                        i++;
                        append = false;
                    }
                }
                if (append) {
                    buffer.append(c);
                }
            }
            return buffer.toString();
        }
    }

    public static boolean isKeyword(String str) {
        try {
            CppTokenId id = CppTokenId.valueOf(str.toUpperCase());
            return id != null
                    && (CppTokenId.KEYWORD_CATEGORY.equals(id.primaryCategory())
                    || CppTokenId.KEYWORD_DIRECTIVE_CATEGORY.equals(id.primaryCategory())
                    || CppTokenId.PREPROCESSOR_KEYWORD_CATEGORY.equals(id.primaryCategory()))
                    || CppTokenId.PREPROCESSOR_KEYWORD_DIRECTIVE_CATEGORY.equals(id.primaryCategory());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean isType(String str) {
        try {
            // replace all spaces
            if (str.contains(" ")) { // NOI18N
                String[] parts = str.split(" "); // NOI18N
                for (String part : parts) {
                    if (isType(part)) {
                        return true;
                    }
                }
            } else {
                CppTokenId id = CppTokenId.valueOf(str.toUpperCase());
                return isType(id);
            }
        } catch (IllegalArgumentException ex) {
            // unknown value
        }
        return false;
    }

    public static boolean isType(CppTokenId id) {
        switch (id) {
            case AUTO:
            case BOOL:
            case CHAR:
            case CONST:
            case DOUBLE:
            case ENUM:
            case EXPORT:
            case FLOAT:
            case INLINE:
            case _INLINE:
            case __INLINE:
            case __INLINE__:
            case INT:
            case __BUILTIN_VA_LIST:
            case LONG:
            case MUTABLE:
            case REGISTER:
            case SHORT:
            case SIGNED:
            case __SIGNED:
            case __SIGNED__:
            case SIZEOF:
            case TYPEDEF:
            case TYPEID:
            case TYPEOF:
            case __TYPEOF:
            case __TYPEOF__:
            case UNSIGNED:
            case __UNSIGNED__:
            case VOID:
            case VOLATILE:
            case WCHAR_T:
            case _BOOL:
            case _COMPLEX:
            case __COMPLEX__:
            case _IMAGINARY:
            case __IMAG__:
            case _INT64:
            case __INT64:
            case __REAL__:
            case __W64:
                return true;
            default:
                return false;
        }
    }

    public static boolean isSeparatorOrOperator(CppTokenId tokenID) {
        String category = tokenID.primaryCategory();
        return CppTokenId.OPERATOR_CATEGORY.equals(category) || CppTokenId.SEPARATOR_CATEGORY.equals(category);
    }
    // filters
    private static Filter<CppTokenId> FILTER_STD_C;
    private static Filter<CppTokenId> FILTER_GCC_C;
    private static Filter<CppTokenId> FILTER_STD_CPP;
    private static Filter<CppTokenId> FILTER_STD_CPP11;
    private static Filter<CppTokenId> FILTER_GCC_CPP;
    private static Filter<CppTokenId> FILTER_GCC_CPP11;
    private static Filter<CppTokenId> FILTER_HEADER_C;
    private static Filter<CppTokenId> FILTER_HEADER_CPP;
    private static Filter<CppTokenId> FILTER_HEADER_CPP11;
    private static Filter<CppTokenId> FILTER_PREPRPOCESSOR;
    private static Filter<CppTokenId> FILTER_OMP;
    private static Filter<FortranTokenId> FILTER_FORTRAN;    
    
    public static Filter<?> getFilter(Language<?> language) {
        return getFilter(language, null);
    }
    public static Filter<?> getFilter(Language<?> language, /*can be null*/CndLanguageStandard preferred) {
        return getFilter(language, preferred, null);
    }
    public static Filter<?> getFilter(Language<?> language, /*can be null*/CndLanguageStandard preferred, Document doc) {
        if (preferred == null) {
            MIMEExtensions me = MIMEExtensions.get(language.mimeType());
            if (me != null) {
                preferred = me.getDefaultStandard();
            }
            if ((preferred == null) && 
                (language == CppTokenId.languageC() || 
                 language == CppTokenId.languageCpp() ||
                 language == CppTokenId.languageHeader())) {
                CndUtils.assertTrueInConsole(false, "Can not detect default standard for " + language); // NOI18N
            }
        }
        if (language == CppTokenId.languageHeader()) {
            if (preferred != null && preferred != CndLanguageStandard.UNKNOWN) {
                switch (preferred) {
                    case C89:
                    case C99:
                    case C11:
                    case C17:
                    case C23:
                        return CndLexerUtilities.getHeaderC11Filter();
                    case CPP98:
                        return CndLexerUtilities.getHeaderCppFilter();
                    case CPP11:
                    case CPP14:
                    case CPP17:
                    case CPP20:
                    case CPP23:
                        return CndLexerUtilities.getHeaderCpp11Filter();
                    default:
                        throw new AssertionError(preferred.name());
                }
            }
            return CndLexerUtilities.getHeaderCpp11Filter();
        } else if (language == CppTokenId.languageCpp()) {
            if (preferred != null && preferred != CndLanguageStandard.UNKNOWN) {
                switch (preferred) {
                    case C89:
                    case C99:
                    case C11:
                    case C17:
                    case C23:
                        CndUtils.assertTrue(false, "Unexpected flavor " + preferred + " for C++ Language"); // NOI18N
                        break;
                    case CPP98:
                        return CndLexerUtilities.getGccCppFilter();
                    case CPP11:
                    case CPP14:
                    case CPP17:
                    case CPP20:
                    case CPP23:
                        return CndLexerUtilities.getGccCpp11Filter();
                    default:
                        throw new AssertionError(preferred.name());
                }
            }
            return CndLexerUtilities.getGccCpp11Filter();
        } else if (language == CppTokenId.languageC()) {
            if (preferred != null && preferred != CndLanguageStandard.UNKNOWN) {
                switch (preferred) {
                    case C89:
                    case C99:
                    case C11:
                    case C17:
                    case C23:
                        return CndLexerUtilities.getGccC11Filter();
                    case CPP98:
                    case CPP11:
                    case CPP14:
                    case CPP17:
                    case CPP20:
                    case CPP23:
                        CndUtils.assertTrue(false, "Unexpected flavor " + preferred + " for C Language"); // NOI18N
                        break;
                    default:
                        throw new AssertionError(preferred.name());
                }
            }
            return CndLexerUtilities.getGccC11Filter();            
        } else if (language == CppTokenId.languagePreproc()) {
            return CndLexerUtilities.getPreprocFilter();
        } else if (language == FortranTokenId.languageFortran()) {
            return CndLexerUtilities.getFortranFilter();
        } else {
            CndUtils.assertTrueInConsole(false, "Unexpected language " + language); // NOI18N
        }
        return null;
    }
    
    public static Filter<?> getDefaultFilter(Language<?> language, Document doc) {
        return getFilter(language, null, doc);
    }

    public static Filter<CppTokenId> getDefatultFilter(boolean cpp) {
        return (Filter<CppTokenId>) (cpp ? 
                getDefaultFilter(CppTokenId.languageCpp(), null) : 
                getDefaultFilter(CppTokenId.languageC(), null));
    }

    public synchronized static Filter<CppTokenId> getPreprocFilter() {
        if (FILTER_PREPRPOCESSOR == null) {
            FILTER_PREPRPOCESSOR = new Filter<CppTokenId>("PreprocFilter"); // NOI18N
            addPreprocKeywords(FILTER_PREPRPOCESSOR);
        }
        return FILTER_PREPRPOCESSOR;
    }

    public synchronized static Filter<CppTokenId> getOmpFilter() {
        if (FILTER_OMP == null) {
            FILTER_OMP = new Filter<CppTokenId>("OmpFilter"); // NOI18N
            addOmpKeywords(FILTER_OMP);
        }
        return FILTER_OMP;
    }

    /*package*/ synchronized static Filter<CppTokenId> getStdCFilter() {
        if (FILTER_STD_C == null) {
            FILTER_STD_C = new Filter<CppTokenId>("StdCFilter"); // NOI18N
            addCommonCCKeywords(FILTER_STD_C);
            addCOnlyKeywords(FILTER_STD_C);
//            addC11OnlyKeywords(FILTER_STD_C);
        }
        return FILTER_STD_C;
    }

    /*package*/ synchronized static Filter<CppTokenId> getGccC11Filter() {
        if (FILTER_GCC_C == null) {
            FILTER_GCC_C = new Filter<CppTokenId>("GccCFilter"); // NOI18N
            addCommonCCKeywords(FILTER_GCC_C);
            addCOnlyKeywords(FILTER_GCC_C);
            addC11OnlyKeywords(FILTER_GCC_C);
            addGccOnlyCommonCCKeywords(FILTER_GCC_C);
            //addGccOnlyCOnlyKeywords(FILTER_GCC_C);
        }
        return FILTER_GCC_C;
    }

    /*package*/ synchronized static Filter<CppTokenId> getStdCppFilter() {
        if (FILTER_STD_CPP == null) {
            FILTER_STD_CPP = new Filter<CppTokenId>("StdCppFilter"); // NOI18N
            addCommonCCKeywords(FILTER_STD_CPP);
            addCppOnlyKeywords(FILTER_STD_CPP);
        }
        return FILTER_STD_CPP;
    }

    /*package*/ synchronized static Filter<CppTokenId> getGccCppFilter() {
        if (FILTER_GCC_CPP == null) {
            FILTER_GCC_CPP = new Filter<CppTokenId>("GccCppFilter"); // NOI18N
            addCommonCCKeywords(FILTER_GCC_CPP);
            addCppOnlyKeywords(FILTER_GCC_CPP);
            addGccOnlyCommonCCKeywords(FILTER_GCC_CPP);
            addGccOnlyCppOnlyKeywords(FILTER_GCC_CPP);
        }
        return FILTER_GCC_CPP;
    }

    /*package*/ synchronized static Filter<CppTokenId> getStdCpp11Filter() {
        if (FILTER_STD_CPP11 == null) {
            FILTER_STD_CPP11 = new Filter<CppTokenId>("StdCpp11Filter"); // NOI18N
            addCommonCCKeywords(FILTER_STD_CPP11);
            addCppOnlyKeywords(FILTER_STD_CPP11);
            addCpp11OnlyKeywords(FILTER_STD_CPP11);
        }
        return FILTER_STD_CPP11;
    }

    /*package*/ synchronized static Filter<CppTokenId> getGccCpp11Filter() {
        if (FILTER_GCC_CPP11 == null) {
            FILTER_GCC_CPP11 = new Filter<CppTokenId>("GccCpp11Filter"); // NOI18N
            addCommonCCKeywords(FILTER_GCC_CPP11);
            addCppOnlyKeywords(FILTER_GCC_CPP11);
            addCpp11OnlyKeywords(FILTER_GCC_CPP11);
            addGccOnlyCommonCCKeywords(FILTER_GCC_CPP11);
            addGccOnlyCppOnlyKeywords(FILTER_GCC_CPP11);
        }
        return FILTER_GCC_CPP11;
    }
    
    /*package*/ synchronized static Filter<CppTokenId> getHeaderC11Filter() {
        if (FILTER_HEADER_C == null) {
            FILTER_HEADER_C = new Filter<CppTokenId>("HeaderCFilter"); // NOI18N
            addCommonCCKeywords(FILTER_HEADER_C);
            addGccOnlyCommonCCKeywords(FILTER_HEADER_C);
            // for header add all C keywords as well
            addCOnlyKeywords(FILTER_HEADER_C);
            addC11OnlyKeywords(FILTER_HEADER_C);
        }
        return FILTER_HEADER_C;
    }    
    
    /*package*/ synchronized static Filter<CppTokenId> getHeaderCppFilter() {
        if (FILTER_HEADER_CPP == null) {
            FILTER_HEADER_CPP = new Filter<CppTokenId>("HeaderCppFilter"); // NOI18N
            addCommonCCKeywords(FILTER_HEADER_CPP);
            addCppOnlyKeywords(FILTER_HEADER_CPP);
            addGccOnlyCommonCCKeywords(FILTER_HEADER_CPP);
            addGccOnlyCppOnlyKeywords(FILTER_HEADER_CPP);
            // for header add all C keywords as well
            addCOnlyKeywords(FILTER_HEADER_CPP);
            addC11OnlyKeywords(FILTER_HEADER_CPP);
        }
        return FILTER_HEADER_CPP;
    }

    /*package*/ synchronized static Filter<CppTokenId> getHeaderCpp11Filter() {
        if (FILTER_HEADER_CPP11 == null) {
            FILTER_HEADER_CPP11 = new Filter<CppTokenId>("HeaderCpp11Filter"); // NOI18N
            addCommonCCKeywords(FILTER_HEADER_CPP11);
            addCppOnlyKeywords(FILTER_HEADER_CPP11);
            addGccOnlyCommonCCKeywords(FILTER_HEADER_CPP11);
            addGccOnlyCppOnlyKeywords(FILTER_HEADER_CPP11);
            // C++11 specific
            addCpp11OnlyKeywords(FILTER_HEADER_CPP11);
            // for header add all C keywords as well
            addCOnlyKeywords(FILTER_HEADER_CPP11);
            addC11OnlyKeywords(FILTER_HEADER_CPP11);            
        }
        return FILTER_HEADER_CPP11;
    }
    
    /*package*/ synchronized static Filter<FortranTokenId> getFortranFilter() {
        if (FILTER_FORTRAN == null) {
            FILTER_FORTRAN = new Filter<FortranTokenId>("FortranFilter"); // NOI18N
            addFortranKeywords(FILTER_FORTRAN);
        }
        return FILTER_FORTRAN;
    }

    ////////////////////////////////////////////////////////////////////////////
    // help methods
    private static void addPreprocKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.PREPROCESSOR_IF,
            CppTokenId.PREPROCESSOR_IFDEF,
            CppTokenId.PREPROCESSOR_IFNDEF,
            CppTokenId.PREPROCESSOR_ELSE,
            CppTokenId.PREPROCESSOR_ELIF,
            CppTokenId.PREPROCESSOR_ENDIF,
            CppTokenId.PREPROCESSOR_DEFINE,
            CppTokenId.PREPROCESSOR_UNDEF,
            CppTokenId.PREPROCESSOR_INCLUDE,
            CppTokenId.PREPROCESSOR_INCLUDE_NEXT,
            CppTokenId.PREPROCESSOR_LINE,
            CppTokenId.PREPROCESSOR_IDENT,
            CppTokenId.PREPROCESSOR_PRAGMA,
            CppTokenId.PREPROCESSOR_WARNING,
            CppTokenId.PREPROCESSOR_ERROR,};
        addToFilter(ids, filterToModify);
    }

    private static void addOmpKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.PRAGMA_OMP_START,
            CppTokenId.PRAGMA_OMP_PARALLEL,
            CppTokenId.PRAGMA_OMP_SECTIONS,
            CppTokenId.PRAGMA_OMP_WORKSHARE,
            CppTokenId.PRAGMA_OMP_NOWAIT,
            CppTokenId.PRAGMA_OMP_ORDERED,
            CppTokenId.PRAGMA_OMP_SCHEDULE,
            CppTokenId.PRAGMA_OMP_DYNAMIC,
            CppTokenId.PRAGMA_OMP_GUIDED,
            CppTokenId.PRAGMA_OMP_RUNTIME,
            CppTokenId.PRAGMA_OMP_SECTION,
            CppTokenId.PRAGMA_OMP_SINGLE,
            CppTokenId.PRAGMA_OMP_MASTER,
            CppTokenId.PRAGMA_OMP_CRITICAL,
            CppTokenId.PRAGMA_OMP_BARRIER,
            CppTokenId.PRAGMA_OMP_ATOMIC,
            CppTokenId.PRAGMA_OMP_SEQ_CST,
            CppTokenId.PRAGMA_OMP_CAPTURE,
            CppTokenId.PRAGMA_OMP_READ,
            CppTokenId.PRAGMA_OMP_WRITE,
            CppTokenId.PRAGMA_OMP_FLUSH,
            CppTokenId.PRAGMA_OMP_THREADPRIVATE,
            CppTokenId.PRAGMA_OMP_PRIVATE,
            CppTokenId.PRAGMA_OMP_FIRSTPRIVATE,
            CppTokenId.PRAGMA_OMP_LASTPRIVATE,
            CppTokenId.PRAGMA_OMP_LINEAR,
            CppTokenId.PRAGMA_OMP_VAL,
            CppTokenId.PRAGMA_OMP_UVAL,
            CppTokenId.PRAGMA_OMP_REF,
            CppTokenId.PRAGMA_OMP_SHARED,
            CppTokenId.PRAGMA_OMP_NONE,
            CppTokenId.PRAGMA_OMP_REDUCTION,
            CppTokenId.PRAGMA_OMP_COPYIN,
            CppTokenId.PRAGMA_OMP_TASK,
            CppTokenId.PRAGMA_OMP_FINAL,
            CppTokenId.PRAGMA_OMP_UNTIED,
            CppTokenId.PRAGMA_OMP_MERGEABLE,
            CppTokenId.PRAGMA_OMP_IN_REDUCTION,
            CppTokenId.PRAGMA_OMP_DEPEND,
            CppTokenId.PRAGMA_OMP_SOURCE,
            CppTokenId.PRAGMA_OMP_SINK,
            CppTokenId.PRAGMA_OMP_IN,
            CppTokenId.PRAGMA_OMP_OUT,
            CppTokenId.PRAGMA_OMP_INOUT,
            CppTokenId.PRAGMA_OMP_PRIORITY,
            CppTokenId.PRAGMA_OMP_TASKWAIT,
            CppTokenId.PRAGMA_OMP_TASKGROUP,
            CppTokenId.PRAGMA_OMP_TASKLOOP,
            CppTokenId.PRAGMA_OMP_GRAINSIZE,
            CppTokenId.PRAGMA_OMP_NUM_TASKS,
            CppTokenId.PRAGMA_OMP_NOGROUP,
            CppTokenId.PRAGMA_OMP_TASKYIELD,
            CppTokenId.PRAGMA_OMP_TARGET,
            CppTokenId.PRAGMA_OMP_DATA,
            CppTokenId.PRAGMA_OMP_DEVICE,
            CppTokenId.PRAGMA_OMP_MAP,
            CppTokenId.PRAGMA_OMP_USE_DEVICE_PTR,
            CppTokenId.PRAGMA_OMP_ENTER,
            CppTokenId.PRAGMA_OMP_EXIT,
            CppTokenId.PRAGMA_OMP_IS_DEVICE_PTR,
            CppTokenId.PRAGMA_OMP_UPDATE,
            CppTokenId.PRAGMA_OMP_TO,
            CppTokenId.PRAGMA_OMP_FROM,
            CppTokenId.PRAGMA_OMP_TOFROM,
            CppTokenId.PRAGMA_OMP_SCALAR,
            CppTokenId.PRAGMA_OMP_ALLOC,
            CppTokenId.PRAGMA_OMP_RELEASE,
            CppTokenId.PRAGMA_OMP_DELETE,
            CppTokenId.PRAGMA_OMP_ALWAYS,
            CppTokenId.PRAGMA_OMP_DEFAULTMAP,
            CppTokenId.PRAGMA_OMP_TEAMS,
            CppTokenId.PRAGMA_OMP_NUM_TEAMS,
            CppTokenId.PRAGMA_OMP_THREAD_LIMIT,
            CppTokenId.PRAGMA_OMP_DISTRIBUTE,
            CppTokenId.PRAGMA_OMP_DIST_SCHEDULE,
            CppTokenId.PRAGMA_OMP_COLLAPSE,
            CppTokenId.PRAGMA_OMP_COPYPRIVATE,
            CppTokenId.PRAGMA_OMP_DEFAULT,
            CppTokenId.PRAGMA_OMP_STATIC,
            CppTokenId.PRAGMA_OMP_IF,
            CppTokenId.PRAGMA_OMP_FOR,
            CppTokenId.PRAGMA_OMP_DO,
            CppTokenId.PRAGMA_OMP_AUTO,
            CppTokenId.PRAGMA_OMP_NUM_THREADS,
            CppTokenId.PRAGMA_OMP_SIMD,
            CppTokenId.PRAGMA_OMP_SAFELEN,
            CppTokenId.PRAGMA_OMP_SIMDLEN,
            CppTokenId.PRAGMA_OMP_ALIGNED,
            CppTokenId.PRAGMA_OMP_DECLARE,
            CppTokenId.PRAGMA_OMP_UNIFORM,
            CppTokenId.PRAGMA_OMP_INBRANCH,
            CppTokenId.PRAGMA_OMP_NOTINBRANCH,
            CppTokenId.PRAGMA_OMP_CANCEL,
            CppTokenId.PRAGMA_OMP_CANCELLATION,
            CppTokenId.PRAGMA_OMP_POINT,
            CppTokenId.PRAGMA_OMP_CONDITIONAL,
            CppTokenId.PRAGMA_OMP_TASK_REDUCTION,
            CppTokenId.PRAGMA_OMP_INITIALIZER,};
        addToFilter(ids, filterToModify);
    }

    private static void addCommonCCKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.AUTO,
            CppTokenId.BREAK,
            CppTokenId.__BUILTIN_VA_LIST,
            CppTokenId.CASE,
            CppTokenId.CHAR,
            CppTokenId.CONST,
            CppTokenId.CONTINUE,
            CppTokenId.DEFAULT,
            CppTokenId.DO,
            CppTokenId.DOUBLE,
            CppTokenId.ELSE,
            CppTokenId.ENUM,
            CppTokenId.EXTERN,
            CppTokenId.FLOAT,
            CppTokenId.FOR,
            CppTokenId.__FUNC__,
            CppTokenId.GOTO,
            CppTokenId.IF,
            CppTokenId.INT,
            CppTokenId.LONG,
            CppTokenId.REGISTER,
            CppTokenId.RETURN,
            CppTokenId.SHORT,
            CppTokenId.SIGNED,
            CppTokenId.SIZEOF,
            CppTokenId.STATIC,
            CppTokenId.STRUCT,
            CppTokenId.SWITCH,
            CppTokenId.TYPEDEF,
            CppTokenId.UNION,
            CppTokenId.UNSIGNED,
            CppTokenId.VOID,
            CppTokenId.VOLATILE,
            CppTokenId.WHILE,};
        addToFilter(ids, filterToModify);
 //       filterToModify.addPrefixedMatch("__builtin_", CppTokenId.BUILT_IN_TYPE);
    }

    private static void addCppOnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.ASM, // gcc and C++
            CppTokenId.BOOL, // C++
            CppTokenId.CATCH, //C++
            CppTokenId.CLASS, //C++
            CppTokenId.CONST_CAST, // C++
            CppTokenId.DELETE, // C++
            CppTokenId.DYNAMIC_CAST, // C++
            CppTokenId.EXPLICIT, // C++
            CppTokenId.EXPORT, // C++
            CppTokenId.FINALLY, //C++
            CppTokenId.FRIEND, // C++
            CppTokenId.INLINE, // gcc, C++, now in C also
            CppTokenId._INLINE,// gcc, C++, now in C also
            CppTokenId.__INLINE,// gcc, C++, now in C also
            CppTokenId.__INLINE__,// gcc, C++, now in C also            
            CppTokenId.MUTABLE, // C++
            CppTokenId.NAMESPACE, //C++
            CppTokenId.NEW, //C++
            CppTokenId.OPERATOR, // C++
            CppTokenId.PRIVATE, //C++
            CppTokenId.PROTECTED, //C++
            CppTokenId.PUBLIC, // C++
            CppTokenId.REINTERPRET_CAST, //C++
            CppTokenId.STATIC_CAST, // C++
            CppTokenId.TEMPLATE, //C++
            CppTokenId.THIS, // C++
            CppTokenId.THROW, //C++
            CppTokenId.TRY, // C++
            CppTokenId.TYPEID, //C++
            CppTokenId.TYPENAME, //C++
            CppTokenId.TYPEOF, // gcc, C++
            CppTokenId.USING, //C++
            CppTokenId.VIRTUAL, //C++
            CppTokenId.WCHAR_T, // C++

            CppTokenId.TRUE, // C++
            CppTokenId.FALSE, // C++

            CppTokenId.ALTERNATE_AND, // C++
            CppTokenId.ALTERNATE_BITOR, // C++
            CppTokenId.ALTERNATE_OR, // C++
            CppTokenId.ALTERNATE_XOR, // C++
            CppTokenId.ALTERNATE_COMPL, // C++
            CppTokenId.ALTERNATE_BITAND, // C++
            CppTokenId.ALTERNATE_AND_EQ, // C++
            CppTokenId.ALTERNATE_OR_EQ, // C++
            CppTokenId.ALTERNATE_XOR_EQ, // C++
            CppTokenId.ALTERNATE_NOT, // C++
            CppTokenId.ALTERNATE_NOT_EQ, // C++
        };
        addToFilter(ids, filterToModify);
    }

    private static void addCpp11OnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.FINAL, // c++11
            CppTokenId.OVERRIDE, // c++11
            CppTokenId.CONSTEXPR, // c++11
            CppTokenId.DECLTYPE, // c++11
            CppTokenId.__DECLTYPE, // c++11
            CppTokenId.NULLPTR, // c++11
            CppTokenId.THREAD_LOCAL, // c++11
            CppTokenId.STATIC_ASSERT, // c++11
            CppTokenId.ALIGNAS, // c++11
            CppTokenId.CHAR16_T, // c++11
            CppTokenId.CHAR32_T, // c++11
            CppTokenId.NOEXCEPT, // c++11
        };
        addToFilter(ids, filterToModify);
    }    
    
    private static void addCOnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.INLINE, // gcc, C++, now in C also
            CppTokenId._INLINE,// gcc, C++, now in C also
            CppTokenId.__INLINE,// gcc, C++, now in C also
            CppTokenId.__INLINE__,// gcc, C++, now in C also
            CppTokenId.RESTRICT, // C
            CppTokenId._BOOL, // C
            CppTokenId._COMPLEX, // C
            CppTokenId._IMAGINARY, // C
        };
        addToFilter(ids, filterToModify);
    }
    
    private static void addC11OnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId._NORETURN, // C11
            CppTokenId._ATOMIC, // C11
            CppTokenId._STATIC_ASSERT, // C11
            CppTokenId._THREAD_LOCAL, // C11
            CppTokenId._ALIGNAS, // C11
            CppTokenId._ALIGNOF, // C11
        };
        addToFilter(ids, filterToModify);
    }

    private static void addGccOnlyCommonCCKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.__ALIGNOF__,
            CppTokenId.ASM,
            CppTokenId._ASM,
            CppTokenId.__ASM,
            CppTokenId.__ASM__,
            CppTokenId.__ATTRIBUTE__,
            CppTokenId.__ATTRIBUTE,
            CppTokenId.__COMPLEX__,
            CppTokenId.__CONST,
            CppTokenId.__CONST__,
            CppTokenId.__IMAG__,
            CppTokenId.INLINE,
            CppTokenId._INLINE,
            CppTokenId.__INLINE,
            CppTokenId.__INLINE__,
            CppTokenId.__FORCEINLINE,            
            CppTokenId.__REAL__,
            CppTokenId.__RESTRICT,
            CppTokenId.__RESTRICT__,
            CppTokenId.__SIGNED,
            CppTokenId.__SIGNED__,
            CppTokenId.TYPEOF,
            CppTokenId.__TYPEOF,
            CppTokenId.__TYPEOF__,
            CppTokenId.__VOLATILE,
            CppTokenId.__VOLATILE__,
            CppTokenId.__THREAD,
            CppTokenId.__GLOBAL,
            CppTokenId.__HIDDEN,
            CppTokenId.__SYMBOLIC,
            CppTokenId.__UNUSED__,};
        addToFilter(ids, filterToModify);
    }

    /*
    private static void addGccOnlyCOnlyKeywords(Filter<CppTokenId> filterToModify) {
    // no C only tokens in gnu c
    }
     */
    private static void addGccOnlyCppOnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.ALIGNOF,
            CppTokenId._ASM,
            CppTokenId._INLINE,
            CppTokenId.PASCAL,
            CppTokenId._PASCAL,
            CppTokenId.__PASCAL,
            CppTokenId.__UNSIGNED__,
            CppTokenId._CDECL,
            CppTokenId.__CDECL,
            CppTokenId.__CLRCALL,
            CppTokenId.__COMPLEX,
            CppTokenId._DECLSPEC,
            CppTokenId.__DECLSPEC,
            CppTokenId.__EXTENSION__,
            CppTokenId._FAR,
            CppTokenId.__FAR,
            CppTokenId.__FINALLY,
            CppTokenId._INT64,
            CppTokenId.__INT64,
            CppTokenId.__INTERRUPT,
            CppTokenId._NEAR,
            CppTokenId.__NEAR,
            CppTokenId._STDCALL,
            CppTokenId.__STDCALL,
            CppTokenId.__TRY,
            CppTokenId.__W64,
            CppTokenId.__NULL,
            CppTokenId.__ALIGNOF,
            CppTokenId.__IS_CLASS,
            CppTokenId.__IS_ENUM,
            CppTokenId.__IS_POD,
            CppTokenId.__IS_BASE_OF,
            CppTokenId.__HAS_TRIVIAL_CONSTRUCTOR,
            CppTokenId.__HAS_NOTHROW_ASSIGN,
            CppTokenId.__HAS_NOTHROW_COPY,
            CppTokenId.__HAS_NOTHROW_CONSTRUCTOR,
            CppTokenId.__HAS_TRIVIAL_ASSIGN,
            CppTokenId.__HAS_TRIVIAL_COPY,
            CppTokenId.__HAS_TRIVIAL_DESTRUCTOR,
            CppTokenId.__HAS_VIRTUAL_DESTRUCTOR,
            CppTokenId.__IS_ABSTRACT,
            CppTokenId.__IS_EMPTY,
            CppTokenId.__IS_LITERAL_TYPE,
            CppTokenId.__IS_POLYMORPHIC,
            CppTokenId.__IS_STANDARD_LAYOUT,
            CppTokenId.__IS_TRIVIAL,
            CppTokenId.__IS_UNION,
            CppTokenId.__UNDERLYING_TYPE   
        };
        addToFilter(ids, filterToModify);
    }

    private static void addFortranKeywords(Filter<FortranTokenId> filterToModify) {
        FortranTokenId[] ids = new FortranTokenId[]{
            // Keywords
            FortranTokenId.KW_ALLOCATABLE,
            FortranTokenId.KW_ALLOCATE,
            FortranTokenId.KW_APOSTROPHE,
            FortranTokenId.KW_ASSIGNMENT,
            FortranTokenId.KW_ASSOCIATE,
            FortranTokenId.KW_ASYNCHRONOUS,
            FortranTokenId.KW_BACKSPACE,
            FortranTokenId.KW_BIND,
            FortranTokenId.KW_BLOCK,
            FortranTokenId.KW_BLOCKDATA,
            FortranTokenId.KW_CALL,
            FortranTokenId.KW_CASE,
            FortranTokenId.KW_CHARACTER,
            FortranTokenId.KW_CLASS,
            FortranTokenId.KW_CLOSE,
            FortranTokenId.KW_COMMON,
            FortranTokenId.KW_COMPLEX,
            FortranTokenId.KW_CONTAINS,
            FortranTokenId.KW_CONTINUE,
            FortranTokenId.KW_CYCLE,
            FortranTokenId.KW_DATA,
            FortranTokenId.KW_DEALLOCATE,
            FortranTokenId.KW_DEFAULT,
            FortranTokenId.KW_DIMENSION,
            FortranTokenId.KW_DO,
            FortranTokenId.KW_DOUBLE,
            FortranTokenId.KW_DOUBLEPRECISION,
            FortranTokenId.KW_ELEMENTAL,
            FortranTokenId.KW_ELSE,
            FortranTokenId.KW_ELSEIF,
            FortranTokenId.KW_ELSEWHERE,
            FortranTokenId.KW_END,
            FortranTokenId.KW_ENDASSOCIATE,
            FortranTokenId.KW_ENDBLOCK,
            FortranTokenId.KW_ENDBLOCKDATA,
            FortranTokenId.KW_ENDDO,
            FortranTokenId.KW_ENDENUM,
            FortranTokenId.KW_ENDFILE,
            FortranTokenId.KW_ENDFORALL,
            FortranTokenId.KW_ENDFUNCTION,
            FortranTokenId.KW_ENDIF,
            FortranTokenId.KW_ENDINTERFACE,
            FortranTokenId.KW_ENDMAP,
            FortranTokenId.KW_ENDMODULE,
            FortranTokenId.KW_ENDPROGRAM,
            FortranTokenId.KW_ENDSELECT,
            FortranTokenId.KW_ENDSTRUCTURE,
            FortranTokenId.KW_ENDSUBROUTINE,
            FortranTokenId.KW_ENDTYPE,
            FortranTokenId.KW_ENDUNION,
            FortranTokenId.KW_ENDWHERE,
            FortranTokenId.KW_ENDWHILE,
            FortranTokenId.KW_ENTRY,
            FortranTokenId.KW_ENUM,
            FortranTokenId.KW_ENUMERATOR,
            FortranTokenId.KW_EQUIVALENCE,
            FortranTokenId.KW_EXIT,
            FortranTokenId.KW_EXTERNAL,
            FortranTokenId.KW_FLUSH,
            FortranTokenId.KW_FORALL,
            FortranTokenId.KW_FORMAT,
            FortranTokenId.KW_FUNCTION,
            FortranTokenId.KW_GO,
            FortranTokenId.KW_GOTO,
            FortranTokenId.KW_IF,
            FortranTokenId.KW_IMPLICIT,
            FortranTokenId.KW_IN,
            FortranTokenId.KW_INCLUDE,
            FortranTokenId.KW_INOUT,
            FortranTokenId.KW_INQUIRE,
            FortranTokenId.KW_INTEGER,
            FortranTokenId.KW_INTENT,
            FortranTokenId.KW_INTERFACE,
            FortranTokenId.KW_INTRINSIC,
            FortranTokenId.KW_KIND,
            FortranTokenId.KW_LEN,
            FortranTokenId.KW_LOGICAL,
            FortranTokenId.KW_MAP,
            FortranTokenId.KW_MODULE,
            FortranTokenId.KW_NAMELIST,
            FortranTokenId.KW_NONE,
            FortranTokenId.KW_NULLIFY,
            FortranTokenId.KW_ONLY,
            FortranTokenId.KW_OPEN,
            FortranTokenId.KW_OPERATOR,
            FortranTokenId.KW_OPTIONAL,
            FortranTokenId.KW_OUT,
            FortranTokenId.KW_PARAMETER,
            FortranTokenId.KW_POINTER,
            FortranTokenId.KW_PRECISION,
            FortranTokenId.KW_PRINT,
            FortranTokenId.KW_PRIVATE,
            FortranTokenId.KW_PROCEDURE,
            FortranTokenId.KW_PROGRAM,
            FortranTokenId.KW_PROTECTED,
            FortranTokenId.KW_PUBLIC,
            FortranTokenId.KW_PURE,
            FortranTokenId.KW_QUOTE,
            FortranTokenId.KW_READ,
            FortranTokenId.KW_REAL,
            FortranTokenId.KW_RECURSIVE,
            FortranTokenId.KW_RESULT,
            FortranTokenId.KW_RETURN,
            FortranTokenId.KW_REWIND,
            FortranTokenId.KW_SAVE,
            FortranTokenId.KW_SELECT,
            FortranTokenId.KW_SELECTCASE,
            FortranTokenId.KW_SELECTTYPE,
            FortranTokenId.KW_SEQUENCE,
            FortranTokenId.KW_STAT,
            FortranTokenId.KW_STOP,
            FortranTokenId.KW_STRUCTURE,
            FortranTokenId.KW_SUBROUTINE,
            FortranTokenId.KW_TARGET,
            FortranTokenId.KW_THEN,
            FortranTokenId.KW_TO,
            FortranTokenId.KW_TYPE,
            FortranTokenId.KW_UNION,
            FortranTokenId.KW_USE,
            FortranTokenId.KW_VALUE,
            FortranTokenId.KW_VOLATILE,
            FortranTokenId.KW_WAIT,
            FortranTokenId.KW_WHERE,
            FortranTokenId.KW_WHILE,
            FortranTokenId.KW_WRITE,
            // Keyword C Extensions
            FortranTokenId.KW_INT,
            FortranTokenId.KW_SHORT,
            FortranTokenId.KW_LONG,
            FortranTokenId.KW_SIGNED,
            FortranTokenId.KW_UNSIGNED,
            FortranTokenId.KW_SIZE_T,
            FortranTokenId.KW_INT8_T,
            FortranTokenId.KW_INT16_T,
            FortranTokenId.KW_INT32_T,
            FortranTokenId.KW_INT64_T,
            FortranTokenId.KW_INT_LEAST8_T,
            FortranTokenId.KW_INT_LEAST16_T,
            FortranTokenId.KW_INT_LEAST32_T,
            FortranTokenId.KW_INT_LEAST64_T,
            FortranTokenId.KW_INT_FAST8_T,
            FortranTokenId.KW_INT_FAST16_T,
            FortranTokenId.KW_INT_FAST32_T,
            FortranTokenId.KW_INT_FAST64_T,
            FortranTokenId.KW_INTMAX_T,
            FortranTokenId.KW_INTPTR_T,
            FortranTokenId.KW_FLOAT,
            FortranTokenId.KW__COMPLEX,
            FortranTokenId.KW__BOOL,
            FortranTokenId.KW_CHAR,
            FortranTokenId.KW_BOOL,
            // Keyword Operator
            FortranTokenId.KWOP_EQ,
            FortranTokenId.KWOP_NE,
            FortranTokenId.KWOP_LT,
            FortranTokenId.KWOP_LE,
            FortranTokenId.KWOP_GT,
            FortranTokenId.KWOP_GE,
            FortranTokenId.KWOP_AND,
            FortranTokenId.KWOP_OR,
            FortranTokenId.KWOP_NOT,
            FortranTokenId.KWOP_EQV,
            FortranTokenId.KWOP_NEQV,
            FortranTokenId.KWOP_TRUE,
            FortranTokenId.KWOP_FALSE
        };
        addToFilter(ids, filterToModify);
    }

    private static void addToFilter(CppTokenId[] ids, Filter<CppTokenId> filterToModify) {
        for (CppTokenId id : ids) {
            assert id.fixedText() != null : "id " + id + " must have fixed text";
            filterToModify.addMatch(id.fixedText(), id);
        }
    }

    private static void addToFilter(FortranTokenId[] ids, Filter<FortranTokenId> filterToModify) {
        for (FortranTokenId id : ids) {
            assert id.fixedText() != null : "id " + id + " must have fixed text";
            filterToModify.addMatch(id.fixedText(), id);
        }
    }
}
