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
 * ProxyContextProvider.java
 *
 * Created on January 25, 2004, 9:28 PM
 */

package org.netbeans.actions.spi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.actions.api.ContextProvider;

/** Convenience implementation of ContextProvider which can proxy an
 * array of ContextProviders.
 *
 * @author  Tim Boudreau
 */
public final class ProxyContextProvider implements ContextProvider {
    private ContextProvider[] providers = null;

    public ProxyContextProvider() {
    }

    /** Creates a new instance of ProxyContextProvider */
    public ProxyContextProvider(ContextProvider[] providers) {
        setProviders(providers);
        assert !Arrays.asList(providers).contains(this) :
            "ProxyContextProvider cannot recursively proxy itself"; //NOI18N
    }

    /** Set the providers from which this provider will compose its
     * context */
    public void setProviders(ContextProvider[] providers) {
        this.providers = providers;
    }
    
    public Map getContext() {
        if (providers == null || providers.length == 0) {
            return Collections.emptyMap();
        }
        Map[] m = new Map[providers.length];
        for (int i=0; i < m.length; i++) {
            if (providers[i] == this) {
                throw new IllegalStateException (
                "ProxyContextProvider cannot recursively proxy itself"); //NOI18N
            }
            m[i] = providers[i].getContext();
        }
        return new ContextProviderSupport.ProxyMap(m);
    }    
    
}
