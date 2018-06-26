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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.earproject.ui;

import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider.LogicalViewRootNode;
import org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Support for creating logical views.
 */
public class J2eeArchiveLogicalViewProvider extends AbstractLogicalViewProvider2 {
    
    private final EarProject project;

    public J2eeArchiveLogicalViewProvider(EarProject project, UpdateHelper helper,
            PropertyEvaluator evaluator, ReferenceHelper resolver, J2eeModuleProvider provider) {
        super(project, helper, evaluator, resolver, provider);
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        return new LogicalViewRootNode("Projects/org-netbeans-modules-j2ee-earproject/Nodes",
                    "org-netbeans-modules-j2ee-earproject",
                    "org/netbeans/modules/j2ee/earproject/ui/resources/projectIcon.gif",
                    NbBundle.getMessage(J2eeArchiveLogicalViewProvider.class, "HINT_project_root_node"),
                    J2eeArchiveLogicalViewProvider.class);
    }
    
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        EarProjectProperties.DEBUG_CLASSPATH,
        ProjectProperties.ENDORSED_CLASSPATH,
        EarProjectProperties.JAR_CONTENT_ADDITIONAL,
    };
    
    @Override
    public String[] getBreakableProperties() {
        return BREAKABLE_PROPERTIES.clone();
    }

    @Override
    protected void setServerInstance(Project project, UpdateHelper helper, String serverInstanceID) {
        EarProjectProperties.setServerInstance((EarProject)project, helper, serverInstanceID);
    }
    
}
