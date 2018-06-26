/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.javaee;

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
