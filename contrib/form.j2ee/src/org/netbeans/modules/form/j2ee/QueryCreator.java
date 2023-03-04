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
package org.netbeans.modules.form.j2ee;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.form.CreationDescriptor;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.codestructure.CodeExpression;
import org.netbeans.modules.form.codestructure.CodeExpressionOrigin;

/**
 * Creator for query obtained from entity manager.
 *
 * @author Jan Stola
 */
class QueryCreator implements CreationDescriptor.Creator {
    /** Parameter types. */
    private final Class[] paramTypes = new Class[] {Object.class/*EntityManager.class*/, String.class, int.class, int.class};
    /** Exception types. */
    private final Class[] exTypes = new Class[0];
    /** Property names. */
    private final String[] propertyNames = new String[] {"entityManager", "query", "firstResult", "maxResults"}; // NOI18N
    
    /**
     * Returns number of parameters of the creator.
     *
     * @return number of parameters of the creator.
     */
    @Override
    public int getParameterCount() {
        return 4;
    }
    
    /**
     * Returns parameter types of the creator.
     *
     * @return parameter types of the creator.
     */
    @Override
    public Class[] getParameterTypes() {
        return paramTypes;
    }
    
    /**
     * Returns exception types of the creator.
     *
     * @return exception types of the creator.
     */
    @Override
    public Class[] getExceptionTypes() {
        return exTypes;
    }
    
    /**
     * Returns property names of the creator.
     *
     * @return property names of the creator.
     */
    @Override
    public String[] getPropertyNames() {
        return propertyNames;
    }
    
    /**
     * Creates instance according to given properties.
     *
     * @param props properties describing the instance to create.
     * @return instance that reflects values of the given properties.
     */
    @Override
    public Object createInstance(FormProperty[] props) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new Object(); // Hack
    }

    /**
     * Creates instance according to given parameter values.
     *
     * @param paramValues parameter values describing the instance to create.
     * @return instance that reflects values of the given parameters.
     */
    @Override
    public Object createInstance(Object[] paramValues) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new Object(); // Hack
    }
    
    /**
     * Returns creation code according to given properties.
     *
     * @param props properties describing the instance whose creation code should be returned.
     * @param propNames not used
     * @param propCodes not used
     * @param expressionType type of the expression to create.
     * @return creation code that reflects values of the given properties.
     */
    @Override
    public String getJavaCreationCode(FormProperty[] props, String[] propNames, String[] propCodes, Class expressionType, String genericTypes) {
        assert (props.length == 4);
        
        String entityManager = null;
        String query = null;
        String firstResult = null;
        String maxResults = null;
        for (int i=0; i<props.length; i++) {
            String propName = props[i].getName();
            if (propertyNames[0].equals(propName)) {
                entityManager = props[i].getJavaInitializationString();
            } else if (propertyNames[1].equals(propName)) {
                query = props[i].getJavaInitializationString();
            } else if (propertyNames[2].equals(propName)) {
                if (props[i].isChanged()) {
                    firstResult = props[i].getJavaInitializationString();
                }
            } else if (propertyNames[3].equals(propName)) {
                if (props[i].isChanged()) {
                    maxResults = props[i].getJavaInitializationString();
                }
            } else {
                assert false;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("java.beans.Beans.isDesignTime() ? null : "); // NOI18N
        if ("null".equals("" + entityManager)) { // NOI18N
            sb.append("((javax.persistence.EntityManager)null)");  // NOI18N
        } else {
            sb.append(entityManager);
        }
        sb.append(".createQuery(").append(query).append(')'); // NOI18N
        if (firstResult != null) {
            sb.append(".setFirstResult(").append(firstResult).append(')'); // NOI18N
        }
        if (maxResults != null) {
            sb.append(".setMaxResults(").append(maxResults).append(')'); // NOI18N
        }
        return sb.toString();
    }
    
    @Override
    public CodeExpressionOrigin getCodeOrigin(CodeExpression[] params) {
        return null; // PENDING how is this used?
    }
    
}
