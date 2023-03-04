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

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.nodes.AbstractNode;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public class RestServiceNode extends AbstractNode{
    private String serviceName;
    private String uriTemplate;
    private String className;
    private ActiveEditorDrop editorDrop;
    
    public  RestServiceNode(Project project, RestServicesModel model,
            RestServiceDescription desc) {
        this(project, model, desc, new InstanceContent());
    }
    
    private RestServiceNode(Project project, RestServicesModel model,
            final RestServiceDescription desc, InstanceContent content) {
        super(new RestServiceChildren(project, model, desc.getName()), new AbstractLookup(content));
        this.serviceName = desc.getName();
        this.uriTemplate = desc.getUriTemplate();
        this.className = desc.getClassName();
        
        content.add(this);
        content.add(desc);
        content.add(new ResourceUriProvider() {
            public String getResourceUri() {
                return desc.getUriTemplate();
            }           
        });
        content.add(project);
        content.add(OpenCookieFactory.create(project, className));
        editorDrop = new ResourceToEditorDrop(this);
    }

    @Override
    public String getDisplayName() {
        if (uriTemplate.length() > 0) {
            return serviceName + " [" + uriTemplate + "]";      //NOI18N
        } else {
            return serviceName;
        }
    }

    @Override
    public String getShortDescription() {
        return "";
    }
    
    public static String getKey(RestServiceDescription desc) {
        return desc.getName() + ":" + desc.getUriTemplate();        //NOI18N
    }
    
    private static final java.awt.Image SERVICE_BADGE =
            ImageUtilities.loadImage( "org/netbeans/modules/websvc/rest/nodes/resources/restservice.png" ); //NOI18N

    @Override
    public java.awt.Image getIcon(int type) {
        return SERVICE_BADGE;
    }
    
    void changeIcon() {
        fireIconChange();
    }

    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    // Create the popup menu:
    @Override
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
    
    @Override
    public Transferable clipboardCopy() throws IOException {

        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(editorDrop);
        t.put(s);

        return t;
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {

        private ActiveEditorDrop drop;

        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);

            this.drop = drop;
        }

        public Object getData () {
            return drop;
        }

    }
}
