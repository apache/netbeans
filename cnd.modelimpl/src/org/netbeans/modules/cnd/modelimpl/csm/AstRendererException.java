/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
