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

package org.netbeans.modules.cnd.navigation.overrides;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * A class that computes annotations -
 * extracted from OverridesTaskFactory for the sake of testability
 */
public class ComputeAnnotations {

    public static ComputeAnnotations getInstance(CsmFile csmFile, StyledDocument doc, AtomicBoolean canceled) {
        return new ComputeAnnotations(csmFile, doc, canceled);
    }

    private final CsmFile csmFile;
    private final StyledDocument doc;
    private final AtomicBoolean canceled;

    private ComputeAnnotations(CsmFile csmFile, StyledDocument doc, AtomicBoolean canceled) {
       this.csmFile = csmFile;
       this.doc = doc;
       this.canceled = canceled;
    }

    /*package*/ void computeAnnotations(Collection<BaseAnnotation> toAdd) {
        if (canceled.get()) {
            return;
        }
        CsmCacheManager.enter();
        try {
            Map<Integer, List<CsmOffsetableDeclaration>> external = new HashMap<Integer, List<CsmOffsetableDeclaration>>();
            Iterator<CsmOffsetableDeclaration> externalDeclarations = CsmSelect.getExternalDeclarations(csmFile);
            if (externalDeclarations != null) {
                while(externalDeclarations.hasNext()) {
                    CsmOffsetableDeclaration next = externalDeclarations.next();
                    int start = next.getStartOffset();
                    List<CsmOffsetableDeclaration> list = external.get(start);
                    if (list == null) {
                        list = new ArrayList<CsmOffsetableDeclaration>();
                        external.put(start, list);
                    }
                    list.add(next);
                }
            }
            computeAnnotations(csmFile.getDeclarations(), toAdd, external);
        } finally {
            CsmCacheManager.leave();
        }
    }

    private void computeAnnotations(Collection<? extends CsmOffsetableDeclaration> toProcess, Collection<BaseAnnotation> toAdd, Map<Integer, List<CsmOffsetableDeclaration>> external) {
        for (CsmOffsetableDeclaration decl : toProcess) {
            if (canceled.get()) {
                return;
            }
            if (this.csmFile.equals(decl.getContainingFile())) {
                if (CsmKindUtilities.isFunction(decl)) {
                    computeAnnotation((CsmFunction) decl, toAdd, external);
                } else if (CsmKindUtilities.isClass(decl)) {
                    if (CsmKindUtilities.isTemplate(decl)) {
                        if (((CsmTemplate)decl).isExplicitSpecialization()) {
                            continue;
                        }
                    }
                    computeAnnotation((CsmClass) decl, toAdd);
                    computeAnnotations(((CsmClass) decl).getMembers(), toAdd, external);
                } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                    computeAnnotations(((CsmNamespaceDefinition) decl).getDeclarations(), toAdd, external);
                }
            }
        }
    }

    private void computeAnnotation(CsmFunction func, Collection<BaseAnnotation> toAdd, Map<Integer, List<CsmOffsetableDeclaration>> external) {
        Collection<CsmVirtualInfoQuery.CsmOverrideInfo> baseMethods = Collections.<CsmVirtualInfoQuery.CsmOverrideInfo>emptyList();
        Collection<CsmVirtualInfoQuery.CsmOverrideInfo> overriddenMethods = Collections.<CsmVirtualInfoQuery.CsmOverrideInfo>emptyList();
        CsmVirtualInfoQuery.CsmOverrideInfo thisMethod = null;
        if (CsmKindUtilities.isMethod(func)) {
            CsmMethod meth = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration(func);
            if(meth != null) {
                CsmVirtualInfoQuery.CsmOverriddenChain chain = CsmVirtualInfoQuery.getDefault().getOverriddenChain(meth);
                if (!chain.getBaseMethods().isEmpty()) {
                    baseMethods = chain.getBaseMethods();
                }
                if (!chain.getDerivedMethods().isEmpty()) {
                    overriddenMethods = chain.getDerivedMethods();
                }
                thisMethod = chain.getThisMethod();
                if (BaseAnnotation.LOGGER.isLoggable(Level.FINEST)) {
                    BaseAnnotation.LOGGER.log(Level.FINEST, "Found {0} base decls for {1}", new Object[]{baseMethods.size(), toString(func)});
                    for (CsmVirtualInfoQuery.CsmOverrideInfo baseMethod : baseMethods) {
                        BaseAnnotation.LOGGER.log(Level.FINEST, "    {0}", toString(baseMethod.getMethod()));
                    }
                }
            }
        }
        if (canceled.get()) {
            return;
        }
        Collection<CsmOffsetableDeclaration> baseTemplates = CsmInstantiationProvider.getDefault().getBaseTemplate(func);
        if (canceled.get()) {
            return;
        }
        Collection<CsmOffsetableDeclaration> templateSpecializations = CsmInstantiationProvider.getDefault().getSpecializations(func);
        if (canceled.get()) {
            return;
        }
        if (canceled.get()) {
            return;
        }
        List<CsmOffsetableDeclaration> pseudoOverrides = external.get(func.getStartOffset());
        if (pseudoOverrides != null) {
            for(CsmOffsetableDeclaration e : pseudoOverrides) {
                overriddenMethods = new ArrayList<CsmVirtualInfoQuery.CsmOverrideInfo>(overriddenMethods);
                if (CsmKindUtilities.isFunction(e)) {
                    CsmFunction f = (CsmFunction) e;
                    CsmFunctionDefinition definition = f.getDefinition();
                    if (definition != null && !definition.equals(f)) {
                        overriddenMethods.add(new CsmVirtualInfoQuery.CsmOverrideInfo(definition, false));
                    }
                }
            }
        }
        if (!baseMethods.isEmpty() || !overriddenMethods.isEmpty() || !baseTemplates.isEmpty() || !templateSpecializations.isEmpty()) {
            toAdd.add(new OverrideAnnotation(doc, func, thisMethod, baseMethods, overriddenMethods, baseTemplates, templateSpecializations));
        }
    }

    private void computeAnnotation(CsmClass cls, Collection<BaseAnnotation> toAdd) {
        Collection<CsmReference> subRefs = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, false);
        if (canceled.get())  {
            return;
        }
        Collection<CsmClass> subClasses = new ArrayList<CsmClass>(subRefs.size());
        Collection<CsmOffsetableDeclaration> baseTemplateClasses = CsmInstantiationProvider.getDefault().getBaseTemplate(cls);
        if (canceled.get())  {
            return;
        }
        Collection<CsmOffsetableDeclaration> templateSpecializationClasses = CsmInstantiationProvider.getDefault().getSpecializations(cls);
        if (canceled.get())  {
            return;
        }
        if (!subRefs.isEmpty()) {
            for (CsmReference ref : subRefs) {
                CsmObject obj = ref.getReferencedObject();
                CndUtils.assertTrue(obj == null || (obj instanceof CsmClass), "getClassifier() should return either null or CsmClass"); //NOI18N
                if (obj instanceof CsmClass) {
                    subClasses.add((CsmClass) obj);
                }
            }
        }
        if (!subClasses.isEmpty() || !baseTemplateClasses.isEmpty() || !templateSpecializationClasses.isEmpty()) {
            toAdd.add(new InheritAnnotation(doc, cls, subClasses, baseTemplateClasses, templateSpecializationClasses));
        }
    }

    private static CharSequence toString(CsmFunction func) {
        StringBuilder sb = new StringBuilder();
        sb.append(func.getClass().getSimpleName());
        sb.append(' ');
        sb.append(func.getQualifiedName());
        sb.append(" ["); // NOI18N
        sb.append(func.getContainingFile().getName());
        sb.append(':');
        sb.append(func.getStartPosition().getLine());
        sb.append(']');
        return sb;
    }
}
