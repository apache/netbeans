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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmVariadicSpecializationParameter;
import org.openide.util.Lookup;

/**
 * Service that provides template instantiations
 * 
 */
public abstract class CsmInstantiationProvider {
    
    /** A dummy provider that never returns any results.
     */
    private static final CsmInstantiationProvider EMPTY = new Empty();
    /** default instance */
    private static CsmInstantiationProvider defaultProvider;

    protected CsmInstantiationProvider() {
    }

    /** Static method to obtain the provider.
     * @return the provider
     */
    public static CsmInstantiationProvider getDefault() {
        /*no need for sync synchronized access*/
        if (defaultProvider != null) {
            return defaultProvider;
        }
        defaultProvider = Lookup.getDefault().lookup(CsmInstantiationProvider.class);
        return defaultProvider == null ? EMPTY : defaultProvider;
    }
    
    /**
     * Deduces template type for the given parameter using patternType
     * as a pattern to find template parameter and actualType as type
     * from which template type should be calculated.
     * 
     * Example: 
     *  templateParam = T
     *  patternType = AAA<T*>
     *  actualType = AAA<BBB**>
     * Function will return type 'BBB*'.
     * 
     * @param templateParam
     * @param patternType
     * @param actualType
     * @param strategy
     * 
     * @return calculated type(s) or null if type couldn't be calculated
     */
    public abstract CsmType[] deduceTemplateType(CsmTemplateParameter templateParam, CsmType patternType, CsmType actualType, DeduceTemplateTypeStrategy strategy);

    /**
     * Returns instantiation of template
     *
     * @param template - template for instantiation
     * @param params - template parameters
     * @return - instantiation
     */
    public abstract CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params);

    /**
     * Returns instantiation of template
     *
     * @param template - template for instantiation
     * @param params - template parameters
     * @param mapping - template mapping
     * @return - instantiation
     */
    public abstract CsmObject instantiate(CsmTemplate template, CsmInstantiation instantiation);

    /**
     * Returns instantiation of template
     *
     * @param template - template for instantiation
     * @param params - template parameters
     * @param type - template type
     * @return - instantiation
     */
    public abstract CsmObject instantiate(CsmTemplate template, CsmType type);
    
    /**
     * Returns template parameter instantiated type
     * 
     * @param templateParam
     * @param instantiations
     * @return parameter type
     */
    public abstract CsmType instantiate(CsmTemplateParameter templateParam, List<CsmInstantiation> instantiations);
    
    /**
     * Returns true if type is instantiation of type.
     * 
     * @param type
     * @return true if type is instantiation
     */
    public abstract boolean isInstantiatedType(CsmType type);
    
    /**
     * Returns the most outer (first) instantiation of the type
     * 
     * @param type
     * @return instantiation or null
     */
    public abstract CsmInstantiation getInstantiatedTypeInstantiation(CsmType type);
    
    /**
     * Returns list of instantiations with which type is instantiated
     * 
     * @param type
     * @return list of instantiations or null
     */
    public abstract List<CsmInstantiation> getInstantiatedTypeInstantiations(CsmType type);
    
    /**
     * Returns the most basic original type.
     * 
     * @param type
     * @return unfolded original type
     */
    public abstract CsmType getOriginalType(CsmType type);
    
    /**
     * Returns the most basic instantiated type.
     * 
     * @param type
     * @return unfolded instantiated type
     */
    public abstract CsmType getInstantiatedType(CsmType type);
    
    /**
     * Return true if instantiation is viable (e.g. it's instantiation parameters are resolved)
     * 
     * @param instantiation
     * @param acceptTemplateParams - whether to accept not binded template params in instantiations
     * @return true if instantiation is viable
     */
    public abstract boolean isViableInstantiation(CsmInstantiation instantiation, boolean acceptTemplateParams);

    /**
     * Creates specialization parameter based on type.
     *
     * @param type - type for parameter
     * @return specialization parameter
     */
    public abstract CsmTypeBasedSpecializationParameter createTypeBasedSpecializationParameter(CsmType type, CsmScope scope);
    
    /**
     * Creates specialization parameter based on type.
     *
     * @param type - type for parameter
     * @param scope - scope of expression
     * @param file - containing file
     * @param start - start offset
     * @param end - end offset* 
     * @return specialization parameter
     */
    public abstract CsmTypeBasedSpecializationParameter createTypeBasedSpecializationParameter(CsmType type, CsmScope scope, CsmFile file, int start, int end);    

    /**
     * Creates variadic specialization parameter (template parameter pack).
     * 
     * @param args
     * @param file
     * @param start
     * @param end
     * @return 
     */
    public abstract CsmVariadicSpecializationParameter createVariadicSpecializationParameter(List<CsmSpecializationParameter> args, CsmFile file, int start, int end);
    
     /**
     * Creates specialization parameter based on expression.
     *
     * @param expression - string with expression
     * @param scope - scope of expression
     * @param file - containing file
     * @param start - start offset
     * @param end - end offset
     * @return specialization parameter
      */
    public abstract CsmExpressionBasedSpecializationParameter createExpressionBasedSpecializationParameter(String expression, CsmScope scope, CsmFile file, int start, int end);
    
    /**
     * returns text of original type
     */
    public abstract CharSequence getOriginalText(CsmType type);
    
    /**
     * returns instantiated text if possible to resolve all instantiation mappings
     */
    public abstract CharSequence getInstantiatedText(CsmType type);

    /**
     * returns signature of template parameters
     */
    public abstract CharSequence getTemplateSignature(CsmTemplate template);
    
    /**
     * Returns class specializations
     *
     * @param classifier - template declaration
     * @param contextFile - file
     * @param contextOffset - offset
     * @return
     */
    public abstract Collection<CsmOffsetableDeclaration> getSpecializations(CsmDeclaration templateDecl, CsmFile contextFile, int contextOffset);
    public Collection<CsmOffsetableDeclaration> getSpecializations(CsmDeclaration templateDecl) {
        return getSpecializations(templateDecl, null, -1);
    }
    
    public abstract Collection<CsmOffsetableDeclaration> getBaseTemplate(CsmDeclaration declaration);
    
    public interface DeduceTemplateTypeStrategy {
        
        /**
         * @param error
         * @return true to continue extracting, false to stop
         */
        boolean canSkipError(Error error);
        
        public enum Error {
            MatchQualsError,
            ExtractNextTypeError
        }        
    }
    
    public static final class DefaultDeduceTemplateTypeStrategy implements DeduceTemplateTypeStrategy {
        
        private final Set<Error> errors = new HashSet<Error>();
        
        private final Set<Error> acceptableErrors;
        
        public DefaultDeduceTemplateTypeStrategy() {
            this(Collections.<Error>emptySet());
        }
        
        public DefaultDeduceTemplateTypeStrategy(Error ... acceptableErrors) {
            this(EnumSet.copyOf(Arrays.asList(acceptableErrors)));
        }

        public DefaultDeduceTemplateTypeStrategy(Set<Error> acceptableErrors) {
            this.acceptableErrors = acceptableErrors;
        }
        
        public final Set<Error> getErrors() {
            return Collections.unmodifiableSet(errors);
        }        

        @Override
        public boolean canSkipError(Error error) {
            errors.add(error);
            return acceptableErrors.contains(error);
        }
    }
    
    //
    // Implementation of the default provider
    //
    private static final class Empty extends CsmInstantiationProvider {

        Empty() {
        }

        @Override
        public CsmType[] deduceTemplateType(CsmTemplateParameter templateParam, CsmType patternType, CsmType actualType, DeduceTemplateTypeStrategy strategy) {
            return null;
        }

        @Override
        public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params) {
            return template;
        }

        @Override
        public CsmObject instantiate(CsmTemplate template, CsmInstantiation instantiation) {
            return template;
        }

        @Override
        public CsmObject instantiate(CsmTemplate template, CsmType type) {
            return template;
        }

        @Override
        public CsmType instantiate(CsmTemplateParameter templateParam, List<CsmInstantiation> instantiations) {
            return null;
        }

        @Override
        public boolean isInstantiatedType(CsmType type) {
            return false;
        }

        @Override
        public CsmInstantiation getInstantiatedTypeInstantiation(CsmType type) {
            return null;
        }

        @Override
        public List<CsmInstantiation> getInstantiatedTypeInstantiations(CsmType type) {
            return null;
        }

        @Override
        public CsmType getOriginalType(CsmType type) {
            return null;
        }

        @Override
        public CsmType getInstantiatedType(CsmType type) {
            return null;
        }

        @Override
        public boolean isViableInstantiation(CsmInstantiation instantiation, boolean acceptTemplateParams) {
            return false;
        }

        @Override
        public CsmTypeBasedSpecializationParameter createTypeBasedSpecializationParameter(CsmType type, CsmScope scope) {
            return null;
        }
        
        @Override
        public CsmTypeBasedSpecializationParameter createTypeBasedSpecializationParameter(CsmType type, CsmScope scope, CsmFile file, int start, int end) {
            return null;
        }        

        @Override
        public CsmExpressionBasedSpecializationParameter createExpressionBasedSpecializationParameter(String expression, CsmScope scope, CsmFile file, int start, int end) {
            return null;
        }

        @Override
        public CsmVariadicSpecializationParameter createVariadicSpecializationParameter(List<CsmSpecializationParameter> args, CsmFile file, int start, int end) {
            return null;
        }

        @Override
        public CharSequence getOriginalText(CsmType type) {
            return type.getText();
        }

        @Override
        public CharSequence getInstantiatedText(CsmType type) {
            return type.getText();
        }

        @Override
        public CharSequence getTemplateSignature(CsmTemplate template) {
            return ""; // NOI18N
        }

        @Override
        public Collection<CsmOffsetableDeclaration> getSpecializations(CsmDeclaration templateDecl, CsmFile contextFile, int contextOffset) {
            return Collections.<CsmOffsetableDeclaration>emptyList();
        }

        @Override
        public Collection<CsmOffsetableDeclaration> getBaseTemplate(CsmDeclaration declaration) {
            return Collections.<CsmOffsetableDeclaration>emptyList();
        }

    }
}
