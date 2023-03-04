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
