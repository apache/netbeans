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

package org.netbeans.modules.cnd.modelimpl.csm.deep;


import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;


import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * Implements ... statement
 */
public final class LabelImpl extends StatementBase implements CsmStatement, CsmLabel {

    private final CharSequence label;

    private LabelImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
        label = NameCache.getManager().getString(AstUtil.getText(ast.getFirstChild()));
    }
    
    private LabelImpl(CharSequence label, CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);
        this.label = label;
    }    

    public static LabelImpl create(AST ast, CsmFile file, CsmScope scope) {
        return new LabelImpl(ast, file, scope);
    }
    
    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.LABEL;
    }
    
    @Override
    public CharSequence getLabel() {
        return label;
    }

    @Override
    public CharSequence getName() {
        return getLabel();
    }
    
    
    public static class LabelBuilder extends StatementBuilder {

        CharSequence label;

        public void setLabel(CharSequence label) {
            this.label = label;
        }

        private CharSequence getLabel() {
            return NameCache.getManager().getString(label);
        }
        
        @Override
        public LabelImpl create() {
            LabelImpl stmt = new LabelImpl(getLabel(), getScope(), getFile(), getStartOffset(), getEndOffset());
            return stmt;
        }
    }     
    
}
