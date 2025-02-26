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
package org.netbeans.modules.php.blade.editor.parser;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.php.blade.editor.hints.BladeHintsProvider;
import org.netbeans.modules.php.blade.editor.navigator.BladeStructureItem;
import org.netbeans.modules.php.blade.editor.parser.listeners.CustomDirectivesListener;
import org.netbeans.modules.php.blade.editor.parser.listeners.PhpExpressionOccurenceListener;
import org.netbeans.modules.php.blade.editor.parser.listeners.ReferenceIdListener;
import org.netbeans.modules.php.blade.editor.parser.listeners.ScopeListener;
import org.netbeans.modules.php.blade.editor.parser.listeners.StructureListener;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bhaidu
 */
public class BladeParserResult extends ParserResult {

    private final List<Error> errors = new ArrayList<>();
    private final List<Error> phpErrors = new ArrayList<>();
    public final List<BladeStructureItem> structure = new ArrayList<>();
    public final List<OffsetRange> folds = new ArrayList<>();
    public final BladeReferenceIdsCollection bladeRreferenceIdsCollection = new BladeReferenceIdsCollection();
    public final BladePhpExpressionOccurences bladePhpExpressionOccurences = new BladePhpExpressionOccurences();
    public final BladeCustomDirectiveOccurences bladeCustomDirectiveOccurences = new BladeCustomDirectiveOccurences();
    public final BladeScope bladeScope = new BladeScope();

    private volatile boolean finished = false;

    public enum ReferenceType {
        YIELD, STACK, SECTION, PUSH, PUSH_IF, PREPEND, INCLUDE, INCLUDE_IF,
        INCLUDE_COND, EXTENDS, EACH, HAS_SECTION,
        SECTION_MISSING, USE, INJECT, CUSTOM_DIRECTIVE, POSSIBLE_DIRECTIVE,
        PHP_FUNCTION, PHP_CLASS, PHP_METHOD, PHP_CONSTANT, PHP_NAMESPACE, PHP_NAMESPACE_PATH_TYPE,
        STATIC_FIELD_ACCESS, VITE_PATH,
        TEMPLATE_PATH;
    }

    public BladeParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    protected BladeAntlrParser createParser(Snapshot snapshot) {
        CharStream cs = CharStreams.fromString(String.valueOf(snapshot.getText()));
        BladeAntlrLexer lexer = new BladeAntlrLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BladeAntlrParser ret = new BladeAntlrParser(tokens);
        ret.removeErrorListener(ConsoleErrorListener.INSTANCE);
        return ret;
    }

    public BladeParserResult get(String taskClass) {
        if (!finished) {
            String taskClassL = taskClass.toLowerCase();
            BladeAntlrParser parser = createParser(getSnapshot());
            parser.setBuildParseTree(false);
            parser.addErrorListener(createErrorListener());
            parser.addParseListener(new ReferenceIdListener(bladeRreferenceIdsCollection));
            parser.addParseListener(new PhpExpressionOccurenceListener(bladePhpExpressionOccurences));
            parser.addParseListener(new CustomDirectivesListener(bladeCustomDirectiveOccurences));

            if (taskClassL.contains("completion")) { //NOI18N
                parser.addParseListener(new ScopeListener(bladeScope));
            }

            //avoid on index
            if (!taskClassL.contains(".indexing.repository")) { //NOI18N
                parser.addParseListener(new StructureListener(structure, folds, getFileObject()));
            }


            evaluateParser(parser);

            if (allowPhpSyntaxParsingForTask(taskClassL) 
                    && BladeHintsProvider.phpSyntaxErrorsDisplayEnabled()
                    ) {
                phpSyntaxAnalyzer();
            }

            finished = true;
        }

        return this;
    }

    protected void evaluateParser(BladeAntlrParser parser) {
        parser.file();
    }

    @Override
    protected boolean processingFinished() {
        return finished;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    public void addError(Error error) {
        errors.add(error);
    }

    @Override
    protected void invalidate() {

    }

    private ANTLRErrorListener createErrorListener() {
        return new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                int errorPosition = 0;
                if (offendingSymbol instanceof Token) {
                    Token offendingToken = (Token) offendingSymbol;
                    errorPosition = offendingToken.getStartIndex();
                }
                errors.add(new BladeError(null, msg, null, getFileObject(), errorPosition, errorPosition, Severity.ERROR));
            }

        };
    }

    public final FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }

    public BladeReferenceIdsCollection getBladeReferenceIdsCollection() {
        return bladeRreferenceIdsCollection;
    }

    public BladePhpExpressionOccurences getBladePhpExpressionOccurences() {
        return bladePhpExpressionOccurences;
    }

    public BladeCustomDirectiveOccurences getBladeCustomDirectiveOccurences() {
        return bladeCustomDirectiveOccurences;
    }

    public BladeScope getBladeScope() {
        return bladeScope;
    }

    public void phpSyntaxAnalyzer() {
        for (OffsetRange range : getBladePhpExpressionOccurences().getPhpInlineOccurences()) {
            CharSequence snapshotExpr = getSnapshot().getText().subSequence(range.getStart(), range.getEnd());
            BladePhpSnippetParser phpSnippetParser = new BladePhpSnippetParser(snapshotExpr.toString(), getFileObject(), range.getStart());
            phpSnippetParser.parse();
            errors.addAll(phpSnippetParser.getDiagnostics());
        }
    }

    public static class BladeStringReference {

        public final int antlrTokentype;
        public final String identifier;

        public BladeStringReference(int antlrTokentype, String identifier) {
            this.antlrTokentype = antlrTokentype;
            this.identifier = identifier;
        }
    }
    
    private boolean allowPhpSyntaxParsingForTask(String taskClassL){
        return !taskClassL.contains("completion")  //NOI18N
                    && !taskClassL.contains("declaration")  //NOI18N
                    && !taskClassL.contains(".indexing.repository")  //NOI18N
                    && !taskClassL.contains("csl.navigation") //NOI18N
                ;  
    }

    public static class Reference {

        public final ReferenceType type;
        public final String identifier;
        public final String ownerClass;
        public final String namespace;
        public final OffsetRange defOffset;

        public Reference(ReferenceType type, String name, OffsetRange defOffset, String ownerClass) {
            this.type = type;
            this.identifier = name;
            this.defOffset = defOffset;
            this.ownerClass = ownerClass;
            this.namespace = null;
        }

        public Reference(ReferenceType type, String name, OffsetRange defOffset, String ownerClass, String namespace) {
            this.type = type;
            this.identifier = name;
            this.defOffset = defOffset;
            this.ownerClass = ownerClass;
            this.namespace = namespace;
        }

        public Reference(ReferenceType type, String name, OffsetRange defOffset) {
            this.type = type;
            this.identifier = name;
            this.defOffset = defOffset;
            this.ownerClass = null;
            this.namespace = null;
        }
    }

    public enum FieldType {
        PROPERTY,
        CONSTANT,
        METHOD;
    }

    /**
     * seems that java caches only this class ? BladeError is not found in some
     * occasions
     */
    public static class BladeError extends DefaultError implements org.netbeans.modules.csl.api.Error.Badging {

        public BladeError(@NullAllowed String key, @NonNull String displayName, @NullAllowed String description, @NonNull FileObject file, @NonNull int start, @NonNull int end, @NonNull Severity severity) {
            super(key, displayName, description, file, start, end, severity);
        }

        @Override
        public boolean showExplorerBadge() {
            return true;
        }

    }
}
