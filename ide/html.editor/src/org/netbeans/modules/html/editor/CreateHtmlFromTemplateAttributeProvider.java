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

import java.util.Collections;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=CreateFromTemplateAttributesProvider.class)
public class CreateHtmlFromTemplateAttributeProvider implements CreateFromTemplateAttributesProvider {

    private static final String DOCTYPE_TEMPLATE_PROPERTY_NAME = "doctype"; //NOI18N

    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        Project project = FileOwnerQuery.getOwner(target.getPrimaryFile());
        HtmlVersion version;
        if(project == null) {
            version = HtmlVersion.getDefaultVersion();
        } else {
            String mimeType = template.getPrimaryFile().getMIMEType();
            boolean xhtml = "text/xhtml".equals(mimeType); //NOI18N
            version = ProjectDefaultHtmlSourceVersionController.getDefaultHtmlVersion(project, xhtml);
            if(version == null) {
                version = xhtml ? HtmlVersion.getDefaultXhtmlVersion() : HtmlVersion.getDefaultVersion();
            }
        }

        return Collections.singletonMap(DOCTYPE_TEMPLATE_PROPERTY_NAME, version.getDoctypeDeclaration());
    }

}
