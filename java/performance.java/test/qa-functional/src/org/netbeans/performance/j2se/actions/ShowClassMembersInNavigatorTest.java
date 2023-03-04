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
package org.netbeans.performance.j2se.actions;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of opening files.
 *
 * @author mmirilovic@netbeans.org
 */
public class ShowClassMembersInNavigatorTest extends PerformanceTestCase {

    /**
     * Node to be opened/edited
     */
    public static Node openNode, openAnotherNode;
    /**
     * Folder with data
     */
    public static String fileProject;
    /**
     * Folder with data
     */
    public static String filePackage;
    /**
     * Name of file to open
     */
    public static String fileName, anotherFileName;
    private static Logger TIMER;
    private long measuredTime;

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     */
    public ShowClassMembersInNavigatorTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ShowClassMembersInNavigatorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(ShowClassMembersInNavigatorTest.class)
                .suite();
    }

    public void testClassMemberInNavigator() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Main20kB.java";
        anotherFileName = "Main.java";

        TIMER = Logger.getLogger("org.netbeans.modules.java.navigation.ClassMemberPanelUI.perf");
        TIMER.setLevel(Level.FINE);
        TIMER.addHandler(phaseHandler);
        openNode = new Node(new SourcePackagesNode(fileProject), filePackage + '|' + fileName);
        openAnotherNode = new Node(new SourcePackagesNode(fileProject), filePackage + '|' + anotherFileName);
    }

    @Override
    public void prepare() {
        openAnotherNode.select();
    }

    @Override
    public ComponentOperator open() {
        openNode.select();
        return null;
    }

    @Override
    public void shutdown() {
        TIMER.removeHandler(phaseHandler);
    }

    @Override
    public long getMeasuredTime() {
        return measuredTime;
    }

    class PhaseHandler extends Handler {

        public boolean published = false;

        @Override
        public void publish(LogRecord record) {

            if (record.getMessage().startsWith("ClassMemberPanelUI refresh took:")) {
                measuredTime = Long.valueOf(record.getParameters()[1].toString());
            }

        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

    PhaseHandler phaseHandler = new PhaseHandler();
}
