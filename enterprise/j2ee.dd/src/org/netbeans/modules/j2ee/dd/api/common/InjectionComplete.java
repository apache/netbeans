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

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Generated interface for InjectionComplete element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @since javaee5
 */
public interface InjectionComplete extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
    
        /** Setter for injection-complete-class property.
         * @param value property value
         */
	public void setInjectionCompleteClass (java.lang.String value);
        
        /** Getter for injection-complete-class property.
         * @return property value 
         */
	public java.lang.String getInjectionCompleteClass();

        /** Setter for injection-complete-method property.
         * @param value property value
         */
	public void setInjectionCompleteMethod (java.lang.String value);
        
        /** Getter for injection-complete-method property.
         * @return property value 
         */
	public java.lang.String getInjectionCompleteMethod();

}
