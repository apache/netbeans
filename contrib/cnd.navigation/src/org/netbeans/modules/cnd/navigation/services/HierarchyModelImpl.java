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

package org.netbeans.modules.cnd.navigation.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;

/**
 *
 */
/*package-local*/ class HierarchyModelImpl implements HierarchyModel {
    private Map<CsmClass,Set<CsmClass>> myMap;
    private Action close;
    private final Action[] actions;
    private final boolean subDirection;
    private final boolean plain;
    private final boolean recursive;
    private final CsmClass startClass;
       
    /** Creates a new instance of HierarchyModel */
    public HierarchyModelImpl(CsmClass cls, Action[] actions, boolean subDirection, boolean plain, boolean recursive) {
        this.actions = actions;
        this.subDirection = subDirection;
        this.plain = plain;
        this.recursive = recursive;
        startClass = cls;
        if (!subDirection) {
            myMap = buildSuperHierarchy(cls);
            if (!recursive) {
                Set<CsmClass> result = myMap.get(cls);
                if (result == null){
                    result = new HashSet<CsmClass>();
                }
                myMap = new HashMap<CsmClass,Set<CsmClass>>();
                myMap.put(cls,result);
            }
            if (plain) {
                Set<CsmClass> result = new HashSet<CsmClass>();
                gatherList(cls, result, myMap);
                myMap = new HashMap<CsmClass,Set<CsmClass>>();
                myMap.put(cls,result);
            }
        }
    }
    
    @Override
    public Collection<HierarchyModel.Node> getHierarchy(CsmClass cls) {
        CsmCacheManager.enter();
        try {
            Collection<CsmClass> inheritances = getHierarchyImpl(cls);
            List<CsmClass> specs = getSpecializationsHierarchyImpl(cls);
            Collection<HierarchyModel.Node> res = new ArrayList<>();
            if (inheritances != null) {
                for (CsmClass c : inheritances) {
                    res.add(new HierarchyModel.Node(c, false));
                }
            }
            if (specs != null) {
                for (CsmClass c : specs) {
                    res.add(new HierarchyModel.Node(c, true));
                }
            }
            return res;
        } finally {
            CsmCacheManager.leave();
        }
    }

    private List<CsmClass> getSpecializationsHierarchyImpl(CsmClass cls) {
        List<CsmClass> specClasses = Collections.emptyList();
        if (subDirection) {            
            Collection<CsmOffsetableDeclaration> templateSpecializations = CsmInstantiationProvider.getDefault().getSpecializations(cls);
            if (templateSpecializations != null && ! templateSpecializations.isEmpty()) {
                specClasses = new ArrayList<CsmClass>();
                for (CsmOffsetableDeclaration ts : templateSpecializations) {
                    if (ts instanceof CsmClass) {
                        specClasses.add((CsmClass) ts);
                    }
                }
            }
        } else {
            Collection<CsmOffsetableDeclaration> baseTemplates = CsmInstantiationProvider.getDefault().getBaseTemplate(cls);
            if (baseTemplates != null && !baseTemplates.isEmpty()) {
                specClasses = new ArrayList<CsmClass>();
                for (CsmOffsetableDeclaration bt : baseTemplates) {
                    if (bt instanceof CsmClass) {
                        specClasses.add((CsmClass) bt);
                    }
                }
            }            
        }
        return specClasses;
    }

    private Collection<CsmClass> getHierarchyImpl(CsmClass cls) {
        if (subDirection) {
            Collection<CsmReference> subRefs = Collections.<CsmReference>emptyList();
            if (plain && recursive) {
                if (startClass.equals(cls)) {
                    subRefs = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, false);
                }
            } else if (!plain && recursive) {
                subRefs = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, true);
            } else if (plain && !recursive) {
                if (startClass.equals(cls)) {
                    subRefs = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, true);
                }
            } else if (!plain && !recursive) {
                if (startClass.equals(cls)) {
                    subRefs = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, true);
                }
            }
            if (!subRefs.isEmpty()) {
                Collection<CsmClass> subClasses = new ArrayList<CsmClass>(subRefs.size());
                for (CsmReference ref : subRefs) {
                    CsmObject obj = ref.getReferencedObject();
                    if (obj instanceof CsmClass) {
                        subClasses.add((CsmClass) obj);
                    }
                }
                return subClasses;
            } else {
                return Collections.<CsmClass>emptyList();
            }
        } else {
            return myMap.get(cls);
        }
    }

    private void gatherList(CsmClass cls, Set<CsmClass> result, Map<CsmClass,Set<CsmClass>> map){
        Set<CsmClass> set = map.get(cls);
        if (set == null) {
            return;
        }
        for(CsmClass c : set){
            if (!result.contains(c)) {
                result.add(c);
                gatherList(c, result, map);
            }
        }
    }

    private Map<CsmClass,Set<CsmClass>> buildSuperHierarchy(CsmClass cls){
        CsmCacheManager.enter();
        try {
            HashMap<CsmClass,Set<CsmClass>> aMap = new HashMap<CsmClass,Set<CsmClass>>();
            buildSuperHierarchy(cls, aMap);
            Collection<CsmOffsetableDeclaration> baseTemplates = CsmInstantiationProvider.getDefault().getBaseTemplate(cls);
            if (baseTemplates != null && !baseTemplates.isEmpty()) {
                for (CsmOffsetableDeclaration bt : baseTemplates) {
                    if (bt instanceof CsmClass) {
                        buildSuperHierarchy((CsmClass)bt, aMap);
                    }
                }
            }            
            return aMap;
        } finally {
            CsmCacheManager.leave();
        }
    }
    
    private CsmClass getClassDeclaration(CsmInheritance inh){
        CsmClass c = CsmInheritanceUtilities.getCsmClass(inh);
        if (CsmKindUtilities.isInstantiation(c)) {
            CsmDeclaration d = ((CsmInstantiation)c).getTemplateDeclaration();
            if (CsmKindUtilities.isClass(d)){
                c = (CsmClass) d;
            }
        }
        return c;
    }

    private void buildSuperHierarchy(CsmClass cls, Map<CsmClass,Set<CsmClass>> map){
        Set<CsmClass> back = map.get(cls);
        if (back != null) {
            return;
        }
        back = new HashSet<CsmClass>();
        map.put(cls, back);
        Collection<CsmInheritance> list = cls.getBaseClasses();
        if (list != null && list.size() >0){
            for(CsmInheritance inh : list){
                CsmClass c = getClassDeclaration(inh);
                if (c != null) {
                    back.add(c);
                    buildSuperHierarchy(c, map);
                }
            }
        }
    }
    
    @Override
    public Action[] getDefaultActions() {
        return actions;
    }

    @Override
    public Action getCloseWindowAction() {
        return close;
    }

    @Override
    public void setCloseWindowAction(Action close) {
        this.close = close;
    }
}
