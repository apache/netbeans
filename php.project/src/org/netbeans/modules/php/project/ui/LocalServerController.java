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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Component (non-UI) for local servers.
 * @author Tomas Mysik
 */
public final class LocalServerController {

    private final JComboBox<LocalServer> localServerComboBox;
    private final JButton localServerBrowseButton;
    private final SourcesFolderProvider sourcesFolderProvider; // can be null
    private final String browseDialogTitle;
    final ChangeSupport changeSupport = new ChangeSupport(this);
    private /*final*/ MutableComboBoxModel<LocalServer> localServerComboBoxModel;
    private final LocalServer.ComboBoxEditor localServerComboBoxEditor;
    private final BrowseHandler browseHandler;

    public static LocalServerController create(JComboBox<LocalServer> localServerComboBox, JButton localServerBrowseButton,
            SourcesFolderProvider sourcesFolderProvider, BrowseHandler browseHandler, String browseDialogTitle, LocalServer... defaultLocalServers) {
        return new LocalServerController(localServerComboBox, localServerBrowseButton, sourcesFolderProvider, browseHandler,
                browseDialogTitle, defaultLocalServers);
    }

    public static LocalServerController create(JComboBox<LocalServer> localServerComboBox, JButton localServerBrowseButton, BrowseHandler browseHandler,
            String browseDialogTitle, LocalServer... defaultLocalServers) {
        return new LocalServerController(localServerComboBox, localServerBrowseButton, null, browseHandler, browseDialogTitle,
                defaultLocalServers);
    }

    private LocalServerController(JComboBox<LocalServer> localServerComboBox, JButton localServerBrowseButton,
            SourcesFolderProvider sourcesFolderProvider, BrowseHandler browseHandler, String browseDialogTitle, LocalServer... defaultLocalServers) {
        assert localServerComboBox != null;
        assert localServerBrowseButton != null;
        assert browseHandler != null;
        assert browseDialogTitle != null;
        assert localServerComboBox.isEditable() : "localServerComboBox has to be editable";
        assert localServerComboBox.getEditor().getEditorComponent() instanceof JTextField;

        this.localServerComboBox = localServerComboBox;
        this.localServerBrowseButton = localServerBrowseButton;

        this.sourcesFolderProvider = sourcesFolderProvider;
        this.browseHandler = browseHandler;
        this.browseDialogTitle = browseDialogTitle;
        localServerComboBoxModel = new LocalServer.ComboBoxModel(defaultLocalServers);
        JTextField editor = (JTextField) localServerComboBox.getEditor().getEditorComponent();
        localServerComboBoxEditor = new LocalServer.ComboBoxEditor(editor);

        localServerComboBox.setModel(localServerComboBoxModel);
        localServerComboBox.setRenderer(new LocalServer.ComboBoxRenderer());
        localServerComboBox.setEditor(localServerComboBoxEditor);

        registerListeners();
    }

    private void registerListeners() {
        localServerComboBoxEditor.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                changeSupport.fireChange();
            }
        });
        localServerBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newSubfolderName = null;
                if (sourcesFolderProvider != null) {
                    newSubfolderName = sourcesFolderProvider.getSourcesFolderName();
                }
                Utils.browseLocalServerAction(localServerComboBox, localServerComboBoxModel,
                        newSubfolderName, browseDialogTitle, browseHandler.getDirKey());
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public LocalServer getLocalServer() {
        return (LocalServer) localServerComboBox.getSelectedItem();
    }

    public MutableComboBoxModel<LocalServer> getLocalServerModel() {
        return localServerComboBoxModel;
    }

    public void setLocalServerModel(MutableComboBoxModel<LocalServer> localServers) {
        localServerComboBoxModel = localServers;
        localServerComboBox.setModel(localServerComboBoxModel);
    }

    public void addLocalServer(LocalServer localServer) {
        localServerComboBox.addItem(localServer);
    }

    public void selectLocalServer(LocalServer localServer) {
        localServerComboBox.setSelectedItem(localServer);
    }

    // to enable/disable components
    public void setEnabled(boolean enabled) {
        localServerComboBox.setEnabled(enabled);
        localServerComboBox.setEditable(enabled);
        localServerBrowseButton.setEnabled(enabled);
    }

    /**
     * Validate given local server instance, its source root.
     * @param localServer local server to validate.
     * @param type the type for error messages.
     * @param allowNonEmpty <code>true</code> if the folder can exist and can be non empty.
     * @param allowInRoot  <code>true</code> if the folder can exist and can be a root directory "/" (on *NIX only).
     * @return error message or <code>null</code> if source root is ok.
     * @see Utils#validateProjectDirectory(java.lang.String, java.lang.String, boolean)
     */
    public static String validateLocalServer(final LocalServer localServer, String type, boolean allowNonEmpty,
            boolean allowInRoot) {
        if (!localServer.isEditable()) {
            return null;
        }
        String err = null;
        String sourcesLocation = localServer.getSrcRoot();
        File sources = FileUtil.normalizeFile(new File(sourcesLocation));
        if (sourcesLocation.trim().length() == 0
                || !Utils.isValidFileName(sources)) {
            err = NbBundle.getMessage(LocalServerController.class, "MSG_Illegal" + type + "Name");
        } else {
            err = Utils.validateProjectDirectory(sourcesLocation, type, allowNonEmpty, allowInRoot);
        }
        return err;
    }

    // handle browse button - provide dir key (for saving & restoring)
    public interface BrowseHandler {
        String getDirKey();
    }

}
