/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.ejb;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * Maven EJB Recommended and Privileged templates implementation
 * 
 * @author Martin Janicek
 */
@org.netbeans.api.annotations.common.SuppressWarnings("EI_EXPOSE_REP")
@ProjectServiceProvider(
    service = {
        RecommendedTemplates.class,
        PrivilegedTemplates.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB
    }
)
public class EjbRecoPrivTemplates implements RecommendedTemplates, PrivilegedTemplates {

    private J2eeProjectCapabilities capabilities;
    private Project project;
    
    
    public EjbRecoPrivTemplates(Project project) {
        this.project = project;
    }
    
    private static final String[] EJB_RECOMMENDED_TYPES_5 = new String[] {
        "ejb-deployment-descriptor",// NOI18N
        "ejb-types",            // NOI18N
        "ejb-types_3_0",        // NOI18N
        "ejb-types-server",     // NOI18N
        "web-services",         // NOI18N
        "web-service-clients",  // NOI18N
        "j2ee-types",           // NOI18N
    };

    private static final String[] EJB_RECOMMENDED_TYPES_6 = new String[] {
        "ejb-deployment-descriptor",// NOI18N
        "ejb-types",                // NOI18N
        "ejb-types-server",         // NOI18N
        "ejb-types_3_1",            // NOI18N
        "web-services",             // NOI18N
        "web-service-clients",      // NOI18N
        "wsdl",                     // NOI18N
        "j2ee-types"                // NOI18N
    };

    private static final String[] EJB_PRIVILEGED_NAMES_5 = new String[] {
        "Templates/J2EE/Session",               // NOI18N
        "Templates/J2EE/Message",               // NOI18N
        "Templates/Classes/Class.java",         // NOI18N
        "Templates/Classes/Package",            // NOI18N
        "Templates/Persistence/Entity.java",    // NOI18N
        "Templates/Persistence/RelatedCMP",     // NOI18N
        "Templates/WebServices/WebService",     // NOI18N
        "Templates/WebServices/WebServiceClient"// NOI18N
    };

    private static final String[] EJB_PRIVILEGED_NAMES_6 = EJB_PRIVILEGED_NAMES_5;
    
    
    @Override
    public String[] getRecommendedTypes() {
        initCapabilities();
        if (capabilities.isEjb32Supported()) {
            return EJB_RECOMMENDED_TYPES_6;
        }
        if (capabilities.isEjb31Supported()) {
            return EJB_RECOMMENDED_TYPES_6;
        }
        if (capabilities.isEjb30Supported()) {
            return EJB_RECOMMENDED_TYPES_5;
        }
        return EJB_RECOMMENDED_TYPES_5;
    }
    
    @Override
    public String[] getPrivilegedTemplates() {
        initCapabilities();
        if (capabilities.isEjb32Supported()) {
            return EJB_PRIVILEGED_NAMES_6;
        }
        if (capabilities.isEjb31Supported()) {
            return EJB_PRIVILEGED_NAMES_6;
        }
        if (capabilities.isEjb30Supported()) {
            return EJB_PRIVILEGED_NAMES_5;
        }
        return EJB_PRIVILEGED_NAMES_5;
    }
    
    private void initCapabilities() {
        if (capabilities == null) {
            capabilities = J2eeProjectCapabilities.forProject(project);
        }
    }
}
