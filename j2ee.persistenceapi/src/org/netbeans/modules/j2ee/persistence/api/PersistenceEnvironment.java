/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
