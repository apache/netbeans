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

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Merged model for beans.xml files.
 * @author ads
 *
 */
public interface BeansModel {

    /**
     * @return all interceptor classes FQNs found in beans.xml files 
     */
    LinkedHashSet<String> getInterceptorClasses();
    
    /**
     * @return all decorator classes FQNs found in beans.xml files
     */
    LinkedHashSet<String> getDecoratorClasses();
    
    /**
     * @return all alternative classes FQNs found in beans.xml files 
     */
    Set<String> getAlternativeClasses();
    
    /**
     * @return all alternative stereotypes FQNs found in beans.xml files
     */
    Set<String> getAlternativeStereotypes();

    /**
     * Gets information about the Bean Archive type of the project.
     * Introduced by CDI 1.1 with implicit bean archive.
     * @return bean archive type, never {@code null}
     */
    BeanArchiveType getBeanArchiveType();

    boolean isCdi11OrLater();

}
