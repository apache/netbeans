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
