/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.upgrade.systemoptions;

/**
 * @author Milos Kleint
 */
public class ProjectUIOptionsTest extends BasicTestForImport {

    //properties as of 5.0
    private static final String LAST_OPEN_PROJECT_DIR = "lastOpenProjectDir"; //NOI18N - String
    private static final String PROP_PROJECT_CATEGORY = "lastSelectedProjectCategory"; //NOI18N - String
    private static final String PROP_PROJECT_TYPE = "lastSelectedProjectType"; //NOI18N - String
    private static final String MAIN_PROJECT_URL = "mainProjectURL"; //NOI18N -URL
    private static final String OPEN_AS_MAIN = "openAsMain"; //NOI18N - boolean
    private static final String OPEN_PROJECTS_URLS = "openProjectsURLs"; //NOI18N - List of URLs
    private static final String OPEN_SUBPROJECTS = "openSubprojects"; //NOI18N - boolean
    private static final String PROP_PROJECTS_FOLDER = "projectsFolder"; //NOI18N - String
    private static final String RECENT_PROJECTS_URLS = "recentProjectsURLs"; //NOI18N List of URLs
    private static final String RECENT_TEMPLATES = "recentTemplates"; // NOI18N -List of Strings
    
    
    
    
    public ProjectUIOptionsTest(String testName) {
        super(testName, "org-netbeans-modules-project-ui-OpenProjectList.settings");
    }
    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/projectui");
    }
    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
                LAST_OPEN_PROJECT_DIR,
                PROP_PROJECT_CATEGORY,
                PROP_PROJECT_TYPE,
                MAIN_PROJECT_URL,
                OPEN_AS_MAIN,
                OPEN_PROJECTS_URLS + ".0",
                OPEN_SUBPROJECTS,
                PROP_PROJECTS_FOLDER,
                RECENT_PROJECTS_URLS + ".0",
                RECENT_TEMPLATES + ".0",
                RECENT_TEMPLATES + ".1",
        });
    }
    
    public void testLastOpenProjectDir() throws Exception {
        assertProperty(LAST_OPEN_PROJECT_DIR, "/Users/mkleint");
    }

    public void testLastSelectedProjectCategory() throws Exception {
        assertProperty(PROP_PROJECT_CATEGORY, "Web");
    }

    public void testLastSelectedProjectType() throws Exception {
        assertProperty(PROP_PROJECT_TYPE, "emptyWeb");
    }

    
    public void testOpenAsMain() throws Exception {
        assertProperty(OPEN_AS_MAIN, "true");
    }


     public void testOpenSubprojects() throws Exception {
        assertProperty(OPEN_SUBPROJECTS, "true");
    }

     public void testProjectsFolder() throws Exception {
        assertProperty(PROP_PROJECTS_FOLDER, "/Users/mkleint");
    }

    public void testMainProjectURL() throws Exception {
        assertProperty(MAIN_PROJECT_URL, "file:/Users/mkleint/WebApplication1/");
    }

    public void testOpenProjectsURLs() throws Exception {
        assertProperty(OPEN_PROJECTS_URLS + ".0", "file:/Users/mkleint/WebApplication1/");
    }
    
     public void testRecentProjectsURLs() throws Exception {
        assertProperty(RECENT_PROJECTS_URLS + ".0", "file:/Users/mkleint/JavaApplication1/");
    }
     
     public void testRecentTemplates() throws Exception {
        assertProperty(RECENT_TEMPLATES + ".0", "Templates/JSP_Servlet/Servlet.java");
        assertProperty(RECENT_TEMPLATES + ".1", "Templates/JSP_Servlet/JSP.jsp");
    }
    
    
}
