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

package org.netbeans.modules.cnd.api.model.xref;

import org.netbeans.modules.cnd.api.model.*;

/**
 * reference object in file
 * i.e.
 * A* B::foo() {
 * }
 * => there are 3 reference objects:
 * 1) "A" pointed to class A
 * 2) "B" pointed to class B
 * 3) "foo" pointed to declaration of method foo in class B
 *
 * reference object could have owner. 
 * Owner reference is the connection between model objects and references.
 * Could be used for instance for searching the scope of reference.
 * in the example above:
 * - reference "1" has as owner return type of method definition
 * - reference "2" has owner method definition
 * - reference "3" has owner method definition as well
 *
 *TODOD: think about example
 * #define MACRO(x) #x
 * #include MACRO(file.h)
 * what are the references and owners?
 *
 */
public interface CsmReference extends CsmOffsetable {
    /**
     * do not use this method, use CsmReferenceResolver.isKindOf instead
     * @return kind of object
     */
    CsmReferenceKind getKind();
    
    /**
     * returns referenced object
     * this could be long operation of resolving, do not call in EQ
     */
    CsmObject getReferencedObject();
    
    CsmObject getOwner();
    
    /**
     * return the closest top level container of this reference
     * @return object which is the closest top level container of this reference
     */
    CsmObject getClosestTopLevelObject();
}
