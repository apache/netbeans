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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import org.openide.modules.InstalledFileLocator;

/**
 * Points to ant.jar for unit tests.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class)
public final class TestInstalledFileLocator extends InstalledFileLocator {

    private final File antHome;

    /** Default instance for lookup. */
    public TestInstalledFileLocator() {
        String anthome = System.getProperty("test.ant.home");
        assert anthome != null : "Must set system property test.ant.home";
        antHome = new File(anthome);
        assert antHome.isDirectory() : "No such dir " + antHome;
    }

    public File locate(String relativePath, String codeNameBase, boolean localized) {
        // Simulate effect of having an Ant-task-providing module in user dir:
        if (relativePath.equals("ant")) {
            return new File("/my/user/dir/ant");
        } else if (relativePath.equals("ant/nblib/bridge.jar") || relativePath.equals("ant/lib/ant.jar")) {
            File f = new File(antHome, relativePath.substring(4).replace('/', File.separatorChar));
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }
    
}
