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
package org.netbeans.qa.form.gaps;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 *
 * @author Adam Senk adam.senk@oracle.com
 *
 * This is Functional test of Gap visualization feature (since 7.2)
 */
public class GapsTest extends ExtJellyTestCase {

    public String DATA_PROJECT_NAME = "SampleProject";
    public String PACKAGE_NAME = "data";
    public String PROJECT_NAME = "Java";
    private String FILE_NAME = "Gaps";
    public String FRAME_ROOT = "[JFrame]";
    public String workdirpath;
    public Node formnode;
    private ProjectsTabOperator pto;
    ProjectRootNode prn;
    FormDesignerOperator opDesigner;
    ContainerOperator jfo;

    public GapsTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(GapsTest.class).addTest(
                "testOpenCloseGapDialog",
                "testpopUpDialogInvoke",
                "testNewSizeOfGap",
                "testpopUpDialogOnButton").gui(true).clusters(".*").enableModules(".*"));

    }

    @Override
    public void setUp() throws IOException {
        openDataProjects(DATA_PROJECT_NAME);
        workdirpath = getWorkDir().getParentFile().getAbsolutePath();
        System.out.println("########  " + getName() + "  #######");

        pto = new ProjectsTabOperator();
        prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();

        formnode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + FILE_NAME);
        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
    }
    
     public void testOpenCloseGapDialog() {
        opDesigner = new FormDesignerOperator(FILE_NAME);
        opDesigner.clickMouse(400, 70, 2);
         
        EditLayoutSpaceOperator elso = new EditLayoutSpaceOperator();
        elso.verifySmall();
        elso.Cancel();
    }
     
     public void testpopUpDialogInvoke() {
        opDesigner = new FormDesignerOperator(FILE_NAME);
        opDesigner.clickForPopup(400, 70);
        JPopupMenuOperator jpmo= new JPopupMenuOperator();
        waitNoEvent(500);
        jpmo.pushMenuNoBlock("Edit Layout Space...");
        waitNoEvent(500);
        EditLayoutSpaceOperator elso = new EditLayoutSpaceOperator();
        
        elso.Ok();
    }
     
     public void testpopUpDialogOnButton() {
        opDesigner = new FormDesignerOperator(FILE_NAME);
        
        ComponentInspectorOperator cio = new ComponentInspectorOperator();
        Node inspectorRootNode = new Node(cio.treeComponents(), FRAME_ROOT);
        inspectorRootNode.select();
        inspectorRootNode.expand();
        
        Node buttonNode = new Node(inspectorRootNode, "jButton1 [JButton]");
        buttonNode.callPopup();
        
        JPopupMenuOperator jpmo= new JPopupMenuOperator();
        waitNoEvent(500);
        jpmo.pushMenuNoBlock("Edit Layout Space...");
        waitNoEvent(500);
        
        
        
        EditLayoutSpaceOperator elso = new EditLayoutSpaceOperator();
        
        elso.verify();
        
        assertEquals("default small", (String) elso.cbBottom().getItemAt(0));
        assertEquals("default medium", (String) elso.cbBottom().getItemAt(1));
        assertEquals("default large", (String) elso.cbBottom().getItemAt(2));
        
        assertEquals("default", (String) elso.cbLeft().getItemAt(0));
        elso.Cancel();
    }

    public void testNewSizeOfGap() {
        opDesigner = new FormDesignerOperator(FILE_NAME);
        ComponentInspectorOperator cio = new ComponentInspectorOperator();
        Node inspectorRootNode = new Node(cio.treeComponents(), FRAME_ROOT);
        inspectorRootNode.select();
        inspectorRootNode.expand();
        
        Node buttonNode = new Node(inspectorRootNode, "jButton1 [JButton]");
        buttonNode.callPopup();
        
        JPopupMenuOperator jpmo= new JPopupMenuOperator();
        waitNoEvent(500);
        jpmo.pushMenuNoBlock("Edit Layout Space...");
        waitNoEvent(500);
        
        EditLayoutSpaceOperator elso = new EditLayoutSpaceOperator();
        elso.setSizeOfGapTop("800");
        waitNoEvent(500);

        findInCode(".addContainerGap(800, Short.MAX_VALUE)", opDesigner);
    }
        
}
