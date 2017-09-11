/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.java.api.common.queries;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 * Default implementation of {@link CreateFromTemplateAttributesProvider}.
 *
 * @author Andrei Badea
 */
class TemplateAttributesProviderImpl implements CreateFromTemplateAttributesProvider {

    private final AntProjectHelper helper;
    private final FileEncodingQueryImplementation encodingQuery;
    private static final Logger LOG = Logger.getLogger(TemplateAttributesProviderImpl.class.getName());
    

    public TemplateAttributesProviderImpl(AntProjectHelper helper, FileEncodingQueryImplementation encodingQuery) {
        super();
        this.helper = helper;
        this.encodingQuery = encodingQuery;
    }

    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        Map<String, String> values = new HashMap<String, String>();
        EditableProperties priv  = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String licensePath = priv.getProperty("project.licensePath");
        if (licensePath == null) {
            licensePath = props.getProperty("project.licensePath");
        }
        if (licensePath != null) {
            licensePath = helper.getStandardPropertyEvaluator().evaluate(licensePath);
            if (licensePath != null) {
                File path = FileUtil.normalizeFile(helper.resolveFile(licensePath));
                if (path.exists() && path.isAbsolute()) { //is this necessary? should prevent failed license header inclusion
                    URI uri = Utilities.toURI(path);
                    licensePath = uri.toString();
                    values.put("licensePath", licensePath);
                } else {
                    LOG.log(Level.INFO, "project.licensePath value not accepted - " + licensePath);
                }
            }
        }
        String license = priv.getProperty("project.license"); // NOI18N
        if (license == null) {
            license = props.getProperty("project.license"); // NOI18N
        }
        if (license != null) {
            values.put("license", license); // NOI18N
        }
        Charset charset = encodingQuery.getEncoding(target.getPrimaryFile());
        String encoding = (charset != null) ? charset.name() : null;
        if (encoding != null) {
            values.put("encoding", encoding); // NOI18N
        }
        try {
            Project prj = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            ProjectInformation info = ProjectUtils.getInformation(prj);
            if (info != null) {
                String pname = info.getName();
                if (pname != null) {
                    values.put("name", pname);// NOI18N
                }
                String pdname = info.getDisplayName();
                if (pdname != null) {
                    values.put("displayName", pdname);// NOI18N
                }
            }
        } catch (Exception ex) {
            //not really important, just log.
            Logger.getLogger(TemplateAttributesProviderImpl.class.getName()).log(Level.FINE, "", ex);
        }

        if (values.isEmpty()) {
            return null;
        } else {
            return Collections.singletonMap("project", values); // NOI18N
        }
    }
}
