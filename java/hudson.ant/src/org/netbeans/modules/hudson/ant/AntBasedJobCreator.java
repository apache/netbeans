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
