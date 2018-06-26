/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        return nodes.toArray(new Node[nodes.size()]);
    }

    private Node[] createClientNodes() {
        ArrayList<Node> nodes = new ArrayList<Node>();
        Client[] clients = jaxWsModel.getClients();
        if (clients != null && clients.length > 0) {
            for (Client client : clients) {
                nodes.add(new JaxWsClientNode(jaxWsModel, client, getProject().getProjectDirectory()));
            }
        }
        return nodes.toArray(new Node[nodes.size()]);
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
