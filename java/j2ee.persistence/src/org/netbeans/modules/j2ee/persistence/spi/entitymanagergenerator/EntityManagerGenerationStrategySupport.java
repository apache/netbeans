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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.persistence.action.*;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * A support class for EntityManagerGenerationStrategy implementations.
 *
 * @author Erno Mononen
 */
public abstract class EntityManagerGenerationStrategySupport implements EntityManagerGenerationStrategy{
    protected static final String ENTITY_MANAGER_FQN = "javax.persistence.EntityManager"; //NOI18N
    private static final String ENTITY_MANAGER_JAKARTA_FQN = "jakarta.persistence.EntityManager"; //NOI18N
    protected static final String ENTITY_MANAGER_FACTORY_FQN = "javax.persistence.EntityManagerFactory"; //NOI18N
    private static final String ENTITY_MANAGER_FACTORY_JAKARTA_FQN = "jakarta.persistence.EntityManagerFactory"; //NOI18N
    protected static final String USER_TX_FQN = "javax.transaction.UserTransaction"; //NOI18N
    private static final String USER_TX_JAKARTA_FQN = "jakarta.transaction.UserTransaction"; //NOI18N
    protected static final String PERSISTENCE_CONTEXT_FQN = "javax.persistence.PersistenceContext"; //NOI18N
    private static final String PERSISTENCE_CONTEXT_JAKARTA_FQN = "jakarta.persistence.PersistenceContext"; //NOI18N
    protected static final String PERSISTENCE_UNIT_FQN = "javax.persistence.PersistenceUnit"; //NOI18N
    private static final String PERSISTENCE_UNIT_JAKARTA_FQN = "jakarta.persistence.PersistenceUnit"; //NOI18N
    protected static final String POST_CONSTRUCT_FQN = "javax.annotation.PostConstruct"; //NOI18N
    private static final String POST_CONSTRUCT_JAKARTA_FQN = "jakarta.annotation.PostConstruct"; //NOI18N
    protected static final String PRE_DESTROY_FQN = "javax.annotation.PreDestroy"; //NOI18N
    private static final String PRE_DESTROY_JAKARTA_FQN = "jakarta.annotation.PreDestroy"; //NOI18N
    protected static final String RESOURCE_FQN = "javax.annotation.Resource"; //NOI18N
    private static final String RESOURCE_JAKARTA_FQN = "jakarta.annotation.Resource"; //NOI18N
    
    protected static final String ENTITY_MANAGER_DEFAULT_NAME = "em"; //NOI18N
    protected static final String ENTITY_MANAGER_FACTORY_DEFAULT_NAME = "emf"; //NOI18N
    
    private TreeMaker treeMaker;
    private ClassTree classTree;
    private WorkingCopy workingCopy;
    private GenerationUtils genUtils;
    private PersistenceUnit persistenceUnit;
    private GenerationOptions generationOptions;
    
    protected enum Initialization {INJECT, EMF, INIT}
    
    protected List<VariableTree> getParameterList(){
        if (getGenerationOptions().getParameterType() == null){
            return Collections.<VariableTree>emptyList();
        }
        VariableTree parameter = getTreeMaker().Variable(
                getTreeMaker().Modifiers(
                Collections.<Modifier>emptySet(),
                Collections.<AnnotationTree>emptyList()
                ),
                getGenerationOptions().getParameterName(),
                getGenUtils().createType(getGenerationOptions().getParameterType(), getClassElement()),
                null
                );
        return Collections.<VariableTree>singletonList(parameter);
    }
    
    protected Tree getReturnTypeTree(){
        if (getGenerationOptions().getReturnType() == null || "void".equals(getGenerationOptions().getReturnType())){ //NOI18N
            return getTreeMaker().PrimitiveType(TypeKind.VOID);
        }
        return getGenUtils().createType(getGenerationOptions().getReturnType(), getClassElement());
    }
    
    /**
     * Just delegates to {@link GeneratorUtilities#importFQNs}. Note that
     * each invocation creates a new instance of <code>GeneratorUtilities</code>.
     */ 
    protected Tree importFQNs(Tree tree){
        return GeneratorUtilities.get(getWorkingCopy()).importFQNs(tree);
    }
    
    protected String computeMethodName(){
        return makeUnique(getGenerationOptions().getMethodName());
    }
    
    private String makeUnique(String methodName){
        List <? extends Tree> members=getClassTree().getMembers();
        String name=methodName;
        int add=1;
        boolean found=false;
        do
        {
            found=false;
            for(Tree membr:members) {
                if(Tree.Kind.METHOD == membr.getKind()) {
                    MethodTree mt = membr instanceof MethodTree ? (MethodTree) membr : null;
                    if(mt!=null && name.equals(mt.getName().toString())) {
                        found = true;
                        name = methodName + add++;
                    }
                }
            }
        }while(found);
        return name;
    }
    
    FieldInfo getEntityManagerFactoryFieldInfo(){
        VariableTree existing = getField(getEntityManagerFactoryFqn());
        if (existing != null){
            return new FieldInfo(existing.getName().toString(), true);
        }
        return new FieldInfo(ENTITY_MANAGER_FACTORY_DEFAULT_NAME, false);
    }
    
    FieldInfo getEntityManagerFieldInfo(){
        VariableTree existing = getField(getEntityManagerFqn());
        if (existing != null){
            return new FieldInfo(existing.getName().toString(), true);
        }
        return new FieldInfo(ENTITY_MANAGER_DEFAULT_NAME, false);
    }
    
    /**
     * Gets the variable tree representing the first field of the given type in
     * our class.
     *
     * @param fieldTypeFqn the fully qualified name of the field's type.
     * @return the variable tree or null if no matching field was found.
     */
    protected VariableTree getField(final String fieldTypeFqn){
        
        Parameters.notEmpty("fieldTypeFqn", fieldTypeFqn); //NOI18N
        
        for (Tree member : getClassTree().getMembers()){
            if (Tree.Kind.VARIABLE == member.getKind()){
                VariableTree variable = (VariableTree) member;
                TreePath path = getWorkingCopy().getTrees().getPath(getWorkingCopy().getCompilationUnit(), variable);
                TypeMirror variableType = getWorkingCopy().getTrees().getTypeMirror(path);
                if (fieldTypeFqn.equals(variableType.toString())){
                    return variable;
                }
                
            }
        }
        return null;
    }
    
    /**
     * Gets the element representing an annotation of the given type. Searches annotations
     *  declared on class, fields and methods (in that order).
     * @param annotationTypeFqn the fully qualified name of the annotation's type.
     * @return the element or null if no matching annotation was found.
     */
    protected Element getAnnotation(final String annotationTypeFqn){
        
        Parameters.notEmpty("annotationTypeFqn", annotationTypeFqn); //NOI18N
        
        TypeElement annotationType = asTypeElement(annotationTypeFqn);
        TypeElement classElement = getClassElement();
        List<Element> elements = new ArrayList<>();
        elements.add(classElement);
        elements.addAll(ElementFilter.fieldsIn(classElement.getEnclosedElements()));
        elements.addAll(ElementFilter.methodsIn(classElement.getEnclosedElements()));
        
        
        return checkElementsForAnnotationType(elements, annotationType);
    }
    
    private Element checkElementsForType(List <? extends Element> elements, TypeElement type){
        for (Element element : elements){
            if (getWorkingCopy().getTypes().isSameType(element.asType(), type.asType())){
                return type;
            }
        }
        return null;
    }
    
    private Element checkElementsForAnnotationType(List<? extends Element> elements, TypeElement annotationType){
        for (Element element : elements){
            for (AnnotationMirror mirror : getWorkingCopy().getElements().getAllAnnotationMirrors(element)){
                if (getWorkingCopy().getTypes().isSameType(annotationType.asType(), ((TypeElement) mirror.getAnnotationType().asElement()).asType())){
                    return annotationType;
                }
            }
        }
        return null;
    }
    
    TypeElement getClassElement(){
        TreePath path = getWorkingCopy().getTrees().getPath(getWorkingCopy().getCompilationUnit(), getClassTree());
        return (TypeElement) getWorkingCopy().getTrees().getElement(path);
    }
    
    private TypeElement asTypeElement(String fqn){
        TypeElement result = getWorkingCopy().getElements().getTypeElement(fqn);
        assert result != null : "Could not get TypeElement for " + fqn; //NOI18N
        return result;
    }
    
    protected String generateCallLines() {
        return generateCallLines(ENTITY_MANAGER_DEFAULT_NAME);
    }
    
    protected String getEmInitCode(FieldInfo em, FieldInfo emf){
        String text = "javax.persistence.EntityManager {0} = {1}.createEntityManager();\n"; //NOI18N
        return MessageFormat.format(text, em.getName(), emf.getName());
    }
    
    /**
     * Generates the code for invoking the operation specified in our
     * <code>generationOptions</code> on the entity manager with the given name.
     *
     * @param emName the name of the entity manager
     */
    protected String generateCallLines(String emName) {
        return MessageFormat.format(getGenerationOptions().getOperation().getBody(getPersistenceVersion()), new Object[] {
            emName,
            getGenerationOptions().getParameterName(),
            getGenerationOptions().getParameterType(),
            getGenerationOptions().getReturnType(),
            getGenerationOptions().getQueryAttribute()});
    }

    protected VariableTree createUserTransaction(){
        VariableTree result = getTreeMaker().Variable(
                getTreeMaker().Modifiers(
                        Collections.<Modifier>singleton(Modifier.PRIVATE),
                        Collections.<AnnotationTree>singletonList(getGenUtils().createAnnotation(getResourceFqn()))
                ),
                "utx", //NOI18N
                getTreeMaker().Identifier(getUserTxFqn()),
                null);
        result = (VariableTree) importFQNs(result);
        return result;
    }
    
    protected VariableTree createEntityManagerFactory(String name){
        return getTreeMaker().Variable(getTreeMaker().Modifiers(
                Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList()),
                name,
                getTypeTree(getEntityManagerFactoryFqn()),
                getTreeMaker().MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                getTreeMaker().MemberSelect(
                getTypeTree("javax.persistence.Persistence"), "createEntityManagerFactory"), // NOI18N
                Collections.<ExpressionTree>singletonList(getTreeMaker().Literal(getPersistenceUnitName()))
                )
                );
    }
    
    protected String getPersistenceUnitName(){
        return getPersistenceUnit() != null ? getPersistenceUnit().getName() : "";
    }
    
    protected ExpressionTree getTypeTree(String fqn){
        return getTreeMaker().QualIdent(getWorkingCopy().getElements().getTypeElement(fqn));
    }
    
    protected ClassTree createEntityManager(Initialization init){
        
        ClassTree result = getClassTree();
        
        List<AnnotationTree> anns = new ArrayList<>();
        ExpressionTree expressionTree = null;
        String emfName = ENTITY_MANAGER_FACTORY_DEFAULT_NAME;
        
        boolean needsEmf = false;
        VariableTree existingEmf = null;
        switch(init){
            
            case INJECT :
                anns.add(getGenUtils().createAnnotation(getPersistenceContextFqn(), Collections.singletonList(getGenUtils().createAnnotationArgument("unitName", getPersistenceUnitName()))));//NOI18N
                break;
                
            case EMF:
                existingEmf = getField(getEntityManagerFactoryFqn());
                assert existingEmf != null : "EntityManagerFactory does not exist in the class";
                expressionTree = getTreeMaker().Literal(existingEmf.getName().toString() + ".createEntityManager();"); //NOI18N
                break;
                
            case INIT:
                
                existingEmf = getField(getEntityManagerFactoryFqn());
                if (existingEmf != null){
                    emfName = existingEmf.getName().toString();
                } else {
                    needsEmf = true;
                }
                
                AnnotationTree postConstruct = getGenUtils().createAnnotation(getPostConstructFqn());
                MethodTree initMethod = getTreeMaker().Method(
                        getTreeMaker().Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC), Collections.<AnnotationTree>singletonList(postConstruct)),
                        makeUnique("init"),
                        getTreeMaker().PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{ " + ENTITY_MANAGER_DEFAULT_NAME + " = " + emfName + ".createEntityManager(); }", //NOI18N
                        null
                        );
                
                result = getTreeMaker().addClassMember(getClassTree(), initMethod);
                
                AnnotationTree preDestroy = getGenUtils().createAnnotation(getPreDestroyFqn());
                MethodTree destroyMethod = getTreeMaker().Method(
                        getTreeMaker().Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC), Collections.<AnnotationTree>singletonList(preDestroy)),
                        makeUnique("destroy"),
                        getTreeMaker().PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{ " + ENTITY_MANAGER_DEFAULT_NAME + " .close(); }",
                        null
                        );
                
                result = getTreeMaker().addClassMember(result, destroyMethod);
                
                if(needsEmf){
                    ExpressionTree annArgument = getGenUtils().createAnnotationArgument("name", getPersistenceUnitName());//NOI18N
                    AnnotationTree puAnn = getGenUtils().createAnnotation(getPersistenceUnitFqn(), Collections.<ExpressionTree>singletonList(annArgument));
                    VariableTree emf = getTreeMaker().Variable(
                            getTreeMaker().Modifiers(
                            Collections.<Modifier>singleton(Modifier.PRIVATE),
                            Collections.<AnnotationTree>singletonList(puAnn)
                            ),
                            emfName,
                            getTypeTree(getEntityManagerFactoryFqn()),
                            null);
                    result = getTreeMaker().insertClassMember(result, getIndexForField(result), emf);
                }
                
                break;
        }
        
        VariableTree entityManager = getTreeMaker().Variable(
                getTreeMaker().Modifiers(
                Collections.<Modifier>singleton(Modifier.PRIVATE),
                anns
                ),
                ENTITY_MANAGER_DEFAULT_NAME,
                getTypeTree(getEntityManagerFqn()),
                expressionTree);
        
        return getTreeMaker().insertClassMember(result, getIndexForField(result), entityManager);
    }
    
    
    protected int getIndexForField(ClassTree clazz){
        int result = 0;
        for (Tree each : clazz.getMembers()){
            if (Tree.Kind.VARIABLE == each.getKind()){
                result++;
            }
        }
        return result;
    }
    
    protected TreeMaker getTreeMaker() {
        return treeMaker;
    }
    
    @Override
    public void setTreeMaker(TreeMaker treeMaker) {
        this.treeMaker = treeMaker;
    }
    
    protected ClassTree getClassTree() {
        return classTree;
    }
    
    @Override
    public void setClassTree(ClassTree classTree) {
        this.classTree = classTree;
    }
    
    protected WorkingCopy getWorkingCopy() {
        return workingCopy;
    }
    
    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
    }
    
    protected GenerationUtils getGenUtils() {
        if (genUtils == null){
            genUtils = GenerationUtils.newInstance(getWorkingCopy());
        }
        return genUtils;
    }
    
    @Override
    public void setGenUtils(GenerationUtils genUtils) {
        this.genUtils = genUtils;
    }
    
    protected PersistenceUnit getPersistenceUnit() {
        return persistenceUnit;
    }
    
    @Override
    public void setPersistenceUnit(PersistenceUnit persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }
    
    protected GenerationOptions getGenerationOptions() {
        return generationOptions;
    }
    
    @Override
    public void setGenerationOptions(GenerationOptions generationOptions) {
        this.generationOptions = generationOptions;
    }

    private String getPersistenceVersion() {
        ClassPath cp = workingCopy.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
        FileObject javaxEntityManagerFo = cp == null ? null : cp.findResource("javax/persistence/EntityManager.class");
        FileObject jakartaEntityManagerFo = cp == null ? null : cp.findResource("jakarta/persistence/EntityManager.class");
        String version;
        if(jakartaEntityManagerFo != null || javaxEntityManagerFo == null) {
            version = Persistence.VERSION_3_0;
        } else {
            version = Persistence.VERSION_1_0;
        }
        if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version =  Persistence.VERSION_3_2;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version = Persistence.VERSION_3_1;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version = Persistence.VERSION_3_0;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version = Persistence.VERSION_2_2;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version = Persistence.VERSION_2_1;
        } else if (persistenceUnit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit) {// we have persistence unit with specific version, should use it
            version = Persistence.VERSION_2_0;
        }
        return version;
    }

    protected String getEntityManagerFqn() {
        String version = getPersistenceVersion();
            switch(version) {
            case Persistence.VERSION_1_0:
            case Persistence.VERSION_2_0:
            case Persistence.VERSION_2_1:
            case Persistence.VERSION_2_2:
                return ENTITY_MANAGER_FQN;
            default:
                return ENTITY_MANAGER_JAKARTA_FQN;
        }
    }

    protected String getEntityManagerFactoryFqn() {
        String version = getPersistenceVersion();
        switch(version) {
            case Persistence.VERSION_1_0:
            case Persistence.VERSION_2_0:
            case Persistence.VERSION_2_1:
            case Persistence.VERSION_2_2:
                return ENTITY_MANAGER_FACTORY_FQN;
            default:
                return ENTITY_MANAGER_FACTORY_JAKARTA_FQN;
        }
    }

    protected String getUserTxFqn() {
        String version = getPersistenceVersion();
        switch(version) {
            case Persistence.VERSION_1_0:
            case Persistence.VERSION_2_0:
            case Persistence.VERSION_2_1:
            case Persistence.VERSION_2_2:
                return USER_TX_FQN;
            default:
                return USER_TX_JAKARTA_FQN;
        }
    }

    protected String getPersistenceContextFqn() {
        String version = getPersistenceVersion();
        switch(version) {
            case Persistence.VERSION_1_0:
            case Persistence.VERSION_2_0:
            case Persistence.VERSION_2_1:
            case Persistence.VERSION_2_2:
                return PERSISTENCE_CONTEXT_FQN;
            default:
                return PERSISTENCE_CONTEXT_JAKARTA_FQN;
        }
    }

    protected String getPersistenceUnitFqn() {
        String version = getPersistenceVersion();
        switch(version) {
            case Persistence.VERSION_1_0:
            case Persistence.VERSION_2_0:
            case Persistence.VERSION_2_1:
            case Persistence.VERSION_2_2:
                return PERSISTENCE_UNIT_FQN;
            default:
                return PERSISTENCE_UNIT_JAKARTA_FQN;
        }
    }

    protected String getPostConstructFqn() {
        String version = getPersistenceVersion();
        switch(version) {
            case Persistence.VERSION_1_0:
            case Persistence.VERSION_2_0:
            case Persistence.VERSION_2_1:
            case Persistence.VERSION_2_2:
                return POST_CONSTRUCT_FQN;
            default:
                return POST_CONSTRUCT_JAKARTA_FQN;
        }
    }

    protected String getPreDestroyFqn() {
        String version = getPersistenceVersion();
        switch(version) {
            case Persistence.VERSION_1_0:
            case Persistence.VERSION_2_0:
            case Persistence.VERSION_2_1:
            case Persistence.VERSION_2_2:
                return PRE_DESTROY_FQN;
            default:
                return PRE_DESTROY_JAKARTA_FQN;
        }
    }

    protected String getResourceFqn() {
        String version = getPersistenceVersion();
        switch(version) {
            case Persistence.VERSION_1_0:
            case Persistence.VERSION_2_0:
            case Persistence.VERSION_2_1:
            case Persistence.VERSION_2_2:
                return RESOURCE_FQN;
            default:
                return RESOURCE_JAKARTA_FQN;
        }
    }

    /**
     * Encapsulates info of a field.
     */
    protected static class FieldInfo {
        /**
         * The name for the field, either the name of an existing field
         * or the default name for the field.
         */
        private String name;
        /**
         * Specifies whether the field existed or whether
         * the name that this class encapsules is the default name.
         */
        private boolean existing;
        
        FieldInfo(String name, boolean existing){
            this.name = name;
            this.existing = existing;
        }
        
        public String getName() {
            return name;
        }
        
        public boolean isExisting() {
            return existing;
        }
    }
}
