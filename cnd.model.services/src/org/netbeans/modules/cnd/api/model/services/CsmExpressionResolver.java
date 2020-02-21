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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.Predicate;
import org.netbeans.modules.cnd.spi.model.services.CsmExpressionResolverImplementation;
import org.netbeans.modules.cnd.utils.Antiloop;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.Lookup;

/**
 *
 */
public final class CsmExpressionResolver {
    
    public static interface ResolvedTypeHandler {
        
        /**
         * Processes result of expression resolving
         * 
         * @param resolvedType
         */
        void process(CsmType resolvedType);
        
    }
    
    /**
     * Resolves expression with the given context (instantiations)
     * @param text to resolve
     * @param contextFile
     * @param contextOffset
     * @param instantiations - context
     * @return collection of acceptable objects
     */
    public static Collection<CsmObject> resolveObjects(CharSequence text, CsmFile contextFile, int contextOffset, List<CsmInstantiation> instantiations) {
        return resolveObjects(text, contextFile, contextOffset, null, instantiations);
    } 
    
    /**
     * Resolves expression with the given context (instantiations)
     * @param text to resolve
     * @param contextFile
     * @param contextOffset
     * @param contextScope - could be null if not known (will be deduced from offset)
     * @param instantiations - context
     * @return collection of acceptable objects
     */
    public static Collection<CsmObject> resolveObjects(CharSequence text, CsmFile contextFile, int contextOffset, CsmScope contextScope, List<CsmInstantiation> instantiations) {
        return DEFAULT.resolveObjects(new SimpleExpression(text, contextFile, contextOffset, contextScope), instantiations);
    }     
    
    /**
     * Resolves expression with the given context (instantiations)
     * @param expression
     * @param instantiations - context
     * @return collection of acceptable objects
     */
    public static Collection<CsmObject> resolveObjects(CsmOffsetable expression, List<CsmInstantiation> instantiations) {
        return DEFAULT.resolveObjects(expression, instantiations);
    }     
    
    /**
     * Resolves type of expression with the given context (instantiations)
     * @param text to resolve
     * @param contextFile
     * @param contextOffset
     * @param instantiations - context
     * @return type
     */
    @Deprecated
    public static CsmType resolveType(CharSequence text, CsmFile contextFile, int contextOffset, List<CsmInstantiation> instantiations) {
        return resolveType(text, contextFile, contextOffset, null, instantiations);
    }   
    
    /**
     * Resolves type of expression with the given context (instantiations)
     * @param text to resolve
     * @param contextFile
     * @param contextOffset
     * @param instantiations - context
     * @param task - all operations on resolved type should be done here
     */
    public static void resolveType(CharSequence text, CsmFile contextFile, int contextOffset, List<CsmInstantiation> instantiations, ResolvedTypeHandler task) {
        resolveType(text, contextFile, contextOffset, null, instantiations, task);
    }       
    
    /**
     * Resolves type of expression with the given context (instantiations)
     * @param text to resolve
     * @param contextFile
     * @param contextOffset
     * @param contextScope - could be null if not known (will be deduced from offset)
     * @param instantiations - context
     * @return type
     */
    @Deprecated
    public static CsmType resolveType(CharSequence text, CsmFile contextFile, int contextOffset, CsmScope contextScope, List<CsmInstantiation> instantiations) {
        SimpleResolvedTypeHandler resolvedHandler = new SimpleResolvedTypeHandler();
        DEFAULT.resolveType(new SimpleExpression(text, contextFile, contextOffset, contextScope), instantiations, resolvedHandler);
        return resolvedHandler.resolved;
    }     
    
    /**
     * Resolves type of expression with the given context (instantiations)
     * @param text to resolve
     * @param contextFile
     * @param contextOffset
     * @param contextScope - could be null if not known (will be deduced from offset)
     * @param instantiations - context
     * @param task - all operations on resolved type should be done here
     */    
    public static void resolveType(CharSequence text, CsmFile contextFile, int contextOffset, CsmScope contextScope, List<CsmInstantiation> instantiations, ResolvedTypeHandler task) {
        DEFAULT.resolveType(new SimpleExpression(text, contextFile, contextOffset, contextScope), instantiations, task);
    }     
    
    /**
     * Resolves type of expression with the given context (instantiations)
     * @param expression to resolve
     * @param instantiations - context
     * @return type
     */
    @Deprecated
    public static CsmType resolveType(CsmOffsetable expression, List<CsmInstantiation> instantiations) {
        SimpleResolvedTypeHandler resolvedHandler = new SimpleResolvedTypeHandler();
        DEFAULT.resolveType(expression, instantiations, resolvedHandler);
        return resolvedHandler.resolved;
    }   

    /**
     * Resolves type of expression with the given context (instantiations)
     * @param expression to resolve
     * @param instantiations - context
     * @param task - all operations on resolved type should be done here
     */
    public static void resolveType(CsmOffsetable expression, List<CsmInstantiation> instantiations, ResolvedTypeHandler task) {
        DEFAULT.resolveType(expression, instantiations, task);
    }       
    
    /**
     * Checks if type should be resolved as a type from macros.
     * 
     * @param type
     * @param scope
     * @return true if type came from macro, false otherwise.
     */
    public static boolean shouldResolveAsMacroType(CsmType type, CsmScope scope) {
        if (type != null && CsmKindUtilities.isOffsetable(scope)) {
            CsmOffsetable offsetable = (CsmOffsetable) scope;
            if (Objects.equals(offsetable.getContainingFile(), type.getContainingFile()) 
                && offsetable.getStartOffset() == type.getStartOffset()
                && offsetable.getEndOffset() == type.getEndOffset()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Can resolve type which comes from macros.
     * 
     * NB! Use with caution!
     * If type is comes from macros it can fail to resolve it's classifier,
     * because it won't determine context appropriately. This method
     * resolves it within passed scope.
     * 
     * @param typeFromMacro
     * @param scope
     * @param instantiations 
     * @param handler - handler for resolved types chain
     * 
     * @return resolved type
     */
    public static CsmType resolveMacroType(CsmType typeFromMacro, CsmScope scope, List<CsmInstantiation> instantiations, ResolvedTypeHandler handler) {
        assert shouldResolveAsMacroType(typeFromMacro, scope);
        CsmType type = typeFromMacro;
        int counter = Antiloop.MAGIC_PLAIN_TYPE_RESOLVING_CONST;
        CompositeResolvedTypeHandler compositeHandler = new CompositeResolvedTypeHandler(new SimpleResolvedTypeHandler(), handler);
        while (type != null && !CsmBaseUtilities.isValid(type.getClassifier()) && !CharSequenceUtils.isNullOrEmpty(type.getClassifierText()) && counter > 0) {
            CsmExpressionResolver.resolveType(
                    CsmInstantiationProvider.getDefault().getOriginalText(type), 
                    type.getContainingFile(), 
                    type.getStartOffset(), 
                    scope, 
                    instantiations,
                    compositeHandler
            );
            type = ((SimpleResolvedTypeHandler) compositeHandler.first).resolved;
            counter--;
        }        
        return type;
    }
    
//<editor-fold defaultstate="collapsed" desc="impl">
    
    private static final CsmExpressionResolverImplementation DEFAULT = new Default();
    
    private CsmExpressionResolver() {
        throw new AssertionError("Not instantiable"); // NOI18N
    }    
    
    /**
     * Keeps various text within context
     */
    private static class SimpleExpression implements CsmExpression {
        
        private final String expression;
        
        private final CsmFile containingFile;
        
        private final CsmScope scope;
        
        private final int startOffset;
        
        private final int endOffset;

        public SimpleExpression(CharSequence expression, CsmFile containingFile, int startOffset, CsmScope scope) {
            this.expression = expression.toString();
            this.containingFile = containingFile;
            this.startOffset = startOffset;
            this.endOffset = startOffset + expression.length();
            this.scope = scope;
        }

        @Override
        public CsmFile getContainingFile() {
            return containingFile;
        }

        @Override
        public int getStartOffset() {
            return startOffset;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public CsmOffsetable.Position getStartPosition() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CsmOffsetable.Position getEndPosition() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CharSequence getText() {
            return expression;
        } 

        @Override
        public CharSequence getExpandedText() {
            return expression;
        }

        @Override
        public CsmExpression.Kind getKind() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public List<CsmStatement> getLambdas() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public List<CsmExpression> getOperands() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CsmExpression getParent() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CsmScope getScope() {
            return scope;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.expression != null ? this.expression.hashCode() : 0);
            hash = 97 * hash + (this.containingFile != null ? this.containingFile.hashCode() : 0);
            hash = 97 * hash + this.startOffset;
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
            final SimpleExpression other = (SimpleExpression) obj;
            if (this.expression != other.expression && (this.expression == null || !this.expression.equals(other.expression))) {
                return false;
            }
            if (this.containingFile != other.containingFile && (this.containingFile == null || !this.containingFile.equals(other.containingFile))) {
                return false;
            }
            if (this.startOffset != other.startOffset) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return expression + " at [" + containingFile.getAbsolutePath() + ":" + startOffset + "]"; // NOI18N
        }
    }       
    
    private static class SimpleResolvedTypeHandler implements ResolvedTypeHandler {
        
        public CsmType resolved;

        @Override
        public void process(CsmType resolvedType) {
            this.resolved = resolvedType;
        }
    }
    
    private static class CompositeResolvedTypeHandler implements ResolvedTypeHandler {
        
        public final ResolvedTypeHandler first;
        
        public final ResolvedTypeHandler second;

        public CompositeResolvedTypeHandler(ResolvedTypeHandler first, ResolvedTypeHandler second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void process(CsmType resolvedType) {
            if (first != null) {
                first.process(resolvedType);
            }
            if (second != null) {
                second.process(resolvedType);
            }
        }
    }
    
    /**
     * Default implementation (just a proxy to a real service)
     */
    private static final class Default implements CsmExpressionResolverImplementation {
        
        private final Lookup.Result<CsmExpressionResolverImplementation> res;
        
        private CsmExpressionResolverImplementation delegate;
        
        
        private Default() {
            res = Lookup.getDefault().lookupResult(CsmExpressionResolverImplementation.class);
        }
        
        private CsmExpressionResolverImplementation getDelegate(){
            CsmExpressionResolverImplementation service = delegate;
            if (service == null) {
                for (CsmExpressionResolverImplementation resolver : res.allInstances()) {
                    service = resolver;
                    break;
                }
                delegate = service;
            }
            return service;
        }

        @Override
        public Collection<CsmObject> resolveObjects(CsmOffsetable expression, List<CsmInstantiation> instantiations) {
            CsmExpressionResolverImplementation service = getDelegate();
            if (service != null) {
                return getDelegate().resolveObjects(expression, instantiations);
            }
            return null;
        }

        @Override
        public void resolveType(CsmOffsetable expression, List<CsmInstantiation> instantiations, ResolvedTypeHandler task) {
            CsmExpressionResolverImplementation service = getDelegate();
            if (service != null) {
                service.resolveType(expression, instantiations, task);
            }
        }
    }
//</editor-fold>    
}
