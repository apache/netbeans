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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;

import org.netbeans.api.project.Project;

import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;


import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.spi.UserDebugCoreAction;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Debug a corefile after putting up a corefile chooser.
 */
public class DebugCoreAction extends SystemAction {

    @Override
    public String getName() {
        return Catalog.get("LOADCOREDIALOGACTION_NAME"); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Node[] activeNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        performAction(activeNodes, false);
    }

    // mimics NodeAction
    public void performAction(Node[] activeNodes, boolean ro) {
        ProjectSupport.ProjectSeed seed;
        DebugCorePanel coreDialogPanel;

        DataNode corefileNode = null;
        String corefilePath = null;
        String host = null;

        //
        // If activeNodes[0] is a corefile set 'corefileNode'
        //
        DataObject dataObject = null;
        if (activeNodes.length == 1) {
            dataObject = activeNodes[0].getCookie(DataObject.class);
            if (MIMENames.ELF_CORE_MIME_TYPE.equals(IpeUtils.getMime(dataObject))) {
                Node node = dataObject.getNodeDelegate();
                if (node instanceof DataNode) {
                    corefileNode = (DataNode) node;
                }
            }
        }

        //
        // Convert corefileNode to corefilePath or reuse last corefilePath.
        //
        if (corefileNode != null && corefileNode.getDataObject() != null) {
            FileObject coreFile = corefileNode.getDataObject().getPrimaryFile();
            ExecutionEnvironment exEnv = FileSystemProvider.getExecutionEnvironment(coreFile);
            host = ExecutionEnvironmentFactory.toUniqueID(exEnv);
            corefilePath = coreFile.getPath();
        }

        //
        // Pop up a dialog to confirm choice of corefile or get a corefile
        // and get other settings.
        //
        JButton debugButton;
        Object options[];
        debugButton = new JButton(Catalog.get("DEBUG_BUTTON_TXT")); // NOI18N
        Catalog.setAccessibleDescription(debugButton, "ACSD_Debug"); // NOI18N
        options = new Object[]{
                    debugButton,
                    DialogDescriptor.CANCEL_OPTION,};

        coreDialogPanel = new DebugCorePanel(corefilePath, debugButton, ro, host);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                coreDialogPanel,
                Catalog.get("LBL_DebugCorefile"), // NOI18N
                true,
                options,
                debugButton,
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null);

        Object ret = DialogDisplayer.getDefault().notify(dialogDescriptor);

        if (ret != debugButton) {
            return;
        }

        //
        // Extract info from dialog fields
        //
        String corefile = coreDialogPanel.getCorefilePath();
        String executable = coreDialogPanel.getExecutablePath().trim();
        Project project = coreDialogPanel.getSelectedProject();
        boolean noproject = coreDialogPanel.getNoProject();
        String hostName = coreDialogPanel.getHostName();
	EngineType engineType = coreDialogPanel.getEngine();

        if (noproject) {
            UserDebugCoreAction action = Lookup.getDefault().lookup(UserDebugCoreAction.class);
            if (action != null) {
                if (Catalog.get("AutoCoreExe").equals(executable)) { // NOI18N
                    executable = null;
                }
                action.debugCore(hostName, corefile, executable, engineType);
                return;
            }
        }

        //
        // Create Project and configuration as needed.
        //
        seed = new ProjectSupport.ProjectSeed(
                project, engineType, noproject,
                executable,
                ProjectSupport.Model.DONTCARE,
                corefile,
                /*pid*/ 0,
                /*workingdir*/ null,
                /*args*/ null,
                /*envs*/ null,
                hostName);
        ProjectSupport.getProject(seed);

        corefile = seed.corefile();
        executable = seed.executableNoSentinel();
        Configuration conf = seed.conf();
        //not used Host host = CndRemote.hostFromName(null, seed.getHostName());

        if (!Host.isRemote(hostName)) {
            if (corefileNode == null) {
                //
                // Validate the core file.
                //
                // findCorefileNode() will do the Node -> DataNode thing.
                // It's worth it because then we get CND's mime recognition of
                // CoreElfObject's.
                //
                corefileNode = findCorefileNode(corefile);

                if (corefileNode == null) {
                    String msg = NbBundle.getMessage(DebugCoreAction.class, "ERROR_NOTACOREFILE", corefile); // NOI18N
                    errorDialog(msg);
                    return;
                }
            }
        }

        //
        // Validate the executable
        //
        if (executable == null ||
                executable.equals(Catalog.get("AutoCoreExe")) ||  // NOI18N
                executable.equals("") || // NOI18N
                executable.equals("-")) { // NOI18N

            executable = "-";	// NOI18N

        } else {
            if (!Host.isRemote(hostName)) {
                File exeFile = new File(executable);
                if (!exeFile.exists() || exeFile.isDirectory()) {
                    StatusDisplayer.getDefault().setStatusText(
                            Catalog.get("MSG_BadExecutable"));  // NOI18N
                    errorDialog(Catalog.get("MSG_BadExecutable")); // NOI18N
                    return;
                }
            }
        }

        //
        // Debug the given core file
        //
        DebugTarget dt = new DebugTarget((MakeConfiguration) conf);
        dt.setExecutable(executable);
        dt.setCorefile(corefile);
        dt.setHostName(seed.getHostName());
	dt.setEngine(engineType);
        NativeDebuggerManager.get().debugCore(dt);
    }

    private void errorDialog(String txt) {
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                txt,
                NotifyDescriptor.ERROR_MESSAGE));
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/cnd/debugger/common2/icons/debug_core_file.png"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DebugCoreAction.class); // FIXUP ???
    }

    private static DataNode findCorefileNode(String filePath) {
        if (filePath == null) {
            return null;
        }
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(filePath)));
        if (fo == null) {
            return null; // FIXUP
        }
        if (!MIMENames.ELF_CORE_MIME_TYPE.equals(fo.getMIMEType())) {
            return null;
        }
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fo);
        } catch (Exception e) {
            // FIXUP
        }
        if (dataObject == null) {
            return null; // FIXUP
        }
        Node node = dataObject.getNodeDelegate();
        if (!(node instanceof DataNode)) {
            return null;
        }
        return (DataNode) node;
    }
}
