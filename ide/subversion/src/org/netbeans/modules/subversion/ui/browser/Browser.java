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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Handles the UI for repository browsing.
 *
 * @author Tomas Stupka
 */
public final class Browser implements VetoableChangeListener, BrowserClient, TreeExpansionListener {

    public static final int BROWSER_SHOW_FILES                  = 1;
    public static final int BROWSER_SINGLE_SELECTION_ONLY       = 2;
    public static final int BROWSER_FILES_SELECTION_ONLY        = 4;
    public static final int BROWSER_FOLDERS_SELECTION_ONLY      = 8;
    public static final int BROWSER_SELECT_ANYTHING = BROWSER_FOLDERS_SELECTION_ONLY | BROWSER_FILES_SELECTION_ONLY;

    public static final String BROWSER_HELP_ID_SEARCH_HISTORY   = "org.netbeans.modules.subversion.ui.browser.searchhistory"; // NOI18N
    public static final String BROWSER_HELP_ID_CHECKOUT         = "org.netbeans.modules.subversion.ui.browser.checkout";      // NOI18N
    public static final String BROWSER_HELP_ID_URL_PATTERN      = "org.netbeans.modules.subversion.ui.browser.urlpattern";    // NOI18N
    public static final String BROWSER_HELP_ID_MERGE            = "org.netbeans.modules.subversion.ui.browser.merge";         // NOI18N
    public static final String BROWSER_HELP_ID_MERGE_TAG        = "org.netbeans.modules.subversion.ui.browser.mergetag";      // NOI18N
    public static final String BROWSER_HELP_ID_SWITCH_TO        = "org.netbeans.modules.subversion.ui.browser.switchto";      // NOI18N
    public static final String BROWSER_HELP_ID_COPY             = "org.netbeans.modules.subversion.ui.browser.copy";          // NOI18N
    public static final String BROWSER_HELP_ID_IMPORT           = "org.netbeans.modules.subversion.ui.browser.import";        // NOI18N
    public static final String BROWSER_HELP_ID_SELECT_DIFF_TREE = "org.netbeans.modules.subversion.ui.browser.selectdifftree"; // NOI18N

    private final int mode;

    private final String helpID;

    private final String username;
    private final char[] password;

    private static final RepositoryFile[] EMPTY_ROOT = new RepositoryFile[0];
    private static final Action[] EMPTY_ACTIONS = new Action[0];

    private final BrowserPanel panel;

    private RepositoryFile repositoryRoot;
    private Action[] nodeActions;

    private boolean keepWarning = false;
    private boolean initialSelection = true;

    private final List<SvnProgressSupport> supportList = new ArrayList<SvnProgressSupport>();
    private volatile boolean cancelled = false;
    private Node[] selectedNodes;
    /**
     * Creates a new instance
     *
     * @param title the browsers window title
     * @param showFiles
     * @param singleSelectionOnly
     * @param fileSelectionOnly
     * @param repositoryRoot the RepositoryFile representing the repository root
     * @param select an array of RepositoryFile-s representing the items which has to be selected
     * @param nodeActions an array of actions from which the context menu on the tree items will be created
     *
     */
    public Browser(String title,
                   int mode,
                   RepositoryFile repositoryRoot,
                   RepositoryFile[] select,
                   BrowserAction[] nodeActions,
                   String helpID) {
        this(title, mode, repositoryRoot, select, null, null, nodeActions, helpID);
    }

    public Browser(String title,
                   int mode,
                   RepositoryFile repositoryRoot,
                   RepositoryFile[] select,
                   String username,
                   char[] password,
                   BrowserAction[] nodeActions,
                   String helpID) {
        this.mode = mode;
        this.helpID = helpID;

        /*
         * This should ensure that either both username and password are null,
         * or both are non-null:
         */
        this.username = username;
        this.password = (username == null) ? null : password;

        panel = new BrowserPanel(title,
                                 org.openide.util.NbBundle.getMessage(Browser.class, "ACSN_RepositoryTree"),                                            // NOI18N
                                 org.openide.util.NbBundle.getMessage(Browser.class, "ACSD_RepositoryTree"),                                            // NOI18N
                                 (mode & BROWSER_SINGLE_SELECTION_ONLY) == BROWSER_SINGLE_SELECTION_ONLY);

        panel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "CTL_Browser_Prompt"));    // NOI18N
        panel.addTreeExpansionListener(this);
        getExplorerManager().addVetoableChangeListener(this);

        if(nodeActions!=null) {
            this.nodeActions = nodeActions;
            panel.setActions(nodeActions);
            for (BrowserAction nodeAction : nodeActions) {
                nodeAction.setBrowser(this);
            }
        } else {
            this.nodeActions = EMPTY_ACTIONS;
        }
        this.repositoryRoot = repositoryRoot;

        RepositoryPathNode rootNode = RepositoryPathNode.createRepositoryPathNode(this, repositoryRoot);

        Node[] selected = getSelectedNodes(rootNode, repositoryRoot, select);
        getExplorerManager().setRootContext(rootNode);
        panel.expandNode((RepositoryPathNode) rootNode);

        if(selected == null) {
            selected = new Node[] { rootNode }; // allways expand the root node
        }
        if(selected.length > 0) {
            for (Node selectedNode : selected) {
                panel.expandNode((RepositoryPathNode) selectedNode);
            }
            try {
                getExplorerManager().setSelectedNodes(selected);
            } catch (PropertyVetoException ex) {
                // not interested
            }
        }
        if (select == null || select.length == 0) { // careful, tomas, select may be null
            rootNode.expand(); // hack - calling panel.expandNode(...) for the root node doesn't seem to work
        }
    }

    public RepositoryFile[] getRepositoryFiles() {
        if(!show()) {
            cancel();
            return EMPTY_ROOT;
        }

        // get the nodes first
        Node[] nodes = selectedNodes;

        // clean up - even if the dialog was closed, we always cancel all running tasks
        cancel();

        if(nodes == null || nodes.length == 0) {
            return EMPTY_ROOT;
        }

        List<RepositoryFile> ret = new ArrayList<RepositoryFile>(nodes.length);
        for (Node node : nodes) {
            if (node instanceof RepositoryPathNode) {
                ret.add(((RepositoryPathNode) node).getEntry().getRepositoryFile());
            }
        }
        return ret.toArray(new RepositoryFile[0]);
    }

    private boolean show() {
        final DialogDescriptor dialogDescriptor =
                new DialogDescriptor(getBrowserPanel(), NbBundle.getMessage(Browser.class, "CTL_Browser_BrowseFolders_Title")); // NOI18N
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(helpID));
        dialogDescriptor.setValid(false);

        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()) ) {
                    Node[] nodes = getSelectedNodes();
                    if (nodes != null && nodes.length > 0) {
                        selectedNodes = nodes;
                        dialogDescriptor.setValid(nodes.length > 0);
                    }
                }
            }
        });

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Browser.class, "CTL_Browser_BrowseFolders_Title")); // NOI18N
        dialog.setVisible(true);

        return DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue());
    }

    private Node[] getSelectedNodes(RepositoryPathNode rootNode, RepositoryFile repositoryRoot, RepositoryFile[] select) {
        if(select==null || select.length <= 0) {
            return null;
        }
        Node segmentParentNode;
        List<Node> nodesToSelect = new ArrayList<Node>(select.length);
        for (RepositoryFile select1 : select) {
            String[] segments = select1.getPathSegments();
            segmentParentNode = rootNode;
            RepositoryFile segmentFile = repositoryRoot;
            for (int j = 0; j < segments.length; j++) {
                segmentFile = segmentFile.appendPath(segments[j]);
                RepositoryPathNode segmentNode = j == segments.length - 1 ?
                        RepositoryPathNode.createRepositoryPathNode(this, segmentFile) :
                        RepositoryPathNode.createPreselectedPathNode(this, segmentFile);
                segmentParentNode.getChildren().add(new Node[] {segmentNode});
                segmentParentNode = segmentNode;
            }
            nodesToSelect.add(segmentParentNode);
        }
        return nodesToSelect.toArray(new Node[0]);
    }

    /**
     * Cancels all running tasks
     */
    private void cancel() {
        SvnProgressSupport[] progressSupports;
        synchronized(supportList) {
            cancelled = true;
            progressSupports = supportList.toArray(new SvnProgressSupport[0]);
            supportList.clear();
        }

        Node rootNode = getExplorerManager().getRootContext();
        if(rootNode != null) {
            getExplorerManager().setRootContext(Node.EMPTY);
            try {
                rootNode.destroy();

                if(progressSupports != null && progressSupports.length > 0) {
                    for(SvnProgressSupport sps : progressSupports) {
                        sps.cancel();
                    }
                }
            } catch (IOException ex) {
                Subversion.LOG.log(Level.INFO, null, ex); // should not happen
            }
        }
    }

    @Override
    public List<RepositoryPathNode.RepositoryPathEntry> listRepositoryPath(final RepositoryPathNode.RepositoryPathEntry entry, SvnProgressSupport support) throws SVNClientException {

        List<RepositoryPathNode.RepositoryPathEntry> ret = new ArrayList<RepositoryPathNode.RepositoryPathEntry>();

        synchronized (supportList) {
            if(cancelled) {
                support.cancel();
                return ret;
            }
            supportList.add(support);
        }

        try {

            if(entry.getSvnNodeKind().equals(SVNNodeKind.FILE)) {
                return ret; // nothing to do...
            }

            Subversion subversion = Subversion.getInstance();
            SVNUrl svnUrl = this.repositoryRoot.getRepositoryUrl();
            SvnClient client = (username != null)
                               ? subversion.getClient(svnUrl, username, password, support)
                               : subversion.getClient(svnUrl, support);
            if(support.isCanceled()) {
                return null;
            }

            ISVNDirEntry[] dirEntries = client.getList(
                                            entry.getRepositoryFile().getFileUrl(),
                                            entry.getRepositoryFile().getRevision(),
                                            false
                                        );

            if(dirEntries == null || dirEntries.length == 0) {
                return ret; // nothing to do...
            }
            for (ISVNDirEntry dirEntry : dirEntries) {
                if(support.isCanceled()) {
                    return null;
                }
                if( dirEntry.getNodeKind()==SVNNodeKind.DIR ||                  // directory or
                        (dirEntry.getNodeKind()==SVNNodeKind.FILE &&                // (file and show_files_allowed)
                        ((mode & BROWSER_SHOW_FILES) == BROWSER_SHOW_FILES)) )
                {
                    RepositoryFile repositoryFile = new RepositoryFile(
                            entry.getRepositoryFile().getRepositoryUrl(),
                            entry.getRepositoryFile().getFileUrl().appendPath(dirEntry.getPath()),
                            dirEntry.getLastChangedRevision());
                    RepositoryPathNode.RepositoryPathEntry e =
                            new RepositoryPathNode.RepositoryPathEntry(
                            repositoryFile,
                            dirEntry.getNodeKind(),
                            dirEntry.getLastChangedRevision(),
                            dirEntry.getLastChangedDate(),
                            dirEntry.getLastCommitAuthor());
                    ret.add(e);
                }
            }

        } catch (SVNClientException ex) {
            if(SvnClientExceptionHandler.isWrongURLInRevision(ex.getMessage())) {
                // is not a folder in the repository
                return null;
            } else {
                support.annotate(ex);
                throw ex;
            }
        }
        finally {
            synchronized (supportList) {
                supportList.remove(support);
            }
        }

        return ret;
    }

    private JPanel getBrowserPanel() {
        return panel;
    }

    public Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {

            boolean initialSelectionDone = !initialSelection;
            initialSelection = false;

            if(!keepWarning) {
                panel.warning(null);
            }
            keepWarning = false;

            Node[] newSelection = (Node[]) evt.getNewValue();
            Node[] oldSelection = (Node[]) evt.getOldValue();
            if(newSelection == null || newSelection.length == 0) {
                return;
            }

            // applies if file selection only
            if((mode & BROWSER_FILES_SELECTION_ONLY) == BROWSER_FILES_SELECTION_ONLY) {
                if(checkForNodeType(newSelection, SVNNodeKind.DIR))  {
                    panel.warning(org.openide.util.NbBundle.getMessage(Browser.class, "LBL_Warning_FileSelectionOnly"));        // NOI18N
                    if(initialSelectionDone) keepWarning = true;
                    throw new PropertyVetoException("", evt);                                                                   // NOI18N
                }
            }

            // applies if folder selection only
            if((mode & BROWSER_FOLDERS_SELECTION_ONLY) == BROWSER_FOLDERS_SELECTION_ONLY) {
                if(checkForNodeType(newSelection, SVNNodeKind.FILE)) {
                    panel.warning(org.openide.util.NbBundle.getMessage(Browser.class, "LBL_Warning_FolderSelectionOnly"));      // NOI18N
                    if(initialSelectionDone) keepWarning = true;
                    throw new PropertyVetoException("", evt);                                                                   // NOI18N
                }
            }

            // RULE: don't select nodes on a different level as the already selected
            if(oldSelection.length == 0 && newSelection.length == 1) {
                // it is first node selected ->
                // -> there is nothig to check
                return;
            }

            if(oldSelection.length != 0 && areDisjunct(oldSelection, newSelection)) {
                // as if the first node would be selected ->
                // -> there is nothig to check
                return;
            }

            Node selectedNode;
            if(oldSelection.length > 0) {
                // we anticipate that nothing went wrong and
                // all nodes in the old selection are at the same level
                selectedNode = oldSelection[0];
            } else {
                selectedNode = newSelection[0];
            }
            if(!selectionIsAtLevel(newSelection, getNodeLevel(selectedNode))) {
                panel.warning(org.openide.util.NbBundle.getMessage(Browser.class, "LBL_Warning_NoMultiSelection"));     // NOI18N
                if(initialSelectionDone) keepWarning = true;
                throw new PropertyVetoException("", evt);                                                               // NOI18N
            }
        }
    }

    private boolean checkForNodeType(Node[] newSelection, SVNNodeKind nodeKind) {
        for (Node newSelection1 : newSelection) {
            if (newSelection1 instanceof RepositoryPathNode) {
                RepositoryPathNode node = (RepositoryPathNode) newSelection1;
                if(node.getEntry().getSvnNodeKind() == nodeKind) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean selectionIsAtLevel(Node[] newSelection, int level) {
        for (Node newSelection1 : newSelection) {
            if (getNodeLevel(newSelection1) != level) {
                return false;
            }
        }
        return true;
    }

    private boolean areDisjunct(Node[] oldSelection, Node[] newSelection) {
        for (Node oldSelection1 : oldSelection) {
            if (isInArray(oldSelection1, newSelection)) {
                return false;
            }
        }
        return true;
    }

    private int getNodeLevel(Node node) {
        int level = 0;
        while(node!=null) {
            node = node.getParentNode();
            level++;
        }
        return level;
    }

    private boolean isInArray(Node node, Node[] nodeArray) {
        return Arrays.asList(nodeArray).contains(node);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getExplorerManager().addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getExplorerManager().removePropertyChangeListener(listener);
    }

    ExplorerManager getExplorerManager() {
        return panel.getExplorerManager();
    }

    @Override
    public Action[] getActions() {
        return nodeActions;
    }

    void setSelectedNodes(Node[] selection) throws PropertyVetoException {
        getExplorerManager().setSelectedNodes(selection);
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        Object obj = event.getPath().getLastPathComponent();
        if(obj == null) return;
        Node n = Visualizer.findNode(obj);
        if(n instanceof RepositoryPathNode) {
            RepositoryPathNode node = (RepositoryPathNode) n;
            node.expand();
        }
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        // do nothing
    }

}
