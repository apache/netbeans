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

package org.netbeans.modules.maven.customizer;

import org.netbeans.modules.maven.TestChecker;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mkleint
 */
public class ActionMappingsTest {

    public ActionMappingsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testSkipTestsAction() throws Exception {
        JTextArea area = new JTextArea();
        area.setText("");
        ActionMappings.SkipTestsAction act = new ActionMappings.SkipTestsAction(area);
        act.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "X"));
        assertTrue(area.getText().contains(TestChecker.PROP_SKIP_TEST + "=true"));

        area.setText(TestChecker.PROP_SKIP_TEST + "=false");
        act.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "X"));
        assertTrue(area.getText().contains(TestChecker.PROP_SKIP_TEST + "=true"));

        area.setText(TestChecker.PROP_SKIP_TEST + " = false\nyyy=xxx");
        act.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "X"));
        assertTrue(area.getText().contains(TestChecker.PROP_SKIP_TEST + "=true"));

        area.setText("aaa=bbb\n" + TestChecker.PROP_SKIP_TEST + " =    false   \nyyy=xxx");
        act.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "X"));
        assertTrue(area.getText().contains(TestChecker.PROP_SKIP_TEST + "=true"));
    }
}
