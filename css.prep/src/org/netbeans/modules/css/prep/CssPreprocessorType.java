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
package org.netbeans.modules.css.prep;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.css.prep.less.LessCssPreprocessor;
import org.netbeans.modules.css.prep.options.CssPrepOptions;
import org.netbeans.modules.css.prep.preferences.CssPreprocessorPreferences;
import org.netbeans.modules.css.prep.preferences.CssPreprocessorPreferencesValidator;
import org.netbeans.modules.css.prep.preferences.LessPreferences;
import org.netbeans.modules.css.prep.preferences.LessPreferencesValidator;
import org.netbeans.modules.css.prep.preferences.SassPreferences;
import org.netbeans.modules.css.prep.preferences.SassPreferencesValidator;
import org.netbeans.modules.css.prep.sass.SassCssPreprocessor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "CssPreprocessorType.sass.displayName=Sass",
    "CssPreprocessorType.less.displayName=LESS",
})
public enum CssPreprocessorType {

    SASS() {
        @Override
        public String getIdentifier() {
            return SassCssPreprocessor.IDENTIFIER;
        }

        @Override
        public String getDisplayName() {
            return Bundle.CssPreprocessorType_sass_displayName();
        }

        @Override
        public String getDefaultDirectoryName() {
            return "scss"; // NOI18N
        }

        @Override
        public Collection<String> getMimeTypes() {
            return Arrays.asList("text/scss", "text/sass"); // NOI18N
        }

        @Override
        public CssPreprocessorPreferences getPreferences() {
            return SassPreferences.getInstance();
        }

        @Override
        public CssPreprocessorPreferencesValidator getPreferencesValidator() {
            return new SassPreferencesValidator();
        }

        @Override
        public String getExecutablePathPropertyName() {
            return CssPrepOptions.SASS_PATH_PROPERTY;
        }

    },
    LESS() {
        @Override
        public String getIdentifier() {
            return LessCssPreprocessor.IDENTIFIER;
        }

        @Override
        public String getDisplayName() {
            return Bundle.CssPreprocessorType_less_displayName();
        }

        @Override
        public String getDefaultDirectoryName() {
            return "less"; // NOI18N
        }

        @Override
        public Collection<String> getMimeTypes() {
            return Collections.singleton("text/less"); // NOI18N
        }

        @Override
        public CssPreprocessorPreferences getPreferences() {
            return LessPreferences.getInstance();
        }

        @Override
        public CssPreprocessorPreferencesValidator getPreferencesValidator() {
            return new LessPreferencesValidator();
        }

        @Override
        public String getExecutablePathPropertyName() {
            return CssPrepOptions.LESS_PATH_PROPERTY;
        }

    };

    public abstract String getIdentifier();
    public abstract String getDisplayName();
    public abstract String getDefaultDirectoryName();
    public abstract Collection<String> getMimeTypes();
    public abstract CssPreprocessorPreferences getPreferences();
    public abstract CssPreprocessorPreferencesValidator getPreferencesValidator();
    public abstract String getExecutablePathPropertyName();

    public Collection<String> getFileExtensions() {
        Set<String> extensions = new HashSet<>();
        for (String mimeType : getMimeTypes()) {
            extensions.addAll(FileUtil.getMIMETypeExtensions(mimeType));
        }
        return extensions;
    }

    private static Map<String, CssPreprocessorType> mime2filetypeMap;

    public static synchronized CssPreprocessorType find(String mimeType) {
        if(mime2filetypeMap == null) {
            mime2filetypeMap = new HashMap<>();
            for(CssPreprocessorType type : values()) {
                for(String mt : type.getMimeTypes()) {
                    mime2filetypeMap.put(mt, type);
                }
            }
        }
        return mime2filetypeMap.get(mimeType);
    }


}
