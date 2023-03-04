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
 * PortTypeOperationCustomization.java
 *
 * Created on January 31, 2006, 5:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.customization.model;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author Roderico Cruz
 */
public interface PortTypeOperationCustomization extends ExtensibilityElement{
    public static final String JAVA_METHOD_PROPERTY = "method";
    public static final String ENABLE_WRAPPER_STYLE_PROPERTY = "enableWrapperStyle";
    public static final String ENABLE_ASYNC_MAPPING_PROPERTY = "enableAsyncMapping";
    public static final String JAVA_PARAMETER_PROPERTY = "parameter";
    
    void setJavaMethod(JavaMethod method);
    void removeJavaMethod(JavaMethod method);
    JavaMethod getJavaMethod();
    
    void setEnableWrapperStyle(EnableWrapperStyle wrapperStyle);
    void removeEnableWrapperStyle(EnableWrapperStyle wrapperStyle);
    EnableWrapperStyle getEnableWrapperStyle();
    
    void setEnableAsyncMapping(EnableAsyncMapping async);
    void removeEnableAsyncMapping(EnableAsyncMapping async);
    EnableAsyncMapping getEnableAsyncMapping();
    
    void addJavaParameter(JavaParameter parameter);
    void removeJavaParameter(JavaParameter parameter);
    Collection<JavaParameter> getJavaParameters();
    
    
}
