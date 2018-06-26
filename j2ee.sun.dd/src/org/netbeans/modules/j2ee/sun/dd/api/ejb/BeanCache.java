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
