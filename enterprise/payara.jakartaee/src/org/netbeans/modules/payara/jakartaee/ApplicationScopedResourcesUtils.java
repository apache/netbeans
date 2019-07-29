/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.jakartaee;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.util.JndiNamespacesDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public final class ApplicationScopedResourcesUtils {

    private ApplicationScopedResourcesUtils() {
        super();
    }

    public static ResourceFileDescription checkNamespaces(J2eeModule module, ResourceFileDescription fileDesc, String namespace) {
        // we need to check and possibly ask for jndi namespace
        if (fileDesc.isIsApplicationScoped() && namespace == null) {
            Set<String> ns = fileDesc.getNamespaces();
            if (!ns.contains(JndiNamespacesDefinition.APPLICATION_NAMESPACE)
                    && !ns.contains(JndiNamespacesDefinition.MODULE_NAMESPACE)) {

                String customNamespace = null;
                if (ApplicationScopedResourcesUtils.isEarChild(fileDesc.getFile())) {
                    customNamespace = JndiNamespacesDefinition.MODULE_NAMESPACE;
                } else {
                    customNamespace = JndiNamespacesDefinition.APPLICATION_NAMESPACE;
                }
                return new ResourceFileDescription(fileDesc.getFile(),
                        fileDesc.isIsApplicationScoped(), Collections.singleton(customNamespace));
            }
        }
        return fileDesc;
    }

    public static String getJndiName(String jndiName, ResourceFileDescription fileDesc) {
        if (!fileDesc.isIsApplicationScoped()) {
            return jndiName;
        }

        String realJndiName = jndiName;
        Set<String> ns = fileDesc.getNamespaces();
        if (ns.isEmpty()) {
            // should not happen
            realJndiName = JndiNamespacesDefinition.normalize(
                    realJndiName, JndiNamespacesDefinition.APPLICATION_NAMESPACE);
        } else {
            if (ns.contains(JndiNamespacesDefinition.MODULE_NAMESPACE)) {
                realJndiName = JndiNamespacesDefinition.normalize(
                        realJndiName, JndiNamespacesDefinition.MODULE_NAMESPACE);
            } else {
                realJndiName = JndiNamespacesDefinition.normalize(
                        realJndiName, JndiNamespacesDefinition.APPLICATION_NAMESPACE);
            }
        }
        return realJndiName;
    }

    public static boolean isEarChild(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        assert fo != null;
        if (fo == null) {
            return false;
        }
        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            return false;
        }
        return isEarChild(p);
    }

    public static boolean isEarChild(Project project) {
        J2eeModuleProvider childProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (childProvider == null || childProvider instanceof J2eeApplicationProvider) {
            return false;
        }
        // FIXME if the ear is closed it wont find it; is it a problem?
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        for (Project candidate : allProjects) {
            J2eeApplicationProvider app = candidate.getLookup().lookup(J2eeApplicationProvider.class);
            if (app != null) {
                for (J2eeModuleProvider p : app.getChildModuleProviders()) {
                    if (p.equals(childProvider)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static interface JndiNameResolver {

        String resolveJndiName(String jndiName);
    }

    public static class ResourceFileDescription {

        private final File file;

        private final boolean isApplicationScoped;

        private final Set<String> namespaces;

        public ResourceFileDescription(File file, boolean isApplicationScoped, Set<String> namespaces) {
            this.file = file;
            this.isApplicationScoped = isApplicationScoped;
            this.namespaces = namespaces;
        }

        public File getFile() {
            return file;
        }

        public boolean isIsApplicationScoped() {
            return isApplicationScoped;
        }

        public Set<String> getNamespaces() {
            return namespaces;
        }
    }
}
