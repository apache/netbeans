/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
