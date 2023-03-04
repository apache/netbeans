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

package org.netbeans.modules.hudson.ui.impl;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.spi.HudsonLogger;
import org.netbeans.modules.hudson.spi.HudsonLogger.HudsonLogSession;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

@ServiceProvider(service=HudsonLogger.class, position = Integer.MAX_VALUE - 100)
public class PlainLogger implements HudsonLogger {

    private static final Logger LOG = Logger.getLogger(PlainLogger.class.getName());

    @Override
    public HudsonLogSession createSession(final HudsonJob job) {
            return new HudsonLogSession() {
                final PlainLoggerLogic logic = new PlainLoggerLogic(job, job.getName());
                public boolean handle(String line, OutputWriter stream) {
                    OutputListener link = logic.findHyperlink(line);
                    if (link != null) {
                        try {
                            stream.println(line, link);
                            return true;
                        } catch (IOException x) {
                            LOG.log(Level.INFO, null, x);
                        }
                    }
                    stream.println(line);
                    return true;
                }
            };
        }

    static class PlainLoggerLogic {
        private static final Pattern REMOTE_URL = Pattern.compile("\\b(https?://[^\\s)>]+)");
        private final HudsonJob job;
        /** Looks for errors mentioning workspace files. Prefix captures Maven's [WARNING], Ant's [javac], etc. */
        private final Pattern hyperlinkable;
        PlainLoggerLogic(HudsonJob job, String jobName) {
            this.job = job;
            // XXX support Windows build servers (using backslashes)
            String jobNameQ = Pattern.quote(jobName);
            hyperlinkable = Pattern.compile("\\s*(?:\\[.+\\] )?/.+?/(?:jobs/" + jobNameQ + "/workspace|workspace/" + jobNameQ + // NOI18N
                    ")/([^:]+):(?:\\[?([0-9]+)[:,](?:([0-9]+)[]:])?)? (?:warning: )?(.+)"); // NOI18N
        }
        OutputListener findHyperlink(String line) {
            try {
                Matcher m = hyperlinkable.matcher(line);
                if (m.matches()) {
                    final String path = m.group(1);
                    final int row = m.group(2) != null ? Integer.parseInt(m.group(2)) - 1 : -1;
                    final int col = m.group(3) != null ? Integer.parseInt(m.group(3)) - 1 : -1;
                    final String message = m.group(4);
                    return new Hyperlink(job, path, message, row, col);
                }
                m = REMOTE_URL.matcher(line);
                if (m.matches()) {
                    return new URLHyperlink(new URL(m.group()));
                }
            } catch (MalformedURLException x) {
                LOG.log(Level.FINE, null, x);
            }
            return null;
        }
    }

    private static class Hyperlink implements OutputListener {

        private static final RequestProcessor RP = new RequestProcessor(Hyperlink.class);

        private final HudsonJob job;
        private final String path;
        private final String message;
        private final int row;
        private final int col;

        public Hyperlink(HudsonJob job, String path, String message, int row, int col) {
            this.job = job;
            this.path = path;
            this.message = message;
            this.row = row;
            this.col = col;
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            acted(true);
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
            acted(false);
        }

        @Messages({"# {0} - file path in workspace", "Hyperlinker.looking_for=Looking for {0}...", "# {0} - file path in workspace", "Hyperlinker.not_found=No file {0} found in remote workspace."})
        private void acted(final boolean force) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    FileObject f = null;
                    Project p = ProjectHudsonProvider.getDefault().findAssociatedProject(ProjectHudsonProvider.Association.forJob(job));
                    if (p != null) {
                        String localPath = null;
                        File localRoot = FileUtil.toFile(p.getProjectDirectory());
                        if (localRoot != null) {
                            for (HudsonSCM scm : Lookup.getDefault().lookupAll(HudsonSCM.class)) {
                                localPath = scm.translateWorkspacePath(job, path, localRoot);
                                if (localPath != null) {
                                    LOG.log(Level.FINE, "Translating remote path {0} to {1} using {2}", new Object[] {path, localPath, scm});
                                    break;
                                }
                            }
                        }
                        if (localPath == null) {
                            LOG.fine("Falling back to guess that remote workspace is a project root");
                            localPath = path;
                        }
                        // XXX permit localPath to include ../ segments; for Hg this is reasonable
                        f = p.getProjectDirectory().getFileObject(localPath);
                        LOG.log(Level.FINE, "Tried to find local file in {0} at {1} using {2}", new Object[] {p, f, localPath});
                        // XXX #159829: consider aligning local line number with remote line number somehow
                    }
                    if (f == null) {
                        StatusDisplayer.getDefault().setStatusText(Bundle.Hyperlinker_looking_for(path));
                        f = job.getRemoteWorkspace().findResource(path);
                        LOG.log(Level.FINE, "Tried to find remote file at {0} using {1}", new Object[] {f, path});
                    }
                    if (f == null) {
                        if (force) {
                        StatusDisplayer.getDefault().setStatusText(Bundle.Hyperlinker_not_found(path));
                            Toolkit.getDefaultToolkit().beep();
                        }
                        return;
                    }
                    // XXX could be useful to select this file in the workspace node (see related #159838)
                    StatusDisplayer.getDefault().setStatusText(message);
                    HudsonLoggerHelper.openAt(f, row, col, force);
                }
            });
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {}

        public @Override String toString() {
            return path + ":" + row + ":" + col + ":" + message; // NOI18N
        }
        
    }

    private static class URLHyperlink implements OutputListener {

        private final URL u;

        URLHyperlink(URL u) {
            this.u = u;
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            URLDisplayer.getDefault().showURL(u);
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {}

        @Override
        public void outputLineCleared(OutputEvent ev) {}

        public @Override String toString() {
            return u.toString();
        }

    }

}
