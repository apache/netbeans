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
package org.netbeans.modules.php.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation.Operator;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.Variadic;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;

/**
 *
 * @author tomslot
 */
public final class CodeUtils {

    public static final String FUNCTION_TYPE_PREFIX = "@fn:"; // NOI18N
    public static final String METHOD_TYPE_PREFIX = "@mtd:"; // NOI18N
    public static final String STATIC_METHOD_TYPE_PREFIX = "@static.mtd:"; // NOI18N
    public static final String NULLABLE_TYPE_PREFIX = "?"; // NOI18N
    public static final String ELLIPSIS = "..."; // NOI18N
    public static final String VAR_TAG = "@var"; // NOI18N
    public static final String EMPTY_STRING = ""; // NOI18N
    public static final String NEW_LINE = "\n"; // NOI18N
    public static final String THIS_VARIABLE = "$this"; // NOI18N
    public static final String NS_SEPARATOR = "\\"; // NOI18N

    public static final Pattern WHITE_SPACES_PATTERN = Pattern.compile("\\s+"); // NOI18N
    public static final Pattern SPLIT_TYPES_PATTERN = Pattern.compile("[()|&]+"); // NOI18N
    public static final Pattern TYPE_NAMES_IN_TYPE_DECLARATION_PATTERN = Pattern.compile("[^?()|&]+"); // NOI18N
    public static final Pattern COMMA_PATTERN = Pattern.compile(","); // NOI18N

    private static final Logger LOGGER = Logger.getLogger(CodeUtils.class.getName());

    private CodeUtils() {
    }

    @CheckForNull
    public static FileObject getFileObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject) sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject) sdp).getPrimaryFile();
        }
        return null;
    }

    public static UseScope.Type mapType(UseStatement.Type type) {
        UseScope.Type newType = null;
        switch (type) {
            case CONST:
                newType = UseScope.Type.CONST;
                break;
            case FUNCTION:
                newType = UseScope.Type.FUNCTION;
                break;
            case TYPE:
                newType = UseScope.Type.TYPE;
                break;
            default:
                assert false : "Unknown type: " + type;
        }
        return newType;
    }

    // XXX move to proper place
    /**
     * Compounds full namespace for the given part of group use.
     * @param groupUseStatementPart group use part
     * @param singleUseStatementPart part to be resolved
     * @param baseOffsets if {@code true}, offsets of base namespace name are used
     * @return full namespace for the given part of group use
     */
    public static NamespaceName compoundName(GroupUseStatementPart groupUseStatementPart, SingleUseStatementPart singleUseStatementPart, boolean baseOffsets) {
        assert groupUseStatementPart != null;
        assert singleUseStatementPart != null;
        assert groupUseStatementPart.getItems().contains(singleUseStatementPart) : singleUseStatementPart + " not found in: " + groupUseStatementPart.getItems();
        NamespaceName baseNamespaceName = groupUseStatementPart.getBaseNamespaceName();
        NamespaceName namespaceName = singleUseStatementPart.getName();
        List<Identifier> segments = new ArrayList<>(baseNamespaceName.getSegments().size() + namespaceName.getSegments().size());
        segments.addAll(baseNamespaceName.getSegments());
        segments.addAll(namespaceName.getSegments());
        int start;
        int end;
        if (baseOffsets) {
            start = baseNamespaceName.getStartOffset();
            end = baseNamespaceName.getEndOffset();
        } else {
            start = namespaceName.getStartOffset();
            end = namespaceName.getEndOffset();
        }
        return new NamespaceName(start, end, segments, baseNamespaceName.isGlobal(), baseNamespaceName.isCurrent());
    }

    /**
     * Checks whether the given name is synthetic name. It means that
     * the name starts with "#".
     * @param name name to be checked
     * @return {@code true} if the given name is synthetic
     */
    public static boolean isSyntheticTypeName(String name) {
        assert name != null;
        return !name.isEmpty()
                && name.charAt(0) == '#'; // NOI18N
    }

    /**
     * Checks whether the given name is synthetic name. It means that
     * the name contains ":" (e.g. LambdaFunctionDeclaration:11).
     * @param name name to be checked
     * @return {@code true} if the given name is synthetic
     */
    public static boolean isSyntheticFunctionName(String name) {
        assert name != null;
        return !name.isEmpty()
                && name.contains(":"); // NOI18N
    }

    public static PhpVersion getPhpVersion(FileObject file) {
        assert file != null;
        return PhpLanguageProperties.forFileObject(file).getPhpVersion();
    }

    public static boolean isPhpVersion(FileObject file, PhpVersion version) {
        assert file != null;
        assert version != null;
        return getPhpVersion(file) == version;
    }

    public static boolean isPhpVersionLessThan(FileObject file, PhpVersion version) {
        assert file != null;
        assert version != null;
        return getPhpVersion(file).compareTo(version) < 0;
    }

    public static boolean isPhpVersionGreaterThan(FileObject file, PhpVersion version) {
        assert file != null;
        assert version != null;
        return getPhpVersion(file).compareTo(version) > 0;
    }

    /**
     * @return {@code true} if the {@link StaticDispatch#getDispatcher() dispatcher}
     * is not just identifier or a namespace name.
     */
    public static boolean isUniformVariableSyntax(StaticDispatch dispatch) {
        assert dispatch != null;
        return isUniformVariableSyntax(dispatch.getDispatcher());
    }

    /**
     * @return {@code true} if the given expression
     * is not just identifier or a namespace name.
     */
    public static boolean isUniformVariableSyntax(Expression expression) {
        assert expression != null;
        return extractUnqualifiedName(expression) == null;
    }

    @CheckForNull
    public static Identifier extractUnqualifiedIdentifier(Expression typeName) {
        Parameters.notNull("typeName", typeName); // NOI18N
        if (typeName instanceof Identifier) {
            return (Identifier) typeName;
        } else if (typeName instanceof NamespaceName) {
            return extractUnqualifiedIdentifier((NamespaceName) typeName);
        } else if (typeName instanceof Variable) {
            Variable v = (Variable) typeName;
            return extractUnqualifiedIdentifier(v.getName()); // #167863
        } else if (typeName instanceof FieldAccess) {
            return extractUnqualifiedIdentifier(((FieldAccess) typeName).getField()); // #167863
        } else if (typeName instanceof NullableType) {
            return extractUnqualifiedIdentifier(((NullableType) typeName).getType());
        }
        //TODO: php5.3 !!!
        //assert false : typeName.getClass(); //NOI18N
        return null;
    }

    /**
     * Extract unqualified name for Identifier, NamespaceName, NullableType, and
     * UnionType.
     *
     * @param typeName The type name
     * @return The type name. If it is a nullable type, the name is returned
     * with "?" If it's union type, type names separated by "|" are returned
     */
    @CheckForNull
    public static String extractUnqualifiedName(Expression typeName) {
        Parameters.notNull("typeName", typeName); // NOI18N
        if (typeName instanceof Identifier) {
            return ((Identifier) typeName).getName();
        } else if (typeName instanceof NamespaceName) {
            return extractUnqualifiedName((NamespaceName) typeName);
        } else if (typeName instanceof NullableType) {
            return NULLABLE_TYPE_PREFIX + extractUnqualifiedName(((NullableType) typeName).getType());
        } else if (typeName instanceof UnionType) {
            return extractUnqualifiedName((UnionType) typeName);
        } else if (typeName instanceof IntersectionType) {
            return extractUnqualifiedName((IntersectionType) typeName);
        }

        //TODO: php5.3 !!!
        //assert false : "[php5.3] className Expression instead of Identifier"; //NOI18N
        return null;
    }

    private static String extractUnqualifiedName(UnionType unionType) {
        StringBuilder sb = new StringBuilder();
        for (Expression type : unionType.getTypes()) {
            if (sb.length() > 0) {
                sb.append(Type.SEPARATOR);
            }
            boolean isIntersectionType = type instanceof IntersectionType;
            if (isIntersectionType) {
                sb.append("("); // NOI18N
            }
            sb.append(extractUnqualifiedName(type));
            if (isIntersectionType) {
                sb.append(")"); // NOI18N
            }
        }
        return sb.toString();
    }

    private static String extractUnqualifiedName(IntersectionType intersectionType) {
        StringBuilder sb = new StringBuilder();
        for (Expression type : intersectionType.getTypes()) {
            if (sb.length() > 0) {
                sb.append(Type.SEPARATOR_INTERSECTION);
            }
            sb.append(extractUnqualifiedName(type));
        }
        return sb.toString();
    }

    /**
     * Extract qualified name for Identifier, NamespaceName, NullableType, and
     * UnionType.
     *
     * @param typeName The type name
     * @return The type name. If it is a nullable type, the name is returned
     * with "?". If it's a union type, type names separated by "|" are returned.
     * If it's an intersection type, type names separated by "&" are returned.
     */
    @CheckForNull
    public static String extractQualifiedName(Expression typeName) {
        Parameters.notNull("typeName", typeName); // NOI18N
        if (typeName instanceof Identifier) {
            return ((Identifier) typeName).getName();
        } else if (typeName instanceof NamespaceName) {
            return extractQualifiedName((NamespaceName) typeName);
        } else if (typeName instanceof NullableType) {
            NullableType nullableType = (NullableType) typeName;
            return NULLABLE_TYPE_PREFIX + extractQualifiedName(nullableType.getType());
        } else if (typeName instanceof ExpressionArrayAccess) {
            return extractQualifiedName(((ExpressionArrayAccess) typeName).getExpression());
        } else if (typeName instanceof UnionType) {
            return extractQualifiedName((UnionType) typeName);
        } else if (typeName instanceof IntersectionType) {
            return extractQualifiedName((IntersectionType) typeName);
        }
        assert false : typeName.getClass();
        return null;
    }

    private static String extractQualifiedName(UnionType unionType) {
        StringBuilder sb = new StringBuilder();
        for (Expression type : unionType.getTypes()) {
            if (sb.length() > 0) {
                sb.append(Type.SEPARATOR);
            }
            boolean isIntersectionType = type instanceof IntersectionType;
            if (isIntersectionType) {
                sb.append("("); // NOI18N
            }
            sb.append(extractQualifiedName(type));
            if (isIntersectionType) {
                sb.append(")"); // NOI18N
            }
        }
        return sb.toString();
    }

    private static String extractQualifiedName(IntersectionType intersectionType) {
        StringBuilder sb = new StringBuilder();
        for (Expression type : intersectionType.getTypes()) {
            if (sb.length() > 0) {
                sb.append(Type.SEPARATOR_INTERSECTION);
            }
            sb.append(extractQualifiedName(type));
        }
        return sb.toString();
    }

    // XXX not only class name anymore in php7+
    public static String extractUnqualifiedClassName(StaticDispatch dispatch) {
        Parameters.notNull("dispatch", dispatch);
        Expression dispatcher = dispatch.getDispatcher();
        if (dispatcher instanceof StaticConstantAccess) {
            // e.g. EnumName::Case::staticMethod();
            dispatcher = ((StaticConstantAccess) dispatcher).getDispatcher();
        }
        return extractUnqualifiedName(dispatcher);
    }

    public static String extractUnqualifiedTypeName(FormalParameter param) {
        Parameters.notNull("param", param);
        Expression typeName = param.getParameterType();
        return typeName != null ? extractUnqualifiedName(typeName) : null;
    }

    public static List<String> extractUnqualifiedTypeName(CatchClause catchClause) {
        Parameters.notNull("catchClause", catchClause);
        List<String> typeNames = new ArrayList<>();
        for (Expression className : catchClause.getClassNames()) {
            if (className != null) {
                String typeName = extractUnqualifiedName(className);
                if (typeName != null) {
                    typeNames.add(typeName);
                }
            }
        }
        return typeNames;
    }

    public static String extractUnqualifiedSuperClassName(ClassDeclaration clsDeclaration) {
        Parameters.notNull("clsDeclaration", clsDeclaration);
        Expression clsName = clsDeclaration.getSuperClass();
        return clsName != null ? extractUnqualifiedName(clsName) : null;
    }

    @CheckForNull
    public static String extractUnqualifiedSuperClassName(ClassInstanceCreation classInstanceCreation) {
        assert classInstanceCreation != null;
        assert classInstanceCreation.isAnonymous() : classInstanceCreation;
        Expression clsName = classInstanceCreation.getSuperClass();
        return clsName != null ? extractUnqualifiedName(clsName) : null;
    }

    public static String extractUnqualifiedName(NamespaceName namespaceName) {
        final List<Identifier> segments = namespaceName.getSegments();
        return segments.get(segments.size() - 1).getName();
    }

    public static String extractQualifiedName(NamespaceName namespaceName) {
        Parameters.notNull("namespaceName", namespaceName);
        StringBuilder sb = new StringBuilder();
        final List<Identifier> segments = namespaceName.getSegments();
        if (namespaceName.isGlobal()) {
            sb.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
        }
        for (Iterator<Identifier> it = segments.iterator(); it.hasNext();) {
            Identifier identifier = it.next();
            sb.append(identifier.getName());
            if (it.hasNext()) {
                sb.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
            }
        }
        return sb.toString();
    }

    public static Identifier extractUnqualifiedIdentifier(NamespaceName namespaceName) {
        final List<Identifier> segments = namespaceName.getSegments();
        if (segments.size() >= 1) {
            return segments.get(segments.size() - 1);
        }
        //TODO: php5.3 !!!
        //assert false : "[php5.3] className Expression instead of Identifier"; //NOI18N
        return null;
    }
    //TODO: rewrite for php53
    public static String extractClassName(ClassName clsName) {
        assert clsName != null;
        Expression name = clsName.getName();
        while (name instanceof Variable || name instanceof FieldAccess) {
            if (name instanceof Variable) {
                Variable var = (Variable) name;
                name = var.getName();
            } else if (name instanceof FieldAccess) {
                FieldAccess fld = (FieldAccess) name;
                name = fld.getField().getName();
            }
        }
        if (name instanceof NamespaceName) {
            return extractQualifiedName((NamespaceName) name);
        }
        return (name instanceof Identifier) ? ((Identifier) name).getName() : ""; //NOI18N
    }

    public static String extractClassName(ClassDeclaration clsDeclaration) {
        return clsDeclaration.getName().getName();
    }

    public static String extractClassName(ClassInstanceCreation classInstanceCreation) {
        return extractClassName(classInstanceCreation.getClassName());
    }

    public static String extractTypeName(TypeDeclaration typeDeclaration) {
        return typeDeclaration.getName().getName();
    }

    private static final class VariableNameVisitor extends DefaultVisitor {

        private String name = null;
        private boolean isDollared = false;

        private VariableNameVisitor() {
        }

        private static class SingletonHolder {
            public static final VariableNameVisitor INSTANCE = new VariableNameVisitor();
        }

        public static VariableNameVisitor getInstance() {
            return SingletonHolder.INSTANCE;
        }

        public String findName(Variable var) {
            name = null;
            scan(var);
            return name;
        }

        @Override
        public void visit(Scalar node) {
            final String scalarName = node.getStringValue();
            if ((scalarName.startsWith("'") && scalarName.endsWith("'"))
                    || (scalarName.startsWith("\"") && scalarName.endsWith("\""))) { //NOI18N
                name = scalarName.substring(1, scalarName.length() - 1);
            } else {
                name = scalarName;
            }
        }

        @Override
        public void visit(Variable node) {
            isDollared = node.isDollared();
            super.visit(node);
        }

        @Override
        public void visit(InfixExpression node) {
        }

        @Override
        public void visit(Identifier identifier) {
            name = isDollared ? "$" + identifier.getName() : identifier.getName();
        }

        @Override
        public void visit(ArrayAccess node) {
            scan(node.getName());
        }
    }


    @CheckForNull // null for RelectionVariable
    public static String extractVariableName(Variable var) {
        String variableName = VariableNameVisitor.getInstance().findName(var);
        if (variableName == null) {
            LOGGER.log(Level.FINE, "Can not retrieve variable name: {0}", var);
        }
        return variableName;
    }

    @CheckForNull
    public static String extractFormalParameterName(FormalParameter param) {
        Expression expression = param.getParameterName();
        if (expression instanceof Reference) {
            expression = ((Reference) expression).getExpression();
        }
        if (expression instanceof Variadic) {
            expression = ((Variadic) expression).getExpression();
        }
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            return extractVariableName(variable);
        }
        return null;
    }

    public static String extractVariableType(Assignment assignment) {
        Expression rightSideExpression = assignment.getRightHandSide();

        if (rightSideExpression instanceof Assignment) {
            // handle nested assignments, e.g. $l = $m = new ObjectName;
            return extractVariableType((Assignment) assignment.getRightHandSide());
        } else if (rightSideExpression instanceof Reference) {
            Reference ref = (Reference) rightSideExpression;
            rightSideExpression = ref.getExpression();
        }

        if (rightSideExpression instanceof ClassInstanceCreation) {
            ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightSideExpression;
            Expression className = classInstanceCreation.getClassName().getName();
            return CodeUtils.extractUnqualifiedName(className);
        } else if (rightSideExpression instanceof ArrayCreation) {
            return Type.ARRAY;
        } else if (rightSideExpression instanceof FunctionInvocation) {
            FunctionInvocation functionInvocation = (FunctionInvocation) rightSideExpression;
            String fname = extractFunctionName(functionInvocation);
            return FUNCTION_TYPE_PREFIX + fname;
        } else if (rightSideExpression instanceof StaticMethodInvocation) {
            StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) rightSideExpression;
            String className = CodeUtils.extractUnqualifiedClassName(staticMethodInvocation);
            String methodName = extractFunctionName(staticMethodInvocation.getMethod());

            if (className != null && methodName != null) {
                return STATIC_METHOD_TYPE_PREFIX + className + '.' + methodName;
            }
        } else if (rightSideExpression instanceof MethodInvocation) {
            MethodInvocation methodInvocation = (MethodInvocation) rightSideExpression;
            String varName = null;

            if (methodInvocation.getDispatcher() instanceof Variable) {
                Variable var = (Variable) methodInvocation.getDispatcher();
                varName = extractVariableName(var);
            }

            String methodName = extractFunctionName(methodInvocation.getMethod());

            if (varName != null && methodName != null) {
                return METHOD_TYPE_PREFIX + varName + '.' + methodName;
            }
        }

        return null;
    }

    public static String extractFunctionName(FunctionInvocation functionInvocation) {
        return extractFunctionName(functionInvocation.getFunctionName());
    }

    public static String extractFunctionName(FunctionDeclaration functionDeclaration) {
        return functionDeclaration.getFunctionName().getName();
    }

    public static String extractMethodName(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getFunction().getFunctionName().getName();
    }

    @CheckForNull
    public static String extractFunctionName(FunctionName functionName) {
        if (functionName.getName() instanceof Identifier) {
            Identifier id = (Identifier) functionName.getName();
            return id.getName();
        } else if (functionName.getName() instanceof NamespaceName) {
            return extractUnqualifiedName((NamespaceName) functionName.getName());
        } else if (functionName.getName() instanceof Scalar) {
            String scalarName = ((Scalar) functionName.getName()).getStringValue();
            if (isQuoted(scalarName)) {
                return scalarName.substring(1, scalarName.length() - 1);
            }
        }
        if (functionName.getName() instanceof Variable) {
            Variable var = (Variable) functionName.getName();
            return extractVariableName(var);
        }

        return null;
    }

    private static boolean isQuoted(String string) {
        return string != null
                && string.length() > 2
                && ((string.startsWith("'") && string.endsWith("'")) || (string.startsWith("\"") && string.endsWith("\""))); // NOI18N
    }

    @CheckForNull
    public static String getParamDefaultValue(FormalParameter param) {
        Expression expr = param.getDefaultValue();
        return getParamDefaultValue(expr);
    }

    @CheckForNull
    private static String getParamDefaultValue(Expression expr) {
        //TODO: can be improved
        Operator operator = null;
        if (expr instanceof UnaryOperation) {
            UnaryOperation unaryExpr = (UnaryOperation) expr;
            operator = unaryExpr.getOperator();
            expr = unaryExpr.getExpression();
        }
        if (expr instanceof Scalar) {
            Scalar scalar = (Scalar) expr;
            String returnValue = scalar.getStringValue();
            return Operator.MINUS.equals(operator) ? "-" + returnValue : returnValue; // NOI18N
        } else if (expr instanceof NamespaceName) {
            return extractQualifiedName((NamespaceName) expr);
        } else if (expr instanceof ArrayCreation) {
            return getParamDefaultValue((ArrayCreation) expr);
        } else if (expr instanceof StaticConstantAccess) {
            StaticConstantAccess staticConstantAccess = (StaticConstantAccess) expr;
            Expression dispatcher = staticConstantAccess.getDispatcher();
            if (dispatcher instanceof Identifier) {
                Identifier i = (Identifier) dispatcher;
                return i.getName() + "::" + staticConstantAccess.getConstantName().getName(); // NOI18N
            } else if (dispatcher instanceof NamespaceName) {
                NamespaceName namespace = (NamespaceName) dispatcher;
                StringBuilder sb = new StringBuilder(extractQualifiedName(namespace));
                return sb.append("::").append(staticConstantAccess.getConstantName().getName()).toString(); // NOI18N
            }
        }
        return expr == null ? null : " "; //NOI18N
    }

    private static String getParamDefaultValue(ArrayCreation param) {
        StringBuilder sb = new StringBuilder("["); //NOI18N
        List<ArrayElement> arrayElements = param.getElements();
        if (arrayElements.size() > 0) {
            ArrayElement firstElement = arrayElements.get(0);
            Expression key = firstElement.getKey();
            if (key != null) {
                sb.append(getParamDefaultValue(key));
                sb.append(" => "); //NOI18N
            }
            sb.append(getParamDefaultValue(firstElement.getValue()));
        }
        if (arrayElements.size() > 1) {
            sb.append(",..."); //NOI18N
        }
        sb.append("]"); //NOI18N
        return sb.toString();
    }

    public static String getParamDisplayName(FormalParameter param) {
        Expression paramNameExpr = param.getParameterName();
        StringBuilder paramName = new StringBuilder();

        if (paramNameExpr instanceof Variable) {
            Variable var = (Variable) paramNameExpr;
            Identifier id = (Identifier) var.getName();

            if (var.isDollared()) {
                paramName.append("$"); //NOI18N
            }

            paramName.append(id.getName());
        } else if (paramNameExpr instanceof Reference) {
            paramName.append("&");
            Reference reference = (Reference) paramNameExpr;

            Expression expression = reference.getExpression();
            if (expression instanceof Variadic) {
                Variadic variadic = (Variadic) expression;
                paramName.append("..."); //NOI18N
                expression = variadic.getExpression();
            }

            if (expression instanceof Variable) {
                Variable var = (Variable) reference.getExpression();

                if (var.isDollared()) {
                    paramName.append("$"); //NOI18N
                }

                Identifier id = (Identifier) var.getName();
                paramName.append(id.getName());
            }
        }

        return paramName.length() == 0 ? null : paramName.toString();
    }

    public static boolean isConstructor(MethodDeclaration node) {
        return "__construct".equals(extractMethodName(node)); //NOI18N
    }

    public static boolean isDollaredName(ClassName className) {
        Expression name = className.getName();
        if (name instanceof Variable) {
            Variable variable = (Variable) name;
            return variable.isDollared();
        }
        return false;
    }

    /**
     * Finds common namespace prefixes for the given <b>sorted</b> namespaces.
     * <p>
     * Note: all returned prefixes start and end with '\\'.
     * @param namespaces input namespaces (<b>must be sorted!</b>)
     * @return list of common namespace prefixes or empty list, never {@code null}
     */
    public static List<String> getCommonNamespacePrefixes(List<String> namespaces) {
        if (namespaces.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedList<String> fqNamespaces = new LinkedList<>();
        for (String namespace : namespaces) {
            fqNamespaces.add(fullyQualifyNamespace(namespace));
        }
        List<String> prefixes = new ArrayList<>(fqNamespaces.size());
        while (!fqNamespaces.isEmpty()) {
            String namespace = fqNamespaces.poll();
            if (fqNamespaces.isEmpty()) {
                break;
            }
            assert namespace.charAt(0) == '\\' : namespace;
            int separatorIndex = namespace.indexOf('\\', 1); // NOI18N
            if (separatorIndex == -1) {
                // no ns separator
                continue;
            }
            String prefix = namespace.substring(0, separatorIndex + 1);
            List<String> prefixedNamespaces = new ArrayList<>(fqNamespaces.size());
            // get all namespaces that start with this prefix
            for (Iterator<String> iterator = fqNamespaces.iterator(); iterator.hasNext();) {
                String ns = iterator.next();
                if (ns.startsWith(prefix)) {
                    prefixedNamespaces.add(ns);
                    iterator.remove();
                } else {
                    break;
                }
            }
            if (prefixedNamespaces.isEmpty()) {
                // not common ns prefix
                continue;
            }
            prefixedNamespaces.add(0, namespace);
            if (prefixedNamespaces.size() > 1) {
                // find common longest prefix
                prefix = null;
                for (int i = 1; i < prefixedNamespaces.size(); i++) {
                    String prev = prefixedNamespaces.get(i - 1);
                    String next = prefixedNamespaces.get(i);
                    String tmpPrefix = getCommonNamespacePrefix(prev, next);
                    assert tmpPrefix != null : prev + " :: " + next;
                    if (prefix == null) {
                        prefix = tmpPrefix;
                    } else if (prefix.length() > tmpPrefix.length()) {
                        prefix = tmpPrefix;
                    }
                }
                assert prefix != null : prefixedNamespaces;
            }
            prefixes.add(prefix);
        }
        return prefixes;
    }

    @CheckForNull
    static String getCommonNamespacePrefix(String ns1, String ns2) {
        assert ns1 != null;
        assert ns2 != null;
        String fqns1 = fullyQualifyNamespace(ns1);
        String fqns2 = fullyQualifyNamespace(ns2);
        int index;
        for (index = 0; index < fqns1.length() && index < fqns2.length(); index++) {
            if (fqns1.charAt(index) != fqns2.charAt(index)) {
                break;
            }
        }
        // check shortest common prefix (e.g. '\A\')
        if (index < 3) {
            return null;
        }
        String prefix = fqns1.substring(0, index);
        if (prefix.charAt(index - 1) == '\\') { // NOI18N
            return prefix;
        }
        // find last '\' (avoid first '\')
        int lastNsIndex = prefix.lastIndexOf('\\'); // NOI18N
        if (lastNsIndex <= 0) {
            // not found or the first '\'
            return null;
        }
        return prefix.substring(0, lastNsIndex + 1);
    }

    public static String fullyQualifyNamespace(String namespace) {
        assert namespace != null;
        if (!namespace.isEmpty()
                && namespace.charAt(0) != '\\') { // NOI18N
            return '\\' + namespace; // NOI18N
        }
        return namespace;
    }

    /**
     * Check whether a type name starts with "?".
     *
     * @param typeName a type name
     * @return {@code true} if the name starts with "?", otherwise
     * {@code false}
     */
    public static boolean isNullableType(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return false;
        }
        return typeName.startsWith(NULLABLE_TYPE_PREFIX);
    }

    /**
     * Remove the nullable type prefix("?") from the type name.
     *
     * @param typeName the type name
     * @return the type name from which the prefix is removed if it is a
     * nullable type, otherwise itself
     */
    public static String removeNullableTypePrefix(String typeName) {
        if (isNullableType(typeName)) {
            return typeName.substring(1);
        }
        return typeName;
    }

    /**
     * Get an OffsetRange of an ASTNode.
     *
     * @param node the ASTNode
     * @return the OffsetRange
     */
    public static OffsetRange getOffsetRagne(@NonNull ASTNode node) {
        return new OffsetRange(node.getStartOffset(), node.getEndOffset());
    }

    public static boolean isDnfType(UnionType unionType) {
        if (unionType != null) {
            for (Expression type : unionType.getTypes()) {
                if (type instanceof IntersectionType) {
                    return true;
                }
            }
        }
        return false;
    }
}
