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
package org.netbeans.modules.performance.utilities;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;

/**
 *
 * @author petr cyhelsky
 */
public class PerformanceTestCase2 extends JellyTestCase {

    public PerformanceTestCase2(String testName) {
        super(testName);
    }

    @Override
    public void openDataProjects(String... projects) throws IOException {
        String[] fullPaths = new String[projects.length];
        String altPath = System.getProperty("nb_perf_alt_path");
        for (int i = 0; i < projects.length; i++) {
            if (altPath != null) {
                fullPaths[i] = mergePaths(altPath, getDataDir().getAbsolutePath()) + File.separator + projects[i];
            } else {
                fullPaths[i] = getDataDir().getAbsolutePath() + File.separator + projects[i];
            }
        }

        openProjects(fullPaths);
    }

    private String mergePaths(String altPath, String absolutePath) {
        if (absolutePath.startsWith(altPath)) {
            return absolutePath;
        }
        final String PERF = "performance"; //NOI18N
        StringBuilder sb;
        if (absolutePath.contains(PERF)) {
            sb = new StringBuilder(absolutePath);
            sb.replace(0, absolutePath.indexOf(PERF) + 11, altPath);
        } else {
            sb = new StringBuilder(altPath);
        }
        return sb.toString();
    }

}
