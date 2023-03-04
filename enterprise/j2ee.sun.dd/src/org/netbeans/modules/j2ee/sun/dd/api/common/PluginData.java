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
 * PluginData.java
 *
 * Created on November 18, 2004, 12:20 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PluginData extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String AUTO_GENERATE_SQL = "AutoGenerateSql";	// NOI18N
    public static final String CLIENT_JAR_PATH = "ClientJarPath";	// NOI18N
    public static final String CLIENT_ARGS = "ClientArgs";	// NOI18N


    /** Setter for auto-generate-sql property
     * @param value property value
     */
    public void setAutoGenerateSql(java.lang.String value);
    /** Getter for auto-generate-sql property.
     * @return property value
     */
    public java.lang.String getAutoGenerateSql();
    /** Setter for client-jar-path property
     * @param value property value
     */
    public void setClientJarPath(java.lang.String value);
    /** Getter for client-jar-path property.
     * @return property value
     */
    public java.lang.String getClientJarPath();
    /** Setter for client-args property
     * @param value property value
     */
    public void setClientArgs(java.lang.String value);
    /** Getter for client-args property.
     * @return property value
     */
    public java.lang.String getClientArgs();
    
}
