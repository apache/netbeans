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
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmKnRName;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterImpl.ParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * parameter list of non K&R function
 */
public class FunctionParameterListImpl extends ParameterListImpl<CsmFunctionParameterList, CsmParameter> implements CsmFunctionParameterList {

    protected FunctionParameterListImpl(CsmFile file, int start, int end, Collection<CsmParameter> parameters) {
        super(file, start, end, parameters);
    }

    public static FunctionParameterListImpl create(CsmFile file, int start, int end, Collection<CsmParameter> parameters) {
        return new FunctionParameterListImpl(file, start, end, parameters);
    }

    private static FunctionParameterListImpl create(CsmFile file, FileContent fileContent, AST lParen, AST rParen, AST firstList, AST krList, CsmScope scope) {
        if (lParen == null || lParen.getType() != CPPTokenTypes.LPAREN || rParen == null || rParen.getType() != CPPTokenTypes.RPAREN) {
            return null;
        }
        List<CsmParameter> parameters = AstRenderer.renderParameters(krList == null ? firstList : krList, file, fileContent, scope);
        return FunctionParameterListImpl.create(file, getStartOffset(lParen), getEndOffset(rParen), parameters);
    }

    /*package*/ static FunctionParameterListImpl create(CsmFunctionParameterList originalParamList, Collection<CsmParameter> parameters) {
        return FunctionParameterListImpl.create(originalParamList.getContainingFile(), originalParamList.getStartOffset(),
                originalParamList.getEndOffset(), parameters);
    }


    @Override
    public CsmParameterList<CsmKnRName> getKernighanAndRitchieParameterList() {
        return null;
    }

    @Override
    public String toString() {
        return "Fun " + super.toString(); // NOI18N
    }

    public static FunctionParameterListImpl create(CsmFile file, FileContent fileContent, AST funAST, CsmScope scope) {
        AST lParen = null;
        AST rParen = null;
        AST paramList = null;
        AST krList = null;
        AST prev = null;
        AST token;
        LinkedList<AST> lParens = new LinkedList<>();
        for (token = funAST.getFirstChild(); token != null; token = token.getNextSibling()) {
            if (token.getType() == CPPTokenTypes.CSM_PARMLIST) {
                paramList = token;
                // previous is "("
                lParen = prev;
                // next is ")"
                rParen = token.getNextSibling();
                break;
            } else if (token.getType() == CPPTokenTypes.RPAREN) {
                // could be function without parameters
                if (!lParens.isEmpty()) {
                    lParen = lParens.removeLast();
                }
                rParen = token;
            } else if (token.getType() == CPPTokenTypes.LITERAL_throw) {
                if (rParen != null) {
                    // after empty fun params
                    assert lParen != null;
                    break;
                }
            } else if (token.getType() == CPPTokenTypes.LPAREN) {
                lParens.addLast(token);
            }
            prev = token;
        }
        if (rParen != null) {
            krList = AstUtil.findSiblingOfType(rParen, CPPTokenTypes.CSM_KR_PARMLIST);
        } else {
            return null;
        }
        return create(file, fileContent, lParen, rParen, paramList, krList, scope);
    }
    
    public static class FunctionParameterListBuilder extends ScopedDeclarationBuilder {
                
        private List<ParameterBuilder> parameterBuilsers = new ArrayList<>();
        
        public void addParameterBuilder(ParameterBuilder parameterBuilser) {
            parameterBuilsers.add(parameterBuilser);
        }
        
        public List<ParameterBuilder> getParameterBuilders() {
            return Collections.unmodifiableList(parameterBuilsers);
        }

        public FunctionParameterListImpl create() {
            List<CsmParameter> parameters = new ArrayList<>();
            for (ParameterBuilder parameterBuilder : parameterBuilsers) {
                parameterBuilder.setScope(getScope());
                parameters.add(parameterBuilder.create());
            }
            FunctionParameterListImpl list = new FunctionParameterListImpl(getFile(), getStartOffset(), getEndOffset(), parameters);
            return list;
        }
    }      
    

    ////////////////////////////////////////////////////////////////////////////
    // persistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }

    @SuppressWarnings("unchecked")
    public FunctionParameterListImpl(RepositoryDataInput input, CsmScope scope) throws IOException {
        super(input, scope);
    }

    /**
     * parameter list of K&R function
     */
    public static final class FunctionKnRParameterListImpl extends FunctionParameterListImpl {
        private final ParameterListImpl<CsmParameterList<CsmKnRName>, CsmKnRName> krList;

        private FunctionKnRParameterListImpl(CsmFile file, int start, int end,
                Collection<CsmParameter> parameters, ParameterListImpl<CsmParameterList<CsmKnRName>, CsmKnRName> krList) {
            super(file, start, end, parameters);
            this.krList = krList;
        }

        @Override
        public CsmParameterList<CsmKnRName> getKernighanAndRitchieParameterList() {
            return krList;
        }

        @Override
        public String toString() {
            return "K&R " + super.toString(); // NOI18N
        }

        ////////////////////////////////////////////////////////////////////////////
        // persistent
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            PersistentUtils.writeParameterList(this.krList, output);
        }

        @SuppressWarnings("unchecked")
        public FunctionKnRParameterListImpl(RepositoryDataInput input, CsmScope scope) throws IOException {
            super(input, scope);
            this.krList = (ParameterListImpl<CsmParameterList<CsmKnRName>, CsmKnRName>) PersistentUtils.readParameterList(input, scope);
        }
    }
}
