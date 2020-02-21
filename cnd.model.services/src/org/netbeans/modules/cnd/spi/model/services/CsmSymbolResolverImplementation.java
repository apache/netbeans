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

package org.netbeans.modules.cnd.spi.model.services;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;

/**
 *
 */
public interface CsmSymbolResolverImplementation {
    
    /**
     * Resolves symbol by qualified name or 
     * signature for functions and methods.
     * 
     * Examples: 
     * 1) String "AAA<int>::BBB" will be resolved into symbol BBB 
     *    inside class AAA<int>
     * 2) String "aaa::foo(int)" is a signature and will be resolved into 
     *    function or method 'foo' which takes one integer parameter and 
     *    declared inside namespace or class 'aaa'
     * 3) String "int aaa::foo(int)" is a signature of a template 
     *    function or method 'foo' which takes one integer parameter, 
     *    declared inside namespace or class 'aaa' and returns integer
     * 
     * @param project
     * @param declText
     * 
     * @return all entities which have the same declaration text
     */    
    Collection<CsmOffsetable> resolveSymbol(NativeProject project, CharSequence declText);    

    /**
     * Resolves symbol by qualified name or 
     * signature for functions and methods.
     * 
     * @param project
     * @param declText
     * 
     * @return all entities which have the same declaration text
     */    
    Collection<CsmOffsetable> resolveSymbol(CsmProject project, CharSequence declText);        

    /**
     * Resolves function by name in global namespace.
     * Does not wait until project parse is finished.
     * 
     * @param project
     * @param functionName
     * 
     * @return all function definitions with name
     */    
    Collection<CsmOffsetable> resolveGlobalFunction(NativeProject project, CharSequence functionName);
}
