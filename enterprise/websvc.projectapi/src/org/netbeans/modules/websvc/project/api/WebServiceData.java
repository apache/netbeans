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


package org.netbeans.modules.websvc.project.api;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.project.spi.WebServiceDataProvider;

/**
 *
 * @author mkuchtiak
 * Main API for accessing the (@link WebService)s in the project. Use the static method to get an instance. Calls are delegated to the appropriate
 * WebServiceDataProvider.
 */
public final class WebServiceData {

    private WebServiceDataProvider wsProvider;

    /**
     * Static method for getting an instance of a WebServiceData that encapsulates an instance of WebServiceDataProvider implementation.
     * @param p Project for which web service data is being queried from.
     * @return WebServiceData instance that delegates to the WebServiceDataProvider in the project.
     */
    public static WebServiceData getWebServiceData(Project p) {
        WebServiceDataProvider provider = p.getLookup().lookup(WebServiceDataProvider.class);
        return provider != null ? new WebServiceData(provider) : null;
    }

    private WebServiceData(WebServiceDataProvider wsProvider) {
        this.wsProvider = wsProvider;
    }

    /**
     * Returns a list of WebServices that act as service providers
     * @return List of WebServices
     */
    public List<WebService> getServiceProviders() {
        return wsProvider.getServiceProviders();
    }

    /**
     * Returns a list of WebServices that act as service consumers
     * @return List of WebServices
     */
    public List<WebService> getServiceConsumers() {
        return wsProvider.getServiceConsumers();
    }

     /**
     * Adds a PropertyChangeListener to the listener list.
     * @param pcl PropertyChangeListener to be added to the list.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        wsProvider.addPropertyChangeListener(pcl);
    }

     /**
     * Removes a PropertyChangeListener from the listener list.
     * @param pcl PropertyChangeListener to be removed from the list.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        wsProvider.removePropertyChangeListener(pcl);
    }

    /**
     * Compares this WebServiceData with specified Object for equality.
     *
     * @param  obj Object to which this WebServiceData is to be compared.
     * @return <tt>true</tt> if and only if the specified Object is a
     *	       WebServiceData and if it delegates to the same {@link org.netbeans.modules.websvc.project.spi.WebServiceDataProvider} SPI object.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebServiceData other = (WebServiceData) obj;
        if (this.wsProvider != other.wsProvider && (this.wsProvider == null || !this.wsProvider.equals(other.wsProvider))) {
            return false;
        }
        return true;
    }

    /**
     * Returns the hash code for this WebServiceData object.
     *
     * @return hash code for this WebServiceData.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.wsProvider != null ? this.wsProvider.hashCode() : 0);
        return hash;
    }

}
