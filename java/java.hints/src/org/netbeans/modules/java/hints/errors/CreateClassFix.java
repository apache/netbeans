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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan lahoda
 */
public abstract class CreateClassFix extends ModificationResultBasedFix implements EnhancedFix {
    
    protected ClasspathInfo cpInfo;
    protected Set<Modifier> modifiers;
    protected List<TypeMirrorHandle> argumentTypes; //if a specific constructor should be created
    protected List<String> argumentNames; //dtto.
    private Integer prio;
    protected List<TypeMirrorHandle> superTypes;
    protected ElementKind kind;
    protected int numTypeParameters;
    protected List<? extends TypeMirror> argumentTypeMirrors;
    
    public CreateClassFix(CompilationInfo info, Set<Modifier> modifiers, List<? extends TypeMirror> argumentTypes, List<String> argumentNames, TypeMirror superType, ElementKind kind, int numTypeParameters) {
        this.cpInfo = info.getClasspathInfo();
        this.modifiers = modifiers;
        
        if (argumentTypes != null && argumentNames != null) {
            this.argumentTypes = new ArrayList<TypeMirrorHandle>();
            this.argumentTypeMirrors = argumentTypes;
            
            for (TypeMirror tm : argumentTypes) {
                this.argumentTypes.add(TypeMirrorHandle.create(tm));
            }
            
            this.argumentNames = argumentNames;
        }
        
        if (superType != null) {
            superTypes = new LinkedList<TypeMirrorHandle>();
            
            if (superType.getKind() == TypeKind.INTERSECTION) {
                for (TypeMirror tm : info.getTypes().directSupertypes(superType)) {
                    superTypes.add(TypeMirrorHandle.create(tm));
                }
            } else {
                superTypes.add(TypeMirrorHandle.create(superType));
            }
        }
        
        this.kind = kind;
        this.numTypeParameters = numTypeParameters;
    }
    
    protected ClassTree createConstructor(WorkingCopy working, TreePath targetTreePath) {
        TreeMaker make = working.getTreeMaker();
        ClassTree targetTree = (ClassTree)targetTreePath.getLeaf();
        boolean removeDefaultConstructor = (kind == ElementKind.INTERFACE) || (kind == ElementKind.ANNOTATION_TYPE);
        
        if (argumentNames != null) {
            List<VariableTree>         argTypes = new ArrayList<VariableTree>();
            Iterator<TypeMirrorHandle> typeIt   = argumentTypes.iterator();
            Iterator<String>           nameIt   = argumentNames.iterator();
            
            while (typeIt.hasNext() && nameIt.hasNext()) {
                TypeMirrorHandle tmh = typeIt.next();
                String           argName = nameIt.next();
                
                argTypes.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), argName, make.Type(tmh.resolve(working)), null));
            }
            
            MethodTree constr = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC/*!!!*/)), "<init>", null, Collections.<TypeParameterTree>emptyList(), argTypes, Collections.<ExpressionTree>emptyList(), "{}" /*XXX*/, null); // NOI18N
            
            targetTree = GeneratorUtilities.get(working).insertClassMember(targetTree, constr);
            
            removeDefaultConstructor = true;
        }
        
        if (removeDefaultConstructor) {
            //remove the original constructor:
            for (Tree t : targetTree.getMembers()) {
                if (t.getKind() == Kind.METHOD) {
                    MethodTree mt = (MethodTree) t;
                    
                    if ("<init>".equals(mt.getName().toString()) && mt.getParameters().size() == 0) { // NOI18N
                        targetTree = make.removeClassMember(targetTree, mt);
                        break;
                    }
                }
            }
        }
            
        Tree extendsClause = null;
        List<Tree> implementsClause = Collections.<Tree>emptyList();
        
        if (superTypes != null) {
            DeclaredType extendsType = null;
            List<DeclaredType> implementsTypes = new LinkedList<DeclaredType>();
            
            for (TypeMirrorHandle h : superTypes) {
                TypeMirror tm = h.resolve(working);
                
                if (tm == null) {
                    //XXX: log
                    continue;
                }
                
                if (tm.getKind() != TypeKind.DECLARED) {
                    //XXX: log
                    continue;
                }
                
                DeclaredType dt = (DeclaredType) tm;
                
                if (dt.asElement().getKind().isClass()) {
                    if (extendsType != null) {
                        //XXX: log
                    }
                    
                    extendsType = dt;
                } else {
                    implementsTypes.add(dt);
                }
            }
            
            if (extendsType != null && !"java.lang.Object".equals(((TypeElement) extendsType.asElement()).getQualifiedName().toString())) { // NOI18N
                extendsClause = make.Type(extendsType);
            }
            
            if (!implementsTypes.isEmpty()) {
                implementsClause = new LinkedList<Tree>();
                
                for (DeclaredType dt : implementsTypes) {
                    implementsClause.add(make.Type(dt));
                }
            }
        }
        
        ModifiersTree nueModifiers = make.Modifiers(modifiers);
        List<TypeParameterTree> typeParameters = new LinkedList<TypeParameterTree>();
        
        for (int cntr = 0; cntr < numTypeParameters; cntr++) {
            typeParameters.add(make.TypeParameter(numTypeParameters == 1 ? "T" : "T" + cntr, Collections.<ExpressionTree>emptyList())); // NOI18N
        }
        
        switch (kind) {
            case CLASS:
                return make.Class(nueModifiers, targetTree.getSimpleName(), typeParameters, extendsClause, implementsClause, targetTree.getMembers());
            case INTERFACE:
                return make.Interface(nueModifiers, targetTree.getSimpleName(), typeParameters, implementsClause, targetTree.getMembers());
            case ANNOTATION_TYPE:
                return make.AnnotationType(nueModifiers, targetTree.getSimpleName(), targetTree.getMembers());
            case ENUM:
                return make.Enum(nueModifiers, targetTree.getSimpleName(), implementsClause, targetTree.getMembers());
            default:
                assert false : kind;
                return null;
        }
    }
    
    private static int valueForBundle(ElementKind kind) {
        switch (kind) {
        case CLASS:
            return 0;
        case INTERFACE:
            return 1;
        case ENUM:
            return 2;
        case ANNOTATION_TYPE:
            return 3;
        default:
            assert false : kind;
            return 0;
        }
    }
    
    public abstract String toDebugString(CompilationInfo info);
    
    /**
     * Will be used in {@link #getSortText()}
     *
     * @param prio
     */
    protected void setPriority(Integer prio) {
        this.prio = prio;
    }

    @Override
    public CharSequence getSortText() {
        //see usage at org.netbeans.modules.editor.hints.FixData.getSortText
        if (null == prio) {
            return getText();
        }

        return String.format("%04d-%s", prio, getText());
    }
    
    static final class CreateOuterClassFix extends CreateClassFix {
        private FileObject targetSourceRoot;
        private String packageName;
        private String rootName;
        private String simpleName;
        
        public CreateOuterClassFix(CompilationInfo info, FileObject targetSourceRoot, String packageName, String simpleName, Set<Modifier> modifiers, List<? extends TypeMirror> argumentTypes, List<String> argumentNames, TypeMirror superType, ElementKind kind, int numTypeParameters, String rootName) {
            super(info, modifiers, argumentTypes, argumentNames, superType, kind, numTypeParameters);
            
            this.targetSourceRoot = targetSourceRoot;
            this.packageName = packageName;
            this.simpleName = simpleName;
            this.rootName = rootName;
        }

        @Override
        public String getText() {
            if (argumentNames == null || argumentNames.isEmpty()) {
                return NbBundle.getMessage(CreateClassFix.class, "FIX_CreateClassInPackage", simpleName, packageName, valueForBundle(kind), rootName);
            } else {
                StringBuilder buf = new StringBuilder();
                for (TypeMirror tm : argumentTypeMirrors) {
                    buf.append(tm.toString());
                    buf.append(",");
                }
                String ctorParams = buf.toString();
                Object[] params = new Object[] {simpleName, packageName, valueForBundle(kind), ctorParams.substring(0, ctorParams.length() - 1), rootName};
                return NbBundle.getMessage(CreateClassFix.class, "FIX_CreateClassAndCtorInPackage", params);
            }
        }
        
        private static String template(ElementKind kind) {
            switch (kind) {
                case CLASS: return "Templates/Classes/Class.java"; // NOI18N
                case INTERFACE: return "Templates/Classes/Interface.java"; // NOI18N
                case ANNOTATION_TYPE: return "Templates/Classes/AnnotationType.java"; // NOI18N
                case ENUM: return "Templates/Classes/Enum.java"; // NOI18N
                default: throw new IllegalStateException();
            }
        }

        @Override
        public ModificationResult getModificationResult() throws IOException {
            //use the original cp-info so it is "sure" that the target can be resolved:
            JavaSource js = JavaSource.create(cpInfo);
            return js.runModificationTask(new Task<WorkingCopy>() {
                public void run(final WorkingCopy working) throws IOException {
                    working.toPhase(Phase.RESOLVED);
                    TreeMaker make = working.getTreeMaker();
                    List<Tree> members = new ArrayList<>();
                    if (argumentNames != null) {
                        List<VariableTree>         argTypes = new ArrayList<VariableTree>();
                        Iterator<TypeMirrorHandle> typeIt   = argumentTypes.iterator();
                        Iterator<String>           nameIt   = argumentNames.iterator();
                        while (typeIt.hasNext() && nameIt.hasNext()) {
                            TypeMirrorHandle tmh = typeIt.next();
                            String           argName = nameIt.next();
                            argTypes.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), argName, make.Type(tmh.resolve(working)), null));
                        }
                        members.add(make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC/*!!!*/)), "<init>", null, Collections.<TypeParameterTree>emptyList(), argTypes, Collections.<ExpressionTree>emptyList(), "{}" /*XXX*/, null)); // NOI18N
                    }
                    Tree extendsClause = null;
                    List<Tree> implementsClause = Collections.<Tree>emptyList();
                    if (superTypes != null) {
                        DeclaredType extendsType = null;
                        List<DeclaredType> implementsTypes = new LinkedList<DeclaredType>();
                        for (TypeMirrorHandle h : superTypes) {
                            TypeMirror tm = h.resolve(working);
                            if (tm == null) {
                                continue;
                            }
                            if (tm.getKind() != TypeKind.DECLARED) {
                                continue;
                            }
                            DeclaredType dt = (DeclaredType) tm;
                            if (dt.asElement().getKind().isClass()) {
                                extendsType = dt;
                            } else {
                                implementsTypes.add(dt);
                            }
                        }
                        if (extendsType != null && !"java.lang.Object".equals(((TypeElement) extendsType.asElement()).getQualifiedName().toString())) { // NOI18N
                            extendsClause = make.Type(extendsType);
                        }
                        if (!implementsTypes.isEmpty()) {
                            implementsClause = new LinkedList<Tree>();
                            for (DeclaredType dt : implementsTypes) {
                                implementsClause.add(make.Type(dt));
                            }
                        }
                    }
                    ModifiersTree nueModifiers = make.Modifiers(modifiers);
                    List<TypeParameterTree> typeParameters = new LinkedList<TypeParameterTree>();
                    for (int cntr = 0; cntr < numTypeParameters; cntr++) {
                        typeParameters.add(make.TypeParameter(numTypeParameters == 1 ? "T" : "T" + cntr, Collections.<ExpressionTree>emptyList())); // NOI18N
                    }
                    ClassTree source;
                    switch (kind) {
                        case CLASS:
                            source = make.Class(nueModifiers, simpleName, typeParameters, extendsClause, implementsClause, members);
                            break;
                        case INTERFACE:
                            source = make.Interface(nueModifiers, simpleName, typeParameters, implementsClause, members);
                            break;
                        case ANNOTATION_TYPE:
                            source = make.AnnotationType(nueModifiers, simpleName, members);
                            break;
                        case ENUM:
                            source = make.Enum(nueModifiers, simpleName, implementsClause, members);
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    CompilationUnitTree cut = make.CompilationUnit(targetSourceRoot, packageName.replace('.', '/') + '/' + simpleName + ".java", Collections.<ImportTree>emptyList(), Collections.singletonList(source));
                    working.rewrite(null, cut);
                }
            });
        }

        @Override
        public ChangeInfo implement() throws IOException {
            FileObject pack = FileUtil.createFolder(targetSourceRoot, packageName.replace('.', '/')); // NOI18N
            FileObject classTemplate/*???*/ = FileUtil.getConfigFile(template(kind));
            FileObject target;

            if (classTemplate != null) {
                DataObject classTemplateDO = DataObject.find(classTemplate);
                DataObject od = classTemplateDO.createFromTemplate(DataFolder.findFolder(pack), simpleName);

                target = od.getPrimaryFile();
            } else {
                target = FileUtil.createData(pack, simpleName + ".java");
            }
            
            final boolean fromTemplate = classTemplate != null;
            
            JavaSource.forFileObject(target).runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);

                    TreeMaker make = parameter.getTreeMaker();
                    CompilationUnitTree cut = parameter.getCompilationUnit();
                    ExpressionTree pack = fromTemplate ? cut.getPackageName() : make.Identifier(packageName);
                    ClassTree source =   fromTemplate
                                       ? (ClassTree) cut.getTypeDecls().get(0)
                                       : make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                                                    simpleName,
                                                    Collections.<TypeParameterTree>emptyList(),
                                                    null,
                                                    Collections.<Tree>emptyList(),
                                                    Collections.<Tree>emptyList());
                    ClassTree nue = createConstructor(parameter, new TreePath(new TreePath(cut), source));
                    
                    parameter.rewrite(cut, make.CompilationUnit(pack, cut.getImports(), Collections.singletonList(nue), cut.getSourceFile()));
                }
            }).commit();
            
            return new ChangeInfo(target, null, null);
        }
        
        public String toDebugString(CompilationInfo info) {
            return "CreateClass:" + packageName + "." + simpleName + ":" + modifiers.toString() + ":" + kind; // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CreateOuterClassFix other = (CreateOuterClassFix) obj;
            if (this.targetSourceRoot != other.targetSourceRoot && (this.targetSourceRoot == null || !this.targetSourceRoot.equals(other.targetSourceRoot))) {
                return false;
            }
            if (this.packageName != other.packageName && (this.packageName == null || !this.packageName.equals(other.packageName))) {
                return false;
            }
            if (this.simpleName != other.simpleName && (this.simpleName == null || !this.simpleName.equals(other.simpleName))) {
                return false;
            }

            // return true for class with empty ctor and class w/o ctor
            if((this.argumentTypeMirrors == null || this.argumentTypeMirrors.isEmpty()) && (other.argumentTypeMirrors == null || other.argumentTypeMirrors.isEmpty())) {
                return true;
            }

            if (this.argumentTypeMirrors != other.argumentTypeMirrors && (this.argumentTypeMirrors == null || !this.argumentTypeMirrors.equals(other.argumentTypeMirrors))) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (this.targetSourceRoot != null ? this.targetSourceRoot.hashCode() : 0);
            hash = 53 * hash + (this.packageName != null ? this.packageName.hashCode() : 0);
            hash = 53 * hash + (this.simpleName != null ? this.simpleName.hashCode() : 0);
            return hash;
        }


    }
    
    static final class CreateInnerClassFix extends CreateClassFix {

        private FileObject targetFile;
        private ElementHandle<TypeElement> target;
        private String name;
        private String inFQN;
        
        public CreateInnerClassFix(CompilationInfo info, String name, Set<Modifier> modifiers, TypeElement target, List<? extends TypeMirror> argumentTypes, List<String> argumentNames, TypeMirror superType, ElementKind kind, int numTypeParameters, FileObject targetFile) {
            super(info, modifiers, argumentTypes, argumentNames, superType, kind, numTypeParameters);
            this.name = name;
            this.target = ElementHandle.create(target);
            this.inFQN = Utilities.target2String(target);
            this.cpInfo = info.getClasspathInfo();
            this.targetFile = targetFile;
        }
            
        @Override
        public String getText() {
            return NbBundle.getMessage(CreateClassFix.class, "FIX_CreateInnerClass", name, inFQN, valueForBundle(kind));
        }

        @Override
        public ChangeInfo implement() throws Exception {
            ModificationResult diff = getModificationResult();
            return Utilities.commitAndComputeChangeInfo(targetFile, diff, null);
        }

        @Override
        public ModificationResult getModificationResult() throws IOException {
            //use the original cp-info so it is "sure" that the target can be resolved:
            JavaSource js = JavaSource.create(cpInfo, targetFile);
            
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
                    
                    TreeMaker make = working.getTreeMaker();
                    MethodTree constr = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "<init>", null, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}" /*XXX*/, null); // NOI18N
                    ClassTree innerClass = make.Class(make.Modifiers(modifiers), name, Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<Tree>singletonList(constr));
                    
                    innerClass = createConstructor(working, new TreePath(targetTree, innerClass));
                    
                    working.rewrite(targetTree.getLeaf(), GeneratorUtilities.get(working).insertClassMember((ClassTree)targetTree.getLeaf(), innerClass));
                }
            });
        }
        
        public String toDebugString(CompilationInfo info) {
            return "CreateInnerClass:" + inFQN + "." + name + ":" + modifiers.toString() + ":" + kind; // NOI18N
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CreateInnerClassFix other = (CreateInnerClassFix) obj;
            if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
                return false;
            }
            if (this.inFQN != other.inFQN && (this.inFQN == null || !this.inFQN.equals(other.inFQN))) {
                return false;
            }

            // return true for class with empty ctor and class w/o ctor
            if((this.argumentTypeMirrors == null || this.argumentTypeMirrors.isEmpty()) && (other.argumentTypeMirrors == null || other.argumentTypeMirrors.isEmpty())) {
                return true;
            }

            if (this.argumentTypeMirrors != other.argumentTypeMirrors && (this.argumentTypeMirrors == null || !this.argumentTypeMirrors.equals(other.argumentTypeMirrors))) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 53 * hash + (this.inFQN != null ? this.inFQN.hashCode() : 0);
            return hash;
        }
    }

}
