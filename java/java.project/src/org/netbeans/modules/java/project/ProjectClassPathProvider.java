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

package org.netbeans.modules.java.project;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;

/**
 * Supplies classpath information according to project file owner.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.classpath.ClassPathProvider.class, position=100)
public class ProjectClassPathProvider implements ClassPathProvider {

    private static final Logger LOG = Logger.getLogger(ProjectClassPathProvider.class.getName());

    /** Default constructor for lookup. */
    public ProjectClassPathProvider() {}
    
    public ClassPath findClassPath(FileObject file, String type) {
        Project p = FileOwnerQuery.getOwner(file);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "findClassPath({0}, {1}) on project {2}", new Object[] {file, type, p});
        }
        if (p != null) {
            ClassPathProvider cpp = p.getLookup().lookup(ClassPathProvider.class);
            if (cpp != null) {
                final ClassPath result = cpp.findClassPath(file, type);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "findClassPath({0}, {1}) -> {2} from {3}", new Object[] {file, type, result, cpp});
                }
                return result;
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "cpp.findClassPath({0}, {1}) -> null", new Object[] {file, type});
                }
                return null;
            }
        } else {
            return null;
        }
    }
    
}
