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
package org.netbeans.modules.cnd.gotodeclaration.util;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 * Utility class with methods that returns context information
 */
public class ContextUtil {

    private ContextUtil() {
    }
    
    /**
     * Return the name of the context of the scope element - 
     * either namespace or (for file-level, such as C-style statics) file
     * (If the element is a nested class, return containing class' namespace)
     */
    public static String getContextName(CsmScopeElement element) {
	CsmScope scope = element.getScope();
	if( CsmKindUtilities.isClass(scope) ) {
	    CsmClass cls = ((CsmClass) scope);
	    CsmNamespace ns = getClassNamespace(cls);
	    return ns.getQualifiedName().toString();
	}
	else if( CsmKindUtilities.isNamespace(scope) ) {
	    return ((CsmNamespace) scope).getQualifiedName().toString();
	}
	else if( CsmKindUtilities.isFile(scope) ) {
	    return ((CsmFile) scope).getName().toString();
	}
	return "";
    }
    
    /**
     * Returns the namespace the given class belongs
     * (even if it's a nested class)
     */
    public static CsmNamespace getClassNamespace(CsmClass cls) {
	CsmScope scope = cls.getScope();
	while( scope != null && CsmKindUtilities.isClass(scope) ) {
	    CsmClass outer = (CsmClass)scope;
	    scope = outer.getScope();
	}
	return CsmKindUtilities.isNamespace(scope) ? (CsmNamespace) scope : null;
    }    
    
    /**
     * Returns the full name of the class:
     * for a top-level class it's just a class name,
     * for a nested class, it contain outer class name
     * (but in any case without a namespace)
     */
    public static String getClassFullName(CsmClass cls) {
	StringBuilder sb = new StringBuilder(cls.getName());
	CsmScope scope = cls.getScope();
	while( scope != null && CsmKindUtilities.isClass(scope) ) {
	    CsmClass outer = (CsmClass)scope;
	    sb.insert(0, "::"); // NOI18N
	    sb.insert(0, (outer).getName());
	    scope = outer.getScope();
	}
	    
        return sb.toString(); 
    }
    

}
