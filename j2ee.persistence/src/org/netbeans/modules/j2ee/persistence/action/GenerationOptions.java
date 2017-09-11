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

package org.netbeans.modules.j2ee.persistence.action;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;

/**
 * This class represents code generation options for invoking
 * <code>javax.persistence.EntityManager</code> .
 *
 * @author Erno Mononen
 */
public final class GenerationOptions {
    
    public enum Operation {
        // {0} the Classname of the entity Class object
        // {1} the name of the given parameter, i.e. <code>parameterName</code>.
        // {2} the class of the given parameter, i.e. <code>parameterType</code>.
        // {3} the return type of the method, i.e. <code>returnType</code>.
        // {4} a query attribute for the query, i.e. <code>queryAttribute</code>.
        // {5} the java.lang.Class object of the entity class
        PERSIST("{0}.persist({1});"),
        MERGE("{0}.merge({1});"),
        REMOVE("{0}.remove({0}.merge({1}));"),
        FIND("return {0}.find({5}, {1});"),
        // here the query attribute represents the name of the entity class
        FIND_ALL(
                "return {0}.createQuery(\"select object(o) from \" + {5}.getSimpleName() + \" as o\").getResultList();",
                "javax.persistence.criteria.CriteriaQuery cq = {0}.getCriteriaBuilder().createQuery();cq.select(cq.from({5}));return {0}.createQuery(cq).getResultList();"
                ),
        //querry to get only items starting from {1}[0] up to {1}[1]-1
        FIND_SUBSET(
                "javax.persistence.Query q = {0}.createQuery(\"select object(o) from \" + {5}.getSimpleName() + \" as o\");\nq.setMaxResults({1}[1]-{1}[0]+1);\nq.setFirstResult({1}[0]);\nreturn q.getResultList();",
                "javax.persistence.criteria.CriteriaQuery cq = {0}.getCriteriaBuilder().createQuery();cq.select(cq.from({5}));javax.persistence.Query q = {0}.createQuery(cq);q.setMaxResults({1}[1]-{1}[0]+1);q.setFirstResult({1}[0]);return q.getResultList();"),
        //qurrry to get count(*) on a table
        COUNT(
                "return ((Long) {0}.createQuery(\"select count(o) from \" + {5}.getSimpleName() + \" as o\").getSingleResult()).intValue();",
                "javax.persistence.criteria.CriteriaQuery cq = {0}.getCriteriaBuilder().createQuery();javax.persistence.criteria.Root<{4}> rt = cq.from({5});cq.select({0}.getCriteriaBuilder().count(rt));javax.persistence.Query q = {0}.createQuery(cq);return ((Long) q.getSingleResult()).intValue();"),
        GET_EM("return {0};");

        private String body;
        private String body2_0;
        
        private Operation(String body){
            this(body, body);
        }

        private Operation(String body, String body2_0){
            this.body2_0=body2_0;
            this.body = body;
        }

        /*
         * @return default body (for jpa 1.0)
         */
        public String getBody(){
            return getBody(Persistence.VERSION_1_0);
        }

        /*
         * @return body for corresponding jpa version, default is 1.0
         */
        public String getBody(String version){
            if(version!=null && !Persistence.VERSION_1_0.equals(version))//any version except 1.0 will get this case
            {
                return body2_0;
            }
            else return body;
        }
    }
    
    private Operation operation;
    private String methodName;
    private String returnType;
    private String parameterName;
    private String parameterType;
    private String queryAttribute;
    private String annotationType;
    private Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);

    /** Creates a new instance of GenerationOptions */
    public GenerationOptions() {
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public Operation getOperation() {
        return operation;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getQueryAttribute() {
        return queryAttribute;
    }
    
    public String getReturnType() {
        return returnType;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = new HashSet<Modifier>(modifiers);
    }


    public String getCallLines(){
        return getCallLines(null, null);
    }

    public String getCallLines(String emName, String ecName){
        return getCallLines(emName, ecName, Persistence.VERSION_1_0);
    }

    public String getCallLines(String emName, String ecName, String version){
        return operation == null ? null : MessageFormat.format(operation.getBody(version), new Object[] {
            emName,
            getParameterName(),
            getParameterType(),
            getReturnType(),
            getQueryAttribute(),
            ecName});
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    
    public void setQueryAttribute(String queryAttribute) {
        this.queryAttribute = queryAttribute;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getAnnotation() {
        return annotationType;
    }

    public void setAnnotation(String annotationType) {
        this.annotationType = annotationType;
    }
 
}
