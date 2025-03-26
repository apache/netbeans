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
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Erno Mononen
 */
public abstract class EntityManagerGenerationTestSupport  extends SourceTestSupport{
    
    public EntityManagerGenerationTestSupport(String testName){
        super(testName);
    }
    
    
    protected FileObject generate(FileObject targetFo, final GenerationOptions options) throws IOException{
        
        JavaSource targetSource = JavaSource.forFileObject(targetFo);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            
            @Override
            public void cancel() {
            }
            
            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                for (Tree typeDeclaration : cut.getTypeDecls()){
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration.getKind())){
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        ClassTree modifiedClazz = getStrategy(workingCopy, make, clazz, options).generate();
                        workingCopy.rewrite(clazz, modifiedClazz);
                    }
                }
                
            }
        };
        targetSource.runModificationTask(task).commit();
        
        return targetFo;
        
    }

    /**
     * @return a persistence unit with name "MyPersistenceUnit". 
     */ 
    protected PersistenceUnit getPersistenceUnit(){
        PersistenceUnit punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        punit.setName("MyPersistenceUnit");
        return punit;
    }
    
    protected EntityManagerGenerationStrategy getStrategy(WorkingCopy workingCopy, TreeMaker make, ClassTree clazz, GenerationOptions options){
        
        EntityManagerGenerationStrategy result = null;
        
        try{
            result = getStrategyClass().getDeclaredConstructor().newInstance();
            result.setClassTree(clazz);
            result.setWorkingCopy(workingCopy);
            result.setGenerationOptions(options);
            result.setTreeMaker(make);
            result.setPersistenceUnit(getPersistenceUnit());
        } catch (ReflectiveOperationException ex){
            throw new RuntimeException(ex);
        }
        
        return result;
    }

    
    protected abstract Class<? extends EntityManagerGenerationStrategy> getStrategyClass();

    
}

