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
package org.netbeans.modules.cnd.modelimpl.impl.services.evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver3;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.ExpressionEvaluator;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.MemberResolverImpl;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.util.MapHierarchy;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 *
 */
public class VariableProvider {
    private static final Logger LOG = Logger.getLogger(VariableProvider.class.getSimpleName());
    public static final int INFINITE_RECURSION = 16;

    private final int level;
    private final CsmScope scope; // could be null, in that case decl is scope
    private CsmOffsetableDeclaration decl;
    private MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping;
    private CsmFile variableFile; 
    private int variableStartOffset;
    private int variableEndOffset;
    
    // pattern to parse fun calls like __is_class(T)
    private static final Pattern intrisicOneArgFunCall = Pattern.compile("([\\w_]+)\\(([\\w_]+)\\)"); // NOI18N
    
    public VariableProvider(int level, CsmScope scope) {
        this.level = level;
        this.scope = scope;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\nVARIABLE PROVIDER CREATED WITHOUT MAP HIERARCHY\n"); // NOI18N
        }
    }
    
    public VariableProvider(CsmOffsetableDeclaration decl, CsmScope scope, MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping, CsmFile variableFile, int variableStartOffset, int variableEndOffset, int level) {
        this.decl = decl;
        this.scope = scope;
        this.mapping = mapping;
        this.variableFile = variableFile != null ? variableFile : (decl != null ? decl.getContainingFile() : null);
        this.variableStartOffset = variableStartOffset;
        this.variableEndOffset = variableEndOffset;
        this.level = level;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\nVARIABLE PROVIDER CREATED WITH MAP HIERARCHY:\n{0}\n", mapping); // NOI18N
        }
    }    

    public int getValue(String variableName) {
        if(level > INFINITE_RECURSION) {
            return 0;
        }
        long time = System.currentTimeMillis();
        try {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "GetValue for {0}:{1}\n", new Object[]{variableName, decl});
            }
            if(variableName.equals("true")) { // NOI18N
                return 1;
            }
            if(variableName.equals("false")) { // NOI18N
                return 0;
            }
            if (decl != null) { // TODO: why this condition?
                final String scopeQualifiedName = CsmKindUtilities.isQualified(scope) ? 
                        ((CsmQualifiedNamedElement) scope).getQualifiedName().toString() : 
                        decl.getQualifiedName().toString();
                
                final String fullVariableName = scopeQualifiedName + APTUtils.SCOPE + variableName;   
                
                for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> entry : mapping.entries()) {
                    CsmTemplateParameter param = entry.getKey();
                    if (variableName.equals(param.getQualifiedName().toString()) || 
                            fullVariableName.equals(param.getQualifiedName().toString())) {
                        CsmSpecializationParameter spec = entry.getValue();
                        if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(spec)) {
                            CsmExpressionBasedSpecializationParameter specParam = (CsmExpressionBasedSpecializationParameter) spec;
                            Object eval = new ExpressionEvaluator(level+1).eval(
                                    specParam.getText().toString(), 
                                    decl, 
                                    specParam.getScope(),
                                    specParam.getContainingFile(),
                                    specParam.getStartOffset(),
                                    specParam.getEndOffset(),
                                    mapping
                            );
                            if (eval instanceof Integer) {
                                return (Integer) eval;
                            }
                        } else if (CsmKindUtilities.isTypeBasedSpecalizationParameter(spec)) {
                            CsmTypeBasedSpecializationParameter specParam = (CsmTypeBasedSpecializationParameter) spec;                                                        
                            Object eval = new ExpressionEvaluator(level+1).eval(
                                    specParam.getText().toString(), 
                                    decl,
                                    specParam.getScope(),
                                    specParam.getContainingFile(), 
                                    specParam.getStartOffset(), 
                                    specParam.getEndOffset(), 
                                    mapping
                            );                            
                            if (eval instanceof Integer) {
                                return (Integer) eval;
                            }
                        }
                    }
                }
                if (CsmKindUtilities.isClass(decl)) {
                    if (CharSequenceUtilities.equals(scopeQualifiedName, decl.getQualifiedName())) {
                        String varName = variableName.replaceAll(".*::(.*)", "$1"); // NOI18N
                        final CsmClass clazz = (CsmClass) decl;
                        MemberResolverImpl r = new MemberResolverImpl();
                        final CsmMember member = r.getDeclaration(clazz, varName);
                        if (member != null) {
                            if(member.isStatic() && CsmKindUtilities.isField(member) && member.getName().toString().equals(varName)) {
                                CsmExpression expr = ((CsmField)member).getInitialValue();
                                if(CsmKindUtilities.isInstantiation(member)) {
                                    Object eval = new ExpressionEvaluator(level+1).eval(
                                            expr.getExpandedText().toString(), 
                                            member.getContainingClass(), 
                                            member.getScope(),
                                            expr.getContainingFile(),
                                            expr.getStartOffset(),
                                            expr.getEndOffset(),
                                            getMapping((CsmInstantiation) member)
                                    );
                                    if (eval instanceof Integer) {
                                        return (Integer) eval;
                                    }
                                } else if (expr != null) {
                                    Object eval = new ExpressionEvaluator(level+1).eval(
                                            expr.getExpandedText().toString(), 
                                            member.getContainingClass(), 
                                            member.getScope(),
                                            expr.getContainingFile(),
                                            expr.getStartOffset(),
                                            expr.getEndOffset(),
                                            Collections.<CsmTemplateParameter, CsmSpecializationParameter>emptyMap()
                                    );
                                    if (eval instanceof Integer) {
                                        return (Integer) eval;
                                    }                            
                                }
                            }
                        }
                    }
                }

                // TODO: parse variableName instead of that simple check
                // as it won't work in many cases
                if (variableName.contains(APTUtils.SCOPE)) {
                    boolean executeSimpleResolution = !(TraceFlags.EXPRESSION_EVALUATOR_DEEP_VARIABLE_PROVIDER && variableName.contains("<")); // NOI18N

                    if (executeSimpleResolution) {
                        CsmObject o = null;
                        Resolver aResolver = ResolverFactory.createResolver(decl);            
                        try {
                            o = aResolver.resolve(Utils.splitQualifiedName(variableName.replaceAll("(.*)::.*", "$1")), Resolver3.ALL); // NOI18N
                        } finally {
                            ResolverFactory.releaseResolver(aResolver);
                        }
                        if (CsmKindUtilities.isClassifier(o)) {
                            CsmClassifier cls = (CsmClassifier) o;
                            CsmClassifier originalClassifier = CsmClassifierResolver.getDefault().getOriginalClassifier(cls, decl.getContainingFile());
                            if(CsmKindUtilities.isInstantiation(originalClassifier)) {
                                Object eval = new ExpressionEvaluator(level+1).eval(variableName.replaceAll(".*::(.*)", "$1"), (CsmInstantiation) originalClassifier, null); // NOI18N
                                if (eval instanceof Integer) {
                                    return (Integer) eval;
                                }
                            } else if (CsmKindUtilities.isOffsetableDeclaration(originalClassifier)) {
                                Object eval = new ExpressionEvaluator(level+1).eval(variableName.replaceAll(".*::(.*)", "$1"), (CsmOffsetableDeclaration) originalClassifier, null, Collections.<CsmTemplateParameter, CsmSpecializationParameter>emptyMap()); // NOI18N
                                if (eval instanceof Integer) {
                                    return (Integer) eval;
                                }                    
                            }
                        }
                    }

                    if (TraceFlags.EXPRESSION_EVALUATOR_DEEP_VARIABLE_PROVIDER) {
                        // it works but does it too slow

                        int flags = CPPParserEx.CPP_CPLUSPLUS;
                        flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
                        // TODO get flavor from variableFile?
                        try {
                            // use cached TS
                            TokenStream buildTokenStream = APTTokenStreamBuilder.buildTokenStream(variableName.replaceAll("(.*)::.*", "$1"), APTLanguageSupport.GNU_CPP); // NOI18N
                            if (buildTokenStream != null) {
                                if (variableStartOffset > 0) {
                                    buildTokenStream = new ShiftedTokenStream(buildTokenStream, variableStartOffset);
                                }

                                CPPParserEx parser = CPPParserEx.getInstance(variableFile, buildTokenStream, flags);
                                parser.type_name();
                                AST ast = parser.getAST();

                                CsmType type = TypeFactory.createType(ast, variableFile, null, 0, this.scope);
                                if (scope != null) {
                                    type = TemplateUtils.checkTemplateType(type, scope);
                                } else if (CsmKindUtilities.isInstantiation(decl)) {
                                    type = checkTemplateType(type, (Instantiation)decl);
                                }

                                if (CsmKindUtilities.isTemplateParameterType(type)) {
                                    CsmSpecializationParameter instantiatedType = resolveTemplateParameter(((CsmTemplateParameterType) type).getParameter());
                                    if (CsmKindUtilities.isTypeBasedSpecalizationParameter(instantiatedType)) {
                                        type = ((CsmTypeBasedSpecializationParameter) instantiatedType).getType();
                                    }
                                }                      

                                // TODO: think about differences between decl and mapping!!!

    //                            if (CsmKindUtilities.isInstantiation(decl)) {
    //                                type = instantiateType(type, (Instantiation)decl);
    //                            }

                                CsmClassifier originalClassifier = CsmClassifierResolver.getDefault().getOriginalClassifier(type.getClassifier(), decl.getContainingFile());

                                // TODO:
                                // This block should be deleted - type should be instantiated with the right parameters
                                // (Or type should not be instantiated at all, but in such case type.getClassifier() shouldn't specialize classifier)
                                if (CsmKindUtilities.isInstantiation(originalClassifier) && !CsmKindUtilities.isSpecialization(originalClassifier)) {
                                    CsmObject instantiation = originalClassifier;

                                    InstantiationProviderImpl ip = (InstantiationProviderImpl) InstantiationProviderImpl.getDefault();

                                    while (CsmKindUtilities.isInstantiation(((CsmInstantiation) instantiation).getTemplateDeclaration())) {
                                        instantiation = (CsmClassifier) ((CsmInstantiation) instantiation).getTemplateDeclaration();
                                    }

                                    List<Map<CsmTemplateParameter, CsmSpecializationParameter>> maps = mapping.getMaps(new MapHierarchy.NonEmptyFilter());

                                    for (int i = maps.size() - 1; i > 0; i--) {
                                        instantiation = ip.instantiate((CsmTemplate) instantiation, variableFile, variableStartOffset, maps.get(i), false);
                                    }
                                    if (!maps.isEmpty()) {
                                        instantiation = ip.instantiate((CsmTemplate) instantiation, variableFile, variableStartOffset, maps.get(0), true);
                                    }

                                    if (CsmKindUtilities.isClassifier(instantiation)) {
                                        originalClassifier = (CsmClassifier) instantiation;
                                    }
                                }

                                if (CsmKindUtilities.isInstantiation(originalClassifier)) {
                                    Object eval = new ExpressionEvaluator(level+1).eval(variableName.replaceAll(".*::(.*)", "$1"), (CsmInstantiation) originalClassifier, null); // NOI18N
                                    if (eval instanceof Integer) {
                                        return (Integer) eval;
                                    }
                                } else if (CsmKindUtilities.isOffsetableDeclaration(originalClassifier)) {
                                    Object eval = new ExpressionEvaluator(level+1).eval(variableName.replaceAll(".*::(.*)", "$1"), (CsmOffsetableDeclaration) originalClassifier, null, mapping); // NOI18N
                                    if (eval instanceof Integer) {
                                        return (Integer) eval;
                                    }                                
                                }
                            }
                        } catch (Throwable ex) {
                        }
                    }
                }
            }

            return Integer.MAX_VALUE;
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                time = System.currentTimeMillis() - time;
                LOG.log(Level.FINE, "getValue {0} took {1}ms\n", new Object[]{variableName, time}); // NOI18N
            }
        }
    }
    
    public int getFunCallValue(String funCall) {
        if (funCall != null) {
            if (funCall.startsWith(CppTokenId.__IS_CLASS.fixedText())) {
                CsmType type = resolveIntrisicTypeTraceFunParam(CppTokenId.__IS_CLASS.fixedText(), funCall);
                return (type != null && CsmKindUtilities.isClass(type.getClassifier())) ? 1 : 0;
            } else if (funCall.startsWith(CppTokenId.__IS_ENUM.fixedText())) {
                CsmType type = resolveIntrisicTypeTraceFunParam(CppTokenId.__IS_ENUM.fixedText(), funCall);
                return (type != null && CsmKindUtilities.isEnum(type.getClassifier())) ? 1 : 0;                
            } else if (funCall.startsWith(CppTokenId.__IS_UNION.fixedText())) {
                CsmType type = resolveIntrisicTypeTraceFunParam(CppTokenId.__IS_UNION.fixedText(), funCall);
                return (type != null && CsmKindUtilities.isUnion(type.getClassifier())) ? 1 : 0;                
            }
        }
        return Integer.MAX_VALUE; // Not supported yet
    }
    
    public int getSizeOfValue(String obj) {
        if (false) {
            return Integer.MAX_VALUE; // Not supported yet
        }
        
        List<CsmInstantiation> instantiations = null;
        
        // TODO: think how to get right isntantiations here
        if (CsmKindUtilities.isInstantiation(decl)) {
            instantiations = new ArrayList<>();
            CsmInstantiation inst = (CsmInstantiation) decl;
            instantiations.add(inst);
            while (CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
                inst = (CsmInstantiation) inst.getTemplateDeclaration();
                instantiations.add(inst);
            }
        } else if (CsmKindUtilities.isTemplate(decl) && !mapping.isEmpty()) {
            List<Map<CsmTemplateParameter, CsmSpecializationParameter>> maps = mapping.getMaps(new MapHierarchy.NonEmptyFilter());
            
            InstantiationProviderImpl ip = (InstantiationProviderImpl) InstantiationProviderImpl.getDefault();
            
            CsmObject instantiation = decl;
            
            instantiations = new ArrayList<>();
            
            for (int i = maps.size() - 1; i >= 0; i--) {
                instantiation = ip.instantiate((CsmTemplate) instantiation, variableFile, variableStartOffset, maps.get(i), false);
                instantiations.add(0, (CsmInstantiation) instantiation);
            }
        }
        
        CsmScope objScope = scope;
        if (objScope == null && CsmKindUtilities.isScope(decl)) {
            objScope = (CsmScope) decl;
        }
        
        CsmType objType = CsmExpressionResolver.resolveType(obj, variableFile, variableEndOffset, objScope, instantiations);
        
        if (CsmExpressionResolver.shouldResolveAsMacroType(objType, objScope)) {
            objType = CsmExpressionResolver.resolveMacroType(objType, objScope, instantiations, null);
        }
        
        return Utils.getSizeOfType(objType, variableFile);
    }
    
    private CsmType resolveIntrisicTypeTraceFunParam(String suggestedFunName, String funCall) {
        Matcher matcher =  intrisicOneArgFunCall.matcher(funCall);
        if (matcher.matches() && matcher.groupCount() >= 2 && suggestedFunName.equals(matcher.group(1))) {
            String paramName = matcher.group(2);
            CsmSpecializationParameter resolvedParam = resolveTemplateParameter(paramName);
            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(resolvedParam)) {
                CsmType type = ((CsmTypeBasedSpecializationParameter) resolvedParam).getType();
                return CsmUtilities.iterateTypeChain(type, new CsmUtilities.ConstantPredicate<CsmType>(false));
            }
        }
        return null;
    }
    
    private CsmSpecializationParameter resolveTemplateParameter(String variableName) {
        if (decl != null) { // TODO: why this condition?
            final String scopeQualifiedName = CsmKindUtilities.isQualified(scope) ? 
                ((CsmQualifiedNamedElement) scope).getQualifiedName().toString() : 
                decl.getQualifiedName().toString();
            
            final String fullVariableName = scopeQualifiedName + APTUtils.SCOPE + variableName;
        
            for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> entry : mapping.entries()) {
                CsmTemplateParameter param = entry.getKey();
                if (variableName.equals(param.getQualifiedName().toString()) || 
                        fullVariableName.equals(param.getQualifiedName().toString())) {        
                    if (CsmKindUtilities.isTypeBasedSpecalizationParameter(entry.getValue())) {
                        final CsmType paramType = ((CsmTypeBasedSpecializationParameter) entry.getValue()).getType();
                        if (CsmKindUtilities.isTemplateParameterType(paramType)) {
                            return resolveTemplateParameter(((CsmTemplateParameterType) paramType).getParameter());
                        }
                    }
                    return entry.getValue();
                }
            }
        }
        return null;
    }
    
    private CsmSpecializationParameter resolveTemplateParameter(CsmTemplateParameter param) {
        CsmSpecializationParameter instParam = mapping.get(param);
        int iteration = 15;
        while (CsmKindUtilities.isTypeBasedSpecalizationParameter(instParam) &&
                CsmKindUtilities.isTemplateParameterType(((CsmTypeBasedSpecializationParameter) instParam).getType()) && iteration != 0) {
            CsmSpecializationParameter nextInstParam = mapping.get(((CsmTemplateParameterType) ((CsmTypeBasedSpecializationParameter) instParam).getType()).getParameter());
            if (nextInstParam != null) {
                instParam = nextInstParam;
            } else {
                break;
            }
            iteration--;
        } 
        return instParam;
    }
    
    private CsmType instantiateType(CsmType type, CsmInstantiation inst) {
        if (CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
            type = instantiateType(type, (CsmInstantiation) inst.getTemplateDeclaration());
        }
        return Instantiation.createType(type, inst);
    }

    private CsmType checkTemplateType(CsmType type, CsmInstantiation inst) {
        if (CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
            type = checkTemplateType(type, (Instantiation)inst.getTemplateDeclaration());
        }
        for (CsmTemplateParameter csmTemplateParameter : inst.getMapping().keySet()) {
            type = TemplateUtils.checkTemplateType(type, csmTemplateParameter.getScope());
        }
        return type;
    }

    private MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> getMapping(CsmInstantiation inst) {
        MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping2 = new MapHierarchy<>(inst.getMapping());
        while (CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
            inst = (CsmInstantiation) inst.getTemplateDeclaration();
            mapping2.push(inst.getMapping());
        }
        return mapping2;
    }

    @Override
    public String toString() {
        return "VariableProvider{" + "level=" + level + ", decl=" + decl + ", mapping=" + mapping + ", variableFile=" + variableFile + ", variableStartOffset=" + variableStartOffset + ", variableEndOffset=" + variableEndOffset + '}'; // NOI18N
    }        
}
