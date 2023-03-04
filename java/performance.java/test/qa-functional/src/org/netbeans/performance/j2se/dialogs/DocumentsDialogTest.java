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
package org.netbeans.performance.j2se.dialogs;

import junit.framework.Test;
import org.netbeans.jellytools.DocumentsDialogOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Documents dialog
 *
 * @author anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class DocumentsDialogTest extends PerformanceTestCase {

    private static EditorOperator editor;

    /**
     * Creates a new instance of DocumentsDialog
     *
     * @param testName test name
     */
    public DocumentsDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of DocumentsDialog
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public DocumentsDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().
                addTest(J2SESetup.class).
                addTest(DocumentsDialogTest.class)
                .suite();
    }

    public void testDocumentsDialog() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        CommonUtilities.openFiles("PerformanceTestFoldersData", getTenSelectedFiles());
    }

    @Override
    public void prepare() {
        editor = new EditorOperator("SampleJavaClass000.java");
    }

    @Override
    public ComponentOperator open() {
        editor.pushKey(java.awt.event.KeyEvent.VK_F4, java.awt.event.KeyEvent.SHIFT_MASK);
        return new DocumentsDialogOperator();
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
    }

    private static String[][] getTenSelectedFiles() {
        String[][] files_path = {
            {"folders.javaFolder100", "SampleJavaClass000.java"},
            {"folders.javaFolder100", "SampleJavaClass001.java"},
            {"folders.javaFolder100", "SampleJavaClass002.java"},
            {"folders.javaFolder100", "SampleJavaClass003.java"},
            {"folders.javaFolder100", "SampleJavaClass004.java"},
            {"folders.javaFolder50", "SampleJavaClass000.java"},
            {"folders.javaFolder50", "SampleJavaClass001.java"},
            {"folders.javaFolder50", "SampleJavaClass002.java"},
            {"folders.javaFolder50", "SampleJavaClass003.java"},
            {"folders.javaFolder50", "SampleJavaClass004.java"},
            {"folders.javaFolder50", "SampleJavaClass005.java"}
        };
        return files_path;
    }
}
