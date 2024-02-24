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
package org.netbeans.modules.micronaut.completion;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeUtilities;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletion;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet;
import org.netbeans.modules.micronaut.db.Utils;
import org.netbeans.modules.micronaut.expression.EvaluationContext;
import org.netbeans.modules.micronaut.expression.MicronautExpressionLanguageParser;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class MicronautDataCompletionTask {

    private static final String JPA_REPOSITORY_ANNOTATION_NAME = "io.micronaut.data.annotation.Repository";
    private static final String JDBC_REPOSITORY_ANNOTATION_NAME = "io.micronaut.data.jdbc.annotation.JdbcRepository";
    private static final String REPOSITORY_TYPE_NAME = "io.micronaut.data.repository.GenericRepository";
    private static final String CONTROLLER_ANNOTATION_NAME = "io.micronaut.http.annotation.Controller";
    private static final String QUERY_ANNOTATION_TYPE_NAME = "io.micronaut.data.annotation.Query";
    private static final String GET = "get";
    private static final List<String> QUERY_PATTERNS = new ArrayList<>(Arrays.asList("find", "get", "query", "read", "retrieve", "search"));
    private static final List<String> SPECIAL_QUERY_PATTERNS = new ArrayList<>(Arrays.asList("count", "countDistinct", "delete", "exists", "update"));
    private static final List<String> QUERY_PROJECTIONS = new ArrayList<>(Arrays.asList("", "Avg", "Distinct", "Max", "Min", "Sum"));
    private static final List<String> CRITERION_EXPRESSIONS = new ArrayList<>(Arrays.asList("", "After", "Before", "Contains", "StartingWith", "StartsWith", "EndingWith", "EndsWith",
            "Equal", "Equals", "NotEqual", "NotEquals", "GreaterThan", "GreaterThanEquals", "LessThan", "LessThanEquals", "Like", "Ilike", "In", "InList", "InRange", "Between",
            "IsNull", "IsNotNull", "IsEmpty", "IsNotEmpty", "True", "False"));
    private static final List<String> COMPOSE_EXPRESSIONS = new ArrayList<>(Arrays.asList("And", "Or"));
    private static final String BY = "By";
    private static final String ORDER_BY = "OrderBy";
    private static final String COUNT = "count";
    private static final String EXISTS = "exists";
    private static final String EMPTY = "";

    private int anchorOffset;

    public static interface ItemFactory<T> extends MicronautExpressionLanguageCompletion.ItemFactory<T> {
        T createControllerMethodItem(CompilationInfo info, VariableElement delegateRepository, ExecutableElement delegateMethod, String controllerId, String id, int offset);
        T createFinderMethodItem(String name, String returnType, int offset);
        T createFinderMethodNameItem(String prefix, String name, int offset);
        T createSQLItem(CompletionItem item);
    }

    public <T> List<T> query(Document doc, int caretOffset, ItemFactory<T> factory) {
        List<T> items = new ArrayList<>();
        try {
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController cc = CompilationController.get(resultIterator.getParserResult(caretOffset));
                    if (cc != null) {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        anchorOffset = caretOffset;
                        String prefix = EMPTY;
                        TokenSequence<JavaTokenId> ts = cc.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                        if (ts.move(anchorOffset) == 0 || !ts.moveNext()) {
                            ts.movePrevious();
                        }
                        int len = anchorOffset - ts.offset();
                        if (len > 0 && ts.token().length() >= len) {
                            if (ts.token().id() == JavaTokenId.IDENTIFIER || ts.token().id().primaryCategory().startsWith("keyword") ||
                                     ts.token().id().primaryCategory().equals("literal")) {
                                prefix = ts.token().text().toString().substring(0, len);
                                anchorOffset = ts.offset();
                            } else if (ts.token().id() == JavaTokenId.STRING_LITERAL) {
                                prefix = ts.token().text().toString().substring(1, ts.token().length() - 1);
                                anchorOffset = ts.offset() + 1;
                            } else if (ts.token().id() == JavaTokenId.MULTILINE_STRING_LITERAL) {
                                prefix = ts.token().text().toString().substring(3, len);
                                anchorOffset = ts.offset() + 3;
                            }
                        }
                        Consumer consumer = (namePrefix, name, type) -> {
                            items.add(type != null ? factory.createFinderMethodItem(name, type, anchorOffset) : factory.createFinderMethodNameItem(namePrefix, name, anchorOffset));
                        };
                        TreeUtilities treeUtilities = cc.getTreeUtilities();
                        SourcePositions sp = cc.getTrees().getSourcePositions();
                        TreePath path = treeUtilities.pathFor(anchorOffset);
                        switch (path.getLeaf().getKind()) {
                            case CLASS:
                            case INTERFACE:
                                int startPos = (int) sp.getEndPosition(cc.getCompilationUnit(), ((ClassTree) path.getLeaf()).getModifiers());
                                if (startPos <= 0) {
                                    startPos = (int) sp.getStartPosition(cc.getCompilationUnit(), path.getLeaf());
                                }
                                String headerText = cc.getText().substring(startPos, anchorOffset);
                                int idx = headerText.indexOf('{'); //NOI18N
                                if (idx >= 0) {
                                    resolveFinderMethods(cc, path, prefix, true, consumer);
                                    items.addAll(resolveControllerMethods(cc, path, prefix, factory));
                                }
                                break;
                            case METHOD:
                                Tree returnType = ((MethodTree) path.getLeaf()).getReturnType();
                                if (returnType != null && findLastNonWhitespaceToken(ts, (int) sp.getEndPosition(path.getCompilationUnit(), returnType), anchorOffset) == null) {
                                    resolveFinderMethods(cc, path.getParentPath(), prefix, false, consumer);
                                }
                                break;
                            case VARIABLE:
                                Tree type = ((VariableTree) path.getLeaf()).getType();
                                if (type != null && findLastNonWhitespaceToken(ts, (int) sp.getEndPosition(path.getCompilationUnit(), type), anchorOffset) == null) {
                                    TreePath parentPath = path.getParentPath();
                                    if (parentPath.getLeaf().getKind() == Tree.Kind.CLASS || parentPath.getLeaf().getKind() == Tree.Kind.INTERFACE) {
                                        resolveFinderMethods(cc, parentPath, prefix, false, consumer);
                                    }
                                }
                                break;
                            case STRING_LITERAL:
                                if (path.getParentPath().getLeaf().getKind() == Tree.Kind.ASSIGNMENT && path.getParentPath().getParentPath().getLeaf().getKind() == Tree.Kind.ANNOTATION) {
                                    items.addAll(resolveExpressionLanguage(cc, path.getParentPath(), prefix, caretOffset - anchorOffset, factory));
                                    for (CompletionItem item : resolveQueryAnnotation(cc, path.getParentPath().getParentPath(), prefix, caretOffset - anchorOffset)) {
                                        items.add(factory.createSQLItem(item));
                                    }
                                }
                                break;
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return items;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    private <T> List<T> resolveExpressionLanguage(CompilationInfo info, TreePath path, String prefix, int off, MicronautExpressionLanguageCompletion.ItemFactory<T> factory) {
        Matcher matcher = MicronautExpressionLanguageParser.MEXP_PATTERN.matcher(prefix);
        while (matcher.find() && matcher.groupCount() == 1) {
            if (off >= matcher.start(1) && off <= matcher.end(1)) {
                EvaluationContext ctx = EvaluationContext.get(info, path);
                if (ctx != null) {
                    MicronautExpressionLanguageCompletion completion = new MicronautExpressionLanguageCompletion(info, ctx, matcher.group(1), anchorOffset + matcher.start(1));
                    MicronautExpressionLanguageCompletion.Result<T> result = completion.query(off - matcher.start(1), factory);
                    int newOffset = result.getAnchorOffset();
                    if (newOffset >= 0) {
                        this.anchorOffset = newOffset;
                    }
                    return result.getItems();
                }
            }
        }
        return Collections.emptyList();
    }

    private List<CompletionItem> resolveQueryAnnotation(CompilationInfo info, TreePath path, String prefix, int off) {
        Element el = info.getTrees().getElement(path);
        if (el instanceof TypeElement) {
            if (QUERY_ANNOTATION_TYPE_NAME.contentEquals(((TypeElement) el).getQualifiedName())) {
                TreePath clsPath = info.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
                if (clsPath != null && Utils.getAnnotation(info.getTrees().getElement(clsPath).getAnnotationMirrors(), JDBC_REPOSITORY_ANNOTATION_NAME) != null) {
                    SQLCompletionContext ctx = SQLCompletionContext.empty()
                            .setStatement(prefix)
                            .setOffset(off)
                            .setDatabaseConnection(ConnectionManager.getDefault().getPreferredConnection(true));
                    SQLCompletion completion = SQLCompletion.create(ctx);
                    SQLCompletionResultSet resultSet = SQLCompletionResultSet.create();
                    completion.query(resultSet, (component, offset, text) -> {
                        final int caretOffset = component.getCaretPosition();
                        final StyledDocument document = (StyledDocument) component.getDocument();
                        try {
                            NbDocument.runAtomicAsUser(document, () -> {
                                try {
                                    int documentOffset = anchorOffset + offset;
                                    document.remove(documentOffset, caretOffset - documentOffset);
                                    document.insertString(documentOffset, text.replace("\"", "\\\""), null);
                                } catch (BadLocationException ex) {
                                }
                            });
                        } catch (BadLocationException ex) {
                        }
                    });
                    int newOffset = resultSet.getAnchorOffset();
                    if (newOffset >= 0) {
                        this.anchorOffset = newOffset;
                    }
                    return resultSet.getItems();
                }
            }
        }
        return Collections.emptyList();
    }

    private <T> List<T> resolveControllerMethods(CompilationController cc, TreePath path, String prefix, ItemFactory<T> factory) {
        List<T> items = new ArrayList<>();
        TypeElement te = (TypeElement) cc.getTrees().getElement(path);
        AnnotationMirror controllerAnn = Utils.getAnnotation(te.getAnnotationMirrors(), CONTROLLER_ANNOTATION_NAME);
        if (controllerAnn != null) {
            List<VariableElement> repositories = Utils.getRepositoriesFor(cc, te);
            if (!repositories.isEmpty()) {
                Utils.collectMissingDataEndpoints(cc, te, prefix, (repository, delegateMethod, controllerId, id) -> {
                    T item = factory.createControllerMethodItem(cc, repository, delegateMethod, controllerId, id, anchorOffset);
                    if (item != null) {
                        items.add(item);
                    }
                });
            }
        }
        return items;
    }

    private void resolveFinderMethods(CompilationInfo info, TreePath path, String prefix, boolean full, Consumer consumer) throws IOException {
        TypeUtilities tu = info.getTypeUtilities();
        TypeElement entity = getEntityFor(info, path);
        if (entity != null) {
            Map<String, String> prop2Types = new HashMap<>();
            for (ExecutableElement method : ElementFilter.methodsIn(entity.getEnclosedElements())) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith(GET) && methodName.length() > 3 && method.getParameters().isEmpty()) {
                    TypeMirror type = method.getReturnType();
                    if (type.getKind() != TypeKind.ERROR) {
                        methodName = methodName.substring(GET.length());
                        methodName = methodName.substring(0, 1).toUpperCase(Locale.ENGLISH) + methodName.substring(1);
                        prop2Types.put(methodName, tu.getTypeName(type).toString());
                    }
                }
            }
            for (RecordComponentElement recordComponent : ElementFilter.recordComponentsIn(entity.getEnclosedElements())) {
                TypeMirror type = recordComponent.asType();
                if (type.getKind() != TypeKind.ERROR) {
                    String name = recordComponent.getSimpleName().toString();
                    name = name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
                    prop2Types.put(name, tu.getTypeName(type).toString());
                }
            }
            addFindByCompletions(entity, prop2Types, prefix, full, consumer);
        }
    }

    private void addFindByCompletions(TypeElement entity, Map<String, String> prop2Types, String prefix, boolean full, Consumer consumer) {
        for (String pattern : QUERY_PATTERNS) {
            String name = pattern + BY;
            if (prefix.length() >= name.length() && prefix.startsWith(name)) {
                addPropertyCriterionCompletions(prop2Types, name, prefix, consumer);
            } else if (Utils.startsWith(name, prefix)) {
                consumer.accept(EMPTY, name, full ? entity.getSimpleName().toString() : null);
            }
            for (String projection : QUERY_PROJECTIONS) {
                for (String propName : prop2Types.keySet()) {
                    name = pattern + projection + propName + BY;
                    if (prefix.length() >= name.length() && prefix.startsWith(name)) {
                        addPropertyCriterionCompletions(prop2Types, name, prefix, consumer);
                    } else if (Utils.startsWith(name, prefix)) {
                        consumer.accept(EMPTY, name, full ? prop2Types.get(propName) : null);
                    }
                }
            }
        }
        for (String pattern : SPECIAL_QUERY_PATTERNS) {
            for (String propName : prop2Types.keySet()) {
                String name = pattern + propName + BY;
                if (prefix.length() >= name.length() && prefix.startsWith(name)) {
                    addPropertyCriterionCompletions(prop2Types, name, prefix, consumer);
                } else if (Utils.startsWith(name, prefix)) {
                    consumer.accept(EMPTY, name, full ? name.startsWith(COUNT) ? "int" : name.startsWith(EXISTS) ? "boolean" : "void" : null);
                }
            }
        }
    }

    private void addPropertyCriterionCompletions(Map<String, String> prop2Types, String namePrefix, String prefix, Consumer consumer) {
        for (String propName : prop2Types.keySet()) {
            for (String criterion : CRITERION_EXPRESSIONS) {
                String name = propName + criterion;
                if (prefix.length() >= namePrefix.length() + name.length() && prefix.startsWith(namePrefix + name)) {
                    addComposeAndOrderCompletions(prop2Types, namePrefix + name, prefix, consumer);
                } else if (Utils.startsWith(namePrefix + name, prefix)) {
                    consumer.accept(namePrefix, name, null);
                }
            }
        }
    }

    private void addComposeAndOrderCompletions(Map<String, String> prop2Types, String namePrefix, String prefix, Consumer consumer) {
        for (String name : COMPOSE_EXPRESSIONS) {
            if (prefix.length() >= namePrefix.length() + name.length() && prefix.startsWith(namePrefix + name)) {
                addPropertyCriterionCompletions(prop2Types, namePrefix + name, prefix, consumer);
            } else if (Utils.startsWith(namePrefix + name, prefix)) {
                consumer.accept(namePrefix, name, null);
            }
        }
        for (String propName : prop2Types.keySet()) {
            String name = ORDER_BY + propName;
            if (prefix.length() < namePrefix.length() + name.length() && Utils.startsWith(namePrefix + name, prefix)) {
                consumer.accept(namePrefix, name, null);
            }
        }
    }

    private static TypeElement getEntityFor(CompilationInfo info, TreePath path) {
        TypeElement te = (TypeElement) info.getTrees().getElement(path);
        if (te.getModifiers().contains(Modifier.ABSTRACT)) {
            if (Utils.getAnnotation(te.getAnnotationMirrors(), JPA_REPOSITORY_ANNOTATION_NAME) != null) {
                Types types = info.getTypes();
                TypeMirror repositoryType = types.erasure(info.getElements().getTypeElement(REPOSITORY_TYPE_NAME).asType());
                for (TypeMirror iface : te.getInterfaces()) {
                    if (iface.getKind() == TypeKind.DECLARED && types.isSubtype(types.erasure(iface), repositoryType)) {
                        List<? extends TypeMirror> typeArguments = ((DeclaredType) iface).getTypeArguments();
                        if (!typeArguments.isEmpty()) {
                            TypeMirror entityType = typeArguments.get(0);
                            if (entityType != null && entityType.getKind() == TypeKind.DECLARED) {
                                return (TypeElement) ((DeclaredType) entityType).asElement();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static TokenSequence<JavaTokenId> findLastNonWhitespaceToken(TokenSequence<JavaTokenId> ts, int startPos, int endPos) {
        ts.move(endPos);
        TokenSequence<JavaTokenId> last = previousNonWhitespaceToken(ts);
        if (last == null || last.offset() < startPos) {
            return null;
        }
        return last;
    }

    private static TokenSequence<JavaTokenId> previousNonWhitespaceToken(TokenSequence<JavaTokenId> ts) {
        while (ts.movePrevious()) {
            switch (ts.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case JAVADOC_COMMENT:
                    break;
                default:
                    return ts;
            }
        }
        return null;
    }

    @FunctionalInterface
    private static interface Consumer {
        void accept(String namePrefix, String name, String type);
    }
}
