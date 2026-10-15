/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.ProjectWebServiceView;
import org.netbeans.modules.websvc.core.AbstractProjectWebServiceViewImpl;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 *
 * @author Ajit
 */
final class ProjectJaxWsWebServiceView extends AbstractProjectWebServiceViewImpl implements PropertyChangeListener {

    private JaxWsModel jaxWsModel;
    private JAXWSSupport jaxwsSupport;
    private JAXWSClientSupport jaxwsClientSupport;
    private PropertyChangeListener weakModelListener;

    ProjectJaxWsWebServiceView(Project p) {
        super(p);
        jaxWsModel = (JaxWsModel) p.getLookup().lookup(JaxWsModel.class);
        weakModelListener = WeakListeners.propertyChange(this, jaxWsModel);
        FileObject projectDir = p.getProjectDirectory();
        jaxwsSupport = JAXWSSupport.getJAXWSSupport(projectDir);
        jaxwsClientSupport = JAXWSClientSupport.getJaxWsClientSupport(projectDir);
    }

    public Node[] createView(ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                return createServiceNodes();
            case CLIENT:
                return createClientNodes();
        }
        return new Node[0];
    }

    private Node[] createServiceNodes() {
        Service[] services = jaxWsModel.getServices();
        if (services == null || services.length <= 0) {
            return new Node[0];
        }
        Sources sources = (Sources) getProject().getLookup().lookup(Sources.class);
        ArrayList<FileObject> roots = new ArrayList<FileObject>();
        if (sources != null) {
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (groups != null) {
                for (SourceGroup group : groups) {
                    roots.add(group.getRootFolder());
                }
            }
        }
        if (roots.isEmpty()) {
            return new Node[0];
        }
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (Service service : services) {
            String implClass = service.getImplementationClass();
            if (implClass == null) {
                continue;
            }
            for (FileObject srcRoot : roots) {
                FileObject implClassFo = srcRoot.getFileObject(implClass.replace('.', '/') + ".java");
                if (implClassFo != null) {
                    nodes.add(new JaxWsNode(jaxWsModel, service, srcRoot, implClassFo));
                    break;
                }
            }
        }
        return nodes.toArray(new Node[0]);
    }

    private Node[] createClientNodes() {
        ArrayList<Node> nodes = new ArrayList<Node>();
        Client[] clients = jaxWsModel.getClients();
        if (clients != null && clients.length > 0) {
            for (Client client : clients) {
                nodes.add(new JaxWsClientNode(jaxWsModel, client, getProject().getProjectDirectory()));
            }
        }
        return nodes.toArray(new Node[0]);
    }

    public boolean isViewEmpty(ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                return jaxWsModel == null || jaxwsSupport == null || jaxWsModel.getServices().length == 0;
            case CLIENT:
                return jaxWsModel == null || jaxwsClientSupport == null || jaxWsModel.getClients().length == 0;
        }
        return true;
    }

    public void addNotify() {
        if (jaxWsModel != null) {
            jaxWsModel.addPropertyChangeListener(weakModelListener);
        }
    }

    public void removeNotify() {
        if (jaxWsModel != null) {
            jaxWsModel.removePropertyChangeListener(weakModelListener);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Object oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();
        if (oldValue instanceof Service || newValue instanceof Service) {
            fireChange(ProjectWebServiceView.ViewType.SERVICE);
        }
        if (oldValue instanceof Client || newValue instanceof Client) {
            fireChange(ProjectWebServiceView.ViewType.CLIENT);
        }
    }
}
