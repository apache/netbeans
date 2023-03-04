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

package org.netbeans.modules.csl.core;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;

/**
 * Supplies classpath information according to project file owner.
 * This is already available in j2seproject, but when the java support
 * is not present it causes user source paths not to be indexed etc.
 * 
 * @author Jesse Glick
 */
public class ProjectClassPathProvider implements ClassPathProvider {

    /** Default constructor for lookup. */
    public ProjectClassPathProvider() {}
    
    public ClassPath findClassPath(FileObject file, String type) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p != null) {
            ClassPathProvider cpp = p.getLookup().lookup(ClassPathProvider.class);
            if (cpp != null) {
                return cpp.findClassPath(file, type);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
}
