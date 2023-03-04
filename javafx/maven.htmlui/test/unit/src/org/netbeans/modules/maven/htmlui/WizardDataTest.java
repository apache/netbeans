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
package org.netbeans.modules.maven.htmlui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class WizardDataTest {
    private WizardData wd;

    @Before
    public void setupModel() {
        wd = new WizardData();
        wd.setIosMoe(false);
        wd.setIos(false);
        wd.setIosRoboVM(false);
    }


    @Test
    public void moeOnlyEnabledIfIosEnabled() {
        assertNull("No moe path", wd.getMoepath());
        wd.setIosMoe(true);
        assertNull("No moe path yet", wd.getMoepath());
        wd.setIos(true);
        assertEquals("Now moe is enabled", "client-moe", wd.getMoepath());
        assertNull("no robovm", wd.getIospath());
    }

    @Test
    public void robovmOnlyEnabledIfIosEnabled() {
        wd.setIosRoboVM(true);
        wd.setIos(false);
        assertNull("No robovm path", wd.getIospath());
        wd.setIos(true);
        assertEquals("Now robovm is enabled", "client-ios", wd.getIospath());
        assertNull("No moe path", wd.getMoepath());
    }
}