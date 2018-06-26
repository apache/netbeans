/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.misc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.RequestProcessor;

public final class NpmLibrariesUsageLogger  implements PropertyChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(NpmLibrariesUsageLogger.class);

    private final PackageJson packageJson;


    private NpmLibrariesUsageLogger(Project project) {
        assert project != null;
        packageJson = new PackageJson(project.getProjectDirectory());
    }

    void startListening() {
        packageJson.addPropertyChangeListener(this);
    }

    void stopListening() {
        packageJson.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (PackageJson.PROP_DEPENDENCIES.equals(propertyName)
                || PackageJson.PROP_DEV_DEPENDENCIES.equals(propertyName)
                || PackageJson.PROP_OPTIONAL_DEPENDENCIES.equals(propertyName)
                || PackageJson.PROP_PEER_DEPENDENCIES.equals(propertyName)) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    logLibraries();
                }
            });
        }
    }

    void logLibraries() {
        PackageJson.NpmDependencies dependencies = packageJson.getDependencies();
        logLibraries("REGULAR", dependencies.dependencies); // NOI18N
        logLibraries("DEVELOPMENT", dependencies.devDependencies); // NOI18N
        logLibraries("OPTIONAL", dependencies.optionalDependencies); // NOI18N
        logLibraries("PEER", dependencies.peerDependencies); // NOI18N
    }

    private void logLibraries(String type, Map<String, String> dependencies) {
        for (Map.Entry<String, String> dependency : dependencies.entrySet()) {
            NodeJsUtils.logUsageNpmLibrary(type, dependency.getKey(), dependency.getValue());
        }
    }

    //~ Inner classes

    // we need this class mainly to instantiate usage logger (it is lazy so someone needs to lookup it from project)
    private static final class NpmProjectOpenedHook extends ProjectOpenedHook {

        private final Project project;


        public NpmProjectOpenedHook(Project project) {
            assert project != null;
            this.project = project;
        }

        @Override
        protected void projectOpened() {
            getUsageLogger().startListening();
        }

        @Override
        protected void projectClosed() {
            getUsageLogger().stopListening();
        }

        private NpmLibrariesUsageLogger getUsageLogger() {
            NpmLibrariesUsageLogger usageLogger = project.getLookup().lookup(NpmLibrariesUsageLogger.class);
            assert usageLogger != null : "Usage logger must be found in lookup of: " + project.getClass().getName();
            return usageLogger;
        }

    }

    //~ Factories

    @ProjectServiceProvider(service = NpmLibrariesUsageLogger.class, projectTypes = {
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-clientproject"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-php-project"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-project"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-maven"), // NOI18N
    })
    public static NpmLibrariesUsageLogger usageLogger(Project project) {
        return new NpmLibrariesUsageLogger(project);
    }

    @ProjectServiceProvider(service = ProjectOpenedHook.class, projectTypes = {
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-clientproject"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-php-project"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-project"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-maven"), // NOI18N
    })
    public static ProjectOpenedHook projectOpenedHook(Project project) {
        return new NpmProjectOpenedHook(project);
    }

}
