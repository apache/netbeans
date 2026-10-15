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
package org.netbeans.modules.acp.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.acp.api.AcpToolCall;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Opens the NetBeans diff viewer for ACP tool call diffs.
 */
@NbBundle.Messages({
    "TTL_DiffOld=Original",
    "TTL_DiffNew=Modified",
    "TTL_DiffTab=Diff: {0}",
    "LBL_UnknownFile=unknown"
})
public final class AcpDiffViewer {

    private static final Logger LOG = Logger.getLogger(AcpDiffViewer.class.getName());

    private AcpDiffViewer() {
    }

    public static void showDiff(AcpToolCall.DiffContent diff) {
        SwingUtilities.invokeLater(() -> {
            try {
                String fileName = diff.path() != null ? diff.path() : Bundle.LBL_UnknownFile();
                String mimeType = guessMimeType(fileName);

                StreamSource oldSource = new StringStreamSource(
                        Bundle.TTL_DiffOld(), fileName, mimeType,
                        diff.oldText() != null ? diff.oldText() : "");
                StreamSource newSource = new StringStreamSource(
                        Bundle.TTL_DiffNew(), fileName, mimeType,
                        diff.newText() != null ? diff.newText() : "");

                DiffController controller = DiffController.createEnhanced(oldSource, newSource);
                JComponent diffComponent = controller.getJComponent();

                TopComponent tc = new TopComponent() {
                    {
                        setLayout(new java.awt.BorderLayout());
                        add(diffComponent, java.awt.BorderLayout.CENTER);
                        setDisplayName(Bundle.TTL_DiffTab(extractFileName(fileName)));
                    }
                };

                Mode editorMode = WindowManager.getDefault().findMode("editor");
                if (editorMode != null) {
                    editorMode.dockInto(tc);
                }
                tc.open();
                tc.requestActive();

                if (controller.getDifferenceCount() > 0) {
                    controller.setLocation(
                            DiffController.DiffPane.Modified,
                            DiffController.LocationType.DifferenceIndex,
                            0);
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Failed to open diff viewer", ex);
            }
        });
    }

    private static String extractFileName(String path) {
        int sep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return sep >= 0 ? path.substring(sep + 1) : path;
    }

    private static String guessMimeType(String fileName) {
        if (fileName.endsWith(".java")) {
            return "text/x-java";
        } else if (fileName.endsWith(".xml") || fileName.endsWith(".fxml")) {
            return "text/xml";
        } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".js")) {
            return "text/javascript";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
        } else if (fileName.endsWith(".properties")) {
            return "text/x-properties";
        } else if (fileName.endsWith(".py")) {
            return "text/x-python";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        }
        return "text/plain";
    }

    private static final class StringStreamSource extends StreamSource {

        private final String title;
        private final String name;
        private final String mimeType;
        private final String content;

        StringStreamSource(String title, String name, String mimeType, String content) {
            this.title = title;
            this.name = name;
            this.mimeType = mimeType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getMIMEType() {
            return mimeType;
        }

        @Override
        public Reader createReader() throws IOException {
            return new StringReader(content);
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}
