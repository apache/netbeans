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

package org.netbeans.modules.j2ee.persistence.wizard.jpacontroller;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.EmbeddedPkSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.TypeInfo;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.MethodInfo;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mbohm
 */
public class JpaControllerGenerator {


    /**
     *
     * @param project
     * @param entityClass
     * @param controllerClass
     * @param exceptionPackage
     * @param pkg
     * @param controllerFileObject
     * @param embeddedPkSupport
     * @throws IOException
     */
    public static void generateJpaController(Project project, final String entityClass, final String controllerClass, String exceptionPackage, FileObject pkg, FileObject controllerFileObject, final EmbeddedPkSupport embeddedPkSupport) throws IOException {
        final boolean isInjection = Util.isContainerManaged(project);
        final String simpleEntityName = JpaControllerUtil.simpleClassName(entityClass);
        String persistenceUnit = Util.getPersistenceUnitAsString(project, entityClass);
        final String fieldName = JpaControllerUtil.fieldFromClassName(simpleEntityName);

        final List<ElementHandle<ExecutableElement>> idGetter = new ArrayList<>();
        final FileObject[] arrEntityClassFO = new FileObject[1];
        final List<ElementHandle<ExecutableElement>> toOneRelMethods = new ArrayList<>();
        final List<ElementHandle<ExecutableElement>> toManyRelMethods = new ArrayList<>();
        final boolean[] fieldAccess = new boolean[] { false };
        final boolean jakartaPersistencePackages = isJakartaPersistenceNs(pkg);

        //detect access type
        final ClasspathInfo classpathInfo = ClasspathInfo.create(pkg);
        JavaSource javaSource = JavaSource.create(classpathInfo);
        javaSource.runUserActionTask( (CompilationController controller) -> {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TypeElement jc = controller.getElements().getTypeElement(entityClass);
            ElementHandle<TypeElement> elementHandle = ElementHandle.create(jc);
            arrEntityClassFO[0] = org.netbeans.api.java.source.SourceUtils.getFile(elementHandle, controller.getClasspathInfo());
            fieldAccess[0] = JpaControllerUtil.isFieldAccess(jc);
            for (ExecutableElement method : JpaControllerUtil.getEntityMethods(jc)) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith("get")) {
                    Element f = fieldAccess[0] ? JpaControllerUtil.guessField(method) : method;
                    if (f != null) {
                        if (JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.Id")
                                || JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.EmbeddedId")
                                || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.Id")
                                || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.EmbeddedId")
                        ) {
                            idGetter.add(ElementHandle.create(method));
                        } else if (JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.OneToOne")
                                || JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.ManyToOne")
                                || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.OneToOne")
                                || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.ManyToOne")
                        ) {
                            toOneRelMethods.add(ElementHandle.create(method));
                        } else if (JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.OneToMany")
                                || JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.ManyToMany")
                                || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.OneToMany")
                                || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.ManyToMany")
                        ) {
                            toManyRelMethods.add(ElementHandle.create(method));
                        }
                    }
                }
            }
        }, true);
        
        if (idGetter.size() < 1) {
            String msg = entityClass + ": " + NbBundle.getMessage(JpaControllerGenerator.class, "ERR_GenJsfPages_CouldNotFindIdProperty"); //NOI18N
            if (fieldAccess[0]) {
                msg += " " + NbBundle.getMessage(JpaControllerGenerator.class, "ERR_GenJsfPages_EnsureSimpleIdNaming"); //NOI18N
            }
            throw new IOException(msg);
        }
        
        if (arrEntityClassFO[0] != null) {
            addImplementsClause(arrEntityClassFO[0], entityClass, "java.io.Serializable"); //NOI18N
        }

        controllerFileObject = addImplementsClause(controllerFileObject, controllerClass, "java.io.Serializable"); //NOI18N
        generateJpaController(fieldName, pkg, idGetter.get(0), persistenceUnit, controllerClass, exceptionPackage,
                entityClass, simpleEntityName, toOneRelMethods, toManyRelMethods, isInjection, fieldAccess[0],
                controllerFileObject, embeddedPkSupport, getPersistenceVersion(project, controllerFileObject),
                jakartaPersistencePackages
        );
    }

    private static boolean isJakartaPersistenceNs(final FileObject javaPackageRoot) {
        for (ClassPath.Entry entry : ClassPath.getClassPath(javaPackageRoot, ClassPath.COMPILE).entries()) {
            if(entry.includes("jakarta/persistence/Entity.class")) {
                return true;
            }
        }
        return false;
    }

    private static String getPersistenceVersion(Project project, FileObject fo) throws IOException {
        String version = Persistence.VERSION_1_0;
        PersistenceScope persistenceScopes[] = PersistenceUtils.getPersistenceScopes(project, fo);
        if (persistenceScopes.length > 0) {
            FileObject persXml = persistenceScopes[0].getPersistenceXml();
            if (persXml != null) {
                Persistence persistence = PersistenceMetadata.getDefault().getRoot(persXml);
                version=persistence.getVersion();
             }
        }
        return version;
    }

    private static FileObject addImplementsClause(FileObject fileObject, final String className, final String interfaceName) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if(javaSource == null){
            if (!fileObject.isValid()) {
                fileObject.getParent().refresh();	//Maybe fo.refresh() is enough
                fileObject = FileUtil.toFileObject(FileUtil.toFile(fileObject));
                if (fileObject != null) {
                    javaSource = JavaSource.forFileObject(fileObject);
                }
            } 
        }
        if(javaSource == null){
            Logger.getLogger(JpaControllerGenerator.class.getName()).log(Level.WARNING, "Can''t find JavaSource for {0}", fileObject.getPath());
            return fileObject;//just return, no need in npe next step
        }
        final boolean[] modified = new boolean[] { false };
        ModificationResult modificationResult = javaSource.runModificationTask( (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
            TypeMirror interfaceType = workingCopy.getElements().getTypeElement(interfaceName).asType();
            if (!workingCopy.getTypes().isSubtype(typeElement.asType(), interfaceType)) {
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = GenerationUtils.newInstance(workingCopy).addImplementsClause(classTree, interfaceName);
                modified[0] = true;
                workingCopy.rewrite(classTree, newClassTree);
            }
        });
        if (modified[0]) {
            modificationResult.commit();
        }
        return fileObject;
    }
    
    private static FileObject generateJpaController(
            final String fieldName, 
            final FileObject pkg, 
            final ElementHandle<ExecutableElement> idGetter, 
            final String persistenceUnit, 
            final String controllerClass,
            final String exceptionPackage, 
            final String entityClass, 
            final String simpleEntityName,
            final List<ElementHandle<ExecutableElement>> toOneRelMethods,
            final List<ElementHandle<ExecutableElement>> toManyRelMethods,
            final boolean isInjection,
            final boolean isFieldAccess,
            FileObject controllerFileObject, 
            final EmbeddedPkSupport embeddedPkSupport,
            final String version,
            final boolean jakartaPersistencePackages
    ) throws IOException {
        
            final String[] idPropertyType = new String[1];
            final String[] derivedIdPropertyType = new String[1];
            final String[] idGetterName = new String[1];
            final String[] derivedIdGetterName = new String[1];
            final boolean[] embeddable = new boolean[] { false };
            final boolean[] derived = new boolean[] {false};
            
            JavaSource controllerJavaSource = JavaSource.forFileObject(controllerFileObject);
            //sometimes javasource isn't refreshed properly yet
            if(controllerJavaSource == null){
                if (!controllerFileObject.isValid()) {
                    controllerFileObject.getParent().refresh();	//Maybe fo.refresh() is enough
                    controllerFileObject = FileUtil.toFileObject(FileUtil.toFile(controllerFileObject));
                    if (controllerFileObject != null) {
                        controllerJavaSource = JavaSource.forFileObject(controllerFileObject);
                    }
                } 
                
                if(controllerJavaSource == null){
                    Logger.getLogger(JpaControllerGenerator.class.getName()).log(Level.WARNING, "Can''t find JavaSource for {0}", controllerFileObject.getPath());
                    return controllerFileObject;//don't need npe later
                }
            }
            controllerJavaSource.runModificationTask( (WorkingCopy workingCopy) -> {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                
                ExecutableElement idGetterElement = idGetter.resolve(workingCopy);
                idGetterName[0] = idGetterElement.getSimpleName().toString();
                TypeMirror idType = idGetterElement.getReturnType();
                TypeElement idClass = null;
                if (null == idType.getKind()) {
                    //instead of throwing exceptions later, just use Object
                    idPropertyType[0] = "java.lang.Object";//NOI18N
                } else {
                    switch (idType.getKind()) {
                        case DECLARED:
                            DeclaredType declaredType = (DeclaredType) idType;
                            idClass = (TypeElement) declaredType.asElement();
                            embeddable[0] = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
                            idPropertyType[0] = idClass.getQualifiedName().toString();
                            if(!embeddable[0] && JpaControllerUtil.haveId(idClass)){//NOI18N
                                //handle derived id, case entity/relationship without composite keys
                                derived[0] =  JpaControllerUtil.isRelationship(idGetterElement ,JpaControllerUtil.isFieldAccess(idClass)) != JpaControllerUtil.REL_NONE;
                                if(derived[0]){
                                    ExecutableElement derivedIdGetterElement  = findPrimaryKeyGetter(workingCopy, idClass);
                                    derivedIdGetterName[0] = derivedIdGetterElement.getSimpleName().toString();
                                    TypeMirror derivedIdType = derivedIdGetterElement.getReturnType();
                                    TypeElement derivedIdClass;
                                    if (TypeKind.DECLARED == idType.getKind()) {
                                        DeclaredType derivedDeclaredType = (DeclaredType) derivedIdType;
                                        derivedIdClass = (TypeElement) derivedDeclaredType.asElement();
                                        derivedIdPropertyType[0] = derivedIdClass.getQualifiedName().toString();
                                    }
                                }
                            }
                            break;
                        case BOOLEAN:
                            idPropertyType[0] = "boolean";//NOI18N
                            break;
                        case BYTE:
                            idPropertyType[0] = "byte";//NOI18N
                            break;
                        case CHAR:
                            idPropertyType[0] = "char";//NOI18N
                            break;
                        case DOUBLE:
                            idPropertyType[0] = "double";//NOI18N
                            break;
                        case FLOAT:
                            idPropertyType[0] = "float";//NOI18N
                            break;
                        case INT:
                            idPropertyType[0] = "int";//NOI18N
                            break;
                        case LONG:
                            idPropertyType[0] = "long";//NOI18N
                            break;
                        case SHORT:
                            idPropertyType[0] = "short";//NOI18N
                            break;
                        default:
                            //instead of throwing exceptions later, just use Object
                            idPropertyType[0] = "java.lang.Object";//NOI18N
                            break;
                    }
                }
                String simpleIdPropertyType = JpaControllerUtil.simpleClassName(idPropertyType[0]);
                
                TypeElement controllerTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                ClassTree classTree = workingCopy.getTrees().getTree(controllerTypeElement);
                ClassTree modifiedClassTree = classTree;
                
                int privateModifier = java.lang.reflect.Modifier.PRIVATE;
                int publicModifier = java.lang.reflect.Modifier.PUBLIC;
                
                CompilationUnitTree modifiedImportCut = null;
                
                List<String> parameterTypes = new ArrayList<>();
                List<String> parameterNames = new ArrayList<>();
                String body = "";   //NOI18N
                boolean isUserTransaction =
                        workingCopy.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE).findResource("jakarta/transaction/UserTransaction.class") != null //NOI18N
                        || workingCopy.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE).findResource("javax/transaction/UserTransaction.class") != null;  //NOI18N
                if (isUserTransaction && isInjection) {
                    if (jakartaPersistencePackages) {
                        modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "utx", "jakarta.transaction.UserTransaction", privateModifier, null, null);   //NOI18N
                        parameterTypes.add("jakarta.transaction.UserTransaction");   //NOI18N
                    } else {
                        modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "utx", "javax.transaction.UserTransaction", privateModifier, null, null);   //NOI18N
                        parameterTypes.add("javax.transaction.UserTransaction");   //NOI18N
                    }
                    parameterNames.add("utx");   //NOI18N
                    body = "this.utx = utx;\n";   //NOI18N
                    
                }
                if (jakartaPersistencePackages) {
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "emf", "jakarta.persistence.EntityManagerFactory", privateModifier, null, null);   //NOI18N
                    parameterTypes.add("jakarta.persistence.EntityManagerFactory");   //NOI18N
                } else {
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "emf", "javax.persistence.EntityManagerFactory", privateModifier, null, null);   //NOI18N
                    parameterTypes.add("javax.persistence.EntityManagerFactory");   //NOI18N
                }
                parameterNames.add("emf");   //NOI18N
                body += "this.emf = emf;";   //NOI18N
                MethodInfo mi = new MethodInfo("<init>", publicModifier, "void", null, parameterTypes.toArray(new String[0]),   //NOI18N
                parameterNames.toArray(new String[0]), body, null, null);
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.modifyDefaultConstructor(classTree, modifiedClassTree, workingCopy, mi);
                
                MethodInfo methodInfo;
                if (jakartaPersistencePackages) {
                    methodInfo = new MethodInfo("getEntityManager", publicModifier, "jakarta.persistence.EntityManager", null, null, null, "return emf.createEntityManager();", null, null);
                } else {
                    methodInfo = new MethodInfo("getEntityManager", publicModifier, "javax.persistence.EntityManager", null, null, null, "return emf.createEntityManager();", null, null);
                }
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                
                String bodyText;
                StringBuilder updateRelatedInCreate = new StringBuilder();
                StringBuilder updateRelatedInEditPre = new StringBuilder();
                StringBuilder attachRelatedInEdit = new StringBuilder();
                StringBuilder updateRelatedInEditPost = new StringBuilder();
                StringBuilder updateRelatedInDestroy = new StringBuilder();
                StringBuilder initRelatedInCreate = new StringBuilder();
                StringBuilder illegalOrphansInCreate = new StringBuilder();
                StringBuilder illegalOrphansInEdit = new StringBuilder();
                StringBuilder illegalOrphansInDestroy = new StringBuilder();
                StringBuilder initCollectionsInCreate = new StringBuilder();  //useful in case user removes listbox from New.jsp
                
                List<ElementHandle<ExecutableElement>> allRelMethods = new ArrayList<>(toOneRelMethods);
                allRelMethods.addAll(toManyRelMethods);
                
                List<String> importFqs = new ArrayList<>();

                if(jakartaPersistencePackages) {
                    importFqs.add("jakarta.persistence.Query");
                    importFqs.add("jakarta.persistence.EntityNotFoundException");
                    importFqs.add("jakarta.persistence.criteria.CriteriaQuery");
                    importFqs.add("jakarta.persistence.criteria.Root");
                } else {
                    importFqs.add("javax.persistence.Query");
                    importFqs.add("javax.persistence.EntityNotFoundException");
                    if(version!=null && !Persistence.VERSION_1_0.equals(version)){//add criteria classes if appropriate
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "javax.persistence.criteria.CriteriaQuery");
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "javax.persistence.criteria.Root");
                    }
                }

                for (String importFq : importFqs) {
                    modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                }

                String oldMe = null;
                
                // <editor-fold desc=" all relations ">
                for(Iterator<ElementHandle<ExecutableElement>> it = allRelMethods.iterator(); it.hasNext();) {
                    ElementHandle<ExecutableElement> handle = it.next();
                    ExecutableElement m = handle.resolve(workingCopy);
                    int multiplicity = JpaControllerUtil.isRelationship(m, isFieldAccess);
                    ExecutableElement otherSide = JpaControllerUtil.getOtherSideOfRelation(workingCopy, m, isFieldAccess);
                    
                    if (otherSide != null) {
                        TypeElement relClass = (TypeElement)otherSide.getEnclosingElement();
                        boolean isRelFieldAccess = JpaControllerUtil.isFieldAccess(relClass);
                        int otherSideMultiplicity = JpaControllerUtil.isRelationship(otherSide, isRelFieldAccess);
                        TypeMirror t = m.getReturnType();
                        Types types = workingCopy.getTypes();
                        TypeMirror tstripped = JpaControllerUtil.stripCollection(t, types);
                        boolean isCollection = t != tstripped;
                        String simpleCollectionTypeName = null;
                        String collectionTypeClass = null;
                        if (isCollection) {
                            TypeElement tAsElement = (TypeElement) types.asElement(t);
                            simpleCollectionTypeName = tAsElement.getSimpleName().toString();
                            collectionTypeClass = tAsElement.getQualifiedName().toString();
                        }
                        String simpleCollectionImplementationTypeName = "ArrayList";    //NOI18N
                        String collectionImplementationTypeClass = "java.util.ArrayList";    //NOI18N
                        Class collectionTypeAsClass = null;
                        if (isCollection) {
                            try {
                                collectionTypeAsClass = Class.forName(collectionTypeClass);
                            } catch (ClassNotFoundException cfne) {
                                //let collectionTypeAsClass be null
                            }
                            if (collectionTypeAsClass != null && Set.class.isAssignableFrom(collectionTypeAsClass)) {
                                simpleCollectionImplementationTypeName = "HashSet";    //NOI18N
                                collectionImplementationTypeClass = "java.util.HashSet";    //NOI18N
                            }
                        }
                        String relType = tstripped.toString();
                        String simpleRelType = JpaControllerUtil.simpleClassName(relType); //just "Pavilion"
                        String relTypeReference = simpleRelType;
                        String mName = m.getSimpleName().toString();
                        String otherName = otherSide.getSimpleName().toString();
                        String otherType = otherSide.getReturnType().toString();
                        String relFieldName = JpaControllerUtil.getPropNameFromMethod(mName);
                        String otherFieldName = JpaControllerUtil.getPropNameFromMethod(otherName);
                        
                        boolean columnNullable = JpaControllerUtil.isFieldOptionalAndNullable(m, isFieldAccess);
                        boolean relColumnNullable = JpaControllerUtil.isFieldOptionalAndNullable(otherSide, isFieldAccess);
                        
                        String relFieldToAttach = isCollection ? relFieldName + relTypeReference + "ToAttach" : relFieldName;
                        String scalarRelFieldName = isCollection ? relFieldName + relTypeReference : relFieldName;
                        
                        if(fieldName.equals(scalarRelFieldName)){
                            scalarRelFieldName  = scalarRelFieldName + "Rel";//if entity have references to itself (i.e. tree/chain etc of entities), need to make name different from entity field name
                        }
                        
                        if (!controllerClass.startsWith(entityClass + "JpaController")) {
                            modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, relType);
                        }
                        
                        ExecutableElement relIdGetterElement = JpaControllerUtil.getIdGetter(isFieldAccess, relClass);
                        String refOrMergeString;
                        
                        if (isCollection) {
                            refOrMergeString = getRefOrMergeString(relIdGetterElement, relFieldToAttach);
                            initCollectionsInCreate.append("if (" + fieldName + "." + mName + "() == null) {\n" +
                                    fieldName + ".s" + mName.substring(1) + "(new " + simpleCollectionImplementationTypeName + "<" + relTypeReference + ">());\n" +
                                            "}\n");
                            
                            
                            modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, collectionImplementationTypeClass);
                            
                            initRelatedInCreate.append(simpleCollectionTypeName + "<" + relTypeReference + "> attached" + mName.substring(3) + " = new " + simpleCollectionImplementationTypeName + "<" + relTypeReference + ">();\n" +
                                    "for (" + relTypeReference + " " + relFieldToAttach + " : " + fieldName + "." + mName + "()) {\n" +
                                    relFieldToAttach + " = " + refOrMergeString +
                                    "attached" + mName.substring(3) + ".add(" + relFieldToAttach + ");\n" +
                                            "}\n" +
                                    fieldName + ".s" + mName.substring(1) + "(attached" + mName.substring(3) + ");\n"
                            );
                        }
                        else {
                            refOrMergeString = getRefOrMergeString(relIdGetterElement, scalarRelFieldName);
                            initRelatedInCreate.append(relTypeReference + " " + scalarRelFieldName + " = " + fieldName + "." + mName +"();\n" +
                                    "if (" + scalarRelFieldName + " != null) {\n" +
                                    scalarRelFieldName + " = " + refOrMergeString +
                                    fieldName + ".s" + mName.substring(1) + "(" + scalarRelFieldName + ");\n" +
                                            "}\n");
                        }
                        
                        String relrelInstanceName = "old" + otherName.substring(3) + "Of" + scalarRelFieldName.substring(0, 1).toUpperCase() + (scalarRelFieldName.length() > 1 ? scalarRelFieldName.substring(1) : "");
                        String relrelGetterName = otherName;
                        
                        if (!columnNullable && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && multiplicity == JpaControllerUtil.REL_TO_ONE) {
                            illegalOrphansInCreate.append(
                                    relTypeReference + " " + scalarRelFieldName + "OrphanCheck = " + fieldName + "." + mName +"();\n" +
                                            "if (" + scalarRelFieldName + "OrphanCheck != null) {\n");
                            illegalOrphansInCreate.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "OrphanCheck." + relrelGetterName + "();\n");
                            illegalOrphansInCreate.append("if (" + relrelInstanceName + " != null) {\n" +
                                    "if (illegalOrphanMessages == null) {\n" +
                                    "illegalOrphanMessages = new ArrayList<String>();\n" +
                                    "}\n" +
                                    "illegalOrphanMessages.add(\"The " + relTypeReference + " \" + " + scalarRelFieldName + "OrphanCheck + \" already has an item of type " + simpleEntityName + " whose " + scalarRelFieldName + " column cannot be null. Please make another selection for the " + scalarRelFieldName + " field.\");\n" +
                                            "}\n");
                            illegalOrphansInCreate.append("}\n");
                        }
                        
                        updateRelatedInCreate.append( (isCollection ? "for(" + relTypeReference + " " + scalarRelFieldName + " : " + fieldName + "." + mName + "()){\n" :
                                "if (" + scalarRelFieldName + " != null) {\n"));
                        //if 1:1, be sure to orphan the related entity's current related entity
                        if (otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE){
                            if (multiplicity != JpaControllerUtil.REL_TO_ONE || columnNullable) { //no need to declare relrelInstanceName if we have already examined it in the 1:1 orphan check
                                String retType = simpleEntityName;
                                if(!otherType.equals(entityClass)){
                                    retType = otherType;
                                }
                                updateRelatedInCreate.append(retType + " " + relrelInstanceName + " = " + scalarRelFieldName + "." + relrelGetterName + "();\n");
                            }
                            if (multiplicity == JpaControllerUtil.REL_TO_ONE) {
                                if (columnNullable) {
                                    updateRelatedInCreate.append("if (" + relrelInstanceName + " != null) {\n" +
                                            relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" +
                                            relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" +
                                                    "}\n");
                                }
                            }
                        }
                        
                        updateRelatedInCreate.append( ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + ".s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
                                scalarRelFieldName + "." + otherName + "().add(" + fieldName +");\n") +
                                scalarRelFieldName + " = em.merge(" + scalarRelFieldName +");\n");
                        if (multiplicity == JpaControllerUtil.REL_TO_MANY && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE){
                            updateRelatedInCreate.append("if " + relrelInstanceName + " != null) {\n" +
                                    relrelInstanceName + "." + mName + "().remove(" + scalarRelFieldName + ");\n" +
                                    relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" +
                                            "}\n");
                        }
                        updateRelatedInCreate.append("}\n");
                        
                        if (oldMe == null) {
                            oldMe = "persistent" + simpleEntityName;
                            String oldMeStatement = simpleEntityName + " " + oldMe + " = em.find(" +
                                    simpleEntityName + ".class, " + fieldName + "." + idGetterName[0] + "());\n";
                            updateRelatedInEditPre.append("\n " + oldMeStatement);
                        }
                        
                        if (isCollection) {
                            String relFieldOld = relFieldName + "Old";
                            String relFieldNew = relFieldName + "New";
                            String oldScalarRelFieldName = relFieldOld + relTypeReference;
                            String newScalarRelFieldName = relFieldNew + relTypeReference;
                            String oldOfNew = "old" + otherName.substring(3) + "Of" + newScalarRelFieldName.substring(0, 1).toUpperCase() + newScalarRelFieldName.substring(1);
                            updateRelatedInEditPre.append("\n " + simpleCollectionTypeName + "<" + relTypeReference + "> " + relFieldOld + " = " + oldMe + "." + mName + "();\n");
                            updateRelatedInEditPre.append(simpleCollectionTypeName + " <" + relTypeReference + "> " + relFieldNew + " = " + fieldName + "." + mName + "();\n");
                            if (!relColumnNullable && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) {
                                illegalOrphansInEdit.append(
                                        "for(" + relTypeReference + " " + oldScalarRelFieldName + " : " + relFieldOld + ") {\n" +
                                                "if (!" + relFieldNew + ".contains(" + oldScalarRelFieldName + ")) {\n" +
                                                        "if (illegalOrphanMessages == null) {\n" +
                                                        "illegalOrphanMessages = new ArrayList<String>();\n" +
                                                        "}\n" +
                                                        "illegalOrphanMessages.add(\"You must retain " + relTypeReference + " \" + " + oldScalarRelFieldName + " + \" since its " + otherFieldName + " field is not nullable.\");\n" +
                                                                "}\n" +
                                                                "}\n");
                            }
                            String relFieldToAttachInEdit = newScalarRelFieldName + "ToAttach";
                            String refOrMergeStringInEdit = getRefOrMergeString(relIdGetterElement, relFieldToAttachInEdit);
                            String attachedRelFieldNew = "attached" + mName.substring(3) + "New";
                            attachRelatedInEdit.append(simpleCollectionTypeName + "<" + relTypeReference + "> " + attachedRelFieldNew + " = new " + simpleCollectionImplementationTypeName + "<" + relTypeReference + ">();\n" +
                                    "for (" + relTypeReference + " " + relFieldToAttachInEdit + " : " + relFieldNew + ") {\n" +
                                    relFieldToAttachInEdit + " = " + refOrMergeStringInEdit +
                                    attachedRelFieldNew + ".add(" + relFieldToAttachInEdit + ");\n" +
                                            "}\n" +
                                    relFieldNew + " = " + attachedRelFieldNew + ";\n" +
                                    fieldName + ".s" + mName.substring(1) + "(" + relFieldNew + ");\n"
                            );
                            if (otherSideMultiplicity == JpaControllerUtil.REL_TO_MANY || relColumnNullable) {
                                updateRelatedInEditPost.append(
                                        "for (" + relTypeReference + " " + oldScalarRelFieldName + " : " + relFieldOld + ") {\n" +
                                                "if (!" + relFieldNew + ".contains(" + oldScalarRelFieldName + ")) {\n" +
                                                ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? oldScalarRelFieldName + ".s" + otherName.substring(1) + "(null);\n" :
                                                        oldScalarRelFieldName + "." + otherName + "().remove(" + fieldName + ");\n") +
                                                oldScalarRelFieldName + " = em.merge(" + oldScalarRelFieldName + ");\n" +
                                                        "}\n" +
                                                        "}\n");
                            }
                            updateRelatedInEditPost.append("for (" + relTypeReference + " " + newScalarRelFieldName + " : " + relFieldNew + ") {\n" +
                                    "if (!" + relFieldOld + ".contains(" + newScalarRelFieldName + ")) {\n" +
                                    ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? simpleEntityName + " " + oldOfNew + " = " + newScalarRelFieldName + "." + relrelGetterName + "();\n" +
                                            newScalarRelFieldName + ".s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
                                            newScalarRelFieldName + "." + otherName + "().add(" + fieldName +");\n") +
                                    newScalarRelFieldName + " = em.merge(" + newScalarRelFieldName + ");\n");
                            if (otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) {
                                updateRelatedInEditPost.append("if " + oldOfNew + " != null && !" + oldOfNew + ".equals(" + fieldName + ")) {\n" +
                                        oldOfNew + "." + mName + "().remove(" + newScalarRelFieldName + ");\n" +
                                        oldOfNew + " = em.merge(" + oldOfNew + ");\n" +
                                                "}\n");
                            }
                            updateRelatedInEditPost.append("}\n}\n");
                        } else {
                            updateRelatedInEditPre.append("\n" + relTypeReference + " " + scalarRelFieldName + "Old = " + oldMe + "." + mName + "();\n");
                            updateRelatedInEditPre.append(relTypeReference + " " + scalarRelFieldName + "New = " + fieldName + "." + mName +"();\n");
                            if (!relColumnNullable && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) {
                                illegalOrphansInEdit.append(
                                        "if(" + scalarRelFieldName + "Old != null && !" + scalarRelFieldName + "Old.equals(" + scalarRelFieldName + "New)) {\n" +
                                                "if (illegalOrphanMessages == null) {\n" +
                                                "illegalOrphanMessages = new ArrayList<String>();\n" +
                                                "}\n" +
                                                "illegalOrphanMessages.add(\"You must retain " + relTypeReference + " \" + " + scalarRelFieldName + "Old + \" since its " + otherFieldName + " field is not nullable.\");\n" +
                                                        "}\n");
                            }
                            String refOrMergeStringInEdit = getRefOrMergeString(relIdGetterElement, scalarRelFieldName + "New");
                            attachRelatedInEdit.append("if (" + scalarRelFieldName + "New != null) {\n" +
                                    scalarRelFieldName + "New = " + refOrMergeStringInEdit +
                                    fieldName + ".s" + mName.substring(1) + "(" + scalarRelFieldName + "New);\n" +
                                            "}\n");
                            if (otherSideMultiplicity == JpaControllerUtil.REL_TO_MANY || relColumnNullable) {
                                updateRelatedInEditPost.append(   
                                        "if(" + scalarRelFieldName + "Old != null && !" + scalarRelFieldName + "Old.equals(" + scalarRelFieldName + "New)) {\n" +
                                                ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + "Old.s" + otherName.substring(1) + "(null);\n" :
                                                        scalarRelFieldName + "Old." + otherName + "().remove(" + fieldName +");\n") +
                                                scalarRelFieldName + "Old = em.merge(" + scalarRelFieldName +"Old);\n}\n");
                            }
                            if (multiplicity == JpaControllerUtil.REL_TO_ONE && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && !columnNullable) {
                                illegalOrphansInEdit.append(
                                        "if(" + scalarRelFieldName + "New != null && !" + scalarRelFieldName + "New.equals(" + scalarRelFieldName + "Old)) {\n");
                                illegalOrphansInEdit.append(simpleEntityName + " " + relrelInstanceName + " = " + scalarRelFieldName + "New." + relrelGetterName + "();\n" +
                                        "if (" + relrelInstanceName + " != null) {\n" + 
                                                "if (illegalOrphanMessages == null) {\n" +
                                                "illegalOrphanMessages = new ArrayList<String>();\n" +
                                                "}\n" +
                                                "illegalOrphanMessages.add(\"The " + relTypeReference + " \" + " + scalarRelFieldName + "New + \" already has an item of type " + simpleEntityName + " whose " + scalarRelFieldName + " column cannot be null. Please make another selection for the " + scalarRelFieldName + " field.\");\n" +
                                                        "}\n");
                                illegalOrphansInEdit.append("}\n");
                            }
                            updateRelatedInEditPost.append(
                                    "if(" + scalarRelFieldName + "New != null && !" + scalarRelFieldName + "New.equals(" + scalarRelFieldName + "Old)) {\n");
                            if (multiplicity == JpaControllerUtil.REL_TO_ONE && otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && columnNullable) {
                                String tmpType = simpleEntityName;
                                if(!otherType.equals(entityClass)){
                                    tmpType = otherType;
                                }
                                updateRelatedInEditPost.append(tmpType + " " + relrelInstanceName + " = " + scalarRelFieldName + "New." + relrelGetterName + "();\n" +
                                        "if (" + relrelInstanceName + " != null) {\n" +
                                        relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" +
                                        relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" +
                                                "}\n");
                            }
                            updateRelatedInEditPost.append(
                                    ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + "New.s" + otherName.substring(1) + "(" + fieldName + ");\n" :
                                            scalarRelFieldName + "New." + otherName + "().add(" + fieldName +");\n") +
                                            scalarRelFieldName + "New = em.merge(" + scalarRelFieldName + "New);\n}\n"
                            );
                        }
                        
                        if (otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE && !relColumnNullable) {
                            String orphanCheckCollection = relFieldName + "OrphanCheck";
                            String orphanCheckScalar = isCollection ? orphanCheckCollection + relTypeReference : relFieldName + "OrphanCheck";
                            illegalOrphansInDestroy.append(
                                    (isCollection ? simpleCollectionTypeName + "<" + relTypeReference + "> " + orphanCheckCollection : relTypeReference + " " + orphanCheckScalar) + " = " + fieldName + "." + mName +"();\n" +
                                            (isCollection ? "for(" + relTypeReference + " " + orphanCheckScalar + " : " + orphanCheckCollection : "if (" + orphanCheckScalar + " != null") + ") {\n" +
                                                    "if (illegalOrphanMessages == null) {\n" +
                                                    "illegalOrphanMessages = new ArrayList<String>();\n" +
                                                    "}\n" +
                                                    "illegalOrphanMessages.add(\"This " + simpleEntityName + " (\" + " +  fieldName + " + \") cannot be destroyed since the " + relTypeReference + " \" + " + orphanCheckScalar + " + \" in its " + relFieldName + " field has a non-nullable " + otherFieldName + " field.\");\n" +
                                                            "}\n");
                        }
                        if (otherSideMultiplicity == JpaControllerUtil.REL_TO_MANY || relColumnNullable) {
                            updateRelatedInDestroy.append( (isCollection ? simpleCollectionTypeName + "<" + relTypeReference + "> " + relFieldName : relTypeReference + " " + scalarRelFieldName) + " = " + fieldName + "." + mName +"();\n" +
                                    (isCollection ? "for(" + relTypeReference + " " + scalarRelFieldName + " : " + relFieldName : "if (" + scalarRelFieldName + " != null") + ") {\n" +
                                    ((otherSideMultiplicity == JpaControllerUtil.REL_TO_ONE) ? scalarRelFieldName + ".s" + otherName.substring(1) + "(null);\n" :
                                            scalarRelFieldName + "." + otherName + "().remove(" + fieldName +");\n") +
                                    scalarRelFieldName + " = em.merge(" + scalarRelFieldName +");\n}\n\n");
                        }
                        
                        if (collectionTypeClass != null) { //(multiplicity == JpaControllerUtil.REL_TO_MANY) {
                            modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, collectionTypeClass);
                        }

                    } else {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot detect other side of a relationship.");
                    }
                    
                }
                // </editor-fold>
                
                String BEGIN = isInjection ? "utx.begin();\nem = getEntityManager();" : "em = getEntityManager();\nem.getTransaction().begin();";
                String COMMIT = isInjection ? "utx.commit();" : "em.getTransaction().commit();";
                String ROLLBACK = isInjection ? "try {\n" +
                        "utx.rollback();" +
                        "\n} catch (Exception re) {\n" +
                        "throw new RollbackFailureException(\"An error occurred attempting to roll back the transaction.\", re);\n" +
                        "}\n"
                        : "";
                
                if (illegalOrphansInCreate.length() > 0 || illegalOrphansInEdit.length() > 0 || illegalOrphansInDestroy.length() > 0) {
                    modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "java.util.ArrayList");
                }
                
                if (illegalOrphansInCreate.length() > 0) {
                    illegalOrphansInCreate.insert(0, "List<String> illegalOrphanMessages = null;\n");
                    illegalOrphansInCreate.append("if (illegalOrphanMessages != null) {\n" +
                            "throw new IllegalOrphanException(illegalOrphanMessages);\n" +
                            "}\n");
                }
                
                TypeElement entityType = workingCopy.getElements().getTypeElement(entityClass);
                StringBuilder codeToPopulatePkFields = new StringBuilder();
                if (embeddable[0]) {
                    for (ExecutableElement pkMethod : embeddedPkSupport.getPkAccessorMethods(entityType)) {
                        if (embeddedPkSupport.isRedundantWithRelationshipField(entityType, pkMethod)) {
                            codeToPopulatePkFields.append(fieldName + "." +idGetterName[0] + "().s" + pkMethod.getSimpleName().toString().substring(1) + "(" +  //NOI18N
                                    fieldName + "." + embeddedPkSupport.getCodeToPopulatePkField(entityType, pkMethod) + ");\n");
                        }
                    }
                }
                
                boolean isGenerated = JpaControllerUtil.isGenerated(idGetterElement, isFieldAccess);
                bodyText = (embeddable[0] ? "if (" + fieldName + "." + idGetterName[0] + "() == null) {\n" +
                        fieldName + ".s" + idGetterName[0].substring(1) + "(new " + idClass.getSimpleName() + "());\n" +
                        "}\n" : "") +
                        initCollectionsInCreate.toString() +
                        codeToPopulatePkFields.toString() +
                        illegalOrphansInCreate.toString() +
                        "EntityManager em = null;\n" +
                        "try {\n " + BEGIN + "\n " +
                        initRelatedInCreate.toString() + "em.persist(" + fieldName + ");\n" + updateRelatedInCreate.toString() + COMMIT + "\n" +   //NOI18N
                        (isInjection || !isGenerated ? "} catch (Exception ex) {\n" : "") +
                        ROLLBACK +
                        (!isGenerated ? "if (find" + simpleEntityName + "(" + fieldName + "." + idGetterName[0] + "()) != null) {\n" +
                        "throw new PreexistingEntityException(\"" + simpleEntityName + " \" + " + fieldName + " + \" already exists.\", ex);\n" +
                        "}\n" : "") +
                        (isInjection || !isGenerated ? "throw ex;\n" : "") +
                        "} finally {\n if (em != null) {\nem.close();\n}\n}";
                
                List<String> methodExceptionTypeList = new ArrayList<>();
                if (illegalOrphansInCreate.length() > 0) {
                    methodExceptionTypeList.add(exceptionPackage + ".IllegalOrphanException");
                }
                if (!isGenerated) {
                    methodExceptionTypeList.add(exceptionPackage + ".PreexistingEntityException");
                }
                if (isInjection) {
                    methodExceptionTypeList.add(exceptionPackage + ".RollbackFailureException");
                }
                if (isInjection || !isGenerated) {
                    methodExceptionTypeList.add("java.lang.Exception");
                }
                String[] createExceptionTypes = methodExceptionTypeList.toArray(new String[0]);
                methodInfo = new MethodInfo("create", publicModifier, "void", createExceptionTypes, new String[]{entityClass}, new String[]{fieldName}, bodyText, null, null);
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                
                if (illegalOrphansInEdit.length() > 0) {
                    illegalOrphansInEdit.insert(0, "List<String> illegalOrphanMessages = null;\n");
                    illegalOrphansInEdit.append("if (illegalOrphanMessages != null) {\n" +
                            "throw new IllegalOrphanException(illegalOrphanMessages);\n" +
                            "}\n");
                }
                
                bodyText = codeToPopulatePkFields.toString() +
                        "EntityManager em = null;\n" + 
                        "try {\n " + BEGIN + "\n" +
                        updateRelatedInEditPre.toString() + illegalOrphansInEdit.toString() + attachRelatedInEdit.toString() +
                        fieldName + " = em.merge(" + fieldName + ");\n " + 
                        updateRelatedInEditPost.toString() + COMMIT + "\n" +   //NOI18N
                        "} catch (Exception ex) {\n" +
                        ROLLBACK +
                        "String msg = ex.getLocalizedMessage();\n" + 
                        "if (msg == null || msg.length() == 0) {\n" +
                        simpleIdPropertyType + " id = " + fieldName + "." + idGetterName[0] + "();\n" +
                        "if (find" + simpleEntityName + "(id) == null) {\n" +
                        "throw new NonexistentEntityException(\"The " + simpleEntityName.substring(0, 1).toLowerCase() + simpleEntityName.substring(1) + " with id \" + id + \" no longer exists.\");\n" +
                        "}\n" +
                        "}\n" +
                        "throw ex;\n} " +   //NOI18N
                        "finally {\n if (em != null) {\nem.close();\n}\n }";
                methodExceptionTypeList.clear();
                if (illegalOrphansInEdit.length() > 0) {
                    methodExceptionTypeList.add(exceptionPackage + ".IllegalOrphanException");
                }
                methodExceptionTypeList.add(exceptionPackage + ".NonexistentEntityException");
                if (isInjection) {
                    methodExceptionTypeList.add(exceptionPackage + ".RollbackFailureException");
                }
                methodExceptionTypeList.add("java.lang.Exception");
                String[] editExceptionTypes = methodExceptionTypeList.toArray(new String[0]);
                methodInfo = new MethodInfo("edit", publicModifier, "void", editExceptionTypes, new String[]{entityClass}, new String[]{fieldName}, bodyText, null, null);
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                
                if (illegalOrphansInDestroy.length() > 0) {
                    illegalOrphansInDestroy.insert(0, "List<String> illegalOrphanMessages = null;\n");
                    illegalOrphansInDestroy.append("if (illegalOrphanMessages != null) {\n" +
                            "throw new IllegalOrphanException(illegalOrphanMessages);\n" +
                            "}\n");
                }
                
                String refOrMergeStringInDestroy = "em.merge(" + fieldName + ");\n";
                if (idGetterElement != null) {
                    refOrMergeStringInDestroy = "em.getReference(" + simpleEntityName + ".class, id);\n";
                }
                bodyText = "EntityManager em = null;\n" +
                        "try {\n " + BEGIN + "\n" + 
                        simpleEntityName + " " + fieldName + ";\n" +
                        "try {\n " +
                        fieldName + " = " + refOrMergeStringInDestroy + 
                        fieldName + "." + idGetterName[0] + "();\n" +
                        "} catch (EntityNotFoundException enfe) {\n" +
                        "throw new NonexistentEntityException(\"The " + fieldName + " with id \" + id + \" no longer exists.\", enfe);\n" +
                        "}\n" + 
                        illegalOrphansInDestroy.toString() +
                        updateRelatedInDestroy.toString() + 
                        "em.remove(" + fieldName + ");\n " + COMMIT + "\n" +   //NOI18N
                        (isInjection ? "} catch (Exception ex) {\n" : "") +
                        ROLLBACK + 
                        (isInjection ? "throw ex;\n" : "") +
                        "} finally {\n if (em != null) {\nem.close();\n}\n }";  //NOI18N
                methodExceptionTypeList.clear();
                if (illegalOrphansInDestroy.length() > 0) {
                    methodExceptionTypeList.add(exceptionPackage + ".IllegalOrphanException");
                }
                methodExceptionTypeList.add(exceptionPackage + ".NonexistentEntityException");
                if (isInjection) {
                    methodExceptionTypeList.add(exceptionPackage + ".RollbackFailureException");
                    methodExceptionTypeList.add("java.lang.Exception");
                }
                String[] destroyExceptionTypes = methodExceptionTypeList.toArray(new String[0]);
                String[] findDestroyType = derived[0] && derivedIdPropertyType[0]!=null ? derivedIdPropertyType : idPropertyType;
                methodInfo = new MethodInfo("destroy", publicModifier, "void", destroyExceptionTypes, findDestroyType, new String[]{"id"}, bodyText, null, null);
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                
                //secondary destroy with derived type (entity type id)
                if(derived[0] && derivedIdPropertyType[0]!=null && !derivedIdPropertyType[0].equals(idPropertyType[0])){
                    bodyText = "destroy( id." + derivedIdGetterName[0] + "() );";
                    methodInfo = new MethodInfo("destroy", publicModifier, "void", destroyExceptionTypes, idPropertyType, new String[]{"id"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                }
                //
                
                TypeInfo listOfEntityType = new TypeInfo("java.util.List", new String[]{entityClass});
                
                bodyText = "return find" + simpleEntityName + "Entities(true, -1, -1);";
                methodInfo = new MethodInfo("find" + simpleEntityName + "Entities", publicModifier, listOfEntityType, null, null, null, bodyText, null, null);
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                
                bodyText = "return find" + simpleEntityName + "Entities(false, maxResults, firstResult);";
                methodInfo = new MethodInfo("find" + simpleEntityName + "Entities", publicModifier, listOfEntityType, null, TypeInfo.fromStrings(new String[]{"int", "int"}), new String[]{"maxResults", "firstResult"}, bodyText, null, null);
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                
                bodyText = "EntityManager em = getEntityManager();\n try{\n" +
                        (
                        version!=null && !Persistence.VERSION_1_0.equals(version) ?
                        "CriteriaQuery cq = em.getCriteriaBuilder().createQuery();\n"+
                        "cq.select(cq.from("+simpleEntityName+".class));\n"+
                        "Query q = em.createQuery(cq);\n"
                        :
                        "Query q = em.createQuery(\"select object(o) from " + simpleEntityName +" as o\");\n"
                        )
                        +
                        "if (!all) {\n" +
                        "q.setMaxResults(maxResults);\n" +
                        "q.setFirstResult(firstResult);\n" + 
                        "}\n" +
                        "return q.getResultList();\n" + 
                        "} finally {\n em.close();\n}\n";
                methodInfo = new MethodInfo("find" + simpleEntityName + "Entities", privateModifier, listOfEntityType, null, TypeInfo.fromStrings(new String[]{"boolean", "int", "int"}), new String[]{"all", "maxResults", "firstResult"}, bodyText, null, null);
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                
                
                //getter for converter
                bodyText = "EntityManager em = getEntityManager();\n try{\n" +
                        "return em.find(" + simpleEntityName + ".class, id);\n" + 
                        "} finally {\n em.close();\n}\n";
                methodInfo = new MethodInfo("find" + simpleEntityName, publicModifier, entityClass, null, findDestroyType, new String[]{"id"}, bodyText, null, null);
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                
                //secondary find with derived type (entity type id), first one above is with derived (i.e. column) type.
                if(derived[0] && derivedIdPropertyType[0]!=null && !derivedIdPropertyType[0].equals(idPropertyType[0])){
                    bodyText = "return find" + simpleEntityName + "( id." + derivedIdGetterName[0] + "() );";
                    methodInfo = new MethodInfo("find" + simpleEntityName, publicModifier, entityClass, null, idPropertyType, new String[]{"id"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                }
                
                bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
                        (
                        version!=null && !Persistence.VERSION_1_0.equals(version) ?
                        "CriteriaQuery cq = em.getCriteriaBuilder().createQuery();\n"+
                        "Root<"+simpleEntityName+"> rt = cq.from("+simpleEntityName+".class); "+
                        "cq.select(em.getCriteriaBuilder().count(rt));\n"+
                        "Query q = em.createQuery(cq);\n"
                        :
                        "Query q = em.createQuery(\"select count(o) from " + simpleEntityName + " as o\");\n"
                        )
                        +
                        "return ((Long) q.getSingleResult()).intValue();\n" +
                        "} finally {\n em.close();\n}";
                methodInfo = new MethodInfo("get" + simpleEntityName + "Count", publicModifier, "int", null, null, null, bodyText, null, null);
                modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                
                workingCopy.rewrite(classTree, modifiedClassTree);
            }).commit();
    
        return controllerFileObject;
    }
    
    //mbohm: probably needs to be private; make public temporarily during refactoring.
    public static String getRefOrMergeString(ExecutableElement relIdGetterElement, String relFieldToAttach) {
        String refOrMergeString = "em.merge(" + relFieldToAttach + ");\n";
        if (relIdGetterElement != null) {
            String relIdGetter = relIdGetterElement.getSimpleName().toString();
            refOrMergeString = "em.getReference(" + relFieldToAttach + ".getClass(), " + relFieldToAttach + "." + relIdGetter + "());\n";
        }
        return refOrMergeString;
    }
    private static ExecutableElement findPrimaryKeyGetter(CompilationController controller, TypeElement bean) {
        ExecutableElement[] methods = JpaControllerUtil.getEntityMethods(bean);
        boolean isField = JpaControllerUtil.isFieldAccess(bean);
        for (ExecutableElement method : methods) {
            if (method.getSimpleName().toString().startsWith("get")) {//NOI18N
                if (isId(method, isField)) {
                    return method;
                }
            }
        }
        return null;
    }
    static boolean isId(ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? JpaControllerUtil.guessField(method) : method;
        if (element != null) {
            if (JpaControllerUtil.isAnnotatedWith(element, "jakarta.persistence.Id") // NOI18N
                    || JpaControllerUtil.isAnnotatedWith(element, "jakarta.persistence.EmbeddedId")  // NOI18N
                    || JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.Id")  // NOI18N
                    || JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.EmbeddedId")  // NOI18N
            ) {
                return true;
            }
        }
        return false;
    }
}

