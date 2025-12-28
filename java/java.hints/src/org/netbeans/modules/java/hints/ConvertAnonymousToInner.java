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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.SelectionAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.editor.rename.InstantRenamePerformer;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Jan Lahoda
 */
public class ConvertAnonymousToInner extends AbstractHint {
    
    public ConvertAnonymousToInner() {
        super(true, false, HintSeverity.CURRENT_LINE_WARNING);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.NEW_CLASS);
    }

    static Fix computeFix(CompilationInfo info, int selStart, int selEnd, boolean onlyHeader) {
        TreePath tp = findNCT(info, info.getTreeUtilities().pathFor((selStart + selEnd + 1) / 2), selStart, selEnd, onlyHeader);

        if (tp == null) {
            tp = findNCT(info, info.getTreeUtilities().pathFor((selStart + selEnd + 1) / 2 + 1), selStart, selEnd, onlyHeader);
        }
        
        if (tp == null) {
            return null;
        }
        
        return new FixImpl(TreePathHandle.create(tp, info), info.getJavaSource(), info.getFileObject());
    }

    private static TreePath findNCT(CompilationInfo info, TreePath tp, int selStart, int selEnd, boolean onlyHeader) {
        while (tp != null) {
            if (tp.getLeaf().getKind() != Kind.NEW_CLASS) {
                tp = tp.getParentPath();
                continue;
            }

            NewClassTree nct = (NewClassTree) tp.getLeaf();

            if (nct.getClassBody() == null) {
                tp = tp.getParentPath();
                continue;
            }

            if (selStart == selEnd) {
                if (onlyHeader) {
                    long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), nct.getClassBody());

                    if (selStart > start) {
                        return null;
                    }
                }

                break;
            } else {
                long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), nct);
                long end = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), nct);

                if (start == selStart && end == selEnd) {
                    break;
                }
            }

            tp = tp.getParentPath();
        }

        return tp;
    }
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        int pos = CaretAwareJavaSourceTaskFactory.getLastPosition(compilationInfo.getFileObject());
        int[] selection = SelectionAwareJavaSourceTaskFactory.getLastSelection(compilationInfo.getFileObject());

        if (selection == null) return null;
        
        Fix f = computeFix(compilationInfo, selection[0], selection[1], true);
        
        if (f == null)
            return null;
        
        List<Fix> fixes = Collections.<Fix>singletonList(f);
        String hintDescription = NbBundle.getMessage(ConvertAnonymousToInner.class, "HINT_ConvertAnonymousToInner");
        
        return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(Severity.HINT, hintDescription, fixes, compilationInfo.getFileObject(), pos, pos));
    }
    
    public String getId() {
        return ConvertAnonymousToInner.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ConvertAnonymousToInner.class, "DN_ConvertAnonymousToInner");
    }

    public String getDescription() {
        return NbBundle.getMessage(ConvertAnonymousToInner.class, "DESC_ConvertAnonymousToInner");
    }
    
    private static class FixImpl implements Fix, Task<WorkingCopy> {
        
        private TreePathHandle tph;
        private JavaSource js;
        private FileObject file;
        
        public FixImpl(TreePathHandle tph, JavaSource js, FileObject file) {
            this.tph = tph;
            this.js = js;
            this.file = file;
        }

        public String getText() {
            return NbBundle.getMessage(ConvertAnonymousToInner.class, "FIX_ConvertAnonymousToInner");
        }

        public ChangeInfo implement() throws IOException {
            ModificationResult mr = js.runModificationTask(this);
            
            mr.commit();
            
            final int[] newClassNameSpan = mr.getSpan(NEW_CLASS_TREE_TAG);
            
            if (newClassNameSpan != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            EditorCookie cook = DataObject.find(file).getLookup().lookup(EditorCookie.class);
                            JEditorPane[] arr = cook.getOpenedPanes();
                            if (arr == null) {
                                return;
                            }
                            arr[0].setCaretPosition((newClassNameSpan[0] + newClassNameSpan[1]) / 2);
                            InstantRenamePerformer.invokeInstantRename(arr[0]);
                        } catch (DataObjectNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
            
            return null;
        }

        public void run(WorkingCopy parameter) throws Exception {
            parameter.toPhase(Phase.RESOLVED);
            TreePath tp = tph.resolve(parameter);
            
            convertAnonymousToInner(parameter, tp);
        }
    }

    private static final class DetectUsedVars extends ErrorAwareTreePathScanner<Void, Set<VariableElement>> {
        private CompilationInfo info;
        private TreePath newClassToConvert;
        
        private DetectUsedVars(CompilationInfo info, TreePath newClassToConvert) {
            this.info = info;
            this.newClassToConvert = newClassToConvert;
        }
        
        private static final Set<ElementKind> VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.PARAMETER, ElementKind.LOCAL_VARIABLE);
        
        @Override
        public Void visitIdentifier(IdentifierTree node, Set<VariableElement> p) {
            Element el = info.getTrees().getElement(getCurrentPath());
            TreePath elPath = el != null ? info.getTrees().getPath(el) : null;
            
            if (el != null && elPath != null && VARIABLES.contains(el.getKind()) && !isParent(newClassToConvert, elPath)) {
                p.add((VariableElement) el);
            }
            
            return super.visitIdentifier(node, p);
        }

    }
    
    private static final class DetectUseOfNonStaticMembers extends ErrorAwareTreePathScanner<Boolean, Void> {
        private CompilationInfo info;
        private TreePath newClassToConvert;
        
        private DetectUseOfNonStaticMembers(CompilationInfo info, TreePath newClassToConvert) {
            this.info = info;
            this.newClassToConvert = newClassToConvert;
        }
        
        @Override
        public Boolean visitIdentifier(IdentifierTree node, Void p) {
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && (el.getKind().isField() || el.getKind() == ElementKind.METHOD) && !el.getModifiers().contains(Modifier.STATIC)) {
                return true;
            }
            
            return super.visitIdentifier(node, p);
        }

        @Override
        public Boolean reduce(Boolean r1, Boolean r2) {
            return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
        }

    }
    
    private static boolean isParent(TreePath tp1, TreePath tp2) {
        while (tp2 != null && tp1.getLeaf() != tp2.getLeaf()) {
            tp2 = tp2.getParentPath();
        }

        if (tp2 == null) {
            return false;
        }

        return tp1.getLeaf() == tp2.getLeaf();
    }
    
    private static String generateName(CompilationInfo info, TreePath newClassToConvert, String prototype) {
        Scope s = info.getTrees().getScope(newClassToConvert);
        Integer extension = null;
        
        if (s == null) return prototype + "Impl"; //NOI18N
        
        while (true) {
            String currentProposal = prototype + "Impl" + (extension == null ? "" : extension.toString()); //NOI18N
            Scope currentScope = s;
            boolean found = false;
            
            OUTER : while (currentScope != null) {
                for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new ElementAcceptor() {
                    public boolean accept(Element e, TypeMirror type) {
                        return true;
                    }
                })) {
                    if (e.getKind().isClass() || e.getKind().isInterface()) {
                        String sn = e.getSimpleName().toString();
                        
                        if (currentProposal.equals(sn)) {
                            found = true;
                            break OUTER;
                        }
                    }
                }
                
                currentScope = currentScope.getEnclosingScope();
            }
            
            if (!found) {
                return currentProposal;
            }
            
            extension = extension != null ? extension + 1 : 1;
        }
    }
    
    static void convertAnonymousToInner(WorkingCopy copy, TreePath newClassToConvert) {
        TreeMaker make = copy.getTreeMaker();
        NewClassTree nct = (NewClassTree) newClassToConvert.getLeaf();

        nct = GeneratorUtilities.get(copy).importComments(nct, newClassToConvert.getCompilationUnit());
        
        Set<VariableElement> usedElementVariables = new LinkedHashSet<VariableElement>();
        
        new DetectUsedVars(copy, newClassToConvert).scan(new TreePath(newClassToConvert, nct.getClassBody()), usedElementVariables);
        boolean usesNonStaticMembers = new DetectUseOfNonStaticMembers(copy, newClassToConvert).scan(new TreePath(newClassToConvert, nct.getClassBody()), null) == Boolean.TRUE;
                
        TreePath tp = newClassToConvert;
        TreePath parentPath = null;
        while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            if (tp.getLeaf().getKind() == Tree.Kind.METHOD || tp.getLeaf().getKind() == Tree.Kind.BLOCK) {
                parentPath = tp;
            }
            tp = tp.getParentPath();
        }
        
        ClassTree target = (ClassTree) tp.getLeaf();
        Element targetElement = copy.getTrees().getElement(tp);

        boolean isInAnonymousClass = false;
        TreePath treePath = newClassToConvert.getParentPath();
        while (treePath != null) {
            if (treePath.getLeaf().getKind() == Kind.NEW_CLASS) {
                isInAnonymousClass = true;
                break;
            }
            treePath = treePath.getParentPath();
        }
        
        TypeMirror superType = copy.getTrees().getTypeMirror(new TreePath(newClassToConvert, nct.getIdentifier()));
        Element superTypeElement = copy.getTrees().getElement(new TreePath(newClassToConvert, nct.getIdentifier()));
        
        boolean isStaticContext = targetElement != null && superTypeElement != null;
        Element currElement = superTypeElement;
        if (isStaticContext) {
            while (currElement != null && currElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                if (!currElement.getModifiers().contains(Modifier.STATIC)) {
                    isStaticContext = false;
                    break;
                }

                currElement = currElement.getEnclosingElement();
            }
        }
        
        if (isStaticContext) {
            while (targetElement != null && targetElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                if (!targetElement.getModifiers().contains(Modifier.STATIC)) {
                    isStaticContext = false;
                    break;
                }
                targetElement = targetElement.getEnclosingElement();
            }
        }
        
        Tree superTypeTree = make.Type(superType);
        
        Logger.getLogger(ConvertAnonymousToInner.class.getName()).log(Level.FINE, "usesNonStaticMembers = {0}", usesNonStaticMembers ); //NOI18N
        
        TreePath superConstructorCall = findSuperConstructorCall(copy, newClassToConvert);
        
        Element currentElement = copy.getTrees().getElement(newClassToConvert);
	boolean errorConstructor = currentElement == null || currentElement.asType() == null || currentElement.asType().getKind() == TypeKind.ERROR;
        boolean isEnclosedByStaticElem = false;
        while (currentElement != null && currentElement.getEnclosingElement() != null) {
            if (currentElement.getModifiers().contains(Modifier.STATIC)) {
                isEnclosedByStaticElem = true; //enclosing method is static
                break;
            }

            currentElement = currentElement.getEnclosingElement();
        }

        Set<Modifier> modifset = null;
        if (isInAnonymousClass) {
            if ((isStaticContext && !usesNonStaticMembers) || isEnclosedByStaticElem) {
                modifset = EnumSet.of(Modifier.STATIC);
            } else {
                modifset = EnumSet.noneOf(Modifier.class);
            }
        } else {
            if ((isStaticContext && !usesNonStaticMembers) || isEnclosedByStaticElem) {
                modifset = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC);
            } else {
                modifset = EnumSet.of(Modifier.PRIVATE);
            }
        }
        ModifiersTree classModifiers = make.Modifiers(modifset);
        
        List<Tree> members = new ArrayList<Tree>();
        List<VariableTree> constrArguments = new ArrayList<VariableTree>();
        List<StatementTree> constrBodyStatements = new ArrayList<StatementTree>();
        List<ExpressionTree> constrRealArguments = new ArrayList<ExpressionTree>();
	ModifiersTree emptyMods = make.Modifiers(EnumSet.noneOf(Modifier.class));
	List<ExpressionTree> nueSuperConstructorCallRealArguments = null;

        if (superConstructorCall != null && !errorConstructor) {
            Element superConstructor = copy.getTrees().getElement(superConstructorCall);
            
            if (superConstructor != null && superConstructor.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement) superConstructor;
                TypeMirror nctTypes = copy.getTrees().getTypeMirror(newClassToConvert);
                
                if (!Utilities.isValidType(nctTypes)) {
                    // issue #236082: try again, but strip the parent statement; must reattribute the part of the tree
                    TreePath skipPath = new TreePath(parentPath, newClassToConvert.getLeaf());
                    copy.getTreeUtilities().attributeTree(newClassToConvert.getLeaf(), copy.getTrees().getScope(skipPath));
                    nctTypes = copy.getTrees().getTypeMirror(skipPath);
                }
                
                if (nctTypes.getKind() != TypeKind.DECLARED) {
                    StringBuilder debug = new StringBuilder();
                    
                    debug.append(nctTypes.getKind())
                         .append(":")
                         .append(nctTypes.toString())
                         .append(":")
                         .append(newClassToConvert.getLeaf().toString());
                    
                    SourcePositions sp = copy.getTrees().getSourcePositions();
                    int s = (int) sp.getStartPosition(copy.getCompilationUnit(), newClassToConvert.getLeaf());
                    int e = (int) sp.getEndPosition(copy.getCompilationUnit(), newClassToConvert.getLeaf());
                    
                    if (e > s) {
                        debug.append(":");
                        debug.append(copy.getText().substring(s, e));
                    }
                    
                    assert false : debug.toString();
                }
                
                ExecutableType et = (ExecutableType) copy.getTypes().asMemberOf((DeclaredType) nctTypes, ee);
                
                if (!ee.getParameters().isEmpty()) {
                    nueSuperConstructorCallRealArguments = new LinkedList<ExpressionTree>();
                    Iterator<? extends VariableElement> names = ee.getParameters().iterator();
                    Iterator<? extends TypeMirror> types = et.getParameterTypes().iterator();

                    while (names.hasNext() && types.hasNext()) {
                        CharSequence name = names.next().getSimpleName();

                        constrArguments.add(make.Variable(emptyMods, name, make.Type(types.next()), null));
                        nueSuperConstructorCallRealArguments.add(make.Identifier(name));
                    }
                }
            }
        } else if (errorConstructor) {
	    Pair<List<? extends TypeMirror>, List<String>> resolvedArguments = Utilities.resolveArguments(copy, newClassToConvert, nct.getArguments(), targetElement);

	    if (resolvedArguments != null) {
		nueSuperConstructorCallRealArguments = new LinkedList<ExpressionTree>();

		Iterator<? extends TypeMirror> typeIt   = resolvedArguments.first().iterator();
		Iterator<String>               nameIt   = resolvedArguments.second().iterator();

		while (typeIt.hasNext() && nameIt.hasNext()) {
		    TypeMirror tm = typeIt.next();
		    String     argName = nameIt.next();

		    constrArguments.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), argName, make.Type(tm), null));
		    nueSuperConstructorCallRealArguments.add(make.Identifier(argName));
		}
	    }
	}

	if (nueSuperConstructorCallRealArguments != null) {
	    constrBodyStatements.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("super"), nueSuperConstructorCallRealArguments)));
	}
        
        constrRealArguments.addAll(nct.getArguments());
        
        ModifiersTree privateFinalMods = make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL));
        ModifiersTree emptyArgs = make.Modifiers(EnumSet.noneOf(Modifier.class));
        
        for (VariableElement ve : usedElementVariables) {
            members.add(make.Variable(privateFinalMods, ve.getSimpleName(), make.Type(ve.asType()), null));
            constrArguments.add(make.Variable(emptyArgs, ve.getSimpleName(), make.Type(ve.asType()), null));
            constrBodyStatements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier(ve.getSimpleName())))); // NOI18N
            constrRealArguments.add(make.Identifier(ve.getSimpleName()));
        }
        
        List<Tree> oldMembers = new ArrayList<Tree>(nct.getClassBody().getMembers());
        
        ModifiersTree constructorModifiers = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
        
        MethodTree constr = make.Method(constructorModifiers, "<init>", null, Collections.<TypeParameterTree>emptyList(), constrArguments, Collections.<ExpressionTree>emptyList(), make.Block(constrBodyStatements, false), null); // NOI18N
        
        members.add(constr);
        members.addAll(oldMembers);
        
        String newClassName = generateName(copy, newClassToConvert, superTypeElement.getSimpleName().toString());
        
        ClassTree clazz = make.Class(classModifiers, newClassName, Collections.<TypeParameterTree>emptyList(), superTypeElement.getKind().isClass() ? superTypeTree : null, superTypeElement.getKind().isClass() ? Collections.<Tree>emptyList() : Collections.<Tree>singletonList(superTypeTree), Collections.emptyList(), members);
        
        copy.rewrite(target, make.addClassMember(target, copy.getTreeMaker().asReplacementOf(clazz, nct)));

        IdentifierTree classNameTree = make.Identifier(newClassName);
        NewClassTree nueNCT = make.NewClass(/*!!!*/null, Collections.<ExpressionTree>emptyList(), classNameTree, constrRealArguments, null);
        
        copy.rewrite(nct, nueNCT);
        copy.tag(classNameTree, NEW_CLASS_TREE_TAG);
    }

    public void cancel() {
    }

    private static TreePath findSuperConstructorCall(final CompilationInfo info, TreePath nct) {
        class FindSuperConstructorCall extends ErrorAwareTreePathScanner<TreePath, Void> {

            private boolean stop;

            @Override
            public TreePath scan(Tree tree, Void p) {
                if (stop) return null;
                return super.scan(tree, p);
            }
            
            @Override
            public TreePath visitMethodInvocation(MethodInvocationTree tree, Void v) {
                if (false && info.getTreeUtilities().isSynthetic(getCurrentPath())) {
                    return null;
                }
                if (tree.getMethodSelect().getKind() == Kind.IDENTIFIER && "super".equals(((IdentifierTree) tree.getMethodSelect()).getName().toString())) {
                    stop = true;
                    return getCurrentPath();
                }

                return null;
            }

            @Override
            public TreePath reduce(TreePath first, TreePath second) {
                if (first == null) {
                    return second;
                } else {
                    return first;
                }
            }

        }
        
        return new FindSuperConstructorCall().scan(nct, null);
    }

    private static final String NEW_CLASS_TREE_TAG = "new-class-tree-tag";
}
