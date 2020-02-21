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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil.ASTExpandedTokensChecker;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil.ASTTokensStringizer;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 *
 */
public class ExpandedExpressionBase extends ExpressionBase {
    
    private static final Logger LOG = Logger.getLogger(ExpandedExpressionBase.class.getSimpleName());
    
    private static final String SKIPPED_STUB = "\"skipped\""; // NOI18N
    
    private static final int MAX_EXPANDING_LENGTH = 512;
    
    private final CharSequence expandedText;
    

    ExpandedExpressionBase(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
        ASTTokensFilteringStringizer stringizer = new ASTTokensFilteringStringizer();
        AstUtil.visitAST(stringizer, ast);
        String expanded = stringizer.getText();
        if (expanded.length() > MAX_EXPANDING_LENGTH) {
            LOG.log(Level.FINE, "Too large expression ({0} symbols) defined inside macros: {1}:{2}", new Object[]{expanded.length(), file, getStartPosition()}); // NOI18N
            expanded = SKIPPED_STUB; // NOI18N
        }
        if (stringizer.getNumberOfStringizedTokens() > 1) {
            expandedText = CharSequences.create(expanded);
        } else {
            expandedText = DefaultCache.getManager().getString(expanded);
        }
    }

    @Override
    public CharSequence getExpandedText() {
        return expandedText;
    }

    public static boolean hasExpandedTokens(AST ast) {
        ASTExpandedTokensChecker checker = new ASTExpandedTokensChecker();
        AstUtil.visitAST(checker, ast);
        return checker.HasExpanded();
    }
        
    private static class ASTTokensFilteringStringizer extends ASTTokensStringizer {

        @Override
        public Action visit(AST token) {
            if (token.getType() == CPPTokenTypes.STRING_LITERAL) {
                sb.append(SKIPPED_STUB);
                numStringizedTokens++;
                return Action.CONTINUE;
            }
            return super.visit(token);
        }        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent    
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeUTF(expandedText, output);
    }    

    public ExpandedExpressionBase(RepositoryDataInput input) throws IOException {
        super(input);
        this.expandedText = PersistentUtils.readUTF(input, DefaultCache.getManager());
    }
}
