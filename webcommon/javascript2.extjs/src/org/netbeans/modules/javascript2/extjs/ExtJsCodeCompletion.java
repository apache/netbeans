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
            return Collections.emptyList();
        }
        // find the object that can be configured
        TokenHierarchy<?> th = ccContext.getParserResult().getSnapshot().getTokenHierarchy();
        if (th == null) {
            return Collections.emptyList();
        }
        int carretOffset  = ccContext.getCaretOffset();
        int eOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(carretOffset);
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, eOffset);
        if (ts == null) {
            return Collections.emptyList();
        }
        
        ts.move(eOffset);
        
        if (!ts.moveNext() && !ts.movePrevious()){
            return Collections.emptyList();
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
            return Collections.emptyList();
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
        return Collections.emptyList();
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (element != null && element instanceof ExtJsElement) {
            return ((ExtJsElement)element).getDocumentation();
        }
        return null;
    }
    
}
