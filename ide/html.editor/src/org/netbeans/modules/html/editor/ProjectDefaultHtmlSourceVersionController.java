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
package org.netbeans.modules.html.editor;

import java.util.prefs.Preferences;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.html.editor.lib.api.HtmlSourceVersionController;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * The fallback version resolver. It reads project's property default-public-id and if
 * set returns the appropriate HtmlVersion instance.
 *
 * @author marekfukala
 */
@ServiceProvider(service = HtmlSourceVersionController.class)
public class ProjectDefaultHtmlSourceVersionController implements HtmlSourceVersionController {

    public static final String HTML_VERSION_PUBLIC_ID_AUX_PROPERTY_NAME = "default-html-public-id"; //NOI18N
    public static final String XHTML_VERSION_PUBLIC_ID_AUX_PROPERTY_NAME = "default-xhtml-public-id"; //NOI18N

    @Override
    public HtmlVersion getSourceCodeVersion(SyntaxAnalyzerResult analyzerResult, HtmlVersion detectedVersion) {
        if (detectedVersion != null) {
            return null;
        }
        FileObject file = analyzerResult.getSource().getSourceFileObject();
        if (file == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }
        
        return findHtmlVersion(project, analyzerResult);
    }

    public static HtmlVersion getDefaultHtmlVersion(Project project, boolean xhtml) {
        return findHtmlVersion(project, null, xhtml);
    }

    private static HtmlVersion findHtmlVersion(Project project, SyntaxAnalyzerResult analyzerResult) {
        return findHtmlVersion(project, analyzerResult.getHtmlTagDefaultNamespace(), analyzerResult.mayBeXhtml());
    }

    private static HtmlVersion findHtmlVersion(Project project, String namespace, boolean xhtml) {
        Preferences prefs = ProjectUtils.getPreferences(project, HtmlSourceVersionController.class, true);
        String publicId = prefs.get(getPropertyKey(xhtml), null);
        if (publicId == null) {
            return null;
        }
        
        //no-public id versions
        if(xhtml && publicId.equals(HtmlVersion.XHTML5.name())) {
            return HtmlVersion.XHTML5;
        } else if(!xhtml && publicId.equals(HtmlVersion.HTML5.name())) {
            return HtmlVersion.HTML5;
        }
         
        try {
            return HtmlVersion.find(publicId, namespace);
        } catch (IllegalArgumentException e) {
            //no-op
        }
        return null;
    }

    public static void setDefaultHtmlVersion(Project project, HtmlVersion version, boolean xhtml) {
        Preferences prefs = ProjectUtils.getPreferences(project, HtmlSourceVersionController.class, true);
        String publicId = version.getPublicID();
        if(publicId == null) {
            publicId = version.name(); //x/html5 has no public id
        }
        prefs.put(getPropertyKey(xhtml), publicId);
    }

    private static String getPropertyKey(boolean xhtml) {
        return xhtml ? XHTML_VERSION_PUBLIC_ID_AUX_PROPERTY_NAME : HTML_VERSION_PUBLIC_ID_AUX_PROPERTY_NAME;
    }
}
