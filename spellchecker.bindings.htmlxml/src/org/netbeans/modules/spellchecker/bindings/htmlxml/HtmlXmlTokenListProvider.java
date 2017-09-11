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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * Portions Copyrighted 2007-2008 Stefan Riha, Roland Poppenreiter
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
