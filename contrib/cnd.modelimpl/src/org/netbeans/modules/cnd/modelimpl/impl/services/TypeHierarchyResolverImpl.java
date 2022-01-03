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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver.class)
public final class TypeHierarchyResolverImpl extends CsmTypeHierarchyResolver {

    private static final Map<CsmUID<CsmProject>, Reference<Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>>>> cache = new HashMap<>();
    private static long lastVersion = -1;

    public TypeHierarchyResolverImpl() {
    }

    @Override
    public Collection<CsmReference> getSubTypes(CsmClass referencedClass, boolean directSubtypesOnly) {
        if (!CsmBaseUtilities.isValid(referencedClass)) {
            return Collections.<CsmReference>emptySet();
        }
        return getSubTypesImpl(referencedClass, directSubtypesOnly);
    }

    private Collection<CsmReference> getSubTypesImpl(CsmClass referencedClass, boolean directSubtypesOnly) {
         CsmFile file = referencedClass.getContainingFile();
        long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
        CsmProject project = file.getProject();
        Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> fullMap = getOrCreateFullMap(project, fileVersion);
        Collection<CsmReference> res = new ArrayList<>();
        for(CsmUID<CsmClass> cls : getSubTypesImpl(referencedClass, fullMap, directSubtypesOnly)) {
            CsmClass clsObj = cls.getObject();
            if (clsObj != null) {
                res.add(CsmReferenceSupport.createObjectReference(clsObj));
            }
        }        
        return res;
    }

    private  Set<CsmUID<CsmClass>> getSubTypesImpl(CsmClass referencedClass, Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> map, boolean directSubtypesOnly) {
        if (directSubtypesOnly) {
            return getSubTypesImpl(referencedClass, map);
        }
        Set<CsmUID<CsmClass>> antiLoop = new HashSet<>();
        Set<CsmUID<CsmClass>> res = new HashSet<>(getSubTypesImpl(referencedClass, map));
        antiLoop.add(UIDs.get(referencedClass));
        while(true) {
            int size = res.size();
            Set<CsmUID<CsmClass>> step = new HashSet<>();
            for(CsmUID<CsmClass> reference : res) {
                if (!antiLoop.contains(reference)) {
                    CsmClass cls = reference.getObject();
                    if (cls != null) {
                        for(CsmUID<CsmClass> increment : getSubTypesImpl(cls, map)) {
                            if (!antiLoop.contains(increment)) {
                                step.add(increment);
                            }
                        }
                    }
                    antiLoop.add(reference);
                }
            }
            res.addAll(step);
            if (res.size() == size) {
                break;
            }
        }
        return res;
    }

    private Set<CsmUID<CsmClass>> getSubTypesImpl(CsmClass referencedClass, Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> map) {
        CsmUID<CsmClass> referencedClassUID = UIDs.get(referencedClass);
        Set<CsmUID<CsmClass>> res = map.get(referencedClassUID);
        if (res != null) {
            return res;
        }
        res = new HashSet<>();
        if (CsmBaseUtilities.isValid(referencedClass)) {
            CsmFile file = referencedClass.getContainingFile();
            CsmProject project = file.getProject();
            processProjectInheritances(project, referencedClass, referencedClassUID, res);
            Set<CsmProject> tracked = new HashSet<>();
            tracked.add(project);
            if (project instanceof ProjectBase) {
                for(CsmProject dependent : ((ProjectBase)project).getDependentProjects()){
                    processProjectInheritances(dependent, referencedClass, referencedClassUID, res);
                    tracked.add(dependent);
                }
            }
            for (CsmProject prj : CsmModelAccessor.getModel().projects()) {
                if (tracked.add(prj)) {
                    processProjectInheritances(prj, referencedClass, referencedClassUID, res);
                }
            }
        }
        map.put(referencedClassUID, res);
        return res;
    }

    private void processProjectInheritances(CsmProject project, CsmClass referencedClass, CsmUID<CsmClass> referencedClassUID, Set<CsmUID<CsmClass>> res) {
        for (CsmInheritance inh : project.findInheritances(referencedClass.getName())){
            CsmClassifier classifier = inh.getClassifier();
            if (classifier != null) {
                if (CsmKindUtilities.isClassForwardDeclaration(classifier)) {
                    CsmClass refferedClass = ((CsmClassForwardDeclaration) classifier).getCsmClass();
                    if (CsmKindUtilities.isClassifier(refferedClass)) {
                        classifier = (CsmClassifier) refferedClass;
                    }
                }
                if (CsmKindUtilities.isInstantiation(classifier)) {
                    CsmOffsetableDeclaration template = ((CsmInstantiation)classifier).getTemplateDeclaration();
                    if (CsmKindUtilities.isClassifier(template)) {
                        classifier = (CsmClassifier) template;
                    }
                }
                if (CsmReferenceSupport.sameDeclaration(referencedClass, classifier)) {
                    CsmScope scope = inh.getScope();
                    if (CsmKindUtilities.isClass(scope)) {
                        res.add(UIDs.get((CsmClass)scope));
                    }
                }
            }
        }
    }

    private synchronized Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>> getOrCreateFullMap(CsmProject project, long version) {
        if (lastVersion != version) {
            cache.clear();
        }
        lastVersion = version;
        CsmUID<CsmProject> prjUID = UIDs.get(project);
        Reference<Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>>> outRef = cache.get(prjUID);
        Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>> out = (outRef == null) ? null : outRef.get();
        if (out == null) {
            out = new ConcurrentHashMap<>();
            cache.put(prjUID, new SoftReference<>(out));
        }
        return out;
    }
}
