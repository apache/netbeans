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

package org.netbeans.modules.cnd.modelimpl.csm.resolver;

import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.BaseUtilitiesProviderImpl;

/**
 *
 */
public class Context {
    private final CsmFile file;
    private final int origOffset;
    private final Resolver3 resolver;
    private CsmNamespace containingNamespace;
    private CsmClass containingClass;
    private CsmFunctionDefinition containingFunction;
    private boolean contextFound = false;

    Context(CsmFile file, int origOffset, Resolver3 resolver) {
        this.file = file;
        this.origOffset = origOffset;
        this.resolver = resolver;
    }

    CsmNamespace getContainingNamespace() {
        if( ! contextFound ) {
            findContext(origOffset);
        }
        return containingNamespace;
    }

    CsmClass getContainingClass() {
        if( ! contextFound ) {
            findContext(origOffset);
        }
        return containingClass;
    }

    CsmFunctionDefinition getContainingFunction() {
        if( ! contextFound ) {
            findContext(origOffset);
        }
        return containingFunction;
    }

    private void findContext(int offset) {
        contextFound = true;
        CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(0, offset);
        Iterator<CsmOffsetableDeclaration> declarations;
        if (file instanceof FileImpl) {
            declarations = ((FileImpl)file).getDeclarations(offset);
        } else {
            declarations = CsmSelect.getDeclarations(file, filter);
        }
        findContext(declarations, filter, offset);
    }

    private void findContext(Iterator<?> it, CsmFilter filter, int offset) {
        while(it.hasNext()) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
                CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                if( nd.getStartOffset() < offset && offset < nd.getEndOffset()  ) {
                    containingNamespace = nd.getNamespace();
                    findContext(CsmSelect.getDeclarations(nd, filter), filter, offset);
                }
            } else if(   decl.getKind() == CsmDeclaration.Kind.CLASS
                    || decl.getKind() == CsmDeclaration.Kind.STRUCT
                    || decl.getKind() == CsmDeclaration.Kind.UNION ) {

                CsmClass cls = (CsmClass) decl;
                if( cls.getStartOffset() < offset && offset < cls.getEndOffset()  ) {
                    containingClass = cls;
                    findContext(CsmSelect.getClassMembers(containingClass, filter), filter, offset);
                }
            } else if( decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                    decl.getKind() == CsmDeclaration.Kind.FUNCTION_LAMBDA ||
                    decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
                CsmFunctionDefinition fd = (CsmFunctionDefinition) decl;
                if( fd.getStartOffset() < offset && offset < fd.getEndOffset()  ) {
                    containingFunction = fd;
                    CsmNamespace ns = BaseUtilitiesProviderImpl.getImpl()._getFunctionNamespace(fd);
                    if( ns != null && ! ns.isGlobal() ) {
                        containingNamespace = ns;
                    }
                    CsmFunction fun = getFunctionDeclaration(fd);
                    if( fun != null && CsmKindUtilities.isMethodDeclaration(fun) ) {
                        containingClass = getMethodContainingClass((CsmMethod) fun);
                    } else {
                        if (CsmKindUtilities.isCastOperator(fd)) {
                            // We could get containing class without declaration of cast operator
                            if (fd instanceof FunctionImplEx) {
                                CsmObject owner = ((FunctionImplEx) fd).findOwner();
                                if (CsmKindUtilities.isClass(owner)) {
                                    containingClass = (CsmClass) owner;
                                }
                            } else {
                                // Here could be logic with CsmEntityResolver
                            }
                        }                        
                    }
                }
            }
        }
    }

    private CsmFunction getFunctionDeclaration(CsmFunctionDefinition fd){
        if (resolver.isRecursionOnResolving(Resolver3.INFINITE_RECURSION)) {
            return null;
        }
        return fd.getDeclaration();
    }

    private CsmClass getMethodContainingClass(CsmMethod m){
        return m.getContainingClass();
    }
}
