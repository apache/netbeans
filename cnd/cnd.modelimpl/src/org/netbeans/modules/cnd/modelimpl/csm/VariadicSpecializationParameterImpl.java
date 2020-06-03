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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmVariadicSpecializationParameter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndCollectionUtils;

/**
 *
 */
public class VariadicSpecializationParameterImpl extends OffsetableBase implements CsmVariadicSpecializationParameter, SelfPersistent, Persistent {
    
    private final List<CsmSpecializationParameter> args = new ArrayList<>();

    public VariadicSpecializationParameterImpl(List<CsmSpecializationParameter> args, CsmFile file, int start, int end) {
        super(file, start, end);
        this.args.addAll(args);
    }

    @Override
    public CsmScope getScope() {
        return null; // Always null here
    }    
    
    @Override
    public List<CsmSpecializationParameter> getArgs() {
        return args;
    }

    @Override
    public CharSequence getText() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CsmSpecializationParameter p : getArgs()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            if(CsmKindUtilities.isTypeBasedSpecalizationParameter(p)) {
                sb.append(TypeImpl.getCanonicalText(((CsmTypeBasedSpecializationParameter) p).getType()));
            }
            if(CsmKindUtilities.isExpressionBasedSpecalizationParameter(p)) {
                sb.append(((CsmExpressionBasedSpecializationParameter) p).getText());
            }
        }
        return sb;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        for (CsmSpecializationParameter a : args) {
            hash = 29 * hash + Objects.hashCode(a);
        }
        hash = 29 * hash + Objects.hashCode(super.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VariadicSpecializationParameterImpl other = (VariadicSpecializationParameterImpl) obj;
        if (!super.equals(obj)) {
            return false;
        }
        return CndCollectionUtils.equals(args, other.args);
    }

    @Override
    public String toString() {
        return getText().toString();
    }
        
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeSpecializationParameters(args, output);
    }

    public VariadicSpecializationParameterImpl(RepositoryDataInput input) throws IOException {
        super(input);
        PersistentUtils.readSpecializationParameters(args, input);
    }    
}
