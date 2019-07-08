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
package org.netbeans.modules.php.editor.model.nodes;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.Variadic;

/**
 *
 * @author Radek Matous
 */
public class ASTNodeInfo<T extends ASTNode> {
    private final T node;
    private Kind kind;

    public enum Kind {
        NAMESPACE_DECLARATION, USE_STATEMENT, GROUP_USE_STATEMENT, IFACE, CLASS, CLASS_INSTANCE_CREATION,
        METHOD, STATIC_METHOD,
        FIELD, STATIC_FIELD,
        CLASS_CONSTANT, STATIC_CLASS_CONSTANT,
        VARIABLE, CONSTANT, FUNCTION, PARAMETER,
        INCLUDE, RETURN_MARKER, GOTO, TRAIT, USE_ALIAS
    }

    ASTNodeInfo(T node) {
        this.node = node;
    }

    ASTNodeInfo(Kind kind, T node) {
        this.kind = kind;
        this.node = node;
    }

    public String getName() {
        return toName(getOriginalNode());
    }

    public QualifiedName getQualifiedName() {
        return toQualifiedName(node, false);
    }

    public static QualifiedName toQualifiedName(ASTNode node, boolean type) {
        QualifiedName retval = null;
        if (node instanceof FunctionInvocation) {
            FunctionInvocation fi = (FunctionInvocation) node;
            retval = QualifiedName.create(fi.getFunctionName().getName());
        } else if (node instanceof ClassName) {
            ClassName cname = (ClassName) node;
            retval = QualifiedName.create(cname.getName());
        } else if (node instanceof Identifier) {
            Identifier cname = (Identifier) node;
            retval = QualifiedName.createUnqualifiedName(cname);
        } else if (node instanceof NamespaceName) {
            retval = QualifiedName.create((NamespaceName) node);
        } else if (node instanceof ClassInstanceCreation) {
            ClassInstanceCreation instanceCreation = (ClassInstanceCreation) node;
            retval = QualifiedName.create(instanceCreation.getClassName().getName());
        } else if (node instanceof SingleUseStatementPart) {
            SingleUseStatementPart statementPart = (SingleUseStatementPart) node;
            retval = QualifiedName.create(statementPart.getName());
        } else if (type && node instanceof StaticDispatch) {
            StaticDispatch staticDispatch = (StaticDispatch) node;
            retval = QualifiedName.create(staticDispatch.getDispatcher());
        } else if (node instanceof Scalar) {
            String toName = toName(node);
            retval = QualifiedName.create(toName);
        }
        if (retval == null) {
            String toName = toName(node);
            if (toName == null) {
                //#185702 - NullPointerException at org.netbeans.modules.php.editor.api.QualifiedNameKind.resolveKind
                //$this->{'_find' . ucfirst($type)}('before', $query);
                retval = QualifiedName.createUnqualifiedName(""); //NOI18N
            } else {
                retval = QualifiedName.createUnqualifiedName(toName);
            }
        }
        return retval;
    }

    public Kind getKind() {
        return (kind == null) ? toKind(getOriginalNode()) : kind;
    }

    public PhpElementKind getPhpElementKind() {
        Kind k = getKind();
        switch (k) {
            case INCLUDE:
                return PhpElementKind.INCLUDE;
            case IFACE:
                return PhpElementKind.IFACE;
            case CLASS:
                return PhpElementKind.CLASS;
            case CLASS_INSTANCE_CREATION:
                return PhpElementKind.CLASS;
            case METHOD:
                return PhpElementKind.METHOD;
            case STATIC_METHOD:
                return PhpElementKind.METHOD;
            case FIELD:
                return PhpElementKind.FIELD;
            case STATIC_FIELD:
                return PhpElementKind.FIELD;
            case CLASS_CONSTANT:
                return PhpElementKind.TYPE_CONSTANT;
            case STATIC_CLASS_CONSTANT:
                return PhpElementKind.TYPE_CONSTANT;
            case VARIABLE:
                return PhpElementKind.VARIABLE;
            case CONSTANT:
                return PhpElementKind.CONSTANT;
            case FUNCTION:
                return PhpElementKind.FUNCTION;
            case USE_STATEMENT:
                return PhpElementKind.USE_STATEMENT;
            case GROUP_USE_STATEMENT:
                return PhpElementKind.GROUP_USE_STATEMENT;
            case TRAIT:
                return PhpElementKind.TRAIT;
            case USE_ALIAS:
                return PhpElementKind.USE_ALIAS;
            default:
                assert false : k;
        }
        throw new IllegalStateException();
    }

    public OffsetRange getRange() {
        return toOffsetRange(getOriginalNode());
    }

    public final T getOriginalNode() {
        return node;
    }
    public static ASTNodeInfo<FieldAccess> create(FieldAccess fieldAccess) {
        return new ASTNodeInfo<>(fieldAccess);
    }

    public static ASTNodeInfo<SingleUseStatementPart> create(SingleUseStatementPart statementPart) {
        return new ASTNodeInfo<>(statementPart);
    }

    public static ASTNodeInfo<GotoStatement> create(GotoStatement statement) {
        return new ASTNodeInfo<>(statement);
    }
    public static ASTNodeInfo<GotoLabel> create(GotoLabel label) {
        return new ASTNodeInfo<>(label);
    }
    public static ASTNodeInfo<FunctionInvocation> create(FunctionInvocation functionInvocation) {
        return new ASTNodeInfo<>(functionInvocation);
    }

    public static ASTNodeInfo<Variable> create(Variable variable) {
        return new ASTNodeInfo<>(variable);
    }

    public static ASTNodeInfo<StaticDispatch> create(StaticDispatch staticDispatch) {
        return new ASTNodeInfo<>(staticDispatch);
    }

    public static ASTNodeInfo<StaticMethodInvocation> create(StaticMethodInvocation staticMethodInvocation) {
        return new ASTNodeInfo<>(staticMethodInvocation);
    }

    public static ASTNodeInfo<StaticFieldAccess> create(StaticFieldAccess staticFieldAccess) {
        return new ASTNodeInfo<>(staticFieldAccess);
    }

    public static ASTNodeInfo<StaticConstantAccess> create(StaticConstantAccess staticConstantAccess) {
        return new ASTNodeInfo<>(staticConstantAccess);
    }

    public static ASTNodeInfo<ClassInstanceCreation> create(ClassInstanceCreation instanceCreation) {
        if (instanceCreation.isAnonymous()) {
            return ClassInstanceCreationInfo.create(instanceCreation);
        }
        return new ASTNodeInfo<>(instanceCreation);
    }

    public static ASTNodeInfo<ClassName> create(ClassName className) {
        return new ASTNodeInfo<>(className);
    }

    public static ASTNodeInfo<Expression> create(Kind kind, NamespaceName namespaceName) {
        return new ASTNodeInfo<Expression>(kind, namespaceName);
    }

    public static ASTNodeInfo<Expression> create(Kind kind, Identifier identifier) {
        return new ASTNodeInfo<Expression>(kind, identifier);
    }

    public static ASTNodeInfo<Scalar> create(Kind kind, Scalar scalar) {
        return new ASTNodeInfo<>(kind, scalar);
    }

    public static ASTNodeInfo<MethodInvocation> create(MethodInvocation methodInvocation) {
        return new ASTNodeInfo<>(methodInvocation);
    }

    public static ASTNodeInfo<ReturnStatement> create(ReturnStatement returnStatement) {
        return new ASTNodeInfo<>(returnStatement);
    }

    private static Kind toKind(ASTNode node) {
        if (node instanceof GotoStatement) {
            return Kind.GOTO;
        } else if (node instanceof GotoLabel) {
            return Kind.GOTO;
        } else if (node instanceof FunctionInvocation) {
            return Kind.FUNCTION;
        } else if (node instanceof Variable) {
            return Kind.VARIABLE;
        } else if (node instanceof StaticMethodInvocation) {
            return Kind.STATIC_METHOD;
        } else if (node instanceof StaticFieldAccess) {
            return Kind.STATIC_FIELD;
        } else if (node instanceof MethodInvocation) {
            return Kind.METHOD;
        } else if (node instanceof StaticConstantAccess) {
            return Kind.STATIC_CLASS_CONSTANT;
        } else if (node instanceof ClassName) {
            return Kind.CLASS;
        } else if (node instanceof ClassInstanceCreation) {
            return Kind.CLASS_INSTANCE_CREATION;
        } else if (node instanceof FieldAccess) {
            return Kind.FIELD;
        } else if (node instanceof ReturnStatement) {
            return Kind.RETURN_MARKER;
        } else if (node instanceof SingleUseStatementPart) {
            return Kind.USE_STATEMENT;
        } else if (node instanceof GroupUseStatementPart) {
            return Kind.GROUP_USE_STATEMENT;
        }
        throw new IllegalStateException(node.getClass().getName());
    }

    protected static String toName(ASTNode node) {
        if (node instanceof GotoStatement) {
            GotoStatement gotoStatement = (GotoStatement) node;
            return gotoStatement.getLabel().getName();
        } else if (node instanceof GotoLabel) {
            GotoLabel gotoLabel = (GotoLabel) node;
            return gotoLabel.getName().getName();
        } else if (node instanceof FunctionInvocation) {
            FunctionInvocation fi = (FunctionInvocation) node;
            return CodeUtils.extractFunctionName(fi);
        } else if (node instanceof Variable) {
            Variable var = (Variable) node;
            return toNameVar(var);
        } else if (node instanceof StaticMethodInvocation) {
            StaticMethodInvocation smi = (StaticMethodInvocation) node;
            return toName(smi.getMethod());
        } else if (node instanceof StaticFieldAccess) {
            StaticFieldAccess sfa = (StaticFieldAccess) node;
            return toNameField(sfa.getField());
        } else if (node instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) node;
            return toName(mi.getMethod());
        } else if (node instanceof StaticConstantAccess) {
            StaticConstantAccess sca = (StaticConstantAccess) node;
            return sca.getConstantName().getName();
        } else if (node instanceof ClassName) {
            ClassName cname = (ClassName) node;
            return CodeUtils.extractClassName(cname);
        } else if (node instanceof Identifier) {
            Identifier cname = (Identifier) node;
            return cname.getName();
        } else if (node instanceof NamespaceName) {
            return toName(CodeUtils.extractUnqualifiedIdentifier((NamespaceName) node));
        } else if (node instanceof Scalar) {
            Scalar scalar = (Scalar) node;
            return NavUtils.isQuoted(scalar.getStringValue()) ? NavUtils.dequote(scalar.getStringValue()) : scalar.getStringValue();
        } else if (node instanceof ClassInstanceCreation) {
            ClassInstanceCreation instanceCreation = (ClassInstanceCreation) node;
            return toName(instanceCreation.getClassName());
        } else if (node instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) node;
            return toNameField(fieldAccess.getField());
        } else if (node instanceof ReturnStatement) {
            return "return"; //NOI18N
        } else if (node instanceof Reference) {
            return toName(((Reference) node).getExpression());
        } else if (node instanceof SingleUseStatementPart) {
            return toQualifiedName(node, false).toString();
        } else if (node instanceof GroupUseStatementPart) {
            // XXX
            assert false : "should not get here: " + node;
            return "?? GroupUseStatementPart ??"; // NOI18N
        } else if (node instanceof Variadic) {
            return toName(((Variadic) node).getExpression());
        }
        throw new IllegalStateException(node.getClass().toString());
    }

    protected static OffsetRange toOffsetRange(ASTNode node) {
        if (node instanceof GotoStatement) {
            GotoStatement gotoStatement = (GotoStatement) node;
            return toOffsetRange(gotoStatement.getLabel());
        } else if (node instanceof GotoLabel) {
            GotoLabel gotoLabel = (GotoLabel) node;
            return toOffsetRange(gotoLabel.getName());
        } else if (node instanceof FunctionInvocation) {
            return toOffsetRange(((FunctionInvocation) node).getFunctionName().getName());
        } else if (node instanceof Variable) {
            Variable var = (Variable) node;
            return toOffsetRangeVar(var);
        } else if (node instanceof StaticMethodInvocation) {
            StaticMethodInvocation smi = (StaticMethodInvocation) node;
            return toOffsetRange(smi.getMethod());
        } else if (node instanceof StaticFieldAccess) {
            StaticFieldAccess sfa = (StaticFieldAccess) node;
            return toOffsetRange(sfa.getField());
        } else if (node instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) node;
            return toOffsetRange(mi.getMethod());
        } else if (node instanceof StaticConstantAccess) {
            StaticConstantAccess sca = (StaticConstantAccess) node;
            Identifier constant = sca.getConstantName();
            return new OffsetRange(constant.getStartOffset(), constant.getEndOffset());
        } else if (node instanceof ClassName) {
            Identifier id = CodeUtils.extractUnqualifiedIdentifier(((ClassName) node).getName());
            if (id == null) { // #168459
                return new OffsetRange(node.getStartOffset(), node.getEndOffset());
            }
            return new OffsetRange(id.getStartOffset(), id.getEndOffset());
        } else if (node instanceof Identifier) {
            Identifier cname = (Identifier) node;
            return new OffsetRange(cname.getStartOffset(), cname.getEndOffset());
        } else if (node instanceof NamespaceName) {
            return toOffsetRange(CodeUtils.extractUnqualifiedIdentifier((NamespaceName) node));
        } else if (node instanceof Scalar) {
            Scalar scalar = (Scalar) node;
            if (NavUtils.isQuoted(scalar.getStringValue())) {
                return new OffsetRange(node.getStartOffset() + 1, node.getEndOffset() - 1);
            } else {
                return new OffsetRange(node.getStartOffset(), node.getEndOffset());
            }
        } else if (node instanceof ClassInstanceCreation) {
            ClassInstanceCreation instanceCreation = (ClassInstanceCreation) node;
            return toOffsetRange(instanceCreation.getClassName());
        } else if (node instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) node;
            return toOffsetRange(fieldAccess.getField());
        } else if (node instanceof ReturnStatement) {
            ReturnStatement returnStatement = (ReturnStatement) node;
            return new OffsetRange(returnStatement.getStartOffset(), returnStatement.getEndOffset());
        } else if (node instanceof Reference) {
            return toOffsetRange(((Reference) node).getExpression());
        } else if (node instanceof SingleUseStatementPart
                || node instanceof GroupUseStatementPart) {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        } else if (node instanceof Variadic) {
            return toOffsetRange(((Variadic) node).getExpression());
        } else if (node instanceof ParenthesisExpression) {
            return toOffsetRange(((ParenthesisExpression) node).getExpression());
        } else if (node instanceof LambdaFunctionDeclaration) {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        } else if (node instanceof ArrowFunctionDeclaration) {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        } else if (node instanceof ArrayCreation) {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }
        throw new IllegalStateException(node.getClass().toString());
    }

    static String toNameVar(Variable var) {
        return CodeUtils.extractVariableName(var);
    }
    static String toNameField(Variable var) {
        String retval = CodeUtils.extractVariableName(var);
        if (retval != null && !retval.startsWith("$")) {
            retval = "$" + retval;
        }
        return retval;
    }

    // public because of frameworks!
    public static OffsetRange toOffsetRangeVar(Variable node) {
        Expression name = node.getName();
        //TODO: dangerous never ending loop
        while ((name instanceof Variable)) {
            while (name instanceof ArrayAccess) {
                ArrayAccess access = (ArrayAccess) name;
                name = access.getName();
            }
            if (name instanceof Variable) {
                Variable var = (Variable) name;
                name = var.getName();
            }
        }
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }
}
