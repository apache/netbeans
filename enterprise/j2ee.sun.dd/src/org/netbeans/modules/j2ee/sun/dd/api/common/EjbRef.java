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
/*
 * EjbRef.java
 *
 * Created on November 17, 2004, 5:05 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface EjbRef extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String EJB_REF_NAME = "EjbRefName";	// NOI18N
    public static final String JNDI_NAME = "JndiName";	// NOI18N

    /** Setter for ejb-ref-name property.
     * @param value property value
     */
    public void setEjbRefName(java.lang.String value);
    /** Getter for ejb-ref-name property.
     * @return property value
     */
    public java.lang.String getEjbRefName();
    /** Setter for jndi-name property.
     * @param value property value
     */
    public void setJndiName(java.lang.String value);
    /** Getter for jndi-name property.
     * @return property value
     */
    public java.lang.String getJndiName();
    
}
