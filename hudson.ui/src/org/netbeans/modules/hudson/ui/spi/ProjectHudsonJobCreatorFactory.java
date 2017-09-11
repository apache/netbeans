/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.spi;

import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.spi.HudsonSCM.ConfigurationStatus;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Service representing the ability to create Hudson jobs for local projects.
 */
public interface ProjectHudsonJobCreatorFactory {

    /**
     * Checks whether this creator can handle a given project.
     * Should return as quickly as possible, i.e. just check basic project type.
     * @param project a local project
     * @return a factory for further work, or null if the project type is not handled
     */
    ProjectHudsonJobCreator forProject(Project project);

    /**
     * Callback to manage creation of the job for a particular project.
     */
    interface ProjectHudsonJobCreator {

        /**
         * Produces a suggested Hudson job name for a project.
         * This might for example use {@link ProjectInformation#getName}.
         * The actual job which gets created might have a uniquified name.
         * @return a proposed code name for the project as a Hudson job
         */
        String jobName();

        /**
         * Provides specialized GUI for configuring options (beyond the project name).
         * @return a customization panel
         */
        JComponent customizer();

        /**
         * Checks whether current configuration seems valid.
         * @return a status including potential error messages
         */
        ConfigurationStatus status();

        /**
         * Adds listener to change in validity.
         * @param listener a listener
         */
        void addChangeListener(ChangeListener listener);

        /**
         * Removes listener to change in validity.
         * @param listener a listener
         */
        void removeChangeListener(ChangeListener listener);

        /**
         * Provides the desired initial configuration for a project.
         * @return a document initially consisting of just {@code <project/>}
         *         to be populated with subelements
         *         following the format of {@code ${workdir}/jobs/${projname}/config.xml}
         * @throws IOException in case project metadata cannot be read or is
         * malformed. Use {@link SilentIOException} to suppress showing error
         * message.
         * @see Helper
         */
        Document configure() throws IOException;

        /**
         * Exception of this class can be thrown from
         * {@link ProjectHudsonJobCreator#configure()} if no error message
         * should be shown to the user. E.g. if some custom dialog has been
         * already displayed.
         *
         * @since 1.23
         */
        public static class SilentIOException extends IOException {

            public SilentIOException() {
            }

            public SilentIOException(String message) {
                super(message);
            }

            public SilentIOException(String message, Throwable cause) {
                super(message, cause);
            }

            public SilentIOException(Throwable cause) {
                super(cause);
            }
        }
    }

    /**
     * Utilities which can be used by {@link ProjectHudsonJobCreator#configure}.
     */
    public final class Helper {

        private Helper() {}

        /**
         * Prepares to add version control information appropriate to the project's basedir.
         * @param basedir the root directory of the source project
         * @return an SCM configuration, or null if there is no known associated SCM
         */
        public static HudsonSCM.Configuration prepareSCM(File basedir) {
            for (HudsonSCM scm : Lookup.getDefault().lookupAll(HudsonSCM.class)) {
                HudsonSCM.Configuration cfg = scm.forFolder(basedir);
                if (cfg != null) {
                    return cfg;
                }
            }
            return null;
        }

        /**
         * @return error message for {@link ProjectHudsonJobCreator#error} in case {@link #prepareSCM} is null
         */
        @Messages("ProjectHudsonJobCreatorFactory.no_vcs=The project does not use any supported version control system.")
        public static ConfigurationStatus noSCMError() {
            return ConfigurationStatus.withError(Bundle.ProjectHudsonJobCreatorFactory_no_vcs());
        }

        /**
         * Adds instruction to keep only the last successful build.
         * @param configXml a {@code config.xml} to which {@code <logRotator>} will be appended
         */
        public static void addLogRotator(Document configXml) {
            Element lr = (Element) configXml.getDocumentElement().appendChild(
                    configXml.createElement("logRotator")); // NOI18N
            lr.appendChild(configXml.createElement("daysToKeep")). // NOI18N
                    appendChild(configXml.createTextNode("-1")); // NOI18N
            lr.appendChild(configXml.createElement("numToKeep")). // NOI18N
                    appendChild(configXml.createTextNode("1")); // NOI18N
        }

    }

}
