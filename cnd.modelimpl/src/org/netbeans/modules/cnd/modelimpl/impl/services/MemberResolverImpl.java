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
