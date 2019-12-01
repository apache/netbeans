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
package org.netbeans.modules.maven.model.settings.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.settings.spi.ElementFactory;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Registry for factories of Profiles elements. In order to register an ElementFactory,
 * a QName must be provided of an element for which the factory will create a
 * SettingsComponent.
 * @author mkleint
 */
public class ElementFactoryRegistry {
    
    private static ElementFactoryRegistry registry = null;
    private Map<QName, ElementFactory> factories = null;
    
    private ElementFactoryRegistry() {
        initialize();
    }
    
    public static ElementFactoryRegistry getDefault(){
        if (registry == null) {
            registry = new ElementFactoryRegistry();
        }
        return registry;
    }

    //TODO listen on changes when we have external extensions
    private void initialize(){
        factories = new Hashtable<QName, ElementFactory>();
        for (ElementFactory service : Lookup.getDefault().lookupAll(ElementFactory.class)) {
            register(service);
        }
        
        //try meta-inf services lookup using this class's classloader
        //if no factories are found, This is required for lookup to work
        //from comp app project ant task
        if (factories.size() < 1) {
            Lookup lu2 = Lookups.metaInfServices(this.getClass().getClassLoader());
            for (ElementFactory service : lu2.lookupAll(ElementFactory.class)) {
                register(service);
            }
        }
    }
    
    public void register(ElementFactory factory) {
        for (QName q : factory.getElementQNames()) {
            factories.put(q, factory);
        }
        resetQNameCache();
    }
    
    public void unregister(ElementFactory fac){
        for (QName q : fac.getElementQNames()) {
            factories.remove(q);
        }
        resetQNameCache();
    }
    
    public ElementFactory get(QName type) {
        return factories.get(type);
    }
    
    private Set<Class> knownEmbeddedModelTypes = null;
    private Set<QName> knownQNames = null;
    private Set<String> knownNames = null;
    
    public void resetQNameCache() {
        knownEmbeddedModelTypes = null;
        knownQNames = null;
        knownNames = null;
    }
    
    public Set<QName> getKnownQNames() {
        return Collections.unmodifiableSet(knownQNames());
    }
    
    private Set<QName> knownQNames() {
        if (knownQNames == null) {
            knownQNames = new HashSet<QName>();
            for (ElementFactory f : factories.values()) {
                for (QName q : f.getElementQNames()) {
                    if (! knownQNames.add(q)) {
                        String msg = "Duplicate factory for: "+q;
                        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "getKnownQNames", msg); // NOI18N
                    }
                }
            }
        }
        return knownQNames;
    }

    public Set<String> getKnownElementNames() {
        return Collections.unmodifiableSet(knownElementNames());
    }
    
    private Set<String> knownElementNames() {
        if (knownNames == null) {
            knownNames = new HashSet<String>();
            for (QName q : knownQNames()) {
                knownNames.add(q.getLocalPart());
            }
        }
        return knownNames;
    }
    
    public void addEmbeddedModelQNames(AbstractDocumentModel<?> embeddedModel) {
        if (knownEmbeddedModelTypes == null) {
            knownEmbeddedModelTypes = new HashSet();
        }
        if (! knownEmbeddedModelTypes.contains(embeddedModel.getClass())) {
            knownQNames().addAll(embeddedModel.getQNames());
            knownNames = null;
        }
    }
}
