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

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 */
public class TemplateParameterTypeImpl implements CsmType, CsmTemplateParameterType, SelfPersistent {
    private final CsmType type;
    private final CsmUID<CsmTemplateParameter> parameter;
    
    public TemplateParameterTypeImpl(CsmType type, CsmTemplateParameter parameter) {
        this.type = type;
        this.parameter = UIDCsmConverter.objectToUID(parameter);
    }

    TemplateParameterTypeImpl(TemplateParameterTypeImpl type, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile) {
        this.type = TypeFactory.createType(type.type, pointerDepth, reference, arrayDepth, _const, _volatile);
        this.parameter = type.parameter;
    }

    TemplateParameterTypeImpl(TemplateParameterTypeImpl type, List<CsmSpecializationParameter> instantiationParams) {
        this.type = TypeFactory.createType(type.type, instantiationParams);
        this.parameter = type.parameter;
    }

    @Override
    public CsmTemplateParameter getParameter() {
        return UIDCsmConverter.UIDtoCsmObject(this.parameter);
    }

    @Override
    public CsmType getTemplateType() {
        return type;
    }

    @Override
    public CsmFile getContainingFile() {
        return type.getContainingFile();
    }

    @Override
    public int getEndOffset() {
        return type.getEndOffset();
    }

    @Override
    public Position getEndPosition() {
        return type.getEndPosition();
    }

    @Override
    public CharSequence getClassifierText() {
        return type.getClassifierText();
    }

    @Override
    public int getStartOffset() {
        return type.getStartOffset();
    }

    @Override
    public Position getStartPosition() {
        return type.getStartPosition();
    }

    @Override
    public CharSequence getText() {
        return type.getText();
    }

    @Override
    public int getArrayDepth() {
        return type.getArrayDepth();
    }

    @Override
    public CharSequence getCanonicalText() {
        return type.getCanonicalText();
    }

    @Override
    public CsmClassifier getClassifier() {
        CsmTemplateParameter ref = UIDCsmConverter.UIDtoCsmObject(parameter);
        if (CsmKindUtilities.isClassifier(ref)) {
            return (CsmClassifier) ref;
        }
        return type.getClassifier(); // fallback
    }

    @Override
    public int getPointerDepth() {
        return type.getPointerDepth();
    }

    @Override
    public boolean isBuiltInBased(boolean resolveTypeChain) {
        return type.isBuiltInBased(resolveTypeChain);
    }

    @Override
    public boolean isConst() {
        return type.isConst();
    }

    @Override
    public boolean isVolatile() {
        return type.isVolatile();
    }

    @Override
    public boolean isPointer() {
        return type.isPointer();
    }

    @Override
    public boolean isReference() {
        return type.isReference();
    }

    @Override
    public boolean isRValueReference() {
        return type.isRValueReference();
    }

    @Override
    public boolean isPackExpansion() {
        return type.isPackExpansion();
    }

    @Override
    public List<CsmSpecializationParameter> getInstantiationParams() {
        return type.getInstantiationParams();
    }

    @Override
    public boolean hasInstantiationParams() {
        return type.hasInstantiationParams();
    }

    @Override
    public boolean isInstantiation() {
        return type.isInstantiation();
    }

    @Override
    public boolean isTemplateBased() {
        return true;
    }

    // package
    CharSequence getOwnText() {
        if (type instanceof TypeImpl) {
            return ((TypeImpl) type).getOwnText();
        } else if (type instanceof TemplateParameterTypeImpl) {
            return ((TemplateParameterTypeImpl) type).getOwnText();
        } else {
            return "";
        }
    }
    
    @Override
    public String toString() {
        return "TEMPLATE PARAMETER TYPE " + getText()  + "[" + getStartOffset() + "-" + getEndOffset() + "]"; // NOI18N;
    }    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.type);
        hash = 67 * hash + Objects.hashCode(this.parameter);
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
        final TemplateParameterTypeImpl other = (TemplateParameterTypeImpl) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.parameter, other.parameter)) {
            return false;
        }
        return true;
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        PersistentUtils.writeType(type, output);
        UIDObjectFactory.getDefaultFactory().writeUID(parameter, output);
    }  
    
    public TemplateParameterTypeImpl(RepositoryDataInput input) throws IOException {
        type = PersistentUtils.readType(input);
        parameter = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}
