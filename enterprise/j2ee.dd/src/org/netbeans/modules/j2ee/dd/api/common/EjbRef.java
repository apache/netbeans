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

//
// This interface has all of the bean info accessor methods.
//

/**
 * Generated interface for EjbRef element.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */
public interface EjbRef extends CommonDDBean, DescriptionInterface {

        public static final String EJB_REF_NAME = "EjbRefName";	// NOI18N
	public static final String EJB_REF_TYPE = "EjbRefType";	// NOI18N
	public static final String HOME = "Home";	// NOI18N
	public static final String REMOTE = "Remote";	// NOI18N
	public static final String EJB_LINK = "EjbLink";	// NOI18N
        public static final String EJB_REF_TYPE_ENTITY = "Entity"; // NOI18N
        public static final String EJB_REF_TYPE_SESSION = "Session"; // NOI18N
        /** Setter for ejb-ref-name property.
         * @param value property value
         */
	public void setEjbRefName(String value);
        /** Getter for ejb-ref-name property.
         * @return property value 
         */
	public String getEjbRefName();
        /** Setter for ejb-ref-type property.
         * @param value property value
         */
	public void setEjbRefType(String value);
        /** Getter for ejb-ref-type property.
         * @return property value 
         */
	public String getEjbRefType();
        /** Setter for home property.
         * @param value property value
         */
	public void setHome(String value);
        /** Getter for home property.
         * @return property value 
         */
	public String getHome();
        /** Setter for remote property.
         * @param value property value
         */
	public void setRemote(String value);
        /** Getter for remote property.
         * @return property value 
         */
	public String getRemote();
        /** Setter for ejb-link property.
         * @param value property value
         */
	public void setEjbLink(String value);
        /** Getter for ejb-link property.
         * @return property value 
         */
	public String getEjbLink();

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

