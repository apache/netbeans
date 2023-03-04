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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.CreateElementUtilities;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.java.api.IntroduceParameterRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.netbeans.modules.refactoring.java.ui.ChangeParametersPanel.Javadoc;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Jan Becicka
 * @author Ralph Ruijs
 */
public class IntroduceParameterPlugin extends JavaRefactoringPlugin {

    private static final EnumSet<ElementKind> VARIABLES = EnumSet.of(ElementKind.FIELD, ElementKind.ENUM_CONSTANT, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER, ElementKind.TYPE_PARAMETER, ElementKind.RESOURCE_VARIABLE);
    private IntroduceParameterRefactoring refactoring;
    private TreePathHandle treePathHandle;
    private Set<ElementHandle<ExecutableElement>> allMethods;
    private ChangeParametersRefactoring.ParameterInfo[] paramTable;
    private int index;
    private TreePathHandle methodHandle;

    /**
     * Creates a new instance of introduce parameter refactoring plugin.
     *
     * @param method  refactored object, i.e. method or constructor
     */
    public IntroduceParameterPlugin(IntroduceParameterRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        Problem p = null;
        return p;
    }

    @Override
    protected Problem fastCheckParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        Problem p = initDelegate(javac);
        final TreePath resolved = treePathHandle.resolve(javac);
        final Element variableElement = javac.getTrees().getElement(resolved);
        ChangeParametersPlugin.Checks check = new ChangeParametersPlugin.Checks(javac) {

            @Override
            Problem duplicateLocalName(Problem p, ParameterInfo[] paramTable, int index, ExecutableElement method) throws MissingResourceException {
                String name = paramTable[index].getName();
                int originalIndex = paramTable[index].getOriginalIndex();
                final VariableElement parameterElement = originalIndex == -1 ? null : method.getParameters().get(originalIndex);
                ErrorAwareTreeScanner<Boolean, String> scanner = new ErrorAwareTreeScanner<Boolean, String>() {
               
                    @Override
                    public Boolean visitVariable(VariableTree vt, String p) {
                        super.visitVariable(vt, p);
                        TreePath path = javac.getTrees().getPath(javac.getCompilationUnit(), vt);
                        Element element = javac.getTrees().getElement(path);
                        boolean sameName = element != null && vt.getName().contentEquals(p) && !element.equals(parameterElement);
                
                        return sameName && element != variableElement;
                    }

                    @Override
                    public Boolean visitIdentifier(IdentifierTree it, String p) {
                        super.visitIdentifier(it, p);
                        TreePath path = javac.getTrees().getPath(javac.getCompilationUnit(), it);
                        Element element = javac.getTrees().getElement(path);
                        boolean sameName = element != null && VARIABLES.contains(element.getKind()) && it.getName().contentEquals(p) && !element.equals(parameterElement);
                
                        return sameName && element != variableElement;
                    }
                
                    @Override
                    public Boolean reduce(Boolean left, Boolean right) {
                        return (left == null ? false : left) || (right == null ? false : right);
                    }
                };

                if (scanner.scan(javac.getTrees().getTree(method), name)) {
                    if (!isParameterBeingRemoved(method, name, paramTable)) {
                        p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_NameAlreadyUsed", name)); // NOI18N
                    }
                }
                
                return p;
            }
        };

        TreePath methodPath = JavaPluginUtils.findMethod(resolved);
        if (methodPath == null) {
            return p;
        }
        final ExecutableElement method = (ExecutableElement) javac.getTrees().getElement(methodPath);
        if (method == null) {
            return p;
        }

        boolean isConstructor = method.getKind() == ElementKind.CONSTRUCTOR;

        TypeElement enclosingTypeElement = javac.getElementUtilities().enclosingTypeElement(method);
        List<? extends Element> allMembers = javac.getElements().getAllMembers(enclosingTypeElement);

        if (!isConstructor) {
            p = check.duplicateSignature(p, paramTable, method, enclosingTypeElement, allMembers);
        } else {
            p = check.duplicateConstructor(p, paramTable, method, enclosingTypeElement, allMembers);
        }
        for (int i = 0; i < paramTable.length; i++) {
            ChangeParametersRefactoring.ParameterInfo parameterInfo = paramTable[i];

            p = check.checkParameterName(p, parameterInfo.getName());
            if (parameterInfo.getOriginalIndex() == -1) {
                p = check.defaultValue(p, parameterInfo.getDefaultValue());
            }
            p = check.duplicateParamName(p, paramTable, i);
            p = check.duplicateLocalName(p, paramTable, i, method);
            p = check.parameterType(p, paramTable, i, method, enclosingTypeElement);
        }
        return p;
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        JavaSource source = JavaSource.create(cpInfo, refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject());

        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {

                @Override
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }

                @Override
                public void run(CompilationController info) throws Exception {
                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    final ElementUtilities elmUtils = info.getElementUtilities();

                    //add all references of overriding methods
                    ExecutableElement el = (ExecutableElement)getMethodElement(treePathHandle, info);
                    ElementHandle<ExecutableElement> methodHandle = ElementHandle.create(el);
                    ElementHandle<TypeElement> enclosingType = ElementHandle.create(elmUtils.enclosingTypeElement(el));
                    allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                    allMethods.add(methodHandle);
                    for (ExecutableElement e : JavaRefactoringUtils.getOverridingMethods(el, info, cancelRequested)) {
                        ElementHandle<ExecutableElement> handle = ElementHandle.create(e);
                        set.add(SourceUtils.getFile(handle, info.getClasspathInfo()));
                        ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        allMethods.add(ElementHandle.create(e));
                    }
                    //add all references of overriden methods
                    for (ExecutableElement e : JavaRefactoringUtils.getOverriddenMethods(el, info)) {
                        ElementHandle<ExecutableElement> handle = ElementHandle.create(e);
                        set.add(SourceUtils.getFile(handle, info.getClasspathInfo()));
                        ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        allMethods.add(ElementHandle.create(e));
                    }
                    set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                    set.add(SourceUtils.getFile(methodHandle, info.getClasspathInfo()));
                }
            }, true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return set;
    }

    @Override
    public Problem prepare(final RefactoringElementsBag elements) {
        Set<FileObject> a = getRelevantFiles();

        initDelegate();
        Javadoc javadoc = refactoring.getContext().lookup(Javadoc.class);
        final ChangeParamsTransformer changeParamsTransformer = new ChangeParamsTransformer(paramTable, null, null, null, refactoring.isOverloadMethod(), javadoc == null? Javadoc.NONE : javadoc, allMethods, methodHandle, true);
//        final ChangeParamsJavaDocTransformer changeParamsJavaDocTransformer = new ChangeParamsJavaDocTransformer(paramTable, null, refactoring.isOverloadMethod(), javadoc == null? Javadoc.NONE : javadoc, allMethods, methodHandle);
        
        fireProgressListenerStart(AbstractRefactoring.PREPARE, a.size() * 2);
        Problem p = null;
        if (!a.isEmpty()) {
        
            CancellableTask<WorkingCopy> t = new CancellableTask<WorkingCopy>() {

                @Override
                public void cancel() {
                }

                @Override
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);

                    if(workingCopy.getFileObject().equals(treePathHandle.getFileObject())) {
                        TreePath resolved = treePathHandle.resolve(workingCopy);
                        TreePath meth = JavaPluginUtils.findMethod(resolved);
                        methodHandle = TreePathHandle.create(meth, workingCopy);

                        final TreeMaker make = workingCopy.getTreeMaker();

                        boolean expressionStatement = resolved.getParentPath().getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT;
                        boolean variableRewrite = resolved.getLeaf().getKind() == Tree.Kind.VARIABLE;

                        BlockTree sttmts;

                        if (refactoring.isReplaceAll() || variableRewrite) {
                            Set<TreePath> candidates = computeDuplicates(workingCopy, resolved, meth);
                            for (TreePath p : candidates) {
                                Tree leaf = p.getLeaf();

                                workingCopy.rewrite(leaf, make.Identifier(refactoring.getParameterName()));
                            }
                            sttmts = findAddPosition(workingCopy, resolved, candidates);
                        } else {
                            sttmts = findAddPosition(workingCopy, resolved, Collections.<TreePath>emptySet());
                        }
                        if (sttmts != null) {
                            List<StatementTree> nueStatements2 = new LinkedList<StatementTree>(sttmts.getStatements());

                            if (expressionStatement) {
                                nueStatements2.remove(resolved.getParentPath().getLeaf());
                            }
                            if (variableRewrite) {
                                nueStatements2.remove(resolved.getLeaf());
                            }

                            BlockTree nueBlock2 = make.Block(nueStatements2, false);
                            workingCopy.rewrite(sttmts, nueBlock2);
                        }
                        if (!variableRewrite) {
                            Tree origParent = resolved.getParentPath().getLeaf();
                            Tree leaf = resolved.getLeaf();
                            Tree identifier = make.Identifier(refactoring.getParameterName());
                            if(leaf.getKind() == Kind.PARENTHESIZED) { // Merge into OperatorPrecedence?
                                switch (origParent.getKind()) {
                                    case FOR_LOOP:
                                    case ENHANCED_FOR_LOOP:
                                    case WHILE_LOOP:
                                    case DO_WHILE_LOOP:
                                    case IF:
                                        identifier = make.Parenthesized((ExpressionTree) identifier);
                                }
                            }
                            Tree newParent = workingCopy.getTreeUtilities().translate(origParent, Collections.singletonMap(leaf, identifier));
                            workingCopy.rewrite(origParent, newParent);
                        }
                    }
                    
                    try {
                        try {
                            changeParamsTransformer.setWorkingCopy(workingCopy);
                        } catch (ToPhaseException e) {
                            return;
                        }
                        CompilationUnitTree cu = workingCopy.getCompilationUnit();
                        if (cu == null) {
                            ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + workingCopy); // NOI18N
                            return;
                        }
                        Element el = null;
                        if (methodHandle != null) {
                            el = methodHandle.resolveElement(workingCopy);
                            if (el == null) {
                                ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot resolve " + methodHandle + "in " + workingCopy.getFileObject().getPath());
                                return;
                            }
                        }

                        changeParamsTransformer.scan(workingCopy.getCompilationUnit(), el);
                        
                        
                    } finally {
                        fireProgressListenerStep();
                    }
                }
            };
//            p = JavaPluginUtils.chainProblems(p, createAndAddElements(a, new TransformTask(changeParamsJavaDocTransformer, methodHandle), elements, refactoring));
            p = JavaPluginUtils.chainProblems(p, createAndAddElements(a, t, elements, refactoring));
        }

        fireProgressListenerStop();
        return p;
    }

    private Set<TreePath> computeDuplicates(final WorkingCopy workingCopy, TreePath resolved, TreePath meth) {
        Set<TreePath> ret = SourceUtils.computeDuplicates(workingCopy, resolved, meth, new AtomicBoolean());
//        final Set<TreePath> ret = new HashSet<TreePath>();
//        ErrorAwareTreePathScanner<Void, Element> scanner = new ErrorAwareTreePathScanner<Void, Element>() {
//
//            @Override
//            public Void visitIdentifier(IdentifierTree node, Element p) {
//                Element el = workingCopy.getTrees().getElement(getCurrentPath());
//                if (el.equals(p)) {
//                    ret.add(getCurrentPath());
//                }
//                return super.visitIdentifier(node, p);
//            }
//        };
//        scanner.scan(meth, workingCopy.getTrees().getElement(resolved));
        return ret;
    }

    @Override
    protected JavaSource getJavaSource(JavaRefactoringPlugin.Phase p) {
        switch (p) {
            case CHECKPARAMETERS:
            case FASTCHECKPARAMETERS:
            case PRECHECK:
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                return JavaSource.create(cpInfo, treePathHandle.getFileObject());
        }
        return null;
    }

    /**
     * Returns list of problems. For the change method signature, there are two
     * possible warnings - if the method is overriden or if it overrides
     * another method.
     * @return  overrides or overriden problem or both
     */
    @Override
    protected Problem preCheck(CompilationController info) throws IOException {
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        Problem preCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);

        TreePath tp = treePathHandle.resolve(info);
        TreePath method = JavaPluginUtils.findMethod(tp);
        if (method == null) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_ChangeParamsWrongType")); //NOI18N
            return preCheckProblem;
        }

        Element el = info.getTrees().getElement(method);
        if (!RefactoringUtils.isExecutableElement(el)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_ChangeParamsWrongType")); //NOI18N
            return preCheckProblem;
        }

        preCheckProblem = JavaPluginUtils.isSourceElement(el, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }

        if (info.getElementUtilities().enclosingTypeElement(el).getKind() == ElementKind.ANNOTATION_TYPE) {
            preCheckProblem = new Problem(true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_MethodsInAnnotationsNotSupported")); //NOI18N
            return preCheckProblem;
        }

        for (ExecutableElement e : JavaRefactoringUtils.getOverriddenMethods((ExecutableElement) el, info)) {
            ElementHandle<ExecutableElement> handle = ElementHandle.create(e);
            if (RefactoringUtils.isFromLibrary(handle, info.getClasspathInfo())) {
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_CannnotRefactorLibrary", el)); //NOI18N
            }
        }

        fireProgressListenerStop();
        return preCheckProblem;
    }

    private static BlockTree findAddPosition(CompilationInfo info, TreePath original, Set<? extends TreePath> candidates) {
        //find least common block holding all the candidates:
        TreePath statement = original;

        for (TreePath p : candidates) {
            Tree leaf = p.getLeaf();
            int leafStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), leaf);
            int stPathStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), statement.getLeaf());

            if (leafStart < stPathStart) {
                statement = p;
            }
        }

        List<TreePath> allCandidates = new LinkedList<TreePath>();

        allCandidates.add(original);
        allCandidates.addAll(candidates);

        statement = JavaPluginUtils.findStatement(statement);

        if (statement == null) {
            //XXX: well....
            return null;
        }

        while (statement.getParentPath() != null && !JavaPluginUtils.isParentOf(statement.getParentPath(), allCandidates)) {
            statement = statement.getParentPath();
        }

        //#126269: the common parent may not be block:
        while (statement.getParentPath() != null && statement.getParentPath().getLeaf().getKind() != Kind.BLOCK) {
            statement = statement.getParentPath();
        }

        if (statement.getParentPath() == null) {
            return null;//XXX: log
        }
        BlockTree statements = (BlockTree) statement.getParentPath().getLeaf();
        StatementTree statementTree = (StatementTree) statement.getLeaf();

        int index = statements.getStatements().indexOf(statementTree);

        if (index == (-1)) {
            //really strange...
            return null;
        }
        return statements;
    }

    private Problem initDelegate() throws IllegalArgumentException {
        final Problem[] p = new Problem[1];
        if (paramTable == null) {
            try {
                JavaSource source = JavaSource.forFileObject(treePathHandle.getFileObject());
                source.runUserActionTask(new CancellableTask<CompilationController>() {

                    @Override
                    public void run(org.netbeans.api.java.source.CompilationController info) {
                        p[0] = initDelegate(info);
                    }

                    @Override
                    public void cancel() {
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            paramTable[index] = new ChangeParametersRefactoring.ParameterInfo(-1, refactoring.getParameterName(), paramTable[index].getType(), paramTable[index].getDefaultValue());
        }
        return p[0];
    }
    
    private Problem initDelegate(CompilationController info) throws IllegalArgumentException {
        Problem p = null;
        final String FINAL = "final ";
        if (paramTable == null) {
            try {
                info.toPhase(org.netbeans.api.java.source.JavaSource.Phase.RESOLVED);
                TreePath path = treePathHandle.resolve(info);
                TreePath methodPath = JavaPluginUtils.findMethod(path);

                ExecutableElement method = methodPath == null ? null : (ExecutableElement) info.getTrees().getElement(methodPath);
                if (method == null) {
                    p = JavaPluginUtils.chainProblems(p, new Problem(true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_canNotResolve", 
                            methodPath != null ? methodPath.getLeaf().toString() : treePathHandle)));
                    return p;
                }
                List<? extends VariableElement> parameters = method.getParameters();
                paramTable = new ChangeParametersRefactoring.ParameterInfo[parameters.size() + 1];
                for (int originalIndex = 0; originalIndex < parameters.size(); originalIndex++) {
                    VariableElement param = parameters.get(originalIndex);
                    VariableTree parTree = (VariableTree) info.getTrees().getTree(param);
                    String typeRepresentation;
                    if (method.isVarArgs() && originalIndex == parameters.size()-1) {
                        typeRepresentation = parTree.getType().toString().replace("[]", "..."); // NOI18N
                    } else {
                        typeRepresentation = parTree.getType().toString();
                    }
                    paramTable[originalIndex] = new ChangeParametersRefactoring.ParameterInfo(originalIndex, param.getSimpleName().toString(), typeRepresentation, null);
                }
                index = paramTable.length - 1;
                if (method.isVarArgs()) {
                    paramTable[index] = paramTable[--index];
                }

                TypeMirror tm = info.getTrees().getTypeMirror(path);
                
                if (tm != null && tm.getKind() == TypeKind.NULL) {
                    List<? extends TypeMirror> targetType = CreateElementUtilities.resolveType(new HashSet<ElementKind>(), info, path.getParentPath(), path.getLeaf(), (int) info.getTrees().getSourcePositions().getStartPosition(path.getCompilationUnit(), path.getLeaf()), new TypeMirror[1], new int[1]);

                    if (!targetType.isEmpty()) {
                        tm = targetType.get(0);
                    } else {
                        TypeElement object = info.getElements().getTypeElement("java.lang.Object");
                        tm = object != null ? object.asType() : null;
                    }
                } else {
                    tm = JavaPluginUtils.convertIfAnonymous(JavaPluginUtils.resolveCapturedType(info, tm));
                }

                if (tm == null) {
                    p = JavaPluginUtils.chainProblems(p, new Problem(true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_canNotResolve", path.getLeaf().toString())));
                }

                String type = info.getTypeUtilities().getTypeName(tm).toString();

                Tree original = path.getLeaf();
                boolean variableRewrite = original.getKind() == Kind.VARIABLE;
                ExpressionTree expression = !variableRewrite ? (ExpressionTree) original : ((VariableTree) original).getInitializer();

                if (expression != null && expression.getKind() == Kind.PARENTHESIZED) { // If parenthesis are necessary, they will be added again later.
                    ParenthesizedTree parents = (ParenthesizedTree) expression;
                    expression = parents.getExpression();
                }

                paramTable[index] = new ChangeParametersRefactoring.ParameterInfo(-1, refactoring.getParameterName(), (refactoring.isFinal() ? FINAL : "") + type, expression == null ? ((VariableTree) original).getName().toString() : expression.toString());
                
                TreePath resolved = treePathHandle.resolve(info);
                TreePath meth = JavaPluginUtils.findMethod(resolved);
                methodHandle = TreePathHandle.create(meth, info);

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            String type = paramTable[index].getType();
            if(type.startsWith(FINAL)) {
                type = refactoring.isFinal()? type : type.substring(FINAL.length());
            } else {
                type = refactoring.isFinal()? FINAL + type : type;
            }
            paramTable[index] = new ChangeParametersRefactoring.ParameterInfo(-1, refactoring.getParameterName(), type, paramTable[index].getDefaultValue());
        }
        return p;
    }

    private ExecutableElement getMethodElement(TreePathHandle handle, CompilationInfo info) {
        return (ExecutableElement) info.getTrees().getElement(JavaPluginUtils.findMethod(handle.resolve(info)));
    }
}
