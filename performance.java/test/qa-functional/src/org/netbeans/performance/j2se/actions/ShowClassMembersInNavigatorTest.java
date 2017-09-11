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
