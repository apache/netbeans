/**
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
package org.netbeans.test.installer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *

 */
public class Uninstaller {

    @org.junit.Test
    public void testUninstaller() {
        TestData data = new TestData(Logger.getLogger("global"));

        try {
            String wd = System.getenv("WORKSPACE");
            TestCase.assertNotNull(wd);
            data.setWorkDir(new File(wd));
        } catch (IOException ex) {
            TestCase.fail("Can not get WorkDir");
        }


        System.setProperty("nbi.dont.use.system.exit", "true");
        System.setProperty("nbi.utils.log.to.console", "false");
        System.setProperty("servicetag.allow.register", "false");
        System.setProperty("show.uninstallation.survey", "false");
        System.setProperty("user.home", data.getWorkDirCanonicalPath());

        Utils.phaseFive(data);
    }
}
