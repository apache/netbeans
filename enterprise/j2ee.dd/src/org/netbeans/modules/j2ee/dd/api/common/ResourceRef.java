/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.dd.api.common;

/**
 * Generated interface for ResourceRef element.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */
public interface ResourceRef extends CommonDDBean, DescriptionInterface {
        public static final String RES_REF_NAME = "ResRefName";	// NOI18N
	public static final String RES_TYPE = "ResType";	// NOI18N
	public static final String RES_AUTH = "ResAuth";	// NOI18N
	public static final String RES_SHARING_SCOPE = "ResSharingScope";	// NOI18N
        public static final String RES_AUTH_APPLICATION = "Application"; // NOI18N
        public static final String RES_AUTH_CONTAINER = "Container"; // NOI18N
        public static final String RES_SHARING_SCOPE_SHAREABLE = "Shareable"; // NOI18N
        public static final String RES_SHARING_SCOPE_UNSHAREABLE = "Unshareable"; // NOI18N
        
        /** Setter for res-ref-name property 
         * @param value property value
         */
	public void setResRefName(java.lang.String value);
        /** Getter for res-ref-name property.
         * @return property value 
         */
	public java.lang.String getResRefName();
        /** Setter for res-type property 
         * @param value property value
         */
	public void setResType(java.lang.String value);
        /** Getter for res-type property.
         * @return property value 
         */
	public java.lang.String getResType();
        /** Setter for res-auth property 
         * @param value property value
         */
	public void setResAuth(java.lang.String value);
        /** Getter for res-auth property.
         * @return property value 
         */
	public java.lang.String getResAuth();
        /** Setter for res-sharing-scope property 
         * @param value property value
         */
	public void setResSharingScope(java.lang.String value);
        /** Getter for res-sharing-scope property.
         * @return property value 
         */
	public java.lang.String getResSharingScope();

        // Java EE 5

        void setMappedName(String value) throws VersionNotSupportedException;
        String getMappedName() throws VersionNotSupportedException;
        void setInjectionTarget(int index, InjectionTarget valueInterface) throws VersionNotSupportedException;
        InjectionTarget getInjectionTarget(int index) throws VersionNotSupportedException;
        int sizeInjectionTarget() throws VersionNotSupportedException;
        void setInjectionTarget(InjectionTarget[] value) throws VersionNotSupportedException;
        InjectionTarget[] getInjectionTarget() throws VersionNotSupportedException;
        int addInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
        int removeInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
        InjectionTarget newInjectionTarget() throws VersionNotSupportedException;

}
