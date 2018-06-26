/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.websvc.rest.codegen.model;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.websvc.rest.support.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author PeterLiu
 */
public class EntityClassInfo {

    private static final String JAVAX_PERSISTENCE = "javax.persistence.";//NOI18N
    
    private static final String MAPPED_SUPERCLASS = JAVAX_PERSISTENCE+"MappedSuperclass";   // NOI18N
    
    private static final String ENTITY = JAVAX_PERSISTENCE+"Entity";    // NOI18N
    
    private static final Set<String> LIFECYCLE_ANNOTATIONS = new HashSet<String>(7);
    static {
        LIFECYCLE_ANNOTATIONS.add("PrePersist");    // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PostPersist");   // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PreRemove");     // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PostRemove");    // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PreUpdate");     // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PostUpdate");    // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PostLoad");      // NOI18N
        }; 
    
    private EntityResourceModelBuilder builder;
    private final String entityFqn;
    private String name;
    private String type;
    private String packageName;
    private Collection<FieldInfo> fieldInfos;
    private FieldInfo idFieldInfo;
    private ElementHandle<TypeElement> handle;

    /** Creates a new instance of ClassInfo */
    public EntityClassInfo(String entityFqn, ElementHandle<TypeElement> handle, 
            Project project, EntityResourceModelBuilder builder)
    {
        this.entityFqn = entityFqn;
        this.fieldInfos = new ArrayList<FieldInfo>();
        this.builder = builder;
        this.handle = handle; 

        extractFields(project);

        if (idFieldInfo != null && idFieldInfo.isEmbeddedId()) {
            extractPKFields(project);
        }
    }

    protected void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setType(String type) {
        this.type = type;
    }

    protected void extractFields(Project project) {
        try {
            final JavaSource source = getJavaSource(project);
            if (source != null) {
                source.runUserActionTask(new AbstractTask<CompilationController>() {

                    @Override
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(Phase.RESOLVED);
                        TypeElement classElement = handle.resolve(controller);
                        if ( classElement == null ){
                            return;
                        }
                        packageName = controller.getElements().getPackageOf(classElement).
                            getQualifiedName().toString();
                        name = classElement.getSimpleName().toString();
                        type = classElement.getQualifiedName().toString();

                        if (useFieldAccess(classElement, controller )) {
                            extractFields((DeclaredType)classElement.asType() , 
                                    classElement, controller);
                        } else {
                            extractFieldsFromMethods((DeclaredType)classElement.asType(), 
                                    classElement, controller );
                        }
                    }
                }, true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void extractFields(DeclaredType originalEntity, TypeElement typeElement, 
            CompilationController controller) 
    {
        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

        for (VariableElement field : fields) {
            Set<Modifier> modifiers = field.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT) || modifiers.contains(Modifier.VOLATILE) || modifiers.contains(Modifier.FINAL)) {
                continue;
            }

            FieldInfo fieldInfo = new FieldInfo();

            fieldInfo.parseAnnotations(field.getAnnotationMirrors());

            if (!fieldInfo.isPersistent()) {
                continue;
            }

            fieldInfos.add(fieldInfo);
            fieldInfo.setName(field.getSimpleName().toString());
            fieldInfo.setType(controller.getTypes().
                    asMemberOf(originalEntity, field), controller);

            if (fieldInfo.isId() && idFieldInfo == null ) {
                idFieldInfo = fieldInfo;
            }
        }
        TypeElement superClass = getJPASuperClass(typeElement, controller);
        if ( superClass == null ){
            return;
        }
        extractFields(originalEntity , superClass , controller );
    }

    protected void extractFieldsFromMethods(DeclaredType originalEntity, 
            TypeElement typeElement, CompilationController controller) 
    {
        List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());

        for (ExecutableElement method : methods) {
            Set<Modifier> modifiers = method.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE)) {
                continue;
            }

            FieldInfo fieldInfo = new FieldInfo();

            fieldInfo.parseAnnotations(method.getAnnotationMirrors());

            if (!fieldInfo.isPersistent() || !fieldInfo.hasPersistenceAnnotation()) {
                continue;
            }

            fieldInfos.add(fieldInfo);
            String name = method.getSimpleName().toString();
            if (name.startsWith("get")) {       //NOI18N
                name = name.substring(3);
                name = Util.lowerFirstChar(name);
            }

            fieldInfo.setName(name);
            TypeMirror methodType = controller.getTypes().asMemberOf(
                    originalEntity, method);
            fieldInfo.setType(((ExecutableType)methodType).getReturnType(), controller);

            if (fieldInfo.isId()) {
                idFieldInfo = fieldInfo;
            }
        }
        TypeElement superClass = getJPASuperClass(typeElement, controller);
        if ( superClass == null ){
            return;
        }
        extractFieldsFromMethods( originalEntity , superClass , controller );
    }

    protected void extractPKFields(Project project) {
        FileObject root = MiscUtilities.findSourceRoot(project);
        if (root != null) {
            try {
                final ClasspathInfo cpInfo = ClasspathInfo.create(root);
                JavaSource pkSource = JavaSource.create(cpInfo);
                if (pkSource == null) {
                    throw new IllegalArgumentException("No JavaSource object for " + idFieldInfo.getType());
                }
                pkSource.runUserActionTask(new AbstractTask<CompilationController>() {
                    @Override
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(Phase.RESOLVED);
                        TypeElement classElement = controller.getElements().getTypeElement(idFieldInfo.getType());
                        extractPKFields(classElement, controller);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            throw new IllegalArgumentException("No source root for " + project.getProjectDirectory().getName());
        }
    }

    protected void extractPKFields(TypeElement typeElement, 
            CompilationController controller) 
    {
        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

        for (VariableElement field : fields) {
            Set<Modifier> modifiers = field.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT) || modifiers.contains(Modifier.VOLATILE) || modifiers.contains(Modifier.FINAL)) {
                continue;
            }

            FieldInfo fieldInfo = new FieldInfo();

            idFieldInfo.addFieldInfo(fieldInfo);
            fieldInfo.setName(field.getSimpleName().toString());
            fieldInfo.setType(field.asType(), controller);
        }
    }
    
    private TypeElement getJPASuperClass(TypeElement typeElement, 
            CompilationController controller)
    {
        TypeMirror superclass = typeElement.getSuperclass();
        if ( superclass == null ){
            return null;
        }
        Element superElement = controller.getTypes().asElement( superclass );
        if ( superElement instanceof TypeElement ){
            if ( hasAnnotation( superElement, controller, MAPPED_SUPERCLASS, 
                    ENTITY))
            {
                return (TypeElement)superElement;
            }
        }
        return null;
    }
    
    private JavaSource getJavaSource(Project project) throws IOException {
        return SourceGroupSupport.getJavaSourceFromClassName(
                entityFqn, project);
    }

    private boolean useFieldAccess(TypeElement typeElement, 
            CompilationController controller) 
    {
        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

        for (VariableElement field : fields) {
            Set<Modifier> modifiers = field.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT) || modifiers.contains(Modifier.VOLATILE) || modifiers.contains(Modifier.FINAL)) {
                continue;
            }

            FieldInfo fieldInfo = new FieldInfo();

            fieldInfo.parseAnnotations(field.getAnnotationMirrors());

            if (fieldInfo.isPersistent() && fieldInfo.hasPersistenceAnnotation()) {
                return true;
            }
        }
        
        TypeElement superClass = getJPASuperClass(typeElement, controller);
        if ( superClass != null ){
                return useFieldAccess(superClass, controller);
        }

        return false;
    }

    public String getEntityFqn() {
        return entityFqn;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }

    public FieldInfo getIdFieldInfo() {
        return idFieldInfo;
    }

    public Collection<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }

    public FieldInfo getFieldInfoByName(String name) {
        for (FieldInfo f : fieldInfos) {
            if (f.getName().equals(name))
                return f;
        }

        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityClassInfo other = (EntityClassInfo) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.packageName != other.packageName && (this.packageName == null || !this.packageName.equals(other.packageName))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 47 * hash + (this.packageName != null ? this.packageName.hashCode() : 0);
        return hash;
    }

    public Set<EntityClassInfo> getEntityClosure(Set<EntityClassInfo> result) {
        if (result.contains(this)) {
            return result;
        }
        result.add(this);
        for (EntityClassInfo info : getRelatedEntities()) {
            result.addAll(info.getEntityClosure(result));
        }
        return result;
    }
    private Set<EntityClassInfo> relatedEntities;

    public Set<EntityClassInfo> getRelatedEntities() {
        if (relatedEntities != null) {
            return relatedEntities;
        }
        relatedEntities = new HashSet<EntityClassInfo>();
        Set<String> allEntityNames = builder.getAllEntityNames();
        for (FieldInfo fi : fieldInfos) {
            String type = fi.getType();
            String typeArg = fi.getTypeArg();
            if (type != null && allEntityNames.contains(type)) {
                relatedEntities.add(builder.getEntityClassInfo(type));
            } else if (typeArg != null && allEntityNames.contains(typeArg)) {
                relatedEntities.add(builder.getEntityClassInfo(typeArg));
            }
        }
        return relatedEntities;
    }
    
    private boolean hasAnnotation ( Element element, 
            CompilationController controller, String... annotations )
    {
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            controller.getElements().getAllAnnotationMirrors(element);
        for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
            Element annotationElement = annotationMirror.
                getAnnotationType().asElement();
            if ( annotationElement instanceof TypeElement ){
                Name name = ((TypeElement)annotationElement).getQualifiedName();
                for (String  annotation : annotations) {
                    if ( name.contentEquals( annotation)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static class FieldInfo {

        private enum Relationship {

            OneToOne, OneToMany, ManyToOne, ManyToMany
        };
        
        /**
         * Define a way to convert String into  
         * 'field info' type instance.
         */
        private enum StringConverter {
            CTOR,       // Constructor with String as a single argument
            VALUE_OF,   // static valueOf method
            FROM_STRING;// static fromString method
        }
        
        private String name;
        private String type;
        private String simpleTypeName;
        private String typeArg;
        private String simpleTypeArgName;
        private Relationship relationship;
        private boolean isPersistent = true;
        private boolean hasPersistenceAnnotation = false;
        private boolean isId = false;
        private boolean isEmbeddedId = false;
        private boolean isGeneratedValue = false;
        private String mappedBy = null;
        private Collection<FieldInfo> fieldInfos;
        private StringConverter stringConverter;
        private boolean hasEmptyCtor;
        private String stringConverterClassName;
        private boolean isArray;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setType(TypeMirror type, CompilationController controller) {
            if (type.getKind() == TypeKind.DECLARED) {
                DeclaredType declType = (DeclaredType) type;
                Element typeElement = declType.asElement();
                
                initTypeElement(type, controller);
                
                setType(typeElement.toString(), controller);
                for (TypeMirror arg : declType.getTypeArguments()) {
                    setTypeArg(arg.toString());
                }
            }
            else if(type.getKind() == TypeKind.ARRAY) {
                isArray = true;
                TypeMirror componentType = ((ArrayType)type).getComponentType();
                
                if ( componentType.getKind() == TypeKind.DECLARED){
                    initTypeElement(componentType, controller);
                }
                setType(type.toString(), controller);
            }
            else {
                setType(type.toString(), controller);
            }
        }
        
        public String getType() {
            return type;
        }

        public String getSimpleTypeName() {
            return simpleTypeName;
        }

        public String getTypeArg() {
            return typeArg;
        }

        public String getSimpleTypeArgName() {
            return simpleTypeArgName;
        }

        public String getEntityClassName() {
            return (simpleTypeArgName != null) ? simpleTypeArgName : simpleTypeName;
        }

        public void parseAnnotations(List<? extends AnnotationMirror> annotationMirrors) {
            for (AnnotationMirror annotation : annotationMirrors) {
                String annotationType = annotation.getAnnotationType().toString();
              
                if (!annotationType.startsWith(JAVAX_PERSISTENCE)) { 
                    continue;     
                }
                String simpleName = annotationType.substring( 
                        JAVAX_PERSISTENCE.length() );
                if ( LIFECYCLE_ANNOTATIONS.contains( simpleName)){
                    continue;
                }
                hasPersistenceAnnotation = true;

                if (annotationType.contains("EmbeddedId")) { //NOI18N
                    isEmbeddedId = true;
                    isId = true;
                } else if (annotationType.contains("Id")) { //NOI18N
                    isId = true;
                } else if (annotationType.contains("OneToOne")) { //NOI18N
                    relationship = Relationship.OneToOne;
                    parseRelationship(annotation);
                } else if (annotationType.contains("OneToMany")) { //NOI18N
                    relationship = Relationship.OneToMany;
                    parseRelationship(annotation);
                } else if (annotationType.contains("ManyToOne")) { //NOI18N
                    relationship = Relationship.ManyToOne;
                } else if (annotationType.contains("ManyToMany")) { //NOI18N
                    relationship = Relationship.ManyToMany;
                    parseRelationship(annotation);
                } else if (annotationType.contains("Transient")) { //NOI18N
                    isPersistent = false;
                } else if (annotationType.contains("GeneratedValue")) { //NOI18N
                    isGeneratedValue = true;
                }
            }
        }

        private void parseRelationship(AnnotationMirror annotation) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotation.getElementValues();
            for (Entry<? extends ExecutableElement, ? extends AnnotationValue>
                entry : map.entrySet() )
            {
                ExecutableElement e = entry.getKey();
                if (e.getSimpleName().toString().equals("mappedBy")) {      //NOI18N
                    mappedBy = entry.getValue().toString();
                    return;
                }
            }
        }
        
        public boolean isPersistent() {
            return isPersistent;
        }

        public boolean hasPersistenceAnnotation() {
            return hasPersistenceAnnotation;
        }

        public boolean isId() {
            return isId;
        }

        public boolean isGeneratedValue() {
            return isGeneratedValue;
        }

        public boolean isEmbeddedId() {
            return isEmbeddedId;

        }

        public boolean isRelationship() {
            return relationship != null;
        }

        public boolean isOneToOne() {
            return relationship == Relationship.OneToOne;

        }

        public boolean isOneToMany() {
            return relationship == Relationship.OneToMany;

        }

        public boolean isManyToOne() {
            return relationship == Relationship.ManyToOne;

        }

        public boolean isManyToMany() {
            return relationship == Relationship.ManyToMany;
        }

        public String getMappedByField() {
            return mappedBy;
        }

        public void addFieldInfo(FieldInfo info) {
            if (fieldInfos == null) {
                fieldInfos = new ArrayList<FieldInfo>();
            }

            fieldInfos.add(info);
        }

        public Collection<FieldInfo> getFieldInfos() {
            return fieldInfos;
        }
        
        public boolean hasEmptyCtor(){
            return hasEmptyCtor;
        }
        
        public boolean isArray(){
            return isArray;
        }
        
        public String getStringConverterMethod(){
            if ( stringConverterClassName == null ){
                return null;
            }
            switch(stringConverter){
                case CTOR:
                    return "new "+stringConverterClassName;         // NOI18N
                case VALUE_OF:
                    return stringConverterClassName+".valueOf";     // NOI18N
                case FROM_STRING:
                    return stringConverterClassName+".fromString";  // NOI18N
            }
            return null;
        }
        
        private StringConverter getStringConverter(){
            return stringConverter;
        }
        
        private void initTypeElement(TypeMirror type, CompilationController controller){
            DeclaredType declType = (DeclaredType) type;
            Element typeElement = declType.asElement();
            
            List<ExecutableElement> constructors = ElementFilter.
                    constructorsIn(typeElement.getEnclosedElements());
            for (ExecutableElement constructor : constructors) {
                List<? extends VariableElement> parameters = constructor.getParameters();
                if ( parameters.size()==0){
                    hasEmptyCtor = true;
                }
                else if (parameters.size()==1){
                    if ( hasSingleStringParam(constructor)){
                        stringConverter = StringConverter.CTOR;
                    }
                }
            }
            if ( stringConverter == null ){
                setStringConverter(typeElement);
            }
            if ( typeElement instanceof TypeElement ){
                stringConverterClassName = ((TypeElement)typeElement).
                        getQualifiedName().toString();
            }
        }

        private void setStringConverter( Element typeElement ) {
            List<ExecutableElement> methods = ElementFilter.methodsIn( 
                    typeElement.getEnclosedElements());
            for (ExecutableElement method : methods) {
                Set<Modifier> modifiers = method.getModifiers();
                if ( !modifiers.contains(Modifier.STATIC) || 
                        !modifiers.contains(Modifier.PUBLIC))
                {
                    continue;
                }
                List<? extends VariableElement> parameters = method.getParameters();
                if ( parameters.size()!= 1){
                    continue;
                }
                
                if ( method.getSimpleName().contentEquals("valueOf")){
                    stringConverter = StringConverter.VALUE_OF;
                }
                else if ( method.getSimpleName().contentEquals("fromString")){
                    stringConverter = StringConverter.FROM_STRING;
                }
            }
        }
        
        private boolean hasSingleStringParam(ExecutableElement method){
            List<? extends VariableElement> parameters = method.getParameters();
            if ( parameters.size() != 1){
                return false;
            }
            VariableElement variableElement = parameters.get(0);
            TypeMirror paramType = variableElement.asType();
            if ( paramType.getKind() == TypeKind.DECLARED){
                Element paramElement = ((DeclaredType)paramType).asElement();
                if ( paramElement instanceof TypeElement) {
                    String fqn = ((TypeElement)paramElement).
                            getQualifiedName().toString();
                    if ( fqn.equals(String.class.getCanonicalName())){
                        return true;
                    }
                }
            }
            return false;
        }

        private void setType(String type, CompilationController controller) {
            Class<?> primitiveType = Util.getPrimitiveType(type);

            if (primitiveType != null) {
                this.type = primitiveType.getSimpleName();
                this.simpleTypeName = primitiveType.getSimpleName();
                Class<?> clazz = primitiveType;
                if ( primitiveType.isArray()){
                    isArray = true;
                    clazz = primitiveType.getComponentType();
                }
                stringConverterClassName = clazz.getCanonicalName();
                TypeElement typeElement = controller.getElements().
                        getTypeElement( stringConverterClassName );
                if ( typeElement!= null ){
                    initTypeElement(typeElement.asType(), controller);
                }
            } else {
                this.type = type;
                this.simpleTypeName = type.substring(type.lastIndexOf(".") + 1);
            }
        }

        private void setTypeArg(String typeArg) {
            this.typeArg = typeArg;
            this.simpleTypeArgName = typeArg.substring(typeArg.lastIndexOf(".") + 1);
        }

    }
}
