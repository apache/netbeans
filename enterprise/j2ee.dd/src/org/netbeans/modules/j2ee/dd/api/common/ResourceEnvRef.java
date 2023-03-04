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
 * Generated interface for ResourceEnvRef element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface ResourceEnvRef extends CommonDDBean, DescriptionInterface {


    public static final String RESOURCE_ENV_REF_NAME = "ResourceEnvRefName"; // NOI18N
    public static final String RESOURCE_ENV_REF_TYPE = "ResourceEnvRefType"; // NOI18N

    /** Setter for resource-env-ref-name property 
     * @param value property value
     */
    public void setResourceEnvRefName(String value);
    /** Getter for resource-env-ref-name property.
     * @return property value 
     */
    public String getResourceEnvRefName();
    /** Setter for resource-env-ref-type property.
     * @param value property value
     */
    public void setResourceEnvRefType(String value);
    /** Getter for resource-env-ref-type property.
     * @return property value 
     */
    public String getResourceEnvRefType();

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
