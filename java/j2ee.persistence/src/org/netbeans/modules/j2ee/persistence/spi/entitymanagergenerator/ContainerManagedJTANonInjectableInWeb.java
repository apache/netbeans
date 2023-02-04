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
import java.text.MessageFormat;
import java.util.Collections;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Comment;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Generates the code needed for invoking a container-managed <code>EntityManager</code> 
 * in classes that don't support dependency injection.
 * 
 * @author Erno Mononen
 */
public final class ContainerManagedJTANonInjectableInWeb extends EntityManagerGenerationStrategySupport {

    @Override
    public ClassTree generate() {

        if(ElementKind.CLASS != getClassElement().getKind())
        {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ContainerManagedJTANonInjectableInWeb.class, "LBL_ClassOnly"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return getClassTree();
        }
        else
        {
            FieldInfo em = getEntityManagerFieldInfo();

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
                    "{ " +
                    getMethodBody(em)
                    + "}",
                    null
                    );

            //TODO: should be added automatically, but accessing web / ejb apis from this module
            // would require API changes that might not be doable for 6.0
            String addToWebXmlComment =
                    " Add this to the deployment descriptor of this module (e.g. web.xml, ejb-jar.xml):\n" +
                    "<persistence-context-ref>\n" +
                    "   <persistence-context-ref-name>persistence/LogicalName</persistence-context-ref-name>\n" +
                    "   <persistence-unit-name>"+ getPersistenceUnitName() + "</persistence-unit-name>\n" +
                    "</persistence-context-ref>\n" +
                    "<resource-ref>\n" +
                    "    <res-ref-name>UserTransaction</res-ref-name>\n" +
                    "     <res-type>javax.transaction.UserTransaction</res-type>\n" +
                    "     <res-auth>Container</res-auth>\n" +
                    "</resource-ref>\n";

            MethodTree method = (MethodTree) importFQNs(newMethod);
            getTreeMaker().addComment(method.getBody().getStatements().get(0),
                    Comment.create(Comment.Style.BLOCK, 0, 0, 4, addToWebXmlComment), true);

            return getTreeMaker().addClassMember(getClassTree(), method);
        }
    }

    private String getMethodBody(FieldInfo em){
        String emInit = em.isExisting() ? "    {0}.joinTransaction();\n" :"    javax.persistence.EntityManager {0} =  (javax.persistence.EntityManager) ctx.lookup(\"java:comp/env/persistence/LogicalName\");\n";

        String text = 
                "try '{'\n" +
                "    javax.naming.Context ctx = new javax.naming.InitialContext();\n" +
                "    javax.transaction.UserTransaction utx = (javax.transaction.UserTransaction) ctx.lookup(\"java:comp/env/UserTransaction\");\n" +
                "    utx.begin();\n" +
                emInit +
                generateCallLines(em.getName()) +
                "    utx.commit();\n" +
                "} catch(Exception e) '{'\n" +
                "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
                "    throw new RuntimeException(e);\n" +
                "}";
        return MessageFormat.format(text, em.getName());
    }
}
