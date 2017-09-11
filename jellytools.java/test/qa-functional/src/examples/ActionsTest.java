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
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.PropertySheetOperator;

public class ActionsTest extends JellyTestCase {
    
    /** Constructor required by JUnit */
    public ActionsTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        return createModuleTest(ActionsTest.class);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    public void testActions() {
        // call main menu item "Windows|Properties"
        Action action = new Action("Window|IDE Tools|Properties", null);
        action.perform();
        new PropertySheetOperator().close();
        
        // call main menu item "Help|About" with 'no block' action because a modal
        // dialog is expected to appear
        new ActionNoBlock("Help|About", null).perform();
        new NbDialogOperator("About").close();      
        
        // use specialized action
        CopyAction copyAction = new CopyAction();
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1");
        copyAction.perform(node);
        
        new OpenAction().perform(new Node(node, "SampleClass1.java"));
        EditorOperator eo = new EditorOperator("SampleClass1.java");
        eo.select(1);
        // perform action on an operator
        copyAction.perform(eo);
        
        // perform action in different modes on a node
        copyAction.performMenu(node);
        copyAction.performShortcut(node);
        copyAction.performAPI(node);
        copyAction.performPopup(node);
        
        System.out.println("\nFinished.");
    }
}
