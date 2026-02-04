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
package org.netbeans.modules.favorites;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.favorites.api.Favorites;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbPreferences;
import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * For lifecycle tasks.
 * @author mbien
 */
public final class Module {

    private static final String INITIAL_OPEN_DONE_KEY = "initial-open-done"; //NOI18N

    private Module() {}

    @OnShowing
    public final static class EDTInit implements Runnable {

        @Override
        public void run() {
            registerFavAppenderFunction();
            attachFirstEditorOpenListener();
        }

        private void registerFavAppenderFunction() {
            Function<File[], File[]> favAppender = (files) -> {
                if (!UIManager.getBoolean(LFCustoms.FILECHOOSER_FAVORITES_ENABLED)) {
                    return files;
                }
                List<File> shortcuts = new ArrayList<>(Arrays.asList(files));
                for (FileObject favorite : Favorites.getDefault().getFavoriteRoots()) {
                    File file = FileUtil.toFile(favorite);
                    if (file.isDirectory()) {
                        shortcuts.add(file);
                    }
                }
                return shortcuts.toArray(File[]::new);
            };
            UIManager.put(LFCustoms.FILECHOOSER_SHORTCUTS_FILESFUNCTION, favAppender);
        }

        // very first file editor opened will also open the Favorites tab
        private void attachFirstEditorOpenListener() {
            Preferences prefs = NbPreferences.forModule(Module.class);
            if (prefs.getBoolean(INITIAL_OPEN_DONE_KEY, false)) {
                return;
            }
            TopComponent.Registry registry = TopComponent.getRegistry();
            registry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (TopComponent.Registry.PROP_TC_OPENED.equals(evt.getPropertyName())
                            && evt.getNewValue() instanceof TopComponent tc
                            && hasFileReference(tc)
                            && WindowManager.getDefault().isEditorTopComponent(tc)) {
                        try {
                            Tab favTab = Tab.findDefault();
                            if (favTab != null && !favTab.wasOpened() && !favTab.isOpened()) {
                                favTab.open();
                            }
                        } finally {
                            prefs.putBoolean(INITIAL_OPEN_DONE_KEY, true);
                            PropertyChangeListener thisListener = this;
                            SwingUtilities.invokeLater(() -> {
                                registry.removePropertyChangeListener(thisListener);
                            });
                        }
                    }

                }
            });
        }

        private boolean hasFileReference(TopComponent tc) {
            return tc.getLookup().lookup(DataObject.class) != null;
        }
    }

}
