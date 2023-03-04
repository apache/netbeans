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

package org.netbeans.modules.hudson.ui.api;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Convenience methods for SCM implementations.
 */
public class HudsonSCMHelper {
    private static final Logger LOG = Logger.getLogger(HudsonSCM.class.getName());

        private HudsonSCMHelper() {}

        /**
         * Add an SCM polling trigger.
         * @param configXml a {@code config.xml}
         */
        public static void addTrigger(Document configXml) {
            Element root = configXml.getDocumentElement();
            root.appendChild(configXml.createElement("triggers")). // NOI18N // XXX reuse existing <triggers> if found
                    appendChild(configXml.createElement("hudson.triggers.SCMTrigger")). // NOI18N
                    appendChild(configXml.createElement("spec")). // NOI18N
                    // XXX pretty arbitrary but seems like a decent first guess
                    appendChild(configXml.createTextNode("@hourly")); // NOI18N
        }

        @Deprecated
        public static String xpath(String expr, Element xml) {
            return Utilities.xpath(expr, xml);
        }

        /**
         * Just notify the user that a diff will be shown for a given path.
         * @param path a path, probably somehow repo-relative
         */
        @NbBundle.Messages({"# {0} - portion of file path", "HudsonSCM.loading_diff=Loading diff for {0}..."})
        public static void noteWillShowDiff(String path) {
            StatusDisplayer.getDefault().setStatusText(Bundle.HudsonSCM_loading_diff(path));
        }

        /**
         * Display a diff window for a file.
         * @param before the former contents
         * @param after the new contents
         * @param path some representation of the file path
         */
        @NbBundle.Messages({"# {0} - file basename", "HudsonSCM.diffing=Diffing {0}"})
        public static void showDiff(final StreamSource before, final StreamSource after, final String path) {
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    try {
                        DiffView view = Diff.getDefault().createDiff(before, after);
                        // XXX reuse the same TC
                        DiffTopComponent tc = new DiffTopComponent(view);
                        tc.setName(path);
                        tc.setDisplayName(Bundle.HudsonSCM_diffing(path.replaceFirst(".+/", ""))); //NOI18N
                        tc.open();
                        tc.requestActive();
                    } catch (IOException x) {
                        LOG.log(Level.INFO, null, x);
                    }
                }
            });
        }
        private static class DiffTopComponent extends TopComponent {
            DiffTopComponent(DiffView view) {
                setLayout(new BorderLayout());
                add(view.getComponent(), BorderLayout.CENTER);
            }
            public @Override int getPersistenceType() {
                return TopComponent.PERSISTENCE_NEVER;
            }
            protected @Override String preferredID() {
                return "DiffTopComponent"; // NOI18N
            }
        }
}
