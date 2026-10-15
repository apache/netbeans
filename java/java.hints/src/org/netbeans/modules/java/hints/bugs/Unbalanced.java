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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerOptions;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class Unbalanced {
    private static final String SEEN_KEY = Unbalanced.class.getName() + ".seen"; // NOI18N
    
    private static boolean isAcceptable(Element el) {
        return el != null && (el.getKind() == ElementKind.LOCAL_VARIABLE || (el.getKind() == ElementKind.FIELD && el.getModifiers().contains(Modifier.PRIVATE)));
    }
    
    private static void record(CompilationInfo info, VariableElement el, State... states) {
        Map<Element, Set<State>> cache = (Map<Element, Set<State>>)info.getCachedValue(SEEN_KEY);

        if (cache == null) {
            info.putCachedValue(SEEN_KEY, cache = new HashMap<>(), CompilationInfo.CacheClearPolicy.ON_CHANGE);
        }

        cache.computeIfAbsent(el, k -> EnumSet.noneOf(State.class))
             .addAll(Arrays.asList(states));
    }

    private static ErrorDescription produceWarning(HintContext ctx, String keyBase) {
        Element el = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (el == null) return null;

        Map<Element, Set<State>> cache = (Map<Element, Set<State>>)ctx.getInfo().getCachedValue(SEEN_KEY);

        if (cache == null) return null;

        Set<State> state = cache.remove(el);

        if (state == null) return null;

        if (state.isEmpty() || state.size() == 2) return null;

        String warningKey = keyBase + state.iterator().next().name();
        String warning = NbBundle.getMessage(Array.class, warningKey, el.getSimpleName().toString());

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), warning);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Unbalanced.Array",
          description = "#DESC_org.netbeans.modules.java.hints.bugs.Unbalanced.Array",
          category="bugs", options=Options.QUERY, suppressWarnings="MismatchedReadAndWriteOfArray")
    public static final class Array {

        private static VariableElement testElement(HintContext ctx) {
            Element el = ctx.getInfo().getTrees().getElement(ctx.getPath());

            if (!isAcceptable(el) || el.asType().getKind() != TypeKind.ARRAY) return null;

            if (((ArrayType) el.asType()).getComponentType().getKind() == TypeKind.ARRAY) return null;

            return (VariableElement) el;
        }

        @TriggerTreeKind({Kind.IDENTIFIER, Kind.MEMBER_SELECT})
        @TriggerOptions(TriggerOptions.PROCESS_GUARDED)
        public static ErrorDescription before(HintContext ctx) {
            VariableElement var = testElement(ctx);

            if (var == null) return null;

            TreePath tp = ctx.getPath();
            
            if (tp.getParentPath().getLeaf().getKind() == Kind.ARRAY_ACCESS) {
                State accessType = State.READ;
                State secondAccess = null;
                Tree access = tp.getParentPath().getLeaf();
                Tree assign = tp.getParentPath().getParentPath().getLeaf();
                
                switch (assign.getKind()) {
                    case ASSIGNMENT:
                        if (((AssignmentTree) assign).getVariable() == access) {
                            accessType = State.WRITE;
                        }
                        break;
                    case AND_ASSIGNMENT: case DIVIDE_ASSIGNMENT: case LEFT_SHIFT_ASSIGNMENT:
                    case MINUS_ASSIGNMENT: case MULTIPLY_ASSIGNMENT: case OR_ASSIGNMENT:
                    case PLUS_ASSIGNMENT: case REMAINDER_ASSIGNMENT: case RIGHT_SHIFT_ASSIGNMENT:
                    case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT: case XOR_ASSIGNMENT:
                        if (((CompoundAssignmentTree) assign).getVariable() == access) {
                            secondAccess = State.WRITE;
                        }
                        break;
                    case POSTFIX_DECREMENT: case POSTFIX_INCREMENT: case PREFIX_DECREMENT:
                    case PREFIX_INCREMENT:
                        secondAccess = State.WRITE;
                        break;
                }
                record(ctx.getInfo(), var, accessType);
                if (secondAccess != null) {
                    record(ctx.getInfo(), var, secondAccess);
                }
            } else {
                record(ctx.getInfo(), var, State.WRITE, State.READ);
            }

            return null;
        }

        @TriggerPattern(value="$mods$ $type[] $name = $init$;")
        public static ErrorDescription after(HintContext ctx) {
            VariableElement var = testElement(ctx);

            if (var == null) return null;

            Tree parent = ctx.getPath().getParentPath().getLeaf();

            if (parent.getKind() == Kind.ENHANCED_FOR_LOOP
                && ((EnhancedForLoopTree) parent).getVariable() == ctx.getPath().getLeaf()) {
                return null;
            }
            
            TreePath init = ctx.getVariables().get("$init$");

            if (init != null) {
                boolean asWrite = true;
                
                if (init.getLeaf().getKind() == Kind.NEW_ARRAY) {
                    NewArrayTree nat = (NewArrayTree) init.getLeaf();

                    if (nat.getInitializers() == null || nat.getInitializers().isEmpty()) {
                        asWrite = false;
                    }
                }
                
                if (asWrite) {
                    record(ctx.getInfo(), var, State.WRITE);
                }
            }

            return produceWarning(ctx, "ERR_UnbalancedArray");
        }
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Unbalanced.Collection",
          description = "#DESC_org.netbeans.modules.java.hints.bugs.Unbalanced.Collection",
          category="bugs", options=Options.QUERY, suppressWarnings="MismatchedQueryAndUpdateOfCollection")
    public static final class Collection {
        private static final Set<String> READ_METHODS = new HashSet<>(Arrays.asList(
                "get", "getOrDefault", "contains", "remove", "containsAll", "removeAll", "removeIf", "retain", "retainAll", "containsKey",
                "containsValue", "iterator", "listIterator", "isEmpty", "size", "toArray", "entrySet", "keySet", "values", "indexOf", "lastIndexOf",
                "stream", "parallelStream", "spliterator", "reversed", "getFirst", "getLast", "removeFirst", "removeLast"));
        private static final Set<String> STANDALONE_READ_METHODS = new HashSet<>(Arrays.asList(
                "forEach"));
        private static final Set<String> WRITE_METHODS = new HashSet<>(Arrays.asList("add", "addAll", "set", "put", "putAll", "putIfAbsent", "addFirst", "addLast"));

        private static boolean testType(CompilationInfo info, TypeMirror actualType, String superClass) {
            TypeElement juCollection = info.getElements().getTypeElement(superClass);

            if (juCollection == null) return false;

            Types t = info.getTypes();

            return t.isAssignable(t.erasure(actualType), t.erasure(juCollection.asType()));
        }

        private static VariableElement testElement(HintContext ctx) {
            TreePath tp = ctx.getPath();
            Element el = ctx.getInfo().getTrees().getElement(tp);

            if (!isAcceptable(el)) return null;

            TypeMirror actualType = ctx.getInfo().getTrees().getTypeMirror(tp);

            if (actualType == null || actualType.getKind() != TypeKind.DECLARED) return null;

            if (testType(ctx.getInfo(), actualType, "java.util.Collection") || testType(ctx.getInfo(), actualType, "java.util.Map")) {
                return (VariableElement) el;
            } else {
                return null;
            }
        }

        @TriggerTreeKind({Kind.IDENTIFIER, Kind.MEMBER_SELECT})
        @TriggerOptions(TriggerOptions.PROCESS_GUARDED)
        public static ErrorDescription before(HintContext ctx) {
            TreePath tp = ctx.getPath();
            VariableElement var = testElement(ctx);

            if (var == null) return null;

            if (tp.getParentPath().getLeaf().getKind() == Kind.MEMBER_SELECT && tp.getParentPath().getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION) {
                String methodName = ((MemberSelectTree) tp.getParentPath().getLeaf()).getIdentifier().toString();
                if (READ_METHODS.contains(methodName)) {
                    if (tp.getParentPath().getParentPath().getParentPath().getLeaf().getKind() != Kind.EXPRESSION_STATEMENT) {
                        record(ctx.getInfo(), var, State.READ);
                    }
                    return null;
                } else if (STANDALONE_READ_METHODS.contains(methodName)) {
                    record(ctx.getInfo(), var, State.READ);
                    return null;
                } else if (WRITE_METHODS.contains(methodName)) {
                    if (tp.getParentPath().getParentPath().getParentPath().getLeaf().getKind() != Kind.EXPRESSION_STATEMENT) {
                        record(ctx.getInfo(), var, State.WRITE, State.READ);
                    } else {
                        record(ctx.getInfo(), var, State.WRITE);
                    }
                    return null;
                }
            }

            record(ctx.getInfo(), var, State.WRITE, State.READ);

            return null;
        }

        @TriggerPattern(value="$mods$ $type $name = $init$;")
        public static ErrorDescription after(HintContext ctx) {
            if (testElement(ctx) == null) return null;

            TreePath init = ctx.getVariables().get("$init$");

            if (init != null) {
                if (init.getLeaf().getKind() != Kind.NEW_CLASS) return null;

                NewClassTree nct = (NewClassTree) init.getLeaf();

                if (nct.getClassBody() != null || nct.getArguments().size() > 1) return null;

                if (nct.getArguments().size() == 1) {
                    TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(new TreePath(init, nct.getArguments().get(0)));

                    if (tm == null || tm.getKind() != TypeKind.INT) return null;
                }
            }

            if (   ctx.getPath().getParentPath().getLeaf().getKind() == Kind.ENHANCED_FOR_LOOP
                && ((EnhancedForLoopTree) ctx.getPath().getParentPath().getLeaf()).getVariable() == ctx.getPath().getLeaf()) {
                return null;
            }
            
            return produceWarning(ctx, "ERR_UnbalancedCollection");
        }
    }

    public enum State {
        READ, WRITE;
    }
}
