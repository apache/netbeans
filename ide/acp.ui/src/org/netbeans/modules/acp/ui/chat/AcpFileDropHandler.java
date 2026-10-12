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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;

/**
 * Handles drag-and-drop of files from the Projects/Files window into the
 * ACP chat input area. Dropped files are inserted as @-mentions and their
 * content is queued for attachment.
 */
final class AcpFileDropHandler extends TransferHandler {

    private static final Logger LOG = Logger.getLogger(AcpFileDropHandler.class.getName());

    private final JTextArea inputArea;
    private final AcpFileMentionSupport mentionSupport;
    private final TransferHandler originalHandler;

    AcpFileDropHandler(JTextArea inputArea, AcpFileMentionSupport mentionSupport) {
        this.inputArea = inputArea;
        this.mentionSupport = mentionSupport;
        this.originalHandler = inputArea.getTransferHandler();
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return true;
        }
        if (hasNodeDndFlavor(support.getDataFlavors())) {
            return true;
        }
        if (originalHandler != null) {
            return originalHandler.canImport(support);
        }
        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {
        List<FileObject> fileObjects = extractFileObjects(support.getTransferable());
        if (!fileObjects.isEmpty()) {
            insertFileReferences(fileObjects);
            return true;
        }
        if (originalHandler != null) {
            return originalHandler.importData(support);
        }
        return false;
    }

    @Override
    public void exportToClipboard(javax.swing.JComponent comp,
            java.awt.datatransfer.Clipboard clip, int action)
            throws IllegalStateException {
        if (originalHandler != null) {
            originalHandler.exportToClipboard(comp, clip, action);
        }
    }

    private void insertFileReferences(List<FileObject> fileObjects) {
        StringBuilder sb = new StringBuilder();
        for (FileObject fo : fileObjects) {
            if (!fo.isData()) {
                continue;
            }
            String name = fo.getNameExt();
            String path = fo.getPath();
            sb.append("@").append(name).append(" ");
            mentionSupport.addAttachment(path, name);
        }
        if (sb.length() > 0) {
            int pos = inputArea.getCaretPosition();
            String existing = inputArea.getText();
            if (pos > 0 && pos <= existing.length()
                    && !Character.isWhitespace(existing.charAt(pos - 1))) {
                sb.insert(0, " ");
            }
            inputArea.insert(sb.toString(), pos);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<FileObject> extractFileObjects(Transferable t) {
        List<FileObject> result = new ArrayList<>();

        try {
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                for (File f : files) {
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                    if (fo != null && fo.isData()) {
                        result.add(fo);
                    }
                }
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            LOG.log(Level.FINE, "Cannot extract javaFileListFlavor", ex);
        }

        if (result.isEmpty()) {
            Node[] nodes = NodeTransfer.nodes(
                    t, NodeTransfer.COPY | NodeTransfer.MOVE);
            if (nodes != null) {
                for (Node node : nodes) {
                    DataObject dobj = node.getLookup().lookup(DataObject.class);
                    if (dobj != null) {
                        FileObject fo = dobj.getPrimaryFile();
                        if (fo.isData()) {
                            result.add(fo);
                        }
                    }
                }
            }
        }

        return result;
    }

    private static final String NODE_DND_MIME_SUBTYPE = "x-java-openide-nodednd"; // NOI18N
    private static final String NODE_DND_MIME_PRIMARY = "application"; // NOI18N

    private static boolean hasNodeDndFlavor(DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (NODE_DND_MIME_PRIMARY.equals(flavor.getPrimaryType())
                    && NODE_DND_MIME_SUBTYPE.equals(flavor.getSubType())) {
                return true;
            }
        }
        return false;
    }
}
