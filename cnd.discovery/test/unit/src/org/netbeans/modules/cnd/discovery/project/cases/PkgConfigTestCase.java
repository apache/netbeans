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

package org.netbeans.modules.cnd.discovery.project.cases;

import org.junit.Test;
import org.netbeans.modules.cnd.discovery.project.MakeProjectTestBase;
import org.openide.util.Utilities;

/**
 *
 */
public class PkgConfigTestCase extends MakeProjectTestBase {

    public PkgConfigTestCase() {
        super("PkgConfig");
    }

    @Test
    public void testPkgConfig() throws Exception {
        if (Utilities.isWindows()) {
            // configure script requires more then 10 minutes
            return;
        }
        // see also RepositoryValidationBase 
        performTestProject("http://pkgconfig.freedesktop.org/releases/pkg-config-0.25.tar.gz", null, false, "");
    }
}
