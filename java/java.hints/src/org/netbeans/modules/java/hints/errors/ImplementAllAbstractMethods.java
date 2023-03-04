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
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import java.util.concurrent.Future;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedException;
import org.netbeans.modules.java.editor.codegen.ImplementOverrideMethodGenerator;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class ImplementAllAbstractMethods implements ErrorRule<Object>, OverrideErrorMessage<Object> {

    private static final String PREMATURE_EOF_CODE = "compiler.err.premature.eof"; // NOI18N
    private static final String RECORD = "RECORD"; // NOI18N
    /** Creates a new instance of ImplementAllAbstractMethodsCreator */
    public ImplementAllAbstractMethods() {
    }

    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList(
                "compiler.err.abstract.cant.be.instantiated", // NOI18N
                "compiler.err.does.not.override.abstract", // NOI18N
                "compiler.err.abstract.cant.be.instantiated", // NOI18N
                "compiler.err.enum.constant.does.not.override.abstract")); // NOI18N
    }
    
    /**
     * Returns the Element which is incomplete, or, for anonymous classes, 
     * returns the extended TypeElement (which is also incomplete). This is because
     * an Element is not available for the incomplete class.
     */
    private static TypeElement findTypeElement(CompilationInfo info, TreePath path) {
        Element e = info.getTrees().getElement(path);
        if (e == null) {
            return null;
        } else if (e.getKind().isClass() || e.getKind().isInterface()) {
            return (TypeElement)e;
        }
        TypeMirror tm = info.getTrees().getTypeMirror(path);
        if (tm == null || tm.getKind() != TypeKind.DECLARED) {
            if (path.getLeaf().getKind() == Tree.Kind.NEW_CLASS) {
                tm = info.getTrees().getTypeMirror(new TreePath(path, ((NewClassTree)path.getLeaf()).getIdentifier()));
            }
        }
        if (tm != null && tm.getKind() == TypeKind.DECLARED) {
            return (TypeElement)((DeclaredType)tm).asElement();
        } else {
            return null;
        }
    }

    @NbBundle.Messages({
        "ERR_CannotOverrideAbstractMethods=Inherited abstract methods are not accessible and could not be implemented"
    })
    @Override
    public String createMessage(CompilationInfo info, Diagnostic diag, int offset, TreePath treePath, Data<Object> data) {
        TreePath path = deepTreePath(info, offset);
        Element e = findTypeElement(info, path);
        if (e == null) {
            return null;
        }
        Map<Tree, Object> d = (Map)data.getData();
        if (d == null) {
            data.setData(d = new HashMap<>());
        }
        List<? extends ExecutableElement> lee = info.getElementUtilities().findUnimplementedMethods((TypeElement)e, true);
        Scope s = info.getTrees().getScope(path);
        boolean hasDefault = false;
        for (ExecutableElement ee : lee) {
            if (!info.getTrees().isAccessible(s, ee, (DeclaredType)e.asType())) {
                // mark the diagnostic as processed; run() will not bother with analysis of the issue.
                d.put(path.getLeaf(), true);
                return Bundle.ERR_CannotOverrideAbstractMethods();
                
            }
            if (ee.getModifiers().contains(Modifier.DEFAULT)) {
                hasDefault = true;
            }
        }
        if (hasDefault) {
            d.put(path.getLeaf(), Boolean.FALSE);
        }
        return null;
    }
    
    public List<Fix> run(final CompilationInfo info, String diagnosticKey, final int offset, TreePath treePath, Data<Object> data) {
        TreePath path = deepTreePath(info, offset);
        if (path == null) {
            return null;
        }

        Map<Tree, Object> holder = data == null ? null : (Map)data.getData();
        Object saved = null;
        if (holder != null) {
            saved = holder.get(path.getLeaf());
        }
        if (Boolean.TRUE == saved) {
            return null;
        }
        Element e = info.getTrees().getElement(path);
        final Tree leaf = path.getLeaf();
        boolean isUsableElement = e != null && (e.getKind().isClass() || e.getKind().isInterface());
        boolean containsDefaultMethod = saved == Boolean.FALSE;

        boolean completingAnonymous = e != null && e.getKind() == ElementKind.CONSTRUCTOR && 
                leaf.getKind() == Tree.Kind.NEW_CLASS;
        TypeElement tel = findTypeElement(info, path);
        
        if (!Utilities.isValidElement(tel)) {
            return null;
        }
        List<Fix> fixes = new ArrayList<>();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(leaf.getKind()) || leaf.getKind().toString().equals(RECORD)) {
            CompilationUnitTree cut = info.getCompilationUnit();
            // do not offer for class declarations without body
            long start = info.getTrees().getSourcePositions().getStartPosition(cut, leaf);
            long end = info.getTrees().getSourcePositions().getEndPosition(cut, leaf);
            for (Diagnostic d : info.getDiagnostics()) {
                long position = d.getPosition();
                if (d.getCode().equals(PREMATURE_EOF_CODE) && position > start && position < end) {
                    return null;
                }
            }
        }
        
        if (completingAnonymous) {
            //if the parent of path.getLeaf is an error, the situation probably is like:
            //new Runnable {}
            //(missing '()' for constructor)
            //do not propose the hint in this case:
            final boolean[] parentError = new boolean[] {false};
            new ErrorAwareTreePathScanner() {
                @Override
                public Object visitNewClass(NewClassTree nct, Object o) {
                    if (leaf == nct) {
                        parentError[0] = getCurrentPath().getParentPath().getLeaf().getKind() == Kind.ERRONEOUS;
                    }
                    return super.visitNewClass(nct, o);
                }
            }.scan(path.getParentPath(), null);
            if (parentError[0]) {
                // ignore
                return null;
            }
            fixes.add(new ImplementAbstractMethodsFix(info, path, tel, containsDefaultMethod));
        }
        boolean someAbstract = false;
        X: if (isUsableElement) {
            for (ExecutableElement ee : ElementFilter.methodsIn(e.getEnclosedElements())) {
                if (ee.getModifiers().contains(Modifier.ABSTRACT)) {
                    // make class abstract. In case of enums, suggest to implement the
                    // abstract methods on all enum values.
                    if (e.getKind() == ElementKind.ENUM) {
                        // cannot make enum abstract, but can generate abstract methods skeleton
                        // to all enum members
                        fixes.add(new ImplementOnEnumValues2(info,  tel, containsDefaultMethod));
                        // avoid other possible fixes:
                        break X;
                    } else if (e.getKind().isClass()) {
                        someAbstract = true;
                        break;
                    }
                }
            }
            // offer to fix all abstract methods
            if (!someAbstract) {
                fixes.add(new ImplementAbstractMethodsFix(info, path, tel, containsDefaultMethod));
            }
            if (e.getKind() == ElementKind.CLASS && e.getSimpleName() != null && !e.getSimpleName().contentEquals("")) {
                fixes.add(new MakeAbstractFix(info, path, e.getSimpleName().toString()).toEditorFix());
            }
        } 
        if (e != null && e.getKind() == ElementKind.ENUM_CONSTANT) {
            fixes.add(new ImplementAbstractMethodsFix(info, path, tel, containsDefaultMethod));
        }
        return fixes;
    }
    
    public void cancel() {
        //XXX: not done yet
    }

    public String getId() {
        return ImplementAllAbstractMethods.class.getName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ImplementAllAbstractMethods.class, "LBL_Impl_Abstract_Methods"); // NOI18N
    }
    
    public String getDescription() {
        return NbBundle.getMessage(ImplementAllAbstractMethods.class, "DSC_Impl_Abstract_Methods"); // NOI18N
    }
    
    private static TreePath deepTreePath(CompilationInfo info, int offset) {
        TreePath basic = info.getTreeUtilities().pathFor(offset);
        TreePath plusOne = info.getTreeUtilities().pathFor(offset + 1);
        
        TreePath parent = plusOne.getParentPath();
        if (parent == null) {
            return basic;
        }
        if (plusOne.getLeaf().getKind() == Kind.NEW_CLASS &&
            parent.getLeaf().getKind() == Kind.EXPRESSION_STATEMENT) {
            parent = parent.getParentPath();
            if (parent == null) {
                return basic;
            }
        }
        if (parent.getLeaf() == basic.getLeaf()) {
            return plusOne;
        }
        return basic;
    }
    
    abstract static class ImplementFixBase extends ModificationResultBasedFix implements Task<WorkingCopy>, DebugFix {
        protected final JavaSource      source;
        protected final TreePathHandle  handle;
        protected final ElementHandle<TypeElement>  implementType;
        protected final boolean         displayUI;
        
        protected TreePath  path;
        private  boolean   commit;
        protected WorkingCopy copy;
        
        private   int round;
        private List<ElementHandle<? extends Element>> elementsToImplement;

        protected ImplementFixBase(CompilationInfo info, TreePath p, TypeElement el, boolean displayUI) {
            this.source = info.getJavaSource();
            this.handle = TreePathHandle.create(p, info);
            this.implementType = ElementHandle.create(el);
            this.displayUI = displayUI;
        }

        protected ImplementFixBase(CompilationInfo info, TypeElement el, boolean displayUI) {
            this.source = info.getJavaSource();
            this.handle = TreePathHandle.create(el, info);
            this.implementType = ElementHandle.create(el);
            this.displayUI = displayUI;
        }

        @Override
        public ChangeInfo implement() throws Exception {
            if (displayUI) {
                final Future[] selector = { null };
                source.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController ctrl) throws Exception {
                        TypeElement tel = implementType.resolve(ctrl);
                        selector[0] = ImplementOverrideMethodGenerator.selectMethodsToImplement(ctrl, tel);
                    }
                }, true);
                if (selector[0] != null) {
                    Future< List<ElementHandle<? extends Element>> > f = (Future< List<ElementHandle<? extends Element>> > )selector[0];
                    elementsToImplement = f.get();
                    if (elementsToImplement == null) {
                        // cancelled.
                        return null;
                    }
                }
            }
            // first round, generate curly braces after each member which does
            // not have any
            ModificationResult res = source.runModificationTask(this);
            if (!commit) {
                return null;
            }
            
            commit = false;
            round++;
            res = source.runModificationTask(this);
            if (commit) {
                res.commit();
            }
            return null;
        }
        
        protected abstract boolean executeRound(Element el, int round) throws Exception;

        @Override
        public void run(WorkingCopy parameter) throws Exception {
            this.copy = parameter;
            parameter.toPhase(Phase.RESOLVED);
            path = handle.resolve(parameter);
            if (path == null) {
                return;
            }

            Element el = implementType.resolve(copy);
            if (el == null) {
                return;
            }
            commit = executeRound(el, round);
        }
        
        protected boolean generateClassBody(TreePath p) throws Exception {
            Element e = copy.getTrees().getElement(p);
            boolean isUsableElement = e != null && (e.getKind().isClass() || e.getKind().isInterface());
            if (isUsableElement) {
                return true;
            }
            if (e.getKind() == ElementKind.ENUM_CONSTANT) {
                VariableTree var = (VariableTree) p.getLeaf();
                if (var.getInitializer() != null && var.getInitializer().getKind() == Kind.NEW_CLASS) {
                    NewClassTree nct = (NewClassTree) var.getInitializer();
                    if (nct.getClassBody() != null) {
                        return true;
                    }
                }
            }
            return !generateClassBody2(copy, p);
        }
        
        protected boolean generateImplementation(Element el, TreePath p) {
            Tree leaf = p.getLeaf();
            
            Element e = copy.getTrees().getElement(p);
            if (e != null && e.getKind() == ElementKind.ENUM_CONSTANT) {
                VariableTree var = (VariableTree) leaf;
                if (var.getInitializer() != null && var.getInitializer().getKind() == Kind.NEW_CLASS) {
                    NewClassTree nct = (NewClassTree) var.getInitializer();
                    assert nct.getClassBody() != null;
                    TreePath enumInit = new TreePath(p, nct);
                    TreePath toModify = new TreePath(enumInit, nct.getClassBody());
                    generateAllAbstractMethodImplementations(copy, toModify, elementsToImplement);
                    return true;
                } else {
                    return false;
                }
            }
            if (el.getKind().isClass() || el.getKind().isInterface()) {
                generateAllAbstractMethodImplementations(copy, p, elementsToImplement);
                return true;
            }
            return false;
        }
    }

    /**
     * Fix which implements the [missing] abstract methods on all enum's values. It does so
     * in two phases: during the first, each of the enum values, which does not (yet) specify
     * class body, gets curly braces. The source is then reparsed to get fresh trees.
     * After that, method generation is applied on all enum values.
     */
    @NbBundle.Messages({
        "LBL_FIX_Impl_Methods_Enum_Values2=Implement abstract methods on all enum values"
    })
    static final class ImplementOnEnumValues2 extends ImplementFixBase {

        public ImplementOnEnumValues2(CompilationInfo info, TypeElement e, boolean prompt) {
            super(info, e, prompt);
        }

        @Override
        public String getText() {
            return Bundle.LBL_FIX_Impl_Methods_Enum_Values2();
        }

        @Override
        public ModificationResult getModificationResult() throws IOException {
            return source.runModificationTask(parameter -> {
            });
        }

        @Override
        protected boolean executeRound(Element el, int round) throws Exception {
            if (el.getKind() != ElementKind.ENUM) {
                return false;
            }
            ClassTree ct = (ClassTree)path.getLeaf();
            for (ListIterator<? extends Tree> it = ct.getMembers().listIterator(ct.getMembers().size()); it.hasPrevious(); ) {
                Tree t = it.previous();
                
                if (t.getKind() != Tree.Kind.VARIABLE) {
                    continue;
                }
                TreePath p = new TreePath(path, t);
                Element e = copy.getTrees().getElement(p);
                if (e == null || e.getKind() != ElementKind.ENUM_CONSTANT) {
                    continue;
                }

                switch (round) {
                    case 0:
                        if (!generateClassBody(p)) {
                            return false;
                        }
                        break;
                    case 1:
                        if (!generateImplementation(el, p)) {
                            return false;
                        }
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
            return true;
        }
        
        @Override
        public String toDebugString() {
             return "IOEV";
        }
    }
    
    private static boolean generateClassBody2(WorkingCopy copy, TreePath p) throws Exception {
        int insertOffset = (int) copy.getTrees().getSourcePositions().getEndPosition(copy.getCompilationUnit(), p.getLeaf());
        if (insertOffset == -1) {
            return false;
        }
        try {
            copy.getDocument().insertString(insertOffset, " {}", null);
        } catch (GuardedException ex) {
            String message = NbBundle.getMessage(ImplementAllAbstractMethods.class, "ERR_CannotApplyGuarded");
            StatusDisplayer.getDefault().setStatusText(message);
            return true;
        } catch (BadLocationException | IOException ex) {
            Exceptions.printStackTrace(ex);
            return true;
        }
        return false;
    }

    /**
     * Makes the class abstract. If the class is final, the final modifier is removed.
     */
    private static class MakeAbstractFix extends JavaFix implements DebugFix {
        private final String makeClassAbstractName;

        public MakeAbstractFix(CompilationInfo info, TreePath tp, String makeClassAbstractName) {
            super(info, tp);
            this.makeClassAbstractName = makeClassAbstractName;
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(ImplementAllAbstractMethods.class, "LBL_FIX_Make_Class_Abstract", makeClassAbstractName); // MOI18N 
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            Tree.Kind k = ctx.getPath().getLeaf().getKind();
            if (!TreeUtilities.CLASS_TREE_KINDS.contains(k)) {
                // TODO: report
                return;
            }
            ClassTree ct = (ClassTree)ctx.getPath().getLeaf();
            ModifiersTree mt = ct.getModifiers();

            Set<Modifier> mods = EnumSet.of(Modifier.ABSTRACT);
            mods.addAll(mt.getFlags());
            mods.remove(Modifier.FINAL);
            ModifiersTree newMt = wc.getTreeMaker().Modifiers(mods, mt.getAnnotations());
            wc.rewrite(mt, newMt);
        }

        @Override
        public String toDebugString() {
            return "MA:" + makeClassAbstractName;
        }
    }
    
    private static class ImplementAbstractMethodsFix extends ImplementFixBase {
        public ImplementAbstractMethodsFix(CompilationInfo info, TreePath path, TypeElement clazz, boolean prompt) {
            super(info, path, clazz, prompt);
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(ImplementAbstractMethodsFix.class, "LBL_FIX_Impl_Abstract_Methods"); // MOI18N 
        }

        @Override
        public ModificationResult getModificationResult() throws IOException {
            return source.runModificationTask(parameter -> {
                this.copy = parameter;
                parameter.toPhase(Phase.RESOLVED);
                TreePath tp = handle.resolve(parameter);
                if (tp != null) {
                    Element e = parameter.getTrees().getElement(tp);
                    if (e != null) {
                        if (e.getKind().isClass() || e.getKind().isInterface()) {
                            if (implementType.equals(ElementHandle.create(e))) {
                                generateImplementation(e, tp);
                            }
                        }
                        if (e.getKind() == ElementKind.ENUM_CONSTANT) {
                            VariableTree var = (VariableTree) tp.getLeaf();
                            if (var.getInitializer() != null && var.getInitializer().getKind() == Kind.NEW_CLASS) {
                                NewClassTree nct = (NewClassTree) var.getInitializer();
                                if (nct.getClassBody() != null) {
                                    Element el = implementType.resolve(parameter);
                                    if (el != null) {
                                        generateImplementation(el, tp);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        @Override
        protected boolean executeRound(Element el, int round) throws Exception {
            switch (round) {
                case 0:
                    return generateClassBody(path);
                case 1: {
                    TreePath p = path;
                    if (path.getLeaf().getKind() == Kind.NEW_CLASS) {
                        p = new TreePath(path, ((NewClassTree)path.getLeaf()).getClassBody());
                    }
                    return generateImplementation(el, p);
                }
            }
            return false;
        }
        @Override
        public String toDebugString() {
            return "IAAM";
        }
    }
    
    // copy from GeneratorUtils, need to change the processing a little.
    public static Map<? extends ExecutableElement, ? extends ExecutableElement> generateAllAbstractMethodImplementations(
            WorkingCopy wc, TreePath path, List<ElementHandle<? extends Element>> toImplementHandles) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind()) || path.getLeaf().getKind().toString().equals(RECORD);
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te == null) {
            return null;
        }
        Map<? extends ExecutableElement, ? extends ExecutableElement> ret;
        ClassTree clazz = (ClassTree)path.getLeaf();
        GeneratorUtilities gu = GeneratorUtilities.get(wc);
        ElementUtilities elemUtils = wc.getElementUtilities();
        List<? extends ExecutableElement> toImplement;
        if (toImplementHandles != null) {
            List<ExecutableElement> els = new ArrayList<>();
            for (ElementHandle<? extends Element> h : toImplementHandles) {
                Element e = h.resolve(wc);
                if (e.getKind() == ElementKind.METHOD) {
                    els.add((ExecutableElement)e);
                }
            }
            toImplement = els;
        } else {
            toImplement = elemUtils.findUnimplementedMethods(te);
        }
        ret = Utilities.findConflictingMethods(wc, te, toImplement);
        if (ret.size() < toImplement.size()) {
            toImplement.removeAll(ret.keySet());
            List<? extends MethodTree> res = gu.createAbstractMethodImplementations(te, toImplement);
            clazz = gu.insertClassMembers(clazz, res);
            wc.rewrite(path.getLeaf(), clazz);
        }
        if (ret.isEmpty()) {
            return ret;
        }
        // should be probably elsewhere: UI separation
        String msg = ret.size() == 1 ?
                NbBundle.getMessage(ImplementAllAbstractMethods.class, "WARN_FoundConflictingMethods1", 
                        ret.keySet().iterator().next().getSimpleName()) :
                NbBundle.getMessage(ImplementAllAbstractMethods.class, "WARN_FoundConflictingMethodsMany", 
                        ret.keySet().size());
        
        StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        return ret;
    }
    interface DebugFix {
        public String toDebugString();
    }
}
