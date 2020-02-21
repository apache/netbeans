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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;

/**
 * A key for CsmInstantiation
 * 
 */
/*package*/ final class InstantiationKey extends OffsetableKey {
    
    InstantiationKey(CsmInstantiation inst) {
        super(inst.getTemplateDeclaration(), NameCache.getManager().getString(getName(inst))); // NOI18N
    }
    
    private static CharSequence getName(CsmInstantiation inst) {
        StringBuilder sb = new StringBuilder(inst.getTemplateDeclaration().getName());
        sb.append("<"); // NOI18N
        Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = inst.getMapping();
        boolean first = true;
        for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> param : mapping.entrySet()) {
            CsmSpecializationParameter specParam = param.getValue();
            if(CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam)) {
                if(!first) {
                    sb.append(","); // NOI18N
                }
                CsmType type = ((CsmTypeBasedSpecializationParameter)specParam).getType();
                sb.append(type.getCanonicalText());
                first = false;
            }
        }
        sb.append(">"); // NOI18N
        return sb;
    }

    /*package*/ InstantiationKey(RepositoryDataInput aStream) throws IOException {
        super(aStream);
    }

    InstantiationKey(KeyDataPresentation presentation) {
        super(presentation);
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return CsmObjectFactory.instance();
    }
    
    @Override
    char getKind() {
        return Utils.getCsmInstantiationKindKey();
    }

    @Override
    public short getHandler() {
        return KeyObjectFactory.KEY_INSTANTIATION_KEY;
    }

    @Override
    public String toString() {
        String retValue;

        retValue = "InstantiationKey: " + super.toString(); // NOI18N
        return retValue;
    }

    @Override
    public int getSecondaryDepth() {
        return super.getSecondaryDepth() + 1;
    }

    @Override
    public int getSecondaryAt(int level) {
        if (level == 0) {
            return getHandler();
        } else {
            return super.getSecondaryAt(level - 1);
        }
    }
}
