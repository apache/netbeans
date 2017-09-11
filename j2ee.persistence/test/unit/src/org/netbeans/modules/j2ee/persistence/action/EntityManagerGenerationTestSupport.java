/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            
            public void cancel() {
            }
            
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
            result = getStrategyClass().newInstance();
            result.setClassTree(clazz);
            result.setWorkingCopy(workingCopy);
            result.setGenerationOptions(options);
            result.setTreeMaker(make);
            result.setPersistenceUnit(getPersistenceUnit());
        } catch (IllegalAccessException iae){
            throw new RuntimeException(iae);
        } catch (InstantiationException ie){
            throw new RuntimeException(ie);
        }
        
        return result;
    }

    
    protected abstract Class<? extends EntityManagerGenerationStrategy> getStrategyClass();

    
}

