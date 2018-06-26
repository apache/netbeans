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

package org.netbeans.modules.php.project.connections.ui.transfer.tree;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChangeSupport;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooser;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooserPanel.TransferFilesChangeListener;
import org.openide.nodes.Node;

final class TransferSelectorModel {

    private final TransferFilesChooser.TransferType transferType;
    private final Set<TransferFile> transferFiles;
    private final Set<TransferFile> selected = Collections.synchronizedSet(new HashSet<TransferFile>());
    private final TransferFilesChangeSupport filesChangeSupport = new TransferFilesChangeSupport(this);
    private final long timestamp;


    public TransferSelectorModel(TransferFilesChooser.TransferType transferType, Set<TransferFile> transferFiles, long timestamp) {
        assert transferType != null;
        assert transferFiles != null;

        this.transferType = transferType;
        this.transferFiles = Collections.synchronizedSet(copyNoProjectRoot(transferFiles));
        this.timestamp = timestamp;


        for (TransferFile file : this.transferFiles) {
            preselect(file);
        }
    }

    private boolean preselect(TransferFile file) {
        boolean select = timestamp == -1;
        if (timestamp != -1) {
            // we have some timestamp
            select = file.getTimestamp() > timestamp;
        }
        if (select) {
            // intentionally not addChildren()!
            selected.add(file);
        }
        return select;
    }

    public void addChangeListener(TransferFilesChangeListener listener) {
        filesChangeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(TransferFilesChangeListener listener) {
        filesChangeSupport.removeChangeListener(listener);
    }

    public boolean isNodeSelected(Node node) {
        TransferFile transferFile = getTransferFile(node);
        if (transferFile == null) {
            // not known yet
            return false;
        }
        return selected.contains(transferFile);
    }

    public boolean isNodePartiallySelected(Node node) {
        TransferFile transferFile = getTransferFile(node);
        if (transferFile == null) {
            // not known yet
            return false;
        }
        if (transferFile.isFile()
                || !selected.contains(transferFile)) {
            return false;
        }
        return !hasAllChildrenSelected(transferFile);
    }

    public void setNodeSelected(Node node, boolean select) {
        TransferFile transferFile = getTransferFile(node);
        if (transferFile == null) {
            // dblclick on root node or not known yet
            return;
        }
        if (select) {
            addChildren(transferFile);
            addParents(transferFile);
        } else {
            removeChildren(transferFile);
        }
        filesChangeSupport.fireSelectedFilesChange();
    }

    public void addNode(Node node) {
        TransferFile transferFile = getTransferFile(node);
        if (transferFile == null) {
            // dblclick on root node or not known yet
            return;
        }
        transferFiles.add(transferFile);
        boolean selectNode = false;
        if (!RemoteUtils.allFilesFetched(transferType == TransferFilesChooser.TransferType.DOWNLOAD)) {
            // lazy children
            selectNode = selected.contains(transferFile.getParent());
        }
        if (selectNode
                || preselect(transferFile)) {
            setNodeSelected(node, true);
        }
    }

    public void selectAll() {
        selected.addAll(transferFiles);
        filesChangeSupport.fireSelectedFilesChange();
    }

    public void unselectAll() {
        selected.clear();
        filesChangeSupport.fireSelectedFilesChange();
    }

    public boolean isAllSelected() {
        return selected.size() == transferFiles.size();
    }

    public Set<TransferFile> getData() {
        return transferFiles;
    }

    public Set<TransferFile> getSelected() {
        return new HashSet<>(selected);
    }

    public int getSelectedSize() {
        return selected.size();
    }

    /**
     * Get {@link TransferFile} or {@code null} if the transfer file is nor known yet.
     * @param node node to get {@link TransferFile} for
     * @return {@link TransferFile} or {@code null} if the transfer file is nor known yet
     */
    private TransferFile getTransferFile(Node node) {
        return node.getLookup().lookup(TransferFile.class);
    }

    private void addChildren(TransferFile file) {
        if (file.isProjectRoot()) {
            // ignored
            return;
        }
        selected.add(file);
        if (hasChildrenFetched(file)) {
            for (TransferFile child : getChildren(file)) {
                addChildren(child);
            }
        }
    }

    private void addParents(TransferFile fromFile) {
        TransferFile parent = fromFile.getParent();
        if (parent != null) {
            if (parent.isProjectRoot()) {
                // ignored
                return;
            }
            selected.add(parent);
            addParents(parent);
        }
    }

    private void removeChildren(TransferFile file) {
        selected.remove(file);
        if (hasChildrenFetched(file)) {
            for (TransferFile child : getChildren(file)) {
                removeChildren(child);
            }
        }
    }

    private boolean hasAllChildrenSelected(TransferFile transferFile) {
        if (!selected.contains(transferFile)) {
            return false;
        }
        if (hasChildrenFetched(transferFile)) {
            for (TransferFile child : getChildren(transferFile)) {
                if (!hasAllChildrenSelected(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Set<TransferFile> copyNoProjectRoot(Set<TransferFile> transferFiles) {
        Set<TransferFile> files = new HashSet<>();
        for (TransferFile file : transferFiles) {
            if (!file.isProjectRoot()) {
                files.add(file);
            }
        }
        return files;
    }

    private boolean hasChildrenFetched(TransferFile transferFile) {
        switch (transferType) {
            case DOWNLOAD:
                return transferFile.hasRemoteChildrenFetched();
            case UPLOAD:
                return transferFile.hasLocalChildrenFetched();
            default:
                throw new IllegalStateException("Unknown transfer type: " + transferType);
        }
    }

    private List<TransferFile> getChildren(TransferFile transferFile) {
        switch (transferType) {
            case DOWNLOAD:
                return transferFile.getRemoteChildren();
            case UPLOAD:
                return transferFile.getLocalChildren();
            default:
                throw new IllegalStateException("Unknown transfer type: " + transferType);
        }
    }

}
