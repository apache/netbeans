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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.EndpointGroupNode;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.EjbPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author Peter Williams
 */
public class EjbNode extends NamedBeanNode {

    // TODO session/entity/mdb specific icons
    public EjbNode(SectionNodeView sectionNodeView, DDBinding binding, final ASDDVersion version) {
        super(sectionNodeView, binding, Ejb.EJB_NAME, ICON_EJB_SESSION_NODE, version);
        
        enableRemoveAction();
        
        Ejb ejb = (Ejb) binding.getSunBean();
        addChild(new ReferencesNode(sectionNodeView, ejb, version));
        if(ASDDVersion.SUN_APPSERVER_8_0.compareTo(version) <= 0) {
            // Webservice endpoints.
            addChild(new EndpointGroupNode(sectionNodeView, binding.getSunBean(), version));
        }
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return new EjbPanel(getSectionNodeView(), this, version);
    }
}
