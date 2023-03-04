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

/*
 * DefinitionsNode.java
 *
 * Created on February 26, 2006, 6:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.multiview;

import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;

/**
 *
 * @author Roderico Cruz
 */
public class ServiceNode extends SectionContainerNode{
    private Service service;
    /** Creates a new instance of DefinitionsNode */
    public ServiceNode(SectionView view, Service service) {
        super(Children.LEAF);
        this.service = service;
        setDisplayName(this.service.getName());
    }
    
    public String getPanelId() {
        return "service"; //NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(WSCustomizationView.ID_SERVICE);
    }
}
