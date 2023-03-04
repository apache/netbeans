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
package org.netbeans.test.mercurial.main.properties;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.mercurial.operators.HgPropertiesOperator;
import org.netbeans.test.mercurial.utils.MessageHandler;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author novakm
 */
public class HgPropertiesTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public PrintStream stream;
    static Logger log;

    public HgPropertiesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
        if (log == null) {
            log = Logger.getLogger(TestKit.LOGGER_NAME);
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(HgPropertiesTest.class).addTest("testHgPropertiesTest").enableModules(".*").clusters(".*"));
    }

    public void testHgPropertiesTest() throws Exception {
        try {
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());
            ProjectSupport.waitScanFinished();
            new EventTool().waitNoEvent(2000);
            
            // set hgProperty for file
            MessageHandler mh = new MessageHandler("Scanning mercurial properties");
            log.addHandler(mh);
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            HgPropertiesOperator hgpo = HgPropertiesOperator.invoke(node);
            TestKit.waitText(mh);
            new EventTool().waitNoEvent(2000);
            
            // username, default-pull and default-push should be in the table.
            JTableOperator jto = hgpo.propertiesTable();
            assertEquals("Wrong row count of table at start.", 3, jto.getRowCount());
            new EventTool().waitNoEvent(1000);
            assertEquals("1st row should be defaut-pull.", (String)(jto.getValueAt(0, 0)), "default-pull");
            new EventTool().waitNoEvent(1000);
            assertEquals("2nd row should be defaut-push.", (String)(jto.getValueAt(1, 0)), "default-push");
            new EventTool().waitNoEvent(1000);
            assertEquals("3rd row should be username.", (String)(jto.getValueAt(2, 0)), "username");
            new EventTool().waitNoEvent(1000);
            hgpo.ok();
            TestKit.closeProject(PROJECT_NAME);
            
//            assertEquals("Wrong row count of table at start.", 1, hgpo.propertiesTable().getRowCount());
//            hgpo.typePropertyName("default-push");
//            hgpo.typePropertyValue("fileValue");
//            hgpo.add();
//            new EventTool().waitNoEvent(500);
//            assertEquals("Wrong row count of table.", 2, hgpo.propertiesTable().getRowCount());
//            new EventTool().waitNoEvent(500);
//            
//            hgpo.propertiesTable().selectCell(1, 0);
//            hgpo.remove();
//            new EventTool().waitNoEvent(500);
//            assertEquals("Wrong row count of table after remove.", 1, hgpo.propertiesTable().getRowCount());
//            hgpo.cancel();
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
}
