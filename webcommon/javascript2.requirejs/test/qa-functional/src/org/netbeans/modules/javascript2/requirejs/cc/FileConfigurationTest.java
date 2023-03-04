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
