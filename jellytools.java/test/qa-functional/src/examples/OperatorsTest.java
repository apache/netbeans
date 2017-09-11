/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package examples;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.BuildJavaProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

public class OperatorsTest extends JellyTestCase {

    /** Constructor required by JUnit */
    public OperatorsTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        return createModuleTest(OperatorsTest.class);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    public void testOperators() {
        ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode("SampleProject");
        new BuildJavaProjectAction().perform(projectNode);
        
        // toolbars, status text
        MainWindowOperator.getDefault().waitStatusText("SampleProject");
        
        SourcePackagesNode sourceNode = new SourcePackagesNode("SampleProject");
        new OpenAction().perform(new Node(sourceNode, "sample1|SampleClass1.java"));

        new FilesTabOperator();
        FavoritesOperator.invoke();
        RuntimeTabOperator.invoke().getRootNode();

        // editing, toolbar, annotations
        new EditorOperator("SampleClass1.java").insert("INSERTED");
        EditorOperator.closeDiscardAll();

        // popup, getText, waitText
        //new OutputTabOperator("SampleProject").waitText("compile");

        // pushMenuOnTab, close, cloneDocument
        new TopComponentOperator("Projects");

        new JavaNode(sourceNode, "sample1|SampleClass1.java").delete();
        new NbDialogOperator("Delet").close();
    }

    /** Wait for something using Jemmy. If time to wait expires and condition
     * is not true, it throws JemmyException.
     */
    public void testWaiting() throws Exception {
        final ProjectsTabOperator pto = new ProjectsTabOperator();
        new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object obj) {
                // it should be true in general
                return pto.isShowing() ? Boolean.TRUE : null;
            }

            @Override
            public String getDescription() {
                return ("Wait Projects tab is showing");
            }
        }).waitAction(null);
    }
}
