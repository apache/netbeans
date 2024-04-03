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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.micronaut.MicronautConfigProperties;
import org.netbeans.modules.micronaut.db.Utils;
import org.netbeans.modules.micronaut.expression.EvaluationContext;
import org.netbeans.modules.micronaut.expression.ExpressionTree;
import org.netbeans.modules.micronaut.expression.MicronautExpressionLanguageParser;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Dusan Balek
 */
public class MicronautExpressionLanguageCompletion {

    private static final String PATTERN = "(?:\\b[_a-zA-Z]|\\B\\$)[_$a-zA-Z0-9]*+";
    private final CompilationInfo info;
    private final EvaluationContext ctx;
    private final int startOffset;
    private final ExpressionTree tree;
    private final TokenSequence<?> ts;

    public MicronautExpressionLanguageCompletion(CompilationInfo info, EvaluationContext ctx, String text, int startOffset) {
        this.info = info;
        this.ctx = ctx;
        this.startOffset = startOffset;
        this.tree = new MicronautExpressionLanguageParser(text).parse();
        TokenHierarchy<String> th = TokenHierarchy.create(text, Language.find("text/x-micronaut-el"));
        this.ts = th.tokenSequence();
    }

    public <T> Result<T> query(int offset, ItemFactory<T> factory) {
        int anchorOffset = -1;
        List<T> items = new ArrayList<>();
        int d = ts.move(offset);
        if (d == 0 && ts.movePrevious() || ts.moveNext() || ts.isEmpty()) {
            List<String> kws = null;
            List<String> builtins = null;
            List<? extends Element> elements = null;
            List<ConfigurationMetadataProperty> properties = null;
            String prefix = "";
            boolean wrapProperties = true;
            String pkgPrefix = null;
            if (tree == null) {
                kws = ctx.getScope().getEnclosingMethod() != null ? Arrays.asList("true", "false", "null", "this", "empty", "not") : Arrays.asList("true", "false", "null", "empty", "not");
                builtins = Arrays.asList("T", "()", "ctx", "[]", "env", "[]");
                elements = ctx.getContextMethods();
                anchorOffset = startOffset + offset;
            } else {
                String tokenText = ts.token().text().subSequence(0, offset - ts.offset()).toString().trim();
                if (Pattern.matches(PATTERN, tokenText)) {
                    prefix = tokenText;
                    offset -= prefix.length();
                }
                anchorOffset = startOffset + offset;
                ExpressionTree.Path path = ExpressionTree.Path.get(tree, offset);
                while (path != null && path.getLeaf().getKind() == ExpressionTree.Kind.ERRONEOUS) {
                    path = path.getParentPath();
                }
                if (path == null) {
                    if (offset <= tree.getStartPosition()) {
                        kws = ctx.getScope().getEnclosingMethod() != null ? Arrays.asList("true", "false", "null", "this", "empty", "not") : Arrays.asList("true", "false", "null", "empty", "not");
                        builtins = Arrays.asList("T", "()", "ctx", "[]", "env", "[]");
                        elements = ctx.getContextMethods();
                    } else {
                        ExpressionTree lastTree = tree;
                        if (tree.getKind() == ExpressionTree.Kind.ERRONEOUS) {
                            for (ExpressionTree errTree : ((ExpressionTree.Erroneous) tree).getErrorTrees()) {
                                if (offset > errTree.getStartPosition()) {
                                    lastTree = errTree;
                                }
                            }
                        }
                        TypeMirror treeType = lastTree.getTypeMirror(ctx);
                        switch (treeType.getKind()) {
                            case BOOLEAN:
                                TypeMirror rtm = lastTree instanceof ExpressionTree.BinaryExpression
                                        ? ((ExpressionTree.BinaryExpression) lastTree).getRightOperand().getTypeMirror(ctx)
                                        : info.getTypes().getNoType(TypeKind.NONE);
                                switch (rtm.getKind()) {
                                    case INT:
                                    case LONG:
                                    case FLOAT:
                                    case DOUBLE:
                                        kws = Arrays.asList("and", "or", "div", "mod", "instanceof");
                                        break;
                                    default:
                                        kws = Arrays.asList("and", "or", "instanceof");
                                }
                                break;
                            case INT:
                            case LONG:
                            case FLOAT:
                            case DOUBLE:
                                kws = Arrays.asList("div", "mod", "instanceof");
                                break;
                            case DECLARED:
                                if ("java.lang.String".contentEquals(((TypeElement) ((DeclaredType) treeType).asElement()).getQualifiedName())) {
                                    kws = Arrays.asList("matches", "instanceof");
                                    break;
                                }
                            case ARRAY:
                                kws = Arrays.asList("instanceof");
                                break;
                            case NONE:
                                String prev = prevNonWSTokenText(prefix);
                                if ("#".equals(prev)) {
                                    elements = ctx.getContextMethods();
                                }
                                break;
                        }
                    }
                } else {
                    TypeMirror lastTreeType = null;
                    switch (path.getLeaf().getKind()) {
                        case STRING_LITERAL:
                            if (path.getParentPath() == null || path.getParentPath().getLeaf().getKind() != ExpressionTree.Kind.ENVIRONMENT_ACCESS) {
                                break;
                            }
                            String value = (String) ((ExpressionTree.Literal) path.getLeaf()).getValue();
                            if (value.startsWith(tokenText)) {
                                prefix = tokenText;
                                anchorOffset = startOffset + ts.offset();
                            }
                            wrapProperties = false;
                            path = path.getParentPath();
                        case ENVIRONMENT_ACCESS:
                            ExpressionTree.EnvironmentAccess ea = (ExpressionTree.EnvironmentAccess) path.getLeaf();
                            ExpressionTree propertyName = ea.getPropertyName();
                            if (propertyName.getKind() == ExpressionTree.Kind.ERRONEOUS || offset < propertyName.getEndPosition()) {
                                Project project = FileOwnerQuery.getOwner(info.getFileObject());
                                if (project != null) {
                                    properties = MicronautConfigProperties.getProperties(project).values().stream().filter(property -> !property.getId().contains("*")).collect(Collectors.toList());
                                }
                            }
                            break;
                        case BEAN_CONTEXT_ACCESS:
                            if (offset > ((ExpressionTree.BeanContextAccess) path.getLeaf()).getTypeReference().getEndPosition()) {
                                break;
                            }
                            builtins = Arrays.asList("T", "()");
                            path = new ExpressionTree.Path(path, ((ExpressionTree.BeanContextAccess) path.getLeaf()).getTypeReference());
                        case TYPE_REFERENCE:
                            ExpressionTree.TypeReference tr = (ExpressionTree.TypeReference) path.getLeaf();
                            int len = offset - tr.getTypeStartPosition();
                            if (len <= tr.getTypeName().length()) {
                                pkgPrefix = len >= 0 && tr.getTypeName().length() > len ? tr.getTypeName().substring(0, len) : tr.getTypeName();
                                PackageElement pkg = info.getElements().getPackageElement(pkgPrefix.isEmpty() ? "java.lang" : pkgPrefix.substring(0, pkgPrefix.length() - 1));
                                if (pkg != null) {
                                    elements = pkg.getEnclosedElements().stream().filter(e -> e.getKind().isClass() || e.getKind().isInterface()).collect(Collectors.toList());
                                }
                            }
                            break;
                        case PLUS:
                        case MINUS:
                        case MULTIPLY:
                        case DIVIDE:
                        case REMAINDER:
                        case POWER:
                        case GREATER_THAN:
                        case LESS_THAN:
                        case GREATER_THAN_EQUAL:
                        case LESS_THAN_EQUAL:
                        case MATCHES:
                            ExpressionTree.BinaryExpression binary = (ExpressionTree.BinaryExpression) path.getLeaf();
                            if (nextNonWSTokenCategory(prefix, binary.getRightOperand().getStartPosition()).startsWith("keyword.operator")) {
                                lastTreeType = binary.getLeftOperand().getTypeMirror(ctx);
                            } else {
                                if (ctx.getScope().getEnclosingMethod() != null) {
                                    kws = Arrays.asList("this");
                                }
                                builtins = Arrays.asList("T", "()", "ctx", "[]", "env", "[]");
                                elements = ctx.getContextMethods();
                            }
                            break;
                        case EQUAL_TO:
                        case NOT_EQUAL_TO:
                            binary = (ExpressionTree.BinaryExpression) path.getLeaf();
                            if (nextNonWSTokenCategory(prefix, binary.getRightOperand().getStartPosition()).startsWith("keyword.operator")) {
                                lastTreeType = binary.getLeftOperand().getTypeMirror(ctx);
                            } else {
                                kws = ctx.getScope().getEnclosingMethod() != null ? Arrays.asList("null", "this"): Arrays.asList("null");
                                builtins = Arrays.asList("T", "()", "ctx", "[]", "env", "[]");
                                elements = ctx.getContextMethods();
                            }
                            break;
                        case AND:
                        case OR:
                            binary = (ExpressionTree.BinaryExpression) path.getLeaf();
                            if (nextNonWSTokenCategory(prefix, binary.getRightOperand().getStartPosition()).startsWith("keyword.operator")) {
                                lastTreeType = binary.getLeftOperand().getTypeMirror(ctx);
                                break;
                            }
                        case NOT:
                            kws = ctx.getScope().getEnclosingMethod() != null ? Arrays.asList("true", "false", "not", "empty", "this"): Arrays.asList("true", "false", "not", "empty");
                            builtins = Arrays.asList("T", "()", "ctx", "[]", "env", "[]");
                            elements = ctx.getContextMethods();
                            break;
                        case EMPTY:
                            builtins = Arrays.asList("T", "()", "ctx", "[]", "env", "[]");
                            elements = ctx.getContextMethods();
                            break;
                        case INSTANCE_OF:
                            ExpressionTree.InstanceOf instanceOf = (ExpressionTree.InstanceOf) path.getLeaf();
                            if (nextNonWSTokenCategory(prefix, instanceOf.getType().getStartPosition()).startsWith("keyword.operator")) {
                                lastTreeType = instanceOf.getExpression().getTypeMirror(ctx);
                            } else {
                                builtins = Arrays.asList("T", "()");
                            }
                            break;
                        case TERNARY:
                            ExpressionTree.TernaryExpression ternary = (ExpressionTree.TernaryExpression) path.getLeaf();
                            String next = nextNonWSTokenCategory(prefix, ternary.getTrueExpression().getStartPosition());
                            if ("keyword.control.ternary.qmark.mexp".equals(next)) {
                                lastTreeType = ternary.getCondition().getTypeMirror(ctx);
                            } else {
                                String prev = prevNonWSTokenText(prefix);
                                if ("?". equals(prev) || ":".equals(prev)) {
                                    kws = ctx.getScope().getEnclosingMethod() != null ? Arrays.asList("true", "false", "null", "this", "empty", "not") : Arrays.asList("true", "false", "null", "empty", "not");
                                    builtins = Arrays.asList("T", "()", "ctx", "[]", "env", "[]");
                                    elements = ctx.getContextMethods();
                                }
                            }
                            break;
                        case PROPERTY_ACCESS:
                            String prev = prevNonWSTokenText(prefix);
                            if (".".equals(prev) || "?.".equals(prev) || "#".equals(prev)) {
                                ExpressionTree.PropertyAccess pa = (ExpressionTree.PropertyAccess) path.getLeaf();
                                ExpressionTree callee = pa.getCallee();
                                if (callee != null) {
                                    TypeMirror pacTM = callee.getTypeMirror(ctx);
                                    if (pacTM.getKind() == TypeKind.DECLARED) {
                                        elements = ElementFilter.methodsIn(((DeclaredType) pacTM).asElement().getEnclosedElements()).stream()
                                                .filter(ee -> callee.getKind() != ExpressionTree.Kind.TYPE_REFERENCE || ee.getModifiers().contains(Modifier.STATIC))
                                                .collect(Collectors.toList());
                                    }
                                } else {
                                    elements = ctx.getContextMethods();
                                }
                            }
                            break;
                        case METHOD_CALL:
                            prev = prevNonWSTokenText(prefix);
                            if (".".equals(prev) || "?.".equals(prev) || "#".equals(prev)) {
                                ExpressionTree.MethodCall methCall = (ExpressionTree.MethodCall) path.getLeaf();
                                ExpressionTree callee = methCall.getCallee();
                                if (callee != null) {
                                    TypeMirror methTM = callee.getTypeMirror(ctx);
                                    if (methTM.getKind() == TypeKind.DECLARED) {
                                        elements = ElementFilter.methodsIn(((DeclaredType) methTM).asElement().getEnclosedElements()).stream()
                                                .filter(ee -> callee.getKind() != ExpressionTree.Kind.TYPE_REFERENCE || ee.getModifiers().contains(Modifier.STATIC))
                                                .collect(Collectors.toList());
                                    }
                                } else {
                                    elements = ctx.getContextMethods();
                                }
                            } else if ("(".equals(prev) || ",".equals(prev)) {
                                kws = ctx.getScope().getEnclosingMethod() != null ? Arrays.asList("true", "false", "null", "this", "empty", "not") : Arrays.asList("true", "false", "null", "empty", "not");
                                builtins = Arrays.asList("T", "()", "ctx", "[]", "env", "[]");
                                elements = ctx.getContextMethods();
                            }
                            break;
                    }
                    if (lastTreeType != null) {
                        switch (lastTreeType.getKind()) {
                            case BOOLEAN:
                                TypeMirror rtm = info.getTypes().getNoType(TypeKind.NONE);
                                if (path.getLeaf().getKind() == ExpressionTree.Kind.TERNARY) {
                                    ExpressionTree.TernaryExpression ternary = (ExpressionTree.TernaryExpression) path.getLeaf();
                                    if (ternary.getCondition() instanceof ExpressionTree.BinaryExpression) {
                                        rtm = ((ExpressionTree.BinaryExpression) ternary.getCondition()).getRightOperand().getTypeMirror(ctx);
                                    }
                                }
                                switch (rtm.getKind()) {
                                    case INT:
                                    case LONG:
                                    case FLOAT:
                                    case DOUBLE:
                                        kws = Arrays.asList("and", "or", "div", "mod", "instanceof");
                                        break;
                                    default:
                                        kws = Arrays.asList("and", "or", "instanceof");
                                }
                                break;
                            case INT:
                            case LONG:
                            case FLOAT:
                            case DOUBLE:
                                ExpressionTree.Path parentPath = path.getParentPath();
                                TypeMirror ptm = parentPath != null && parentPath.getLeaf() instanceof ExpressionTree.BinaryExpression
                                        ? parentPath.getLeaf().getTypeMirror(ctx)
                                        : info.getTypes().getNoType(TypeKind.NONE);
                                if (ptm.getKind() == TypeKind.BOOLEAN) {
                                    kws = Arrays.asList("and", "or", "div", "mod", "instanceof");
                                } else {
                                    kws = Arrays.asList("div", "mod", "instanceof");
                                }
                                break;
                            case DECLARED:
                                if ("java.lang.String".contentEquals(((TypeElement) ((DeclaredType) lastTreeType).asElement()).getQualifiedName())) {
                                    kws = Arrays.asList("matches", "instanceof");
                                    break;
                                }
                            case ARRAY:
                                kws = Arrays.asList("instanceof");
                                break;
                        }
                    }
                }
            }
            if (kws != null) {
                for (String kw : kws) {
                    if (Utils.startsWith(kw, prefix)) {
                        items.add(factory.createKeywordItem(kw, anchorOffset));
                    }
                }
            }
            if (builtins != null) {
                for (int j = 0; j < builtins.size(); j += 2) {
                    if (Utils.startsWith(builtins.get(j), prefix)) {
                        items.add(factory.createBuiltInItem(builtins.get(j), builtins.get(j + 1), anchorOffset));
                    }
                }
            }
            if (elements != null) {
                for (Element element : elements) {
                    String name = element.getSimpleName().toString();
                    if (element.getKind() == ElementKind.METHOD) {
                        TypeMirror enclType = element.getEnclosingElement().asType();
                        if (enclType.getKind() == TypeKind.DECLARED) {
                            if (Utils.startsWith(name, prefix) && info.getTrees().isAccessible(ctx.getScope(), element, (DeclaredType) enclType)) {
                                items.add(factory.createJavaElementItem(info, element, anchorOffset));
                            }
                            String propertyName = element.getKind() == ElementKind.METHOD ? ExpressionTree.getPropertyName((ExecutableElement) element) : null;
                            if (Utils.startsWith(propertyName, prefix) && info.getTrees().isAccessible(ctx.getScope(), element, (DeclaredType) enclType)) {
                                String returnType = Utils.getTypeName(info, ((ExecutableElement)element).getReturnType(), false, false).toString();
                                items.add(factory.createBeanPropertyItem(propertyName, returnType, anchorOffset));
                            }
                        }
                    } else {
                        if (Utils.startsWith(name, prefix) && info.getTrees().isAccessible(ctx.getScope(), (TypeElement) element)) {
                            items.add(factory.createJavaElementItem(info, element, anchorOffset));
                        }
                    }
                }
            }
            if (properties != null) {
                for (ConfigurationMetadataProperty property : properties) {
                    if (Utils.startsWith(property.getId(), prefix)) {
                        items.add(factory.createEnvPropertyItem(wrapProperties ? "'" + property.getId() + "'" : property.getId(), new MicronautConfigDocumentation(property).getText(), anchorOffset, startOffset + offset));
                    }
                }
            }
            if (pkgPrefix != null) {
                Set<String> seenPkgs = new HashSet<>();
                ModuleElement module = info.getElements().getModuleOf(ctx.getScope().getEnclosingClass());
                for (String pkgName : info.getClasspathInfo().getClassIndex().getPackageNames(pkgPrefix, false, EnumSet.allOf(ClassIndex.SearchScope.class))) {
                    if (Utils.startsWith(pkgName, pkgPrefix + prefix) && (module != null ? info.getElements().getPackageElement(module, pkgName) : info.getElements().getPackageElement(pkgName)) != null) {
                        String name = pkgName.substring(pkgPrefix.length());
                        int idx = name.indexOf('.');
                        if (idx > 0) {
                            name = name.substring(0, idx);
                        }
                        if (seenPkgs.add(name)) {
                            items.add(factory.createPackageItem(name, anchorOffset));
                        }
                    }
                }
            }
        }
        return new Result<>(items, anchorOffset);
    }

    private String prevNonWSTokenText(String prefix) {
        int idx = ts.index();
        try {
            if (prefix.isEmpty() || ts.movePrevious()) {
                if (ts.token().text().toString().trim().isEmpty()) {
                    ts.movePrevious();
                }
            }
            return ts.token().text().toString();
        } finally {
            while (ts.index() > idx) {
                ts.movePrevious();
            }
        }
    }

    private String nextNonWSTokenCategory(String prefix, int upToOffset) {
        int idx = ts.index();
        try {
            if ((!prefix.isEmpty() || ts.moveNext()) && ts.offset() < upToOffset) {
                List<String> categories = (List<String>) ts.token().getProperty("categories");
                if (categories != null && categories.size() > 1) {
                    return categories.get(categories.size() - 1);
                }
            }
            return "";
        } finally {
            while (ts.index() > idx) {
                ts.movePrevious();
            }
        }
    }

    public static interface ItemFactory<T> {
        T createKeywordItem(String name, int offset);
        T createBuiltInItem(String name, String parenPair, int offset);
        T createPackageItem(String name, int offset);
        T createBeanPropertyItem(String name, String typeName, int offset);
        T createEnvPropertyItem(String name, String documentation, int anchorOffset, int offset);
        T createJavaElementItem(CompilationInfo info, Element element, int offset);
    }

    public static class Result<T> {

        private final List<T> items;
        private final int anchorOffset;

        private Result(List<T> items, int anchorOffset) {
            this.items = items;
            this.anchorOffset = anchorOffset;
        }

        public List<T> getItems() {
            return items;
        }

        public int getAnchorOffset() {
            return anchorOffset;
        }
    }
}
