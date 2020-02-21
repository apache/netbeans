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

import java.util.Collection;

/**
 * Represents class, struct, enum and union
 *
 * TODO: think over how to represent built-in types
 */
public interface CsmClass extends CsmCompoundClassifier {

//    public static class Kind extends TypeSafeEnum {
//
//        public Kind(String value) {
//            super(value);
//        }
//
//        public static final Kind CLASS = new Kind("class");
//        public static final Kind UNION = new Kind("union");
//        public static final Kind STRUCT = new Kind("struct");
//        
//    }
//    
//    CsmClass.Kind  getClassKind();
    
    /**
     * Returns immutable collection of this class' members
     * Members migt be:
     *	fields
     *	methods
     *	nested classes
     *	enumerations
     *	bit fields
     *	friends (?)
     *	typedefs
     *
     * TODO: collection of WHAT?
     */
    Collection<CsmMember> getMembers();
    
    Collection<CsmFriend> getFriends();

    /** Returns the list of base classes */
    Collection<CsmInheritance> getBaseClasses();   
    
    /**
     * Gets the offset of the class' open curly bracket.
     */
    int getLeftBracketOffset();
    
}
