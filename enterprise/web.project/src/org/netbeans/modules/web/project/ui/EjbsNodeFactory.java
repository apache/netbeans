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

package org.netbeans.modules.web.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * NodeFactory to create EJB nodes.
 *
 * @author kaktus
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-web-project", position=350)
public class EjbsNodeFactory implements NodeFactory {

    public EjbsNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        WebProject project = p.getLookup().lookup(WebProject.class);
        assert project != null;
        return new EjbNodeList(project);
    }

    private static class EjbNodeList implements NodeList<String>, PropertyChangeListener{
        private static final String KEY_EJBS = "ejbKey"; //NOI18N
        private final WebProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private Node view = null;
        private boolean isViewEmpty = true;
        private final J2eeProjectCapabilities projectCap;
        private static final RequestProcessor rp = new RequestProcessor();
        private Task checkTask = null;

        EjbNodeList(WebProject proj) {
            this.project = proj;
            this.projectCap = J2eeProjectCapabilities.forProject(project);
        }

        public List<String> keys() {
            if (projectCap.isEjb31LiteSupported()){
                checkView();
            }
            if (!isViewEmpty){
                return Collections.singletonList(KEY_EJBS);
            }else{
                return Collections.emptyList();
            }
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(String key) {
            if (KEY_EJBS.equals(key)) {
                return view;
            }
            return null;
        }

        public void addNotify() {
            if (projectCap.isEjb31LiteSupported()){
                EjbJar ejbModule = project.getAPIEjbJar();
                view = J2eeProjectView.createEjbsView(ejbModule, project);
            }
            project.evaluator().addPropertyChangeListener(this);
        }

        public void removeNotify() {
            project.evaluator().removePropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p != null && (p.equals(WebProjectProperties.J2EE_PLATFORM) || p.startsWith("/EnterpriseBeans/"))){ //NOI18N
                fireChange();
            }
        }

        private void fireChange(){
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }

        private synchronized void checkView(){
            if(checkTask != null){
                checkTask.schedule(100);
                return;
            }
            checkTask = rp.post(new Runnable(){
                public void run() {
                    Boolean isEmpty = Boolean.TRUE;
                    try {
                        final Future<Boolean> becomesIsEmpty = project.getAPIEjbJar().getMetadataModel().runReadActionWhenReady(new MetadataModelAction<EjbJarMetadata, Boolean>() {
                            public Boolean run(EjbJarMetadata metadata) {
                                org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = metadata.getRoot();
                                if (ejbJar != null) {
                                    EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
                                    if (enterpriseBeans != null) {
                                        enterpriseBeans.removePropertyChangeListener(EjbNodeList.this);
                                        enterpriseBeans.addPropertyChangeListener(EjbNodeList.this);
                                        if (enterpriseBeans.getEjbs().length > 0){
                                            return Boolean.FALSE;
                                        }
                                    }
                                }
                                return Boolean.TRUE;
                            }
                        });
                        isEmpty = becomesIsEmpty.get();
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (InterruptedException ie) {
                        Exceptions.printStackTrace(ie);
                    } catch (ExecutionException ee) {
                        Exceptions.printStackTrace(ee);
                    }

                    // #247325
                    if (isEmpty != null && isEmpty ^ isViewEmpty){
                        isViewEmpty = isEmpty;
                        fireChange();
                    }
                }
            }, 100);
        }
    }
}
