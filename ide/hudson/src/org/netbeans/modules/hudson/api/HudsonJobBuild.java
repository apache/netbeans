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

package org.netbeans.modules.hudson.api;

import java.util.Collection;
import org.netbeans.modules.hudson.spi.ConsoleDataDisplayerImpl;
import org.netbeans.modules.hudson.spi.FailureDataDisplayerImpl;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.openide.filesystems.FileSystem;

/**
 * Information about one build of a job.
 */
public interface HudsonJobBuild {

    public enum Result {
        SUCCESS, FAILURE, UNSTABLE, NOT_BUILT, ABORTED
    }

    HudsonJob getJob();

    int getNumber();

    Result getResult();

    String getUrl();

    boolean isBuilding();

    /**
     * Gets a changelog for the build.
     * This requires SCM-specific parsing using {@link HudsonSCM#parseChangeSet}.
     * @return a list of changes, possibly empty (including if it could not be parsed)
     */
    Collection<? extends HudsonJobChangeItem> getChanges();

    /**
     * Obtains a filesystem representing the build artifacts as accessed by Hudson web services.
     */
    FileSystem getArtifacts();

    /**
     * Gets modules contained in a Maven-type job.
     * Will be empty for non-Maven jobs.
     */
    Collection<? extends HudsonMavenModuleBuild> getMavenModules();

    /**
     * Display name comprised of job display name and build number.
     */
    String getDisplayName();

    /**
     * Check whether build console is supported by this build.
     *
     * @return True if the build console can be shown, false otherwise.
     */
    boolean canShowConsole();

    /**
     * Show console data using a displayer.
     *
     * @param displayer Displayer capable to display the console data.
     */
    void showConsole(ConsoleDataDisplayerImpl displayer);

    /**
     * Check whether build failures are supported by this build.
     *
     * @return True if build failures can be shown, false otherwise.
     */
    boolean canShowFailures();

    /**
     * Show build failures using a displayer.
     *
     * @param displayer Displayer capable to display the failure data.
     */
    void showFailures(FailureDataDisplayerImpl displayer);
}
