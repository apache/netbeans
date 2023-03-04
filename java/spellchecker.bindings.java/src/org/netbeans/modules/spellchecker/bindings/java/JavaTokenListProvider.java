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
package org.netbeans.modules.spellchecker.bindings.java;

import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.netbeans.modules.spellchecker.spi.language.TokenListProvider;
import org.netbeans.modules.spellchecker.spi.language.support.MultiTokenList;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.spellchecker.spi.language.TokenListProvider.class)
public class JavaTokenListProvider implements TokenListProvider {

    static final boolean ENABLE_SEMANTIC_TOKEN_LIST = Boolean.getBoolean(JavaSemanticTokenList.class.getName() + "-enable");
    
    public JavaTokenListProvider() {
    }

    public TokenList findTokenList(Document doc) {
        List<TokenList> lists = new LinkedList<TokenList>();

        if ("text/x-java".equals(doc.getProperty("mimeType")) && doc instanceof BaseDocument) {
            lists.add(new JavaTokenList((BaseDocument) doc));
        }

        if (ENABLE_SEMANTIC_TOKEN_LIST) {
            Object o = doc.getProperty(Document.StreamDescriptionProperty);

            if (o instanceof DataObject) {
                TokenList l = JavaSemanticTokenList.get(((DataObject) o).getPrimaryFile());

                if (l != null) {
                    lists.add(l);
                }
            }
        }

        return !lists.isEmpty() ? MultiTokenList.create(lists) : null;
    }
    
}
