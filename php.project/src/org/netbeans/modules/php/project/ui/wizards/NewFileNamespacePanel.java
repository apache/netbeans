/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.wizards;

import java.awt.EventQueue;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Bottom panel for providing namespace.
 */
class NewFileNamespacePanel implements NewFileWizardIterator.BottomPanel {

    public static final String NAMESPACE = "NAMESPACE"; // NOI18N
    static final RequestProcessor RP = new RequestProcessor(NewFileNamespacePanel.class);

    private final RequestProcessor.Task namespaceFetcherTask;

    NewFileNamespacePanelVisual panel;
    volatile FileObject targetFolder = null;
    volatile WizardDescriptor settings = null;


    public NewFileNamespacePanel() {
        namespaceFetcherTask = RP.create(new NamespaceFetcher());
    }

    @Override
    public boolean isPresentForProject(PhpProject project) {
        return ProjectPropertiesSupport.getPhpVersion(project).hasNamespaces();
    }

    @Override
    public NewFileNamespacePanelVisual getComponent() {
        if (panel == null) {
            panel = new NewFileNamespacePanelVisual();
        }
        return panel;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        this.settings = settings;
        fetchNamespaces(Templates.getTargetFolder(settings));
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(NAMESPACE, getComponent().getSelectedNamespace());
    }

    @NbBundle.Messages("NewFileNamespacePanel.error.namespace=Namespace must be provided")
    @Override
    public boolean isValid() {
        if (settings == null) {
            // not displayed yet
            return false;
        }
        String namespace = getComponent().getSelectedNamespace();
        if (namespace == null) {
            // fetching namespaces...
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
            return false;
        }
        String error = NewFileNamespacePanelVisual.validateNamespace(namespace);
        if (error != null) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
            return false;
        }
        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    private void fetchNamespaces(FileObject folder) {
        assert EventQueue.isDispatchThread();
        if (folder == null) {
            return;
        }
        if (folder.equals(targetFolder)) {
            return;
        }
        if (!folder.isFolder()) {
            return;
        }
        targetFolder = folder;
        namespaceFetcherTask.schedule(20);
    }

    @Override
    public void targetFolderChanged(FileObject targetFolder) {
        fetchNamespaces(targetFolder);
    }

    //~ Inner classes

    private final class NamespaceFetcher implements Runnable {

        @Override
        public synchronized void run() {
            final FileObject folder = targetFolder;
            // any selected namespace?
            final String selectedNamespace = panel.getSelectedNamespace();
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.setPleaseWaitState();
                }
            });
            SortedSet<String> namespaces = new TreeSet<>();
            // add default namespace
            namespaces.add(""); // NOI18N
            EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
            for (FileObject child : folder.getChildren()) {
                if (!folder.equals(targetFolder)) {
                    // folder change => end
                    return;
                }
                if (child.isFolder()) {
                    continue;
                }
                if (FileUtils.isPhpFile(child)) {
                    for (PhpType phpType : editorSupport.getTypes(child)) {
                        String name = phpType.getName();
                        String fqn = phpType.getFullyQualifiedName();
                        if (fqn == null
                                || name.length() + 1 == fqn.length()) {
                            // fqn not known or default namespace
                            continue;
                        }
                        // remove leading "\", class name itself and trailing "\"
                        String namespace = fqn.substring(1, fqn.length() - name.length() - 1);
                        if (StringUtils.hasText(namespace)) {
                            namespaces.add(namespace);
                        }
                    }
                }
            }
            if (!folder.equals(targetFolder)) {
                // folder change => end
                return;
            }
            if (selectedNamespace != null) {
                namespaces.add(selectedNamespace);
            }
            final List<String> namespacesCopy = new CopyOnWriteArrayList<>(namespaces);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.setNamespaces(namespacesCopy);
                    if (StringUtils.hasText(selectedNamespace)) {
                        panel.setSelectedNamespace(selectedNamespace);
                    } else if (namespacesCopy.size() == 2) {
                        // exactly one namespace in the whole folder => preselect it
                        panel.setSelectedNamespace(namespacesCopy.get(1));
                    }
                }
            });
        }

    }

}
