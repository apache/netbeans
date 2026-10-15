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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import java.util.ArrayList;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.AttributeEntry;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.InnerTablePanel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.ParentManagedDDBeanTableModel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.TableEntry;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.ValueEntry;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;

/**
 * @author pfiala
 * @author Peter Williams
 */
public class SunWebJspConfigPropertyNode extends BaseSectionNode {

    public SunWebJspConfigPropertyNode(SectionNodeView sectionNodeView, SunWebApp sunWebApp, final ASDDVersion version) {
        super(sectionNodeView, sunWebApp, version, NbBundle.getMessage(SunWebJspConfigPropertyNode.class, "HEADING_JspConfigProperties"),
                ICON_BASE_MISC_NODE);
    }

    @Override
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        ArrayList<TableEntry> tableColumns = 
                new ArrayList<TableEntry>(3);
        tableColumns.add(new AttributeEntry(
                WebProperty.NAME, NbBundle.getMessage(SunWebJspConfigPropertyNode.class, 
                "LBL_Name"), 150, true)); // NOI18N
        tableColumns.add(new AttributeEntry(
                WebProperty.VALUE, NbBundle.getMessage(SunWebJspConfigPropertyNode.class, 
                "LBL_Value"), 150, true)); // NOI18N
        tableColumns.add(new ValueEntry(
                WebProperty.DESCRIPTION, NbBundle.getMessage(SunWebJspConfigPropertyNode.class, 
                "LBL_Description"), 300)); // NOI18N		
        
        SunWebApp swa = (SunWebApp) key;
        SectionNodeView sectionNodeView = getSectionNodeView();
        return new InnerTablePanel(sectionNodeView, new ParentManagedDDBeanTableModel(
                sectionNodeView.getModelSynchronizer(), 
                swa.getJspConfig(), JspConfig.PROPERTY, tableColumns,
                null, new JspConfigPropertyFactory()), version);
    }
    

    private static class JspConfigPropertyFactory implements ParentManagedDDBeanTableModel.ParentPropertyFactory {
        public CommonDDBean newInstance(CommonDDBean parent) {
            return ((JspConfig) parent).newWebProperty();
        }
    } 
}
