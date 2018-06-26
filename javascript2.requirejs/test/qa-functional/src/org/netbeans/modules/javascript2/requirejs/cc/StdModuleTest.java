/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.requirejs.cc;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.requirejs.GeneralRequire;

/**
 *
 * @author Vladimir Riha
 */
public class StdModuleTest extends GeneralRequire {

    public StdModuleTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(StdModuleTest.class).addTest(
                        "openProject",
                        "testDateMethod",
                        "testLiteralMethod",
                        "testLiteralMethodNested",
                        "testLiteralMethodNested2",
                        "testArrayMethod",
                        "testLiteralProperty",
                        "testLiteralPropertyNested",
                        "testModDateMethod",
                        "testModLiteralMethod",
                        "testModLiteralMethodNested",
                        "testModLiteralMethodNested2",
                        "testModArrayMethod",
                        "testModLiteralProperty",
                        "testModLiteralPropertyNested"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        StdModuleTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("index.html", "SimpleRequire");
        endTest();
    }

    public void testDateMethod() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 75);
        endTest();
    }

    public void testLiteralMethod() throws Exception { // issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 77);
        endTest();
    }

    public void testLiteralMethodNested() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 79);
        endTest();
    }

    public void testLiteralMethodNested2() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 81);
        endTest();
    }

    public void testArrayMethod() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 83);
        endTest();
    }

    public void testLiteralProperty() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 85);
        endTest();
    }

    public void testLiteralPropertyNested() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 87);
        endTest();
    }

    public void testModDateMethod() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 75);
        endTest();
    }

    public void testModLiteralMethod() throws Exception { // issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 77);
        endTest();
    }

    public void testModLiteralMethodNested() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 79);
        endTest();
    }

    public void testModLiteralMethodNested2() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 81);
        endTest();
    }

    public void testModArrayMethod() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 83);
        endTest();
    }

    public void testModLiteralProperty() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 85);
        endTest();
    }

    public void testModLiteralPropertyNested() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 87);
        endTest();
    }

    @Override
    public void tearDown() {
        if (!StdModuleTest.currentFile.equalsIgnoreCase("index.html")) {
            clearCurrentLine(new EditorOperator(StdModuleTest.currentFile));
        }
    }
}
