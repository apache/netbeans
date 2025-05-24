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
package org.netbeans.modules.javascript2.editor.doc;

import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.ClassElement;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.ir.VarNode;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.SyntaxProvider;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsElement.Kind;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.javascript2.model.api.Index;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationCompleter {

    public static final RequestProcessor RP = new RequestProcessor("JavaScript Documentation Completer", 1); //NOI18N

    public static void generateCompleteComment(Document doc, int caretOffset, int indent) {
        Runnable documentationGenerator = new DocumentationGenerator(doc, caretOffset, indent);
        RP.post(documentationGenerator);
    }

    private static class DocumentationGenerator implements Runnable {

        private final Document doc;
        private final int offset;
        private final int indent;

        public DocumentationGenerator(Document doc, int offset, int indent) {
            this.doc = doc;
            this.offset = offset;
            this.indent = indent;
        }

        @Override
        public void run() {
            try {
                ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        ParserResult parserResult = (ParserResult) resultIterator.getParserResult(offset);
                        if (parserResult instanceof JsParserResult jsParserResult) {
                            if (jsParserResult.getRoot() == null) {
                                // broken source
                                return;
                            }
                            int embeddedOffset = parserResult.getSnapshot().getEmbeddedOffset(offset);
                            Node nearestNode = getNearestNode(jsParserResult, embeddedOffset);
                            if (nearestNode == null) {
                                // no non-doc node found in the file
                                return;
                            }
                            int examinedOffset = nearestNode instanceof VarNode ? nearestNode.getStart() : nearestNode.getFinish();
                            int originalExaminedOffset = parserResult.getSnapshot().getOriginalOffset(examinedOffset);
                            if (originalExaminedOffset == -1) {
                                originalExaminedOffset = parserResult.getSnapshot().getOriginalOffset(nearestNode.getStart());
                            }
                            JsObject jsObject = findJsObjectFunctionVariable(Model.getModel(jsParserResult, false).getGlobalObject(), originalExaminedOffset);
                            assert jsObject != null;
                            if (jsObject.getJSKind() == Kind.FILE || isWrapperObject(jsParserResult, jsObject, nearestNode)) {
                                String fqn = getFqnName(jsParserResult, nearestNode);
                                jsObject = ModelUtils.findJsObjectByName(Model.getModel(jsParserResult, false), fqn);
                                // looks to be within anonymous object, use the global object instead
                                if (jsObject == null) {
                                    jsObject = Model.getModel(jsParserResult, false).getGlobalObject();
                                }
                            }
                            JsObject wrapperScope = getWrapperScope(jsParserResult, jsObject, nearestNode, originalExaminedOffset);
                            if (wrapperScope != null) {
                                if (nearestNode instanceof VarNode && wrapperScope instanceof JsFunction) {
                                    jsObject = ModelUtils.getJsObjectByName((JsFunction) wrapperScope, ((VarNode) nearestNode).getName().getName());
                                } else {
                                    jsObject = wrapperScope;
                                }
                            }
                            // when no code/object for doc comment found, generate just empty doc comment - issue #218945
                            if (jsObject == null) {
                                return;
                            }

                            // do not generate doc comment when the object offset is lower than the current caret offset
                            int originalStart = parserResult.getSnapshot().getOriginalOffset(jsObject.getOffsetRange().getStart());
                            if (originalStart != -1 && originalStart < offset) {
                                return;
                            }

                            if (isField(jsObject)) {
                                generateFieldComment(doc, offset, indent, jsParserResult, jsObject);
                            } else if (isFunction(jsObject)) {
                                generateFunctionComment(doc, offset, indent, jsParserResult, jsObject);
                            } else {
                                // object - generate field for now, could be cleverer
                                generateFieldComment(doc, offset, indent, jsParserResult, jsObject);
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static JsObject getWrapperScope(JsParserResult jsParserResult, JsObject jsObject, Node nearestNode, int offset) {
        JsObject result = null;
        if (jsObject instanceof JsFunction jsFunction) {
            result = jsObject;
            for (DeclarationScope declarationScope : jsFunction.getChildrenScopes()) {
                if (declarationScope instanceof JsFunction declarationFunction) {
                    if (declarationFunction.getOffsetRange(jsParserResult).containsInclusive(offset)) {
                        result = getWrapperScope(jsParserResult, declarationFunction, nearestNode, offset);
                    }
                }
            }
        }
        return result;
    }

    private static boolean isWrapperObject(JsParserResult jsParserResult, JsObject jsObject, Node nearestNode) {
        List<Identifier> nodeName = Model.getModel(jsParserResult, false).getNodeName(nearestNode);
        if (nodeName == null || nodeName.isEmpty()) {
            return false;
        }
        return jsObject.getProperties().containsKey(nodeName.get(nodeName.size() - 1).getName());
    }

    /**
     * Tries to get fully qualified name for given node.
     *
     * @param parserResult JavaScript parser results
     * @param node examined node for its FQN
     * @return fully qualified name of the node
     */
    public static String getFqnName(JsParserResult parserResult, Node node) {
        PathToNodeVisitor ptnv = new PathToNodeVisitor(node);
        FunctionNode root = parserResult.getRoot();
        root.accept(ptnv);
        StringBuilder fqn = new StringBuilder();
        for (Node currentNode : ptnv.getFinalPath()) {
            List<Identifier> name = Model.getModel(parserResult, false).getNodeName(currentNode);
            if (name != null) {
                for (Identifier identifier : name) {
                    fqn.append(".").append(identifier.getName()); //NOI18N
                }
            }
        }
        if (fqn.length() > 0) {
            return fqn.toString().substring(1);
        } else {
            return "";
        }
    }

    private static void generateFieldComment(Document doc, int offset, int indent, JsParserResult jsParserResult, JsObject jsObject) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();
        SyntaxProvider syntaxProvider = JsDocumentationSupport.getSyntaxProvider(jsParserResult);

        Collection<? extends TypeUsage> assignments = jsObject.getAssignments();
        Collection<TypeUsage> resolveTypes = ModelUtils.resolveTypes(assignments,
                Model.getModel(jsParserResult, false), Index.get(jsParserResult.getSnapshot().getSource().getFileObject()), true);
        StringBuilder types = new StringBuilder();
        for (TypeUsage typeUsage : resolveTypes) {
            // name and type are equivalent in the case of assigning parrametrs like "this.name = name"
            if (!typeUsage.getType().equals(jsObject.getName())) {
                types.append("|").append(ModelUtils.getDisplayName(typeUsage));
            }
        }
        String type = types.length() == 0 ? null : types.toString().substring(1);
        generateDocEntry(doc, toAdd, syntaxProvider.typeTagTemplate(), indent, null, type); //NOI18N

        doc.insertString(offset, toAdd.toString(), null);
    }

    private static void generateFunctionComment(Document doc, int offset, int indent, JsParserResult jsParserResult, JsObject jsObject) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();
        SyntaxProvider syntaxProvider = JsDocumentationSupport.getSyntaxProvider(jsParserResult);
        // TODO - could know constructors
        JsFunction function = ((JsFunction) jsObject);
        addParameters(doc, toAdd, syntaxProvider, indent, function.getParameters()); //NOI18N
        Collection<? extends TypeUsage> returnTypes = function.getReturnTypes();
        Collection<TypeUsage> types = ModelUtils.resolveTypes(returnTypes,
                Model.getModel(jsParserResult, false), Index.get(jsParserResult.getSnapshot().getSource().getFileObject()), true);
        if (types.isEmpty()) {
            if (hasReturnClause(jsParserResult, jsObject)) {
                addReturns(doc, toAdd, syntaxProvider, indent, Collections.singleton(new TypeUsage(Type.UNRESOLVED)));
            }
        } else {
            addReturns(doc, toAdd, syntaxProvider, indent, types);
        }

        doc.insertString(offset, toAdd.toString(), null);
    }

    private static boolean hasReturnClause(JsParserResult jsParserResult, JsObject jsObject) {
        OffsetRange offsetRange = jsObject.getOffsetRange();
        TokenHierarchy<?> tokenHierarchy = jsParserResult.getSnapshot().getTokenHierarchy();
        TokenSequence<? extends JsTokenId> ts = tokenHierarchy.tokenSequence(JsTokenId.javascriptLanguage());
        if (ts == null) {
            return false;
        }
        ts.move(offsetRange.getStart());
        if (!ts.moveNext() || !ts.movePrevious()) {
            return false;
        }

        while (ts.moveNext() && ts.offset() <= offsetRange.getEnd()) {
            if (ts.token().id() == JsTokenId.KEYWORD_RETURN) {
                return true;
            }
        }
        return false;
    }

    private static void addParameters(Document doc, StringBuilder toAdd, SyntaxProvider syntaxProvider, int indent, Collection<? extends JsObject> params) {
        for (JsObject jsObject : params) {
            generateDocEntry(doc, toAdd, syntaxProvider.paramTagTemplate(), indent, jsObject.getName(), null);
        }
    }

    private static void addReturns(Document doc, StringBuilder toAdd, SyntaxProvider syntaxProvider, int indent, Collection<? extends TypeUsage> returns) {
        StringBuilder sb = new StringBuilder();

        for (TypeUsage typeUsage : returns) {
            if (syntaxProvider.typesSeparator() == null) {
                // any first char which will be removed below
                sb.append(" ").append(typeUsage.getType()); //NOI18N
                break;
            } else {
                sb.append(syntaxProvider.typesSeparator()).append(typeUsage.getType());
            }
        }

        int separatorLength = syntaxProvider.typesSeparator() == null ? 1 : syntaxProvider.typesSeparator().length();
        String returnString = returns.isEmpty() ? "" : sb.toString().substring(separatorLength);
        generateDocEntry(doc, toAdd, syntaxProvider.returnTagTemplate(), indent, null, returnString);
    }

    private static void generateDocEntry(Document doc, StringBuilder toAdd, String template, int indent, String name, String type) {
        toAdd.append("\n"); //NOI18N
        toAdd.append(IndentUtils.createIndentString(doc, indent));

        toAdd.append("* "); //NOI18N
        toAdd.append(getProcessedTemplate(template, name, type));
    }

    private static String getProcessedTemplate(String template, String name, String type) {
        String finalTag = template;
        if (name != null) {
            finalTag = finalTag.replace(SyntaxProvider.NAME_PLACEHOLDER, name);
        }
        if (type != null) {
            finalTag = finalTag.replace(SyntaxProvider.TYPE_PLACEHOLDER, type);
        } else {
            finalTag = finalTag.replace(SyntaxProvider.TYPE_PLACEHOLDER, "type"); //NOI18N
        }
        return finalTag;
    }

    private static boolean isField(JsObject jsObject) {
        Kind kind = jsObject.getJSKind();
        return kind == Kind.FIELD || kind == Kind.VARIABLE || kind == Kind.PROPERTY;
    }

    private static boolean isFunction(JsObject jsObject) {
        return jsObject.getJSKind().isFunction();
    }

    /**
     * Gets the nearest next node for given offset.
     *
     * @param parserResult parser result of the JS file
     * @param offset offset where to start searching
     * @return {@code Node} which is the closest one
     */
    private static Node getNearestNode(JsParserResult parserResult, int offset) {
        FunctionNode root = parserResult.getRoot();
        NearestNodeVisitor offsetVisitor = new NearestNodeVisitor(offset);
        root.accept(offsetVisitor);
        return offsetVisitor.getNearestNode();
    }

    private static JsObject findJsObjectFunctionVariable(JsObject jsObject, int offset) {
        JsObject result = null;
        JsObject tmpObject = null;
        if (jsObject.getOffsetRange().containsInclusive(offset)) {
            result = jsObject;
            for (JsObject property : jsObject.getProperties().values()) {
                JsElement.Kind kind = property.getJSKind();
                if (kind == JsElement.Kind.OBJECT
                        || kind == JsElement.Kind.FUNCTION
                        || kind == JsElement.Kind.METHOD
                        || kind == JsElement.Kind.CONSTRUCTOR
                        || kind == JsElement.Kind.VARIABLE
                        || kind == JsElement.Kind.ARROW_FUNCTION) {
                    tmpObject = findJsObjectFunctionVariable(property, offset);
                }
                if (tmpObject != null) {
                    result = tmpObject;
                    break;
                }
            }
        }
        return result;
    }

    private static class NearestNodeVisitor extends PathNodeVisitor {

        private final int offset;
        private Node nearestNode = null;

        public NearestNodeVisitor(int offset) {
            this.offset = offset;
        }

        private void processNode(Node node) {
            if (offset < node.getStart() && (nearestNode == null || node.getStart() < nearestNode.getStart())) {
                nearestNode = node;
            }
        }

        public Node getNearestNode() {
            if (nearestNode instanceof AccessNode) {
                FarestIdentNodeVisitor farestNV = new FarestIdentNodeVisitor();
                nearestNode.accept(farestNV);
                return farestNV.getFarestNode();
            }
            return nearestNode;
        }

        @Override
        public boolean enterAccessNode(AccessNode accessNode) {
            processNode(accessNode);
            return super.enterAccessNode(accessNode);
        }

        @Override
        public boolean enterFunctionNode(FunctionNode functionNode) {
            if (functionNode.getKind() != FunctionNode.Kind.SCRIPT) {
                processNode(functionNode);
            }
            return super.enterFunctionNode(functionNode);
        }

        @Override
        public boolean enterPropertyNode(PropertyNode propertyNode) {
            processNode(propertyNode);
            return super.enterPropertyNode(propertyNode);
        }

        @Override
        public boolean enterClassElement(ClassElement classElement) {
            processNode(classElement);
            return super.enterClassElement(classElement);
        }

        @Override
        public boolean enterVarNode(VarNode varNode) {
            processNode(varNode);
            return super.enterVarNode(varNode);
        }

        @Override
        public boolean enterBinaryNode(BinaryNode binaryNode) {
            processNode(binaryNode);
            return super.enterBinaryNode(binaryNode);
        }
    }

    private static class FarestIdentNodeVisitor extends PathNodeVisitor {

        private Node farestNode;
        private final StringBuilder farestPath = new StringBuilder();

        @Override
        public boolean enterIdentNode(IdentNode identNode) {
            farestNode = identNode;
            farestPath.append(".").append(identNode.getName()); //NOI18N
            return super.enterIdentNode(identNode);
        }

        @Override
        public Node leaveIdentNode(IdentNode identNode) {
            farestNode = identNode;
            return super.leaveIdentNode(identNode);
        }

        public Node getFarestNode() {
            return farestNode;
        }

        public String getFarestFqn() {
            return farestPath.toString().substring(1);
        }
    }

    private static class PathToNodeVisitor extends PathNodeVisitor {

        private final Node finalNode;
        private List<? extends Node> finalPath;

        public PathToNodeVisitor(Node finalNode) {
            this.finalNode = finalNode;
        }

        @Override
        public void addToPath(Node node) {
            super.addToPath(node);
            if (node.equals(finalNode)) {
                finalPath = new LinkedList<Node>(getPath());
            }
        }

        public List<? extends Node> getFinalPath() {
            return finalPath;
        }
    }
}
