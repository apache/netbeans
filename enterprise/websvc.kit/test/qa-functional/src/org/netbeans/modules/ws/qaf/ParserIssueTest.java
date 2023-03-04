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

package org.netbeans.modules.ws.qaf;

import java.io.File;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.BuildJavaProjectAction;
import org.netbeans.jellytools.actions.OutputWindowViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.test.ProjectSupport;

/**
 *
 * @author lukas
 */
public class ParserIssueTest extends NbTestCase {

    public ParserIssueTest(String name) {
        super(name);
    }

    public void testBuild() {
        //open existing Java SE project
        Project p = (Project) ProjectSupport.openProject(new File(getDataDir(), "projects/Sample"));
        //set Ant verbosity level to "Debug"
        OptionsOperator oo = OptionsOperator.invoke();
        oo.selectJava();
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(oo);
        jtpo.selectPage("Ant"); // NOI18N
        JComboBoxOperator jcbo = new JComboBoxOperator(oo);
        jcbo.selectItem("Debug");
        oo.ok();
        //open output window
        new OutputWindowViewAction().perform();
        //build project
        Node n = ProjectsTabOperator.invoke().getProjectRootNode("Sample"); 
        new BuildJavaProjectAction().perform(n);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ie) {
            //ignore
        }
        String output = new OutputTabOperator("Sample").getText();
        assertTrue("build action output: \n" + output, output.contains("BUILD SUCCESSFUL")); //NOI18N
    }

    public static Test suite() {
        
        //System.setProperty("", "");
        return NbModuleSuite.create(ParserIssueTest.class, ".*", ".*");
    }
}
