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
 * Cmp.java
 *
 * Created on November 17, 2004, 5:11 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface Cmp extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String MAPPING_PROPERTIES = "MappingProperties";	// NOI18N
    public static final String IS_ONE_ONE_CMP = "IsOneOneCmp";	// NOI18N
    public static final String ONE_ONE_FINDERS = "OneOneFinders";	// NOI18N
    public static final String PREFETCH_DISABLED = "PrefetchDisabled";	// NOI18N
    
    /** Setter for mapping-properties property
     * @param value property value
     */
    public void setMappingProperties(java.lang.String value);
    /** Getter for mapping-properties property.
     * @return property value
     */
    public java.lang.String getMappingProperties();
    /** Setter for is-one-one-cmp property
     * @param value property value
     */
    public void setIsOneOneCmp(java.lang.String value);
    /** Getter for is-one-one-cmp property.
     * @return property value
     */
    public java.lang.String getIsOneOneCmp();
    /** Setter for one-one-finders property
     * @param value property value
     */
    public void setOneOneFinders(OneOneFinders value);
    /** Getter for one-one-finders property.
     * @return property value
     */
    public OneOneFinders getOneOneFinders(); 
     
    public OneOneFinders newOneOneFinders();
    
    //AppServer 8.1
    /** Setter for prefetch-disabled property
     * @param value property value
     */ 
    public void setPrefetchDisabled (PrefetchDisabled  value) throws VersionNotSupportedException; 
    /** Getter for prefetch-disabled property.
     * @return property value
     */
    public PrefetchDisabled getPrefetchDisabled () throws VersionNotSupportedException;  
    
    public PrefetchDisabled newPrefetchDisabled () throws VersionNotSupportedException;  
}
