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
package org.netbeans.modules.javascript2.editor;

import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.ExportSpecifierNode;
import com.oracle.js.parser.ir.ForNode;
import com.oracle.js.parser.ir.FromNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.ImportSpecifierNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.NameSpaceImportNode;
import com.oracle.js.parser.ir.ObjectNode;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.ir.UnaryNode;
import com.oracle.js.parser.ir.VarNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.javascript2.model.api.JsReference;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author Petr Pisl
 */
public class JsSemanticAnalyzer extends SemanticAnalyzer<JsParserResult> {
    //public static final EnumSet<ColoringAttributes> UNUSED_VARIABLE_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.VA);
    public static final EnumSet<ColoringAttributes> UNUSED_OBJECT_SET = EnumSet.of( ColoringAttributes.UNUSED,  ColoringAttributes.CLASS);
    public static final EnumSet<ColoringAttributes> UNUSED_METHOD_SET = EnumSet.of( ColoringAttributes.UNUSED,  ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> LOCAL_VARIABLE_DECLARATION = EnumSet.of(ColoringAttributes.LOCAL_VARIABLE_DECLARATION);
    public static final EnumSet<ColoringAttributes> LOCAL_VARIABLE_DECLARATION_UNUSED = EnumSet.of(ColoringAttributes.LOCAL_VARIABLE_DECLARATION, ColoringAttributes.UNUSED);
    public static final EnumSet<ColoringAttributes> LOCAL_VARIABLE_USE = EnumSet.of(ColoringAttributes.LOCAL_VARIABLE);
    public static final EnumSet<ColoringAttributes> GLOBAL_DEFINITION = EnumSet.of(ColoringAttributes.GLOBAL, ColoringAttributes.CLASS);
    public static final EnumSet<ColoringAttributes> NUMBER_OXB_CHAR = EnumSet.of(ColoringAttributes.CUSTOM1);
    public static final EnumSet<ColoringAttributes> SEMANTIC_KEYWORD = EnumSet.of(ColoringAttributes.CUSTOM2);

    private static final List<String> GLOBAL_TYPES = Arrays.asList(Type.ARRAY, Type.STRING, Type.BOOLEAN, Type.NUMBER);

    private final Collection<OffsetRange> globalJsHintInlines = new ArrayList<>();
    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    public JsSemanticAnalyzer() {
        this.cancelled = false;
        this.semanticHighlights = null;
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    @Override
    public void run(JsParserResult result, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<>(100);
        Model model = Model.getModel(result, false);
        JsObject global = model.getGlobalObject();
        Collection<Identifier> definedGlobal = ModelUtils.getDefinedGlobal(result.getSnapshot(), -1);
        for (Identifier iden: definedGlobal) {
            globalJsHintInlines.add(iden.getOffsetRange());
        }
        highlights = count(result, global, highlights, new HashSet<>());
        highlights = processSemanticKeywords(result, highlights);
        highlights = processNumbers(result, highlights);

        if (highlights != null && !highlights.isEmpty()) {
            semanticHighlights = highlights;
        } else {
            semanticHighlights = null;
        }
    }

    private Map<OffsetRange, Set<ColoringAttributes>> count (JsParserResult result, JsObject parent, Map<OffsetRange, Set<ColoringAttributes>> highlights, Set<String> processedObjects) {
        if (ModelUtils.wasProcessed(parent, processedObjects)) {
            return highlights;
        }
        for (Iterator<? extends JsObject> it = parent.getProperties().values().iterator(); it.hasNext();) {
            JsObject object = it.next();
            if (object.getDeclarationName() != null) {
                switch (object.getJSKind()) {
                    case CONSTRUCTOR:
                    case METHOD:
                    case FUNCTION:
                    case GENERATOR:
                        if(object.isDeclared() && !object.isAnonymous() && !object.getDeclarationName().getOffsetRange().isEmpty()) {
                            EnumSet<ColoringAttributes> coloring = ColoringAttributes.METHOD_SET;
                            if (object.getModifiers().contains(Modifier.PRIVATE)) {
                                if (object.getOccurrences().isEmpty()) {
                                    coloring = UNUSED_METHOD_SET;
                                } else if (object.getOccurrences().size() == 1) {
                                    OffsetRange orDeclaration = object.getDeclarationName().getOffsetRange();
                                    OffsetRange orOccurrence = object.getOccurrences().get(0).getOffsetRange();
                                    if (orDeclaration.equals(orOccurrence)) {
                                        coloring = UNUSED_METHOD_SET;
                                    }
                                }
                            }
                            addColoring(result, highlights, object.getDeclarationName().getOffsetRange(), coloring);
                        }
                        for(JsObject param: ((JsFunction)object).getParameters()) {
                            if (!(object instanceof JsReference && !((JsReference)object).getOriginal().isAnonymous())) {
                                count(result, param, highlights, processedObjects);
                            }
                            if (!hasSourceOccurences(result, param)) {
                                OffsetRange range = LexUtilities.getLexerOffsets(result, param.getDeclarationName().getOffsetRange());
                                if (range.getStart() < range.getEnd()) {
                                    // only for declared parameters
                                    highlights.put(range, ColoringAttributes.UNUSED_SET);
                                }
                            }
                        }
                        break;
                    case PROPERTY_GETTER:
                    case PROPERTY_SETTER:
                        int offset = LexUtilities.getLexerOffset(result, object.getDeclarationName().getOffsetRange().getStart());
                        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(result.getSnapshot(), offset);
                        if (ts != null) {
                            ts.move(offset);
                            if (ts.moveNext() && ts.movePrevious()) {
                                Token token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.DOC_COMMENT));
                                if ((token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) && token.length() == 3) {
                                    highlights.put(new OffsetRange(ts.offset(), ts.offset() + token.length()), ColoringAttributes.METHOD_SET);
                                }
                            }
                            highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.FIELD_SET);
                        }
                        break;
                    case OBJECT:
                    case OBJECT_LITERAL:
                    case CLASS:
                        if(!"UNKNOWN".equals(object.getName())) {
                             if (parent.getParent() == null && !GLOBAL_TYPES.contains(object.getName())) {
                                addColoring(result, highlights, object.getDeclarationName().getOffsetRange(), GLOBAL_DEFINITION);
                                for (Occurrence occurence : object.getOccurrences()) {
                                    addColoring(result, highlights, occurence.getOffsetRange(), ColoringAttributes.GLOBAL_SET);
                                }
                            } else if (object.isDeclared() && !ModelUtils.PROTOTYPE.equals(object.getName()) && !object.isAnonymous()) {
                                if((object.getOccurrences().isEmpty()
                                        || (object.getOccurrences().size() == 1 && object.getOccurrences().get(0).getOffsetRange().equals(object.getDeclarationName().getOffsetRange())))
                                        && object.getModifiers().contains(Modifier.PRIVATE)) {
                                    highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), UNUSED_OBJECT_SET);
                                } else {
                                    highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.CLASS_SET);
                                    TokenSequence<? extends JsTokenId> cts = LexUtilities.getJsTokenSequence(result.getSnapshot(), object.getDeclarationName().getOffsetRange().getStart());
                                    for (Occurrence occurrence: object.getOccurrences()) {
                                        cts.move(occurrence.getOffsetRange().getStart());
                                        if (cts.moveNext() && cts.token().id() == JsTokenId.STRING && !occurrence.getOffsetRange().equals(object.getDeclarationName().getOffsetRange())) {
                                            highlights.put(LexUtilities.getLexerOffsets(result, occurrence.getOffsetRange()), ColoringAttributes.CLASS_SET);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case PROPERTY:
                    case FIELD:
                        if(object.isDeclared()) {
                            addColoring(result, highlights, object.getDeclarationName().getOffsetRange(), ColoringAttributes.FIELD_SET);
                            for(Occurrence occurence: object.getOccurrences()) {
                                addColoring(result, highlights, occurence.getOffsetRange(), ColoringAttributes.FIELD_SET);
                            }
                        } else {
                            // we need to check whether the fiels is not used in aa["bb"], then bb color with black
                            TokenSequence<? extends JsTokenId> cts = LexUtilities.getJsTokenSequence(result.getSnapshot(), object.getOffset());
                            cts.move(object.getOffsetRange().getStart());
                            if (cts.moveNext() && cts.token().id() == JsTokenId.STRING) {
                                addColoring(result, highlights, object.getOffsetRange(), ColoringAttributes.FIELD_SET);
                            }
                            for (Occurrence occurrence : object.getOccurrences()) {
                                cts.move(occurrence.getOffsetRange().getStart());
                                if (cts.moveNext() && cts.token().id() == JsTokenId.STRING) {
                                    addColoring(result, highlights, occurrence.getOffsetRange(), ColoringAttributes.FIELD_SET);
                                }

                            }
                        }
                        break;
                    case VARIABLE:
                        if (parent.getParent() == null && !GLOBAL_TYPES.contains(object.getName())) {
                            addColoring(result, highlights, object.getDeclarationName().getOffsetRange(), ColoringAttributes.GLOBAL_SET);
                            for(Occurrence occurence: object.getOccurrences()) {
                                addColoring(result, highlights, occurence.getOffsetRange(), ColoringAttributes.GLOBAL_SET);
                            }
                        } else {
                            if ((object.getOccurrences().isEmpty()
                                    || (object.getOccurrences().size() == 1 && object.getOccurrences().get(0).getOffsetRange().equals(object.getDeclarationName().getOffsetRange())))
                                    && !GLOBAL_TYPES.contains(object.getName())) {
                                OffsetRange range = object.getDeclarationName().getOffsetRange();
                                if (range.getStart() < range.getEnd()) {
                                    // some virtual variables (like arguments) doesn't have to be declared, but are in the model
                                    if (object.getModifiers().contains(Modifier.PRIVATE) || object.getModifiers().contains(Modifier.PROTECTED)) {
                                        highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), LOCAL_VARIABLE_DECLARATION_UNUSED);
                                    } else {
                                        highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.UNUSED_SET);
                                    }
                                }
                            } else if (object instanceof JsObject && !ModelUtils.ARGUMENTS.equals(object.getName())) {   // NOI18N
                                if (object.getOccurrences().size() <= ((JsObject)object).getAssignmentCount()) {
                                    // probably is used only on the left site => is unused
                                    if (object.getDeclarationName().getOffsetRange().getLength() > 0) {
                                        highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.UNUSED_SET);
                                    }
                                    for(Occurrence occurence: object.getOccurrences()) {
                                        if (occurence.getOffsetRange().getLength() > 0) {
                                            highlights.put(LexUtilities.getLexerOffsets(result, occurence.getOffsetRange()), ColoringAttributes.UNUSED_SET);
                                        }
                                    }
                                } else if (object.getModifiers().contains(Modifier.PRIVATE) || object.getModifiers().contains(Modifier.PROTECTED)) {
                                    OffsetRange decOffset = object.getDeclarationName().getOffsetRange();
                                    addColoring(result, highlights, decOffset, LOCAL_VARIABLE_DECLARATION);
                                    for(Occurrence occurence: object.getOccurrences()) {
                                        if (occurence.getOffsetRange().getLength() > 0 && !occurence.getOffsetRange().equals(decOffset)) {
                                            addColoring(result, highlights, occurence.getOffsetRange(), LOCAL_VARIABLE_USE);
                                        }
                                    }
                                }
                            }
                        }
                }
            }
            if (isCancelled()) {
                highlights = null;
                break;
            }
            if (!(object instanceof JsReference && ModelUtils.isDescendant(object, ((JsReference)object).getOriginal()))) {
                highlights = count(result, object, highlights, processedObjects);
            }
        }

        return highlights;
    }

    private Map<OffsetRange, Set<ColoringAttributes>> processSemanticKeywords(final JsParserResult result,
            final Map<OffsetRange, Set<ColoringAttributes>> highlights) {

        FunctionNode root = result.getRoot();
        if (root == null) {
            return highlights;
        }

        NodeVisitor visitor = new NodeVisitor(new LexicalContext()) {

            @Override
            public boolean enterFunctionNode(FunctionNode functionNode) {
                if (functionNode.isModule()) {
                    functionNode.visitImports(this);
                    functionNode.visitExports(this);
                }

                if (functionNode.isAsync() && !functionNode.isMethod()) {
                    int pos = com.oracle.js.parser.Token.descPosition(functionNode.getFirstToken());
                    if (functionNode.getKind() != FunctionNode.Kind.ARROW) {
                        // in arrow function async is the first token
                        pos--;
                    }
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(result.getSnapshot(), pos);
                    if (ts != null) {
                        Token<? extends JsTokenId> token = LexUtilities.findPreviousNonWsNonComment(ts);
                        if (token != null && (token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) && "async".equals(token.text().toString())) {
                            highlights.put(LexUtilities.getLexerOffsets(result,
                                    new OffsetRange(ts.offset(), ts.offset() + token.length())), SEMANTIC_KEYWORD);
                        }
                    }
                }
                return super.enterFunctionNode(functionNode);
            }

            @Override
            public boolean enterImportSpecifierNode(ImportSpecifierNode importSpecifierNode) {
                if (importSpecifierNode.getIdentifier() != null) {
                    int start = importSpecifierNode.getIdentifier().getFinish();
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(result.getSnapshot(), start);
                    if (ts != null) {
                        Token<? extends JsTokenId> token = LexUtilities.findNextNonWsNonComment(ts);
                        if (token != null && (token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) && ts.offset() < importSpecifierNode.getBindingIdentifier().getStart()) {
                            // it has to be "as"
                            highlights.put(LexUtilities.getLexerOffsets(result,
                                    new OffsetRange(ts.offset(), ts.offset() + token.length())), SEMANTIC_KEYWORD);
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean enterExportSpecifierNode(ExportSpecifierNode exportSpecifierNode) {
                if (exportSpecifierNode.getExportIdentifier() != null) {
                    int start = exportSpecifierNode.getIdentifier().getFinish();
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(result.getSnapshot(), start);
                    if (ts != null) {
                        Token<? extends JsTokenId> token = LexUtilities.findNextNonWsNonComment(ts);
                        if (token != null && (token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) && ts.offset() < exportSpecifierNode.getExportIdentifier().getStart()) {
                            // it has to be "as"
                            highlights.put(LexUtilities.getLexerOffsets(result,
                                    new OffsetRange(ts.offset(), ts.offset() + token.length())), SEMANTIC_KEYWORD);
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean enterNameSpaceImportNode(NameSpaceImportNode nameSpaceImportNode) {
                int start = nameSpaceImportNode.getBindingIdentifier().getStart();
                if (start <= 0) {
                    return false;
                }

                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(result.getSnapshot(), start - 1);
                if (ts != null) {
                    Token<? extends JsTokenId> token = LexUtilities.findPreviousNonWsNonComment(ts);
                    if (token != null && token.id() == JsTokenId.IDENTIFIER && ts.token().length() > 1) {
                        // it has to be "as"
                        highlights.put(LexUtilities.getLexerOffsets(result,
                                new OffsetRange(ts.offset(), ts.offset() + token.length())), SEMANTIC_KEYWORD);
                    }
                }
                return false;
            }

            @Override
            public boolean enterFromNode(FromNode fromNode) {
                highlights.put(LexUtilities.getLexerOffsets(result,
                                new OffsetRange(fromNode.getStart(), fromNode.getStart() + "from".length())), SEMANTIC_KEYWORD); // NOI18N
                return false;
            }

            @Override
            public boolean enterClassNode(ClassNode classNode) {
                for (PropertyNode p : classNode.getClassElements()) {
                    handleProperty(p, true);
                }
                return super.enterClassNode(classNode);
            }

            @Override
            public boolean enterObjectNode(ObjectNode objectNode) {
                for (PropertyNode p : objectNode.getElements()) {
                    handleProperty(p, false);
                }
                return super.enterObjectNode(objectNode);
            }

            @Override
            public boolean enterVarNode(VarNode varNode) {
                if (varNode.isLet()) {
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(result.getSnapshot(), varNode.getStart() - 1);
                    if (ts != null) {
                        Token<? extends JsTokenId> token = LexUtilities.findPreviousNonWsNonComment(ts);
                        if (token != null && token.id() == JsTokenId.RESERVED_LET) {
                            highlights.put(LexUtilities.getLexerOffsets(result,
                                    new OffsetRange(ts.offset(), ts.offset() + token.length())), SEMANTIC_KEYWORD);
                        }
                    }
                }
                return super.enterVarNode(varNode);
            }

            @Override
            public boolean enterUnaryNode(UnaryNode unaryNode) {
                if (unaryNode.isTokenType(TokenType.AWAIT)) {
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(result.getSnapshot(), unaryNode.getStart());
                    if (ts != null) {
                        Token<? extends JsTokenId> token = LexUtilities.findPreviousNonWsNonComment(ts);
                        if (token != null && token.id() == JsTokenId.RESERVED_AWAIT) {
                            highlights.put(LexUtilities.getLexerOffsets(result,
                                    new OffsetRange(ts.offset(), ts.offset() + token.length())), SEMANTIC_KEYWORD);
                        }
                    }
                }
                return super.enterUnaryNode(unaryNode);
            }

            @Override
            public boolean enterForNode(ForNode forNode) {
                if (forNode.isForAwaitOf()) {
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(result.getSnapshot(), forNode.getStart());
                    if (ts != null) {
                        while(ts.moveNext()) {
                            Token<? extends JsTokenId> token = ts.token();
                            if (token != null && token.id() == JsTokenId.RESERVED_AWAIT) {
                                highlights.put(LexUtilities.getLexerOffsets(result,
                                        new OffsetRange(ts.offset(), ts.offset() + token.length())), SEMANTIC_KEYWORD);
                                break;
                            }
                        }
                    }
                }
                return super.enterForNode(forNode);
            }

            private void handleProperty(PropertyNode p, boolean classElement) {
                int offset = -1;
                if ((p.getValue() instanceof FunctionNode) && ((FunctionNode) p.getValue()).isAsync()) {
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(result.getSnapshot(), p.getStart() - 1);
                    if (ts != null) {
                        Token<? extends JsTokenId> token = LexUtilities.findPreviousNonWsNonComment(ts);
                        if (token != null && (token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) && "async".equals(token.text().toString())) {
                            offset = ts.offset();
                            highlights.put(LexUtilities.getLexerOffsets(result,
                                    new OffsetRange(ts.offset(), ts.offset() + token.length())), SEMANTIC_KEYWORD);
                        }
                    }
                }
                if (classElement && p.isStatic()) {
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(result.getSnapshot(), offset >= 0 ? offset - 1 : p.getStart() - 1);
                    if (ts != null) {
                        Token<? extends JsTokenId> token = LexUtilities.findPreviousNonWsNonComment(ts);
                        if (token != null && token.id() == JsTokenId.RESERVED_STATIC) {
                            highlights.put(LexUtilities.getLexerOffsets(result,
                                    new OffsetRange(ts.offset(), ts.offset() + token.length())), SEMANTIC_KEYWORD);
                        }
                    }
                }
            }
        };

        root.accept(visitor);
        return highlights;
    }

    private Map<OffsetRange, Set<ColoringAttributes>> processNumbers(JsParserResult result, Map<OffsetRange, Set<ColoringAttributes>> highlights) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(result.getSnapshot(), 0);
        if (ts != null) {
            ts.move(0);

            List<JsTokenId> lookFor = new ArrayList<>(3);
            lookFor.add(JsTokenId.NUMBER);
            Token<? extends JsTokenId> token;
            while (ts.moveNext() && (token = LexUtilities.findNextToken(ts, lookFor)) != null) {
                if (token.id() == JsTokenId.NUMBER) {
                    String number = token.text().toString().toLowerCase(Locale.ENGLISH);
                    if (number.startsWith("0b") || number.startsWith("0x") || number.startsWith("0o")) { //NOI18N
                        highlights.put(LexUtilities.getLexerOffsets(result, new OffsetRange(ts.offset() + 1, ts.offset() + 2)), NUMBER_OXB_CHAR);
                    }
                }
            }
        }
        return highlights;
    }

    private void addColoring(JsParserResult result, Map<OffsetRange, Set<ColoringAttributes>> highlights, OffsetRange astRange, Set<ColoringAttributes> coloring) {
        int start = result.getSnapshot().getOriginalOffset(astRange.getStart());
        int end = result.getSnapshot().getOriginalOffset(astRange.getEnd());
        if (start > -1 && end > -1 && start < end && !isInComment(result, astRange)) {
            OffsetRange range = start == astRange.getStart() ? astRange : new OffsetRange(start, end);
            highlights.put(range, coloring);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public synchronized void cancel() {
        cancelled = true;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    private boolean hasSourceOccurences(JsParserResult result, JsObject param) {
        if (param.getOccurrences().isEmpty()) {
            return false;
        }
        if (param.getOccurrences().size() == 1 && param.getOccurrences().get(0).getOffsetRange().equals(param.getDeclarationName().getOffsetRange())) {
            return false;
        }

        int sourceOccurenceCount = 0;
        for (Occurrence occurrence : param.getOccurrences()) {
            if (!isInComment(result, occurrence.getOffsetRange())) {
                 sourceOccurenceCount++;
            }
            if (sourceOccurenceCount > 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isInComment(JsParserResult result, OffsetRange range) {
        for (JsComment comment : JsDocumentationSupport.getDocumentationHolder(result).getCommentBlocks().values()) {
            if (comment.getOffsetRange().containsInclusive(range.getStart())) {
                return true;
            }
        }
        if (globalJsHintInlines.contains(range)) {
            return true;
        }
        return false;
    }

}
