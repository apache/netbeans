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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.PortInfoPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;


/**
 * @author Peter Williams
 */
public class PortInfoNode extends NamedBeanNode {

    public PortInfoNode(SectionNodeView sectionNodeView, final DDBinding binding, final ASDDVersion version) {
        super(sectionNodeView, binding, null, generateTitle((PortInfo) binding.getSunBean()), ICON_BASE_PORT_INFO_NODE, version);
        enableRemoveAction();
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return new PortInfoPanel(getSectionNodeView(), this, version);
    }
    
    @Override
    protected String generateTitle() {
        return generateTitle((PortInfo) key);
    }
    
    static String generateTitle(PortInfo portInfo) {
        StringBuilder builder = new StringBuilder(128);
        
        String sei = portInfo.getServiceEndpointInterface();
        if(Utils.notEmpty(sei)) {
            builder.append(sei);
        } else {
            WsdlPort wsdlPort = portInfo.getWsdlPort();
            if(wsdlPort != null) {
                String nsuri = wsdlPort.getNamespaceURI();
                String localPart = wsdlPort.getLocalpart();

                if(Utils.notEmpty(nsuri)) {
//                    if(builder.length() > 0) {
//                        builder.append(", ");
//                    }
                    builder.append(nsuri);
                }

                if(Utils.notEmpty(localPart)) {
                    if(builder.length() > 0) {
                        builder.append(", ");
                    }
                    builder.append(localPart);
                }
            }
        }
        
        if(builder.length() == 0) {
            builder.append("Unbound port-info");
        }
        
        return builder.toString();
    }
    
}
