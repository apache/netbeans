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
 * Ability to find an instance of CommonDDBean class nested inside this bean.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface FindCapability {

    /**
     * Method is looking for the nested DD element according to the specified property and value
     *
     * @param beanName e.g. "Servlet" or "ResourceRef"
     * @param propertyName e.g. "ServletName" or ResourceRefName"
     * @param value specific propertyName value e.g. "ControllerServlet" or "jdbc/EmployeeAppDb"
     * @return Bean satisfying the parameter values or null if not found
     */    
    public CommonDDBean findBeanByName(String beanName, String propertyName, String value);
}
