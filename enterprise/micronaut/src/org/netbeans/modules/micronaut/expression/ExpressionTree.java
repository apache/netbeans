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
package org.netbeans.modules.micronaut.expression;

import java.beans.Introspector;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

/**
 *
 * @author Dusan Balek
 */
public abstract class ExpressionTree {

    private final Kind kind;
    protected Element element;
    protected TypeMirror typeMirror;

    private ExpressionTree(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public Element getElement(EvaluationContext ctx) {
        if (typeMirror == null && element == null) {
            resolve(ctx);
        }
        return element;
    }

    public TypeMirror getTypeMirror(EvaluationContext ctx) {
        if (typeMirror == null && element == null) {
            resolve(ctx);
        }
        return typeMirror;
    }

    public abstract int getStartPosition();
    public abstract int getEndPosition();
    public abstract <R,D> R accept(Scanner<R,D> scanner, D data);

    protected void resolve(EvaluationContext ctx) {
        typeMirror = ctx.getTypes().getNoType(TypeKind.NONE);
    }

    private static TypeMirror unbox(EvaluationContext ctx, TypeMirror tm) {
        try {
            return ctx.getTypes().unboxedType(tm);
        } catch (IllegalArgumentException e) {
            return tm;
        }
    }

    public static String getPropertyName(ExecutableElement ee) {
        if (ee.getParameters().isEmpty() && ee.getModifiers().contains(Modifier.PUBLIC) && !ee.getModifiers().contains(Modifier.STATIC)) {
            TypeKind ret = ee.getReturnType().getKind();
            if (ret != TypeKind.VOID) {
                String name = ee.getSimpleName().toString();
                if (ret == TypeKind.BOOLEAN && name.startsWith("is")) {
                    return name.length() > 2 ? Introspector.decapitalize(name.substring(2)) : null;
                } else if (name.startsWith("get")) {
                    return name.length() > 3 ? Introspector.decapitalize(name.substring(3)) : null;
                }
            }
        }
        return null;
    }

    public static final class Literal extends ExpressionTree {

        private final Object value;
        private final int start;
        private final int end;

        Literal(Kind kind, Object value, int start, int end) {
            super(kind);
            this.value = value;
            this.start = start;
            this.end = end;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R,D> R accept(Scanner<R,D> scanner, D data) {
            return scanner.visitLiteral(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            switch (getKind()) {
                case NULL_LITERAL:
                    typeMirror = ctx.getTypes().getNullType();
                    break;
                case BOOLEAN_LITERAL:
                    typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.BOOLEAN);
                    break;
                case STRING_LITERAL:
                    typeMirror = ctx.getElements().getTypeElement("java.lang.String").asType();
                    break;
                case INT_LITERAL:
                    typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.INT);
                    break;
                case LONG_LITERAL:
                    typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.LONG);
                    break;
                case FLOAT_LITERAL:
                    typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.FLOAT);
                    break;
                case DOUBLE_LITERAL:
                    typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.DOUBLE);
                    break;
                default:
                    throw new AssertionError("Unexpected kind: " + getKind());
            }
        }
    }

    public static final class UnaryExpression extends ExpressionTree {

        private final ExpressionTree expression;
        private final int start;

        public UnaryExpression(Kind kind, ExpressionTree expression, int start) {
            super(kind);
            this.expression = expression;
            this.start = start;
        }

        public ExpressionTree getExpression() {
            return expression;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return expression.getEndPosition();
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitUnaryExpression(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            TypeMirror tm = unbox(ctx, expression.getTypeMirror(ctx));
            switch (getKind()) {
                case NOT:
                case EMPTY:
                    typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.BOOLEAN);
                    break;
                case PLUS:
                case MINUS:
                    switch (tm.getKind()) {
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            typeMirror = tm;
                            break;
                        default:
                            typeMirror = ctx.getTypes().getNoType(TypeKind.INT);
                    }
                    break;
                default:
                    throw new AssertionError("Unexpected kind: " + getKind());
            }
        }
    }

    public static final class BinaryExpression extends ExpressionTree {

        private final ExpressionTree left;
        private final ExpressionTree right;

        public BinaryExpression(Kind kind, ExpressionTree left, ExpressionTree right) {
            super(kind);
            this.left = left;
            this.right = right;
        }

        public ExpressionTree getLeftOperand() {
            return left;
        }

        public ExpressionTree getRightOperand() {
            return right;
        }

        @Override
        public int getStartPosition() {
            return left.getStartPosition();
        }

        @Override
        public int getEndPosition() {
            return right.getEndPosition();
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitBinaryExpression(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            TypeMirror leftTM = unbox(ctx, left.getTypeMirror(ctx));
            TypeMirror rightTM = unbox(ctx, right.getTypeMirror(ctx));
            switch (getKind()) {
                case EQUAL_TO:
                case NOT_EQUAL_TO:
                case GREATER_THAN:
                case LESS_THAN:
                case GREATER_THAN_EQUAL:
                case LESS_THAN_EQUAL:
                case MATCHES:
                case AND:
                case OR:
                case ELVIS:
                    typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.BOOLEAN);
                    break;
                case PLUS:
                    if (leftTM.getKind() == TypeKind.DECLARED && "java.lang.String".contentEquals(((TypeElement) ((DeclaredType) leftTM).asElement()).getQualifiedName())) {
                        typeMirror = leftTM;
                        break;
                    }
                case MINUS:
                case MULTIPLY:
                case DIVIDE:
                case REMAINDER:
                case POWER:
                    if (leftTM.getKind() == TypeKind.DOUBLE || rightTM.getKind() == TypeKind.DOUBLE) {
                        typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.DOUBLE);
                    } else if (leftTM.getKind() == TypeKind.FLOAT || rightTM.getKind() == TypeKind.FLOAT) {
                        typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.FLOAT);
                    } else if (leftTM.getKind() == TypeKind.LONG || rightTM.getKind() == TypeKind.LONG) {
                        typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.LONG);
                    } else {
                        typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.INT);
                    }
                    break;
                default:
                    throw new AssertionError("Unexpected kind: " + getKind());
            }
        }
    }

    public static final class InstanceOf extends ExpressionTree {

        private final ExpressionTree expression;
        private final TypeReference type;

        public InstanceOf(ExpressionTree expression, TypeReference type) {
            super(Kind.INSTANCE_OF);
            this.expression = expression;
            this.type = type;
        }

        public ExpressionTree getExpression() {
            return expression;
        }

        public TypeReference getType() {
            return type;
        }

        @Override
        public int getStartPosition() {
            return expression.getStartPosition();
        }

        @Override
        public int getEndPosition() {
            return type.getEndPosition();
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitInstanceOf(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.BOOLEAN);
        }
    }

    public static final class TernaryExpression extends ExpressionTree {

        private final ExpressionTree condition;
        private final ExpressionTree trueExpression;
        private final ExpressionTree falseExpression;

        public TernaryExpression(ExpressionTree condition, ExpressionTree trueExpression, ExpressionTree falseExpression) {
            super(Kind.TERNARY);
            this.condition = condition;
            this.trueExpression = trueExpression;
            this.falseExpression = falseExpression;
        }

        public ExpressionTree getCondition() {
            return condition;
        }

        public ExpressionTree getTrueExpression() {
            return trueExpression;
        }

        public ExpressionTree getFalseExpression() {
            return falseExpression;
        }

        @Override
        public int getStartPosition() {
            return condition.getStartPosition();
        }

        @Override
        public int getEndPosition() {
            return falseExpression.getEndPosition();
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitTernaryExpression(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            TypeMirror trueTM = unbox(ctx, trueExpression.getTypeMirror(ctx));
            TypeMirror falseTM = unbox(ctx, falseExpression.getTypeMirror(ctx));
            if (trueTM.getKind() == TypeKind.DOUBLE || falseTM.getKind() == TypeKind.DOUBLE) {
                typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.DOUBLE);
            } else if (trueTM.getKind() == TypeKind.FLOAT || falseTM.getKind() == TypeKind.FLOAT) {
                typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.FLOAT);
            } else if (trueTM.getKind() == TypeKind.LONG || falseTM.getKind() == TypeKind.LONG) {
                typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.LONG);
            } else if (trueTM.getKind() == TypeKind.INT || falseTM.getKind() == TypeKind.INT) {
                typeMirror = ctx.getTypes().getPrimitiveType(TypeKind.INT);
            } else {
                typeMirror = trueTM;
            }
        }
    }

    public static final class ParenthesizedExpression extends ExpressionTree {

        private final ExpressionTree expression;
        private final int start;
        private final int end;

        public ParenthesizedExpression(Kind kind, ExpressionTree expression, int start, int end) {
            super(kind);
            this.expression = expression;
            this.start = start;
            this.end = end;
        }

        public ExpressionTree getExpression() {
            return expression;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitParenthesizedExpression(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            typeMirror = expression.getTypeMirror(ctx);
        }
    }

    public static final class TypeReference extends ExpressionTree {

        private final String typeName;
        private final int typeStart;
        private final int start;
        private final int end;

        public TypeReference(String typeName, int typeStart, int start, int end) {
            super(Kind.TYPE_REFERENCE);
            this.typeName = typeName;
            this.typeStart = typeStart;
            this.start = start;
            this.end = end;
        }

        public String getTypeName() {
            return typeName;
        }

        public int getTypeStartPosition() {
            return typeStart;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitTypeReference(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            element = ctx.getElements().getTypeElement(typeName);
            if (element == null) {
                element = ctx.getElements().getTypeElement("java.lang." + typeName);
            }
            typeMirror = element != null ? element.asType() : ctx.getTypes().getNoType(TypeKind.NONE);
        }
    }

    public static final class ThisAccess extends ExpressionTree {

        private final int start;
        private final int end;

        public ThisAccess(int start, int end) {
            super(Kind.THIS_ACCESS);
            this.start = start;
            this.end = end;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitThisAccess(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            TypeElement te = ctx.getScope().getEnclosingClass();
            typeMirror = te != null ? te.asType() : ctx.getTypes().getNoType(TypeKind.NONE);
        }
    }

    public static final class MethodCall extends ExpressionTree {

        private final ExpressionTree callee;
        private final String identifier;
        private final List<? extends ExpressionTree> arguments;
        private final int start;
        private final int end;

        public MethodCall(ExpressionTree callee, String identifier, List<? extends ExpressionTree> arguments, int end) {
            super(Kind.METHOD_CALL);
            this.callee = callee;
            this.identifier = identifier;
            this.arguments = arguments;
            this.start = callee.getStartPosition();
            this.end = end;
        }

        public MethodCall(String identifier, List<? extends ExpressionTree> arguments, int start, int end) {
            super(Kind.METHOD_CALL);
            this.callee = null;
            this.identifier = identifier;
            this.arguments = arguments;
            this.start = start;
            this.end = end;
        }

        public ExpressionTree getCallee() {
            return callee;
        }

        public String getIdentifier() {
            return identifier;
        }

        public List<? extends ExpressionTree> getArguments() {
            return arguments;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitMethodCall(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            List<ExecutableElement> methods = null;
            DeclaredType dt = null;
            if (callee == null) {
                methods = ctx.getContextMethods();
            } else {
                TypeMirror calleeTM = callee.getTypeMirror(ctx);
                if (calleeTM.getKind() == TypeKind.DECLARED) {
                    dt = (DeclaredType) calleeTM;
                    methods = ElementFilter.methodsIn(((TypeElement) dt.asElement()).getEnclosedElements());
                }
            }
            if (methods != null && !methods.isEmpty()) {
                List<TypeMirror> argTypes = arguments.stream().map(arg -> arg.getTypeMirror(ctx)).collect(Collectors.toList());
                for (ExecutableElement ee : methods) {
                    TypeMirror enclType = dt != null ? dt : ee.getEnclosingElement().asType();
                    if (enclType.getKind() == TypeKind.DECLARED && identifier.contentEquals(ee.getSimpleName()) && ctx.getTrees().isAccessible(ctx.getScope(), ee, (DeclaredType) enclType)) {
                        ExecutableType et = (ExecutableType) ctx.getTypes().asMemberOf((DeclaredType) enclType, ee);
                        List<? extends TypeMirror> paramTypes = et.getParameterTypes();
                        if (ee.isVarArgs() && argTypes.size() >= paramTypes.size() || argTypes.size() == paramTypes.size()) {
                            boolean match = true;
                            Iterator<? extends TypeMirror> paramsIt = paramTypes.iterator();
                            TypeMirror param = paramsIt.hasNext() ? paramsIt.next() : null;
                            for (Iterator<TypeMirror> argsIt = argTypes.iterator(); match && argsIt.hasNext();) {
                                TypeMirror arg = argsIt.next();
                                if (!ctx.getTypes().isAssignable(arg, param)) {
                                    match = false;
                                }
                                if (paramsIt.hasNext()) {
                                    param = paramsIt.next();
                                }
                            }
                            if (match) {
                                element = ee;
                                typeMirror = et.getReturnType();
                                return;
                            }
                        }
                    }
                }
            }
            typeMirror = ctx.getTypes().getNoType(TypeKind.NONE);
        }
    }

    public static final class PropertyAccess extends ExpressionTree {

        private final ExpressionTree callee;
        private final String identifier;
        private final int start;
        private final int end;

        public PropertyAccess(ExpressionTree callee, String identifier, int end) {
            super(Kind.PROPERTY_ACCESS);
            this.callee = callee;
            this.identifier = identifier;
            this.start = callee.getStartPosition();
            this.end = end;
        }

        public PropertyAccess(String identifier, int start, int end) {
            super(Kind.PROPERTY_ACCESS);
            this.callee = null;
            this.identifier = identifier;
            this.start = start;
            this.end = end;
        }

        public ExpressionTree getCallee() {
            return callee;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitPropertyAccess(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            List<ExecutableElement> methods = null;
            DeclaredType dt = null;
            if (callee == null) {
                methods = ctx.getContextMethods();
            } else {
                TypeMirror calleeTM = callee.getTypeMirror(ctx);
                if (calleeTM.getKind() == TypeKind.DECLARED) {
                    dt = (DeclaredType) calleeTM;
                    methods = ElementFilter.methodsIn(((TypeElement) dt.asElement()).getEnclosedElements());
                }
            }
            if (methods != null && !methods.isEmpty()) {
                for (ExecutableElement ee : methods) {
                    TypeMirror enclType = dt != null ? dt : ee.getEnclosingElement().asType();
                    if (enclType.getKind() == TypeKind.DECLARED && identifier.equals(getPropertyName(ee)) && ctx.getTrees().isAccessible(ctx.getScope(), ee, (DeclaredType) enclType)) {
                        ExecutableType et = (ExecutableType) ctx.getTypes().asMemberOf((DeclaredType) enclType, ee);
                        element = ee;
                        typeMirror = et.getReturnType();
                        return;
                    }
                }
            }
            typeMirror = ctx.getTypes().getNoType(TypeKind.NONE);
        }
    }

    public static final class ArrayAccess extends ExpressionTree {

        private final ExpressionTree callee;
        private final ExpressionTree index;
        private final int end;

        public ArrayAccess(ExpressionTree callee, ExpressionTree index, int end) {
            super(Kind.ARRAY_ACCESS);
            this.callee = callee;
            this.index = index;
            this.end = end;
        }

        public ExpressionTree getCallee() {
            return callee;
        }

        public ExpressionTree getIndex() {
            return index;
        }

        @Override
        public int getStartPosition() {
            return callee.getStartPosition();
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitArrayAccess(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            TypeMirror calleeTM = callee.getTypeMirror(ctx);
            typeMirror = calleeTM.getKind() == TypeKind.ARRAY ? ((ArrayType) calleeTM).getComponentType() : ctx.getTypes().getNoType(TypeKind.NONE);
        }
    }

    public static final class BeanContextAccess extends ExpressionTree {

        private final TypeReference typeReference;
        private final int start;
        private final int end;

        public BeanContextAccess(TypeReference typeReference, int start, int end) {
            super(Kind.BEAN_CONTEXT_ACCESS);
            this.typeReference = typeReference;
            this.start = start;
            this.end = end;
        }

        public TypeReference getTypeReference() {
            return typeReference;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitBeanContextAccess(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            typeMirror = typeReference.getTypeMirror(ctx);
        }
    }

    public static final class EnvironmentAccess extends ExpressionTree {

        private final ExpressionTree propertyName;
        private final int start;
        private final int end;

        public EnvironmentAccess(ExpressionTree propertyName, int start, int end) {
            super(Kind.ENVIRONMENT_ACCESS);
            this.propertyName = propertyName;
            this.start = start;
            this.end = end;
        }

        public ExpressionTree getPropertyName() {
            return propertyName;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitEnvironmentAccess(this, data);
        }

        @Override
        protected void resolve(EvaluationContext ctx) {
            typeMirror = ctx.getElements().getTypeElement("java.lang.String").asType();
        }
    }

    public static final class Erroneous extends ExpressionTree {

        private final List<? extends ExpressionTree> errors;
        private final int start;
        private final int end;

        public Erroneous(List<? extends ExpressionTree> errors, int start, int end) {
            super(Kind.ERRONEOUS);
            this.errors = errors;
            this.start = start;
            this.end = end;
        }

        public List<? extends ExpressionTree> getErrorTrees() {
            return errors;
        }

        @Override
        public int getStartPosition() {
            int pos = start;
            for (ExpressionTree error : errors) {
                if (error.getStartPosition() < pos) {
                    pos = error.getStartPosition();
                }
            }
            return pos;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public <R, D> R accept(Scanner<R, D> scanner, D data) {
            return scanner.visitErroneous(this, data);
        }
    }

    public static enum Kind {
        NULL_LITERAL,
        BOOLEAN_LITERAL,
        STRING_LITERAL,
        INT_LITERAL,
        LONG_LITERAL,
        FLOAT_LITERAL,
        DOUBLE_LITERAL,
        UNARY_PLUS,
        UNARY_MINUS,
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        REMAINDER,
        POWER,
        EQUAL_TO,
        NOT_EQUAL_TO,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_EQUAL,
        LESS_THAN_EQUAL,
        MATCHES,
        AND,
        OR,
        NOT,
        EMPTY,
        ELVIS,
        TERNARY,
        PARENTHESIZED,
        TYPE_REFERENCE,
        INSTANCE_OF,
        THIS_ACCESS,
        METHOD_CALL,
        PROPERTY_ACCESS,
        ARRAY_ACCESS,
        BEAN_CONTEXT_ACCESS,
        ENVIRONMENT_ACCESS,
        ERRONEOUS
    }

    public static final class Path {

        private final ExpressionTree leaf;
        private final Path parent;

        public Path(ExpressionTree root) {
            this(null, root);
        }

        public Path(Path parent, ExpressionTree leaf) {
            this.parent = parent;
            this.leaf = leaf;
        }

        public ExpressionTree getLeaf() {
            return leaf;
        }

        public Path getParentPath() {
            return parent;
        }

        public static Path get(ExpressionTree root, ExpressionTree target) {
            if (root == null || target == null) {
                return null;
            }
            class PathFinder extends Scanner<Path, ExpressionTree> {
                private Path result;
                @Override
                public Path scan(Path path, ExpressionTree target) {
                    super.scan(path, target);
                    return result;
                }
                @Override
                public Path scan(ExpressionTree tree, ExpressionTree target) {
                    if (result == null) {
                        if (tree == target) {
                            result = new Path(getCurrentPath(), target);
                        } else {
                            super.scan(tree, target);
                        }
                    }
                    return result;
                }
                @Override
                public Path scan(Iterable<? extends ExpressionTree> nodes, ExpressionTree target) {
                    if (nodes != null && result == null) {
                        for (ExpressionTree node : nodes) {
                            scan(node, target);
                            if (result != null) {
                                break;
                            }
                        }
                    }
                    return result;
                }
            }
            Path path = new Path(root);
            return path.getLeaf() == target ? path : new PathFinder().scan(path, target);
        }

        public static Path get(ExpressionTree root, int offset) {
            if (root == null) {
                return null;
            }
            class PathFinder extends Scanner<Path, Void> {
                private Path result;
                @Override
                public Path scan(Path path, Void p) {
                    super.scan(path, p);
                    if (result == null && path.getLeaf().getStartPosition() < offset && offset <= path.getLeaf().getEndPosition()) {
                        result = path;
                    }
                    return result;
                }
                @Override
                public Path scan(ExpressionTree tree, Void p) {
                    if (tree != null && tree.getStartPosition() < offset && offset <= tree.getEndPosition()) {
                        super.scan(tree, p);
                        if (result == null) {
                            result = new Path(getCurrentPath(), tree);
                        }
                    }
                    return result;
                }
                @Override
                public Path scan(Iterable<? extends ExpressionTree> nodes, Void p) {
                    if (nodes != null && result == null) {
                        for (ExpressionTree node : nodes) {
                            scan(node, p);
                            if (result != null) {
                                break;
                            }
                        }
                    }
                    return result;
                }
            }
            Path path = new Path(root);
            return new PathFinder().scan(path, null);
        }
    }

    public static class Scanner<R, P> {

        private Path path;

        public Path getCurrentPath() {
            return path;
        }

        public R scan(Path path, P p) {
            this.path = path;
            try {
                return path.getLeaf().accept(this, p);
            } finally {
                this.path = null;
            }
        }

        public R scan(ExpressionTree tree, P p) {
            if (tree == null) {
                return null;
            }
            Path prev = path;
            path = new Path(path, tree);
            try {
                return tree.accept(this, p);
            } finally {
                path = prev;
            }
        }

        private R scanAndReduce(ExpressionTree node, P p, R r) {
            return reduce(scan(node, p), r);
        }

        public R scan(Iterable<? extends ExpressionTree> nodes, P p) {
            R r = null;
            if (nodes != null) {
                boolean first = true;
                for (ExpressionTree node : nodes) {
                    r = (first ? scan(node, p) : scanAndReduce(node, p, r));
                    first = false;
                }
            }
            return r;
        }

        private R scanAndReduce(Iterable<? extends ExpressionTree> nodes, P p, R r) {
            return reduce(scan(nodes, p), r);
        }

        public R reduce(R r1, R r2) {
            return r1;
        }

        public R visitLiteral(Literal node, P p) {
            return null;
        }

        public R visitUnaryExpression(UnaryExpression node, P p) {
            return scan(node.getExpression(), p);
        }

        public R visitBinaryExpression(BinaryExpression node, P p) {
            R r = scan(node.getLeftOperand(), p);
            r = scanAndReduce(node.getRightOperand(), p, r);
            return r;
        }

        public R visitInstanceOf(InstanceOf node, P p) {
            R r = scan(node.getExpression(), p);
            r = scanAndReduce(node.getType(), p, r);
            return r;
        }

        public R visitTernaryExpression(TernaryExpression node, P p) {
            R r = scan(node.getCondition(), p);
            r = scanAndReduce(node.getTrueExpression(), p, r);
            r = scanAndReduce(node.getFalseExpression(), p, r);
            return r;
        }

        public R visitParenthesizedExpression(ParenthesizedExpression node, P p) {
            return scan(node.getExpression(), p);
        }

        public R visitTypeReference(TypeReference node, P p) {
            return null;
        }

        public R visitThisAccess(ThisAccess node, P p) {
            return null;
        }

        public R visitMethodCall(MethodCall node, P p) {
            R r = scan(node.getCallee(), p);
            r = scanAndReduce(node.getArguments(), p, r);
            return r;
        }

        public R visitPropertyAccess(PropertyAccess node, P p) {
            return scan(node.getCallee(), p);
        }

        public R visitArrayAccess(ArrayAccess node, P p) {
            R r = scan(node.getCallee(), p);
            r = scanAndReduce(node.getIndex(), p, r);
            return r;
        }

        public R visitBeanContextAccess(BeanContextAccess node, P p) {
            return scan(node.getTypeReference(), p);
        }

        public R visitEnvironmentAccess(EnvironmentAccess node, P p) {
            return scan(node.getPropertyName(), p);
        }

        public R visitErroneous(Erroneous node, P p) {
            return scan(node.getErrorTrees(), p);
        }
    }
}
