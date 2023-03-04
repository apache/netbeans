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

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.modules.java.hints.jdk.UseSpecificCatch.SW_KEY;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.jdk.UseSpecificCatch", description = "#DESC_org.netbeans.modules.java.hints.jdk.UseSpecificCatch", 
        category="rules15", suppressWarnings=SW_KEY, customizerProvider = UseSpecificCatch.class)
public class UseSpecificCatch implements CustomizerProvider {
    
    public static final String OPTION_EXCEPTION_LIST = "specificCatch.exceptions"; // NOI18N
    static final String DEFAULT_EXCEPTION_LIST = "java.lang.Throwable, java.lang.Exception"; // NOI18N
    
    public static final String SW_KEY = "UseSpecificCatch";

    @Override
    public JComponent getCustomizer(Preferences prefs) {
        return new UseSpecificCatchCustomizer(prefs);
    }

    @TriggerPatterns({
        @TriggerPattern("try {$tryBlock$;} catch $catches$ "),
        @TriggerPattern("try {$tryBlock$;} catch $catches$ finally {$fin$;}"),
    })
    public static List<ErrorDescription> hint1(HintContext ctx) {
        if (ctx.getPath().getLeaf().getKind() != Tree.Kind.TRY) {
            return null;
        }
        TypeElement throwableEl = ctx.getInfo().getElements().getTypeElement("java.lang.Throwable");
        if (throwableEl == null) {
            return null;
        }
        TypeMirror throwableType = throwableEl.asType();
        TryTree tt = (TryTree) ctx.getPath().getLeaf();
        Queue<TypeMirror> process = new ArrayDeque<>(ctx.getInfo().getTreeUtilities().getUncaughtExceptions(new TreePath(ctx.getPath(), tt.getBlock())));
        Map<String, TypeMirror> declExceptions = new HashMap<>(process.size());
        
        while (!process.isEmpty()) {
            TypeMirror e = process.poll();
            switch (e.getKind()) {
                case INTERSECTION: {
                    IntersectionType itt = (IntersectionType)e;
                    for (TypeMirror t : itt.getBounds()) {
                        if (ctx.getInfo().getTypes().isAssignable(t, throwableType)) {
                            process.add(t);
                            break;
                        }
                    }
                }
                break;
                
                case TYPEVAR: {
                    TypeVariable tv = (TypeVariable)e;
                    if (tv.getUpperBound() != null) {
                     process.offer(tv.getUpperBound());   
                    }
                    if (tv.getLowerBound() != null) {
                        process.offer(tv.getLowerBound());
                    }
                    break;
                }
                
                case DECLARED:
                    DeclaredType decl = (DeclaredType)e;
                    Element el = decl.asElement();
                    if (el.getKind() == ElementKind.CLASS) {
                        declExceptions.putIfAbsent(((TypeElement)el).getQualifiedName().toString(), decl);
                    }
                    break;
            }
        }
        // sort declExceptions to get consistent behaviour
        List<String> fqns = new ArrayList<>(declExceptions.keySet());
        fqns.sort(null);
        List<TypeMirror> exceptions = new ArrayList<>(declExceptions.size());
        for (String s : fqns) {
            exceptions.add(declExceptions.get(s));
        }
        
        StringTokenizer tukac = new StringTokenizer(
            ctx.getPreferences().get(OPTION_EXCEPTION_LIST, DEFAULT_EXCEPTION_LIST), ", " // NOI18N
        );
        Collection<TypeMirror> generics = new ArrayList<TypeMirror>(3);
        while (tukac.hasMoreTokens()) {
            String th = tukac.nextToken();
            TypeElement throwable = ctx.getInfo().getElements().getTypeElement(th);
            if (throwable != null) {
                generics.add(throwable.asType());
            }
        }
        
        List<ErrorDescription> descs = new ArrayList<ErrorDescription>(2);
        
        Collection<? extends TreePath> catchPaths = ctx.getMultiVariables().get("$catches$"); // NOI18N
        String displayName = NbBundle.getMessage(UseSpecificCatch.class, "ERR_UseSpecificCatch"); // NOI18N
        OUTTER: for (TreePath p : catchPaths) {
            if (p.getLeaf().getKind() != Tree.Kind.CATCH) {
                continue;
            }
            CatchTree kec = (CatchTree)p.getLeaf();
            TypeMirror t = ctx.getInfo().getTrees().getTypeMirror(
                new TreePath(p, kec.getParameter().getType()));
            // remove the found catch from exceptions to avoid including it in a multicatch later
            if (t == null || exceptions.remove(t)) {
                continue;
            }
            if (generics.contains(t)) {
                TreePath parameterPath = new TreePath(p, kec.getParameter());
                if (assignsTo(
                        ctx, parameterPath,
                        Collections.singletonList(new TreePath(p, kec.getBlock())))) {
                    // cannot generate multi-catch and even no catch blocks with the assignment
                    continue;
                }
                TypeElement sw = ctx.getInfo().getElements().getTypeElement(SuppressWarnings.class.getName());
                Element catchParamElement = ctx.getInfo().getTrees().getElement(parameterPath);
                if (sw != null && catchParamElement != null) {
                    for (AnnotationMirror am : catchParamElement.getAnnotationMirrors()) {
                        if (!sw.equals(am.getAnnotationType().asElement())) continue;
                        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> attrEntry : am.getElementValues().entrySet()) {
                            if (!attrEntry.getKey().getSimpleName().contentEquals("value")) continue;
                            if (!(attrEntry.getValue().getValue() instanceof List<?>)) continue;
                            for (AnnotationValue av : NbCollections.checkedListByCopy(((List<?>) attrEntry.getValue().getValue()), AnnotationValue.class, false)) {
                                if (SW_KEY.equals(av.getValue()))
                                    continue OUTTER;
                            }
                        }
                    }
                }
                Set<TypeMirrorHandle<TypeMirror>> exceptionHandles = new LinkedHashSet<TypeMirrorHandle<TypeMirror>>();
                TypeMirror single = null;
                for (TypeMirror tm : exceptions) {
                    if (ctx.getInfo().getTypes().isSubtype(tm, t)) {
                        single = tm;
                        exceptionHandles.add(TypeMirrorHandle.create(tm));
                    }
                }
                boolean source17 = ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_7) >= 0;
                Fix f;
                
                if (!exceptionHandles.isEmpty()) {
                    if (exceptionHandles.size() > 1) {
                        if (source17) {
                            f = new FixImpl(ctx.getInfo(), 
                                        p,
                                        exceptionHandles
                                    ).toEditorFix();
                        } else {
                            f = new SplitExceptionInCatches(
                                        ctx.getInfo(),
                                        p,
                                        exceptionHandles,
                                        null
                                    ).toEditorFix();
                        }
                    } else {
                        f = new SplitExceptionInCatches(
                                    ctx.getInfo(),
                                    p,
                                    exceptionHandles,
                                    ctx.getInfo().getTypeUtilities().getTypeName(single).toString()
                                ).toEditorFix();
                    }
                    descs.add(ErrorDescriptionFactory.forName(
                            ctx, 
                            kec.getParameter().getType(), 
                            displayName, 
                            f
                    ));
                }
            }
        }
        return descs;
    }

    /**
     * Determines whether the catch exception parameter is assigned to.
     * 
     * @param ctx HintContext - for CompilationInfo
     * @param variable the inspected variable
     * @param statements statements that should be checked for assignment
     * @return true if 'variable' is assigned to within 'statements'
     */
    public static boolean assignsTo(final HintContext ctx, TreePath variable, Iterable<? extends TreePath> statements) {
        final Element tEl = ctx.getInfo().getTrees().getElement(variable);

        if (tEl == null || tEl.getKind() != ElementKind.EXCEPTION_PARAMETER) return true;
        final boolean[] result = new boolean[1];

        for (TreePath tp : statements) {
            new ErrorAwareTreePathScanner<Void, Void>() {
                @Override
                public Void visitAssignment(AssignmentTree node, Void p) {
                    if (tEl.equals(ctx.getInfo().getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable())))) {
                        result[0] = true;
                    }
                    return super.visitAssignment(node, p);
                }
            }.scan(tp, null);
        }

        return result[0];
    }
    
    /**
     * Fix that generates a multi-catch in place of the original overly broad
     * catch clause
     */
    public static final class FixImpl extends JavaFix {

        private final Set<TypeMirrorHandle<TypeMirror>> exceptionHandles;
        
        public FixImpl(CompilationInfo info, TreePath tryStatement, Set<TypeMirrorHandle<TypeMirror>> exceptionHandles) {
            super(info, tryStatement);
            this.exceptionHandles = exceptionHandles;
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(UseSpecificCatch.class, "FIX_UseSpecificCatch"); // NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            List<Tree> exceptions = new LinkedList<Tree>();

            for (TypeMirrorHandle<TypeMirror> h : exceptionHandles) {
                TypeMirror tm = h.resolve(wc);

                if (tm == null) return ; //XXX: log

                exceptions.add(wc.getTreeMaker().Type(tm));
            }

            VariableTree excVar = ((CatchTree) tp.getLeaf()).getParameter();

            wc.rewrite(excVar.getType(), wc.getTreeMaker().UnionType(exceptions));
        }

    }
    
    /**
     * Fix that generates multiple catch handlers in place of the original one.
     * One catch handler for each real exception. Usable also on Java &lt; 7.
     */
    public static class SplitExceptionInCatches extends JavaFix {
        private Collection<TypeMirrorHandle<TypeMirror>> newTypes;
        private final String singleName;

        public SplitExceptionInCatches(CompilationInfo info, TreePath tp, Collection<TypeMirrorHandle<TypeMirror>> newTypes, String singleName) {
            super(info, tp);
            this.newTypes = newTypes;
            this.singleName = singleName;
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(UseSpecificCatch.class, 
                    singleName != null ? 
                    "FIX_UseSpecificCatchSingle" : 
                    "FIX_UseSpecificCatchSplit", singleName); // NOI18N
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) throws Exception {
            CatchTree oldTree = (CatchTree)ctx.getPath().getLeaf();
            TryTree oldTry = (TryTree)ctx.getPath().getParentPath().getLeaf();
            
            WorkingCopy wcopy = ctx.getWorkingCopy();
            GeneratorUtilities gen = GeneratorUtilities.get(wcopy);
            TreeMaker mk = wcopy.getTreeMaker();
            int index = oldTry.getCatches().indexOf(oldTree);
            TryTree result = mk.removeTryCatch(oldTry, index);
            
            for (TypeMirrorHandle h : newTypes) {
                TypeMirror m = h.resolve(wcopy);
                if (m == null || m.getKind() != TypeKind.DECLARED) {
                    continue;
                }
                CatchTree branch = mk.Catch(
                    mk.Variable(
                        oldTree.getParameter().getModifiers(),
                        oldTree.getParameter().getName(),
                        mk.Type(m),
                        oldTree.getParameter().getInitializer()),
                    oldTree.getBlock());
                gen.copyComments(oldTree, branch, true);
                result = mk.insertTryCatch(result, index++, branch);
            }
            wcopy.rewrite(oldTry, result);
        }
    }
}
