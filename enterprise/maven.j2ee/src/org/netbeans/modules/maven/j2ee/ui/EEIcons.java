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

package org.netbeans.modules.maven.j2ee.ui;

import javax.swing.Icon;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.nodes.SpecialIcon;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ImageUtilities;

public abstract class EEIcons implements SpecialIcon {

    @ProjectServiceProvider(service = SpecialIcon.class, projectType = "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR)
    public static class WarIcon extends EEIcons {

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/maven/j2ee/ui/resources/WebIcon.png", true);
        }
    }

    @ProjectServiceProvider(service = SpecialIcon.class, projectType = "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB)
    public static class EjbIcon extends EEIcons {

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/maven/j2ee/ui/resources/EjbIcon.png", true);
        }
    }

    @ProjectServiceProvider(service = SpecialIcon.class, projectType = "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR)
    public static class EarIcons extends EEIcons {

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/maven/j2ee/ui/resources/EarIcon.png", true);
        }
    }

    @ProjectServiceProvider(service = SpecialIcon.class, projectType = "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT)
    public static class AppClientIcons extends EEIcons {

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/maven/j2ee/ui/resources/AppClientIcon.png", true);

        }
    }
}
