/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
