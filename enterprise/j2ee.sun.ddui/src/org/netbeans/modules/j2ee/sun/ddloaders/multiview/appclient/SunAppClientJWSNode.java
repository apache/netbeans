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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.appclient;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.other.AppClientJWSPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;

/**
 * @author Peter Williams
 */
public class SunAppClientJWSNode extends BaseSectionNode {

    SunAppClientJWSNode(SectionNodeView sectionNodeView, SunApplicationClient sunAppClient, final ASDDVersion version) {
        super(sectionNodeView, sunAppClient, version,
                NbBundle.getMessage(SunAppClientJWSNode.class, "LBL_JWSHeader"), 
                ICON_BASE_MISC_NODE);
        setExpanded(true);
        this.helpProvider = true;
    }

    @Override
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        SectionNodeView sectionNodeView = getSectionNodeView();
        return new AppClientJWSPanel(sectionNodeView, (SunApplicationClient) key, version);
    }
}
