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

package org.netbeans.modules.websvc.saas.model.wsdl.impl;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.WeakHashMap;

/**
 *
 * @author mkuchtiak
 */
public class WsdlModelerFactory {
    
    private static WsdlModelerFactory factory;
    WeakHashMap<URL, WeakReference<WsdlModeler>> modelers;
    
    /** Creates a new instance of WsdlModelerFactory */
    private WsdlModelerFactory() {
        modelers = new WeakHashMap<>(5);
    }
    
    /**
    * Accessor method for WsdlModelerFactory singleton
    * @return WsdlModelerFactory object
    */
    public static synchronized WsdlModelerFactory getDefault() {
        if (factory==null) factory = new WsdlModelerFactory();
        return factory;
    }
    
    /** Get WsdlModeler for particular WSDL
     */
    public WsdlModeler getWsdlModeler(URL wsdlUrl) {
        WsdlModeler modeler = null;
        synchronized (modelers) {
            modeler = getFromCache(wsdlUrl);
            if (modeler!=null) {
                return modeler;
            }
            modeler = new WsdlModeler(wsdlUrl);
            modelers.put(wsdlUrl, new WeakReference<>(modeler));
        }
        return modeler;
    }
    
    private WsdlModeler getFromCache (URL url) {
        if (url == null) {
            return null;
        }
        WeakReference<WsdlModeler> wr = modelers.get(url);
        if (wr == null) {
            return null;
        }
        WsdlModeler modeler = (WsdlModeler) wr.get();
        if (modeler == null) {
            modelers.remove(url);
        }
        return modeler;
    }
    
    int mapLength() {
        return modelers.size();
    }
    
    
}
