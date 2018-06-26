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
package org.netbeans.performance.languages.actions;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import junit.framework.Test;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingExpandFolderTest extends PerformanceTestCase {

    /**
     * Name of the folder which test creates and expands
     */
    protected String project;
    /**
     * Path to the folder which test creates and expands
     */
    protected String pathToFolderNode;
    /**
     * Node represantation of the folder which test creates and expands
     */
    protected Node nodeToBeExpanded;
    /**
     * Projects tab
     */
    protected ProjectsTabOperator projectTab;

    public ScriptingExpandFolderTest(String testName) {
        super(testName);
    }

    public ScriptingExpandFolderTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() throws URISyntaxException {
        URL u = ScriptingExpandFolderTest.class.getProtectionDomain().getCodeSource().getLocation();
        File f = new File(u.toURI());
        while (f != null) {
            File hg = new File(f, ".hg");
            if (hg.isDirectory()) {
                System.setProperty("versioning.unversionedFolders", f.getPath());
                System.err.println("ignoring Hg folder: " + f);
                break;
            }
            f = f.getParentFile();
        }

        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(ScriptingExpandFolderTest.class).suite();
    }

    @Override
    public void initialize() {
        projectTab = ScriptingUtilities.invokePTO();
        new MaximizeWindowAction().performAPI(projectTab);
        projectTab.getProjectRootNode(project).collapse();
        repaintManager().addRegionFilter(LoggingRepaintManager.EXPLORER_FILTER);
    }

    public void prepare() {
        if (pathToFolderNode.equals("")) {
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        } else {
            nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
        }
    }

    public ComponentOperator open() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EXPLORER_FILTER);
        nodeToBeExpanded.tree().doExpandPath(nodeToBeExpanded.getTreePath());
        nodeToBeExpanded.expand();
        return null;
    }

    @Override
    public void close() {
        nodeToBeExpanded.collapse();
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        projectTab.getProjectRootNode(project).collapse();
        new RestoreWindowAction().performAPI(projectTab);
    }

    public void testExpandPHPProjectNode() {
        WAIT_AFTER_OPEN = 1000;
        project = Projects.PHP_PROJECT;
        pathToFolderNode = "";
        expectedTime = 1000;
        doMeasurement();
    }

    public void testExpandFolderWith100JSFiles() {
        WAIT_AFTER_OPEN = 1000;
        project = Projects.SCRIPTING_PROJECT;
        pathToFolderNode = "Web Pages" + "|" + "100JsFiles";
        expectedTime = 1000;
        doMeasurement();
    }

    public void testExpandFolderWith100CssFiles() {
        WAIT_AFTER_OPEN = 1000;
        project = Projects.SCRIPTING_PROJECT;
        pathToFolderNode = "Web Pages" + "|" + "100CssFiles";
        expectedTime = 1000;
        doMeasurement();
    }

    public void testExpandFolderWith100PhpFiles() {
        WAIT_AFTER_OPEN = 1000;
        project = Projects.PHP_PROJECT;
        pathToFolderNode = "Source Files" + "|" + "100PhpFiles";
        expectedTime = 1000;
        doMeasurement();
    }
}
