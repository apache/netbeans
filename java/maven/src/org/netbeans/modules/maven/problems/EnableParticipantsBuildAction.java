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
package org.netbeans.modules.maven.problems;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * enable project loading with custom build participation
 * 
 * @author mkleint
 */

//NOT used, executing build participants is fairly dangerous
class EnableParticipantsBuildAction extends AbstractAction {
    
    public static final String NAMESPACE = "http://www.netbeans.org/ns/maven-build-participants/1"; 
    public static final String ROOT = "participants";
    public static final String ENABLED = "enabled";
    
    private final NbMavenProjectImpl project;

    public EnableParticipantsBuildAction(NbMavenProjectImpl nbproject) {
        super("Enable Build Participants");
        this.project = nbproject;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        AuxiliaryConfiguration aux = project.getLookup().lookup(AuxiliaryConfiguration.class);
        Element el = XMLUtil.createDocument(ROOT, NAMESPACE, null, null).getDocumentElement();
        el.appendChild(el.getOwnerDocument().createElementNS(NAMESPACE, ENABLED));
        aux.putConfigurationFragment(el, true);
    }
    
    public static boolean isEnabled(AuxiliaryConfiguration aux) {
        Element el = aux.getConfigurationFragment(ROOT, NAMESPACE, true);
        if (el != null) {
            NodeList nl = el.getElementsByTagNameNS(NAMESPACE, ENABLED);
            if (nl != null) {
                return nl.getLength() == 1;
            }
        }
        return false;

    }
    
}
