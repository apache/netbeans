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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ws.qaf.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JDialog;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.xml.sax.SAXException;

/**
 * Tests for REST samples. Simply said - user must be able to only create
 * and run the particular sample, no additional steps should be needed.
 *
 * Duration of this test suite: approx. 4min
 *
 * @author lukas
 */
public class RestSamplesTest extends RestTestBase {

    public RestSamplesTest(String name) {
        super(name, Server.GLASSFISH);
    }

    @Override
    protected String getProjectName() {
        return getName().substring(4);
    }

    @Override
    protected ProjectType getProjectType() {
        return ProjectType.SAMPLE;
    }

    @Override
    protected String getSamplesCategoryName() {
        return Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro");
    }

    /**
     * Test HelloWorld Sample
     *
     * @throws java.io.IOException
     * @throws java.net.MalformedURLException
     * @throws org.xml.sax.SAXException
     */
    public void testHelloWorldSample() throws IOException, MalformedURLException, SAXException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro/HelloWorldSampleProject");
        createProject(sampleName, getProjectType(), null);
        OutputOperator.invoke();
        deployProject(getProjectName());
        undeployProject(getProjectName());
    }

    /**
     * Test Customer Database Sample
     *
     * @throws java.io.IOException
     */
    public void testCustomerDBSample() throws IOException {
        new Thread("Close REST Resources Configuration dialog") {

            private boolean found = false;
            private static final String dlgLbl = "REST Resources Configuration";

            @Override
            public void run() {
                while (!found) {
                    try {
                        sleep(300);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                    JDialog dlg = JDialogOperator.findJDialog(dlgLbl, true, true);
                    if (null != dlg) {
                        found = true;
                        new NbDialogOperator(dlg).ok();
                    }
                }
            }
        }.start();
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro/CustomerDBSampleProject");
        createProject(sampleName, getProjectType(), null);
        deployProject(getProjectName());
        undeployProject(getProjectName());
    }

    /**
     * Test Customer Database on Spring Sample
     *
     * @throws java.io.IOException
     */
    public void testCustomerDBSpringSample() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro/CustomerDBSpringSampleProject");
        createProject(sampleName, getProjectType(), null);
        // do not deploy - need to be fixed manually
        //deployProject(getProjectName());
    }

    /**
     * Test Message Board Sample
     *
     * @throws java.io.IOException
     */
    public void testMessageBoardSample() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro/MessageBoardSample");
        createProject(sampleName, getProjectType(), null);
        // close dialog about missing JUnit
        if (JDialogOperator.findJDialog("Open Project", true, true) != null) {
            new NbDialogOperator("Open Project").close();
        }
        deployProject(getProjectName());
        undeployProject(getProjectName());
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, RestSamplesTest.class,
                "testHelloWorldSample", //NOI18N
                "testCustomerDBSample", //NOI18N
                "testCustomerDBSpringSample", //NOI18N
                "testMessageBoardSample" //NOI18N
                );
    }
}
