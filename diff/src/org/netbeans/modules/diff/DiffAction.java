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
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.diff.builtin.DefaultDiff;
import org.netbeans.modules.diff.builtin.SingleDiffPanel;
import org.netbeans.modules.diff.options.AccessibleJFileChooser;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.lookup.ProxyLookup;

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
            nodes = nodez.toArray(new Node[nodez.size()]);
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
            ProgressHandle ph = ProgressHandleFactory.createHandle(name, killer);
            try {
                ph.start();
                sdp = new SingleDiffPanel(fo1, fo2, type);
            } finally {
                ph.finish();
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
