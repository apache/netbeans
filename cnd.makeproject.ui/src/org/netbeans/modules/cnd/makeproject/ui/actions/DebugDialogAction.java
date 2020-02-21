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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.launchers.Launcher;
import org.netbeans.modules.cnd.makeproject.api.launchers.LaunchersRegistry;
import org.netbeans.modules.cnd.makeproject.api.launchers.LaunchersRegistryFactory;
import org.netbeans.modules.cnd.makeproject.ui.launchers.actions.LauncherAction;
import org.netbeans.modules.cnd.makeproject.ui.launchers.actions.LauncherAction.LauncherExecutableAction;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;

public class DebugDialogAction extends NodeAction {

    protected JButton debugButton = null;
    private Object options[];

    private void init() {
        if (debugButton == null) {
            debugButton = new JButton(getString("DebugButtonText")); // NOI18N
            debugButton.getAccessibleContext().setAccessibleDescription(getString("DebugButtonAD"));
            options = new Object[]{
                        debugButton,
                        DialogDescriptor.CANCEL_OPTION,};
        }
    }

    @Override
    public String getName() {
        return getString("DEBUG_COMMAND"); // NOI18N
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        FileObject executableFO = null;
        if (activatedNodes != null && activatedNodes.length == 1) {
            DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
            String mime = getMime(dataObject);
            if (dataObject != null  && dataObject.isValid() && MIMENames.isBinary(mime)) {
                FileObject fo = dataObject.getPrimaryFile();
                if (fo != null) {
                    executableFO = fo;
                }
            }
        }
        if (executableFO != null) {
            perform(executableFO);
        }
    }

    private String getMime(DataObject dob) {
        FileObject primaryFile = dob == null ? null : dob.getPrimaryFile();
        String mime = primaryFile == null ? "" : primaryFile.getMIMEType();
        return mime;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        String mime = getMime(dataObject);
        // disabled for core files, see issue 136696
        if (!MIMENames.isBinary(mime) || MIMENames.ELF_CORE_MIME_TYPE.equals(mime)) {
            return false;
        }
        return true;
    }

    private void perform(FileObject executableFO) {
        if (debugButton == null) {
            init();
        }
        try {
            perform(new RunDialogPanel(executableFO, debugButton, true));
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void perform(final RunDialogPanel runDialogPanel) {
        if (debugButton == null) {
            init();
        }
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                runDialogPanel,
                getString("DebugDialogTitle"),
                true,
                options,
                debugButton,
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null);
        Object ret = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (ret == debugButton) {
            runDialogPanel.getSelectedProject((Project project) -> {
                performDebug(project, runDialogPanel.getExecutablePath() + " " + runDialogPanel.getArguments()); // NOI18N
            });
        }
    }

    private void performDebug(Project project, String runCommand) {
        final LaunchersRegistry registry = LaunchersRegistryFactory.getInstance(project.getProjectDirectory());
        Launcher launcher = null;
        if (registry.hasLaunchers()) {
            for (Launcher l : registry.getLaunchers()) {
                if (runCommand.equals(l.getCommand())) {
                    launcher = l;
                    break;
                }
            }
        }
        if (launcher == null) {
            launcher = new Launcher(runCommand, null);
            registry.add(launcher);
        }
        // we do not have API to "execute" launcher, so
        LauncherAction action = LauncherAction.debugAsAction();
        action.createContextAwareInstance(Lookups.fixed(project));
        action.new LauncherExecutableAction(launcher).actionPerformed(null);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RunDialogAction.class); // FIXUP ???
    }

    private ResourceBundle bundle;

    private String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(RunDialogAction.class);
        }
        return bundle.getString(s);
    }
    
    public final class SimpleDebugActionProxy extends AbstractAction {

        private final Project project;
        private final String executable;

        public SimpleDebugActionProxy(Project project, String executable) {
            this.project = project;
            this.executable = executable;
        }
        
        @Override
        public Object getValue(String key) {
            if (NAME.equals(key)) {
                return DebugDialogAction.this.getName();
            }
            return super.getValue(key);
        }        
        
        @Override
        public void actionPerformed(ActionEvent e) {
            performDebug(project, executable);
        }
    }
               
    
}
