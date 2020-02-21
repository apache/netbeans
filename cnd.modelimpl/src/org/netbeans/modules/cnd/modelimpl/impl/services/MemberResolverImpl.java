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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelutil.ClassifiersAntiLoop;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;

/**
 *
 */
public final class MemberResolverImpl {

    public MemberResolverImpl(){
    }
    
    public CsmMember getDeclaration(CsmClassifier cls, CharSequence name) {
        Iterator<CsmMember> declarations = getDeclarations(cls, name, true);
        if (declarations.hasNext()) {
            return declarations.next();
        }
        return null;
    }       

    public Iterator<CsmMember> getDeclarations(CsmClassifier cls, CharSequence name) {
        return getDeclarations(cls, name, false);
    }       

    private Iterator<CsmMember> getDeclarations(CsmClassifier cls, CharSequence name, boolean first) {
        if (CsmKindUtilities.isOffsetable(cls)) {
            if (CharSequenceUtils.indexOf(name, ':') >= 0) {
                //TODO Fix Evaluator.g
                CndUtils.assertTrueInConsole(false, "Attempt to find member \""+name+"\" in the class "+cls.getQualifiedName()); // NOI18N
                return Collections.<CsmMember>emptyList().iterator();
            }
            Resolver aResolver = ResolverFactory.createResolver((CsmOffsetable) cls);
            try {
                cls = aResolver.getOriginalClassifier(cls);
            } finally {
                ResolverFactory.releaseResolver(aResolver);
            }
            if (CsmKindUtilities.isClass(cls)){
                List<CsmMember> res = new ArrayList<>();
                getClassMembers((CsmClass)cls, name, res, first);
                getSuperClasses((CsmClass)cls, name, res, first, new ClassifiersAntiLoop());
                return res.iterator();
            }
        }
        return Collections.<CsmMember>emptyList().iterator();
    }       

    private void getClassMembers(CsmClass cls, CharSequence name, List<CsmMember> res, boolean first){
        Iterator<CsmMember> it = CsmSelect.getClassMembers(cls,
                    CsmSelect.getFilterBuilder().createNameFilter(name, true, true, false));
        while(it.hasNext()){
            CsmMember m = it.next();
            if (CsmSortUtilities.matchName(m.getName(), name, true, true)){
                res.add(m);
                if (first) {
                    return;
                }
            }
        }
    }

    private void getSuperClasses(CsmClass cls, CharSequence name, List<CsmMember> res, boolean first, ClassifiersAntiLoop antiLoop){
        if (first && !res.isEmpty()) {
            return;
        }
        if (antiLoop.contains(cls)){
            return;
        }
        antiLoop.add(cls);
        for(CsmInheritance inh : cls.getBaseClasses()){
            CsmVisibility v = inh.getVisibility();
            switch (v){
                case PRIVATE:
                    break;
                default:
                    CsmClass base = CsmInheritanceUtilities.getCsmClass(inh);
                    if (base != null) {
                        getClassMembers(base, name, res, first);
                        getSuperClasses(base, name, res, first, antiLoop);
                    }
            }
            if (first && !res.isEmpty()) {
                return;
            }
        }
    }
    
    public Iterator<CsmClassifier> getNestedClassifiers(CsmClassifier cls, CharSequence name) {
        Iterator<CsmMember> it =  getDeclarations(cls, name);
        List<CsmClassifier> res = new ArrayList<>();
        while(it.hasNext()){
            CsmMember m = it.next();
            if (CsmKindUtilities.isClassifier(m)){
                res.add((CsmClassifier) m);
            }
        }

        if (CsmKindUtilities.isClass(cls) && CsmSortUtilities.matchName(cls.getName(), name, true, true)){
            res.add(cls);
        }
        
        return res.iterator();
    }
}
