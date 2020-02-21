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
package org.netbeans.modules.remote.impl.fs;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;

/**
 * Manages paths that are marked as "delete on exit".
 */
public class DeleteOnExitSupport {

    private final ExecutionEnvironment execEnv;
    private final File cache;

    /** If the ALLOW_ALTERNATIVE_DELETE_ON_EXIT is ON and transport does not support delete-on-exit, 
     *  then alternative delete-on-exit will work */
    private static final boolean ALLOW_ALTERNATIVE_DELETE_ON_EXIT = 
            RemoteFileSystemUtils.getBoolean("remote.alternative.delete.on.exit", true);

    private static final String DELETE_ON_EXIT_FILE_NAME = ".rfs_delete_on_exit"; // NOI18N

    private static final Object lock = new Object();
    
    // The idea about filesToDelete and filesToRemember is as follows:
    // When a file is marked as "delete on exit", it is added to filesToRemember
    // When a disconnect occurs, we move all files from filesToRemember into filesToDelete
    // (and also store them on disk)
    // When connnect occurs, filesToDelete are deleted.
    // This prevents sync issues 
    
    /** guarded by lock */
    private final LinkedHashSet<String> filesToDelete = new LinkedHashSet<>();

    /** guarded by lock */
    private final LinkedHashSet<String> filesToRemember = new LinkedHashSet<>();

    
    public DeleteOnExitSupport(ExecutionEnvironment execEnv, File cacheRoot) {
        this.execEnv = execEnv;
        this.cache = new File(cacheRoot, DELETE_ON_EXIT_FILE_NAME);
        if (ALLOW_ALTERNATIVE_DELETE_ON_EXIT) {
            synchronized (lock) {
                loadDeleteOnExit(cache, filesToDelete);
            }
        }
    }
    
    /** Called directly from ConnectionListener.connected */
    public void notifyConnected() {        
    }

    /** Called directly from ConnectionListener.disconnected */
    public void notifyDisconnected() {        
        if (ALLOW_ALTERNATIVE_DELETE_ON_EXIT) {
            List<String> paths;
            synchronized (lock) {
                filesToDelete.addAll(filesToRemember);
                filesToRemember.clear();
                paths = new ArrayList<>(filesToDelete);
            }
            storeDeleteOnExit(cache, paths);
        }
    }
    /**
     * Is called from the request processor 
     * in reaction on connect OR disconnect
     */
    public void processConnectionChange() {
        if (ALLOW_ALTERNATIVE_DELETE_ON_EXIT) {
            if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                List<String> paths;
                synchronized (lock) {
                    paths = new ArrayList<>(filesToDelete);
                    filesToDelete.clear();
                }
                if (!paths.isEmpty()) {
                    deleteImpl(execEnv, paths);
                }
            }
        }
    }
    
    public void deleteOnExit(String... paths) {
        if (ALLOW_ALTERNATIVE_DELETE_ON_EXIT) {
            synchronized (lock) {
                for (String p : paths) {
                    filesToRemember.add(p);
                }
            }
        }
    }

    private static void deleteImpl(ExecutionEnvironment execEnv, Collection<String> paths) {
        assert ALLOW_ALTERNATIVE_DELETE_ON_EXIT;
        if (paths.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String p : paths) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(p);
        }
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            return;
        }
        ProcessUtils.execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable("xargs").setArguments("rm"), sb.toString().getBytes()); // NOI18N

    }

    private static void storeDeleteOnExit(File file, Collection<String> paths) {
        assert ALLOW_ALTERNATIVE_DELETE_ON_EXIT;
        // the existence of cache root ensured in ctor
        try (PrintWriter pw = new PrintWriter(file, "UTF8")) { // NOI18N
            if (!paths.isEmpty()) {
                for (String path : paths) {
                    pw.append(path).append('\n');
                }
                pw.close();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex); // should never occur
        }
    }

    private static void loadDeleteOnExit(File file, Collection<String> pathsToAdd) {
        assert ALLOW_ALTERNATIVE_DELETE_ON_EXIT;
        // the existence of cache root ensured in ctor
        // this is called from ctor only, so it's OK to do file ops in sync block
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String path; (path = br.readLine()) != null;) {
                if (!path.isEmpty()) {
                    pathsToAdd.add(path);
                }
            }
            // line is not visible here.
        } catch (FileNotFoundException ex) {
            // nothing to do: no file is quite normal
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
