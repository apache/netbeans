/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.cdnjs.ui;

import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import javax.swing.JPanel;
import org.netbeans.modules.javascript.cdnjs.Library;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.util.NbBundle;

/**
 * Panel for selection of library files.
 *
 * @author Jan Stola
 */
public class FileSelectionPanel extends JPanel implements ExplorerManager.Provider {
    /** Explorer manager provided by this panel. */
    private final ExplorerManager manager = new ExplorerManager();
    /** Table of the available files. */
    private OutlineView outlineView;

    /**
     * Creates a new {@code FileSelectionPanel}.
     */
    public FileSelectionPanel() {
        setLayout(new BorderLayout());
        initOutlineView();
        add(outlineView);
    }

    /**
     * Initializes the table of the available files.
     */
    @NbBundle.Messages({
        "FileSelectionPanel.fileColumn.title=File",
        "FileSelectionPanel.installColumn.title=Install"
    })
    private void initOutlineView() {
        outlineView = new OutlineView(Bundle.FileSelectionPanel_fileColumn_title());
        outlineView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        outlineView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        outlineView.setShowNodeIcons(false);
        outlineView.addPropertyColumn(
                FileNode.InstallProperty.NAME,
                Bundle.FileSelectionPanel_installColumn_title());

        Outline outline = outlineView.getOutline();
        outline.setRootVisible(false);        
    }

    /**
     * Sets the library whose files should be selected.
     * 
     * @param version library version whose files should be selected.
     * @param installedFiles files that are installed.
     */
    public void setLibrary(Library.Version version, String[] installedFiles) {
        manager.setRootContext(new FilesNode(version, installedFiles));
    }

    /**
     * Returns version of the library whose files reflect the selection made
     * by the user.
     * 
     * @return version of the library whose files reflect the selection made
     * by the user.
     */
    public Library.Version getSelection() {
        FilesNode filesNode = (FilesNode)manager.getRootContext();
        return filesNode.getSelection();
    }

    /**
     * Returns the explorer manager used by this panel.
     * 
     * @return explorer manager used by this panel.
     */
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
}
