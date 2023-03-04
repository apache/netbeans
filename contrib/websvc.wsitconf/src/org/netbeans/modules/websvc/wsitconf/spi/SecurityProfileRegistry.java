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

package org.netbeans.modules.websvc.wsitconf.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Grebac
 */
public class SecurityProfileRegistry {
    
    private static SecurityProfileRegistry instance;
    
    private Map<String, SecurityProfile> profiles = 
            Collections.synchronizedMap(new HashMap<String, SecurityProfile>());
    
    /**
     * Creates a new instance of SecurityProfileRegistry
     */
    private SecurityProfileRegistry() {}

    /**
     * Returns default singleton instance of registry
     * @return 
     */
    public static SecurityProfileRegistry getDefault(){
        if (instance == null) {
            instance = new SecurityProfileRegistry();
            instance.populateRegistry();
        }
        return instance;
    }
    
    /** 
     * Returns profile from list based on it's display name
     * @param displayName 
     * @return 
     */
    public SecurityProfile getProfile(String displayName) {
        return profiles.get(displayName);
    }
    
    /**
     * Registers profile to the list
     * @param profile 
     */
    public void register(SecurityProfile profile){
        profiles.put(profile.getDisplayName(), profile);
    }
    
    /**
     * Unregisters profile from the list
     * @param profile 
     */
    public void unregister(SecurityProfile profile){
        profiles.remove(profile.getDisplayName());
    }
    
    public void unregister(String profile){
        profiles.remove(profile);
    }
    
    public Set<SecurityProfile> getSecurityProfiles() {
        
        TreeSet<SecurityProfile> set = new TreeSet<SecurityProfile>(new Comparator<SecurityProfile>() {
            public int compare(SecurityProfile o1, SecurityProfile o2) {
                Integer i1 = o1.getId();
                Integer i2 = o2.getId();
                return i1.compareTo(i2);
            }
        });
        set.addAll(profiles.values());
        return Collections.unmodifiableSet(Collections.synchronizedSortedSet(set));
    }
    
    private final SecurityProfileRegistry populateRegistry() {
        SecurityProfileRegistry registry = SecurityProfileRegistry.getDefault();
        if (registry.getSecurityProfiles().isEmpty()) {
            Lookup.Result results = Lookup.getDefault().
                    lookup(new Lookup.Template<SecurityProfile>(SecurityProfile.class));
            Collection<SecurityProfile> profs = results.allInstances();
            for (SecurityProfile p : profs) {
                registry.register(p); 
            }
        }
        return registry;
    }
    
}
