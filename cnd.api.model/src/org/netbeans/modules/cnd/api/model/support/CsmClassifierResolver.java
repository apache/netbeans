/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.model.support;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmClassifierResolver {
    private static final CsmClassifierResolver DEFAULT = new Default();

    public abstract CsmClassifier getOriginalClassifier(CsmClassifier orig, CsmFile contextFile);
    
    @Deprecated
    public abstract CsmClassifier getTypeClassifier(CsmType type, CsmFile contextFile, int contextOffset, boolean resolveTypeChain);
    
    public abstract CsmClassifier getTypeClassifier(CsmType type, CsmScope contextScope, CsmFile contextFile, int contextOffset, boolean resolveTypeChain);

    /**
     * trying to find classifier with full qualified name used in file
     * @param qualifiedName full qualified name of classifier
     * @param csmFile file where classifier is used by name
     * @param classesOnly true if need to check classes and not typedefs/enums
     * @return best (prefer visible) classifier
     */
    public CsmClassifier findClassifierUsedInFile(CharSequence qualifiedName, CsmFile csmFile, boolean classesOnly) {
        if (csmFile != null) {
            CsmProject project = csmFile.getProject();
            if (project != null) {
                return project.findClassifier(qualifiedName);
            }
        }
        return null;
    }

    public boolean isForwardClassifier(CsmObject obj) {
        return isForwardClass(obj) || isForwardEnum(obj);
    }
    public abstract boolean isForwardClass(CsmObject cls);
    public abstract boolean isForwardEnum(CsmObject cls);

    protected CsmClassifierResolver() {
    }

    /**
     * Static method to obtain the CsmClassifierResolver implementation.
     * @return the selector
     */
    public static CsmClassifierResolver getDefault() {
        return DEFAULT;
    }

    /**
     * Implementation of the default resolver
     */
    private static final class Default extends CsmClassifierResolver {
        private final Lookup.Result<CsmClassifierResolver> res;
        private static final boolean FIX_SERVICE = true;
        private CsmClassifierResolver fixedResolver;
        
        private static final ThreadLocal<Set<TypeResolveRequest>> threadLocalTypeAntiloopSet = new ThreadLocal<Set<TypeResolveRequest>>() {

            @Override
            protected Set<TypeResolveRequest> initialValue() {
                return new HashSet<TypeResolveRequest>();
            }

        };      
        
        private static final ThreadLocal<Set<ClassifierResolveRequest>> threadLocalClassifierAntiloopSet = new ThreadLocal<Set<ClassifierResolveRequest>>() {

            @Override
            protected Set<ClassifierResolveRequest> initialValue() {
                return new HashSet<ClassifierResolveRequest>();
            }

        };          
        
        Default() {
            res = Lookup.getDefault().lookupResult(CsmClassifierResolver.class);
        }

        private CsmClassifierResolver getService(){
            CsmClassifierResolver service = fixedResolver;
            if (service == null) {
                for (CsmClassifierResolver selector : res.allInstances()) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    // I see that it is ugly solution, but NB core cannot fix performance of FolderInstance.waitFinished()
                    fixedResolver = service;
                }
            }
            return service;
        }

        @Override
        public CsmClassifier getOriginalClassifier(CsmClassifier orig, CsmFile contextFile) {
            final ClassifierResolveRequest request = new ClassifierResolveRequest(orig, contextFile);
            if (enterAntiloop(threadLocalClassifierAntiloopSet.get(), request)) {
                try {
                    CsmClassifierResolver service = getService();
                    if (service != null) {
                        return service.getOriginalClassifier(orig, contextFile);
                    }
                } finally {
                    exitAntiloop(threadLocalClassifierAntiloopSet.get(), request);
                }
            }
            return orig;
        }

        @Override
        public CsmClassifier getTypeClassifier(CsmType type, CsmFile contextFile, int contextOffset, boolean resolveTypeChain) {
            return getTypeClassifier(type, null, contextFile, contextOffset, resolveTypeChain);
        }

        @Override
        public CsmClassifier getTypeClassifier(CsmType type, CsmScope contextScope, CsmFile contextFile, int contextOffset, boolean resolveTypeChain) {
            CsmClassifier classifier = null;
            final TypeResolveRequest request = new TypeResolveRequest(type, contextFile, contextOffset);
            if (enterAntiloop(threadLocalTypeAntiloopSet.get(), request)) {
                try {
                    CsmClassifierResolver service = getService();            
                    if (service != null) {
                        return service.getTypeClassifier(type, contextScope, contextFile, contextOffset, resolveTypeChain);
                    }
                    classifier = type.getClassifier();
                    if (resolveTypeChain) {
                        classifier = getOriginalClassifier(classifier, contextFile);
                    }
                } finally {
                    exitAntiloop(threadLocalTypeAntiloopSet.get(), request);
                }
            }
            return classifier;
        }

        @Override
        public CsmClassifier findClassifierUsedInFile(CharSequence qualifiedName, CsmFile  csmFile, boolean classesOnly) {
            CsmClassifierResolver service = getService();
            if (service != null) {
                return service.findClassifierUsedInFile(qualifiedName, csmFile, classesOnly);
            }
            return super.findClassifierUsedInFile(qualifiedName, csmFile, classesOnly);
        }

        @Override
        public boolean isForwardClass(CsmObject cls) {
            CsmClassifierResolver service = getService();
            if (service != null) {
                return service.isForwardClass(cls);
            }
            return false;
        }

        @Override
        public boolean isForwardEnum(CsmObject cls) {
            CsmClassifierResolver service = getService();
            if (service != null) {
                return service.isForwardEnum(cls);
            }
            return false;
        }
        
        private static <T> boolean enterAntiloop(Set<T> antiloop, T request) {
            if (antiloop.contains(request)) {
                return false;
            }
            antiloop.add(request);
            return true;
        }
        
        private static <T> void exitAntiloop(Set<T> antiloop, T request) {
            antiloop.remove(request);
        }
        
        private static class TypeResolveRequest {
            
            private final CsmType type;
            
            private final CsmFile contextFile;
            
            private final int offset;

            public TypeResolveRequest(CsmType type, CsmFile contextFile, int offset) {
                this.type = type;
                this.contextFile = contextFile;
                this.offset = offset;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 67 * hash + (this.contextFile != null ? this.contextFile.hashCode() : 0);
                hash = 67 * hash + this.offset;
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final TypeResolveRequest other = (TypeResolveRequest) obj;
                if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
                    return false;
                }
                if (this.contextFile != other.contextFile && (this.contextFile == null || !this.contextFile.equals(other.contextFile))) {
                    return false;
                }
                if (this.offset != other.offset) {
                    return false;
                }
                return true;
            }            
        }
        
        private static class ClassifierResolveRequest {
            
            private final CsmClassifier cls;
            
            private final CsmFile contextFile;

            public ClassifierResolveRequest(CsmClassifier cls, CsmFile contextFile) {
                this.cls = cls;
                this.contextFile = contextFile;
            }

            @Override
            public int hashCode() {
                int hash = 5;
                hash = 17 * hash + (this.cls != null ? this.cls.hashCode() : 0);
                hash = 17 * hash + (this.contextFile != null ? this.contextFile.hashCode() : 0);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final ClassifierResolveRequest other = (ClassifierResolveRequest) obj;
                if (this.cls != other.cls && (this.cls == null || !this.cls.equals(other.cls))) {
                    return false;
                }
                if (this.contextFile != other.contextFile && (this.contextFile == null || !this.contextFile.equals(other.contextFile))) {
                    return false;
                }
                return true;
            }
        }        
    }
}
