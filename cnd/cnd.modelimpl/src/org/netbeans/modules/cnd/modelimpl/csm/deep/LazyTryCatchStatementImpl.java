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

import org.netbeans.modules.cnd.antlr.TokenStream;

import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider.CsmParser;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider.CsmParserResult;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Lazy try-catch statements
 *
 */
public final class LazyTryCatchStatementImpl extends LazyStatementImpl implements CsmCompoundStatement {

    private LazyTryCatchStatementImpl(CsmFile file, int start, int end, int macroStartMarker, CsmFunction scope) {
        super(file, start, end, macroStartMarker, scope);
    }

    public static LazyTryCatchStatementImpl create(AST ast, CsmFile file, CsmFunction scope) {
        assert (ast.getType() == CPPTokenTypes.CSM_TRY_CATCH_STATEMENT_LAZY);
        return new LazyTryCatchStatementImpl(file, getStartOffset(ast), getEndOffset(ast), getMacroStartMarker(ast), scope);
    }

    @Override
    protected CsmParserResult resolveLazyStatement(TokenStream tokenStream) {
        CsmParser parser = CsmParserProvider.createParser(getContainingFile());
        if (parser != null) {
            parser.init(this, tokenStream, null);
            return parser.parse(CsmParser.ConstructionKind.TRY_BLOCK);
        }
        assert false : "parser not found";
        return null;
    }

    @Override
    protected int/*CPPTokenTypes*/ getFirstTokenID() {
        return CPPTokenTypes.LITERAL_try;
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }

    public LazyTryCatchStatementImpl(RepositoryDataInput input) throws IOException {
        super(input);
    }

}
