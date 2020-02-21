/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
