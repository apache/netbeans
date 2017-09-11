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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Adding ability for a NetBeans Suite modules to provide a GUI customizer.
 *
 * @author Martin Krauskopf
 */
public final class SuiteCustomizer extends BasicCustomizer {
    
    // Programmatic names of categories
    static final String SOURCES = "Sources"; // NOI18N
    static final String LIBRARIES = "Libraries"; // NOI18N
    public static final String APPLICATION = "Application"; // NOI18N
    public static final String APPLICATION_CREATE_STANDALONE_APPLICATION = "standaloneApp"; // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    
    private SuiteProperties suiteProps;
    
    public SuiteCustomizer(Project project, AntProjectHelper helper,
            PropertyEvaluator evaluator) {
        super(project, "Projects/org-netbeans-modules-apisupport-project-suite/Customizer");
        this.helper = helper;
        this.evaluator = evaluator;
    }
    
    void storeProperties() throws IOException {
        suiteProps.triggerLazyStorages();
        suiteProps.storeProperties();
    }
    
    void dialogCleanup() {
        suiteProps = null;
    }    
    
    protected Lookup prepareData() {
        Set<NbModuleProject> subModules = SuiteUtils.getSubProjects(getProject());
        suiteProps = new SuiteProperties((SuiteProject) getProject(), helper, evaluator, subModules);
        return Lookups.fixed(suiteProps, getProject());
    }
}

