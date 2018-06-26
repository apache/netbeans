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
package org.netbeans.modules.javascript2.extjs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Petr Pisl
 */
@CompletionProvider.Registration(priority=11)
public class ExtJsCodeCompletion implements CompletionProvider {
    
    private static final String FILE_LOCATION = "docs/extjs-properties.xml"; //NOI18N
    private static File extPropertyFile;
    
    private static synchronized File getDataFile() {
        if (extPropertyFile == null) {
            extPropertyFile = InstalledFileLocator.getDefault().locate(FILE_LOCATION, "org.netbeans.modules.javascript2.extjs", false); //NOI18N
        }
        return extPropertyFile;
    }
    
    
    private static HashMap<String, Collection<ExtJsDataItem>> ccData = null;
       
    private synchronized static Map<String, Collection<ExtJsDataItem>> getData() {
        return DataLoader.getData(getDataFile());
    }
    
    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        if (jsCompletionContext != CompletionContext.OBJECT_PROPERTY_NAME) {
            return Collections.EMPTY_LIST;
        }
        // find the object that can be configured
        TokenHierarchy<?> th = ccContext.getParserResult().getSnapshot().getTokenHierarchy();
        if (th == null) {
            return Collections.EMPTY_LIST;
        }
        int carretOffset  = ccContext.getCaretOffset();
        int eOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(carretOffset);
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, eOffset);
        if (ts == null) {
            return Collections.EMPTY_LIST;
        }
        
        ts.move(eOffset);
        
        if (!ts.moveNext() && !ts.movePrevious()){
            return Collections.EMPTY_LIST;
        }
        
        Token<? extends JsTokenId> token = null;
        JsTokenId tokenId;
        //find the begining of the object literal
        int balance = 1;
        while (ts.movePrevious() && balance > 0) {
            token = ts.token();
            tokenId = token.id();
            if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                balance++;
            } else if (tokenId == JsTokenId.BRACKET_LEFT_CURLY) {
                balance--;
            }
        }
        if (token == null || balance != 0) {
            return Collections.EMPTY_LIST;
        }
        
        // now we should be at the beginning of the object literal. 
        token = LexUtilities.findPreviousToken(ts, Arrays.asList(JsTokenId.IDENTIFIER));
        tokenId = token.id();
        StringBuilder sb = new StringBuilder(token.text());
        while ((tokenId == JsTokenId.IDENTIFIER || tokenId == JsTokenId.OPERATOR_DOT) && ts.movePrevious()) {
            token = ts.token(); tokenId = token.id();
            if (tokenId == JsTokenId.OPERATOR_DOT) {
                sb.insert(0, '.'); // NOI18N
            } else if (tokenId == JsTokenId.IDENTIFIER) {
                sb.insert(0, token.text());
            }
        }
        
        String fqn = sb.toString();
        Map<String, Collection<ExtJsDataItem>> data = getData();
        Collection<ExtJsDataItem> items = data.get(fqn);
        int anchorOffset = eOffset - ccContext.getPrefix().length();
        if (items != null) {
            List<CompletionProposal> result = new ArrayList<CompletionProposal>();
            for (ExtJsDataItem item : items) {
                if (item.getName().startsWith(prefix)) {
                    result.add(ExtJsCompletionItem.createExtJsItem(item, anchorOffset));
                }
            }
            return result;
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (element != null && element instanceof ExtJsElement) {
            return ((ExtJsElement)element).getDocumentation();
        }
        return null;
    }
    
}
