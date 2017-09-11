/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
