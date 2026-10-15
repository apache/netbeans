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
package org.netbeans.modules.javascript2.model;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.json.parser.JsonLexer;
import org.netbeans.modules.javascript2.json.parser.JsonParser;
import org.netbeans.modules.javascript2.json.parser.JsonParserBaseVisitor;
import org.netbeans.modules.javascript2.json.parser.ParseTreeToXml;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;

/**
 *
 * @author Tomas Zezula
 */
public final class JsonModelResolver extends JsonParserBaseVisitor<Boolean> implements ModelResolver {

    private static final Logger LOG = Logger.getLogger(JsonModelResolver.class.getName());

    private final ParserResult parserResult;
    private final ModelBuilder modelBuilder;
    private final Deque<Pair<ParserRuleContext,JsObject>> path = new ArrayDeque<>();
    private boolean importantTerminalExpected;

    private JsonModelResolver(
        @NonNull final ParserResult parserResult,
        @NonNull final OccurrenceBuilder occurrenceBuilder) {
        Parameters.notNull("parserResult", parserResult);   //NOI18N
        Parameters.notNull("occurrenceBuilder", occurrenceBuilder); //NOI18N
        this.parserResult = parserResult;
        final FileObject fileObject = parserResult.getSnapshot().getSource().getFileObject();
        this.modelBuilder = new ModelBuilder(JsFunctionImpl.createGlobal(
                fileObject,
                Integer.MAX_VALUE,
                parserResult.getSnapshot().getMimeType()));
    }

    @Override
    public void init() {
        final JsonParser.JsonContext parseTree = parserResult.getLookup().lookup(JsonParser.JsonContext.class);
        if (LOG.isLoggable(Level.FINEST)) {
            final FileObject file = parserResult.getSnapshot().getSource().getFileObject();
            if (parseTree != null) {
                try {
                    final JsonLexer l = new JsonLexer(new ANTLRInputStream());
                    JsonParserBaseVisitor<Document> visitor = new ParseTreeToXml(
                            l,
                            new JsonParser(new CommonTokenStream(l)));
                    LOG.log(Level.FINEST,
                            "Parse tree for file: {0}\n{1}",    //NOI18N
                            new Object[]{
                                file == null ? null : FileUtil.getFileDisplayName(file),
                                ParseTreeToXml.stringify(visitor.visit(parseTree))
                            });
                } catch (IOException ioe) {
                    LOG.log(
                        Level.FINEST,
                        "Error dumping parse tree for file: {0} : {1}",      //NOI18N
                        new Object[] {
                            file == null ? null : FileUtil.getFileDisplayName(file),
                            ioe.getMessage()
                        });
                }
            } else {
                LOG.log(
                        Level.FINEST,
                        "No parse tree for file: {0}",      //NOI18N
                        file == null ? null : FileUtil.getFileDisplayName(file));
            }
        }
        if (parseTree != null) {
            parseTree.accept(this);
        }
    }

    @Override
    public JsObject getGlobalObject() {
        return modelBuilder.getGlobal();
    }

    @Override
    public JsObject resolveThis(JsObject where) {
        return where;
    }

    @Override
    public void processCalls(
            ModelElementFactory elementFactory,
            Map<String, Map<Integer, List<TypeUsage>>> returnTypesFromFrameworks) {
    }

    @Override
    public List<Identifier> getASTNodeName(Object astNode) {
        return Collections.emptyList();
    }

    @Override
    public Boolean visitObject(JsonParser.ObjectContext ctx) {
        JsObjectImpl object = null;
        if (!path.isEmpty()) {
            final Pair<ParserRuleContext,JsObject> top = path.peek();
            if (top.first() instanceof JsonParser.PairContext) {
                object = createJSObject(ctx, (JsonParser.PairContext) top.first());
            } else if (top.first() instanceof JsonParser.ArrayContext) {
                object = createJSObject(ctx, null);
                ((JsArrayImpl)top.second()).addTypeInArray(
                        new TypeUsage(object.getFullyQualifiedName(), ctx.start.getStartIndex()));
            }
        }
        if (object == null) {
            //Top level object or broken source
            object = createJSObject(ctx, null);
        }
        modelBuilder.setCurrentObject(object);
        path.push(Pair.<ParserRuleContext,JsObject>of(ctx,object));
        super.visitObject(ctx);
        path.pop();
        modelBuilder.reset();
        return true;
    }

    @Override
    public Boolean visitArray(JsonParser.ArrayContext ctx) {
        JsObjectImpl object = null;
        if (!path.isEmpty()) {
            final Pair<ParserRuleContext,JsObject> top = path.peek();
            if (top.first() instanceof JsonParser.PairContext) {
                object = createJsArray(ctx, (JsonParser.PairContext)top.first());
            }  else if (top.first() instanceof JsonParser.ArrayContext) {
                object = createJsArray(ctx, null);
                ((JsArrayImpl)top.second()).addTypeInArray(
                        new TypeUsage(object.getFullyQualifiedName(), ctx.start.getStartIndex()));
            }
        }
        if (object == null) {
            //Top level array or broken source
            object = createJsArray(ctx, null);
        }
        modelBuilder.setCurrentObject(object);
        path.push(Pair.<ParserRuleContext,JsObject>of(ctx,object));
        super.visitArray(ctx);
        path.pop();
        modelBuilder.reset();
        return true;
    }

    @Override
    public Boolean visitTerminal(TerminalNode node) {
        if (!importantTerminalExpected) {
            return false;
        }
        if (!path.isEmpty()) {
            final Pair<ParserRuleContext,JsObject> top = path.peek();
            if (top.first() instanceof JsonParser.PairContext) {
                createJSObject(node, (JsonParser.PairContext) top.first());
            } else if (top.first() instanceof JsonParser.ArrayContext) {
                ((JsArrayImpl)top.second()).addTypeInArray(getLiteralType(node));
            }
        } else {
            createJSObject(node, null);
        }
        super.visitTerminal(node);
        return true;
    }

    @Override
    public Boolean visitValue(JsonParser.ValueContext ctx) {
        importantTerminalExpected = hasImportantTerminal(ctx);
        final Boolean res = super.visitValue(ctx);
        importantTerminalExpected = false;
        return res;
    }

    @Override
    public Boolean visitPair(JsonParser.PairContext ctx) {
        path.push(Pair.<ParserRuleContext,JsObject>of(ctx,null));
        super.visitPair(ctx);
        path.pop();
        return true;
    }

    @NonNull
    private JsObjectImpl createJSObject(
            @NonNull final JsonParser.ObjectContext objLit,
            @NullAllowed final JsonParser.PairContext property) {
        final JsObjectImpl declarationScope = modelBuilder.getCurrentObject();
        final Identifier name = property != null ?
                new Identifier(
                        stringValue(property.key().getText()),
                        createOffsetRange(property.key())) :
                new Identifier(modelBuilder.getUnigueNameForAnonymObject(parserResult), OffsetRange.NONE);
        JsObjectImpl object = new JsObjectImpl(
                declarationScope,
                name,
                createOffsetRange(property != null ? property : objLit),
                true,
                declarationScope.getMimeType(),
                declarationScope.getSourceLabel());
        declarationScope.addProperty(object.getName(), object);
        object.setJsKind(JsElement.Kind.OBJECT_LITERAL);
        if (property == null) {
            object.setAnonymous(true);
        } else {
            object.addOccurrence(name.getOffsetRange());
        }
        return object;
    }

    @NonNull
    private JsObjectImpl createJSObject(
            @NonNull final TerminalNode literal,
            @NullAllowed final JsonParser.PairContext property) {
        final JsObjectImpl declarationScope = modelBuilder.getCurrentObject();
        final Identifier name = property != null ?
                new Identifier(
                        stringValue(property.key().getText()),
                        createOffsetRange(property.key())) :
                new Identifier(modelBuilder.getUnigueNameForAnonymObject(parserResult), OffsetRange.NONE);
        JsObjectImpl object = new JsObjectImpl(
                declarationScope,
                name,
                property != null ?
                        createOffsetRange(property) :
                        createOffsetRange(literal),
                true,
                declarationScope.getMimeType(),
                declarationScope.getSourceLabel());
        declarationScope.addProperty(object.getName(), object);
        object.addAssignment(
                getLiteralType(literal),
                object.getOffset());
        if (property == null) {
            object.setAnonymous(true);
            object.setJsKind(JsElement.Kind.OBJECT);
        } else {
            object.setJsKind(JsElement.Kind.PROPERTY);
            object.addOccurrence(name.getOffsetRange());
        }
        return object;
    }

    @NonNull
    private JsArrayImpl createJsArray(
            @NonNull final JsonParser.ArrayContext arrayLit,
            @NullAllowed final JsonParser.PairContext property) {
        JsObjectImpl declarationScope = modelBuilder.getCurrentObject();
        final Identifier name = property != null ?
                new Identifier(
                        stringValue(property.key().getText()),
                        createOffsetRange(property.key())) :
                new Identifier(modelBuilder.getUnigueNameForAnonymObject(parserResult), OffsetRange.NONE);
        JsArrayImpl array = new JsArrayImpl(
                declarationScope,
                name,
                createOffsetRange(property != null ? property : arrayLit),
                declarationScope.getMimeType(),
                declarationScope.getSourceLabel());
        declarationScope.addProperty(array.getName(), array);
        array.addAssignment(
                new TypeUsage(TypeUsage.ARRAY, -1, true),
                array.getOffset());
        array.setDeclared(true);    //Todo: Why? but when not set it's not displayed by navigator
        if (property == null) {
            array.setAnonymous(true);
            array.setJsKind(JsElement.Kind.OBJECT_LITERAL);
        } else {
            array.setJsKind(JsElement.Kind.PROPERTY);
            array.addOccurrence(name.getOffsetRange());
        }
        return array;
    }

    @NonNull
    private static OffsetRange createOffsetRange(@NonNull final ParserRuleContext parseTree) {
        int start = parseTree.start.getStartIndex();
        int end = parseTree.stop.getStopIndex() +1;
        if (end <= start) {
            end = start + 1;
            LOG.log(
                    Level.FINE,
                    "ParseTree.start offset is bigger then end offset [{0}, {1}]",      //NOI18N
                    new Object[]{parseTree.start.getStartIndex(), parseTree.stop.getStopIndex()});
        }
        return new OffsetRange( start, end);
    }

    @NonNull
    private static OffsetRange createOffsetRange(@NonNull final TerminalNode terminal) {
        return new OffsetRange(
            terminal.getSymbol().getStartIndex(),
            terminal.getSymbol().getStopIndex() +1);
    }

    @NonNull
    private String stringValue(@NonNull final String strTknVal) {
        return strTknVal.substring(1, strTknVal.length()-1);
    }

    private static boolean hasImportantTerminal(@NonNull final JsonParser.ValueContext valCtx) {
        return valCtx.array() == null && valCtx.object() == null;
    }

    private static TypeUsage getLiteralType(@NonNull final TerminalNode t) {
        switch (t.getSymbol().getType()) {
            case JsonLexer.FALSE:
            case JsonLexer.TRUE:
                return new TypeUsage(TypeUsage.BOOLEAN, -1, true);
            case JsonLexer.NUMBER:
                return new TypeUsage(TypeUsage.NUMBER, -1, true);
            case JsonLexer.STRING:
                return new TypeUsage(TypeUsage.STRING, -1, true);
            case JsonLexer.NULL:
                return new TypeUsage(TypeUsage.OBJECT, -1, true);
            default:
                throw new IllegalArgumentException(t.toString());
        }
    }

    @ServiceProvider(service = ModelResolver.Provider.class, position = 100)
    public static final class Provider implements ModelResolver.Provider {

        @CheckForNull
        @Override
        public ModelResolver create(
                @NonNull final ParserResult result,
                @NonNull final OccurrenceBuilder occurrenceBuilder) {
            final JsonParser.JsonContext parseTree = result.getLookup().lookup(JsonParser.JsonContext.class);
            if (parseTree == null) {
                return null;
            }
            return new JsonModelResolver(result, occurrenceBuilder);
        }
    }
}
