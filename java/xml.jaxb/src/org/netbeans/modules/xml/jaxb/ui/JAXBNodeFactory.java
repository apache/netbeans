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
package org.netbeans.modules.xml.jaxb.ui;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListener;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListenerAdapter;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.nodes.Node;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;

/**
 *
 * @author gpatil
 */
@NodeFactory.Registration(projectType={"org-netbeans-modules-java-j2seproject"} ,position=275)
public class JAXBNodeFactory implements NodeFactory {
    public static final String JAXB_NODE_NAME = "JAXB Bindings" ; // NOI18N

    @NodeFactory.Registration(projectType="org-netbeans-modules-web-project",position=425)
    public static JAXBNodeFactory webproject() {
        return new JAXBNodeFactory();
    }

    @NodeFactory.Registration(projectType="org-netbeans-modules-j2ee-ejbjarproject",position=175)
    public static JAXBNodeFactory ejbproject() {
        return new JAXBNodeFactory();
    }

    public JAXBNodeFactory() {
    }
    
    public synchronized NodeList<String> createNodes(Project project) {
        return new JAXBRootNodeList(project); 
    }
    
    private class JAXBRootNodeList  implements NodeList<String> {
        private Project project;
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private JAXBWizEventListener modelListener = new ModelListener();
        
        private List<String> rootKeys = null;
        
        public JAXBRootNodeList(Project prj){
            this.project = prj;
            rootKeys = new ArrayList<String>();
            ProjectHelper.addModelListener(prj, modelListener);
            updateKeys();
        }
        
        private synchronized void updateKeys(){
            Schemas scs = ProjectHelper.getXMLBindingSchemas(project);        
            updateKeys(scs);
        }

        private synchronized void updateKeys(Schemas scs){
            rootKeys.clear();
            if (scs != null && scs.sizeSchema() > 0){
                rootKeys.add(JAXB_NODE_NAME);
            }            
        }

        public List<String> keys() {
            List<String> immutable = Collections.unmodifiableList(this.rootKeys);
            return immutable;
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        public void addNotify() {
        }
        
        public void removeNotify() {
        }
        
        public synchronized Node node(String key) {
            Node ret = null;
            if (JAXB_NODE_NAME.equals(key)){
                ret = new JAXBWizardRootNode(this.project);
            }
            return ret;
        }
        
        private void fireChange() {
            ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
            synchronized (this) {
                list.addAll(listeners);
            }
            Iterator<ChangeListener> it = list.iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                elem.stateChanged(new ChangeEvent( this ));
            }
        }
        
        private final class ModelListener extends JAXBWizEventListenerAdapter {

            @Override
            public void bindingAdded(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateKeys((Schemas) event.getSource());    
                    fireChange();
                }
            }
            
            @Override
            public void bindingDeleted(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateKeys((Schemas) event.getSource());    
                    fireChange();
                }
            }

            @Override
            public void configFileEdited(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateKeys((Schemas) event.getSource());    
                    fireChange();
                }
            }            
        }
    }
}
