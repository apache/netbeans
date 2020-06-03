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
package org.netbeans.modules.cnd.modelimpl.parser.spi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.indexing.api.CndTextIndex;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTTokenStreamProducer;
import org.netbeans.modules.cnd.modelimpl.parser.clank.ClankTokenStreamProducer;
import org.netbeans.modules.cnd.support.Interrupter;
import org.openide.util.Lookup;
import org.netbeans.modules.cnd.apt.support.spi.CndTextIndexFilter;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.impl.services.FileInfoQueryImpl;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public abstract class TokenStreamProducer {
    
    private PreprocHandler curPreprocHandler;
    private PreprocHandler.State curPreprocHandlerStartState;
    private FileImpl startFile;
    private String language = APTLanguageSupport.GNU_CPP;
    private String languageFlavor = APTLanguageSupport.FLAVOR_UNKNOWN;
    private final FileImpl fileImpl;
    private final FileContent fileContent;
    private boolean allowToCacheOnRelease;
    private CodePatch codePatch;
    private final boolean fromEnsureParsed;

    protected TokenStreamProducer(FileImpl fileImpl, FileContent newFileContent, boolean fromEnsureParsed) {
        assert fileImpl != null : "null file is not allowed";        
        assert newFileContent != null : "null file content is not allowed";        
        this.fileImpl = fileImpl;
        this.fileContent = newFileContent;
        this.fromEnsureParsed = fromEnsureParsed;
    }        
    
    public static TokenStreamProducer create(FileImpl file, boolean emptyFileContent, boolean fromEnsureParsed) {
        FileContent newFileContent = FileContent.getHardReferenceBasedCopy(file.getCurrentFileContent(), emptyFileContent);
        if (fromEnsureParsed) {
            indexFileContent(file);
        }
        if (APTTraceFlags.USE_CLANK) {
            return ClankTokenStreamProducer.createImpl(file, newFileContent, fromEnsureParsed);
        } else {
            return APTTokenStreamProducer.createImpl(file, newFileContent, fromEnsureParsed);
        }
    }

    public abstract TokenStream getTokenStreamOfIncludedFile(PreprocHandler.State includeOwnerState, CsmInclude include, Interrupter interrupter);

    public abstract TokenStream getTokenStreamForParsingAndCaching(Interrupter interrupter);

    public abstract TokenStream getTokenStreamForParsing(String language, Interrupter interrupter);

    public abstract TokenStream getTokenStreamForCaching(Interrupter interrupter);

    /** must be called when TS was completely consumed */
    public abstract FilePreprocessorConditionState release();

    public void prepare(PreprocHandler handler, String language, String languageFlavor, boolean allowToCacheOnRelease) {
        assert handler != null : "null preprocHandler is not allowed";
        curPreprocHandler = handler;
        curPreprocHandlerStartState = handler.getState();
        startFile = Utils.getStartFile(handler.getState());
        assert language != null : "null language is not allowed";
        this.language = language;
        assert languageFlavor != null : "null language flavor is not allowed";
        this.languageFlavor = languageFlavor;
        this.allowToCacheOnRelease = allowToCacheOnRelease;
    }
    
    public PreprocHandler getCurrentPreprocHandler() {
        return curPreprocHandler;
    }

    public PreprocHandler.State getPreprocHandlerStartState() {
        return curPreprocHandlerStartState;
    }
        
    public String getLanguage() {
        return language;
    }        

    public String getLanguageFlavor() {
        return languageFlavor;
    }

    public FileImpl getInterestedFile() {
        return fileImpl;
    }    

    protected FileImpl getStartFile() {
        if (startFile != null) {
            return startFile;
        }
        return fileImpl;
    }

    public FileContent getFileContent() {
        assert fileContent != null;
        return fileContent;
    }

    protected final boolean isAllowedToCacheOnRelease() {
        return allowToCacheOnRelease;
    }

    protected final boolean isFromEnsureParsed() {
        return fromEnsureParsed;
    }
    
    protected CodePatch getCodePatch() {
        return codePatch;
    }

    protected void resetHandler(PreprocHandler ppHandler) {
        this.curPreprocHandler = ppHandler;
        this.curPreprocHandlerStartState = ppHandler.getState();
    }

    public void setCodePatch(CodePatch codePatch) {
        this.codePatch = codePatch;
    }

    public static final class CodePatch {
        private final int startOffset;
        private final int endOffset;
        private final String patch;

        public CodePatch(int startOffset, int endOffset, String patch) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.patch = patch;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public String getPatch() {
            return patch;
        }

    }

    private static void indexFileContent(FileImpl file) {
        TokenSequence<?> tsToIndex = createFileTokenSequence(file);
        if (tsToIndex == null) {
            assert !file.isValid() : "must have token stream for valid files";
            return;
        }
        CndTextIndexFilter[] extraIndexFilters = getExtraTextIndexFilters(file);
        assert extraIndexFilters != null;
        Set<CharSequence> ids = new HashSet<>(1024);
        indexFileTokens(tsToIndex, extraIndexFilters, ids);
        CndTextIndex.put(file.getTextIndexKey(), ids);
    }

    private static TokenSequence<?> createFileTokenSequence(FileImpl file) {
        FileBuffer buffer = file.getBuffer();
        if (buffer == null) {
            return null;
        }
        char[] charBuffer;
        try {
            charBuffer = buffer.getCharBuffer();
        } catch (IOException ex) {
            // OK for removed files
            return null;
        }
        Language<TokenId> language;
        Set<TokenId> mergedSkippedTokens;
        if (APTLanguageSupport.FORTRAN.equals(file.getFileLanguage())) {
            language = FortranSkippedTokensPool.LANGUAGE;
            mergedSkippedTokens = FortranSkippedTokensPool.SKIP_TOKENS;
        } else {
            language = CppSkippedTokensPool.LANGUAGE;
            mergedSkippedTokens = CppSkippedTokensPool.SKIP_TOKENS;
        }
        TokenHierarchy<?> hi = TokenHierarchy.create(new CharBufferChars(charBuffer), false, language, mergedSkippedTokens, null);
//        TokenHierarchy<?> hi = TokenHierarchy.create(new String(charBuffer), false, language, mergedSkippedTokens, null);
        return hi.tokenSequence();
    }


    private static final class CppSkippedTokensPool {
        static final Set<TokenId> SKIP_TOKENS;
        static final Language<TokenId> LANGUAGE;
        static {
            LANGUAGE = (Language<TokenId>)(Language<?>)CppTokenId.languageCpp();
            Set<TokenId> skipNumTokens = LANGUAGE.tokenCategoryMembers(CppTokenId.NUMBER_CATEGORY);
            Set<TokenId> skipWSTokens = LANGUAGE.tokenCategoryMembers(CppTokenId.WHITESPACE_CATEGORY);
            Set<TokenId> skipCommentTokens = LANGUAGE.tokenCategoryMembers(CppTokenId.COMMENT_CATEGORY);
            Set<TokenId> skipSeparatorTokens = LANGUAGE.tokenCategoryMembers(CppTokenId.SEPARATOR_CATEGORY);
            Set<TokenId> skipOperatorTokens = LANGUAGE.tokenCategoryMembers(CppTokenId.OPERATOR_CATEGORY);
            SKIP_TOKENS = LANGUAGE.merge(skipNumTokens, 
                        LANGUAGE.merge(skipWSTokens, 
                            LANGUAGE.merge(skipCommentTokens, 
                                    LANGUAGE.merge(skipSeparatorTokens, 
                                            skipOperatorTokens))));
        }
    }
    
    private static final class FortranSkippedTokensPool {
        static final Set<TokenId> SKIP_TOKENS;
        static final Language<TokenId> LANGUAGE;
        static {
            LANGUAGE = (Language<TokenId>)(Language<?>)FortranTokenId.languageFortran();
            Set<TokenId> skipNumTokens = LANGUAGE.tokenCategoryMembers(FortranTokenId.NUMBER_CATEGORY);
            Set<TokenId> skipWSTokens = LANGUAGE.tokenCategoryMembers(FortranTokenId.WHITESPACE_CATEGORY);
            Set<TokenId> skipCommentTokens = LANGUAGE.tokenCategoryMembers(FortranTokenId.COMMENT_CATEGORY);
            Set<TokenId> skipSeparatorTokens = LANGUAGE.tokenCategoryMembers(FortranTokenId.SPECIAL_CATEGORY);
            Set<TokenId> skipOperatorTokens = LANGUAGE.tokenCategoryMembers(FortranTokenId.OPERATOR_CATEGORY);
            Set<TokenId> skipKwdOperatorTokens = LANGUAGE.tokenCategoryMembers(FortranTokenId.KEYWORD_OPERATOR_CATEGORY);
            SKIP_TOKENS = LANGUAGE.merge(skipNumTokens, 
                        LANGUAGE.merge(skipWSTokens, 
                            LANGUAGE.merge(skipCommentTokens, 
                                    LANGUAGE.merge(skipSeparatorTokens, 
                                            LANGUAGE.merge(skipOperatorTokens,
                                                     skipKwdOperatorTokens)))));
        }
    }
    
    private static void indexFileTokens(TokenSequence<?> expTS, CndTextIndexFilter[] indexFilters, Set<CharSequence> ids) {
        if (expTS != null) {
            expTS.moveStart();
            while (expTS.moveNext()) {
                Token<?> expToken = expTS.token();
                // index preprocessor directive tokens as well
                if (expToken.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                    indexFileTokens(expTS.embedded(), indexFilters, ids);
                } else {
                    String primaryCategory = expToken.id().primaryCategory();
                    if (CppTokenId.IDENTIFIER_CATEGORY.equals(primaryCategory) ||
                        CppTokenId.PREPROCESSOR_IDENTIFIER_CATEGORY.equals(primaryCategory) ||
                        CppTokenId.KEYWORD_CATEGORY.equals(primaryCategory)) {
                        ids.add(expToken.text().toString());
                    } if (indexFilters.length > 0 && 
                          CppTokenId.STRING_CATEGORY.equals(primaryCategory)) {
                        for (CndTextIndexFilter filter : indexFilters) {
                            CharSequence indexText = filter.getStringIndexText(expToken.text());
                            if (indexText != null) {
                                ids.add(indexText.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    private static CndTextIndexFilter[] getExtraTextIndexFilters(FileImpl file) {
        Collection<? extends CndTextIndexFilter> extraIndexFilters = Collections.emptyList();
        Object pp = file.getProject().getPlatformProject();
        if (pp instanceof NativeProject) {
            final Lookup.Provider project = ((NativeProject) pp).getProject();
            if (project != null) {
                extraIndexFilters = project.getLookup().lookupAll(CndTextIndexFilter.class);
            }
        }
        // index using CndLexer and index filters
        final CndTextIndexFilter[] indexFilters = new CndTextIndexFilter[extraIndexFilters.size()];
        int i = 0;
        for (CndTextIndexFilter f : extraIndexFilters) {
            indexFilters[i] = f;
            i++;
        }
        return indexFilters;
    }    

    private static final class CharBufferChars implements CharSequence {
        private final char[] buffer;
        private final int firstIndex;
        private final int length;

        public CharBufferChars(char[] charBuffer) {
            this(charBuffer, 0, charBuffer.length);
        }

        private CharBufferChars(char[] charBuffer, int firstInclusiveIndex, int lastExclusiveIndex) {
            assert charBuffer != null;
            this.buffer = charBuffer;
            this.firstIndex = firstInclusiveIndex;
            this.length = lastExclusiveIndex - firstInclusiveIndex;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public char charAt(int index) {
            return buffer[index];
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new CharBufferChars(buffer, start, end);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Arrays.hashCode(this.buffer);
            hash = 97 * hash + this.firstIndex;
            hash = 97 * hash + this.length;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CharBufferChars other = (CharBufferChars) obj;
            if (this.firstIndex != other.firstIndex) {
                return false;
            }
            if (this.length != other.length) {
                return false;
            }
            if (!Arrays.equals(this.buffer, other.buffer)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return new String(this.buffer, this.firstIndex, this.length);
        }
    }

    public static PPIncludeHandler.IncludeInfo createIncludeInfo(CsmInclude include) {
        FileImpl includedFile = (FileImpl) include.getIncludeFile();
        if (includedFile == null) {
            // error recovery
            return null;
        }
        FileSystem fileSystem = includedFile.getFileSystem();
        if (fileSystem == null) {
            // error recovery
            return null;
        }
        CharSequence includedAbsPath = includedFile.getAbsolutePath();
        int includeDirFileIndex = FileInfoQueryImpl.getIncludeDirectiveIndex(include);
        if (includeDirFileIndex < 0) {
            // error recovery
            return null;
        }
        return new IncludeInfoImpl(include, fileSystem, includedAbsPath, includeDirFileIndex);
    }
    
    private static final class IncludeInfoImpl implements PPIncludeHandler.IncludeInfo {

        private final int line;
        private final CsmInclude include;
        private final FileSystem fs;
        private final CharSequence path;
        private final int includedDirectiveIndex;

        private IncludeInfoImpl(CsmInclude include, FileSystem fs, CharSequence path, int includedDirectiveIndex) {
            this.line = include.getStartPosition().getLine();
            this.include = include;
            this.fs = fs;
            this.path = path;
            this.includedDirectiveIndex = includedDirectiveIndex;
        }

        @Override
        public CharSequence getIncludedPath() {
            return path;
        }

        @Override
        public FileSystem getFileSystem() {
            return fs;
        }

        @Override
        public int getIncludeDirectiveLine() {
            return line;
        }

        @Override
        public int getIncludeDirectiveOffset() {
            return include.getStartOffset();
        }

        @Override
        public int getResolvedDirectoryIndex() {
            return 0;
        }

        @Override
        public String toString() {
            return "restore " + include + " #" + includedDirectiveIndex + " from line " + line + " in file " + include.getContainingFile(); // NOI18N
        }

        @Override
        public int getIncludeDirectiveIndex() {
            return includedDirectiveIndex;
        }
    }    
}
