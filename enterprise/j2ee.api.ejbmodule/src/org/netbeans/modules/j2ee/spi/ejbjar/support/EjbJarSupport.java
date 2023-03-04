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

package org.netbeans.modules.j2ee.spi.ejbjar.support;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.openide.filesystems.FileObject;

/**
 * Factory for creating default implementations of EJB jar related interfaces.
 * 
 * @author kaktus
 */
public class EjbJarSupport {

    /**
     * Creates default implementation of {@link org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider}.
     */
    public static EjbJarProvider createEjbJarProvider(Project project, EjbJar ejbJar){
        return new EjbJarProviderImpl(project, ejbJar);
    }

    /**
     * Creates default implementation of {@link org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject}.
     */
    public static EjbJarsInProject createEjbJarsInProject(EjbJar ejbJar){
        return new EjbJarsInProjectImpl(ejbJar);
    }

    private static class EjbJarProviderImpl implements EjbJarProvider{
        private Project project;
        private EjbJar ejbJar;

        public EjbJarProviderImpl(Project project, EjbJar ejbJar) {
            this.project = project;
            this.ejbJar = ejbJar;
        }

        public EjbJar findEjbJar(FileObject file) {
            Project owner = FileOwnerQuery.getOwner (file);
            if (owner != null && owner == project) {
                return ejbJar;
            }
            return null;
        }
    }

    private static class EjbJarsInProjectImpl implements EjbJarsInProject{
        private EjbJar ejbJar;

        public EjbJarsInProjectImpl(EjbJar ejbJar) {
            this.ejbJar = ejbJar;
        }

        public EjbJar[] getEjbJars() {
            return new EjbJar [] {ejbJar};
        }
    }

}
