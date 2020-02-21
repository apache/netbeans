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
