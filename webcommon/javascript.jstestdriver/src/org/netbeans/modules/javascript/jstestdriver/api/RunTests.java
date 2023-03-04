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

package org.netbeans.modules.javascript.jstestdriver.api;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.jstestdriver.JSTestDriverSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public final class RunTests {

    private RunTests() {
    }

    public static void runAllTests(Project p, FileObject baseDirectory, FileObject configurationFile) {
        File base = FileUtil.toFile(baseDirectory);
        File config = FileUtil.toFile(configurationFile);
        JSTestDriverSupport.getDefault().runAllTests(p, base, config, "all"); //NOI18N
    }
    
}
