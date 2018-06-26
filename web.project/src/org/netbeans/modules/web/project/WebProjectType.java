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

package org.netbeans.modules.web.project;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.project.spi.WebProjectImplementationFactory;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.Lookup;

public final class WebProjectType {

    public static final String TYPE = "org.netbeans.modules.web.project";
    private static final String PROJECT_CONFIGURATION_NAME = "data";
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/web-project/3";
    private static final String PRIVATE_CONFIGURATION_NAME = "data";
    private static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/web-project-private/1";
    
    private static final String[] PROJECT_CONFIGURATION_NAMESPACE_LIST =
            {"http://www.netbeans.org/ns/web-project/1",
            "http://www.netbeans.org/ns/web-project/2",
            "http://www.netbeans.org/ns/web-project/3"};

    /** Do nothing, just a service. */
    private WebProjectType() {}
    
    @AntBasedProjectRegistration(
        iconResource="org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif", // NOI18N
        type=TYPE,
        sharedName=PROJECT_CONFIGURATION_NAME,
        sharedNamespace=PROJECT_CONFIGURATION_NAMESPACE,
        privateName=PRIVATE_CONFIGURATION_NAME,
        privateNamespace=PRIVATE_CONFIGURATION_NAMESPACE
    )
    public static Project createProject(AntProjectHelper helper) throws IOException {
        for(WebProjectImplementationFactory factory : getProjectFactories()) {
            if (factory.acceptProject(helper)) {
                //delegate project completely to another implementation
                return factory.createProject(helper);
            }
        }
        return new WebProject(helper);
    }

    private static Collection<? extends WebProjectImplementationFactory> getProjectFactories() {
        return Lookup.getDefault().lookupAll(WebProjectImplementationFactory.class);
    }

    public static String[] getConfigurationNamespaceList() {
        return PROJECT_CONFIGURATION_NAMESPACE_LIST.clone();
    }
}
