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

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;


/**
 * Common interface for results that contains elements with stereotypes.  
 * 
 * @author ads
 *
 */
public interface Result {
    
    /**
     * Return list of all element's stereotypes ( including recursively
     * inherited ).    
     * @param element element with stereotypes  
     * @return list of element's stereotypes  
     */
    List<AnnotationMirror> getAllStereotypes( Element element );
    
    List<AnnotationMirror> getStereotypes( Element element );

}
