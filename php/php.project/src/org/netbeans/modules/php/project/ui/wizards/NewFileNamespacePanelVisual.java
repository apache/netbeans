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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

public class NewFileNamespacePanelVisual extends JPanel {

    private static final String NAMESPACE_SEPARATOR = "\\"; // NOI18N
    private static final Pattern NAMESPACE_PART_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$", Pattern.CASE_INSENSITIVE); // NOI18N

    private final NamespaceComboBoxModel comboBoxModel = new NamespaceComboBoxModel();
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public NewFileNamespacePanelVisual() {
        assert EventQueue.isDispatchThread();
        initComponents();
        init();
    }

    private void init() {
        assert EventQueue.isDispatchThread();
        namespaceComboBox.setModel(comboBoxModel);
        // listeners
        namespaceComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireChange();
            }
        });
        namespaceComboBox.getEditor().getEditorComponent().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // noop
            }
            @Override
            public void keyPressed(KeyEvent e) {
                // noop
            }
            @Override
            public void keyReleased(KeyEvent e) {
                fireChange();
            }
        });
    }

    public String getSelectedNamespace() {
        return Mutex.EVENT.readAccess(new Mutex.Action<String>() {
            @Override
            public String run() {
                assert EventQueue.isDispatchThread();
                if (!namespaceComboBox.isEnabled()) {
                    return null;
                }
                return (String) namespaceComboBox.getEditor().getItem();
            }
        });
    }

    public void setSelectedNamespace(String namespace) {
        comboBoxModel.setSelectedItem(namespace);
    }

    public void setNamespaces(List<String> namespaces) {
        assert EventQueue.isDispatchThread();
        namespaceComboBox.setEnabled(true);
        comboBoxModel.setNamespaces(namespaces);
    }

    @NbBundle.Messages("NewFileNamespacePanelVisual.message.pleaseWait=Please wait...")
    public void setPleaseWaitState() {
        assert EventQueue.isDispatchThread();
        namespaceComboBox.setEnabled(false);
        comboBoxModel.setNamespaces(Collections.singletonList(Bundle.NewFileNamespacePanelVisual_message_pleaseWait()));
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @NbBundle.Messages("NewFileNamespacePanelVisual.error.namespace.invalid=Namespace is not valid.")
    public static String validateNamespace(String namespace) {
        assert namespace != null;
        if (namespace.isEmpty()) {
            return null;
        }
        if (namespace.startsWith(NAMESPACE_SEPARATOR)
                || namespace.endsWith(NAMESPACE_SEPARATOR)) {
            return Bundle.NewFileNamespacePanelVisual_error_namespace_invalid();
        }
        for (String part : StringUtils.explode(namespace, NAMESPACE_SEPARATOR)) {
            if (!NAMESPACE_PART_PATTERN.matcher(part).matches()) {
                return Bundle.NewFileNamespacePanelVisual_error_namespace_invalid();
            }
        }
        return null;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        namespaceLabel = new JLabel();
        namespaceComboBox = new JComboBox<String>();

        Mnemonics.setLocalizedText(namespaceLabel, NbBundle.getMessage(NewFileNamespacePanelVisual.class, "NewFileNamespacePanelVisual.namespaceLabel.text")); // NOI18N

        namespaceComboBox.setEditable(true);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(namespaceLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(namespaceComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(namespaceLabel)
                .addComponent(namespaceComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<String> namespaceComboBox;
    private JLabel namespaceLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class NamespaceComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {

        private static final long serialVersionUID = -5783235465654654L;

        private final List<String> namespaces = new CopyOnWriteArrayList<>();

        private volatile String selectedNamespace = null;


        @Override
        public int getSize() {
            return namespaces.size();
        }

        @Override
        public String getElementAt(int index) {
            return namespaces.get(index);
        }

        @Override
        public void setSelectedItem(Object namespace) {
            selectedNamespace = (String) namespace;
            fireContentsChanged();
        }

        @Override
        public String getSelectedItem() {
            return selectedNamespace;
        }

        public void setNamespaces(List<String> namespaces) {
            clearNamespaces();
            this.namespaces.addAll(namespaces);
            for (String namespace : namespaces) {
                selectedNamespace = namespace;
                break;
            }
            fireContentsChanged();
        }

        private void clearNamespaces() {
            namespaces.clear();
            selectedNamespace = null;
            fireContentsChanged();
        }

        private void fireContentsChanged() {
            fireContentsChanged(this, -1, -1);
        }

    }

}
