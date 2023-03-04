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

package org.netbeans.modules.java.hints.encapsulation;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class ReturnEncapsulation {

    private static final String COLLECTION = "java.util.Collection";    //NOI18N
    private static final String MAP = "java.util.Map";                  //NOI18N
    private static final String DATE = "java.util.Date";                //NOI18N
    private static final String CALENDAR="java.util.Calendar";          //NOI18N
    private static final String A_OBJ = "java.lang.Object[]";           //NOI18N
    private static final String A_BOOL = "boolean[]";                   //NOI18N
    private static final String A_BYTE = "byte[]";                      //NOI18N
    private static final String A_CHAR = "char[]";                      //NOI18N
    private static final String A_SHORT = "short[]";                    //NOI18N
    private static final String A_INT = "int[]";                        //NOI18N
    private static final String A_LONG = "long[]";                      //NOI18N
    private static final String A_FLOAT = "float[]";                    //NOI18N
    private static final String A_DOUBLE = "double[]";                  //NOI18N
    private static final Iterable<? extends String> UNMODIFIABLE = Arrays.asList(
            "java.util.Collections.<$T$>emptySet()",
            "java.util.Collections.<$T$>emptyList()",
            "java.util.Collections.<$T$>emptyMap()",
            "java.util.Collections.EMPTY_SET",
            "java.util.Collections.EMPTY_LIST",
            "java.util.Collections.EMPTY_MAP",
            "java.util.Collections.<$T$>singleton($any)",
            "java.util.Collections.<$T$>singletonList($any)",
            "java.util.Collections.<$T$>singletonMap($any)",
            "java.util.Collections.<$T$>unmodifiableCollection($any)",
            "java.util.Collections.<$T$>unmodifiableList($any)",
            "java.util.Collections.<$T$>unmodifiableSet($any)",
            "java.util.Collections.<$T$>unmodifiableSortedSet($any)",
            "java.util.Collections.<$T$>unmodifiableMap($any)",
            "java.util.Collections.<$T$>unmodifiableSortedMap($any)",
            "java.util.Arrays.<$T$>asList($any$)"
    );

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.ReturnEncapsulation.collection", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.ReturnEncapsulation.collection", category="encapsulation",suppressWarnings="ReturnOfCollectionOrArrayField", enabled=false) //NOI18N
    @TriggerPatterns({
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=COLLECTION)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=MAP)   //NOI18N
            }
        )
    })
    public static ErrorDescription collection(final HintContext ctx) {
        assert ctx != null;
        return create(ctx,
               NbBundle.getMessage(ReturnEncapsulation.class, "TXT_ReturnCollection"),
               "ReturnOfCollectionOrArrayField",    //NOI18N
               new FixProvider());   //NOI18N
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.ReturnEncapsulation.array", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.ReturnEncapsulation.array", category="encapsulation",suppressWarnings="ReturnOfCollectionOrArrayField", enabled=false, options=Options.QUERY) //NOI18N
    @TriggerPatterns ({
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_OBJ)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_BOOL)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_BYTE)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_CHAR)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_SHORT)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_INT)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_LONG)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_FLOAT)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_DOUBLE)   //NOI18N
            }
        )
    })
    public static ErrorDescription array(final HintContext ctx) {
        assert ctx != null;
        return create(ctx,
            NbBundle.getMessage(ReturnEncapsulation.class, "TXT_ReturnArray"),
            "ReturnOfCollectionOrArrayField");  //NOI18N
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.ReturnEncapsulation.date", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.ReturnEncapsulation.date", category="encapsulation", suppressWarnings={"ReturnOfDateField"}, enabled=false, options=Options.QUERY) //NOI18N
    @TriggerPatterns({
        @TriggerPattern(value="return $expr",   //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=DATE)   //NOI18N
        }),
        @TriggerPattern(value="return $expr",   //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=CALENDAR)   //NOI18N
        })
    })
    public static ErrorDescription date(final HintContext ctx) {
        assert ctx != null;
        return create(ctx, NbBundle.getMessage(ReturnEncapsulation.class, "TXT_ReturnDate"),
            "ReturnOfDateField");   //NOI18N
    }

    private static ErrorDescription create(final HintContext ctx,
            final String description,
            final String suppressWarnings,
            FixProvider ... providers) {
        assert ctx != null;
        assert suppressWarnings != null;
        final CompilationInfo info = ctx.getInfo();
        final TreePath tp = ctx.getPath();
        final TreePath exprPath = ctx.getVariables().get("$expr");      //NOI18N
        final Element exprElement = info.getTrees().getElement(exprPath);
        final TypeMirror exprType = info.getTrees().getTypeMirror(exprPath);
        if (exprElement == null ||
            !Utilities.isValidType(exprType) ||
            exprElement.getKind() != ElementKind.FIELD) {
            return null;
        }
        if (exprElement.getModifiers().contains(Modifier.FINAL)) {
            TreePath decl = ctx.getInfo().getTrees().getPath(exprElement);

            if (decl != null && decl.getLeaf().getKind() == Kind.VARIABLE && ((VariableTree) decl.getLeaf()).getInitializer() != null) {
                TreePath init = new TreePath(decl, ((VariableTree) decl.getLeaf()).getInitializer());

                for (String p : UNMODIFIABLE) {
                    if (MatcherUtilities.matches(ctx, init, p)) {
                        return null;
                    }
                }
            }
        }
        final Element enclMethod = getElementOrNull(info,findEnclosingMethod(tp));
        if (enclMethod == null || enclMethod.getEnclosingElement() != exprElement.getEnclosingElement()) {
            return null;
        }
        List<Fix> fixes = new ArrayList<Fix>(providers.length + 1);
        for (int i=0; i<providers.length; i++) {
            Fix f = providers[i].fixFor(ctx,(ExecutableElement) enclMethod,exprPath);
            if (f != null) {
                fixes.add(f);
            }
        }
        return ErrorDescriptionFactory.forTree(ctx, tp,
            description,
            fixes.toArray(new Fix[0]));
    }

    private static final TreePath findEnclosingMethod(TreePath tp) {
        while (tp != null && tp.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
            if (tp.getLeaf().getKind() == Tree.Kind.METHOD) {
                return tp;
            }
            tp = tp.getParentPath();
        }
        return null;
    }

    private static Element getElementOrNull(final CompilationInfo info, final TreePath path) {
        return path == null ? null : info.getTrees().getElement(path);
    }

    private static class FixProvider {

        private static final Map<String, String> TO_UNMODIFIABLE;

        static {
            TO_UNMODIFIABLE = new LinkedHashMap<String, String>();
            TO_UNMODIFIABLE.put("java.util.SortedMap", "unmodifiableSortedMap");
            TO_UNMODIFIABLE.put("java.util.SortedSet", "unmodifiableSortedSet");
            TO_UNMODIFIABLE.put("java.util.Map", "unmodifiableMap");
            TO_UNMODIFIABLE.put("java.util.Set", "unmodifiableSet");
            TO_UNMODIFIABLE.put("java.util.List", "unmodifiableList");
            TO_UNMODIFIABLE.put("java.util.Collection", "unmodifiableCollection");
        }

        private FixProvider() {
        }

        public Fix fixFor(final HintContext ctx, ExecutableElement enclMethod, final TreePath tp) {
            CompilationInfo info = ctx.getInfo();
            
            assert info != null;
            assert tp != null;
            Element fe = info.getTrees().getElement(tp);
            if (fe == null) {
                return null;
            }
            String field = fe.getSimpleName().toString();
            final Types types = info.getTypes();
            final Elements elements = info.getElements();
            TypeMirror returnTypeEr = types.erasure(enclMethod.getReturnType());

            for (Entry<String, String> e : TO_UNMODIFIABLE.entrySet()) {
                TypeElement el = elements.getTypeElement(e.getKey());
                if (el != null && types.isSameType(returnTypeEr, types.erasure(el.asType()))) {
                    return JavaFixUtilities.rewriteFix(ctx, NbBundle.getMessage(ReturnEncapsulation.class, "FIX_ReplaceWithUC",e.getValue(),field), tp, "java.util.Collections." + e.getValue() + "($expr)");
                }
            }
            return null;
        }

    }

}
