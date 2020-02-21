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

package org.netbeans.modules.cnd.spi.remote;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * A factory for RemoteSyncWorker
 */
public abstract class RemoteSyncFactory {

    /**
     * Creates an instance of RemoteSyncWorker.
     *
     * @param files local directories and files that should be synchronized
     *
     * @param executionEnvironment
     *
     * @param out output stream:
     * in the case implementation uses an external program (rsync? scp?),
     * it can redirect its stdout here
     *
     * @param err error stream:
     * in the case implementation uses an external program (rsync? scp?),
     * it can redirect its stderr here
     *
     * @param privProjectStorageDir a directory to store misc. cache-ing information;
     * it is caller's responsibility top guarantee that different local dirs
     * has different privProjectStorage associated
     * (usually it is "nbprohect/private" :-))
     *
     * @return new instance of the RemoteSyncWorker
     */
    public abstract RemoteSyncWorker createNew(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, 
            FileObject privProjectStorageDir, String workingDir, List<FSPath> files, List<FSPath> buildResults);
    
    @Deprecated
    public RemoteSyncWorker createNew(ExecutionEnvironment executionEnvironment, 
            PrintWriter out, PrintWriter err, FileObject privProjectStorageDir, String workingDir, 
            FSPath... files) {
        return createNew(executionEnvironment, out, err, privProjectStorageDir, workingDir,                
                Arrays.asList(files), Collections.<FSPath>emptyList());
    }

    /**
     * Creates an instance of RemoteSyncWorker.
     *
     * @param project determines executionEnvironment and dirs to sync
     *
     * @param out output stream:
     * in the case implementation uses an external program (rsync? scp?),
     * it can redirect its stdout here
     *
     * @param err error stream:
     * in the case implementation uses an external program (rsync? scp?),
     * it can redirect its stderr here
     *
     * @return new instance of the RemoteSyncWorker
     */
    public abstract RemoteSyncWorker createNew(Lookup.Provider project,
            PrintWriter out, PrintWriter err);

    /**
     * Determines whether this factory is applicable for the given execution environment
     * @param execEnv execution environment
     * @return true in the case this factory is applicable, otherwise false
     */
    public abstract boolean isApplicable(ExecutionEnvironment execEnv);

    /**
     * Returns a name of this factory to display in the UI
     * @return a name of this factory to be displayed in the UI
     */
    public abstract String getDisplayName();

    /**
     * Returns a brief description of this factory to be used for tool tips, etc
     * @return a brief description of this factory
     */
    public abstract String getDescription();

    /**
     * Returns a unique string that identifies this factory
     * among the others. It can be stored in preferences,
     * and be restored later via fromID()
     * @return A unique string that identifies this factory
     */
    public abstract String getID();
    
    /**
     * Determines whether files are copies to remote host 
     * (as in FTP, AutoCopy) or not (as in Shared and FullRemote)
     * @return true i
     */
    public abstract boolean isCopying();

    /**
     * Gets a factory by its ID. See comments to getID() method.
     * @param id
     * @return
     */
    public static RemoteSyncFactory fromID(String id) {
        assert id != null;
        for (RemoteSyncFactory factory : getFactories()) {
            if (id.equals(factory.getID())) {
                return factory;
            }
        }
        Logger log = Logger.getLogger("org.netbeans.modules.cnd.spi.remote"); // NOI18N
        log.log(Level.SEVERE, "No RemoteSyncFactory found by with ID {0}", id); //NOI18N
        return null;
    }

    /**
     * Gets all available factories.
     * That's just a shortcut for the standard Lookup calls.
     * @return
     */
    public static RemoteSyncFactory[] getFactories() {
        final Collection<? extends RemoteSyncFactory> instances = Lookup.getDefault().lookupAll(RemoteSyncFactory.class);
        List<RemoteSyncFactory> result = new ArrayList<RemoteSyncFactory>(instances);
        String defaultId = System.getProperty("cnd.remote.default.sync");
        if (defaultId != null) {
            for (int i = 0; i < result.size(); i++) {
                if (defaultId.equals(result.get(i).getID())) {
                    if (i > 0) {
                        RemoteSyncFactory oldFirst = result.get(0);
                        result.set(0, result.get(i));
                        result.set(i, oldFirst);
                    }
                    break;
                }
            }
        }
        return result.toArray(new RemoteSyncFactory[result.size()]);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }


    public static RemoteSyncFactory getDefault() {
        RemoteSyncFactory[] factories = getFactories();
        assert factories.length > 0;
        return factories[0];
    }

    public boolean isPathMappingCustomizable() {
        return false;
    }

    public abstract PathMap getPathMap(ExecutionEnvironment executionEnvironment);
}
