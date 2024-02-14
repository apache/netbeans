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
package org.netbeans.modules.php.codeception.ui;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

public final class CodeceptRunParametersPanel extends JPanel {

    // <source directory path, parameters>
    private static final Map<String, ParameterContainer> PARAM_HISTORY = new HashMap<>();

    private final PhpModule phpModule;

    private CodeceptRunParametersPanel(@NonNull PhpModule phpModule) {
        assert EventQueue.isDispatchThread();
        assert phpModule != null;
        this.phpModule = phpModule;
        initComponents();
        init();
    }

    @CheckForNull
    @NbBundle.Messages("CodeceptRunParametersPanel.dialog.title=Additional parameters")
    public static String showDialog(@NonNull final PhpModule phpModule) {
        return Mutex.EVENT.readAccess(new Mutex.Action<String>() {

            @Override
            public String run() {
                assert EventQueue.isDispatchThread();
                CodeceptRunParametersPanel panel = new CodeceptRunParametersPanel(phpModule);
                NotifyDescriptor descriptor = new NotifyDescriptor(
                        panel,
                        Bundle.CodeceptRunParametersPanel_dialog_title(),
                        NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.PLAIN_MESSAGE,
                        null,
                        NotifyDescriptor.OK_OPTION);
                if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.OK_OPTION) {
                    return null;
                }
                panel.storeParameters();
                return panel.getSelectedParameters();
            }
        });
    }

    private void init() {
        List<String> params = new ArrayList<>();
        params.add(""); // NOI18N
        params.addAll(getStoredParameters());
        parametersComboBox.setModel(new DefaultComboBoxModel<>(params.toArray(new String[0])));
        preselectLastSelectedParam();
    }

    private void preselectLastSelectedParam() {
        ParameterContainer params = getParameters();
        if (params == null) {
            return;
        }
        String lastSelected = params.getLastSelected();
        if (lastSelected == null) {
            parametersComboBox.setSelectedItem(""); // NOI18N
            return;
        }
        for (int i = 0; i < parametersComboBox.getItemCount(); i++) {
            Object item = parametersComboBox.getItemAt(i);
            if (item.equals(lastSelected)) {
                parametersComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private String getSelectedParameters() {
        return parametersComboBox.getSelectedItem().toString().trim();
    }

    @CheckForNull
    private ParameterContainer getParameters() {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return null;
        }
        String sourceDirectoryPath = sourceDirectory.getPath();
        return PARAM_HISTORY.get(sourceDirectoryPath);
    }

    private List<String> getStoredParameters() {
        ParameterContainer stored = getParameters();
        if (stored == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(stored.getParams());
        Collections.sort(result);
        return result;
    }

    private void storeParameters() {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return;
        }
        String sourceDirectoryPath = sourceDirectory.getPath();
        ParameterContainer params = PARAM_HISTORY.get(sourceDirectoryPath);
        if (params == null) {
            params = new ParameterContainer();
            PARAM_HISTORY.put(sourceDirectoryPath, params);
        }
        String currentParam = getSelectedParameters();

        params.addParam(currentParam);
        params.setLastSelected(currentParam);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        parametersComboBox = new JComboBox<String>();

        parametersComboBox.setEditable(true);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(parametersComboBox, GroupLayout.Alignment.TRAILING, 0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(parametersComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<String> parametersComboBox;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    /**
     * Holds a set of parameters and maintains info on what parameter was the
     * last one selected.
     */
    private static final class ParameterContainer {

        private final Set<String> params = new HashSet<>();

        // @GuardedBy("EDT")
        private String lastSelected;


        public void addParam(String param) {
            params.add(param);
        }

        public String getLastSelected() {
            assert EventQueue.isDispatchThread();
            return lastSelected;
        }

        public void setLastSelected(String lastSelected) {
            assert EventQueue.isDispatchThread();
            this.lastSelected = lastSelected;
        }

        public Set<String> getParams() {
            return Collections.unmodifiableSet(params);
        }

    }

}
