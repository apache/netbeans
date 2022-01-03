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

package org.netbeans.modules.cnd.api.model;

import java.util.Map;

/**
 * instantiation information 
 * i.e. A<int, double> aa;
 * aa has type A<int, double> 
 * this CsmType has getClassifier method which 
 * returns classifier as CsmInstantiation object.
 * getTemplateDeclaration() will be original template declaration
 * of template class A and getInstantiationType
 * will be CsmType presenting A<int, double> 
 */
public interface CsmInstantiation extends CsmObject {
    /**
     * returns template declaration which was instantiated 
     * i.e. template class A for A<int, double> aa;
     * @return
     */
    CsmOffsetableDeclaration getTemplateDeclaration();
    
    /**
     * returns mapping of template parameters to the values
     * @return
     */
    Map<CsmTemplateParameter, CsmSpecializationParameter> getMapping();
}
