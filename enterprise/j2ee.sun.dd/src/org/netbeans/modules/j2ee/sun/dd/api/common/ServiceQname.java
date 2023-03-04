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
 * ServiceQname.java
 *
 * Created on November 18, 2004, 10:29 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface ServiceQname extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String NAMESPACEURI = "NamespaceURI";	// NOI18N
    public static final String LOCALPART = "Localpart";	// NOI18N

    /** Setter for namespaceURI property
     * @param value property value
     */
    public void setNamespaceURI(java.lang.String value);
    /** Getter for namespaceURI property.
     * @return property value
     */
    public java.lang.String getNamespaceURI();
    /** Setter for localpart property
     * @param value property value
     */
    public void setLocalpart(java.lang.String value);
    /** Getter for localpart property.
     * @return property value
     */
    public java.lang.String getLocalpart();
}
