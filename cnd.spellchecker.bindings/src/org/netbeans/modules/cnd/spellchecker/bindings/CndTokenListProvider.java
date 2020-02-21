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
