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

package org.netbeans.modules.cnd.api.model.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.modelutil.ClassifiersAntiLoop;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;
import org.openide.util.Lookup;

/**
 * API to query information about virtuality of method
 */
public abstract class CsmVirtualInfoQuery {
    private static final CsmVirtualInfoQuery EMPTY = new Empty();
    /** default instance */
    private static CsmVirtualInfoQuery defaultQuery;
    
    protected CsmVirtualInfoQuery() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmVirtualInfoQuery getDefault() {
        /*no need for sync synchronized access*/
        if (defaultQuery != null) {
            return defaultQuery;
        }
        defaultQuery = Lookup.getDefault().lookup(CsmVirtualInfoQuery.class);
        return defaultQuery == null ? EMPTY : defaultQuery;
    }

    public abstract boolean isVirtual(CsmMethod method);
    public abstract Collection<CsmMethod> getTopmostBaseDeclarations(CsmMethod method);
    public abstract Collection<CsmMethod> getFirstBaseDeclarations(CsmMethod method);
    public abstract Collection<CsmMethod> getAllBaseDeclarations(CsmMethod method);
    public abstract Collection<CsmMethod> getOverriddenMethods(CsmMethod method, boolean searchFromBase);
    public abstract CsmMethod getFirstDestructor(CsmClass cls);
    
    public abstract CsmOverriddenChain getOverriddenChain(CsmMethod method);
    
    public static final class CsmOverriddenChain {
        
        private final Collection<CsmOverrideInfo> baseMethods;
        private final CsmOverrideInfo thisMethod;
        private final Collection<CsmOverrideInfo> derivedMethods;

        private CsmOverriddenChain(Collection<CsmOverrideInfo> baseMethods, CsmOverrideInfo thisMethod, Collection<CsmOverrideInfo> derivedMethods) {
            this.baseMethods = baseMethods;
            this.thisMethod = thisMethod;
            this.derivedMethods = derivedMethods;
        }

        public Collection<CsmOverrideInfo> getBaseMethods() {
            return Collections.unmodifiableCollection(baseMethods);
        }

        public Collection<CsmOverrideInfo> getDerivedMethods() {
            return Collections.unmodifiableCollection(derivedMethods);
        }

        public CsmOverrideInfo getThisMethod() {
            return thisMethod;
        }                
    }

    public static final class CsmOverrideInfo {
        private final CsmFunction method;
        private final boolean virtual;

        public CsmOverrideInfo(CsmFunction method, boolean virtual) {
            this.method = method;
            this.virtual = virtual;
        }

        public CsmFunction getMethod() {
            return method;
        }

        public boolean isVirtual() {
            return virtual;
        }
        
        public boolean hasVirtualKeyword() {
            if (CsmKindUtilities.isMethod(method)) {
                return ((CsmMethod)method).isVirtual();
            }
            return false;
        }        
    }    
    
    //<editor-fold defaultstate="collapsed" desc="implementation">
    
    private static boolean methodEquals(CsmMethod toSearch, CsmMethod method) {
        if (CsmKindUtilities.isDestructor(toSearch) && CsmKindUtilities.isDestructor(method)) {
            return true;
        }
        if (!toSearch.getName().equals(method.getName())) {
            return false;
        }
        Collection<CsmParameter> list1 = toSearch.getParameters();
        Collection<CsmParameter> list2 = method.getParameters();
        if (list1.size() != list2.size()) {
            return false;
        }
        Iterator<CsmParameter> it2 = list2.iterator();
        for (CsmParameter p1 : list1) {
            CsmParameter p2 = it2.next();
            if (p1 != null && p2 != null) {
                if (p1.isVarArgs() && p2.isVarArgs()) {
                    continue;
                }
                CsmType type1 = p1.getType();
                CsmType type2 = p2.getType();
                // TODO: consider using method CsmUtilities.checkTypesEqual here
                if (type1 != null && type2 != null) {
                    if (!CsmUtilities.checkTypesEqual(type1, p1.getContainingFile(), type2, p2.getContainingFile(), new CsmUtilities.ExactMatchQualsEqualizer())) {
                        return false;
                    }
                    continue;
                } else if (type1 == null && type2 == null) {
                    continue;
                }
            } else if (p1 == null && p2 == null) {
                continue;
            }
            return false;
        }
        return true;
    }
        
    
    //
    // Implementation of the default query
    //
    private static final class Empty extends CsmVirtualInfoQuery {
        private static enum Overridden {
            FIRST, // returns nearest parent virtual method
            TOP, // returns topmost parent virtual method
            ALL, // returns all parent virtual methods
            PSEUDO // returns all parent virtual and not virtual methods
        }
        
        private Empty() {
        }
        
        @Override
        public boolean isVirtual(CsmMethod method) {
            CsmCacheManager.enter();
            try {
                if (method.isVirtual()) {
                    return true;
                }            
                return processClass(method, getFilterFor(method), method.getContainingClass(), new ClassifiersAntiLoop());
            } finally {
                CsmCacheManager.leave();
            }
        }
        
        @Override
        public CsmMethod getFirstDestructor(CsmClass cls) {
            return processClass(cls, getFilterDestructor(), new ClassifiersAntiLoop());
        }

        private CsmMethod processClass(CsmClass cls, CsmSelect.CsmFilter filter, ClassifiersAntiLoop antilLoop) {
            if (cls == null || antilLoop.contains(cls)) {
                return null;
            }
            antilLoop.add(cls);
            Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(cls, filter);
            while(classMembers.hasNext()) {
                CsmMember m = classMembers.next();
                if (CsmKindUtilities.isDestructor(m)) {
                    return (CsmMethod)m;
                }
            }
            for(CsmInheritance inh : cls.getBaseClasses()) {
                CsmMethod res = processClass(CsmInheritanceUtilities.getCsmClass(inh), filter, antilLoop);
                if (res != null) {
                    return res;
                }
            }
            return null;
        }

        private boolean processClass(CsmMethod toSearch, CsmSelect.CsmFilter filter, CsmClass cls, ClassifiersAntiLoop antilLoop){
            if (cls == null || antilLoop.contains(cls)) {
                return false;
            }
            antilLoop.add(cls);
            Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(cls, filter);
            while(classMembers.hasNext()) {
                CsmMember m = classMembers.next();
                if (CsmKindUtilities.isMethod(m)) {
                    CsmMethod met = (CsmMethod) m;
                    if (methodEquals(toSearch, met)) {
                        if (met.isVirtual()){
                            return true;
                        }
                        break;
                    }
                }
            }
            for(CsmInheritance inh : cls.getBaseClasses()){
                if (processClass(toSearch, filter, CsmInheritanceUtilities.getCsmClass(inh), antilLoop)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public Collection<CsmMethod> getTopmostBaseDeclarations(CsmMethod method) {
            Map<CsmMethod, CsmOverrideInfo> result = new HashMap<CsmMethod, CsmOverrideInfo>();
            getBaseDeclaration(result, method, Overridden.TOP);
            return result.keySet();
        }
        
        @Override
        public Collection<CsmMethod> getFirstBaseDeclarations(CsmMethod method) {
            Map<CsmMethod, CsmOverrideInfo> result = new HashMap<CsmMethod, CsmOverrideInfo>();
            getBaseDeclaration(result, method, Overridden.FIRST);
            return result.keySet();
        }
        
        @Override
        public Collection<CsmMethod> getAllBaseDeclarations(CsmMethod method) {
            Map<CsmMethod, CsmOverrideInfo> result = new HashMap<CsmMethod, CsmOverrideInfo>();
            getBaseDeclaration(result, method, Overridden.ALL);
            return result.keySet();
        }
        
        @Override
        public CsmOverriddenChain getOverriddenChain(CsmMethod method) {
            Map<CsmMethod, CsmOverrideInfo> result = new HashMap<CsmMethod, CsmOverrideInfo>();
            boolean virtual = getBaseDeclaration(result, method, Overridden.PSEUDO);
            CsmOverrideInfo current = new CsmOverrideInfo(method, virtual);
            ArrayList<CsmOverrideInfo> overridden = new ArrayList<CsmOverrideInfo>();
            for(CsmMethod m : getOverriddenMethods(method, false)) {
                // simplified virtual
                // TODO: count true virtual
                overridden.add(new CsmOverrideInfo(m, virtual||m.isVirtual()));
            }
            overridden.trimToSize();
            return new CsmOverriddenChain(result.values(), current, overridden);
        }
        
        private boolean getBaseDeclaration(Map<CsmMethod, CsmOverrideInfo> result, CsmMethod method, Overridden overridden) {
            LinkedList<CharSequence> antilLoop = new LinkedList<CharSequence>();
            CsmClass cls = method.getContainingClass();
            boolean virtual = method.isVirtual();
            
            if (cls != null) {
                CsmSelect.CsmFilter filter = getFilterFor(method);
                for(CsmInheritance inh : cls.getBaseClasses()) {
                    virtual |= processMethod(method, filter, CsmInheritanceUtilities.getCsmClass(inh), antilLoop,
                            null, null, result, overridden);
                }
            }
            return virtual;
        }
        
        private CsmSelect.CsmFilter getFilterFor(CsmMethod method) {
            if (CsmKindUtilities.isDestructor(method)) {
                return getFilterDestructor();
            } else {
                return CsmSelect.getFilterBuilder().createNameFilter(method.getName(), true, true, false);
            }
        }

        private CsmSelect.CsmFilter getFilterDestructor() {
            return CsmSelect.getFilterBuilder().createNameFilter("~", false, true, false); //NOI18N
        }
        
        /**
         * Searches for method with the given signature in the given class and its ancestors.
         * @param sig signature to search
         * @param cls class to start with
         * @param antilLoop prevents infinite loops
         * @param result if a method with the given signature is found, it's stored in result - even if it is not virtual.
         * @param first if true, returns first found method, otherwise the topmost one
         * @return true if method found and it is virtual, otherwise false
         */
        private boolean processMethod(CsmMethod toSearch, CsmSelect.CsmFilter filter, CsmClass cls, LinkedList<CharSequence> antilLoop,
                CsmMethod firstFound, CsmMethod lastFound,
                Map<CsmMethod, CsmOverrideInfo> result, Overridden overridden) {
            
            boolean virtual = false;
            boolean theLastInHierarchy;
            if (cls == null || antilLoop.contains(cls.getQualifiedName())) {
                theLastInHierarchy = true;
            } else {
                antilLoop.addLast(cls.getQualifiedName());
                try {
                    Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(cls, filter);
                    while(classMembers.hasNext()) {
                        CsmMember member = classMembers.next();
                        if (CsmKindUtilities.isMethod(member)) {
                            CsmMethod method = (CsmMethod) member;
                            if (methodEquals(toSearch, method)) {
                                if (firstFound == null) {
                                    firstFound = method;
                                }
                                lastFound = method;
                                if (method.isVirtual()) {
                                    virtual = true;
                                    switch (overridden) {
                                        case FIRST:
                                            result.put(firstFound, new CsmOverrideInfo(firstFound, virtual));
                                            return true;
                                    }
                                }
                                break;
                            }
                        }
                    }
                    theLastInHierarchy = cls.getBaseClasses().isEmpty();
                    for(CsmInheritance inh : cls.getBaseClasses()) {
                        virtual |= processMethod(toSearch, filter, CsmInheritanceUtilities.getCsmClass(inh), antilLoop, firstFound, lastFound, result, overridden);
                    }
                    if (lastFound != null) {
                        switch (overridden) {
                            case PSEUDO:
                                result.put(lastFound, new CsmOverrideInfo(lastFound, virtual));
                                break;
                            case ALL:
                                if (virtual) {
                                    result.put(lastFound, new CsmOverrideInfo(lastFound, virtual));
                                }
                                break;
                        }
                    }
                } finally {
                    antilLoop.removeLast();
                }
            }
            if (theLastInHierarchy && lastFound != null) {
                if (lastFound.isVirtual()) {
                    CndUtils.assertNotNull(firstFound, "last found != null && first found == null ?!"); //NOI18N
                    switch (overridden) {
                        case FIRST:
                            result.put(firstFound, new CsmOverrideInfo(firstFound, true));
                            break;
                        case TOP:
                            result.put(lastFound, new CsmOverrideInfo(lastFound, true));
                            break;
                    }
                    return true;
                }
            }
            return virtual;
        }
        
        @Override
        public Collection<CsmMethod> getOverriddenMethods(CsmMethod method, boolean searchFromBase) {
            CsmCacheManager.enter();
            try {
                return getOverriddenMethodsImpl(method, searchFromBase);
            } finally {
                CsmCacheManager.leave();
            }
        }
        
        private Collection<CsmMethod> getOverriddenMethodsImpl(CsmMethod method, boolean searchFromBase) {
            Set<CsmMethod> res = new HashSet<CsmMethod>();
            CsmClass cls;
            if (searchFromBase) {
                Iterator<CsmMethod> it = getTopmostBaseDeclarations(method).iterator();
                if (it.hasNext()){
                    method = it.next();
                }
                res.add(method);
            }
            cls = method.getContainingClass();
            if (cls != null){
                CsmSelect.CsmFilter filter = getFilterFor(method);
                for(CsmReference ref :CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, false)){
                    CsmClass c = (CsmClass) ref.getReferencedObject();
                    if (c != null) {
                        Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(c, filter);
                        while(classMembers.hasNext()) {
                            CsmMember m = classMembers.next();
                            if (CsmKindUtilities.isMethod(m)) {
                                CsmMethod met = (CsmMethod) m;
                                if (methodEquals(met, method)){
                                    res.add(met);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return res;
        }
    }
//</editor-fold>
}
