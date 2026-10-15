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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
import org.netbeans.modules.j2ee.persistence.spi.targetinfo.JPATargetInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Generates the code needed for invoking an <code>EntityManager</code> in Java EE 5
 * web components with a container-managed persistence unit.
 *
 * @author Erno Mononen
 */
public final class ContainerManagedJTAInjectableInWeb extends EntityManagerGenerationStrategySupport {
    
    
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
        
        FieldInfo em = getEntityManagerFieldInfo();
        
        if(!em.isExisting()){
            List<ExpressionTree> attribs = new ArrayList<>();
            attribs.add(getGenUtils().createAnnotationArgument("name", "persistence/LogicalName")); //NOI18N
            attribs.add(getGenUtils().createAnnotationArgument("unitName", getPersistenceUnitName())); //NOI18N
            modifiedClazz = getGenUtils().addAnnotation(modifiedClazz, getGenUtils().createAnnotation(getPersistenceContextFqn(), attribs));
        }

        boolean simple = GenerationOptions.Operation.GET_EM.equals(getGenerationOptions().getOperation());//if simple (or with return etc) - no transactions
        if(!(isEJB || simple)){
            modifiedClazz = getTreeMaker().insertClassMember(modifiedClazz, getIndexForField(modifiedClazz), createUserTransaction());
        }
        
        ModifiersTree methodModifiers = getTreeMaker().Modifiers(
                Collections.<Modifier>singleton(Modifier.PROTECTED),
                Collections.<AnnotationTree>emptyList()
                );
        MethodTree newMethod = getTreeMaker().Method(
                methodModifiers,
                computeMethodName(),
                getTreeMaker().PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                getParameterList(),
                Collections.<ExpressionTree>emptyList(),
                "{ " + getMethodBody(em, isEJB) + "}",
                null
                );
        
        return getTreeMaker().addClassMember(modifiedClazz, importFQNs(newMethod));
    }
    

    private String getMethodBody(FieldInfo em, boolean isEJB){
        String ctxInit = em.isExisting() ? "" : "    javax.naming.Context ctx = (javax.naming.Context) new javax.naming.InitialContext().lookup(\"java:comp/env\");\n";
        String emInit = em.isExisting() ? "    {0}.joinTransaction();\n" :"    javax.persistence.EntityManager {0} =  (javax.persistence.EntityManager) ctx.lookup(\"persistence/LogicalName\");\n";
        boolean simple = GenerationOptions.Operation.GET_EM.equals(getGenerationOptions().getOperation());//if simple (or with return etc) - no transactions
        
        String text =
                "try '{'\n" +
                ctxInit +
                (isEJB || simple ? "" : "    utx.begin();\n") +
                emInit +
                generateCallLines(em.getName()) +
                (isEJB || simple ? "" : "    utx.commit();\n") +
                "} catch(Exception e) '{'\n" +
                "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
                "    throw new RuntimeException(e);\n" +
                "}";
        return MessageFormat.format(text, em.getName());
    }
    
}
