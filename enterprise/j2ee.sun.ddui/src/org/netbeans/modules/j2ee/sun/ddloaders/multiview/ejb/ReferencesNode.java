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
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EjbRefGroupNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceEnvRefGroupNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceRefGroupNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ServiceRefGroupNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationRefGroupNode;
import org.netbeans.modules.glassfish.eecommon.api.config.J2EEBaseVersion;
import org.netbeans.modules.glassfish.eecommon.api.config.J2EEVersion;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ReferencesNode extends BaseSectionNode {

    ReferencesNode(SectionNodeView sectionNodeView, Ejb ejb, final ASDDVersion version) {
        super(sectionNodeView, new Children.Array(), ejb, version, 
                NbBundle.getMessage(ReferencesNode.class, "LBL_ReferencesHeader"), 
                ICON_BASE_REFERENCES_NODE);
        this.helpProvider = true;

        // References
        addChild(new EjbRefGroupNode(sectionNodeView, ejb, version));
        addChild(new ResourceRefGroupNode(sectionNodeView, ejb, version));
        addChild(new ResourceEnvRefGroupNode(sectionNodeView, ejb, version));
        if(ASDDVersion.SUN_APPSERVER_8_0.compareTo(version) <= 0) {
            SunDescriptorDataObject dataObject = (SunDescriptorDataObject) sectionNodeView.getDataObject();
            J2EEBaseVersion j2eeVersion = dataObject.getJ2eeModuleVersion();
            if(j2eeVersion == null || j2eeVersion.compareSpecification(J2EEVersion.J2EE_1_4) >= 0) {
                addChild(new ServiceRefGroupNode(sectionNodeView, ejb, version));
                if(ASDDVersion.SUN_APPSERVER_9_0.compareTo(version) <= 0) {
                    if(j2eeVersion == null || j2eeVersion.compareSpecification(J2EEVersion.JAVAEE_5_0) >= 0) {
                        addChild(new MessageDestinationRefGroupNode(sectionNodeView, ejb, version));
                    }
                }
            }
        }
    }
    
}
