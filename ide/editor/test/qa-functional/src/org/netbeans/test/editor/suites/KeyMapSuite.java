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
package org.netbeans.test.editor.suites;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.editor.shortcuts.BasicShortcuts;
import org.netbeans.test.editor.suites.keybindings.KeyMapTest;

/**
 *
 * @author Jiri Prox Jiri.Prox@SUN.Com
 */
public class KeyMapSuite {

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(KeyMapTest.class)
                    .addTest(KeyMapTest.class, "testVerify")
                    .addTest(KeyMapTest.class, "testAddDuplicateCancel")
                    .addTest(KeyMapTest.class, "testAddShortcut")
                    .addTest(KeyMapTest.class, "testUnassign")
                    .addTest(KeyMapTest.class, "testAssignAlternativeShortcut")
                    //.addTest(KeyMapTest.class, "testProfileRestore")//fails due to issue 151254
                    .addTest(KeyMapTest.class, "testProfileDuplicte")
                    .addTest(KeyMapTest.class, "testHelp")                                    
                    .clusters(".*").enableModules(".*"));
    }
}
