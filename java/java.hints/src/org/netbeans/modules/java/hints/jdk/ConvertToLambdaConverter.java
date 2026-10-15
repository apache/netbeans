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
/*
 * Contributor(s): Lyle Franklin <lylejfranklin@gmail.com>
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ReturnTree;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.spiimpl.pm.PatternCompiler;

public class ConvertToLambdaConverter {

    private final TreePath pathToNewClassTree;
    private final NewClassTree newClassTree;
    private final WorkingCopy copy;
    private final Scope localScope;
    private final ConvertToLambdaPreconditionChecker preconditionChecker;

    public ConvertToLambdaConverter(TreePath pathToNewClassTree, WorkingCopy copy) {
        this.pathToNewClassTree = pathToNewClassTree;
        this.newClassTree = (NewClassTree) this.pathToNewClassTree.getLeaf();
        this.copy = copy;
        this.localScope = getScopeFromTree(this.pathToNewClassTree);

        this.preconditionChecker = new ConvertToLambdaPreconditionChecker(this.pathToNewClassTree, this.copy);
    }

    public void performRewriteToLambda() {

        LambdaExpressionTree lambdaTree = getLambdaTreeFromAnonymous(newClassTree, copy);
        if (lambdaTree == null) {
            return;
        }
        if (preconditionChecker.foundShadowedVariable()) {
            TreePath pathToLambda = new TreePath(pathToNewClassTree, lambdaTree);
            renameShadowedVariables(pathToLambda);
        }

        ExpressionTree convertedTree = lambdaTree;
        if (preconditionChecker.needsCastToExpectedType()) {
            convertedTree = getTreeWithCastPrepended(lambdaTree, newClassTree.getIdentifier());
        }

        copy.rewrite(newClassTree, convertedTree);
    }
    
    private static Tree possiblyCast(WorkingCopy copy, ExpressionTree tree, TreePath path, boolean typeCast) {
        if (!typeCast) {
            return tree;
        }
        NewClassTree nct = (NewClassTree)path.getLeaf();
        return copy.getTreeMaker().TypeCast(nct.getIdentifier(), tree);
    }
    
    public static Tree newClassToConstructorReference(WorkingCopy copy, Tree tree, TreePath contextPath, List<? extends VariableTree> passedParameters, boolean addTypeCast) {
        NewClassTree nct = (NewClassTree)tree;
        if (passedParameters.size() != nct.getArguments().size()) {
            return null;
        }
        Element e = copy.getTrees().getElement(new TreePath(contextPath, tree));
        if (e == null || e.getKind() != ElementKind.CONSTRUCTOR) {
            return null;
        }
        TreeMaker make = copy.getTreeMaker();
        return possiblyCast(copy, 
                make.MemberReference(MemberReferenceTree.ReferenceMode.NEW, nct.getIdentifier(), "new", (List<? extends ExpressionTree>)nct.getTypeArguments()), 
                contextPath, addTypeCast);
    }

    public static Tree methodInvocationToMemberReference(WorkingCopy copy, Tree tree, TreePath contextPath, List<? extends VariableTree> passedParameters, boolean addTypeCast) {
        if (tree.getKind() != Tree.Kind.METHOD_INVOCATION)
            return null;
        ExpressionTree ms = ((MethodInvocationTree)tree).getMethodSelect();
        Element e = copy.getTrees().getElement(new TreePath(contextPath, ms));
        if (e == null || e.getKind() != ElementKind.METHOD) {
            return null;
        }
        Name name = null;
        ExpressionTree expr = null;
        TreeMaker make = copy.getTreeMaker();
        
        if (ms.getKind() == Tree.Kind.IDENTIFIER) {
            name = ((IdentifierTree)ms).getName();
            expr = e.getModifiers().contains(Modifier.STATIC) ? 
                    make.Identifier(e.getEnclosingElement()) :
                    make.Identifier("this"); //NOI18N
        } else if (ms.getKind() == Tree.Kind.MEMBER_SELECT) {
            name = ((MemberSelectTree)ms).getIdentifier();
            if (passedParameters.size() == ((MethodInvocationTree)tree).getArguments().size()) {
                expr = ((MemberSelectTree)ms).getExpression();
            } else {
                expr = make.Identifier(e.getEnclosingElement());
            }
        }
        if (name == null || expr == null) {
            return null;
        }
        return possiblyCast(copy, 
                make.MemberReference(MemberReferenceTree.ReferenceMode.INVOKE, expr, name, Collections.<ExpressionTree>emptyList()),
                contextPath, addTypeCast
        );
    }
    
    public void performRewriteToMemberReference() {
        MethodTree methodTree = getMethodFromFunctionalInterface(newClassTree);
        if (methodTree.getBody() == null || methodTree.getBody().getStatements().size() != 1)
            return;
        Tree tree = methodTree.getBody().getStatements().get(0);
        if (tree.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
            tree = ((ExpressionStatementTree)tree).getExpression();
        } else if (tree.getKind() == Tree.Kind.RETURN) {
            tree = ((ReturnTree)tree).getExpression();
        } else {
            return;
        }
        Tree changed = null;
        if (tree.getKind() == Tree.Kind.METHOD_INVOCATION) {
            changed = methodInvocationToMemberReference(copy, tree, pathToNewClassTree, methodTree.getParameters(),
                    preconditionChecker.needsCastToExpectedType());
        } else if (tree.getKind() == Tree.Kind.NEW_CLASS) {
            changed = newClassToConstructorReference(copy, tree, pathToNewClassTree, methodTree.getParameters(), preconditionChecker.needsCastToExpectedType());
        }
        if (changed != null) {
            copy.rewrite(newClassTree, changed);
        }
    }

    private LambdaExpressionTree getLambdaTreeFromAnonymous(NewClassTree newClassTree, WorkingCopy copy) {

        TreeMaker make = copy.getTreeMaker();

        MethodTree methodTree = getMethodFromFunctionalInterface(newClassTree);
        Tree lambdaBody = getLambdaBody(methodTree, copy);
        if (lambdaBody == null) {
            return null;
        }
        return make.LambdaExpression(methodTree.getParameters(), lambdaBody);
    }

    private MethodTree getMethodFromFunctionalInterface(NewClassTree newClassTree) {
        //ignore first member, which is a synthetic constructor call
        ClassTree classTree = newClassTree.getClassBody();
        return (MethodTree) classTree.getMembers().get(1);
    }

    private Tree getLambdaBody(MethodTree methodTree, WorkingCopy copy) {
        if (methodTree.getBody() == null) {
            return null;
        }
        TreePath pathToMethodBody = TreePath.getPath(pathToNewClassTree, methodTree.getBody());

        //if body is just a return statement, the lambda body should omit the block and return keyword
        Pattern pattern = PatternCompiler.compile(copy, "{ return $expression; }",
                Collections.<String, TypeMirror>emptyMap(), Collections.<String>emptyList());
        Collection<? extends Occurrence> matches = Matcher.create(copy)
                .setSearchRoot(pathToMethodBody)
                .setTreeTopSearch()
                .match(pattern);

        if (matches.isEmpty()) {
            return methodTree.getBody();
        }
        return matches.iterator().next().getVariables().get("$expression").getLeaf();
    }

    private void renameShadowedVariables(TreePath treePath) {
        new ShadowedVariableRenameScanner().scan(treePath, copy.getTrees());
    }

    private ExpressionTree getTreeWithCastPrepended(ExpressionTree tree, Tree expectedType) {
        TreeMaker make = copy.getTreeMaker();
        return make.TypeCast(expectedType, tree);
    }

    private class ShadowedVariableRenameScanner extends ErrorAwareTreePathScanner<Tree, Trees> {

        private final Map<Element, CharSequence> originalToNewName = new HashMap<Element, CharSequence>();

        @Override
        public Tree visitMethod(MethodTree methodTree, Trees trees) {

            //don't visit synthetic methods
            TreePath path = getCurrentPath();
            if (copy.getTreeUtilities().isSynthetic(path)) {
                return methodTree;
            }
            return super.visitMethod(methodTree, trees);
        }

        @Override
        public Tree visitVariable(VariableTree variableDeclTree, Trees trees) {
            //check for shadowed variable
            if (isNameAlreadyUsed(variableDeclTree.getName())) {
                TreePath path = getCurrentPath();

                CharSequence newName = getUniqueName(variableDeclTree.getName());

                Element el = trees.getElement(path);
                if (el != null) {
                    originalToNewName.put(el, newName);
                }

                VariableTree newTree = copy.getTreeMaker()
                        .Variable(variableDeclTree.getModifiers(), newName, variableDeclTree.getType(), variableDeclTree.getInitializer());
                copy.rewrite(variableDeclTree, newTree);
            }

            return super.visitVariable(variableDeclTree, trees);
        }

        @Override
        public Tree visitIdentifier(IdentifierTree identifierTree, Trees trees) {
            //rename shadowed variable
            TreePath currentPath = getCurrentPath();
            CharSequence newName = originalToNewName.get(trees.getElement(currentPath));
            if (newName != null) {
                IdentifierTree newTree = copy.getTreeMaker().Identifier(newName);
                copy.rewrite(identifierTree, newTree);
            }

            return super.visitIdentifier(identifierTree, trees);
        }

        private boolean isNameAlreadyUsed(CharSequence name) {
            return originalToNewName.containsValue(name.toString()) ||
                    shadowsVariable(name);
        }

        private CharSequence getUniqueName(CharSequence originalName) {
            int counter = 1;
            CharSequence newName = originalName;
            while (isNameAlreadyUsed(newName)) {
                newName = getNameWithCounterAdded(newName, counter);
                counter++;
            }
            return newName;
        }

        private CharSequence getNameWithCounterAdded(CharSequence newName, int counter) {
            char lastChar = newName.charAt(newName.length() - 1);
            if (Character.isDigit(lastChar)) {
                return newName.subSequence(0, newName.length() - 1).toString() + counter;
            } else {
                return newName.toString() + counter;
            }
        }
    }

    private boolean shadowsVariable(CharSequence variableName) {
        return Utilities.isSymbolUsed(copy, pathToNewClassTree, variableName, localScope);
    }

    private Scope getScopeFromTree(TreePath path) {
        return copy.getTrees().getScope(path);
    }
}
