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

import java.io.IOException;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.ParserEventForward;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.event.FileChangeSupport;
import org.netbeans.modules.parsing.impl.event.ParserChangeSupport;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Connects the Source instance to its environment.It is not implemented by
 * Parsing API as it may introduce extra dependencies on file or object access
 * libraries (e.g. Data Systems API). In exchange, the Provider gets a control
 * interface, which can be used to signal source change events originating from
 * the storage/file/DataObject.
 * <p/>
 * Instance of the SourceEnvironment is referenced from the Source object. It is 
 * advised not to store Source reference, but rather extract the Source instance
 * from the {@link Source.EnvControl} to allow Source instances to be GCed. 
 * 
 * @author sdedic
 * @since 9.2
 */
public abstract class SourceEnvironment {

    /** Default reparse - sliding window for editor events*/
    private static final int DEFAULT_REPARSE_DELAY = 500;
    /** Default reparse - sliding window for focus events*/
    private static final int IMMEDIATE_REPARSE_DELAY = 10;

    private static int reparseDelay = DEFAULT_REPARSE_DELAY;
    private static int immediateReparseDelay = IMMEDIATE_REPARSE_DELAY;

    private final SourceControl sourceControl;
    private FileChangeListener fileChangeListener;
    private ChangeListener parserListener;

    /**
     * Initialized a new SourceEnvironment.
     * @param sourceControl handle to control a Source object
     */
    protected SourceEnvironment(@NonNull final SourceControl sourceControl) {
        Parameters.notNull("sourceControl", sourceControl); //NOI18N
        this.sourceControl = sourceControl;
    }
    /**
     * Reads the Document based on the Source's properties
     *
     * @param f the FIleObject
     * @param forceOpen true, if the document should be opened even though
     * it does not exist yet
     * @return the document, or {@code null} if the document was not opened.
     */
    @CheckForNull
    public abstract Document readDocument(@NonNull FileObject f, boolean forceOpen) throws IOException;
    
    /**
     * Attaches a Scheduler to the source in this environment. The implementation
     * may need to listen on certain environment objects in order to schedule
     * tasks at appropriate time.
     * <p/>
     * 
     * @param s the scheduler to attach or detach
     * @param attach if false, the scheduler is just detached from the source.
     */
    public abstract void attachScheduler(@NonNull SchedulerControl s, boolean attach);

    /**
     * Notifies that the source was actually used, and the parser wants to be
     * notified on changes through {@link SourceControl}.
     * Until this method is called, the environment implementation need not to
     * listen or forward source affecting events to the parser API.
     */
    public abstract void activate();

    /**
     * Returns true, if reparse should NOT occur immediately when source is invalidated.
     * This is used when e.g. completion is active; postpones reparsing. The SourceEnvironment
     * is responsible for calling {@link SourceControl#revalidate()} when the condition changes.
     * 
     * @return true, if a reparse should not be scheduled.
     */
    public abstract boolean isReparseBlocked();

    /**
     * Returns the {@link SourceControl}.
     * @return the {@link SourceControl}
     */
    protected final SourceControl getSourceControl() {
        return this.sourceControl;
    }

    /**
     * Starts listening on file changes.
     * Should be called from {@link SourceEnvironment#activate}
     */
    protected final void listenOnFileChanges() {
        final FileObject fo = sourceControl.getSource().getFileObject();
        if (fo != null) {
            fileChangeListener = new FileChangeSupport(sourceControl);
            fo.addFileChangeListener(FileUtil.weakFileChangeListener(this.fileChangeListener,fo));
        }
    }

    /**
     * Starts listening on {@link Source} parsers.
     * Should be called from {@link SourceEnvironment#activate}
     */
    protected final void listenOnParser() {
        final ParserEventForward peFwd = SourceAccessor.getINSTANCE().getParserEventForward(
            sourceControl.getSource());
        parserListener = new ParserChangeSupport(sourceControl);
        peFwd.addChangeListener(parserListener);
    }

    /**
     * Returns a {@link SourceEnvironment} for given {@link Source}.
     * @param source the {@link Source} to return {@link SourceEnvironment} for
     * @return the {@link SourceEnvironment}
     */
    @NonNull
    protected static SourceEnvironment forSource(@NonNull final Source source) {
        return SourceAccessor.getINSTANCE().getEnv(source);
    }

    public static int getReparseDelay(final boolean fast) {
        return fast ? immediateReparseDelay : reparseDelay;
    }

    /**
     * Sets the reparse delays.
     * Used by unit tests.
     */
    static void setReparseDelays(
        final int standardReparseDelay,
        final int fastReparseDelay) throws IllegalArgumentException {
        if (standardReparseDelay < fastReparseDelay) {
            throw new IllegalArgumentException(
                    String.format(
                        "Fast reparse delay %d > standatd reparse delay %d",    //NOI18N
                        fastReparseDelay,
                        standardReparseDelay));
        }
        immediateReparseDelay = fastReparseDelay;
        reparseDelay = standardReparseDelay;
    }
}
