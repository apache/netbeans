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
 * BeanCache.java
 *
 * Created on November 17, 2004, 5:19 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface BeanCache extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String MAX_CACHE_SIZE = "MaxCacheSize";	// NOI18N
    public static final String RESIZE_QUANTITY = "ResizeQuantity";	// NOI18N
    public static final String IS_CACHE_OVERFLOW_ALLOWED = "IsCacheOverflowAllowed";	// NOI18N
    public static final String CACHE_IDLE_TIMEOUT_IN_SECONDS = "CacheIdleTimeoutInSeconds";	// NOI18N
    public static final String REMOVAL_TIMEOUT_IN_SECONDS = "RemovalTimeoutInSeconds";	// NOI18N
    public static final String VICTIM_SELECTION_POLICY = "VictimSelectionPolicy";	// NOI18N
        
    /** Setter for max-cache-size property
     * @param value property value
     */
    public void setMaxCacheSize(java.lang.String value);
    /** Getter for max-cache-size property.
     * @return property value
     */
    public java.lang.String getMaxCacheSize();
    
    /** Setter for resize-quantity property
     * @param value property value
     */
    public void setResizeQuantity(java.lang.String value);
    /** Getter for resize-quantity property.
     * @return property value
     */
    public java.lang.String getResizeQuantity();
    
    /** Setter for is-cache-overflow-allowed property
     * @param value property value
     */
    public void setIsCacheOverflowAllowed(java.lang.String value);
    /** Getter for is-cache-overflow-allowed property.
     * @return property value
     */
    public java.lang.String getIsCacheOverflowAllowed();
    
    /** Setter for cache-idle-timeout-in-seconds property
     * @param value property value
     */
    public void setCacheIdleTimeoutInSeconds(java.lang.String value);
    /** Getter for cache-idle-timeout-in-seconds property.
     * @return property value
     */
    public java.lang.String getCacheIdleTimeoutInSeconds();
    
    
    /** Setter for removal-timeout-in-seconds property
     * @param value property value
     */
    public void setRemovalTimeoutInSeconds(java.lang.String value);
    /** Getter for removal-timeout-in-seconds property.
     * @return property value
     */
    public java.lang.String getRemovalTimeoutInSeconds();
    
    /** Setter for victim-selection-policy property
     * @param value property value
     */
    public void setVictimSelectionPolicy(java.lang.String value);
    /** Getter for victim-selection-policy property.
     * @return property value
     */
    public java.lang.String getVictimSelectionPolicy();    
    
}
