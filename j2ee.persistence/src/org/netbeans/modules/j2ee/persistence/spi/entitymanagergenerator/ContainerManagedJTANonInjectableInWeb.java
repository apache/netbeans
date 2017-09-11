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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import java.text.MessageFormat;
import java.util.Collections;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
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

    public ClassTree generate() {

        if(!ElementKind.CLASS.equals(getClassElement().getKind()))
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
