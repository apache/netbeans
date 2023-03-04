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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.openide.filesystems.FileObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openide.nodes.Node;

/**
 * @author pfiala
 */
public abstract class EntityAndSessionHelper implements PropertyChangeListener, PropertyChangeSource {
    
    protected final EntityAndSession ejb;
    protected final EjbJarMultiViewDataObject ejbJarMultiViewDataObject;
    protected final FileObject ejbJarFile;
    private List<PropertyChangeListener> listeners = new LinkedList<>();
    public AbstractMethodController abstractMethodController;
    
    public EntityAndSessionHelper(EjbJarMultiViewDataObject ejbJarMultiViewDataObject, EntityAndSession ejb) {
        this.ejb = ejb;
        this.ejbJarMultiViewDataObject = ejbJarMultiViewDataObject;
        this.ejbJarFile = ejbJarMultiViewDataObject.getPrimaryFile();
        ejbJarMultiViewDataObject.getEjbJar().addPropertyChangeListener(this);
    }
    
    public void removeInterfaces(boolean local) {
    }
    
    public void modelUpdatedFromUI() {
        ejbJarMultiViewDataObject.modelUpdatedFromUI();
    }
    
    public String getEjbClass() {
        return ejb.getEjbClass();
    }
    
    public String getLocal() {
        return ejb.getLocal();
    }
    
    public String getLocalHome() {
        return ejb.getLocalHome();
    }
    
    public String getRemote() {
        return ejb.getRemote();
    }
    
    public String getHome() {
        return ejb.getHome();
    }
    
    public void addInterfaces(boolean local) {
//        String packageName = Utils.getPackage(ejb.getEjbClass());
    }
    
    protected Node createEntityNode() {
        return null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    protected void firePropertyChange(PropertyChangeEvent evt) {
        for (Iterator<PropertyChangeListener> iterator = listeners.iterator(); iterator.hasNext();) {
            iterator.next().propertyChange(evt);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
    }
    
}
