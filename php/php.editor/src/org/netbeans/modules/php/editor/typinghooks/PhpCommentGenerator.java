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

package org.netbeans.modules.php.editor.typinghooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ThrowExpression;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.Variadic;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public final class PhpCommentGenerator {

    static final RequestProcessor RP = new RequestProcessor("Generating Bracket Completer", 1); //NOI18N
    static final String TYPE_PLACEHOLDER = "type"; // NOI18N

    private PhpCommentGenerator() {
    }

    static void generateDocTags(final BaseDocument doc, final int offset, final int indent) {
        Runnable docTagsGenerator = new DocTagsGenerator(doc, offset, indent);
        RP.post(docTagsGenerator);
    }

    private static void generateFunctionDoc(BaseDocument doc, int offset, int indent, ParserResult info, FunctionDeclaration decl) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();
        ScannerImpl i = new ScannerImpl(info, decl);

        i.scan(decl);

        addVariables(doc, toAdd, "@global", indent, i.globals);
        addVariables(doc, toAdd, "@staticvar", indent, i.staticvars);
        addVariables(doc, toAdd, "@param", indent, i.params);

        if (i.hasReturn) {
            generateDocEntry(doc, toAdd, "@return", indent, null, i.getReturnType()); // NOI18N
        }

        addVariables(doc, toAdd, "@throws", indent, i.throwsExceptions);

        doc.insertString(offset, toAdd.toString(), null);
    }

    private static void addVariables(BaseDocument doc, StringBuilder toAdd, String text, int indent, List<Pair<String, String>> vars) {
        for (Pair<String, String> p : vars) {
            generateDocEntry(doc, toAdd, text, indent, p.first(), p.second());
        }
    }

    private static void generateDocEntry(BaseDocument doc, StringBuilder toAdd, String text, int indent, String name, String type) {
        toAdd.append("\n");
        toAdd.append(IndentUtils.createIndentString(doc, indent));

        toAdd.append(" * ");
        toAdd.append(text);
        String returnType = convertThisReturnType(type);
        if (returnType != null && !returnType.isEmpty()) {
            toAdd.append(" ");
            toAdd.append(returnType);
        } else {
            toAdd.append(" ");
            toAdd.append(TYPE_PLACEHOLDER);
        }
        if (name != null) {
            toAdd.append(" ");
            toAdd.append(name);
        }
    }

    /**
     * Convert \this to $this.
     * @param returnTypes return types separated by "|"
     * @return converted return types
     */
    private static String convertThisReturnType(String returnTypes) {
        if (StringUtils.isEmpty(returnTypes)) {
            return ""; //NOI18N
        }
        StringBuilder sb = new StringBuilder(returnTypes.length());
        boolean first = true;
        for (String typeName : returnTypes.split("\\" + Type.SEPARATOR)) { //NOI18N
            if (first) {
                first = false;
            } else {
                sb.append(Type.SEPARATOR);
            }
            if (typeName.equals("\\this")) { //NOI18N
                sb.append("$this"); //NOI18N
            } else {
                sb.append(typeName);
            }
        }
        return sb.toString();
    }

    private static void generateGlobalVariableDoc(BaseDocument doc, int offset, int indent, String indexName, String type) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();

        generateDocEntry(doc, toAdd, "@global", indent, "$GLOBALS['" + indexName + "']", type);
        toAdd.append("\n").append(IndentUtils.createIndentString(doc, indent));
        toAdd.append(" * ").append("@name $").append(indexName);

        doc.insertString(offset - 1, toAdd.toString(), null);
    }

    private static void generateFieldDoc(BaseDocument doc, int offset, int indent, ParserResult info, FieldsDeclaration decl) throws BadLocationException {
        StringBuilder toAdd = new StringBuilder();
        Expression fieldType = decl.getFieldType();
        String type = null;
        if (fieldType != null) {
            type = getDeclaredTypes(fieldType);
        }
        generateDocEntry(doc, toAdd, "@var", indent, null, type);

        doc.insertString(offset, toAdd.toString(), null);
    }

    private static String getDeclaredTypes(Expression declaredType) {
        String typeName;
        if (declaredType instanceof UnionType) {
            typeName = VariousUtils.getUnionType((UnionType) declaredType);
        } else if (declaredType instanceof IntersectionType) {
            typeName = VariousUtils.getIntersectionType((IntersectionType) declaredType);
        } else {
            QualifiedName name = QualifiedName.create(declaredType);
            assert name != null : declaredType;
            typeName = name.toString();
        }
        if (declaredType instanceof NullableType) {
            typeName = typeName + Type.SEPARATOR + Type.NULL;
        }
        return typeName;
    }

    private static class ScannerImpl extends DefaultVisitor {
        private final List<Pair<String, String>> globals = new LinkedList<>();
        private final List<Pair<String, String>> staticvars = new LinkedList<>();
        private final List<Pair<String, String>> params = new LinkedList<>();
        private final List<Pair<String, String>> throwsExceptions = new LinkedList<>();
        private final List<String> usedThrows = new LinkedList<>();
        final Set<VariableName> declaredVariables = new HashSet<>();
        private boolean hasReturn;
        private final FunctionDeclaration decl;
        private final FunctionScope fnc;
        private Collection<? extends UseScope> declaredUses;

        public ScannerImpl(ParserResult info, FunctionDeclaration decl) {
            if (info instanceof PHPParseResult) {
                PHPParseResult parseResult = (PHPParseResult) info;
                Model model = parseResult.getModel();
                final VariableScope variableScope = model.getVariableScope(decl.getEndOffset() - 1);
                NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), decl.getEndOffset() - 1);
                if (namespaceScope != null) {
                    declaredUses = namespaceScope.getAllDeclaredSingleUses();
                }
                if (variableScope instanceof FunctionScope) {
                    fnc = (FunctionScope) variableScope;
                    declaredVariables.addAll(fnc.getDeclaredVariables());
                } else {
                    fnc = null;
                }
            } else {
                fnc = null;
            }
            this.decl = decl;
        }

        public String getReturnType() {
            // if type is already declared, just use it
            if (decl.getReturnType() != null) {
                Expression returnType = decl.getReturnType();
                return getDeclaredTypes(returnType);
            }

            StringBuilder type = new StringBuilder();
            if (hasReturn) {
                Collection<? extends String> typeNames = fnc.getReturnTypeNames();
                String item;
                String resolvedItem;
                for (Iterator<String> i = (Iterator<String>) typeNames.iterator(); i.hasNext();) {
                    item = i.next();
                    if (VariousUtils.isSemiType(item)) {
                        break;
                    }
                    resolvedItem = resolveProperType(item);
                    type = type.toString().isEmpty() ? type.append(resolvedItem) : type.append(Type.SEPARATOR).append(resolvedItem);
                }
            }
            return type.toString();
        }


        @Override
        public void scan(ASTNode node) {
            if (fnc != null) {
                super.scan(node);
            }
        }

        @Override
        public void visit(final FormalParameter p) {
            String name = "";
            Expression expr = p.getParameterName();
            Variable var = null;
            if (expr instanceof Reference) {
                 expr = ((Reference) expr).getExpression();
            }
            if (expr instanceof Variadic) {
                expr = ((Variadic) expr).getExpression();
            }
            if (expr instanceof Variable) {
                var = (Variable) expr;
            }
            if (var != null && var.getName() instanceof Identifier) {
                name = ((Identifier) var.getName()).getName();
            }
            if (name != null) {
                Expression parameterType = p.getParameterType();
                if (parameterType != null) {
                    // if type is already declared, just use it
                    params.add(Pair.of("$" + name, getDeclaredTypes(parameterType))); // NOI18N
                } else {
                    for (VariableName variable : ElementFilter.forName(NameKind.exact(name)).filter(declaredVariables)) {
                        final Collection<? extends String> typeNames = variable.getTypeNames(variable.getNameRange().getEnd());
                        final List<String> resolvedTypeNames = new ArrayList<>();
                        typeNames.forEach(typeName -> resolvedTypeNames.add(resolveProperType(typeName)));
                        String type = typeNames.isEmpty() ? null : Type.asUnionType(resolvedTypeNames);
                        if (VariousUtils.isSemiType(type)) {
                            type = null;
                        }
                        params.add(Pair.of(variable.getName(), type));
                    }
                }
            }
            super.visit(p);
        }

        private String resolveProperType(String type) {
            String typeName = type;
            boolean isNullableType = CodeUtils.isNullableType(typeName);
            if (isNullableType) {
                typeName = typeName.substring(1);
            }
            if (declaredUses != null && typeName != null) {
                QualifiedName qualifiedType = QualifiedName.create(typeName);
                for (UseScope useScope : declaredUses) {
                    QualifiedName qualifiedUseScope = QualifiedName.create(useScope.getName());
                    if (QualifiedName.create(true, qualifiedUseScope.getSegments()).equals(qualifiedType)) {
                        AliasedName aliasedName = useScope.getAliasedName();
                        if (aliasedName != null) {
                            typeName = aliasedName.getAliasName();
                        } else {
                            typeName = qualifiedUseScope.getName();
                        }
                        break;
                    }
                }
            }
            if (isNullableType) {
                return typeName + Type.SEPARATOR + Type.NULL;
            }
            return typeName;
        }

        @Override
        public void visit(GlobalStatement node) {
            for (Variable v : node.getVariables()) {
                final String name = CodeUtils.extractVariableName(v);
                if (name != null) {
                    for (VariableName variable : ElementFilter.forName(NameKind.exact(name)).filter(declaredVariables)) {
                        final Collection<? extends String> typeNames = variable.getTypeNames(variable.getNameRange().getEnd());
                        String type = typeNames.isEmpty() ? null : typeNames.iterator().next();
                        if (VariousUtils.isSemiType(type)) {
                            type = null;
                        }
                        globals.add(Pair.of(variable.getName(), resolveProperType(type)));
                    }
                }
            }

            super.visit(node);
        }

        @Override
        public void visit(LambdaFunctionDeclaration declaration) {
            // do not scan internal functions
        }

        @Override
        public void visit(ReturnStatement node) {
            hasReturn = true;
        }

        @Override
        public void visit(StaticStatement node) {
            for (Variable v : node.getVariables()) {
                final String name = CodeUtils.extractVariableName(v);
                if (name != null) {
                    for (VariableName variable : ElementFilter.forName(NameKind.exact(name)).filter(declaredVariables)) {
                        final Collection<? extends String> typeNames = variable.getTypeNames(variable.getNameRange().getEnd());
                        String type = typeNames.isEmpty() ? null : typeNames.iterator().next();
                        if (VariousUtils.isSemiType(type)) {
                            type = null;
                        }
                        staticvars.add(Pair.of(variable.getName(), resolveProperType(type)));
                    }
                }
            }

            super.visit(node);
        }

        @Override
        public void visit(ThrowExpression node) {
            String type = getTypeFromThrowExpression(node);
            if (!usedThrows.contains(type)) {
                usedThrows.add(type);
                throwsExceptions.add(Pair.of(null, resolveProperType(type)));
            }
            super.visit(node);
        }

        private String getTypeFromThrowExpression(ThrowExpression throwExpression) {
            String type = null;
            Expression expression = throwExpression.getExpression();
            if (expression instanceof ClassInstanceCreation) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
                Expression name = classInstanceCreation.getClassName().getName();
                if (name instanceof NamespaceName) {
                    NamespaceName namespaceName = (NamespaceName) name;
                    type = getTypeFromNamespaceName(namespaceName);
                }
            } else if (expression instanceof Variable) {
                Variable v = (Variable) expression;
                final String name = CodeUtils.extractVariableName(v);
                for (VariableName variable : ElementFilter.forName(NameKind.exact(name)).filter(declaredVariables)) {
                    final Collection<? extends String> typeNames = variable.getTypeNames(variable.getNameRange().getEnd());
                    type = typeNames.isEmpty() ? null : typeNames.iterator().next();
                }
            }
            return resolveProperType(type);
        }

        private String getTypeFromNamespaceName(NamespaceName namespaceName) {
            StringBuilder sbType = new StringBuilder();
            if (namespaceName.isGlobal()) {
                sbType.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
            }
            List<Identifier> segments = namespaceName.getSegments();
            for (Iterator<Identifier> iter =  segments.iterator(); iter.hasNext();) {
                sbType.append(iter.next().getName());
                if (iter.hasNext()) {
                    sbType.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
                }
            }
            return sbType.toString();
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (node == decl) {
                // functions and methods can have a return type declaration since PHP7.0
                Expression type = node.getReturnType();
                if (type != null) {
                    hasReturn = true;
                }
                super.visit(node);
            }
        }

        @Override
        public void visit(ClassDeclaration node) {
        }

    }

    private static class DocTagsGenerator implements Runnable {
        private final BaseDocument doc;
        private final int offset;
        private final int indent;

        public DocTagsGenerator(final BaseDocument doc, final int offset, final int indent) {
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
                        final ParserResult parserResult = (ParserResult) resultIterator.getParserResult();
                        if (parserResult != null) {
                            //find coresponding ASTNode:
                            //TODO: slow and ugly:
                            class Result extends Error {

                                private ASTNode node;

                                public Result(ASTNode node) {
                                    this.node = node;
                                }
                            }
                            ASTNode n = null;
                            try {
                                DefaultVisitor visitor = new DefaultVisitor() {

                                    @Override
                                    public void scan(ASTNode node) {
                                        if (node != null) {
                                            Comment c = Utils.getCommentForNode(Utils.getRoot(parserResult), node);

                                            if (c != null && c.getStartOffset() <= offset && offset <= c.getEndOffset()) {
                                                //found:
                                                throw new Result(node);
                                            }
                                        }
                                        super.scan(node);
                                    }
                                };
                                visitor.scan(Utils.getRoot(parserResult));
                            } catch (Result r) {
                                n = r.node;
                            }

                            if (n == null) {
                                //no found
                                return;
                            }

                            if (n instanceof FunctionDeclaration) {
                                generateFunctionDoc(doc, offset, indent, parserResult, (FunctionDeclaration) n);
                            }

                            if (n instanceof MethodDeclaration) {
                                generateFunctionDoc(doc, offset, indent, parserResult, ((MethodDeclaration) n).getFunction());
                            }

                            if (n instanceof ExpressionStatement) {
                                if (((ExpressionStatement) n).getExpression() instanceof Assignment) {
                                    Assignment assignment = (Assignment) ((ExpressionStatement) n).getExpression();
                                    if (assignment.getLeftHandSide() instanceof ArrayAccess) {
                                        ArrayAccess arrayAccess = (ArrayAccess) assignment.getLeftHandSide();
                                        if (arrayAccess.getName() instanceof Variable) {
                                            Variable variable = (Variable) arrayAccess.getName();
                                            if (variable.isDollared()
                                                    && variable.getName() instanceof Identifier
                                                    && "GLOBALS".equals(((Identifier) variable.getName()).getName())
                                                    && arrayAccess.getDimension().getIndex() instanceof Scalar) {
                                                String index = ((Scalar) arrayAccess.getDimension().getIndex()).getStringValue().trim();
                                                if (index.length() > 0
                                                        && (index.charAt(0) == '\'' || index.charAt(0) == '"')) {
                                                    index = index.substring(1, index.length() - 1);
                                                }
                                                String type = null;
                                                if (assignment.getRightHandSide() instanceof Scalar) {
                                                    switch (((Scalar) assignment.getRightHandSide()).getScalarType()) {
                                                        case INT:
                                                            type = Type.INTEGER;
                                                            break;
                                                        case REAL:
                                                        case FLOAT:
                                                            type = Type.FLOAT;
                                                            break;
                                                        case STRING:
                                                            type = Type.STRING;
                                                            break;
                                                        default:
                                                            //no-op
                                                    }
                                                }
                                                generateGlobalVariableDoc(doc, offset, indent, index, type);
                                            }
                                        }
                                    }
                                }
                            }

                            if (n instanceof FieldsDeclaration) {
                                generateFieldDoc(doc, offset, indent, parserResult, (FieldsDeclaration) n);
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

}
