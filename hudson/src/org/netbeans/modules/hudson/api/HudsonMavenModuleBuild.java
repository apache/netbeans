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
