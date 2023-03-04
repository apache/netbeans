/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.spellchecker.bindings.php;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.netbeans.modules.spellchecker.spi.language.TokenListProvider;
import org.netbeans.modules.spellchecker.spi.language.support.MultiTokenList;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TokenListProvider.class)
public class PHPTokenListProvider implements TokenListProvider {

    @Override
    public TokenList findTokenList(Document doc) {
        String mimeType = NbEditorUtilities.getMimeType(doc);
        if (FileUtils.PHP_MIME_TYPE.equals(mimeType)
                && doc instanceof BaseDocument) {
            for (TokenListProvider p : MimeLookup.getLookup(MimePath.get("text/html")).lookupAll(TokenListProvider.class)) { // NOI18N
                TokenList l = p.findTokenList(doc);
                if (l != null) {
                    List<TokenList> tokens = new ArrayList<>(2);
                    tokens.add(new PHPTokenList(doc));
                    tokens.add(l);
                    return MultiTokenList.create(tokens);
                }
            }
        }
        return null;
    }

}
