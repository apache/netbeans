/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf.rest.suite;

import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.modules.ws.qaf.rest.JEE6CRUDTest;
import org.netbeans.modules.ws.qaf.rest.JEE6FromDBTest;
import org.netbeans.modules.ws.qaf.rest.JEE6PatternsTest;
import org.netbeans.modules.ws.qaf.rest.JEE6RestCStubsTest;
import org.netbeans.modules.ws.qaf.rest.RestSamplesTest;

/**
 *
 * @author lukas
 */
public class AntJEE6Suite extends J2eeTestCase {

    public AntJEE6Suite(String name) {
        super(name);
    }

    public static Test suite() {
        // This "nicely recursive" implementation is due to limitations in J2eeTestCase API
        return addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH, emptyConfiguration(), JEE6FromDBTest.class,
                "testFromDB",
                "testDeploy",
                "testUndeploy"), JEE6CRUDTest.class,
                "testRfE", //NOI18N
                "testPropAccess", //NOI18N
                "testDeploy", //NOI18N
                "testCreateRestClient", //NOI18N
                "testUndeploy"), JEE6PatternsTest.class,
                "testSingletonDef", //NOI18N
                "testContainerIDef", //NOI18N
                "testCcContainerIDef", //NOI18N
                "testSingleton1", //NOI18N
                "testCcContainerI1", //NOI18N
                "testSingleton2", //NOI18N
                "testContainerI1", //NOI18N
                "testContainerI2", //NOI18N
                "testSingleton3", //NOI18N
                "testContainerI3", //NOI18N
                "testCcContainerI2", //NOI18N
                "testCcContainerI3", //NOI18N
                "testDeploy", //NOI18N
                "testUndeploy"), JEE6RestCStubsTest.class,
                "testWizard", //NOI18N
                "testCreateSimpleStubs", //NOI18N
                "testFromWADL", //NOI18N
                "testCloseProject"), RestSamplesTest.class,
                "testHelloWorldSample", //NOI18N
                "testCustomerDBSample", //NOI18N
                // sample temporarily hidden until bug 228684 is fixed
                //"testCustomerDBSpringSample", //NOI18N
                "testMessageBoardSample").enableModules(".*").clusters(".*").suite();
    }
}
