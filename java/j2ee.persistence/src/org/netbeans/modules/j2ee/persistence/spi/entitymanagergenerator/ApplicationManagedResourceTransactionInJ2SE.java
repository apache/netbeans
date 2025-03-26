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

import java.text.MessageFormat;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import java.util.Collections;
import javax.lang.model.type.TypeKind;

/**
 * Generates the code needed for invoking an <code>EntityManager</code> in Java SE
 * enviroment with an application-managed persistence unit.
 *
 * @author Erno Mononen
 */
public final class ApplicationManagedResourceTransactionInJ2SE extends EntityManagerGenerationStrategySupport{
    
    @Override
    public ClassTree generate() {
        
        
        String body = "";

        FieldInfo em = getEntityManagerFieldInfo();
        
        if (!em.isExisting()) {
            body += getEmInitCode(getEntityManagerFactoryFieldInfo());
        }
        
        body += getInvocationCode(em);
        
        ModifiersTree methodModifiers = getTreeMaker().Modifiers(
                getGenerationOptions().getModifiers(),
                Collections.<AnnotationTree>emptyList()
                );
        
        MethodTree newMethod = getTreeMaker().Method(
                methodModifiers,
                computeMethodName(),
                getTreeMaker().PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                getParameterList(),
                Collections.<ExpressionTree>emptyList(),
                "{ " + body + "}",
                null
                );
        return getTreeMaker().addClassMember(getClassTree(), importFQNs(newMethod));
    }
    
    private String getEmInitCode(FieldInfo emf){
        if (!emf.isExisting()){
            return MessageFormat.format(
                    "javax.persistence.EntityManagerFactory {0} = javax.persistence.Persistence.createEntityManagerFactory(\"{1}\");\n" +
                    "javax.persistence.EntityManager {2} = {0}.createEntityManager();\n",
                    emf.getName(), getPersistenceUnitName(), ENTITY_MANAGER_DEFAULT_NAME);
            
        }
        return MessageFormat.format(
                "javax.persistence.EntityManager {0} = {1}.createEntityManager();\n",
                ENTITY_MANAGER_DEFAULT_NAME, emf.getName());
        
    }
    
    protected String getInvocationCode(FieldInfo em) {
        String text =
                "{0}.getTransaction().begin();\n" +
                "try '{'\n" +
                generateCallLines(em.getName())     +
                "    {0}.getTransaction().commit();\n" +
                "} catch (Exception e) '{'\n" +
                "    e.printStackTrace();\n" +
                "    {0}.getTransaction().rollback();\n" +
                "} finally '{'\n" +
                "    {0}.close();\n" +
                "}";
        return MessageFormat.format(text, em.getName());
    }
    
}
