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
package org.netbeans.modules.mercurial.ui.diff;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.util.common.FileTreeView;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ondrej Vrabec
 */
class DiffFileTreeImpl extends FileTreeView<DiffNode> {
    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; //NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; //NOI18N
    private final static String PATH_SEPARATOR_REGEXP = File.separator.replace("\\", "\\\\"); //NOI18N
    
    /**
     * Defines labels for Diff view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);
    private static Image FOLDER_ICON;
    private final MultiDiffPanel master;

    {
        ResourceBundle loc = NbBundle.getBundle(DiffFileTreeImpl.class);
        columnLabels.put(DiffNode.COLUMN_NAME_STATUS, new String [] { 
                loc.getString("CTL_DiffTable_Column_Status_Title"), 
                loc.getString("CTL_DiffTable_Column_Status_Desc")});
        columnLabels.put(DiffNode.COLUMN_NAME_LOCATION, new String [] { 
                loc.getString("CTL_DiffTable_Column_Location_Title"), 
                loc.getString("CTL_DiffTable_Column_Location_Desc")});
    }

    public DiffFileTreeImpl (MultiDiffPanel master) {
        super();
        this.master = master;
        setupColumns();
    }
    
    @SuppressWarnings("unchecked")
    private void setupColumns() {
        view.setPropertyColumns(DiffNode.COLUMN_NAME_STATUS, columnLabels.get(DiffNode.COLUMN_NAME_STATUS)[0],
                DiffNode.COLUMN_NAME_LOCATION, columnLabels.get(DiffNode.COLUMN_NAME_LOCATION)[0]);
        view.setPropertyColumnDescription(DiffNode.COLUMN_NAME_STATUS, columnLabels.get(DiffNode.COLUMN_NAME_STATUS)[1]);
        view.setPropertyColumnDescription(DiffNode.COLUMN_NAME_LOCATION, columnLabels.get(DiffNode.COLUMN_NAME_LOCATION)[1]);
        view.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffFileTreeImpl.class, "ACSN_DiffTable")); // NOI18N
        view.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffFileTreeImpl.class, "ACSD_DiffTable")); // NOI18N
        view.getOutline().setRenderDataProvider(createRenderProvider());
    }
    
    @Override
    protected DiffNode convertToAcceptedNode (Node node) {
        return node instanceof DiffNode ? (DiffNode) node : null;
    }

    private RenderDataProvider createRenderProvider () {
        return new AbstractRenderDataProvider() {
            @Override
            protected String annotateName (DiffNode node, String originalLabel) {
                if (HgModuleConfig.getDefault().isExcludedFromCommit(node.getSetup().getBaseFile().getAbsolutePath())) {
                    originalLabel = "<s>" + (originalLabel == null ? node.getName() : originalLabel) + "</s>"; //NOI18N
                }
                return originalLabel;
            }
        };
    }

    @Override
    protected void nodeSelected (DiffNode node) {
        master.nodeSelected(node);
    }

    @Override
    protected JPopupMenu getPopup () {
        return master.getPopup();
    }
    
    @Override
    protected void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int width = view.getWidth();
                view.getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 40 / 100);
                view.getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 20 / 100);
                view.getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 40 / 100);
            }
        });
    }
    
}
