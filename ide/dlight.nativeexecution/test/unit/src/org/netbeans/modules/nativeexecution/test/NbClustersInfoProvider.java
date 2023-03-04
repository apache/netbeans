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
package org.netbeans.modules.nativeexecution.test;

import java.io.File;
import java.io.FileFilter;
import java.util.TreeSet;
import org.junit.Assert;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author akrasny
 */
public final class NbClustersInfoProvider {

    private NbClustersInfoProvider() {
    }

    public static String getClusters() {
        // Setting netbeans.dirs makes installedFileLocator work properly
        File[] clusters = findClusters();
        StringBuilder sb = new StringBuilder();
        for (File cluster : clusters) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(cluster.getPath());
        }
        return sb.toString();
    }

    // it's like what org.netbeans.junit.NbModuleSuite does,
    // but reusing NbModuleSuite will cause too massive changes in existing CND tests
    private static File[] findClusters() {
        File netbeans = findNetbeans();
        assert netbeans != null;
        File[] clusters = netbeans.listFiles(new FileFilter() {

            @Override
            public boolean accept(File dir) {
                if (dir.isDirectory()) {
                    File m = new File(new File(dir, "config"), "Modules");
                    return m.exists();
                }
                return false;
            }
        });
        return clusters;
    }

    // it's like what org.netbeans.junit.NbModuleSuite does,
    // but reusing NbModuleSuite will cause too massive changes in existing CND tests
    private static File findNetbeans() {
        try {
            Class<?> lookup = Class.forName("org.openide.util.Lookup"); // NOI18N
            File util = new File(lookup.getProtectionDomain().getCodeSource().getLocation().toURI());
            Assert.assertTrue("Util exists: " + util, util.exists());
            return util.getParentFile().getParentFile().getParentFile();
        } catch (Exception ex) {
            try {
                File nbjunit = new File(NbModuleSuite.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File harness = nbjunit.getParentFile().getParentFile();
                Assert.assertEquals("NbJUnit is in harness", "harness", harness.getName());
                TreeSet<File> sorted = new TreeSet<>();
                File[] listFiles = harness.getParentFile().listFiles();
                if (listFiles != null) {
                    for (File p : listFiles) {
                        if (p.getName().startsWith("platform")) {
                            sorted.add(p);
                        }
                    }
                }
                Assert.assertFalse("Platform shall be found in " + harness.getParent(), sorted.isEmpty());
                return sorted.last();
            } catch (Exception ex2) {
                Assert.fail("Cannot find utilities JAR: " + ex + " and: " + ex2);
            }
            return null;
        }
    }
}
