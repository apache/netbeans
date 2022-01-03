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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenBuffer;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmCacheMap;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.csm.ExpressionBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.VariableProvider;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.parser.generated.EvaluatorParser;
import org.netbeans.modules.cnd.modelimpl.util.MapHierarchy;
import org.netbeans.modules.cnd.spi.model.services.CsmExpressionEvaluatorProvider;

/**
 * Expression evaluator servise implementation.
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.model.services.CsmExpressionEvaluatorProvider.class)
public class ExpressionEvaluator implements CsmExpressionEvaluatorProvider {
    
    private static final Logger LOG = Logger.getLogger(ExpressionEvaluator.class.getSimpleName());

    private final int level;
    
    public ExpressionEvaluator() {
        this.level = 0;
    }

    public ExpressionEvaluator(int level) {
        this.level = level;
    }

    @Override
    public Object eval(String expr, CsmScope scope) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\nEvaluating expression \"{0}\"\n", expr); // NOI18N
        }
        org.netbeans.modules.cnd.antlr.TokenStream ts = APTTokenStreamBuilder.buildTokenStream(expr, APTFile.Kind.C_CPP);

        APTLanguageFilter lang = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP);
        ts = lang.getFilteredStream(ts);

        TokenBuffer tb = new TokenBuffer(ts);

        int result = 0;
        try {
            TokenStream tokens = new MyTokenStream(tb);
            EvaluatorParser parser = new EvaluatorParser(tokens);
            parser.setVariableProvider(new VariableProvider(level + 1, scope));
            result = parser.expr();
            //System.out.println(result);
        } catch (RecognitionException ex) {
        }
        return result;
    }

    @Override
    public Object eval(String expr, CsmInstantiation inst, CsmScope scope) {
        CsmCacheMap cache = getEvaluatorEvalCache();
        EvaluateRequest key =  new EvaluateRequest(expr, inst, scope);
        boolean[] found = new boolean[] { false };
        Object cached = CsmCacheMap.getFromCache(cache, key, found);
        if (cached != null && found[0]) {
            return cached;
        } else {
            long time = System.currentTimeMillis();
            Object result;
            if (CsmKindUtilities.isOffsetableDeclaration(inst)) {
                result = eval(expr, (CsmOffsetableDeclaration)inst, scope, getMapping(inst));
            } else {
                result = eval(expr, inst.getTemplateDeclaration(), scope, getMapping(inst));
            }
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(result, time));
            }
            return result;
        }
    }
    
    @Override
    public Object eval(String expr, CsmOffsetableDeclaration decl, CsmScope scope,  Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        return eval(expr, decl, scope, new MapHierarchy<>(mapping));
    }
    
    public Object eval(String expr, CsmOffsetableDeclaration decl, CsmScope scope, MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        return eval(expr, decl, scope, null, 0, 0, mapping);
    }    

    @Override
    public Object eval(String expr, CsmOffsetableDeclaration decl, CsmScope scope, CsmFile expressionFile, int startOffset, int endOffset, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        return eval(expr, decl, scope, expressionFile, startOffset, endOffset, new MapHierarchy<>(mapping));
    }
    
    public Object eval(String expr, CsmOffsetableDeclaration decl, CsmScope scope, CsmFile expressionFile, int startOffset, int endOffset, MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\nEvaluating expression \"{0}\"\n", expr); // NOI18N
        }

        org.netbeans.modules.cnd.antlr.TokenStream ts = APTTokenStreamBuilder.buildTokenStream(expr, APTFile.Kind.C_CPP);

        APTLanguageFilter lang = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP);
        ts = lang.getFilteredStream(ts);

        TokenBuffer tb = new TokenBuffer(ts);

        int result = 0;
        try {
            TokenStream tokens = new MyTokenStream(tb);
            EvaluatorParser parser = new EvaluatorParser(tokens);
            parser.setVariableProvider(new VariableProvider(decl, scope, mapping, expressionFile, startOffset, endOffset, level + 1));
            result = parser.expr();
            //System.out.println(result);
        } catch (RecognitionException ex) {
        }
        return result;
    }
    
    @Override
    public boolean isValid(Object evaluated) {
        return evaluated instanceof Integer && ((Integer) evaluated) != Integer.MAX_VALUE;
    }
    
    private MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> getMapping(CsmInstantiation inst) {
        CsmCacheMap cache = getEvaluatorGetMappingCache();
        GetMappingRequest key =  new GetMappingRequest(inst);
        boolean[] found = new boolean[] { false };
        Object cached = CsmCacheMap.getFromCache(cache, key, found);
        if (cached != null && found[0]) {
            return (MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter>) cached;
        } else {
            long time = System.currentTimeMillis();
            
            MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapHierarchy = new MapHierarchy<>(inst.getMapping());
            
            if (TraceFlags.EXPRESSION_EVALUATOR_RECURSIVE_CALC) {
                while(CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
                    inst = (CsmInstantiation) inst.getTemplateDeclaration();

                    Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = new HashMap<>();                
                    mapHierarchy.push(mapping);

                    List<CsmTemplateParameter> orderedParamsList = new ArrayList<>(inst.getMapping().keySet());

                    final CsmInstantiation finalInst = inst;
                    Collections.sort(orderedParamsList, new Comparator<CsmTemplateParameter>() {

                        @Override
                        public int compare(CsmTemplateParameter o1, CsmTemplateParameter o2) {
                            int score1 = calcScore(o1);
                            int score2 = calcScore(o2);
                            return score1 - score2;
                        }

                        private int calcScore(CsmTemplateParameter param) {
                            CsmSpecializationParameter spec = finalInst.getMapping().get(param);
                            if(CsmKindUtilities.isExpressionBasedSpecalizationParameter(spec)) {
                                if (!((CsmExpressionBasedSpecializationParameter) spec).isDefaultValue()) {
                                    return -1;
                                }
                            }
                            return param.getStartOffset();
                        }

                    });

                    for (CsmTemplateParameter param : orderedParamsList) {
                        Map<CsmTemplateParameter, CsmSpecializationParameter> newMapping = new HashMap<>();
                        CsmSpecializationParameter spec = inst.getMapping().get(param);
                        if(CsmKindUtilities.isExpressionBasedSpecalizationParameter(spec)) {
                            Object o = eval(
                                    ((CsmExpressionBasedSpecializationParameter) spec).getText().toString(), 
                                    inst.getTemplateDeclaration(), 
                                    spec.getScope(),
                                    spec.getContainingFile(),
                                    spec.getStartOffset(),
                                    spec.getEndOffset(),
                                    mapHierarchy
                            );
                            CsmSpecializationParameter newSpec = ExpressionBasedSpecializationParameterImpl.create(
                                o.toString(),
                                ((CsmExpressionBasedSpecializationParameter) spec).getScope(),
                                spec.getContainingFile(), 
                                spec.getStartOffset(), 
                                spec.getEndOffset()
                            );
                            newMapping.put(param, newSpec);
                        } else {
                            newMapping.put(param, spec);
                        }
                        mapping.putAll(newMapping);
                    }
                }
            } else {
                while (CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
                    inst = (CsmInstantiation) inst.getTemplateDeclaration();
                    mapHierarchy.push(inst.getMapping());
                }
            }
            
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(mapHierarchy, time));
            }

            return mapHierarchy;
        }
    }

    static private class MyToken implements Token {

        org.netbeans.modules.cnd.antlr.Token t;

        public MyToken(org.netbeans.modules.cnd.antlr.Token t) {
            this.t = t;
        }

        @Override
        public String getText() {
            return t.getText();
        }

        @Override
        public void setText(String arg0) {
            t.setText(arg0);
        }

        @Override
        public int getType() {
            return t.getType();
        }

        @Override
        public void setType(int arg0) {
            t.setType(arg0);
        }

        @Override
        public int getLine() {
            return t.getLine();
        }

        @Override
        public void setLine(int arg0) {
            t.setLine(arg0);
        }

        @Override
        public int getCharPositionInLine() {
            return t.getColumn();
        }

        @Override
        public void setCharPositionInLine(int arg0) {
            t.setColumn(arg0);
        }

        @Override
        public int getChannel() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public void setChannel(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int getTokenIndex() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public void setTokenIndex(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public CharStream getInputStream() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public void setInputStream(CharStream arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

    }


    static private class MyTokenStream implements TokenStream {
        TokenBuffer tb;

        int lastMark;
        
        public MyTokenStream(TokenBuffer tb) {
            this.tb = tb;
        }

        @Override
        public Token LT(int arg0) {
            return new MyToken(tb.LT(arg0));
        }

        @Override
        public void consume() {
            tb.consume();
        }

        @Override
        public int LA(int arg0) {
            return tb.LA(arg0);
        }

        @Override
        public int mark() {
            lastMark = tb.index();
            return tb.mark();
        }

        @Override
        public int index() {
            return tb.index();
        }

        @Override
        public void rewind(int arg0) {
            tb.rewind(arg0);
        }

        @Override
        public void rewind() {
            tb.mark();
            tb.rewind(lastMark);
        }

        @Override
        public void seek(int arg0) {
            tb.seek(arg0);
        }

        @Override
        public Token get(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public TokenSource getTokenSource() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public String toString(int arg0, int arg1) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public String toString(Token arg0, Token arg1) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public void release(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public String getSourceName() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int range() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }
    
    
    private CsmCacheMap getEvaluatorEvalCache() {
        if (level > 0) {
            return null;
        }
        return CsmCacheManager.getClientCache(EvaluateRequest.class, EVAL_INITIALIZER);
    }
    
    private CsmCacheMap getEvaluatorGetMappingCache() {
        if (level > 0) {
            return null;
        }
        return CsmCacheManager.getClientCache(GetMappingRequest.class, GET_MAPPING_INITIALIZER);
    }
    
    private static final Callable<CsmCacheMap> EVAL_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("Evaluator: eval cache", 1); // NOI18N
        }

    };

    private static final Callable<CsmCacheMap> GET_MAPPING_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("Evaluator: get mapping cache", 1); // NOI18N
        }

    };
    
    private final static class EvaluateRequest {
        
        private final String expression;
        
        private final CsmInstantiation instantiation;
        
        private final CsmScope scope;

        public EvaluateRequest(String expression, CsmInstantiation instantiation, CsmScope scope) {
            this.expression = expression;
            this.instantiation = instantiation;
            this.scope = scope;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 19 * hash + Objects.hashCode(this.expression);
            hash = 19 * hash + System.identityHashCode(this.instantiation);
            hash = 19 * hash + System.identityHashCode(this.scope);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EvaluateRequest other = (EvaluateRequest) obj;
            if (!Objects.equals(this.expression, other.expression)) {
                return false;
            }
            if (this.instantiation != other.instantiation) {
                return false;
            }
            if (this.scope != other.scope) {
                return false;
            }
            return true;
        }
    }
    
    private final static class GetMappingRequest {
        
        private final CsmInstantiation instantiation;

        public GetMappingRequest(CsmInstantiation instantiation) {
            this.instantiation = instantiation;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.instantiation);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GetMappingRequest other = (GetMappingRequest) obj;
            if (this.instantiation != other.instantiation) {
                return false;
            }
            return true;
        }
    }
}
