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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LambdaExpressionTree.BodyKind;

import static com.sun.source.tree.LambdaExpressionTree.BodyKind.STATEMENT;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jdk.ConvertToLambdaConverter;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class Lambda {
    
    private static final String[] LAMBDA_PARAMETER_ERROR_CODES = {"compiler.err.invalid.lambda.parameter.declaration"};// NOI18N

    @Hint(displayName="#DN_lambda2Class", description="#DESC_lambda2Class", category="suggestions", hintKind=Hint.Kind.ACTION,
            minSourceVersion = "8")
    @Messages({
        "DN_lambda2Class=Convert Lambda Expression to Anonymous Innerclass",
        "DESC_lambda2Class=Converts lambda expressions to anonymous inner classes",
        "ERR_lambda2Class=Anonymous class can be used"
    })
    @TriggerTreeKind(Kind.LAMBDA_EXPRESSION)
    public static ErrorDescription lambda2Class(HintContext ctx) {
        TypeMirror samType = ctx.getInfo().getTrees().getTypeMirror(ctx.getPath());
        
        if (samType == null || samType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.ERR_lambda2Class(), new Lambda2Anonymous(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }
    
    @Hint(displayName="#DN_lambda2MemberReference", description="#DESC_lambda2MemberReference", category="suggestions", hintKind=Hint.Kind.ACTION,
            minSourceVersion = "8")
    @Messages({
        "DN_lambda2MemberReference=Convert Lambda Expression to Member Reference",
        "DESC_lambda2MemberReference=Converts lambda expressions to member references",
        "ERR_lambda2MemberReference=Member reference can be used",
        "FIX_lambda2MemberReference=Use member reference"
    })
    @TriggerTreeKind(Kind.LAMBDA_EXPRESSION)
    public static ErrorDescription lambda2MemberReference(HintContext ctx) {
        TypeMirror samType = ctx.getInfo().getTrees().getTypeMirror(ctx.getPath());        
        if (samType == null || samType.getKind() != TypeKind.DECLARED) {
            return null;
        }

        LambdaExpressionTree lambda = (LambdaExpressionTree) ctx.getPath().getLeaf();
        Tree tree = lambda.getBody();
        if (tree == null) {
            return null;
        }
        if (tree.getKind() == Tree.Kind.BLOCK) {
            if (((BlockTree)tree).getStatements().size() == 1) {
                tree = ((BlockTree)tree).getStatements().get(0);
                if (tree.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                    tree = ((ExpressionStatementTree)tree).getExpression();
                } else if (tree.getKind() == Tree.Kind.RETURN) {
                    tree = ((ReturnTree)tree).getExpression();
                } else {
                    return null;
                }
                if (tree == null) {
                    return null;
                }
            } else {
                return null;
            }
        }

        if (tree.getKind() != Tree.Kind.METHOD_INVOCATION) {
            return null;
        }

        boolean check = true;
        Iterator<? extends VariableTree> paramsIt = lambda.getParameters().iterator();
        ExpressionTree methodSelect = ((MethodInvocationTree)tree).getMethodSelect();
        if (paramsIt.hasNext() && methodSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
            ExpressionTree expr = ((MemberSelectTree) methodSelect).getExpression();
            if (expr.getKind() == Tree.Kind.IDENTIFIER) {
                if (!((IdentifierTree)expr).getName().contentEquals(paramsIt.next().getName())) {
                    paramsIt = lambda.getParameters().iterator();
                }
            }
        }
        Iterator<? extends ExpressionTree> argsIt = ((MethodInvocationTree)tree).getArguments().iterator();
        while (check && argsIt.hasNext() && paramsIt.hasNext()) {
            ExpressionTree arg = argsIt.next();
            if (arg.getKind() != Tree.Kind.IDENTIFIER || !paramsIt.next().getName().contentEquals(((IdentifierTree)arg).getName())) {
                check = false;
            }
        }
        if (!check || paramsIt.hasNext() || argsIt.hasNext()) {
            return null;
        }

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.ERR_lambda2MemberReference(), new Lambda2MemberReference(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }
    
    @Hint(displayName="#DN_expression2Return", description="#DESC_expression2Return", category="suggestions", hintKind=Hint.Kind.ACTION,
            minSourceVersion = "8")
    @Messages({
        "DN_expression2Return=Convert Lambda Body to Use a Block",
        "DESC_expression2Return=Converts lambda bodies to use blocks rather than expressions",
        "ERR_expression2Return=Block as the lambda's body can be used",
        "FIX_expression2Return=Use block as the lambda's body"
    })
    @TriggerPattern("($args$) -> $lambdaExpression")
    public static ErrorDescription expression2Return(HintContext ctx) {
        if (((LambdaExpressionTree) ctx.getPath().getLeaf()).getBodyKind() != BodyKind.EXPRESSION) {
            return null;
        }
        
        TypeMirror lambdaExpressionType = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$lambdaExpression"));
        String target =   lambdaExpressionType == null || lambdaExpressionType.getKind() != TypeKind.VOID
                        ? "($args$) -> { return $lambdaExpression; }"
                        : "($args$) -> { $lambdaExpression; }";
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.ERR_expression2Return(), JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_expression2Return(), ctx.getPath(), target));
    }
    
    @Hint(displayName="#DN_memberReference2Lambda", description="#DESC_memberReference2Lambda", category="suggestions", hintKind=Hint.Kind.ACTION)
    @Messages({
        "DN_memberReference2Lambda=Convert Member Reference to Lambda Expression",
        "DESC_memberReference2Lambda=Converts member references to lambda expressions",
        "ERR_memberReference2Lambda=Lambda expression can be used",
        "FIX_memberReference2Lambda=Use lambda expression"
    })
    @TriggerTreeKind(Kind.MEMBER_REFERENCE)
    public static ErrorDescription reference2Lambda(HintContext ctx) {
        Element refered = ctx.getInfo().getTrees().getElement(ctx.getPath());
        
        if (refered == null || refered.getKind() != ElementKind.METHOD) {
            return null;//XXX: constructors!
        }        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.ERR_memberReference2Lambda(), new MemberReference2Lambda(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }
    
    @Hint(displayName="#DN_addExplicitLambdaParameters", description="#DESC_addExplicitLambdaParameters", category="suggestions", hintKind=Hint.Kind.ACTION)
    @Messages({
        "DN_addExplicitLambdaParameters=Convert Lambda to Use Explicit Parameter Types",
        "DESC_addExplicitLambdaParameters=Converts lambdas to use explicit parameter types",
        "ERR_addExplicitLambdaParameters=Explicit parameter types can be used",
        "FIX_addExplicitLambdaParameters=Use explicit parameter types"
    })
    @TriggerTreeKind(Kind.LAMBDA_EXPRESSION)
    public static ErrorDescription explicitParameterTypes(HintContext ctx) {
        LambdaExpressionTree let = (LambdaExpressionTree) ctx.getPath().getLeaf();

        if (ctx.getInfo().getTreeUtilities().hasError(let)) {
            return null;
        }

        boolean hasSyntheticParameterName = false;
        
        for (VariableTree var : let.getParameters()) {
            hasSyntheticParameterName |= var.getType() == null || ctx.getInfo().getTreeUtilities().isSynthetic(TreePath.getPath(ctx.getPath(), var.getType()));
        }
        
        if (!hasSyntheticParameterName) {
            return null;
        }
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_addExplicitLambdaParameters(), new AddExplicitLambdaParameterTypes(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }
    
    @Hint(displayName = "#DN_ConvertVarLambdaParameters", description = "#DESC_ConvertVarLambdaParameters", category = "suggestions", hintKind = Hint.Kind.ACTION, minSourceVersion = "11")
    @TriggerTreeKind(Kind.LAMBDA_EXPRESSION)
    public static ErrorDescription implicitVarParameterTypes(HintContext ctx) {
        // hint will be enable only for JDK-11 or above.
        if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_9) < 2) {
            return null;
        }
        // Check invalid lambda parameter declaration
        if (ctx.getInfo().getTreeUtilities().hasError(ctx.getPath().getLeaf())) {
            return null;
        }
        // Check var parameter types
        LambdaExpressionTree let = (LambdaExpressionTree) ctx.getPath().getLeaf();
        if (let.getParameters() == null || let.getParameters().isEmpty()) {
            return null;
        }
        VariableTree var = let.getParameters().get(0);
        if (ctx.getInfo().getTreeUtilities().isVarType(new TreePath(ctx.getPath(), var))) {
            return null;
        }

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), NbBundle.getMessage(Lambda.class, "ERR_ConvertVarLambdaParameters"), new AddVarLambdaParameterTypes(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }

    private static ExecutableElement findAbstractMethod(CompilationInfo info, TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return null;
        }
        
        TypeElement clazz = (TypeElement) ((DeclaredType) type).asElement();
        
        if (!clazz.getKind().isInterface()) {
            return null;
        }
        
        for (ExecutableElement ee : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
            if (ee.getModifiers().contains(Modifier.ABSTRACT)) {
                return ee;
            }
        }
        
        for (TypeMirror tm : info.getTypes().directSupertypes(type)) {
            ExecutableElement ee = findAbstractMethod(info, tm);
            
            if (ee != null) {
                return ee;
            }
        }
        
        return null;
    }
    
    private static final class Lambda2Anonymous extends JavaFix {

        public Lambda2Anonymous(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        @Messages("FIX_lambda2Class=Use anonymous inner class")
        protected String getText() {
            return Bundle.FIX_lambda2Class();
        }
        
        private static TypeMirror avoidIntersectionType(CompilationInfo copy, TypeMirror org) {
            if (org.getKind() == TypeKind.INTERSECTION) {
                Element objEl = copy.getElements().getTypeElement("java.lang.Object"); // NOI18N
                if (objEl == null) {
                    // TODO: report
                    return org;
                }
                return objEl.asType();
            } else {
                return org;
            }
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            final WorkingCopy copy = ctx.getWorkingCopy();
            LambdaExpressionTree lambda = (LambdaExpressionTree) ctx.getPath().getLeaf();
            TypeMirror samType = copy.getTrees().getTypeMirror(ctx.getPath());
            
            if (samType == null || samType.getKind() != TypeKind.DECLARED) {
                // FIXME: report
                return ;
            }
            
            ExecutableType descriptorType = copy.getTypeUtilities().getDescriptorType((DeclaredType) samType);
            ExecutableElement abstractMethod = findAbstractMethod(copy, samType);
            TypeElement samTypeElement = (TypeElement) ((DeclaredType) samType).asElement();
            List<VariableTree> methodParams = new ArrayList<>();
            Iterator<? extends TypeMirror> resolvedParamTypes = descriptorType.getParameterTypes().iterator();
            Iterator<? extends VariableTree> actualParams = lambda.getParameters().iterator();
            final TreeMaker make = copy.getTreeMaker();
            
            while (resolvedParamTypes.hasNext() && actualParams.hasNext()) {
                VariableTree p = actualParams.next();
                TypeMirror resolvedType = resolvedParamTypes.next();
                
                //XXX: should handle anonymous lambda parameters ('_')
                if (p.getType() == null || copy.getTreeUtilities().isSynthetic(new TreePath(ctx.getPath(), p.getType()))) {
                    methodParams.add(make.Variable(p.getModifiers(), p.getName(), make.Type(SourceUtils.resolveCapturedType(copy, resolvedType)), null));
                } else {
                    methodParams.add(p);
                }
            }
            
            BlockTree newMethodBody;
            switch (lambda.getBodyKind()) {
                case STATEMENT:
                    newMethodBody = (BlockTree) lambda.getBody();
                    break;
                case EXPRESSION:
                    StatementTree mainStatement;
                    if (descriptorType.getReturnType() == null || descriptorType.getReturnType().getKind() != TypeKind.VOID) {
                        mainStatement = make.Return((ExpressionTree) lambda.getBody());
                    } else {
                        mainStatement = make.ExpressionStatement((ExpressionTree) lambda.getBody());
                    }
                    newMethodBody = make.Block(Collections.singletonList(mainStatement), false);
                    break;
                default:
                    throw new IllegalStateException();
            }
            
            List<ExpressionTree> thrownTypes = new ArrayList<>(abstractMethod.getThrownTypes().size());
            for (TypeMirror tm : abstractMethod.getThrownTypes()) {
                // ErrorTypes are somehow handled, too, by make.Type
                thrownTypes.add((ExpressionTree)make.Type(tm));
            }
            ModifiersTree mt = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
            // should I ever test for >= source 5, if there's a Lambda :) in the source already ?
//            if (copy.getSourceVersion().compareTo(SourceVersion.RELEASE_5) >= 0) {
                boolean generate = copy.getElements().getTypeElement("java.lang.Override") != null;

                if (generate) {
                   mt = make.addModifiersAnnotation(
                           mt, make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList()));
                }
//            }
    
            TypeMirror retType = avoidIntersectionType(copy, descriptorType.getReturnType());
            
            MethodTree newMethod = make.Method(mt,
                                               abstractMethod.getSimpleName(),
                                               make.Type(retType),
                                               Collections.<TypeParameterTree>emptyList(), //XXX: type parameters
                                               methodParams,
                                               // TODO: possibly filter out those exceptions, which are handled/never thrown 
                                               // from the body
                                               thrownTypes,
                                               newMethodBody,
                                               null);
            ClassTree innerClass = make.Class(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                              samTypeElement.getSimpleName(),
                                              Collections.<TypeParameterTree>emptyList(),
                                              null,
                                              Collections.<Tree>emptyList(),
                                              Collections.singletonList(newMethod));
            ExpressionTree targetTypeTree;
            
            if (((DeclaredType) samType).getTypeArguments().isEmpty()) {
                targetTypeTree = make.QualIdent(samTypeElement);
            } else {
                List<Tree> typeArguments = new ArrayList<>();
                for (TypeMirror ta : ((DeclaredType) samType).getTypeArguments()) {
                    typeArguments.add(make.Type(
                            avoidIntersectionType(copy, SourceUtils.resolveCapturedType(copy, ta))));
                }
                targetTypeTree = (ExpressionTree) make.ParameterizedType(make.QualIdent(samTypeElement), typeArguments);
            }
            
            
            NewClassTree newClass = make.NewClass(null, Collections.<ExpressionTree>emptyList(), targetTypeTree, Collections.<ExpressionTree>emptyList(), innerClass);
            
            TreePath clazz = ctx.getPath();
            
            while (clazz != null && !TreeUtilities.CLASS_TREE_KINDS.contains(clazz.getLeaf().getKind())) {
                clazz = clazz.getParentPath();
            }
            
            if (clazz == null) {
                return;
            }
            
            Element clazzElement = copy.getTrees().getElement(clazz);
            if (clazzElement == null || !(
                    clazzElement.getKind().isClass() || clazzElement.getKind().isInterface())) {
                return;
            }
            
            copy.rewrite(ctx.getPath().getLeaf(), newClass);

            final Name outterClassName = ((ClassTree) clazz.getLeaf()).getSimpleName();
            // possibly wrong, since the rewritten code will work in context of a different class.
            Scope s = copy.getTrees().getScope(ctx.getPath());
            final Map<Name, Element> types = new HashMap<>();
            final Map<Name, Element> vars = new HashMap<>();
            final Set<Name> methods = new HashSet<>();

            // lambda parameter names will be used as method parameter names, so variable clash should
            // not occur.
            for (Element e : copy.getElementUtilities().getMembers(samTypeElement.asType(), null)) {
                switch (e.getKind()) {
                    case ENUM:
                    case CLASS:
                    case ANNOTATION_TYPE:
                    case INTERFACE:
                    case RECORD:
                        types.put(e.getSimpleName(), e);
                        break;

                    case ENUM_CONSTANT:
                    case FIELD:
                        vars.put(e.getSimpleName(), e);
                        break;

                    case METHOD:
                        methods.add(e.getSimpleName());
                        break;
                }
            }
            types.put(samTypeElement.getSimpleName(), samTypeElement);
            
            new ErrorAwareTreePathScanner<Void, Boolean>() {
                @Override public Void visitIdentifier(final IdentifierTree node, Boolean p) {
                    boolean rewrite = false;
                    boolean statRef = false;
                    
                    if (node.getName().contentEquals("this") || node.getName().contentEquals("super")) {
                        if (types.containsKey(outterClassName)) {
                            copy.rewrite(node, make.MemberSelect(make.QualIdent(clazzElement), node.getName()));
                        } else {
                            copy.rewrite(node, make.MemberSelect(make.Identifier(outterClassName), node.getName()));
                        }
                    } else if (Boolean.TRUE != p) {
                        Element e = copy.getTrees().getElement(getCurrentPath());
                        Element other = null;
                        if (e != null) {
                            switch (e.getKind()) {
                                case METHOD: {
                                    if (methods.contains(e.getSimpleName())) {
                                        Map<? extends ExecutableElement,? extends ExecutableElement> conflicting = Utilities.findConflictingMethods(copy, samTypeElement, 
                                                true, Collections.singleton((ExecutableElement)e));
                                        rewrite = !conflicting.isEmpty();
                                    }
                                    break;
                                }
                                // fields and enum fields may be hidden by interface fields.
                                case ENUM_CONSTANT: 
                                    statRef = true;
                                    // fall through
                                case FIELD: 
                                    rewrite = (other = vars.get(e.getSimpleName())) != null;
                                    break;
                                // types may be hidden by inner interface types
                                case ANNOTATION_TYPE:
                                case CLASS:
                                case INTERFACE:
                                case RECORD:
                                case ENUM:
                                    rewrite = (other = types.get(e.getSimpleName())) != null;
                                    statRef = true;
                                    break;
                            }
                        }
                        if (rewrite) {
                            statRef |= (e.getModifiers().contains(Modifier.STATIC));
                            ExpressionTree n;
                            if (statRef && other == e) {
                                // static reference && the element was reintroduced actually
                                return super.visitIdentifier(node, p);
                            }
                            if (types.containsKey(outterClassName)) {
                                n = make.QualIdent(clazzElement);
                            } else {
                                n = make.Identifier(outterClassName);
                            }
                            if (!statRef) {
                                n = make.MemberSelect(n, "this"); // NOI18N
                            }
                            if (rewrite) {
                                copy.rewrite(node, make.MemberSelect(n, node.getName()));
                            }
                        }
                    }
                    return super.visitIdentifier(node, p);
                }
            }.scan(ctx.getPath(), null);
        }        
    }

    private static final class Lambda2MemberReference extends JavaFix {

        public Lambda2MemberReference(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_lambda2MemberReference();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            final WorkingCopy copy = ctx.getWorkingCopy();
            TypeMirror samType = copy.getTrees().getTypeMirror(ctx.getPath());
            if (samType == null || samType.getKind() != TypeKind.DECLARED) {
                // FIXME: report
                return ;
            }

            LambdaExpressionTree lambda = (LambdaExpressionTree) ctx.getPath().getLeaf();
            Tree tree = lambda.getBody();
            if (tree.getKind() == Tree.Kind.BLOCK) {
                if (((BlockTree)tree).getStatements().size() == 1) {
                    tree = ((BlockTree)tree).getStatements().get(0);
                    if (tree.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                        tree = ((ExpressionStatementTree)tree).getExpression();
                    } else if (tree.getKind() == Tree.Kind.RETURN) {
                        tree = ((ReturnTree)tree).getExpression();
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }

            Tree changed = null;
            if (tree.getKind() == Tree.Kind.METHOD_INVOCATION) {
                changed = ConvertToLambdaConverter.methodInvocationToMemberReference(copy, tree, ctx.getPath(), lambda.getParameters(), false);
            } else if (tree.getKind() == Tree.Kind.NEW_CLASS) {
                changed = ConvertToLambdaConverter.newClassToConstructorReference(copy, tree, ctx.getPath(), lambda.getParameters(), false);
            }
            if (changed != null) {
                copy.rewrite(lambda, changed);
            }
        }
    }

    private static final class MemberReference2Lambda extends JavaFix {

        public MemberReference2Lambda(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_memberReference2Lambda();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath reference = ctx.getPath();
            Element refered = ctx.getWorkingCopy().getTrees().getElement(reference);

            if (refered == null || refered.getKind() != ElementKind.METHOD) {
                //TODO: log
                return ;
            }

            MemberReferenceTree mrt = (MemberReferenceTree) ctx.getPath().getLeaf();

            Element on = ctx.getWorkingCopy().getTrees().getElement(new TreePath(ctx.getPath(), mrt.getQualifierExpression()));
            ExpressionTree reciever = mrt.getQualifierExpression();
            List<VariableTree> formals = new ArrayList<>();
            List<IdentifierTree> actuals = new ArrayList<>();
            Scope scope = ctx.getWorkingCopy().getTrees().getScope(reference);
            Set<String> usedNames = new HashSet<>();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            
            if (on != null && (on.getKind().isClass() || on.getKind().isInterface()) && !refered.getModifiers().contains(Modifier.STATIC)) {
                //static reference to instance method:
                String name = org.netbeans.modules.java.hints.errors.Utilities.getName(on.asType());
                name = org.netbeans.modules.java.hints.errors.Utilities.makeNameUnique(ctx.getWorkingCopy(), scope, name);
                formals.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, null, null));
                reciever = make.Identifier(name);
                usedNames.add(name);
            }
            
            for (VariableElement param : ((ExecutableElement) refered).getParameters()) {
                String name = org.netbeans.modules.java.hints.errors.Utilities.makeNameUnique(ctx.getWorkingCopy(), scope, param.getSimpleName().toString(), usedNames, null, null);                
                formals.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, null, null));
                actuals.add(make.Identifier(name));
            }
            
            LambdaExpressionTree lambda = make.LambdaExpression(formals, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(reciever, mrt.getName()), actuals));
            
            ctx.getWorkingCopy().rewrite(mrt, lambda);
        }
    }
    
    private static final class AddExplicitLambdaParameterTypes extends JavaFix {

        public AddExplicitLambdaParameterTypes(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_addExplicitLambdaParameters();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            LambdaExpressionTree let = (LambdaExpressionTree) ctx.getPath().getLeaf();

            for (VariableTree var : let.getParameters()) {
                TreePath typePath = TreePath.getPath(ctx.getPath(), var.getType());
                if (ctx.getWorkingCopy().getTreeUtilities().isSynthetic(typePath)) {
                    Tree imported = ctx.getWorkingCopy().getTreeMaker().Type(ctx.getWorkingCopy().getTrees().getTypeMirror(typePath));
                    ctx.getWorkingCopy().rewrite(var.getType(), imported);
                }
            }
        }
    }
    
    private static final class AddVarLambdaParameterTypes extends JavaFix {

        public AddVarLambdaParameterTypes(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(Lambda.class, "FIX_ConvertVarLambdaParameters");
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) throws Exception {
            if (ctx.getPath().getLeaf().getKind() == Kind.LAMBDA_EXPRESSION) {
                LambdaExpressionTree let = (LambdaExpressionTree) ctx.getPath().getLeaf();
                TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
                let.getParameters().forEach((var) -> {
                    ctx.getWorkingCopy().rewrite(var.getType(), make.Type("var")); // NOI18N
                });
            }
        }
    }
}
