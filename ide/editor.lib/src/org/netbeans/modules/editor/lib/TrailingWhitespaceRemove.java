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

package org.netbeans.modules.editor.lib;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.document.ModRootElement;
import org.netbeans.modules.editor.lib2.document.TrailingWhitespaceRemoveProcessor;
import org.netbeans.spi.editor.document.OnSaveTask;

/**
 * Removal of trailing whitespace
 *
 * @author Miloslav Metelka
 * @since 1.27
 */
public final class TrailingWhitespaceRemove implements OnSaveTask {

    // -J-Dorg.netbeans.modules.editor.lib.TrailingWhitespaceRemove.level=FINE
    static final Logger LOG = Logger.getLogger(TrailingWhitespaceRemove.class.getName());

    private final Document doc;
    
    private AtomicBoolean canceled = new AtomicBoolean();

    TrailingWhitespaceRemove(Document doc) {
        this.doc = doc;
    }

    @Override
    public void performTask() {
        Preferences prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(doc)).lookup(Preferences.class);
        if (prefs.getBoolean(SimpleValueNames.ON_SAVE_USE_GLOBAL_SETTINGS, Boolean.TRUE)) {
            prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        }
        String policy = prefs.get(SimpleValueNames.ON_SAVE_REMOVE_TRAILING_WHITESPACE, "never"); //NOI18N
        if (!"never".equals(policy)) { //NOI18N
            ModRootElement modRootElement = ModRootElement.get(doc);
            if (modRootElement != null) {
                boolean origEnabled = modRootElement.isEnabled();
                modRootElement.setEnabled(false);
                try {
                    new TrailingWhitespaceRemoveProcessor(doc, "modified-lines".equals(policy), canceled).removeWhitespace(); //NOI18N
                } finally {
                    modRootElement.setEnabled(origEnabled);
                }
            }
        }
    }

    @Override
    public void runLocked(Runnable run) {
        run.run();
    }

    @Override
    public boolean cancel() {
        canceled.set(true);
        return true;
    }

    @MimeRegistration(mimeType="", service=OnSaveTask.Factory.class, position=1000)
    public static final class FactoryImpl implements Factory {

        @Override
        public OnSaveTask createTask(Context context) {
            return new TrailingWhitespaceRemove(context.getDocument());
        }

    }

}
