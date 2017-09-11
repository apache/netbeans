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
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Closing All Documents.
 *
 * @author mmirilovic@netbeans.org
 */
public class CloseAllEditorsTest extends PerformanceTestCase {

    /**
     * Nodes represent files to be opened
     */
    private static Node[] openFileNodes;
    private EditorOperator editor;

    /**
     * Creates a new instance of CloseAllEditors
     *
     * @param testName the name of the test
     */
    public CloseAllEditorsTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 2000;
    }

    /**
     * Creates a new instance of CloseAllEditors
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseAllEditorsTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 2000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenFoldersProject")
                .addTest(CloseAllEditorsTest.class)
                .suite();
    }

    public void testCloseAllEditors() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        prepareFiles();
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
    }

    @Override
    public void prepare() {
        new OpenAction().performAPI(openFileNodes);
        editor = new EditorOperator("SampleJavaClass000.java");
    }

    @Override
    public ComponentOperator open() {
        editor.pushKey(java.awt.event.KeyEvent.VK_W, java.awt.event.KeyEvent.SHIFT_MASK + java.awt.event.KeyEvent.CTRL_MASK);
        return null;
    }

    /**
     * Prepare ten selected file from project
     */
    protected void prepareFiles() {
        String[][] files_path = getTenSelectedFiles();
        openFileNodes = new Node[files_path.length];
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode("PerformanceTestFoldersData");
        for (int i = 0; i < files_path.length; i++) {
            openFileNodes[i] = new Node(sourcePackagesNode, files_path[i][0] + '|' + files_path[i][1]);
        }
    }

    private static String[][] getTenSelectedFiles() {
        String[][] files_path = {
            {"folders.javaFolder100", "SampleJavaClass099.java"},
            {"folders.javaFolder100", "SampleJavaClass098.java"},
            {"folders.javaFolder100", "SampleJavaClass097.java"},
            {"folders.javaFolder100", "SampleJavaClass096.java"},
            {"folders.javaFolder100", "SampleJavaClass095.java"},
            {"folders.javaFolder50", "SampleJavaClass000.java"},
            {"folders.javaFolder50", "SampleJavaClass001.java"},
            {"folders.javaFolder50", "SampleJavaClass002.java"},
            {"folders.javaFolder50", "SampleJavaClass003.java"},
            {"folders.javaFolder50", "SampleJavaClass004.java"}
        };
        return files_path;
    }
}
