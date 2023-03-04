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

package test.pkg.not.in.junit;

import java.io.File;
import junit.framework.TestCase;

public class NbModuleSuiteTimestamps extends TestCase {

    public NbModuleSuiteTimestamps(String t) {
        super(t);
    }

    public void testCheckUserDirStamps() {
        long current = Long.getLong("stamps", -1);
        File userDir = new File(System.getProperty("netbeans.user"));
        assertTrue("User dir exists: " + userDir, userDir.exists());
        File[] newest = new File[1];
        long now = stamps(userDir, current, newest);
        if (current >= 0) {
            assertEquals("Old and new value is the same, newest for " + newest[0], current, now);
        }
        System.setProperty("stamps", String.valueOf(now));
    }

    private static long stamps(File f, long current, File[] newest) {
        if (f.isDirectory()) {
            for (File subFile : f.listFiles()) {
                current = stamps(subFile, current, newest);
            }
        } else {
            if (f.getName().endsWith("xml")) {
                long s = f.lastModified();
                if (s > current) {
                    current = s;
                    newest[0] = f;
                }
            }
        }
        return current;
    }
}
