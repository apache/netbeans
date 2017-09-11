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

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import static org.netbeans.junit.NbModuleSuite.emptyConfiguration;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 *
 *
 */
public class SwitchToFileTest extends PerformanceTestCase {

    String filenameFrom;
    String filenameTo;

    /**
     * Creates a new instance of SwitchToFile
     *
     * @param testName test name
     */
    public SwitchToFileTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    /**
     * Creates a new instance of SwitchToFile
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public SwitchToFileTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenFoldersProject")
                .addTest(SwitchToFileTest.class)
                .suite();
    }

    public void testSwitchJavaToJava() {
        filenameFrom = "SampleJavaClass001.java";
        filenameTo = "SampleJavaClass000.java";
        doMeasurement();
    }

    public void testSwitchJSPToJSP() {
        filenameFrom = "Test.jsp";
        filenameTo = "index.jsp";
        doMeasurement();
    }

    public void testSwitchJavaToJSP() {
        filenameFrom = "SampleJavaClass000.java";
        filenameTo = "test.jsp";
        doMeasurement();
    }

    public void testSwitchJSPToXML() {
        filenameFrom = "Test.jsp";
        filenameTo = "build.xml";
        doMeasurement();
    }

    public void testSwitchXMLToJSP() {
        filenameFrom = "build.xml";
        filenameTo = "Test.jsp";
        doMeasurement();
    }

    @Override
    protected void initialize() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders.javaFolder50|SampleJavaClass000.java"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders.javaFolder50|SampleJavaClass001.java"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders|Test.jsp"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders|index.jsp"));
        FilesTabOperator fto = FilesTabOperator.invoke();
        Node f = fto.getProjectNode("PerformanceTestFoldersData");
        new OpenAction().performAPI(new Node(f, "build.xml"));
        CommonUtilities.setSpellcheckerEnabled(false);
    }

    @Override
    public void prepare() {
        EditorOperator eo = new EditorOperator(filenameFrom);
    }

    @Override
    public ComponentOperator open() {
        MY_START_EVENT = ActionTracker.TRACK_OPEN_BEFORE_TRACE_MESSAGE;
        return new EditorOperator(filenameTo);
    }

    @Override
    public void close() {
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
        CommonUtilities.setSpellcheckerEnabled(true);
    }
}
