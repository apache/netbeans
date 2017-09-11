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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.util.PNGEncoder;

/** Test of SortByNameAction class.
 *
 * @author Jiri Skrivanek
 */
public class SortByNameActionTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public SortByNameActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(SortByNameActionTest.class);
    }

    @Override
    protected void setUp() throws IOException {
        openDataProjects("SampleProject");
    }

    /** Test performPopup */
    public void testPerformPopup() {
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        new PropertiesAction().perform(node);
        PropertySheetOperator pso = new PropertySheetOperator("SampleClass1.java"); // NOI18N
        if (pso.tblSheet().getRowCount() == 0) {
            // property sheet not initialized properly => try it once more
            pso.close();
            int oldDispatching = JemmyProperties.getCurrentDispatchingModel();
            JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
            try {
                new QueueTool().waitEmpty(2000);
                new PropertiesAction().perform(node);
            } finally {
                JemmyProperties.setCurrentDispatchingModel(oldDispatching);
            }
            pso = new PropertySheetOperator("SampleClass1.java"); // NOI18N
        }
        // set default sorting
        pso.sortByCategory();
        int oldCount = pso.tblSheet().getRowCount();
        log("oldCount=" + oldCount);
        assertTrue("Property sheet mustn't be empty.", oldCount != 0); // NOI18N
        try {
            PNGEncoder.captureScreen(getWorkDir().getAbsolutePath() + File.separator + "screen1-AfterSortByCategory.png");
        } catch (Exception e) {
            // ignore it
        }
        new SortByNameAction().perform(pso);
        int newCount = pso.tblSheet().getRowCount();
        log("newCount=" + newCount);
        try {
            PNGEncoder.captureScreen(getWorkDir().getAbsolutePath() + File.separator + "screen2-AfterSortByName.png");
        } catch (Exception e) {
            // ignore it
        }
        // re-set default sorting
        pso.sortByCategory();
        try {
            PNGEncoder.captureScreen(getWorkDir().getAbsolutePath() + File.separator + "screen3-AfterSortByCategory.png");
        } catch (Exception e) {
            // ignore it
        }
        pso.close();
        // if sorted by name there are no categories displayed in property sheet
        assertTrue("Sort by name failed.", oldCount > newCount); // NOI18N
    }
}
