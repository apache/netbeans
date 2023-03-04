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

package org.netbeans.performance.enterprise.actions;



import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.Log;
import org.openide.util.Lookup;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
/**
 * Test 
 *
 * @author  Alexander Kouznetsov <mrkam@netbeans.org>, Jaroslav Tulach <jtulach@netbeans.org>
 */
public class WatchProjectsTest extends JellyTestCase {

    private static Logger LOG = Logger.getLogger(WatchProjectsTest.class.getName());
    
    
    private static Method getProjects;
    private static Method closeProjects;
    private static Object projectManager;
    
    public WatchProjectsTest(String arg0) {
        super(arg0);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(WatchProjectsTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }
    
    public static void initialize() throws Exception {
        Log.enableInstances(Logger.getLogger("TIMER"), "Project", Level.FINEST);
        
        final ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        assertNotNull("Classloader must exists", loader);
        LOG.fine("Classloader: " + loader);
        Class pmClass = Class.forName(
            "org.netbeans.api.project.ui.OpenProjects", false, loader); //NOI18N
        LOG.fine("class: " + pmClass);
        Method getDefault = pmClass.getMethod("getDefault");
        LOG.fine("  getDefault: " + getDefault);
        projectManager = getDefault.invoke(null);
             
        getProjects = pmClass.getMethod("getOpenProjects");
        LOG.fine("getOpenProjects: " + getProjects);
        
        Class projectArray = Class.forName("[Lorg.netbeans.api.project.Project;");
        
        closeProjects = pmClass.getMethod("close", projectArray);
        LOG.fine("getOpenProjects: " + getProjects);
    }
    
    public void assertProjects() throws Exception {
        closeProjects.invoke(
            projectManager,
            getProjects.invoke(projectManager)
        );
        
        System.setProperty("assertgc.paths", "20");
        
        ProjectsTabOperator.invoke();
        new EventTool().waitNoEvent(2000);
        PNGEncoder.captureScreen(getWorkDir().getAbsolutePath() + java.io.File.separator + "screen_before_testGCProjects.png");
        
        Log.assertInstances("Checking if all projects and DesignView are really garbage collected");
    }

    public void testInitGCProjects() throws Exception {
        initialize();
    }
    
    
    public void testGCProjects() throws Exception {
        assertProjects();
    }

}
