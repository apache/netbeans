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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.JsObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.api.StructureScanner.Configuration;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.api.Index;
import org.netbeans.modules.javascript2.model.api.JsReference;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public class JsStructureScanner implements StructureScanner {

    private static final String FONT_GRAY_COLOR = "<font color=\"#999999\">"; //NOI18N
    private static final String CLOSE_FONT = "</font>";                   //NOI18N

    private static final Logger LOGGER = Logger.getLogger(JsStructureScanner.class.getName());

    private final Language<JsTokenId> language;

    public JsStructureScanner(Language<JsTokenId> language) {
        this.language = language;
    }

    @Override
    public List<? extends StructureItem> scan(org.netbeans.modules.csl.spi.ParserResult info) {
        final List<StructureItem> items = new ArrayList<>();
        long start = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Structure scanner started at {0} ms", start);
        ParserResult result = (ParserResult) info;
        final Model model = Model.getModel(result, false);
        model.resolve();
        JsObject globalObject = model.getGlobalObject();
        final CancelSupport cancel = CancelSupport.getDefault();
        getEmbededItems(result, globalObject, items, new HashSet<>(), cancel);
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Creating structure took {0} ms", new Object[]{(end - start)});
        return items;
    }

    private List<StructureItem> getEmbededItems(ParserResult result, JsObject jsObject, List<StructureItem> collectedItems, Set<String> processedObjects, CancelSupport cancel) {
        if (ModelUtils.wasProcessed(jsObject, processedObjects)) {
            return collectedItems;
        }
        if (jsObject.isVirtual()) {
            return collectedItems;
        }
        if (cancel.isCancelled()) {
            return collectedItems;
        }
        Collection<? extends JsObject> properties = new ArrayList<>(jsObject.getProperties().values());
        boolean countFunctionChild = (jsObject.getJSKind().isFunction() && !jsObject.isAnonymous() && jsObject.getJSKind() != JsElement.Kind.CONSTRUCTOR
                && !containsFunction(jsObject))
                || (ModelUtils.PROTOTYPE.equals(jsObject.getName()) && properties.isEmpty());


        for (JsObject child : properties) {
            if (cancel.isCancelled()) {
                return collectedItems;
            }
            // we do not want to show items from virtual source
            if (result.getSnapshot().getOriginalOffset(child.getOffset()) < 0 && !ModelUtils.PROTOTYPE.equals(child.getName())) {
                continue;
            }

            if (child.isVirtual()) {
                continue;
            }
            if (child.getName().equals(ModelUtils.PROTOTYPE) && child.getProperties().isEmpty()) {
                // don't display prototype, if thre are no properties
                continue;
            }
            List<StructureItem> children = new ArrayList<>();
            if ((((countFunctionChild && !child.getModifiers().contains(Modifier.STATIC)
                    && !child.getName().equals(ModelUtils.PROTOTYPE)) || child.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT) &&  child.getJSKind() != JsElement.Kind.OBJECT_LITERAL)
                    || (child.getJSKind().isFunction() && child.isAnonymous() && child.getParent().getJSKind().isFunction() && child.getParent().getJSKind() != JsElement.Kind.FILE)) {
                // don't count children for functions and methods and anonyms
                continue;
            }
            if (!(child instanceof JsReference && ModelUtils.isDescendant(child, ((JsReference)child).getOriginal()))) {
                children = getEmbededItems(result, child, children, processedObjects, cancel);
            }
            if ((child.hasExactName() || child.isAnonymous() || child.getJSKind() == JsElement.Kind.CONSTRUCTOR) && child.getJSKind().isFunction()) {
                JsFunction function = (JsFunction)child;
                if (function.isAnonymous()) {
                    collectedItems.addAll(children);
                } else {
                    if (function.isDeclared() /*&& (!jsObject.isAnonymous() || (jsObject.isAnonymous() && jsObject.getFullyQualifiedName().indexOf('.') == -1))*/) {
                        collectedItems.add(new JsFunctionStructureItem(function, children, result));
                    }
                }
            } else if (((child.getJSKind() == JsElement.Kind.OBJECT && (!children.isEmpty() || child.isDeclared())) || child.getJSKind() == JsElement.Kind.OBJECT_LITERAL || child.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT)
                    && (!children.isEmpty() || child.isDeclared())) {
                if(!(jsObject.getJSKind() == JsElement.Kind.FILE && JsTokenId.JSON_MIME_TYPE.equals(jsObject.getMimeType()))) {
                    collectedItems.add(new JsObjectStructureItem(child, children, result));
                } else {
                    // don't include the first anonymous object.
                    collectedItems.addAll(children);
                }
            } else if (child.getJSKind() == JsElement.Kind.PROPERTY) {
                if(child.isDeclared() && (child.getModifiers().contains(Modifier.PUBLIC)
                        || !(jsObject.getParent() instanceof JsFunction) || jsObject.getJSKind() == JsElement.Kind.CLASS))
                    collectedItems.add(new JsSimpleStructureItem(child, children.isEmpty() ? null : children, "prop-", result)); //NOI18N
            } else if ((child.getJSKind() == JsElement.Kind.VARIABLE || child.getJSKind() == JsElement.Kind.CONSTANT)&& child.isDeclared()
                && (!jsObject.isAnonymous() || (jsObject.isAnonymous() && jsObject.getFullyQualifiedName().indexOf('.') == -1))) {
                    if (children.isEmpty()) {
                        collectedItems.add(new JsSimpleStructureItem(child, "var-", result)); //NOI18N
                    } else {
                        collectedItems.add(new JsObjectStructureItem(child, children, result));
                    }
            } else if ((child.getJSKind() == JsElement.Kind.CLASS && child.isDeclared())) {
                collectedItems.add(new JsClassStructureItem(child, children, result));
            } else if (child.getJSKind() == JsElement.Kind.BLOCK) {
                collectedItems.addAll(children);
            }
         }

        if (jsObject instanceof JsFunction jsFunction) {
            for (JsObject param: jsFunction.getParameters()) {
                if (hasDeclaredProperty(param) && !(jsObject instanceof JsReference && !((JsReference)jsObject).getOriginal().isAnonymous())) {
                    final List<StructureItem> items = new ArrayList<>();
                    getEmbededItems(result, param, items, processedObjects, cancel);
                    collectedItems.add(new JsObjectStructureItem(param, items, result));
                }
            }
            if (jsFunction.getReturnTypes().size() == 1 && !jsFunction.isAnonymous()) {
                TypeUsage returnType = jsFunction.getReturnTypes().iterator().next();
                JsObject returnObject = ModelUtils.findJsObjectByName(Model.getModel(result, false).getGlobalObject(), returnType.getType());
                if(returnObject != null && returnObject.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT) {
                     for (JsObject property: returnObject.getProperties().values()) {
                        final List<StructureItem> items = new ArrayList<>();
                        getEmbededItems(result, property, items, processedObjects, cancel);
                        collectedItems.add(new JsObjectStructureItem(property, items, result));
                    }
                }
            }
        }
        if (jsObject.getJSKind() == JsElement.Kind.BLOCK) {

        } else if (jsObject.getDeclarationName() != null) {
            Collection<? extends TypeUsage> assignmentForOffset = jsObject.getAssignmentForOffset(jsObject.getDeclarationName().getOffsetRange().getEnd());
            if (assignmentForOffset.size() == 1) {
                JsObject assignedObject = ModelUtils.findJsObjectByName(Model.getModel(result, false).getGlobalObject(), assignmentForOffset.iterator().next().getType());
                if (assignedObject != null && assignedObject.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT
                        && processedObjects.contains(assignedObject.getParent().getFullyQualifiedName())) {
                    for (JsObject property : assignedObject.getProperties().values()) {
                        final List<StructureItem> items = new ArrayList<>();
                        getEmbededItems(result, property, items, processedObjects, cancel);
                        collectedItems.add(new JsObjectStructureItem(property, items, result));
                    }
                }
            }
        }
        return collectedItems;
    }

    private boolean containsFunction(JsObject jsObject) {
        for (JsObject property: jsObject.getProperties().values()) {
            if (property.getJSKind().isFunction() && property.isDeclared() && !property.isAnonymous()) {
                return true;
            }
            if (containsFunction(property)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotAnonymousFunction(TokenSequence<?> tsInput, int functionKeywordPosition) {
        @SuppressWarnings("unchecked")
        TokenSequence<JsTokenId> ts = (TokenSequence<JsTokenId>) tsInput;
        // expect that the ts in on "{"
        int position = ts.offset();
        boolean value = false;
        // find the function keyword
        ts.move(functionKeywordPosition);
        ts.moveNext();
        Token<?> token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE));
        if ((token.id() == JsTokenId.OPERATOR_ASSIGNMENT || token.id() == JsTokenId.OPERATOR_COLON) && ts.movePrevious()) {
            token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE));
            if (token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) {
                // it's:
                // name : function() ...
                // name = function() ...
                value = true;
            }
        }
        if (!value) {
            ts.move(functionKeywordPosition);
            ts.moveNext(); ts.moveNext();
            token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE));
            if (token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) {
                value = true;
            }
        }
        ts.move(position);
        ts.moveNext();
        return value;
    }

    private static class FoldingItem {
        String kind;
        int start;

        public FoldingItem(String kind, int start) {
            this.kind = kind;
            this.start = start;
        }

    }

    @Override
    public Map<String, List<OffsetRange>> folds(org.netbeans.modules.csl.spi.ParserResult info) {
        long start = System.currentTimeMillis();
        Map<String, List<OffsetRange>> folds;
        String mimeType = info.getSnapshot().getMimeType();
        if (JsTokenId.isJSONBasedMimeType(mimeType)) {
            folds = foldsJson((ParserResult)info);
        } else {
            folds = new HashMap<>();
            TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
            TokenSequence<?> ts = th.tokenSequence(language);
            List<TokenSequence<?>> list = th.tokenSequenceList(ts.languagePath(), 0, info.getSnapshot().getText().length());
            List<FoldingItem> stack = new ArrayList<>();
            for (TokenSequenceIterator tsi = new TokenSequenceIterator(list, false); tsi.hasMore();) {
                ts = tsi.getSequence();
                TokenId tokenId;
                JsTokenId lastContextId = null;
                int functionKeywordPosition = 0;
                ts.moveStart();
                while (ts.moveNext()) {
                    tokenId = ts.token().id();
                    if (tokenId == JsTokenId.DOC_COMMENT) {
                        // hardcoded values should be ok since token comes in case if it's completed (/** ... */)
                        int startOffset = ts.offset() + 3;
                        int endOffset = ts.offset() + ts.token().length() - 2;
                        appendFold(folds, FoldType.DOCUMENTATION.code(),  info.getSnapshot().getOriginalOffset(startOffset),
                                info.getSnapshot().getOriginalOffset(endOffset));
                    } else if (tokenId == JsTokenId.BLOCK_COMMENT) {
                        int startOffset = ts.offset() + 2;
                        int endOffset = ts.offset() + ts.token().length() - 2;
                        appendFold(folds, FoldType.COMMENT.code(), info.getSnapshot().getOriginalOffset(startOffset),
                                info.getSnapshot().getOriginalOffset(endOffset));
                    } else if (((JsTokenId) tokenId).isKeyword()) {
                        lastContextId = (JsTokenId) tokenId;
                        if(lastContextId == JsTokenId.KEYWORD_FUNCTION) {
                            functionKeywordPosition = ts.offset();
                        }
                    } else if (tokenId == JsTokenId.BRACKET_LEFT_CURLY) {
                        String kind;
                        if (lastContextId == JsTokenId.KEYWORD_FUNCTION && isNotAnonymousFunction(ts, functionKeywordPosition)) {
                            kind = FoldType.MEMBER.code();
                        } else {
                            kind = FoldType.CODE_BLOCK.code();
                        }
                        stack.add(new FoldingItem(kind, ts.offset()));
                    } else if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY && !stack.isEmpty()) {
                        FoldingItem fromStack = stack.remove(stack.size() - 1);

                        TokenId previousTokenId = null;
                        if (ts.movePrevious()) {
                            previousTokenId = ts.token().id();
                            ts.moveNext();
                        }

                        if (previousTokenId != null && previousTokenId != JsTokenId.BRACKET_LEFT_CURLY) {
                            appendFold(folds, fromStack.kind, info.getSnapshot().getOriginalOffset(fromStack.start),
                                    info.getSnapshot().getOriginalOffset(ts.offset() + 1));
                        }
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Folding took %s ms", (end - start));
        return folds;
    }

    private Map<String, List<OffsetRange>> foldsJson(ParserResult info) {
        final Map<String, List<OffsetRange>> folds = new HashMap<>();
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        TokenSequence<?> ts = th.tokenSequence(language);
        List<TokenSequence<?>> list = th.tokenSequenceList(ts.languagePath(), 0, info.getSnapshot().getText().length());
        List<FoldingItem> stack = new ArrayList<>();
        for (TokenSequenceIterator tsi = new TokenSequenceIterator(list, false); tsi.hasMore();) {
            ts = tsi.getSequence();
            TokenId tokenId;
            ts.moveStart();
            while (ts.moveNext()) {
                tokenId = ts.token().id();
                if (tokenId == JsTokenId.BRACKET_LEFT_CURLY) {
                    stack.add(new FoldingItem(JsonFoldTypeProvider.OBJECT.code(), ts.offset()));
                } else if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY && !stack.isEmpty()) {
                    FoldingItem fromStack = stack.remove(stack.size() - 1);
                    appendFold(folds, fromStack.kind, info.getSnapshot().getOriginalOffset(fromStack.start),
                            info.getSnapshot().getOriginalOffset(ts.offset() + 1));
                } else if (tokenId == JsTokenId.BRACKET_LEFT_BRACKET) {
                    stack.add(new FoldingItem(JsonFoldTypeProvider.ARRAY.code(), ts.offset()));
                } else if (tokenId == JsTokenId.BRACKET_RIGHT_BRACKET && !stack.isEmpty()) {
                    FoldingItem fromStack = stack.remove(stack.size() - 1);
                    appendFold(folds, fromStack.kind, info.getSnapshot().getOriginalOffset(fromStack.start),
                            info.getSnapshot().getOriginalOffset(ts.offset() + 1));
                }

            }
        }
        return folds;
    }

    private void appendFold(Map<String, List<OffsetRange>> folds, String kind, int startOffset, int endOffset) {
        if (startOffset >= 0 && endOffset >= startOffset) {
            getRanges(folds, kind).add(new OffsetRange(startOffset, endOffset));
        }
    }

    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<>();
            folds.put(kind, ranges);
        }
        return ranges;
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, true);
    }

    private boolean hasDeclaredProperty(JsObject jsObject) {
        boolean result =  false;

        Iterator<? extends JsObject> it = jsObject.getProperties().values().iterator();
        while (!result && it.hasNext()) {
            JsObject property = it.next();
            result = property.isDeclared();
            if (!result) {
                result = hasDeclaredProperty(property);
            }
        }

        return result;
    }


    private abstract class JsStructureItem implements StructureItem {

        private JsObject modelElement;

        private final List<? extends StructureItem> children;
        private final String sortPrefix;
        protected final ParserResult parserResult;
        private final String fqn;

        public JsStructureItem(JsObject elementHandle, List<? extends StructureItem> children, String sortPrefix, ParserResult parserResult) {
            this.modelElement = elementHandle;
            this.sortPrefix = sortPrefix;
            this.parserResult = parserResult;
            this.fqn = modelElement.getFullyQualifiedName();
            if (children != null) {
                this.children = children;
            } else {
                this.children = Collections.emptyList();
            }
        }

        @Override
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JsStructureItem other = (JsStructureItem) obj;
            if ((this.fqn == null) ? (other.fqn != null) : !this.fqn.equals(other.fqn)) {
                return false;
            }
            if ((this.modelElement == null && other.modelElement != null)
                    || (this.modelElement != null && other.modelElement == null)) {
                return false;
            }
            if (modelElement != other.modelElement) {
                if ((this.modelElement.getJSKind() == null) ? (other.modelElement.getJSKind() != null) :
                        !this.modelElement.getJSKind().equals(other.modelElement.getJSKind())) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + (this.fqn != null ? this.fqn.hashCode() : 0);
            hash = 37 * hash + (this.modelElement != null && this.modelElement.getJSKind() != null ?this.modelElement.getJSKind().hashCode() : 0);
            return hash;
        }

        @Override
        public String getName() {
            return modelElement.getName();
        }

        @Override
        public String getSortText() {
            return sortPrefix + modelElement.getName();
        }

        @Override
        public ElementHandle getElementHandle() {
            return modelElement;
        }

        @Override
        public ElementKind getKind() {
              return modelElement.getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            Set<Modifier> modifiers;

            if (modelElement.getModifiers().isEmpty()) {
                modifiers = Collections.emptySet();
            } else {
                modifiers = EnumSet.noneOf(Modifier.class);
                modifiers.addAll(modelElement.getModifiers());
            }

            if (modifiers.contains(Modifier.PRIVATE) && (modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED))) {
                modifiers.remove(Modifier.PUBLIC);
                modifiers.remove(Modifier.PROTECTED);
            }
            return modifiers;
        }

        @Override
        public boolean isLeaf() {
            return children.isEmpty();
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return children;
        }

        @Override
        public long getPosition() {
            return parserResult.getSnapshot().getOriginalOffset(modelElement.getOffset());
        }

        @Override
        public long getEndPosition() {
            return parserResult.getSnapshot().getOriginalOffset(modelElement.getOffsetRange().getEnd());
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }

        public JsObject getModelElement() {
            return modelElement;
        }

        protected void appendTypeInfo(HtmlFormatter formatter, Collection<? extends Type> types) {
            Collection<String> displayNames = Utils.getDisplayNames(types);
            if (!displayNames.isEmpty()) {
                formatter.appendHtml(FONT_GRAY_COLOR);
                formatter.appendText(" : ");
                boolean addDelimiter = false;
                for (String displayName : displayNames) {
                    if (addDelimiter) {
                        formatter.appendText("|");
                    } else {
                        addDelimiter = true;
                    }
                    formatter.appendHtml(displayName);
                }
                formatter.appendHtml(CLOSE_FONT);
            }
        }

    }

    private class JsClassStructureItem extends JsStructureItem {

        public JsClassStructureItem(JsObject elementHandle, List<? extends StructureItem> children, ParserResult parserResult) {
            super(elementHandle, children, "cl", parserResult); //NOI18N
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            JsObject clObject = getModelElement();
            boolean isDeprecated = clObject.isDeprecated();
            if (isDeprecated) {
                formatter.deprecated(true);
            }
            formatter.appendText(clObject.getDeclarationName().getName());
            if (isDeprecated) {
                formatter.deprecated(false);
            }
            JsObject prototype = clObject.getProperty(ModelUtils.PROTOTYPE);
            if (prototype != null) {
                Collection<? extends TypeUsage> assignments = prototype.getAssignments();
                if (assignments != null && !assignments.isEmpty()) {
                    // the class extends
                    formatter.appendHtml(FONT_GRAY_COLOR);
                    formatter.appendText(" :: ");   // NOI18N
                    boolean addComma = false;
                    for (TypeUsage type : assignments) {
                        if (addComma) {
                            formatter.appendText(", "); // NOI18N
                        } else {
                            addComma = true;
                        }
                        formatter.appendText(type.getType());
                    }
                    formatter.appendHtml(CLOSE_FONT);
                }
            }
            return formatter.getText();
        }

    }

    private static ImageIcon priviligedIcon = null;
    private static ImageIcon callbackIcon = null;
    private static ImageIcon publicGenerator = null;
    private static ImageIcon privateGenerator = null;
    private static ImageIcon priviligedGenerator = null;

    private class JsFunctionStructureItem extends JsStructureItem {

        private final List<TypeUsage> resolvedTypes;

        public JsFunctionStructureItem(
                JsFunction elementHandle,
                List<? extends StructureItem> children,
                ParserResult parserResult) {
            super(elementHandle, children, "fn", parserResult); //NOI18N
            Collection<? extends TypeUsage> returnTypes = getFunctionScope().getReturnTypes();
            resolvedTypes = new ArrayList<>(ModelUtils.resolveTypes(returnTypes,
                    Model.getModel(parserResult, false), Index.get(parserResult.getSnapshot().getSource().getFileObject()), false));
        }

        public final JsFunction getFunctionScope() {
            return (JsFunction) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendFunctionDescription(getFunctionScope(), formatter);
            return formatter.getText();
        }

        protected void appendFunctionDescription(JsFunction function, HtmlFormatter formatter) {
            formatter.reset();
            if (function == null) {
                return;
            }
            boolean isDeprecated = getFunctionScope().isDeprecated();
            if (isDeprecated) {
                formatter.deprecated(true);
            }
            formatter.appendText(getFunctionScope().getDeclarationName().getName());
            if (isDeprecated) {
                formatter.deprecated(false);
            }
            formatter.appendText("(");   //NOI18N
            boolean addComma = false;
            for(JsObject jsObject : function.getParameters()) {
                if (addComma) {
                    formatter.appendText(", "); //NOI8N
                } else {
                    addComma = true;
                }
                Collection<? extends TypeUsage> types = jsObject.getAssignmentForOffset(jsObject.getDeclarationName().getOffsetRange().getStart());
                if (!types.isEmpty()) {
                    formatter.appendHtml(FONT_GRAY_COLOR);
                    StringBuilder typeSb = new StringBuilder();
                    for (TypeUsage type : types) {

                        if (typeSb.length() > 0) {
                            typeSb.append("|"); //NOI18N
                        }
                        typeSb.append(type.getType());
                    }
                    if (typeSb.length() > 0) {
                        formatter.appendText(typeSb.toString());
                    }
                    formatter.appendText(" ");   //NOI18N
                    formatter.appendHtml(CLOSE_FONT);
                }
                formatter.appendText(jsObject.getName());
            }
            formatter.appendText(")");   //NOI18N
            appendTypeInfo(formatter, resolvedTypes);
        }

        @Override
        public String getName() {
            return getFunctionScope().getDeclarationName().getName();
        }

        @Override
        public ImageIcon getCustomIcon() {
            if (getFunctionScope().getJSKind() == JsElement.Kind.CALLBACK) {
                if (callbackIcon == null) {
                    callbackIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/methodCallback.png", false); //NOI18N
                }
                return callbackIcon;
            } else  if (getFunctionScope().getJSKind() == JsElement.Kind.GENERATOR) {
                if (getModifiers().contains(Modifier.PUBLIC)) {
                    if (publicGenerator == null) {
                        publicGenerator = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/generatorPublic.png", false); //NOI18N
                    }
                    return publicGenerator;
                } else if (getModifiers().contains(Modifier.PRIVATE)) {
                    if (privateGenerator == null) {
                        privateGenerator = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/generatorPrivate.png", false); //NOI18N
                    }
                    return privateGenerator;
                } else if (getModifiers().contains(Modifier.PROTECTED)) {
                    if (priviligedGenerator == null) {
                        priviligedGenerator = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/generatorPriviliged.png", false); //NOI18N
                    }
                    return priviligedGenerator;
                }
            }
            if (getModifiers().contains(Modifier.PROTECTED)) {
                if(priviligedIcon == null) {
                    priviligedIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/methodPriviliged.png", false); //NOI18N
                }
                return priviligedIcon;
            }
            return super.getCustomIcon();
        }


    }

    private class JsObjectStructureItem extends JsStructureItem {

        public JsObjectStructureItem(JsObject elementHandle, List<? extends StructureItem> children, ParserResult parserResult) {
            super(elementHandle, children, "ob", parserResult); //NOI18N
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                appendObjectDescription(getModelElement(), formatter);
                return formatter.getText();
        }

        protected void appendObjectDescription(JsObject object, HtmlFormatter formatter) {
            formatter.reset();
            if (object == null) {
                return;
            }
            boolean isDeprecated = object.isDeprecated();
            if (isDeprecated) {
                formatter.deprecated(true);
            }
            formatter.appendText(object.isAnonymous() ? "{...}" : object.getName()); //NOI18N
            if (isDeprecated) {
                formatter.deprecated(false);
            }
        }

    }

    private class JsSimpleStructureItem extends JsStructureItem {

        private final JsObject object;

        private final List<TypeUsage> resolvedTypes;

        public JsSimpleStructureItem(JsObject elementHandle, String sortPrefix, ParserResult parserResult) {
            this(elementHandle, null, sortPrefix, parserResult);
        }

        public JsSimpleStructureItem(JsObject elementHandle, List<? extends StructureItem> children, String sortPrefix, ParserResult parserResult) {
            super(elementHandle, children, sortPrefix, parserResult);
            this.object = elementHandle;

            Collection<? extends TypeUsage> assignmentForOffset = object.getAssignments();//tForOffset(object.getDeclarationName().getOffsetRange().getEnd());
            resolvedTypes = new ArrayList<>(ModelUtils.resolveTypes(assignmentForOffset,
                    Model.getModel(parserResult, false), Index.get(parserResult.getSnapshot().getSource().getFileObject()), false));
        }


        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            boolean isDeprecated = object.isDeprecated();
            if (isDeprecated) {
                formatter.deprecated(true);
            }
            formatter.appendText(getElementHandle().getName());
            if (isDeprecated) {
                formatter.deprecated(false);
            }
            appendTypeInfo(formatter, resolvedTypes);
            return formatter.getText();
        }

    }

    private static final class TokenSequenceIterator {

        private final List<TokenSequence<?>> list;
        private final boolean backward;

        private int index;

        public TokenSequenceIterator(List<TokenSequence<?>> list, boolean backward) {
            this.list = list;
            this.backward = backward;
            this.index = -1;
        }

        public boolean hasMore() {
            return backward ? hasPrevious() : hasNext();
        }

        public TokenSequence<?> getSequence() {
            assert index >= 0 && index < list.size() : "No sequence available, call hasMore() first."; //NOI18N
            return list.get(index);
        }

        private boolean hasPrevious() {
            boolean anotherSeq = false;

            if (index == -1) {
                index = list.size() - 1;
                anotherSeq = true;
            }

            for( ; index >= 0; index--) {
                TokenSequence<?> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveEnd();
                }

                if (seq.movePrevious()) {
                    return true;
                }

                anotherSeq = true;
            }

            return false;
        }

        private boolean hasNext() {
            boolean anotherSeq = false;

            if (index == -1) {
                index = 0;
                anotherSeq = true;
            }

            for( ; index < list.size(); index++) {
                TokenSequence<?> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveStart();
                }

                if (seq.moveNext()) {
                    return true;
                }

                anotherSeq = true;
            }

            return false;
        }
    }
}
