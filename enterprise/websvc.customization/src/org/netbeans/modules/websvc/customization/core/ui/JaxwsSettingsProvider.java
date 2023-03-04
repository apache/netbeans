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

package org.netbeans.modules.websvc.customization.core.ui;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author rico
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider.class)
public class JaxwsSettingsProvider implements WSEditorProvider {

    public boolean enable(Node node) {
        Client client = node.getLookup().lookup(Client.class);
        boolean doEnable = false;
        if (client != null) {
            doEnable = true;
        } else {
            Service service = node.getLookup().lookup(Service.class);
            if (service != null && service.getWsdlUrl() != null) {
                doEnable = true;
            }
        }
        return doEnable;
    }

    public WSEditor createWSEditor(Lookup nodeLookup) {
        FileObject srcRoot = nodeLookup.lookup(FileObject.class);
        if (srcRoot != null) {
            Project prj = FileOwnerQuery.getOwner(srcRoot);
            JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                return new JaxwsSettingsEditor(jaxWsModel);
            }
        }
        return null;
    }

}
