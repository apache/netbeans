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

package org.netbeans.modules.parsing.spi;

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.api.ParserManager;

import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.impl.ParserAccessor;


/**
 * Represents implementation of parser for one language. 
 * Parser is always created by {@link ParserFactory} for some concrete 
 * {@link Snapshot} or collection of {@link Snapshot}s.
 * Parser fires change when some conditions are changed and sources 
 * should be reparsed. One instance of Parser can be reused for more Snapshots
 * created from one Source.
 * 
 * @author Jan Jancura
 * @author Tomas Zezula
 */
public abstract class Parser {
    
    static {
        ParserAccessor.setINSTANCE(new MyAccessor());
    }
    
    /**
     * Called by infrastructure when {@link org.netbeans.modules.parsing.api.Source}
     * is changed, and a new {@link org.netbeans.modules.parsing.api.Snapshot}
     * has been created for it. 
     * 
     * @param snapshot          A snapshot that should be parsed.
     * @param task              A task asking for parsing result.
     * @param event             A scheduler event.
     */
    public abstract void parse (
        Snapshot                snapshot,
        Task                    task,
        SourceModificationEvent event
    ) throws ParseException;
    
    /**
     * Called when some task needs some result of parsing. Task parameter 
     * contains {@link org.netbeans.modules.parsing.api.UserTask}, or 
     * {@link SchedulerTask} instance, that requests {@link Parser.Result}.
     * 
     * @param task              A task asking for parsing result.
     * @return                  Result of parsing or null.
     */
    public abstract Result getResult (
        Task                    task
    ) throws ParseException;
        
    
    /**
     * Called by the infrastructure to stop the parser operation.
     * @deprecated use {@link Parser#cancel(CancelReason, org.netbeans.modules.parsing.spi.SourceModificationEvent)}
     */
    @Deprecated
    public void cancel () {};
    
    /**
     * Called by the infrastructure to stop the parser operation.
     * @param reason of the cancel, see {@link Parser#CancelReason}
     * @param event an additional info if the reason is SOURCE_MODIFICATION_EVENT, otherwise null
     * @since 
     */
    public void cancel (@NonNull CancelReason reason, @NullAllowed SourceModificationEvent event) {}
    
    /**
     * Registers new listener.
     * 
     * @param changeListener
     *                      A change listener to be registered.
     */
    public abstract void addChangeListener (
        ChangeListener          changeListener
    );
    
    /**
     * Unregisters listener.
     * 
     * @param changeListener
     *                      A change listener to be unregistered.
     */
    public abstract void removeChangeListener (
        ChangeListener          changeListener
    );
    
    /**
     * Represents result of parsing created for one specific {@link Task}. 
     * Implementation of this class should provide AST for parsed file, 
     * semantic information, etc. A new instance of Result is created for each
     * Task. When Task execution is finished, task should be invalidated.
     */
    public abstract static class Result {
        
        private final Snapshot  snapshot;
        
        /**
         * Creates a {@link Result} for given snapshot
         * @param snapshot
         */
        protected Result (
            final Snapshot      _snapshot
        ) {
            snapshot = _snapshot;
        }
        
        /**
         * Returns a {@link Snapshot} represented by this {@link Result}
         * @return Returns a <code>Snapshot</code>, which was used for producing this
         *   parser result.
         */
        public Snapshot getSnapshot () {
            return this.snapshot;
        }
        
        /**
         * This method is called by Parsing API, when {@link Task} is finished.
         */
        protected abstract void invalidate ();

        /**
         * Return {@code true} if the parser was able to process the file fully.
         *
         * Return {@code false}, if the parser had to stop the processing (e.g. for
         * memory limit reasons), and the file should be processed again.
         *
         * @return whether file processing finished or not
         * @since 9.20
         */
        protected boolean processingFinished() {
            return true;
        }
    }
    
    /**
     * The {@link CancelReason} is passed to {@link Parser#cancel(org.netbeans.modules.parsing.spi.Parser.CancelReason, org.netbeans.modules.parsing.spi.SourceModificationEvent)}
     * as a hint. The parser may use this information to optimize the canceling.
     * @since 1.36
     */
    public enum CancelReason {
        /**
         * The cancel is called due to source modification.
         * Any information calculated by the parser is based on obsolete data,
         * the parser should stop and throw all the cached data.
         */
        SOURCE_MODIFICATION_EVENT,
        
        /**
         * The cancel is called because {@link ParserManager}'s parse method
         * was called.
         * The source used by the parser is still valid, the parser can use
         * these data during the next request.
         */
        USER_TASK,
        
        /**
         * The cancel is called because a higher priority task was added.
         * The source used by the parser is still valid, the parser can use
         * these data during next request.
         */
        PARSER_RESULT_TASK;
    }
    
    
    private static class MyAccessor extends ParserAccessor {

        @Override
        public void invalidate (
            final Result        result) {
            assert result != null;
            result.invalidate();
        }        

        @Override
        public boolean processingFinished(Result result) {
            assert result != null;
            return result.processingFinished();
        }

    }
}




