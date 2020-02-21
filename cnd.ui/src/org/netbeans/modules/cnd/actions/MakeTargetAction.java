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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
