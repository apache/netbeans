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

package org.netbeans.test.subversion.main.checkout;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.ProxyConfigurationOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class ProxySettingsUITest extends JellyTestCase {
    
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of ProxySettingsUITest */
    public ProxySettingsUITest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {        
        System.out.println("### "+getName()+" ###");
        
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(ProxySettingsUITest.class).addTest(
                    "testProxySettings",
                    "testProxyBeforeUrl"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }

//        public static Test suite() {
//         return NbModuleSuite.create(
//                 NbModuleSuite.createConfiguration(ProxySettingsUITest.class).addTest(
//                    "testProxySettings" +
//                    ""
//                 )
//                 .enableModules(".*")
//                 .clusters(".*")
//        );
//     }
    
    public void testProxySettings() throws RuntimeException{

        


        if (TestKit.getOsName().indexOf("Mac") > -1)
                new NewProjectWizardOperator().invoke().close();
        
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        new EventTool().checkNoEvent(3000);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        new EventTool().checkNoEvent(3000);
        RepositoryStepOperator co1so = new RepositoryStepOperator();
        new EventTool().checkNoEvent(3000);
        ProxyConfigurationOperator pco = null;

        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS + "localhost");

        pco = co1so.invokeProxy();
        new EventTool().waitNoEvent(2000);   
        pco.verify();
        pco.useSystemProxySettings();
        pco.noProxyDirectConnection();
        pco.hTTPProxy();
        pco.setProxyHost("host");// NOI18N
        pco.setPort("8080");
//        pco.checkProxyServerRequiresLogin(true);
//        pco.setName("name");// NOI18N
//        pco.setPassword("password");// NOI18N

        pco.ok();
        co.btCancel().pushNoBlock();
        

    }
    
    public void testProxyBeforeUrl() throws Exception {
        try {
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        new EventTool().checkNoEvent(3000);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        new EventTool().checkNoEvent(3000);
        RepositoryStepOperator co1so = new RepositoryStepOperator();
        new EventTool().checkNoEvent(3000);
         ProxyConfigurationOperator pco = null;

        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS + "localhost");
 

        TimeoutExpiredException tee = null;
        
        try {
            pco = co1so.invokeProxy();
            pco.ok();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
//        assertNotNull(tee);
    
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        tee = null;

        try {
            pco = co1so.invokeProxy();
            pco.ok();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
//        assertNotNull(tee);
        
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        tee = null;

        try {
            pco = co1so.invokeProxy();
            pco.ok();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
//        assertNotNull(tee);
        
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        tee = null;

        try {
            pco = co1so.invokeProxy();
            pco.ok();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }

//        assertNotNull(tee);
        
        co.btCancel().pushNoBlock();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        }
    }
}
