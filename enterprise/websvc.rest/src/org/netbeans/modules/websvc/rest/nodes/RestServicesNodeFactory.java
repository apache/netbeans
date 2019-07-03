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
package org.netbeans.modules.websvc.rest.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milan Kuchtiak
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-web-project",position=450)
public class RestServicesNodeFactory implements NodeFactory {


    @NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=90)
    public static RestServicesNodeFactory mavenproject() {
        return new RestServicesNodeFactory();
    }

    @NodeFactory.Registration(projectType="org-netbeans-modules-gradle",position=90)
    public static RestServicesNodeFactory gradleproject() {
        return new RestServicesNodeFactory();
    }

    /** Creates a new instance of WebServicesNodeFactory */
    public RestServicesNodeFactory() {
    }

    @Override
    public NodeList createNodes(Project p) {
        assert p != null;
        return new RestNodeList(p);
    }
    
    private static class RestNodeList implements NodeList<String>, PropertyChangeListener {

        private static final String KEY_SERVICES = "rest_services"; // NOI18N
        private static final String NO_SERVICES = "no_rest_services";   //NOI18N
        private Project project;
        private AtomicReference<String> result = new AtomicReference<String>();
        private RequestProcessor.Task restModelTask =
                new RequestProcessor("RestServicesModel-request-processor").create(new Runnable() { //NOI18N
            @Override
            public void run() {
                try {
                    RestServicesModel model = getModel();
                    if (model != null) {
                        model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {

                            @Override
                            public Void run(RestServicesMetadata metadata) throws IOException {
                                RestServices root = metadata.getRoot();
                                String oldValue;
                                String newValue = root.sizeRestServiceDescription() > 0 ? KEY_SERVICES : NO_SERVICES;
                                oldValue = result.getAndSet(newValue);
                                if (!newValue.equals(oldValue)) {
                                    fireChange();
                                }
                                return null;
                            }
                        });
                    } else {
                        result.set(NO_SERVICES);
                    }
                } catch (IOException ex) {
                    Logger.getLogger( RestServiceChildFactory.class.getName()).
                        log(Level.INFO, null , ex);
                }
            }
        });
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

        public RestNodeList(Project proj) {
            this.project = proj;
        }

        @Override
        public List<String> keys() {
            final String keys = result.get();
            if (keys != null) {
                List<String> tmpResult = new ArrayList<String>();
                if (KEY_SERVICES.equals(keys)) {
                    tmpResult.add(KEY_SERVICES);
                }
                return tmpResult;
            } else {
                restModelTask.schedule(100);
            }
            return Collections.emptyList();
        }

        private RestSupport getRestSupport() {
            return project.getLookup().lookup(RestSupport.class);
        }

        private RestServicesModel getModel() {
            RestSupport support = getRestSupport();
            if (support != null) {
                return support.getRestServicesModel();
            }
            return null;
        }

        @Override
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        @Override
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }

        private void fireChange() {
            ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
            synchronized (this) {
                list.addAll(listeners);
            }
            Iterator<ChangeListener> it = list.iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                elem.stateChanged(new ChangeEvent(this));
            }
        }

        @Override
        public Node node(String key) {
            if (KEY_SERVICES.equals(key)) {
                return new RestServicesNode(project, getRestSupport());
            }
            return null;
        }

        @Override
        public void addNotify() {
            RestServicesModel m = getModel();
            if (m != null) {
                m.addPropertyChangeListener(this);
            }
        }

        @Override
        public void removeNotify() {
            RestServicesModel m = getModel();
            if (m != null) {
                m.removePropertyChangeListener(this);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (RestServices.PROP_SERVICES.equals(evt.getPropertyName())) {
                restModelTask.schedule(100);
            }
        }
    }
}
