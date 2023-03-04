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
package org.netbeans.performance.j2se.menus;

import junit.framework.Test;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test List Of The Recent Opened Windows popup menu on Editor Window down
 * button if 10 files opened
 *
 * @author juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class EditorDownButtonPopupMenuTest extends PerformanceTestCase {

    /**
     * Test of popup menu on Editor's 'Down Button'
     *
     * @param testName test name
     */
    public EditorDownButtonPopupMenuTest(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }

    /**
     * Test of popup menu on Editor's 'Down Button'
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public EditorDownButtonPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenFoldersProject")
                .addTest(EditorDownButtonPopupMenuTest.class)
                .suite();
    }

    public void testEditorDownButtonPopupMenu() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        CommonUtilities.openFiles("PerformanceTestFoldersData", getTenSelectedFiles());
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        EditorWindowOperator.btDown().clickForPopup();
        ComponentOperator popupComponent = new ComponentOperator(EditorWindowOperator.btDown().getContainer(ComponentSearcher.getTrueChooser("org.netbeans.core.windows.view.ui.RecentViewListDlg")));
        return popupComponent;
    }

    @Override
    public void shutdown() {
        EditorWindowOperator.closeDiscard();
    }

    private static String[][] getTenSelectedFiles() {
        String[][] files_path = {
            {"folders.javaFolder50", "SampleJavaClass000.java"},
            {"folders.javaFolder50", "SampleJavaClass001.java"},
            {"folders.javaFolder50", "SampleJavaClass002.java"},
            {"folders.javaFolder50", "SampleJavaClass003.java"},
            {"folders.javaFolder50", "SampleJavaClass004.java"},
            {"folders.javaFolder50", "SampleJavaClass005.java"},
            {"folders.javaFolder50", "SampleJavaClass006.java"},
            {"folders.javaFolder50", "SampleJavaClass007.java"},
            {"folders.javaFolder50", "SampleJavaClass008.java"},
            {"folders.javaFolder50", "SampleJavaClass009.java"}
        };
        return files_path;
    }
}
