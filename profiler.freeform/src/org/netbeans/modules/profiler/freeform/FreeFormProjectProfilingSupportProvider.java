/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.profiler.freeform;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.nbimpl.project.JavaProjectProfilingSupportProvider;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 * A class providing basic support for profiling free-form projects.
 *
 * @author Ian Formanek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider.class, 
                        projectTypes={@ProjectType(id="org-netbeans-modules-ant-freeform", position=1210)}) // NOI18N
public final class FreeFormProjectProfilingSupportProvider extends JavaProjectProfilingSupportProvider {
    private static final String NBJDK_PROPERTIES = "nbproject/nbjdk.properties"; // NOI18N
    private static final String NBJDK_ACTIVE = "nbjdk.active"; // NOI18N

    @Override
    public boolean checkProjectCanBeProfiled(final FileObject profiledClassFile) {
        Project project = getProject();
        Element e = ProjectUtils.getAuxiliaryConfiguration(project).getConfigurationFragment("data", // NOI18N
                ProjectUtilities.PROFILER_NAME_SPACE, false);

        if (e != null) {
            final String profileTarget = e.getAttribute(FreeFormProjectsSupport.PROFILE_TARGET_ATTRIBUTE);
            final String profileSingleTarget = e.getAttribute(FreeFormProjectsSupport.PROFILE_SINGLE_TARGET_ATTRIBUTE);
            
            if (profileTarget == null && profileSingleTarget == null) return false;
        } else {
            if (!FreeFormProjectsSupport.saveProfilerConfig(project, null, null)) return false;
        }

        return super.checkProjectCanBeProfiled(profiledClassFile);
    }

    @Override
    public JavaPlatform resolveProjectJavaPlatform() {
        ProjectAccessor acc = getProject().getLookup().lookup(ProjectAccessor.class);
        EditableProperties ep = acc.getHelper().getProperties(NBJDK_PROPERTIES);
        String platformName = ep.getProperty(NBJDK_ACTIVE);

        return getPlatformByName(platformName);
    }

    @Override
    public boolean isProfilingSupported() {
        boolean supported = super.isProfilingSupported();
        if (!supported) return false;
        
        final String WEB_MODULE_PROVIDER_TYPE = "org.netbeans.modules.web.freeform.WebModules";
        
        for(Object x : getProject().getLookup().lookupAll(Object.class)) {
            if (WEB_MODULE_PROVIDER_TYPE.equals(x.getClass().getName())) {
                return false; // web freeforms are not supported
            }
        }
        return true;
    }
    
    public FreeFormProjectProfilingSupportProvider(Project project) {
        super(project);
    }
}
