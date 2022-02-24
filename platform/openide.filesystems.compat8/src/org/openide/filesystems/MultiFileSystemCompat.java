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
        List<SystemAction> al = new ArrayList<>(101); // randomly choosen constant
        Set<SystemAction> uniq = new HashSet<>(101); // not that randommly choosen

        FileSystem[] del = this.getDelegates();

        for (FileSystem del1 : del) {
            if (del1 == null) {
                continue;
            }
            SystemAction[] acts = compat(del1).getActions();
            for (SystemAction act : acts) {
                if (uniq.add(act)) {
                    al.add(act);
                }
            }
        }

        return al.toArray(new SystemAction[al.size()]);
    }

    public SystemAction[] getActions(final Set<FileObject> foSet) {
        List<SystemAction> al = new ArrayList<>(101); // randomly choosen constant
        Set<SystemAction> uniq = new HashSet<>(101); // not that randommly choosen

        final FileSystem[] del = this.getDelegates();

        for (FileSystem del1 : del) {
            if (del1 == null) {
                continue;
            }
            final SystemAction[] acts = compat(del1).getActions(foSet);
            for (SystemAction act : acts) {
                if (uniq.add(act)) {
                    al.add(act);
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

        for (FileSystem layer : layers) {
            if (layer != null) {
                try {
                    compat(layer).prepareEnvironment(env);
                }catch (EnvironmentNotSupportedException ense) {
                    // Fine.
                }
            }
        }
    }

    protected abstract FileSystem[] getDelegates();
}
