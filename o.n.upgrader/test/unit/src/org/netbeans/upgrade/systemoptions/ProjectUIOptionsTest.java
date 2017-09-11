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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997/2006 Sun
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
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/projectui");
    }
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
