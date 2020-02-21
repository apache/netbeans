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
package org.netbeans.modules.cnd.editor.options;

import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = CodeStylePreferences.Provider.class)
public class CodeStylePreferencesProvider implements CodeStylePreferences.Provider {

    @Override
    public Preferences forFile(FileObject file, String mimeType) {
        return INSTANCE.forFile(file, mimeType);
    }

    @Override
    public Preferences forDocument(Document doc, String mimeType) {
        return INSTANCE.forDocument(doc, mimeType);
    }
    
    public static final CodeStylePreferences.Provider INSTANCE = new CodeStylePreferences.Provider() {
        @Override
        public Preferences forFile(FileObject file, String mimeType) {
            // forFile can be called with (null, null) by infrastructure
            if (mimeType == null) {              
                mimeType = file == null ? null : MIMESupport.getSourceFileMIMEType(file);
            }
            if (mimeType == null) {
              // not accepted 
              return null;
            }
            if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
                return NbPreferences.forModule(CodeStyle.class);
                //return MimeLookup.getLookup(MimePath.parse(MIMENames.C_MIME_TYPE)).lookup(Preferences.class);
            } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
                return NbPreferences.forModule(CodeStyle.class);
                //return MimeLookup.getLookup(MimePath.parse(MIMENames.CPLUSPLUS_MIME_TYPE)).lookup(Preferences.class);
            } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
                return NbPreferences.forModule(CodeStyle.class);
                //return MimeLookup.getLookup(MimePath.parse(MIMENames.HEADER_MIME_TYPE)).lookup(Preferences.class);
            }
            return null;
        }

        @Override
        public Preferences forDocument(Document doc, String mimeType) {
            if (mimeType == null) {
                mimeType = DocumentUtilities.getMimeType(doc);
            }
            if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
                return NbPreferences.forModule(CodeStyle.class);
                //return MimeLookup.getLookup(MimePath.parse(MIMENames.C_MIME_TYPE)).lookup(Preferences.class);
            } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
                return NbPreferences.forModule(CodeStyle.class);
                //return MimeLookup.getLookup(MimePath.parse(MIMENames.CPLUSPLUS_MIME_TYPE)).lookup(Preferences.class);
            } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
                return NbPreferences.forModule(CodeStyle.class);
                //return MimeLookup.getLookup(MimePath.parse(MIMENames.HEADER_MIME_TYPE)).lookup(Preferences.class);
            }
            return null;
        }
    };
}
