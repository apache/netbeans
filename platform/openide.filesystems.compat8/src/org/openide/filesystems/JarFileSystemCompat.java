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

package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import org.openide.modules.ConstructorDelegate;
import org.openide.modules.PatchFor;

/**
 * Support for compatibility with NB 8.0 and earlier.
 * 
 * @author sdedic
 */
@PatchFor(JarFileSystem.class)
public abstract class JarFileSystemCompat extends AbstractFileSystem {
    public JarFileSystemCompat() {
        super();
    }
    
    @ConstructorDelegate
    public static void createJarFileSystemCompat(JarFileSystemCompat jfs, FileSystemCapability cap) throws IOException {
        FileSystemCompat.compat(jfs).setCapability(cap);
    }
    
    /** Prepare environment for external compilation or execution.
    * <P>
    * Adds name of the ZIP/JAR file, if it has been set, to the class path.
     * @deprecated Useless.
    */
    @Deprecated
    public void prepareEnvironment(FileSystem$Environment env) {
        if (getJarFile() != null) {
            env.addClassPath(getJarFile().getAbsolutePath());
        }
    }
    
    public abstract File getJarFile();
}
