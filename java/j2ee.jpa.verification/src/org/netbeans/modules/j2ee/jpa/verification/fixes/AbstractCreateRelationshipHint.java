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

package org.netbeans.modules.j2ee.jpa.verification.fixes;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.awt.Dialog;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.JPAHelper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToOne;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class AbstractCreateRelationshipHint implements Fix {
    private FileObject fileObject;
    private FileObject targetFileObject;
    private ElementHandle<TypeElement> classHandle;
    private AccessType accessType;
    private String targetEntityClassName;
    private String localAttrName;
    
    private String annotationClass;
    private String complimentaryAnnotationClassName;
    private String relationName;
    
    private Collection<String> fieldsExistingAtTargetClass;
    private Collection<String> compatibleFieldsExistingAtTargetClass;
    
    public AbstractCreateRelationshipHint(FileObject fileObject,
            ElementHandle<TypeElement> classHandle,
            AccessType accessType,
            String localAttrName,
            String targetEntityClassName,
            String annotationClass,
            String complimentaryAnnotationClassName) {
        
        this.classHandle = classHandle;
        this.fileObject = fileObject;
        this.accessType = accessType;
        this.targetEntityClassName = targetEntityClassName;
        this.annotationClass = annotationClass;
        this.complimentaryAnnotationClassName = complimentaryAnnotationClassName;
        this.localAttrName = localAttrName;
        
        int dotPos = annotationClass.lastIndexOf('.');
        relationName = dotPos > -1 ? annotationClass.substring(dotPos+1) : annotationClass;
    }
    
    public ChangeInfo implement(){
        examineTargetClass();
        boolean owningSide = isOwningSideByDefault();
        String mappedBy = getExistingFieldInRelation();
        
        if (mappedBy == null){
            // couldn't get the corresponding field automatically, display dialog
            
            CreateRelationshipPanel pnlPickOrCreateField = new CreateRelationshipPanel();
            
            pnlPickOrCreateField.setEntityClassNames(
                    Utilities.getShortClassName(classHandle.getQualifiedName()),
                    Utilities.getShortClassName(targetEntityClassName));
            
            pnlPickOrCreateField.setAvailableSelection(getAvailableRelationTypeSelection());
            pnlPickOrCreateField.setAvailableFields(compatibleFieldsExistingAtTargetClass);
            pnlPickOrCreateField.setDefaultFieldName(genDefaultFieldName());
            pnlPickOrCreateField.setExistingFieldNames(fieldsExistingAtTargetClass);
            
            DialogDescriptor ddesc = new DialogDescriptor(pnlPickOrCreateField,
                    NbBundle.getMessage(AbstractCreateRelationshipHint.class, "LBL_CreateRelationDlgTitle",
                    relationName, targetEntityClassName));
            
            pnlPickOrCreateField.setDlgDescriptor(ddesc);
            Dialog dlg = DialogDisplayer.getDefault().createDialog(ddesc);
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
            
            if (ddesc.getValue() == DialogDescriptor.OK_OPTION){
                String fieldName = null;
                
                if (pnlPickOrCreateField.wasCreateNewFieldSelected()){
                    // create new
                    fieldName = pnlPickOrCreateField.getNewIdName();
                }else{
                    // pick existing
                    fieldName = pnlPickOrCreateField.getSelectedField().toString();
                }
                
                owningSide = pnlPickOrCreateField.owningSide();
                mappedBy = fieldName;
            } else {
                mappedBy = null;
            }
        } else {
            owningSide = false;
        }
        
        if (mappedBy != null){
            modifyFiles(owningSide, mappedBy);
        }
        
        return null;
    }
    
    private void examineTargetClass(){
        fieldsExistingAtTargetClass = new TreeSet<String>();
        compatibleFieldsExistingAtTargetClass = new TreeSet<String>();
        
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>(){
            public void cancel() {}
            
            public void run(CompilationController ccontrol) throws Exception {
                ccontrol.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                
                TypeElement targetClass = ccontrol.getElements().getTypeElement(targetEntityClassName);
                assert targetClass != null;
                targetFileObject = org.netbeans.api.java.source.SourceUtils.getFile(targetClass,
                        ccontrol.getClasspathInfo());
                
                for (VariableElement field : ElementFilter.fieldsIn(targetClass.getEnclosedElements())){
                    fieldsExistingAtTargetClass.add(field.getSimpleName().toString());
                    
                    TypeMirror type = field.asType();
                    Element typeElement = null;
                    
                    if (isMultiValuedAtTargetEntity()){
                        if (type.getKind() == TypeKind.DECLARED){
                            List<? extends TypeMirror> typeArgs = ((DeclaredType)type).getTypeArguments();
                            
                            if (typeArgs.size() == 1){
                                typeElement = ccontrol.getTypes().asElement(typeArgs.get(0));
                            }
                        };
                    } else{
                        typeElement = ccontrol.getTypes().asElement(type);
                    }
                    
                    if (typeElement != null && typeElement.getKind() == ElementKind.CLASS &&
                            ((TypeElement)typeElement).getQualifiedName().contentEquals(classHandle.getQualifiedName())){
                        
                        compatibleFieldsExistingAtTargetClass.add(field.getSimpleName().toString());
                    }
                }
            }
        };
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        
        try{
            javaSource.runUserActionTask(task, true);
        } catch (IOException e){
            JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    private String getExistingFieldInRelation(){
        String mappedBy = null;
        try {
            MetadataModel<EntityMappingsMetadata> emModel = ModelUtils.getModel(fileObject);
            mappedBy = emModel.runReadAction(new MetadataModelAction<EntityMappingsMetadata, String>() {
                
                public String run(EntityMappingsMetadata metadata) {
                    Entity remoteEntity = ModelUtils.getEntity(metadata, targetEntityClassName);
                    assert remoteEntity != null;
                    
                    if (complimentaryAnnotationClassName.equals(JPAAnnotations.ONE_TO_ONE)){
                        return getMappedByFromOneToOne(remoteEntity);
                    }
                    
                    if (complimentaryAnnotationClassName.equals(JPAAnnotations.MANY_TO_MANY)){
                        return getMappedByFromManyToMany(remoteEntity);
                    }
                    
                    if (complimentaryAnnotationClassName.equals(JPAAnnotations.ONE_TO_MANY)){
                        return getMappedByFromOneToMany(remoteEntity);
                    }
                    
                    return null;
                }
            });
        } catch (MetadataModelException ex) {
            JPAProblemFinder.LOG.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            JPAProblemFinder.LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        return mappedBy;
    }
    
    private String getMappedByFromOneToOne(Entity remoteEntity){
        for (OneToOne one2one : remoteEntity.getAttributes().getOneToOne()){
            if (classHandle.getQualifiedName().equals(one2one.getTargetEntity())
                    && localAttrName.equals(one2one.getMappedBy())){
                return one2one.getName();
            }
        }
        
        return null;
    }
    
    private String getMappedByFromManyToMany(Entity remoteEntity){
        for (ManyToMany many2many : remoteEntity.getAttributes().getManyToMany()){
            if (classHandle.getQualifiedName().equals(many2many.getTargetEntity())
                    && localAttrName.equals(many2many.getMappedBy())){
                return many2many.getName();
            }
        }
        
        return null;
    }
    
    private String getMappedByFromOneToMany(Entity remoteEntity){
        for (OneToMany oneToMany : remoteEntity.getAttributes().getOneToMany()){
            if (classHandle.getQualifiedName().equals(oneToMany.getTargetEntity())
                    && localAttrName.equals(oneToMany .getMappedBy())){
                return oneToMany .getName();
            }
        }
        
        return null;
    }
    
    private void modifyFiles(final boolean owningSide, final String mappedBy){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                
                if (fileObject.equals(workingCopy.getFileObject()) 
                        && targetFileObject.equals(workingCopy.getFileObject())){
                    modifyLocalClass(workingCopy, mappedBy, owningSide);
                    modifyTargetClass(workingCopy, mappedBy, !owningSide);
                } else if (fileObject.equals(workingCopy.getFileObject())){
                    modifyLocalClass(workingCopy, mappedBy, owningSide);
                } else {
                    modifyTargetClass(workingCopy, mappedBy, !owningSide);
                }
            }
        };
        
        ClasspathInfo cpi = ClasspathInfo.create(fileObject);
        JavaSource javaSource = JavaSource.create(cpi, fileObject, targetFileObject);
        
        try{
            javaSource.runModificationTask(task).commit();
        } catch (IOException e){
            JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    private void modifyLocalClass(WorkingCopy workingCopy, String mappedBy, boolean owningSide) throws IOException{
        TypeElement localClass = classHandle.resolve(workingCopy);
        
        GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
        
        List<ExpressionTree> annArgs = null;
        
        if (!owningSide){
            annArgs = Collections.singletonList(
                    genUtils.createAnnotationArgument("mappedBy", mappedBy)); //NOI18N
        } else{
            annArgs = Collections.<ExpressionTree>emptyList();
        }
        
        AnnotationTree ann = genUtils.createAnnotation(annotationClass, annArgs);
        
        if (accessType == AccessType.FIELD){
            VariableElement field = ModelUtils.getField(localClass, localAttrName);
            VariableTree fieldTree = (VariableTree) workingCopy.getTrees().getTree(field);
            VariableTree modifiedTree = genUtils.addAnnotation(fieldTree, ann);
            workingCopy.rewrite(fieldTree, modifiedTree);
        } else { // accessType == AccessType.PROPERTY
            ExecutableElement accesor = ModelUtils.getAccesor(localClass, localAttrName);
            MethodTree fieldTree = (MethodTree) workingCopy.getTrees().getTree(accesor);
            MethodTree modifiedTree = genUtils.addAnnotation(fieldTree, ann);
            workingCopy.rewrite(fieldTree, modifiedTree);
        }
    }
    
    private void modifyTargetClass(WorkingCopy workingCopy, String mappedBy, boolean owningSide) throws IOException{
        TypeElement targetClass = workingCopy.getElements().getTypeElement(targetEntityClassName);
        assert targetClass != null;
        
        ClassTree targetClassTree = workingCopy.getTrees().getTree(targetClass);
        GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
        
        String remoteFieldType = classHandle.getQualifiedName();
        
        if (isMultiValuedAtTargetEntity()){
            remoteFieldType = String.format("java.util.List<%s>", remoteFieldType); //NOI18N
        }
        
        VariableTree targetField = null;
        VariableElement targetFieldElem = ModelUtils.getField(targetClass, mappedBy);
        MethodTree targetFieldAccesor = resolveAccessor(targetFieldElem, targetClass, workingCopy);
        
        ClassTree modifiedClass = targetClassTree;
        if (targetFieldElem != null){
            targetField = (VariableTree) workingCopy.getTrees().getTree(targetFieldElem);
        } else {
            ModifiersTree fieldModifiers = workingCopy.getTreeMaker().Modifiers(Collections.singleton(Modifier.PRIVATE));
            targetField = genUtils.createField(targetClass, fieldModifiers, mappedBy, remoteFieldType, null);
            modifiedClass = genUtils.addClassFields(targetClassTree, Collections.singletonList(targetField));
        }

        AccessType targetEntityAccessType = findTargetEntityAccessType(targetClass);
        if (AccessType.PROPERTY == targetEntityAccessType){

            MethodTree targetFieldMutator = resolveMutator(targetFieldElem, targetClass, workingCopy);
            ModifiersTree accessorMutatorModifiers = workingCopy.getTreeMaker().Modifiers(Collections.singleton(Modifier.PUBLIC));

            if (targetFieldAccesor == null){
                targetFieldAccesor = genUtils.createPropertyGetterMethod(targetClass, accessorMutatorModifiers, mappedBy, remoteFieldType);
                modifiedClass = workingCopy.getTreeMaker().addClassMember(modifiedClass, targetFieldAccesor);
            }            
            if (targetFieldMutator == null) {
                MethodTree mutator = genUtils.createPropertySetterMethod(targetClass, accessorMutatorModifiers, mappedBy, remoteFieldType);
                modifiedClass = workingCopy.getTreeMaker().addClassMember(modifiedClass, mutator);
            }
        }
        workingCopy.rewrite(targetClassTree, modifiedClass);
            
        
        List<ExpressionTree> targetAnnArgs = null;
        
        if (!owningSide){
            targetAnnArgs = Collections.singletonList(
                    genUtils.createAnnotationArgument("mappedBy", localAttrName)); //NOI18N
        } else{
            targetAnnArgs = Collections.<ExpressionTree>emptyList();
        }
        
        AnnotationTree targetAnn = genUtils.createAnnotation(complimentaryAnnotationClassName, targetAnnArgs);
        
        if (targetEntityAccessType == AccessType.FIELD && targetField != null){
            if(targetField.getModifiers()!=null && targetField.getModifiers().getAnnotations()!=null) {
                for (AnnotationTree at : targetField.getModifiers().getAnnotations()) {
                    if (complimentaryAnnotationClassName.endsWith(at.getAnnotationType().toString())) {
                        //more complex resolve in this case?
                        return;
                    }
                }
            }

            VariableTree modifiedTree = genUtils.addAnnotation(targetField, targetAnn);
            workingCopy.rewrite(targetField, modifiedTree);
        } else if (targetFieldAccesor!=null) { // accessType == AccessType.PROPERTY
            if(targetFieldAccesor.getModifiers()!=null && targetFieldAccesor.getModifiers().getAnnotations()!=null) {
                for (AnnotationTree at : targetFieldAccesor.getModifiers().getAnnotations()) {
                    if (complimentaryAnnotationClassName.endsWith(at.getAnnotationType().toString())) {
                        //more complex resolve in this case?
                        return;
                    }
                }
            }
            MethodTree modifiedTree = genUtils.addAnnotation(targetFieldAccesor, targetAnn);
            workingCopy.rewrite(targetFieldAccesor, modifiedTree);
        }
    }

    private MethodTree resolveAccessor(VariableElement fieldElem, TypeElement clazz, WorkingCopy workingCopy){
       return resolvePropertyMethod(fieldElem, clazz, workingCopy, true);
    }
    
    private MethodTree resolveMutator(VariableElement fieldElem, TypeElement clazz, WorkingCopy workingCopy){
        return resolvePropertyMethod(fieldElem, clazz, workingCopy, false);
    }
    
    private MethodTree resolvePropertyMethod(VariableElement fieldElem, TypeElement clazz, WorkingCopy workingCopy, boolean accessor){
        if (fieldElem == null){
            return null;
        }
       VariableTree field = (VariableTree) workingCopy.getTrees().getTree(fieldElem);

       ExecutableElement propertyMethod = accessor
               ? ModelUtils.getAccesor(clazz, field.getName().toString())
               : ModelUtils.getMutator(workingCopy, clazz, fieldElem);

       return propertyMethod == null ? null : workingCopy.getTrees().getTree(propertyMethod);
    }
    
    private AccessType findTargetEntityAccessType(final TypeElement targetEntityClass){
        AccessType accessType = AccessType.INDETERMINED;
        try {
            MetadataModel<EntityMappingsMetadata> emModel = ModelUtils.getModel(fileObject);
            accessType = emModel.runReadAction(new MetadataModelAction<EntityMappingsMetadata, AccessType>() {
                
                public AccessType run(EntityMappingsMetadata metadata) {
                    Entity remoteEntity = ModelUtils.getEntity(metadata, targetEntityClassName);
                    assert remoteEntity != null;
                    
                    return JPAHelper.findAccessType(targetEntityClass, remoteEntity);
                }
            });
        } catch (MetadataModelException ex) {
            JPAProblemFinder.LOG.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            JPAProblemFinder.LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        return accessType;
    }
    
    protected String genDefaultFieldName() {
        String defaultFieldNameBase = Utilities.getShortClassName(classHandle.getQualifiedName());
        
        char initial = Character.toLowerCase(defaultFieldNameBase.charAt(0));
        defaultFieldNameBase = initial + defaultFieldNameBase.substring(1);
        
        if (isMultiValuedAtTargetEntity()){
            defaultFieldNameBase += "s"; //NOI18N
        }
        
        String defaultFieldName = null;
        int suffix = 0;
        
        do{
            defaultFieldName = defaultFieldNameBase + (suffix == 0 ? "" : suffix); //NOI18N
            suffix ++;
        }
        while (fieldsExistingAtTargetClass.contains(defaultFieldName));
        
        return defaultFieldName;
    }
    
    public String getText(){
        return NbBundle.getMessage(AbstractCreateRelationshipHint.class,
                "LBL_CreateRelationHint", relationName);
    }
    
    protected boolean isOwningSideByDefault() {
        return getAvailableRelationTypeSelection() != CreateRelationshipPanel.AvailableSelection.INVERSE_ONLY;
    }
    
    protected CreateRelationshipPanel.AvailableSelection getAvailableRelationTypeSelection() {
        return CreateRelationshipPanel.AvailableSelection.BOTH;
    }
    
    protected boolean isMultiValuedAtTargetEntity(){
        return JPAAnnotations.MANY_TO_MANY.equals(complimentaryAnnotationClassName)
                || JPAAnnotations.ONE_TO_MANY.equals(complimentaryAnnotationClassName);
    }
    
    protected boolean isMultiValuedAtLocalEntity(){
        return JPAAnnotations.MANY_TO_MANY.equals(annotationClass)
                || JPAAnnotations.MANY_TO_ONE.equals(annotationClass);
    }
}
