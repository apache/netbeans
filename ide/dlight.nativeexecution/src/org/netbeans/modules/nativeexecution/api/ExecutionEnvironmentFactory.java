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
package org.netbeans.modules.nativeexecution.api;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.ExecutionEnvironmentFactoryServiceImpl;
import org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * A factory for ExecutionEnvironment.
 *
 * The purpose is as follows
 * 1) share a single local execution environment
 * 2) probably remote execution environments as well
 * 3) during transitional period,
 * transform the user@host string to ExecutionEnvironment
 *
 * I guess the (3) will die some time
 * and the class will be moved to
 * org.netbeans.modules.nativeexecution.api
 *
 * @author Vladimir Kvashin
 */
public class ExecutionEnvironmentFactory {

    private static final ExecutionEnvironmentFactoryService defaultFactory =
            new ExecutionEnvironmentFactoryServiceImpl();
    private static final CopyOnWriteArrayList<ExecutionEnvironmentFactoryService> allFactories =
            new CopyOnWriteArrayList<>();
    private static final Lookup.Result<ExecutionEnvironmentFactoryService> lookupResult =
            Lookup.getDefault().lookupResult(ExecutionEnvironmentFactoryService.class);
    private static final LookupListener ll;


    static {
        ll = new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                Collection<? extends ExecutionEnvironmentFactoryService> newSet =
                        lookupResult.allInstances();
                synchronized (this) {
                    allFactories.retainAll(newSet);
                    allFactories.addAllAbsent(newSet);
                }
            }
        };

        lookupResult.addLookupListener(ll);
        ll.resultChanged(null);
    }

    /** prevents instantiation */
    private ExecutionEnvironmentFactory() {
    }

    public static ExecutionEnvironmentFactoryService getDefault() {
        return defaultFactory;
    }

    /**
     * Returns an instance of <tt>ExecutionEnvironment</tt> for local execution.
     */
    public static ExecutionEnvironment getLocal() {
        ExecutionEnvironment result;

        for (ExecutionEnvironmentFactoryService f : allFactories) {
            if ((result = f.getLocal()) != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Creates a new instance of <tt>ExecutionEnvironment</tt>. If <tt>host</tt>
     * refers to the localhost or is <tt>null</tt> then task, started in this
     * environment will be executed locally. Otherwise it will be executed
     * remotely using ssh connection to the specified host using default ssh
     * port (22).
     *
     * @param user user name to be used in this environment
     * @param host host identification string (either hostname or IP address)
     */
    public static ExecutionEnvironment createNew(String user, String host) {
        Logger.assertTrue(user != null && !user.isEmpty());
        Logger.assertTrue(host != null && !host.isEmpty());
        ExecutionEnvironment result;

        for (ExecutionEnvironmentFactoryService f : allFactories) {
            if ((result = f.createNew(user, host)) != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Creates a new instance of <tt>ExecutionEnvironment</tt>.
     * It is allowable to pass <tt>null</tt> values for <tt>user</tt> and/or
     * <tt>host</tt> params. In this case
     * <tt>System.getProperty("user.name")</tt> will be used as a username and
     * <tt>HostInfo.LOCALHOST</tt> will be used for <tt>host</tt>.
     * If sshPort == 0 and host identification string represents remote host,
     * port 22 will be used.
     *
     * @param user user name for ssh connection.
     * @param host host identification string. Either hostname or IP address.
     * @param sshPort port to be used to establish ssh connection.
     */
    public static ExecutionEnvironment createNew(String user, String host, int port) {
        Logger.assertTrue(user != null && !user.isEmpty());
        Logger.assertTrue(host != null && !host.isEmpty());
        ExecutionEnvironment result;

        for (ExecutionEnvironmentFactoryService f : allFactories) {
            if ((result = f.createNew(user, host, port)) != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Returns a string representation of the executionEnvironment,
     * so that client can store it (for example, in properties)
     * and restore later via fromUniqueID
     * either user@host or "localhost"
     */
    public static String toUniqueID(ExecutionEnvironment executionEnvironment) {
        String result;

        for (ExecutionEnvironmentFactoryService f : allFactories) {
            try {
                if ((result = f.toUniqueID(executionEnvironment)) != null) {
                    return result;
                }
            } catch (Throwable th) {
                Logger.getInstance().log(Level.FINE, "Exception in " + f.getClass().getName() + " for " + executionEnvironment.getDisplayName(), th); // NOI18N
            }
        }

        return null;
    }

    /**
     * Creates an instance of ExecutionEnvironment
     * by string that was got via toUniqueID() method
     * @param hostKey a string that was returned by toUniqueID() method.
     */
    public static ExecutionEnvironment fromUniqueID(String hostKey) {
        ExecutionEnvironment result;

        for (ExecutionEnvironmentFactoryService f : allFactories) {
            try {
                if ((result = f.fromUniqueID(hostKey)) != null) {
                    return result;
                }
            } catch (Throwable th) {
                Logger.getInstance().log(Level.FINE, "Exception in " + f.getClass().getName() + " for " + hostKey, th); // NOI18N
            }
        }

        return null;
    }
}
