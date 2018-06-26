/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
