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

package org.netbeans.junit;


import java.net.URISyntaxException;
import test.pkg.not.in.junit.NbModuleSuiteIns;
import test.pkg.not.in.junit.NbModuleSuiteT;
import test.pkg.not.in.junit.NbModuleSuiteS;
import java.io.File;
import java.util.Properties;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import test.pkg.not.in.junit.*;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuiteHideExtraTest extends TestCase {

    public NbModuleSuiteHideExtraTest(String testName) {
        super(testName);
    }

    public void testWinSysOnByDefault() {
        System.setProperty("winsys.on", "don't know");
        Test instance = NbModuleSuite.createConfiguration(NbModuleSeekWinSys.class).gui(false).suite();
        junit.textui.TestRunner.run(instance);

        assertEquals("core.windows is on", "true", System.getProperty("winsys.on"));
    }

    public void testWinSysCanBeHidden() {
        System.setProperty("winsys.on", "don't know");
        Test instance = NbModuleSuite.createConfiguration(NbModuleSeekWinSys.class).hideExtraModules(true).gui(false).suite();
        junit.textui.TestRunner.run(instance);

        assertEquals("core.windows is on", "hidden", System.getProperty("winsys.on"));
    }
}
