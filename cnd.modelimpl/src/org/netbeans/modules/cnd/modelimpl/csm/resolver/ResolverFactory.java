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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
