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
 * CustomizationWSEditorProvider.java
 *
 * Created on February 17, 2006, 11:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.light;

import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Roderico Cruz
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider.class)
public class CustomizationWSEditorProvider
        implements WSEditorProvider{

    public WSEditor createWSEditor(Lookup nodeLookup) {
        JaxWsService service = nodeLookup.lookup(JaxWsService.class);
        if (service != null) {
            JAXWSLightSupport jaxWsSupport = nodeLookup.lookup(JAXWSLightSupport.class);
            if (jaxWsSupport != null) {
                return new CustomizationWSEditor(jaxWsSupport, service);
            } else {
                FileObject srcRoot = nodeLookup.lookup(FileObject.class);
                if (srcRoot != null) {
                    jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(srcRoot);
                    if (jaxWsSupport != null) {
                        return new CustomizationWSEditor(jaxWsSupport, service);
                    }
                }
            }
        }
        return null;
    }
    
    public boolean enable(Node node) {
        JaxWsService service = node.getLookup().lookup(JaxWsService.class);
        return service != null && service.getLocalWsdl() != null;
    }
    
}
