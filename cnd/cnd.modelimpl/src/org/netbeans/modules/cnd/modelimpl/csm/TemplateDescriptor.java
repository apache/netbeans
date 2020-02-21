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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateParameterImpl.TemplateParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndCollectionUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 *
 */
public final class TemplateDescriptor {
    private final Collection<CsmUID<CsmTemplateParameter>> templateParams;
    private final CharSequence templateSuffix;
    private final int inheritedTemplateParametersNumber;
    private final boolean specialization;

    public TemplateDescriptor(List<CsmTemplateParameter> templateParams, CharSequence templateSuffix, boolean specialization, boolean global) {
        this(templateParams, templateSuffix, 0, specialization, global);
    }

    public TemplateDescriptor(List<CsmTemplateParameter> templateParams, CharSequence templateSuffix, int inheritedTemplateParametersNumber, boolean specialization, boolean global) {
        register(templateParams, global);
        this.templateParams = UIDCsmConverter.objectsToUIDs(templateParams);
        this.templateSuffix = NameCache.getManager().getString(templateSuffix);
        this.inheritedTemplateParametersNumber = inheritedTemplateParametersNumber;
        this.specialization = specialization;
    }
    
    public TemplateDescriptor(List<CsmTemplateParameter> templateParams, CharSequence templateSuffix, int inheritedTemplateParametersNumber, boolean specialization) {
        this.templateParams = UIDCsmConverter.objectsToUIDs(templateParams);
        this.templateSuffix = NameCache.getManager().getString(templateSuffix);
        this.inheritedTemplateParametersNumber = inheritedTemplateParametersNumber;
        this.specialization = specialization;
    }    

    private void register(List<CsmTemplateParameter> templateParams, boolean global){
        for (CsmTemplateParameter par : templateParams){
            if (global) {
                RepositoryUtils.put(par);
            } else {
                Utils.setSelfUID((CsmDeclaration)par);
            }
        }
    }

    public List<CsmTemplateParameter> getTemplateParameters() {
        if (templateParams != null && !templateParams.isEmpty()) {
            List<CsmTemplateParameter> res = new ArrayList<>(templateParams.size());
            for(CsmTemplateParameter par : UIDCsmConverter.UIDsToCsmObjects(templateParams)){
                res.add(par);
            }
            return res;
        }
    	return Collections.<CsmTemplateParameter>emptyList();
    }
    
    public CharSequence getTemplateSuffix() {
        return templateSuffix;
    }
    
    public int getInheritedTemplateParametersNumber() {
        return inheritedTemplateParametersNumber;
    }

    public static TemplateDescriptor createIfNeeded(AST ast, CsmFile file, CsmScope scope, boolean global) {
        if (ast == null) {
            return null;
        }
        return createIfNeededDirect(ast.getFirstChild(), file, scope, global);
    }
    
    public static TemplateDescriptor createIfNeededDirect(AST ast, CsmFile file, CsmScope scope, boolean global) {
        AST start = TemplateUtils.getTemplateStart(ast);
        for (AST token = start; token != null; token = token.getNextSibling()) {
            if (token.getType() == CPPTokenTypes.LITERAL_template) {
                CharSequence classSpecializationSuffix = TemplateUtils.getClassSpecializationSuffix(token, null);
                return new TemplateDescriptor(TemplateUtils.getTemplateParameters(token, file, scope, global),
                            CharSequenceUtils.concatenate("<", classSpecializationSuffix, ">"), //NOI18N
                            classSpecializationSuffix.length() > 0, global);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getTemplateSuffix().toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + CndCollectionUtils.hashCode(templateParams);
        hash = 67 * hash + Objects.hashCode(this.templateSuffix);
        hash = 67 * hash + this.inheritedTemplateParametersNumber;
        hash = 67 * hash + (this.specialization ? 1 : 0);
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
        final TemplateDescriptor other = (TemplateDescriptor) obj;
        if (this.inheritedTemplateParametersNumber != other.inheritedTemplateParametersNumber) {
            return false;
        }
        if (this.specialization != other.specialization) {
            return false;
        }
        if (!Objects.equals(this.templateSuffix, other.templateSuffix)) {
            return false;
        }
        return CndCollectionUtils.equals(this.templateParams, other.templateParams);
    }
    
    public static class TemplateDescriptorBuilder extends ScopedDeclarationBuilder {

        private final List<TemplateParameterBuilder> parameterBuilders = new ArrayList<>();
        private int inheritedTemplateParametersNumber = 0;
        private boolean specialization = false;
        
        public void addParameterBuilder(TemplateParameterBuilder parameterBuilser) {
            parameterBuilders.add(parameterBuilser);
        }
        
        public void addTemplateDescriptorBuilder(TemplateDescriptorBuilder builder) {
            inheritedTemplateParametersNumber = parameterBuilders.size();
            for (TemplateParameterBuilder templateParameterBuilder : builder.parameterBuilders) {
                addParameterBuilder(templateParameterBuilder);
            }
        }

        public void setSpecialization() {
            this.specialization = true;
        }
        
        public TemplateDescriptor create() {
            List<CsmTemplateParameter> templateParams = new ArrayList<>();
            for (TemplateParameterBuilder paramBuilder : parameterBuilders) {
                paramBuilder.setScope(getScope());
                templateParams.add(paramBuilder.create());
            }
            for (CsmTemplateParameter param : templateParams){
                if (isGlobal()) {
                    RepositoryUtils.put(param);
                } else {
                    Utils.setSelfUID((CsmDeclaration)param);
                }
            }
            
            TemplateDescriptor descriptor = new TemplateDescriptor(templateParams, NameCache.getManager().getString(TEMP_PARAM), inheritedTemplateParametersNumber, specialization); 
            return descriptor;
        }
        private static final CharSequence TEMP_PARAM = CharSequences.create("<T>"); // NOI18N
    }      
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent    
    

    public TemplateDescriptor(RepositoryDataInput input) throws IOException {
        int collSize = input.readInt();
        if (collSize < 0) {
            this.templateParams = null;
        } else {
            this.templateParams = UIDObjectFactory.getDefaultFactory().readUIDCollection(new ArrayList<CsmUID<CsmTemplateParameter>>(collSize), input, collSize);
        }
        this.templateSuffix = PersistentUtils.readUTF(input, NameCache.getManager());
        this.inheritedTemplateParametersNumber = input.readInt();
        this.specialization = input.readBoolean();
    }

    public void write(RepositoryDataOutput output) throws IOException {
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(templateParams, output, false);
        PersistentUtils.writeUTF(templateSuffix, output);
        output.writeInt(this.inheritedTemplateParametersNumber);
        output.writeBoolean(specialization);
    }

    boolean isSpecialization() {
        return this.specialization;
    }
}
