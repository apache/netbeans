/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelProvider;
import org.netbeans.modules.web.jsf.api.metamodel.ModelUnit;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 * @author ads
 * @author Martin Fousek <marfous@netbeans.org>
 */
@ProjectServiceProvider(service = JsfModelProvider.class, projectType = {
    "org-netbeans-modules-maven",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"})
public final class JsfModelProviderImpl implements JsfModelProvider {

    private static final Logger LOG = Logger.getLogger(JsfModelProviderImpl.class.getName());

    //@GuardedBy(this)
    private MetadataModel<JsfModel> model = null;

    private final Project project;

    public JsfModelProviderImpl(Project project) {
        this.project = project;
    }

    @Override
    public synchronized MetadataModel<JsfModel> getModel() {
        if (model == null) {
            LOG.log(Level.FINEST, "JsfModel requested and not created yet, initializing it.");
            long startTime = System.currentTimeMillis();
            ModelUnit unit = getUnit(project);
            if (unit == null) {
                return null;
            }
            model = JsfModelProviderImpl.createMetaModel(unit);
            LOG.log(Level.FINEST, "JsfModel created in {0} ms.", (System.currentTimeMillis() - startTime));
        }
        return model;
    }

    public static MetadataModel<JsfModel> createMetaModel(ModelUnit unit) {
        return MetadataModelFactory.createMetadataModel(JsfModelImplementation.create(unit));
    }

    private static ModelUnit getUnit(Project project) {
        if (project == null) {
            return null;
        }
        ClassPath boot = getClassPath(project, ClassPath.BOOT);
        ClassPath compile = getClassPath(project, ClassPath.COMPILE);
        ClassPath src = getClassPath(project, ClassPath.SOURCE);
        return (src == null) ? null : ModelUnit.create(boot, compile, src, project);
    }

    private static ClassPath getClassPath(Project project, String type) {
        ClassPathProvider provider = project.getLookup().lookup(
                ClassPathProvider.class);
        if (provider == null) {
            return null;
        }
        Sources sources = ProjectUtils.getSources(project);
        if (sources == null) {
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            ClassPath path = provider.findClassPath(rootFolder, type);
            // return classpath of the first source group, that is ignore test source roots:
            return ClassPathSupport.createProxyClassPath(path);
        }
        return null;
    }
}
