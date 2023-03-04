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

package org.netbeans.modules.debugger.jpda.truffle;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;

public class RemoteServicesNGTest {

    public RemoteServicesNGTest() {
    }

    @Test
    public void verifyTruffleBackendResourceExists() throws IOException {
        final InputStream is = RemoteServices.openRemoteClasses();
        assertNotNull(is);
        JarInputStream jar = new JarInputStream(is);
        for (;;) {
            ZipEntry entry = jar.getNextEntry();
            if (entry == null) {
                fail("org.netbeans.modules.debugger.jpda.backend.truffle.JPDATruffleAccessor not found");
            }
            if (entry.getName().equals("org/netbeans/modules/debugger/jpda/backend/truffle/JPDATruffleAccessor.class")) {
                // OK
                return;
            }
        }
    }

}
