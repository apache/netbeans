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
package org.netbeans.modules.j2ee.persistence.api;

import java.net.URL;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * helper class to get project based environment info
 * todo: consider to combine with scope/location providers
 * @author sp153251
 */
public interface PersistenceEnvironment {
    /**
     * Prepares and returns a custom classloader for this project.
     * The classloader is capable of loading project classes and resources.
     * 
     * @param classpaths, custom classpaths that are registered along with project based classpath.
     * @return classloader which is a URLClassLoader instance.
     */
    ClassLoader getProjectClassLoader(URL[] classpaths);

    /**
     * Returns the NetBeans project to which this HibernateEnvironment instance is bound.
     *
     * @return NetBeans project.
     */
    Project getProject();

    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     *
     * @param projectFile file in current project, may not be used in method realization
     * @return List of java.io.File objects representing each entry on the classpath.
     */
    List<URL> getProjectClassPath(FileObject projectFile);

    public List<URL> getProjectClassPath();
    
    /**
     * 
      * @return return persistence.xml location
     */
    FileObject getLocation();
}
