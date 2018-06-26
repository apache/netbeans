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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.requirejs.GeneralRequire;

/**
 *
 * @author Vladimir Riha
 */
public class FileConfigurationTest extends GeneralRequire {

    private static String originalContent;
    private static String uncommentedContent;
    private static boolean setup = false;
    private static int numberOfTests = 3;

    public FileConfigurationTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(FileConfigurationTest.class).addTest(
                        "openProject",
                        "testBaseUrl",
                        "testBaseUrlNested"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        FileConfigurationTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("js|main2.js", "SimpleRequire");
        FileConfigurationTest.originalContent = openFile("js|main2.js", "SimpleRequire").getText();
        endTest();
    }

    public void testBaseUrl() throws Exception {
        startTest();
        testCompletion(openFile("js|main2.js", "SimpleRequire"), 8);
        endTest();
    }

    public void testBaseUrlNested() throws Exception {
        startTest();
        testCompletion(openFile("js|main2.js", "SimpleRequire"), 11);
        endTest();
    }

      @Override
    public void tearDown() {

        FileConfigurationTest.numberOfTests--;

        if (FileConfigurationTest.numberOfTests > 0 && FileConfigurationTest.uncommentedContent == null) {
            return;
        }
        EditorOperator eo = openFile("js|main2.js", "SimpleRequire");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(KeyEvent.VK_DELETE);

        if (FileConfigurationTest.numberOfTests == 0) {
            eo.insert(FileConfigurationTest.originalContent);
        } else {
            eo.insert(FileConfigurationTest.uncommentedContent);
        }
        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(1000);

    }

    @Override
    public void setUp() {
        if (FileConfigurationTest.originalContent != null && !FileConfigurationTest.setup) {
            EditorOperator eo = openFile("js|main2.js", "SimpleRequire");
            eo.typeKey('a', InputEvent.CTRL_MASK);
            eo.typeKey('/', InputEvent.CTRL_MASK);
            eo.clickMouse();
            eo.save();
            evt.waitNoEvent(1000);
            FileConfigurationTest.setup = true;
            FileConfigurationTest.uncommentedContent = eo.getText();
        }
    }
    
}
