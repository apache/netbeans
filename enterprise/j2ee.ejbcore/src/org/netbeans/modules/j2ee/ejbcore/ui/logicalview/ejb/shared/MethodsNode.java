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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddActionGroup;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.GoToSourceAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Represents Local/Remote Methods node under Session and Entity nodes 
 * in EJB logical view
 *
 * @author Martin Adamek
 */
public class MethodsNode extends AbstractNode implements OpenCookie {
    public static enum ViewType{NO_INTERFACE, LOCAL, REMOTE};
    private final String ejbClass;
    private final MetadataModel<EjbJarMetadata> model;
    private final EjbViewController controller;
    private DataObject dataObject;
    private ViewType viewType;

    public MethodsNode(String ejbClass, EjbJar ejbModule, Children children, ViewType viewType) {
        this(new InstanceContent(), ejbClass, ejbModule, children, viewType);
    }
    
    private MethodsNode(InstanceContent content, final String ejbClass, EjbJar ejbModule, Children children, final ViewType viewType) {
        super(children, new AbstractLookup(content));
        this.ejbClass = ejbClass;
        this.model = ejbModule.getMetadataModel();
        this.controller = new EjbViewController(ejbClass, ejbModule);
        this.viewType = viewType;
        String iClassName = null;
        try {
            iClassName = model.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws Exception {
                    EntityAndSession entityAndSession = (EntityAndSession) metadata.findByEjbClass(ejbClass);
                    if (entityAndSession != null){
                        switch (viewType){
                            case NO_INTERFACE: return entityAndSession.getEjbClass();
                            case LOCAL: return entityAndSession.getLocal();
                            case REMOTE: return entityAndSession.getRemote();
                        }
                    }
                    return null;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        this.dataObject = controller.getDataObject(iClassName);
        content.add(this);
        if (dataObject != null) {
             content.add(dataObject);
        }
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            new GoToSourceAction(dataObject, NbBundle.getMessage(MethodsNode.class, "LBL_GoToSourceGroup")),
            SystemAction.get(AddActionGroup.class),
        };
    }

    public Action getPreferredAction() {
        return new GoToSourceAction(dataObject, NbBundle.getMessage(MethodsNode.class, "LBL_GoToSourceGroup"));
    }

    public void open() {
        DataObject dataObject = controller.getBeanDo();
        if (dataObject != null) {
            OpenCookie cookie = dataObject.getCookie(OpenCookie.class);
            if(cookie != null){
                cookie.open();
            }
        }
    }
    
    public boolean isLocal() {
	return viewType == ViewType.LOCAL;
    }

    public boolean isRemote() {
	return viewType == ViewType.REMOTE;
    }
}
