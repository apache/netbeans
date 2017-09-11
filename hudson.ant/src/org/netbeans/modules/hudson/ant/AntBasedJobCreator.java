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

package org.netbeans.modules.hudson.ant;

import java.util.List;
import java.util.Properties;
import org.netbeans.api.project.Project;

/**
 * Representation of targets and so on used when creating a job for an Ant-based project.
 * Searched for in global lookup.
 * Could become a friend API in the future if needed, but it's probably not needed.
 */
public interface AntBasedJobCreator {
    
    /** As in {@link AntBasedProjectType#getType}. */
    String type();

    /** Creates configuration for a given project. */
    Configuration forProject(Project project);

    interface Configuration {
        /**
         * Targets to display as potential options to build.
         * @return possibly empty list
         */
        List<Target> targets();
    }

    interface Target {
        /** Name of Ant target to run. */
        String antName();
        /** Label with optional mnemonic to display for the checkbox. */
        String labelWithMnemonic();
        /** Whether the target should by default be selected. */
        boolean selected();
        /** Whether to let the user change the selected status. */
        boolean enabled();
        /**
         * Files in the workspace which should be included in a build's artifacts if this target is run.
         * @return a patternset, or null to skip archiving
         */
        ArchivePattern artifactArchival();
        /**
         * Subdirectory in the workspace which should be included in a build's Javadoc if this target is run.
         * @return a subdirectory name, or null to skip archiving
         */
        String javadocDir();
        /**
         * Test results to collect in the build if this target is run.
         * @return a patternset, or null to skip archiving
         */
        String testResults();
        /**
         * Ant properties to set in the build.
         * @return properties ({@link Properties} syntax), or null
         */
        String properties();
    }

    interface ArchivePattern {
        /** Ant pattern(s) to include in fileset. */
        String includes();
        /** Ant pattern(s) to exclude, or null. */
        String excludes();
    }

}
