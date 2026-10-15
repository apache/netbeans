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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReturnTree;
import static com.sun.source.doctree.DocTree.Kind.AUTHOR;
import static com.sun.source.doctree.DocTree.Kind.DEPRECATED;
import static com.sun.source.doctree.DocTree.Kind.EXCEPTION;
import static com.sun.source.doctree.DocTree.Kind.PARAM;
import static com.sun.source.doctree.DocTree.Kind.RETURN;
import static com.sun.source.doctree.DocTree.Kind.SEE;
import static com.sun.source.doctree.DocTree.Kind.SERIAL;
import static com.sun.source.doctree.DocTree.Kind.SERIAL_DATA;
import static com.sun.source.doctree.DocTree.Kind.SERIAL_FIELD;
import static com.sun.source.doctree.DocTree.Kind.SINCE;
import static com.sun.source.doctree.DocTree.Kind.THROWS;
import static com.sun.source.doctree.DocTree.Kind.UNKNOWN_BLOCK_TAG;
import static com.sun.source.doctree.DocTree.Kind.VERSION;
import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.ParamTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.netbeans.modules.refactoring.java.ui.ChangeParametersPanel.Javadoc;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * <b>!!! Do not use {@link Element} parameter of visitXXX methods. Use {@link #allMethods} instead!!!</b>
 *
 * @author Jan Becicka
 */
public class ChangeParamsTransformer extends RefactoringVisitor {

    private static final Set<Modifier> ALL_ACCESS_MODIFIERS = EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
    private static final int NOPOS = -2;
    private Set<ElementHandle<ExecutableElement>> allMethods;
    /** refactored element is a synthetic default constructor */
    private boolean synthConstructor;
    /**
     * refactored element is a constructor; {@code null} if it is has not been initialized yet
     * @see #init()
     */
    private Boolean constructorRefactoring;
    private final ParameterInfo[] paramInfos;
    private Collection<? extends Modifier> newModifiers;
    private String returnType;
    private boolean compatible;
    private final Javadoc javaDoc;
    private final TreePathHandle refactoringSource;
    private MethodTree origMethod;
    private final String newName;
    private boolean fromIntroduce = false;
    
    public ChangeParamsTransformer(ParameterInfo[] paramInfo,
            Collection<? extends Modifier> newModifiers,
            String returnType,
            String newName,
            boolean compatible,
            Javadoc javaDoc,
            Set<ElementHandle<ExecutableElement>> am,
            TreePathHandle refactoringSource,
            boolean fromIntroduce) {
        this(paramInfo, newModifiers, returnType, newName, compatible, javaDoc, am, refactoringSource);
        this.fromIntroduce = fromIntroduce;
    }

    public ChangeParamsTransformer(ParameterInfo[] paramInfo,
            Collection<? extends Modifier> newModifiers,
            String returnType,
            String newName,
            boolean compatible,
            Javadoc javaDoc,
            Set<ElementHandle<ExecutableElement>> am,
            TreePathHandle refactoringSource) {
        super(true);
        this.paramInfos = paramInfo;
        this.newModifiers = newModifiers;
        this.returnType = returnType;
        this.newName = newName;
        this.compatible = compatible;
        this.javaDoc = javaDoc;
        this.allMethods = am;
        this.refactoringSource = refactoringSource;
    }
    
    private Problem problem;
    private LinkedList<ClassTree> problemClasses = new LinkedList<ClassTree>();

    public Problem getProblem() {
        return problem;
    }

    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        super.setWorkingCopy(workingCopy);
        if(origMethod == null
                && workingCopy.getFileObject().equals(refactoringSource.getFileObject())) {
            TreePath resolvedPath = refactoringSource.resolve(workingCopy);
            TreePath meth = JavaPluginUtils.findMethod(resolvedPath);
            origMethod = (MethodTree) meth.getLeaf();
        }
    }

    private void checkNewModifier(TreePath tree, Element p) throws MissingResourceException {
        if (newModifiers == null || newModifiers.contains(Modifier.PUBLIC)) {
            return;
        }
        ClassTree classTree = (ClassTree) JavaRefactoringUtils.findEnclosingClass(workingCopy, tree, true, true, true, true, false).getLeaf();
        if (problemClasses.contains(classTree)) {
            // Only give one warning for every file
            return;
        }
        Element el = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), classTree));
        if (el == null || p == null) {
            return;
        }
        TypeElement enclosingTypeElement1 = workingCopy.getElementUtilities().outermostTypeElement(el);
        TypeElement enclosingTypeElement2 = workingCopy.getElementUtilities().outermostTypeElement(p);
        if(!workingCopy.getTypes().isSameType(enclosingTypeElement1.asType(), enclosingTypeElement2.asType())) {
            if(newModifiers.contains(Modifier.PRIVATE)) {
                problem = MoveTransformer.createProblem(problem, false, NbBundle.getMessage(ChangeParamsTransformer.class, "ERR_StrongAccMod", Modifier.PRIVATE, enclosingTypeElement1)); //NOI18N
                problemClasses.add(classTree);
            } else {
                PackageElement package1 = workingCopy.getElements().getPackageOf(el);
                PackageElement package2 = workingCopy.getElements().getPackageOf(p);
                if(!package1.getQualifiedName().equals(package2.getQualifiedName())) {
                    if(newModifiers.contains(Modifier.PROTECTED)) {
                        if(!workingCopy.getTypes().isSubtype(enclosingTypeElement1.asType(), enclosingTypeElement2.asType())) {
                            problem = MoveTransformer.createProblem(problem, false, NbBundle.getMessage(ChangeParamsTransformer.class, "ERR_StrongAccMod", Modifier.PROTECTED, enclosingTypeElement1)); //NOI18N
                            problemClasses.add(classTree);
                        }
                    } else {
                        problem = MoveTransformer.createProblem(problem, false, NbBundle.getMessage(ChangeParamsTransformer.class, "ERR_StrongAccMod", "<default>", enclosingTypeElement1)); //NOI18N
                        problemClasses.add(classTree);
                    }
                }
            }
        }
    }

    private void init() {
        if (constructorRefactoring == null) {
            ElementHandle<ExecutableElement> handle = allMethods.iterator().next();
            constructorRefactoring = handle.getKind() == ElementKind.CONSTRUCTOR;
            Element el;
            synthConstructor = constructorRefactoring
                    && (el = handle.resolve(workingCopy)) != null
                    && workingCopy.getElementUtilities().isSynthetic(el);
        }
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        init();
        return super.visitCompilationUnit(node, p);
    }

    @Override
    public Tree visitClass(ClassTree node, Element p) {
        if(compatible) {
            List<? extends Tree> members = node.getMembers();
            for (int i = 0; i < members.size(); i++) {
                Tree tree = members.get(i);
                if (tree.getKind() == Kind.METHOD) {
                    ExecutableElement element = (ExecutableElement) workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), tree));
                    if (p.equals(element)) {
                        List<ExpressionTree> paramList = getNewCompatibleArguments(((MethodTree)tree).getParameters());
                        MethodInvocationTree methodInvocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                constructorRefactoring? make.Identifier("this") : make.Identifier(element),
                                paramList);
                        TypeMirror methodReturnType = element.getReturnType();
                        boolean hasReturn = true;
                        Types types = workingCopy.getTypes();
                        if (types.isSameType(methodReturnType, types.getNoType(TypeKind.VOID))) {
                            hasReturn = false;
                        }
                        StatementTree statement = null;
                        if (hasReturn) {
                            statement = make.Return(methodInvocation);
                        } else {
                            statement = make.ExpressionStatement(methodInvocation);
                        }
                        final GeneratorUtilities genutils = GeneratorUtilities.get(workingCopy);
                        tree = genutils.importComments(tree, workingCopy.getCompilationUnit());
                        
                        BlockTree body = make.Block(Collections.singletonList(statement), false);
                        final BlockTree oldBody = ((MethodTree)tree).getBody();
                        genutils.copyComments(oldBody, body, true);
                        genutils.copyComments(oldBody, body, false);
                        MethodTree newMethod;
                        if (!fromIntroduce) {
                            newMethod = make.Method(
                                    make.Modifiers(element.getModifiers()),
                                    newName == null ? element.getSimpleName() : newName,
                                    ((MethodTree)tree).getReturnType(),
                                    ((MethodTree)tree).getTypeParameters(),
                                    ((MethodTree)tree).getParameters(),
                                    ((MethodTree)tree).getThrows(),
                                    body,
                                    null,
                                    element.isVarArgs());
                        } else {
                            newMethod = make.Method(element, body);
                            if(element.getKind() == ElementKind.CONSTRUCTOR) {
                                newMethod = make.Method(newMethod.getModifiers(),
                                        newMethod.getName(),
                                        null, // workaround: make.Method(element, body) uses void as return type for contructors
                                        newMethod.getTypeParameters(),
                                        newMethod.getParameters(),
                                        newMethod.getThrows(),
                                        newMethod.getBody(),
                                        (ExpressionTree)newMethod.getDefaultValue(),
                                        element.isVarArgs());
                            }
                        }
                        genutils.copyComments(tree, newMethod, true);
                        genutils.copyComments(tree, newMethod, false);

                        ClassTree addMember = make.insertClassMember(node, i, newMethod);
                        rewrite(node, addMember);
                    }
                }
            }
        }
        return super.visitClass(node, p);
    }
    
    @Override
    public Tree visitNewClass(NewClassTree tree, Element p) {
        if (constructorRefactoring && !compatible && !workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            ExecutableElement constructor = (ExecutableElement) p;
            final Trees trees = workingCopy.getTrees();
            Element el = trees.getElement(getCurrentPath());
            el = resolveAnonymousClassConstructor(el, tree, trees);
            if (isMethodMatch(el, p)) {
                List<ExpressionTree> arguments = getNewArguments(tree.getArguments(), false, constructor);
                NewClassTree nju = make.NewClass(tree.getEnclosingExpression(),
                        (List<ExpressionTree>)tree.getTypeArguments(),
                        tree.getIdentifier(),
                        arguments,
                        tree.getClassBody());
                rewrite(tree, nju);
            }
        }
        return super.visitNewClass(tree, p);
    }

    @Override
    public DocTree visitDocComment(DocCommentTree node, Element p) {
        if(javaDoc != Javadoc.UPDATE) {
            return node;
        }
        TreePath path = getCurrentDocPath().getTreePath();
        Element el = workingCopy.getTrees().getElement(path);
        if (isMethodMatch(el, p)) {
            List<? extends DocTree> blockTags = node.getBlockTags();
            List<DocTree> newTags = new LinkedList<DocTree>();
            Map<String, ParamTree> oldParams = new HashMap<>();
            ParamTree fake = new FakaParamTree();
            int index = 0;
            ReturnTree returnTree;
            for (DocTree docTree : blockTags) {
                if(docTree.getKind() != DocTree.Kind.PARAM || ((ParamTree) docTree).isTypeParameter()) {
                    if(docTree.getKind() == DocTree.Kind.RETURN) {
                        returnTree = (ReturnTree) docTree;
                    } else {
                        newTags.add(docTree);
                    }
                    if(TagComparator.compareTag(fake, docTree) != TagComparator.HIGHER) {
                        index++;
                    }
                } else {
                    ParamTree paramTree = (ParamTree) docTree;
                    oldParams.put(paramTree.getName().getName().toString(), paramTree);
                }
            }
            for (ParameterInfo parameterInfo : paramInfos) {
                ParamTree tag;
                if(parameterInfo.getOriginalIndex() == -1) {
                    tag = make.Param(false, make.DocIdentifier(parameterInfo.getName()), Collections.singletonList(make.Text("the value of " + parameterInfo.getName())));
                } else {
                    String name = ((ExecutableElement)el).getParameters().get(parameterInfo.getOriginalIndex()).getSimpleName().toString();
                    tag = oldParams.get(name);
                    if(tag == null) {
                        tag = make.Param(false, make.DocIdentifier(parameterInfo.getName()), Collections.singletonList(make.Text("the value of " + parameterInfo.getName())));
                    } else if(parameterInfo.getName() != null) {
                        tag = make.Param(false, make.DocIdentifier(parameterInfo.getName()), tag.getDescription());
                    }
                }
                newTags.add(index++, tag);
            }
            // @Return
            String returnTypeString;
            TypeMirror returnType = ((ExecutableElement)el).getReturnType();
            if (this.returnType == null) {
                if (returnType != null && returnType.getKind() != TypeKind.VOID) {
                    returnTypeString = returnType.toString();
                } else {
                    returnTypeString = null;
                }
            } else {
                if(this.returnType.equals("void")) {
                    returnTypeString = null;
                } else {
                    returnTypeString = this.returnType;
                }
            }
            if(returnTypeString != null) {
                newTags.add(make.DocReturn(Collections.singletonList(make.Text("the " + returnTypeString))));
            }
            
            rewrite(path.getLeaf(), node, make.DocComment(node.getFirstSentence(), node.getBody(), newTags));
        }
        return node;
    }
    
    /**
     * special treatment for anonymous classes to resolve the proper constructor
     * of extended class instead of the synthetic one.
     * @see <a href="https://netbeans.org/bugzilla/show_bug.cgi?id=168775">#168775</a>
     */
    private Element resolveAnonymousClassConstructor(Element el, NewClassTree tree, final Trees trees) {
        if (el != null && tree.getClassBody() != null) {
            Tree t = trees.getTree(el);
            if (t != null && t.getKind() == Tree.Kind.METHOD) {
                MethodTree constructorTree = (MethodTree) t;
                Tree superCall = constructorTree.getBody().getStatements().get(0);
                TreePath superCallPath = trees.getPath(
                        getCurrentPath().getCompilationUnit(),
                        ((ExpressionStatementTree) superCall).getExpression());
                el = trees.getElement(superCallPath);
            }
        }
        return el;
    }
    
    private List<ExpressionTree> getNewCompatibleArguments(List<? extends VariableTree> parameters) {
        List<ExpressionTree> arguments = new ArrayList<>();
        ParameterInfo[] pi = paramInfos;
        for (int i = 0; i < pi.length; i++) {
            int originalIndex = pi[i].getOriginalIndex();
            ExpressionTree vt;
            String value;
            if (originalIndex < 0) {
                value = pi[i].getDefaultValue();
            } else {
                value = parameters.get(originalIndex).getName().toString();
            }
            SourcePositions pos[] = new SourcePositions[1];
            vt = workingCopy.getTreeUtilities().parseExpression(value, pos);
            arguments.add(vt);
        }
        return arguments;
    }
    
    private List<VariableTree> getNewParameters(List<? extends VariableTree> currentParameters, TreePath path) {
        List<VariableTree> arguments = new ArrayList<>();
        boolean skipType = currentParameters.size() > 0 && (currentParameters.get(0).getType() == null
                || workingCopy.getTreeUtilities().isSynthetic(new TreePath(path, currentParameters.get(0).getType())));
        ParameterInfo[] pi = paramInfos;
        for (int i = 0; i < pi.length; i++) {
            int originalIndex = pi[i].getOriginalIndex();
            VariableTree vt;
            boolean isVarArgs = i == pi.length -1 && pi[i].getType().endsWith("..."); // NOI18N
            String newType = isVarArgs? pi[i].getType().replace("...", "") : pi[i].getType(); //NOI18N
            if (originalIndex < 0) {
                vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()),
                        pi[i].getName(),
                        skipType? null : make.Identifier(newType), // NOI18N
                        null);
            } else {
                vt = currentParameters.get(originalIndex);
                if(!skipType) {
                    Tree typeTree = null;
                    if (origMethod != null) {
                        if (!pi[i].getType().equals(origMethod.getParameters().get(originalIndex).getType().toString())) {
                            typeTree = make.Identifier(newType);
                        }
                    } else {
                        typeTree = make.Identifier(newType);
                    }
                    if(typeTree != null) {
                        vt = make.Variable(vt.getModifiers(),
                                vt.getName(),
                                typeTree,
                                vt.getInitializer());
                    }
                }
            }
            arguments.add(vt);
        }
        return arguments;
    }
    
    private List<ExpressionTree> getNewArguments(List<? extends ExpressionTree> currentArguments, boolean passThrough, ExecutableElement method) {
        List<ExpressionTree> arguments = new ArrayList<>();
        ParameterInfo[] pi = paramInfos;
        for (int i = 0; i < pi.length; i++) {
            int originalIndex = pi[i].getOriginalIndex();
            ExpressionTree vt;
            if (originalIndex < 0) {
                SourcePositions pos[] = new SourcePositions[1];
                if(passThrough) {
                    String value = pi[i].getName();
                    vt = workingCopy.getTreeUtilities().parseExpression(value, pos);
                } else {
                    String value = pi[i].getDefaultValue();
                    if (i == pi.length - 1 && pi[i].getType().endsWith("...")) { // NOI18N
                        // last param is vararg, so split the default value for the remaining arguments
                        MethodInvocationTree parsedExpression = (MethodInvocationTree) workingCopy.getTreeUtilities().parseExpression("method("+value+")", pos); //NOI18N
                        for (ExpressionTree expressionTree : parsedExpression.getArguments()) {
                            arguments.add(translateExpression(expressionTree, currentArguments, method));
                        }
                        break;
                    } else {
                        vt = translateExpression(workingCopy.getTreeUtilities().parseExpression(value, pos), currentArguments, method);
                    }
                }
            } else {
                if (i == pi.length - 1 && pi[i].getType().endsWith("...") && method.isVarArgs() && method.getParameters().size()-1 == originalIndex) { // NOI18N
                    // last param is vararg, so copy all remaining arguments
                    for (int j = originalIndex; j < currentArguments.size(); j++) {
                        arguments.add(currentArguments.get(j));
                    }
                    break;
                } else {
                    vt = currentArguments.get(originalIndex);
                }
            }
            arguments.add(vt);
        }
        return arguments;
    }

    @Override
    public Tree visitMethodInvocation(MethodInvocationTree tree, Element p) {
        if ((constructorRefactoring || !workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) && !compatible) {
            ExecutableElement method = (ExecutableElement) p;
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (isMethodMatch(el, method)) {
                checkNewModifier(getCurrentPath(), method);
                boolean passThrough = false;
                TreePath enclosingMethod = JavaPluginUtils.findMethod(getCurrentPath());
                if(enclosingMethod != null) {
                    Element enclosingElement = workingCopy.getTrees().getElement(enclosingMethod);
                    if(isMethodMatch(enclosingElement, method)) {
                        passThrough = true;
                    }
                }
                List<ExpressionTree> arguments = getNewArguments(tree.getArguments(), passThrough, method);

                MethodInvocationTree nju = make.MethodInvocation(
                        (List<ExpressionTree>)tree.getTypeArguments(),
                        newName != null ? make.setLabel(tree.getMethodSelect(), newName) : tree.getMethodSelect(),
                        arguments);

                if (constructorRefactoring && workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
                    rewriteSyntheticConstructor(nju);
                } else {
                    // rewrite existing super(); statement
                    rewrite(tree, nju);
                }
            }
        }
        return super.visitMethodInvocation(tree, p);
    }

    @Override
    public Tree visitLambdaExpression(LambdaExpressionTree tree, Element p) {
        TreePath path = getCurrentPath();
        if (!compatible && !workingCopy.getTreeUtilities().isSynthetic(path)) {
            ExecutableElement method = (ExecutableElement) p;
            TypeMirror tm = workingCopy.getTrees().getTypeMirror(path);
            if (tm != null && workingCopy.getTypes().isSameType(tm, method.getEnclosingElement().asType())) {
                checkNewModifier(path, method);
                List<VariableTree> params = getNewParameters(tree.getParameters(), path);
                LambdaExpressionTree nju = make.LambdaExpression(params, tree.getBody());
                rewrite(tree, nju);
            }
        }
        return super.visitLambdaExpression(tree, p);
    }

    /** workaround to rewrite synthetic super(); statement */
    private void rewriteSyntheticConstructor(MethodInvocationTree nju) {
        TreePath constructorPath = getCurrentPath();
        while (constructorPath != null && constructorPath.getLeaf().getKind() != Tree.Kind.METHOD) {
            constructorPath = constructorPath.getParentPath();
        }
        if (constructorPath != null) {
            MethodTree constrTree = (MethodTree) constructorPath.getLeaf();
            BlockTree body = constrTree.getBody();
            body = make.removeBlockStatement(body, 0);
            body = make.insertBlockStatement(body, 0, make.ExpressionStatement(nju));
            if (workingCopy.getTreeUtilities().isSynthetic(constructorPath)) {
                // in case of synthetic default constructor declaration the whole constructor has to be rewritten
                MethodTree njuConstructor = make.Method(
                        make.Modifiers(constrTree.getModifiers().getFlags(),
                        constrTree.getModifiers().getAnnotations()),
                        constrTree.getName(),
                        constrTree.getReturnType(),
                        constrTree.getTypeParameters(),
                        constrTree.getParameters(),
                        constrTree.getThrows(),
                        body,
                        (ExpressionTree) constrTree.getDefaultValue());
                rewrite(constrTree, njuConstructor);
            } else {
                // declared default constructor => body rewrite is sufficient
                rewrite(constrTree.getBody(), body);
            }
        }
    }
    
    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        if (constructorRefactoring && isSyntheticConstructorOfAnnonymousClass(workingCopy.getTrees().getElement(getCurrentPath()))) {
            return tree;
        }
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitMethod(tree, p);
    }

    private void renameDeclIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (!synthConstructor && workingCopy.getTreeUtilities().isSynthetic(path)) {
            return;
        }
        final GeneratorUtilities genutils = GeneratorUtilities.get(workingCopy);
        final MethodTree current;
        if(!compatible) { // Do not import comments twice.
            current = genutils.importComments((MethodTree)tree, workingCopy.getCompilationUnit());
        } else {
            current = (MethodTree) tree;
        }
        Element el = workingCopy.getTrees().getElement(path);
        if (isMethodMatch(el, elementToFind)) {
            
            List<? extends VariableTree> currentParameters = current.getParameters();
            List<VariableTree> newParameters = new ArrayList<VariableTree>(paramInfos.length);
            
            boolean renameParams = !fromIntroduce;
            
            ExecutableElement oMethod = (ExecutableElement) el;
            ExecutableElement refMethod = (ExecutableElement) elementToFind;
            
            if(oMethod != refMethod) {
                List<? extends VariableElement> oParams = oMethod.getParameters();
                List<? extends VariableElement> rParams = refMethod.getParameters();
                for (int i = 0; i < oParams.size(); i++) {
                    if(!oParams.get(i).getSimpleName().contentEquals(rParams.get(i).getSimpleName())) {
                        renameParams = false;
                        break;
                    }
                }
            }
            
            ParameterInfo[] p = paramInfos;
            for (int i=0; i<p.length; i++) {
                int originalIndex = p[i].getOriginalIndex();
                VariableTree vt;
                if (originalIndex <0) {
                    boolean isVarArgs = i == p.length -1 && p[i].getType().endsWith("..."); // NOI18N
                    vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()),
                            p[i].getName(),
                            make.Identifier(isVarArgs? p[i].getType().replace("...", "") : p[i].getType()), // NOI18N
                            null);
                } else {
                    VariableTree originalVt = currentParameters.get(originalIndex);
                    boolean isVarArgs = i == p.length -1 && p[i].getType().endsWith("..."); // NOI18N
                    String newType = isVarArgs? p[i].getType().replace("...", "") : p[i].getType();
                    
                    final Tree typeTree;
                    if (origMethod != null) {
                        if (p[i].getType().equals(origMethod.getParameters().get(originalIndex).getType().toString())) { // Type has not changed
                            typeTree = originalVt.getType();
                        } else {
                            typeTree = make.Identifier(newType); // NOI18N
                        }
                    } else {
                        typeTree = make.Identifier(newType); // NOI18N
                    }
                    vt = make.Variable(originalVt.getModifiers(),
                            renameParams? p[i].getName() : originalVt.getName(),
                            typeTree,
                            originalVt.getInitializer());
                }
                newParameters.add(vt);
            }

            // apply new access modifiers if necessary
            Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            modifiers.addAll(current.getModifiers().getFlags());
            if (newModifiers!=null && !el.getEnclosingElement().getKind().isInterface()) {
                modifiers.removeAll(ALL_ACCESS_MODIFIERS);
                modifiers.addAll(newModifiers);
            }
            
            // apply new return type if necessary
            boolean applyNewReturnType = false;
            if(this.returnType != null) {
                ExecutableElement exEl = (ExecutableElement) el;
                String oldReturnType = exEl.getReturnType().toString();
                if(!this.returnType.equals(oldReturnType)) {
                    applyNewReturnType = true;
                }
            }

            //Compute new imports
            for (VariableTree vt : newParameters) {
                Set<ElementHandle<TypeElement>> declaredTypes = workingCopy.getClasspathInfo().getClassIndex().getDeclaredTypes(vt.getType().toString(), NameKind.SIMPLE_NAME, EnumSet.allOf(ClassIndex.SearchScope.class));
                Set<ElementHandle<TypeElement>> declaredTypesMirr = new HashSet<ElementHandle<TypeElement>>(declaredTypes);
                TypeElement type = null;

                //remove private types
                //TODO: and possibly package private?
                for (ElementHandle<TypeElement> typeName : declaredTypes) {
                    TypeElement te = workingCopy.getElements().getTypeElement(typeName.getQualifiedName());

                    if (te == null) {
                        Logger.getLogger(ChangeParamsTransformer.class.getName()).log(Level.INFO, "Cannot resolve type element \"{0}\".", typeName);
                        continue;
                    }
                    if (te.getModifiers().contains(Modifier.PRIVATE)) {
                        declaredTypesMirr.remove(typeName);
                    }

                }

                if (declaredTypesMirr.size() == 1) { //creates import if there is just one proposed type
                    ElementHandle<TypeElement> typeName = declaredTypesMirr.iterator().next();
                    TypeElement te = workingCopy.getElements().getTypeElement(typeName.getQualifiedName());

                    if (te == null) {
                        Logger.getLogger(ChangeParamsTransformer.class.getName()).log(Level.INFO, "Cannot resolve type element \"{0}\".", typeName);
                        continue;
                    }
                    type = te;
                }

                if (type != null) {
                    PackageElement packageOf = workingCopy.getElements().getPackageOf(type);
                    if (packageOf.getQualifiedName().toString().equals("java.lang")) {
                        continue;
                    }
                    try {
                        SourceUtils.resolveImport(workingCopy, path, type.getQualifiedName().toString());
                    } catch (NullPointerException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            final BlockTree body = translateBody(current.getBody(), current.getParameters(), (ExecutableElement)el, renameParams);

            MethodTree nju = make.Method(
                    make.Modifiers(modifiers, current.getModifiers().getAnnotations()),
                    newName != null ? newName : current.getName(),
                    applyNewReturnType? make.Type(this.returnType) : current.getReturnType(),
                    current.getTypeParameters(),
                    newParameters,
                    current.getThrows(),
                    fromIntroduce? current.getBody() : body,
                    (ExpressionTree) current.getDefaultValue(),
                    p.length > 0 && p[p.length-1].getType().endsWith("...")); //NOI18N

            genutils.copyComments(current, nju, true);
            genutils.copyComments(current, nju, false);
            
            if(javaDoc == Javadoc.GENERATE) {
                List<DocTree> tags = new LinkedList<DocTree>();
                // @TypeParam
                for (TypeParameterTree typeParameterTree : current.getTypeParameters()) {
                    tags.add(make.Param(true, make.DocIdentifier(typeParameterTree.getName()), Collections.EMPTY_LIST));
                }
                // @Param
                for (VariableTree variableTree : newParameters) {
                    tags.add(make.Param(false, make.DocIdentifier(variableTree.getName()), Collections.singletonList(make.Text("the value of " + variableTree.getName()))));
                }
                // @Return
                String returnTypeString;
                Tree returnType = nju.getReturnType();
                if (this.returnType == null) {
                    boolean hasReturn = false;
                    if (returnType != null && returnType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
                        if (((PrimitiveTypeTree) returnType).getPrimitiveTypeKind() != TypeKind.VOID) {
                            hasReturn = true;
                        }
                    }
                    if (hasReturn) {
                        returnTypeString = returnType.toString();
                    } else {
                        returnTypeString = null;
                    }
                } else {
                    if(this.returnType.equals("void")) {
                        returnTypeString = null;
                    } else {
                        returnTypeString = this.returnType;
                    }
                }
                if(returnTypeString != null) {
                    tags.add(make.DocReturn(Collections.singletonList(make.Text("the " + returnTypeString))));
                }
                // @Throw
                for (ExpressionTree expressionTree : current.getThrows()) {
                    tags.add(make.Throws(make.Reference(expressionTree, null, null), Collections.EMPTY_LIST));
                }
                DocCommentTree newDoc = make.DocComment(Collections.EMPTY_LIST, Collections.EMPTY_LIST, tags);
                rewrite(synthConstructor? nju : tree, null, newDoc);
            }
            rewrite(tree, make.asReplacementOf(nju, tree));
        }
    }

    private boolean isMethodMatch(Element method, Element p) {
        if (!RefactoringUtils.isExecutableElement(method)) {
            return false;
        }
        if(compatible) {
            return method == p;
        } else if (allMethods !=null) {
            for (ElementHandle<ExecutableElement> mh: allMethods) {
                ExecutableElement baseMethod =  mh.resolve(workingCopy);
                if (baseMethod==null) {
                    Logger.getLogger("org.netbeans.modules.refactoring.java").info("ChangeParamsTransformer cannot resolve " + mh);
                    continue;
                }
                if (baseMethod.equals(method) || workingCopy.getElements().overrides((ExecutableElement)method, baseMethod, workingCopy.getElementUtilities().enclosingTypeElement(baseMethod))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSyntheticConstructorOfAnnonymousClass(Element el) {
        if (el != null && el.getKind() == ElementKind.CONSTRUCTOR
                && workingCopy.getElementUtilities().isSynthetic(el)) {
            Element enclosingElement = el.getEnclosingElement();
            return enclosingElement != null && enclosingElement.getKind().isClass()
                    && ((TypeElement) enclosingElement).getNestingKind() == NestingKind.ANONYMOUS;
        }
        return false;
    }
    
    private BlockTree translateBody(BlockTree blockTree,  final List<? extends VariableTree> parameters, ExecutableElement p, boolean renameParams) {
        final Map<ExpressionTree, ExpressionTree> original2Translated = new HashMap<ExpressionTree, ExpressionTree>();
        boolean changed = false;
        do {
            original2Translated.clear();
            if(renameParams) {
                ErrorAwareTreeScanner<Void, Void> idScan = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitIdentifier(IdentifierTree node, Void p) {
                        String name = node.getName().toString();
                        if (getCurrentPath().getParentPath().getLeaf().getKind() != Kind.MEMBER_SELECT){
                            for (int i = 0; i < paramInfos.length; i++) {
                                ParameterInfo parameterInfo = paramInfos[i];
                                if(parameterInfo.getOriginalIndex() >= 0 &&
                                        parameters.get(parameterInfo.getOriginalIndex()).getName().contentEquals(name)) {
                                    original2Translated.put(node, make.Identifier(parameterInfo.getName()));
                                }
                            }
                        }
                        return super.visitIdentifier(node, p);
                    }
                };
                idScan.scan(blockTree, null);
                blockTree = (BlockTree) workingCopy.getTreeUtilities().translate(blockTree, original2Translated);
            }
            original2Translated.clear();
            ErrorAwareTreeScanner<Boolean, ExecutableElement> methodScanner = new ErrorAwareTreeScanner<Boolean, ExecutableElement>() {
                @Override
                public Boolean visitMethodInvocation(MethodInvocationTree node, ExecutableElement p) {
                    boolean changed = false;
                    final TreePath path = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), node);
                    if(path != null) {
                        Element el = workingCopy.getTrees().getElement(path);
                        if (isMethodMatch(el, p)) {
                            List<ExpressionTree> arguments = getNewArguments(node.getArguments(), false, p);
                            MethodInvocationTree nju = make.MethodInvocation(
                                    (List<ExpressionTree>)node.getTypeArguments(),
                                    newName != null ? make.setLabel(node.getMethodSelect(), newName) : node.getMethodSelect(),
                                    arguments);
                            original2Translated.put(node, nju);
                            changed = true;
                        }
                    }
                    return super.visitMethodInvocation(node, p) || changed;
                }
                
                @Override
                public Boolean reduce(Boolean r1, Boolean r2) {
                    return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
                }
            };
            changed = methodScanner.scan(blockTree, p) == Boolean.TRUE;
            if(changed) {
                blockTree = (BlockTree) workingCopy.getTreeUtilities().translate(blockTree, original2Translated);
            }
        } while(changed);

        return blockTree;
    }

    private ExpressionTree translateExpression(ExpressionTree expressionTree, final List<? extends ExpressionTree> currentArguments, ExecutableElement p) {
        final Map<ExpressionTree, ExpressionTree> original2Translated = new HashMap<ExpressionTree, ExpressionTree>();
        boolean changed = false;
        do {
            original2Translated.clear();
            ErrorAwareTreeScanner<Void, Void> idScan = new ErrorAwareTreeScanner<Void, Void>() {
                @Override
                public Void visitIdentifier(IdentifierTree node, Void p) {
                    String name = node.getName().toString();
                    if (getCurrentPath().getParentPath().getLeaf().getKind() != Kind.MEMBER_SELECT){
                        for (int i = 0; i < paramInfos.length; i++) {
                            ParameterInfo parameterInfo = paramInfos[i];
                            if(parameterInfo.getOriginalIndex() >= 0 && parameterInfo.getName().equals(name)) {
                                original2Translated.put(node, currentArguments.get(parameterInfo.getOriginalIndex()));
                            }
                        }
                    }
                    return super.visitIdentifier(node, p);
                }
            };
            idScan.scan(expressionTree, null);
            expressionTree = (ExpressionTree) workingCopy.getTreeUtilities().translate(expressionTree, original2Translated);
            
            original2Translated.clear();
            ErrorAwareTreeScanner<Boolean, ExecutableElement> methodScanner = new ErrorAwareTreeScanner<Boolean, ExecutableElement>() {
                @Override
                public Boolean visitMethodInvocation(MethodInvocationTree node, ExecutableElement p) {
                    boolean changed = false;
                    final TreePath path = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), node);
                    if(path != null) {
                        Element el = workingCopy.getTrees().getElement(path);
                        if (isMethodMatch(el, p)) {
                            List<ExpressionTree> arguments = getNewArguments(node.getArguments(), false, p);
                            MethodInvocationTree nju = make.MethodInvocation(
                                    (List<ExpressionTree>)node.getTypeArguments(),
                                    newName != null ? make.setLabel(node.getMethodSelect(), newName) : node.getMethodSelect(),
                                    arguments);
                            original2Translated.put(node, nju);
                            changed = true;
                        }
                    }
                    return super.visitMethodInvocation(node, p) || changed;
                }
                
                @Override
                public Boolean reduce(Boolean r1, Boolean r2) {
                    return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
                }
            };
            changed = methodScanner.scan(expressionTree, p) == Boolean.TRUE;
            if(changed) {
                expressionTree = (ExpressionTree) workingCopy.getTreeUtilities().translate(expressionTree, original2Translated);
            }
        } while(changed);

        return expressionTree;
    }

/**
     * Orders tags as follows
     * <ul>
     * <li>@author (classes and interfaces only, required)</li>
     * <li>@version (classes and interfaces only, required. See footnote 1)</li>
     * <li>@param (methods and constructors only)</li>
     * <li>@return (methods only)</li>
     * <li>@exception (</li>
     * <li>@throws is a synonym added in Javadoc 1.2)</li>
     * <li>@see</li>
     * <li>@since</li>
     * <li>@serial (or @serialField or @serialData)</li>
     * <li>@deprecated (see How and When To Deprecate APIs)</li>
     * </ul>
     */
    private static class TagComparator implements Comparator<DocTree> {
        
        private static final int HIGHER = -1;
        private static final int EQUAL = 0;
        private static final int LOWER = 1;

        @Override
        public int compare(DocTree t, DocTree t1) {
            return compareTag(t, t1);
        }
        
        public static int compareTag(DocTree t, DocTree t1) {
            if(t.getKind() == t1.getKind()) {
                if(t.getKind() == DocTree.Kind.PARAM) {
                    ParamTree p = (ParamTree) t;
                    ParamTree p1 = (ParamTree) t1;
                    if(p.isTypeParameter() && !p1.isTypeParameter()) {
                        return HIGHER;
                    } else if(!p.isTypeParameter() && p1.isTypeParameter()) {
                        return LOWER;
                    }
                }
                return EQUAL;
            }
            switch(t.getKind()) {
                case AUTHOR:
                    return HIGHER;
                case VERSION:
                    if(t1.getKind() == DocTree.Kind.AUTHOR) {
                        return LOWER;
                    }
                    return HIGHER;
                case PARAM:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION) {
                        return LOWER;
                    }
                    return HIGHER;
                case RETURN:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM) {
                        return LOWER;
                    }
                    return HIGHER;
                case EXCEPTION:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN) {
                        return LOWER;
                    }
                    return HIGHER;
                case THROWS:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN
                            || t1.getKind() == DocTree.Kind.EXCEPTION) {
                        return LOWER;
                    }
                    return HIGHER;
                case SEE:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN
                            || t1.getKind() == DocTree.Kind.EXCEPTION
                            || t1.getKind() == DocTree.Kind.THROWS) {
                        return LOWER;
                    }
                    return HIGHER;
                case SINCE:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN
                            || t1.getKind() == DocTree.Kind.EXCEPTION
                            || t1.getKind() == DocTree.Kind.THROWS
                            || t1.getKind() == DocTree.Kind.SEE) {
                        return LOWER;
                    }
                    return HIGHER;
                case SERIAL:
                case SERIAL_DATA:
                case SERIAL_FIELD:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN
                            || t1.getKind() == DocTree.Kind.EXCEPTION
                            || t1.getKind() == DocTree.Kind.THROWS
                            || t1.getKind() == DocTree.Kind.SEE
                            || t1.getKind() == DocTree.Kind.SINCE) {
                        return LOWER;
                    }
                    return HIGHER;
                case DEPRECATED:
                    if(t1.getKind() == DocTree.Kind.UNKNOWN_BLOCK_TAG) {
                        return HIGHER;
                    }
                    return LOWER;
                case UNKNOWN_BLOCK_TAG:
                    return LOWER;
            }
            return LOWER;
        }
    }

    private static class FakaParamTree implements ParamTree {

        public FakaParamTree() {
        }

        @Override
        public boolean isTypeParameter() {
            return false;
        }

        @Override
        public com.sun.source.doctree.IdentifierTree getName() {
            return null;
        }

        @Override
        public List<? extends DocTree> getDescription() {
            return null;
        }

        @Override
        public String getTagName() {
            return null;
        }

        @Override
        public DocTree.Kind getKind() {
            return PARAM;
        }

        @Override
        public <R, D> R accept(DocTreeVisitor<R, D> visitor, D data) {
            return null;
        }
    }
}
