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
package org.netbeans.modules.cnd.makeproject.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.openide.util.Lookup;

public class PackagerManager {

    private static final PackagerManager instance = new PackagerManager();


    static {
        instance.addRegisteredPackagers();
    }
    private final List<PackagerDescriptor> list = new ArrayList<>();

    public static PackagerManager getDefault() {
        return instance;
    }

    private PackagerManager() {
    }

    /*
     * Installed via services
     */
    private void addRegisteredPackagers() {
        Set<PackagerDescriptorProvider> set = getPackagerDescriptorProviders();
        for (PackagerDescriptorProvider packagerDescriptorProvider : set) {
            List<PackagerDescriptor> aList = packagerDescriptorProvider.getPackagerDescriptorProviderList();
            for (PackagerDescriptor packagerDescriptor : aList) {
                addPackagingDescriptor(packagerDescriptor);
            }
        }
    }

    public void addPackagingDescriptor(PackagerDescriptor packagingDescriptor) {
        synchronized (list) {
            PackagerDescriptor packagerDescriptor = getPackager(packagingDescriptor.getName());
            if (packagerDescriptor != null) {
                return; // Already there...
            }
            list.add(packagingDescriptor);
        }
    }

    public List<PackagerDescriptor> getPackagerList() {
        synchronized (list) {
            return new ArrayList<>(list);
        }
    }

    public PackagerDescriptor getPackager(String name) {
        for (PackagerDescriptor packagerDescriptor : getPackagerList()) {
            if (packagerDescriptor.getName().equals(name)) {
                return packagerDescriptor;
            }
        }
        return null;
    }

    public int getNameIndex(String name) {
        int index = 0;
        for (PackagerDescriptor packagerDescriptor : getPackagerList()) {
            if (packagerDescriptor.getName().equals(name)) {
                return index;
            }
            index++;
        }
        return 0;
    }

    public String[] getDisplayNames() {
        List<PackagerDescriptor> aList = getPackagerList();
        String[] ret = new String[aList.size()];
        int i = 0;
        for (PackagerDescriptor packagerDescriptor : aList) {
            ret[i++] = packagerDescriptor.getDisplayName();
        }
        return ret;
    }

    public String getDisplayName(String name) {
        for (PackagerDescriptor packagerDescriptor : getPackagerList()) {
            if (packagerDescriptor.getName().equals(name)) {
                return packagerDescriptor.getDisplayName();
            }
        }
        return null;
    }

    public String getName(String displayName) {
        for (PackagerDescriptor packagerDescriptor : getPackagerList()) {
            if (packagerDescriptor.getDisplayName().equals(displayName)) {
                return packagerDescriptor.getName();
            }
        }
        return null;
    }

    /*
     * Get list of packager providers registered via services
     */
    private static Set<PackagerDescriptorProvider> getPackagerDescriptorProviders() {
        HashSet<PackagerDescriptorProvider> providers = new HashSet<>();
        Lookup.Template<PackagerDescriptorProvider> template = new Lookup.Template<>(PackagerDescriptorProvider.class);
        Lookup.Result<PackagerDescriptorProvider> result = Lookup.getDefault().lookup(template);
        Iterator iterator = result.allInstances().iterator();
        while (iterator.hasNext()) {
            Object caop = iterator.next();
            if (caop instanceof PackagerDescriptorProvider) {
                providers.add((PackagerDescriptorProvider) caop);
            }
        }
        return providers;
    }
}
