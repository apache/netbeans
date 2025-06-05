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
// //        NbTestSuite suite = new NbTestSuite("AddRemoveSjsasInstanceMethods");
// //        suite.addTest(new AddRemoveSjsasInstanceMethods("addSjsasInstance"));
// //        suite.addTest(new AddRemoveSjsasInstanceMethods("removeSjsasInstance"));
// //        return suite;
//    }
}
