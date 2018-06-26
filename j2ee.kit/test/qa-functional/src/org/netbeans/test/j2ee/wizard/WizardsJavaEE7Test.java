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
package org.netbeans.test.j2ee.wizard;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author jungi, Jiri Skrivanek
 */
public class WizardsJavaEE7Test extends NewFileWizardsTest {

    /** Creates a new instance of WizardsJavaEE7Test */
    public WizardsJavaEE7Test(String testName) {
        super(testName, "7");
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf);  // register server
        if (isRegistered(Server.GLASSFISH)) {
            conf = conf.addTest(Suite.class);
        }
        return conf.suite();
    }

    public static class Suite extends NbTestSuite {

        public Suite() {
            super();
            addTest(new WizardsJavaEE7Test("testLocalSessionBean"));
            addTest(new WizardsJavaEE7Test("testRemoteSessionBean"));
            addTest(new WizardsJavaEE7Test("testLocalRemoteSessionBean"));
            addTest(new WizardsJavaEE7Test("testLocalStatefulSessionBean"));
            addTest(new WizardsJavaEE7Test("testRemoteStatefulSessionBean"));
            addTest(new WizardsJavaEE7Test("testLocalRemoteStatefulSessionBean"));
            addTest(new WizardsJavaEE7Test("testPersistenceUnitInEjb"));
            addTest(new WizardsJavaEE7Test("testEntityClassInEjb"));
            addTest(new WizardsJavaEE7Test("testQueueMdbBean"));
            addTest(new WizardsJavaEE7Test("testTopicMdbBean"));
            addTest(new WizardsJavaEE7Test("testServiceLocatorInEjb"));
            addTest(new WizardsJavaEE7Test("testCachingServiceLocatorInEjb"));
            addTest(new WizardsJavaEE7Test("testBuildDefaultNewEJBMod"));
            // web project
            addTest(new WizardsJavaEE7Test("testServiceLocatorInWeb"));
            addTest(new WizardsJavaEE7Test("testCachingServiceLocatorInWeb"));
            addTest(new WizardsJavaEE7Test("testPersistenceUnitInWeb"));
            addTest(new WizardsJavaEE7Test("testEntityClassInWeb"));
            addTest(new WizardsJavaEE7Test("testBuildDefaultNewWebMod"));
            addTest(new NewProjectWizardsTest("closeProjects", "7"));
        }
    }
}
