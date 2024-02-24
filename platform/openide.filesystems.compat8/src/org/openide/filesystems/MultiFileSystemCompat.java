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

        return al.toArray(new SystemAction[0]);
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

        return al.toArray(new SystemAction[0]);
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
