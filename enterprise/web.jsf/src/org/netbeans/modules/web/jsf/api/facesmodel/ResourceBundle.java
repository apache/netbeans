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

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 *
 * @author Petr Pisl, ads
 */
public interface ResourceBundle extends ApplicationElement, DescriptionGroup, 
    IdentifiableElement 
{
    /**
     * Property name of &lt;base-name&gt; element.
     * The fully qualified class name of the
     * java.util.ResourceBundle instance.
     */ 
    public static final String BASE_NAME = JSFConfigQNames.BASE_NAME.getLocalName();
    /**
     * Property name of &lt;var&gt; element.
     * The name by which this ResourceBundle instance is retrieved by a call to
     * Application.getResourceBundle().
     */ 
    public static final String VAR = JSFConfigQNames.VAR.getLocalName();
    
    String getBaseName();
    void setBaseName(String baseName);
    
    String getVar();
    void setVar(String var);
}
