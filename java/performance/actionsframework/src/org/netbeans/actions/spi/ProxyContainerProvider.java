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
/*
 * ProxyContainerProvider.java
 *
 * Created on January 24, 2004, 3:42 PM
 */

package org.netbeans.actions.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.actions.spi.ContainerProvider;

/** Container provider which can proxy other container providers.
 *
 * @author  Tim Boudreau
 */
public abstract class ProxyContainerProvider extends ContainerProvider {
    private Map caches = new HashMap();
    private String[] menuContainerContexts = null;
    private String[] toolbarContainerContexts = null;

    protected ProxyContainerProvider() {
    }

    protected abstract ContainerProvider[] getProviders();

    /** Get the cache for this particular container type, such as TYPE_TOOLBAR.
     * This cache is used to store previous reverse lookups, mapping container
     * contexts to the providers that provide them.  Implementations that
     * support container types not defined on ContainerProvider can use this
     * to create caches for additional types, if they override findProviderOf().
     */
    protected final Map getCache (Object type) {
        Map result = (Map) caches.get(type);
        if (result == null) {
            result = new HashMap();
            caches.put (type, result);
        }
        return result;
    }
    
    protected String[] sort (ContainerProvider[] providers, Object[] containers, int count) {
        String[] result = new String[count];
        int pos = 0;
        for (int i=0; i < providers.length; i++) {
            String[] curr = (String[]) containers[i];
            System.arraycopy (curr, 0, result, pos, curr.length);
            pos += curr.length;
        }
        return result;        
    }
    
    protected final void providersChanged() {
        caches.clear();
        menuContainerContexts = null;
        toolbarContainerContexts = null;
    }
    
    protected ContainerProvider findProviderOf (String containerCtx, Object type) {
        Map cache = getCache(type);
        ContainerProvider result = (ContainerProvider) cache.get(containerCtx);
        if (result == null && type == TYPE_TOOLBAR || type == TYPE_MENU) {
            ContainerProvider[] providers = getProviders();
            for (int i=0; i < providers.length; i++) {
                String[] curr = type == TYPE_TOOLBAR ? 
                    providers[i].getToolbarContainerContexts() : 
                    providers[i].getMenuContainerContexts();
                    
                if (Arrays.asList(curr).contains (containerCtx)) {
                    result = providers[i];
                    cache.put (containerCtx, providers[i]);
                    break;
                }
            }
        }
        return result;
    }
    
    public final int getContainerState(Object type, String containerCtx, java.util.Map context) {
        ContainerProvider provider = findProviderOf(containerCtx, type);
        return provider.getContainerState(type, containerCtx, context);
    }
    
    public final String[] getMenuContainerContexts() {
        if (menuContainerContexts != null) {
            return menuContainerContexts;
        }
        
        ContainerProvider[] providers = getProviders();
        Object[] items = new Object[providers.length];
        int total = 0;
        for (int i=0; i < providers.length; i++) {
            String[] curr = providers[i].getMenuContainerContexts();
            total += curr.length;
            items[i] = curr;
        }
        String[] result = sort (providers, items, total);
        if (result == null) {
            throw new NullPointerException ("Sort may not return null");
        }
        if (result.length != total) {
            throw new IllegalStateException ("Sort returned " + result.length + " items but was passed " + total);
        }
        menuContainerContexts = result;
        return result;
    }
    
    public final String[] getToolbarContainerContexts() {
        if (toolbarContainerContexts != null) {
            return toolbarContainerContexts;
        }
        
        ContainerProvider[] providers = getProviders();
        Object[] items = new Object[providers.length];
        int total = 0;
        for (int i=0; i < providers.length; i++) {
            String[] curr = providers[i].getToolbarContainerContexts();
            total += curr.length;
            items[i] = curr;
        }
        String[] result = sort (providers, items, total);
        if (result == null) {
            throw new NullPointerException ("Sort may not return null"); //NOI18N
        }
        if (result.length != total) {
            throw new IllegalStateException ("Sort returned " + result.length + 
                " items but was passed " + total); //NOI18N
        }
        toolbarContainerContexts = result;
        return result;
    }
    
    public String getDisplayName (Object type, String containerCtx) {
        ContainerProvider provider = findProviderOf(containerCtx, type);
        return provider.getDisplayName(type, containerCtx);
    }
    
}
