package org.netbeans.modules.java.editor.options;

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

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.util.NbPreferences;

public class InlineHintsSettings {
    
    private static final String INLINE_HINTS = "InlineHints"; // NOI18N

    // see org.netbeans.modules.editor.actions.ShowInlineHintsAction
    private static final String JAVA_INLINE_HINTS_KEY = "enable.inline.hints"; // NOI18N

    private InlineHintsSettings() {
    }

    public static Preferences getCurrentNode() {
        Preferences preferences = NbPreferences.forModule(MarkOccurencesOptionsPanelController.class);
        return preferences.node(INLINE_HINTS).node(getCurrentProfileId());
    }

    private static Preferences getJavaEditorPreferences() {
        // ShowInlineHintsAction is registering the action without setting the mime type
        // this means this is a global toggle right now
        return MimeLookup.getLookup("").lookup(Preferences.class);
//        return MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
    }

    public static boolean isInlineHintsEnabled() {
        return getJavaEditorPreferences().getBoolean(JAVA_INLINE_HINTS_KEY, false);
    }

    public static void setInlineHintsEnabled(boolean enabled) {
        getJavaEditorPreferences().putBoolean(JAVA_INLINE_HINTS_KEY, enabled);
    }

    private static String getCurrentProfileId() {
        return "default"; // NOI18N
    }
    
}
