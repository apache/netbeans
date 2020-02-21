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
package org.netbeans.modules.subversion.remote.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.options.SvnOptionsController;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * 
 */
public class MissingClient implements ActionListener, HyperlinkListener {
    
    private final MissingClientPanel panel;
    private static final HashSet<String> ALLOWED_EXECUTABLES =  new HashSet<>(Arrays.asList(new String[] {"svn"} )); //NOI18N
    private final Context context;
    
    /** Creates a new instance of MissingSvnClient */
    public MissingClient(Context context) {
        this.context = context;
        panel = new MissingClientPanel();
        panel.browseButton.addActionListener(this);        
        panel.executablePathTextField.setText(SvnModuleConfig.getDefault(context.getFileSystem()).getExecutableBinaryPath());
        panel.textPane.addHyperlinkListener(this);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void show() {        
        JButton ok = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_OK"));        
        JButton cancel = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Cancel"));        
        NotifyDescriptor descriptor = new NotifyDescriptor (
                panel, 
                NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_CommandFailed_Title"), 
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object [] { ok, cancel },
                ok);
        if(DialogDisplayer.getDefault().notify(descriptor) == ok) {
            SvnModuleConfig.getDefault(context.getFileSystem()).setExecutableBinaryPath(panel.executablePathTextField.getText());
            SvnClientFactory.resetClient();
        }
    }
    
    @Messages({
        "FileChooser.SvnExecutables.desc=SVN Executables"
    })
    private void onBrowseClick() {
        VCSFileProxy oldFile = getExecutableFile();
        final JFileChooser fileChooser = VCSFileProxySupport.createFileChooser(oldFile);
        fileChooser.setDialogTitle(NbBundle.getMessage(SvnOptionsController.class, "Browse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);       
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || ALLOWED_EXECUTABLES.contains(f.getName());
            }
            @Override
            public String getDescription() {
                return Bundle.FileChooser_SvnExecutables_desc();
            }
        });
        fileChooser.showDialog(panel, NbBundle.getMessage(SvnOptionsController.class, "OK_Button")); // NOI18N
        VCSFileProxy f = VCSFileProxySupport.getSelectedFile(fileChooser);
        if (f != null) {
            while (!f.exists() || f.isFile()) {
                VCSFileProxy parent = f.getParentFile();
                if (parent == null) {
                    break;
                } else {
                    f = parent;
                }
            }
            panel.executablePathTextField.setText(f.getPath());
        }
    }

    private VCSFileProxy getExecutableFile() {
        String execPath = panel.executablePathTextField.getText();
        VCSFileProxy resource = VCSFileProxySupport.getResource(context.getFileSystem(), execPath);
        return resource.normalizeFile();
    }    

    @Override
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == panel.browseButton) {
            onBrowseClick();
        }
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings("RCN") // assert in release mode does not guarantee that "displayer != null"
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
        URL url = e.getURL();
        assert url != null;
        HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
        assert displayer != null : "HtmlBrowser.URLDisplayer found.";
        if (displayer != null) {
            displayer.showURL (url);
        } else {
            Subversion.LOG.info("No URLDisplayer found.");
        }
    }
}
