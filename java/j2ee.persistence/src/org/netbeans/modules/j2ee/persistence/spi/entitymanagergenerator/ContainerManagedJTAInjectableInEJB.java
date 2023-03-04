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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import javax.lang.model.SourceVersion;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
import org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Generates the code needed for invoking an <code>EntityManager</code> in EJB 3 
 * environment with a container-managed persistence unit.
 * 
 * @author Erno Mononen
 */
public final class ContainerManagedJTAInjectableInEJB extends EntityManagerGenerationStrategySupport {
    
    @Override
    public ClassTree generate() {
        
        ClassTree modifiedClazz = getClassTree();
        FileObject fo = FileUtil.toFileObject(new File(getWorkingCopy().getCompilationUnit().getSourceFile().toUri().getPath()));

        Project project = FileOwnerQuery.getOwner(fo);
        JPATargetInfo ti = project != null ? project.getLookup().lookup(JPATargetInfo.class) : null;
        boolean isEJB = false;
        if(ti != null){
            JPATargetInfo.TargetType tt = ti.getType(fo, getClassElement().getQualifiedName().toString());
            isEJB = JPATargetInfo.TargetType.EJB.equals(tt);
        }
        ModifiersTree methodModifiers = getTreeMaker().Modifiers(
                getGenerationOptions().getModifiers(),
                Collections.<AnnotationTree>emptyList()
                );
        
        FieldInfo em = getEntityManagerFieldInfo();
        if(!em.isExisting()){
            modifiedClazz = createEntityManager(Initialization.INJECT);
        }
        boolean simple = GenerationOptions.Operation.GET_EM.equals(getGenerationOptions().getOperation());//if simple (or with return etc) - no transactions
        if(!(isEJB || simple)){
            modifiedClazz = getTreeMaker().insertClassMember(modifiedClazz, getIndexForField(modifiedClazz), createUserTransaction());
        }
        
        MethodTree newMethod = getTreeMaker().Method(
                methodModifiers,
                computeMethodName(),
                getReturnTypeTree(),
                Collections.<TypeParameterTree>emptyList(),
                getParameterList(),
                Collections.<ExpressionTree>emptyList(),
                "{ " + getMethodBody(em, isEJB) + "}",
                null
                );

        if (getWorkingCopy().getSourceVersion().compareTo(SourceVersion.RELEASE_5) >= 0) {
            // create annotation for the method if required
            String annotation = getGenerationOptions().getAnnotation();
            if (annotation != null) {
                GenerationUtils genUtils = GenerationUtils.newInstance(getWorkingCopy());
                AnnotationTree annotationTree = genUtils.createAnnotation(annotation);
                if (annotationTree != null) {
                    newMethod = genUtils.addAnnotation(newMethod, annotationTree);
                }
            }
        }

        return getTreeMaker().addClassMember(modifiedClazz, importFQNs(newMethod));
    }
    private String getMethodBody(FieldInfo em, boolean isEJB){
        boolean simple = GenerationOptions.Operation.GET_EM.equals(getGenerationOptions().getOperation());//if simple (or with return etc) - no transactions
        String text =
                (isEJB || simple ?
                    "" :
                ("try '{'\n" +
                "    utx.begin();\n")) +
                generateCallLines(em.getName()) +
                (isEJB || simple ?
                    "" :
                "    utx.commit();\n" +
                "} catch(Exception e) '{'\n" +
                "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
                "    throw new RuntimeException(e);\n" +
                "}");
        return MessageFormat.format(text, em.getName());
    }
}
