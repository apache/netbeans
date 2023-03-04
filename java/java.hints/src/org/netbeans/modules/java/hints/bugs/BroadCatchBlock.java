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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.CatchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jdk.UseSpecificCatch;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;


/**
 * This inspection reports too broad catch blocks. The inspection can be configured
 * with FQNs of 'generic' exceptions like RuntimeException, Error etc.
 * <p/>
 * It can be also configured with a list of 'umbrella' exceptions, which are 
 * permitted to catch.
 * <p/>
 * If two exceptions are thrown with a common supertype, that supertype is permitted
 * to be catched, unless it is one of the 'generic' ones.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - the catched exception type",
    "# {1} - the actual matching exception type",
    "TEXT_BroadCatchMoreSpecificException=The catch({0}) is too broad, the actually caught exception is {1}",
    "# {0} - the catched exception type",
    "# {1} - the actual matching exception types",
    "TEXT_BroadCatchMaskedExceptions=The catch({0}) is too broad, it catches the following exception types: {1}",
    "FIX_BroadCatchSplitIntoCatches=Create catches for each exception type",
    "FIX_BroadCatchUnionCatch=Change to multicatch",
    "FIX_BroadCatchNarrowCatch=Use specific type in catch",
    "# {0} - starting item",
    "TEXT_BroadCatchExceptionListStart={0}",
    "# {0} - preceding items as text",
    "# {1} - next item",
    "TEXT_BroadCatchExceptionListMiddle={0}, {1}",
    "# {0} - preceding items as text",
    "# {1} - final item",
    "TEXT_BroadCatchExceptionListEnd={0} and {1}"
})
public class BroadCatchBlock {
    /**
     * Do not report configured umbrella exceptions, by default
     * IOException, SqlException.
     */
    public static final String OPTION_EXCLUDE_UMBRELLA = "catch.umbrella"; // NOI18N
    public static final String OPTION_UMBRELLA_LIST = "catch.umbrella.types"; // NOI18N
    public static final String OPTION_EXCLUDE_COMMON = "catch.common";
    
    static final boolean DEFAULT_EXCLUDE_COMMON = true;
    static final boolean DEFAULT_EXCLUDE_UMBRELLA = false;
    static final boolean DEFAULT_ONLY_GENERIC = false;
    static final String DEFAULT_GENERIC_LIST = ""; // NOI18N
    static final String DEFAULT_UMBRELLA_LIST = "java.io.IOException, java.sql.SqlException"; // NOI18N
    
    @Hint(
      displayName = "#DN_BroadCatch",
      description = "#DESC_BroadCatch",
      category = "rules15",
      customizerProvider = CF.class,
      suppressWarnings = { "BroadCatchBlock", "TooBroadCatch" },
      enabled = false
    )
    @TriggerPatterns({
        @TriggerPattern("try { $statements$; } catch $catches$"),
        @TriggerPattern("try { $statements$; } catch $catches$ finally { $handler$; }")
    })
    public static List<ErrorDescription> broadCatch(HintContext ctx) {
        if (ctx.getPath().getLeaf().getKind() != Tree.Kind.TRY) {
            return null;
        }
        TryTree tt = (TryTree)ctx.getPath().getLeaf();
        Set<TypeMirror> realExceptions = ctx.getInfo().getTreeUtilities().getUncaughtExceptions(
                new TreePath(ctx.getPath(), tt.getBlock()));
        
        CatchClauseProcessor processor = new CatchClauseProcessor(ctx.getInfo(), ctx, realExceptions);
        
        if (ctx.getPreferences().getBoolean(OPTION_EXCLUDE_COMMON, DEFAULT_EXCLUDE_COMMON)) {
            processor.excludeCommons();
        }
        if (ctx.getPreferences().getBoolean(OPTION_EXCLUDE_UMBRELLA, DEFAULT_EXCLUDE_UMBRELLA)) {
            processor.suppressUmbrellas(ctx.getPreferences().get(OPTION_UMBRELLA_LIST, DEFAULT_UMBRELLA_LIST));
        }

        processor.process(ctx.getMultiVariables().get("$catches$"));
        
        return processor.errors;
    }
    
    private static class CatchClauseProcessor {
        private final CompilationInfo info;
        private final Collection<TypeMirror>    exceptionList;
        private final HintContext ctx;

        private boolean excludeCommons;
        private Set<String> genericQNames = Collections.emptySet();
        private Set<String> umbrellas = Collections.emptySet();
        private List<ErrorDescription> errors = Collections.emptyList();

        public CatchClauseProcessor(CompilationInfo info, HintContext ctx, Collection<TypeMirror> exceptionList) {
            this.info = info;
            this.ctx = ctx;
            this.exceptionList = exceptionList;
            initGenericQNames(ctx.getPreferences().get(UseSpecificCatch.OPTION_EXCEPTION_LIST, "")); // NOI18N
        }
        
        private void addErrorDescription(ErrorDescription desc) {
            if (desc == null) {
                return;
            }
            if (errors.isEmpty()) {
                errors = new ArrayList<ErrorDescription>(3);
            }
            errors.add(desc);
        }
        
        public CatchClauseProcessor excludeCommons() {
            this.excludeCommons = true;
            return this;
        }
        
        private CatchClauseProcessor initGenericQNames(String names) {
            StringTokenizer tukac = new StringTokenizer(names, ", "); // NOI18N
            genericQNames = new HashSet<String>(5);
            while (tukac.hasMoreTokens()) {
                genericQNames.add(tukac.nextToken());
            }
            genericQNames.addAll(HARDCODED_GENERAL_EXCEPTIONS);
            return this;
        }
        
        public CatchClauseProcessor suppressUmbrellas(String names) {
            StringTokenizer tukac = new StringTokenizer(names, ", "); // NOI18N
            umbrellas = new HashSet<String>(5);
            while (tukac.hasMoreTokens()) {
                umbrellas.add(tukac.nextToken());
            }
            return this;
        }

        private TreePath catchPath;
        
        private TreePath getCurrentPath() {
            return catchPath;
        }
        
        private final Set<TypeMirror> otherCatchedExceptions = new HashSet<TypeMirror>();
        
        public void process(Collection<? extends TreePath> catches) {
            // first pass; collect the exceptions that SHOULD be passed unnoticed
            for (TreePath tp : catches) {
                if (tp.getLeaf().getKind() != Tree.Kind.CATCH) {
                    continue;
                }
                this.catchPath = tp;
                CatchTree node = (CatchTree)tp.getLeaf();
                
                TypeMirror ex = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getParameter().getType()));
                if (ex != null) {
                    switch (ex.getKind()) {
                        case DECLARED:
                            if (!shouldReportException(ex)) {
                                otherCatchedExceptions.add(ex);
                            }
                            break;
                        case UNION:
                            for (TypeMirror t : ((UnionType)ex).getAlternatives()) {
                                if (!shouldReportException(t)) {
                                    otherCatchedExceptions.add(t);
                                }
                            }
                            break;
                    }
                }
            }

            // second pass, actually report fixes but exclude the otherCatchedExceptions collected previously
            for (TreePath tp : catches) {
                if (tp.getLeaf().getKind() != Tree.Kind.CATCH) {
                    continue;
                }
                this.catchPath = tp;
                CatchTree node = (CatchTree)tp.getLeaf();
                
                TypeMirror ex = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getParameter().getType()));
                if (ex != null) {
                    switch (ex.getKind()) {
                        case DECLARED:
                            if (shouldReportException(ex)) { 
                                addErrorDescription(processCaughtException(ex, null));
                            }
                            break;
                        case UNION:
                            for (TypeMirror t : ((UnionType)ex).getAlternatives()) {
                                if (shouldReportException(ex)) {
                                    addErrorDescription(processCaughtException(t, ((UnionType)ex).getAlternatives()));
                                }
                            }
                            break;
                    }
                }
            }
        }
        
        private boolean shouldReportException(TypeMirror excType) {
            // if the exception is really thrown, do not report it, even though it is a generic abomination.
            if (exceptionList.contains(excType)) {
                return false;
            }
            TypeElement excElement = (TypeElement)info.getTypes().asElement(excType);
            if (excElement == null) {
                return false;
            }
            String fqn = excElement.getQualifiedName().toString();
            
            List<TypeMirror> masked = new ArrayList<TypeMirror>(3);
            for (TypeMirror t : exceptionList) {
                if (info.getTypes().isSubtype(t, excType)) {
                    masked.add(t);
                }
            }
            
            if (masked.isEmpty()) {
                // either exception not thrown at all, or a RuntimeException subclass, which is not declared anywhere.
                return false;
            }
            
            if (masked.size() > 1) {
                if (umbrellas.contains(fqn)) {
                    return false;
                }
                // 
                if (excludeCommons && !genericQNames.contains(fqn)) {
                   return false;
                }
            } else {
                // 1 exception is masked, the caught exception is among the umbrellas. In the case that fqn is a 
                // RuntimeException subclass (not the RTE itself), do not report - see issue #230548 for an example.
                Element e = info.getElements().getTypeElement("java.lang.RuntimeException"); // NOI18N
                if (e == null) {
                    // bad JDK ?
                    return false;
                }
                TypeMirror rtt = e.asType();
                if (info.getTypes().isSubtype(excElement.asType(), rtt) && !info.getTypes().isSameType(excElement.asType(), rtt)) {
                    return false;
                }
            }
            if (masked.size() == 1) {
                TypeMirror one = masked.iterator().next();
                TypeElement oneElement = (TypeElement)info.getTypes().asElement(one);
                if (oneElement == null) {
                    return false;
                }
            }
            return true;
        }
        
        private ErrorDescription processCaughtException(TypeMirror excType, List<? extends TypeMirror> alternatives) {
            TypeElement excElement = (TypeElement)info.getTypes().asElement(excType);
            if (excElement == null) {
                return null;
            }
            String fqn = excElement.getQualifiedName().toString();
            List<TypeMirror> masked = new ArrayList<TypeMirror>(3);
            for (TypeMirror t : exceptionList) {
                if (!otherCatchedExceptions.contains(t) && info.getTypes().isSubtype(t, excType)) {
                    masked.add(t);
                }
            }
            CatchTree catchTree = (CatchTree)getCurrentPath().getLeaf();
            TreePath varPath = new TreePath(getCurrentPath(), catchTree.getParameter());
            if (masked.size() == 1) {
                TypeMirror one = masked.iterator().next();
                TypeElement oneElement = (TypeElement)info.getTypes().asElement(one);
                if (oneElement != null) {
                    return ErrorDescriptionFactory.forTree(ctx, varPath, 
                            TEXT_BroadCatchMoreSpecificException(fqn, oneElement.getQualifiedName().toString()),
                            new ReplaceCatchType(info, getCurrentPath(), TypeMirrorHandle.create(one)).toEditorFix()
                    );
                }
                return null;
            } else {
                Set<TypeMirrorHandle<TypeMirror>> handles = new LinkedHashSet<TypeMirrorHandle<TypeMirror>>(masked.size());
                String stringList = null;
                int cnt = 0;
                List<String> sortedFqns = new ArrayList<String>(masked.size());
                Map<String, TypeMirror> pickup = new HashMap<String, TypeMirror>(masked.size());
                for (TypeMirror m : masked) {
                    TypeElement te = (TypeElement)info.getTypes().asElement(m);
                    if (te == null) {
                        continue;
                    }
                    String teqn = te.getQualifiedName().toString();
                    sortedFqns.add(teqn);
                    pickup.put(teqn, m);
                }
                // maintain some order, least surprise to users and tests
                Collections.sort(sortedFqns);
                for (String teqn : sortedFqns) {
                    TypeMirror m = pickup.get(teqn);
                    if (cnt == 0) {
                        stringList = TEXT_BroadCatchExceptionListStart(teqn);
                    } else if (cnt < masked.size() - 1) {
                        stringList = TEXT_BroadCatchExceptionListMiddle(stringList, teqn);
                    } else {
                        stringList = TEXT_BroadCatchExceptionListEnd(stringList, teqn);
                    }
                    cnt++;
                    handles.add(TypeMirrorHandle.create(m));
                }
                // the broad catch clause may be actually found in a multi-catch. In that case, 
                // we need to just extend the current union type with enumerated masked exceptions.
                if (alternatives != null) {
                    Set<TypeMirrorHandle<TypeMirror>> extHandles = new LinkedHashSet<TypeMirrorHandle<TypeMirror>>(handles.size());
                    for (TypeMirror m : alternatives) {
                        if (!m.equals(excType)) {
                            extHandles.add(TypeMirrorHandle.create(m));
                        } else {
                            extHandles.addAll(handles);
                        }
                    }
                    handles = extHandles;
                }
                Fix[] fixes = {};
                
                if (!UseSpecificCatch.assignsTo(ctx, varPath, 
                        Collections.singletonList(new TreePath(getCurrentPath(), catchTree.getBlock())))) {
                
                    if (info.getSourceVersion().compareTo(SourceVersion.RELEASE_7) >= 0) {
                        if (alternatives != null) {
                            fixes = new Fix[] {
                                new UseSpecificCatch.FixImpl(info, getCurrentPath(), handles).toEditorFix(),
                            };
                        } else {
                            fixes = new Fix[] {
                                new UseSpecificCatch.FixImpl(info, getCurrentPath(), handles).toEditorFix(),
                                new UseSpecificCatch.SplitExceptionInCatches(info, getCurrentPath(), handles, null).toEditorFix()
                            };
                        }
                    } else {
                        fixes = new Fix[] {
                            new UseSpecificCatch.SplitExceptionInCatches(info, getCurrentPath(), handles, null).toEditorFix()
                        };
                    }
                }
                return ErrorDescriptionFactory.forTree(ctx, varPath, 
                        TEXT_BroadCatchMaskedExceptions(fqn, stringList), fixes);
            }
        }
    }
    
    public static class CF implements CustomizerProvider {

        @Override
        public JComponent getCustomizer(Preferences prefs) {
            return new BroadCatchCustomizer(prefs);
        }
    }
    
    private static final Set<String> HARDCODED_GENERAL_EXCEPTIONS = new HashSet<String>();
    
    static {
        HARDCODED_GENERAL_EXCEPTIONS.add("java.lang.RuntimeException"); // NOI18N
        HARDCODED_GENERAL_EXCEPTIONS.add("java.lang.Throwable"); // NOI18N
        HARDCODED_GENERAL_EXCEPTIONS.add("java.lang.Exception"); // NOI18N
        HARDCODED_GENERAL_EXCEPTIONS.add("java.lang.Error"); // NOI18N
    }
    
    /**
     * Replaces the catch variable type with the actually caught one.
     */
    private static class ReplaceCatchType extends JavaFix {
        private final TypeMirrorHandle    newCatchType;
        
        public ReplaceCatchType(CompilationInfo info, TreePath tp, TypeMirrorHandle newCatchType) {
            super(info, tp);
            this.newCatchType = newCatchType;
        }

        @Override
        protected String getText() {
            return FIX_BroadCatchNarrowCatch();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            CatchTree oldTree = (CatchTree)ctx.getPath().getLeaf();
            WorkingCopy wcopy = ctx.getWorkingCopy();
            Tree varType = oldTree.getParameter().getType();
            Tree newVarType = wcopy.getTreeMaker().Type(newCatchType.resolve(wcopy));
            wcopy.rewrite(varType, newVarType);
        }
    }
    
}
