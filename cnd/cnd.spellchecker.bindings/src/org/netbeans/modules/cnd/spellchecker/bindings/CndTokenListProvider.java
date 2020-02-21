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
package org.netbeans.modules.cnd.spellchecker.bindings;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.netbeans.modules.spellchecker.spi.language.TokenListProvider;
import org.netbeans.modules.spellchecker.spi.language.support.MultiTokenList;

/**
 * based on JavaTokenListProvider
 */
@MimeRegistrations({
    // cnd source files
    @MimeRegistration(mimeType=MIMENames.HEADER_MIME_TYPE, service=TokenListProvider.class, position=1000),
    @MimeRegistration(mimeType=MIMENames.CPLUSPLUS_MIME_TYPE, service=TokenListProvider.class, position=1000),
    @MimeRegistration(mimeType=MIMENames.C_MIME_TYPE, service=TokenListProvider.class, position=1000),
    @MimeRegistration(mimeType=MIMENames.FORTRAN_MIME_TYPE, service=TokenListProvider.class, position=1000),
    // scripts and make
    @MimeRegistration(mimeType=MIMENames.MAKEFILE_MIME_TYPE, service=TokenListProvider.class, position=1000),
    @MimeRegistration(mimeType=MIMENames.CMAKE_MIME_TYPE, service=TokenListProvider.class, position=1000),
    @MimeRegistration(mimeType=MIMENames.CMAKE_INCLUDE_MIME_TYPE, service=TokenListProvider.class, position=1000),
    @MimeRegistration(mimeType=MIMENames.QTPROJECT_MIME_TYPE, service=TokenListProvider.class, position=1000),
    @MimeRegistration(mimeType=MIMENames.SHELL_MIME_TYPE, service=TokenListProvider.class, position=1000),
    @MimeRegistration(mimeType=MIMENames.BAT_MIME_TYPE, service=TokenListProvider.class, position=1000)
})
public class CndTokenListProvider implements TokenListProvider {
    static final Logger LOG = Logger.getLogger(CndTokenListProvider.class.getName());

    public CndTokenListProvider() {
    }

    @Override
    public TokenList findTokenList(Document doc) {
//        LOG.log(Level.INFO, "creating list for {0}", doc);
        List<TokenList> lists = new LinkedList<TokenList>();
        if (doc instanceof BaseDocument) {
            String mime = DocumentUtilities.getMimeType(doc);
            if (MIMENames.CND_TEXT_MIME_TYPES.contains(mime)) {
//                LOG.log(Level.INFO, "creating source token list for {0}", mime);
                lists.add(new CndTokenList((BaseDocument) doc));
            }
            if (MIMENames.CND_SCRIPT_MIME_TYPES.contains(mime)) {
//                LOG.log(Level.INFO, "creating script token list for {0}", mime);
                lists.add(new ScriptAndMakeTokenList((BaseDocument) doc));
            }
        }

        return !lists.isEmpty() ? MultiTokenList.create(lists) : null;
    }
}
