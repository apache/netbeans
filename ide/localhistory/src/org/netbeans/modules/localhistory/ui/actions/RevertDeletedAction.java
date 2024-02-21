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
package org.netbeans.modules.localhistory.ui.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.localhistory.ui.actions.FileNode.PlainFileNode;
import org.netbeans.modules.localhistory.ui.actions.FileNode.StoreEntryNode;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.localhistory.ui.actions.RevertDeletedAction", category = "History")
@ActionRegistration(lazy = false, displayName = "#CTL_ShowRevertDeleted")
@ActionReference(path = "OptionsDialog/Actions/History", name = "RevertDeletedAction")
public class RevertDeletedAction extends NodeAction {
    
    /** Creates a new instance of ShowLocalHistoryAction */
    public RevertDeletedAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    @Override
    protected void performAction(final Node[] activatedNodes) {
        final RevertPanel p = new RevertPanel();
        p.tree.setCellRenderer(new DeletedListRenderer());
        FileNodeListener l = new FileNodeListener();
        p.tree.addMouseListener(l);
        p.tree.addKeyListener(l);
        
        LocalHistory.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                retrieveDeletedFiles(activatedNodes, p);
            }                                       
        });                
        if(!p.open()) {
            return;
        }
        LocalHistory.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                revert(p.getRootNode());        
            }
        });                
    }

    private void retrieveDeletedFiles(final Node[] activatedNodes, final RevertPanel p) {
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        Set<VCSFileProxy> rootSet = ctx.getRootFiles();        
        if(rootSet == null || rootSet.size() < 1) { 
            return;
        }                                        
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        for (VCSFileProxy root : rootSet) {            
            PlainFileNode rfn = new PlainFileNode(root);
            populateNode(rfn, root, !VersioningSupport.isFlat(root));
            if(rfn.getChildCount() > 0) {
                rootNode.add(rfn);
            }
        }
        if(rootNode.getChildCount() > 0) {
            p.setRootNode(rootNode);
        } else {
            p.setRootNode(null);
        }
    }

    private List<StoreEntryNode> getDeletedEntries(VCSFileProxy file) {
        StoreEntry[] entries = LocalHistory.getInstance().getLocalHistoryStore().getDeletedFiles(file);
        if(entries.length == 0) {
            return new LinkedList<StoreEntryNode>();
                }            
        List<StoreEntryNode> l = new LinkedList<StoreEntryNode>();
        for (StoreEntry e : entries) {
            if(!e.getFile().exists()) { 
                // the files version was created by a delete &&
                // the file wasn't created again.  
                l.add(new StoreEntryNode(e));
            }
        }
        return l;                
    }
    
    private void revert(TreeNode rootNode) {
        List<StoreEntryNode> nodes = getSelectedNodes(rootNode);

        for(StoreEntryNode sen : nodes) {
            revert(sen.getStoreEntry());
        }
    }
    
    private List<StoreEntryNode> getSelectedNodes(TreeNode node) {
        List<StoreEntryNode> ret = new LinkedList<StoreEntryNode>();
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            TreeNode child = node.getChildAt(i);
            if(child instanceof StoreEntryNode) {
                StoreEntryNode sen = (StoreEntryNode) child;
                if(sen.isSelected()) {
                    ret.add(sen);
                }
            }
            ret.addAll(getSelectedNodes(child));
        }
        return ret;
    }
    
    protected boolean enable(Node[] activatedNodes) {     
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        Set<VCSFileProxy> rootSet = ctx.getRootFiles();        
        if(rootSet == null || rootSet.size() < 1) { 
            return false;
        }                        
        for (VCSFileProxy p : rootSet) {            
            if(p != null && !p.isDirectory()) {
                return false;
            }
        }        
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "CTL_ShowRevertDeleted");   // NOI18N      
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RevertDeletedAction.class);
    }

    private static void revert(StoreEntry se) {        
        VCSFileProxy file = se.getFile();
        if(file.exists()) {
            // created externaly?
            if(file.isFile()) {                
                LocalHistory.LOG.log(Level.WARNING, "Skipping revert for file {0} which already exists.", FileUtils.getPath(file));    // NOI18N
            }  
            // fix history
            // XXX create a new entry vs. fixing the entry timestamp and deleted flag?
            LocalHistory.getInstance().getLocalHistoryStore().fileCreate(file, file.lastModified());
        }
        File storeFile = se.getStoreFile();
                
        InputStream is = null;
        OutputStream os = null;
        try {               
            FileObject parentFO = file.getParentFile().toFileObject();             
            if(parentFO != null) {
                if(!storeFile.isFile()) {
                    FileUtil.createFolder(parentFO, file.getName());             
                } else {            
                    FileObject fo = FileUtil.createData(parentFO, file.getName());                

                    os = getOutputStream(fo);     
                    is = se.getStoreFileInputStream();                    
                    FileUtil.copy(is, os);            
                }
            } else {
                VCSFileProxy parentFile = file.getParentFile();
                if(parentFile.toFile() != null) {
                    LocalHistory.LOG.log(Level.WARNING, "FileObject for local file {0} is null.", file.getParentFile().getPath());
                    
                    // fallback on io.File
                    if(!storeFile.isFile()) {
                        file.toFile().mkdirs();
                    } else {            
                        is = se.getStoreFileInputStream();                    
                        FileUtils.copy(is, file.toFile());
                    }
                    
                } else {
                    LocalHistory.LOG.log(Level.WARNING, "FileObject for remote file {0} is null. Can''t revert.", file.getParentFile().getPath());
                }
            }
        } catch (Exception e) {            
            LocalHistory.LOG.log(Level.SEVERE, null, e);
            return;
        } finally {
            try {
                if(os != null) { os.close(); }
                if(is != null) { is.close(); }
            } catch (IOException e) {}
        } 
    }
    
    private static OutputStream getOutputStream(FileObject fo) throws FileAlreadyLockedException, IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return fo.getOutputStream();                
            } catch (IOException ioe) {            
                retry++;
                if (retry > 7) {
                    throw ioe;
                }
                Thread.sleep(retry * 30);
            } 
        }                    
    }
    
    private void populateNode(FileNode node, VCSFileProxy root, boolean recursively) {
        
        List<StoreEntryNode> deletedEntries = getDeletedEntries(root);
        if(!recursively) {
            for (StoreEntryNode sen : deletedEntries) {
                node.add(sen);
            }
            return;
        }
        
        // check all previosly deleted children files if they by chance 
        // also contain something deleted
        for (StoreEntryNode sen : deletedEntries.toArray(new StoreEntryNode[0])) {
            node.add(sen);
            if(!sen.getStoreEntry().representsFile()) {
                populateNode(sen, sen.getStoreEntry().getFile(), true);
            }
        }

        // check all existing children files if they contain anything deleted
        VCSFileProxy[] files = root.listFiles();
        if(files != null) {
            for(VCSFileProxy f : files) {
                if(f.isDirectory()) {
                    PlainFileNode pfn = new PlainFileNode(f);
                    populateNode(pfn, f, true);
                    if(pfn.getChildCount() > 0) {
                        node.add(pfn);
                    }
                }            
            }
        } else {
            LocalHistory.LOG.log(Level.WARNING, "listFiles() for directory {0} returned null", root);
        }
    }
    
    private class FileNodeListener implements MouseListener, KeyListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            JTree tree = (JTree) e.getSource();
            Point p = e.getPoint();
            int row = tree.getRowForLocation(e.getX(), e.getY());
            TreePath path = tree.getPathForRow(row);
            
            // if path exists and mouse is clicked exactly once
            if (path != null) {
                FileNode node = (FileNode) path.getLastPathComponent();
                Rectangle chRect = DeletedListRenderer.getCheckBoxRectangle();
                Rectangle rowRect = tree.getPathBounds(path);
                chRect.setLocation(chRect.x + rowRect.x, chRect.y + rowRect.y);
                if (e.getClickCount() == 1 && chRect.contains(p)) {
                    boolean isSelected = !(node.isSelected());
                    node.setSelected(isSelected);
                    ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                    if (row == 0) {
                        tree.revalidate();
                    }
                    tree.repaint();
                }
            }
        }

        @Override public void keyTyped(KeyEvent e) { }
        @Override public void keyReleased(KeyEvent e) { }
        @Override public void mouseEntered(MouseEvent e) { }
        @Override public void mouseExited(MouseEvent e) { }
        @Override public void mouseReleased(MouseEvent e) { }
        @Override public void mousePressed(MouseEvent event) { }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_SPACE) {
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    FileNode node = (FileNode) path.getLastPathComponent();
                    node.setSelected(!node.isSelected());
                    tree.repaint();
                    e.consume();
                }
            } 
        }
    } // end FileNodeListener    
}
