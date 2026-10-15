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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;

/**
 * Navigates the editor to a file path, optionally scrolling to a specific line.
 * The {@link #navigateTo} method may be called from any thread; it schedules
 * the navigation on the Event Dispatch Thread.
 */
public final class AcpLocationNavigator {

    private static final Logger LOG = Logger.getLogger(AcpLocationNavigator.class.getName());

    private AcpLocationNavigator() {
    }

    /**
     * Opens the file in the NetBeans editor and, if {@code lineNumber} is
     * provided, scrolls to that line and requests focus.
     *
     * @param path       absolute or project-relative file path
     * @param lineNumber 1-based line number, or {@code null} to just open
     */
    public static void navigateTo(String path, Integer lineNumber) {
        SwingUtilities.invokeLater(() -> {
            File file = new File(path);
            if (!file.isAbsolute()) {
                // best-effort resolution: try relative to the current directory
                file = file.getAbsoluteFile();
            }
            org.openide.filesystems.FileObject fo =
                    FileUtil.toFileObject(FileUtil.normalizeFile(file));
            if (fo == null) {
                LOG.log(Level.WARNING, "navigateTo: file not found: {0}", path);
                return;
            }
            try {
                DataObject dob = DataObject.find(fo);
                if (lineNumber != null && lineNumber > 0) {
                    LineCookie lc = dob.getLookup().lookup(LineCookie.class);
                    if (lc != null) {
                        // LineCookie uses 0-based line indices
                        Line line = lc.getLineSet().getCurrent(lineNumber - 1);
                        line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                        return;
                    }
                }
                OpenCookie open = dob.getLookup().lookup(OpenCookie.class);
                if (open != null) {
                    open.open();
                } else {
                    LOG.log(Level.WARNING, "navigateTo: no OpenCookie for: {0}", path);
                }
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.WARNING, "navigateTo: DataObject not found for: " + path, ex);
            }
        });
    }

    /**
     * Parses an {@code acp-nav:} hyperlink URL and delegates to
     * {@link #navigateTo(String, Integer)}.
     * <p>
     * URL format: {@code acp-nav:<path>} or {@code acp-nav:<path>:<line>}
     * where {@code <line>} is the last colon-delimited token and is a positive
     * integer.
     *
     * @param url the raw hyperlink description starting with {@code acp-nav:}
     */
    public static void handleNavUrl(String url) {
        if (url == null || !url.startsWith("acp-nav:")) { // NOI18N
            return;
        }
        String rest = url.substring("acp-nav:".length()); // NOI18N
        Integer lineNumber = null;
        String path = rest;

        int lastColon = rest.lastIndexOf(':');
        if (lastColon > 0) {
            String afterColon = rest.substring(lastColon + 1);
            try {
                int n = Integer.parseInt(afterColon);
                if (n > 0) {
                    lineNumber = n;
                    path = rest.substring(0, lastColon);
                }
            } catch (NumberFormatException ex) {
                // no line number — treat the whole thing as the path
            }
        }
        navigateTo(path, lineNumber);
    }
}
