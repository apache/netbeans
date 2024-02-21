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

package org.netbeans.modules.diff;

import java.io.IOException;
import java.io.File;
import java.util.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.api.diff.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.diff.builtin.DefaultDiff;
import org.netbeans.modules.diff.builtin.SingleDiffPanel;
import org.netbeans.modules.diff.options.AccessibleJFileChooser;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;

/**
 * Diff Action. It gets the default diff visualizer and diff provider if needed
 * and display the diff visual representation of two files selected in the IDE.
 *
 * @author  Martin Entlicher
 */
public class DiffAction extends NodeAction {

    /** Creates new DiffAction */
    public DiffAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    private static boolean diffAvailable = true;
    
    private class DiffActionImpl extends AbstractAction {
        
        private final Node [] nodes;

        private DiffActionImpl(Lookup context) {
            Collection<? extends Node> nodez = context.lookup(new Lookup.Template<Node>(Node.class)).allInstances();
            nodes = nodez.toArray(new Node[0]);
            if (nodes.length == 1) {
                putValue(Action.NAME, NbBundle.getMessage(DiffAction.class, "CTL_DiffToActionName"));
            } else {
                putValue(Action.NAME, getName());                
            }
        }

        public void actionPerformed(ActionEvent e) {
            performAction(nodes);
        }

        public boolean isEnabled() {
            return DiffAction.this.enable(nodes);
        }
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DiffActionImpl(actionContext);
    }

    public String getName() {
        return NbBundle.getMessage(DiffAction.class, "CTL_DiffActionName");
    }
    
    static FileObject getFileFromNode(Node node) {
        FileObject fo = (FileObject) node.getLookup().lookup(FileObject.class);
        if (fo == null) {
            Project p = (Project) node.getLookup().lookup(Project.class);
            if (p != null) return p.getProjectDirectory();

            DataObject dobj = (DataObject) node.getCookie(DataObject.class);
            if (dobj instanceof DataShadow) {
                dobj = ((DataShadow) dobj).getOriginal();
            }
            if (dobj != null) {
                fo = dobj.getPrimaryFile();
            }
        }
        return fo;
    }
    
    public boolean enable(Node[] nodes) {
        //System.out.println("DiffAction.enable() = "+(nodes.length == 2));
        if (!diffAvailable) {
            return false;
        }
        if (nodes.length == 2) {
            FileObject fo1 = getFileFromNode(nodes[0]);
            FileObject fo2 = getFileFromNode(nodes[1]);
            if (fo1 != null && fo2 != null) {
                if (fo1.isData() && fo2.isData()) {
                    return true;
                }
            }
        } else if (nodes.length == 1) {
            FileObject fo1 = getFileFromNode(nodes[0]);
            if (fo1 != null) {
                if (fo1.isData()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * This action should not be run in AWT thread, because it opens streams
     * to files.
     * @return true not to run in AWT thread!
     */
    protected boolean asynchronous() {
        return false;
    }
    
    public void performAction(Node[] nodes) {
        ArrayList<FileObject> fos = new ArrayList<FileObject>();
        for (int i = 0; i < nodes.length; i++) {
            FileObject fo = getFileFromNode(nodes[i]);
            if (fo != null) {
                fos.add(fo);
            }
        }
        if (fos.size() < 1) return ;
        final FileObject fo1 = fos.get(0);
        final FileObject fo2;
        if (fos.size() > 1) {
            fo2 = fos.get(1);
        } else {
            fo2 = promptForFileobject(fo1);
            if (fo2 == null) return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                performAction(fo1, fo2, null);
            }
        });
    }

    private FileObject promptForFileobject(FileObject peer) {
        String path = DiffModuleConfig.getDefault().getPreferences().get("diffToLatestFolder", peer.getParent().getPath());
        File latestPath = FileUtil.normalizeFile(new File(path));
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(DiffAction.class, "ACSD_BrowseDiffToFile"), latestPath); // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(DiffAction.class, "DiffTo_BrowseFile_Title", peer.getName())); // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        EditorBufferSelectorPanel editorSelector = new EditorBufferSelectorPanel(fileChooser, peer);
        fileChooser.setAccessory(editorSelector);

        int result = fileChooser.showDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(DiffAction.class, "DiffTo_BrowseFile_OK")); // NOI18N
        if (result != JFileChooser.APPROVE_OPTION) return null;

        FileObject userSelectedFo = editorSelector.getSelectedEditorFile();
        if (userSelectedFo != null) {
            return userSelectedFo;
        }
        
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            File file = f.getAbsoluteFile();
            DiffModuleConfig.getDefault().getPreferences().put("diffToLatestFolder", file.getParent());
            return FileUtil.toFileObject(f);
        }
        return null;
    }
    
    /**
     * Shows the diff between two FileObject objects.
     * This is expected not to be called in AWT thread.
     * @param type Use the type of that FileObject to load both files.
     */
    static void performAction(FileObject fo1, FileObject fo2, FileObject type) {
        //System.out.println("performAction("+fo1+", "+fo2+")");
        //doDiff(fo1, fo2);
        Diff diff = Diff.getDefault();
        //System.out.println("dv = "+dv);
        if (diff == null) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(DiffAction.class,
                    "MSG_NoDiffVisualizer")));
            diffAvailable = false;
            return ;
        }
        SingleDiffPanel sdp = null;
        try {
            final Thread victim = Thread.currentThread();
            Cancellable killer = new Cancellable() {
                public boolean cancel() {
                    victim.interrupt();
                    return true;
                }
            };
            String name = NbBundle.getMessage(DiffAction.class, "BK0001");
            try (ProgressHandle ph = ProgressHandle.createHandle(name, killer)) {
                ph.start();
                sdp = new SingleDiffPanel(fo1, fo2, type);
            }
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return ;
        }
        //System.out.println("tp = "+tp);
        if (sdp != null) {
            final SingleDiffPanel fsdp = sdp;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TopComponent dtc = new DefaultDiff.DiffTopComponent(fsdp) {
                        @Override
                        protected void componentActivated() {
                            super.componentActivated();
                            fsdp.requestActive();
                        }

                        @Override
                        protected void componentClosed() {
                            super.componentClosed(); //To change body of generated methods, choose Tools | Templates.
                            fsdp.closed();
                        }
                        
                        @Override
                        public UndoRedo getUndoRedo() {
                            return fsdp.getUndoRedo();
                        }
                    };
                    fsdp.putClientProperty(TopComponent.class, dtc);
                    fsdp.activateNodes();
                    dtc.open();
                    dtc.requestActive();
                }
            });
        }
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DiffAction.class);
    }

}
