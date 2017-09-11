/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.api.beans.model;

import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 * Like helper class for getting Spring model related inner, outer data.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class SpringMetaModelSupport {

    private final Project project;
    // copied from WebProjectConstants.TYPE_WEB_INF to save one dependency
    private static final String TYPE_WEB_INF = "web_inf"; //NOI18N

    public SpringMetaModelSupport(Project project) {
        this.project = project;
    }

    /**
     * Return {@link MetadataModel} for current project.
     * @return meta model
     */
    public MetadataModel<SpringModel> getMetaModel() {
        ModelUnit modelUnit = getModelUnit();
        if (modelUnit == null) {
            return null;
        }
        return SpringModelFactory.getMetaModel(modelUnit);
    }

    /**
     * Returns {@link ModelUnit} for current project.
     * @return {@code ModelUnit} holding all {@code ClassPath}es
     */
    public ModelUnit getModelUnit() {
        if (project == null) {
            return null;
        }
        ClassPath boot = getClassPath(ClassPath.BOOT);
        ClassPath compile = getClassPath(ClassPath.COMPILE);
        ClassPath src = getClassPath(ClassPath.SOURCE);
        return ModelUnit.create(boot, compile, src);
    }

    /**
     * Returns merged {@link ClassPath} for given type.
     * @param type a classpath type such as {@link ClassPath#COMPILE}
     * @return generated read-only project's classpath of given type
     */
    public ClassPath getClassPath(String type) {
        ClassPathProvider provider = project.getLookup().lookup(
                ClassPathProvider.class);
        if (provider == null) {
            return null;
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        if (sources == null) {
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup[] webGroup = sources.getSourceGroups(TYPE_WEB_INF);
        ClassPath[] paths = new ClassPath[sourceGroups.length + webGroup.length];
        int i = 0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[i] = provider.findClassPath(rootFolder, type);
            i++;
        }
        for (SourceGroup sourceGroup : webGroup) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[i] = provider.findClassPath(rootFolder, type);
            i++;
        }
        return ClassPathSupport.createProxyClassPath(paths);
    }
}
