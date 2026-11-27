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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import junit.framework.TestCase;

public class AcpFileDropHandlerTest extends TestCase {

    public void testInsertsFileMentionAndSkipsDirectories() throws Exception {
        Path directory = Files.createTempDirectory("acp-drop-test");
        Path file = Files.createFile(directory.resolve("notes.txt"));
        try {
            SwingUtilities.invokeAndWait(() -> {
                JTextArea input = new JTextArea("Review");
                input.setCaretPosition(input.getDocument().getLength());
                AcpFileMentionSupport mentions = new AcpFileMentionSupport(input);
                AcpFileDropHandler handler = new AcpFileDropHandler(input, mentions);
                TransferHandler.TransferSupport drop = new TransferHandler.TransferSupport(input,
                        new DroppedFiles(List.of(directory.toFile(), file.toFile())));
                assertTrue(handler.canImport(drop));
                assertTrue(handler.importData(drop));
                List<AcpFileMentionSupport.FileAttachment> attachments = mentions.consumeAttachedFiles();
                assertEquals(1, attachments.size());
                assertEquals(file.toAbsolutePath(), Path.of(attachments.get(0).path()));
                assertTrue(input.getText(), input.getText().startsWith("Review @"));
            });
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }
    }

    public void testDelegatesPlainTextImport() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTextArea input = new JTextArea();
            input.setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(TransferSupport support) {
                    return support.isDataFlavorSupported(DataFlavor.stringFlavor);
                }

                @Override
                public boolean importData(TransferSupport support) {
                    input.setText("original transfer handler");
                    return true;
                }
            });
            AcpFileMentionSupport mentions = new AcpFileMentionSupport(input);
            AcpFileDropHandler handler = new AcpFileDropHandler(input, mentions);
            TransferHandler.TransferSupport drop = new TransferHandler.TransferSupport(input, new StringSelection("text"));
            assertTrue(handler.canImport(drop));
            assertTrue(handler.importData(drop));
            assertEquals("original transfer handler", input.getText());
            assertTrue(mentions.consumeAttachedFiles().isEmpty());
        });
    }

    private record DroppedFiles(List<File> files) implements Transferable {

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (isDataFlavorSupported(flavor)) {
                return files;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
