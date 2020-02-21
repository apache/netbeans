/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 * Lazy compound statements
 * 
 */
public final class LazyCompoundStatementImpl extends LazyStatementImpl implements CsmCompoundStatement {

    private LazyCompoundStatementImpl(CsmFile file, int start, int end, int macroStartMarker, CsmFunction scope) {
        super(file, start, end, macroStartMarker, scope);
    }

    public static LazyCompoundStatementImpl create(AST ast, CsmFile file, CsmFunction scope) {
        assert (ast.getType() == CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY);
        return new LazyCompoundStatementImpl(file, getStartOffset(ast), getEndOffset(ast), getMacroStartMarker(ast), scope);
    }

    @Override
    protected CsmParserResult resolveLazyStatement(TokenStream tokenStream) {
        CsmParser parser = CsmParserProvider.createParser(getContainingFile());
        if (parser != null) {
            parser.init(this, tokenStream, null);
            return parser.parse(CsmParser.ConstructionKind.COMPOUND_STATEMENT);
        }
        assert false : "parser not found";
        return null;
    }

    @Override
    protected int/*CPPTokenTypes*/ getFirstTokenID() {
        return CPPTokenTypes.LCURLY;
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        // HAVE TO BE ONLY DELEGATION INTO SUPER
        // because non-lazy will be deserialized as lazy
        super.write(output);
    }

    public LazyCompoundStatementImpl(RepositoryDataInput input) throws IOException {
        super(input);
    }
}
