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
package org.netbeans.modules.web.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public final class CreateFromTemplateAttributesImpl implements CreateFromTemplateAttributes {

    private static final Logger LOGGER = Logger.getLogger(CreateFromTemplateAttributesImpl.class.getName());

    private final AntProjectHelper helper;
    private final CreateFromTemplateAttributesProvider delegate;


    CreateFromTemplateAttributesImpl(AntProjectHelper helper, FileEncodingQueryImplementation encodingQuery) {
        assert helper != null;
        assert encodingQuery != null;
        this.helper = helper;
        delegate = QuerySupport.createTemplateAttributesProvider(helper, encodingQuery);
    }

    @Override
    public Map<String, ?> attributesFor(CreateDescriptor desc) {
        try {
            Map<String, Object> values = (Map<String, Object>) delegate.attributesFor(
                    DataObject.find(desc.getTemplate()), DataFolder.findFolder(desc.getTarget()), desc.getName());
            if (values == null) {
                values = new HashMap<>();
            }
            Map<String, Object> projectValues = (Map<String, Object>) values.get("project"); // NOI18N
            if (projectValues == null) {
                projectValues = new HashMap<>();
                values.put("project", projectValues); // NOI18N
            }
            Project prj = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            assert prj != null;
            projectValues.put("webRootPath", getWebRootPath(prj)); // NOI18N
            return values;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    @CheckForNull
    private static String getWebRootPath(Project project) {
        for (FileObject webRoot : ProjectWebRootQuery.getWebRoots(project)) {
            return FileUtil.getRelativePath(project.getProjectDirectory(), webRoot);
        }
        return null;
    }

}
