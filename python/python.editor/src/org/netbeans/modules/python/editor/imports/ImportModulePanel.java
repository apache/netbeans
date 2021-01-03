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
package org.netbeans.modules.python.editor.imports;

import org.netbeans.modules.python.source.ImportManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.python.source.PythonIndex;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.lexer.Call;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * This file is originally from Retouche, the Java Support
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible.
 *
 * (This used to be ImportClassPanel in org.netbeans.modules.java.editor.imports)
 *
 */
public class ImportModulePanel extends javax.swing.JPanel {

    private final String ident;
    private PythonParserResult info;
    private DefaultListModel model;
    private final int position;

    /** Creates new form ImportClassPanel */
    @SuppressWarnings("deprecation")
    public ImportModulePanel(String ident, List</*TypeElement*/String> priviledged, List</*TypeElement*/String> denied, Font font, PythonParserResult info, int position) {
        this.ident = ident;
        // System.err.println("priviledged=" + priviledged);
        // System.err.println("denied=" + denied);
        this.info = info;
        this.position = position;
        createModel(priviledged, denied);

        initComponents();
        setBackground(jList1.getBackground());

        if (model.size() > 0) {
            jList1.setModel(model);
            setFocusable(false);
            setNextFocusableComponent(jList1);
            jScrollPane1.setBackground(jList1.getBackground());
            setBackground(jList1.getBackground());
            if (font != null) {
                jList1.setFont(font);
            }
            int modelSize = jList1.getModel().getSize();
            if (modelSize > 0) {
                jList1.setSelectedIndex(0);
            }
            jList1.setVisibleRowCount(modelSize > 8 ? 8 : modelSize);
            jList1.setCellRenderer(new Renderer(jList1));
            jList1.grabFocus();
        } else {
            remove(jScrollPane1);
            JLabel nothingFoundJL = new JLabel(NbBundle.getMessage(ImportModulePanel.class, "NoModsFound"));
            if (font != null) {
                nothingFoundJL.setFont(font);
            }
            nothingFoundJL.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 4, 4));
            nothingFoundJL.setEnabled(false);
            nothingFoundJL.setBackground(jList1.getBackground());
            //nothingFoundJL.setOpaque(true);
            add(nothingFoundJL);
        }

        setA11Y();
        updatePreview(null);
    }

    private void setA11Y() {
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ImportModulePanel.class, "ImportClassPanel_ACN"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportModulePanel.class, "ImportClassPanel_ACSD"));
        jList1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ImportModulePanel.class, "ImportClassPanel_JList1_ACN"));
        jList1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportModulePanel.class, "ImportClassPanel_JList1_ACSD"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        ctrlLabel = new javax.swing.JLabel();
        importPreviewLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(64, 64, 64)));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 4, 4));

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listMouseReleased(evt);
            }
        });
        jList1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                updatePreview(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                listKeyReleased(evt);
                updatePreview(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setLabelFor(jList1);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ImportModulePanel.class, "ModuleToImport")); // NOI18N
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jLabel1.setOpaque(true);
        add(jLabel1, java.awt.BorderLayout.PAGE_START);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));

        ctrlLabel.setText(org.openide.util.NbBundle.getMessage(ImportModulePanel.class, "LBL_PackageImport")); // NOI18N

        importPreviewLabel.setText(" ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ctrlLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
            .addComponent(importPreviewLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(ctrlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importPreviewLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void listMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseReleased
        boolean packageImport = (evt.getModifiers() & InputEvent.ALT_MASK) > 0;
        boolean useFqn = (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0;
        importModule(getSelected(), packageImport, useFqn);
    }//GEN-LAST:event_listMouseReleased

    private void listKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listKeyReleased
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(evt);
        if (ks.getKeyCode() == KeyEvent.VK_ENTER ||
                ks.getKeyCode() == KeyEvent.VK_SPACE) {
            boolean packageImport = (evt.getModifiers() & InputEvent.ALT_MASK) > 0;
            boolean useFqn = (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0;
            importModule(getSelected(), packageImport, useFqn);
        }
    }//GEN-LAST:event_listKeyReleased

    private void updatePreview(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updatePreview
        boolean packageImport = evt != null && (evt.getModifiers() & InputEvent.ALT_MASK) > 0;
        boolean useFqn = evt != null && (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0;

        Object selected = jList1.getSelectedValue();
        if (selected != null) {
            String module = ((TypeDescription)selected).qualifiedName;
            // Strip off list symbol
            int colon = module.indexOf(':');
            if (colon != -1) {
                module = module.substring(0, colon);
            }

            String preview;
            if (PythonIndex.isBuiltinModule(module)) {
                preview = "<html><i>" + NbBundle.getMessage(ImportModulePanel.class, "ModuleBuiltin", module) + "</i></html>"; // NOI18N
            } else if (module.equals(ident)) {
                preview = "<html><code><b>import " + module + "</b></code></html>"; // NOI18N
            } else if (packageImport) {
                preview = "<html><code><b>from " + module + " import *</b></code></html>"; // NOI18N
            } else if (useFqn) {
                preview = "<html><code><b>import " + module + "</b>;    ....  <b>" + module + ".</b>" + ident + "...</code></html>"; // NOI18N
            } else {
                preview = "<html><code><b>from " + module + " import " + ident + "</b></code></html>"; // NOI18N
            }
            importPreviewLabel.setText(preview);
        }
    }//GEN-LAST:event_updatePreview

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel ctrlLabel;
    public javax.swing.JLabel importPreviewLabel;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JList jList1;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables


    public String getSelected() {
        TypeDescription typeDescription = ((TypeDescription) jList1.getSelectedValue());
        return typeDescription == null ? null : typeDescription.qualifiedName;
    }

    private void createModel(List<String> priviledged, List<String> denied) {

        List<TypeDescription> l = new ArrayList<>(priviledged.size());
        for (String typeElement : priviledged) {
            l.add(new TypeDescription(typeElement, false));
        }
        for (String typeElement : denied) {
            l.add(new TypeDescription(typeElement, true));
        }

        Collections.sort(l);

        model = new DefaultListModel();
        for (TypeDescription td : l) {
            model.addElement(td);
        }
    }

    private void importModule(String name, boolean packageImport, boolean useFqn) {
        if (name != null) {
            // Strip off list symbol
            int colon = name.indexOf(':');
            if (colon != -1) {
                name = name.substring(0, colon);
            }
        }
        PopupUtil.hidePopup();

        Document document = info.getSnapshot().getSource().getDocument(false);
        try {
            Position pos = document.createPosition(position);

            new ImportManager(info).ensureImported(name, name.equals(ident) ? null : ident, packageImport, useFqn, true);

            if (useFqn && ident != null) {
                BaseDocument doc = (BaseDocument)document;
                TokenHierarchy<Document> th = TokenHierarchy.get(document);
                int offset = pos.getOffset();
                Call call = Call.getCallType(doc, th, offset);
                if (!name.equals(ident)) {
                    if (call.getLhs() == null) {
                        // Unqualified usage of a symbol
                        int start = Utilities.getWordStart(doc, offset);
                        // TODO - compute correct location
                        if (start >= 0) {
                            doc.insertString(start, name + ".", null);
                        }
                    }
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static class Renderer extends DefaultListCellRenderer {

        private static int DARKER_COLOR_COMPONENT = 5;
        private static int LIGHTER_COLOR_COMPONENT = DARKER_COLOR_COMPONENT;
        private Color denidedColor = new Color(0x80, 0x80, 0x80);
        private Color fgColor;
        private Color bgColor;
        private Color bgColorDarker;
        private Color bgSelectionColor;
        private Color fgSelectionColor;

        public Renderer(JList list) {
            setFont(list.getFont());
            fgColor = list.getForeground();
            bgColor = list.getBackground();
            bgColorDarker = new Color(
                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT));
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean hasFocus) {

            if (isSelected) {
                setForeground(fgSelectionColor);
                setBackground(bgSelectionColor);
            } else {
                setForeground(fgColor);
                setBackground(index % 2 == 0 ? bgColor : bgColorDarker);
            }

            if (value instanceof TypeDescription) {
                TypeDescription td = (TypeDescription) value;
                // setIcon(td.getIcon());
                setText(td.qualifiedName);
                if (td.isDenied) {
                    setForeground(denidedColor);
                }
                // TODO - depend on gsf directly and get icons from there!
                //setIcon( ElementIcons.getElementIcon( td.kind, null ) );
                setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/python/editor/imports/module.png", false)); // NOI18N
            } else {
                setText(value.toString());
            }

            return this;
        }
    }

    private static class TypeDescription implements Comparable<TypeDescription> {

        private boolean isDenied;
        private final ElementKind kind;
        private final String qualifiedName;

        public TypeDescription(String typeElement, boolean isDenied) {
            this.isDenied = isDenied;
            //this.kind = typeElement.getKind();
            //this.qualifiedName = typeElement.getQualifiedName().toString();
            this.kind = ElementKind.MODULE;
            this.qualifiedName = typeElement;
        }

        @Override
        public int compareTo(TypeDescription o) {

            if (isDenied && !o.isDenied) {
                return 1;
            } else if (!isDenied && o.isDenied) {
                return -1;
            } else {
                return qualifiedName.compareTo(o.qualifiedName);
            }
        }
    }
}
