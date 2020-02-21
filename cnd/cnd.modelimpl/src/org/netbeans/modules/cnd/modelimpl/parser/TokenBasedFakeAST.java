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

package org.netbeans.modules.cnd.modelimpl.parser;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;

/**
 *
 */
public final class TokenBasedFakeAST extends OffsetableFakeAST implements TokenBasedAST {
    
    private Token token = new APTBaseToken();

    public TokenBasedFakeAST() {
        this.token = new APTBaseToken();
    }
    
    public void initialize(AST ast) {
        super.initialize(ast);
        if (ast instanceof TokenBasedAST) {
            this.token = ((TokenBasedAST) ast).getToken();
        }
    }

    @Override
    public Token getToken() {
        return token;
    }    
}
