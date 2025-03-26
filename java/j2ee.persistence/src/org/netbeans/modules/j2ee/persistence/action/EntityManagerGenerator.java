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

package org.netbeans.modules.j2ee.persistence.action;

import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolver;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInJ2SE;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Generates appropriate code for retrieving and invoking <code>javax.persistence.EntityManager</code>.
 * The generated code depends on the target class' enviroment.
 *
 * TODO: move this class to different package if anybody else wants to use it
 * @author Martin Adamek, Erno Mononen
 */

public final class EntityManagerGenerator {
    
    /**
     * The fully qualified name of the target class.
     */
    private final String fqn;
    /**
     *  The target java source file.
     */
    private final JavaSource targetSource;
    /**
     * The file object of the target source file.
     */
    private final FileObject targetFo;
    
    /**
     * The project to which the target file belongs.
     */
    private final Project project;
    
    /**
     * Creates a new EntityManagerGenerator.
     * @param targetFo the file object of the target java source file.
     * @param fqn the fully qualified name of the target java class.
     */
    public EntityManagerGenerator(FileObject targetFo, String fqn) {
        this.fqn = fqn;
        this.targetFo = targetFo;
        this.targetSource = JavaSource.forFileObject(targetFo);
        this.project = FileOwnerQuery.getOwner(targetFo);
    }

    /**
     * Generates the code needed for retrieving and invoking
     * an instance of <code>javax.persistence.EntityManager</code>. The generated 
     * code depends on the environment of the target class (e.g. whether
     * it supports injection or not).
     * 
     * @param options the options for the generation. Must not be null.
     * @return the modified file object of the target java class.
     */
    public FileObject generate(final GenerationOptions options) throws IOException{
        
        final Class<? extends EntityManagerGenerationStrategy> strategyClass = getStrategy();
    
        if (strategyClass == null){
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(EntityManagerGenerator.class, "ERR_NotSupported"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            
            return targetFo;
        }
        
        return generate(options, strategyClass);
        
    }
        
    /**
     * Generates the code needed for retrieving and invoking
     * an instance of <code>javax.persistence.EntityManager</code>. The generated 
     * code depends on the given <code>strategyClass</code>. 
     * 
     * @param options the options for the generation. Must not be null.
     * @param strategyClass the generation strategy that should be used. Must not be null.
     * @return the modified file object of the target java class.
     */
    public FileObject generate(final GenerationOptions options, 
            final Class<? extends EntityManagerGenerationStrategy> strategyClass) throws IOException{
    
        Parameters.notNull("options", options); //NOI18N
        Parameters.notNull("strategyClass", strategyClass); //NOI18N
        
        Task task = (Task<WorkingCopy>) (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            
            for (Tree typeDeclaration : cut.getTypeDecls()){
                if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration.getKind())){
                    ClassTree clazz = (ClassTree) typeDeclaration;
                    EntityManagerGenerationStrategy strategy = instantiateStrategy(strategyClass, workingCopy, make, clazz, options);
                    workingCopy.rewrite(clazz, strategy.generate());
                }
            }
        };
        
        targetSource.runModificationTask(task).commit();
        
        return targetFo;
    }
    
    private Class<? extends EntityManagerGenerationStrategy> getStrategy(){

        EntityManagerGenerationStrategyResolver resolver = project.getLookup().lookup(EntityManagerGenerationStrategyResolver.class);
        if (resolver != null){
            return resolver.resolveStrategy(targetFo);
        }
        
        // must be a java se project (we don't want it to implement the EntityManagerGenerationStrategyResolver SPI)
        return ApplicationManagedResourceTransactionInJ2SE.class;
    }
    
    private EntityManagerGenerationStrategy instantiateStrategy(Class<? extends EntityManagerGenerationStrategy> strategy, WorkingCopy workingCopy,
            TreeMaker make, ClassTree clazz, GenerationOptions options){
        
        EntityManagerGenerationStrategy result = null;
        
        try{
            result = strategy.getDeclaredConstructor().newInstance();
            result.setClassTree(clazz);
            result.setWorkingCopy(workingCopy);
            result.setGenerationOptions(options);
            result.setTreeMaker(make);
            result.setPersistenceUnit(getPersistenceUnit());
        } catch (ReflectiveOperationException ex){
            throw new RuntimeException(ex); //TODO
        }
        
        return result;
    }
    
    private  PersistenceUnit getPersistenceUnit() {
        PersistenceScope persistenceScope = PersistenceScope.getPersistenceScope(targetFo);
        
        if (persistenceScope == null){
            return null;
        }
        
        try {
            Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceScope.getPersistenceXml());
            if(persistence != null){
                PersistenceUnit[] pus=persistence.getPersistenceUnit();
                PersistenceUnit ret=pus.length>0 ? pus[0] : null;//if there is only one pu, return in any case (even if do not contain fqn)
                if(pus.length>1) {//searchh for best match
                    PersistenceUnit forAll=null;
                    PersistenceUnit forOne=null;
                    for(int i=0;i<pus.length && forOne==null;i++) {
                        PersistenceUnit tmp=pus[i];
                        if(forAll ==null && !tmp.isExcludeUnlistedClasses()) {
                            forAll=tmp;//first match sutable for all entities in the project
                        }
                        if(tmp.isExcludeUnlistedClasses()) {
                            String []classes = tmp.getClass2();
                            for(String clas:classes){
                                if(fqn.equals(clas)) {
                                    forOne = tmp;
                                    break;
                                }
                            }
                        }
                    }
                    ret = forOne != null ? forOne : (forAll != null ? forAll : ret);
                }
                return ret;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
}
