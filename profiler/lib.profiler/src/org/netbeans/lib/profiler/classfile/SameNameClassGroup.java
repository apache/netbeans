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

package org.netbeans.lib.profiler.classfile;

import java.util.ArrayList;
import java.util.List;


/**
 * A container for a group of classes/placeholders with the same name and different classloaders,
 * plus the functionality to browse this group and check for compatible classes.
 *
 * @author Tomas Hurka
 * @author Misha Dmitirev
 */
public class SameNameClassGroup {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private List classes;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SameNameClassGroup() {
        classes = new ArrayList(4); // Hope we are not going to have too many class versions...
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public List getAll() {
        return classes;
    }

    public void add(BaseClassInfo clazz) {
        classes.add(clazz);
    }

    /**
     * Check if class clazz with its existing loader is compatible with loader classLoaderId - that is,
     * loader classLoaderId will return this class if asked for the class with this name.
     */
    public static BaseClassInfo checkForCompatibility(BaseClassInfo clazz, int classLoaderId) {
        int entryLoader = clazz.getLoaderId();

        if (entryLoader == classLoaderId) {
            return clazz;
        } else {
            if (isParentLoaderTo(entryLoader, classLoaderId)) {
                return clazz;
            } else if (clazz instanceof PlaceholderClassInfo && isParentLoaderTo(classLoaderId, entryLoader)) { // This can happen at least with placeholders
                clazz.setLoaderId(classLoaderId);

                return clazz;
            } else if (classLoaderId > 0) {
                // In some cases, the class loader graph for the app may be a non-tree structure, i.e. one class loader may delegate
                // not just to its parent loader, but to some other loader(s) as well. In that case, our last resort is to ask for
                // its defining loader.          
                int loader = ClassRepository.getDefiningClassLoaderId(clazz.getName(), classLoaderId);

                if (loader == -1) {
                    return null;
                }

                if (loader == entryLoader) {
                    return clazz;
                }
            }

            return null;
        }
    }

    /** Find a class compatible with the given loader (see definition in checkFroCompatibility()) in this group. */
    public BaseClassInfo findCompatibleClass(int classLoaderId) {
        int size = classes.size();
        for (int i = 0; i < size; i++) {
            BaseClassInfo clazz = (BaseClassInfo) classes.get(i);
            if (clazz.getLoaderId() == classLoaderId) {
                return clazz;
            }
        }
        for (int i = 0; i < size; i++) {
            BaseClassInfo clazz = (BaseClassInfo) classes.get(i);
            clazz = checkForCompatibility(clazz, classLoaderId);

            if (clazz != null) {
                return clazz;
            }
        }

        return null;
    }

    public void replace(BaseClassInfo clazz1, BaseClassInfo clazz2) {
        classes.remove(clazz1);
        classes.add(clazz2);
    }

    private static boolean isParentLoaderTo(int testParentLoader, int testChildLoader) {
        int parent = ClassLoaderTable.getParentLoader(testChildLoader);

        while (parent != testParentLoader) {
            if (parent == 0) {
                return false;
            } else {
                parent = ClassLoaderTable.getParentLoader(parent);
            }
        }

        return true;
    }
}
