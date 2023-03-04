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

package org.netbeans.modules.websvc.project.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * This class is used to merge the content of WebServiceDataProvider instances in the project. Use the static method to create a 
 * LookupMerger for WebServiceDataProvider and register it in the project lookup.
 * @see WebServiceDataProvider
 * @author mkuchtiak
 */
public class LookupMergerSupport {
    /**
     * Factory method for creating {@link org.netbeans.spi.project.LookupMerger} instance that merges
     * {@link WebServiceDataProvider} instances in the project lookup. A composite {@link WebServiceDataProvider} 
     * which contains the merged content of all providers can be obtained from this LookupMerger.
     * @return instance of LookupMerger to include in the project lookup
     */
    public static LookupMerger<WebServiceDataProvider> createWebServiceDataProviderMerger() {
        return new WebServiceDataProviderMerger();
    }
    
    private static class WebServiceDataProviderMerger implements LookupMerger<WebServiceDataProvider> {
        public Class<WebServiceDataProvider> getMergeableClass() {
            return WebServiceDataProvider.class;
        }

        public WebServiceDataProvider merge(Lookup lookup) {
            return new WebServiceDataProviderImpl(lookup);
        }
    }
    
    private static class WebServiceDataProviderImpl implements WebServiceDataProvider, LookupListener {
        private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
        private final Lookup.Result<WebServiceDataProvider> delegates;
        private Collection<WebServiceDataProvider> wsDataProviders = new ArrayList<WebServiceDataProvider>();

        WebServiceDataProviderImpl(Lookup lookup) {
            // clearing WebServiceDataProvider collection
            if (wsDataProviders.size() > 0) {
                for (WebServiceDataProvider old : wsDataProviders) {
                    for (PropertyChangeListener pcl:changeSupport.getPropertyChangeListeners()) {
                        old.removePropertyChangeListener(pcl);
                    }
                }
                wsDataProviders.clear();
            }
            
            // create delegates and new WebServiceDataProvider collection 
            Lookup.Result<WebServiceDataProvider> srcs = lookup.lookupResult(WebServiceDataProvider.class);
            for (WebServiceDataProvider ns : srcs.allInstances()) {
                for (PropertyChangeListener pcl:changeSupport.getPropertyChangeListeners()) {
                    ns.addPropertyChangeListener(pcl);
                }
                wsDataProviders.add(ns);
            }
            srcs.addLookupListener(this);
            delegates = srcs;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            for (WebServiceDataProvider provider: wsDataProviders) {
                provider.addPropertyChangeListener(listener);
            }
            changeSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            changeSupport.removePropertyChangeListener(listener);
            for (WebServiceDataProvider provider: wsDataProviders) {
                provider.removePropertyChangeListener(listener);
            }
        }

        public void resultChanged(LookupEvent ev) {
            if (wsDataProviders.size() > 0) {
                for (WebServiceDataProvider old : wsDataProviders) {
                    for (PropertyChangeListener pcl:changeSupport.getPropertyChangeListeners()) {
                        old.removePropertyChangeListener(pcl);
                    }
                }
                wsDataProviders.clear();
            }
            delegates.removeLookupListener( this );
            for (WebServiceDataProvider ns : delegates.allInstances()) {
                    for (PropertyChangeListener pcl:changeSupport.getPropertyChangeListeners()) {
                        ns.addPropertyChangeListener(pcl);
                    }
                wsDataProviders.add(ns);
            }
            delegates.addLookupListener( this );
        }

        public List<WebService> getServiceProviders() {
            assert delegates != null;
            List<WebService> result = new ArrayList<WebService>();
            for (WebServiceDataProvider ns : delegates.allInstances()) {
                List<WebService> providers = ns.getServiceProviders();
                if (providers != null) {
                    result.addAll(providers);
                }
            }
            return result;
        }

        public List<WebService> getServiceConsumers() {
            assert delegates != null;
            List<WebService> result = new ArrayList<WebService>();
            for (WebServiceDataProvider ns : delegates.allInstances()) {
                List<WebService> providers = ns.getServiceConsumers();
                if (providers != null) {
                    result.addAll(providers);
                }
            }
            return result;
        }
    }
    
}
