/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
