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

package org.netbeans.modules.parsing.implspi;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.util.Lookup;

/**
 * Factory for {@link SourceEnvironment} instances. The Factory method is given
 * a {@link Source.EnvControl} object so the created SourceEnvironment may
 * manipulate the Source state using privileged APIs.
 * <p/>
 * An instance of this Provider must be registered in the default Lookup. The first
 * instance found will be used.
 * @since 9.2
 */
public interface EnvironmentFactory {
    /**
     * Provides access to context-dependent Lookup, to multi-user environment
     * @return context-depedent Lookup
     */
    public Lookup   getContextLookup();
    
    /**
     * Parovides a class of a predefined Scheduler.
     * The predefined Schedulers are available as final field values.
     *
     * @param schedulerName
     * @return
     */
    @CheckForNull
    public Class<? extends Scheduler> findStandardScheduler(@NonNull String schedulerName);
    
    /**
     * Creates parser for the mime type
     * @param mimeType
     * @return 
     */
    public Parser   findMimeParser(Lookup context, String mimeType);
    
    public Collection<? extends Scheduler> getSchedulers(Lookup context);

    /**
     * Creates an environment for the specified Source.
     * The passed control object may be {@code null}, which means the source cannot
     * be controlled by environment and listening may not be necessary.
     *
     * @param src the Source instance
     * @param control the control object; if null, the source cannot be controlled.
     * @return the SourceEnvironment, must not be null.
     */
    @NonNull
    public SourceEnvironment createEnvironment(@NonNull Source src, @NullAllowed SourceControl control);
    
    
    /**
     * Runs a priority I/O operation. The environment may lock out or suspend 
     * some (background) activities during the priority I/O operation.
     * 
     * @param <T> type of result
     * @param r the code to execute
     * @return computed result
     * @throws Exception propagated from the executed code
     */
    public abstract <T> T runPriorityIO (final Callable<T> r) throws Exception;
}
