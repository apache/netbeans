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

package org.netbeans.modules.hudson.spi;

import java.io.File;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.w3c.dom.Document;

/**
 * Represents one kind of SCM (version control) supported by the Hudson integration.
 * Registered to global lookup.
 */
public interface HudsonSCM {

    /**
     * Possibly recognizes a disk folder as being under version control.
     * @param folder a disk folder which may or may not be versioned
     * @return information about its versioning, or null if unrecognized
     */
    Configuration forFolder(File folder);

    /**
     * Information about how a folder (such as the basedir of a project) is versioned.
     */
    interface Configuration {

        /**
         * Creates configuration for Hudson.
         * Would typically append a {@code <scm>} element.
         * @param configXml Hudson's {@code config.xml}
         */
        void configure(Document configXml);

        /**
         * Optional problem with the SCM configuration.
         * @return a problem (warning or error), or null
         */
        ConfigurationStatus problems();

        // XXX should permit SCM to say that project is in a specific subdir

    }

    /**
     * A problem with the SCM configuration.
     */
    public static final class ConfigurationStatus {

        private String errorMessage;
        private String warningMessage;
        private JButton extraButton;

        private ConfigurationStatus() {
        }

        /**
         * Creates a valid configuration.
         */
        public static ConfigurationStatus valid() {
            return new ConfigurationStatus();
        }

        /**
         * Creates a configuration with a fatal error.
         */
        public static ConfigurationStatus withError(String error) {
            ConfigurationStatus s = new ConfigurationStatus();
            s.errorMessage = error;
            return s;
        }

        /**
         * Creates a configuration with a nonfatal warning.
         */
        public static ConfigurationStatus withWarning(String warning) {
            ConfigurationStatus s = new ConfigurationStatus();
            s.warningMessage = warning;
            return s;
        }

        /**
         * Creates a similar configuration but with an extra button added to the
         * dialog.
         *
         * @see NotifyDescriptor#setAdditionalOptions
         */
        public ConfigurationStatus withExtraButton(JButton extraButton) {
            if (this.extraButton != null) {
                throw new IllegalArgumentException();
            }
            ConfigurationStatus s = new ConfigurationStatus();
            s.errorMessage = errorMessage;
            s.warningMessage = warningMessage;
            s.extraButton = extraButton;
            return s;
        }

        /**
         * for internal use only
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * for internal use only
         */
        public String getWarningMessage() {
            return warningMessage;
        }

        /**
         * for internal use only
         */
        public JButton getExtraButton() {
            return extraButton;
        }
    }

    /**
     * Attempts to convert a path in a remote Hudson workspace to a local file path.
     * May use SCM information to guess at how these paths should be aligned.
     * @param job a Hudson job
     * @param workspacePath a relative path within the job's remote workspace, e.g. {@code src/p/C.java}
     * @param localRoot a local disk root to consider as a starting point
     * @return a file within {@code localRoot} corresponding to {@code workspacePath}, or null if unknown
     */
    String translateWorkspacePath(HudsonJob job, String workspacePath, File localRoot);

    /**
     * Attempts to parse a build's changelog.
     * Will generally use {@code ?tree=changeSet[...]} from the remote API
     * corresponding to some {@code hudson.scm.ChangeLogSet} subtype.
     * (Cannot unconditionally parse this subtree due to HUDSON-8994;
     * cannot rely on {@code ChangeLogSet.getKind} due to HUDSON-8995.)
     * @param build the build
     * @return a list of parsed changelog items, or null if the SCM is unrecognized
     */
    List<? extends HudsonJobChangeItem> parseChangeSet(HudsonJobBuild build);
}
