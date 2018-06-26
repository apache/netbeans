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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;

/**
 * The interface that should serverplugin should implement in order to
 * support the server library management.
 *
 * @since 1.68
 * @author Petr Hejl
 * @see org.netbeans.modules.j2ee.deployment.plugins.spi.config.ServerLibraryConfiguration
 */
public interface ServerLibraryManager {

    /**
     * Returns the set of libraries the server has access to and can be deployed
     * on request (by call to {@link #deployRequiredLibraries(java.util.Set)}.
     *
     * @return the set of libraries which can be deployed on server
     */
    @NonNull
    Set<ServerLibrary> getDeployableLibraries();

    /**
     * Returns the set of libraries already deployed to the server.
     *
     * @return the set of libraries already deployed to the server
     */
    @NonNull
    Set<ServerLibrary> getDeployedLibraries();

    @NonNull
    Set<ServerLibraryDependency> getMissingDependencies(
            @NonNull Set<ServerLibraryDependency> dependencies);

    @NonNull
    Set<ServerLibraryDependency> getDeployableDependencies(
            @NonNull Set<ServerLibraryDependency> dependencies);

    /**
     * Deploys all the required libraries passed to the method. The libraries
     * passed to the method may be already deployed and it is up to implementor
     * to handle such case.
     *
     * @param libraries the libraries to deploy
     * @throws ConfigurationException if there was a problem during
     *             the deployment
     */
    void deployLibraries(@NonNull Set<ServerLibraryDependency> libraries)
            throws ConfigurationException;

    /**
     * Exception there are missing libraries which cannot be deployed.
     *
     * @since 1.105
     */
    public static class MissingLibrariesException extends ConfigurationException {

        private final Set<ServerLibraryDependency> missingLibraries;

        public MissingLibrariesException(String message, Set<ServerLibraryDependency> missingLibraries) {
            super(message);
            this.missingLibraries = missingLibraries;
        }

        public Set<ServerLibraryDependency> getMissingLibraries() {
            return missingLibraries;
        }
    }
}
