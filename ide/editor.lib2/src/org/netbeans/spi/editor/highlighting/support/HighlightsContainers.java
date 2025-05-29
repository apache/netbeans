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
package org.netbeans.spi.editor.highlighting.support;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.openide.util.WeakListeners;

public class HighlightsContainers {
    public static HighlightsContainer inlineHintsSettingAwareContainer(Document doc, HighlightsContainer delegate) {
        class InlineHintsSettingsAwareContainer extends AbstractHighlightsContainer implements PreferenceChangeListener {
            private static final String KEY_ENABLE_INLINE_HINTS = "enable.inline.hints";

            private final Preferences prefs;
            private boolean enabled;

            public InlineHintsSettingsAwareContainer() {
                String mimeType = DocumentUtilities.getMimeType(doc);
                prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
                inlineSettingChanged();
            }

            @Override
            public HighlightsSequence getHighlights(int startOffset, int endOffset) {
                synchronized (this) {
                    if (!enabled) {
                        return HighlightsSequence.EMPTY;
                    }
                }
                return delegate.getHighlights(startOffset, endOffset);
            }

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (KEY_ENABLE_INLINE_HINTS.equals(evt.getKey())) {
                    inlineSettingChanged();
                }
            }

            private void inlineSettingChanged() {
                synchronized (this) {
                    boolean newValue = prefs.getBoolean(KEY_ENABLE_INLINE_HINTS, false);

                    if (enabled == newValue) {
                        return ;
                    }

                    enabled = newValue;
                }

                fireHighlightsChange(0, doc.getLength());
            }
        }

        return new InlineHintsSettingsAwareContainer();
    }
}
