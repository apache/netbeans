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

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.OffsetableAST;

/**
 *
 */
public class AstRendererException extends Exception {
    
    public static AstRendererException throwAstRendererException(FileImpl file, AST ast, int offset, String message) throws AstRendererException {
        StringBuilder buf = new StringBuilder();
        buf.append('\n').append(file.getAbsolutePath()); //NOI18N
        if (offset != 0) {
            int[] lineColumn = file.getLineColumn(offset);
            buf.append(':').append(lineColumn[0]); //NOI18N
            buf.append(':').append(lineColumn[1]); //NOI18N
            buf.append(": error: ").append(message); //NOI18N
            throw new AstRendererException(buf.toString());
        } else if (ast != null) {
            offset = getStartOffset(ast);
            if (offset == -1) {
                offset = getEndOffset(ast);
            }
            if (offset != -1) {
                int[] lineColumn = file.getLineColumn(offset);
                buf.append(':').append(lineColumn[0]); //NOI18N
                buf.append(':').append(lineColumn[1]); //NOI18N
                buf.append(": error: ").append(message); //NOI18N
                throw new AstRendererException(buf.toString());
            }
        }
        buf.append(": undefined position : error: ").append(message); //NOI18N
        throw new AstRendererException(buf.toString());
    }
    
    private AstRendererException(String message){
        super(message);
    }
    
    private static int getStartOffset(AST node) {
        OffsetableAST csmAst = AstUtil.getFirstOffsetableAST(node);
        if( csmAst != null ) {
            return csmAst.getOffset();
        }
        return -1;
    }

    private static int getEndOffset(AST node) {
        AST lastChild = AstUtil.getLastChildRecursively(node);
        if(lastChild.getType() != Token.EOF_TYPE && lastChild instanceof CsmAST) {
            return ((CsmAST) lastChild).getEndOffset();
        } else {
            // #error directive broke parsing
            // end offset should not be < start one
            lastChild = AstUtil.getLastNonEOFChildRecursively(node);
            if( lastChild instanceof CsmAST ) {
                return ((CsmAST) lastChild).getEndOffset();
            }
        }
        return -1;
    }
}
