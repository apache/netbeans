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

package org.netbeans.test.ide;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;

/**
 * Overall sanity check suite for IDE before commit.<br>
 * Look at IDEValidation.java for test specification and implementation.
 *
 * @author Jiri Skrivanek, mrkam@netbeans.org
 */
public class IDECommitValidationTest extends IDEValidation {

    /** Need to be defined because of JUnit */
    public IDECommitValidationTest(String name) {
        super(name);
    }
    
    public static Test suite() throws IOException {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            IDECommitValidationTest.class
        ).clusters(".*").enableModules(".*").honorAutoloadEager(true)
        .failOnException(Level.WARNING)
        .failOnMessage(Level.SEVERE);
        
        
        CountingSecurityManager.initWrites();
        
        /* too easy to break:
        conf = conf.addTest("testReflectionUsage");
         */
        conf = conf.addTest("testWriteAccess");
        //conf = conf.addTest("testInitGC");
        conf = conf.addTest("testMainMenu");
        conf = conf.addTest("testHelp");
        conf = conf.addTest("testOptions");
        conf = conf.addTest("testNewProject");
        conf = conf.addTest("testShortcuts"); // sample project must exist before testShortcuts
        conf = conf.addTest("testNewFile");
        conf = conf.addTest("testProjectsView");
        conf = conf.addTest("testFilesView");
        conf = conf.addTest("testEditor");
        conf = conf.addTest("testBuildAndRun");
        conf = conf.addTest("testDebugging");
        //conf = conf.addTest("testPlugins"); //not in commit suite because it needs net connectivity
        //conf = conf.addTest("testJUnit");  //needs JUnit installed in testPlugins
        conf = conf.addTest("testXML");
        conf = conf.addTest("testDb");
        conf = conf.addTest("testWindowSystem");
//        conf = conf.addTest("testGCDocuments");
//        conf = conf.addTest("testGCProjects");
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(conf));
        suite.addTest(new IDECommitValidationTest("testPostRunCheck"));
        return suite;
    }

    public void testPostRunCheck() throws Exception {
        String ud = System.getProperty("netbeans.user");
        assertNotNull("User dir is provided", ud);

        File loaders = new File(new File(new File(ud), "config"), "loaders.ser");
        if (loaders.exists()) {
            fail("loaders.ser file shall not be created, as loaders shall now be " +
                "defined using layers:\n" + loaders);
        }
    }
}
