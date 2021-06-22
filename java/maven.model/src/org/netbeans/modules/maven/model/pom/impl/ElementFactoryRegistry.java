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
package org.netbeans.modules.maven.model.pom.impl;

import java.util.Hashtable;
import java.util.Map;
import javax.xml.namespace.QName;

import org.netbeans.modules.maven.model.pom.spi.ElementFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Registry for factories of POM elements. In order to register an ElementFactory,
 * a QName must be provided of an element for which the factory will create a
 * POMComponent.
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
        for (ElementFactory service : Lookup.getDefault().lookupAll(ElementFactory.class)){
            register(service);
        }
        
        //try meta-inf services lookup using this class's classloader
        //if no factories are found, This is required for lookup to work
        //from comp app project ant task
        if (factories.size() < 1) {
            Lookup lu2 = Lookups.metaInfServices(this.getClass().getClassLoader());
            for (ElementFactory service : lu2.lookupAll(ElementFactory.class)){
                register(service);
            }
        }
    }
    
    private void register(ElementFactory factory) {
        for (QName q : factory.getElementQNames()) {
            factories.put(q, factory);
        }
    }
    
    public ElementFactory get(QName type) {
        return factories.get(type);
    }
    
}
