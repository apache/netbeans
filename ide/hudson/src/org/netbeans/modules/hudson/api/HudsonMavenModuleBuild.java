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

import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.spi.ConsoleDataDisplayerImpl;
import org.netbeans.modules.hudson.spi.FailureDataDisplayerImpl;
import org.openide.filesystems.FileSystem;

/**
 * Represents a build of one Maven module built as part of a Maven moduleset job.
 */
public interface HudsonMavenModuleBuild {

    /**
     * Maven name in the format {@code group.id:modulename}.
     */
    String getName();

    /**
     * Display name.
     */
    String getDisplayName();

    /**
     * Status of this one module.
     * (Tests can fail in some modules but not others.)
     */
    Color getColor();

    /**
     * URL to this module.
     */
    String getUrl();

    /**
     * The moduleset build in which this module can be found.
     */
    HudsonJobBuild getBuild();

    /**
     * Obtains a filesystem representing the build artifacts as accessed by Hudson web services.
     */
    FileSystem getArtifacts();

    /**
     * Display name comprised of module display name and build number.
     */
    String getBuildDisplayName();

    /**
     * Check whether build console is supported by this module build.
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
     * Check whether build failures are supported by this module build.
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
