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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmCompilationUnit;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;

/**
 * Creates an instance of appropriate resolver
 * and delegates work to it
 */
public class ResolverFactory {
    private static final ThreadLocal<LinkedList<Resolver>> stack = new ThreadLocal<LinkedList<Resolver>>() {

        @Override
        protected LinkedList<Resolver> initialValue() {
            return new LinkedList<>();
        }
    };

    /** prevents creation */
    private ResolverFactory() {
    }
    
    public static Resolver createResolver(CsmOffsetable context) {
        return createResolver(context.getContainingFile(), context.getStartOffset());
    }

    public static Resolver createResolver(CsmFile file, int offset) {
        return createResolver(file, offset, null);
    }
    
    public static Resolver createResolver(CsmOffsetable context, CsmFile contextFile) {
        return createResolver(context.getContainingFile(), context.getStartOffset(), contextFile);
    }

    public static void releaseResolver(Resolver resolver) {
        LinkedList<Resolver> aStack = stack.get();
        assert !aStack.isEmpty();
        Resolver removeLast = aStack.removeLast();
        assert resolver == removeLast;
    }

    public static Resolver getCurrentResolver() {
        LinkedList<Resolver> aStack = stack.get();
        if (aStack.isEmpty()) {
            return null;
        }
        return aStack.peekLast();
    }
    
    public static CsmUID<CsmFile> getCurrentStartFile(CsmOffsetable context) {
        Resolver parent = null;
        LinkedList<Resolver> aStack = stack.get();
        if (!aStack.isEmpty()) {
            parent = aStack.getLast();
        } else {
            parent = null;
        }
        CsmFile curFile = parent == null ? context.getContainingFile() : parent.getStartFile();
        return UIDCsmConverter.fileToUID(curFile);
    }
    
    private static Resolver createResolver(CsmFile file, int offset, CsmFile contextFile) {
        Resolver parent = null;
        LinkedList<Resolver> aStack = stack.get();
        if (!aStack.isEmpty()) {
            parent = aStack.getLast();
        } else {
            parent = null;
        }
        if (file == null || aStack.size() > Resolver.INFINITE_RECURSION) {
            if (file == null) {
                // this can be in situation when old tasks finishes work in resolver, while file become invalid or project was closed or reparsed
                System.err.println("FALLBACK INTO EMPTY RESOLVER"); // NOI18N
            } else if (parent instanceof Resolver3) {
                if (Resolver.TRACE_RECURSION) { 
                    ((Resolver3)parent).traceRecursion();
                }
            }
            aStack.addLast(EMPTY_RESOLVER);
            return EMPTY_RESOLVER;
        } else {
            if (contextFile == null) {
                if (parent == null) {
                    // init contextFile for correct checks of visibility in Resolver
                    for (CsmCompilationUnit cu : CsmFileInfoQuery.getDefault().getCompilationUnits(file, offset)) {
                        final CsmFile startFile = cu.getStartFile();
                        if (startFile != null) {
                            contextFile = startFile;
                            break;
                        }
                    }                  
                    if (contextFile == null) {
                        contextFile = file;
                    }
                } else {
                    contextFile = parent.getStartFile();
                }
            }
            Resolver3 aResolver = new Resolver3(file, offset, parent, contextFile);
            aStack.addLast(aResolver);
            return aResolver;
        }
    }
    
    private static final Resolver EMPTY_RESOLVER = new EmptyResolver();
    
    private static final class EmptyResolver implements Resolver {

        @Override
        public Collection<CsmProject> getLibraries() {
            return Collections.emptyList();
        }

        @Override
        public CsmFile getStartFile() {
            return null;
        }

        @Override
        public CsmObject resolve(CharSequence[] nameTokens, int interestedKind) {
            return null;
        }

        @Override
        public boolean isRecursionOnResolving(int maxRecursion) {
            return true;
        }

        @Override
        public CsmClassifier getOriginalClassifier(CsmClassifier orig) {
            return null;
        }        
    }
}
