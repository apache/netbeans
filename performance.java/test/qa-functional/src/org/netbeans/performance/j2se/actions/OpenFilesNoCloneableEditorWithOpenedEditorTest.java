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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of opening files if Editor is already opened. OpenFilesNoCloneableEditor
 * is used as a base for tests of opening files when editor is already opened.
 *
 * @author mmirilovic@netbeans.org
 */
public class OpenFilesNoCloneableEditorWithOpenedEditorTest extends OpenFilesNoCloneableEditorTest {

    /**
     * Name of file to pre-open
     */
    public static String fileName_preopen;

    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     *
     * @param testName the name of the test
     */
    public OpenFilesNoCloneableEditorWithOpenedEditorTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of OpenFilesNoCloneableEditorWithOpenedEditor
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFilesNoCloneableEditorWithOpenedEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(OpenFilesNoCloneableEditorWithOpenedEditorTest.class)
                .suite();
    }

    @Override
    public void testOpening20kBPropertiesFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Bundle20kB.properties";
        fileName_preopen = "Bundle.properties";
        doMeasurement();
    }

    @Override
    public void testOpening20kBPictureFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "splash.gif";
        fileName_preopen = "Main.java";
        doMeasurement();
    }

    @Override
    public void testOpening20kBFormFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "JFrame20kB.java";
        fileName_preopen = "Main.java";
        expectedTime = 2000;
        doMeasurement();
    }

    /**
     * Initialize test - open Main.java file in the Source Editor.
     */
    @Override
    public void initialize() {
        super.initialize();
        SourcePackagesNode spn = new SourcePackagesNode("PerformanceTestData");
        Node n = new Node(spn, "org.netbeans.test.performance|" + fileName_preopen);
        new OpenAction().performAPI(n);
    }
}
