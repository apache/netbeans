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
package org.netbeans.modules.java.classpath;

import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.WeakListeners;

/** ProxyClassPathImplementation provides read only proxy for ClassPathImplementations.
 *  The order of the resources is given by the order of its delegates.
 *  The proxy is designed to be used as a union of class paths.
 *  E.g. to be able to easily iterate or listen on all design resources = sources + compile resources
 */
public class ProxyClassPathImplementation implements ClassPathImplementation {

    private ClassPathImplementation[] classPaths;
    private List<PathResourceImplementation> resourcesCache;
    private ArrayList<PropertyChangeListener> listeners;
    private PropertyChangeListener classPathsListener;

    public ProxyClassPathImplementation (ClassPathImplementation[] classPaths) {
        if (classPaths == null)
            throw new IllegalArgumentException ();
        List<ClassPathImplementation> impls = new ArrayList<ClassPathImplementation> ();
        classPathsListener = new DelegatesListener ();
        for (ClassPathImplementation cpImpl : classPaths) {
            if (cpImpl == null)
                continue;
            cpImpl.addPropertyChangeListener (WeakListeners.propertyChange(classPathsListener,cpImpl));
            impls.add (cpImpl);
        }
        this.classPaths = impls.toArray(new ClassPathImplementation[0]);
    }



    public List <? extends PathResourceImplementation> getResources() {
        synchronized (this) {
            if (this.resourcesCache != null) {
                return this.resourcesCache;
            }
        }
        
        ArrayList<PathResourceImplementation> result = new ArrayList<PathResourceImplementation> (classPaths.length*10);
        for (ClassPathImplementation cpImpl : classPaths) {
            List<? extends PathResourceImplementation> subPath = cpImpl.getResources();
            if (subPath == null)
                throw new NullPointerException (
                    "ClassPathImplementation.getResources() returned null. ClassPathImplementation.class: "
                    + cpImpl.getClass ().toString () + " ClassPathImplementation: " + cpImpl.toString ()
                );
            result.addAll (subPath);
        }
        
        synchronized (this) {
            if (this.resourcesCache == null) {
                resourcesCache = Collections.unmodifiableList (result);
            }
            return this.resourcesCache;
        }
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.listeners == null)
            this.listeners = new ArrayList<PropertyChangeListener> ();
        this.listeners.add (listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.listeners == null)
            return;
        this.listeners.remove (listener);
    }
    
    public String toString () {
        StringBuilder builder = new StringBuilder("[");   //NOI18N
        for (ClassPathImplementation cpImpl : this.classPaths) {
            builder.append (cpImpl.toString());
            builder.append(", ");   //NOI18N
        }
        builder.append ("]");   //NOI18N
        return builder.toString ();
    }


    private class DelegatesListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener[] _listeners;
            synchronized (ProxyClassPathImplementation.this) {
                ProxyClassPathImplementation.this.resourcesCache = null;    //Clean the cache
                if (ProxyClassPathImplementation.this.listeners == null)
                    return;
                _listeners = ProxyClassPathImplementation.this.listeners.toArray(new PropertyChangeListener[0]);
            }
            PropertyChangeEvent event = new PropertyChangeEvent (ProxyClassPathImplementation.this, evt.getPropertyName(),null,null);
            for (PropertyChangeListener l : _listeners) {
                l.propertyChange (event);
            }
        }
    }

}
