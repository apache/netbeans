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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmCompilationUnit;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmResolveContext;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardEnum;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver.class)
public class ClassifierResolverImpl extends CsmClassifierResolver {
    @Override
    public CsmClassifier getTypeClassifier(CsmType type, CsmFile contextFile, int contextOffset, boolean resolveTypeChain) {
        return getTypeClassifier(type, null, contextFile, contextOffset, resolveTypeChain);
    }

    @Override
    public CsmClassifier getTypeClassifier(CsmType type, CsmScope contextScope, CsmFile contextFile, int contextOffset, boolean resolveTypeChain) {
        CsmProject project = contextFile.getProject();
        // we'd prefer to start from real project, not artificial one
        if (project != null && project.isArtificial()) {
            for (CsmCompilationUnit cu : CsmFileInfoQuery.getDefault().getCompilationUnits(contextFile, contextOffset)) {
                if (cu.getStartFile() != null) {
                    contextFile = cu.getStartFile();
                    break;
                }
            }
        }
        Resolver resolver = ResolverFactory.createResolver(contextFile, contextOffset);
        CsmClassifier cls = null;
        try {
            cls = type.getClassifier();
            if (CsmBaseUtilities.isUnresolved(cls) && CsmExpressionResolver.shouldResolveAsMacroType(type, contextScope)) {
                // This type came from macro! Let's resolve it again.
                CsmType resolvedMacroType = CsmExpressionResolver.resolveMacroType(
                    type, 
                    contextScope, 
                    Instantiation.getInstantiatedTypeInstantiations(type),
                    null
                );
                if (resolvedMacroType != null) {
                    cls = resolvedMacroType.getClassifier();
                }
            }
            if (resolveTypeChain) {
                cls = resolver.getOriginalClassifier(cls);
            }
        } finally {
            ResolverFactory.releaseResolver(resolver);
        }
        return cls;
    }

    @Override
    public CsmClassifier getOriginalClassifier(CsmClassifier orig, CsmFile contextFile) {
        if (orig instanceof CsmOffsetable) {
            CsmProject project = contextFile.getProject();
            // we'd prefer to start from real project, not artificial one
            if (project != null && project.isArtificial()) {
                boolean useCompilationUnit = true;
                CsmResolveContext lastResolveContext = CsmResolveContext.getLast();
                if (lastResolveContext != null && lastResolveContext.getFile() != null) {
                    contextFile = lastResolveContext.getFile();
                    if (contextFile.isSourceFile()) {
                        useCompilationUnit = false;
                    }
                }
                if (useCompilationUnit) {
                    for (CsmCompilationUnit cu : CsmFileInfoQuery.getDefault().getCompilationUnits(contextFile, 0)) {
                        if (cu.getStartFile() != null) {
                            contextFile = cu.getStartFile();
                            break;
                        }
                    }
                }
            }
            Resolver aResolver = ResolverFactory.createResolver((CsmOffsetable) orig, contextFile);
            try {
                return aResolver.getOriginalClassifier(orig);
            } finally {
                ResolverFactory.releaseResolver(aResolver);
            }
        }
        return orig;
    }

    @Override
    public boolean isForwardClass(CsmObject cls) {
        return CsmKindUtilities.isDeclaration(cls) && ForwardClass.isForwardClass((CsmDeclaration)cls);
    }

    @Override
    public boolean isForwardEnum(CsmObject cls) {
        return CsmKindUtilities.isDeclaration(cls) && ForwardEnum.isForwardEnum((CsmDeclaration) cls);
    }

    @Override
    public CsmClassifier findClassifierUsedInFile(CharSequence qualifiedName, CsmFile file, boolean classesOnly) {
        CsmProject project = file.getProject();
        if (project == null) {
            return null;
        }
        AtomicBoolean visible = new AtomicBoolean(false);
        CsmClassifier result = findVisibleDeclaration(project, qualifiedName, file, visible, classesOnly);
        // we prefer to skip even visible class forward based classes
        if (!ForwardClass.isForwardClass(result) && visible.get()) {
            assert result != null : "how can visible be true without a result?";
            return result;
        }
        Collection<CsmProject> libraries = getLibraries(file, 0);
        // continue in libs
        for (Iterator<CsmProject> iter = libraries.iterator(); iter.hasNext();) {
            visible.set(false);
            CsmProject lib = iter.next();
            CsmClassifier visibleDecl = findVisibleDeclaration(lib, qualifiedName, file, visible, classesOnly);
            // we prefer to skip even visible class forward based classes
            if (!ForwardClass.isForwardClass(visibleDecl) && visible.get()) {
                return visibleDecl;
            }
            if (result == null) {
                result = visibleDecl;
            }
        }
        return result;
    }

    private CsmClassifier findVisibleDeclaration(CsmProject project, CharSequence uniqueName,
            CsmFile file, AtomicBoolean visible, boolean classesOnly) {
        Collection<CsmClassifier> decls = project.findClassifiers(uniqueName);
        List<CsmClassifier> visibles = new ArrayList<>();
        CsmClassifier first = null;
        final CsmIncludeResolver ir = CsmIncludeResolver.getDefault();
        List<CsmObject> td = new ArrayList<>();
        AtomicBoolean hasClassifier = new AtomicBoolean(false);
        for (CsmClassifier decl : decls) {
            if (!classesOnly || CsmKindUtilities.isClass(decl)) {
                if ((first == null || ForwardClass.isForwardClass(first))) {
                    if(!(CsmKindUtilities.isTemplate(decl) && ((CsmTemplate)decl).isSpecialization())) {
                        first = decl;
                    }
                }
                if (ir.isObjectVisible(file, decl)) {
                    if (CsmKindUtilities.isTypedef(decl) || CsmKindUtilities.isTypeAlias(decl)) {
                        if (!hasClassifier.get()) {
                            // if no any visible classifiers found so far
                            // check if same typedef is not yet added
                            CharSequence typeTxt = ((CsmTypedef)decl).getType().getClassifierText();
                            boolean foundSameTD = false;
                            for (CsmClassifier cls : visibles) {
                                if ((CsmKindUtilities.isTypedef(cls) || CsmKindUtilities.isTypeAlias(cls))
                                     && typeTxt.equals(((CsmTypedef)cls).getType().getClassifierText())) {
                                    foundSameTD = true;
                                    break;
                                }
                            }
                            if (!foundSameTD) {
                                visibles.add(decl);
                                td.add((CsmTypedef) decl);
                            }
                        }
                    } else if (CsmKindUtilities.isClassForwardDeclaration(decl) || ForwardClass.isForwardClass(decl)) {
                        if (!hasClassifier.get()) {
                            visibles.add(decl);
                            td.add(decl);
                        }
                    } else {
                        if (!td.isEmpty()) {
                            // remove typedef
                            visibles.removeAll(td);
                            td.clear();
                        }
                        hasClassifier.set(true);
                        visibles.add(decl);
                    }
                }
            }
        }
        if (!visibles.isEmpty()) {
            visible.set(true);
            // trace
            if (TRACE_MULTIPLE_VISIBE_OBJECTS) {
                if (visibles.size() > 1) {
                    // we have several visible classifiers
                    System.err.printf("findVisibleDeclaration: we have several classifiers %s visible from %s%n", uniqueName, file.getAbsolutePath()); // NOI18N
                    int ind = 0;
                    for (CsmClassifier csmClassifier : visibles) {
                        String fileName = "<builtin"; // NOI18N
                        if (CsmKindUtilities.isOffsetable(csmClassifier)) {
                            CsmFile containingFile = ((CsmOffsetable)csmClassifier).getContainingFile();
                            if (containingFile != null) {
                                fileName = containingFile.getAbsolutePath().toString();
                            }
                        }
                        System.err.printf("[%d] %s from %s%n", ind++, csmClassifier, fileName); // NOI18N
                    }
                }
            }
            return visibles.get(0);
        }
        return first;
    }
    private static final boolean TRACE_MULTIPLE_VISIBE_OBJECTS = Boolean.getBoolean("cnd.trace.multiple.visible");

    private Collection<CsmProject> getLibraries(CsmFile file, int contextOffset) {
        CsmProject project = file.getProject();
        if (project == null) {
            return Collections.emptyList();
        }
        if (project.isArtificial()) {
            Collection<CsmProject> out = new HashSet<>(2);
            for (CsmCompilationUnit cu : CsmFileInfoQuery.getDefault().getCompilationUnits(file, contextOffset)) {
                out.addAll(cu.getStartProject().getLibraries());
            }
            return out;
        } else {
            return project.getLibraries();
        }
    }
}
