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

package org.netbeans.modules.websvc.rest.codegen;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerIterator;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.ProgressReporter;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.WebXmlHelper;
import org.openide.filesystems.FileObject;


import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;

/**
 *
 * @author ads
 */
public class J2eeEntityResourcesGenerator extends EntityResourcesGenerator {
    
    protected boolean  generateInfrastracture( final Set<FileObject> createdFiles,
            final String entityFqn, final FileObject facade ) throws IOException
    {
        if ( !super.generateInfrastracture(createdFiles, entityFqn, facade) ){
            return false;
        }
        String entityManagerAccessor = generateEntityManagerFactoryAccess( facade );
        return generateJpaControllerAccess( facade , 
                JavaIdentifiers.unqualify(entityFqn) , entityManagerAccessor);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator#preGenerate(java.util.List)
     */
    @Override
    protected void preGenerate( List<String> fqnEntities ) throws IOException {
        ProgressReporter reporter = new ProgressReporterImpl( this  );
        jpaControllers = JpaControllerIterator.generateJpaControllers(reporter, 
                fqnEntities, getProject(), getControllerPackageName(), 
                getControllerFolder(), null, true);
    }

    @Override
    protected void configurePersistence() {
        // Add <persistence-unit-ref> to web.xml
        new WebXmlHelper(getProject(), getPersistenceUnit().getName()).configure();
    }
    
    protected int getTotalWorkUnits() {
        return JpaControllerIterator.getProgressStepCount( getEntitiesCount() ) 
            +getEntitiesCount() ;
    }
    
    @Override
    protected RestGenerationOptions getGenerationOptions(
            RestFacadeMethod method, String entityFQN, String paramArg,
            String idType )
    {
        String entitySimpleName = JavaIdentifiers.unqualify(entityFQN);
        RestGenerationOptions options = super.getGenerationOptions(method, 
                entityFQN, paramArg, idType);
        boolean needPathSegment = false;
        EntityClassInfo entityInfo = getModel().getEntityInfo(entityFQN);
        if ( entityInfo!= null ){
            FieldInfo idFieldInfo = entityInfo.getIdFieldInfo();
            needPathSegment = idFieldInfo!= null && idFieldInfo.isEmbeddedId()&& 
                    idFieldInfo.getType()!= null;
        }
        StringBuilder builder ;
        switch ( method ){
            case CREATE:
                builder = new StringBuilder("try { ");                          // NOI18N
                builder.append("getJpaController().create(entity);");           // NOI18N
                builder.append("return Response.created(");                     //NOI18N
                builder.append("URI.create(");                                  //NOI18N
                builder.append(getIdFieldToUriStmt(getModel().getEntityInfo(entityFQN).
                        getIdFieldInfo()));        
                builder.append(".toString())).build();");                       //NOI18N
                builder.append("} catch (Exception ex) {");                     // NOI18N
                builder.append("return Response.notModified(ex.getMessage()).build();");// NOI18N
                options.setBody(builder.toString());
                return options;
            case EDIT:
                builder = new StringBuilder("try { ");                          // NOI18N
                builder.append("getJpaController().edit(entity);");             // NOI18N
                builder.append("return Response.ok().build();");                //NOI18N
                builder.append("} catch (Exception ex) {");                     // NOI18N
                builder.append("return Response.notModified(ex.getMessage()).build();");// NOI18N
                options.setBody(builder.toString());
                return options;
            case REMOVE:
                builder = new StringBuilder("try { ");                          // NOI18N
                if ( needPathSegment ){
                    builder.append(idType);
                    builder.append( " key=getPrimaryKey(id);\n");               // NOI18N
                    paramArg = "key";                                           // NOI18N
                }
                builder.append("getJpaController().destroy(");                  // NOI18N
                builder.append( paramArg );
                builder.append("; return Response.ok().build();");              //NOI18N
                builder.append("} catch (Exception ex) {");                     // NOI18N
                builder.append("return Response.notModified(ex.getMessage()).build();");// NOI18N
                options.setBody(builder.toString());
                return options;
            case FIND:
                builder = new StringBuilder();
                if ( needPathSegment ){
                    builder.append(idType);
                    builder.append( " key=getPrimaryKey(id);\n");               // NOI18N
                    paramArg = "key";                                           // NOI18N
                }
                builder.append("return getJpaController().find");  // NOI18N
                builder.append(entitySimpleName);
                builder.append('(');
                builder.append(paramArg);
                builder.append(");");                                           // NOI18N
                options.setBody(builder.toString());
                return options;
            case FIND_ALL:
                builder = new StringBuilder("return getJpaController().find");  // NOI18N
                builder.append(entitySimpleName);
                builder.append("Entities();");                                  // NOI18N
                options.setBody(builder.toString());
                return options;
            case FIND_RANGE:
                builder = new StringBuilder("return getJpaController().find");  // NOI18N
                builder.append(entitySimpleName);
                builder.append("Entities(max,first);");                         // NOI18N
                options.setBody(builder.toString());
                return options;
            case COUNT:
                builder = new StringBuilder(
                        "return String.valueOf(getJpaController().get");        // NOI18N
                builder.append(entitySimpleName);
                builder.append("Count();");                                     // NOI18N
                options.setBody( builder.toString());
                return options;
        }
        return null;
    }
    
    private boolean generateJpaControllerAccess(FileObject fileObject , 
            String entityName, String emGetter) throws IOException 
    {
        String jpaControllerName = entityName+"JpaController";  // NOI18N
        FileObject controller = null;
        for ( FileObject jpaController : jpaControllers ){
            if ( jpaController.getName().startsWith(jpaControllerName)){
                jpaControllerName = jpaController.getName();
                controller = jpaController;
                break;
            }
        }
        if ( controller== null ){
            return false;
        }
        final String fqnController = JavaSourceHelper.getClassType(
                JavaSource.forFileObject(controller));
        Project project = FileOwnerQuery.getOwner( fileObject );
        final StringBuilder bodyText = new StringBuilder("{try { "); // NOI18N
        boolean needUtx = false;
        if (project != null && Util.isContainerManaged( project)) {
            needUtx = true;
            bodyText.append("UserTransaction utx = (UserTransaction) new ");// NOI18N
            bodyText.append("InitialContext().lookup(\"java:comp/UserTransaction\");");// NOI18N
        }
        bodyText.append("return new ");                                 // NOI18N
        bodyText.append( jpaControllerName );
        if ( needUtx ){
            bodyText.append( "(utx, ");                                     // NOI18N
        }
        else {
            bodyText.append( "(null, ");                                     // NOI18N
        }
        bodyText.append( emGetter);
        bodyText.append( "());} catch (NamingException ex) {" );        // NOI18N
        bodyText.append( "throw new RuntimeException(ex);}");           // NOI18N

        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if ( javaSource== null){
            return false;
        }
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree tree = workingCopy.getCompilationUnit();
                
                String imports[] = new String[]{
                        "javax.transaction.UserTransaction",
                        "javax.persistence.EntityManagerFactory",
                        "javax.naming.NamingException",
                        "javax.naming.InitialContext"};             // NOI18N
                JavaSourceHelper.addImports(workingCopy, imports);
                
                for (Tree typeDeclaration : tree.getTypeDecls()){
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration.getKind())){
                        ClassTree classTree = (ClassTree) typeDeclaration;
                        ClassTree newTree = JavaSourceHelper.addMethod(workingCopy, classTree,
                                new Modifier[]{Modifier.PRIVATE}, new String[]{} , 
                                null,
                                "getJpaController", fqnController, null, null, 
                                null, null, bodyText.toString(), null); // NOI18N
                        workingCopy.rewrite(classTree, newTree);
                    }
                }
            }
        };
        javaSource.runModificationTask(task).commit();
        return true;
    }
    

    private String generateEntityManagerFactoryAccess( FileObject fileObject ) 
        throws IOException 
    {
        
        final StringBuilder bodyText = new StringBuilder(
                "{return (EntityManagerFactory) new ");             // NOI18N
        bodyText.append("InitialContext().lookup(\"java:comp/env/");// NOI18N
        bodyText.append( WebXmlHelper.PERSISTENCE_FACTORY);
        bodyText.append( "\");");                                   // NOI18N
        
        
        final String entityManagerMethod = "getEntityManagerFactory";// NOI18N
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree tree = workingCopy.getCompilationUnit();
                
                TreeMaker maker = workingCopy.getTreeMaker();
                
                ExpressionTree exceptionTree = JavaSourceHelper.createTypeTree(workingCopy, 
                    "javax.naming.NamingException");                    // NOI18N
                Tree returnTypeTree = JavaSourceHelper.createTypeTree(workingCopy, 
                        "javax.persistence.EntityManagerFactory");      // NOI18N
                
                ModifiersTree modifiersTree = JavaSourceHelper.createModifiersTree(
                        workingCopy,new Modifier[]{Modifier.PRIVATE} , 
                        null, null);
                
                MethodTree methodTree = maker.Method(modifiersTree, 
                        entityManagerMethod, returnTypeTree, 
                        Collections.<TypeParameterTree>emptyList(), 
                        Collections.<VariableTree>emptyList(), 
                        Collections.singletonList( exceptionTree ), 
                        bodyText.toString(), null);
                
                for (Tree typeDeclaration : tree.getTypeDecls()){
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration.getKind())){
                        ClassTree classTree = (ClassTree) typeDeclaration;
                        ClassTree newTree = maker.addClassMember(classTree, methodTree);
                        workingCopy.rewrite(classTree, newTree);
                    }
                }
                
            }
        };
        
        javaSource.runModificationTask(task).commit();
        return entityManagerMethod;
    }
    
    private FileObject[] jpaControllers;
}
