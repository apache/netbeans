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
package org.netbeans.modules.spellchecker.bindings.htmlxml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.netbeans.modules.spellchecker.spi.language.TokenListProvider;

/**
 *
 * @author Poppenreiter, Riha
 */
public class HtmlXmlTokenListProvider implements TokenListProvider {

    private static final Map<String, String> MIME_TO_SETTING_NAME = new HashMap<String, String>();
    static {
        MIME_TO_SETTING_NAME.put("text/html", "HTML"); //NOI18N
        MIME_TO_SETTING_NAME.put("text/xhtml", "XHTML"); //NOI18N
        MIME_TO_SETTING_NAME.put("text/x-jsp", "JSP"); //NOI18N
        MIME_TO_SETTING_NAME.put("text/x-tag", "JSP"); //NOI18N
        MIME_TO_SETTING_NAME.put("text/x-gsp", "GSP"); //NOI18N
        MIME_TO_SETTING_NAME.put("text/x-php5", "PHP"); //NOI18N
    }

    /** Creates a new instance of RubyTokenListProvider */
    public HtmlXmlTokenListProvider() {
    }

    public TokenList findTokenList(Document doc) {
        if (!(doc instanceof BaseDocument)) {
            Logger.getLogger(HtmlXmlTokenListProvider.class.getName()).log(Level.INFO, null,
                    new IllegalStateException("The given document is not an instance of the BaseDocument, is just " +  //NOI18N
                    doc.getClass().getName()));
            return null;
        }

        //html
        final BaseDocument bdoc = (BaseDocument) doc;
        final String docMimetype = NbEditorUtilities.getMimeType(doc);
        final AtomicReference<TokenList> ret = new AtomicReference<TokenList>();
        doc.render(new Runnable() {
            public void run() {
                TokenHierarchy<?> th = TokenHierarchy.get(bdoc);
                Set<LanguagePath> paths = th.languagePaths();
                for(LanguagePath path : paths) {
                    if(path.innerLanguage() == HTMLTokenId.language()) {
                        String settingName = MIME_TO_SETTING_NAME.get(docMimetype);
                        ret.set(new HtmlTokenList(bdoc, settingName));
                        break;
                    }
                }
            }
        });
        if(ret.get() != null) {
            return ret.get();
        }

        //xml
        if ((docMimetype != null) && (docMimetype.contains("xml"))) { //NOI18N
            return new XmlTokenList(bdoc);
        }

        return null;
    }
}
