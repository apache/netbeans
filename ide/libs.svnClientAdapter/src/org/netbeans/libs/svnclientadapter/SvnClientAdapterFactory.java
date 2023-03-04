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

package org.netbeans.libs.svnclientadapter;

import java.util.Collection;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;

/**
 *
 * @author Tomas Stupka
 */
public abstract class SvnClientAdapterFactory {
    
    public static final String JAVAHL_WIN32_MODULE_CODE_NAME = "org.netbeans.libs.svnjavahlwin32";
    
    protected static final Logger LOG = Logger.getLogger("org.netbeans.libs.svnclientadapter");// NOI18N
    private static SvnClientAdapterFactory instance;
    private static Client client;

    public SvnClientAdapterFactory() { }

    public enum Client {
        JAVAHL,
        SVNKIT
    }

    public static synchronized SvnClientAdapterFactory getInstance(Client client) {
        if (instance == null || SvnClientAdapterFactory.client != client) {
            instance = null;
            Collection<SvnClientAdapterFactory> cl = (Collection<SvnClientAdapterFactory>) Lookup.getDefault().lookupAll(SvnClientAdapterFactory.class);
            for (SvnClientAdapterFactory f : cl) {
                if(f.provides() == client) {
                    if(f.isAvailable()) {
                        instance = f;
                        SvnClientAdapterFactory.client = client;
                        break;
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Creates a new {@link ISVNClientAdapter} instance
     * @return
     */
    public abstract ISVNClientAdapter createClient();

    /**
     * Returns the client type provided by this factory
     * @return
     */
    protected abstract Client provides();

    /**
     * Setups the {@link SvnClientAdapterFactory}
     * @return true if the client is available, otherwise false
     */
    protected abstract boolean isAvailable();

    
}
