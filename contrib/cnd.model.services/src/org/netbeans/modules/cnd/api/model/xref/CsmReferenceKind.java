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

import java.util.EnumSet;
import java.util.Set;

/**
 *
 */
public enum CsmReferenceKind {
    DEFINITION,
    DECLARATION,        
    DIRECT_USAGE, // references like "var" in "var" or "var.foo" or "foo->var" are interested
    IN_PREPROCESSOR_DIRECTIVE, // references in #preprocessor directives are interested
    IN_DEAD_BLOCK, // references in dead code are interested
    COMMENT, // references in comments
    UNKNOWN;

    /**
     * declaration of definition of functions
     */
    public static final Set<CsmReferenceKind> FUNCTION_DECLARATION_KINDS = EnumSet.of(CsmReferenceKind.DECLARATION, CsmReferenceKind.DEFINITION);
    
    /**
     * all references in active code
     */
    public static final Set<CsmReferenceKind> ANY_REFERENCE_IN_ACTIVE_CODE;

    /**
     * all references in active code and live preprocessor directives
     */
    public static final Set<CsmReferenceKind> ANY_REFERENCE_IN_ACTIVE_CODE_AND_PREPROCESSOR;

    /**
     * all references
     */
    public static final Set<CsmReferenceKind> ALL;
    
    static {
        ANY_REFERENCE_IN_ACTIVE_CODE = EnumSet.range(DEFINITION, DIRECT_USAGE);
        ANY_REFERENCE_IN_ACTIVE_CODE_AND_PREPROCESSOR = EnumSet.range(DEFINITION, IN_PREPROCESSOR_DIRECTIVE);
        ALL = EnumSet.range(DEFINITION, IN_DEAD_BLOCK);
    }
}
