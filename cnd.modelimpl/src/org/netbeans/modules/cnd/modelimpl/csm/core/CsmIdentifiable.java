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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.*;

/**
 * intefrace to present object that has unique ID
 * unique ID is used to long-time stored references on Csm Objects
 * class which implements CsmIdentifiable must return CsmUID<T> where T is
 * the same as implementation class or one of it's super types
 * @see CsmUID
 */
public interface CsmIdentifiable extends CsmObject {
    
    /**
     * gets unique identifier associated with object to store reference
     */
    CsmUID<?> getUID();
}
