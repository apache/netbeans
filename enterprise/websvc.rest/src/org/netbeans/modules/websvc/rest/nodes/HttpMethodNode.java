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
package org.netbeans.modules.websvc.rest.nodes;

import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.support.Utils;
import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.openide.nodes.AbstractNode;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public class HttpMethodNode extends AbstractNode{
    private String methodName;
    private String produceMime;
    private String consumeMime;
    private String returnType;
    
    
    public HttpMethodNode(Project project, RestServiceDescription desc, HttpMethod method) {
        this(project, desc, method, new InstanceContent());
    }
    
    private HttpMethodNode(Project project, RestServiceDescription desc, HttpMethod method,
            InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        this.methodName = method.getName();
        this.produceMime = method.getProduceMime();
        this.consumeMime = method.getConsumeMime();
        this.returnType = method.getReturnType();
        content.add(this);
        // enable Test method Uri action only for GET methods
        if ("GET".equals(method.getType())) { //NOI18N
            content.add(new MethodUriProvider(desc.getUriTemplate(), method.getPath()));
        }
        content.add(project);
        content.add(OpenCookieFactory.create(project, desc.getClassName(), methodName));
    }
    
    public String getDisplayName() {
        return methodName + "() : " + Utils.stripPackageName(returnType);
    }
    
    public String getShortDescription() {       
        String desc = "";       //NOI18N
        
        if (consumeMime.length() > 0) {
            desc += "@ConsumeMime(\"" + consumeMime + "\") ";        //NOI18N
        }
        
        if (produceMime.length() > 0) {
            desc += "@ProduceMime(\"" + produceMime + "\") ";        //NOI18N
        }
        
        return desc;
    }
    
    public String getKey() {
        return methodName + ":" + returnType + ":" +consumeMime +  ":" + produceMime;  //NOI18N
    }
    
    private static final java.awt.Image METHOD_BADGE =
            ImageUtilities.loadImage( "org/netbeans/modules/websvc/rest/nodes/resources/method.png" ); //NOI18N
    
    public java.awt.Image getIcon(int type) {
        return METHOD_BADGE;
    }
    
    void changeIcon() {
        fireIconChange();
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(TestResourceUriAction.class),
            null,
            //SystemAction.get(DeleteAction.class),
            //null,
            SystemAction.get(PropertiesAction.class),
        };
    }

    private static class MethodUriProvider implements ResourceUriProvider {

        private String resourcePath, methodPath;

        private MethodUriProvider(String resourcePath, String methodPath) {
            this.resourcePath = resourcePath;
            this.methodPath = methodPath;
        }

        public String getResourceUri() {
            if (methodPath == null || methodPath.length() == 0) {
                return resourcePath;
            } else if (methodPath.startsWith("/")) { //NOI18N
                return resourcePath+methodPath;
            } else {
                return resourcePath+"/"+methodPath; //NOI18N
            }
        }

    }
}
