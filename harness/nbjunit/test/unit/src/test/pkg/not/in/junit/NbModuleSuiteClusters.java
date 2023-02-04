/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class NbModuleSuiteClusters extends TestCase {

    private static final Pattern PATTERN_DIR_NAME_FILTER_1 = Pattern.compile(".*/");
    private static final Pattern PATTERN_DIR_NAME_FILTER_2 = Pattern.compile("platform|harness|extra");

    public NbModuleSuiteClusters(String t) {
        super(t);
    }

    public void testSetClusters() {
        String dirs = System.getProperty("netbeans.dirs");
        assertNotNull("Dirs specified", dirs);

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String d : dirs.replace(File.separatorChar, '/').split(File.pathSeparator)) {
            String sd = PATTERN_DIR_NAME_FILTER_1.matcher(d).replaceFirst("");
            if (PATTERN_DIR_NAME_FILTER_2.matcher(sd).matches()) { // extra for libs.junit4
                continue;
            }
            sb.append(sep).append(sd);
            sep = ":";
        }
        System.setProperty("clusters", sb.toString());
    }
}
