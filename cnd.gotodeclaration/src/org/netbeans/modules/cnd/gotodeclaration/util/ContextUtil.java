/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
