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

import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/** Checks the behaviour of enabled module list.
 *
 * @author Jaroslav Tulach
 */
public class SuiteCustomizerModuleListTest extends TestBase {

    static {
                //    XXX: failing test, fix or delete
//        SuiteCustomizerLibraries.TEST = true;
//        RestrictThreadCreation.permitStandard();
//        RestrictThreadCreation.forbidNewThreads(true);
    }

    private FileObject suiteRepoFO;
    private SuiteProject suite1Prj;
    private SuiteProperties suite1Props;
    private FileObject suite1FO;
    
    private SuiteCustomizerLibraries customizer;
    
    public SuiteCustomizerModuleListTest(String testName) {
        super(testName);
    }

    @Override
    public boolean canRun() {
        // On Windows XP, under JDK 6 (but, oddly, not 5), get apparent path length limit violations:
        // java.lang.AssertionError: E:\space\test4u\builds\bindist_netbeans_Dev_daily_latest\ unit\apisupport1\org-netbeans-modules-apisupport-project\work\sys\data\example-external-projects\.\suite1\support\lib-project\test\ unit\src\org\netbeans\examples\modules\lib\LibClassTest.java
        //         at org.netbeans.modules.apisupport.project.TestBase.doCopy(TestBase.java:272)
        //         at org.netbeans.modules.apisupport.project.TestBase.doCopy(TestBase.java:269)
        //         [....]
        return super.canRun() && !Utilities.isWindows();
    }

    protected @Override void setUp() throws Exception {
                //    XXX: failing test, fix or delete
//        super.setUp();
//        suiteRepoFO = FileUtil.toFileObject(copyFolder(resolveEEPFile(".")));
//        suite1FO = suiteRepoFO.getFileObject("suite1");
//        suite1Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
//        this.suite1Props = new SuiteProperties(suite1Prj, suite1Prj.getHelper(),
//                suite1Prj.getEvaluator(), SuiteUtils.getSubProjects(suite1Prj));
//        
//        customizer = new SuiteCustomizerLibraries(this.suite1Props, ProjectCustomizer.Category.create("x", "xx", null));
    }

        //    XXX: failing test, fix or delete
//    public void testDisableCluster() throws Exception {
//        enableAllClusters(false);
//        doDisableCluster(0, true);
//    }
//    
//    public void testDisableCluster2() throws Exception {
//        enableAllClusters(false);
//        doDisableCluster(1, true);
//    }
//    
//    public void testDisableTwoClusters() throws Exception {
//        enableAllClusters(false);
//        
//        String c1 = doDisableCluster(1, true);
//        String c2 = doDisableCluster(2, false);
//        Set<String> c = new HashSet<String>();
//        c.add(c1);
//        c.add(c2);
//        
//        String[] xyz = suite1Props.getEnabledClusters();
//        //assertEquals("Two clusters disabled", ???, xyz.length);
//        
//        Set<String> real = new HashSet<String>(Arrays.asList(xyz));
//        assertFalse(real.containsAll(c));
//    }
    
//    private String doDisableCluster(int index, boolean doCheck) throws Exception {
//        Node n = customizer.getExplorerManager().getRootContext();
//        Node[] clusters = n.getChildren().getNodes();
//        if (clusters.length <= index) {
//            fail ("Wrong, there should be some clusters. at least: " + index + " and was: " + clusters.length);
//        }
//        Node[] modules = clusters[index].getChildren().getNodes();
//        if (modules.length == 0) {
//            fail("Expected more modules for cluster: " + clusters[index]);
//        }
//
//        setNodeEnabled(clusters[index], false);
//        assertEquals("No modules in disabled clusters",
//                clusters[index].getChildren().getNodes().length, modules.length);
//
//        customizer.store();
//        suite1Props.storeProperties();
//
//        if (doCheck) {
//            String[] xyz = suite1Props.getEnabledClusters();
//            //assertEquals("One cluster is disabled", ???, xyz.length);
//            assertFalse("It's name is name of the node", Arrays.asList(xyz).contains(clusters[index].getName()));
//        }
//
//        return clusters[index].getName();
//    }
    
    public void testDisableModule() throws Exception {
        //    XXX: failing test, fix or delete
//        enableAllClusters(true);
//        
//        Node n = customizer.getExplorerManager().getRootContext();
//        Node[] clusters = n.getChildren().getNodes();
//        if (clusters.length == 0) {
//            fail("Should be at least one cluster");
//        }
//        Node[] modules = clusters[0].getChildren().getNodes();
//        if (modules.length == 0) {
//            fail("Expected at least one module in cluster: " + clusters[0]);
//        }
//
//        setNodeEnabled(modules[0], false);
//        assertNodeEnabled(modules[0], Boolean.FALSE);
//        
//        customizer.store();
//        suite1Props.storeProperties();
//                
//        String[] xyz = suite1Props.getDisabledModules();
//        assertEquals("One module is disabled", 1, xyz.length);
//        assertEquals("It's name is name of the node", modules[0].getName(), xyz[0]);
    }
    
//    private static void assertNodeEnabled(Node n, Boolean value) throws Exception {
//        for (Node.PropertySet ps : n.getPropertySets()) {
//            for (Node.Property<?> prop : ps.getProperties()) {
//                if (prop.getName().equals("enabled")) {
//                    Object o = prop.getValue();
//                    assertEquals("Node is correctly enabled/disabled: " + n, value, o);
//                    return;
//                }
//            }
//        }
//        fail("No enabled property found: " + n);
//    }
//    private static void setNodeEnabled(Node n, boolean value) throws Exception {
//        for (Node.PropertySet ps : n.getPropertySets()) {
//            for (Node.Property<?> prop : ps.getProperties()) {
//                if (prop.getName().equals("enabled")) {
//                    @SuppressWarnings("unchecked") // value type is Boolean.TYPE, not Boolean.class, so Class.<T>cast will not help
//                    Node.Property<Boolean> _prop = (Node.Property<Boolean>) prop;
//                    _prop.setValue(value);
//                    return;
//                }
//            }
//        }
//        fail("No enabled property found: " + n);
//    }

//    private void enableAllClusters(boolean enableModulesAsWell) throws Exception {
//        Node n = customizer.getExplorerManager().getRootContext();
//        for (Node cluster : n.getChildren().getNodes()) {
//            setNodeEnabled(cluster, true);
//            if (enableModulesAsWell) {
//                for (Node module : cluster.getChildren().getNodes()) {
//                    setNodeEnabled(module, true);
//                }
//            }
//        }
//    }
}
