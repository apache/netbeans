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
