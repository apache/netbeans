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

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.EnumSet;
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
                "javax.persistence.criteria.CriteriaQuery cq = {0}.getCriteriaBuilder().createQuery();cq.select(cq.from({5}));return {0}.createQuery(cq).getResultList();",
                "jakarta.persistence.criteria.CriteriaQuery cq = {0}.getCriteriaBuilder().createQuery();cq.select(cq.from({5}));return {0}.createQuery(cq).getResultList();"
                ),
        //querry to get only items starting from {1}[0] up to {1}[1]-1
        FIND_SUBSET(
                "javax.persistence.Query q = {0}.createQuery(\"select object(o) from \" + {5}.getSimpleName() + \" as o\");\nq.setMaxResults({1}[1]-{1}[0]+1);\nq.setFirstResult({1}[0]);\nreturn q.getResultList();",
                "javax.persistence.criteria.CriteriaQuery cq = {0}.getCriteriaBuilder().createQuery();cq.select(cq.from({5}));javax.persistence.Query q = {0}.createQuery(cq);q.setMaxResults({1}[1]-{1}[0]+1);q.setFirstResult({1}[0]);return q.getResultList();",
                "jakarta.persistence.criteria.CriteriaQuery cq = {0}.getCriteriaBuilder().createQuery();cq.select(cq.from({5}));jakarta.persistence.Query q = {0}.createQuery(cq);q.setMaxResults({1}[1]-{1}[0]+1);q.setFirstResult({1}[0]);return q.getResultList();"
        ),
        //qurrry to get count(*) on a table
        COUNT(
                "return ((Long) {0}.createQuery(\"select count(o) from \" + {5}.getSimpleName() + \" as o\").getSingleResult()).intValue();",
                "javax.persistence.criteria.CriteriaQuery cq = {0}.getCriteriaBuilder().createQuery();javax.persistence.criteria.Root<{4}> rt = cq.from({5});cq.select({0}.getCriteriaBuilder().count(rt));javax.persistence.Query q = {0}.createQuery(cq);return ((Long) q.getSingleResult()).intValue();",
                "jakarta.persistence.criteria.CriteriaQuery cq = {0}.getCriteriaBuilder().createQuery();jakarta.persistence.criteria.Root<{4}> rt = cq.from({5});cq.select({0}.getCriteriaBuilder().count(rt));jakarta.persistence.Query q = {0}.createQuery(cq);return ((Long) q.getSingleResult()).intValue();"),
        GET_EM("return {0};");

        private final String body;
        private final String body2_0;
        private final String body3_0;

        private Operation(String body) {
            this(body, body);
        }

        private Operation(String body, String body2_0) {
            this(body, body2_0, body2_0);
        }

        private Operation(String body, String body2_0, String body3_0) {
            this.body3_0 = body3_0;
            this.body2_0 = body2_0;
            this.body = body;
        }

        /*
         * @return default body (for jpa 1.0)
         */
        public String getBody() {
            return getBody(Persistence.VERSION_1_0);
        }

        /*
         * @return body for corresponding jpa version, default is 1.0
         */
        public String getBody(String version) {
            if(version == null) {
                return body;
            }
            if(JPA_VERSION_COMPARATOR.compare(version, Persistence.VERSION_3_0) >= 0) {
                return body3_0;
            } else if(JPA_VERSION_COMPARATOR.compare(version, Persistence.VERSION_2_0) >= 0) {
                return body2_0;
            } else {
                return body;
            }
        }

        private static final Comparator<String> JPA_VERSION_COMPARATOR = (a, b) -> {
            String[] aComponents = a.split("\\D");
            String[] bComponents = b.split("\\D");
            for(int i = 0; i < Math.min(aComponents.length, bComponents.length); i++) {
                int numA;
                int numB;
                try {
                    numA = Integer.parseInt(aComponents[i]);
                } catch (NumberFormatException ex) {
                    numA = 0;
                }
                try {
                    numB = Integer.parseInt(bComponents[i]);
                } catch (NumberFormatException ex) {
                    numB = 0;
                }
                if (numA != numB) {
                    return numA - numB;
                }
            }
            return 0;
        };
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
        EnumSet<Modifier> mod = EnumSet.noneOf(Modifier.class);
        mod.addAll(modifiers);
        this.modifiers = mod;
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
