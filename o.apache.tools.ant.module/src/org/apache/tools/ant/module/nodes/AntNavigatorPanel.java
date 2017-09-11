/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.apache.tools.ant.module.nodes;

import java.awt.BorderLayout;
import java.util.Collection;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.apache.tools.ant.module.loader.AntProjectDataObject;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Displays Ant targets in the Navigator.
 * @author Jesse Glick
 */
@NavigatorPanel.Registration(mimeType=AntProjectDataObject.MIME_TYPE, displayName="#ANP_label")
public final class AntNavigatorPanel implements NavigatorPanel {
    
    private Lookup.Result<DataObject> selection;
    private final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            Mutex.EVENT.readAccess(new Runnable() { // #69355: safest to run in EQ
                public void run() {
                    if (selection != null) { // #111772
                        display(selection.allInstances());
                    }
                }
            });
        }
    };
    private JComponent panel;
    private final ExplorerManager manager = new ExplorerManager();
    private final Lookup lookup = ExplorerUtils.createLookup(manager, new ActionMap());
    
    /**
     * Default constructor for layer instance.
     */
    public AntNavigatorPanel() {}
    
    public String getDisplayName() {
        return NbBundle.getMessage(AntNavigatorPanel.class, "ANP_label");
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(AntNavigatorPanel.class, "ANP_hint");
    }
    
    public JComponent getComponent() {
        if (panel == null) {
            final ListView view = new ListView();
            view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            class Panel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
                // Make sure action context works correctly:
                {
                    setLayout(new BorderLayout());
                    add(view, BorderLayout.CENTER);
                }
                public ExplorerManager getExplorerManager() {
                    return manager;
                }
                @Override
                public boolean requestFocusInWindow() {
                    return view.requestFocusInWindow();
                }
                public Lookup getLookup() {
                    return lookup;
                }
            }
            panel = new Panel();
        }
        return panel;
    }
    
    public void panelActivated(Lookup context) {
        selection = context.lookupResult(DataObject.class);
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    public void panelDeactivated() {
        selection.removeLookupListener(selectionListener);
        selection = null;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    private void display(Collection<? extends DataObject> selectedFiles) {
        // Show list of targets for selected file:
        if (selectedFiles.size() == 1) {
            DataObject d = selectedFiles.iterator().next();
            if (d.isValid()) { // #145571
                manager.setRootContext(d.getNodeDelegate());
                return;
            }
        }
        // Fallback:
        manager.setRootContext(Node.EMPTY);
    }
    
}
