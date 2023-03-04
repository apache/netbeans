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
 * GenClasses.java
 *
 * Created on November 17, 2004, 5:18 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface GenClasses extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String REMOTE_IMPL = "RemoteImpl";	// NOI18N
    public static final String LOCAL_IMPL = "LocalImpl";	// NOI18N
    public static final String REMOTE_HOME_IMPL = "RemoteHomeImpl";	// NOI18N
    public static final String LOCAL_HOME_IMPL = "LocalHomeImpl";	// NOI18N

    /** Setter for remote-impl property
     * @param value property value
     */
    public void setRemoteImpl(java.lang.String value);
    /** Getter for remote-impl property.
     * @return property value
     */
    public java.lang.String getRemoteImpl();
    /** Setter for local-impl property
     * @param value property value
     */
    public void setLocalImpl(java.lang.String value);
    /** Getter for local-impl property.
     * @return property value
     */
    public java.lang.String getLocalImpl();
    /** Setter for remote-home-impl property
     * @param value property value
     */
    public void setRemoteHomeImpl(java.lang.String value);
    /** Getter for remote-home-impl property.
     * @return property value
     */
    public java.lang.String getRemoteHomeImpl();
    /** Setter for local-home-impl property
     * @param value property value
     */
    public void setLocalHomeImpl(java.lang.String value);
    /** Getter for local-home-impl property.
     * @return property value
     */
    public java.lang.String getLocalHomeImpl();
}
