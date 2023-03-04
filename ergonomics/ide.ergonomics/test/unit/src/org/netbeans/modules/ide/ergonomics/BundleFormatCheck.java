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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class BundleFormatCheck extends NbTestCase {

    public BundleFormatCheck(String name) {
        super(name);
    }
    
    public void testVerifyBundleFormat() throws Exception {
        String clusters = System.getProperty("netbeans.dirs");
        assertNotNull("Clusters are specified", clusters);
        
        int cnt = 0;
        String[] paths = clusters.split(File.pathSeparator);
        for (String c : paths) {
            int last = c.lastIndexOf(File.separatorChar);
            String clusterName = c.substring(last + 1).replaceFirst("[0-9\\.]*$", "");
            String basename = "/org/netbeans/modules/ide/ergonomics/" + clusterName;
            String bundleName = basename + "/Bundle.properties";
            URL bundle = FeatureManager.class.getResource(bundleName);
            if (bundle != null) {
                checkBundle(bundle);
                cnt++;
            }
        }
        
        if (cnt == 0) {
            fail("Found no bundles to check for " + clusters);
        }
    }

    private void checkBundle(URL bundle) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(bundle.openStream()));
        for (int num = 0; ;num++) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("#")) {
                continue;
            }
            if (line.contains("=")) {
                while (line.endsWith("\\")) {
                    line = r.readLine();
                }
                continue;
            }
            if (line.trim().isEmpty()) {
                continue;
            }
            fail("Unexpected line " + num + " in " + bundle + ":\n" + line);
        }
    }
}
