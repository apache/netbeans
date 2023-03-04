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
 * CustomizationElements.java
 *
 * Created on February 6, 2006, 10:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

/**
 *
 * @author Roderico Cruz
 */
public enum CustomizationElements {
    BINDINGS("bindings"),
    PACKAGE("package"),
    CLASS("class"),
    ENABLEWRAPPERSTYLE("enableWrapperStyle"),
    ENABLEASYNCMAPPING("enableAsyncMapping"),
    ENABLEMIMECONTENT("enableMIMEContent"),
    JAVAEXCEPTION("exception"),
    METHOD("method"),
    PARAMETER("parameter"),
    JAVADOC("javadoc"),
    PROVIDER("provider");
    
    CustomizationElements(String docName) {
        this.docName = docName;
    }
    
    public String getName() {
        return docName;
    }
    
    
    private final String docName;
} 
