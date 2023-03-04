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

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileUtil;

/**
 * Interface for deploying test modules.
 * @author Jesse Glick
 * @since org.netbeans.core/1 1.1
 */
public final class TestModuleDeployer {

    /**
     * Deploy a module in test mode.
     * You pass the JAR file.
     * Module system figures out the rest (i.e. whether it needs
     * to be installed, reinstalled, etc.).
     * The deployment is run synchronously so do not call this
     * method from a sensitive thread (e.g. event queue), nor
     * call it with any locks held. Best to call it e.g. from the
     * execution engine.
     * @param jar the path to the module JAR
     * @throws IOException if there is some error in the process
     */
    public static void deployTestModule(File jar) throws IOException {
        Main.getModuleSystem().deployTestModule(FileUtil.normalizeFile(jar));
    }
    
}
