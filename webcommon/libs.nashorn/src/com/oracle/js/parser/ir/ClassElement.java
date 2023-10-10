/*
 * Copyright (c) 2022, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.js.parser.ir;

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * IR representation for class elements.
 */
public final class ClassElement extends PropertyNode {

    /**
     * Class element kinds types: method, accessor (getter, setter), field and static init.
     */
    private static final int KIND_METHOD = 1 << 0;
    private static final int KIND_ACCESSOR = 1 << 1;
    private static final int KIND_FIELD = 1 << 2;
    private static final int KIND_STATIC_INIT = 1 << 3;
    private static final int KIND_AUTO_ACCESSOR = 1 << 4;

    /** Class element kind. */
    private final int kind;

    private ClassElement(long token, int finish, int kind, Expression key, Expression value, FunctionNode get, FunctionNode set, List<Expression> decorators,
                    boolean hasComputedKey, boolean isStatic) {
        super(token, finish, key, value, get, set, isStatic, hasComputedKey, decorators);
        this.kind = kind;
    }

    private ClassElement(ClassElement element, int kind, Expression key, Expression value, FunctionNode get, FunctionNode set, List<Expression> decorators,
                    boolean hasComputedKey, boolean isStatic) {
        super(element.getToken(), element.finish, key, value, get, set, isStatic, hasComputedKey, decorators);
        this.kind = kind;
    }

    /**
     * Create a Method class element.
     *
     * @param token token.
     * @param finish finish.
     * @param key The name of the method.
     * @param value The value of the method.
     * @param decorators The decorators of the method. Optional.
     * @param isStatic static method.
     * @param hasComputedKey has computed key.
     * @return A ClassElement node representing a method.
     */
    public static ClassElement createMethod(long token, int finish, Expression key, Expression value, List<Expression> decorators, boolean isStatic, boolean hasComputedKey) {
        return new ClassElement(token,
                        finish,
                        KIND_METHOD,
                        key,
                        value,
                        null,
                        null,
                        decorators,
                        hasComputedKey,
                        isStatic);
    }

    /**
     * Create an Accessor (i.e., get/set) element.
     *
     * @param token token.
     * @param finish finish.
     * @param key The name of the accessor.
     * @param get The getter of the accessor. Optional.
     * @param set The setter of the accessor. Optional.
     * @param decorators The decorators of the accessor. Optional.
     * @param isStatic static accessor.
     * @param hasComputedKey has computed key.
     * @return A ClassElement node representing an accessor (getter, setter).
     */
    public static ClassElement createAccessor(long token,
                    int finish,
                    Expression key,
                    FunctionNode get,
                    FunctionNode set,
                    List<Expression> decorators,
                    boolean isStatic,
                    boolean hasComputedKey) {
        return new ClassElement(token,
                        finish,
                        KIND_ACCESSOR,
                        key,
                        null,
                        get,
                        set,
                        decorators,
                        hasComputedKey,
                        isStatic);
    }

    /**
     * Create a class field element.
     *
     * @param token token.
     * @param finish finish.
     * @param key The name of the field.
     * @param initialize The initialization value of the field. Optional.
     * @param decorators The decorators of the field. Optional.
     * @param isStatic static field,
     * @param hasComputedKey has computed key.
     * @return A ClassElement node representing a field.
     */
    public static ClassElement createField(long token,
                    int finish,
                    Expression key,
                    Expression initialize,
                    List<Expression> decorators,
                    boolean isStatic,
                    boolean hasComputedKey) {
        return new ClassElement(token, finish, KIND_FIELD, key, initialize, null, null, decorators, hasComputedKey, isStatic);
    }

    /**
     * Create the class default constructor.
     *
     * @param token token.
     * @param finish finish.
     * @param key class name.
     * @param value value.
     * @return A ClassElement node representing a default constructor.
     */
    public static ClassElement createDefaultConstructor(long token, int finish, Expression key, Expression value) {
        return new ClassElement(token, finish, KIND_METHOD, key, value, null, null, Collections.emptyList(), false, false);
    }

    /**
     * Create a static initializer element.
     *
     * @param token token.
     * @param finish finish.
     * @param functionNode function node.
     * @return A ClassElement node representing a static initializer.
     */
    public static ClassElement createStaticInitializer(long token, int finish, FunctionNode functionNode) {
        return new ClassElement(token, finish, KIND_STATIC_INIT, null, functionNode, null, null, Collections.emptyList(), false, true);
    }

    /**
     * Create an auto-accessor class element.
     *
     * @param token token.
     * @param finish finish.
     * @param key key name.
     * @param initializer initializer body.
     * @param classElementDecorators decorators.
     * @param isStatic is static.
     * @param hasComputedKey has computed key.
     * @return A ClassElement node representing an auto-accessor.
     */
    public static ClassElement createAutoAccessor(long token, int finish, Expression key, FunctionNode initializer, List<Expression> classElementDecorators, boolean isStatic, boolean hasComputedKey) {
        return new ClassElement(token, finish, KIND_AUTO_ACCESSOR, key, initializer, null, null, classElementDecorators, hasComputedKey, isStatic);
    }

    @Override
    public Node accept(NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterClassElement(this)) {
            ClassElement element = setKey(key == null ? null : (Expression) key.accept(visitor)).setValue(value == null ? null : (Expression) value.accept(visitor)).setGetter(
                            getter == null ? null : (FunctionNode) getter.accept(visitor)).setSetter(setter == null ? null : (FunctionNode) setter.accept(visitor));
            if (decorators != null) {
                element = element.setDecorators(Node.accept(visitor, new ArrayList<>()));
            } else {
                element = element.setDecorators(null);
            }
            return visitor.leaveClassElement(element);
        }
        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterClassElement(this);
    }

    public List<Expression> getDecorators() {
        return Collections.unmodifiableList(decorators);
    }

    public ClassElement setDecorators(List<Expression> decorators) {
        if (this.decorators == decorators) {
            return this;
        }
        return new ClassElement(this, kind, key, value, getter, setter, decorators, computed, isStatic);
    }

    @Override
    public ClassElement setGetter(FunctionNode get) {
        if (this.getter == get) {
            return this;
        }
        return new ClassElement(this, kind, key, value, get, setter, decorators, computed, isStatic);
    }

    public ClassElement setKey(final Expression key) {
        if (this.key == key) {
            return this;
        }
        return new ClassElement(this, kind, key, value, getter, setter, decorators, computed, isStatic);
    }

    @Override
    public ClassElement setSetter(FunctionNode set) {
        if (this.setter == set) {
            return this;
        }
        return new ClassElement(this, kind, key, value, getter, set, decorators, computed, isStatic);
    }

    @Override
    public ClassElement setValue(Expression value) {
        if (this.value == value) {
            return this;
        }
        return new ClassElement(this, kind, key, value, getter, setter, decorators, computed, isStatic);
    }

    public boolean isAccessor() {
        return (kind & KIND_ACCESSOR) != 0;
    }

    public boolean isAutoAccessor() {
        return (kind & KIND_AUTO_ACCESSOR) != 0;
    }

    public boolean isClassField() {
        return (kind & KIND_FIELD) != 0;
    }

    /**
     * Returns true if the class element is one of: field, auto accessor.
     */
    public boolean isClassFieldOrAutoAccessor() {
        return isClassField() || isAutoAccessor();
    }

    public boolean isClassStaticBlock() {
        return (kind & KIND_STATIC_INIT) != 0;
    }

    public boolean isMethod() {
        return (kind & KIND_METHOD) != 0;
    }

    /**
     * Returns true if the class element is one of: method, getter, setter, auto accessor.
     */
    public boolean isMethodOrAccessor() {
        return isMethod() || isAccessor() || isAutoAccessor();
    }

    public boolean isPrivate() {
        return (key instanceof IdentNode && ((IdentNode) key).isPrivate());
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public void toString(StringBuilder sb, boolean printType) {
        if (decorators != null) {
            for (Expression decorator : decorators) {
                sb.append("@");
                decorator.toString(sb, printType);
                sb.append(" ");
            }
        }
        if (isStatic()) {
            sb.append("static ");
            if (isClassStaticBlock()) {
                sb.append("{}");
            }
        }
        if (isAutoAccessor()) {
            sb.append("accessor ");
        }
        if (isMethod()) {
            toStringKey(sb, printType);
            ((FunctionNode) value).toString(sb, printType);
        }
        if (isAccessor()) {
            if (getter != null) {
                sb.append("get ");
                toStringKey(sb, printType);
                getter.toString(sb, printType);
            }
            if (setter != null) {
                sb.append("set ");
                toStringKey(sb, printType);
                setter.toString(sb, printType);
            }
        }
        if (isClassFieldOrAutoAccessor()) {
            toStringKey(sb, printType);
            if (value != null) {
                sb.append(" = ");
                value.toString(sb, printType);
            }
        }
    }

    private void toStringKey(final StringBuilder sb, final boolean printType) {
        if (computed) {
            sb.append('[');
        }
        key.toString(sb, printType);
        if (computed) {
            sb.append(']');
        }
    }

}