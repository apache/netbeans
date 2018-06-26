/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
