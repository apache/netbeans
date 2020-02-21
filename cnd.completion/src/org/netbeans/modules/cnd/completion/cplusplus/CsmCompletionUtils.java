/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.completion.cplusplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Misc static utility functions
 */
public class CsmCompletionUtils {
    public static final String CPP_AUTO_COMPLETION_TRIGGERS = "cppAutoCompletionTriggers"; //NOI18N
    public static final String CPP_AUTO_COMPLETION_TRIGGERS_DEFAULT = ".;->;.*;->*;::;new ;"; //NOI18N
    public static final String PREPRPOC_AUTO_COMPLETION_TRIGGERS = "autoCompletionTriggersPreproc"; //NOI18N
    public static final String PREPRPOC_AUTO_COMPLETION_TRIGGERS_DEFAULT = "\";<; ;/;"; //NOI18N
    public static final String CPP_AUTO_INSERT_INCLUDE_DIRECTIVES = "autoInsertIncludeDirectives"; //NOI18N
    public static final String DOC_PROVIDER_LIST = "docProviderList"; //NOI18N
    public static final String DOC_PROVIDER_LIST_DEFAULT = DocProviderList.DEFAULT_LIST.toStorageString();

    private CsmCompletionUtils() {
    }

    /**
     * Gets the mime type of a document. If the mime type can't be determined
     * this method will return <code>null</code>. This method should work reliably
     * for Netbeans documents that have their mime type stored in a special
     * property. For any other documents it will probably just return <code>null</code>.
     *
     * @param doc The document to get the mime type for.
     *
     * @return The mime type of the document or <code>null</code>.
     * @see NbEditorDocument#MIME_TYPE_PROP
     */
    public  static String getMimeType(Document doc) {
        return DocumentUtilities.getMimeType(doc);
    }

    /**
     * Gets the mime type of a document in <code>JTextComponent</code>. If
     * the mime type can't be determined this method will return <code>null</code>.
     * It tries to determine the document's mime type first and if that does not
     * work it uses mime type from the <code>EditorKit</code> attached to the
     * component.
     *
     * @param component The component to get the mime type for.
     *
     * @return The mime type of a document opened in the component or <code>null</code>.
     */
    public static String getMimeType(JTextComponent component) {
        if (component == null) {
            return "";
        }
        Document doc = component.getDocument();
        String mimeType = getMimeType(doc);
        if (mimeType == null) {
            EditorKit kit = component.getUI().getEditorKit(component);
            if (kit != null) {
                mimeType = kit.getContentType();
            }
        }
        return mimeType;
    }

    public static boolean isCaseSensitive(String mimeType) {
        if (mimeType == null || mimeType.length() == 0) {
            return false;
        }
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false);
    }

    public static boolean isNaturalSort(String mimeType) {
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_NATURAL_SORT, false);
    }

    public static boolean isAutoInsertIncludeDirectives() {
        String mimeType = MIMENames.SOURCES_MIME_TYPE; // now all settings are from C++
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        return prefs.getBoolean(CPP_AUTO_INSERT_INCLUDE_DIRECTIVES, true);
    }

    public static String[] getCppAutoCompletionTrigers() {
        String mimeType = MIMENames.SOURCES_MIME_TYPE; // now all settings are from C++
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        return prefs.get(CPP_AUTO_COMPLETION_TRIGGERS, CPP_AUTO_COMPLETION_TRIGGERS_DEFAULT).split(";"); //NOI18N
    }

    public static String[] getPreprocAutoCompletionTrigers() {
        String mimeType = MIMENames.SOURCES_MIME_TYPE; // now all settings are from C++
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        return prefs.get(PREPRPOC_AUTO_COMPLETION_TRIGGERS, PREPRPOC_AUTO_COMPLETION_TRIGGERS_DEFAULT).split(";"); //NOI18N
    }

    public static DocProviderList getDocProviderList() {
        String mimeType = MIMENames.SOURCES_MIME_TYPE; // now all settings are from C++
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        return DocProviderList.fromStorageString(prefs.get(DOC_PROVIDER_LIST, DOC_PROVIDER_LIST_DEFAULT));
    }

    public static enum DocProvider {
        SourceCode("source-code"), // NOI18N
        Manual("manual"); // NOI18N

        private final String id;

        private DocProvider(String name) {
            this.id = name;
        }

        private String getID() {
            return id;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CsmCompletionUtils.class, "DocProvider." + id); // NOI18N
        }

        public static DocProvider byId(String id) {
            for (DocProvider m : values()) {
                if (m.id.equals(id)) {
                    return m;
                }
            }
            return null;
        }
    }
    
    public static class DocProviderList {

        public static final DocProviderList DEFAULT_LIST = new DocProviderList(
                Pair.of(DocProvider.SourceCode, true),
                Pair.of(DocProvider.Manual, true));

        private final DocProvider[] providers;
        private final boolean[] enabled;

        public DocProviderList(List<Pair<DocProvider, Boolean>> pairs) {
            this.providers = new DocProvider[pairs.size()];
            this.enabled = new boolean[pairs.size()];
            for (int i = 0; i < pairs.size(); i++) {
                this.providers[i] = pairs.get(i).first();
                this.enabled[i] = pairs.get(i).second();
            }
        }

        private static DocProviderList fromStorageString(String value) {
            if (value.isEmpty()) {
                return DEFAULT_LIST;
            }
            List<Pair<DocProvider, Boolean>> pairs = new ArrayList<>(2);
            for (String s : value.split(";")) { // NOI18N
                String[] p = s.split("#"); // NOI18N
                if (p.length == 2) {
                    DocProvider provider = DocProvider.byId(p[0]);
                    if (provider != null) {
                        boolean enabled = "1".equals(p[1]); // NOI18N
                        pairs.add(Pair.of(provider, enabled));
                    }
                }
            }
            return new DocProviderList(pairs);
        }


        private DocProviderList(Pair<DocProvider, Boolean>... pairs) {
            this.providers = new DocProvider[pairs.length];
            this.enabled = new boolean[pairs.length];
            for (int i = 0; i < pairs.length; i++) {
                providers[i] = pairs[i].first();
                enabled[i] = pairs[i].second();
            }
        }

        public String toStorageString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < providers.length; i++) {
                if (sb.length() > 0) {
                    sb.append(';'); //NOI18N
                }
                sb.append(providers[i].getID());
                sb.append('#'); //NOI18N
                sb.append(enabled[i] ? '1' : '0'); //NOI18N
            }
            return sb.toString();
        }

        public boolean isEmpty() {
            return providers.length == 0;
        }

        public DocProvider[] getProviders() {
            return providers.clone();
        }

        public boolean isEnabled(DocProvider provider) {
            for (int i = 0; i < providers.length; i++) {
                if (providers[i] == provider) {
                    return enabled[i];
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return toStorageString();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Arrays.deepHashCode(this.providers);
            hash = 89 * hash + Arrays.hashCode(this.enabled);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DocProviderList other = (DocProviderList) obj;
            if (!Arrays.deepEquals(this.providers, other.providers)) {
                return false;
            }
            if (!Arrays.equals(this.enabled, other.enabled)) {
                return false;
            }
            return true;
        }
    }
}
