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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
