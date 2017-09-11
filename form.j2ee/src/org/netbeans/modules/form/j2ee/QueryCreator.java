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
