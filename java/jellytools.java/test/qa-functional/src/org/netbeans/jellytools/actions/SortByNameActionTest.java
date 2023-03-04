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
