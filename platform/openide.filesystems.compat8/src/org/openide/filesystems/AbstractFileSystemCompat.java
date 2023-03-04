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

package org.openide.filesystems;

import java.util.Enumeration;
import java.util.StringTokenizer;
import org.openide.modules.PatchFor;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.SystemAction;

/**
 * 8.0 compatibility patch for AbstractFileSystem.
 * @author sdedic
 */
@PatchFor(AbstractFileSystem.class)
public abstract class AbstractFileSystemCompat extends FileSystem {
    /** system actions for this FS if it has refreshTime != 0 */
    private static SystemAction[] SYSTEM_ACTIONS;

    /** system actions for this FS */
    private static final SystemAction[] NO_SYSTEM_ACTIONS = new SystemAction[] {  };

    
    /* Finds file when its name is provided.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one want to obtain name of package and not file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    package and not file name
    *
    * @warning when one of name or ext is <CODE>null</CODE> then name and
    *    ext should be ignored and scan should look only for a package
    *
    * @return FileObject that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    @Deprecated
    public FileObject find(String aPackage, String name, String ext) {
        // create enumeration of name to look for
        Enumeration<String> st = NbCollections.checkedEnumerationByFilter(new StringTokenizer(aPackage, "."), String.class, true); // NOI18N

        if ((name == null) || (ext == null)) {
            // search for folder, return the object only if it is folder
            FileObject fo = afs().getAbstractRoot().find(st);

            return ((fo != null) && fo.isFolder()) ? fo : null;
        } else {
            Enumeration<String> en = Enumerations.concat(st, Enumerations.singleton(name + '.' + ext));

            // tries to find it (can return null)
            return afs().getAbstractRoot().find(en);
        }
    }
    
    private AbstractFileSystem afs() {
        return (AbstractFileSystem)(Object)this;
    }

    /* Action for this filesystem.
    *
    * @return refresh action
    */
    public SystemAction[] getActions() {
        if (!afs().isEnabledRefreshFolder()) {
            return NO_SYSTEM_ACTIONS;
        } else {
            if (SYSTEM_ACTIONS == null) {
                try {
                    ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

                    if (l == null) {
                        l = getClass().getClassLoader();
                    }

                    Class<?> c = Class.forName("org.openide.actions.FileSystemRefreshAction", true, l); // NOI18N
                    SystemAction ra = SharedClassObject.findObject(c.asSubclass(SystemAction.class), true);

                    // initialize the SYSTEM_ACTIONS
                    SYSTEM_ACTIONS = new SystemAction[] { ra };
                } catch (Exception ex) {
                    // ok, we are probably running in standalone mode and
                    // classes needed to initialize the RefreshAction are
                    // not available
                    SYSTEM_ACTIONS = NO_SYSTEM_ACTIONS;
                }
            }

            return SYSTEM_ACTIONS;
        }
    }
}
