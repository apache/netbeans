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

package org.netbeans.modules.remote.spi;

import java.util.Collection;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class FileSystemCacheProvider {

    private static final  Collection<? extends FileSystemCacheProvider> ALL_PROVIDERS =
            Lookup.getDefault().lookupAll(FileSystemCacheProvider.class);

    protected abstract String getCacheImpl(ExecutionEnvironment executionEnvironment);

    // XXX reconsider API - why not File? why /-separated on Windows?
    public static String getCacheRoot(ExecutionEnvironment executionEnvironment) {
        FileSystemCacheProvider provider = Lookup.getDefault().lookup(FileSystemCacheProvider.class);
        RemoteLogger.assertTrue(provider != null, "No FileSystemCacheProvider found"); // NOI18N
        return provider.getCacheImpl(executionEnvironment);
    }
}
