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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import org.openide.util.RequestProcessor;

public class JaxWsRootChildren extends Children.Keys<Service> {
    
    private static final RequestProcessor JAX_WS_ROOT_RP =
            new RequestProcessor(JaxWsRootChildren.class); //NOI18N
    
    JaxWsModel jaxWsModel;
    Service[] services;
    JaxWsListener listener;
    FileObject[] srcRoots;

    private final RequestProcessor.Task updateNodeTask = JAX_WS_ROOT_RP.create(new Runnable() {
        public void run() {
            updateKeys();
        }
    });
    
    public JaxWsRootChildren(JaxWsModel jaxWsModel, FileObject[] srcRoots) {
        this.jaxWsModel = jaxWsModel;
        this.srcRoots=srcRoots;
    }
    
    @Override
    protected void addNotify() {
        listener = new JaxWsListener();
        jaxWsModel.addPropertyChangeListener(listener);
        updateKeys();
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.<Service>emptySet());
        jaxWsModel.removePropertyChangeListener(listener);
    }
       
    private void updateKeys() {
        List<Service> keys = new ArrayList<Service>();
        services = jaxWsModel.getServices();
        if (services != null) {
            for (int i = 0; i < services.length; i++) {
                //WebServiceWrapper key = new WebServiceWrapper(webServiceDescription);
                keys.add(services[i]);
            }
        }
        setKeys(keys);
    }

    protected Node[] createNodes(Service key) {
        String implClass = key.getImplementationClass();
        for (FileObject srcRoot:srcRoots) {
            FileObject implClassFo = getImplementationClass(implClass, srcRoot);
            if (implClassFo!=null)
                return new Node[] {new JaxWsNode(jaxWsModel, key, srcRoot, implClassFo)};
        }
        return new Node[0];
    }
    
    class JaxWsListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateNodeTask.schedule(2000);
        }        
    }
    
    private FileObject getImplementationClass(String implClass, FileObject srcRoot) {
        if(implClass != null && srcRoot!=null) {
            return srcRoot.getFileObject(implClass.replace('.','/')+".java");
            //return JMIUtils.findClass(implBean, srcRoot);
        }
        return null;
    }
    
}
