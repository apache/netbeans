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
 * Represents a "logical" namespace - not a particular namespace declaration,
 * but a join of all namespace declarations, which have thje given name
 * (see interface CsmNamespaceDeclaration)
 *
 */
public interface CsmNamespace extends CsmQualifiedNamedElement, CsmScope {

    CsmNamespace getParent();
    
    Collection<CsmNamespace> getNestedNamespaces();
    
    Collection<CsmNamespace> getInlinedNamespaces();

    /** Gets top-level objects */
    //TODO: what is the common ancestor for the namespace objects?
    Collection<CsmOffsetableDeclaration> getDeclarations();

    /**
     * Gets all definitions for this namespace
     */
    Collection<CsmNamespaceDefinition> getDefinitions();
    
    //TODO: think over the relationship between projects and namespaces
    ///** Gets the project, to which this namespace belong */
    //CsmProject getProject();
    
    /** returns true if this is default namespace */
    boolean isGlobal();
    
    /**
     * returns true if it is inline namespace (C++11)
     */
    boolean isInline();
    
    /** the project where the namespace (or it's particular part) is defined */
    CsmProject getProject();
}
