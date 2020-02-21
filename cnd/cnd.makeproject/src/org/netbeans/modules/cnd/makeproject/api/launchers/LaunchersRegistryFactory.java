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
package org.netbeans.modules.cnd.makeproject.api.launchers;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;

/**
 *
 */
public final class LaunchersRegistryFactory {
    private static final HashMap<FileObject, LaunchersRegistry> instances = new HashMap<>();
    private static final Logger LOG = Logger.getLogger("LaunchersRegistry");//NOI18N

    public static synchronized LaunchersRegistry getInstance(FileObject projectDirectory) {
        LaunchersRegistry launcherRegistryInstance = instances.get(projectDirectory);
        if (launcherRegistryInstance == null) {
            try {
                launcherRegistryInstance = new LaunchersRegistry();
                instances.put(projectDirectory, launcherRegistryInstance);
            } catch (Exception e) {
                LOG.log(Level.INFO, "LauncherList - getInstance - e {0}", e); //FIXUP //NOI18N
                LOG.info("Cannot restore LauncherList ..."); //FIXUP //NOI18N
            }
        }
        return launcherRegistryInstance;
    }


}
