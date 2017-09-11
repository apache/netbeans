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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.modules.PatchFor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author sdedic
 */
@PatchFor(MultiFileSystem.class)
public abstract class MultiFileSystemCompat extends FileSystem {
    /** Merge actions from all delegates.
    */
    public SystemAction[] getActions() {
        List<SystemAction> al = new ArrayList<SystemAction>(101); // randomly choosen constant
        Set<SystemAction> uniq = new HashSet<SystemAction>(101); // not that randommly choosen

        FileSystem[] del = this.getDelegates();

        for (int i = 0; i < del.length; i++) {
            if (del[i] == null) {
                continue;
            }

            SystemAction[] acts = compat(del[i]).getActions();

            for (int j = 0; j < acts.length; j++) {
                if (uniq.add(acts[j])) {
                    al.add(acts[j]);
                }
            }
        }

        return al.toArray(new SystemAction[al.size()]);
    }

    public SystemAction[] getActions(final Set<FileObject> foSet) {
        List<SystemAction> al = new ArrayList<SystemAction>(101); // randomly choosen constant
        Set<SystemAction> uniq = new HashSet<SystemAction>(101); // not that randommly choosen

        final FileSystem[] del = this.getDelegates();

        for (int i = 0; i < del.length; i++) {
            if (del[i] == null) {
                continue;
            }

            final SystemAction[] acts = compat(del[i]).getActions(foSet);

            for (int j = 0; j < acts.length; j++) {
                if (uniq.add(acts[j])) {
                    al.add(acts[j]);
                }
            }
        }

        return al.toArray(new SystemAction[al.size()]);
    }
    
    static FileSystemCompat compat(FileSystem fs) {
        Object o = fs;
        return (FileSystemCompat)o;
    }

    /** Lets any sub filesystems prepare the environment.
     * If they do not support it, it does not care.
     * @deprecated Useless.
     */
    @Deprecated
    public void prepareEnvironment(FileSystem$Environment env)
    throws EnvironmentNotSupportedException {
        FileSystem[] layers = getDelegates();

        for (int i = 0; i < layers.length; i++) {
            if (layers[i] != null) {
                try {
                    compat(layers[i]).prepareEnvironment(env);
                } catch (EnvironmentNotSupportedException ense) {
                    // Fine.
                }
            }
        }
    }

    protected abstract FileSystem[] getDelegates();
}
