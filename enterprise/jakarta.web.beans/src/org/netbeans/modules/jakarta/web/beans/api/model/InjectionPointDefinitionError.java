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
package org.netbeans.modules.jakarta.web.beans.api.model;

import javax.lang.model.element.Element;


/**
 * This exception could be thrown when injection point deifinition 
 * contains error. 
 * @author ads
 *
 */
public class InjectionPointDefinitionError extends CdiException {

    private static final long serialVersionUID = -6893993336079352757L;

    public InjectionPointDefinitionError(Element errorElement, String msg) {
        super( msg );
        myElement = errorElement;
    }
    
    /**
     * There could be errors detected when element is checked as injection point.
     * In most such cases possible injection point is the error element.
     * But in some cases error could be detected on enclosing element.
     * F.e. method could be wrongly defined . In this case its parameters
     * cannot be considered as correct injection points.     
     * 
     * @return element that is wrongly defined
     */
    public Element getErrorElement(){
        return myElement;
    }
    
    private Element myElement;
}
