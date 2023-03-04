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

package org.netbeans.modules.php.project.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.openide.util.ChangeSupport;

// XXX should be replaced (?) by PhpEnvironment.DocumentRoot
/**
 * @author Tomas Mysik
 */
public class LocalServer implements Comparable<LocalServer> {
    public static final LocalServer PENDING_LOCAL_SERVER = new LocalServer(PhpEnvironment.PENDING_DOCUMENT_ROOT.getDocumentRoot());

    private final String virtualHost;
    private final String url;
    private final String documentRoot;
    private final boolean editable;
    private String hint = " "; // NOI18N
    private String srcRoot;

    public static LocalServer getEmpty() {
        return new LocalServer("", ""); // NOI18N
    }

    public LocalServer(final LocalServer localServer) {
        this(localServer.virtualHost, localServer.documentRoot, localServer.srcRoot, localServer.editable);
    }

    public LocalServer(String srcRoot) {
        this(null, null, srcRoot);
    }

    public LocalServer(String documentRoot, String srcRoot) {
        this(null, documentRoot, srcRoot);
    }

    public LocalServer(String virtualHost, String documentRoot, String srcRoot) {
        this(virtualHost, documentRoot, srcRoot, true);
    }

    public LocalServer(String virtualHost, String documentRoot, String srcRoot, boolean editable) {
        this(virtualHost, null, documentRoot, srcRoot, editable);
    }

    public LocalServer(String virtualHost, String url, String documentRoot, String srcRoot, boolean editable) {
        this.virtualHost = virtualHost;
        this.url = url;
        this.documentRoot = documentRoot;
        this.srcRoot = srcRoot;
        this.editable = editable;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getUrl() {
        return url;
    }

    public String getDocumentRoot() {
        return documentRoot;
    }

    public String getSrcRoot() {
        return srcRoot;
    }

    public void setSrcRoot(String srcRoot) {
        if (!editable) {
            throw new IllegalStateException("srcRoot cannot be changed because instance is not editable");
        }
        this.srcRoot = srcRoot;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isPending() {
        return this == PENDING_LOCAL_SERVER;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(150);
        sb.append(getClass().getName());
        sb.append("[virtualHost: "); // NOI18N
        sb.append(virtualHost);
        sb.append(", url: "); // NOI18N
        sb.append(url);
        sb.append(", documentRoot: "); // NOI18N
        sb.append(documentRoot);
        sb.append(", srcRoot: "); // NOI18N
        sb.append(srcRoot);
        sb.append(", hint: "); // NOI18N
        sb.append(hint);
        sb.append(", editable: "); // NOI18N
        sb.append(editable);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    @Override
    public int compareTo(LocalServer ls) {
        if (!editable) {
            return -1;
        }
        return srcRoot.compareTo(ls.getSrcRoot());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LocalServer other = (LocalServer) obj;
        if ((this.virtualHost == null) ? (other.virtualHost != null) : !this.virtualHost.equals(other.virtualHost)) {
            return false;
        }
        if ((this.documentRoot == null) ? (other.documentRoot != null) : !this.documentRoot.equals(other.documentRoot)) {
            return false;
        }
        if (this.editable != other.editable) {
            return false;
        }
        if ((this.srcRoot == null) ? (other.srcRoot != null) : !this.srcRoot.equals(other.srcRoot)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (virtualHost != null ? virtualHost.hashCode() : 0);
        hash = 97 * hash + (documentRoot != null ? documentRoot.hashCode() : 0);
        hash = 97 * hash + (editable ? 1 : 0);
        hash = 97 * hash + (srcRoot != null ? srcRoot.hashCode() : 0);
        return hash;
    }

    public static class ComboBoxEditor implements javax.swing.ComboBoxEditor, UIResource, DocumentListener {

        private static final long serialVersionUID = -4527321803090719483L;
        private final JTextField component;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private LocalServer activeItem = null;

        public ComboBoxEditor(JTextField editor) {
            super();

            component = editor;
            component.getDocument().addDocumentListener(this);
        }

        @Override
        public Component getEditorComponent() {
            return component;
        }

        @Override
        public void setItem(Object anObject) {
            if (anObject == null) {
                return;
            }
            assert anObject instanceof LocalServer;
            activeItem = (LocalServer) anObject;
            component.setText(activeItem.getSrcRoot());
        }

        @Override
        public Object getItem() {
            return new LocalServer(activeItem);
        }

        @Override
        public void selectAll() {
            component.selectAll();
            component.requestFocus();
        }

        @Override
        public void addActionListener(ActionListener l) {
            component.addActionListener(l);
        }

        @Override
        public void removeActionListener(ActionListener l) {
            component.removeActionListener(l);
        }

        /**
         * Add listener to the combobox changes.
         * @param l listener to add.
         */
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        /**
         * Remove listener from the combobox changes.
         * @param l listener to remove.
         */
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            if (activeItem == null) {
                // no items set yet
                return;
            }
            boolean enabled = false;
            if (activeItem.isEditable()) {
                enabled = true;
                activeItem.setSrcRoot(component.getText().trim());
            }
            component.setEnabled(enabled);
            changeSupport.fireChange();
        }
    }

    public static class ComboBoxRenderer extends JLabel implements ListCellRenderer<LocalServer>, UIResource {

        private static final long serialVersionUID = 146876454678878410L;

        public ComboBoxRenderer() {
            super();
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends LocalServer> list, LocalServer value, int index, boolean isSelected,
                boolean cellHasFocus) {
            setName("ComboBox.listRenderer"); // NOI18N
            if (value != null) {
                String srcRoot = value.getSrcRoot();
                setText(srcRoot.length() == 0 ? " " : srcRoot); // NOI18N // combo is too low otherwise
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

    public static class ComboBoxModel extends AbstractListModel<LocalServer> implements MutableComboBoxModel<LocalServer> {

        private static final long serialVersionUID = 4857683465761112L;

        private final List<LocalServer> data;
        private LocalServer selected = null;


        public ComboBoxModel(LocalServer... defaultLocalServers) {
            if (defaultLocalServers == null || defaultLocalServers.length == 0) {
                // prevent NPE
                defaultLocalServers = new LocalServer[] {getEmpty()};
            }
            data = new ArrayList<>(2 * defaultLocalServers.length);
            data.addAll(Arrays.asList(defaultLocalServers));
            selected = data.get(0);
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public LocalServer getElementAt(int index) {
            return data.get(index);
        }

        @Override
        public void addElement(LocalServer localServer) {
            if (!data.add(localServer)) {
                return;
            }
            Collections.sort(data);
            int idx = indexOf(localServer);
            fireIntervalAdded(this, idx, idx);
        }

        @Override
        public void insertElementAt(LocalServer localServer, int index) {
            data.add(index, localServer);
            fireIntervalAdded(this, index, index);
        }

        public int indexOf(LocalServer configuration) {
            return data.indexOf(configuration);
        }

        @Override
        public void removeElement(Object object) {
            assert object instanceof LocalServer;
            LocalServer localServer = (LocalServer) object;
            int idx = indexOf(localServer);
            if (idx == -1) {
                return;
            }
            boolean result = data.remove(localServer);
            assert result;
            fireIntervalRemoved(this, idx, idx);
        }

        @Override
        public void removeElementAt(int index) {
            if (getElementAt(index) == selected) {
                if (index == 0) {
                    setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
                } else {
                    setSelectedItem(getElementAt(index - 1));
                }
            }
            data.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        @Override
        public void setSelectedItem(Object object) {
            if ((selected != null && !selected.equals(object))
                    || selected == null && object != null) {
                assert object == null || object instanceof LocalServer : "Trying to set object of type: " + object.getClass().getName();
                selected = (LocalServer) object;
                fireContentsChanged(this, -1, -1);
            }
        }

        public void fireContentsChanged() {
            fireContentsChanged(this, -1, -1);
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        public List<LocalServer> getElements() {
            return Collections.unmodifiableList(data);
        }

        public void setElements(List<LocalServer> localServers) {
            int size = data.size();
            data.clear();
            if (size > 0) {
                fireIntervalRemoved(this, 0, size - 1);
            }
            if (localServers.size() > 0) {
                data.addAll(localServers);
                Collections.sort(data);
                fireIntervalAdded(this, 0, data.size() - 1);
            }
        }
    }
}
