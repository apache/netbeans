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

package org.netbeans.modules.web.jsf.palette.items;

import org.netbeans.modules.web.jsf.JsfTemplateUtils;
import org.netbeans.modules.web.jsf.api.palette.PaletteItem;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.NbBundle;

public final class JsfTableFromEntity extends FromEntityBase implements ActiveEditorDrop, PaletteItem {

    public JsfTableFromEntity() {
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(JsfForm.class, "NAME_jsp-JsfTableFromEntity"); // NOI18N
    }
    
    @Override
    protected boolean isCollectionComponent() {
        return true;
    }

    @Override
    protected boolean showReadOnlyFormFlag() {
        return false;
    }

    @Override
    protected String getDialogTitle() {
        return NbBundle.getMessage(JsfFormFromEntity.class, "JTFE_DialogTitle"); // NOI18N
    }

    @Override
    protected String getTemplate(String templatesStyle) {
        return JsfTemplateUtils.getTemplatePath(JsfTemplateUtils.TemplateType.SNIPPETS, templatesStyle, ManagedBeanCustomizer.TABLE_TEMPLATE);
    }

}
