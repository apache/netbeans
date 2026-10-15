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
package org.netbeans.modules.diff;

import java.awt.Component;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Mode;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.*;
import java.io.File;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Maros Sandor
 */
class EditorBufferSelectorPanel extends JPanel implements ListSelectionListener, PropertyChangeListener {
    
    private final JFileChooser fileChooser;
    private final FileObject peer;
    private JList elementsList;
    private FileObject selectedEditorFile;

    /** Creates new form EditorBufferSelectorPanel 
     * @param fileChooser*/
    public EditorBufferSelectorPanel(JFileChooser fileChooser, FileObject peer) {
        this.fileChooser = fileChooser;
        this.peer = peer;
        initComponents();
        initEditorDocuments();
        fileChooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, this);
    }

    private void initEditorDocuments() {
        elementsList = new JList() {
            @Override
            public String getToolTipText(MouseEvent event) {
                int index = locationToIndex(event.getPoint());
                if (index != -1) {
                    EditorListElement element = (EditorListElement) elementsList.getModel().getElementAt(index);
                    return element.fileObject.getPath();
                }
                return null;
            }
        };
        
        List<EditorListElement> elements = new ArrayList<EditorListElement>();

        WindowManager wm = WindowManager.getDefault();
        Set<? extends Mode> modes = wm.getModes();
        for (Mode mode : modes) {
            if (wm.isEditorMode(mode)) {
                TopComponent[] tcs = mode.getTopComponents();
                for (TopComponent tc : tcs) {
                    Lookup lukap = tc.getLookup();
                    FileObject fo = lukap.lookup(FileObject.class);
                    if (fo == null) {
                        DataObject dobj = lukap.lookup(DataObject.class);
                        if (dobj != null) {
                            fo = dobj.getPrimaryFile();
                        }
                    }
                    if (fo != null && fo != peer) {
                        if (tc.getHtmlDisplayName() != null) {
                            elements.add(new EditorListElement(fo, tc.getHtmlDisplayName(), true));
                        } else {
                            elements.add(new EditorListElement(fo, tc.getName(), false));
                        }   
                    }
                }
            }
        }

        elementsList.setListData(elements.toArray(new EditorListElement[0]));
        elementsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        elementsList.addListSelectionListener(this);
        elementsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (isSelected && value instanceof EditorListElement && ((EditorListElement) value).isHtml()) {
                    value = stripHtml(((EditorListElement) value).toString());
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }

            private String stripHtml (String htmlText) {
                if (null == htmlText) {
                    return null;
                }
                return htmlText.replaceAll( "<[^>]*>", "" ) // NOI18N
                               .replace( "&nbsp;", " " ) // NOI18N
                               .trim();
            }
        });

        JScrollPane sp = new JScrollPane(elementsList);
        jPanel1.add(sp);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        EditorListElement element = (EditorListElement) elementsList.getSelectedValue();
        if (element != null) {
            File file = FileUtil.toFile(element.fileObject);
            if (file != null) {
                fileChooser.setSelectedFile(file);
            } else {
                /*FileObject may not be disk file but a remote file. Following line serves only to 
                   enable open action on JFileChooser. The URL gets prefixed with current directory
                   and cannot be used to get FileObject from File
                 */
                fileChooser.setSelectedFile(new File(element.fileObject.toURL().toString()));
            }
            selectedEditorFile = element.fileObject;
        } else {
            File file = new File("");
            fileChooser.setSelectedFile(file);
            selectedEditorFile = null;
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //user has selected a different file from file chooser
        selectedEditorFile = null;
    }
    
    /**
     * @return FileObject of the editor tab chosen by the user
     */
    public final FileObject getSelectedEditorFile() {
        return selectedEditorFile;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditorBufferSelectorPanel.class, "EditorBufferSelectorPanel.jLabel1.text")); // NOI18N

        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    private class EditorListElement {
        FileObject      fileObject;
        String          displayName;
        private final boolean html;

        EditorListElement(FileObject tc, String displayName, boolean isHtml) {
            this.fileObject = tc;
            this.displayName = displayName;
            this.html = isHtml;
        }

        @Override
        public String toString() {
            return displayName;
        }
        
        public boolean isHtml () {
            return html;
        }
    }
}
