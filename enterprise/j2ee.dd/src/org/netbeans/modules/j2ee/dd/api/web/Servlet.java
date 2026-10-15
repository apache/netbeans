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

package org.netbeans.modules.j2ee.dd.api.web;
/**
 * Generated interface for Servlet element.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */

public interface Servlet extends org.netbeans.modules.j2ee.dd.api.common.ComponentInterface {
        /** Setter for servlet-name property.
         * @param value property value
         */
	public void setServletName(java.lang.String value);
        /** Getter for servlet-name property.
         * @return property value 
         */
	public java.lang.String getServletName();
        /** Setter for servlet-class property.
         * @param value property value
         */
	public void setServletClass(java.lang.String value);
        /** Getter for servlet-class property.
         * @return property value 
         */
	public java.lang.String getServletClass();
        /** Setter for jsp-file property.
         * @param value property value
         */
	public void setJspFile(java.lang.String value);
        /** Getter for jsp-file property.
         * @return property value 
         */
	public java.lang.String getJspFile();
        /** Setter for init-param element.
         * @param index position in the array of elements
         * @param valueInterface init-param element (InitParam object)
         */
	public void setInitParam(int index, org.netbeans.modules.j2ee.dd.api.common.InitParam valueInterface);
        /** Getter for init-param element.
         * @param index position in the array of elements
         * @return init-param element (InitParam object)
         */
	public org.netbeans.modules.j2ee.dd.api.common.InitParam getInitParam(int index);
        /** Setter for init-param elements.
         * @param value array of init-param elements (InitParam objects)
         */
	public void setInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam[] value);
        /** Getter for init-param elements.
         * @return array of init-param elements (InitParam objects)
         */
	public org.netbeans.modules.j2ee.dd.api.common.InitParam[] getInitParam();
        /** Returns size of init-param elements.
         * @return number of init-param elements 
         */
	public int sizeInitParam();
        /** Adds init-param element.
         * @param valueInterface init-param element (InitParam object)
         * @return index of new init-param
         */
	public int addInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam valueInterface);
        /** Removes init-param element.
         * @param valueInterface init-param element (InitParam object)
         * @return index of the removed init-param
         */
	public int removeInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam valueInterface);
        /** Setter for load-on-startup property.
         * @param value property value
         */
	public void setLoadOnStartup(java.math.BigInteger value);
        /** Getter for load-on-startup property.
         * @return property value 
         */
	public java.math.BigInteger getLoadOnStartup();
        /** Setter for run-as element.
         * @param valueInterface run-as element (RunAs object)
         */
	public void setRunAs(org.netbeans.modules.j2ee.dd.api.common.RunAs valueInterface);
        /** Getter for run-as element.
         * @return run-as element (RunAs object)
         */
	public org.netbeans.modules.j2ee.dd.api.common.RunAs getRunAs();
        /** Setter for security-role-ref element.
         * @param index position in the array of elements
         * @param valueInterface security-role-ref element (SecurityRoleRef object)
         */
	public void setSecurityRoleRef(int index, org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef valueInterface);
        /** Getter for security-role-ref  element.
         * @param index position in the array of elements
         * @return security-role-ref element (SecurityRoleRef object)
         */
	public org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef getSecurityRoleRef(int index);
        /** Setter for security-role-ref elements.
         * @param value array of security-role-ref elements (SecurityRoleRef objects)
         */
	public void setSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef[] value);
        /** Getter for security-role-ref elements.
         * @return array of security-role-ref  elements (SecurityRoleRef objects)
         */
	public org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef[] getSecurityRoleRef();
        /** Returns size of security-role-ref elements.
         * @return number of security-role-ref elements 
         */
	public int sizeSecurityRoleRef();
        /** Adds security-role-ref element.
         * @param valueInterface security-role-ref element (SecurityRoleRef object)
         * @return index of new security-role-ref
         */
	public int addSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef valueInterface);
        /** Removes security-role-ref element.
         * @param valueInterface security-role-ref element (SecurityRoleRef object)
         * @return index of the removed security-role-ref
         */
	public int removeSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef valueInterface);

}
