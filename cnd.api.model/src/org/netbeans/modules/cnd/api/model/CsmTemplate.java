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

import java.util.List;

/**
 * Represents function or class template.
 *
 * Class template should be represesented by an instance,
 * which implements both CsmClass and CsmTemplate interfaces;
 *
 * Function template should be implemented by an instance,
 * which implements both CsmFunction and CsmTemplate interfaces;
 *
 */
public interface CsmTemplate extends CsmObject {
    /**
     * Returns true if this declaration is template, otherwise false.
     */
    boolean isTemplate();

    boolean isSpecialization();

    boolean isExplicitSpecialization();

    List<CsmTemplateParameter> getTemplateParameters();

// This method is never used.
//    /**
//     * Gets a string that acts like a signature for function:
//     * a name followed by comma-separater list of parameter types
//     */
//    String getTemplateSignature(); 
    
    /*
     * Returns the name including template specialization part
     */
    CharSequence getDisplayName();
}
