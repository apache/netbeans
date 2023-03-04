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

package org.apache.tools.ant.module.spi;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/** Factory method for urls.
 *
 * @author Jaroslav Tulach
 */
final class AutomaticExtraClasspath implements AutomaticExtraClasspathProvider {
    private File file;


    private AutomaticExtraClasspath(File file) {
        this.file = file;
    }

    public static AutomaticExtraClasspathProvider url(Map<?,?> map) throws Exception {
        Object obj = map.get("url"); // NOI18N
        if (obj instanceof URL) {
            FileObject fo = URLMapper.findFileObject((URL)obj);
            File f = fo != null ? FileUtil.toFile(fo) : null;
            if (f == null) {
                Logger.getLogger(AutomaticExtraClasspathProvider.class.getName()).log(obj.toString().contains("com.jcraft.") ? Level.FINE : Level.WARNING, "No File found for {0}", obj);
            }
            return new AutomaticExtraClasspath(f);
        } else {
            throw new IllegalArgumentException("url arg is not URL: " + obj); // NOI18N
        }
    }

    @Override public File[] getClasspathItems() {
        return file != null ? new File[] {file} : new File[0];
    }
}
