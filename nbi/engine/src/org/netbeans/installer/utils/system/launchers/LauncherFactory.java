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

package org.netbeans.installer.utils.system.launchers;

import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.launchers.impl.CommandLauncher;
import org.netbeans.installer.utils.system.launchers.impl.ExeLauncher;
import org.netbeans.installer.utils.system.launchers.impl.JarLauncher;
import org.netbeans.installer.utils.system.launchers.impl.ShLauncher;

/**
 *
 * @author Dmitry Lipin 
 */
public final class LauncherFactory {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static Launcher newLauncher(
            final LauncherProperties properties, 
            final Platform platform) {
        if (platform.isCompatibleWith(Platform.WINDOWS)) {
            return new ExeLauncher(properties);
        }

        if (platform.isCompatibleWith(Platform.MACOSX)) {
            return new CommandLauncher(properties);
        }
        
        if (platform.isCompatibleWith(Platform.UNIX)) {
            return new ShLauncher(properties);
        }
        
        
        return new JarLauncher(properties);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private LauncherFactory() {
    }
}
