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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.php.blade.editor.EditorStringUtils;
import org.netbeans.modules.php.blade.editor.navigator.BladeStructureItem;
import org.netbeans.modules.php.blade.editor.navigator.BladeStructureItem.DirectiveBlockStructureItem;
import org.netbeans.modules.php.blade.editor.navigator.BladeStructureItem.DirectiveInlineStructureItem;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer.*;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParserBaseListener;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bhaidu
 */
public class BladeParserResult extends ParserResult {

    private static final Logger LOGGER = Logger.getLogger(BladeParserResult.class.getSimpleName());
    public final List<Error> errors = new ArrayList<>();
    private final Map<String, Reference> yieldReferences = new TreeMap<>();
    private final Map<String, Reference> stackReferences = new TreeMap<>();
    public final Map<String, List<OffsetRange>> includeBladeOccurences = new HashMap<>();
    public final Map<OffsetRange, Reference> occurancesForDeclaration = new TreeMap<>();
    public final Map<OffsetRange, String> phpClassOccurences = new TreeMap<>();
    public final Map<OffsetRange, String> phpFunctionOccurences = new TreeMap<>();
    public final Map<OffsetRange, Reference> phpMethodOccurences = new TreeMap<>();
    public final Map<OffsetRange, String> phpConstantOccurences = new TreeMap<>();
    public final Map<OffsetRange, String> phpNamespacePathOccurences = new TreeMap<>();
    public final Map<OffsetRange, Reference> customDirectivesReferences = new TreeMap<>();
    public final Map<OffsetRange, FieldAccessReference> fieldCallType = new TreeMap<>();
    public final Map<OffsetRange, Set<String>> loopScopedVariables = new TreeMap<>();
    public final List<BladeStructureItem> structure = new ArrayList<>();
    public final List<OffsetRange> folds = new ArrayList<>();
    volatile boolean finished = false;
    volatile boolean debugMode = false;

    public enum ReferenceType {
        YIELD, STACK, SECTION, PUSH, PUSH_IF, PREPEND, INCLUDE, INCLUDE_IF,
        INCLUDE_COND, EXTENDS, EACH, HAS_SECTION,
        SECTION_MISSING, USE, INJECT, CUSTOM_DIRECTIVE, POSSIBLE_DIRECTIVE,
        PHP_FUNCTION, PHP_CLASS, PHP_METHOD, PHP_CONSTANT, PHP_NAMESPACE, PHP_NAMESPACE_PATH_TYPE,
        STATIC_FIELD_ACCESS, VITE_PATH,
        TEMPLATE_PATH,
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
        long startTime = System.currentTimeMillis();
        if (debugMode){
            LOGGER.log(Level.INFO, "PARSER TRIGGERED BY {0}", taskClass);
        }
        if (!finished) {
            BladeAntlrParser parser = createParser(getSnapshot());
            parser.setBuildParseTree(false);
            //LOGGER.info(String.format("parser created in %d ms", System.currentTimeMillis() - startTime));
            parser.addErrorListener(createErrorListener());
            parser.addParseListener(createDeclarationReferencesListener());
            parser.addParseListener(createPhpElementsOccurencesListener());

            if (taskClass.toLowerCase().contains("completion")) {
                parser.addParseListener(createVariableListener());
            }

            parser.addParseListener(createStructureListener());

            if (taskClass.toLowerCase().contains("hints")) {
                parser.addParseListener(createSemanticsListener());
            }
            evaluateParser(parser);
            if (debugMode){
                LOGGER.info(String.format("Parser evaluated in %d ms " + taskClass + " | " + this.getFileObject().getNameExt(), System.currentTimeMillis() - startTime));
            }
            finished = true;
        }
        long time = System.currentTimeMillis() - startTime;
        
        if (debugMode){
            LOGGER.info(String.format("finished parser took %d ms " + this.getFileObject().getNameExt(), time));
        }
        return this;
    }

    protected void evaluateParser(BladeAntlrParser parser) {
        parser.file();
    }

    protected ParseTreeListener createDeclarationReferencesListener() {

        return new BladeAntlrParserBaseListener() {
            @Override
            public void exitDoubleArgWrapperP(BladeAntlrParser.DoubleArgWrapperPContext ctx) {
                Token directive = ctx.getParent().getStart();
                if (directive == null) {
                    return;
                }

                Token paramString = ctx.idString;

                if (paramString == null || paramString.getText().length() < 3) {
                    return;
                }

                addIdentifiableOccurenceForDeclaration(directive, paramString);
            }

            @Override
            public void exitSingleArgWrapperP(BladeAntlrParser.SingleArgWrapperPContext ctx) {
                Token directive = ctx.getParent().getStart();
                if (directive == null) {
                    return;
                }

                Token paramString = ctx.idString;

                if (paramString == null || paramString.getText().length() < 3) {
                    return;
                }

                addIdentifiableOccurenceForDeclaration(directive, paramString);
            }

            @Override
            public void exitIncludeCond(BladeAntlrParser.IncludeCondContext ctx) {
                Token directive = ctx.d_name;
                if (directive == null) {
                    return;
                }

                Token paramString = ctx.idString;

                if (paramString == null || paramString.getText().length() < 3) {
                    return;
                }

                addIdentifiableOccurenceForDeclaration(directive, paramString);
            }

            @Override
            public void exitEach(BladeAntlrParser.EachContext ctx) {
                Token directive = ctx.getStart();
                if (directive == null) {
                    return;
                }

                Token paramString = ctx.idString;

                if (paramString == null || paramString.getText().length() < 3) {
                    return;
                }

                addIdentifiableOccurenceForDeclaration(directive, paramString);
            }

            @Override
            public void exitIdentifiableType(BladeAntlrParser.IdentifiableTypeContext ctx) {
                Token directive = ctx.d_name;
                if (directive == null) {
                    return;
                }

                Token paramString = ctx.idString;

                if (paramString == null || paramString.getText().length() < 3) {
                    return;
                }

                addIdentifiableOccurenceForDeclaration(directive, paramString);
            }

            @Override
            public void exitCustom_directive(BladeAntlrParser.Custom_directiveContext ctx) {
                String directiveName = ctx.getStart().getText();
                OffsetRange range = new OffsetRange(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                occurancesForDeclaration.put(range, new Reference(ReferenceType.CUSTOM_DIRECTIVE, directiveName, range));
            }

            /**
             * for @stack, @yield, @each, @include()
             */
            private void addIdentifiableOccurenceForDeclaration(Token directive,
                    Token paramString) {

                OffsetRange range = new OffsetRange(paramString.getStartIndex(), paramString.getStopIndex());
                String bladeParamText = EditorStringUtils.stripSurroundingQuotes(paramString.getText());

                //used for indexing
                switch (directive.getType()) {
                    case D_STACK:
                        addStackReference(ReferenceType.STACK, bladeParamText, range);
                        break;
                    case D_YIELD:
                        addYieldReference(ReferenceType.YIELD, bladeParamText, range);
                        break;
                }

                ReferenceType type = getReferenceType(directive.getType());

                if (type == null) {
                    return;
                }

                Reference ref;
                if (type.equals(ReferenceType.USE) || type.equals(ReferenceType.INJECT)) {
                    int lastSlashPos = bladeParamText.lastIndexOf("\\");
                    if (lastSlashPos < 0 || lastSlashPos >= bladeParamText.length() - 1) {
                        return;
                    }
                    range = new OffsetRange(paramString.getStartIndex(), paramString.getStopIndex());
                    //extracting the namespace and classname
                    ref = new Reference(type, bladeParamText.substring(lastSlashPos + 1), range, null, bladeParamText.substring(0, lastSlashPos));
                } else {
                    ref = new Reference(type, bladeParamText, range);
                }

                occurancesForDeclaration.put(range, ref);

                switch (directive.getType()) {
                    case D_EACH:
                    case D_INCLUDE_WHEN:
                    case D_INCLUDE_UNLESS:
                    case D_INCLUDE:
                    case D_INCLUDE_IF:
                    case D_INCLUDE_FIRST:
                        if (bladeParamText.contains("::")) {
                            //don't include package resources
                            break;
                        }
                        markIncludeBladeOccurrence(bladeParamText, range);
                        break;
                }
            }
            
            @Override
            public void exitAsset_bundler(BladeAntlrParser.Asset_bundlerContext ctx) {
                if (ctx.id_string != null){
                    Token idToken = ctx.id_string;
                    String path = idToken.getText();
                    path = EditorStringUtils.stripSurroundingQuotes(path);
                    OffsetRange range = new OffsetRange(idToken.getStartIndex(), idToken.getStopIndex());
                    Reference ref = new Reference(ReferenceType.VITE_PATH, path, range);
                    occurancesForDeclaration.put(range, ref);
                    return;
                }
                Token dirToken = ctx.dir;
                String dir = "";
                if (dirToken != null){
                    dir = dirToken.getText();
                }

                for (TerminalNode node : ctx.EXPR_STRING()){
                    Token symbolNode = node.getSymbol();
                    if (symbolNode == null){
                        continue;
                    }
                    String path = node.getText();
                    if (path.equals(dir)){
                        //skipping last parameter
                        continue;
                    }
                    path = EditorStringUtils.stripSurroundingQuotes(path);
                    OffsetRange range = new OffsetRange(symbolNode.getStartIndex(), symbolNode.getStopIndex());
                    Reference ref = new Reference(ReferenceType.VITE_PATH, path, range);
                    occurancesForDeclaration.put(range, ref);
                }
            }

        };

    }

    private ParseTreeListener createVariableListener() {
        return new BladeAntlrParserBaseListener() {
            List<ForeachVariables> foreachVariableList = new ArrayList<>();
            int foreachBalance = 0;

            @Override
            public void exitSimple_foreach_expr(BladeAntlrParser.Simple_foreach_exprContext ctx) {
                ForeachVariables foreachVariables = new ForeachVariables();
                foreachVariables.arrayVariable = ctx.loop_array.getText();
                if (ctx.item != null) {
                    foreachVariables.keyVariable = ctx.key.getText();
                    foreachVariables.itemVariable = ctx.item.getText();
                } else {

                    foreachVariables.itemVariable = ctx.key.getText();
                }
                foreachVariableList.add(foreachVariables);
            }

            @Override
            public void enterForeach(BladeAntlrParser.ForeachContext ctx) {
                foreachBalance++;
            }

            @Override
            public void exitForeach(BladeAntlrParser.ForeachContext ctx) {
                if (!foreachVariableList.isEmpty()) {
                    Set<String> varList = new LinkedHashSet<>();
                    for (ForeachVariables foreachVariables : foreachVariableList) {

                        varList.add(foreachVariables.arrayVariable);
                        if (foreachVariables.keyVariable != null) {
                            varList.add(foreachVariables.keyVariable);
                        }
                        varList.add(foreachVariables.itemVariable);

                    }
                    OffsetRange range = new OffsetRange(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                    loopScopedVariables.put(range, varList);
                }

                foreachBalance--;
                //reset
                if (foreachBalance < 0) {
                    foreachVariableList.clear();
                }
            }

            @Override
            public void exitForelse(BladeAntlrParser.ForelseContext ctx) {
                if (!foreachVariableList.isEmpty()) {
                    Set<String> varList = new LinkedHashSet<>();
                    for (ForeachVariables foreachVariables : foreachVariableList) {

                        varList.add(foreachVariables.arrayVariable);
                        if (foreachVariables.keyVariable != null) {
                            varList.add(foreachVariables.keyVariable);
                        }
                        varList.add(foreachVariables.itemVariable);

                    }
                    OffsetRange range = new OffsetRange(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                    loopScopedVariables.put(range, varList);
                }

                //reset
                foreachVariableList.clear();
            }

            @Override
            public void exitFile(BladeAntlrParser.FileContext ctx) {
                foreachVariableList.clear();
            }
        };
    }

    private ParseTreeListener createPhpElementsOccurencesListener() {
        return new BladeAntlrParserBaseListener() {
            /**
             * handling isolated PHP_IDENTIFIER to reference them as possible
             * constants
             */
            @Override
            public void exitComposed_php_expression(BladeAntlrParser.Composed_php_expressionContext ctx) {
                if (ctx.PHP_IDENTIFIER() != null && ctx.PHP_IDENTIFIER().getSymbol() != null) {
                    String identifierString = ctx.PHP_IDENTIFIER().getSymbol().getText();
                    if (identifierString != null && org.netbeans.modules.php.blade.syntax.StringUtils.isUpperCase(identifierString)) {
                        OffsetRange range = new OffsetRange(ctx.PHP_IDENTIFIER().getSymbol().getStartIndex(), ctx.PHP_IDENTIFIER().getSymbol().getStopIndex() + 1);
                        phpConstantOccurences.put(range, identifierString);
                    }
                }
                if (ctx.PHP_NAMESPACE_PATH() != null && ctx.PHP_NAMESPACE_PATH().getSymbol() != null) {
                    String identifierString = ctx.PHP_NAMESPACE_PATH().getSymbol().getText();
                    if (identifierString != null) {
                        OffsetRange range = new OffsetRange(ctx.PHP_NAMESPACE_PATH().getSymbol().getStartIndex(), ctx.PHP_NAMESPACE_PATH().getSymbol().getStopIndex() + 1);
                        phpNamespacePathOccurences.put(range, identifierString);
                    }
                }
            }

            @Override
            public void exitNamespacePath(BladeAntlrParser.NamespacePathContext ctx) {
                if (ctx.PHP_IDENTIFIER() == null || ctx.PHP_NAMESPACE_PATH() == null) {
                    return;
                }
                String namespace = ctx.PHP_NAMESPACE_PATH().getSymbol().getText();
                Token classIdentifier = ctx.PHP_IDENTIFIER().getSymbol();
                String className = classIdentifier.getText();
                OffsetRange classRange = new OffsetRange(classIdentifier.getStartIndex(), classIdentifier.getStopIndex() + 1);
                OffsetRange range = new OffsetRange(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                occurancesForDeclaration.put(range, new Reference(ReferenceType.PHP_NAMESPACE_PATH_TYPE, className, classRange, null, namespace));
            }

            @Override
            public void exitStatic_direct_namespace_class_access(BladeAntlrParser.Static_direct_namespace_class_accessContext ctx) {
                if (ctx.class_name == null) {
                    return;
                }

                Token classIdentifier = ctx.class_name;
                String className = classIdentifier.getText();
                OffsetRange range = new OffsetRange(classIdentifier.getStartIndex(), classIdentifier.getStopIndex() + 1);

                Reference classReference = new Reference(
                        ReferenceType.PHP_CLASS,
                        className, range,
                        null,
                        ctx.namespace.getText()
                );
                occurancesForDeclaration.put(range, classReference);

                OffsetRange callRange = null;
                int start = ctx.PHP_STATIC_ACCESS().getSymbol().getStartIndex();
                String fieldName = null;
                FieldType fieldType = null;

                if (ctx.static_property != null) {
                    //constants
                    callRange = new OffsetRange(start, ctx.static_property.getStopIndex() + 1);
                    fieldName = ctx.static_property.getText();
                    fieldType = FieldType.CONSTANT;
                } else if (ctx.func_name != null) {
                    //methods
                    callRange = new OffsetRange(start, ctx.func_name.getStopIndex() + 1);//bad
                    fieldName = ctx.func_name.getText();
                    fieldType = FieldType.METHOD;
                    OffsetRange functionRange = new OffsetRange(ctx.func_name.getStartIndex(), ctx.func_name.getStopIndex() + 1);

                    phpMethodOccurences.put(functionRange,
                            new Reference(ReferenceType.PHP_METHOD,
                                    fieldName, range, className,
                                    ctx.namespace.getText())
                    );

                }

                if (callRange != null) {
                    FieldAccessReference fieldAccess = new FieldAccessReference(
                            ReferenceType.STATIC_FIELD_ACCESS,
                            classReference,
                            fieldName,
                            fieldType
                    );
                    fieldCallType.put(callRange, fieldAccess);
                }
            }

            //this will be always static
            @Override
            public void exitStatic_direct_class_access(BladeAntlrParser.Static_direct_class_accessContext ctx) {
                if (ctx.class_name == null) {
                    return;
                }

                Token classIdentifier = ctx.class_name;
                String className = classIdentifier.getText();
                OffsetRange range = new OffsetRange(classIdentifier.getStartIndex(), classIdentifier.getStopIndex() + 1);

                phpClassOccurences.put(range, className);

                OffsetRange callRange = null;
                int start = ctx.PHP_STATIC_ACCESS().getSymbol().getStartIndex();
                String fieldName = null;
                FieldType fieldType = null;
                if (ctx.static_property != null) {
                    //constants
                    callRange = new OffsetRange(start, ctx.static_property.getStopIndex() + 1);
                    fieldName = ctx.static_property.getText();
                    fieldType = FieldType.CONSTANT;
                } else if (ctx.func_name != null) {
                    //methods
                    callRange = new OffsetRange(start, ctx.func_name.getStopIndex() + 1);
                    fieldName = ctx.func_name.getText();
                    fieldType = FieldType.METHOD;
                    OffsetRange functionRange = new OffsetRange(ctx.func_name.getStartIndex(), ctx.func_name.getStopIndex() + 1);
                    phpMethodOccurences.put(functionRange, new Reference(ReferenceType.PHP_METHOD, fieldName, range, className));
                }

                if (callRange != null) {
                     Reference classReference = new Reference(
                            ReferenceType.PHP_CLASS,
                            className, range,
                            null,
                            null
                    );
                    FieldAccessReference fieldAccess = new FieldAccessReference(
                            ReferenceType.STATIC_FIELD_ACCESS,
                            classReference,
                            fieldName,
                            fieldType
                    );
                    fieldCallType.put(callRange, fieldAccess);
                }
            }

            @Override
            public void exitFunction_call(BladeAntlrParser.Function_callContext ctx) {
                if (ctx.func_name == null || ctx.func_name.getText() == null) {
                    return;
                }
                String functionName = ctx.func_name.getText();
                OffsetRange range = new OffsetRange(ctx.func_name.getStartIndex(), ctx.func_name.getStopIndex() + 1);
                phpFunctionOccurences.put(range, functionName);
            }
        };
    }

    private ParseTreeListener createStructureListener() {

        return new BladeAntlrParserBaseListener() {

            final List<BladeStructureItem> lexerStructure = new ArrayList<>();
            int blockBalance = 0;
            String identifier;

            @Override
            public void enterInline_directive(BladeAntlrParser.Inline_directiveContext ctx) {
                identifier = null;
            }

            @Override
            public void exitIdentifiableType(BladeAntlrParser.IdentifiableTypeContext ctx) {
                Token directive = ctx.d_name;
                if (directive == null) {
                    return;
                }

                Token paramString = ctx.idString;

                if (paramString == null || paramString.getText().length() < 3) {
                    return;
                }

                String bladeParamText = paramString.getText();
                identifier = EditorStringUtils.stripSurroundingQuotes(bladeParamText);
            }
            
            @Override
            public void exitInline_directive(BladeAntlrParser.Inline_directiveContext ctx) {
                Token directiveToken = ctx.getStart();

                if (directiveToken == null) {
                    return;
                }

                DirectiveInlineStructureItem inlineElement;
                String directiveName = directiveToken.getText();

                inlineElement = new DirectiveInlineStructureItem(directiveName, identifier,
                        getFileObject(), directiveToken.getStartIndex(), directiveToken.getStopIndex() + 1);

                if (blockBalance > 0) {
                    lexerStructure.add(inlineElement);
                } else {
                    structure.add(inlineElement);
                }

            }

            @Override
            public void enterBlock_statement(BladeAntlrParser.Block_statementContext ctx) {
                identifier = null;
                blockBalance++;
            }

            @Override
            public void exitBlock_statement(BladeAntlrParser.Block_statementContext ctx) {
                blockBalance--;
                Token directiveToken = ctx.getStart();

                if (directiveToken == null) {
                    return;
                }

                String directiveName = directiveToken.getText();
                DirectiveBlockStructureItem blockItem = new DirectiveBlockStructureItem(directiveName, identifier,
                        getFileObject(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);

                blockItem.nestedItems.addAll(lexerStructure);
                lexerStructure.clear();
                if (blockBalance > 0 && !directiveName.startsWith("@else")) {
                    lexerStructure.add(blockItem);
                } else {
                    structure.add(blockItem);
                }
                //folds
                int start = ctx.getStart().getStartIndex() + 1 + directiveName.length();
                int end = ctx.getStop().getStartIndex();//the start of the close directive

                if (start > end) {
                    return;
                }
                OffsetRange range = new OffsetRange(start, end);
                if (!folds.contains(range)) {
                    folds.add(range);
                }
            }
        };
    }

    private ReferenceType getReferenceType(int type) {
        switch (type) {
            case D_INCLUDE:
                return ReferenceType.INCLUDE;
            case D_INCLUDE_IF:
                return ReferenceType.INCLUDE_IF;
            case D_EXTENDS:
                return ReferenceType.EXTENDS;
            case D_USE:
                return ReferenceType.USE;
            case D_INJECT:
                return ReferenceType.INJECT;
            case D_SECTION:
                return ReferenceType.SECTION;
            case D_HAS_SECTION:
                return ReferenceType.HAS_SECTION;
            case D_SECTION_MISSING:
                return ReferenceType.SECTION_MISSING;
            case D_PUSH:
                return ReferenceType.PUSH;
            case D_PUSH_IF:
                return ReferenceType.PUSH_IF;
            case D_PREPEND:
                return ReferenceType.PREPEND;
            case D_EACH:
                return ReferenceType.EACH;
            default:
                return null;
        }
    }

    /**
     * might move to a model all these processing
     *
     * @param offset
     * @return
     */
    public Reference findOccuredRefrence(int offset) {

        //TODO could do a similar thing and generate the Reference class on loop
        for (Map.Entry<OffsetRange, Reference> entry : occurancesForDeclaration.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return entry.getValue();
            }
        }

        for (Map.Entry<OffsetRange, String> entry : phpFunctionOccurences.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return new Reference(ReferenceType.PHP_FUNCTION, entry.getValue(), range);
            }
        }

        for (Map.Entry<OffsetRange, String> entry : phpClassOccurences.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return new Reference(ReferenceType.PHP_CLASS, entry.getValue(), range);
            }
        }

        for (Map.Entry<OffsetRange, Reference> entry : phpMethodOccurences.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return entry.getValue();
            }
        }

        for (Map.Entry<OffsetRange, String> entry : phpConstantOccurences.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return new Reference(ReferenceType.PHP_CONSTANT, entry.getValue(), range);
            }
        }

        for (Map.Entry<OffsetRange, String> entry : phpNamespacePathOccurences.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return new Reference(ReferenceType.PHP_NAMESPACE_PATH_TYPE, entry.getValue(), range);
            }
        }

        return null;
    }

    public FieldAccessReference findFieldAccessRefrence(int offset) {
        for (Map.Entry<OffsetRange, FieldAccessReference> entry : fieldCallType.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return entry.getValue();
            }
        }

        return null;
    }

    protected final void markIncludeBladeOccurrence(String refName, OffsetRange or) {
        includeBladeOccurences.computeIfAbsent(refName, s -> new ArrayList<>()).add(or);
    }

    //not used so far
    public Collection<? extends OffsetRange> findIncludeOccurrence(String refName) {
        ArrayList<OffsetRange> ret = new ArrayList<>();
        if (includeBladeOccurences.containsKey(refName)) {
            ret.addAll(includeBladeOccurences.get(refName));
        }
        return ret;
    }

    public Set<String> findLoopVariablesForScope(int offset) {

        Set<String> variableList = new LinkedHashSet<>();

        for (Map.Entry<OffsetRange, Set<String>> entry : loopScopedVariables.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                variableList.addAll(entry.getValue());
            }
        }

        if (variableList.isEmpty()) {
            return null;
        }
        return variableList;
    }

    private ParseTreeListener createSemanticsListener() {
        return new BladeAntlrParserBaseListener() {
            int ifBalance = 0;
            int ifStart = 0;

            @Override
            public void exitCustom_directive(BladeAntlrParser.Custom_directiveContext ctx) {
                String directiveName = ctx.getStart().getText();
                OffsetRange range = new OffsetRange(ctx.getStart().getStartIndex(), ctx.getStart().getStartIndex() + directiveName.length());
                customDirectivesReferences.put(range, new Reference(ReferenceType.CUSTOM_DIRECTIVE, directiveName, range));
            }

            @Override
            public void exitPossibleDirective(BladeAntlrParser.PossibleDirectiveContext ctx) {
                String directiveName = ctx.getStart().getText();
                OffsetRange range = new OffsetRange(ctx.getStart().getStartIndex(), ctx.getStart().getStartIndex() + directiveName.length());
                customDirectivesReferences.put(range, new Reference(ReferenceType.POSSIBLE_DIRECTIVE, directiveName, range));
            }

            @Override
            public void enterIf(BladeAntlrParser.IfContext ctx) {
                ifBalance++;
                ifStart = ctx.getStart().getStartIndex();
            }

            @Override
            public void exitEndif(BladeAntlrParser.EndifContext ctx) {
                ifBalance--;
            }

            @Override
            public void exitFile(BladeAntlrParser.FileContext ctx) {
                if (ifBalance != 0) {
                    errors.add(new BladeError(null, "Unclosed @if", null, getFileObject(), ifStart, ctx.getStart().getStopIndex(), Severity.ERROR));
                }
            }

        };
    }

    public void addYieldReference(ReferenceType type, String yieldId, OffsetRange range) {
        Reference ref = new Reference(type, yieldId, range);
        yieldReferences.put(ref.identifier, ref);
    }

    public void addStackReference(ReferenceType type, String stackId, OffsetRange range) {
        Reference ref = new Reference(type, stackId, range);
        stackReferences.put(ref.identifier, ref);
    }

    @Override
    protected boolean processingFinished() {
        return finished;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    @Override
    protected void invalidate() {
        loopScopedVariables.clear();
    }

    public Map<String, Reference> getYieldReferences() {
        return yieldReferences;
    }

    public Map<String, Reference> getStackReferences() {
        return stackReferences;
    }

    protected ANTLRErrorListener createErrorListener() {
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

    public static class FieldAccessReference {

        public final ReferenceType type;
        public final Reference ownerClass;
        public final String fieldName;
        public final FieldType fieldType;

        public FieldAccessReference(ReferenceType type, Reference ownerClass,
                String fieldName, FieldType fieldType) {
            this.type = type;
            this.ownerClass = ownerClass;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }
    }

    public class ForeachVariables {

        public String arrayVariable;
        public String keyVariable;
        public String itemVariable;
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
