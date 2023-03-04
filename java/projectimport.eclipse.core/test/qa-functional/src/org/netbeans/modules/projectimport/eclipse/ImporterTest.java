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

package org.netbeans.modules.projectimport.eclipse;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.projectimport.eclipse.gui.ImportAppRunParams;
import org.netbeans.modules.projectimport.eclipse.gui.ImportJavaCParams;
import org.netbeans.modules.projectimport.eclipse.gui.ImportJavaVersion;
import org.netbeans.modules.projectimport.eclipse.gui.ImportMultipleRootsJavaProjectFromWS;
import org.netbeans.modules.projectimport.eclipse.gui.ImportProjectWithJarRef;
import org.netbeans.modules.projectimport.eclipse.gui.ImportProjectWithTransitiveDeps;
import org.netbeans.modules.projectimport.eclipse.gui.ImporterMenu;
import org.netbeans.modules.projectimport.eclipse.gui.ImporterWizard;
import org.netbeans.modules.projectimport.eclipse.gui.ImportSimpleJavaProjectFromWS;
import org.netbeans.modules.projectimport.eclipse.gui.ImportSourceFilters;
import org.netbeans.modules.projectimport.eclipse.gui.ImportStandaloneProject;
import org.netbeans.modules.projectimport.eclipse.gui.ImportTestProjects;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ImporterTest {

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ImporterMenu.class).
                addTest(ImporterWizard.class).
                addTest(ImportSimpleJavaProjectFromWS.class).
                //addTest(ImportSimpleWebProjectFromWS.class).
                addTest(ImportMultipleRootsJavaProjectFromWS.class).
                addTest(ImportJavaVersion.class).
                addTest(ImportProjectWithTransitiveDeps.class).
                addTest(ImportAppRunParams.class).
                addTest(ImportJavaCParams.class).
                addTest(ImportSourceFilters.class).
                addTest(ImportTestProjects.class).
                addTest(ImportProjectWithJarRef.class).
                addTest(ImportStandaloneProject.class).
                enableModules(".*").clusters(".*").
                gui(true).reuseUserDir(true).suite();
    }

    private ImporterTest() {}

}
