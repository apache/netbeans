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
package org.netbeans.modules.web.beans.api.model;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;


/**
 * @author ads
 *
 */
public interface InterceptorsResult extends Result, BeansResult {

    /**
     * Subject element accessor .
     * @return element which is used for interceptor resolution
     */
    Element getElement();
    
    /**
     * Returns interceptors which have @Interceptor annotation and 
     * meets the interceptor resolution requirements. 
     * @return result of interceptor resolution.
     */
    List<TypeElement> getResolvedInterceptors();
    
    /**
     * Interceptors could be assigned via @Interceptors annotation.
     * @return explicitly declared interceptors
     */
    List<TypeElement> getDeclaredInterceptors();
    
    /**
     * The result is union of resolved and declared interceptors.  
     * @return all available interceptors
     */
    List<TypeElement> getAllInterceptors();
    
}
