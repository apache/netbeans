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

package org.netbeans.modules.cnd.modelui.actions;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.modules.Places;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.sendopts.OptionProcessor.class)
public class CleanupCachePseudoOptionProcessor extends OptionProcessor {
    private static final String[] CACHES = new String[] {
                 "cnd" // NOI18N
                //,"remote-files" // NOI18N
                ,"index" // NOI18N
                //,"svnremotecache" // NOI18N
            };
    private static final String MARKER_FILE = "cnd-cleanup"; // NOI18N
            
    @Override
    protected Set<Option> getOptions() {
        for(String cache:CACHES) {
            cleanupCache(cache);
        }
        return Collections.emptySet();
    }
    
    @Override
    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
    }

    static void markToCleanup() {
        for(String cache:CACHES) {
            markToCleanup(cache);
        }
    }

    private static void markToCleanup(String cacheName) {
        File cache = Places.getCacheSubdirectory(cacheName);
        if (cache.exists()) {
            File cleanupFile = new File(cache, MARKER_FILE);
            if (!cleanupFile.exists()) {
                try {
                    cleanupFile.createNewFile();
                } catch (IOException x) {
                }
            }
        }
    }
    
    private static void cleanupCache(String cacheName) {
        File cacheDirectory = Places.getCacheDirectory();
        if (cacheDirectory.exists()) {
            File cache = new File(cacheDirectory, cacheName);
            if (cache.exists()) {
                File cleanupFile = new File(cache, MARKER_FILE); 
                if (cleanupFile.exists()) {
                    deleteRecursively(cache);
                }
            }
        }
    }

    public static void deleteRecursively(File file) {
        File [] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }
}
