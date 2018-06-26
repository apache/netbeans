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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

public class ProjectUtil {

    private static final Logger LOGGER = Logger.getLogger(ProjectUtil.class.getName());

    public static Set<Profile> getSupportedProfiles(Project project) {
        Set<Profile> supportedProfiles = new HashSet<Profile>();
        J2eePlatform j2eePlatform = getPlatform(project);
        if (j2eePlatform != null) {
            supportedProfiles = j2eePlatform.getSupportedProfiles();
        }
        return supportedProfiles;
    }

    /**
     * Gets {@link J2eePlatform} for the given {@code Project}.
     *
     * @param project project
     * @return {@code J2eePlatform} for given project if found, {@code null} otherwise
     * @since 1.69
     */
    public static J2eePlatform getPlatform(Project project) {
        try {
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            if (provider != null) {
                String instance = provider.getServerInstanceID();
                if (instance != null) {
                    return Deployment.getDefault().getServerInstance(provider.getServerInstanceID()).getJ2eePlatform();
                }
            }
        } catch (InstanceRemovedException ex) {
            // will return null
        }
        return null;
    }

    /**
     * Is J2EE version of a given project JavaEE 5 or higher?
     *
     * @param project J2EE project
     * @return true if J2EE version is JavaEE 5 or higher; otherwise false
     */
    public static boolean isJavaEE5orHigher(Project project) {
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                J2eeModule.Type type = j2eeModule.getType();
                String strVersion = j2eeModule.getModuleVersion();
                assert strVersion != null : "Module type " + j2eeModule.getType() + " returned null module version";
                try {
                    double version = Double.parseDouble(strVersion);
                    if (J2eeModule.Type.EJB.equals(type) && (version > 2.1)) {
                        return true;
                    }
                    if (J2eeModule.Type.WAR.equals(type) && (version > 2.4)) {
                        return true;
                    }
                    if (J2eeModule.Type.CAR.equals(type) && (version > 1.4)) {
                        return true;
                    }
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.INFO, "Module version invalid " + strVersion, ex);
                }
            }
        }
        return false;
    }

}
