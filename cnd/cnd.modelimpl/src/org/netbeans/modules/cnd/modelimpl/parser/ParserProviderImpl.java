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

package org.netbeans.modules.cnd.modelimpl.parser;

import java.util.List;
import java.util.Map;
import org.antlr.runtime.tree.CommonTree;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.LazyStatementImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.fsm.core.DataRenderer;
import org.netbeans.modules.cnd.modelimpl.parser.generated.FortranParser;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=CsmParserProvider.class, position=1000)
public final class ParserProviderImpl extends CsmParserProvider {

    @Override
    protected CsmParser create(CsmParserParameters params) {
        CsmFile file = params.getMainFile();
        if (file instanceof FileImpl) {
            if (file.getFileType() == CsmFile.FileType.SOURCE_FORTRAN_FILE) {
                return new Antrl3FortranParser(params);
            }
            if(!TraceFlags.CPP_PARSER_NEW_GRAMMAR) {
                return new Antlr2CppParser(params);
            } else {
                return new Antlr3CXXParser(params);
            }
        } else {
            return null;
        }
    }

    public static int adjustAntlr2ParserFlagsForLanguage(int flags, String lang, String langFlavour) {
        if (APTLanguageSupport.GNU_CPP.equals(lang)) {
            flags |= CPPParserEx.CPP_CPLUSPLUS;
        } else {
            flags |= CPPParserEx.CPP_ANSI_C;
        }
        if (APTLanguageSupport.FLAVOR_CPP11.equals(langFlavour)) {
            flags |= CPPParserEx.CPP_FLAVOR_CPP11;
        } else if (APTLanguageSupport.FLAVOR_CPP14.equals(langFlavour)) {
            flags |= CPPParserEx.CPP_FLAVOR_CPP14;
        } else if (APTLanguageSupport.FLAVOR_CPP17.equals(langFlavour)) {
            flags |= CPPParserEx.CPP_FLAVOR_CPP17;
        }
        return flags;
    }
    
    private final static class Antlr2CppParser implements CsmParserProvider.CsmParser, CsmParserProvider.CsmParserResult {
        private final FileImpl file;
        private CPPParserEx parser;
        private final int flags;
        private CsmObject parserContainer;
        private AST ast;
        private ConstructionKind kind;
        private long initTime;
        private long parseTime;
        private long renderTime;
        private int numTokens;
        private final String language;
        private final String languageFlavor;
//        private

        private Map<Integer, CsmObject> objects = null;
        private final CsmParserProvider.CsmParserParameters params;
        private final CsmCorePackageAccessor csmCorePackageAccessor;

        Antlr2CppParser(CsmParserProvider.CsmParserParameters params) {
            this.params = params;
            this.file = (FileImpl) params.getMainFile();
            this.language = params.getLanguage();
            this.languageFlavor = params.getLanguageFlavor();
            int aFlags = TraceFlags.REPORT_PARSING_ERRORS ? 0 : CPPParserEx.CPP_SUPPRESS_ERRORS;
            aFlags = ParserProviderImpl.adjustAntlr2ParserFlagsForLanguage(aFlags, language, languageFlavor);
            this.flags = aFlags;
            csmCorePackageAccessor = CsmCorePackageAccessor.get();
        }

        @Override
        public void init(CsmObject object, TokenStream ts, CsmParseCallback callback) {
            long start = System.currentTimeMillis();
            assert parser == null : "parser can not be reused " + parser;
            assert object != null;
            assert ts != null;
            parserContainer = object;
            CppParserActionEx cppCallback = (CppParserActionEx)callback;
            if (cppCallback == null) {
                if(TraceFlags.CPP_PARSER_ACTION && ((FileImpl)params.getMainFile()).getParsingFileContent() != null) {
                    if (TraceFlags.TRACE_CPP_PARSER_ACTION) {
                        cppCallback = new CppParserActionTracer(params, null);
                    } else {
                        cppCallback = new CppParserActionImpl(params, null);
                    }
                } else {
                    cppCallback = new CppParserEmptyActionImpl(file);
                }
            }
            if (cppCallback instanceof CppParserActionImpl) {
                objects = ((CppParserActionImpl)cppCallback).getObjectsMap();
            }
            parser = CPPParserEx.getInstance(file, ts, flags, cppCallback);
            numTokens = parser.getTokenCount();
            initTime = System.currentTimeMillis() - start;
        }

        @Override
        public CsmParserProvider.CsmParserResult parse(ConstructionKind kind) {
            long start = System.currentTimeMillis();
            if (TraceFlags.PARSE_ENABLED) {
                try {
                    this.kind = kind;
                    switch (kind) {
                        case TRANSLATION_UNIT_WITH_COMPOUND:
                            parser.setLazyCompound(false);
                            parser.translation_unit();
                            break;
                        case TRANSLATION_UNIT:
                            parser.translation_unit();
                            break;
                        case TRY_BLOCK:
                            parser.setLazyCompound(false);
                            parser.function_try_block(CsmKindUtilities.isConstructor((((CsmScopeElement)parserContainer).getScope())));
                            break;
                        case COMPOUND_STATEMENT:
                            parser.setLazyCompound(false);
                            parser.compound_statement();
                            break;
                        case INITIALIZER:
                            parser.initializer();
                            break;
                        case NAMESPACE_DEFINITION_BODY:
                            parser.translation_unit();
                            break;
                        case ENUM_BODY:
                            parser.fix_fake_enum_members();
                            break;
                        case CLASS_BODY:
                            parser.fix_fake_class_members();
                            break;
                        default:
                            assert false: "unexpected parse kind " + kind;
                    }
                } catch (Throwable ex) {
                    System.err.println(ex.getClass().getName() + " at parsing file " + file.getAbsolutePath()); // NOI18N
                    CndUtils.printStackTraceOnce(ex);
                }
                ast = parser.getAST();
            }
            parseTime = System.currentTimeMillis() - start;
            return this;
        }

        @Override
        public void render(Object... context) {
            if (!TraceFlags.PARSE_ENABLED) {
                return;
            }
            try {
                CsmCacheManager.enter();
                long start = System.currentTimeMillis();
                boolean incFileParseCounter = true;
                switch (kind) {
                    case TRY_BLOCK:
                    case COMPOUND_STATEMENT: {
                        @SuppressWarnings("unchecked")
                        List<CsmStatement> list = (List<CsmStatement>) context[0];
                        ((LazyStatementImpl)parserContainer).renderStatements(ast, list, objects);
                        incFileParseCounter = false;
                        break;
                    }
                    case INITIALIZER: {
                        List<CsmStatement> list = (List<CsmStatement>) context[0];
                        ((ExpressionBase)parserContainer).renderStatements(ast, list, objects);
                        incFileParseCounter = false;
                        break;
                    }
                    case TRANSLATION_UNIT_WITH_COMPOUND:
                    case TRANSLATION_UNIT:
                        if (ast != null) {
                            CsmParserProvider.CsmParserParameters descr = (CsmParserProvider.CsmParserParameters) context[0];
                            FileContent parseFileContent = getCsmCorePackageAccessor().getFileContent(descr);
                            new AstRenderer(file, parseFileContent, language, languageFlavor, objects).render(ast);
                        }
                        break;
                    case NAMESPACE_DEFINITION_BODY:
                    {
                        FileContent fileContent = (FileContent) context[0];
                        FileImpl nsBodyFile = fileContent.getFile();
                        NamespaceDefinitionImpl nsDef = (NamespaceDefinitionImpl) context[1];
                        CsmNamespace ns = nsDef.getNamespace();
                        if (ast != null && ns instanceof NamespaceImpl) {
                            new AstRenderer(nsBodyFile, fileContent, language, languageFlavor, objects).render(ast, (NamespaceImpl) ns, nsDef);
                        }
                        RepositoryUtils.put(ns);
                        break;
                    }
                    case CLASS_BODY:
                    {
                        FileContent fileContent = (FileContent) context[0];
                        ClassImpl cls = (ClassImpl) context[1];
                        CsmVisibility visibility = (CsmVisibility) context[2];
                        boolean localClass = (Boolean) context[3];
                        cls.fixFakeRender(language, languageFlavor, fileContent, visibility, ast, localClass);
                        if (!localClass) {
                            RepositoryUtils.put(cls);
                        }
                        break;
                    }
                    case ENUM_BODY:
                    {
                        FileContent fileContent = (FileContent) context[0];
                        EnumImpl enumImpl = (EnumImpl) context[1];
                        boolean localEnum = (Boolean) context[2];
                        enumImpl.fixFakeRender(fileContent, ast, localEnum);
                        if (!localEnum) {
                            RepositoryUtils.put(enumImpl);
                        }
                        break;
                    }
                    default:
                        assert false : "unexpected parse kind " + kind;
                }
                if (incFileParseCounter) {
                    file.incParseCount();
                }
                renderTime = System.currentTimeMillis() - start;
                dumpParseStatistics();
            } finally {
                CsmCacheManager.leave();
            }
        }

        private CsmCorePackageAccessor getCsmCorePackageAccessor() {
            return csmCorePackageAccessor;
        }

        @Override
        public boolean isEmptyAST() {
            return AstUtil.isEmpty(ast, true);
        }

        @Override
        public void dumpAST() {
            System.err.println("\n");
            System.err.print("AST: ");
            System.err.print(file.getAbsolutePath());
            System.err.print(' ');
            AstUtil.toStream(ast, System.err);
            System.err.println("\n");
        }

        private void dumpParseStatistics() {
            if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT) {
                System.err.printf(" [ Parsing %s] %d Tokens (took %d ms), Parse=%d ms, Render=%d ms (Lang=%s, Flavor=%s)%n", file.getAbsolutePath(), numTokens, initTime, parseTime, renderTime, language, languageFlavor);
            }
        }

        @Override
        public Object getAST() {
            return ast;
        }

        @Override
        public int getErrorCount() {
            return parser.getErrorCount();
        }

        @Override
        public void setErrorDelegate(ParserErrorDelegate delegate) {
        }
    }

    private final static class Antrl3FortranParser implements CsmParserProvider.CsmParser, CsmParserProvider.CsmParserResult {
        private final FileImpl file;
        private FortranParserEx parser;
        private CsmObject parserContainer;
        private FortranParser.program_return ret;
        private ConstructionKind kind;
        private final CsmParserProvider.CsmParserParameters params;
        private long initTime;
        private long parseTime;
        private long renderTime;

        Antrl3FortranParser(CsmParserProvider.CsmParserParameters params) {
            this.file = (FileImpl) params.getMainFile();
            this.params = params;
        }

        @Override
        public void init(CsmObject object, TokenStream ts, CsmParseCallback callback) {
            long start = System.currentTimeMillis();
            int form = APTLanguageSupport.FLAVOR_FORTRAN_FIXED.equals(file.getFileLanguageFlavor()) ?
                    FortranParserEx.FIXED_FORM :
                    FortranParserEx.FREE_FORM;

            parser = new FortranParserEx(ts, form);
            initTime = System.currentTimeMillis() - start;
        }

        @Override
        public CsmParserResult parse(ConstructionKind kind) {
            long start = System.currentTimeMillis();
            try {
                this.kind = kind;
                switch (kind) {
                    case TRANSLATION_UNIT_WITH_COMPOUND:
                    case TRANSLATION_UNIT:
                            ret = parser.program();
                        break;
                    default:
                        assert false : "unexpected parse kind " + kind;
                }
            } catch (Exception ex) {
                System.err.println(ex.getClass().getName() + " at parsing file " + file.getAbsolutePath()); // NOI18N
                CndUtils.printStackTraceOnce(ex);
            }
            parseTime = System.currentTimeMillis() - start;
            return this;
        }

        @Override
        public void render(Object... context) {
            long start = System.currentTimeMillis();
            switch (kind) {
                case TRANSLATION_UNIT_WITH_COMPOUND:
                case TRANSLATION_UNIT:
                    new DataRenderer((CsmParserProvider.CsmParserParameters)context[0]).render(parser.parsedObjects);
                    break;
                default:
                    assert false : "unexpected render kind " + kind;
            }
            file.incParseCount();
            renderTime = System.currentTimeMillis() - start;
            dumpParseStatistics();
        }

        @Override
        public boolean isEmptyAST() {
            return ret == null || ret.getTree() == null;
        }

        @Override
        public Object getAST() {
            return ret == null ? null : ret.getTree();
        }

        @Override
        public int getErrorCount() {
            return parser.getNumberOfSyntaxErrors();
        }

        @Override
        public void dumpAST() {
            CommonTree tree = (CommonTree) ret.getTree();
            System.err.println(tree);
            System.err.println(tree.getChildren());
        }

        private void dumpParseStatistics() {
            if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT) {
                System.err.printf(" [ Parsing %s] %d Fortran Tokens (took %d ms), Parse=%d ms, Render=%d ms (Lang=(%s), Flavor=%s)%n", file.getAbsolutePath(), -1, initTime, parseTime, renderTime, file.getFileLanguage(), file.getFileLanguageFlavor());
            }
        }

        @Override
        public void setErrorDelegate(ParserErrorDelegate delegate) {
        }
    }

    static Token convertToken(org.antlr.runtime.Token token) {
        //assert token == null || token instanceof Antlr3CXXParser.MyToken;
        return (token instanceof Antlr3CXXParser.Antlr2ToAntlr3TokenAdapter) ? ((Antlr3CXXParser.Antlr2ToAntlr3TokenAdapter) token).t : null;
    }

    final static class Antlr3CXXParser implements CsmParserProvider.CsmParser, CsmParserProvider.CsmParserResult {
        private final FileImpl file;
        private final CsmParserProvider.CsmParserParameters params;
        private CXXParserEx parser;

        private ConstructionKind kind;

        private Map<Integer, CsmObject> objects = null;
        private long initTime;
        private long parseTime;
        private int numTokens;

        Antlr3CXXParser(CsmParserProvider.CsmParserParameters params) {
            this.params = params;
            this.file = (FileImpl) params.getMainFile();
        }

        @Override
        public void init(CsmObject object, TokenStream ts, CsmParseCallback callback) {
            long start = System.currentTimeMillis();
            assert parser == null : "parser can not be reused " + parser;
            assert ts != null;
            CXXParserActionEx cppCallback = (CXXParserActionEx)callback;
            if (cppCallback == null) {
                cppCallback = new CXXParserActionImpl(params);
            }
            if (cppCallback instanceof CXXParserActionImpl) {
                objects = ((CXXParserActionImpl) cppCallback).getObjectsMap();
            }
            org.netbeans.modules.cnd.antlr.TokenBuffer tb = new org.netbeans.modules.cnd.antlr.TokenBuffer(ts);
            Antrl2ToAntlr3TokenStreamAdapter tokens;
            if (TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                tokens = new PPTokensBasedTokenStream(tb, cppCallback);
            } else {
                tokens = new Antrl2ToAntlr3TokenStreamAdapter(tb);
            }
            parser = new CXXParserEx(tokens, cppCallback);
            ((CXXParserActionImpl)cppCallback).setParser(parser);
            tokens.setParser(parser);
            this.numTokens = tb.size();
            initTime = System.currentTimeMillis() - start;
        }

        @Override
        public CsmParserProvider.CsmParserResult parse(ConstructionKind kind) {
            long start = System.currentTimeMillis();
            CsmCacheManager.enter();
            try {
                this.kind = kind;
                switch (kind) {
                    case TRANSLATION_UNIT_WITH_COMPOUND:
                    case TRANSLATION_UNIT:
                        parser.compilation_unit();
                        break;
                    case COMPOUND_STATEMENT:
                        parser.compound_statement(false);
                        break;
                    case FUNCTION_DEFINITION_AFTER_DECLARATOR:
                        parser.function_definition_after_declarator(false, true, true);
                        break;
                }
            } catch (Throwable ex) {
                System.err.println(ex.getClass().getName() + " at parsing file " + file.getAbsolutePath()); // NOI18N
                CndUtils.printStackTraceOnce(ex);
            } finally {
                CsmCacheManager.leave();
            }
            parseTime = System.currentTimeMillis() - start;
            return this;
        }

        @Override
        public void render(Object... context) {
            dumpParseStatistics();
        }

        @Override
        public boolean isEmptyAST() {
            return true;
        }

        @Override
        public void dumpAST() {
        }

        private void dumpParseStatistics() {
            if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT) {
                System.err.printf(" [ Parsing %s] %d Tokens (took %d ms), Parse=%d ms, No Rendering%n", file.getAbsolutePath(), numTokens, initTime, parseTime);
            }
        }

        @Override
        public Object getAST() {
            return null;
        }

        @Override
        public int getErrorCount() {
            return parser.getNumberOfSyntaxErrors();
        }

        @Override
        public void setErrorDelegate(ParserErrorDelegate delegate) {
            parser.setErrorDelegate(delegate);
        }

        private static final class Antlr2ToAntlr3TokenAdapter implements org.antlr.runtime.Token {

            org.netbeans.modules.cnd.antlr.Token t;
            org.antlr.runtime.CharStream s = null;

            public Antlr2ToAntlr3TokenAdapter(org.netbeans.modules.cnd.antlr.Token antlr2Token) {
                this.t = (antlr2Token.getType() == APTTokenTypes.EOF) ? APTUtils.EOF_TOKEN2 : antlr2Token;
            }

            @Override
            public String getText() {
                return t.getText();
            }

            @Override
            public void setText(String arg0) {
                t.setText(arg0);
            }

            @Override
            public int getType() {
                return t.getType();
            }

            @Override
            public void setType(int arg0) {
                t.setType(arg0);
            }

            @Override
            public int getLine() {
                return t.getLine();
            }

            @Override
            public void setLine(int arg0) {
                t.setLine(arg0);
            }

            @Override
            public int getCharPositionInLine() {
                return t.getColumn();
            }

            @Override
            public void setCharPositionInLine(int arg0) {
                t.setColumn(arg0);
            }

            @Override
            public int getChannel() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public void setChannel(int arg0) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public int getTokenIndex() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public void setTokenIndex(int arg0) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public org.antlr.runtime.CharStream getInputStream() {
                return s;
            }

            @Override
            public void setInputStream(org.antlr.runtime.CharStream arg0) {
                s = arg0;
            }

            @Override
            public String toString() {
                return t.toString();
            }
        }


        static private class Antrl2ToAntlr3TokenStreamAdapter implements org.antlr.runtime.TokenStream {
            org.netbeans.modules.cnd.antlr.TokenBuffer tb;

            int lastMark;
            protected CXXParserEx parser;

            public Antrl2ToAntlr3TokenStreamAdapter(org.netbeans.modules.cnd.antlr.TokenBuffer antrl2TokenBuffer) {
                this.tb = antrl2TokenBuffer;
            }

            @Override
            public Antlr2ToAntlr3TokenAdapter LT(int arg0) {
                return new Antlr2ToAntlr3TokenAdapter(tb.LT(arg0));
            }

            @Override
            public void consume() {
                tb.consume();
            }

            @Override
            public int LA(int arg0) {
                int LA = tb.LA(arg0);
                return LA == APTTokenTypes.EOF ? CXXParserEx.EOF : LA;
            }

            @Override
            public int mark() {
                lastMark = tb.index();
                return tb.mark();
            }

            @Override
            public int index() {
                return tb.index();
            }

            @Override
            public void rewind(int arg0) {
                tb.rewind(arg0);
            }

            @Override
            public void rewind() {
                tb.mark();
                tb.rewind(lastMark);
            }

            @Override
            public void seek(int arg0) {
                tb.seek(arg0);
            }

            @Override
            public org.antlr.runtime.Token get(int arg0) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public org.antlr.runtime.TokenSource getTokenSource() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public String toString(int arg0, int arg1) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public String toString(org.antlr.runtime.Token arg0, org.antlr.runtime.Token arg1) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public void release(int arg0) {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public String getSourceName() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            @Override
            public int range() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

            public void setParser(CXXParserEx parser) {
                this.parser = parser;
            }

            protected final boolean canUseCallback() {
                return parser == null ? true : parser.backtrackingLevel() == 0;
            }
        }

        private static class PPTokensBasedTokenStream extends Antrl2ToAntlr3TokenStreamAdapter {
            private final CXXParserActionEx cppCallback;

            public PPTokensBasedTokenStream(org.netbeans.modules.cnd.antlr.TokenBuffer tb, CXXParserActionEx cppCallback) {
                super(tb);
                this.cppCallback = cppCallback;
                this.lastConsumed = new Antlr2ToAntlr3TokenAdapter(APTUtils.EOF_TOKEN);
            }

            private static final boolean TRACE = false;

            @Override
            public int LA(int i) {
                if (i == 0) {
                    if (TRACE) System.err.println("LA(0)=" + lastConsumed + " C="+canUseCallback());
                    assert lastConsumed != null;
                    //assert canUseCallback();
                    return lastConsumed.getType();
                }
                final int newIndex = skipIncludeTokensIfNeeded(i);
                int LA = super.LA(newIndex);
                assert !isIncludeToken(LA) : super.LT(newIndex) + " not expected";
                return LA;
            }

            @Override
            public Antlr2ToAntlr3TokenAdapter LT(int i) {
                if (i == 0) {
                    if (TRACE) System.err.println("LT(0)=" + lastConsumed + " C="+canUseCallback());
                    assert lastConsumed != null;
                    //assert canUseCallback();
                    assert !isIncludeToken(lastConsumed.getType()) : lastConsumed + " not expected ";
                    return lastConsumed;
                }
                Antlr2ToAntlr3TokenAdapter LT = super.LT(skipIncludeTokensIfNeeded(i));
                assert !isIncludeToken(LT.getType()) : LT + " not expected ";
                return LT;
            }

            private Antlr2ToAntlr3TokenAdapter lastConsumed;
            @Override
            public void consume() {
                if (TRACE) System.err.println("consuming LT(1)=" + super.LT(1) + " LT(0)=" + super.LT(0) + "; lastConsumed=" + lastConsumed + " C="+canUseCallback());
                assert !isIncludeToken(super.LA(1)) : super.LT(1) + " not expected ";
                if (canUseCallback()) {
                    lastConsumed = super.LT(1);
                }
                super.consume();
                if (TRACE) System.err.println("after consume LT(1)=" + super.LT(1) + "; consumed LT(0)=" + super.LT(0) + "; lastConsumed=" + lastConsumed + " C="+canUseCallback());
                if (false) { // DISABLED: let LA to be the only one consumer
                    // consume following includes as well
                    while (isIncludeToken(super.LA(1))) {
                        if (canUseCallback()) {
                            org.antlr.runtime.Token t = super.LT(1);
                            onIncludeToken(t);
                            if (TRACE) System.err.println("extra consuming LT(1)=" + t + " LT(0)=" + super.LT(0));
                            super.consume();
                            if (TRACE) System.err.println("after extra consume LT(1)=" + super.LT(1) + "; consumed LT(0)=" + super.LT(0));
                        } else {
                            if (TRACE) System.err.println("skipping LT(1)=" + super.LT(1) + " LT(0)=" + super.LT(0));
                            super.consume();
                            if (TRACE) System.err.println("after skipping LT(1)=" + super.LT(1) + "; skipped LT(0)=" + super.LT(0));
                        }
                    }
                }
            }

            private int skipIncludeTokensIfNeeded(int i) {
                if (i == 0) {
                    assert !isIncludeToken(super.LA(0)) : super.LT(0) + " not expected ";
                    return 0;
                }
                int superIndex = 0;
                int nonIncludeTokens = 0;
                do {
                    superIndex++;
                    int LA = super.LA(superIndex);
                    assert LA == super.LA(superIndex) : "how can LA be different?";
                    if (isIncludeToken(LA)) {
                        if (superIndex == 1 && canUseCallback()) {
                            // consume if the first and no markers
                            Antlr2ToAntlr3TokenAdapter t = super.LT(1);
                            if (TRACE) System.err.println("CONSUMING include token: " + t + " for LA(" + i + ")");
                            assert isIncludeToken(t.getType()) : t + " not expected ";
                            onIncludeToken(t);
                            assert super.LT(1).t == t.t : t + " have to be the same as " + super.LT(1).t;
                            super.consume();
                            superIndex = 0;
                        } else {
                            if (TRACE) System.err.println("NOT consumed include token: superIndex=" + superIndex + " canUseCallback=" + canUseCallback());
                        }
                    } else {
                        nonIncludeTokens++;
                    }
                } while (nonIncludeTokens < i);
                assert (superIndex >= i) && nonIncludeTokens == i : "LA(" + i + ") => LA(" + superIndex + ") " + nonIncludeTokens + ")" + super.LT(superIndex);
                return superIndex;
            }

            private static boolean isIncludeToken(int LA) {
                return LA == APTTokenTypes.INCLUDE || LA == APTTokenTypes.INCLUDE_NEXT;
            }

            private void onIncludeToken(org.antlr.runtime.Token t) {
                assert t instanceof Antlr2ToAntlr3TokenAdapter : t.getClass();
                if (((Antlr2ToAntlr3TokenAdapter)t).t instanceof APTToken) {
                    APTToken aptToken = (APTToken) ((Antlr2ToAntlr3TokenAdapter)t).t;
                    Boolean preInclude = (Boolean) aptToken.getProperty(Boolean.class);
                    CsmFile inclFile = (CsmFile) aptToken.getProperty(CsmFile.class);
                    if (inclFile != null) {
                        if (preInclude == Boolean.TRUE) {
                            if (TRACE) System.err.println(" >>> " + inclFile.getAbsolutePath());
                            cppCallback.pushFile(inclFile);
                            assert inclFile instanceof FileImpl;
                        } else {
                            CsmFile popFile = cppCallback.popFile();
                            if (TRACE) System.err.println(" <<< " + popFile.getAbsolutePath());
                            assert popFile == inclFile : "EXPECTED: " + inclFile + "\n POPED: " + popFile;
                        }
                    }
                }
            }
        }
    }

}
