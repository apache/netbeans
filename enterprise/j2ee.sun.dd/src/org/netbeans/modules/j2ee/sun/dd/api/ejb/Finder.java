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
 * Finder.java
 *
 * Created on November 18, 2004, 11:59 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface Finder extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

     public static final String METHOD_NAME = "MethodName";	// NOI18N
     public static final String QUERY_PARAMS = "QueryParams";	// NOI18N
     public static final String QUERY_FILTER = "QueryFilter";	// NOI18N
     public static final String QUERY_VARIABLES = "QueryVariables";	// NOI18N
     public static final String QUERY_ORDERING = "QueryOrdering";	// NOI18N
        
    /** Setter for method-name property
     * @param value property value
     */
    public void setMethodName(java.lang.String value);
    /** Getter for method-name property.
     * @return property value
     */
    public java.lang.String getMethodName();
    /** Setter for query-params property
     * @param value property value
     */
    public void setQueryParams(java.lang.String value);
    /** Getter for query-params property.
     * @return property value
     */
    public java.lang.String getQueryParams();
    /** Setter for query-filter property
     * @param value property value
     */
    public void setQueryFilter(java.lang.String value);
    /** Getter for query-filter property.
     * @return property value
     */
    public java.lang.String getQueryFilter();
    /** Setter for query-variables property
     * @param value property value
     */
    public void setQueryVariables(java.lang.String value);
    /** Getter for query-variables property.
     * @return property value
     */
    public java.lang.String getQueryVariables();
    /** Setter for query-ordering property
     * @param value property value
     */
    public void setQueryOrdering(java.lang.String value);
    /** Getter for query-ordering property.
     * @return property value
     */
    public java.lang.String getQueryOrdering();
    
}
