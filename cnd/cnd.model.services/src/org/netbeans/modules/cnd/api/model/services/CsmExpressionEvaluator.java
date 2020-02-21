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

import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.spi.model.services.CsmExpressionEvaluatorProvider;
import org.openide.util.Lookup;

/**
 *
 */
public class CsmExpressionEvaluator {

    /** A dummy providers that never returns any results.*/
    private static final CsmExpressionEvaluatorProvider EMPTY_PROVIDER = new EmptyExpressionEvaluator();
    /** Default provider. */
    private static CsmExpressionEvaluatorProvider defaultProvider;

    /**
     * Constructor.
     */
    private CsmExpressionEvaluator() {
    }

    /** Static method to obtain the provider.
     * @return the provider
     */
    public static synchronized CsmExpressionEvaluatorProvider getProvider() {
        if (defaultProvider != null) {
            return defaultProvider;
        }
        defaultProvider = Lookup.getDefault().lookup(CsmExpressionEvaluatorProvider.class);
        return defaultProvider == null ? EMPTY_PROVIDER : defaultProvider;
    }

    /**
     * Evaluates expression.
     *
     * @param expr - expression as string
     * @return result object
     */
    public static Object eval(String expr, CsmScope scope) {
        return getProvider().eval(expr, scope);
    }

    /**
     * Evaluates expression.
     *
     * @param expr - expression as string
     * @param inst - instantiation
     * @return result object
     */
    public static Object eval(String expr, CsmInstantiation inst, CsmScope scope) {
        return getProvider().eval(expr, inst, scope);
    }

    /**
     * Evaluates expression.
     *
     * @param expr - expression as string
     * @param decl - context declaration
     * @param mapping - specialization mapping
     * @return result object
     */
    public static Object eval(String expr, CsmOffsetableDeclaration decl, CsmScope scope, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        return getProvider().eval(expr, decl, scope, mapping);
    }
    
    /**
     * Checks if a result of evaluation is valid
     * 
     * @param evaluated - result of evaluation
     * @return true if result is valid, false otherwise
     */
    public static boolean isValid(Object evaluated) {
        return getProvider().isValid(evaluated);
    }

    //
    // Implementation of the default provider
    //
    private static final class EmptyExpressionEvaluator implements CsmExpressionEvaluatorProvider {

        EmptyExpressionEvaluator() {
        }

        @Override
        public Object eval(String expr, CsmScope scope) {
            return expr;
        }

        @Override
        public Object eval(String expr, CsmInstantiation inst, CsmScope scope) {
            return expr;
        }

        @Override
        public Object eval(String expr, CsmOffsetableDeclaration decl, CsmScope scope, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            return expr;
        }

        @Override
        public Object eval(String expr, CsmOffsetableDeclaration decl, CsmScope scope, CsmFile expressionFile, int startOffset, int endOffset, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            return expr;
        }

        @Override
        public boolean isValid(Object evaluated) {
            return false;
        }
    }
}
