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
package org.netbeans.modules.j2ee.common;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider;

public class ServerUtil {

    /**
     * Checks whether the given <code>project</code>'s target server instance
     * is present.
     *
     * @param  project the project to check; can not be null.
     * @return true if the target server instance of the given project
     *          exists, false otherwise.
     *
     * @since 1.8
     */
    public static boolean isValidServerInstance(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            return false;
        }
        return isValidServerInstance(j2eeModuleProvider);
    }

    /**
     * Checks whether the given <code>provider</code>'s target server instance
     * is present.
     *
     * @param  provider the provider to check; can not be null.
     * @return true if the target server instance of the given provider
     *          exists, false otherwise.
     *
     * @since 1.10
     */
    public static boolean isValidServerInstance(J2eeModuleProvider j2eeModuleProvider) {
        String serverInstanceID = j2eeModuleProvider.getServerInstanceID();
        if (serverInstanceID == null) {
            return false;
        }
        return Deployment.getDefault().getServerID(serverInstanceID) != null;
    }

    /**
     * Default implementation of ServerStatusProvider.
     */
    public static ServerStatusProvider createServerStatusProvider(final J2eeModuleProvider j2eeModuleProvider) {
        return new ServerStatusProvider() {
            @Override
            public boolean validServerInstancePresent() {
                return ServerUtil.isValidServerInstance(j2eeModuleProvider);
            }
        };
    }

}
