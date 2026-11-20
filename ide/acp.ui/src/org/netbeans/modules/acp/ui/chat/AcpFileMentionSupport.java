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
package org.netbeans.modules.acp.ui.chat;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Provides @-mention file completion for the ACP chat input area.
 * When the user types '@', a popup lists open files for quick insertion.
 */
final class AcpFileMentionSupport {

    private static final Logger LOG = Logger.getLogger(AcpFileMentionSupport.class.getName());

    private final JTextArea inputArea;
    private final List<FileAttachment> attachedFiles = new ArrayList<>();
    private JPopupMenu popup;
    private JList<FileEntry> fileList;
    private int mentionStartOffset = -1;

    AcpFileMentionSupport(JTextArea inputArea) {
        this.inputArea = inputArea;
        setupListeners();
    }

    private void setupListeners() {
        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (e.getLength() == 1) {
                    SwingUtilities.invokeLater(() -> checkForMentionTrigger(e.getOffset()));
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (popup != null && popup.isVisible()) {
                        updateFilter();
                    }
                });
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (popup != null && popup.isVisible()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_DOWN -> {
                            int idx = fileList.getSelectedIndex();
                            if (idx < fileList.getModel().getSize() - 1) {
                                fileList.setSelectedIndex(idx + 1);
                                fileList.ensureIndexIsVisible(idx + 1);
                            }
                            e.consume();
                        }
                        case KeyEvent.VK_UP -> {
                            int idx = fileList.getSelectedIndex();
                            if (idx > 0) {
                                fileList.setSelectedIndex(idx - 1);
                                fileList.ensureIndexIsVisible(idx - 1);
                            }
                            e.consume();
                        }
                        case KeyEvent.VK_ENTER, KeyEvent.VK_TAB -> {
                            selectCurrentItem();
                            e.consume();
                        }
                        case KeyEvent.VK_ESCAPE -> {
                            hidePopup();
                            e.consume();
                        }
                    }
                }
            }
        });
    }

    private void checkForMentionTrigger(int offset) {
        try {
            String inserted = inputArea.getDocument().getText(offset, 1);
            if ("@".equals(inserted)) {
                boolean atStart = offset == 0;
                boolean afterSpace = !atStart
                        && Character.isWhitespace(inputArea.getDocument().getText(offset - 1, 1).charAt(0));
                if (atStart || afterSpace) {
                    mentionStartOffset = offset;
                    showFilePopup("");
                }
            } else if (popup != null && popup.isVisible()) {
                updateFilter();
            }
        } catch (BadLocationException ex) {
            LOG.log(Level.FINE, "Bad location in mention check", ex);
        }
    }

    private void updateFilter() {
        if (mentionStartOffset < 0) {
            return;
        }
        try {
            int caretPos = inputArea.getCaretPosition();
            if (caretPos <= mentionStartOffset) {
                hidePopup();
                return;
            }
            String filter = inputArea.getDocument().getText(
                    mentionStartOffset + 1, caretPos - mentionStartOffset - 1);
            showFilePopup(filter);
        } catch (BadLocationException ex) {
            hidePopup();
        }
    }

    private void showFilePopup(String filter) {
        List<FileEntry> files = collectOpenFiles();
        String lowerFilter = filter.toLowerCase();
        List<FileEntry> filtered = files.stream()
                .filter(f -> lowerFilter.isEmpty()
                        || f.displayName.toLowerCase().contains(lowerFilter)
                        || f.path.toLowerCase().contains(lowerFilter))
                .toList();

        if (filtered.isEmpty()) {
            hidePopup();
            return;
        }

        DefaultListModel<FileEntry> model = new DefaultListModel<>();
        filtered.forEach(model::addElement);

        if (popup == null) {
            fileList = new JList<>();
            fileList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        selectCurrentItem();
                    }
                }
            });
            popup = new JPopupMenu();
            JScrollPane sp = new JScrollPane(fileList);
            popup.add(sp);
        }

        fileList.setModel(model);
        fileList.setSelectedIndex(0);
        popup.setPopupSize(400, Math.min(filtered.size() * 22 + 4, 200));

        try {
            int caretPos = inputArea.getCaretPosition();
            @SuppressWarnings("deprecation")
            java.awt.Rectangle rect = inputArea.modelToView(caretPos);
            if (rect != null) {
                Point p = new Point(rect.x, rect.y);
                popup.show(inputArea, p.x, p.y - popup.getPreferredSize().height);
            }
        } catch (BadLocationException ex) {
            LOG.log(Level.FINE, "Bad location showing popup", ex);
        }
    }

    private void selectCurrentItem() {
        FileEntry selected = fileList.getSelectedValue();
        if (selected == null || mentionStartOffset < 0) {
            hidePopup();
            return;
        }
        try {
            int caretPos = inputArea.getCaretPosition();
            inputArea.getDocument().remove(mentionStartOffset, caretPos - mentionStartOffset);
            String insertion = "@" + selected.displayName + " ";
            inputArea.getDocument().insertString(mentionStartOffset, insertion, null);

            attachedFiles.add(new FileAttachment(selected.path, selected.displayName));
        } catch (BadLocationException ex) {
            LOG.log(Level.FINE, "Bad location inserting mention", ex);
        }
        hidePopup();
    }

    private void hidePopup() {
        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
        }
        mentionStartOffset = -1;
    }

    void addAttachment(String path, String displayName) {
        attachedFiles.add(new FileAttachment(path, displayName));
    }

    List<FileAttachment> consumeAttachedFiles() {
        List<FileAttachment> result = new ArrayList<>(attachedFiles);
        attachedFiles.clear();
        return result;
    }

    String buildAttachmentContext(List<FileAttachment> files) {
        if (files.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (FileAttachment fa : files) {
            sb.append("Referenced file: ").append(fa.path).append("\n");
            String content = readFileContent(fa.path);
            if (content != null) {
                sb.append("```\n").append(content).append("\n```\n\n");
            }
        }
        return sb.toString();
    }

    private static String readFileContent(String path) {
        FileObject fo = FileUtil.toFileObject(new java.io.File(path));
        if (fo == null || !fo.isData()) {
            return null;
        }
        try {
            return fo.asText();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to read file: " + path, ex);
            return null;
        }
    }

    private static List<FileEntry> collectOpenFiles() {
        Set<FileEntry> entries = new LinkedHashSet<>();

        for (JTextComponent comp : EditorRegistry.componentList()) {
            Object sdp = comp.getDocument().getProperty(
                    javax.swing.text.Document.StreamDescriptionProperty);
            if (sdp instanceof DataObject dataObj) {
                FileObject fo = dataObj.getPrimaryFile();
                entries.add(new FileEntry(fo.getNameExt(), fo.getPath()));
            }
        }

        for (Mode mode : WindowManager.getDefault().getModes()) {
            for (TopComponent tc : mode.getTopComponents()) {
                if (tc.isOpened()) {
                    org.openide.util.Lookup lkp = tc.getLookup();
                    DataObject dobj = lkp.lookup(DataObject.class);
                    if (dobj != null) {
                        FileObject fo = dobj.getPrimaryFile();
                        entries.add(new FileEntry(fo.getNameExt(), fo.getPath()));
                    }
                }
            }
        }

        return new ArrayList<>(entries);
    }

    record FileEntry(String displayName, String path) {
        @Override
        public String toString() {
            return displayName + "  (" + path + ")";
        }
    }

    record FileAttachment(String path, String displayName) {
    }
}
