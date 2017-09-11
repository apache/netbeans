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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
