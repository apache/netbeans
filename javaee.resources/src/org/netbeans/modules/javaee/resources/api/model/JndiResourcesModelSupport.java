/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.resources.api.model;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 * Helper class for getting JndiResources model related data.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class JndiResourcesModelSupport {

    private static final Logger LOGGER = Logger.getLogger(JndiResourcesModelSupport.class.getName());

    private JndiResourcesModelSupport() {
    }

    public static final Map<Project, WeakReference<MetadataModel<JndiResourcesModel>>> MODELS =
            new WeakHashMap<Project, WeakReference<MetadataModel<JndiResourcesModel>>>();

    /**
     * Return JndiResourcesModel for current project.
     * @return JndiResourcesModel
     */
    public static MetadataModel<JndiResourcesModel> getModel(Project project) {
        WeakReference<MetadataModel<JndiResourcesModel>> reference = MODELS.get(project);
        MetadataModel<JndiResourcesModel> metadataModel = null;
        if (reference != null) {
            metadataModel = reference.get();
        }
        if (metadataModel == null) {
            JndiResourcesModelUnit modelUnit = getModelUnit(project);
            LOGGER.log(Level.FINE, "Metadata model not found in cache for model unit: {0}, reference: {1}",new Object[]{modelUnit, reference});
            if (modelUnit == null) {
                return null;
            }
            metadataModel = JndiResourcesModelFactory.createMetaModel(modelUnit);
            reference = new WeakReference<MetadataModel<JndiResourcesModel>>(metadataModel);
            MODELS.put(project, reference);
        }
        return metadataModel;
    }

    /**
     * Returns JndiResourcesModelUnit for current project.
     * @return JndiResourcesModelUnit holding all ClassPaths
     */
    private static JndiResourcesModelUnit getModelUnit(Project project) {
        if (project == null) {
            return null;
        }
        ClassPath boot = getClassPath(project, ClassPath.BOOT);
        ClassPath compile = getClassPath(project, ClassPath.COMPILE);
        ClassPath src = getClassPath(project, ClassPath.SOURCE);
        return JndiResourcesModelUnit.create(boot, compile, src);
    }

    /**
     * Returns merged classpath for given type.
     * @param type a classpath type such as {@link ClassPath#COMPILE}
     * @return generated read-only project's classpath of given type
     */
    private static ClassPath getClassPath(Project project, String type) {
        ClassPathProvider provider = project.getLookup().lookup(ClassPathProvider.class);
        if (provider == null) {
            return null;
        }

        Sources sources = ProjectUtils.getSources(project);
        if (sources == null) {
            return null;
        }

        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath[] paths = new ClassPath[sourceGroups.length];
        int i = 0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[i] = provider.findClassPath(rootFolder, type);
            i++;
        }
        return ClassPathSupport.createProxyClassPath(paths);
    }

}
