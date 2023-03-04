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

package org.netbeans.modules.ide.ergonomics;

import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AllClustersProcessedCheck extends NbTestCase {
    public AllClustersProcessedCheck(String n) {
        super(n);
    }
    
    public void testAllClustersProcessedCheck() {
        String clusters = System.getProperty("netbeans.dirs");
        assertNotNull("clusters OK", clusters);
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        assertNotNull("Classloader found", l);

        StringBuilder sb = new StringBuilder();
        for (String c : clusters.split(File.pathSeparator)) {
            String n = new File(c).getName().replaceFirst("[\\.0-9]+$", "");
            if (n.equals("platform")) {
                continue;
            }
            if (n.equals("harness")) {
                continue;
            }
            if (n.equals("ide")) {
                continue;
            }
            if (n.equals("extide")) {
                continue;
            }
            if (n.equals("ergonomics")) {
                continue;
            }
            if (n.equals("extra")) {
                continue;
            }
            if (n.equals("nb")) {
                continue;
            }
            
            URL u = l.getResource("org/netbeans/modules/ide/ergonomics/" + n + "/Bundle.properties");
            if (u == null) {
                sb.append("Missing ").append(n).append('\n');
            }
        }

        if (sb.length() > 0) {
            fail("Cannot find some clusters:\n" + sb);
        }
    }

    public void testVerifyThatAllErgoClustersAreDisabled() {
        CharSequence log = Log.enable("org.netbeans.modules.ide.ergonomics", Level.ALL);
        int cnt = FeatureManager.dumpModules();
        assertEquals("No module enabled:\n" + log, 0, cnt);
    }
}
