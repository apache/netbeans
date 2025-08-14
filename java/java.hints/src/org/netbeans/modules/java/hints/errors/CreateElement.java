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
package org.netbeans.modules.java.hints.errors;

import java.util.Collection;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.hints.errors.CreateClassFix.CreateInnerClassFix;
import org.netbeans.modules.java.hints.errors.CreateClassFix.CreateOuterClassFix;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.errors.CreateElementUtilities.*;
import org.netbeans.modules.java.hints.errors.ErrorFixesFakeHint.FixKind;
import org.netbeans.modules.java.hints.errors.Utilities.MethodArguments;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Pair;

/**
 *
 * @author Jan Lahoda
 * @author markiewb (contributions)
 */
public final class CreateElement implements ErrorRule<Void> {
    private static final Logger LOG = Logger.getLogger(CreateElement.class.getName());
    private static final int PRIO_TESTSOURCEGROUP = 500;
    private static final int PRIO_MAINSOURCEGROUP = 1000;
    private static final int PRIO_INNER = 2000;
    
    /** Creates a new instance of CreateElement */
    public CreateElement() {
    }

    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList("compiler.err.cant.resolve.location", "compiler.err.cant.resolve.location.args", "compiler.err.cant.apply.symbol", "compiler.err.cant.apply.symbol.1", "compiler.err.cant.apply.symbols", "compiler.err.cant.resolve", "compiler.err.cant.resolve.args", CAST_KEY, "compiler.err.try.with.resources.expr.needs.var", "compiler.err.invalid.mref", "compiler.err.bad.initializer")); // NOI18N
    }
    public static final String CAST_KEY = "compiler.err.prob.found.req";

    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        try {
            return analyze(info, diagnosticKey, offset);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        } catch (ClassCastException e) {
            Logger.getLogger(CreateElement.class.getName()).log(Level.FINE, null, e);
            return null;
        }
    }

    static List<Fix> analyze(CompilationInfo info, int offset) throws IOException {
        return analyze(info, null, offset);
    }
    
    static List<Fix> analyze(CompilationInfo info, String diagnosticKey, int offset) throws IOException {
        List<Fix> result = analyzeImpl(info, diagnosticKey, offset);
        
        if (CAST_KEY.equals(diagnosticKey)) {
            result = new ArrayList<>(result);
            
            for (Iterator<Fix> it = result.iterator(); it.hasNext();) {
                Fix f = it.next();
                
                if (!(f instanceof CreateMethodFix)) {
                    it.remove();
                }
            }
        }
        
        return result;
    }
    
    private static List<Fix> analyzeImpl(CompilationInfo info, String diagnosticKey, int offset) throws IOException {
        if ("compiler.err.invalid.mref".equals(diagnosticKey)) {
            return computeMissingMemberRefFixes(info, offset);
        }

        TreePath errorPath = ErrorHintsProvider.findUnresolvedElement(info, offset);

        if (errorPath == null) {
            return Collections.<Fix>emptyList();
        }
        
        if (CAST_KEY.equals(diagnosticKey) && errorPath.getParentPath() != null && errorPath.getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION) {
            MethodInvocationTree mit = (MethodInvocationTree) errorPath.getParentPath().getLeaf();
            errorPath = new TreePath(errorPath.getParentPath(), mit.getMethodSelect());
            offset = (int) info.getTrees().getSourcePositions().getStartPosition(errorPath.getCompilationUnit(), errorPath.getLeaf());
        }

        if (info.getElements().getTypeElement("java.lang.Object") == null) { // NOI18N
            // broken java platform
            return Collections.<Fix>emptyList();
        }

        TreePath parent = null;
        TreePath firstClass = null;
        TreePath firstMethod = null;
        TreePath firstLambda = null;
        TreePath firstVar = null;
        TreePath firstInitializer = null;
        TreePath methodInvocation = null;
        TreePath newClass = null;
        boolean baseType = false;
        boolean lookupMethodInvocation = true;
        boolean lookupNCT = true;

        TreePath path = info.getTreeUtilities().pathFor(Math.max((int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), errorPath.getLeaf()), offset) + 1);

        while(path != null) {
            Tree leaf = path.getLeaf();
            Kind leafKind = leaf.getKind();

            if (!baseType && TreeUtilities.CLASS_TREE_KINDS.contains(leafKind) && parent != null && (((ClassTree)leaf).getExtendsClause() == parent.getLeaf() || ((ClassTree)leaf).getImplementsClause().contains(parent.getLeaf())))
                baseType = true;
            if (parent != null && parent.getLeaf() == errorPath.getLeaf())
                parent = path;
            if (leaf == errorPath.getLeaf() && parent == null)
                parent = path;
            if (TreeUtilities.CLASS_TREE_KINDS.contains(leafKind) && firstClass == null)
                firstClass = path;
            if (leafKind == Kind.METHOD && firstMethod == null && firstClass == null)
                firstMethod = path;
            if (leafKind == Kind.LAMBDA_EXPRESSION && firstMethod == null &&
                firstClass == null && firstLambda == null && firstInitializer == null)
                firstLambda = path;
            //static/dynamic initializer:
            if (   leafKind == Kind.BLOCK && TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())
                && firstMethod == null && firstClass == null)
                firstInitializer = path;
            
            if (leafKind == Kind.ANNOTATION) {
                // discard any methods, since there cannot be method calls in anno values
                methodInvocation = null;
                lookupMethodInvocation = false;
            }

            if (lookupMethodInvocation && leafKind == Kind.METHOD_INVOCATION) {
                methodInvocation = path;
            }

            if (leafKind == Kind.VARIABLE) {
                firstVar = path;
            }
            
            if (lookupNCT && leafKind == Kind.NEW_CLASS) {
                newClass = path;
            }

            if (leafKind == Kind.MEMBER_SELECT) {
                lookupMethodInvocation = leaf == errorPath.getLeaf();
            }

            if (leafKind != Kind.MEMBER_SELECT && leafKind != Kind.IDENTIFIER) {
                lookupMethodInvocation = false;
            }

            if (leafKind != Kind.MEMBER_SELECT && leafKind != Kind.IDENTIFIER && leafKind != Kind.PARAMETERIZED_TYPE) {
                lookupNCT = false;
            }

            path = path.getParentPath();
        }

        if (parent == null || parent.getLeaf() == errorPath.getLeaf())
            return Collections.<Fix>emptyList();

        Element e = info.getTrees().getElement(errorPath);

        if (e == null) {
            return Collections.<Fix>emptyList();
        }

        Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        Name name = e.getSimpleName();
        if (name == null) {
            if (ErrorHintsProvider.ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "e.simpleName=null"); // NOI18N
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "offset=" + offset); // NOI18N
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "errorTree=" + errorPath.getLeaf()); // NOI18N
            }

            return Collections.<Fix>emptyList();
        }
        String simpleName = name.toString();
        final TypeElement source = firstClass != null ? (TypeElement) info.getTrees().getElement(firstClass) : null;
        Element target = null;
        boolean wasMemberSelect = false;

        if (errorPath.getLeaf().getKind() == Kind.MEMBER_SELECT) {
            TreePath exp = new TreePath(errorPath, ((MemberSelectTree) errorPath.getLeaf()).getExpression());
            TypeMirror targetType = info.getTrees().getTypeMirror(exp);

            if (targetType != null) {
                if (targetType.getKind() == TypeKind.DECLARED) {
                    Element expElement = info.getTrees().getElement(exp);

                    if (isClassLikeElement(expElement)) {
                        modifiers.add(Modifier.STATIC);
                    }

                    Element targetElement = info.getTypes().asElement(targetType);

                    if (isClassLikeElement(targetElement)) {
                        target = (TypeElement) targetElement;
                    }
                } else if (targetType.getKind() == TypeKind.PACKAGE) {
                    target = info.getTrees().getElement(exp);
                }
            }

            wasMemberSelect = true;
        } else {
	    Element enclosingElement = e.getEnclosingElement();
	    if(enclosingElement != null && enclosingElement.getKind() == ElementKind.ANNOTATION_TYPE) //unresolved element inside annot.
			target = enclosingElement;
	    else

		if (errorPath.getLeaf().getKind() == Kind.IDENTIFIER) {
		    //TODO: Handle Annotations
                target = source;

                if (firstMethod != null) {
                    if (((MethodTree)firstMethod.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC)) {
                        modifiers.add(Modifier.STATIC);
                    }
                } else if (firstInitializer != null) {
                    if (((BlockTree) firstInitializer.getLeaf()).isStatic()) {
                        modifiers.add(Modifier.STATIC);
                    }
                } else if (firstVar != null && ((VariableTree)firstVar.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC)) {
                    modifiers.add(Modifier.STATIC);
                }
            }
        }

        if (target == null) {
            if (ErrorHintsProvider.ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "target=null"); // NOI18N
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "offset=" + offset); // NOI18N
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "errorTree=" + errorPath.getLeaf()); // NOI18N
            }

            return Collections.<Fix>emptyList();
        }

        if (target instanceof TypeElement)
            modifiers.addAll(Utilities.getAccessModifiers(info, source, (TypeElement) target).getRequiredModifiers());
        else
            modifiers.add(Modifier.PUBLIC);

        List<Fix> result = new ArrayList<Fix>();

        if (methodInvocation != null) {
            //create method:
            MethodInvocationTree mit = (MethodInvocationTree) methodInvocation.getLeaf();
            //return type:
            Set<ElementKind> fixTypes = EnumSet.noneOf(ElementKind.class);
            List<? extends TypeMirror> types = resolveType(fixTypes, info, methodInvocation.getParentPath(), methodInvocation.getLeaf(), offset, null, null);

            if (types == null || types.isEmpty()) {
                return Collections.<Fix>emptyList();
            }
            try {
                result.addAll(prepareCreateMethodFix(info, methodInvocation, modifiers, (TypeElement) target, simpleName, mit.getArguments(), types));
            } catch (IllegalArgumentException ex) {
                // FIXME: see issue #243028; EXECUTABLE somehow gets here and causes an exception. Let's log all the necessary info incl. source
                LOG.log(Level.INFO, "Unexpected exception, perhaps a type that cannot be converted to a Handle. See issue #243028 for more details. Please attach" +
                        "the following ide.log to the issue");
                LOG.log(Level.INFO, "Caused by source:\n==============\n" + 
                                    info.getSnapshot().getText().toString() + "\n==============\n");
                LOG.log(Level.INFO, "Caused by error at offset " + offset + ", tree: " + methodInvocation.getLeaf().toString(), ex);
                LOG.log(Level.INFO, "Invocation starts at " + info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), methodInvocation.getLeaf()));
                throw ex;
            }
        }

        Set<ElementKind> fixTypes = EnumSet.noneOf(ElementKind.class);
        TypeMirror[] superType = new TypeMirror[1];
        int[] numTypeParameters = new int[1];
        List<TypeMirror> types = (List<TypeMirror>)resolveType(fixTypes, info, parent, errorPath.getLeaf(), offset, superType, numTypeParameters);
        ElementKind classType = getClassType(fixTypes);
        
        if (!ErrorFixesFakeHint.enabled(info.getFileObject(), FixKind.CREATE_LOCAL_VARIABLE)) {
            fixTypes.remove(ElementKind.LOCAL_VARIABLE);
        }
        final TypeMirror type;
        
        if (types != null && !types.isEmpty()) {
            List<TypeMirror> resolvedTypes = null;
            int i = 0;
            for (Iterator<TypeMirror> it = types.iterator(); it.hasNext(); ) {
                final TypeMirror t = it.next();
                final TypeMirror resolved = Utilities.resolveTypeForDeclaration(info, t);
                if (resolved != t) {
                    if (resolvedTypes == null) {
                        resolvedTypes = new ArrayList(types);
                        types = resolvedTypes;
                    }
                    resolvedTypes.set(i, resolved);
                }
                i++;
            }
            //XXX: should reasonably consider all the found type candidates, not only the one:
            type = types.get(0);
            
            if (superType[0] == null) {
                // the type must be already un-captured.
                superType[0] = type;
            }
        } else {
            type = null;
        }

        if (target.getKind() == ElementKind.PACKAGE) {
            result.addAll(prepareCreateOuterClassFix(info, null, target, modifiers, simpleName, null, superType[0], classType != null ? classType : ElementKind.CLASS, numTypeParameters[0]));
            return result;
        }
        
        TypeElement outermostTypeElement = source != null ? info.getElementUtilities().outermostTypeElement(source) : null;

        if (newClass != null) {
            NewClassTree nct = (NewClassTree) newClass.getLeaf();
            Element clazz = info.getTrees().getElement(new TreePath(newClass, nct.getIdentifier()));

            if (clazz == null || clazz.asType().getKind() == TypeKind.ERROR || (!clazz.getKind().isClass() && !clazz.getKind().isInterface())) {
                //the class does not exist...
                ExpressionTree ident = nct.getIdentifier();
                int numTypeArguments = 0;

                if (ident.getKind() == Kind.PARAMETERIZED_TYPE) {
                    numTypeArguments = ((ParameterizedTypeTree) ident).getTypeArguments().size();
                }

                if (wasMemberSelect) {
                    return prepareCreateInnerClassFix(info, newClass, (TypeElement) target, modifiers, simpleName, nct.getArguments(), type, ElementKind.CLASS, numTypeArguments);
                } else {
		    List<Fix> currentResult = new LinkedList<Fix>();

		    currentResult.addAll(prepareCreateOuterClassFix(info, newClass, source, EnumSet.of(Modifier.PUBLIC), simpleName, nct.getArguments(), type, ElementKind.CLASS, numTypeArguments));
                    if (!baseType || outermostTypeElement != source)
		        currentResult.addAll(prepareCreateInnerClassFix(info, newClass, outermostTypeElement, EnumSet.of(outermostTypeElement != null && outermostTypeElement.getKind().isInterface() ? Modifier.PUBLIC : Modifier.PRIVATE, Modifier.STATIC), simpleName, nct.getArguments(), type, ElementKind.CLASS, numTypeArguments));
		    
                    return currentResult;
                }
            }

            if (nct.getClassBody() != null) {
                return Collections.<Fix>emptyList();
            }

            TypeElement clazzTarget = (TypeElement) clazz;

            result.addAll(prepareCreateMethodFix(info, newClass, Utilities.getAccessModifiers(info, source, clazzTarget).getRequiredModifiers(), clazzTarget, "<init>", nct.getArguments(), null)); //NOI18N
        }

        //field like or class (type):
        if (classType != null && e.asType().getKind() == TypeKind.ERROR) {
            if (wasMemberSelect) {
                 result.addAll(prepareCreateInnerClassFix(info, null, (TypeElement) target, modifiers, simpleName, null, superType[0], classType, numTypeParameters[0]));
            } else {
                result.addAll(prepareCreateOuterClassFix(info, null, source, EnumSet.noneOf(Modifier.class), simpleName, null, superType[0], classType, numTypeParameters[0]));
                if (!baseType || outermostTypeElement != source)
                    result.addAll(prepareCreateInnerClassFix(info, null, outermostTypeElement, EnumSet.of(outermostTypeElement != null && outermostTypeElement.getKind().isInterface() ? Modifier.PUBLIC : Modifier.PRIVATE, Modifier.STATIC), simpleName, null, superType[0], classType, numTypeParameters[0]));
            }
        }
        // check if this may be tested above, just after assignment to a type
        if (type == null || type.getKind() == TypeKind.VOID || type.getKind() == TypeKind.OTHER || type.getKind() == TypeKind.NONE || type.getKind() == TypeKind.EXECUTABLE) {
            return result;
        }

        //currently, we cannot handle error types:
        if (Utilities.containsErrorsRecursively(type)) {
            return result;
        }

        Collection<TypeVariable> typeVars = Utilities.containedTypevarsRecursively(type);

        if (!Utilities.allTypeVarsAccessible(typeVars, target)) {
            fixTypes.remove(ElementKind.FIELD);
        }

        if (fixTypes.contains(ElementKind.FIELD) && Utilities.isTargetWritable((TypeElement) target, info)) { //IZ 111048 -- don't offer anything if target file isn't writable
            Element enclosingElement = e.getEnclosingElement();
            if (enclosingElement != null && enclosingElement.getKind() == ElementKind.ANNOTATION_TYPE) {
//                FileObject targetFile = SourceUtils.getFile(target, info.getClasspathInfo());
                FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
                if (targetFile != null) {
                    result.add(new CreateMethodFix(info, simpleName, modifiers, (TypeElement) target, type, types, Collections.<String>emptyList(), Collections.<TypeMirror>emptyList(), Collections.<String>emptyList(), targetFile));
                }

                return result;
            } else {
                FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
                if (targetFile != null) {
                    if (target.getKind() == ElementKind.ENUM) {
                        if (source != null) { //TODO: maybe create a constant? - but the test below seems very suspicious:
                        if (source.equals(target)) {
                            result.add(new CreateFieldFix(info, simpleName, modifiers, (TypeElement) target, type, targetFile));
                        } else {
                            result.add(new CreateEnumConstant(info, simpleName, modifiers, (TypeElement) target, type, targetFile));
                        }
                        }
                    } else {
                        if (firstMethod != null && info.getTrees().getElement(firstMethod).getKind() == ElementKind.CONSTRUCTOR && ErrorFixesFakeHint.isCreateFinalFieldsForCtor(ErrorFixesFakeHint.getPreferences(targetFile, FixKind.CREATE_FINAL_FIELD_CTOR))) {
                            if (CreateElementUtilities.canDeclareVariableFinal(info, firstMethod, e)) {
                                modifiers.add(Modifier.FINAL);
                            }
                        }
                        if (ErrorFixesFakeHint.enabled(info.getFileObject(), ErrorFixesFakeHint.FixKind.CREATE_FINAL_FIELD_CTOR)) {
                            result.add(new CreateFieldFix(info, simpleName, modifiers, (TypeElement) target, type, targetFile));
                        }
                    }
                }
            }
        }

        if (!wasMemberSelect && (fixTypes.contains(ElementKind.LOCAL_VARIABLE) || fixTypes.contains(ElementKind.PARAMETER) || fixTypes.contains(ElementKind.RESOURCE_VARIABLE) || fixTypes.contains(ElementKind.OTHER))) {
            ExecutableElement ee = null;

            if (firstMethod != null) {
                ee = (ExecutableElement) info.getTrees().getElement(firstMethod);
            }

            int identifierPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), errorPath.getLeaf());
            if (ee != null && fixTypes.contains(ElementKind.PARAMETER) && !Utilities.isMethodHeaderInsideGuardedBlock(info, (MethodTree) firstMethod.getLeaf()))
                result.add(new AddParameterOrLocalFix(info, type, simpleName, ElementKind.PARAMETER, identifierPos).toEditorFix());
            if ((firstMethod != null || firstInitializer != null || firstLambda != null) && fixTypes.contains(ElementKind.LOCAL_VARIABLE) && ErrorFixesFakeHint.enabled(ErrorFixesFakeHint.FixKind.CREATE_LOCAL_VARIABLE))
                result.add(new AddParameterOrLocalFix(info, type, simpleName, ElementKind.LOCAL_VARIABLE, identifierPos).toEditorFix());
            if (fixTypes.contains(ElementKind.RESOURCE_VARIABLE))
                result.add(new AddParameterOrLocalFix(info, type, simpleName, ElementKind.RESOURCE_VARIABLE, identifierPos).toEditorFix());
            if (fixTypes.contains(ElementKind.OTHER))
                result.add(new AddParameterOrLocalFix(info, type, simpleName, ElementKind.OTHER, identifierPos).toEditorFix());
        }

        return result;
    }

    private static List<Fix> computeMissingMemberRefFixes(CompilationInfo info, int offset) {
        TreePath errorPath = info.getTreeUtilities().pathFor(offset + 1);
        while (errorPath != null && errorPath.getLeaf().getKind() != Kind.MEMBER_REFERENCE) {
            errorPath = errorPath.getParentPath();
        }
        if (errorPath == null) {
            return Collections.<Fix>emptyList();
        }
        MemberReferenceTree mref = (MemberReferenceTree) errorPath.getLeaf();
        TypeMirror mrefType = info.getTrees().getTypeMirror(errorPath);
        if (mrefType.getKind() == TypeKind.ERROR) {
            TypeMirror expectedTargetType = info.getTrees().getOriginalType((ErrorType) mrefType);
            if (expectedTargetType == null || expectedTargetType.getKind() != TypeKind.DECLARED) {
                return Collections.<Fix>emptyList();
            }
            ExecutableElement expectedMethod = Utilities.getFunctionalMethodFromElement(info, info.getTypes().asElement(expectedTargetType));
            if (expectedMethod == null) {
                return Collections.<Fix>emptyList();
            }
            ExecutableType methodType = (ExecutableType) info.getTypes().asMemberOf((DeclaredType) expectedTargetType, expectedMethod);
            if (Utilities.containsErrorsRecursively(methodType)) {
                return Collections.<Fix>emptyList();
            }
            TypeMirror targetType = /*XXX: check the target*/info.getTrees().getTypeMirror(new TreePath(errorPath, mref.getQualifierExpression()));
            TypeElement target = (TypeElement) info.getTypes().asElement(targetType);
            //TODO: use thrown types?
            //IZ 111048 -- don't offer anything if target file isn't writable
            if(!Utilities.isTargetWritable(target, info))
                return Collections.<Fix>emptyList();

            FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
            if (targetFile == null)
                return Collections.<Fix>emptyList();
            Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            TreePath tp = errorPath;
            while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                tp = tp.getParentPath();
            }
            modifiers.addAll(Utilities.getAccessModifiers(info, (TypeElement) info.getTrees().getElement(tp), (TypeElement) target).getRequiredModifiers());
            Element targetExprEl = info.getTrees().getElement(new TreePath(errorPath, mref.getQualifierExpression()));
            List<Fix> fixes = new ArrayList<>();
            if (targetExprEl.getKind().isClass() || targetExprEl.getKind().isInterface()) {
                if (methodType.getParameterTypes().size() > 0 && target.equals(info.getTypes().asElement(methodType.getParameterTypes().get(0)))) {
                    //static ref to instance type:
                    fixes.add(new CreateMethodFix(info, mref.getName().toString(), EnumSet.copyOf(modifiers), target, methodType.getReturnType(), methodType.getParameterTypes().subList(1, methodType.getParameterTypes().size()), expectedMethod.getParameters().stream().skip(1).map(var -> var.getSimpleName().toString()).collect(Collectors.toList()), Collections.emptyList(), Collections.emptyList(), targetFile));
                }
                modifiers.add(Modifier.STATIC);
            }
            fixes.add(new CreateMethodFix(info, mref.getName().toString(), modifiers, target, methodType.getReturnType(), methodType.getParameterTypes(), expectedMethod.getParameters().stream().map(var -> var.getSimpleName().toString()).collect(Collectors.toList()), Collections.emptyList(), Collections.emptyList(), targetFile));
            return fixes;
        }

        return Collections.<Fix>emptyList();
    }

    private static List<Fix> prepareCreateMethodFix(CompilationInfo info, TreePath invocation, Set<Modifier> modifiers, TypeElement target, String simpleName, List<? extends ExpressionTree> arguments, List<? extends TypeMirror> returnTypes) {
        //return type:
        //XXX: should reasonably consider all the found type candidates, not only the one:
        TypeMirror returnType = returnTypes != null ? Utilities.resolveTypeForDeclaration(info, returnTypes.get(0)) : null;

        //currently, we cannot handle error types, TYPEVARs and WILDCARDs:
        if (returnType != null && Utilities.containsErrorsRecursively(returnType)) {
            return Collections.<Fix>emptyList();
        }
        
        //create method:
        MethodArguments formalArguments = Utilities.resolveArguments(info, invocation, arguments, target, returnType);

        //currently, we cannot handle error types, TYPEVARs and WILDCARDs:
        if (formalArguments == null) {
            return Collections.<Fix>emptyList();
        }

       	//IZ 111048 -- don't offer anything if target file isn't writable
	if(!Utilities.isTargetWritable(target, info))
	    return Collections.<Fix>emptyList();

        FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
        if (targetFile == null)
            return Collections.<Fix>emptyList();

        return Collections.<Fix>singletonList(new CreateMethodFix(info, simpleName, modifiers, target, returnType, formalArguments.parameterTypes, formalArguments.parameterNames, formalArguments.typeParameterTypes, formalArguments.typeParameterNames, targetFile));
    }

    /**
     * Gets the possible sourceGroups based on the current file.
     * <ul>
     * <li>If it is a file from src/main/java it will return only
     * src/main/java.</li>
     * <li>If it is a file from src/test/java it will return src/main/java AND
     * src/test/java. (src/test/java will have a higher prio than src/main/java)</li>
     * </ul>
     *
     * @param fileObject
     * @return map of sourceGroup and its hint-priority
     */
    private static Map<SourceGroup, Integer> getPossibleSourceGroups(FileObject fileObject) {
        Boolean isInTestSources = isInTestSources(fileObject);
        if (null == isInTestSources) {
            return Collections.emptyMap();
        }

        Project p = FileOwnerQuery.getOwner(fileObject);
        if (null == p) {
            return Collections.emptyMap();
        }
        
        Sources src = ProjectUtils.getSources(p);
        SourceGroup[] sGroups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        SourceGroup sourceGroup = null;
        
        Set<FileObject> testRoots = new HashSet<>();
        SourceGroup linkedSources = null;
        
        for (SourceGroup sg : sGroups) {
            URL[] urls = UnitTestForSourceQuery.findUnitTests(sg.getRootFolder());
            for (URL u : urls) {
                FileObject r = URLMapper.findFileObject(u);
                if (r != null) {
                    if (testRoots.add(r)) {
                        if (FileUtil.isParentOf(r, fileObject)) {
                            isInTestSources = true;
                            linkedSources = sg;
                        }
                    }
                }
            }
            if (FileUtil.isParentOf(sg.getRootFolder(), fileObject)) {
                sourceGroup = sg;
            }
        }
        

        Map<SourceGroup, Integer> list = new HashMap<>();
        if (isInTestSources) {
            //in test sources (f.e. src/test/java) -> return main sources and test sources
            if (null != linkedSources) {
                list.put(linkedSources, PRIO_MAINSOURCEGROUP);
            }

            if (null != sourceGroup) {
                //test source group has a higher prio -> before main source group
                list.put(sourceGroup, PRIO_TESTSOURCEGROUP);
            }

        } else {
            //in sources (f.e. src/main/java) -> return only main sources
            if (null != sourceGroup) {
                list.put(sourceGroup, PRIO_MAINSOURCEGROUP);
            }
        }
        return list;
    }
    
    private static Boolean isInTestSources(FileObject fileObject) {
        Project p = FileOwnerQuery.getOwner(fileObject);
        if (null == p) {
            return null;
        }

        SourceGroup testSourceGroup = SourceGroupModifier.createSourceGroup(p, JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_HINT_TEST);
        boolean isInTestSources = false;
        if (null != testSourceGroup) {
            isInTestSources = FileUtil.isParentOf(testSourceGroup.getRootFolder(), fileObject);
        }
        return isInTestSources;
    }
    
    private static List<Fix> prepareCreateOuterClassFix(CompilationInfo info, TreePath invocation, Element source, Set<Modifier> modifiers, String simpleName, List<? extends ExpressionTree> realArguments, TypeMirror superType, ElementKind kind, int numTypeParameters) {
        Pair<List<? extends TypeMirror>, List<String>> formalArguments = invocation != null ? Utilities.resolveArguments(info, invocation, realArguments, null) : Pair.<List<? extends TypeMirror>, List<String>>of(null, null);
        
        if (formalArguments == null) {
            return Collections.<Fix>emptyList();
        }
        
        if (superType != null && (superType.getKind() == TypeKind.OTHER)) {
            return Collections.<Fix>emptyList();
        }
        final FileObject fileObject = info.getFileObject();
        Project p = FileOwnerQuery.getOwner(fileObject);
        List<Fix> fixes = new ArrayList<>();

        for (Map.Entry<SourceGroup, Integer> entrySet : getPossibleSourceGroups(fileObject).entrySet()) {
            SourceGroup sourceGroup = entrySet.getKey();
            Integer value = entrySet.getValue();

            final FileObject sourceGroupRoot = sourceGroup.getRootFolder();
            String sourceRootName = sourceGroup.getDisplayName();
            PackageElement packageElement = (PackageElement) (source instanceof PackageElement ? source : info.getElementUtilities().outermostTypeElement(source).getEnclosingElement());
            final CreateOuterClassFix fix = new CreateOuterClassFix(info, sourceGroupRoot, packageElement.getQualifiedName().toString(), simpleName, modifiers, formalArguments.first(), formalArguments.second(), superType, kind, numTypeParameters, sourceRootName);
            fix.setPriority(value);
            fixes.add(fix);
        }
        if (null == p || fixes.isEmpty()) {
            // fall back to CP info, for siblings outside projects (and tests)
            FileObject root = info.getClasspathInfo().getClassPath(
                    PathKind.SOURCE).findOwnerRoot(info.getFileObject());
            if (root == null) {
                return Collections.emptyList();
            }
            PackageElement packageElement = (PackageElement) (source instanceof PackageElement ? source : info.getElementUtilities().outermostTypeElement(source).getEnclosingElement());
            final CreateOuterClassFix fix = new CreateOuterClassFix(
                    info, root, packageElement.getQualifiedName().toString(), simpleName, modifiers, formalArguments.first(), formalArguments.second(), superType, kind, numTypeParameters,
                    root.getName());
            fix.setPriority(PRIO_MAINSOURCEGROUP);
            fixes.add(fix);
        }
        return fixes;
    }

    private static List<Fix> prepareCreateInnerClassFix(CompilationInfo info, TreePath invocation, TypeElement target, Set<Modifier> modifiers, String simpleName, List<? extends ExpressionTree> realArguments, TypeMirror superType, ElementKind kind, int numTypeParameters) {
        Pair<List<? extends TypeMirror>, List<String>> formalArguments = invocation != null ? Utilities.resolveArguments(info, invocation, realArguments, target) : Pair.<List<? extends TypeMirror>, List<String>>of(null, null);

        if (formalArguments == null) {
            return Collections.<Fix>emptyList();
        }

	//IZ 111048 -- don't offer anything if target file isn't writable
	if (!Utilities.isTargetWritable(target, info))
	    return Collections.<Fix>emptyList();

        FileObject targetFile = SourceUtils.getFile(target, info.getClasspathInfo());

        if (targetFile == null)
            return Collections.<Fix>emptyList();
        final CreateInnerClassFix fix = new CreateInnerClassFix(info, simpleName, modifiers, target, formalArguments.first(), formalArguments.second(), superType, kind, numTypeParameters, targetFile);
        fix.setPriority(PRIO_INNER);
        return Collections.<Fix>singletonList(fix);
    }

    private static ElementKind getClassType(Set<ElementKind> types) {
        if (types.contains(ElementKind.CLASS))
            return ElementKind.CLASS;
        if (types.contains(ElementKind.ANNOTATION_TYPE))
            return ElementKind.ANNOTATION_TYPE;
        if (types.contains(ElementKind.INTERFACE))
            return ElementKind.INTERFACE;
        if (types.contains(ElementKind.ENUM))
            return ElementKind.ENUM;

        return null;
    }

    private static boolean isClassLikeElement(Element expElement) {
        return expElement != null && (expElement.getKind().isClass() || expElement.getKind().isInterface());
    }

    public void cancel() {
        //XXX: not done yet
    }

    public String getId() {
        return CreateElement.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CreateElement.class, "LBL_Create_Field");
    }

    public String getDescription() {
        return NbBundle.getMessage(CreateElement.class, "DSC_Create_Field");
    }


}
