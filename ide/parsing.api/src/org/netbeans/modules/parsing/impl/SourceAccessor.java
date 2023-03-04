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

package org.netbeans.modules.parsing.impl;

import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.implspi.SchedulerControl;
import org.netbeans.modules.parsing.implspi.SourceControl;
import org.netbeans.modules.parsing.implspi.SourceEnvironment;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SourceAccessor {
    public static synchronized SourceAccessor getINSTANCE () {
        if (INSTANCE == null) {
            try {
                Class.forName("org.netbeans.modules.parsing.api.Source", true, SourceAccessor.class.getClassLoader());   //NOI18N            
                assert INSTANCE != null;
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return INSTANCE;
    }
    
    public static void setINSTANCE (SourceAccessor instance) {
        assert instance != null;
        INSTANCE = instance;
    }
    
    private static volatile SourceAccessor INSTANCE;
    
    /**
     * Sets given {@link SourceFlags} of given {@link Source}
     * @param source
     * @param flags
     */
    public abstract void setFlags (Source source, Set<SourceFlags> flags);

    /**
     * Tests if given {@link Source} has a given {@link SourceFlags}
     * @param source
     * @param flag
     * @return true if the source has given flag
     */
    public abstract boolean testFlag (Source source, SourceFlags flag);

    /**
     * Removes a given {@link SourceFlags} from a given {@link Source}
     * @param source
     * @param flag
     * @return true if the source had given flag and it was removed
     */
    public abstract boolean cleanFlag (Source source, SourceFlags flag);

    /**
     * Tests if a given {@link Source} has a given {@link SourceFlags} and cleans the
     * clean flags.
     * @param source
     * @param flag
     * @return true if the source had given flag
     */
    public abstract boolean testAndCleanFlags (Source source, SourceFlags test, Set<SourceFlags> clean);

    /**
     * Invalidates given {@link Source}
     * @param source to be invalidated
     * @param force if true source is always invalidated
     */
    public abstract void invalidate (Source source, boolean force);

    /**
     * Invalidates given {@link Source}
     * @param source to be invalidated
     * @param id
     * @param snapshot
     * @return true if the snapshot is up to date and was refreshed
     */
    public abstract boolean invalidate (Source source, long id, Snapshot snapshot);

    public abstract SourceModificationEvent getSourceModificationEvent (Source source);

    public abstract void setSourceModification (Source source, boolean sourceChanged, int startOffset, int endOffset);

    public abstract void parsed (Source source);

    @NonNull
    public abstract Map<Class<? extends Scheduler>,SchedulerEvent> createSchedulerEvents(@NonNull Source source, @NonNull Iterable<? extends Scheduler> schedulers, @NonNull SourceModificationEvent sourceModificationEvent);

    public abstract void setSchedulerEvent(@NonNull Source source, @NonNull Scheduler scheduler, @NonNull SchedulerEvent event);

    public abstract void mimeTypeMayChanged(@NonNull Source source);
    
    public abstract SchedulerEvent getSchedulerEvent (Source source, Class<? extends Scheduler> schedulerType);
    
    /**
     * Returns cached {@link Parser} when available
     * @param source for which the parser should be obtained
     * @return the {@link Parser} or null
     */
    public abstract Parser getParser (Source source);
    
    /**
     * Sets a cached {@link Parser}.
     * Used only by ParserManagerImpl
     * @param source for which the parser should be set
     * @param the parser
     * @throws IllegalStateException when the given source is already associated
     * with a parser.
     */
    public abstract void setParser (Source source, Parser parser) throws IllegalStateException;
    
    /**
     * SPI method - don't call it directly.
     * Called when Source is passed to TaskProcessor to start listening.
     * @param source to assign listeners to
     */
    public abstract void assignListeners(Source source);
    
    public abstract SourceControl getEnvControl(Source s);
    public abstract SourceEnvironment getEnv(Source s);
    
    public abstract long getLastEventId (Source source);
    
    public abstract SourceCache getCache (Source source);
    public abstract SourceCache getAndSetCache(Source source, SourceCache sourceCache);

    /**
     * SPI method - don't call it directly.
     * Called by the TaskProcessor when a new ParserResultTask is registered
     * @param source for which the task was registered
     * @return number of already registered tasks
     */
    public abstract int taskAdded (Source source);
    
    /**
     * SPI method - don't call it directly.
     * Called by the TaskProcessor when a ParserResultTask is unregistered
     * @param source for which the task was unregistered
     * @return number of still registered tasks
     */
    public abstract int taskRemoved (Source source);

    /**
     * Returns a Source for given {@link FileObject} iff it exists
     * otherwise returns null
     * @param file for which the {@link Source} should be returned.
     * @return the Source or null
     */
    public abstract Source get (final FileObject file);

    /**
     * Suppress listening on {@link Source}s created by scan.
     * @param suppress true to suppress listening
     * @param preferFiles true if files should be preferred to documents.
     */
    public abstract void suppressListening(
            boolean suppress,
            boolean preferFiles);

    public abstract int getLineStartOffset(Snapshot snapshot, int lineIdx);

    public abstract Snapshot createSnapshot(
        CharSequence        text,
        int []              lineStartOffsets,
        Source              source,
        MimePath            mimePath,
        int[][]             currentToOriginal,
        int[][]             originalToCurrent
    );
    
    public abstract void attachScheduler(Source src, SchedulerControl sched, boolean attach);

    @NonNull
    public abstract ParserEventForward getParserEventForward(@NonNull Source source);

    @CheckForNull
    public abstract Source create(@NonNull FileObject file, @NonNull String mimeType, @NonNull Lookup context);
    
    public final void init() {
        Utilities.getEnvFactory().getSchedulers(Lookup.getDefault());
    }
}
