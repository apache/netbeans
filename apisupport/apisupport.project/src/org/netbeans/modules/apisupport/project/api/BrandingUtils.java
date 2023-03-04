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

package org.netbeans.modules.apisupport.project.api;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import org.netbeans.modules.apisupport.project.ui.branding.BrandingEditorPanel;

/**
 * Utility class to expose NB platform application branding editor.
 *
 * @since 1.39
 * @author S. Aubrecht
 */
public class BrandingUtils {

    private BrandingUtils() {
    }

    /**
     * Opens branding editor for given project. Must be invoked from EDT.
     * @param displayName Editor's display name.
     * @param p Project to be branded.
     * @param model a branding model to use
     */
    public static void openBrandingEditor(String displayName, final Project p, BrandingModel model) {
        if( !SwingUtilities.isEventDispatchThread() ) {
            throw new IllegalStateException("This method must be invoked from EDT."); //NOI18N
        }
        synchronized( project2dialog ) {
            Dialog dlg = project2dialog.get(p);
            if( null == dlg ) {
                BrandingEditorPanel editor = new BrandingEditorPanel(displayName, model);
                dlg = editor.open();
                project2dialog.put(p, dlg);
                dlg.addWindowListener( new WindowAdapter() {
                    @Override public void windowClosed(WindowEvent e) {
                        synchronized( project2dialog ) {
                            project2dialog.remove(p);
                        }
                    }
                });
            } else {
                dlg.setVisible(true);
                dlg.requestFocusInWindow();
            }
        }
    }

    private static final Map<Project,Dialog> project2dialog = new HashMap<Project,Dialog>(10);

}
