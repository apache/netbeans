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
package org.netbeans.spi.java.project.classpath;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.openide.filesystems.FileObject;

/**
 * Allows to update the module declaration in projects which support JDK9. For modular projects,
 * it is not sufficient just to add artifacts on classpath - modular projects cannot access unnamed module,
 * so the project would not see such classes/resources during compilation. Even if added to the module path,
 * a directive needs to be added so that compiler and runtime honour the dependency.
 * <p/>
 * This SPI allows the project implementation for modular project to diverge additions to a more suitable classpath,
 * and to update the module information.
 * <p/>
 * Project-specific implementation should be placed in <b>project lookup</b>. If not present, default implementation
 * will handle the requests, so that artifacts, libraries and projects will land on {@link JavaClassPathConstants#MODULE_COMPILE_PATH}
 * or {@link JavaClassPathConstants#MODULE_EXECUTE_PATH} respectively. The default implementation does nothing for projects (sources), which do
 * not use {@code module-info} declaration, although they target JDK 9.
 * 
 * @author sdedic
 * @since 1.70
 */
public interface ProjectModulesModifier {
    /**
     * Translates the classpath type into a proper modular one, if necessary. If the project
     * is modular, artifacts added to {@link ClassPath#COMPILE} does not make sense, since it
     * is inaccessible (without specific startup parameters), so modular project may need to
     * diverge the request to some other classpath type.
     * If the project is not modular, or modular project can use artifacts from the original classpath
     * type, the value of {@code null} should be returned. If {@code null} is returned,
     * the {@link #addRequiredModules} or {@link #removeRequiredModuels} should not be called.
     * 
     * @param classPathType original classpath type
     * @param projectArtifact source, directory which will determine the proper {@code module-info.java}
     * @return modular replacement for the {@code classPathType}.
     */
    public String   provideModularClasspath(FileObject projectArtifact, String classPathType);
    
    /**
     * Adds "requires" clauses to module-info. "requires" directive will be created
     * for each of the module names in module-info which applies to the "source" file. 
     * The implementation may ignore requests for classpath types, which do not interact with the
     * module system.
     * 
     * @param projectArtifact source, directory which will determine the proper {@code module-info.java}
     * @param locations module names to add required directives for
     * @param pathType the original path type
     * @return true, if some modification has been made
     * @throws IOException on error
     */
    public boolean  addRequiredModules(String pathType, FileObject projectArtifact, Collection<URL> locations) throws IOException;

    /**
     * Removes "requires" clauses from module-info. Requires directives will be removed for all
     * modules in the "moduleNames".
     * The implementation may ignore requests for classpath types, which do not interact with the
     * module system.
     * 
     * @param projectArtifact source, directory which will determine the proper {@code module-info.java}
     * @param locations module names whose required directives should be removed
     * @param pathType the original path type
     * @return true, if some modification has been made
     * @throws IOException on error
     */
    public boolean  removeRequiredModules(String pathType, FileObject projectArtifact, Collection<URL> locations) throws IOException;
    
    /**
     * Finds source groups which use the specified modules. The implementation should find all SourceGroups
     * whose configuation (not content! like individual java sources) references the specified library/module.
     * The call is used, for example, to determine whether removal of a library would break some modules, and 
     * what those modules are.
     * 
     * @param projectArtifact project artifact a file owned by a project.
     * @param locations locations whose usage should be found
     * @return usage map
     */
    @NonNull
    public Map<URL, Collection<ClassPath>> findModuleUsages(FileObject projectArtifact, Collection<URL> locations);
}
