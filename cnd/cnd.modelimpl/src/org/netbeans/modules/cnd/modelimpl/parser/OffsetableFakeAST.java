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

import org.netbeans.modules.cnd.antlr.collections.AST;

/**
 *
 */
public class OffsetableFakeAST extends FakeAST implements OffsetableAST {
    
    private int line = 1;
    
    private int column = 1;
    
    private int startOffset = 0;
    
    private int endOffset = 0;
    

    public OffsetableFakeAST() {
    }
    
    public void initialize(AST ast) {
        super.initialize(ast);
        if (ast instanceof OffsetableAST) {
            OffsetableAST csmAst = (OffsetableAST) ast;
            this.line = csmAst.getLine();
            this.column = csmAst.getColumn();
            this.startOffset = csmAst.getOffset();
            this.endOffset = csmAst.getEndOffset();
        }
    }

    @Override
    public int getLine() {
        return line;
    }
    
    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int getOffset() {
        return startOffset;
    }

    @Override
    public int getEndOffset() {
        return endOffset;   
    }
}
