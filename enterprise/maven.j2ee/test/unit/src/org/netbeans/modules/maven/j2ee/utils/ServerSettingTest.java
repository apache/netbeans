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
package org.netbeans.modules.maven.j2ee.utils;

import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.j2ee.JavaEEMavenTestBase;
import org.netbeans.modules.maven.j2ee.SessionContent;

/**
 *
 * @author Martin Janicek
 */
public class ServerSettingTest extends JavaEEMavenTestBase {

    public ServerSettingTest(String name) {
        super(name);
    }

    public void testObtainServerIDs_withSetSessionID() {
        SessionContent session = project.getLookup().lookup(SessionContent.class);
        session.setServerInstanceId("SessionServerID"); // NOI18N

        String[] serverIDs = MavenProjectSupport.obtainServerIds(project);

        assertEquals("SessionServerID", serverIDs[0]); // NOI18N
        assertNull(serverIDs[1]);
    }

    public void testObtainServerIDs_withSetServerInstanceID() {
        JavaEEProjectSettings.setServerInstanceID(project, "InstanceServerID"); // NOI18N

        String[] serverIDs = MavenProjectSupport.obtainServerIds(project);

        assertEquals("InstanceServerID", serverIDs[0]); // NOI18N
        assertNull(serverIDs[1]);
    }

    public void testObtainServerIDs_withSetServerID() {
        MavenProjectSupport.setServerID(project, "ServerID"); // NOI18N

        String[] serverIDs = MavenProjectSupport.obtainServerIds(project);

        assertNull(serverIDs[0]);
        assertEquals("ServerID", serverIDs[1]); // NOI18N
    }

    public void testObtainServerIDs_withSetServerAndServerInstanceID() {
        JavaEEProjectSettings.setServerInstanceID(project, "InstanceServerID"); // NOI18N
        MavenProjectSupport.setServerID(project, "ServerID"); // NOI18N

        String[] serverIDs = MavenProjectSupport.obtainServerIds(project);

        assertEquals("InstanceServerID", serverIDs[0]); // NOI18N
        assertEquals("ServerID", serverIDs[1]); // NOI18N
    }
}
