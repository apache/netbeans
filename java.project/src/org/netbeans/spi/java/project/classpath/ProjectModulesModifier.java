/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
