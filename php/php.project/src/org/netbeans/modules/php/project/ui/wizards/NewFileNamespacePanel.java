/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
