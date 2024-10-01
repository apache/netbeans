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
package org.netbeans.api.project.test;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import static junit.framework.Assert.fail;
import org.netbeans.api.project.Project;
import org.openide.loaders.FolderInstance;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * Collected project testing utilities.
 * 
 * @author sdedic
 */
public final class ProjectTestUtils {
    private static final String FOLDER_PROXY_LOOKUP_NAME = "org.openide.loaders.FolderLookup$ProxyLkp"; // NOI18N
    
    /**
     * After the project is loaded, reloaded or opened, wait for the Lookup stabilization.
     * Projects use extensively FolderLookups that initialize asynchronously and fire their events
     * also asynchronously to the proxy wrappers. The implementation relies on details
     * from openide.loaders, as no API is available to wait for the process to finish/stabilize.
     * 
     * @param p the project
     * @return promise with the Lookup
     */
    public static CompletableFuture<Lookup> waitProjectLookup(Project p) {
        Class instClass = FolderInstance.class;
        Class fldLookupClass = FolderLookup.class;
        Class fldPrxClass;
        CompletableFuture<Lookup> f = new CompletableFuture<>();
        try {
            Field processor = instClass.getDeclaredField("PROCESSOR");
            fldPrxClass = fldLookupClass.getClassLoader().loadClass(fldLookupClass.getName() + "$Dispatch");
            Field dispatch = fldPrxClass.getDeclaredField("DISPATCH");
            
            processor.setAccessible(true);
            dispatch.setAccessible(true);
            
            // participants in the project lookup first initialize themselves in the PROCESSOR thread
            // then the partial Lookups themselves broadcast changes in DISPATCH thread, which provokes
            // ProxyLookup event flood. We need to wait first for PROCESSORs to complete, then
            // DISPATCH thread to complete.
            RequestProcessor.Task dispatchTask = ((RequestProcessor)dispatch.get(null)).create(()->{});
            dispatchTask.addTaskListener((e) -> {
                f.complete(p.getLookup());
            });
            ((RequestProcessor)processor.get(null)).post(() -> {
                dispatchTask.schedule(0);
            });
        } catch (ReflectiveOperationException | SecurityException ex) {
            fail("Error accessing DataSystems internals");
            // should not happen
            f.completeExceptionally(new IllegalStateException());
        }
        return f;
    }
}
