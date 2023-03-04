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

import org.netbeans.modules.websvc.rest.support.Utils;
import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.nodes.AbstractNode;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.SubResourceLocator;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public class SubResourceLocatorNode extends AbstractNode{
    private String methodName;
    private String uriTemplate;
    private String returnType;
    
    public SubResourceLocatorNode(Project project, String className, SubResourceLocator method) {
        this(project, className, method, new InstanceContent());
    }
    
    private SubResourceLocatorNode(Project project, String className, SubResourceLocator method,
            InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        this.methodName = method.getName();
        this.uriTemplate = method.getUriTemplate();
        this.returnType = method.getReturnType();
        
        content.add(this);
        content.add(OpenCookieFactory.create(project, className, methodName));
    }
    
    public String getDisplayName() {
        return uriTemplate + " : " + Utils.stripPackageName(returnType);    //NOI18N
    }
    
    public String getShortDescription() {
        return methodName + "() : " + Utils.stripPackageName(returnType);       //NOI18N
    }
    
    public static String getKey(SubResourceLocator method) {
        return method.getUriTemplate() + " : " + method.getReturnType();    //NOI18N
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
            null,
            //SystemAction.get(DeleteAction.class),
            //null,
            SystemAction.get(PropertiesAction.class),
        };
    }
}
