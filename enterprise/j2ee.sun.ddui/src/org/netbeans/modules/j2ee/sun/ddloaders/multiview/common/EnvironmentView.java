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

import java.util.LinkedList;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationRefGroupNode;
import org.netbeans.modules.glassfish.eecommon.api.config.J2EEBaseVersion;
import org.netbeans.modules.glassfish.eecommon.api.config.J2EEVersion;
import org.netbeans.modules.xml.multiview.SectionNode;


/**
 * @author Peter Williams
 */
public class EnvironmentView extends DDSectionNodeView {

    public EnvironmentView(SunDescriptorDataObject dataObject) {
        super(dataObject);
        
        if(!(rootDD instanceof SunWebApp || rootDD instanceof SunApplicationClient)) {
            throw new IllegalArgumentException("Data object is not a root that contains top level reference elements (" + rootDD + ")");
        }

        LinkedList<SectionNode> children = new LinkedList<SectionNode>();
        children.add(new EjbRefGroupNode(this, rootDD, version));
        children.add(new ResourceRefGroupNode(this, rootDD, version));
        children.add(new ResourceEnvRefGroupNode(this, rootDD, version));
        if(ASDDVersion.SUN_APPSERVER_9_0.compareTo(version) <= 0) {
            J2EEBaseVersion j2eeVersion = dataObject.getJ2eeModuleVersion();
            if(j2eeVersion == null || j2eeVersion.compareSpecification(J2EEVersion.JAVAEE_5_0) >= 0) {
                children.add(new MessageDestinationRefGroupNode(this, rootDD, version));
            }
        }
        setChildren(children);
    }

}
