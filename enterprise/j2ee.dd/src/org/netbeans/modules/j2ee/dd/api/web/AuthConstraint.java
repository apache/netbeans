

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


package org.netbeans.modules.j2ee.dd.api.web;
import org.netbeans.modules.j2ee.dd.api.common.*;
/**
 * Generated interface for AuthConstraint element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface AuthConstraint extends CommonDDBean, DescriptionInterface {
        /** Setter for role-name property.
         * @param index index of role-name element
         * @param value value for role-name element
         */
	public void setRoleName(int index, java.lang.String value);
        /** Getter for role-name property.
         * @param index index of role-name element
         * @return role-name element value 
         */
	public java.lang.String getRoleName(int index);
        /** Setter for role-name property.
         * @param value array of role-name values
         */
	public void setRoleName(java.lang.String[] value);
        /** Getter for role-name property.
         * @return string array of role-name elements 
         */
	public java.lang.String[] getRoleName();
        /** Returns size of role-name elements.
         * @return size of role-names 
         */
	public int sizeRoleName();
        /** Adds role-name element.
         * @param value value for role-name element
         * @return index of new role-name
         */
	public int addRoleName(java.lang.String value);
        /** Removes role-name element.
         * @param value role-name to be removed
         * @return index of the removed role-name
         */
	public int removeRoleName(java.lang.String value);

}
