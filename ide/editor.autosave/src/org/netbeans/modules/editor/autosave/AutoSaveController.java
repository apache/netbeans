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
package org.netbeans.modules.editor.autosave;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.Timer;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.LifecycleManager;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel Graciano
 */
public final class AutoSaveController {

    public static final String KEY_ACTIVE = "autoSaveActive";
    public static final String KEY_INTERVAL = "autoSaveInterval";
    public static final String KEY_SAVE_ON_FOCUS_LOST = "autoSaveOnFocusLost";

    public static final boolean KEY_ACTIVE_DEFAULT = false;

    private static AutoSaveController controller;

    private final PropertyChangeListener listener = evt -> {
        final String name = evt.getPropertyName();
        if (EditorRegistry.FOCUS_LOST_PROPERTY.equals(name)) {
            final Object old = evt.getOldValue();
            if (old instanceof JTextComponent) {
                final Document doc = ((JTextComponent) old).getDocument();
                final DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
                if (dobj != null) {
                    final SaveCookie saveOperation = dobj.getCookie(SaveCookie.class);
                    if (saveOperation != null) {
                        try {
                            saveOperation.save();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
    };

    private Timer timer;

    public static AutoSaveController getInstance() {
        if (controller == null) {
            controller = new AutoSaveController();
        }
        return controller;
    }

    public static Preferences prefs() {
        return NbPreferences.forModule(AutoSaveController.class);
    }

    private void startTimerSave() {
        int delay = prefs().getInt(KEY_INTERVAL, 10);

        if (delay == 0 && timer != null) {
            timer.stop();
            return;
        }
        delay = delay * 1000 * 60;

        if (timer == null) {
            timer = new Timer(delay, event -> {
                if (prefs().getBoolean(KEY_ACTIVE, KEY_ACTIVE_DEFAULT)) {
                    LifecycleManager.getDefault().saveAll();
                }
            });
        } else {
            timer.stop();
        }

        timer.setInitialDelay(delay);
        timer.setDelay(delay);
        timer.start();
    }

    private void stopTimerSave() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void startFocusSave() {
        EditorRegistry.addPropertyChangeListener(listener);
    }

    private void stopFocusSave() {
        EditorRegistry.removePropertyChangeListener(listener);
    }

    public void stop() {
        stopTimerSave();
        stopFocusSave();
    }

    public void synchronize() {
        if (prefs().getBoolean(KEY_ACTIVE, KEY_ACTIVE_DEFAULT)) {
            startTimerSave();
        } else {
            stopTimerSave();
        }

        if (prefs().getBoolean(KEY_SAVE_ON_FOCUS_LOST, false)) {
            startFocusSave();
        } else {
            stopFocusSave();
        }
    }

    @OnStart
    public static class InitializeMe implements Runnable {

        @Override
        public void run() {
            AutoSaveController.getInstance().synchronize();
        }

    }

    @OnStop
    public static class Cleanup implements Runnable {

        @Override
        public void run() {
            AutoSaveController.getInstance().stop();
        }

    }
}
