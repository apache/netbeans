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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Container for template specialization parameters
 *
 */
public class SpecializationDescriptor {
    private final List<CsmSpecializationParameter> specializationParams;

    public SpecializationDescriptor(List<CsmSpecializationParameter> specializationParams, boolean global) {
        this.specializationParams = new ArrayList<>(specializationParams);
    }

    public static SpecializationDescriptor create(List<CsmSpecializationParameter> specializationParams, boolean global) {
        return new SpecializationDescriptor(specializationParams, global);
    }

    public List<CsmSpecializationParameter> getSpecializationParameters() {
        if (specializationParams != null) {
            return new ArrayList<>(specializationParams);
        }
    	return Collections.<CsmSpecializationParameter>emptyList();
    }

    public static SpecializationDescriptor createIfNeeded(AST ast, CsmFile file, CsmScope scope, boolean global) {
        if (ast == null) {
            return null;
        }
        AST start = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        if(start != null) {
            return SpecializationDescriptor.create(TemplateUtils.getSpecializationParameters(start, file, scope, global), global);
        }
        start = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        if(start != null) {
            return SpecializationDescriptor.create(TemplateUtils.getSpecializationParameters(start, file, scope, global), global);
        }
        return null;
    }

    @Override
    public String toString() {
        return specializationParams.toString();
    }
    
    public static abstract class SpecializationParameterBuilder extends ScopedDeclarationBuilder {
        public abstract CsmSpecializationParameter create();
    }

    public static class SpecializationDescriptorBuilder extends ScopedDeclarationBuilder {

        private final List<SpecializationParameterBuilder> parameterBuilders = new ArrayList<>();
        
        public void addParameterBuilder(SpecializationParameterBuilder parameterBuilser) {
            parameterBuilders.add(parameterBuilser);
        }
        
        public SpecializationDescriptor create() {
            List<CsmSpecializationParameter> params = new ArrayList<>();
            for (SpecializationParameterBuilder paramBuilder : parameterBuilders) {
                paramBuilder.setScope(getScope());
                params.add(paramBuilder.create());
            }
            SpecializationDescriptor descriptor = new SpecializationDescriptor(params, isGlobal());
            return descriptor;
        }
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    public SpecializationDescriptor(RepositoryDataInput input) throws IOException {
        this.specializationParams = PersistentUtils.readSpecializationParameters(input);
    }

    public void write(RepositoryDataOutput output) throws IOException {
        PersistentUtils.writeSpecializationParameters(specializationParams, output);
    }
}
