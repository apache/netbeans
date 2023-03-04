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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan lahoda
 */
public final class CreateMethodFix extends ModificationResultBasedFix implements Fix {
    
    private FileObject targetFile;
    private ElementHandle<TypeElement> target;
    private TypeMirrorHandle returnType;
    private List<TypeMirrorHandle> argumentTypes;
    private List<String> argumentNames;
    private final List<TypeMirrorHandle> typeParameterTypes;
    private final List<String> typeParameterNames;
    private ClasspathInfo cpInfo;
    private Set<Modifier> modifiers;
    
    private String name;
    private String inFQN;
    private String methodDisplayName;
    
    public CreateMethodFix(CompilationInfo info, String name, Set<Modifier> modifiers, TypeElement target, TypeMirror returnType, List<? extends TypeMirror> argumentTypes, List<String> argumentNames, List<? extends TypeMirror> typeParameterTypes, List<String> typeParameterNames, FileObject targetFile) {
        this.name = name;
        this.inFQN = Utilities.target2String(target);
        this.cpInfo = info.getClasspathInfo();
        this.modifiers = modifiers;
        this.targetFile = targetFile;
        this.target = ElementHandle.create(target);
        if (returnType != null && returnType.getKind() == TypeKind.NULL) {
            TypeElement te = info.getElements().getTypeElement("java.lang.Object");
            returnType = te == null ? null : te.asType(); // NOI18N
        }
        this.returnType = returnType != null ? TypeMirrorHandle.create(returnType) : null;
        this.argumentTypes = new ArrayList<TypeMirrorHandle>();
        
        for (TypeMirror tm : argumentTypes) {
            this.argumentTypes.add(TypeMirrorHandle.create(tm));
        }
        
        this.argumentNames = argumentNames;
        
        this.typeParameterTypes = new ArrayList<TypeMirrorHandle>();
        
        for (TypeMirror tm : typeParameterTypes) {
            this.typeParameterTypes.add(TypeMirrorHandle.create(tm));
        }
        
        this.typeParameterNames = typeParameterNames;
        
        StringBuilder methodDisplayName = new StringBuilder();
        
        if (returnType != null) {
            methodDisplayName.append(name);
        } else {
            methodDisplayName.append(target.getSimpleName().toString());
        }
        
        methodDisplayName.append('(');
        
        boolean first = true;
        
        for (TypeMirror tm : argumentTypes) {
            if (!first)
                methodDisplayName.append(','); // NOI18N
            first = false;
            methodDisplayName.append(org.netbeans.modules.editor.java.Utilities.getTypeName(info, tm, true));
        }
        
        methodDisplayName.append(')'); // NOI18N
        
        this.methodDisplayName = methodDisplayName.toString();
    }

    @Override
    public String getText() {
        if(target.getKind() == ElementKind.ANNOTATION_TYPE)
            return NbBundle.getMessage(CreateMethodFix.class, "LBL_FIX_Create_Annotation_Element", methodDisplayName, inFQN );
        if (returnType != null) {
            return NbBundle.getMessage(CreateMethodFix.class, "LBL_FIX_Create_Method", methodDisplayName, inFQN );
        } else {
            return NbBundle.getMessage(CreateMethodFix.class, "LBL_FIX_Create_Constructor", methodDisplayName, inFQN );
        }
    }

    // tag used for selection
    final String methodBodyTag = "mbody"; //NOI18N
    
    @Override
    public ChangeInfo implement() throws IOException {
        ModificationResult diff = getModificationResult();
        return Utilities.commitAndComputeChangeInfo(targetFile, diff, methodBodyTag);
    }

    @Override
    public ModificationResult getModificationResult() throws IOException {
        //use the original cp-info so it is "sure" that the proposedType can be resolved:
        JavaSource js = JavaSource.create(cpInfo, targetFile);
        if (js == null) {
            return null;
        }
        
        return js.runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy working) throws IOException {
                working.toPhase(Phase.RESOLVED);
                TypeElement targetType = target.resolve(working);
                
                if (targetType == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target."); // NOI18N
                    return;
                }
                
                TreePath targetTree = working.getTrees().getPath(targetType);
                
                if (targetTree == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target tree: " + targetType.getQualifiedName() + "."); // NOI18N
                    return;
                }
                
                TypeMirrorHandle returnTypeHandle = CreateMethodFix.this.returnType;
                TypeMirror returnType = returnTypeHandle != null ? returnTypeHandle.resolve(working) : null;
                
                if (returnTypeHandle != null && returnType == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve proposed type."); // NOI18N
                    return;
                }
                
                TreeMaker make = working.getTreeMaker();
                
                List<VariableTree>         argTypes = new ArrayList<VariableTree>();
                Iterator<TypeMirrorHandle> typeIt   = CreateMethodFix.this.argumentTypes.iterator();
                Iterator<String>           nameIt   = CreateMethodFix.this.argumentNames.iterator();
                
                while (typeIt.hasNext() && nameIt.hasNext()) {
                    TypeMirrorHandle tmh = typeIt.next();
                    TypeMirror tm = tmh.resolve(working);
                    
                    if (tm == null) {
                        ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve argument type."); // NOI18N
                        return;
                    }

                    argTypes.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), nameIt.next(), make.Type(tm), null));
                }
                
                List<TypeParameterTree> typeParameters = new ArrayList<TypeParameterTree>();
                Iterator<TypeMirrorHandle> tpTypeIt   = CreateMethodFix.this.typeParameterTypes.iterator();
                Iterator<String>           tpNameIt   = CreateMethodFix.this.typeParameterNames.iterator();
                
                while (tpTypeIt.hasNext() && tpNameIt.hasNext()) {
                    TypeMirrorHandle tmh = tpTypeIt.next();
                    TypeMirror tm = tmh.resolve(working);
                    
                    if (tm == null) {
                        ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve type argument type."); // NOI18N
                        return;
                    }

                    List<ExpressionTree> bounds = new ArrayList<ExpressionTree>();
                    List<? extends TypeMirror> boundTypes = new ArrayList<TypeMirror>(working.getTypes().directSupertypes(tm));
                    TypeElement jlObject = working.getElements().getTypeElement("java.lang.Object");
                    
                    if (boundTypes.size() == 1 && jlObject != null && boundTypes.get(0).equals(jlObject.asType())) {
                        boundTypes.remove(0);
                    }
                    
                    for (TypeMirror bound : boundTypes) {
                        bounds.add((ExpressionTree) make.Type(bound));
                    }

                    typeParameters.add(make.TypeParameter(((TypeVariable) tm).asElement().getSimpleName(), bounds));
                }
                
                BlockTree body = targetType.getKind().isClass() ? createDefaultMethodBody(working, targetTree, returnType, name) : null;
                
                if(body != null && !body.getStatements().isEmpty()) {
                    working.tag(body.getStatements().get(0), methodBodyTag);
                }
                
                MethodTree mt = make.Method(make.Modifiers(modifiers), name, returnType != null ? make.Type(returnType) : null, typeParameters, argTypes, Collections.<ExpressionTree>emptyList(), body, null);
                ClassTree decl = GeneratorUtilities.get(working).insertClassMember((ClassTree)targetTree.getLeaf(), mt);
                working.rewrite(targetTree.getLeaf(), decl);
            }
        });
    }
    
    private void addArguments(CompilationInfo info, StringBuilder value) {
        value.append("("); // NOI18N
        
        Iterator<TypeMirrorHandle> typeIt = CreateMethodFix.this.argumentTypes.iterator();
        Iterator<String>           nameIt = CreateMethodFix.this.argumentNames.iterator();
        boolean                    first  = true;
        
        while (typeIt.hasNext() && nameIt.hasNext()) {
            if (!first) {
                value.append(",");
            }
            first = false;
            
            TypeMirrorHandle tmh = typeIt.next();
            String           argName = nameIt.next();
            
            value.append(org.netbeans.modules.editor.java.Utilities.getTypeName(info, tmh.resolve(info), true));
            value.append(' '); // NOI18N
            value.append(argName);
        }
        
        value.append(")"); // NOI18N
    }
    
    public String toDebugString(CompilationInfo info) {
        StringBuilder value = new StringBuilder();
        
        if (returnType != null) {
            value.append("CreateMethodFix:"); // NOI18N
            value.append(name);
            addArguments(info, value);
            value.append(org.netbeans.modules.editor.java.Utilities.getTypeName(info, returnType.resolve(info), true));
        } else {
            value.append("CreateConstructorFix:"); // NOI18N
            addArguments(info, value);
        }
        
        value.append(':'); // NOI18N
        value.append(inFQN); // NOI18N
        
        return value.toString();
    }
    
    //XXX should be moved into the GeneratorUtils:
    private static BlockTree createDefaultMethodBody(WorkingCopy wc, TreePath targetTree, TypeMirror returnType, String name) {
        TreeUtilities tu = wc.getTreeUtilities();
        TypeElement targetClazz = (TypeElement)wc.getTrees().getElement(targetTree);
        StatementTree st = tu.parseStatement("{class ${abstract " + (returnType != null ? returnType.toString() : "void") + " " + ("<init>".equals(name) ? targetClazz.getSimpleName() : name) + "();}}", new SourcePositions[1]); //NOI18N
        Trees trees = wc.getTrees();
        List<? extends Tree> members = ((ClassTree) targetTree.getLeaf()).getMembers();
        Scope scope = members.isEmpty() ? trees.getScope(targetTree) : trees.getScope(new TreePath(targetTree, members.get(0)));
        tu.attributeTree(st, scope);
        Tree first = null;
        for(Tree t : ((ClassTree)((BlockTree)st).getStatements().get(0)).getMembers()) {
            if (t.getKind() == Tree.Kind.METHOD && !"<init>".contentEquals(((MethodTree)t).getName())) { //NOI19N
                first = t;
                break;
            }
        }
        ExecutableElement ee = (ExecutableElement) wc.getTrees().getElement(new TreePath(targetTree, first));
        return GeneratorUtilities.get(wc).createAbstractMethodImplementation(targetClazz, ee).getBody();
    }
}
