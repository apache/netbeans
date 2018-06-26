/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.glassfish.javaee.test;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
//import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
import org.netbeans.modules.glassfish.common.ServerDetails;
import org.netbeans.modules.glassfish.common.wizards.AddServerLocationVisualPanel;
import org.netbeans.modules.glassfish.common.wizards.GlassfishWizardProvider;
import org.netbeans.modules.glassfish.common.wizards.Retriever;
import org.netbeans.modules.glassfish.common.wizards.ServerWizardIterator;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
//import org.netbeans.modules.j2ee.sun.ide.j2ee.PlatformValidator;
//import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.AddDomainWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 *
 * @author Michal Mocnak
 */
public class AddRemoveV3InstanceMethods extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public AddRemoveV3InstanceMethods(String testName) {
        super(testName);
    }
            GlassfishWizardProvider gip = GlassfishWizardProvider.createEe6();
    
    public void addV3Instance() throws IOException {
            File f = new File(Util._V3_LOCATION);

            if (!f.exists() || f.list().length < 1) {
                // time to retrieve
                Retriever r = new Retriever(f.getParentFile(),ServerDetails.GLASSFISH_SERVER_3.getIndirectUrl(),
                        AddServerLocationVisualPanel.V3_DOWNLOAD_PREFIX,
                        ServerDetails.GLASSFISH_SERVER_3.getDirectUrl(), new Retriever.Updater() {

                    public void updateMessageText(String msg) {
                        //System.out.println(msg);
                    }

                    public void updateStatusText(String status) {
                        //System.out.println(status);
                    }

                    public void clearCancelState() {
                    }
                }, "glassfishv3");
                r.run();
            }
            ServerWizardIterator inst = new ServerWizardIterator(new ServerDetails[] { ServerDetails.GLASSFISH_SERVER_3 }, 
                    new ServerDetails[0]);
            WizardDescriptor wizard = new WizardDescriptor(new Panel[] {});

            inst.setInstallRoot(Util._V3_LOCATION);
            int dex = Util._V3_LOCATION.lastIndexOf(File.separator);
            if (dex > -1) {
                inst.setInstallRoot(Util._V3_LOCATION.substring(0, dex));
            }
            inst.setGlassfishRoot(Util._V3_LOCATION); // "/export/home/vkraemer/GlassFiah_v3_Prelude/glassfish");
            inst.setDomainLocation(Util._V3_LOCATION+ File.separator + "domains" +
                    File.separator + "domain1");
            inst.setHttpPort(8080);
            inst.setAdminPort(4848);
            wizard.putProperty("ServInstWizard_displayName","GlassFish V3");
            
            inst.initialize(wizard);
            inst.instantiate();
            
            ServerRegistry.getInstance().checkInstanceExists(inst.formatUri("localhost", 4848, null, 
                    Util._V3_LOCATION+ File.separator + "domains", "domain1")); //"[/export/home/vkraemer/GlassFiah_v3_Prelude/glassfish]deployer:gfv3:localhost:4848");
            
            Util.sleep(SLEEP);
    }
    
    public void removeV3Instance() {
        try {
            Util.sleep(SLEEP);

            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(
                    "["+Util._V3_LOCATION+"]deployer:gfv3ee6:localhost:4848"); //inst.formatUri( "localhost", 4848));
            boolean wasRunning = inst.isRunning();

            inst.remove();

            if (wasRunning) {
                Util.sleep(SLEEP);
            }

            try {
                ServerRegistry.getInstance().checkInstanceExists("["+Util._V3_LOCATION+"]deployer:gfv3ee6:localhost:4848"); //gip.formatUri(Util._V3_LOCATION, "localhost", 4848));
            } catch (Exception e) {
                if (wasRunning && inst.isRunning()) {
                    fail("remove did not stop the instance");
                }
                String instances[] = ServerRegistry.getInstance().getInstanceURLs();
                if (null != instances) {
                    if (instances.length > 0) {
                        fail("too many instances");
                    }
                }
                return;
            }

            fail("Sjsas instance still exists !");
        } finally {
//                File ff = new File(Util._V3_LOCATION);
//                if (ff.getAbsolutePath().contains("DELETEME")) {
//                    System.out.println("Deleting: " + ff.getAbsolutePath());
//                    Util.deleteJunk(ff.getParentFile());
//                }
        }
    }
    
    public void deleteJunkInstall() {
                File ff = new File(Util._PRELUDE_LOCATION);
                if (ff.getAbsolutePath().contains(Util.TEMP_FILE_PREFIX)) {
                    System.out.println("Deleting: " + ff.getAbsolutePath());
                    Util.deleteJunk(ff.getParentFile());
                }
    }

    //    public static Test suite() {
//        return NbModuleSuite.create(
//                NbModuleSuite.createConfiguration(AddRemoveV3InstanceMethods.class).
//                addTest("addSjsasInstance","removeSjsasInstance").enableModules(".*").clusters(".*"));
////        NbTestSuite suite = new NbTestSuite("AddRemoveSjsasInstanceMethods");
////        suite.addTest(new AddRemoveSjsasInstanceMethods("addSjsasInstance"));
////        suite.addTest(new AddRemoveSjsasInstanceMethods("removeSjsasInstance"));
////        return suite;
//    }
}
