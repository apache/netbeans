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

package org.netbeans.performance.enterprise.actions;



import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
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
        Assert.assertNotNull("Classloader must exists", loader);
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
