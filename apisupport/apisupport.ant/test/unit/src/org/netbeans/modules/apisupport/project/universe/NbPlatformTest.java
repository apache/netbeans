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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.TestFileUtils;

public class NbPlatformTest extends NbTestCase {

    public NbPlatformTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testIsSupportedPlatform() throws Exception {
        {
            File plaf = new File(getWorkDir(), "plaf1");
            TestFileUtils.writeZipFile(new File(plaf, "platform.test/core/core.jar"));
            assertFalse(NbPlatform.isPlatformDirectory(plaf));
            assertFalse(NbPlatform.isSupportedPlatform(plaf));
        }
        {
            File plaf = new File(getWorkDir(), "plaf2");
            TestFileUtils.writeZipFile(new File(plaf, "platform5/core/core.jar"));
            assertTrue(NbPlatform.isPlatformDirectory(plaf));
            assertFalse(NbPlatform.isSupportedPlatform(plaf));
        }
        {
            File plaf = new File(getWorkDir(), "plaf3");
            TestFileUtils.writeZipFile(new File(plaf, "platform6/core/core.jar"));
            assertTrue(NbPlatform.isPlatformDirectory(plaf));
            assertTrue(NbPlatform.isSupportedPlatform(plaf));
        }
        {
            File plaf = new File(getWorkDir(), "plaf4");
            TestFileUtils.writeZipFile(new File(plaf, "platform/core/core.jar"));
            assertTrue(NbPlatform.isPlatformDirectory(plaf));
            assertTrue(NbPlatform.isSupportedPlatform(plaf));
        }
    }

}
