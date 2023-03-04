/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssertTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;
import static com.sun.source.tree.Tree.Kind.*;

/**
 * Constant expression evaluator. 
 * Evaluates the constant expression encoded by tree at a certain TreePath. If the tree does not produce a constant expression,
 * the method returns {@code null}. If the tree produces a constant expression with value {@code null}, the method returns
 * {@link #NULL} as a placeholder.
 * <p/>
 * In general the evaluator may return boxed values of constants, {@link #NULL} to indicate the expression is a null constant, 
 * or a {@link #NOT_NULL} to indicate the expression is some non-null value, but not a primitive or boxed primitive value.
 * Note that {@link #NOT_NULL} does not undergo such sophisticated analysis as in Flow, where branches are checked.
 * <p/>
 * If resolving of constants is enabled, the method will attempt to resolve values of referenced symbols. If
 * enhanced processing is enabled, analysis beyond JLS specification is allowed. The enhancements include:
 * <ul>
 * <li>tracking of null and definitely non-null values
 * <li>casts to boxing reference types (i.e. Integer, Long)
 * <li>casts to assignment-compatible types
 * <li>inspection of values of final local variables and final non-static fields with initializers
 * <ul>
 * Results of enhanced analysis do not interfere with strict JLS processing, although results are cached. 
 * <p/>
 * A convenience method is provided for strict JLS processing.
 * <p/>
 * The evaluator tries to cache values for branching points (conditions in if, switch, cycles, assert) and for field/variable
 * initializers and assignments.
 * 
 * <p/>
 * Future enhancements may include pluggable evaluation of well-known functions, like Math.* etc (if pluggable, then
 * library support could declare const patterns or plug in an auxiliary evaluator).
 * <p/>
 * Annotations like @NotNull could be exploited to produce known-null/not null values
 * 
 * @author lahvac
 */
public class ArithmeticUtilities {

    /**
     * Evaluates constant expression at tree path 'tp'. Uses strict JLS rules, and optionally resolves constants.
     * 
     * @param info context
     * @param tp path for the expression to evaluate
     * @param resolveCompileTimeConstants if true, referenced symbols are resolved according to JLS compile-time constant definition
     * @return constant value, or {@code null} if the tree does not produce a compile-time constant expression.
     */
    public static Number compute(CompilationInfo info, TreePath tp, boolean resolveCompileTimeConstants) {
        Object o = compute(info, tp, resolveCompileTimeConstants, false);
        return o instanceof Number ? (Number)o : null;
    }
    
    /**
     * Placeholder value for 'null' constant value. New string is constructed so it is not interned and
     * could be compared using ==. "null" value is chosen so that NULL.toString() provides null for String
     * concatenation operation.
     */
    // something other than String, so instanceof- checks in clients won't detect the result as String const
    static final Object NULL = new Object() {
        @Override public String toString() { return "null"; }
    };
    
    /**
     * Identifies some value, which is not null.
     */
    public static final Object NOT_NULL = new Object();
    
    private static final Object UNKNOWN = new Object();
    
    public static boolean isRealValue(Object o) {
        return !(o == null || o == NULL || o == NOT_NULL);
    }
    
    public static boolean isNull(Object o) {
        return o == NULL;
    }
    
    public static boolean isNeverNull(Object o) {
        return o == NOT_NULL;
    }
    
    /**
     * Evaluates the constant expression encoded by tree at 'tp' path. See {@link ArithmeticUtilities} for more information.
     * <b>Always</b> check the result value for instanceof before using the value. Without the check, only != null test
     * is valid and has meaning "the expression is known to be constant".
     * 
     * @param info context
     * @param tp tree path for the expression
     * @param resolveCompileTimeConstants if false, symbol resolution is enabled
     * @param enhanced if true, improved analysis is enabled. If false, strict JLS rules apply.
     * @return 
     */
    public static Object compute(CompilationInfo info, TreePath tp, boolean resolveCompileTimeConstants, boolean enhanced) {
        // examine parent, if the expression is in some condition/cycle, it might be already evaluated
        boolean save = false;
        ElementValue v = null;
        Map<Object, ElementValue> cache = null;
        if (tp.getParentPath() != null) {
            Tree parentL = tp.getParentPath().getLeaf();
            switch (parentL.getKind()) {
                case IF: 
                case DO_WHILE_LOOP:
                case CONDITIONAL_EXPRESSION:
                case FOR_LOOP:
                case ASSIGNMENT:
                case VARIABLE:
                    save = true;
                    break;
                case ASSERT: 
                    save = ((AssertTree)parentL).getCondition() == tp.getLeaf();
                    break;
            }
            
            if (save) {
                cache = VisitorImpl.getValueCache(info);
                v = cache.get(tp.getLeaf());
                if (v != null) {
                    if (enhanced && v.constant != null) {
                        return v.constant == UNKNOWN ? null : v.constant;
                    } else if (!enhanced && v.jlsConstant != null) {
                        return v.jlsConstant == UNKNOWN ? null : v.jlsConstant;
                    }
                }
                
            }
        }
        Object o;
        try {
            o = new VisitorImpl(info, resolveCompileTimeConstants, enhanced).scan(tp, null);
        } catch (ArithmeticException | IndexOutOfBoundsException | IllegalArgumentException ex) {
            o = null;
        }
        if (save) {
            if (v == null) {
                v = new ElementValue();
                cache.put(tp.getLeaf(), v);
            }
            if (enhanced) {
                v.constant = o == null ? UNKNOWN : o;
            } else {
                v.jlsConstant = o == null ? UNKNOWN : o;
            }
        }
        return o;
        
    }

    private static final class VisitorImpl extends ErrorAwareTreePathScanner<Object, Void> {
        // PENDING: instanceof String could be handled
	private static final Set<Kind> ACCEPTED_KINDS = EnumSet.of(
		MULTIPLY, DIVIDE, REMAINDER, PLUS, MINUS,
		LEFT_SHIFT, RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT, AND, XOR,
		OR, UNARY_MINUS, UNARY_PLUS, PARENTHESIZED, IDENTIFIER,
		MEMBER_SELECT, 
                INT_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL, CHAR_LITERAL, BOOLEAN_LITERAL, NULL_LITERAL, STRING_LITERAL,
                
                // boolean ops
                CONDITIONAL_AND, CONDITIONAL_OR, CONDITIONAL_EXPRESSION,
                
                // relational ops
                EQUAL_TO, NOT_EQUAL_TO,
                GREATER_THAN, GREATER_THAN_EQUAL, LESS_THAN, LESS_THAN_EQUAL,
                TYPE_CAST, NEW_CLASS, NEW_ARRAY
        );

        private static final EnumSet<TypeKind> PRIMITIVE_KINDS = EnumSet.of(
                    TypeKind.BOOLEAN, TypeKind.CHAR,
                    TypeKind.BYTE, TypeKind.SHORT, TypeKind.INT, TypeKind.LONG,
                    TypeKind.DOUBLE, TypeKind.FLOAT
                );
        
        private final CompilationInfo info;
        private final boolean resolveCompileTimeConstants;
        private final boolean enhanceProcessing;

        public VisitorImpl(CompilationInfo info, boolean resolveCompileTimeConstants, boolean enhanceProcessing) {
            this.info = info;
            this.resolveCompileTimeConstants = resolveCompileTimeConstants;
            this.enhanceProcessing = enhanceProcessing;
        }

	@Override
	public Object scan(TreePath tree, Void p) {
	    if (!ACCEPTED_KINDS.contains(tree.getLeaf().getKind())) return null;
	    return super.scan(tree, p);
	}

	@Override
	public Object scan(Tree tree, Void p) {
	    if (tree == null) return null;
	    if (!ACCEPTED_KINDS.contains(tree.getKind())) return null;
	    return super.scan(tree, p);
	}
        
        private Object resolveTypeCast(TypeCastTree node, TypeMirror target, Object result, Void p) {
            if (target.getKind() != TypeKind.DECLARED) {
                return null;
            }
            DeclaredType dt = (DeclaredType)target;
            TypeElement e = (TypeElement)dt.asElement();
            if (enhanceProcessing) {
                // accept null constant typecasted to anything as null
                if (result == NULL) {
                    return NULL;
                } else if (result == NOT_NULL) {
                    // some unspecified reference type...
                    return result;
                }
            }
            String qn = e.getQualifiedName().toString();
            // type casts to String are permitted by JLS 15.28
            if ("java.lang.String".equals(qn)) { // NOI18N
                return result instanceof String ? result : null;
            } else if (!enhanceProcessing) {
                // other typecasts are not lised in JLS 15.28
                return null;
            }
            TypeMirror castee = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));
            if (!Utilities.isValidType(castee)) {
                return null;
            }
            if (info.getTypes().isAssignable(castee, target)) {
                return result;
            }
            // a constant of primitive type may be casted / wrapped to the wrapper type
            switch (qn) {
                case "java.lang.Boolean": // NOI18N
                    if (result instanceof Boolean) return result;
                    break;
                case "java.lang.Byte": // NOI18N
                    // the casted expression may be typed as Byte or byte; 
                    if (result instanceof Number && castee != null && castee.getKind() == TypeKind.BYTE) 
                        return ((Number)result).byteValue();
                    break;
                case "java.lang.Character": // NOI18N
                    if (result instanceof Number && castee != null && castee.getKind() == TypeKind.CHAR) 
                        return Character.valueOf((char)((Number)result).intValue());
                    break;
                case "java.lang.Double": // NOI18N
                    if (result instanceof Number && castee != null && castee.getKind() == TypeKind.DOUBLE) 
                        return ((Number)result).doubleValue();
                    break;
                case "java.lang.Float": // NOI18N
                    if (result instanceof Number && castee != null && castee.getKind() == TypeKind.FLOAT) 
                        return ((Number)result).floatValue();
                    break;
                case "java.lang.Integer": // NOI18N
                    if (result instanceof Number && castee != null && castee.getKind() == TypeKind.INT) 
                        return ((Number)result).intValue();
                    break;
                case "java.lang.Long": // NOI18N
                    if (result instanceof Number && castee != null && castee.getKind() == TypeKind.LONG) 
                        return ((Number)result).longValue();
                    break;
                case "java.lang.Short": // NOI18N
                    if (result instanceof Number && castee != null && castee.getKind() == TypeKind.SHORT) 
                        return ((Number)result).shortValue();
                    break;
            }
            return null;
        }

        @Override
        public Object visitTypeCast(TypeCastTree node, Void p) {
            Object op = scan(node.getExpression(), p);
            if (op == null) {
                return null;
            }
            Object result = null;
            TypeMirror tm = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getType()));
            if (!Utilities.isValidType(tm)) {
                return null;
            }
            // casting to some non-char primitive type, perform unary numeric promotion JLS 5.6.1
            if (tm.getKind() != TypeKind.CHAR && op instanceof Character) {
                op = Integer.valueOf(((Character)op).charValue());
            }
            // accept unboxing conversion, primitive conversion
            switch (tm.getKind()) {
                case BOOLEAN:
                    if (op instanceof Boolean) result = op;
                    break;
                case BYTE:
                    if (op instanceof Number)  result = ((Number)op).byteValue();
                    break;
                case CHAR:
                    if (op instanceof Character) result = op;
                    if (op instanceof Number)  result = Character.valueOf((char)((Number)op).intValue());
                    break;
                case DOUBLE:
                    if (op instanceof Number)  result = ((Number)op).doubleValue();
                    break;
                case FLOAT:
                    if (op instanceof Number)  result = ((Number)op).floatValue();
                    break;
                case INT:
                    if (op instanceof Number)  result = ((Number)op).intValue();
                    break;
                case LONG:
                    if (op instanceof Number)  result = ((Number)op).longValue();
                    break;
                case SHORT:
                    if (op instanceof Number)  result = ((Number)op).shortValue();
                    break;
                default:
                    return resolveTypeCast(node, tm, op, p);
                    
            }
            return result;
        }

        /**
         * If enhanced processing is on, report a NOT_NULL, since the result
         * of new arr expression may not be null.
         */
        @Override
        public Object visitNewArray(NewArrayTree node, Void p) {
            return enhanceProcessing ? NOT_NULL : null;
        }

        @Override
        public Object visitNewClass(NewClassTree node, Void p) {
            return enhanceProcessing ? NOT_NULL : null;
        }

        @Override
        public Object visitLiteral(LiteralTree node, Void p) {
            if (node.getKind() == NULL_LITERAL) {
                return enhanceProcessing ? NULL : null;
            } 
            return node.getValue();
        }

        @Override
        public Object visitIdentifier(IdentifierTree node, Void p) {
            return resolve();
        }

        @Override
        public Object visitMemberSelect(MemberSelectTree node, Void p) {
            return resolve();
        }
        
        private static Map<Object, ElementValue> getValueCache(CompilationInfo info) {
            Map<Object, ElementValue> cache = (Map)info.getCachedValue(CONST_EVAL_KEY);
            if (cache == null) {
                cache = new HashMap<Object, ElementValue>(7);
                info.putCachedValue(CONST_EVAL_KEY, cache, CompilationInfo.CacheClearPolicy.ON_TASK_END);
            }
            return cache;
        }
        
        /**
         * Attempts to resolve Elements value. For JLS-strict mode, values are only resolved for fields. Null value
         * or known-not-null values are ignored. For enhanced mode, null & non-null may be returned for further processing
         * 
         * @param info
         * @param path
         * @param enhanceProcessing
         * @return 
         */
        private static Object resolveElementValue(CompilationInfo info, TreePath path, boolean enhanceProcessing) {
            Element el = info.getTrees().getElement(path);
            if (el == null) {
                return null;
            }
            Map<Object, ElementValue> cache = getValueCache(info);
            ElementValue entry = cache.get(el);
            if (entry != null) {
                Object v = enhanceProcessing ? entry.constant : entry.jlsConstant;
                if (v != null) {
                    return v != UNKNOWN ? v : null;
                }
            }
            
            if (el == null) {
                return null;
            }
            Object obj = null;
            if (el.getKind() == ElementKind.FIELD) {
                obj = ((VariableElement) el).getConstantValue();
                // if enhanced processing is enabled, give other chance to fields that are static final
                if (obj == null && (!el.getModifiers().contains(Modifier.FINAL) || !enhanceProcessing)) {
                    obj = UNKNOWN;
                }
            } else if (el.getKind() == ElementKind.LOCAL_VARIABLE) {
                obj = ((VariableElement) el).getConstantValue();
                if (obj == null && (!el.getModifiers().contains(Modifier.FINAL) || !enhanceProcessing)) {
                    obj = UNKNOWN;
                }
            }
            EVAL: if (obj == null) {
                TreePath varPath = info.getTrees().getPath(el);
                if (varPath != null && varPath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                    // #262309: if variable is errnoeously referenced from its own initializer, we must not recurse.
                    for (Tree t : path) {
                        if (t == varPath.getLeaf()) {
                            break EVAL;
                        }
                        if (StatementTree.class.isAssignableFrom(t.getKind().asInterface())) {
                            break;
                        }
                    }
                    VariableTree vt = (VariableTree)varPath.getLeaf();
                    if (vt.getInitializer() != null) {
                        VisitorImpl recurse = new VisitorImpl(info, true, enhanceProcessing);
                        try {
                            obj = recurse.scan(new TreePath(varPath, vt.getInitializer()), null);
                        } catch (ArithmeticException | IndexOutOfBoundsException | IllegalArgumentException ex) {
                            // no op, obj is already null.
                        }
                    }
                }
            } else if (entry == null) {
                // obj was resolved as a constant value and entry still does not exist for it -> save memory
                return obj != UNKNOWN ? obj : null;
            }
            
            if (entry == null) {
                entry = new ElementValue();
                cache.put(el, entry);
            }
            if (obj == null) {
                obj = UNKNOWN;
            }
            if (enhanceProcessing) {
                entry.constant = obj;
            } else {
                entry.jlsConstant = obj;
                if (obj == NULL || obj == NOT_NULL) {
                    return null;
                }
            }
            return obj != UNKNOWN ? obj : null;
        }

        private Object resolve() {
            if (!resolveCompileTimeConstants) {
                return null;
            }
            return resolveElementValue(info, getCurrentPath(), enhanceProcessing);
        }
        
        private interface BinaryOp {
            public Object eval(Object left, Object right);
        }
        
        private static final BinaryOp OP_EQUAL  = new BinaryOp() { public Object eval(Object left, Object right) { return left.equals(right); }};
        
        private Object numericBinaryOp(BinaryOp op, Object left, Object right) {
            return null;
        }

        @Override
        public Object visitConditionalExpression(ConditionalExpressionTree node, Void p) {
            Object condition = scan(node.getCondition(), p);
            if (condition == Boolean.TRUE) {
                return scan(node.getTrueExpression(), p);
            } else if (condition == Boolean.FALSE) {
                return scan(node.getFalseExpression(), p);
            }
            if (enhanceProcessing) {
                Object first = scan(node.getTrueExpression(), p);
                Object second = scan(node.getFalseExpression(), p);
                if (first == NULL && second == NULL) {
                    return NULL;
                } else if (first != null && second != null) {
                    return NOT_NULL;
                }
            }
            // indeterminate
            return null;
        }
        
        @Override
        public Object visitBinary(BinaryTree node, Void p) {
            Object left  = scan(node.getLeftOperand(), p);
            Object right = scan(node.getRightOperand(), p);
            
            // JSL 5.6.2, binary numeric promotion + JLS 5.1.2, widening primitive conversion for char values.
            // other value types are handled by the Number class in JDK. Chars may be promoted further to float / double.
            if (left instanceof Character && !(right instanceof String)) {
                left = Integer.valueOf(((Character)left).charValue());
            }
            if (right instanceof Character && !(left instanceof String)) {
                right = Integer.valueOf(((Character)right).charValue());
            }
            if (left != null && right != null) {
                Object result = null;
                switch (node.getKind()) {
                    case EQUAL_TO:
                        if (left instanceof Number && right instanceof Number) {
                            result = numericBinaryOp(OP_EQUAL, left, right);
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() == rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() == rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() == rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() == rn.intValue();
                            } else {
                                return null;
                            }
                        } else if (left instanceof Boolean && right instanceof Boolean) {
                            return left.equals(right);
                        } else if (left == NULL || right == NULL) {
                            // cannot accept primitives, boxing conversion does not apply
                            TypeMirror m = info.getTrees().getTypeMirror(
                                    new TreePath(getCurrentPath(), 
                                        left == NULL ?  node.getRightOperand() : 
                                                        node.getLeftOperand()
                            ));
                            if (Utilities.isValidType(m) && !PRIMITIVE_KINDS.contains(m.getKind())) {
                                result = left == right;
                            }
                        }
                        break;
                        
                    case NOT_EQUAL_TO:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() != rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() != rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() != rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() != rn.intValue();
                            } else {
                                return null;
                            }
                        } else if (left instanceof Boolean && right instanceof Boolean) {
                            return left.equals(right);
                        } else if (enhanceProcessing && (left == NULL || right == NULL)) {
                            // cannot accept primitives, boxing conversion does not apply
                            TypeMirror m = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), 
                                    left == NULL ? node.getRightOperand() : node.getLeftOperand()));
                            if (Utilities.isValidType(m) && !PRIMITIVE_KINDS.contains(m.getKind())) {
                                result = left != right;
                            }
                        }
                        break;
                        
                    case LESS_THAN:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() < rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() < rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() < rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() < rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;
                    case LESS_THAN_EQUAL:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() <= rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() <= rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() <= rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() <= rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;
                    case GREATER_THAN:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() > rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() > rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() > rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() > rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;
                    case GREATER_THAN_EQUAL:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() >= rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() >= rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() >= rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() >= rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;
                        
                    case MULTIPLY:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() * rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() * rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() * rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() * rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;

                    case DIVIDE:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() / rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() / rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() / rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() / rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        boolean a = true;
                        boolean b = false;
                        boolean c = a & b;
                        break;
                        
                    case REMAINDER:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() % rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() % rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() % rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() % rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;

                    case MINUS:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() - rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() - rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() - rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() - rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;

                    case LEFT_SHIFT:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() << rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() << rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;

                    case RIGHT_SHIFT:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() >> rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() >> rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;

                    case UNSIGNED_RIGHT_SHIFT:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() >>> rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() >>> rn.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;

                    // PLUS is also supported for String operands. `null' value is represented by String containing "null",
                    // so it will produce the correct concatenation result.
                    case PLUS:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Double || right instanceof Double) {
                                result = ln.doubleValue() + rn.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = ln.floatValue() + rn.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() + rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() + rn.intValue();
                            } else {
                                return null;
                            }
                        } else if (left instanceof String) {
                            if (right != NOT_NULL) {
                                result = (String)left + right;
                            }
                        } else if (right instanceof String) {
                            if (left != NOT_NULL) {
                                result = left + (String)right;
                            }
                        }
                        break;

                    // AND, OR apply as well to booleans
                    case CONDITIONAL_AND:
                        if (left instanceof Boolean && right instanceof Boolean) {
                            result = ((Boolean)left) && ((Boolean)right);
                        }
                        break;
                    case CONDITIONAL_OR:
                        if (left instanceof Boolean && right instanceof Boolean) {
                            result = ((Boolean)left) || ((Boolean)right);
                        }
                        break;
                    case XOR:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() ^ rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() ^ rn.intValue();
                            } else {
                                return null;
                            }
                        } else if (left instanceof Boolean && right instanceof Boolean) {
                            result = ((Boolean)left) ^ ((Boolean)right);
                        }
                        break;

                    case AND:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() & rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() & rn.intValue();
                            } else {
                                return null;
                            }
                        } else if (left instanceof Boolean && right instanceof Boolean) {
                            result = ((Boolean)left) & ((Boolean)right);
                        }
                        break;

                    case OR:
                        if (left instanceof Number && right instanceof Number) {
                            Number ln = (Number)left;
                            Number rn = (Number)right;
                            if (left instanceof Long || right instanceof Long) {
                                result = ln.longValue() | rn.longValue();
                            } else if (integerLike(ln) || integerLike(rn)) {
                                result = ln.intValue() | rn.intValue();
                            } else {
                                return null;
                            }
                        } else if (left instanceof Boolean && right instanceof Boolean) {
                            result = ((Boolean)left) | ((Boolean)right);
                        }
                        break;
                }

                return result;
            }

            return null;
        }

        @Override
        public Object visitUnary(UnaryTree node, Void p) {
            Object op  = scan(node.getExpression(), p);
            if (op != null) {
                Object result = null;
                if (op instanceof Character) {
                    op = Integer.valueOf(((Character)op).charValue());
                }
                switch (node.getKind()) {
                    case BITWISE_COMPLEMENT:
                        if (op instanceof Long) {
                            result = ~((Long)op).longValue();
                        } else if (op instanceof Number && integerLike((Number)op)) {
                            result = ~(((Number)op).intValue());
                        }
                        break;
                    case LOGICAL_COMPLEMENT:
                        if (op instanceof Boolean) {
                            result = !((Boolean)op).booleanValue();
                        }
                        break;
                    case UNARY_MINUS:
                        if (op instanceof Number) {
                            Number nop = (Number)op;
                            if (op instanceof Double) {
                                result = -nop.doubleValue();
                            } else if (op instanceof Float) {
                                result = -nop.floatValue();
                            } else if (op instanceof Long) {
                                result = -nop.longValue();
                            } else if (integerLike(nop)) {
                                result = -nop.intValue();
                            } else {
                                return null;
                            }
                        }
                        break;
                    case UNARY_PLUS:
                        if (op instanceof Number) {
                            result = op;
                        }
                        break;
                }
                return result;
            }

            return super.visitUnary(node, p);
        }

        private static boolean integerLike(Number n) {
            return n instanceof Integer || n instanceof Short || n instanceof Byte;
        }
    }
    
    /**
     * Performs implicit conversion of a literal value to the target type.
     * Returns {@code null} if the conversion is not possible (incovertible, lossy
     * conversion etc).
     * 
     * @param val the value
     * @param convertTo the target type
     * @return new literal value
     */
    public static Object implicitConversion(CompilationInfo info, Object val, TypeMirror convertTo) {
        if (!convertTo.getKind().isPrimitive()) {
            if (Utilities.isPrimitiveWrapperType(convertTo)) {
                convertTo = info.getTypes().unboxedType(convertTo);
            } else if (isNull(val)) { 
                return val;
            } else {
// possibly a String ?
                if (val instanceof String && convertTo.getKind() == TypeKind.DECLARED) {
                    Element el = info.getTypes().asElement(convertTo);
                    if (el.getKind() == ElementKind.CLASS && ((TypeElement)el).getQualifiedName().contentEquals("java.lang.String")) {
                        return val;
                    }
                }
                return null;
            }
        }
        switch (convertTo.getKind()) {
            case BOOLEAN:
                if (val instanceof Boolean) {
                    return val;
                } else {
                    return null;
                }
            case BYTE: {
                if (val instanceof Short || val instanceof Long || val instanceof Integer) {
                    long l = ((Number)val).longValue();
                    if (l >= 0 && l <= 0xff) {
                        return (byte)l;
                    }
                } else if (val instanceof Character) {
                    return (byte)((Character)val).charValue();
                }
                
                return null;
            }
            case CHAR:
                if (val instanceof Character) {
                    return val;
                } else if (val instanceof Short) {
                    short n = (Short)val;
                    if (n < (1 << 15)) {
                        return Character.valueOf((char)n);
                    }
                } else if (val instanceof Integer) {
                    int n = (Integer)val;
                    if ( n < (1 << 16)) {
                        return Character.valueOf((char)n);
                    }
                }
                return null;
                
            case DOUBLE:
                if (val instanceof Number) {
                    return ((Number)val).doubleValue();
                } else if (val instanceof Character) {
                    return Double.valueOf((Character)val);
                }
                return null;
            case FLOAT:
                if (val instanceof Double) {
                    return null;
                }
                if (val instanceof Number) {
                    return ((Number)val).floatValue();
                } else if (val instanceof Character) {
                    return Double.valueOf((Character)val);
                }
                return null;
                
            case INT:
                if (val instanceof Short || val instanceof Long || val instanceof Integer) {
                    long l = ((Number)val).longValue();
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                        return (int)l;
                    }
                } else if (val instanceof Character) {
                    return (int)((Character)val);
                }
                return null;
            case LONG:
                if (val instanceof Character) {
                    return (long)((Character)val).charValue();
                } else if (val instanceof Double || val instanceof Float) {
                    return null;
                } else if (val instanceof Number) {
                    return ((Number)val).longValue();
                }
                return null;
                
            case SHORT:
                if (val instanceof Short || val instanceof Long || val instanceof Integer) {
                    long l = ((Number)val).longValue();
                    if (l >= Short.MIN_VALUE && l <= Short.MAX_VALUE) {
                        return (short)l;
        }
                } else if (val instanceof Character) {
                    return (short)((Character)val).charValue();
    }
                return null;
            default:
                return null;
        }
    }
    
    private static final String CONST_EVAL_KEY = ArithmeticUtilities.class.getName();
    
    /**
     * Cached information about an element once seen; contains the value, 
     */
    private static class ElementValue {
        private Object  jlsConstant;
        private Object  constant;
    }
}
