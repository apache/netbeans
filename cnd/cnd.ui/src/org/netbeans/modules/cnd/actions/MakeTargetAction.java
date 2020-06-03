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

package org.netbeans.modules.cnd.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.builds.MakefileTargetProvider;
import org.netbeans.modules.cnd.builds.TargetEditor;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/**
 * Implements Make target Action
 */
public class MakeTargetAction extends MakeBaseAction {

    /* target mnemonics for the first 10 targets */
    private static final String mnemonics = "1234567890"; // NOI18N

    @Override
    public String getName () {
        return getString("BTN_Target");	// NOI18N
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(MakeTargetAction.class); // FIXUP ???
    }

    @Override
    public JMenuItem getPopupPresenter() {
        Node[] activeNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        return new TargetPopupMenu(this, enable(activeNodes), activeNodes);
    }

    private class TargetPopupMenu extends JMenu {
        private boolean initialized = false;
        private SystemAction action = null;
        private final Node[] activeNodes;

        public TargetPopupMenu(SystemAction action, boolean en, Node[] activeNodes) {
            super();
            this.action = action;
            this.activeNodes = activeNodes;
            setEnabled(en);
            setText(action.getName());
        }

        @Override
        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
            if (!initialized) {
                if (activeNodes == null || activeNodes.length != 1) {
                    return popup;
                }

                Node activeNode = activeNodes[0];

                MakefileTargetProvider targetProvider = activeNode.getLookup().lookup(MakefileTargetProvider.class);
                if (targetProvider != null) {
                    try {
                        DataObject dao = activeNode.getLookup().lookup(DataObject.class);
                        if (dao != null) {
                            FileObject fo = dao.getPrimaryFile();
                            // checking mime type, see bug 224915
                            if (fo != null && fo.isValid() && MIMENames.MAKEFILE_MIME_TYPE.equals(fo.getMIMEType())) {
                                List<String> targets = new ArrayList<String>(targetProvider.getPreferredTargets());
                                Collections.sort(targets);
                                for (String target : targets) {
                                    popup.add(new PopupItemTarget(activeNode, target, -1));
                                }
                                if (!targets.isEmpty()) {
                                    popup.add(new JSeparator());
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                MakeExecSupport mes = activeNode.getLookup().lookup(MakeExecSupport.class);
                if (mes != null) {
                    String[] targets = mes.getMakeTargetsArray();

                    //popup.add(new PopupItemDefaultTarget(activedNode, getString("DEFAULT_TARGET"))); // NOI18N
                    //if (targets.length > 0)
                    //popup.add(new JSeparator());
                    for (int i = 0; i < targets.length; i++) {
                        popup.add(new PopupItemTarget(activeNode, targets[i], -1));
                    }
                    if (targets.length > 0) {
                        popup.add(new JSeparator());
                    }
                    popup.add(new PopupItemAddTarget(activeNode));
                }
                initialized = true;
            }
            return popup;
        }

    }

    /**
     * Compose new name with a mnemonic: <targetname>  (<mnemonic>)
     * Compose new name with a mnemonic: <mnemonic> <targetname>
     */
    private String nameWithMnemonic(String name, int mne) {
        if (mne >= 0 && mne < mnemonics.length()) {
            return "" + mnemonics.charAt(mne) + "  " + name; // NOI18N
        //return name + "  (" + mnemonics.charAt(mne) + ")"; // NOI18N
        } else {
            return name; // no mnemonic
        }
    }

    private class PopupItemTarget extends JMenuItem implements ActionListener {

        private final Node node;
        private final String target;

        public PopupItemTarget(Node activeNode, String name, int mne) {
            //super(nameWithMnemonic(name, mne), new ImageIcon(Utilities.loadImage("org/netbeans/modules/cnd/resources/blank.gif", true)));
            super(nameWithMnemonic(name, mne));
            node = activeNode;
            target = name;
            addActionListener(this);
            if (mne >= 0 && mne < mnemonics.length()) {
                setMnemonic(mnemonics.charAt(mne));
            }
        }

        /** Invoked when an action occurs.
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            performAction(node, target);
        }
    }

    private static class PopupItemAddTarget extends JMenuItem implements ActionListener {

        private final Node node;

        public PopupItemAddTarget(Node activeNode) {
            //super(getString("ADD_NEW_TARGET"), new ImageIcon(Utilities.loadImage("org/netbeans/modules/cnd/resources/AddMakeTargetAction.gif", true))); // NOI18N
            super(getString("ADD_NEW_TARGET")); // NOI18N
            node = activeNode;
            addActionListener(this);
            setMnemonic(getString("ADD_NEW_TARGET_MNEMONIC").charAt(0));
        }

        /** Invoked when an action occurs. */
        @Override
        public void actionPerformed(ActionEvent e) {
            MakeExecSupport mes = node.getLookup().lookup(MakeExecSupport.class);
            if (mes != null) {
                TargetEditor targetEditor = new TargetEditor(mes.getMakeTargetsArray(), null, null);
                int ret = targetEditor.showOpenDialog((JFrame) WindowManager.getDefault().getMainWindow());
                if (ret == TargetEditor.OK_OPTION) {
                    mes.setMakeTargets(targetEditor.getTargets());
                }
            }
        }
    }
}
