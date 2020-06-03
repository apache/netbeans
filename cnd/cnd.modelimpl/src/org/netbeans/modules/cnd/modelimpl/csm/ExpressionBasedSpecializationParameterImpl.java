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
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.modelimpl.csm.SpecializationDescriptor.SpecializationParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Template specialization parameter based on expression implementation.
 *
 */
public final class ExpressionBasedSpecializationParameterImpl extends OffsetableBase implements CsmExpressionBasedSpecializationParameter, SelfPersistent, Persistent {
    
    private final CsmUID<CsmScope> scope;

    private final CharSequence expression;
    
    private final boolean defaultValue;

    private ExpressionBasedSpecializationParameterImpl(CharSequence expression, CsmScope scope, CsmFile file, int start, int end, boolean defaultValue) {
        super(file, start, end);
        this.expression = NameCache.getManager().getString(expression);
        this.defaultValue = defaultValue;
        if ((scope instanceof CsmIdentifiable)) {
            this.scope = UIDCsmConverter.scopeToUID(scope);
        } else {
            this.scope = null;
        }
    }
    
    public static ExpressionBasedSpecializationParameterImpl create(CsmExpressionStatement expression, CsmFile file, int start, int end) {
        return create(expression, file, start, end, false);
    }    

    public static ExpressionBasedSpecializationParameterImpl create(CsmExpressionStatement expression, CsmFile file, int start, int end, boolean defaultValue) {
        return new ExpressionBasedSpecializationParameterImpl(expression.getText(), expression.getScope(), file, start, end, defaultValue);
    }
    
    public static ExpressionBasedSpecializationParameterImpl create(CharSequence expression, CsmScope scope, CsmFile file, int start, int end) {
        return create(expression, scope, file, start, end, false);
    }  

    public static ExpressionBasedSpecializationParameterImpl create(CharSequence expression, CsmScope scope, CsmFile file, int start, int end, boolean defaultValue) {
        return new ExpressionBasedSpecializationParameterImpl(expression, scope, file, start, end, defaultValue);
    }

    @Override
    public CsmScope getScope() {
        return scope == null? null : scope.getObject();
    }

    @Override
    public boolean isDefaultValue() {
        return defaultValue;
    }
    
    @Override
    public CharSequence getText() {
        return expression;
    }

    @Override
    public String toString() {
        return expression.toString() + super.getOffsetString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.expression);
        hash = 53 * hash + Objects.hashCode(super.hashCode());
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
        final ExpressionBasedSpecializationParameterImpl other = (ExpressionBasedSpecializationParameterImpl) obj;
        if (!Objects.equals(this.expression, other.expression)) {
            return false;
        }
        return super.equals(obj);
    }
    
    public static class ExpressionBasedSpecializationParameterBuilder extends SpecializationParameterBuilder {

        ExpressionBuilder expression;

        public void setExpressionBuilder(ExpressionBuilder expression) {
            this.expression = expression;
        }
        
        @Override
        public ExpressionBasedSpecializationParameterImpl create() {
            CharSequence expr;
            if(expression != null) {
                expr = expression.create().getText();
            } else {
                expr = NameCache.getManager().getString("1"); // NOI18N
            }
            
            ExpressionBasedSpecializationParameterImpl param = new ExpressionBasedSpecializationParameterImpl(expr, null, getFile(), getStartOffset(), getEndOffset(), false);
            return param;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeUTF(expression, output);
        output.writeBoolean(defaultValue);
        UIDObjectFactory.getDefaultFactory().writeUID(scope, output);
    }

    public ExpressionBasedSpecializationParameterImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.expression = PersistentUtils.readUTF(input, NameCache.getManager());
        this.defaultValue = input.readBoolean();
        this.scope = UIDObjectFactory.getDefaultFactory().readUID(input);
    }

}
