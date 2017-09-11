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
package org.netbeans.jellytools.actions;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jemmy.JemmyException;

/** org.netbeans.jellytools.actions.DebugProjectAction
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class DebugProjectActionTest extends JellyTestCase {

    public static final String[] tests = new String[]{"testPerformPopup", "testPerformMenu", "testPerformShortcut"};

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DebugProjectActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(DebugProjectActionTest.class, tests);
    }
    private static MainWindowOperator.StatusTextTracer statusTextTracer;

    /** Method called before all test cases. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");  // NOI18N
        openDataProjects("SampleProject");
        if (statusTextTracer == null) {
            // increase timeout to 60 seconds
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
            statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        }
        statusTextTracer.start();
    }

    /** method called after each testcase
     */
    @Override
    protected void tearDown() {
        try {
            // "SampleWebProject (debug)"
            String outputTarget = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle", "TITLE_output_target",
                    new Object[]{"SampleProject", null, "debug"});  // NOI18N
            // "Building SampleProject (debug)..."
            String buildingMessage = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle", "FMT_running_ant",
                    new Object[]{outputTarget});
            // "Finished building SampleProject (debug)"
            String finishedMessage = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status",
                    new Object[]{outputTarget});
            // wait status text "Building SampleProject (debug)..."
            statusTextTracer.waitText(buildingMessage);
            // wait status text "Finished building SampleProject (debug)."
            statusTextTracer.waitText(finishedMessage);
            // wait status text "User program finished"
            statusTextTracer.waitText("User program finished"); // NOI18N
        } catch (JemmyException e) {
            log("debugOutput.txt", new OutputTabOperator("SampleProject").getText()); // NOI18N
            throw e;
        } finally {
            statusTextTracer.stop();
        }
    }

    /** Test performPopup. */
    public void testPerformPopup() {
        new DebugProjectAction().performPopup(ProjectsTabOperator.invoke().getProjectRootNode("SampleProject")); // NOI18N
    }

    /** Test performMenu */
    public void testPerformMenu() {
        // Set as Main Project
        new Action("Run|Set Main Project|SampleProject", null).perform();
        new DebugProjectAction().performMenu();
    }

    /** Test performShortcut */
    public void testPerformShortcut() {
        new DebugProjectAction().performShortcut();
    }
}
