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
 * Cache.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface Cache extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String MAXENTRIES = "MaxEntries";	// NOI18N
	public static final String TIMEOUTINSECONDS = "TimeoutInSeconds";	// NOI18N
	public static final String ENABLED = "Enabled";	// NOI18N
	public static final String CACHE_HELPER = "CacheHelper";	// NOI18N
	public static final String DEFAULT_HELPER = "DefaultHelper";	// NOI18N
	public static final String PROPERTY = "WebProperty";	// NOI18N
	public static final String CACHE_MAPPING = "CacheMapping";	// NOI18N
        
        /** Setter for max-entries attribute
         * @param value attribute value
         */
	public void setMaxEntries(java.lang.String value);
        /** Getter for max-entries attribute.
         * @return attribute value
         */
	public java.lang.String getMaxEntries();
        /** Setter for timeout-in-seconds attribute
         * @param value attribute value
         */
	public void setTimeoutInSeconds(java.lang.String value);
        /** Getter for timeout-in-seconds attribute.
         * @return attribute value
         */
	public java.lang.String getTimeoutInSeconds();
        /** Setter for enabled attribute
         * @param value attribute value
         */
	public void setEnabled(java.lang.String value);
        /** Getter for enabled property.
         * @return property value
         */
	public java.lang.String getEnabled();

	public void setCacheHelper(int index, CacheHelper value); 
	public CacheHelper getCacheHelper(int index);
	public int sizeCacheHelper();
	public void setCacheHelper(CacheHelper[] value);
	public CacheHelper[] getCacheHelper();
	public int addCacheHelper(CacheHelper value);
	public int removeCacheHelper(CacheHelper value);
	public CacheHelper newCacheHelper();

        /** Setter for default-helper property
         * @param value property value
         */
	public void setDefaultHelper(DefaultHelper value); 
        /** Getter for default-helper property.
         * @return property value
         */
	public DefaultHelper getDefaultHelper();

	public DefaultHelper newDefaultHelper(); 

	public void setWebProperty(int index, WebProperty value);
	public WebProperty getWebProperty(int index);
	public int sizeWebProperty();
	public void setWebProperty(WebProperty[] value);
	public WebProperty[] getWebProperty();
	public int addWebProperty(WebProperty value);
	public int removeWebProperty(WebProperty value);
	public WebProperty newWebProperty();

	public void setCacheMapping(int index, CacheMapping value);
	public CacheMapping getCacheMapping(int index);
	public int sizeCacheMapping();
	public void setCacheMapping(CacheMapping[] value);
	public CacheMapping[] getCacheMapping();
	public int addCacheMapping(CacheMapping value);
	public int removeCacheMapping(CacheMapping value);
	public CacheMapping newCacheMapping();

}
