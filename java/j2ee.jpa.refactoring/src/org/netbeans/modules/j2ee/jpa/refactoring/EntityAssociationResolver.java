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


package org.netbeans.modules.j2ee.jpa.refactoring;


import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToOne;
import org.openide.util.Parameters;

/**
 * This class resolves non-type safe associations between entities. Entities
 * might have non-type safe associations via annotations that
 * can have attributes such as "mappedBy" that specifies a field in another
 * entity.
 *
 * @author Erno Mononen
 */
public class EntityAssociationResolver {
    
    static final String ONE_TO_ONE = "javax.persistence.OneToOne"; //NO18N
    static final String ONE_TO_MANY = "javax.persistence.OneToMany"; //NO18N
    static final String MANY_TO_ONE = "javax.persistence.ManyToOne"; //NO18N
    static final String MANY_TO_MANY = "javax.persistence.ManyToMany"; //NO18N
    
    // supported annotations
    private static final List<String> ANNOTATIONS = Arrays.asList(
            new String[]{ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY});
    
    
    static final String MAPPED_BY = "mappedBy"; //NO18N
    static final String TARGET_ENTITY = "targetEntity"; //NO18N
    
    private final MetadataModel<EntityMappingsMetadata> entityMappingsModel;
    /**
     * The property being refactored.
     */
    private final TreePathHandle refactoringSource;
    
    /**
     * Constructs a new EntityAssociationResolver.
     *
     * @param refactoringSource the property being refactored.
     * @param entityMappingsModel
     *
     */
    public EntityAssociationResolver(TreePathHandle refactoringSource, MetadataModel<EntityMappingsMetadata> entityMappingsModel) {
        Parameters.notNull("entityMappingsModel", entityMappingsModel); //NO18N
        Parameters.notNull("refactoringSource", refactoringSource); //NO18N
        this.entityMappingsModel = entityMappingsModel;
        this.refactoringSource = refactoringSource;
    }
    
    
    /**
     * Resolves the references to the property being refactored.
     *
     * @return the references or an empty list if there were none.
     */
    public List<EntityAnnotationReference> resolveReferences() throws IOException{

        final List<EntityAnnotationReference> result = new ArrayList<EntityAnnotationReference>();
            final List<Reference> references = getReferringProperties();

        entityMappingsModel.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>(){

            public Void run(EntityMappingsMetadata metadata) throws Exception {
                
                for (Reference reference : references){
                        Entity entity = getByClass(metadata.getRoot().getEntity(), reference.getClassName());
                    if (entity == null){
                            continue;
                        }
                    result.addAll(getOneToX(entity, reference));
                    }
                return null;
                }
            
            });
        return result;
        }
    
    private List<EntityAnnotationReference> getOneToX(Entity entity, Reference reference) throws IOException{
        
        List<EntityAnnotationReference> result = new ArrayList<EntityAnnotationReference>();
        boolean fieldAccess = Entity.FIELD_ACCESS.equals(entity.getAccess());
        
        TreePathHandle handle = RefactoringUtil.getTreePathHandle(reference.getPropertyName(), reference.getClassName(), refactoringSource.getFileObject());
        for (OneToMany oneToMany : entity.getAttributes().getOneToMany()){
            if (isMatching(oneToMany.getName(), fieldAccess, reference)){
                result.add(new EntityAnnotationReference(reference.getClassName(),
                        ONE_TO_MANY, MAPPED_BY, reference.getSourceProperty(), handle));
            }
        }
        
        for (OneToOne oneToOne : entity.getAttributes().getOneToOne()){
            if (isMatching(oneToOne.getName(), fieldAccess, reference)){
                result.add(new EntityAnnotationReference(reference.getClassName(),
                        ONE_TO_ONE, MAPPED_BY, reference.getSourceProperty(), handle));
            }
        }
        return result;
    }
    
    private boolean isMatching(String propertyName, boolean fieldAccess, Reference reference){
        if (fieldAccess && reference.isField()){
            return propertyName.equals(reference.getPropertyName());
        }
        if (!fieldAccess && !reference.isField()){
            return propertyName.equals(RefactoringUtil.getPropertyName(reference.getPropertyName()));
        }
        return false;
    }
    
    private Entity getByClass(Entity[] entities, String clazz){
        for (Entity entity : entities){
            if (entity.getClass2().equals(clazz)){
                return entity;
            }
        }
        return null;
    }
    
    /**
     * Gets the references to the properties that might have an annotation containing a reference
     * to the property that is being refactored.
     *
     * @return a list of <code>Reference<code>s representing the referring properties.
     */
    List<Reference> getReferringProperties() throws IOException{
        
        final List<Reference> result = new ArrayList<Reference>();
        
        JavaSource source = JavaSource.forFileObject(refactoringSource.getFileObject());
        
        //TODO: should not be an anonymous class
        source.runUserActionTask(new CancellableTask<CompilationController>(){
            
            public void cancel() {
            }
            
            private List<IdentifierTree> getTypeArgs(ParameterizedTypeTree ptt){
                List<IdentifierTree> result = new ArrayList<IdentifierTree>();
                for (Tree typeArg : ptt.getTypeArguments()){
                    if (Tree.Kind.IDENTIFIER == typeArg.getKind()){
                        IdentifierTree it = (IdentifierTree) typeArg;
                        result.add(it);
                    }
                }
                return result;
            }
            
            private List<IdentifierTree> getIdentifier(Tree tree){
                if (Tree.Kind.PARAMETERIZED_TYPE == tree.getKind()){
                    return getTypeArgs((ParameterizedTypeTree) tree);
                }
                if (Tree.Kind.IDENTIFIER == tree.getKind()){
                    return Collections.<IdentifierTree>singletonList((IdentifierTree)tree);
                }
                return Collections.<IdentifierTree>emptyList();
                
            }
            
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                Element refactoringTargetProperty = refactoringSource.resolveElement(info);
                
                if(refactoringTargetProperty == null || refactoringTargetProperty.getEnclosingElement()==null) {
                    return;
                }
                
                String sourceClass = refactoringTargetProperty.getEnclosingElement().asType().toString();
                String propertyName = refactoringTargetProperty.getSimpleName().toString();
                String targetClass = refactoringTargetProperty.asType().toString();
                
                TypeElement te = info.getElements().getTypeElement(targetClass);
                if (te == null){
                    //TODO:
                    // seem to be possible, for methods type.toString returns "()f.q.N"
                    // either incorrect usage or a bug
                    return;
                }
                for (Element element : te.getEnclosedElements()){
                    Tree propertyTree = info.getTrees().getTree(element);
                    if (propertyTree == null){
                        continue;
                    }
                    List<IdentifierTree> identifiers = new ArrayList<IdentifierTree>();
                    boolean field = false;
                    if (element.getKind() == ElementKind.FIELD) {
                        field = true;
                        if (Tree.Kind.VARIABLE == propertyTree.getKind()){
                            VariableTree vt = (VariableTree) propertyTree;
                            identifiers = getIdentifier(vt.getType());
                        }
                    } else if (element.getKind() == ElementKind.METHOD){
                        MethodTree mt = (MethodTree) propertyTree;
                        identifiers = getIdentifier(mt.getReturnType());
                    }
                    for (IdentifierTree it : identifiers){
                        TypeMirror type = info.getTreeUtilities().parseType(it.getName().toString(), te);
                        if (sourceClass.equals(type.toString())){
                            result.add(new Reference(element.getSimpleName().toString(),
                                    propertyName,
                                    targetClass,
                                    field));
                        }
                    }
                }
            }
        }, false);
        
        return result;
    }
    
    /**
     * Encapsulates information of a referring property.
     */
    static class Reference {
        
        /**
         * The FQN of the class to which the property being refactored points to.
         */
        private final String className;
        /**
         * The name of the property in the target class that possibly has a reference
         * to the property being refactored.
         */
        private final String propertyName;
        /**
         * The name of the property that is being refactored.
         */
        private final String sourceProperty;
        /**
         * Specifies whether the property to which the <code>propertyName</code> points
         * is a field or a method.
         */
        private final boolean field;
        
        public Reference(String propertyName, String sourceProperty, String clazz, boolean field){
            this.propertyName = propertyName;
            this.sourceProperty = sourceProperty;
            this.className = clazz;
            this.field = field;
        }
        
        /**
         * @see #propertyName
         */
        public String getPropertyName(){
            return propertyName;
        }
        
        /**
         * @see #className
         */
        public String getClassName(){
            return className;
        }
        
        /**
         * @see #sourceProperty
         */
        public String getSourceProperty() {
            return sourceProperty;
        }
        
        /**
         * @see #field
         */
        public boolean isField() {
            return field;
        }
        
        
    }
}


