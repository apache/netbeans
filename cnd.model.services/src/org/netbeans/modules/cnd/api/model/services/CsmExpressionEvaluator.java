/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
