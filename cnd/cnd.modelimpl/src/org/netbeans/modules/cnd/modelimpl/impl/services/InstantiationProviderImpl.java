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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionPointerType;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariadicSpecializationParameter;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmCacheMap;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionEvaluator;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmResolveContext;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.support.CsmTypes;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImplSpecialization;
import org.netbeans.modules.cnd.modelimpl.csm.ExpressionBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.TypeBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariadicSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.util.MapHierarchy;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.TypeInfoCollector;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.Qualificator;
import org.netbeans.modules.cnd.spi.model.services.CsmExpressionEvaluatorProvider;
import org.netbeans.modules.cnd.utils.CndCollectionUtils;
import org.openide.util.Pair;
import static org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities.isPointer;
import static org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities.isReference;
import static org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities.tryGetFunctionPointerType;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation.TemplateParameterResolver;
import org.netbeans.modules.cnd.utils.Antiloop;

/**
 * Service that provides template instantiations
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider.class)
public final class InstantiationProviderImpl extends CsmInstantiationProvider {
    private static final Logger LOG = Logger.getLogger(InstantiationProviderImpl.class.getSimpleName());

    private static final char LT = '<'; // NOI18N

    private static final char GT = '>'; // NOI18N

    private static final ThreadLocal<Antiloop<CsmClassifier>> threadLocalSpecializeAntiloop = new ThreadLocal<Antiloop<CsmClassifier>>() {

        @Override
        protected Antiloop<CsmClassifier> initialValue() {
            return new Antiloop<>();
        }

    };


    @Override
    public CsmType[] deduceTemplateType(CsmTemplateParameter templateParam, CsmType patternType, CsmType actualType, DeduceTemplateTypeStrategy strategy) {
        if (patternType != null && actualType != null) {
//            CsmClassifir cls = patternType.getClassifier();
            TypeDigger digger = TypeDigger.create(templateParam, patternType);
            if (digger != null) {
                CsmType templateType = digger.getTemplateType();
                CsmType targetTypes[] = digger.extract(actualType, strategy);
                if (targetTypes != null) {
                    TypeInfoCollector templateTypeInfo = new CsmUtilities.TypeInfoCollector();
                    /*CsmType templateUnderlyingType =*/ CsmUtilities.iterateTypeChain(templateType, templateTypeInfo);
                    List<Qualificator> templateTypeQuals = templateTypeInfo.qualificators;
                    List<CsmType> results = new ArrayList<>();
                    for (CsmType targetType : targetTypes) {
                        ListIterator<Qualificator> templateQualIter = templateTypeQuals.listIterator(templateTypeQuals.size());
                        
                        if (templateQualIter.hasPrevious()) {
                            TypeInfoCollector targetTypeInfo = new CsmUtilities.TypeInfoCollector();
                            CsmType targetUnderlyingType = CsmUtilities.iterateTypeChain(targetType, targetTypeInfo);
                            List<Qualificator> targetTypeQuals = targetTypeInfo.qualificators;
                            ListIterator<Qualificator> targetQualIter = targetTypeQuals.listIterator(targetTypeQuals.size());
                            
                            // Qualificators must be changed
                            boolean qualsError = false;
                            while (templateQualIter.hasPrevious()) {
                                Qualificator paramQual = templateQualIter.previous();
                                Qualificator actualQual = null;
                                while (targetQualIter.hasPrevious() && !paramQual.equals(actualQual)) {
                                    actualQual = targetQualIter.previous();
                                }
                                if (paramQual.equals(actualQual)) {
                                    targetQualIter.remove();
                                } else if (!targetQualIter.hasPrevious()) {
                                    if (Qualificator.REFERENCE.equals(paramQual)) {
                                        continue;
                                    } else if (Qualificator.RVALUE_REFERENCE.equals(paramQual)) {
                                        continue;
                                    }
                                    if (strategy.canSkipError(DeduceTemplateTypeStrategy.Error.MatchQualsError)) {
                                        qualsError = true;
                                        break;
                                    } else {
                                        return null; // TODO: or existing results if they exists?
                                    }
                                }
                            }
                            if (qualsError) {
                                results.add(targetType);
                                continue;
                            }

                            List<Qualificator> remainingQualifiers = targetTypeQuals;

                            boolean newConst = remainingQualifiers.contains(Qualificator.CONST);
                            boolean newVolatile = remainingQualifiers.contains(Qualificator.VOLATILE);
                            int newPtrDepth = howMany(remainingQualifiers, Qualificator.POINTER);
                            int newArrayDepth = howMany(remainingQualifiers, Qualificator.ARRAY);
                            int newReference = CsmTypes.TypeDescriptor.NON_REFERENCE;
                            if (remainingQualifiers.contains(Qualificator.REFERENCE)) {
                                newReference =  CsmTypes.TypeDescriptor.REFERENCE;
                            } else if (remainingQualifiers.contains(Qualificator.RVALUE_REFERENCE)) {
                                newReference =  CsmTypes.TypeDescriptor.RVALUE_REFERENCE;
                            }
                            CsmTypes.TypeDescriptor td = new CsmTypes.TypeDescriptor(
                                    newConst,
                                    newVolatile,
                                    newReference,
                                    newPtrDepth,
                                    newArrayDepth
                            );

                            results.add(CsmTypes.createType(targetUnderlyingType, td));
                        } else {
                            results.add(targetType);
                        }
                    }
                    return results.toArray(new CsmType[results.size()]);
                }
            }
        }
        return null;
    }

    @Override
    public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params) {
        return instantiate(template, params, true);
    }

    public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, boolean specialize) {
        CsmResolveContext context = CsmResolveContext.getLast();
        CsmFile contextFile = (context != null) ? context.getFile() : null;
        int contextOffset = (context != null) ? context.getOffset() : 0;
        return instantiate(template, contextFile, contextOffset, params, specialize);
    }

    @Override
    public CsmObject instantiate(CsmTemplate template, CsmType type) {
        return instantiate(template, type, true);
    }

    public CsmObject instantiate(CsmTemplate template, CsmType type, boolean specialize) {
        CsmResolveContext context = CsmResolveContext.getLast();
        CsmFile contextFile = (context != null) ? context.getFile() : null;
        int contextOffset = (context != null) ? context.getOffset() : 0;
        return instantiate(template, contextFile, contextOffset, type.getInstantiationParams(), specialize);
    }

    public CsmObject instantiate(CsmTemplate template, CsmFile contextFile, int contextOffset, List<CsmSpecializationParameter> params, boolean specialize) {
        long time = System.currentTimeMillis();
        CsmCacheMap cache = getTemplateRelatedCache(template, specialize);
        Object key = new InstantiateListKey(params);
        boolean[] found = new boolean[] { false };
        CsmObject result = (CsmObject) CsmCacheMap.getFromCache(cache, key, found);
        boolean cached = true;
        if (result == null || found[0]) {
            cached = false;
            time = System.currentTimeMillis();
            LOG.log(Level.FINEST, "instantiate 1 {0}spec:{1};params={2}\n", new Object[]{template.getDisplayName(), specialize, params});
            result = template;
            if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunction(template) 
                    || CsmKindUtilities.isTypeAlias(template) || CsmKindUtilities.isVariable(template)) {
                if (contextFile == null) {
                    contextFile = ((CsmOffsetable) template).getContainingFile();
                    contextOffset = ((CsmOffsetable) template).getStartOffset();
                }
                if (hasVariadicParams(params)) {
                    params = expandVariadicParams(params);
                }
                List<CsmTemplateParameter> templateParams = template.getTemplateParameters();
                Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = new HashMap<>();
                Iterator<CsmSpecializationParameter> paramsIter = params.iterator();
                int i = 0;
                for (CsmTemplateParameter templateParam : templateParams) {
                    if(templateParam.isVarArgs() && i == templateParams.size() - 1) {
                        List<CsmSpecializationParameter> args = new ArrayList<>();
                        while(paramsIter.hasNext()) {
                            args.add(paramsIter.next());
                        }
                        mapping.put(templateParam, createVariadicSpecializationParameter(args, ((CsmOffsetableDeclaration)template).getContainingFile(), 0, 0));
                    } else if (paramsIter.hasNext()) {
                        mapping.put(templateParam, paramsIter.next());
                    } else {
                        CsmSpecializationParameter defaultValue = getTemplateParameterDefultValue(template, templateParam, i);
                        if (CsmKindUtilities.isTypeBasedSpecalizationParameter(defaultValue)) {
                            CsmType defaultType = ((CsmTypeBasedSpecializationParameter)defaultValue).getType();
                            defaultType = TemplateUtils.checkTemplateType(defaultType, template);
                            if (defaultType != null) {
                                CsmScope paramScope = null;
                                if (CsmKindUtilities.isScope(template)) {
                                    paramScope = (CsmScope) template;
                                } else if (CsmKindUtilities.isScopeElement(template)) {
                                    paramScope = ((CsmScopeElement) template).getScope();
                                }
                                mapping.put(templateParam, new TypeBasedSpecializationParameterImpl(defaultType, paramScope));
                            }
                        } else if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(defaultValue)) {
                            mapping.put(templateParam, defaultValue);
                        }
                    }
                    i++;
                }
                result = Instantiation.create(template, mapping);
                if (specialize) {
                    if (CsmKindUtilities.isClassifier(result)) {
                        CsmClassifier specialization = specialize((CsmClassifier) result, contextFile, contextOffset);
                        if (CsmKindUtilities.isTemplate(specialization)) {
                            result = (CsmTemplate) specialization;
                        }
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(result, time));
            }
        } else {
            time = System.currentTimeMillis() - time;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Instantiate 1 took {0}ms ({1})\n", new Object[]{time, cached ? "CACHE HIT" : "calculated"});
        }
        return result;
    }

    @Override
    public CsmObject instantiate(CsmTemplate template, CsmInstantiation instantiation) {
        return instantiate(template, instantiation, true);
    }

    public CsmObject instantiate(CsmTemplate template, CsmInstantiation instantiation, boolean specialize) {
        CsmResolveContext context = CsmResolveContext.getLast();
        CsmFile contextFile = (context != null) ? context.getFile() : null;
        int contextOffset = (context != null) ? context.getOffset() : 0;
        return instantiate(template, contextFile, contextOffset, instantiation.getMapping(), specialize);
    }

    public CsmObject instantiate(CsmTemplate template, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, boolean specialize) {
        CsmResolveContext context = CsmResolveContext.getLast();
        CsmFile contextFile = (context != null) ? context.getFile() : null;
        int contextOffset = (context != null) ? context.getOffset() : 0;
        return instantiate(template, contextFile, contextOffset, mapping, specialize);
    }

    public CsmObject instantiate(CsmTemplate template, CsmFile contextFile, int contextOffset, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, boolean specialize) {
        long time = System.currentTimeMillis();
        CsmCacheMap cache = getTemplateRelatedCache(template, specialize);
        Object key =  new InstantiateMapKey(mapping);
        boolean[] found = new boolean[] { false };
        CsmObject result = (CsmObject) CsmCacheMap.getFromCache(cache, key, found);
        boolean cached = true;
        if (result == null || found[0]) {
            cached = false;
            LOG.log(Level.FINEST, "instantiate 2 {0}; spec:{1};mapping={2}\n", new Object[]{template.getDisplayName(), specialize, mapping});
            result = template;
            time = System.currentTimeMillis();
            if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunctional(template) || CsmKindUtilities.isTypeAlias(template)) {
                if (contextFile == null) {
                    contextFile = ((CsmOffsetable) template).getContainingFile();
                    contextOffset = ((CsmOffsetable) template).getStartOffset();
                }
                result = Instantiation.create(template, mapping);
                if (specialize && CsmKindUtilities.isClassifier(result)) {
                    CsmClassifier specialization = specialize((CsmClassifier) result, contextFile, contextOffset);
                    if (CsmKindUtilities.isTemplate(specialization)) {
                        result = (CsmTemplate) specialization;
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(result, time));
            }
        } else {
            time = System.currentTimeMillis() - time;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Instantiate 2 took {0}ms ({1})\n", new Object[]{time, cached ? "CACHE HIT" : "calculated"});
        }
        return result;
    }

    @Override
    public CsmType instantiate(CsmTemplateParameter templateParam, List<CsmInstantiation> instantiations) {
        CsmSpecializationParameter resolvedParam = Instantiation.resolveTemplateParameter(templateParam, TemplateUtils.gatherMapping(instantiations));
        if (CsmKindUtilities.isTypeBasedSpecalizationParameter(resolvedParam)) {
            CsmType resolvedType = createTypeInstantiationForTypeParameter((CsmTypeBasedSpecializationParameter) resolvedParam, instantiations.iterator(), 0);
            return resolvedType != null ? resolvedType : (CsmTypeBasedSpecializationParameter) resolvedParam;
        }
        return null;
    }

    @Override
    public boolean isInstantiatedType(CsmType type) {
        return Instantiation.isInstantiatedType(type);
    }

    @Override
    public CsmInstantiation getInstantiatedTypeInstantiation(CsmType type) {
        return Instantiation.getInstantiatedTypeInstantiation(type);
    }

    @Override
    public List<CsmInstantiation> getInstantiatedTypeInstantiations(CsmType type) {
        return Instantiation.getInstantiatedTypeInstantiations(type);
    }

    @Override
    public CsmType getOriginalType(CsmType type) {
        return Instantiation.unfoldOriginalType(type);
    }

    @Override
    public CsmType getInstantiatedType(CsmType type) {
        return Instantiation.unfoldInstantiatedType(type);
    }

    @Override
    public boolean isViableInstantiation(CsmInstantiation instantiation, boolean acceptTemplateParams) {
        List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> params = getInstantiationParams(instantiation);
        InstantiationParametersInfo paramsInfo = new InstantiationParametersInfoImpl(instantiation, params);
        return isViableInstantiation(paramsInfo, acceptTemplateParams);
    }

    private boolean isViableInstantiation(InstantiationParametersInfo paramsInfo, boolean acceptTemplateParams) {
        for (CsmType paramType : paramsInfo.getParamsTypes()) {
            if (paramType != null) {
                if (acceptTemplateParams) {
                    paramType = CsmUtilities.iterateTypeChain(paramType, new UnfoldWhileNestedPredicate());
                    CsmClassifier paramCls = (paramType != null) ? paramType.getClassifier() : null;
                    if (paramCls == null || CsmBaseUtilities.isUnresolved(paramCls)) {
                        return false;
                    }
                } else {
                    CsmType lastType = CsmUtilities.iterateTypeChain(paramType, new CsmUtilities.ConstantPredicate<CsmType>(false));
                    if (CsmKindUtilities.isTemplateParameterType(lastType)) {
                        return false;
                    } else if (lastType != null) {
                        CsmClassifier paramCls = paramType.getClassifier();
                        if (paramCls == null || CsmBaseUtilities.isUnresolved(paramCls)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public CharSequence getOriginalText(CsmType type) {
        return Instantiation.getOriginalText(type);
    }

    @Override
    public CharSequence getInstantiatedText(CsmType type) {
        long time = System.currentTimeMillis();
        try {
            return Instantiation.getInstantiatedText(type);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                time = System.currentTimeMillis() - time;
                LOG.log(Level.FINE, "getInstantiatedText took {0}ms\n", new Object[]{time}); // NOI18N
            }
        }
    }

    @Override
    public CharSequence getTemplateSignature(CsmTemplate template) {
        long time = System.currentTimeMillis();
        LOG.log(Level.FINEST, "getTemplateSignature {0}\n", new Object[]{template});// NOI18N
        StringBuilder sb = new StringBuilder();
        if (CsmKindUtilities.isQualified(template)) {
            sb.append(((CsmQualifiedNamedElement)template).getQualifiedName());
        } else if (CsmKindUtilities.isNamedElement(template)) {
            sb.append(((CsmNamedElement)template).getName());
        } else {
            System.err.println("uknown template object " + template);
        }
        appendTemplateParamsSignature(template.getTemplateParameters(), sb);
        if (LOG.isLoggable(Level.FINE)) {
            time = System.currentTimeMillis() - time;
            LOG.log(Level.FINE, "getTemplateSignature took {0}ms\n", new Object[]{time}); // NOI18N
        }
        return sb;
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getSpecializations(CsmDeclaration templateDecl, CsmFile contextFile, int contextOffset) {
        long time = System.currentTimeMillis();
        CsmCacheMap cache = CsmCacheManager.getClientCache(TemplateSpecializationsKey.class, SPECIALIZATIONS_INITIALIZER);
        Object key = new TemplateSpecializationsKey(templateDecl, contextFile, contextOffset);
        Collection<CsmOffsetableDeclaration> specs = (Collection<CsmOffsetableDeclaration>) CsmCacheMap.getFromCache(cache, key, null);
        boolean cached = true;
        if (specs == null) {
            cached = false;
            LOG.log(Level.FINEST, "getSpecializations {0}\n", new Object[]{templateDecl});
            time = System.currentTimeMillis();
            specs = Collections.emptyList();
            if (CsmKindUtilities.isTemplate(templateDecl)) {
                if (contextFile == null && CsmKindUtilities.isOffsetable(templateDecl)) {
                    contextFile = ((CsmOffsetable)templateDecl).getContainingFile();
                }
                CsmProject proj = contextFile != null ? contextFile.getProject() : null;
                if (proj instanceof ProjectBase) {
                    StringBuilder fqn = new StringBuilder(templateDecl.getUniqueName());
                    fqn.append('<'); // NOI18N
                    Collection<CsmOffsetableDeclaration> decls = ((ProjectBase) proj).findDeclarationsByPrefix(fqn.toString());
                    specs = new ArrayList<>(decls.size());
                    for (CsmOffsetableDeclaration d : decls) {
                        if (!ForwardClass.isForwardClass(d)) {
                            specs.add(d);
                        }
                    }
                }
            } else if (CsmKindUtilities.isMethod(templateDecl)) {
                // try to find explicit specialization of method if any
                CsmClass cls = CsmBaseUtilities.getFunctionClass((CsmFunction) templateDecl);
                if (CsmKindUtilities.isTemplate(cls)) {
                    specs = new ArrayList<>();
                    CharSequence funName = templateDecl.getName();
                    Collection<CsmOffsetableDeclaration> specializations = getSpecializations(cls, contextFile, contextOffset);
                    for (CsmOffsetableDeclaration specialization : specializations) {
                        CsmTemplate spec = (CsmTemplate) specialization;
                        Iterator<CsmMember> classMembers = CsmSelect.getClassMembers((CsmClass) spec, CsmSelect.getFilterBuilder().createNameFilter(funName, true, true, false));
                        //if (spec.isExplicitSpecialization()) {
                        while(classMembers.hasNext()) {
                            CsmMember next = classMembers.next();
                            if (CsmKindUtilities.isFunctionDeclaration(next)) {
                                CsmFunctionDefinition definition = ((CsmFunction) next).getDefinition();
                                if (definition != null && !definition.equals(next)) {
                                    specs.add(definition);
                                }
                            }
                        }
                        //}
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(specs, time));
            }
        } else {
            if (specs.isEmpty()) {
                specs = Collections.emptyList();
            } else {
                specs = new ArrayList<>(specs);
            }
            time = System.currentTimeMillis() - time;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "getSpecializations took {0}ms ({1})\n", new Object[]{time, cached ? "CACHE HIT" : "calculated"});
        }

        return specs;
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getBaseTemplate(CsmDeclaration declaration) {
        long time = System.currentTimeMillis();
        CsmCacheMap cache = CsmCacheManager.getClientCache(BaseTemplateKey.class, BASE_TEMPLATE_INITIALIZER);
        Object key = new BaseTemplateKey(declaration);
        Collection<CsmOffsetableDeclaration> result = (Collection<CsmOffsetableDeclaration>) CsmCacheMap.getFromCache(cache, key, null);
        boolean cached = true;
        if (result == null) {
            cached = false;
            LOG.log(Level.FINEST, "getBaseTemplate {0}\n", new Object[]{declaration});
            time = System.currentTimeMillis();
            result = Collections.<CsmOffsetableDeclaration>emptyList();
            if (CsmKindUtilities.isSpecialization(declaration)) {
                if (CsmKindUtilities.isOffsetable(declaration) && CsmKindUtilities.isQualified(declaration)) {
                    CharSequence qualifiedName = ((CsmQualifiedNamedElement)declaration).getQualifiedName();
                    String removedSpecialization = qualifiedName.toString().replaceAll("<.*>", "");// NOI18N
                    CsmFile contextFile = ((CsmOffsetable) declaration).getContainingFile();
                    CsmProject proj = contextFile != null ? contextFile.getProject() : null;
                    Iterator<? extends CsmObject> decls = Collections.<CsmObject>emptyList().iterator();
                    if (CsmKindUtilities.isClass(declaration)) {
                        if (proj instanceof ProjectBase) {
                            decls = ((ProjectBase)proj).findClassifiers(removedSpecialization).iterator();
                        }
                    } else if (proj != null && CsmKindUtilities.isFunction(declaration)) {
                        String removedParams = removedSpecialization.replaceAll("\\(.*", "");// NOI18N
                        decls = CsmSelect.getFunctions(proj, removedParams);
                    }
                    result = new ArrayList<>();
                    while (decls.hasNext()) {
                        CsmObject decl = decls.next();
                        if (!CsmKindUtilities.isSpecialization(decl)) {
                            result.add((CsmOffsetableDeclaration) decl);
                        }
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(result, time));
            }
        } else {
            if (result.isEmpty()) {
                result = Collections.<CsmOffsetableDeclaration>emptyList();
            } else {
                result = new ArrayList<>(result);
            }
            time = System.currentTimeMillis() - time;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "getBaseTemplate took {0}ms ({1})\n", new Object[]{time, cached ? "CACHE HIT" : "calculated"});
        }
        return result;
    }

    private static final int PARAMETERS_LIMIT = 1000; // do not produce too long signature

    public static void appendParametersSignature(Collection<CsmParameter> params, StringBuilder sb) {
        sb.append('(');
        int limit = 0;
        for (Iterator<CsmParameter> iter = params.iterator(); iter.hasNext();) {
            if (limit >= PARAMETERS_LIMIT) {
                break;
            }
            limit++;
            CsmParameter param = iter.next();
            CsmType type = param.getType();
            if (type != null) {
                sb.append(type.getCanonicalText());
                if (iter.hasNext()) {
                    sb.append(',');
                }
            } else if (param.isVarArgs()) {
                sb.append("..."); // NOI18N
            }
        }
        sb.append(')');
    }

    public static void appendTemplateParamsSignature(List<CsmTemplateParameter> params, StringBuilder sb) {
        if (params != null && params.size() > 0) {
            sb.append('<');
            int limit = 0;
            for (Iterator<CsmTemplateParameter> iter = params.iterator(); iter.hasNext();) {
                if (limit >= PARAMETERS_LIMIT) {
                    break;
                }
                limit++;
                CsmTemplateParameter param = iter.next();
                if (CsmKindUtilities.isVariableDeclaration(param)) {
                    CsmVariable var = (CsmVariable) param;
                    CsmType type = var.getType();
                    if (type != null) {
                        sb.append(type.getCanonicalText());
                        if (iter.hasNext()) {
                            sb.append(',');
                        }
                    }
                }
                if (CsmKindUtilities.isClassifier(param)) {
                    CsmClassifier classifier = (CsmClassifier) param;
                    sb.append("class"); // NOI18N // Name of parameter does not matter
                    if (CsmKindUtilities.isTemplate(param)) {
                        appendTemplateParamsSignature(((CsmTemplate) classifier).getTemplateParameters(), sb);
                    }
                    if (iter.hasNext()) {
                        sb.append(',');
                    }
                }
            }
            TemplateUtils.addGREATERTHAN(sb);
        }
    }

    private CsmClassifier specialize(final CsmClassifier classifier, CsmFile contextFile, int contextOffset) {
        long time = System.currentTimeMillis();
        CsmClassifier specialization = null;
        if (threadLocalSpecializeAntiloop.get().enter(classifier)) {
            try {
                List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> params = getInstantiationParams(classifier);
                InstantiationParametersInfo paramsInfo = new InstantiationParametersInfoImpl(classifier, params);
                CsmCacheMap cache = getSpecializeCache();
                SpecializeRequest request = new SpecializeRequest(classifier, paramsInfo);
                CsmClassifier result = (CsmClassifier) CsmCacheMap.getFromCache(cache, request, null);
                if (result == null) {
                    if (CsmKindUtilities.isTemplate(classifier) && !CsmKindUtilities.isSpecialization(classifier)) {
                        List<CsmTemplateParameter> templateParams = ((CsmTemplate) classifier).getTemplateParameters();
                        List<ProjectBase> projects = null;
                        List<CsmOffsetableDeclaration> visibleSpecs = null;
                        if (params.size() == templateParams.size() && CsmKindUtilities.isClass(classifier)) {
                            projects = collectProjects(contextFile);
                            if (!projects.isEmpty()) {
                                // try to find full specialization of class
                                CsmClass cls = (CsmClass) classifier;
                                List<CsmSpecializationParameter> plainParams = getPlainParams(params);

                                if (checkAllowFastSearchFullSpecializations(plainParams)) {
                                    StringBuilder fqn = new StringBuilder(cls.getUniqueName());
                                    fqn.append(Instantiation.getInstantiationCanonicalText(plainParams));

                                    for (CsmProject proj : projects) {
                                        CsmDeclaration decl = proj.findDeclaration(fqn.toString());
                                        if(decl instanceof ClassImplSpecialization && CsmIncludeResolver.getDefault().isObjectVisible(contextFile, decl)) {
                                            specialization = (CsmClassifier) decl;
                                            break;
                                        }
                                    }
                                }

                                // try to find partial specialization of class
                                if (specialization == null) {
                                    visibleSpecs = collectVisibleSpecializations(cls, projects, contextFile);
                                    specialization = findBestSpecialization(visibleSpecs, paramsInfo, cls);
                                }
                            }
                        }
                        if (specialization == null && isClassForward(classifier)) {
                            // try to find specialization of class forward
                            CsmClass cls = (CsmClass) classifier;
                            if (projects == null) {
                                projects = collectProjects(contextFile);
                            }
                            if (visibleSpecs == null) {
                                visibleSpecs = collectVisibleSpecializations(cls, projects, contextFile);
                            }
                            for (CsmOffsetableDeclaration decl : visibleSpecs) {
                                if (decl instanceof ClassImplSpecialization) {
                                    ClassImplSpecialization spec = (ClassImplSpecialization) decl;
                                    specialization = spec;
                                    break;
                                }
                            }
                        }
                    }
                    if(specialization instanceof ClassImplSpecialization && !classifier.equals(specialization) &&
                            CsmKindUtilities.isTemplate(specialization) && CsmKindUtilities.isInstantiation(classifier)) {
                        // inherit mapping
                        List<CsmTemplateParameter> specTemplateParams = ((CsmTemplate)specialization).getTemplateParameters();

                        MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping = TemplateUtils.gatherMapping((CsmInstantiation) classifier);
                        Map<CsmTemplateParameter, CsmSpecializationParameter> newMapping = new HashMap<>();

                        for (CsmTemplateParameter specTemplateParam : specTemplateParams) {
                            CsmSpecializationParameter deducedParam = deduceSpecializationParam(
                                    specTemplateParam,
                                    (ClassImplSpecialization) specialization,
                                    paramsInfo
                            );
                            if (deducedParam != null) {
                                newMapping.put(specTemplateParam, deducedParam);
                            }
                        }

                        mapping.pop();
                        mapping.push(newMapping);

                        CsmObject obj = specialization;
                        List<Map<CsmTemplateParameter, CsmSpecializationParameter>> maps = mapping.getMaps(new MapHierarchy.NonEmptyFilter());
                        for (int i = maps.size() - 1; i >= 0; i--) {
                            obj = instantiate((CsmTemplate) obj, contextFile, contextOffset, maps.get(i), false);
                        }

                        if(CsmKindUtilities.isClassifier(obj)) {
                            specialization = (CsmClassifier) obj ;
                        }
                    }
                    time = System.currentTimeMillis() - time;
                    if (cache != null) {
                        cache.put(request, CsmCacheMap.toValue(specialization != null ? specialization : classifier, time));
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "CLASSIFIER\n{0}\nSPECIALIZED as {1}", new Object[] {classifier, specialization});
                    }
                } else {
                    time = System.currentTimeMillis() - time;
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "specialize took {0}ms ({1})\n", new Object[]{time, result != null ? "CACHE HIT" : "calculated"}); // NOI18N
                }
            } finally {
                threadLocalSpecializeAntiloop.get().exit(classifier);
            }
        }
        return specialization != null ? specialization : classifier;
    }

    private boolean checkAllowFastSearchFullSpecializations(List<CsmSpecializationParameter> params) {
        for (CsmSpecializationParameter param : params) {
            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                CsmTypeBasedSpecializationParameter typeBasedParam = (CsmTypeBasedSpecializationParameter) param;
                if (CsmKindUtilities.isTemplateParameterType(typeBasedParam.getType())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Deduces type/expression for template parameter of specialization.
     *
     * @param specTemplateParam - template param of specialization
     * @param specialization - specialization itself
     * @param paramsInfo - information about instantiation parameters
     * @return specialization parameter for the given template parameter
     */
    private CsmSpecializationParameter deduceSpecializationParam(CsmTemplateParameter specTemplateParam, ClassImplSpecialization specialization, InstantiationParametersInfo paramsInfo) {
        ListIterator<CsmSpecializationParameter> specParamIter = specialization.getSpecializationParameters().listIterator();
        ListIterator<CsmSpecializationParameter> instParamIter = paramsInfo.getInstParams().listIterator();
        ListIterator<CsmType> instParamTypeIter = paramsInfo.getParamsTypes().listIterator();
        ListIterator<String> instParamTextIter = paramsInfo.getParamsTexts().listIterator();
        while (specParamIter.hasNext() && instParamIter.hasNext() && instParamTypeIter.hasNext() && instParamTextIter.hasNext()) {
            CsmSpecializationParameter specParam = specParamIter.next();
            CsmSpecializationParameter instParam = instParamIter.next();
            CsmType instType = instParamTypeIter.next();
            String instParamText = instParamTextIter.next();
            if (specTemplateParam.isTypeBased() && CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam) && instType != null) {
                CsmType specParamType = ((CsmTypeBasedSpecializationParameter) specParam).getType();
                List<CsmType> results = new ArrayList<>(2);
                DefaultDeduceTemplateTypeStrategy calcStrategy = new DefaultDeduceTemplateTypeStrategy(DeduceTemplateTypeStrategy.Error.MatchQualsError);
                CsmType deduced[] = deduceTemplateType(specTemplateParam, specParamType, instType, calcStrategy);
                if (deduced != null && deduced.length > 0) {
                    results.addAll(Arrays.asList(deduced));
                    if (specTemplateParam.isVarArgs()
                            && instParamIter.hasNext()
                            && CsmBaseUtilities.isValid(specParamType.getClassifier())
                            && CharSequenceUtilities.textEquals(specTemplateParam.getQualifiedName(), specParamType.getClassifier().getQualifiedName()))
                    {
                        // 1. specTemplateParam is variadic
                        // 2. specTemplateParam appears in specialization parameters as is. (Except qualifiers maybe)
                        // 3. there are more instantiation parameters
                        // All that means that specTemplateParam must contain other instantiation params
                        while (instParamIter.hasNext() && instParamTypeIter.hasNext() && instParamTextIter.hasNext()) {
                            instParam = instParamIter.next();
                            instType = instParamTypeIter.next();
                            instParamText = instParamTextIter.next();
                            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(instParam) && instType != null) {
                                deduced = deduceTemplateType(specTemplateParam, specParamType, instType, calcStrategy);
                                if (deduced != null && deduced.length > 0) {
                                    results.addAll(Arrays.asList(deduced));
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
                if (results.size() > 0) {
                    int originalInstDepth = Instantiation.getInstantiationDepth(instType);
                    if (!specTemplateParam.isVarArgs()) {
                        CsmType unfolded = Instantiation.unfoldInstantiatedType(results.get(0), originalInstDepth);
                        return createTypeBasedSpecializationParameter(unfolded, specParam.getScope());
                    } else {
                        List<CsmSpecializationParameter> varArgs = new ArrayList<>();
                        for (CsmType result : results) {
                            CsmType unfolded = Instantiation.unfoldInstantiatedType(result, originalInstDepth);
                            varArgs.add(createTypeBasedSpecializationParameter(unfolded, specParam.getScope()));
                        }
                        return createVariadicSpecializationParameter(varArgs, specParam.getContainingFile(), 0, 0);
                    }
                }
            } else if (!specTemplateParam.isTypeBased() && CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam)) {
                CsmType specParamType = ((CsmTypeBasedSpecializationParameter) specParam).getType();
                String specParamText = specParamType != null ? specParamType.getCanonicalText().toString() : null;
                if (specTemplateParam.getName().toString().equals(specParamText)) {
                    return createExpressionBasedSpecializationParameter(
                        instParamText,
                        specParam.getScope(),
                        instParam.getContainingFile(),
                        instParam.getStartOffset(),
                        instParam.getEndOffset()
                    );
                }
            } else if (!specTemplateParam.isTypeBased() && CsmKindUtilities.isExpressionBasedSpecalizationParameter(specParam)) {
                String specParamText = specParam.getText().toString();
                if (specTemplateParam.getName().toString().equals(specParamText)) {
                    return createExpressionBasedSpecializationParameter(
                        instParamText,
                        specParam.getScope(),
                        instParam.getContainingFile(),
                        instParam.getStartOffset(),
                        instParam.getEndOffset()
                    );
                }
            }
        }
        if (specTemplateParam.isVarArgs()) {
            return createVariadicSpecializationParameter(
                    Collections.<CsmSpecializationParameter>emptyList(),
                    specTemplateParam.getContainingFile(),
                    0,
                    0
            );
        } else {
            if (specTemplateParam.getDefaultValue() != null) {
                return specTemplateParam.getDefaultValue();
            }
        }
        return null;
    }

    /**
     * Collects projects to find specialization in
     *
     * @param contextFile - start file
     *
     * @return list of projects. First element in the list is the project which contains start file
     */
    private List<ProjectBase> collectProjects(CsmFile contextFile) {
        List<ProjectBase> projects = new ArrayList<>(4);

        CsmProject project = contextFile.getProject();

        if (project instanceof ProjectBase) {
            projects.add((ProjectBase) project);
        }

        if (project != null && !project.isArtificial()) {
            for (CsmProject libProject : project.getLibraries()) {
                if (libProject instanceof ProjectBase) {
                    projects.add((ProjectBase) libProject);
                }
            }
        }

        return projects;
    }

    private List<CsmOffsetableDeclaration> collectVisibleSpecializations(CsmClass cls, List<ProjectBase> projects, CsmFile contextFile) {
        List<CsmOffsetableDeclaration> specs = new ArrayList<>();

        for (ProjectBase proj : projects) {
            StringBuilder fqn = new StringBuilder();
            fqn.append(Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS));
            fqn.append(OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR);
            fqn.append(cls.getQualifiedName());
            fqn.append('<'); // NOI18N
            specs.addAll(proj.findDeclarationsByPrefix(fqn.toString()));
            fqn.setLength(0);
            fqn.append(Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.STRUCT));
            fqn.append(OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR);
            fqn.append(cls.getQualifiedName());
            fqn.append('<'); // NOI18N
            specs.addAll(proj.findDeclarationsByPrefix(fqn.toString()));
        }

        List<CsmOffsetableDeclaration> visibleSpecs = new ArrayList<>();
        for (CsmOffsetableDeclaration spec : specs) {
            if(CsmIncludeResolver.getDefault().isObjectVisible(contextFile, spec)) {
                visibleSpecs.add(spec);
            }
        }

        return visibleSpecs;
    }

    private CsmClassifier findBestSpecialization(Collection<CsmOffsetableDeclaration> specializations, InstantiationParametersInfo paramsInfo, final CsmClassifier cls) {
        // TODO : update

        CsmClassifier bestSpecialization = null;

        boolean variadic = paramsInfo.isVariadic();

        if (!specializations.isEmpty()) {
            final boolean tryFullResolve = !CsmKindUtilities.isInstantiation(cls) 
                || (!Instantiation.isTemplateBasedInstantiation((CsmInstantiation) cls) && !Instantiation.isRecursiveInstantiation(cls));
            int bestMatch = 0;
            int paramsSize = 0;
            for (Pair<CsmSpecializationParameter, List<CsmInstantiation>> pair : paramsInfo.getExpandedParams()) {
                CsmSpecializationParameter param = pair.first();
                if(CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                    paramsSize += ((CsmVariadicSpecializationParameter)param).getArgs().size();
                } else {
                    paramsSize++;
                }
            }

            List<CsmSpecializationParameter> instParams = paramsInfo.getInstParams();
            List<String> paramsText = paramsInfo.getParamsTexts();
            List<CsmType> paramsType = paramsInfo.getParamsTypes();

            for (CsmOffsetableDeclaration decl : specializations) {
                if (decl instanceof ClassImplSpecialization) {
                    ClassImplSpecialization specialization = (ClassImplSpecialization) decl;
                    List<CsmSpecializationParameter> specParams = specialization.getSpecializationParameters();
                    int match = 0;
                    if (variadic) {
                        if (hasVariadicParams(specialization)) {
                            if (specParams.size() - 1  <= paramsSize) {
                                match += specParams.size() - 1;
                            }
                        } else {
                            if (specParams.size() == paramsSize) {
                                match += specParams.size() + 1;
                            }
                        }
                    }
                    if (specParams.size() == paramsSize) {
                        for (int i = 0; i < paramsSize - 1; i++) {
                            CsmSpecializationParameter specParam1 = specParams.get(i);
                            CsmSpecializationParameter param1 = instParams.get(i);
//                            for (int j = i + 1; j < paramsSize; j++) {
                            int j = i + 1;
                                CsmSpecializationParameter specParam2 = specParams.get(j);
                                CsmSpecializationParameter param2 = instParams.get(j);
                                if (specParam1.getText().toString().equals(specParam2.getText().toString())
                                        && param1.getText().toString().equals(param2.getText().toString())) {
                                    match += 1;
                                }
                                if(TraceFlags.EXPRESSION_EVALUATOR_EXTRA_SPEC_PARAMS_MATCHING) {
                                    // it works but does it too slow
                                    if(specParam1.getText().toString().equals(specParam2.getText().toString()) &&
                                            CsmKindUtilities.isTypeBasedSpecalizationParameter(param1) &&
                                            CsmKindUtilities.isTypeBasedSpecalizationParameter(param2)) {
                                        CsmType type1 = paramsType.get(i);
                                        CsmType type2 = paramsType.get(j);
                                        if (CsmUtilities.checkTypesEqual(
                                                type1, param1.getContainingFile(),
                                                type2, param2.getContainingFile(),
                                                new CsmUtilities.AlwaysEqualQualsEqualizer(),
                                                tryFullResolve
                                            )) 
                                        {
                                            match += 1;
                                        }
                                    }
                                }
//                            }
                        }
                        for (int i = 0; i < paramsSize; i++) {
                            CsmSpecializationParameter specParam = specParams.get(i);
                            CsmSpecializationParameter param = instParams.get(i);
                            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam) &&
                                    CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                                CsmTypeBasedSpecializationParameter typeSpecParam = (CsmTypeBasedSpecializationParameter) specParam;
                                CsmClassifier specParamCls = typeSpecParam.getClassifier();
                                if (specParamCls != null && !CsmKindUtilities.isTemplateParameter(specParamCls)) {
                                    String specClsQualifiedName = specParamCls.getQualifiedName().toString();
                                    if (specClsQualifiedName.equals(paramsText.get(i))) {
                                        match += 2;
                                    } else if (specParamCls.isValid()) {
                                        int matchValue = 0;
                                        final Set<String> specParamNames = getNestedTypeNames(typeSpecParam.getType(), tryFullResolve);
                                        final Set<String> instParamNames = getNestedTypeNames(paramsType.get(i), tryFullResolve);
                                        for (String specParamName : specParamNames) {
                                            if (instParamNames.contains(specParamName)) {
                                                matchValue = 2;
                                                break;
                                            }
                                        }
                                        if (matchValue == 0) {
                                            for (String specParamName : specParamNames) {
                                                for (String instParamName : instParamNames) {
                                                    matchValue = getQualifiedNamesMatchValue(instParamName, specParamName);
                                                    if (matchValue > 0) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (matchValue > 0) {
                                            match += matchValue;
                                        }/* else {
                                        match -= 2;
                                        }*/
                                    }
                                }
                                CsmFunctionPointerType declSpecFunType = tryGetFunctionPointerType(typeSpecParam.getType());
                                if (CsmKindUtilities.isFunctionPointerType(declSpecFunType)) {
                                    CsmFunctionPointerType paramFunType = tryGetFunctionPointerType(paramsType.get(i));
                                    if (CsmKindUtilities.isFunctionPointerType(paramFunType)) {
                                        match += 1;
                                        // TODO: check below should be eliminated after implementing
                                        // checking of viability of specialization above.
                                        if (declSpecFunType.getParameters().size() == paramFunType.getParameters().size()) {
                                            match += 1;
                                        }
                                        if (((CsmType)declSpecFunType).getPointerDepth() == ((CsmType)paramFunType).getPointerDepth()) {
                                            match += 1;
                                        }
                                    }
                                } else if (isPointer(typeSpecParam.getType()) && isPointer(paramsType.get(i))) {
                                    match += 1;
                                }
                                if (typeSpecParam.isReference()) {
                                    int checkReference = isReference(paramsType.get(i));
                                    if (checkReference > 0) {
                                        match += 1;
                                        if ((checkReference == CsmTypes.TypeDescriptor.RVALUE_REFERENCE) == typeSpecParam.isRValueReference()) {
                                            match +=1;
                                        }
                                    }
                                }
                            } else if (isExpressionParameter(cls, specParam, i)) {
                                match += evaluateExpression(cls, (ClassImplSpecialization) decl, specParam, i, paramsInfo);
                            } else {
                                match = 0;
                                break;
                            }
                        }
                    }
                    if (match > bestMatch) {
                        bestMatch = match;
                        bestSpecialization = (CsmClassifier) decl;
                    }
                }
            }
        }
        return bestSpecialization;
    }

    private static Set<String> getNestedTypeNames(CsmType instSpecParam, final boolean resolveTypeChain) {
        // TODO: it seems to be good idea to do caching in iterateTypeChain method instead of this local cache.
        CsmCacheMap cache = getNestedTypesCache();
        NestedTypesRequest request = new NestedTypesRequest(instSpecParam, resolveTypeChain);
        Set<String> result = (Set<String>) CsmCacheMap.getFromCache(cache, request, null);
        if (result == null) {
            long time = System.currentTimeMillis();
            final Set<String> nestedQualifiedNames = new HashSet<>();
            CsmUtilities.iterateTypeChain(instSpecParam, new CsmUtilities.Predicate<CsmType>() {
                @Override
                public boolean check(CsmType value) {
                    CsmClassifier classifier = value.getClassifier();
                    if (classifier != null) {
                        if (!nestedQualifiedNames.add(classifier.getQualifiedName().toString())) {
                            return true;
                        }
                    }
                    return !resolveTypeChain;
                }
            });
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(request, CsmCacheMap.toValue(nestedQualifiedNames, time));
            }
            return nestedQualifiedNames;
        } else {
            return result;
        }
    }

    private static int getQualifiedNamesMatchValue(String qn1, String qn2) {
        if (qn1 != null && qn2 != null) {
            if (qn1.equals(qn2)) {
                return 2;
            } else if (qn1.length() > qn2.length() && qn1.startsWith(qn2)) {
                final boolean isLastQualifiedPart = qn1.indexOf(APTUtils.SCOPE, qn2.length()) == -1;
                final boolean isSpecialization = (LT == qn1.charAt(qn2.length()));
                return isLastQualifiedPart && isSpecialization ? 1 : 0;
            } else if (qn2.length() > qn1.length() && qn2.startsWith(qn1)) {
                final boolean isLastQualifiedPart = qn2.indexOf(APTUtils.SCOPE, qn1.length()) == -1;
                final boolean isSpecialization = (LT == qn2.charAt(qn1.length()));
                return isLastQualifiedPart && isSpecialization ? 1 : 0;
            }
        }
        return 0;
    }

    private static boolean isExpressionParameter(CsmClassifier cls, CsmSpecializationParameter specParam, int specParamIndex) {
        if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(specParam)) {
            return true;
        }
        if (CsmKindUtilities.isTemplate(cls)) {
            List<CsmTemplateParameter> templateParams = ((CsmTemplate) cls).getTemplateParameters();
            if (templateParams != null && templateParams.size() > specParamIndex) {
                CsmTemplateParameter param = templateParams.get(specParamIndex);
                if (param != null && !param.isTypeBased()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int evaluateExpression(final CsmClassifier cls, final ClassImplSpecialization spec, CsmSpecializationParameter specParam, int instParamIndex, InstantiationParametersInfo paramsInfo) {
        List<String> instParamsText = paramsInfo.getParamsTexts();
        List<CsmSpecializationParameter> instParams = paramsInfo.getInstParams();

        // Get specialization parameter text and the same time check if it is specialized
        String specParamText = null;
        CsmTemplateParameter relatedSpecTemplateParam = null;
        if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(specParam)) {
            specParamText = specParam.getText().toString();
            for (CsmTemplateParameter specTemplateParam : spec.getTemplateParameters()) {
                if (!specTemplateParam.isTypeBased()) {
                    if (specTemplateParam.getName().toString().equals(specParamText)) {
                        relatedSpecTemplateParam = specTemplateParam;
                        break;
                    }
                }
            }
        } else if (CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam)) {
            CsmType specParamType = ((CsmTypeBasedSpecializationParameter)specParam).getType();
            CharSequence canonicalText = specParamType.getCanonicalText();
            if (canonicalText != null) {
                specParamText = canonicalText.toString();
            }
            if (CsmKindUtilities.isTemplateParameterType(specParamType)) {
                relatedSpecTemplateParam = ((CsmTemplateParameterType) specParamType).getParameter();
            }
        }
        if (relatedSpecTemplateParam != null && !relatedSpecTemplateParam.isTypeBased()) {
            // parameter is not specialized actually,
            // but it is correct at least
            return 1;
        }
        if (specParamText != null) {
            if (instParamsText.get(instParamIndex).equals(specParamText)) {
                return 2;
            }
            // Expression evaluation
            if (TraceFlags.EXPRESSION_EVALUATOR) {
                CsmExpressionEvaluatorProvider p = CsmExpressionEvaluator.getProvider();
                if (CsmKindUtilities.isInstantiation(cls)) {
                    final Object val1;
                    final Object val2;
                    List<CsmTemplateParameter> templateParameters = ((CsmTemplate) ((CsmInstantiation) cls).getTemplateDeclaration()).getTemplateParameters();
                    CsmTemplateParameter templateParameter = (templateParameters != null && templateParameters.size() > instParamIndex) ? templateParameters.get(instParamIndex) : null;
                    if (templateParameter != null) {
                        if(p instanceof ExpressionEvaluator) {
                             val1 = ((ExpressionEvaluator)p).eval(
                                 templateParameter.getName().toString(),
                                 (CsmInstantiation) cls,
                                 CsmKindUtilities.isScope(cls) ? (CsmScope) cls : cls.getScope()
                             );
                             val2 = ((ExpressionEvaluator)p).eval(
                                     specParamText,
                                     (CsmInstantiation) cls,
                                     specParam.getScope()
                             );
                        } else {
                             val1 = p.eval(
                                 templateParameter.getName().toString(),
                                 (CsmInstantiation) cls,
                                 CsmKindUtilities.isScope(cls) ? (CsmScope) cls : cls.getScope()
                             );
                             val2 = p.eval(
                                     specParamText,
                                     (CsmInstantiation) cls,
                                     specParam.getScope()
                             );
                        }
                        if (p.isValid(val1) && p.isValid(val2) && val1.equals(val2)) {
                            return 2;
                        }
                    } else {
                        LOG.log(Level.WARNING, "Not found template parameter with index {0} in {1}",  // NOI18N
                            new Object[]{instParamIndex, ((CsmInstantiation) cls).getTemplateDeclaration().getQualifiedName()}
                        );
                    }
                } else {
                    final Object val1;
                    final Object val2;
                    if(p instanceof ExpressionEvaluator) {
                        val1 = ((ExpressionEvaluator)p).eval(instParamsText.get(instParamIndex), instParams.get(instParamIndex).getScope());
                        val2 = ((ExpressionEvaluator)p).eval(specParamText, specParam.getScope());
                    } else {
                        val1 = p.eval(instParamsText.get(instParamIndex), instParams.get(instParamIndex).getScope());
                        val2 = p.eval(specParamText, specParam.getScope());
                    }
                    if (p.isValid(val1) && p.isValid(val2) && val1.equals(val2)) {
                        return 2;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Instantiates parameter type with appropriate mappings.
     *
     * @param param - specialization parameter
     * @param instantiation - whole instantiation
     * @return instantiated type or null
     */
    public static CsmType createTypeInstantiationForTypeParameter(CsmTypeBasedSpecializationParameter param, Iterator<CsmInstantiation> instantiations, int level) {
        if (level > 10 || !instantiations.hasNext()) {
            return null;
        }

        CsmInstantiation instantiation = instantiations.next();

        Collection<CsmSpecializationParameter> parameters = instantiation.getMapping().values();

        for (CsmSpecializationParameter parameter : parameters) {
            if (parameter == param) {
                return param.getType(); // paramater has been found
            }
        }

        CsmType instantiatedType = createTypeInstantiationForTypeParameter(param, instantiations, level + 1);

        if (instantiatedType != null) {
            // parameter found and we are instantiating it
            return Instantiation.createType((CsmType) instantiatedType, instantiation);
        }

        return null; // parameter not found
    }

    private boolean isClassForward(CsmClassifier cls) {
        while (CsmKindUtilities.isInstantiation(cls)) {
            CsmOffsetableDeclaration decl = ((CsmInstantiation) cls).getTemplateDeclaration();
            if (CsmKindUtilities.isClassifier(cls)) {
                cls = (CsmClassifier) decl;
            } else {
                break;
            }
        }
        return ForwardClass.isForwardClass(cls);
    }

    @Override
    public CsmTypeBasedSpecializationParameter createTypeBasedSpecializationParameter(CsmType type, CsmScope scope) {
        return new TypeBasedSpecializationParameterImpl(type, scope);
    }

    @Override
    public CsmTypeBasedSpecializationParameter createTypeBasedSpecializationParameter(CsmType type, CsmScope scope, CsmFile file, int start, int end) {
        return new TypeBasedSpecializationParameterImpl(type, scope, file, start, end);
    }

    @Override
    public CsmVariadicSpecializationParameter createVariadicSpecializationParameter(List<CsmSpecializationParameter> args, CsmFile file, int start, int end) {
        return new VariadicSpecializationParameterImpl(args, file, start, end);
    }

    @Override
    public CsmExpressionBasedSpecializationParameter createExpressionBasedSpecializationParameter(String expression, CsmScope scope, CsmFile file, int start, int end) {
        return ExpressionBasedSpecializationParameterImpl.create(expression, scope, file, start, end);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("SF")
    public static List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> getInstantiationParams(CsmObject o) {
        if (!CsmKindUtilities.isInstantiation(o)) {
            return Collections.emptyList();
        }
        long time = System.currentTimeMillis();
        List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> res = new ArrayList<>();
        CsmInstantiation i = (CsmInstantiation) o;
        Map<CsmTemplateParameter, CsmSpecializationParameter> m = i.getMapping();
        CsmOffsetableDeclaration decl = i.getTemplateDeclaration();
        if(!CsmKindUtilities.isInstantiation(decl)) {
            // first inst
            if(CsmKindUtilities.isTemplate(decl)) {
                for (CsmTemplateParameter tp : ((CsmTemplate)decl).getTemplateParameters()) {
                    CsmSpecializationParameter sp = m.get(tp);
                    if(sp != null) {
                        List<CsmInstantiation> insts = new ArrayList<>();
                        insts.add(i);
                        res.add(Pair.of(sp, insts));
                    }
                }
            }
        } else {
            // non first inst
            List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> sps = getInstantiationParams(decl);
            for (Pair<CsmSpecializationParameter, List<CsmInstantiation>> pair : sps) {
                CsmInstantiation currentInstantiation = i;
                CsmSpecializationParameter instParam = pair.first();
                List<CsmInstantiation> instantiations = pair.second();
                if (CsmKindUtilities.isVariadicSpecalizationParameter(instParam)) {
                    CsmVariadicSpecializationParameter variadicParam = (CsmVariadicSpecializationParameter) instParam;
                    List<CsmSpecializationParameter> processedArgs = new ArrayList<>();
                    VariadicRemapping remapped = getVariadicInstantiationParams(variadicParam, m, processedArgs);
                    switch (remapped) {
                        case ALL:
                            instantiations.clear();
                            // fall through
                        case SOME: {
                            CsmSpecializationParameter newVariadicParam = new VariadicSpecializationParameterImpl(
                                    processedArgs,
                                    variadicParam.getContainingFile(),
                                    variadicParam.getStartOffset(),
                                    variadicParam.getEndOffset()
                            );
                            res.add(Pair.of(newVariadicParam, instantiations));
                            break;
                        }
                        case NONE: {
                            res.add(pair);
                            break;
                        }
                    }
                } else if (isTemplateSpecParameter(instParam)) {
                    CsmTemplateParameterType paramType = (CsmTemplateParameterType) ((CsmTypeBasedSpecializationParameter) instParam).getType();
                    CsmType asType;
                    if (CsmKindUtilities.isType(paramType)) {
                        asType = (CsmType) paramType;
                    } else {
                        asType = paramType.getTemplateType();
                    }
                    // TODO: if it is ok for performance, then no remapping of type should be done here!
                    // Always execute res.add(pair) instruction.
                    if (!asType.isPointer() && !asType.isConst() 
                            && !asType.isVolatile() && !asType.isReference() 
                            && !asType.isRValueReference()) {
                        // TODO: paramResolver should be used in case of variadic parameters (template parameter packs) as well.
                        TemplateParameterResolver paramResolver = new Instantiation.TemplateParameterResolver();
                        CsmSpecializationParameter newTp = paramResolver.resolveTemplateParameter(paramType.getParameter(), new MapHierarchy<>(m));
                        if (newTp != null && newTp != instParam) {
                            instantiations.clear();
                            currentInstantiation = paramResolver.alterInstantiation(currentInstantiation);
                            res.add(Pair.of(newTp, instantiations));
                        } else {
                            res.add(pair);
                        }
                    } else {
                        res.add(pair);
                    }
                } else {
                    res.add(pair);
                }
                if (currentInstantiation != null) {
                    instantiations.add(currentInstantiation);
                }
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            time = System.currentTimeMillis() - time;
            LOG.log(Level.FINE, "getInstantiationParams took {0}ms\n", new Object[]{time});// NOI18N
        }
        return res;
    }

    public static List<CsmSpecializationParameter> getPlainParams(List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> instParams) {
        List<CsmSpecializationParameter> params = new ArrayList<>(instParams.size());
        for (Pair<CsmSpecializationParameter, List<CsmInstantiation>> pair : instParams) {
            params.add(pair.first());
        }
        return params;
    }

    public static interface InstantiationParametersInfo {

        List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> getOriginalParams();

        List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> getExpandedParams();

        // True if those parameters were collected for template with template parameter pack
        boolean isVariadic();

        List<CsmSpecializationParameter> getInstParams();

        List<CsmType> getParamsTypes();

        List<String> getParamsTexts();
    }

    public static final class InstantiationParametersInfoImpl implements InstantiationParametersInfo {

        private final CsmObject obj;

        private final List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> originalParams;

        private final List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> expandedParams;

        private final boolean variadic;

        private List<CsmSpecializationParameter> instParams;

        private List<CsmType> paramsTypes;

        private List<String> paramsText;

        public InstantiationParametersInfoImpl(CsmObject obj, List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> originalParams) {
            this.obj = obj;
            this.originalParams = originalParams;
            if (CsmKindUtilities.isTemplate(obj)) {
                List<CsmTemplateParameter> templateParams = ((CsmTemplate) obj).getTemplateParameters();
                if (!templateParams.isEmpty() && templateParams.get(templateParams.size() - 1).isVarArgs()) {
                    this.variadic = true;
                } else {
                    this.variadic = false;
                }
            } else {
                this.variadic = false;
            }
            if (hasVariadicParameters(originalParams)) {
                this.expandedParams = expandVariadicParameters(originalParams);
            } else {
                this.expandedParams = this.originalParams;
            }
        }

        @Override
        public List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> getOriginalParams() {
            return Collections.unmodifiableList(originalParams);
        }

        @Override
        public List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> getExpandedParams() {
            return Collections.unmodifiableList(expandedParams);
        }

        @Override
        public boolean isVariadic() {
            return variadic;
        }

        @Override
        public List<CsmSpecializationParameter> getInstParams() {
            ensureInitialized();
            return Collections.unmodifiableList(instParams);
        }

        @Override
        public List<CsmType> getParamsTypes() {
            ensureInitialized();
            return Collections.unmodifiableList(paramsTypes);
        }

        @Override
        public List<String> getParamsTexts() {
            ensureInitialized();
            return Collections.unmodifiableList(paramsText);
        }

        private void ensureInitialized() {
            if (instParams == null || paramsTypes == null || paramsText == null) {
                this.instParams = new ArrayList<>();
                this.paramsTypes = new ArrayList<>();
                this.paramsText = new ArrayList<>();
                for (Pair<CsmSpecializationParameter, List<CsmInstantiation>> pair : expandedParams) {
                    CsmSpecializationParameter param = pair.first();
                    if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                        CsmType paramType = ((CsmTypeBasedSpecializationParameter) param).getType();

                        if (CsmKindUtilities.isInstantiation(obj)) {
                            paramType = Instantiation.createType(paramType, pair.second());
                        }

                        this.instParams.add(param);
                        this.paramsTypes.add(paramType);
                        CharSequence paramText = paramType.getCanonicalText();
                        this.paramsText.add(paramText != null ? paramText.toString() : ""); // NOI18N
                    } else if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(param)) {
                        this.instParams.add(param);
                        this.paramsTypes.add(null);
                        CharSequence paramText = ((CsmExpressionBasedSpecializationParameter) param).getText();
                        this.paramsText.add(paramText != null ? paramText.toString() : ""); // NOI18N
                    }
                }
            }
        }

        private static boolean hasVariadicParameters(List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> params) {
            for (Pair<CsmSpecializationParameter, List<CsmInstantiation>> pair : params) {
                CsmSpecializationParameter param = pair.first();
                if(CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                    return true;
                }
            }
            return false;
        }

        private static List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> expandVariadicParameters(List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> params) {
            List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> params2 = new ArrayList<>();
            for (Pair<CsmSpecializationParameter, List<CsmInstantiation>> pair : params) {
                CsmSpecializationParameter param = pair.first();
                if(CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                    for (CsmSpecializationParameter arg : ((CsmVariadicSpecializationParameter)param).getArgs()) {
                        params2.add(Pair.of(arg, pair.second()));
                    }
                } else {
                    params2.add(pair);
                }
            }
            return params2;
        }
    }

    private static enum VariadicRemapping {
        ALL,
        SOME,
        NONE
    }

    private static VariadicRemapping getVariadicInstantiationParams(CsmVariadicSpecializationParameter variadicParam, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, List<CsmSpecializationParameter> results) {
        boolean hasRemapped = false;
        boolean hasNotRemapped = false;
        List<CsmSpecializationParameter> expanded = expandVariadicParams(Arrays.<CsmSpecializationParameter>asList(variadicParam));
        for (CsmSpecializationParameter argParam : expanded) {
            if (isTemplateSpecParameter(argParam)) {
                CsmTemplateParameterType paramType = (CsmTemplateParameterType) ((CsmTypeBasedSpecializationParameter) argParam).getType();
                CsmSpecializationParameter newSpecParam = mapping.get(paramType.getParameter());
                if (newSpecParam != null && newSpecParam != argParam) {
                    if (CsmKindUtilities.isVariadicSpecalizationParameter(newSpecParam)) {
                        // TODO: do we need to expand it?
                        for (CsmSpecializationParameter param : ((CsmVariadicSpecializationParameter) newSpecParam).getArgs()) {
                            results.add(param);
                        }
                    } else {
                        results.add(newSpecParam);
                    }
                    hasRemapped = true;
                } else {
                    results.add(argParam);
                    hasNotRemapped = true;
                }
            } else {
                hasNotRemapped = true;
            }
        }
        if (hasRemapped && hasNotRemapped) {
            return VariadicRemapping.SOME;
        }
        return hasRemapped ? VariadicRemapping.ALL : VariadicRemapping.NONE;
    }

    private static boolean isTemplateSpecParameter(CsmSpecializationParameter param) {
        return CsmKindUtilities.isTypeBasedSpecalizationParameter(param) &&
               CsmKindUtilities.isTemplateParameterType(((CsmTypeBasedSpecializationParameter) param).getType());
    }

    private CsmClassForwardDeclaration findCsmClassForwardDeclaration(CsmClass cls) {
        if (TraceFlags.INSTANTIATION_FULL_FORWARDS_SEARCH) {
            CsmScope scope = cls.getScope();
            if (CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace) scope;
                if (!ns.isGlobal() && !ns.getDefinitions().isEmpty()) {
                    for (CsmNamespaceDefinition definition : ns.getDefinitions()) {
                        CsmClassForwardDeclaration result = findCsmClassForwardDeclaration(definition, cls);
                        if (result != null) {
                            return result;
                        }
                    }
                    return null;
                }
            }
        }
        return findCsmClassForwardDeclaration(cls.getContainingFile(), cls);
    }

    private CsmClassForwardDeclaration findCsmClassForwardDeclaration(CsmScope scope, CsmClass cls) {
        if (scope != null) {
            if (CsmKindUtilities.isFile(scope)) {
                CsmFile file = (CsmFile) scope;
                CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION);
                Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(file, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClass fwdCls = ((CsmClassForwardDeclaration) decl).getCsmClass();
                    if (fwdCls != null && fwdCls.equals(cls)) {
                        return (CsmClassForwardDeclaration) decl;
                    }
                }
                filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION);
                declarations = CsmSelect.getDeclarations(file, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClassForwardDeclaration fdecl = findCsmClassForwardDeclaration((CsmNamespaceDefinition) decl, cls);
                    if (fdecl != null) {
                        return fdecl;
                    }
                }
            }
            if (CsmKindUtilities.isNamespaceDefinition(scope)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) scope;
                if (CharSequenceUtilities.startsWith(cls.getQualifiedName(), nsd.getQualifiedName())) {
                    CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION);
                    Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(nsd, filter);
                    for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                        CsmOffsetableDeclaration decl = it.next();
                        CsmClass fwdCls = ((CsmClassForwardDeclaration) decl).getCsmClass();
                        if (fwdCls != null && fwdCls.equals(cls)) {
                            return (CsmClassForwardDeclaration) decl;
                        }
                    }
                    filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION);
                    declarations = CsmSelect.getDeclarations(nsd, filter);
                    for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                        CsmOffsetableDeclaration decl = it.next();
                        CsmClassForwardDeclaration fdecl = findCsmClassForwardDeclaration((CsmNamespaceDefinition) decl, cls);
                        if (fdecl != null) {
                            return fdecl;
                        }
                    }
                }
            }
        }
        return null;
    }

    private CsmSpecializationParameter getTemplateParameterDefultValue(CsmTemplate declaration, CsmTemplateParameter param, int index) {
        CsmSpecializationParameter res = param.getDefaultValue();
        if (res != null) {
            return res;
        }
        if (CsmKindUtilities.isClass(declaration)) {
            CsmClass cls = (CsmClass) declaration;
            CsmClassForwardDeclaration fdecl;
            fdecl = findCsmClassForwardDeclaration(cls);
            if (fdecl != null) {
                List<CsmTemplateParameter> templateParameters = ((CsmTemplate) fdecl).getTemplateParameters();
                if (templateParameters.size() > index) {
                    CsmTemplateParameter p = templateParameters.get(index);
                    if (p != null) {
                        res = p.getDefaultValue();
                        if (res != null) {
                            return res;
                        }
                    }
                }
            }
        }
        return res;
    }

    private static CsmCacheMap getTemplateRelatedCache(CsmObject template, boolean specialize) {
        if (true) return null;
        return CsmCacheManager.getClientCache(new TemplateCacheKey(template, specialize), new TemplateCacheInitializer(template));
    }

    private static CsmCacheMap getSpecializeCache() {
        if (true) return null;
        return CsmCacheManager.getClientCache(SpecializeRequest.class, SPECIALIZE_INITIALIZER);
    }
    
    private static CsmCacheMap getNestedTypesCache() {
        if (false) return null;
        return CsmCacheManager.getClientCache(NestedTypesRequest.class, NESTED_TYPES_CACHE);
    }

    private static <T> int howMany(Collection<T> collection, T elem) {
        int counter = 0;
        for (T t : collection) {
            if (Objects.equals(t, elem)) {
                counter++;
            }
        }
        return counter;
    }

    private static boolean hasVariadicParams(CsmTemplate template) {
        if (template != null && template.getTemplateParameters() != null) {
            for (CsmTemplateParameter param : template.getTemplateParameters()) {
                if (param.isVarArgs()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasVariadicParams(List<CsmSpecializationParameter> params) {
        if (params != null) {
            for (CsmSpecializationParameter param : params) {
                if(CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<CsmSpecializationParameter> expandVariadicParams(List<CsmSpecializationParameter> params) {
        List<CsmSpecializationParameter> params2 = new ArrayList<>();
        for (CsmSpecializationParameter param : params) {
            if(CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                for (CsmSpecializationParameter arg : ((CsmVariadicSpecializationParameter)param).getArgs()) {
                    params2.add(arg);
                }
            } else {
                params2.add(param);
            }
        }
        return params2;
    }

    private static CsmType[] wrapType(CsmType type) {
        return type != null ? new CsmType[]{type} : null;
    }

    private static class TypeDigger {

        private final CsmType templateType;

        private final List<ExtractAction> actions;

        /**
         * Creates digger which will extract template parameter according
         * to the passed type.
         *
         * @param templateParam - template parameter for this digger
         * @param type - template type which digger uses to get path to the template parameter
         * @return digger
         */
        public static TypeDigger create(CsmTemplateParameter templateParam, CsmType type) {
            List<ExtractAction> actions = new ArrayList<>();
            CsmType templateType = findTemplateParam(templateParam.getQualifiedName().toString(), templateParam.isVarArgs(), type, actions);
            if (templateType != null) {
                return new TypeDigger(actions, templateType);
            }
            return null;
        }

        public CsmType getTemplateType() {
            return templateType;
        }

        public CsmType[] extract(CsmType target, DeduceTemplateTypeStrategy strategy) {
            CsmType types[] = wrapType(target);
            for (ExtractAction action : actions) {
                CsmType nextTypes[] = action.extract(types[0]);
                if (nextTypes == null || nextTypes.length == 0) {
                    return strategy.canSkipError(DeduceTemplateTypeStrategy.Error.ExtractNextTypeError) ? types : null;
                }
                types = nextTypes;
            }
            return types;
        }

        private static CsmType findTemplateParam(String templateParamName, boolean variadic, CsmType type, List<ExtractAction> digActions) {
            if (type == null) {
                return null;
            }
            CsmType foundType = null;
            CsmClassifier cls = type.getClassifier();
            if (CsmBaseUtilities.isValid(cls)) {
                if (!cls.getQualifiedName().toString().equals(templateParamName)) {
                    CsmFunctionPointerType funPtrType = tryGetFunctionPointerType(type);
                    if (funPtrType != null) {
                        CsmType retType = funPtrType.getReturnType();
                        foundType = findTemplateParam(templateParamName, variadic, retType, digActions);
                        if (foundType != null) {
                            digActions.add(0, new ExtractFunctionReturnTypeAction());
                            return foundType;
                        }
                        int paramIndex = 0;
                        Iterator<CsmParameter> paramIter = funPtrType.getParameters().iterator();
                        while (paramIter.hasNext()) {
                            CsmType paramType = paramIter.next().getType();
                            foundType = findTemplateParam(templateParamName, variadic, paramType, digActions);
                            if (foundType != null) {
                                digActions.add(0, new ExtractFunctionParamTypeAction(paramIndex, isActionVariadic(variadic, digActions)));
                                return foundType;
                            }
                            paramIndex++;
                        }
                    } else {
                        CsmType instType = tryGetInstantiationType(type);
                        if (instType != null) {
                            List<CsmSpecializationParameter> params = instType.getInstantiationParams();
                            if (params != null) {
                                for (int i = 0; i < params.size(); i++) {
                                    CsmSpecializationParameter param = params.get(i);
                                    if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                                        CsmType paramType = ((CsmTypeBasedSpecializationParameter) param).getType();
                                        foundType = findTemplateParam(templateParamName, variadic, paramType, digActions);
                                        if (foundType != null) {
                                            digActions.add(0, new ExtractInstantiationParamTypeAction(i, isActionVariadic(variadic, digActions)));
                                            return foundType;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    return type;
                }
            }
            return foundType;
        }

        private static boolean isActionVariadic(boolean variadic, List<ExtractAction> digActions) {
            // Only last extract action is variadic
            return variadic && digActions.isEmpty();
        }

        private static CsmType tryGetInstantiationType(CsmType type) {
            CsmType result = CsmUtilities.iterateTypeChain(type, new CsmUtilities.Predicate<CsmType>() {
                @Override
                public boolean check(CsmType value) {
                    // In fact we always must unfold all types prior to deducing template parameter.
                    // The same is true for pattern type. If we are ever consider doing
                    // that we must bind template parameter we are deducing to some 
                    // subtype inside the most unfolded type (Synonym of that operation
                    // is modifiyng pattern type).
                    
                    // Example (CCC specialized via alias1 and instantiated via alias2):
                    /*
                        template <typename T>
                        struct AAA {};

                        template <typename T>
                        struct BBB {};

                        template <typename T>
                        using alias1 = BBB<T>;

                        template <typename T>
                        using alias2 = alias1<AAA<T> >;

                        template <typename T>
                        struct CCC {};

                        template <typename T>
                        struct CCC<alias1<T> > {
                            typedef T type;
                        };

                        int main() {
                            CCC<alias2<int>>::type var;
                            std::cout << typeid(var).name() << std::endl;
                            return 0;
                        }
                    */
                    return value != null // just precaution to not get NullPointerException
                            && value.isInstantiation() // value must be instantation
                            && value.hasInstantiationParams(); // value must have instantiation parameters
                }
            });
            return (result != null && result.isInstantiation()) ? result  : null;
        }

        private static CsmInstantiation tryGetInstantiation(CsmType type) {
            // This is a hack to detect class BaseType
            if (!type.isInstantiation() && type.getStartPosition() == null && type.getEndPosition() == null) {
                CsmClassifier cls = type.getClassifier();
                return CsmKindUtilities.isInstantiation(cls) ? (CsmInstantiation) cls : null;
            }
            return null;
        }

        private static CsmFunctionPointerType tryGetFunctionPointerType(CsmType type) {
            CsmType result = CsmUtilities.iterateTypeChain(type, new CsmUtilities.Predicate<CsmType>() {
                @Override
                public boolean check(CsmType value) {
                    return CsmKindUtilities.isFunctionPointerType(value);
                }
            });
            return (CsmFunctionPointerType) (CsmKindUtilities.isFunctionPointerType(result) ? result : null);
        }

        private TypeDigger(List<ExtractAction> actions, CsmType templateType) {
            this.actions = actions;
            this.templateType = templateType;
        }

        private abstract static class ExtractAction {

            /**
             * Extracts sub-type(s) from the given type
             * @return array of types or null
             */
            public abstract CsmType[] extract(CsmType type);

            /**
             * Returns string representation of how what that action extracts
             * type.
             * @param target
             * @return string
             */
            public abstract String asString(String target);
        }

        private static final class ExtractInstantiationParamTypeAction extends ExtractAction {

            private final boolean variadic;

            private final int index;

            public ExtractInstantiationParamTypeAction(int index, boolean variadic) {
                this.index = index;
                this.variadic = variadic;
            }

            @Override
            public CsmType[] extract(CsmType type) {
                List<CsmSpecializationParameter> params = null;
                CsmType instType = tryGetInstantiationType(type);
                if (instType != null) {
                    params = extractInstantiationParams(instType);
                } else {
                    CsmInstantiation instantiation = tryGetInstantiation(type);
                    if (instantiation != null) {
                        params = extractInstantiationParams(instantiation);
                    }
                }
                if (params != null && index < params.size()) {
                    if (!variadic) {
                        CsmSpecializationParameter param = params.get(index);
                        if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                            return new CsmType[]{((CsmTypeBasedSpecializationParameter) param).getType()};
                        }
                    } else {
                        List<CsmType> result = new ArrayList<>();
                        ListIterator<CsmSpecializationParameter> paramsIter = params.listIterator(index);
                        while (paramsIter.hasNext()) {
                            CsmSpecializationParameter param = paramsIter.next();
                            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                                result.add(((CsmTypeBasedSpecializationParameter) param).getType());
                            }
                        }
                        return result.toArray(new CsmType[result.size()]);
                    }
                }
                if (variadic && params != null && params.size() == index) {
                    return new CsmType[0];
                }
                return null;
            }

            private List<CsmSpecializationParameter> extractInstantiationParams(CsmType instType) {
                List<CsmSpecializationParameter> params = instType.getInstantiationParams();
                if (hasVariadicParams(params)) {
                    params = expandVariadicParams(params);
                }
                return params;
            }

            private List<CsmSpecializationParameter> extractInstantiationParams(CsmInstantiation inst) {
                Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = inst.getMapping();
                List<CsmTemplateParameter> templateParams = new ArrayList<>(mapping.keySet());
                Collections.sort(templateParams, new TemplateParamsComparator());
                if (!templateParams.isEmpty()) {
                    List<CsmSpecializationParameter> params = new ArrayList<>();
                    for (CsmTemplateParameter templateParam : templateParams) {
                        CsmSpecializationParameter mappedParam = mapping.get(templateParam);
                        if (mappedParam != null) {
                            params.add(mappedParam);
                        }
                    }
                    if (hasVariadicParams(params)) {
                        params = expandVariadicParams(params);
                    }
                    return params;
                }
                return null;
            }

            @Override
            public String asString(String target) {
                StringBuilder sb = new StringBuilder("class<"); // NOI18N
                for (int i = 0; i < index; i++) {
                    sb.append("class,"); // NOI18N
                }
                sb.append(target);
                sb.append(">"); // NOI18N
                return sb.toString();
            }

            private static class TemplateParamsComparator implements Comparator<CsmTemplateParameter> {
                @Override
                public int compare(CsmTemplateParameter o1, CsmTemplateParameter o2) {
                    if (Objects.equals(o1.getContainingFile(), o2.getContainingFile())) {
                        return o1.getStartOffset() - o2.getStartOffset();
                    }
                    return 0;
                }
            }
        }

        private static final class ExtractFunctionParamTypeAction extends ExtractAction {

            private final boolean variadic; // Not used yet

            private final int index;

            public ExtractFunctionParamTypeAction(int index, boolean variadic) {
                this.index = index;
                this.variadic = variadic;
            }

            @Override
            public CsmType[] extract(CsmType type) {
                CsmFunctionPointerType funPtrType = tryGetFunctionPointerType(type);
                if (funPtrType != null) {
                    Collection<CsmParameter> params = funPtrType.getParameters();
                    if (params != null && index < params.size()) {
                        int current = 0;
                        Iterator<CsmParameter> paramsIter = params.iterator();
                        while (index != current) {
                            paramsIter.next();
                            ++current;
                        }
                        CsmParameter param = paramsIter.next();
                        return new CsmType[]{param.getType()};
                    }
                }
                return null;
            }

            @Override
            public String asString(String target) {
                StringBuilder sb = new StringBuilder("ret(*)("); // NOI18N
                for (int i = 0; i < index; i++) {
                    sb.append("class,"); // NOI18N
                }
                sb.append(target);
                sb.append(")"); // NOI18N
                return sb.toString();
            }
        }

        private static final class ExtractFunctionReturnTypeAction extends ExtractAction {

            @Override
            public CsmType[] extract(CsmType type) {
                CsmFunctionPointerType funPtrType = tryGetFunctionPointerType(type);
                if (funPtrType != null) {
                    return new CsmType[]{funPtrType.getReturnType()};
                }
                return null;
            }

            @Override
            public String asString(String target) {
                return target + "(*)(...)"; // NOI18N
            }
        }
    }

    private static final class TemplateCacheKey {

        private final CsmObject obj;
        private final boolean specialize;

        public TemplateCacheKey(CsmObject instance, boolean specialize) {
            this.obj = instance;
            this.specialize = specialize;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + System.identityHashCode(this.obj);
            hash = 79 * hash + (this.specialize ? 1 : 0);
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
            final TemplateCacheKey other = (TemplateCacheKey) obj;
            if (this.specialize != other.specialize) {
                return false;
            }
            return this.obj == other.obj;
        }

        @Override
        public String toString() {
            return "TemplateCacheKey{" + "obj=" + obj + ", specialize=" + specialize + '}'; // NOI18N
        }
    }

    private static final class TemplateCacheInitializer implements Callable<CsmCacheMap> {

        private final CsmObject template;

        private TemplateCacheInitializer(CsmObject template) {
            this.template = template;
        }

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("Cache Template " + CsmDisplayUtilities.getTooltipText(template), 1);// NOI18N
        }
    }

    private static final class InstantiateListKey {
        private final List<CsmSpecializationParameter> params;
        private int hashCode = 0;

        public InstantiateListKey(List<CsmSpecializationParameter> params) {
            this.params = new ArrayList<>(params);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int hash = 7;
                hash = 17 * hash + CndCollectionUtils.hashCode(params);
                hashCode = hash;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InstantiateListKey other = (InstantiateListKey) obj;
            if (this.hashCode != other.hashCode && (this.hashCode != 0 && other.hashCode != 0)) {
                return false;
            }
            return CndCollectionUtils.equals(params, other.params);
        }

        @Override
        public String toString() {
            return "InstantiateListKey{" + params + '}';// NOI18N
        }
    }

    private static final class InstantiateMapKey {
        private final HashMap<CsmTemplateParameter, CsmSpecializationParameter> params;
        private int hashCode = 0;

        public InstantiateMapKey(Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            this.params = new HashMap<>(mapping);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int hash = 7;
                hash += CndCollectionUtils.hashCode(params);
                hashCode = hash;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InstantiateMapKey other = (InstantiateMapKey) obj;
            if (this.hashCode != other.hashCode && (this.hashCode != 0 && other.hashCode != 0)) {
                return false;
            }
            return CndCollectionUtils.equals(params, other.params);
        }

        @Override
        public String toString() {
            return "InstantiateMapKey{" + params + '}';// NOI18N
        }
    }

    private static final Callable<CsmCacheMap> BASE_TEMPLATE_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("BASE_TEMPLATE Cache", 1); // NOI18N
        }

    };

    private static class BaseTemplateKey {
        private final CsmDeclaration declaration;

        public BaseTemplateKey(CsmDeclaration declaration) {
            this.declaration = declaration;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + Objects.hashCode(this.declaration);
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
            final BaseTemplateKey other = (BaseTemplateKey) obj;
            return Objects.equals(this.declaration, other.declaration);
        }
    }

    private static final Callable<CsmCacheMap> SPECIALIZATIONS_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("SPECIALIZATIONS Cache", 1); // NOI18N
        }

    };

    private static class TemplateSpecializationsKey {
        private final CsmDeclaration decl;
        private final CsmFile contextFile;
        private final int contextOffset;

        public TemplateSpecializationsKey(CsmDeclaration templateDecl, CsmFile contextFile, int contextOffset) {
            this.decl = templateDecl;
            this.contextFile = contextFile;
            this.contextOffset = contextOffset;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + this.contextOffset;
            hash = 31 * hash + Objects.hashCode(this.contextFile);
            hash = 31 * hash + Objects.hashCode(this.decl);
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
            final TemplateSpecializationsKey other = (TemplateSpecializationsKey) obj;
            if (this.contextOffset != other.contextOffset) {
                return false;
            }
            if (!Objects.equals(this.contextFile, other.contextFile)) {
                return false;
            }
            return Objects.equals(this.decl, other.decl);
        }
    }

    private static final Callable<CsmCacheMap> SPECIALIZE_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("SPECIALIZE Cache", 1); // NOI18N
        }

    };

    /**
     * Tries to compare two classifiers using only unique name and
     * parameters which are really used by classifier
     */
    private static class SpecializeRequest {

        private final CsmClassifier classifier;

        private final InstantiationParametersInfo paramsInfo;

        public SpecializeRequest(CsmClassifier classifier, InstantiationParametersInfo paramsInfo) {
            this.classifier = classifier;
            this.paramsInfo = paramsInfo;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            if (CsmKindUtilities.isOffsetableDeclaration(classifier)) {
                CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) classifier;
                hash = 31 * hash + decl.getStartOffset();
                hash = 31 * hash + Objects.hashCode(decl.getContainingFile());
            }
            hash = 31 * hash + Objects.hashCode(classifier.getUniqueName());
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
            final SpecializeRequest other = (SpecializeRequest) obj;
            boolean ourOffsetable = CsmKindUtilities.isOffsetableDeclaration(classifier);
            boolean otherOffsetable = CsmKindUtilities.isOffsetableDeclaration(other.classifier);
            if (ourOffsetable != otherOffsetable) {
                return false;
            }
            if (ourOffsetable && otherOffsetable) {
                CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) classifier;
                CsmOffsetableDeclaration otherDecl = (CsmOffsetableDeclaration) other.classifier;
                if (!decl.getContainingFile().equals(otherDecl.getContainingFile())) {
                    return false;
                }
                if (decl.getStartOffset() != otherDecl.getStartOffset()) {
                    return false;
                }
            }
            if (!Objects.equals(this.classifier.getUniqueName(), other.classifier.getUniqueName())) {
                return false;
            }
            if (paramsInfo.getParamsTypes().size() != other.paramsInfo.getParamsTypes().size()) {
                return false;
            }
            boolean hasNullTypes = false;
            Iterator<CsmType> types = paramsInfo.getParamsTypes().iterator();
            Iterator<CsmType> otherTypes = other.paramsInfo.getParamsTypes().iterator();
            while (types.hasNext() && otherTypes.hasNext()) {
                CsmType type = types.next();
                CsmType otherType = otherTypes.next();
                if (type == null && otherType == null) {
                    hasNullTypes = true;
                    break;
                }
                if (!Objects.equals(type, otherType)) {
                    return false;
                }
            }
            return hasNullTypes ? classifier.equals(other.classifier) : true;
        }
    }
    
    private static final Callable<CsmCacheMap> NESTED_TYPES_CACHE = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("Nested Type Names Cache", 1); // NOI18N
        }

    };

    /**
     * Compares two types by reference
     */
    private static class NestedTypesRequest {

        private final CsmType type;
        
        private final boolean fullResolve;

        public NestedTypesRequest(CsmType type, boolean fullResolve) {
            this.type = type;
            this.fullResolve = fullResolve;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + System.identityHashCode(this.type);
            hash = 67 * hash + (this.fullResolve ? 1 : 0);
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
            final NestedTypesRequest other = (NestedTypesRequest) obj;
            return other.type == type && other.fullResolve == fullResolve;
        }
    }

    private static class UnfoldWhileNestedPredicate implements CsmUtilities.Predicate<CsmType> {
        @Override
        public boolean check(CsmType value) {
            return !Instantiation.isNestedType(value);
        }
    }
}
